package trip.planner.osm.api


class PlaceValidator {

    static final Double DIFFERENCE_BETWEEN_POINTS = 1.0

    static List<Pair<Point, Point>> mergeLists(List<Double> lons, List<Double> lats) {
        if (lons.size() == 0 || lats.size() == 0) {
            return Collections.emptyList()
        }
        List<Pair<Point, Point>> mergedList = new ArrayList<>()

        for (int i = 0; i < lons.size() - 1; i++) {
            for (int j = 0; j < lats.size() - 1; j++) {
                mergedList.add(new Pair<>(new Point(lons.get(i), lats.get(j)),
                        new Point(lons.get(i + 1), lats.get(j + 1))))
            }
        }
        mergedList
    }

    static List<Double> splitCoords(double start, double destination) {
        List<Double> coords = new ArrayList<>()
        if (destination - start > DIFFERENCE_BETWEEN_POINTS) {
            for (double i = start; i < destination; i++) {
                coords.add(i)
            }
            coords.add(destination)
        } else {
            coords.add(start)
            coords.add(destination)
        }
        coords
    }

    static List<Pair<Point, Point>> split(POIApi api) {
        List<Double> lons = splitCoords(api.getStartLon(), api.getDestinationLon())
        List<Double> lats = splitCoords(api.getStartLat(), api.getDestinationLat())
        mergeLists(lons, lats)
    }
}
