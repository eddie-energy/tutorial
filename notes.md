[Concept Note](https://fhooe.sharepoint.com/:w:/r/teams/tm-EDDIE--FHO-fue/Freigegebene%20Dokumente/General/EDDIE/My%20first%2021%20days%20with%20EDDIE.docx?d=w64b12c62bd844a94ae838b9c567e7948&csf=1&web=1&e=8JZCeI)

## Tasks 📝

- [x] Check state of example app
- [x] Establish timeline and tasks
- [ ] Define scope of the example app
- [ ] Start work in the example app repo while making notes for the guide
- [ ] Which tasks can we delegate
- [ ] Set up repo and define structure
    - [ ] README
    - [ ] CONTRIBUTING

## Questions ❓

### Vue or Angular?

**Vue** is

- our framework of choice allowing us to improve the example app faster
- easy to read without knowledge of the framework

**Angular** is

- highly stable and a common combination with and modular in a way similar to Spring

**React** while common is very _noisy_ for a framework-agnostic guide.
**Svelte** is not popular enough and harder to read without knowledge of the framework.

### Guide and example app in same repository?

- **Shared** makes it easier to keep them in sync
    - can reduce work by removing cross-repo coordination
    - final checkpoint naturally matches example app
    - example app polish has to be included in guide steps
- **Separate** favors independent development ✅
    - easier to polish, separate versioning
    - guide result drifts from example app

Not placing it in `eddie-energy/eddie` as it creates a lot of noise.

## Overview  and Timeline 📆

- Est. -> Estimate in hours
- Impl. -> Implemented ✔
- Doc. -> Documented ✔

| Day | Topic                                                  | Est.  | Impl. | Doc. |
|-----|--------------------------------------------------------|-------|-------|------|
|     | Preparation, Research, Repo                            | 8h    | ✔     |      |
| 1   | Set up EDDIE with Simulation Connector                 | 5h    | ✔     | ✔    |
| 2   | Set up real Region Connectors                          | 4h    |       |      |
| 3   | Explain Outbound Connectors                            | 5h    |       |      |
| 4   | Explain permission life-cycle, terminate via API       | 3h    |       |      |
| 5   | Set up application (Spring, Angular, Register, Button) | 12h   |       |      |
| 6   | Map connection ID to user and request AP + VHD         | 4h    |       |      |
| 7   | Persist data and explain raw data                      | 8h    |       |      |
| 8   | Future data, find gaps, retransmit                     | 6h    |       |      |
| 9   | OpenTelemetry logging and tracing                      | 8h    |       |      |
| 10  | CIM client library                                     | 6h    |       |      |
| 11  | Admin Console                                          | 8h    |       |      |
| 12  | Time series real-time chart                            | 4h    |       |      |
| 13  | Enrich with carbon intensity from external             | 8h?   |       |      |
| 14  | Set up AIIDA with DN, explain data sources             | 16h   |       |      |
| 15  | Consume, persist, visualize real-time data             | 5h    |       |      |
| 16  | Send IoT commands to AIIDA -> inbound / outbound       | 20h   |       |      |
| 17  | React to domain events, explain event-driven           | 8h    |       |      |
| 18  | Host multiple eligible parties                         | 16h?? |       |      |
| 19  | Explore security                                       | 8h??  |       |      |
| 20  | Package and deploy                                     | 6h    |       |      |
| 21  | Summary and where to go next                           | 8h    |       |      |

⏰ About **180h** ~ 4.5 weeks
🚩 **24. April** -> around 20 work days

## Goals 🎯

### Side-goals

- Identify and fix documentation gaps
- Identify developer experience issues
- Improve example app to work as a complete starter for eligible parties
- Find and fix bugs in EDDIE and AIIDA

### Non-goals

- Will not teach how to build EDDIE or AIIDA from source, extend, or contribute
    - Refer to Contributing Guide, Architecture Docs, Operation Manual
- Will use recent stable Docker images instead of latest
- Unit and end-to-end tests as well as per-day verification are future work
- Result should be close but does not have to exactly match the example app
    - Example app might include tests and features the guide does not have time for

## Example App 🚀

Readers of the 21 days guide build the example app (minus some polish).
Guide acts as deep documentation and tutorial towards the example app.
Example app can be used as a starter for eligible parties to build their own solution.

### Stack

**Java Spring** as a highly common and stable backend implementation that also matches our own implementation language.

**Keycloak** as an open-source authentication manager common in our documentation.

### Current state

- https://github.com/eddie-energy/example-app
- Based on previous eclipse marketplace frontend
- Stack is Vue bundled with Spring
- Visualize data
- Name and revoke permissions

## Checkpoint Strategy 🏁

🚩 Can start on specific day
🚩 Compare expected outcome
🚩 Maintainable

### Branches ✅

🪾 `main` -> Guide Markdown (and example app if same repo)
🪾 `day-01` ... `day-21` -> Expected state at the end of each day

➕ Avoids directory duplication
➕ Easy compare and switch with `git diff` and `git switch`
➖ Structure and maintenance require additional explanation

[Git worktrees](https://git-scm.com/docs/git-worktree) can help managing multiple days as directories during maintenance.
IntelliJ has support for Git worktrees since version `2026.1`.

### Directories

📁 `guide` -> Guide Markdown
📁 `day-01` ... `day-21` -> Expected state at the end of each day

➕ Discovery and navigation are trivial `cd day-12`
➖ Duplication and drift risk -> requires tooling to keep in sync

### ~~Commits or tags~~

Minimal maintenance and highly stable once published but

- hard to iteratively improve
- requires discipline and knowledge of the workflow to work on
- needs additional explanation

## Presentation 📹

- **Markdown-first** approach -> should be readable entirely from GitHub
- Guide should render as a website -> **VitePress** to match documentation
- Guide should always live on main and not mix with implementation checkpoints

## Repository Structure 📂

Naming ideas: `eddie-energy/21-days`, `eddie-energy/example-app`, `eddie-energy/guide`

- 📄 `README.md` -> Explains structure, introduces guide / app, how to report issues
- 📄 `CONTRIBUTING.md` -> Explains how to make changes and manage checkpoints
- 📁 `.vitepress` -> VitePress configuration
- 📁 `days`
    - 📄 `day-01.md` -> Documentation page of that day
- 📁 `example-app` -> Kept usable independent of the guide (maybe separate repo)
- 🔧 `package.json` -> VitePress import and run scripts

## Ideas 💡

- Curl and [jq](https://jqlang.org/) for REST calls?
- Document API calls to confirm expected outcomes with http files to run with the [JetBrains HTTP Client CLI](https://www.jetbrains.com/ijhttp/download)?