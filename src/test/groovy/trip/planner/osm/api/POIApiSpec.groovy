package trip.planner.osm.api

import spock.lang.Specification

class POIApiSpec extends Specification {

    private static final String VALIDATION_ERROR = "The start-position is equals to the destination-position."
    private static final BBox LOWER_THAN_BBOX = new BBox(new Point(50, 50), new Point(0, 0))
    private static final BBox HIGHER_THAN_BBOX = new BBox(new Point(0, 0), new Point(50, 50))
    private static final BBox MIXED_BBOX = new BBox(new Point(50, 50), new Point(0, 100))

    def "lowerThan bbox changes to higherThan"() {
        when:
        POIApi api = new POIApi(LOWER_THAN_BBOX);

        then:
        api.getBBox().equals(HIGHER_THAN_BBOX)
    }

    def "higherThan is still higherThan"() {
        when:
        POIApi api = new POIApi(HIGHER_THAN_BBOX)

        then:
        api.getBBox().equals(HIGHER_THAN_BBOX)
    }

    def "fails on equals validation"() {
        when:
        new POIApi(new BBox(50, 0, 50, 0))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == VALIDATION_ERROR
    }

    def "mixed bbox changes"() {
        when:
        POIApi api = new POIApi(MIXED_BBOX);

        then:
        api.getBBox().getStart() == new Point(0.0, 100.0)
        api.getBBox().getEnd() == new Point(50.0, 50.0)
    }
}
