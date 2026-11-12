#!/usr/bin/env bash
# Compiles the game and the JUnit tests, then runs every test.
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

# --- 3. Start clean ---
rm -rf out/main out/test
mkdir -p out/main out/test

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

# --- 7. Run all tests. Prints "OK (N tests)" or exits with an error ---
echo "Running tests..."
java -cp "out/main${SEP}out/test${SEP}res${SEP}${JUNIT}" org.junit.runner.JUnitCore $CLASSES