"""
PAP solution verifier.

Applies the same preprocessing as Instance.java (union-find merging of adjacent nodes with
degree <= 2) and checks the solution on the reduced graph with the PAP spreading process.
Seed node ids in the solution files refer to that reduced graph.

Usage:
  python3 check_solution.py <instance.in> <ils_instance.txt>    check a single solution
  python3 check_solution.py <instances_folder> <solutions_folder>  check every instance
                                                                   with an ils_<name>.txt file
"""

import sys
import os
from collections import deque


# ── Union-Find ───────────────────────────────────────────────────────────────
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank   = [0] * n

    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]

    def union(self, a, b):
        ra, rb = self.find(a), self.find(b)
        if ra == rb: return
        if   self.rank[ra] < self.rank[rb]: self.parent[ra] = rb
        elif self.rank[ra] > self.rank[rb]: self.parent[rb] = ra
        else: self.parent[rb] = ra; self.rank[ra] += 1


# ── Preprocessing (mirrors Instance.java::preprocessing) ────────────────────
def preprocessing(edges):
    """
    Merges adjacent nodes u,v when both have degree <= 2.
    Returns reduced edge list with nodes relabelled 0..k-1.
    """
    if not edges:
        return []

    # Step A: map original labels → compact indices
    label_to_idx = {}
    idx_to_label = []
    for u, v in edges:
        for node in (u, v):
            if node not in label_to_idx:
                label_to_idx[node] = len(idx_to_label)
                idx_to_label.append(node)

    n = len(idx_to_label)
    degree = [0] * n

    # Step B: compute degrees (ignore self-loops)
    for u, v in edges:
        iu, iv = label_to_idx[u], label_to_idx[v]
        if iu == iv: continue
        degree[iu] += 1
        degree[iv] += 1

    # Step C: union nodes where both degree <= 2
    uf = UnionFind(n)
    for u, v in edges:
        iu, iv = label_to_idx[u], label_to_idx[v]
        if iu == iv: continue
        if degree[iu] <= 2 and degree[iv] <= 2:
            uf.union(iu, iv)

    # Step D+E: assign new IDs and build reduced edge list (dedup)
    rep_to_new = {}
    next_id    = [0]
    seen       = set()
    new_edges  = []

    def get_id(rep):
        if rep not in rep_to_new:
            rep_to_new[rep] = next_id[0]
            next_id[0] += 1
        return rep_to_new[rep]

    for u, v in edges:
        iu, iv   = label_to_idx[u], label_to_idx[v]
        rep_u    = uf.find(iu)
        rep_v    = uf.find(iv)
        if rep_u == rep_v: continue   # self-loop after merge

        a, b = get_id(rep_u), get_id(rep_v)
        key  = (min(a, b), max(a, b))
        if key not in seen:
            seen.add(key)
            new_edges.append([min(a,b), max(a,b)])

    return new_edges


# ── Parse files ───────────────────────────────────────────────────────────────
def parse_instance(path):
    with open(path) as f:
        lines = f.read().strip().splitlines()
    n_edges_orig = int(lines[3])
    raw_edges = []
    for line in lines[4:4 + n_edges_orig]:
        u, v = map(int, line.split())
        raw_edges.append((u, v))

    reduced = preprocessing(raw_edges)

    # Build adjacency list for reduced graph
    nodes = set()
    for u, v in reduced:
        nodes.add(u); nodes.add(v)
    n_nodes = len(nodes)
    adj = {i: [] for i in range(n_nodes)}
    for u, v in reduced:
        adj[u].append(v)
        adj[v].append(u)
    return n_nodes, adj


def parse_solution(path):
    with open(path) as f:
        lines = f.read().strip().splitlines()
    raw = lines[2].strip().strip('(').strip(')').strip()
    return [int(x) for x in raw.split(',') if x.strip()]


# ── PAP spreading process ─────────────────────────────────────────────────────
def spreading_process(n_nodes, adj, seeds):
    aware          = [False] * n_nodes
    spreader       = [False] * n_nodes
    spreader_count = [0]     * n_nodes
    queue          = deque()

    for s in seeds:
        aware[s] = spreader[s] = True
        queue.append(s)

    aware_total = spreader_total = len(seeds)

    while queue:
        node = queue.popleft()
        if not spreader[node]:
            continue
        for nbr in adj[node]:
            if not aware[nbr]:
                aware[nbr] = True
                aware_total += 1
            if not spreader[nbr]:
                spreader_count[nbr] += 1
                deg = len(adj[nbr])
                if deg == 0 or spreader_count[nbr] >= deg * 0.5:
                    spreader[nbr] = True
                    spreader_total += 1
                    queue.append(nbr)

    return aware, spreader, aware_total, spreader_total


# ── Verify a single pair ──────────────────────────────────────────────────────
def verify(instance_path, solution_path, verbose=True):
    n_nodes, adj = parse_instance(instance_path)
    seeds        = parse_solution(solution_path)

    if verbose:
        print(f"\n{'='*60}")
        print(f"Instance       : {os.path.basename(instance_path)}")
        print(f"Reduced nodes  : {n_nodes}")
        print(f"Seeds ({len(seeds)})     : {seeds}")

    aware, spreader, n_aware, n_spreader = spreading_process(n_nodes, adj, seeds)

    all_reached = n_aware == n_nodes

    violations = []
    for v in range(n_nodes):
        deg = len(adj[v])
        if deg == 0: continue
        aw_nbrs = sum(1 for u in adj[v] if aware[u])
        if aw_nbrs < deg * 0.5:
            violations.append((v, aw_nbrs, deg))

    ok = all_reached and len(violations) == 0

    if verbose:
        print(f"Aware          : {n_aware}/{n_nodes}  {'✓' if all_reached else '✗'}")
        print(f"Spreaders      : {n_spreader}/{n_nodes}")
        print(f"Half-nbr check : {'✓' if not violations else f'✗ {len(violations)} violations'}")
        if not all_reached:
            unreached = [v for v in range(n_nodes) if not aware[v]]
            print(f"Unreached ({len(unreached)}): {unreached[:20]}{'...' if len(unreached)>20 else ''}")
        if violations:
            for v, g, d in violations[:5]:
                print(f"  Node {v}: {g}/{d} aware neighbors (need {d*0.5})")
        print(f"Result         : {'✓ VALID' if ok else '✗ INVALID'}")

    return ok, n_aware, n_nodes, len(violations)


# ── Main ──────────────────────────────────────────────────────────────────────
def verify_folder(inst_dir, sol_dir):
    instances = sorted(f for f in os.listdir(inst_dir) if f.endswith('.in'))
    total = valid = 0
    for inst_file in instances:
        key      = inst_file[:-3]
        sol_path = os.path.join(sol_dir, f'ils_{key}.txt')
        if not os.path.exists(sol_path):
            continue
        ok, n_aware, n_nodes, viols = verify(os.path.join(inst_dir, inst_file), sol_path, verbose=False)
        total += 1
        if ok:
            valid += 1
        else:
            print(f"✗ {key:45s} aware={n_aware}/{n_nodes}  violations={viols}")
    print(f"{valid}/{total} valid  |  {total - valid} invalid")


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(1)
    if os.path.isdir(sys.argv[1]):
        verify_folder(sys.argv[1], sys.argv[2])
    else:
        verify(sys.argv[1], sys.argv[2])
