#!/usr/bin/env bash
# WBS 3.8.1 - list every font created outside engine/GameFonts.java.
# Usage (repo root): bash scripts/find-hardcoded-fonts.sh
# Exit code 1 when something is found, so it can run in CI.

set -u
hits=$(grep -rn --include='*.java' -E 'new Font\(|Font\.MONOSPACED|"(Arial|Courier|SansSerif|Serif|Dialog)"' src \
       | grep -v 'src/engine/GameFonts.java' || true)

if [ -z "$hits" ]; then
  echo "OK: all fonts go through GameFonts."
  exit 0
fi

echo "Hard-coded fonts found (replace with GameFonts / UiFont):"
echo "$hits"
exit 1
