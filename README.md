# An Iterated Local Search to the Perfect Awareness Problem <a href="https://doi.org/TODO-DOI"><img src="https://upload.wikimedia.org/wikipedia/commons/1/11/DOI_logo.svg" alt="DOI" width="20"/></a> <a href="https://doi.org/10.5281/zenodo.19069610"><img src="https://upload.wikimedia.org/wikipedia/commons/e/e8/Zenodo-gradient-square.svg" alt="Zenodo" width="60"/></a>

[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC_BY--NC_4.0-lightgrey.svg?logo=creativecommons)](https://creativecommons.org/licenses/by-nc/4.0/deed.en)
[![Code](https://img.shields.io/badge/Code-Java-orange.svg)]()
[![Code](https://img.shields.io/badge/Code-Python-blue.svg)]()
[![Journal](https://img.shields.io/badge/Journal-Journal_of_Heuristics-blue.svg)](https://link.springer.com/journal/10732)
[![Impact Factor](https://img.shields.io/badge/JCR_IF-2.1_(Q3)-yellow.svg)]()
[![Website](https://img.shields.io/badge/Web-grafo.etsii.urjc.es%2FPAP-informational.svg)](https://grafo.etsii.urjc.es/PAP/)
[![Zenodo](https://img.shields.io/badge/Zenodo-10.5281%2Fzenodo.19069610-blue.svg)](https://doi.org/10.5281/zenodo.19069610)

## Abstract

This work addresses the Perfect Awareness Problem, a combinatorial optimization task that seeks the smallest set of initial spreaders required to ensure that all nodes in a social network become aware of given information. The study proposes a heuristic based on the Iterated Local Search framework that begins with an initial solution and is subsequently improved through local search and perturbation mechanisms. The local search employs a biased sampling scheme based on the weighted random sampling with reservoir technique, restricted to high-degree candidate nodes. Two existing network preprocessing techniques, edge reduction and leaf blindness, are applied to simplify the input instances. The algorithm is evaluated on benchmark networks of different sizes and densities. Experimental results show that the proposed method outperforms existing approaches in solution quality while maintaining competitive computation times. The algorithm achieves 0.22% deviation from best-known values, obtaining solutions with the best-known value for 813 out of 840 instances, including 12 new best-known solutions that had not been previously reported in the literature. Bayesian statistical analysis demonstrates superior performance with a highest winning probability versus the literature. Finally, the algorithm is evaluated on a set of large real-world instances, obtaining the best-known value in 7 instances and yielding 4 new best-known solutions with respect to the values previously reported in the literature, demonstrating its scalability and robustness across networks of vastly different sizes and structural properties.

**Keywords:** Perfect Awareness Problem, Social Networks Influence, Metaheuristics, Iterated Local Search

## Authors

- Cristian Pérez-Corral <sup>1</sup> <a href="mailto:cpercor@upv.es" aria-label="Cristian Pérez-Corral Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a> <a href="https://orcid.org/0009-0006-4616-1960"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>
- Isaac Lozano-Osorio <sup>2,*</sup> <a href="mailto:isaac.lozano@urjc.es" aria-label="Isaac Lozano-Osorio Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a> <a href="https://orcid.org/0000-0002-2608-8464"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>
- Anna Martínez-Gavara <sup>3</sup> <a href="mailto:gavara@uv.es" aria-label="Anna Martínez-Gavara Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a> <a href="https://orcid.org/0000-0001-9995-010X"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>
- Angel A. Juan <sup>4</sup> <a href="mailto:ajuanp@upv.es" aria-label="Angel A. Juan Email"><img src="https://upload.wikimedia.org/wikipedia/commons/b/b1/Email_Shiny_Icon.svg" alt="email" width="20" style="vertical-align:middle;"/></a> <a href="https://orcid.org/0000-0003-1392-1776"><img src="https://upload.wikimedia.org/wikipedia/commons/0/06/ORCID_iD.svg" alt="ORCID" width="20" style="vertical-align:middle;"/></a>

### Affiliations

1. Universitat Politècnica de València — Camí de Vera, S/N, València, 46022, Spain
2. GRAFO Research, Department of Computer Science and Statistics, Universidad Rey Juan Carlos — C/Tulipán, S/N, Móstoles, 28933, Spain
3. Departament d'Estadística i Investigació Operativa, Universitat de València — C/Doctor Moliner, 50, Burjassot, 46100, Spain
4. CIGIP, Universitat Politècnica de València — Plaça Ferrándiz i Carbonell, S/N, Alcoy, 03801, Spain

<sup>*</sup>Corresponding author.

---

## Table of Contents

- [Repository Structure](#repository-structure)
- [Datasets](#datasets)
- [Code Execution](#code-execution)
- [Requirements](#requirements)
- [Results](#results)
- [License](#license)
- [Funding](#funding)
- [Citation](#citation)

---

## Repository Structure

```
.
├── code/
│   ├── ILS/                     # Proposed algorithm (Java)
│   │   ├── src/                 # Source code (ILS, local search, preprocessing, evaluation)
│   │   └── PerfectAwarenessProblem.jar  # Pre-built jar (built from src/)
│   ├── DC/                      # Centrality-based heuristics from the literature (Python)
│   └── MIP/                     # Gurobi MIP model (Python)
├── instances/
│   ├── benchmark/               # 840 benchmark instances (.in)
│   └── real-world/              # 10 real-world social networks (.in)
├── solutions/
│   ├── benchmark-ils/           # ILS solution for each benchmark instance
│   ├── real-world-ils/          # ILS solution for each real-world instance
│   └── mip-1h/                  # Gurobi solution (1 h limit) for 569 benchmark instances
├── results.xlsx                 # Per-instance results and summary tables
├── check_solution.py            # Solution verifier (Python)
├── citation.cff
├── LICENSE
└── README.md
```

---

## Datasets

### Benchmark instances

The 840 benchmark instances in [`instances/benchmark/`](instances/benchmark/) were introduced by Pereira, de Rezende & de Souza (2021, [doi:10.1016/j.procs.2021.11.059](https://doi.org/10.1016/j.procs.2021.11.059)) and are available at [https://www.ic.unicamp.br/~cid/Problem-instances/Perfect-Awareness-Problem/](https://www.ic.unicamp.br/~cid/Problem-instances/Perfect-Awareness-Problem/): social networks with 10 to 1000 nodes and different edge densities. File names follow the pattern `<nodes>_<edges>_<m0>_social_<id>.in`.

Each file has the following format:

```
<seed>
<m0_parameter>
<number_nodes>
<number_edges>
<edge_list>        # one "u v" pair per line
```

### Real-world instances

The 10 real-world networks, used by Gautam, Kare & Bhavani (2023, [doi:10.1007/978-3-031-36402-0_8](https://doi.org/10.1007/978-3-031-36402-0_8)), are in [`instances/real-world/`](instances/real-world/), with the same format:

| Instance (paper) | File |
|---|---|
| Karate | `karate.in` |
| Jazz | `jazz.in` |
| Facebook | `facebook_combined.in` |
| Power grid | `USpowerGrid.in` |
| CA-GrQc | `ca-GrQc.in` |
| CA-HepTh | `ca-HepTh.in` |
| BlogCatalog3 | `soc-BlogCatalog-ASU.in` |
| CA-HepPh | `ca-HepPh.in` |
| BuzzNet | `soc-buzznet.in` |
| YouTube | `soc-YouTube-ASU.in` |

---

## Code Execution

All commands are run from the repository root.

### ILS (proposed algorithm)

Only a Java runtime is required. Run the ILS with the pre-built jar:

```bash
java -jar code/ILS/PerfectAwarenessProblem.jar <instance.in | instances_folder> <time_limit_seconds> [output_folder]
```

| Argument | Description |
|---|---|
| `instance.in` or `instances_folder` | A single instance file, or a folder: every `.in` file in it is solved, in alphabetical order |
| `time_limit_seconds` | Time limit per instance, in seconds (the paper uses 300 for the benchmark and 3600 for the real-world instances) |
| `output_folder` | Optional, default `solutions` |

Examples:

```bash
# One instance, 300 s
java -jar code/ILS/PerfectAwarenessProblem.jar instances/benchmark/100_1010_11_social_0.in 300

# All real-world instances, 1 h each, results in results-real-world/
java -jar code/ILS/PerfectAwarenessProblem.jar instances/real-world 3600 results-real-world
```

For each instance, the best solution is written to `<output_folder>/ils_<instance>.txt`:

```
<instance name>
<objective value (number of seed nodes)>
(<seed node ids>)
<time in ms>
```

and a line with the objective value, the total time, the time to the best solution and the feasibility check is appended to `<output_folder>/summary.csv`. The same information is printed on the console (lines starting with `RESULT`).

The instances are preprocessed in memory when they are read (adjacent nodes with degree ≤ 2 are merged), so the seed node ids refer to the reduced graph. No extra files are needed.

#### Checking a solution

[`check_solution.py`](check_solution.py) is an independent Python verifier (no external dependencies) that checks whether a solution file is feasible:

1. It reads the original instance and applies the same preprocessing as the Java code (adjacent nodes with degree ≤ 2 are merged), so that the seed node ids of the solution file refer to the same reduced graph.
2. It simulates the spreading process from the seed nodes:
   - every seed node is a spreader;
   - a node becomes **aware** as soon as at least one of its neighbours is a spreader;
   - a node becomes a **spreader** once at least half of its neighbours are spreaders, and then spreads to its own neighbours;
   - the process continues until no new spreaders appear.
3. The solution is **valid** if every node of the graph is aware at the end of the process.

Check a single solution (prints the number of reduced nodes, the seeds, the number of aware nodes and spreaders, the unreached nodes if any, and the result `VALID` / `INVALID`):

```bash
python3 check_solution.py instances/real-world/jazz.in solutions/real-world-ils/ils_jazz.txt
```

Check every instance of a folder that has a solution file `ils_<instance>.txt` in the solutions folder (prints the invalid ones, if any, and a final count):

```bash
python3 check_solution.py instances/benchmark solutions/benchmark-ils
# 840/840 valid  |  0 invalid
```

All the solutions included in [`solutions/benchmark-ils/`](solutions/benchmark-ils/) and [`solutions/real-world-ils/`](solutions/real-world-ils/) are valid. The jar also performs an independent feasibility check of the final solution (`feasible` column of `summary.csv`).

#### Building from source

```bash
cd code/ILS
javac -d build src/*.java
jar cfe PerfectAwarenessProblem.jar Main -C build .
```

### DC (centrality-based heuristics)

[`code/DC/centrality_heuristics.py`](code/DC/centrality_heuristics.py) implements the centrality-based heuristics of Gautam, Kare & Bhavani (MIWAI 2023, [doi:10.1007/978-3-031-36402-0_8](https://doi.org/10.1007/978-3-031-36402-0_8)) with five centrality measures: degree (DC, the one reported in the paper), PageRank (PR), eigenvector (EC), closeness (CC) and betweenness (BC).

```bash
python code/DC/centrality_heuristics.py instances/real-world/karate.in instances/real-world/jazz.in
```

Results are printed and saved as one CSV per instance in `code/DC/results_centrality/`. Each heuristic is limited to 2 hours per instance (`PER_ALGO_TIMEOUT_SECS` environment variable). To run every real-world instance in parallel and aggregate the CSVs into an Excel file:

```bash
cd code/DC
./run_centrality_heuristics_all.sh
```

### MIP model (Gurobi)

[`code/MIP/pap_mip.py`](code/MIP/pap_mip.py) solves the time-indexed formulation (|V|² binary variables) with Gurobi:

```bash
python code/MIP/pap_mip.py instances/benchmark/20_42_2_social_0.in --time-limit 3600 --out-dir solutions/mip
```

For each instance it writes `<out-dir>/<instance>.txt` with the objective value and the time in seconds, the same format as [`solutions/mip-1h/`](solutions/mip-1h/).


## Requirements

- **ILS:** Java 21 or later. No external dependencies. The verifier only needs Python 3.
- **DC:** Python 3 with `networkx` (and `pandas` + `openpyxl` for the aggregation script).
- **MIP:** Python 3 with `gurobipy` (requires a valid Gurobi license, see [gurobi.com](https://www.gurobi.com)) and `networkx`.

## Results

### Benchmark instances (840)

The solution (seed set and time) found for every instance is in [`solutions/`](solutions/), with the formats described in [Code Execution](#code-execution). Detailed per-instance results are in [`results.xlsx`](results.xlsx):

- **Benchmark (840):** objective value of GR (best previous method in the literature) and ILS, ILS computation time, best value and deviation for every instance.
- **Summary:** averages by size group, computed with live spreadsheet formulas.
- **MIP vs ILS:** Gurobi (1 h) against ILS on the 569 instances where the model was run.
- **Real-world (Table 6):** values reported in the paper for GR, DC (our reimplementation and the values reported by Gautam et al.) and ILS.

| #Nodes | Algorithm | Avg OF | Avg Dev (%) | #Best |
|---|---|---|---|---|
| [0, 1000] | GR | 6.32 | 0.82 | 807 |
| [0, 1000] | **ILS** | **6.30** | **0.22** | **813** |

### Real-world instances

| Instance | GR | DC | ILS |
|---|---|---|---|
| Karate | **3** | **3 (3)** | **3** |
| Jazz | **13** | 18 (8) | **13** |
| Facebook | **10** | **10 (10)** | **10** |
| Power grid | 602 | **571 (561)** | 574 |
| CA-GrQc | 769 | 761 (727) | **454** |
| CA-HepTh | 1164 | 1129 (1043) | **725** |
| BlogCatalog3 | 208 | **131 (130)** | 207 |
| CA-HepPh | 1243 | 969 (920) | **887** |
| BuzzNet | **125** | -- | 126 |
| YouTube | 38668 | -- | **38519 (35878)** |

Following Gautam et al. (2023), a time limit of one hour per execution was used. GR and DC are the heuristics of [Gautam et al. (2023)](https://doi.org/10.1007/978-3-031-36402-0_8); DC was reimplemented by us ([`code/DC/`](code/DC/)): the value outside parentheses is our reimplementation and the value in parentheses is the one originally reported by Gautam et al. A dash means that no feasible solution was obtained within one hour. For YouTube, the value in parentheses corresponds to an extended run of ILS.

The ILS solution for each instance is in [`solutions/real-world-ils/`](solutions/real-world-ils/) (seed node ids refer to the instance after preprocessing).

<!-- TODO: YouTube. Run the jar until it reaches 35878 (the value reported in the paper) and replace solutions/real-world-ils/ils_soc-YouTube-ASU.txt (currently the final solution of the 48 h run, 35877):
     nohup java -jar code/ILS/PerfectAwarenessProblem.jar instances/real-world/soc-YouTube-ASU.in 54000 results-youtube > youtube.log 2>&1 &
     In the original 48 h run, 35878 was reached at 13.97 h and 35877 at 38.7 h, so a 15 h limit (54000 s) should stop at 35878.
     Also review the times reported in the paper: the convergence figure was generated with plot_ils_convergence.py --max-time 50400 --time-scale 2.03665 (real time: 38519 at 2.03 h, 35878 at 13.97 h). -->

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0) - see the [LICENSE](LICENSE) file for details.

## Funding

This research was supported by:

- **Comunidad Autónoma de Madrid** - Grant ref. TEC-2024/COM-404
- **MICIU/AEI/10.13039/501100011033 and ERDF/FEDER, European Union** - Grant ref. PID2024-160226OB-C21
- **MICIU/AEI/10.13039/501100011033 and ERDF/FEDER, European Union** - Grant ref. PID2024-156045NB-I00
- **Ministerio para la Transformación Digital y de la Función Pública** - Cátedra ENIA AI4DDS, Grant ref. TSI-100930-2023-3
- **Generalitat Valenciana** - Grant ref. CIAICO/2026/100

The funders had no role in study design, data collection and analysis, decision to publish, or preparation of the manuscript.

## Citation

If you use this work in your research, please cite our paper:

### DOI

https://doi.org/TODO-DOI

Code, instances and results are archived in Zenodo: https://doi.org/10.5281/zenodo.19069610 (project page: https://grafo.etsii.urjc.es/PAP/).

### Bibtex

```bibtex
@article{perezcorral2026pap,
  title={An Iterated Local Search to the Perfect Awareness Problem},
  author={P{\'e}rez-Corral, Cristian and Lozano-Osorio, Isaac and Mart{\'i}nez-Gavara, Anna and Juan, Angel A.},
  journal={Journal of Heuristics},
  volume={TODO},
  number={TODO},
  pages={TODO},
  year={2026},
  publisher={Springer},
  doi={TODO-DOI}
}
```

To cite the code and data:

```bibtex
@misc{PAP_zenodo,
  doi={10.5281/ZENODO.19069610},
  author={P{\'e}rez-Corral, Cristian and Lozano-Osorio, Isaac and Mart{\'i}nez-Gavara, Anna and Juan, Angel A.},
  title={Perfect Awareness Problem},
  publisher={Zenodo},
  year={2026},
  url={https://grafo.etsii.urjc.es/PAP/}
}
```

### APA Format
Pérez-Corral, C., Lozano-Osorio, I., Martínez-Gavara, A., & Juan, A. A. (2026). An Iterated Local Search to the Perfect Awareness Problem. *Journal of Heuristics*, TODO. https://doi.org/TODO-DOI

### IEEE Format
C. Pérez-Corral, I. Lozano-Osorio, A. Martínez-Gavara, and A. A. Juan, "An Iterated Local Search to the Perfect Awareness Problem," *Journal of Heuristics*, TODO, 2026, doi: TODO-DOI.
