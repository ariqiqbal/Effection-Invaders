#!/usr/bin/env bash
# Compiles the game and the JUnit tests, runs every test, and measures
# code coverage with JaCoCo (report: reports/coverage/index.html).
# Usage (from the repo root): bash scripts/test.sh
set -e
cd "$(dirname "$0")/.."

# --- 1. Classpath separator: ";" on Windows Git Bash, ":" on Mac/Linux ---
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
  *)                    SEP=":" ;;
esac

# --- 2. Get JUnit + Hamcrest if missing (lib/ is not committed) ---
mkdir -p lib
download() {
  if [ ! -f "lib/$1" ]; then
    echo "Downloading $1..."
    curl -fsSL -o "lib/$1" "$2"
  fi
}
download junit-4.13.2.jar      https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
download hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
JUNIT="lib/junit-4.13.2.jar${SEP}lib/hamcrest-core-1.3.jar"
# JaCoCo: the agent records which lines the tests run, the CLI writes the report
download jacocoagent-0.8.14.jar https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.14/org.jacoco.agent-0.8.14-runtime.jar
download jacococli-0.8.14.jar   https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.14/org.jacoco.cli-0.8.14-nodeps.jar

# --- 3. Start clean ---
rm -rf out/main out/test reports/coverage reports/jacoco.exec
mkdir -p out/main out/test reports

# --- 4. Compile the game into out/main ---
echo "Compiling game source..."
find src -name "*.java" > out/sources.txt
javac -encoding UTF-8 -d out/main @out/sources.txt

# --- 5. Find and compile tests (test/**/*Test.java) into out/test ---
find test -name "*Test.java" > out/tests.txt
if [ ! -s out/tests.txt ]; then
  echo "No test files found (test/**/*Test.java)."
  exit 1
fi
echo "Compiling tests..."
javac -encoding UTF-8 -d out/test -cp "out/main${SEP}${JUNIT}" @out/tests.txt

# --- 6. Turn file paths into class names: test/engine/ScoreTest.java -> engine.ScoreTest ---
CLASSES=$(sed -e 's#^test/##' -e 's#\.java$##' -e 's#/#.#g' out/tests.txt)

# --- 7. Run all tests with the JaCoCo agent. Prints "OK (N tests)" or exits with an error ---
echo "Running tests..."
java -javaagent:lib/jacocoagent-0.8.14.jar=destfile=reports/jacoco.exec \
  -cp "out/main${SEP}out/test${SEP}res${SEP}${JUNIT}" org.junit.runner.JUnitCore $CLASSES

# --- 8. Coverage report: HTML for people, CSV for the summary line ---
echo "Writing coverage report..."
java -jar lib/jacococli-0.8.14.jar report reports/jacoco.exec \
  --classfiles out/main --sourcefiles src \
  --html reports/coverage --csv reports/coverage.csv --quiet

# CSV columns 8 and 9 = LINE_MISSED, LINE_COVERED
awk -F, 'NR > 1 { missed += $8; covered += $9 }
  END { total = missed + covered
        printf "Line coverage: %d / %d lines (%.1f%%)\n", covered, total, total ? 100 * covered / total : 0 }' reports/coverage.csv
echo "Coverage report: reports/coverage/index.html"
