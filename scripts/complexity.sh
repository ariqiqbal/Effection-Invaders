#!/usr/bin/env bash
# Cyclomatic complexity check with PMD 7.7.0 (runs in Git Bash on Windows).
# Lists methods with CC > 10 and fails if any method exceeds MAX_CC.
set -euo pipefail

MAX_CC=40   # current highest value (GameScreen.update); stops complexity from growing

cd "$(dirname "$0")/.."                 # work from the repo root with relative paths
PMD="lib/pmd-bin-7.29.0/bin/pmd"
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) PMD="$PMD.bat" ;; esac
mkdir -p reports

"$PMD" check -d src -R config/pmd-complexity.xml \
  -f csv -r reports/pmd-raw.csv --no-fail-on-violation --no-progress

# PMD CSV: "Problem","Package","File","Priority","Line","Description","Rule set","Rule"
echo "CC,Method,Line" > reports/complexity.csv
tr -d '\r' < reports/pmd-raw.csv | awk -F'","' 'NR>1 && $8 ~ /CyclomaticComplexity/ {
    desc=$6
    match(desc, /\047[^\047]*\047/); m=substr(desc, RSTART+1, RLENGTH-2)
    cc=desc; sub(/.* of /, "", cc); sub(/[^0-9].*/, "", cc)
    f=$3; gsub(/\\/, "/", f); sub(/.*\//, "", f); sub(/\.java$/, "", f)
    print cc "," f "." m "," $5
}' | sort -t, -k1,1nr >> reports/complexity.csv

COUNT=$(( $(wc -l < reports/complexity.csv) - 1 ))
HIGHEST=$(sed -n 2p reports/complexity.csv | cut -d, -f1)

echo "Methods with CC > 10: $COUNT   (MAX_CC = $MAX_CC)"
echo "Top 10:"
head -n 11 reports/complexity.csv | awk -F, '{ printf "%-5s %-50s %s\n", $1, $2, $3 }'

if [ "$COUNT" -gt 0 ] && [ "${HIGHEST:-0}" -gt "$MAX_CC" ]; then
  echo "FAIL: highest CC is $HIGHEST, above MAX_CC $MAX_CC"
  exit 1
fi
echo "OK"