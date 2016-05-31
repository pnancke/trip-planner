package trip.planner.osm.api

class Point {

    double lon
    double lat

    Point(double lon, double lat) {
        this.lon = lon
        this.lat = lat
    }


    @Override
    public String toString() {
        return "Point{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
