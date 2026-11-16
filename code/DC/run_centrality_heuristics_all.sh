#!/usr/bin/env bash
# Runs centrality_heuristics.py on every instance in instances/real-world
# in parallel, output redirected to results_centrality/<name>.txt.
# Each of the 5 algorithms (DC/PR/EC/CC/BC) inside centrality_heuristics.py is
# individually capped at PER_ALGO_TIMEOUT_SECS (2h) via SIGALRM, and reports
# its result (stdout + CSV) as soon as it finishes, times out, or errors --
# a slow/hanging algorithm no longer blocks or loses the others' results.
# The outer TIME_LIMIT_SECS below is just a safety net for the whole process
# (5 algorithms x per-algo timeout + buffer).
set -u

cd "$(dirname "$0")"

INSTANCES_DIR="../../instances/real-world"
OUT_DIR="results_centrality"
export PER_ALGO_TIMEOUT_SECS=$((2 * 3600))  # 2h per algorithm
TIME_LIMIT_SECS=$((5 * PER_ALGO_TIMEOUT_SECS + 1800))  # safety net for the whole instance

mkdir -p "$OUT_DIR"

# Use GNU `timeout` if present (standard on Linux); otherwise fall back to
# perl's alarm() (e.g. on macOS, which has no `timeout`).
if command -v timeout >/dev/null 2>&1; then
    TIMEOUT_CMD=(timeout "${TIME_LIMIT_SECS}s")
else
    TIMEOUT_CMD=(perl -e 'alarm shift; exec @ARGV' "$TIME_LIMIT_SECS")
fi

pids=()
for f in "$INSTANCES_DIR"/*.in; do
    name=$(basename "$f" .in)
    out="$OUT_DIR/${name}.txt"
    echo "Launching $name -> $out"
    "${TIMEOUT_CMD[@]}" python3 -u centrality_heuristics.py "$f" > "$out" 2>&1 &
    pids+=($!)
done

echo "Launched ${#pids[@]} jobs (PIDs: ${pids[*]}). Waiting for all to finish..."
wait "${pids[@]}"

echo "All jobs finished (or timed out). Aggregating results into Excel..."
python3 aggregate_centrality_results.py
