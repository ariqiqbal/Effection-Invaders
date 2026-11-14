# Test Plan – Effection Invaders

WBS 2.3.3 – Prepare and execute test cases · Jira: VFX-43 · Owner: Dian (Quality Tester)

This plan describes **what** we test, **which tool or method** we use, **when** testing happens, and **how** test cases are recorded.

---

## 1. Testing overview

| Test type | Tool / method | What it checks | When | Owner |
|---|---|---|---|---|
| Unit testing | JUnit 4 (`bash scripts/test.sh`) | Game logic in isolation (e.g. `Score`, `GameEvents`, `Cooldown`) | Every PR (CI) and locally before pushing | Dian / PR author |
| Code coverage | JaCoCo | How much of the source code the unit tests execute | Every PR (CI) | Ariq |
| Smoke testing | CI starts the game on a virtual display | The game launches and reaches the title screen without exceptions | Every PR (CI) | Ariq |
| Static analysis | CodeQL, PMD | Security issues (CodeQL) and cyclomatic complexity (PMD) | Every PR (CI) | Ariq / Sufi |
| Manual regression testing | Play-through checklist (section 5) | Existing features still work after changes | Before refactoring, after major merges, before release | Dian |

A PR can be merged only when all CI checks are green and a reviewer approves.

---

## 2. Test-writing rules

1. **Location:** store tests in `test/`, mirroring the source packages.
   `src/engine/Score.java` → `test/engine/ScoreTest.java`
2. **Naming:** test classes end with `Test`. Test methods describe the behaviour, e.g. `higherScoreComesFirst()`.
3. **One behaviour per test method.** If a test fails, its name should tell you what broke.
4. **Test logic, not drawing.** Unit tests must not open the game window. Test calculations, state and rules; check visuals with manual testing.
5. **Keep tests independent.** Clean up shared/static state in `@After` (e.g. unsubscribe from `GameEvents`).
6. **Bug fixes start with a test.** Before fixing a bug, write a test that reproduces it (it fails), then fix the code (it passes). This test stays as a regression test.
7. **Run before pushing:** `bash scripts/test.sh` must print `OK (N tests)`.

---

## 3. Test case template

Copy this template for every new test case (automated or manual).

| Field | Description |
|---|---|
| **ID** | TC-XXX |
| **Jira key** | VFX-XX |
| **Title** | Short description of what is tested |
| **Type** | Automated / Manual |
| **Preconditions** | State required before starting (branch, save files, level, etc.) |
| **Steps** | 1. … 2. … 3. … |
| **Expected result** | What should happen |
| **Actual result** | What actually happened |
| **Status** | Pass / Fail |
| **Notes** | Screenshots, logs, suspected cause, related PR |

---

## 4. Example test case – Bug 3.1.3

| Field | Description |
|---|---|
| **ID** | TC-001 |
| **Jira key** | VFX-XX (bug 3.1.3) |
| **Title** | Score drops by 100 on game over |
| **Type** | Manual (to be automated when fixed) |
| **Preconditions** | Latest `main` built and running. Start a new game at level 1. |
| **Steps** | 1. Start a new game.<br>2. Destroy some enemies and note the score shown on screen (e.g. 300).<br>3. Lose all lives without destroying more enemies.<br>4. Read the final score on the game over / score screen. |
| **Expected result** | The final score equals the score shown just before the last life was lost (e.g. 300). |
| **Actual result** | The final score is 100 lower than expected (e.g. 200). |
| **Status** | Fail |
| **Notes** | Likely cause: `GameScreen.run()` adds a life bonus with `this.score += LIFE_SCORE * (this.lives - 1);`. On game over `lives` is 0, so this adds `100 * (0 - 1) = -100`. Fix idea: only add the bonus when `lives > 0`. Per rule 6, add a unit test for the bonus calculation before fixing. |

---

## 5. Manual regression checklist

Run on the latest `main` before refactoring and before each release.

- Title menu and ESC
- Levels 1–4
- Boss levels 5 and 10
- Pause / resume / restart / quit
- Game over and score saving
- Shop: coins and diamonds
- Customization
- Items using keys 1–4
- Achievements
- High scores still saved after closing the game
- No unexpected exceptions in the console

Every failure gets a Jira Bug with a description and steps to reproduce.

---

## 6. Regression log

| Date | Tester | Commit ID | Result | Notes |
|---|---|---|---|---|
| | | | | |
| 2026-11-14 | Dian | 926dda5 | 10/12 features passed, 2 failed | All 4 known bugs reproduced (3.1.1–3.1.4), see tables below |

### Regression run 1 – detailed results (2026-11-14, commit 926dda5)

| # | Feature | Result | Notes |
|---|---|---|---|
| 1 | Title menu and ESC | Pass | Ship select, shop, high scores, settings and achievements screens open and return to the title screen |
| 2 | Levels 1–4 | Pass | All four levels cleared (scores 480 → 880 → 2080 → 2660) |
| 3 | Boss level 5 | Pass | MidBoss defeated, +1200 score as designed (2660 → 3860) |
| 4 | Boss level 10 | Pass | FinalBoss defeated, final score 6700 (6500 boss + 200 life bonus), run completed. Tested by temporarily starting at level 10 (`Core.java` line 141), change reverted |
| 5 | Pause / resume / restart / quit | **Fail** | Pause, resume, restart and quit work in normal play, but pause does not stop all timers (3.1.2) and Restart still works during the game over animation (3.1.4) |
| 6 | Game over and score saving | **Fail** | Scores are saved, but the final score is 100 too low on game over. Quitting with 0 ships destroyed gives -100, which is saved to high scores. Bug 3.1.3, see TC-001 |
| 7 | Shop: coins and diamonds | Pass | Coins collected and balance kept between sessions; meteor diamond +1; pending diamonds banked on completed run and forfeited on lost run (as designed) |
| 8 | Customization | Pass | Ship selection works (`Ship selected: STANDARD`) |
| 9 | Items using keys 1–4 | Pass | Items used from slots 1–3. The game has 3 item slots, so key 4 has no slot |
| 10 | Achievements | Pass | Achievements screen opens |
| 11 | High scores saved after closing the game | Pass | After restarting the game, user high scores and coin balance are loaded (not defaults) |
| 12 | No unexpected console exceptions | Pass | No exceptions in any session |
