package trip.planner.osm.api

import spock.lang.Specification

class POIApiSpec extends Specification {


    public static final String VALIDATION_ERROR = "The start-position is equals to the destination-position."
    public static final Pair<Point, Point> LOWER_THAN_PAIR = new Pair<Point, Point>(new Point(50, 50), new Point(0, 0))
    public static final Pair<Point, Point> HIGHER_THAN_PAIR = new Pair<Point, Point>(new Point(0, 0), new Point(50, 50))
    public static final Pair<Point, Point> MIXED_PAIR = new Pair<Point, Point>(new Point(50, 50), new Point(0, 100))

    def "lowerThan bbox changes to higherThan"() {
        when:
        POIApi api = new POIApi(LOWER_THAN_PAIR);

        then:
        api.getBBox().equals(HIGHER_THAN_PAIR)
    }

    def "higherThan is still higherThan"() {
        when:
        POIApi api = new POIApi(HIGHER_THAN_PAIR)

        then:
        api.getBBox().equals(HIGHER_THAN_PAIR)
    }

    def "fails on equals validation"() {
        when:
        new POIApi(50, 0, 50, 0)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == VALIDATION_ERROR
    }

    def "mixed bbox is still mixed"() {
        when:
        POIApi api = new POIApi(MIXED_PAIR);

        then:
        api.getBBox().equals(MIXED_PAIR)
    }
}
