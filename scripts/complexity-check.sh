#!/usr/bin/env bash
# Cyclomatic complexity check with PMD 7.7.0.
# Lists methods with CC > 10 and fails if any method exceeds MAX_CC.
set -euo pipefail

MAX_CC=40   # current highest value (GameScreen.update); stops complexity from growing

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PMD="$ROOT/lib/pmd-bin-7.29.0/bin/pmd"
RAW="$ROOT/reports/pmd-raw.csv"
OUT="$ROOT/reports/complexity.csv"
mkdir -p "$ROOT/reports"

"$PMD" check -d "$ROOT/src" -R "$ROOT/config/pmd-complexity.xml" \
  -f csv -r "$RAW" --no-fail-on-violation --no-progress

# PMD CSV: "Problem","Package","File","Priority","Line","Description","Rule set","Rule"
echo "CC,Method,Line" > "$OUT"
awk -F'","' 'NR>1 && $8 ~ /CyclomaticComplexity/ {
    desc=$6
    match(desc, /\047[^\047]*\047/); m=substr(desc, RSTART+1, RLENGTH-2)
    cc=desc; sub(/.* of /, "", cc); sub(/[^0-9].*/, "", cc)
    f=$3; gsub(/\\/, "/", f); sub(/.*\//, "", f); sub(/\.java$/, "", f)
    print cc "," f "." m "," $5
}' "$RAW" | sort -t, -k1,1nr >> "$OUT"

COUNT=$(( $(wc -l < "$OUT") - 1 ))
HIGHEST=$(sed -n 2p "$OUT" | cut -d, -f1)

echo "Methods with CC > 10: $COUNT   (MAX_CC = $MAX_CC)"
echo "Top 10:"
head -n 11 "$OUT" | column -t -s,

if [ "$COUNT" -gt 0 ] && [ "$HIGHEST" -gt "$MAX_CC" ]; then
  echo "FAIL: highest CC is $HIGHEST, above MAX_CC $MAX_CC"
  exit 1
fi
echo "OK"