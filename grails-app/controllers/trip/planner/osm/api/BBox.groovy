package trip.planner.osm.api

import com.google.common.base.Preconditions

class BBox {
    Pair<Point, Point> bbox

    BBox(double startLon, double startLat, double destinationLon, double destinationLat) {
        if (validateCoords(startLon, startLat, destinationLon, destinationLat)) {
            double tmpLon = startLon
            startLon = destinationLon
            destinationLon = tmpLon

            double tmpLat = startLat
            startLat = destinationLat
            destinationLat = tmpLat
        }
        this.bbox = new Pair<Point, Point>(new Point(startLon, startLat)
                , new Point(destinationLon, destinationLat))
    }

    BBox(Point start, Point end) {
        this(start.lon, start.lat, end.lon, end.lat)
    }

    BBox(Pair<Point, Point> bbox) {
        this(bbox.a.lon, bbox.a.lat, bbox.b.lon, bbox.b.lat)
    }

    /**
     * @param startLon
     * @param startLat
     * @param destinationLon
     * @param destinationLat
     * @return swapIsNecessary
     */
    static boolean validateCoords(double startLon, double startLat, double destinationLon, double destinationLat) {
        Preconditions.checkNotNull(startLon)
        Preconditions.checkNotNull(startLat)
        Preconditions.checkNotNull(destinationLon)
        Preconditions.checkNotNull(destinationLat)

        if (startLat == destinationLat && destinationLon == startLon) {
            throw new IllegalArgumentException("The start-position is equals to the destination-position.")
        }

        return !(startLon < destinationLon && startLat < destinationLat)
    }

    Pair<Point, Point> boxed() {
        return bbox
    }

    void set(Pair<Point, Point> bbox) {
        this.bbox = bbox
    }

    Point getStart() {
        return bbox.a
    }

    void setStart(Point start) {
        this.bbox.a = start
    }

    Point getEnd() {
        return bbox.b
    }

    void setEnd(Point end) {
        this.bbox.b = end
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        BBox bBox = (BBox) o

        if (bbox != bBox.bbox) return false

        return true
    }

    int hashCode() {
        return (bbox != null ? bbox.hashCode() : 0)
    }

    @Override
    public String toString() {
        return "$bbox.a.lon,$bbox.a.lat,$bbox.b.lon,$bbox.b.lat";
    }
}
