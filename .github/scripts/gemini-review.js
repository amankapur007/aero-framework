module.exports = async ({ github, context }) => {
  const fs = require('fs');

  if (!fs.existsSync('pr_diff.txt')) {
    console.log("No diff file found.");
    return;
  }

  const diff = fs.readFileSync('pr_diff.txt', 'utf8');
  if (!diff.trim()) {
    console.log("Empty diff; no changes to review.");
    return;
  }

  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new Error("Missing GEMINI_API_KEY in environment.");
  }

  // Cap diff at 40,000 characters to stay within safety boundaries
  const trimmedDiff = diff.slice(0, 40000);

  const prompt = [
    "You are an expert senior code reviewer. Review the following Git pull request diff.",
    "Provide actionable, constructive feedback:",
    "1. Summary: What is this PR doing?",
    "2. Critical Issues: Bugs, null checks, edge cases, race conditions, memory/resource leaks.",
    "3. Security: Injection risks, sensitive leaks, unsafe operations.",
    "4. Suggestions: Performance tweaks or cleaner idiomatic alternatives.",
    "",
    "Diff:",
    "```diff",
    trimmedDiff,
    "```"
  ].join("\n");

  const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

  async function callGeminiWithBackoff(model, maxRetries = 2) {
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      console.log(`Calling ${model} (attempt ${attempt}/${maxRetries})...`);

      try {
        // Enforce a 45-second timeout so stalled requests fail fast and retry
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 45000);

        const response = await fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          signal: controller.signal,
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: { temperature: 0.2 }
          })
        });

        clearTimeout(timeoutId);
        const data = await response.json();

        if (response.ok) {
          return { success: true, data };
        }

        const status = response.status;
        const isTransient = status === 503 || status === 429 || status === 500;
        console.warn(`Attempt ${attempt} failed with HTTP ${status}: ${data.error?.message}`);

        if (isTransient && attempt < maxRetries) {
          const delay = Math.pow(2, attempt) * 1500 + Math.floor(Math.random() * 1000);
          console.log(`Temporary server load. Retrying ${model} in ${Math.round(delay / 1000)}s...`);
          await sleep(delay);
          continue;
        }

        return { success: false, status, error: data.error };
      } catch (err) {
        console.warn(`Attempt ${attempt} network/timeout error: ${err.message}`);
        if (attempt < maxRetries) {
          await sleep(2000);
          continue;
        }
        return { success: false, status: 408, error: { message: err.message } };
      }
    }
  }

  // Active generation models suggested by the API
  const candidateModels = ["gemini-3.8-flash","gemini-3.8-flash-lite"];
  let finalData = null;
  let lastError = null;

  for (const model of candidateModels) {
    const result = await callGeminiWithBackoff(model, 2);
    if (result.success) {
      finalData = result.data;
      break;
    }
    lastError = result.error;
    console.warn(`Model ${model} unavailable. Falling back to next available model...`);
  }

  if (!finalData) {
    const errMsg = lastError?.message || "All candidate models returned high demand / unavailable.";
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: context.payload.pull_request.number,
      body: `### 🤖 Gemini Code Review Skipped\n\n**API Notice**: \`${errMsg}\`\n*Google's API endpoints are currently saturated. Trigger the workflow manually from the PR Actions tab once demand stabilizes.*`
    });
    return;
  }

  const reviewText = finalData.candidates?.[0]?.content?.parts?.[0]?.text;
  const finishReason = finalData.candidates?.[0]?.finishReason;
  const finalComment = reviewText || `Review could not be completed. Model finish reason: **${finishReason || "UNKNOWN"}**`;

  await github.rest.issues.createComment({
    owner: context.repo.owner,
    repo: context.repo.repo,
    issue_number: context.payload.pull_request.number,
    body: `### 🤖 Gemini Code Review\n\n${finalComment}`
  });
};