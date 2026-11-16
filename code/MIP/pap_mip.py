"""
Time-indexed MIP formulation (|V|^2 binary variables) of the Perfect Awareness
Problem, solved with Gurobi.

Variables s[v, t] = 1 if node v is a spreader at round t (t = 0..n).
s[v, 0] = 1 means v belongs to the initial seed set.

Usage:
    python pap_mip.py <instance.in> [<instance.in> ...] [--time-limit 3600] [--threads 8] [--out-dir ../../solutions/mip]

For each instance, a file <out-dir>/<instance name>.txt is written with:
    <objective value (number of seed nodes)>
    <time in seconds>
"""
import argparse
import math
import os
import time

import networkx as nx
from gurobipy import GRB, GurobiError, Model, quicksum


def load_instance(instance_path):
    with open(instance_path) as f:
        f.readline(); f.readline()
        n = int(f.readline())
        edges = int(f.readline())
        G = nx.Graph()
        G.add_nodes_from(range(n))
        for _ in range(edges):
            a, b = map(int, f.readline().split())
            G.add_edge(a, b)
    return G


def solve(G, time_limit, threads):
    n = G.number_of_nodes()
    init_time = time.time()

    model = Model("PAP")
    model.setParam("LogToConsole", 1)
    model.setParam("TimeLimit", time_limit)
    model.setParam("Threads", threads)

    s = model.addVars(G.nodes, range(n + 1), vtype=GRB.BINARY, name="s")

    # Minimize the number of initial spreaders
    model.setObjective(quicksum(s[v, 0] for v in G.nodes), GRB.MINIMIZE)

    for v in G.nodes:
        T = math.ceil(0.5 * G.degree[v])  # spreading threshold
        for tau in range(1, n + 1):
            neigh = quicksum(s[u, tau - 1] for u in G.neighbors(v))
            # Spreaders remain spreaders
            model.addConstr(s[v, tau] - s[v, tau - 1] >= 0)
            # A non-seed node becomes a spreader only if enough neighbours spread
            model.addConstr(neigh - 0.5 * G.degree[v] * (s[v, tau] - s[v, 0]) >= 0)
            # A node reaching its threshold must become a spreader
            model.addConstr(neigh <= (T - 1) + G.degree[v] * s[v, tau])
    # Every node becomes aware (it is a seed or has a spreader neighbour)
    for v in G.nodes:
        model.addConstr(s[v, 0] + quicksum(s[u, n - 1] for u in G.neighbors(v)) >= 1)

    model.optimize()
    elapsed = time.time() - init_time
    if model.SolCount == 0:
        return None, None, elapsed
    seed_set = sorted(v for v in G.nodes if s[v, 0].X > 0.5)
    return len(seed_set), seed_set, elapsed


def main():
    parser = argparse.ArgumentParser(description="Gurobi MIP model for the Perfect Awareness Problem")
    parser.add_argument("instances", nargs="+", help="Instance files (.in)")
    parser.add_argument("--time-limit", type=float, default=3600, help="Time limit in seconds (default: 3600)")
    parser.add_argument("--threads", type=int, default=8, help="Gurobi threads (default: 8)")
    parser.add_argument("--out-dir", default="solutions/mip", help="Output directory (default: solutions/mip)")
    args = parser.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)
    for instance_path in args.instances:
        name = os.path.splitext(os.path.basename(instance_path))[0]
        G = load_instance(instance_path)
        try:
            value, seed_set, elapsed = solve(G, args.time_limit, args.threads)
        except GurobiError as e:
            print(f"{name}: Gurobi error: {e}")
            continue
        print(f"{name}: value={value} time={elapsed:.2f}s seed={seed_set}")
        with open(os.path.join(args.out_dir, f"{name}.txt"), "w") as f:
            f.write(f"{value}\n{elapsed}\n")


if __name__ == "__main__":
    main()
