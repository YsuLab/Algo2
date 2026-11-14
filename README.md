# CPCS-324 Project, Part I: Minimum-Cost Campus Network (Prim's Algorithm)

[![CI](https://github.com/YsuLab/Algo2/actions/workflows/ci.yml/badge.svg)](https://github.com/YsuLab/Algo2/actions/workflows/ci.yml)

Connect six campus buildings (B1 to B6) with fiber at minimum cost.
The program reads `campus_graph.txt`, validates it, and builds the Minimum Spanning Tree with
Prim's algorithm (Java `PriorityQueue`), printing every step.
It then times Prim on random graphs for Experiments A and B.

Assignment: `CPCS_324_Project_part_1` (Fall 2026-2027). Team plan (Arabic): [`docs/team-plan.ar.md`](docs/team-plan.ar.md).

## Who owns what

| Owner | Files | Done when |
|---|---|---|
| Badr | `Edge.java`, `Graph.java`, `campus_graph.txt` | `GraphTest` all green: 6 vertices, 9 edges, 1 component, weight 41; bad input detected |
| Yasir | `PrimMST.java`, `Main.java`, saved runs (`RunRecorder`, `GraphPainter`, `ReportPage`, `ReportServer`) | `PrimMSTTest` and `MainOutputTest` green: cost 13, exact step trace |
| Ahmed | `RandomGraphGenerator.java`, `Experiments.java` | `RandomGraphGeneratorTest` (all 8 configs) and `ExperimentsTest` green; "Run experiments" workflow prints both tables |

Every class already has its public methods, with Javadoc describing the contract.
The bodies throw `UnsupportedOperationException("TODO(Name): ...")` until the owner writes them.
The tests encode the assignment's expected output, so a green test means the requirement is met.

Graph is the shared dependency.
Badr ships the constructor, `addEdge` and `getNeighbors` first; Prim and the generator need nothing else to start.

## Build and run

From the repository root (so `campus_graph.txt` is found):

```bash
mvn verify                                   # compile + all tests
mvn -q package -DskipTests
java -jar target/algo2.jar                   # campus graph + experiments
java -jar target/algo2.jar --skip-experiments
java -jar target/algo2.jar --experiments-only
java -jar target/algo2.jar --serve           # also open the visualizer in a browser
java -jar target/algo2.jar --no-save         # do not create a runs/N folder
```

Without Maven (this is what CI's "Plain javac" job checks):

```bash
javac -d out src/main/java/cpcs324/*.java
java -cp out cpcs324.Main
```

## Saved runs and the visualizer

Every run saves its results in a new numbered folder: `runs/1`, `runs/2`, `runs/3`, and so on.
Nothing is overwritten, so you can compare the timings of different runs.

| File | What it is |
|---|---|
| `output.txt` | Exactly what the program printed |
| `graph.png` | The input graph |
| `step-1.png` ... `step-5.png` | One picture per Prim step |
| `mst.png` | The finished minimum spanning tree |
| `experiment-a.png`, `experiment-b.png` | The two charts (A red, B blue) |
| `experiment-a.csv`, `experiment-b.csv` | The two timing tables |
| `index.html` | Interactive page for this run: step through Prim and see the priority queue |

The PNG files have a white background, so they can go straight into the report.

Open `runs/N/index.html` by double-click, or run with `--serve` and open <http://localhost:8080>.
The server is part of the program (it uses the HTTP server inside the JDK) and shows the newest run.
`runs/` is in `.gitignore`, so saved runs never reach the repository.

Classes: `RunRecorder` (folders and files), `GraphPainter` (pictures), `ReportPage` (the page), `ReportServer` (`--serve`). Owner: Yasir.

IDE: NetBeans → *Open Project* → this folder. IntelliJ → *Open* → `pom.xml`. VS Code → Java extension pack, open the folder.

## Tests and CI

- `mvn verify` runs JUnit 5 tests in `src/test/java/cpcs324/`.
- A test that reaches a TODO stub is **pending** (skipped), not failed. So CI is green from day one, and each test starts enforcing the spec once its code exists.
- Every PR and push to `main` runs [CI](.github/workflows/ci.yml). The run summary shows a per-owner progress table.
- [`Run experiments`](.github/workflows/experiments.yml) is a manual button that prints both experiment tables.
- Pushing a tag like `v1.0.0` builds the Blackboard zip and attaches it to a GitHub Release ([release.yml](.github/workflows/release.yml)).

See [CONTRIBUTING.md](CONTRIBUTING.md) for the day-to-day workflow and [docs/ci-cd.md](docs/ci-cd.md) for the CI/CD design.

## Internal timeline

| Date | Milestone |
|---|---|
| Tue 10 Nov 2026 | Code complete and mergeable, plus each person's report section draft |
| Sat 14 Nov 2026 | Merged, experiments run on final code, report reviewed, upload tested |
| Sun 15 Nov 2026 | Official deadline (check the exact closing time on Blackboard) |
