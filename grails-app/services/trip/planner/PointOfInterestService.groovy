package trip.planner

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LineString
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.hibernate.SQLQuery
import trip.planner.osm.api.Pair
import trip.planner.osm.api.Point
import trip.planner.osm.api.Polygon

import java.util.stream.Collectors

class PointOfInterestService {

    public static final String CONTAINS_STATEMENT = "SELECT * FROM point_of_interest FORCE INDEX (point)" +
            " WHERE ST_CONTAINS(GeomFromText(:polygon), point) = TRUE"
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
        if (route.isEmpty()) {
            return new Pair<>("", "")
        }
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate> coordList = route.stream().map { p -> new Coordinate(p.lat, p.lon) }.collect(Collectors.toList())

        Coordinate[] coordinates = coordList.toArray()
        LineString lineString = gf.createLineString(coordinates);
        Geometry buffer = lineString.buffer(area)

        List<Point> bufferShape = buffer.getCoordinates().toList().stream().map { c -> new Point(c.x, c.y) }
                .collect(Collectors.toList())
        bufferShape.add(bufferShape.last())
        new Pair<>(CONTAINS_STATEMENT, new Polygon(bufferShape).toString())
    }

    public static List<PointOfInterest> containsPolygon(String query, String polygon) {
        List<PointOfInterest> result = new ArrayList<>()
        PointOfInterest.withSession {
            SQLQuery sqlQuery = it.createSQLQuery(query)
            result = sqlQuery.addEntity(PointOfInterest).setString('polygon', polygon).list()
        }
        result
    }
}
