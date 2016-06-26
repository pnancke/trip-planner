package trip.planner

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import trip.planner.osm.api.Circle
import trip.planner.osm.api.Pair
import trip.planner.osm.api.Point
import trip.planner.osm.api.Polygon

import java.util.stream.Collectors

class PointOfInterestService {

    private static final Log log = LogFactory.getLog(PointOfInterestService.class)

    public static List<PointOfInterest> poisFoundInRouteArea(List<Point> route, Double area) {
        if (route.isEmpty()) {
            return Collections.emptyList()
        }

        Pair<String, String> queryWithParam = generatePolygonQueryPair(simplify(route), area)
        containsPolygon(queryWithParam.a, queryWithParam.b)
    }

    public static List<Point> simplify(List<Point> route) {
        if (route.isEmpty()) {
            Collections.emptyList()
        }
        List<Point> simplified = new ArrayList<>()
        route.eachWithIndex { Point entry, int i ->
            if (i % 3 == 0 && i != route.size() - 1) {
                simplified.add(entry)
            }
        }

        simplified.add(route.last())
        if (simplified.size() <= 2) {
            return route
        }
        simplified
    }

    public static Pair<String, String> generatePolygonQueryPair(List<Point> route, Double area) {
        Double circleArea = area
        if (route.isEmpty()) {
            return new Pair<>("", [:])
        }
        Circle beginFilter = new Circle(route.first(), circleArea)
        Circle endFilter = new Circle(route.last(), circleArea)
        List<Point> under = shifting(route, -area, endFilter, beginFilter)
        List<Point> upper = shifting(route, area, endFilter, beginFilter)
        List<Point> polygonShape = new ArrayList<>()
        if (!under.isEmpty() && !upper.isEmpty()) {
            log.info "generate route area with circles"
            Collections.reverse(upper)

            polygonShape.addAll(under)
            polygonShape.addAll(Circle.generateOpenCircle(under.last(), upper.first(), route.last(), circleArea))
            polygonShape.addAll(upper)
            polygonShape.addAll(Circle.generateOpenCircle(under.first(), upper.last(), route.first(), circleArea))
        } else {
            log.info "generate route area without circles"
            List<Point> underNoCircle = shifting(route, -area, null, null)
            List<Point> upperNoCircle = shifting(route, area, null, null)
            Collections.reverse(upperNoCircle)

            polygonShape.addAll(underNoCircle)
            polygonShape.add(new Point(underNoCircle.last().lat, upperNoCircle.first().lon))
            polygonShape.addAll(upperNoCircle)
            polygonShape.add(new Point(upperNoCircle.last().lat, underNoCircle.first().lon))
        }
        polygonShape.add(polygonShape.get(0))

        new Pair<>("FROM PointOfInterest WHERE ST_CONTAINS(GeomFromText(?), point) = TRUE",
                new Polygon(polygonShape).toString())
    }

    private static List<Point> shifting(List<Point> route, double area, Circle endFilter, Circle beginFilter) {
        if (Objects.nonNull(endFilter) && Objects.nonNull(beginFilter)) {
            return area < 0 ?
                    route.stream().filter { !endFilter.contains(it) }
                            .map { new Point(it.lat + area, it.lon + area) }
                            .filter { !beginFilter.contains(it) }.filter { !endFilter.contains(it) }
                            .collect(Collectors.toList())
                    : route.stream().filter { !beginFilter.contains(it) }
                    .map { new Point(it.lat + area, it.lon + area) }
                    .filter { !beginFilter.contains(it) }.filter { !endFilter.contains(it) }
                    .collect(Collectors.toList())
        } else {
            return route.stream().map { new Point(it.lat + area, it.lon + area) }.collect(Collectors.toList())
        }
    }

    public static List<PointOfInterest> containsPolygon(String query, String polygon) {
        PointOfInterest.executeQuery(query, [polygon])
    }

    public static List<PointOfInterest> execute(String query) {
        PointOfInterest.executeQuery(query)
    }
}
