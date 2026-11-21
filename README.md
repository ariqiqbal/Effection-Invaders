
# Effection Invaders

> Course: Software Development Practices (CSE2024) · 2026-2 · Section 23621 · Team **Effection**

A Java [Space Invaders](https://en.wikipedia.org/wiki/Space_Invaders) game, based on [RobertoIA/Invaders](https://github.com/RobertoIA/Invaders) and extended during the course. In this second project, team Effection improves the game (WBS 3.0) with a managed development process: Jira (space **VFX**), GitHub Flow, code review, automated tests and CI.

What we build and why: [`Effection.md`](Effection.md)

## Team

| Group | Name | Role | GitHub |
|---|---|---|---|
| Project Management | Elisya Natasha | Project Manager | [deluluclover](https://github.com/deluluclover) |
| Project Management | Alya Fatihah | Scrum / Jira Master | [alyafths](https://github.com/alyafths) |
| Development & Technical | NurSofia | Risk Analyst, Developer | [sofiajourke](https://github.com/sofiajourke) |
| Development & Technical | Najihah | Estimation Lead, Developer | [najihahs](https://github.com/najihahs) |
| Development & Technical | Ariq Iqbal | Git Master | [ariqiqbal](https://github.com/ariqiqbal) |
| Quality Assurance | Khairul Suffie | Quality Lead | [KaiSuf](https://github.com/KaiSuf) |
| Quality Assurance | Dian Melissa | Quality Tester | [dianpines](https://github.com/dianpines) |
| Quality Assurance | Maisarah | Report Lead, Developer | [maisarah-mg](https://github.com/maisarah-mg) |

## Second-Half Goals

In the first half of the course, ten teams each added one feature to a shared repository. In the second half, team Effection works alone, like a small software company: we decide what to improve, plan it in Jira, and deliver it through our own process.

**Game improvements (WBS 3.0)**

| Area | Goal |
|---|---|
| 3.1 Bug fixes | Fix the level-skip, pause, game-over score and level-restart bugs |
| 3.2 Sound | New lobby sounds, in-game sound effects, boss music, volume in the pause menu |
| 3.3 Gameplay | Tutorial in the main menu, a better boss fight, destructible shields, new enemy types |
| 3.4 Items | Item icons, a readable item HUD, an item guide, feedback when an item is used |
| 3.5 Code refactoring | Clear package structure and variable names |
| 3.6 Currency | Fix the diamond exploit, "+1" coin popup, a clear risk / cash-out choice |
| 3.7 Font and UI | Readable fonts, no overlapping text, an animated gameplay background |
| 3.8 Player profile | One save file, a better end-of-run score screen, multiple player profiles |

**Process goals**

- Plan and track all work in Jira with Scrum (sprints, story points, sprint reviews).
- Every change goes through a reviewed pull request with green CI checks (tests, coverage, complexity, security).
- Improve code quality: more unit tests and lower cyclomatic complexity than the baseline (0.5 % line coverage, highest method CC 40 on 2026-11-17).

## Screenshots

| Title Screen | Game Screen | Score Screen |
| :---: | :---: | :---: |
| ![Space Invaders title screen](https://user-images.githubusercontent.com/69495129/136980139-7ad6adab-3f11-4711-b0a6-341080aa3361.png) | ![Space Invaders game screen](https://user-images.githubusercontent.com/69495129/136980236-c5d9ef85-f09a-47a7-b9d9-948f7b624002.png) | ![Space Invaders score screen](https://user-images.githubusercontent.com/69495129/136980681-93dcadaf-08cb-48d8-90c9-68c651a115c9.png) |

## Getting Started

Requirements: JDK 17 or newer (CI uses Temurin 17), Git. On Windows, use **Git Bash** for the commands and scripts.

```bash
git clone https://github.com/ariqiqbal/Effection-Invaders.git
cd Effection-Invaders
git config commit.template .gitmessage.txt
```

Build and run (from the repository root):

| OS | Command |
|---|---|
| Windows (Git Bash) | `javac -encoding UTF-8 -d out $(find src -name "*.java") && java -cp "out;res" engine.Core` |
| macOS / Linux | `javac -encoding UTF-8 -d out $(find src -name "*.java") && java -cp out:res engine.Core` |

Recommended IDE: IntelliJ IDEA.

## Code Structure

| Package | Contents |
|---|---|
| `engine` | Game loop (`Core`), drawing (`DrawManager`), input, files, settings, events |
| `engine.vfx` | Visual effects: trails, lightning chain, screen shake, warp-in, meteors, popups |
| `engine.currency` | Coins, diamonds, economy settings, purchases |
| `engine.achievement` | Achievements and the player profile |
| `engine.soundEngine` | Music and sound effects (Java Sound) |
| `screen` | One class per screen: title, game, shop, settings, scores, ... |
| `entity` | Ship, enemies, bullets, coins, meteors |
| `item`, `shop` | Item system and shop catalogue |
| `test/` | JUnit unit tests (`scripts/test.sh`) |
| `res/` | Graphics, fonts, sounds, settings files |

## Tests and Code Quality

Run both before you open a pull request. CI runs the same scripts on every pull request.

```bash
bash scripts/test.sh               # JUnit tests + JaCoCo coverage → reports/coverage/index.html
bash scripts/complexity-check.sh   # PMD cyclomatic complexity     → reports/complexity.csv
```

The test script downloads JUnit and JaCoCo into `lib/` on the first run. The complexity script needs PMD 7.29.0 in `lib/pmd-bin-7.29.0/` ([download](https://github.com/pmd/pmd/releases/tag/pmd_releases%2F7.29.0)). `lib/` and `reports/` are not committed.

| CI check | What it does |
|---|---|
| `build` | Compiles the game |
| `smoke` | Starts the game on a virtual display; fails on an exception |
| `test` | Runs the unit tests with code coverage |
| `complexity` | Lists methods with CC > 10; fails if one is above `MAX_CC` |
| `CodeQL` | Static security analysis of the Java code and the workflows |
| `PR naming` | Checks the branch name, commit messages and PR title |

## How We Work

| Topic | Document |
|---|---|
| Branches, commits, merging, sprint tags | [`docs/git-workflow.md`](docs/git-workflow.md) |
| Pull requests, reviews, Definition of Done | [`CONTRIBUTING.md`](CONTRIBUTING.md) |
| Test plan, test case template, regression log | [`docs/test-plan.md`](docs/test-plan.md) |
| Who reviews which files | [`.github/CODEOWNERS`](.github/CODEOWNERS) |

Every change starts from a Jira work item in the **VFX** space and reaches `main` only through a reviewed pull request with green checks.

## Attribution and Licensing

This course project is based on [RobertoIA/Invaders](https://github.com/RobertoIA/Invaders). The upstream repository does not currently declare an open-source license. This repository therefore does not assert a new open-source license over the upstream code or third-party assets. Any permission or licensing terms confirmed by the relevant rights holders should be documented here before redistribution beyond the permissions provided through GitHub.

Third-party assets added by team Effection:

| Asset | Source | License |
|---|---|---|
| Game font Silkscreen | `res/silkscreen/` | SIL Open Font License 1.1 (`res/silkscreen/OFL.txt`) |
| `res/sounds/bgm_title.wav` | "Short chiptune loop" by 2D_PlatformerGuy, [OpenGameArt](https://opengameart.org/content/short-chiptune-loop) | CC0 |
| `res/sounds/menu_move.wav`, `menu_select.wav`, `menu_cancel.wav` | [Kenney Interface Sounds](https://kenney.nl/assets/interface-sounds) | CC0 |

Add every new asset to this table in the same pull request. Verify the licenses of other assets separately before bundling or redistributing them.
