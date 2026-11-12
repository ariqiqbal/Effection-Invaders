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
Any Work-in-progress commits can be written however you like (Squash merge cleans up the history anyway, but keeps the last commit).
However, please do remember **The last commit before you push** starts with its type: `<type>: <commit title> [VFX-<n>]`. Thusly:
```
test: Added JUnit Setup [VFX-43]
feat: Established rules in CONTRIBUTING [VFX-38]
```
We strongly suggest all commits to start imperatively in past tense verb. ("Add" not "Added").
!!! If any changes are made after the last commit is written, just -ammend the new changes in before pushing !!!


## Pull requests
All of our coding efforts in this project are based on one Jira task and are done through PRs. Therefore all these considerations are important for the success of our project:

- Try to aim for about 400 changed lines or fewer, with reduced Code Complexity (CC) if possible. 
- Remember to always rebase on or merge the latest `main` before requesting review. PRs with conflicts will not be reviewed.
- A good PR Title, let anyone on the team understand what the whole PR does from the title alone, even without opening it. Eg:
```
Good: `Add Jira webhook so merged PRs move tasks to Done [VFX-66]`
Bad: `fix stuff`, `VFX-43`, `update`
```
- Put the Jira task in the title and in the body of the PR: `<type>: <PR Title [VFX-<n>]`.
- Fill in the PRs template already establish. Add screenshots for anything visible.
- Once review preview process has started, push new commits instead of force-pushing, so reviewers can see what changed.
- Always and always request **1–2 reviewers**, to speed up the reviewing process.
- The author or The Reviewer will merge once the PR is approved and CI is green.
- Suggested merge method: **Squash and merge**. and do not forget to delete the branch.
- If it depends on another PR, put `Depends on: #N` on the first line of the body.



## Reviews
Reviewing process is the most crucial yet the longest process in our project. A slow reviewing process may stall other task from completing. 
Therefore, please keep in mind:
Reviewer: give a first response within 24 hours. If you can't, say so and suggest someone else.
Author: if a PR has had no review after 24 hours, pings the reviewer once, then the team channel.

Inside a review comment, always tag every comment with a priority so the author knows what's required:

| Tag | Meaning | Reviewer action |
|---|---|---|
| **P1** | Must fix: breaks behaviour or violates a rule | Request changes |
| **P2** | Should fix: strong recommendation | Request changes |
| **P3** | Consider: fix now or leave a follow-up issue | Comment |
| **P4** | Optional / nit / opinion | Approve |

These focus might ease the reviewing process. In order: 
(1) does it work, 
(2) does it touch shared files more than needed, 
(3) is it consistent with existing code, 
(4) naming conventions

!!! An approval must say **what you checked** in one line. "LGTM" on its own is not a review. !!!

## Shared files — extra care
`engine/DrawManager.java`, `engine/Core.java`, `screen/GameScreen.java` are used by every team.
Any change there must be mentioned in the PR description and reviewed by the team that owns the file (menu framework: Hello World).

## Definition of Done
- Code compiles (CI green) and the game runs
- Reviewed and approved
- Screen reachable from the menu and exits with ESC (for screen work)
- Docs/wiki updated if behaviour changed
