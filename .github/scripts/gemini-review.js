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

  // Cap diff at 40,000 characters to prevent overflow
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

  // Helper: Sleep with jitter
  const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

  // Request function with Exponential Backoff
  async function callGeminiWithBackoff(model, maxRetries = 3) {
    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      console.log(`Calling ${model} (attempt ${attempt}/${maxRetries})...`);

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: { temperature: 0.2 }
        })
      });

      const data = await response.json();

      // If successful, return the data
      if (response.ok) {
        return { success: true, data };
      }

      const status = response.status;
      const isTransient = status === 503 || status === 429 || status === 500;

      console.warn(`Attempt ${attempt} failed with HTTP ${status}: ${data.error?.message}`);

      // If it's a transient server capacity spike and we have retries left, wait and retry
      if (isTransient && attempt < maxRetries) {
        // Exponential backoff: 2s, 4s, 8s + random jitter (0-1s)
        const delay = Math.pow(2, attempt) * 1000 + Math.floor(Math.random() * 1000);
        console.log(`Temporary high demand. Retrying in ${Math.round(delay / 1000)}s...`);
        await sleep(delay);
        continue;
      }

      // If non-transient error (e.g. 400, 401, 404), do not retry
      return { success: false, status, error: data.error };
    }
  }

  // Model fallback chain: primary fast model -> secondary general model
  const candidateModels = ["gemini-3.8-flash", "gemini-2.5-pro", "gemini-1.5-flash"];
  let finalData = null;
  let lastError = null;

  for (const model of candidateModels) {
    const result = await callGeminiWithBackoff(model, 3);
    if (result.success) {
      finalData = result.data;
      break;
    }
    lastError = result.error;
    console.warn(`Model ${model} unavailable. Trying next fallback...`);
  }

  if (!finalData) {
    const errMsg = lastError?.message || "Service unavailable across all candidate models.";
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: context.payload.pull_request.number,
      body: `### 🤖 Gemini Code Review Failed\n\n**API Error**: \`${errMsg}\`\n*Google's servers are temporarily experiencing high demand. Please trigger the workflow again in a few moments.*`
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