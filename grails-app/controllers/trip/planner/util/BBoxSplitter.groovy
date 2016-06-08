package trip.planner.util

import trip.planner.osm.api.BBox
import trip.planner.osm.api.Point

class BBoxSplitter {
    static List<BBox> mergeLists(List<Double> lons, List<Double> lats) {
        if (lons.size() == 0 || lats.size() == 0) {
            return Collections.emptyList()
        }
        List<BBox> mergedList = new ArrayList<>()

        for (int i = 0; i < lons.size() - 1; i++) {
            for (int j = 0; j < lats.size() - 1; j++) {
                mergedList.add(new BBox(new Point(lons.get(i), lats.get(j)),
                        new Point(lons.get(i + 1), lats.get(j + 1))))
            }
        }
        mergedList
    }

    static List<Double> splitCoords(double start, double destination, double difference) {
        if (difference <= 0.0) {
            throw new IllegalArgumentException("Difference $difference is equals 0 or negative!")
        }
        println "start: $start, destination: $destination, difference: $difference"
        List<Double> coords = new ArrayList<>()
        if (destination - start > difference) {
            for (double i = start; i < destination; i = i + difference) {
                coords.add(i)
            }
            coords.add(destination)
        } else {
            coords.add(start)
            coords.add(destination)
        }
        coords
    }
}
