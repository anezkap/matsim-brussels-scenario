This repository expects two files:
- `belgium.osm.pbf` -- the network file
- `DTM_RBC_50cm.tif` -- the digital terrain model

To get the `belgium.osm.pbf` file, you can follow these steps:

1. Download data from GeoFabrik: https://download.geofabrik.de/europe/belgium.html
2. Run the following in the terminal (adjust file names as needed):

```bash
# All roads in Brussels
osmosis --read-pbf-fast file=belgium-260204.osm.pbf \
  --bounding-box top=50.9300 left=4.2300 bottom=50.7500 right=4.5100 \
  completeWays=true --used-node \
  --write-pbf brussels_network.osm.pbf

# Major roads in Belgium
osmosis --read-pbf-fast file=belgium-260204.osm.pbf --tf accept-ways \
  highway=motorway,motorway_link,trunk,trunk_link,primary,primary_link \
  --used-node \
  --write-pbf bigroads_belgium_network.osm.pbf

# Merged network
osmosis --rb file=bigroads_belgium_network.osm.pbf \
  --read-pbf-fast brussels_network.osm.pbf --merge \
  --write-pbf belgium.osm.pbf
```

The `DTM_RBC_50cm.tif` can be downloaded from https://datastore.brussels/web/data/dataset/1d7bd49d-fe83-4388-af85-6f5dc8ec7909