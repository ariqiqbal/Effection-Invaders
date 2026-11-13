# Git Workflow (WBS 2.1.1)

Owner: Ariq (Git Master)

Naming rules for branches, commits and PR titles: [`CONTRIBUTING.md`](../CONTRIBUTING.md). Quality rules a PR must pass: [`docs/coding-standards.md`](coding-standards.md), [`docs/test-plan.md`](test-plan.md).

## 1.0 Strategy: GitHub Flow

We use GitHub Flow: one long-lived branch (`main`) and one short-lived branch for each Jira work item.

### 1.1 Rationale for Selecting GitHub Flow as the Version Control Strategy

- We chose GitHub Flow because it suits our eight-member team, two-week sprints, and single demo build.
- It keeps branch management simple: completed work is reviewed and merged directly into `main` through a pull request.
- A separate `develop` branch would add another integration step and require the team to keep two long-lived branches synchronized.
- Since `main` is protected and pull requests are checked by CI, a separate `develop` branch is not necessary for this project.

## 2.0 Branch Types and Naming

| Branch | Purpose |
|---|---|
| `main` | Protected integration branch containing stable, playable code |
| `feat/<JIRA-KEY>-<description>` | New features or improvements |
| `fix/<JIRA-KEY>-<description>` | Bug fixes |
| `refactor/<JIRA-KEY>-<description>` | Code improvements without behaviour changes |
| `test/<JIRA-KEY>-<description>` | Unit tests and test setup |
| `docs/<JIRA-KEY>-<description>` | Documentation changes |
| `ci/<JIRA-KEY>-<description>` | GitHub Actions and CI changes |

Note: The Jira key (e.g. `VFX-7`) links the branch to its Jira task. Use lowercase words separated by hyphens for the description.

### 2.1 Branch Rules

1. Create each branch from the latest `main`.
2. Each branch should focus on one Jira work item.
3. Keep branches short-lived (1–3 days) and merge them as soon as the task is completed and reviewed.
4. Do not create long-lived personal or phase branches.
5. After a branch is successfully merged, delete it.
6. Do not push directly to `main`; all changes must go through a pull request.
7. Keep `main` stable and playable at all times.

## 3.0 Commit Rules

### Commit Guidelines

Each commit should contain one logical change.

A commit should:

- Be related to the current Jira task.
- Compile successfully whenever possible.
- Not contain unrelated changes.
- Not include debug code, temporary files, or generated files (`out/`, `lib/`, `reports/`).
- Keep the game playable whenever possible.
- Avoid mixing multiple features, bug fixes, or documentation changes in one commit.

### Commit Message Format

Use:

```
<type>(<scope>): <short description>
```

| Type | Usage |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `refactor` | Code restructuring without behaviour changes |
| `perf` | Performance improvement |
| `test` | Add or update tests |
| `ci` | GitHub Actions and CI changes |
| `style` | Formatting only |
| `chore` | Maintenance |
| `revert` | Revert a previous commit |

Scope shows which part of the project the commit affects. It is written in lowercase inside parentheses, right after the type. The scope is optional for changes that affect the whole repository (e.g. `ci: add complexity job`).

| Scope | Area |
|---|---|
| `particles` | Particle system and particle pool |
| `explosion` | Explosion effects |
| `trail` | Bullet and movement trails |
| `events` | VFX event system |
| `game` | Game screen and game loop (`GameScreen`, `Core`) |
| `test` | Unit tests and test scripts |
| `quality` | Complexity checks and coding standards |

Examples:

```
feat(explosion): add enemy explosion effect
fix(particles): fix particle cleanup
docs(events): update effect event documentation
perf(trail): reduce bullet trail particles
refactor(game): split GameScreen.update into smaller methods
test(particles): add particle pool tests
ci: add complexity job
```

Commit messages should be short, clear, and describe what was changed.

Before committing, developers should check their changes with:

```bash
git status
git diff
```

## 4.0 Pull Request and Merging Workflow

1. Move the Jira item to **In Progress**.
2. Update `main` and create a task branch:
   ```bash
   git switch main
   git pull
   git switch -c feat/VFX-21-enemy-explosion
   ```
3. Make changes and commit them in small, logical steps:
   ```bash
   git status
   git diff
   git add <files>
   git commit -m "feat(explosion): add enemy explosion effect"
   ```
4. Run the required local checks (available after the WBS 2.3 test and quality setup is merged):
   ```bash
   bash scripts/test.sh
   bash scripts/complexity.sh
   ```
5. Fetch the latest `main` and rebase your branch:
   ```bash
   git fetch origin
   git rebase origin/main
   ```
   If there is a conflict: fix the files, then `git add <file>` and `git rebase --continue`. Not sure? Run `git rebase --abort` and ask the author of the other change. If another member also pushes to your branch, use `git merge origin/main` instead of rebase.
6. Push the branch:
   ```bash
   # First push
   git push -u origin feat/VFX-21-enemy-explosion

   # After rebasing a branch that has already been pushed
   git push --force-with-lease
   ```
7. Open a pull request with `main` as the base:
   - Example title: `feat: add enemy explosion effect [VFX-21]`
   - Complete the PR template.
   - Request one reviewer.
   - Move the Jira item to **In Review**.
8. Address review comments, push updates, and request another review if needed.
9. Merge only when all required CI checks pass, at least one reviewer approves, and all review conversations are resolved. Use **Create a merge commit**, so `main` keeps every commit of the branch. Because all commits stay in history, each commit message must follow the commit format in section 3.0.
10. Delete the remote branch after merging and move the Jira item to **Done**.
11. Update the local repository:
    ```bash
    git switch main
    git pull
    git branch -d feat/VFX-21-enemy-explosion
    git fetch --prune
    ```

## 5.0 Sprint-End Tags

At the end of each sprint, the Git Master tags the exact `main` commit demonstrated during the sprint review. This makes it possible to revisit the demonstrated version after `main` changes.

| Purpose | Example |
|---|---|
| Check out the demonstrated version | `git switch --detach sprint-1` |
| Compare sprint versions | `git diff sprint-1 sprint-2 --stat` |
| Compare complexity | Run `bash scripts/complexity.sh` at each tag |
| Find sprint evidence | View the tag and commit in GitHub → Tags |

Planned tags:

- `sprint-1` – November 22
- `sprint-2` – December 6 (final presentation build)

Git Master procedure:

```bash
git switch main
git pull
git tag -a sprint-1 -m "Sprint 1 review"
git push origin sprint-1
```

Use the appropriate sprint number when creating later tags.

## 6.0 Team Agreement

This workflow was presented to the team and agreed in the team group chat on 2026-11-13.

| Member | Role | Agreed |
|---|---|---|
| Elisya | PM | [✅] |
| Alya | Scrum/Jira Master | [✅] |
| Nursofia | Risk Analyst / Dev | [✅] |
| Najihah | Estimation Lead / Dev | [✅] |
| Ariq | Git Master | [✅] |
| Khairul Suffie | Quality Lead | [✅] |
| Dian Melissa | Quality Tester | [✅] |
| Maisarah | Report Lead | [✅] |

Changes to this workflow: open a pull request that changes this file, approved by the Git Master and the PM.
