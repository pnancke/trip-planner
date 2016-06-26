package trip.planner.util

import trip.planner.PointOfInterest
import trip.planner.osm.api.Point

import java.util.stream.Collectors


class ListUtils {

    /**
     * @see trip.planner.HomeController
     * @param route - note: this type will be used in HomeController for the json request.
     *
     * @return points - List<Point>
     */
    public static List<Point> mapToPoints(List<List<String>> route) {
        route.stream().map({ strings ->
            new Point(Double.valueOf(strings.get(1)), Double.valueOf(strings.get(0)))
        }).collect(Collectors.toList())
    }

    public static String readablePoints(List<Point> points) {
        return points*.toStringDarrinWardFormat().stream().inject { str, item -> str + item }
    }

    public static String readablePOIs(List<PointOfInterest> pois) {
        return readablePoints(pois.stream().map({ poi -> new Point(poi.point.x, poi.point.y) })
                .collect(Collectors.toList()))
    }
}
