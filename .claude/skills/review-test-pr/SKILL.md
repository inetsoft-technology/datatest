---
name: review-test-pr
description: Use when reviewing one pull request in inetsoft-technology/datatest - expected data (json/csv/txt checked; png content left to the author with a reminder), Groovy specs, the shared test framework in commons, build and CI files, and docs. Works read-only under a review bot (the caller posts the result) or invoked directly by a person (posts the review, merges only on request). Processes exactly one PR per run.
---

# Review Test-Repo PR (datatest)

## Overview

A datatest PR can be wrong in three ways:

1. **The expected data is wrong**: a baseline image or text file was regenerated from a broken
   render or a run-specific state, so the spec now passes against a wrong result. The review
   checks text baselines; image content is left to the author (there are usually too many), and
   the review always reminds them of it.
2. **A spec or the framework stops checking**: an assertion or comparison is skipped, a missing
   export is ignored, or a change in `commons` silently alters every spec that inherits from it.
   This is a false green and is the most important thing to catch.
3. **Code, build and docs drift apart**: a profile in a `pom.xml`, a workflow, the README run
   commands and the `docs/` assessments no longer agree.

The review is judgment only. Nothing in this skill compiles or runs specs. The only write to
the local clone is `git fetch` (refs and objects); never change its working tree or check out.

## Who calls this skill

**Under a review bot** (e.g. team-bot-kit, which starts this skill in a read-only session). The
caller's prompt says so and gives some of the following. Use what it gives instead of doing that
step yourself:

| Caller gives | Use it instead of |
|---|---|
| The local clone path (read-only, `git -C <path> ...`) | Guessing a path |
| The head commit under review (`HEAD_SHA`) | `headRefOid` from Step 1 |
| "the fetch is done" | Step 1's `git fetch` (still subject to the `cat-file` check there) |
| The number of unresolved review threads | The graphql query in Step 4 |
| The previous round's review and the commit it covered | Reviewing the whole PR again (see **Re-review**) |
| A required output format and tiers | Steps 3 and 4 |

Under a bot the only write allowed is Step 1's `git fetch`: do not run `gh pr review`,
`gh pr comment`, `gh pr merge`, do not write or delete files, do not use `cd`, pipes, `;`, `&&` or
`$(...)`, and do not send chat messages. Steps that need any of these are skipped; output the
decision and body in the caller's format and let the caller post it. If the caller says it
exported the changed images (e.g. "改动的图片已经导出到 ...，直接用 Read 看"), ignore the export:
do not open the images and do not comment on the export (see **Expected data**).

**Invoked directly** (`/review-test-pr <number> [review 并合并]`): you do every step yourself,
including posting (Step 4) and, only when asked, merging.

## Inputs

`OWNER/REPO` (default `inetsoft-technology/datatest`), PR number `N`, and the mode: `只 review`
(default) or `review 并合并`. Pass `--repo OWNER/REPO` on every `gh` command.

**Hard limits:** only PR `N`; never `gh pr merge --admin`; never push to, edit or close the PR;
never delete its branch; never check out in the clone.

## 1. Load the PR

```bash
gh pr view N --repo OWNER/REPO --json number,title,body,url,author,state,isDraft,baseRefName,headRefName,headRefOid,mergeable,mergeStateStatus,reviewDecision,latestReviews,statusCheckRollup,commits,additions,deletions,changedFiles
gh repo view OWNER/REPO --json defaultBranchRef --jq .defaultBranchRef.name
gh pr diff N --repo OWNER/REPO --name-only
gh pr diff N --repo OWNER/REPO
git -C <clone> fetch -q origin +pull/N/head:refs/remotes/pr/N +BASE:refs/remotes/origin/BASE
```

`HEAD_SHA` is the caller's head commit, or `headRefOid`. Check it is local with
`git -C <clone> cat-file -e HEAD_SHA`; if not, run the fetch above.

**The clone's working tree is not the PR.** Read, Glob and Grep on the clone show whatever branch
it has checked out, which may be old. For anything about the PR's code, go through git:
- one file's change: `git -C <clone> diff refs/remotes/origin/BASE...HEAD_SHA -- PATH`
  (`gh pr diff` takes no path filter);
- a file at the PR head: `git -C <clone> show HEAD_SHA:PATH`;
- searching the PR head (subclasses, callers): `git -C <clone> grep -n PATTERN HEAD_SHA`.

Stop and report (no review) if the PR is not `OPEN`, is a draft, or targets a branch other than
the default branch (name the branch it targets).

**Re-review.** `PREV` is the commit the previous review covered: given by the caller, or, when
invoked directly, the `commit.oid` of this account's latest review in `gh pr view N --json reviews`.
No previous review means a full review.
- If `PREV` is not local, run `git -C <clone> fetch -q origin PREV`; if it still cannot be found
  (force push), review the whole PR.
- If `git -C <clone> log --merges --oneline PREV..HEAD_SHA` is empty, review
  `git -C <clone> diff PREV HEAD_SHA`. Otherwise the author merged the base branch in, and that
  diff also contains the base's changes: for each file in the PR's file list, compare
  `git -C <clone> diff refs/remotes/origin/BASE...PREV -- PATH` with
  `git -C <clone> diff refs/remotes/origin/BASE...HEAD_SHA -- PATH` and review only what differs.
- Then check each blocking and should-fix item of the previous review: drop the resolved ones,
  repeat the unresolved ones at their old tier. Code the new commits did not change only gets new
  findings when they are blocking.

## 2. Review

Group the changed files by module (first path segment: `chart`, `vsothers`, `mv`, `commons`, ...)
and by kind below. Every change should be explained by the PR title and body; a change that is
not is should-fix.

### Expected data (`*/src/test/resources/expectData/**`)

Files sit under the spec and case they belong to, sometimes below a suite folder (e.g.
`chart/.../expectData/Actions/Actions_Spec/Brush_Case1/`, `ws/.../expectData/DataComposition_Spec/<case>/`,
`mv/.../expectData/AssociationMV/<case>/`). Folder and file names may contain spaces.

**Images (`.png` etc.): do not view them.** Expected-data PRs often update many images, so
the review does not open or compare image content, even when the caller has exported them. Check
only what the file list shows:
- Images are only under `expectData`, in the spec folders of the modules the title (or, if the
  title names none, the body) names. An image elsewhere is should-fix.
- A deleted image has a reason in the body (see **Deleted or added baselines**).
- Count the changed images per module and spec folder for the `## Images` section.

Then always remind the author, in `## Images`, that image correctness is theirs to confirm: each
new image must be a complete, correct render (not blank, not an error or login page, not
truncated, no data missing that the old image had) and must show the change the PR claims. Say
plainly that the review did not look at image content.

**Text (`.json`, `.csv`, `.txt`, `.xml`).** Read the diff.
- Blocking: stack traces or `Exception` text baked into expected output; error placeholders where
  the old file had data.
- Should fix: machine-specific values (local paths, hostnames, IPs, user names) or run-specific
  values (timestamps, generated IDs, session IDs, "today"-based dates) that make the spec flaky;
  a whole-file reformat or reordering that hides the real change.

**Deleted or added baselines.** A deleted expected file is blocking unless the body explains why
the case was removed. A new baseline folder `.../<Spec>/<case>/` whose case does not exist at the
PR head (`git -C <clone> grep -n "def '<case>'" HEAD_SHA -- '*<Spec>.groovy'` finds nothing) is
should-fix (orphan).

**Scope of an expected-data PR** ("update expected data ..."): it should touch only `expectData`,
plus the spec source if the title says so. Anything else (workflows, `pom.xml`, `commons`,
credentials, modules the title does not mention) is should-fix at least; a credential is blocking.

### Specs and test code (`*/src/test/groovy/**`, `*/src/test/java/**`, `.claude/**/*.py`)

Review to the development standard: correctness, edge cases, readability, regressions. On top:
- **Blocking: a false green.** A case that passes while the defect it guards is present: a
  comparison that is skipped or returns early, an assertion inside a branch the defect never
  reaches, an expected file that is never compared, an export failure that is caught and ignored.
  Name the defective result that would pass and the line it gets past; without that it is
  should-fix.
- **Blocking:** a case disabled (`@Ignore`, `@IgnoreIf`, commented-out compare or assert) without
  a reason and a bug number in the body or the code.
- **Should fix:** a `Thread.sleep` or retry loop used as the fix; a hard-coded host, port, user or
  password instead of the existing config; a case whose bug number in a comment does not match
  the PR body.
- Baseline update switches (`-Dupdate.baseline`, `-Dupdate.baseline.png`) must stay opt-in. A
  change that makes them the default, or turns comparison off without them, is blocking.

### The shared framework (`commons/src/**`)

A change here changes every module. For each changed class or method:
- Find every subclass and caller across all modules at the PR head with
  `git -C <clone> grep -n PATTERN HEAD_SHA` (e.g. `extends ViewsheetTest`, the method name), not
  with Grep on the working tree, and check each one still behaves as intended. A subclass whose override, private
  method or constructor now clashes with the change, or a caller that now fails earlier (e.g. a
  constructor that touches the file system), is blocking when you can name it.
- New behavior that turns on for every spec (strict checks, cleanup, new failures) needs the PR
  body to say which modules and profiles were run and with what result. Not run and impact
  unknown: should fix, listing the modules to run.

### Build and CI (`pom.xml`, `*/pom.xml`, `.github/workflows/**`, `Jenkinsfile`, `docker/**`, `gcp-db-server-iac/**`)

- The project version (e.g. `X.Y.Z-SNAPSHOT`) appears in the root `pom.xml` and in every module
  `pom.xml` (some files twice). A bump must change all of them; a module left on the old
  version is blocking.
- A new or renamed profile must match the README run commands and the workflow that runs it.
- The `test-*.yml` workflows are manual (`workflow_dispatch`); a change that adds a schedule or a
  push / pull_request trigger to them needs a reason in the body. `codeql.yml` already runs on
  push, pull_request and a schedule; `build-stylebi.yml` is a reusable workflow (`workflow_call`).
- Credentials in workflows must come from `${{ secrets.* }}`; a literal value is blocking.
- `gcp-db-server-iac`: never `terraform.tfvars`, `*.tfstate`, `*.tfplan` or keys in the PR
  (blocking).

### Docs (`README.md`, `docs/**`, `.claude/**` except `*.py`)

- Run commands, profile names and module lists must match the poms.
- A `docs/test-gap-assessment` doc must match the code it describes: a gap marked as covered must
  have the spec or case that covers it in the repo; a renamed class or method must be renamed in
  the doc too.
- Docs may be in Chinese; do not flag the language.

### Secrets

Literal tokens, passwords, license keys (`inetsoft.license.key`), DB server IPs and credentials,
cloud keys, anywhere in the diff, title, body or commit messages: always blocking. Never quote the
secret. References are fine: `${{ secrets.* }}`, environment variables, `<ip>`-style placeholders
in docs.

### Tiers

- **Blocking**: a false green, a correctness bug or regression, wrong or broken baselines, a
  secret, a doc that contradicts the code. Any blocker means request changes; no blocker means
  approve, with the should-fix items in the body.
- **Should fix**: real but smaller problems, flaky values, unknown impact of a framework change,
  missing run results.
- **Suggestion**: naming, comments, style, optional refactors.
- **Only what this PR introduces blocks.** A problem already on the base branch in lines the PR
  does not touch is a suggestion at most.
- If the caller gives its own tiers or limits, follow the caller.

### Do not run specs

Specs need a license key, a StyleBI build and the DB servers. List under `## Needs manual run`
the module and profile commands whose specs or expected data changed, e.g.
`./mvnw test -pl vsothers -Pvscalc`, and what a green run should show. A `commons` change also
needs `./mvnw package -pl commons -DskipTests` first.

## 3. Write the review

English Markdown, sections in this order, files (and lines) named for every finding:

- `## Verdict`: ready to merge, or every blocking issue in one line each.
- `## Blocking`, `## Should fix`, `## Suggestions` (write `None` for an empty section).
- `## Images`: how many changed (per module / spec folder), "image content was not reviewed",
  and the reminder to the author to confirm each image is correct (omit when none changed).
- `## Needs manual run`.

Under a bot, output this body in the caller's format and stop here.

## 4. Post and merge (invoked directly only)

Post with `gh pr review N --repo OWNER/REPO --approve|--request-changes --body-file <file>`
(write the body to a temporary file outside the clone). If the author is your own account
(`gh api user --jq .login`), GitHub does not allow approving: post `--comment` and do not merge.

Merge only in mode `review 并合并`, and only if all of these hold right before merging
(re-query with `gh pr view`):
1. `OPEN`, not a draft, and `headRefOid` is the commit you reviewed.
2. You approved, and no reviewer's latest review is `CHANGES_REQUESTED`.
3. Every check in `statusCheckRollup` is `SUCCESS`, `NEUTRAL` or `SKIPPED` (usually only CodeQL
   runs on a PR; the test workflows are manual). No checks at all is fine; say "no CI checks".
4. `mergeable == MERGEABLE` and `mergeStateStatus` is `CLEAN` or `HAS_HOOKS`.
5. No unresolved review threads:
   ```bash
   gh api graphql -F owner=OWNER -F name=REPO -F n=N -f query='query($owner:String!,$name:String!,$n:Int!){repository(owner:$owner,name:$name){pullRequest(number:$n){reviewThreads(first:100){nodes{isResolved}}}}}' --jq '[.data.repository.pullRequest.reviewThreads.nodes[]|select(.isResolved==false)]|length'
   ```

Then `gh pr merge N --repo OWNER/REPO --squash`. If it fails, report the error; do not retry with
other flags. Report the verdict, merged or not (and which condition failed), the blocking issues,
and how many images changed (with the reminder that their content was not reviewed).

## Common mistakes

| Mistake | Correct behavior |
|---|---|
| `gh pr diff N -- PATH` | No path filter; use `git -C <clone> diff ... -- PATH` |
| Hard-coding a clone path or a reviewer account | Use the caller's path and `gh api user` |
