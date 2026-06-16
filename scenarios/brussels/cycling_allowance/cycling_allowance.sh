#!/bin/bash
#SBATCH --job-name="cycling_allowance"
#SBATCH --array=1-10
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=16
#SBATCH --mem=28G
#SBATCH --time=30:00:00
#SBATCH --partition=rome
#SBATCH --mail-type=BEGIN,END,FAIL,ARRAY_TASKS
#SBATCH --mail-user=example@email.com
#SBATCH --output=logs/%x_%A_%a.out
#SBATCH --error=logs/%x_%A_%a.err

# Define parameter values for each array task
BIKING_VALUES=(0.00 0.20 0.25 0.30 0.35 0.37 0.40 0.45 0.50 0.55)
BIKING_ALLOWANCE=${BIKING_VALUES[$SLURM_ARRAY_TASK_ID - 1]}

RUN_ID="cycling_allowance_${SLURM_ARRAY_TASK_ID}"
RESULTS_DIR="$HOME/results/run_$RUN_ID"
THESIS_RESULTS_DIR="$HOME/matsim-example-project/results/run_$RUN_ID"

echo "== Starting job on $(hostname) at $(date) =="
echo "== Biking allowance set to $BIKING_ALLOWANCE =="

# Create logs directory if it doesn't exist
mkdir -p "$HOME/matsim-example-project/logs"

# Copy input files to fast local scratch
echo "== Copying input files to scratch at $(date) =="
cp -r $HOME/matsim-example-project $TMPDIR/ || { echo "ERROR: Failed to copy input files"; exit 1; }
cd $TMPDIR/matsim-example-project/scenarios/brussels/cycling_allowance

# Run MATSim
echo "== Running MATSim at $(date) =="
java -Xms24G -Xmx24G \
     -XX:+AlwaysPreTouch \
     -XX:+UseParallelGC \
     -XX:ParallelGCThreads=16 \
     -jar ../../../matsim.jar run \
     --config config_cycling_allowance.xml \
     --yaml ../asc_params.yaml \
     --output $TMPDIR/matsim_run/runs/$RUN_ID \
     --runId $RUN_ID \
     --config:controler.lastIteration 800 \
     --bikingAllowancePerKm $BIKING_ALLOWANCE \
     || { echo "ERROR: MATSim failed — skipping result copy"; exit 1; }

# Copy results back
echo "== Copying results back at $(date) =="
mkdir -p "$RESULTS_DIR" || { echo "ERROR: Failed to create results dir"; exit 1; }
mkdir -p "$THESIS_RESULTS_DIR" || { echo "ERROR: Failed to create thesis results dir"; exit 1; }

cp -r "$TMPDIR/matsim_run/runs/$RUN_ID"/* "$RESULTS_DIR/" \
     || { echo "ERROR: Failed to copy full results"; exit 1; }

cp "$TMPDIR/matsim_run/runs/$RUN_ID/$RUN_ID.scorestats.csv" "$THESIS_RESULTS_DIR/scorestats.csv" \
     || { echo "WARNING: scorestats.csv not found"; }

cp "$TMPDIR/matsim_run/runs/$RUN_ID/$RUN_ID.modestats.csv" "$THESIS_RESULTS_DIR/modestats.csv" \
     || { echo "WARNING: modestats.csv not found"; }

echo "== DONE at $(date) =="