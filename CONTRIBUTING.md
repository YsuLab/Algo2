# Working together

## The loop

```bash
git switch main && git pull                 # start fresh
git switch -c badr/graph-reader             # one branch per task: name/what
# ... write code ...
mvn verify                                  # pending tests are fine; failures are not
git add -A && git commit -m "Graph: read campus_graph.txt"
git push -u origin badr/graph-reader        # then open a Pull Request on GitHub
```

1. Open the PR early, even half-done (mark it *Draft*). Teammates see what is coming.
2. CI runs on the PR. Check the summary for the progress table.
3. One teammate reviews: does it match the Javadoc contract, and can the author explain it?
4. *Squash and merge*. Delete the branch.

Small PRs merge fast.
Prefer three PRs ("constructor + addEdge", "file reader", "validation") over one giant one.

## Rules that prevent merge pain

- **One owner per file.** Edit someone else's file only after telling them.
- **Public signatures are a contract.** `Graph`'s public methods are used by all three of us. Change them only after agreeing in the group chat, then update the Javadoc and tests in the same PR.
- **Never push straight to `main`.** Branch protection enforces this (setup below).
- **Keep the output format exact.** `docs/expected-output.txt` is copied from the assignment, and `MainOutputTest` checks it.
- **No MST libraries, no copied Prim code.** The assignment forbids both. Every one of us must be able to explain our part in the oral presentation.

## What "pending" means

Each stub throws `UnsupportedOperationException("TODO(Name): what")`.
`PendingUntilImplemented` turns that into a skipped test with the TODO message.
So:

- ⏳ pending = not written yet (fine)
- ✅ passing = written and matches the spec
- ❌ failing = written but wrong (fix before merging)

When you finish a method, delete its `throw` line. Its tests then run for real.

## One-time repo setup (repo admin)

**Settings → Branches → Add branch ruleset** (or *branch protection rule*) for `main`:

- Require a pull request before merging, with 1 approval.
- Require status checks to pass: `Build & test (Maven)` and `Plain javac on JDK 17`.
- Block force pushes.

**Settings → Collaborators** → add each teammate with *Write* access.

Optional: create `.github/CODEOWNERS` with your GitHub usernames so the owner is auto-requested as reviewer:

```
src/main/java/cpcs324/Graph.java                 <badr-username>
src/main/java/cpcs324/Edge.java                  <badr-username>
src/main/java/cpcs324/PrimMST.java               <yasir-username>
src/main/java/cpcs324/Main.java                  <yasir-username>
src/main/java/cpcs324/RandomGraphGenerator.java  <ahmed-username>
src/main/java/cpcs324/Experiments.java           <ahmed-username>
```
