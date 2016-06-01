package trip.planner.osm.api

import spock.lang.Specification

class POIApiSpec extends Specification {


    public static final String VALIDATION_ERROR = "The start-position is higher or equals to the destination-position."

    def "fails on higherThan validation"() {
        when:
        new POIApi(50, 50, 0, 0)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == VALIDATION_ERROR
    }

    def "fails on equals validation"() {
        when:
        new POIApi(50, 0, 50, 0)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == VALIDATION_ERROR
    }

    def "fails on mixed validation"() {
        when:
        new POIApi(50, 50, 0, 100)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == VALIDATION_ERROR
    }

    def "do request with time logging"() {
        when:
        POIApi api = new POIApi(50, 50, 56, 54)
        then:
        api.doRequest()
    }
}
