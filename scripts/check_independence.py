"""Reject application coupling and local binary dependencies in tracked source."""

from pathlib import Path
import re
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
tracked = subprocess.check_output(
    ["git", "ls-files", "-z"], cwd=root
).decode("utf-8").split("\0")
allowed_imports = (
    "kotlin.", "kotlinx.serialization.", "com.tencent.kuikly.core.",
    "io.github.nobbyyinchen.kuikly.",
)
allowed_modules = {"kuikly-toolkit", "kuikly-lenient-serialization", "sample", "android-demo"}
violations = []

for relative in tracked:
    if not relative or not relative.endswith((".kt", ".kts", ".properties")):
        continue
    path = root / relative
    text = path.read_text(encoding="utf-8")
    for line_number, line in enumerate(text.splitlines(), 1):
        if relative.endswith(".kt"):
            match = re.match(r"\s*import\s+([^\s;]+)", line)
            host_import = relative.startswith("android-demo/src/") and match and match[1].startswith(("android.", "java."))
            if match and not host_import and not match[1].startswith(allowed_imports):
                violations.append(f"{relative}:{line_number}: application import")
        if re.search(r"(?:flatDir|mavenLocal|includeBuild|files)\s*\(", line):
            violations.append(f"{relative}:{line_number}: local dependency source")
        if re.search(r"(?<![A-Za-z])(?:[A-Za-z]:[/\\]|/(?:Users|home|tmp)/)", line):
            violations.append(f"{relative}:{line_number}: absolute filesystem path")
        if re.search(r"https?://(?:localhost|127\.|10\.|192\.168\.|172\.(?:1[6-9]|2\d|3[01])\.)", line):
            violations.append(f"{relative}:{line_number}: private repository endpoint")
        for module in re.findall(r'project\("\:([^"\)]+)"\)', line):
            if module not in allowed_modules:
                violations.append(f"{relative}:{line_number}: external project dependency")

if violations:
    print("\n".join(violations), file=sys.stderr)
    sys.exit(1)
print("Standalone boundaries verified: public imports and repository dependencies only.")
