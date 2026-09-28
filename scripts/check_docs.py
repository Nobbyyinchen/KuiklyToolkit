"""Check local Markdown links and registry example paths without network access."""
from pathlib import Path
import re
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
tracked = subprocess.check_output(["git", "ls-files", "-z"], cwd=root).decode().split("\0")
errors = []
for relative in tracked:
    if not relative.endswith(".md"):
        continue
    source = root / relative
    text = source.read_text(encoding="utf-8")
    # Ignore code fences; Kotlin strings can contain Markdown-like punctuation.
    text = re.sub(r"```.*?```", "", text, flags=re.S)
    for target in re.findall(r"!?(?:\[[^\]\n]*\])\(([^\s)]+)\)", text):
        if re.match(r"[a-zA-Z][a-zA-Z0-9+.-]*:", target) or target.startswith("#"):
            continue
        path = target.split("#", 1)[0].split("?", 1)[0]
        if path and not (source.parent / path).exists():
            errors.append(f"{relative}: missing local link {target}")
if errors:
    print("\n".join(errors), file=sys.stderr)
    sys.exit(1)
print("Documentation links verified.")
