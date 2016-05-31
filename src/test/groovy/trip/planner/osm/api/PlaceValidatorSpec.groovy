package trip.planner.osm.api

import spock.lang.Specification


class PlaceValidatorSpec extends Specification {

    public static final double START_LON = 49.3
    public static final double START_LAT = 59.5
    public static final double DESTINATION_LON = 100.4
    public static final double DESTINATION_LAT = 110.9
    public static final int TWO_THOUSAND_FIVE_HUNDRED = 2500

    def "split contains start and destination"() {
        when:
        POIApi api = new POIApi(START_LON, START_LAT, DESTINATION_LON, DESTINATION_LAT)
        List<Pair<Point, Point>> allPoints = PlaceValidator.split(api)
        Point startPoint = allPoints.get(0).getA()
        Point endPoint = allPoints.get(allPoints.size() - 1).getB()

        then:
        startPoint.lon == START_LON
        startPoint.lat == START_LAT
        endPoint.lon == DESTINATION_LON
        endPoint.lat == DESTINATION_LAT
    }

    def "split size check"() {
        when:
        POIApi api = new POIApi(50, 50, 100, 100)
        List<Pair<Point, Point>> allPoints = PlaceValidator.split(api)

        then:
        allPoints.size() == TWO_THOUSAND_FIVE_HUNDRED
    }

    def "split with a small but long place"() {
        when:
        POIApi api = new POIApi(50, 50, 52, 50.5)
        List<Pair<Point, Point>> allPoints = PlaceValidator.split(api)
        println allPoints

        then:
        allPoints.size() == 2
    }

    def "test berlin leipzig connection"() {
        when:
        POIApi api = new POIApi(12.373714, 51.327971, 12.569131, 52.515618)
        List<Pair<Point, Point>> allPoints = PlaceValidator.split(api)
        println allPoints

        then:
        allPoints.size() == 4
    }

    def "split with a tiny place"() {
        when:
        POIApi api = new POIApi(50, 50, 50.5, 50.5)
        List<Pair<Point, Point>> allPoints = PlaceValidator.split(api)

        then:
        allPoints.size() == 1
    }
}
