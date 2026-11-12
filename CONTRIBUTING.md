# Contributing
This file establish all the rules every one must follow before starting any contribution into our project. A quick 2 min read should satisfy all the necessary informations to start contributing. 


## Branches
Our `main` branch is always protected. No direct pushes are allowed and any changes must arrive only through PRs.

Format: `<type>/VFX-<n>_<title>`. One branch per Jira task. Always delete after merge to trim dead branches.
- `<type>`: one of the types listed below.
- `VFX-<n>`: the Jira task this branch belongs to.
- `<title>`: a short, meaningful title in lowercase & multiple words with spaces are joined with hyphens (-).

Branch Types:
| Type | Purpose |
|---|---|
| **feat** | New features or improvements |
| **fix** | Code improvements without behaviour changes |
| **test** | Unit tests and test setup |
| **docs** | Documentation changes |
| **ci** | GitHub Actions and CI changes |

Eg:
```
feat/VFX-43_jira-setup
fix/VFX-38_jode-rules
```
!!! Keep branches short-lived. If a branch is older than a week, split it or merge what's done. !!!


## Commits
Any Work-in-progress commits can be written however you like.
However, please do remember **The last commit before you push** starts with its type: `<type>: <commit title> [VFX-n]`. Thusly:
```
test: Added JUnit Setup [VFX-43]
feat: Established rules in CONTRIBUTING [VFX-38]
```
We strongly suggest all commits to start imperatively in past tense verb. ("Add" not "Added").
!!! If any changes are made after the last commit is written, just -ammend the new changes in before pushing !!!


## Pull requests
- One feature per PR. Target ~400 changed lines or less.
- Fill the template. Screenshots for anything visible.
- Base branch: **your team fork `main`** for daily work. Upstream only when a requirement is complete and after the leaders' channel has been told.
- Rebase/merge latest `main` before requesting review. PRs with conflicts are not reviewed.
- Request **1–2 reviewers**, not everyone.
- Merge method: **Squash and merge**. Delete the branch.
- **Upstream PRs:** title `type(requirement): subject` (e.g. `feat(main-menu): ...`) so the shared PR list shows which team it is. If it depends on another team's PR, put `Depends on: #N` on the first line of the body.

## Reviews
Tag every comment with a priority so the author knows what is required:

| Tag | Meaning | Reviewer action |
|---|---|---|
| **P1** | Must fix — breaks behaviour or violates a rule | Request changes |
| **P2** | Should fix — strong recommendation | Request changes |
| **P3** | Consider — fix now or leave a follow-up note | Comment |
| **P4** | Optional | Approve |
| **P5** | Nit / opinion | Approve |

Review focus, in order: (1) does it work, (2) does it touch shared files more than needed, (3) consistency with existing code, (4) naming.

Approval must say **what you checked** in one line. "LGTM" alone is not a review.

## Shared files — extra care
`engine/DrawManager.java`, `engine/Core.java`, `screen/GameScreen.java` are used by every team.
Any change there must be mentioned in the PR description and reviewed by the team that owns the file (menu framework: Hello World).

## Definition of Done
- Code compiles (CI green) and the game runs
- Reviewed and approved
- Screen reachable from the menu and exits with ESC (for screen work)
- Docs/wiki updated if behaviour changed
