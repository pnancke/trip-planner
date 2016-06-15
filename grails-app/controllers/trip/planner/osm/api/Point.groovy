package trip.planner.osm.api

class Point {

    Double lon
    Double lat

    Point(Double lon, Double lat) {
        Objects.nonNull(lon)
        Objects.nonNull(lat)

        this.lon = lon
        this.lat = lat
    }

    public String toStringDarrinWardFormat() {
        return "$lat,$lon\n"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Point point = (Point) o

        if (Double.compare(point.lat, lat) != 0) return false
        if (Double.compare(point.lon, lon) != 0) return false

        return true
    }

    int hashCode() {
        int result
        long temp
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L
        result = (int) (temp ^ (temp >>> 32))
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L
        result = 31 * result + (int) (temp ^ (temp >>> 32))
        return result
    }

    @Override
    public String toString() {
        return "[" + lon + "," + lat + "]";
    }
}
