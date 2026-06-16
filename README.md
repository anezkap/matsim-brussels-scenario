# MATSim Brussels scenario

This is an adapted repository of the [MATSim example project](https://github.com/matsim-org/matsim-examples),
containing the Brussels commuters scenario.

The input synthetic population was created using the [Synthetic Population Pipeline](https://github.com/anezkap/synthetic-population-pipeline).
  
### Import into eclipse

1. download a modern version of eclipse. This should have maven and git included by default.
1. `file->import->git->projects from git->clone URI` and clone as specified above.  _It will go through a 
sequence of windows; it is important that you import as 'general project'._
1. `file->import->maven->existing maven projects`

Sometimes, step 3 does not work, in particular after previously failed attempts.  Sometimes, it is possible to
right-click to `configure->convert to maven project`.  If that fails, the best thing seems to remove all 
pieces of the failed attempt in the directory and start over.

### Import into IntelliJ

`File -> New -> Project from Version Control` paste the repository url and hit 'clone'. IntelliJ usually figures out
that the project is a maven project. If not: `Right click on pom.xml -> import as maven project`.

### Java Version

The project uses Java 11. Usually a suitable SDK is packaged within IntelliJ or Eclipse. Otherwise, one must install a 
suitable sdk manually, which is available [here](https://openjdk.java.net/)

### Building and Running it locally

You can build an executable jar-file by executing the following command:

```sh
./mvnw clean package
```

or on Windows:

```sh
mvnw.cmd clean package
```

This will download all necessary dependencies (it might take a while the first time it is run) and create a file `matsim-example-project-0.0.1-SNAPSHOT.jar` in the top directory. Rename it to `matsim.jar` (the name expected by the SLURM job scripts):

```sh
mv matsim-example-project-0.0.1-SNAPSHOT.jar matsim.jar
```

The jar can then be run from the command line, from RunMatsim.java in the IDE, or submitted to a supercomputer — see the Scenarios section below for instructions.

### Scenarios

All policy scenarios are in `scenarios/brussels/` and start from the converged baseline output plans (`baseline_scenario_output_plans.xml.gz`). Each scenario isolates one intervention to cycling or car use.

#### Baseline (`baseline_scenario_10pct`)
The calibrated reference run. Car availability is respected from household attributes (`considerCarAvailability=true`) and company car users are restricted to using a car. All three bicycle disutility factors are active: surface comfort, road gradient, and infrastructure quality each penalise cycling.

#### Bicycle scenarios
These scenarios switch off individual cycling disutility factors to test the sensitivity of mode choice to each:

| Scenario | Comfort penalty | Gradient penalty | Infrastructure penalty |
|---|---|---|---|
| `biking_icg` | off | off | off |
| `comfort` | **off** | on | on |
| `gradient` | on | **off** | on |
| `infrastructure` | on | on | **off** |

`biking_icg` removes all three factors simultaneously, representing ideal cycling conditions across the board. The individual scenarios isolate the effect of e.g. smoother road surfaces (`comfort`), e-bikes eliminating hill effort (`gradient`), or a fully built-out cycling network (`infrastructure`).

#### Constraints testing scenarios

**`car_availability`** — sets `considerCarAvailability=false` so every agent may choose car regardless of household car ownership. All bicycle factors remain active. Tests what mode shares would look like if car access were universal.

**`company_cars`** — gives the `company_car` subpopulation the option to choose a different mode of transport, while setting the daily monetary cost and per-km distance cost of their car travel (`dailyMonetaryConstant`, `monetaryDistanceRate`) to zero.

**`cycling_allowance`** — all scenarios include the Belgian cycling allowance as a per-km monetary bonus on every cycling link (via `--bikingAllowancePerKm`). Most scenarios run with the standard €0.35/km. This scenario specifically *varies* the allowance: the SLURM array job sweeps values from €0.00 to €0.55/km across 10 parallel runs to test how the modal shift responds to the allowance level.

**`cycling_allowance_company_cars`** — combines the cycling allowance with the company car modal choice contraint removed, testing both together.

**`parking`** — increases the car daily monetary constant from −€5.30 to −€6.30, representing a €1/day increase in parking charges.

#### Running a scenario locally

Pass a scenario config to `RunMatsim.java` via the `--config` flag:

```sh
java -Xmx16g -jar matsim.jar run \
     --config scenarios/brussels/cycling_allowance/config_cycling_allowance.xml \
     --bikingAllowancePerKm 0.35
```

All scenarios accept `--bikingAllowancePerKm`; omit it to use the default of €0.35/km.

#### Running on a supercomputer (Snellius)

Each scenario folder contains a SLURM job script (e.g. `cycling_allowance.sh` for array runs, `cycling_allowance_single.sh` for a single run). Before submitting, update the `--mail-user` address in the script. Then submit with:

```sh
sbatch scenarios/brussels/cycling_allowance/cycling_allowance.sh
```

Results are copied back to `~/results/run_<RUN_ID>/` and key stats (`scorestats.csv`, `modestats.csv`) to `~/matsim-example-project/results/run_<RUN_ID>/`.

### Troubleshooting
If you are running the scenario locally, make sure you have updated the memory in configuration of the run
by adding VM options like -Xmx16g.

We recommend to test with 5pct sample and at least 300 iterations. 1pct sample was shown to be unstable.
The baseline requires 1000 iterations for full convergence. Policy scenarios start from the converged baseline plans and reach convergence in ~800 iterations.

### Licenses
The **MATSim program code** in this repository is distributed under the terms of the [GNU General Public License as published by the Free Software Foundation (version 2)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html). The MATSim program code are files that reside in the `src` directory hierarchy and typically end with `*.java`.

The **MATSim input files, output files, analysis data and visualizations** are licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/80x15.png" /></a><br /> MATSim input files are those that are used as input to run MATSim. They often, but not always, have a header pointing to matsim.org. They typically reside in the `scenarios` directory hierarchy. MATSim output files, analysis data, and visualizations are files generated by MATSim runs, or by postprocessing.  They typically reside in a directory hierarchy starting with `output`.