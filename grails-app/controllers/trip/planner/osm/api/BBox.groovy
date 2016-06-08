package trip.planner.osm.api

import com.google.common.base.Preconditions
import trip.planner.util.BBoxSplitter

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

    /**
     *
     * @param differenceLon - difference in longitudinal direction
     * @param differenceLat - difference in latitudinal direction
     * @return bboxes - a list of bboxes
     */
    List<BBox> splitWithDifferences(double differenceLon, double differenceLat) {
        List<Double> lons = BBoxSplitter.splitCoords(this.bbox.getA().lon, this.bbox.getB().lon, differenceLon)
        List<Double> lats = BBoxSplitter.splitCoords(this.bbox.getA().lat, this.bbox.getB().lat, differenceLat)
        BBoxSplitter.mergeLists(lons, lats)
    }

    /**
     *
     * @param difference - difference in longitudinal and latitudinal direction
     * @return bboxes - a list of bboxes
     */
    List<BBox> splitWithDifference(double difference) {
        List<Double> lons = BBoxSplitter.splitCoords(this.bbox.getA().lon, this.bbox.getB().lon, difference)
        List<Double> lats = BBoxSplitter.splitCoords(this.bbox.getA().lat, this.bbox.getB().lat, difference)
        BBoxSplitter.mergeLists(lons, lats)
    }

    /**
     *
     * @param dimension - the dimension (pieces in latitudinal or longitudinal direction)
     * @return bboxes - a list of bboxes
     */
    List<BBox> splitIntoGridWithDim(int dimension) {
        double diffLon = this.bbox.getB().lon - this.bbox.getA().lon
        double diffLat = this.bbox.getB().lat - this.bbox.getA().lat
        splitWithDifferences(diffLon / dimension, diffLat / dimension)
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

    boolean contains(Point point) {
        return (start.lon <= point.lon
                && start.lat <= point.lat
                && end.lon >= point.lon
                && end.lat >= point.lat)
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

    public String toStringDarrinWardFormat() {
        return "$bbox.a.lat,$bbox.a.lon\n$bbox.b.lat,$bbox.b.lon\n"
    }
}
