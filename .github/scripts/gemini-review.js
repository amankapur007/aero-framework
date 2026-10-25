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

  // Cap diff at 40,000 characters to stay well within limits
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

  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=${apiKey}`;

  const response = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      contents: [{ parts: [{ text: prompt }] }],
      generationConfig: {
        temperature: 0.2
      }
    })
  });

  const data = await response.json();

  if (!response.ok) {
    const errMsg = data.error?.message || `HTTP ${response.status}: ${response.statusText}`;
    console.error("Gemini API Error:", JSON.stringify(data, null, 2));
    await github.rest.issues.createComment({
      owner: context.repo.owner,
      repo: context.repo.repo,
      issue_number: context.payload.pull_request.number,
      body: `### 🤖 Gemini Code Review Failed\n\n**API Error**: \`${errMsg}\``
    });
    return;
  }

  const reviewText = data.candidates?.[0]?.content?.parts?.[0]?.text;
  const finishReason = data.candidates?.[0]?.finishReason;
  const finalComment = reviewText || `Review could not be completed. Model finish reason: **${finishReason || "UNKNOWN"}**`;

  await github.rest.issues.createComment({
    owner: context.repo.owner,
    repo: context.repo.repo,
    issue_number: context.payload.pull_request.number,
    body: `### 🤖 Gemini Code Review\n\n${finalComment}`
  });
};
