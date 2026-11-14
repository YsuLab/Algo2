package cpcs324;

import java.util.List;
import java.util.Locale;

/**
 * Builds {@code index.html}, the interactive page saved with every run.
 *
 * <p>The page is one self-contained file. The run's real data (graph, Prim trace, timings)
 * is written into it as JSON, so it opens by double-click and also through
 * {@link ReportServer}. It lets you step through Prim edge by edge and see the priority
 * queue, the saved pictures, and both experiment charts.
 *
 * <p>The page template below is kept free of backslashes and non-ASCII characters on purpose,
 * so it compiles the same way with any {@code javac} default encoding.
 *
 * <p><b>Owner:</b> Yasir.
 */
public final class ReportPage {

    private ReportPage() {
        // static utility class
    }

    /**
     * @param runNumber   number of the run folder
     * @param savedAt     date and time of the run, already formatted
     * @param graph       the campus graph, or {@code null} if the campus part did not run
     * @param startVertex index Prim started from
     * @param prim        Prim after a traced run, or {@code null} if the campus part did not run
     * @param experimentA rows of Experiment A; empty if the experiments did not run
     * @param experimentB rows of Experiment B; empty if the experiments did not run
     * @return the complete HTML document
     */
    public static String render(int runNumber, String savedAt, Graph graph, int startVertex, PrimMST prim,
            List<RunRecorder.ExperimentRow> experimentA, List<RunRecorder.ExperimentRow> experimentB) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"run\":").append(runNumber);
        json.append(",\"savedAt\":").append(quote(savedAt));
        if (graph != null && prim != null) {
            json.append(",\"graph\":{\"n\":").append(graph.getVertexCount()).append(",\"edges\":[");
            List<Edge> edges = graph.getEdges();
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                json.append(i == 0 ? "" : ",").append('[').append(edge.getFrom()).append(',')
                        .append(edge.getTo()).append(',').append(edge.getWeight()).append(']');
            }
            json.append("]}");
            json.append(",\"pos\":[");
            double[][] layout = GraphPainter.layout(graph.getVertexCount());
            for (int v = 0; v < layout.length; v++) {
                json.append(v == 0 ? "" : ",").append('[').append(number(layout[v][0])).append(',')
                        .append(number(layout[v][1])).append(']');
            }
            json.append(']');
            json.append(",\"start\":").append(startVertex);
            json.append(",\"components\":").append(graph.countConnectedComponents());
            json.append(",\"totalWeight\":").append(graph.getTotalEdgeWeight());
            json.append(",\"cost\":").append(prim.getTotalCost());
            json.append(",\"mstEdges\":").append(prim.getMstEdges().size());
            json.append(",\"trace\":[");
            List<String> trace = prim.getTrace();
            for (int i = 0; i < trace.size(); i++) {
                json.append(i == 0 ? "" : ",").append(quote(trace.get(i)));
            }
            json.append(']');
        } else {
            json.append(",\"graph\":null");
        }
        json.append(",\"expA\":").append(rowsToJson(experimentA));
        json.append(",\"expB\":").append(rowsToJson(experimentB));
        json.append('}');
        return TEMPLATE.replace("@@RUN_NUMBER@@", String.valueOf(runNumber)).replace("@@DATA@@", json.toString());
    }

    /**
     * Builds {@code runs/index.html}, a plain list of every saved run with the newest first.
     *
     * @param latestRun the highest run number that exists
     * @return the complete HTML document
     */
    public static String renderRunList(int latestRun) {
        StringBuilder items = new StringBuilder();
        for (int run = latestRun; run >= 1; run--) {
            items.append("<li><a href=\"").append(run).append("/index.html\">Run ").append(run).append("</a></li>\n");
        }
        return "<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>Prim runs</title><style>body{background:#141011;color:#ece4dd;"
                + "font:16px/1.6 system-ui,Arial,sans-serif;max-width:640px;margin:0 auto;padding:28px 16px}"
                + "a{color:#62a3e0}h1{font:700 1.8rem Georgia,serif}li{margin:4px 0}</style></head><body>"
                + "<h1>Saved runs</h1><p>Newest first. Each run has its own folder with the printed output, "
                + "the pictures and the timing tables.</p><ul>\n" + items + "</ul></body></html>\n";
    }

    private static String rowsToJson(List<RunRecorder.ExperimentRow> rows) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            RunRecorder.ExperimentRow row = rows.get(i);
            json.append(i == 0 ? "" : ",").append("{\"n\":").append(row.n()).append(",\"m\":").append(row.m())
                    .append(",\"runs\":[");
            for (int r = 0; r < row.runs().length; r++) {
                json.append(r == 0 ? "" : ",").append(number(row.runs()[r]));
            }
            json.append("],\"avg\":").append(number(row.average())).append('}');
        }
        return json.append(']').toString();
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    /** A JSON string that is also safe inside a script tag. */
    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c == '<' || c == '>' || c == '&' || c < 0x20 || c > 0x7E) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.append('"').toString();
    }

    private static final String TEMPLATE = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Prim run @@RUN_NUMBER@@</title>
<style>
/* Colour always means the same thing: green = in the tree, blue = the step in focus, grey = the rest. */
:root {
  color-scheme: dark;
  --bg: #141011; --surface: #1d1718; --raised: #261e1f; --line: #3b2f30;
  --ink: #ece4dd; --muted: #a69892; --scarlet: #890810; --scarlet-hi: #d2535a;
  --ok: #62b37f; --focus: #62a3e0; --bad: #e5605a; --idle: #6b5d5c;
  --display: Georgia, "Times New Roman", serif;
  --body: system-ui, "Segoe UI", Roboto, Arial, sans-serif;
  --mono: ui-monospace, Menlo, Consolas, "Courier New", monospace;
}
* { box-sizing: border-box; }
html, body { margin: 0; }
body { background: var(--bg); color: var(--ink); font: 400 16px/1.55 var(--body); }
main { max-width: 1080px; margin: 0 auto; padding: 28px 16px 56px; display: flex; flex-direction: column; gap: 36px; }
header h1 { font: 700 2.1rem/1.15 var(--display); margin: 0 0 8px; }
header p { margin: 0; color: var(--muted); max-width: 62ch; }
header nav { margin-top: 10px; display: flex; gap: 16px; flex-wrap: wrap; }
a { color: var(--focus); }
h2 { font: 600 1.4rem/1.2 var(--display); margin: 0 0 4px; }
h3 { font: 600 .95rem/1.3 var(--body); margin: 0 0 8px; color: var(--muted); }
p.lede { margin: 0 0 16px; color: var(--muted); max-width: 65ch; }
code, .mono { font-family: var(--mono); font-variant-numeric: tabular-nums; }
.card { background: var(--surface); border: 1px solid var(--line); border-radius: 10px; padding: 14px; min-width: 0; }
.split { display: grid; gap: 14px; grid-template-columns: minmax(0, 1fr); }
@media (min-width: 860px) { .split { grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr); align-items: start; } }
.stack { display: flex; flex-direction: column; gap: 14px; min-width: 0; }
#graph { width: 100%; height: auto; display: block; }
.edge { stroke: var(--idle); stroke-width: 2; opacity: .55; }
.edge.frontier { stroke: var(--ink); opacity: .8; stroke-dasharray: 5 5; }
.edge.tree { stroke: var(--ok); stroke-width: 5; opacity: 1; }
.edge.now { stroke: var(--focus); stroke-width: 6; opacity: 1; }
.wbox { fill: var(--surface); stroke: var(--line); }
.wtext { fill: var(--ink); font: 500 15px var(--mono); text-anchor: middle; dominant-baseline: central; }
.node circle { fill: var(--raised); stroke: var(--idle); stroke-width: 2; }
.node text { fill: var(--muted); font: 600 16px var(--body); text-anchor: middle; dominant-baseline: central; }
.node.in circle { fill: #1f3a2a; stroke: var(--ok); }
.node.new circle { fill: #1c3148; stroke: var(--focus); stroke-width: 3; }
.node.in text, .node.new text { fill: var(--ink); }
.legend { display: flex; flex-wrap: wrap; gap: 6px 16px; margin-top: 10px; font-size: .84rem; color: var(--muted); }
.legend span { display: inline-flex; align-items: center; gap: 6px; }
.legend i { width: 22px; height: 0; border-top: 4px solid var(--idle); display: inline-block; }
.legend .l-tree { border-color: var(--ok); } .legend .l-now { border-color: var(--focus); }
.legend .l-front { border-top: 2px dashed var(--ink); }
.controls { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin-top: 12px; }
button, select { font: inherit; color: inherit; background: var(--raised); border: 1px solid var(--line); border-radius: 8px; padding: 8px 14px; min-height: 40px; }
button { cursor: pointer; }
button:disabled { opacity: .4; cursor: default; }
button.primary { background: var(--scarlet); border-color: #a51a23; font-weight: 600; }
:focus-visible { outline: 2px solid var(--focus); outline-offset: 2px; }
.stepcount { margin-left: auto; color: var(--muted); font-size: .9rem; }
input[type=range] { width: 100%; accent-color: var(--scarlet-hi); margin-top: 10px; }
label.inline { display: inline-flex; align-items: center; gap: 8px; color: var(--muted); font-size: .9rem; }
#now { font-size: 1.02rem; margin: 0 0 4px; }
#now b { color: var(--focus); font-weight: 600; }
#why { margin: 0; color: var(--muted); font-size: .92rem; }
.queue { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 4px; max-height: 260px; overflow-y: auto; }
.queue li { display: flex; gap: 10px; align-items: baseline; padding: 5px 9px; border-radius: 6px; background: var(--raised); font-family: var(--mono); font-size: .88rem; }
.queue li .w { margin-left: auto; }
.queue li.next { outline: 1px solid var(--focus); }
.queue li.stale { color: var(--idle); text-decoration: line-through; }
.queue li.plain { background: none; font-family: var(--body); color: var(--muted); }
.queue li small { font-family: var(--body); }
.queue li.next small { color: var(--focus); }
.trace { margin: 0; padding: 0; list-style: none; overflow-x: auto; }
.trace li { font-family: var(--mono); font-size: .82rem; white-space: nowrap; padding: 3px 8px; border-left: 3px solid transparent; color: var(--idle); }
.trace li.done { color: var(--ink); border-left-color: var(--ok); }
.trace li.cur { color: var(--ink); border-left-color: var(--focus); background: var(--raised); }
.stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(128px, 1fr)); gap: 8px; margin: 0; }
.stats div { background: var(--raised); border-radius: 8px; padding: 8px 10px; }
.stats dt { color: var(--muted); font-size: .8rem; }
.stats dd { margin: 0; font: 500 1.15rem var(--mono); }
.pill { display: inline-block; border-radius: 999px; padding: 2px 10px; font-size: .85rem; font-weight: 600; border: 1px solid; }
.pill.ok { color: var(--ok); } .pill.wait { color: var(--muted); }
.note { color: var(--muted); font-size: .88rem; margin: 8px 0 0; }
.charts, .gallery { display: grid; gap: 14px; grid-template-columns: minmax(0, 1fr); }
@media (min-width: 860px) { .charts { grid-template-columns: repeat(2, minmax(0, 1fr)); } .gallery { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
.gallery a { display: block; color: var(--muted); font-size: .85rem; text-decoration: none; }
.gallery img { width: 100%; height: auto; display: block; border-radius: 8px; border: 1px solid var(--line); margin-bottom: 4px; background: #fff; }
.chart svg { width: 100%; height: auto; display: block; }
.chart .axis { stroke: var(--line); } .chart .grid { stroke: var(--line); opacity: .5; }
.chart text { fill: var(--muted); font: 400 11px var(--mono); }
.chart .lbl { fill: var(--ink); }
.chart .fit { stroke: var(--muted); stroke-dasharray: 5 4; fill: none; stroke-width: 1.5; }
.tablewrap { overflow-x: auto; margin-top: 10px; }
table { border-collapse: collapse; width: 100%; font: 400 .8rem var(--mono); }
th, td { text-align: right; padding: 4px 8px; border-bottom: 1px solid var(--line); white-space: nowrap; }
th { color: var(--muted); font-weight: 500; }
.swatch { display: inline-block; width: 18px; border-top: 3px solid; vertical-align: middle; margin-right: 4px; }
footer { color: var(--muted); font-size: .85rem; border-top: 1px solid var(--line); padding-top: 14px; }
[hidden] { display: none !important; }
</style>
</head>
<body>
<main>
<header>
  <h1>Campus network, run <span id="runno"></span></h1>
  <p id="saved"></p>
  <nav id="nav"></nav>
</header>

<section id="campus">
  <h2>Minimum spanning tree, step by step</h2>
  <p class="lede">Each step takes the cheapest link from a visited building to an unvisited one. The trace is the text the Java program printed in this run.</p>
  <div class="split">
    <div class="stack">
      <div class="card">
        <svg id="graph" viewBox="0 0 600 350" role="img" aria-label="Campus graph"></svg>
        <div class="legend">
          <span><i class="l-now"></i>picked in this step</span>
          <span><i class="l-tree"></i>in the tree</span>
          <span><i class="l-front"></i>waiting in the queue</span>
          <span><i></i>not reached yet</span>
        </div>
        <input type="range" id="slider" min="0" max="5" value="0" aria-label="Step">
        <div class="controls">
          <button id="prev">&#9664; Back</button>
          <button id="next" class="primary">Next step &#9654;</button>
          <button id="play">Play</button>
          <button id="reset">Reset</button>
          <span class="stepcount mono" id="stepcount"></span>
        </div>
        <div class="controls">
          <label class="inline" for="start">Start from <select id="start"></select></label>
        </div>
      </div>
      <div class="card">
        <h3>Graph statistics</h3>
        <dl class="stats" id="stats"></dl>
      </div>
    </div>
    <div class="stack">
      <div class="card"><h3>This step</h3><p id="now"></p><p id="why"></p></div>
      <div class="card"><h3>Priority queue, cheapest first</h3><ul class="queue" id="queue"></ul></div>
      <div class="card"><h3>Program trace</h3><ol class="trace" id="trace"></ol></div>
      <div class="card"><h3>MST summary</h3><dl class="stats" id="summary"></dl></div>
    </div>
  </div>
</section>

<section id="pictures">
  <h2>Saved pictures</h2>
  <p class="lede">These PNG files are in this run's folder, ready to paste into the report.</p>
  <div class="gallery" id="gallery"></div>
</section>

<section id="experiments">
  <h2>Running time experiments</h2>
  <p class="lede">Average of the timed runs per graph size. The dashed line is the expected shape for heap-based Prim, <code>c &middot; m &middot; log2(n)</code>, fitted to the measurements.</p>
  <div class="charts">
    <div class="card chart">
      <h3><span class="swatch" style="border-color:var(--bad)"></span>Experiment A: vertices vs. average time</h3>
      <svg id="chartA" viewBox="0 0 520 300" role="img" aria-label="Experiment A chart"></svg>
      <div class="tablewrap" id="tableA"></div>
    </div>
    <div class="card chart">
      <h3><span class="swatch" style="border-color:var(--focus)"></span>Experiment B: edges vs. average time</h3>
      <svg id="chartB" viewBox="0 0 520 300" role="img" aria-label="Experiment B chart"></svg>
      <div class="tablewrap" id="tableB"></div>
    </div>
  </div>
  <p class="note">The same numbers are in <a href="experiment-a.csv">experiment-a.csv</a> and <a href="experiment-b.csv">experiment-b.csv</a>. The full printed output is in <a href="output.txt">output.txt</a>.</p>
</section>

<footer>CPCS-324 Project, Part I. Badr (graph), Yasir (Prim), Ahmed (experiments).</footer>
</main>

<script>
"use strict";
const RUN = @@DATA@@;
const $ = id => document.getElementById(id);
const NS = "http://www.w3.org/2000/svg";
const el = (tag, attrs, text) => {
  const e = document.createElementNS(NS, tag);
  for (const k in attrs) e.setAttribute(k, attrs[k]);
  if (text != null) e.textContent = text;
  return e;
};
const label = v => "B" + (v + 1);
const stat = (k, v) => "<div><dt>" + k + "</dt><dd>" + v + "</dd></div>";

$("runno").textContent = RUN.run;
$("saved").textContent = "Saved " + RUN.savedAt + ". Every run of the program gets its own numbered folder.";
$("nav").innerHTML = (RUN.run > 1 ? "<a href='../" + (RUN.run - 1) + "/index.html'>&larr; Run " + (RUN.run - 1) + "</a>" : "")
  + "<a href='../index.html'>All runs</a><a href='output.txt'>Printed output</a>";

/* ---------- Prim, same order as PrimMST.java (weight, then target, then source) ---------- */
let G = null, POS = [], STEPS = [], K = 0, timer = null, startNow = 0;

function buildGraph() {
  const edges = RUN.graph.edges.map((e, i) => ({ id: i, u: e[0], v: e[1], w: e[2] }));
  const adj = [];
  for (let i = 0; i < RUN.graph.n; i++) adj.push([]);
  edges.forEach(e => { adj[e.u].push(e); adj[e.v].push(e); });
  return { n: RUN.graph.n, edges: edges, adj: adj };
}
const other = (e, x) => (e.u === x ? e.v : e.u);

function prim(g, start) {
  const visited = new Array(g.n).fill(false);
  const cmp = (a, b) => a.e.w - b.e.w || a.to - b.to || a.from - b.from;
  const pq = [];
  const visit = x => {
    visited[x] = true;
    for (const e of g.adj[x]) { const y = other(e, x); if (!visited[y]) pq.push({ e: e, from: x, to: y }); }
  };
  const order = [start], tree = [], steps = [];
  let cost = 0;
  const snapshot = (pick, skipped) => ({
    pick: pick, skipped: skipped, order: order.slice(), tree: tree.slice(), cost: cost,
    queue: pq.slice().sort(cmp).map(c => ({ e: c.e, from: c.from, to: c.to, stale: visited[c.to] }))
  });
  visit(start);
  steps.push(snapshot(null, []));
  while (tree.length < g.n - 1 && pq.length) {
    const skipped = [];
    let c = null;
    while (pq.length) {
      pq.sort(cmp);
      const top = pq.shift();
      if (visited[top.to]) { skipped.push(top); continue; }
      c = top; break;
    }
    if (!c) break;
    tree.push(c.e.id); cost += c.e.w; order.push(c.to);
    visit(c.to);
    steps.push(snapshot(c, skipped));
  }
  return steps;
}

function traceLine(i) {
  // The recorded start uses the lines the Java program printed. Other starts are replayed here.
  if (startNow === RUN.start && RUN.trace[i - 1]) return RUN.trace[i - 1];
  const st = STEPS[i];
  return "Step " + i + ": " + label(st.pick.from) + " - " + label(st.pick.to) + " | Weight: " + st.pick.e.w + " | Visited: {" + st.order.map(label).join(", ") + "}";
}

function runFrom(start) {
  stop();
  startNow = start;
  STEPS = prim(G, start);
  K = 0;
  $("slider").max = STEPS.length - 1;
  render();
}

function render() {
  const s = STEPS[K], last = STEPS.length - 1;
  const complete = K === last && s.tree.length === G.n - 1;
  const svg = $("graph");
  svg.innerHTML = "";
  const inTree = new Set(s.tree), inQueue = new Set(s.queue.filter(c => !c.stale).map(c => c.e.id));
  const nowId = s.pick && !complete ? s.pick.e.id : -1;
  const weights = [];
  for (const e of G.edges) {
    const a = POS[e.u], b = POS[e.v];
    let cls = "edge";
    if (e.id === nowId) cls += " now";
    else if (inTree.has(e.id)) cls += " tree";
    else if (inQueue.has(e.id)) cls += " frontier";
    svg.appendChild(el("line", { x1: a[0], y1: a[1], x2: b[0], y2: b[1], class: cls }));
    weights.push([(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, e.w]);
  }
  for (const w of weights) {
    const half = 8 + String(w[2]).length * 4.8;
    svg.appendChild(el("rect", { x: w[0] - half, y: w[1] - 11, width: half * 2, height: 22, rx: 5, class: "wbox" }));
    svg.appendChild(el("text", { x: w[0], y: w[1], class: "wtext" }, w[2]));
  }
  const newest = s.pick && !complete ? s.pick.to : -1;
  for (let v = 0; v < G.n; v++) {
    const g = el("g", { class: "node" + (v === newest ? " new" : s.order.indexOf(v) >= 0 ? " in" : "") });
    g.appendChild(el("circle", { cx: POS[v][0], cy: POS[v][1], r: G.n > 16 ? 16 : 23 }));
    g.appendChild(el("text", { x: POS[v][0], y: POS[v][1] }, label(v)));
    svg.appendChild(g);
  }

  if (K === 0) {
    $("now").innerHTML = "Start at <b>" + label(s.order[0]) + "</b>.";
    $("why").textContent = "Its links go into the priority queue. Nothing is in the tree yet.";
  } else {
    const p = s.pick, name = label(p.from) + " - " + label(p.to);
    $("now").innerHTML = "Step " + K + ": take <b>" + name + "</b>, weight " + p.e.w + ".";
    let t = name + " was the cheapest link in the queue that reaches an unvisited building. " + label(p.to) + " is now visited and its links join the queue.";
    if (s.skipped.length)
      t = "Skipped first: " + s.skipped.map(c => label(c.from) + " - " + label(c.to) + " (" + c.e.w + ")").join(", ") + ". Both ends were already visited, so taking it would close a cycle. " + t;
    if (complete) t += " The tree has n - 1 edges, so the algorithm stops.";
    $("why").textContent = t;
  }

  const q = $("queue");
  q.innerHTML = "";
  if (!s.queue.length || complete) {
    const li = document.createElement("li");
    li.className = "plain";
    li.textContent = complete ? "Done. Whatever is left in the queue is never needed." : "The queue is empty.";
    q.appendChild(li);
  } else {
    let marked = false;
    for (const c of s.queue) {
      const li = document.createElement("li");
      let tag = "";
      if (c.stale) { li.className = "stale"; tag = "<small>would close a cycle</small>"; }
      else if (!marked) { li.className = "next"; tag = "<small>next pick</small>"; marked = true; }
      li.innerHTML = "<span>" + label(c.from) + " &rarr; " + label(c.to) + "</span>" + tag + "<span class='w'>" + c.e.w + "</span>";
      q.appendChild(li);
    }
  }

  const tr = $("trace");
  tr.innerHTML = "";
  for (let i = 1; i < STEPS.length; i++) {
    const li = document.createElement("li");
    li.textContent = traceLine(i);
    li.className = i < K || (i === K && complete) ? "done" : i === K ? "cur" : "";
    tr.appendChild(li);
  }

  const verdict = complete
    ? "<span class='pill ok'>Passed (" + s.tree.length + " = " + G.n + " - 1)</span>"
    : "<span class='pill wait'>" + s.tree.length + " of " + (G.n - 1) + "</span>";
  $("summary").innerHTML = stat("MST edges", s.tree.length) + stat(complete ? "Total MST cost" : "MST cost so far", s.cost)
    + "<div style='grid-column:1/-1'><dt>Edge count verification</dt><dd style='font-size:1rem'>" + verdict + "</dd></div>";

  $("slider").value = K;
  $("stepcount").textContent = "step " + K + " / " + last;
  $("prev").disabled = K === 0;
  $("next").disabled = K === last;
}

function go(k) { K = Math.max(0, Math.min(STEPS.length - 1, k)); render(); }
function stop() { if (timer) { clearInterval(timer); timer = null; } $("play").textContent = "Play"; }

if (RUN.graph) {
  G = buildGraph();
  POS = RUN.pos.map(p => [45 + p[0] * 510, 40 + p[1] * 270]);
  const sel = $("start");
  for (let v = 0; v < G.n; v++) { const o = document.createElement("option"); o.value = v; o.textContent = label(v); sel.appendChild(o); }
  sel.value = RUN.start;
  $("stats").innerHTML = stat("Vertices", G.n) + stat("Edges", G.edges.length) + stat("Components", RUN.components) + stat("Total edge weight", RUN.totalWeight);
  $("prev").onclick = () => { stop(); go(K - 1); };
  $("next").onclick = () => { stop(); go(K + 1); };
  $("reset").onclick = () => { stop(); go(0); };
  $("slider").oninput = e => { stop(); go(+e.target.value); };
  sel.onchange = e => runFrom(+e.target.value);
  $("play").onclick = () => {
    if (timer) { stop(); return; }
    if (K === STEPS.length - 1) go(0);
    $("play").textContent = "Pause";
    timer = setInterval(() => { if (K >= STEPS.length - 1) stop(); else go(K + 1); }, 1300);
  };
  document.addEventListener("keydown", e => {
    if (e.target.tagName === "SELECT" || e.target.tagName === "INPUT") return;
    if (e.key === "ArrowRight") { stop(); go(K + 1); }
    if (e.key === "ArrowLeft") { stop(); go(K - 1); }
  });
  runFrom(RUN.start);

  const files = [["graph.png", "Input graph"]];
  for (let i = 1; i <= RUN.mstEdges; i++) files.push(["step-" + i + ".png", "Step " + i]);
  files.push(["mst.png", "Minimum spanning tree, cost " + RUN.cost]);
  if (RUN.expA.length) files.push(["experiment-a.png", "Experiment A chart"]);
  if (RUN.expB.length) files.push(["experiment-b.png", "Experiment B chart"]);
  $("gallery").innerHTML = files.map(f => "<a href='" + f[0] + "'><img src='" + f[0] + "' alt='" + f[1] + "' loading='lazy'>" + f[1] + " (" + f[0] + ")</a>").join("");
} else {
  $("campus").hidden = true;
  const files = [];
  if (RUN.expA.length) files.push(["experiment-a.png", "Experiment A chart"]);
  if (RUN.expB.length) files.push(["experiment-b.png", "Experiment B chart"]);
  $("gallery").innerHTML = files.map(f => "<a href='" + f[0] + "'><img src='" + f[0] + "' alt='" + f[1] + "'>" + f[1] + " (" + f[0] + ")</a>").join("");
}

/* ---------- experiments ---------- */
function niceMax(v) {
  const p = Math.pow(10, Math.floor(Math.log10(v))), f = v / p;
  return (f <= 1 ? 1 : f <= 2 ? 2 : f <= 2.5 ? 2.5 : f <= 5 ? 5 : 10) * p;
}
function drawChart(svgId, rows, key, color, xTitle) {
  const svg = $(svgId);
  const L = 52, R = 500, T = 18, B = 250;
  const xMax = Math.max.apply(null, rows.map(r => r[key]));
  const shape = r => r.m * Math.log2(Math.max(2, r.n));
  const c = rows.reduce((s, r) => s + r.avg * shape(r), 0) / rows.reduce((s, r) => s + shape(r) * shape(r), 0);
  const yMax = niceMax(Math.max.apply(null, rows.map(r => Math.max(r.avg, c * shape(r)))) * 1.08);
  const X = v => L + (v / xMax) * (R - L - 14), Y = v => B - (v / yMax) * (B - T);
  for (let i = 0; i <= 4; i++) {
    const v = (yMax * i) / 4, y = Y(v);
    svg.appendChild(el("line", { x1: L, x2: R, y1: y, y2: y, class: i ? "grid" : "axis" }));
    svg.appendChild(el("text", { x: L - 8, y: y + 4, "text-anchor": "end" }, +v.toFixed(3)));
  }
  svg.appendChild(el("line", { x1: L, x2: L, y1: T, y2: B, class: "axis" }));
  svg.appendChild(el("text", { x: 14, y: (T + B) / 2, "text-anchor": "middle", transform: "rotate(-90 14 " + (T + B) / 2 + ")" }, "average time (ms)"));
  svg.appendChild(el("text", { x: (L + R) / 2, y: 292, "text-anchor": "middle" }, xTitle));
  svg.appendChild(el("polyline", { points: rows.map(r => X(r[key]) + "," + Y(c * shape(r))).join(" "), class: "fit" }));
  svg.appendChild(el("polyline", { points: rows.map(r => X(r[key]) + "," + Y(r.avg)).join(" "), fill: "none", stroke: color, "stroke-width": 2.5, "stroke-linejoin": "round" }));
  rows.forEach((r, i) => {
    const x = X(r[key]), y = Y(r.avg);
    svg.appendChild(el("line", { x1: x, x2: x, y1: B, y2: B + 5, class: "axis" }));
    svg.appendChild(el("text", { x: x, y: B + 18, "text-anchor": "middle" }, r[key]));
    svg.appendChild(el("circle", { cx: x, cy: y, r: 4.5, fill: color }));
    svg.appendChild(el("text", { x: Math.min(x, R - 22), y: y - 10 > T + 6 ? y - 10 : y + 18, "text-anchor": i === 0 ? "start" : "middle", class: "lbl" }, r.avg.toFixed(3)));
  });
}
function drawTable(id, rows) {
  let h = "<table><thead><tr><th>n</th><th>m</th>";
  for (let i = 1; i <= rows[0].runs.length; i++) h += "<th>Run " + i + "</th>";
  h += "<th>Average</th></tr></thead><tbody>";
  for (const r of rows) h += "<tr><td>" + r.n + "</td><td>" + r.m + "</td>" + r.runs.map(x => "<td>" + x.toFixed(3) + "</td>").join("") + "<td>" + r.avg.toFixed(3) + "</td></tr>";
  $(id).innerHTML = h + "</tbody></table>";
}
if (RUN.expA.length || RUN.expB.length) {
  if (RUN.expA.length) { drawChart("chartA", RUN.expA, "n", "var(--bad)", "number of vertices (n)"); drawTable("tableA", RUN.expA); }
  if (RUN.expB.length) { drawChart("chartB", RUN.expB, "m", "var(--focus)", "number of edges (m)"); drawTable("tableB", RUN.expB); }
} else {
  $("experiments").innerHTML = "<h2>Running time experiments</h2><p class='lede'>The experiments were skipped in this run. Run the program without <code>--skip-experiments</code> to record them.</p>";
}
if (!RUN.graph && !RUN.expA.length && !RUN.expB.length) $("pictures").hidden = true;
</script>
</body>
</html>
""";
}
