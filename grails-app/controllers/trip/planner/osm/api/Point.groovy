package trip.planner.osm.api

class Point {

    Double lat
    Double lon
    String name
    String wiki
    String poiId

    Point(Double lat, Double lon) {
        Objects.requireNonNull(lat)
        Objects.requireNonNull(lon)

        this.lat = lat
        this.lon = lon
    }

    Point(Double lat, Double lon, Long poiId) {
        Objects.requireNonNull(lat)
        Objects.requireNonNull(lon)
        Objects.requireNonNull(poiId)

        this.lat = lat
        this.lon = lon
        this.poiId = poiId
    }

    Point(Double lat, Double lon, Long poiId, String name, String wiki) {
        Objects.requireNonNull(lat)
        Objects.requireNonNull(lon)
        Objects.requireNonNull(poiId)
        Objects.requireNonNull(name)

        this.lat = lat
        this.lon = lon
        this.poiId = poiId
        this.name = name
        if (wiki == null) {
            this.wiki = ""
        } else {
            this.wiki = wiki
        }
    }

    void setName(String name) {
        this.name = name
    }

    void setWiki(String wiki) {
        this.wiki = wiki
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

    public String toShortString() {
        return lat + ' ' + lon
    }

    @Override
    public String toString() {
        return "[" + lat + "," + lon + "]";
    }
}
