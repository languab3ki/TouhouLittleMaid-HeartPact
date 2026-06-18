import { readdirSync, readFileSync, statSync } from "node:fs";
import { extname, join, relative } from "node:path";

// Chinese encoding audit:
// - Read files as UTF-8 with Node, so PowerShell code page display does not affect detection.
// - Report replacement characters, suspicious question-mark runs, and common mojibake fragments.
// - This script only reports; it never rewrites source or resource files.

const root = process.cwd();
const ignoredDirs = new Set([".git", ".gradle", "build", ".idea", ".vscode"]);
const textExtensions = new Set([
  ".java", ".json", ".toml", ".properties", ".md", ".txt", ".mcmeta",
  ".cfg", ".csv", ".xml", ".yml", ".yaml", ".mjs"
]);

const mojibakePatterns = [
  { name: "replacement-char", re: /\uFFFD/g },
  { name: "question-runs", re: /\?{3,}/g },
  {
    name: "gbk-mojibake",
    re: /(?:\u95c1[\u54c4\u544a\u6c47\u9773\u7ed8]|\u5a75[\u70b2\u728\u72b3]|\u6fde[\u5b58\u6216]|\u95bb[\u719f\u72b3]|\u9435|\u9286|\u951b|\u9205|\u6d93.|\u6d60.|\u6fc2.|\u7c8f|\u52ec|\u64b6|\u719a|\u6c2.|\u579a|\u682b|\u72b2)/g
  }
];

function isTextFile(file) {
  if (file.endsWith(".mixins.json")) {
    return true;
  }
  return textExtensions.has(extname(file));
}

function walk(dir, files = []) {
  for (const name of readdirSync(dir)) {
    if (ignoredDirs.has(name)) {
      continue;
    }
    const file = join(dir, name);
    const stat = statSync(file);
    if (stat.isDirectory()) {
      walk(file, files);
    } else if (isTextFile(file)) {
      files.push(file);
    }
  }
  return files;
}

const findings = [];
for (const file of walk(root)) {
  const rel = relative(root, file).replaceAll("\\", "/");
  const text = readFileSync(file, "utf8");
  const lines = text.split(/\r?\n/);
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (rel === "scripts/check-mojibake.mjs") {
      continue;
    }
    if (rel === "PORTING_PLAN_1.21_NEOFORGE.md" && line.includes("脚本扫描")) {
      continue;
    }
    for (const pattern of mojibakePatterns) {
      pattern.re.lastIndex = 0;
      if (pattern.re.test(line)) {
        findings.push({
          file: rel,
          line: i + 1,
          type: pattern.name,
          text: line.trim().slice(0, 180)
        });
      }
    }
  }
}

if (findings.length === 0) {
  console.log("No obvious mojibake or question-mark runs found.");
  process.exit(0);
}

console.log(`Found ${findings.length} suspicious encoding issue(s):`);
for (const finding of findings.slice(0, 300)) {
  console.log(`${finding.file}:${finding.line} [${finding.type}] ${finding.text}`);
}
if (findings.length > 300) {
  console.log(`Only first 300 findings shown; ${findings.length - 300} more hidden.`);
}
process.exit(1);
