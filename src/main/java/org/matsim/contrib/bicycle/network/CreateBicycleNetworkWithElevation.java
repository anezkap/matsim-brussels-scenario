package org.matsim.contrib.bicycle.network;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.osm.networkReader.OsmBicycleReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CreateBicycleNetworkWithElevation {

    private static final String outputCRS = "EPSG:31370"; // Belgian Lambert 72
    private static final String tiffFileCRS = "EPSG:31370"; // Belgian Lambert 72

    private static final String inputOsmFile = "inputs_network/belgium_merged_network.osm.pbf";
    private static final String inputTiffFile = "inputs_network/DTM_RBC_50cm.tif";
    private static final String outputFile = "belgium_network.xml.gz";

    // This file is not publicly available, only de-comment if you have access to it
//    private static final String quietnessFile = "inputs_network/Bruxelles_Cyclability_Data.geojson";

    public static void main(String[] args) throws IOException {

        var elevationParser = new ElevationDataParser(inputTiffFile, outputCRS, tiffFileCRS);

        // The OSM data are usually in WGS84
        var transformation = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, outputCRS);

        // Pre-load quietness map once -- only if you have the data available
//        Map<Long, Integer> quietnessMap = QuietnessLoader.loadQuietnessMap(quietnessFile);

        var network = new OsmBicycleReader.Builder()
                .setCoordinateTransformation(transformation)
                .setAfterLinkCreated((link, tags, direction) -> {
                    addElevationIfNecessary(link.getFromNode(), elevationParser);
                    addElevationIfNecessary(link.getToNode(), elevationParser);
//                    addQuietness(link, quietnessMap);
                })
                .build()
                .read(inputOsmFile);

        NetworkUtils.cleanNetwork(network, Set.of(TransportMode.car, TransportMode.bike));

        new NetworkWriter(network).write(outputFile);
    }

    private static synchronized void addElevationIfNecessary(Node node, ElevationDataParser elevationParser) {
        if (node.getCoord().hasZ()) return; // already has elevation, skip

        Coord coord = node.getCoord();

        try {
            double elevation = elevationParser.getElevation(coord);

            // Clamp invalid/null values (TIFF uses -999 for no-data)
            if (Double.isNaN(elevation) || elevation < -500) {
                elevation = 50.0;
            }

            node.setCoord(CoordUtils.createCoord(coord.getX(), coord.getY(), elevation));

        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            // Node is outside the TIFF bounds — default to elevation 30
            node.setCoord(CoordUtils.createCoord(coord.getX(), coord.getY(), 50.0));
        }
    }

    private static void addQuietness(Link link, Map<Long, Integer> quietnessMap) {
        Object origIdObj = link.getAttributes().getAttribute("origid");
        if (origIdObj != null) {
            long origId = ((Number) origIdObj).longValue();
            Integer quietness = quietnessMap.get(origId);
            if (quietness != null) {
                link.getAttributes().putAttribute("quietness", quietness);
            }
        }
    }
}