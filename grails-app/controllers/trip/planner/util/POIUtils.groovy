package trip.planner.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import trip.planner.osm.api.BBox
import trip.planner.osm.api.Point

import java.util.stream.Collectors

class POIUtils {

    private static Log log = LogFactory.getLog(POIUtils.class)

    public static BBox calcBBoxOfRoute(List<Point> route) {
        List<Double> lats = route.stream().map({ point -> point.lat }).collect(Collectors.toList())
        List<Double> lons = route.stream().map({ point -> point.lon }).collect(Collectors.toList())
        new BBox(lons.min(), lats.min(), lons.max(), lats.max())
    }

    public static List<BBox> calcResultingBBox(List<Point> route, double travelTime) {
        Objects.nonNull(travelTime)
        Objects.nonNull(route)
        BBox bbox = calcBBoxOfRoute(route)
        Integer rate = rateTravelTime(travelTime)
        log.info "rate split of bboxes: $rate, depending on travelTime: $travelTime"
        List<BBox> bboxes = bbox.splitIntoGridWithDim(rate)
        List resultingBBoxes = bboxes.stream().filter({
            box ->
                route.stream().anyMatch(
                        { r -> box.contains(r) })
        }).collect(Collectors.toList())
        resultingBBoxes
    }

    static Integer rateTravelTime(double value) {
        Objects.nonNull(value)
        if (boundCheck(Integer.MIN_VALUE, 0, value)) {
            throw new IllegalArgumentException("Given traveltime $value is out of Range.")
        } else if (boundCheck(0, 15, value)) {
            return 1
        } else if (boundCheck(15, 30, value)) {
            return 2
        } else if (boundCheck(30, 60, value)) {
            return 3
        } else if (boundCheck(60, 70, value)) {
            return 4
        } else if (boundCheck(70, 100, value)) {
            return 5
        } else if (boundCheck(100, 130, value)) {
            return 6
        } else if (boundCheck(130, 150, value)) {
            return 7
        } else if (boundCheck(150, 170, value)) {
            return 9
        } else if (boundCheck(170, 200, value)) {
            return 12
        } else if (boundCheck(200, 300, value)) {
            return 13
        } else if (boundCheck(300, 400, value)) {
            return 14
        } else if (boundCheck(400, 450, value)) {
            return 16
        } else if (boundCheck(450, 500, value)) {
            return 20
        } else if (boundCheck(500, 700, value)) {
            return 22
        } else if (boundCheck(700, 1000, value)) {
            return 25
        } else if (boundCheck(1000, Integer.MAX_VALUE, value)) {
            throw new IllegalArgumentException("Given traveltime $value is out of Range.")
        }
        null
    }

    private static boolean boundCheck(int lower, int higher, double value) {
        return (value.toInteger() >= lower) && (value.toInteger() <= higher)
    }
}
