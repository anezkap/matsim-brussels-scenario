#!/bin/bash
#SBATCH --job-name="parking"
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=16
#SBATCH --mem=28G
#SBATCH --time=30:00:00
#SBATCH --partition=rome
#SBATCH --mail-type=BEGIN,END,FAIL
#SBATCH --mail-user=example@email.com
#SBATCH --output=logs/%x_%j.out
#SBATCH --error=logs/%x_%j.err

# Configuration parameters
RUN_ID="parking"
BIKING_ALLOWANCE="0.45"
RESULTS_DIR="$HOME/results/run_$RUN_ID"
THESIS_RESULTS_DIR="$HOME/matsim-example-project/results/run_$RUN_ID"

echo "== Starting job on $(hostname) at $(date) =="

echo "== Biking allowance set to $BIKING_ALLOWANCE =="

# Create logs directory if it doesn't exist
mkdir -p "$HOME/matsim-example-project/logs"

# Copy input files to fast local scratch
cp -r $HOME/matsim-example-project $TMPDIR/
cd $TMPDIR/matsim-example-project/scenarios/brussels/parking

# Run MATSim
echo "== Running MATSim =="
java -Xms24G -Xmx24G \
     -XX:+AlwaysPreTouch \
     -XX:+UseParallelGC \
     -XX:ParallelGCThreads=16 \
     -jar ../../../matsim.jar run \
     --config config_parking.xml \
     --yaml ../asc_params.yaml \
     --output $TMPDIR/matsim_run/runs/$RUN_ID \
     --runId $RUN_ID \
     --config:controler.lastIteration 800 \
     --bikingAllowancePerKm $BIKING_ALLOWANCE \
     || { echo "ERROR: MATSim failed — skipping result copy"; exit 1; }

# Copy results back
echo "== Copying results back at $(date) =="
mkdir -p "$RESULTS_DIR"
mkdir -p "$THESIS_RESULTS_DIR"

cp -r "$TMPDIR/matsim_run/runs/$RUN_ID"/* "$RESULTS_DIR/"

cp "$TMPDIR/matsim_run/runs/$RUN_ID/$RUN_ID.scorestats.csv" "$THESIS_RESULTS_DIR/scorestats.csv"
cp "$TMPDIR/matsim_run/runs/$RUN_ID/$RUN_ID.modestats.csv" "$THESIS_RESULTS_DIR/modestats.csv"

echo "== DONE at $(date) =="