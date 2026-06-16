/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.population;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

import java.nio.file.Path;

/**
 * @author kn
 * @author jlaudan
 */
class RunPopulationDownsamplingExample {

    private final String inputPopFilename;
    private final String outputPopFilename;

    private RunPopulationDownsamplingExample(String inputPopFilename, String outputPopFilename) {
        this.inputPopFilename = inputPopFilename;
        this.outputPopFilename = outputPopFilename;
    }

    public static void main(final String[] args) {

        String inputPopFilename = "src/main/java/org/matsim/population/output/full_population.xml.gz";
        String outputPopFilename = "src/main/java/org/matsim/population/output/full_population_1percent_sample.xml.gz";

        RunPopulationDownsamplingExample app = new RunPopulationDownsamplingExample(inputPopFilename, outputPopFilename);
        app.run();
    }

    private void run() {
        final String forcedAgentId = "KRISTINA";
        final double samplingRate = 0.01;
        final long baseSeed = 12345L;
        final int numberOfSamples = 10;
        final java.util.Random rng = new java.util.Random();

        for (int sampleIndex = 1; sampleIndex <= numberOfSamples; sampleIndex++) {
            String sampleOutputPopFilename = outputPopFilename.replace(
                    ".xml.gz",
                    "_" + sampleIndex + ".xml.gz"
            );

            writeSample(forcedAgentId, samplingRate, baseSeed + sampleIndex, sampleOutputPopFilename);
        }
    }

    private void writeSample(
            String forcedAgentId,
            double samplingRate,
            long seed,
            String sampleOutputPopFilename
    ) {
        final java.util.Random rng = new java.util.Random(seed);

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationWriter writer = new StreamingPopulationWriter(1.0); // write all that reach it

        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(person -> {
            if (person.getId().toString().equals(forcedAgentId) || rng.nextDouble() < samplingRate) {
                writer.writePerson(person);
            }
        });

        try {
            writer.startStreaming(sampleOutputPopFilename);
            reader.readFile(inputPopFilename);
        } finally {
            writer.closeStreaming();
        }
    }
}