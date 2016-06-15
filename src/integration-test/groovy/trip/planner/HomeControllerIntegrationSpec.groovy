package trip.planner

import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.util.GrailsWebMockUtil
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification
import trip.planner.osm.api.Point
import trip.planner.util.ListUtils

@TestFor(HomeController)
@Integration
@Rollback
class HomeControllerIntegrationSpec extends Specification {

    private static final String LEIPZIG = "Leipzig"
    private static final String TAUCHA = "Taucha"
    private static final String NOT_EXISTING_CITY_1 = "not-existing-15451245"
    private static final String NOT_EXISTING_CITY_2 = "not-existing-10092454"
    private static final String LANG_DE = "de"
    private static final Point TAUCHA_POI_COORDS = new Point(12.4356056, 51.3692482)

    @Autowired
    WebApplicationContext ctx

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void "test get route returns OK"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        def response = controller.response

        then:
        response.status == HttpStatus.SC_OK
    }

    void "test get route contains route"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        String content = controller.response.content.toString()

        then:
        content.contains('"route":[[')
        content.contains('"],["')
        content.contains('"]]')
    }

    void "test get route starts with success true"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().startsWith('{"success":true,')
    }

    void "test get route contains start coordinates"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().contains('"startCoordinates":[51.3391827,12.3810549]')
    }

    void "test get route with not existing start place"() {
        when:
        controller.getRoute(NOT_EXISTING_CITY_1, LEIPZIG, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\'!"}')
    }

    void "test get route with not existing destination place"() {
        when:
        controller.getRoute(LEIPZIG, NOT_EXISTING_CITY_1, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\'!"}')
    }

    void "test get route with not existing start and destination place"() {
        when:
        controller.getRoute(NOT_EXISTING_CITY_1, NOT_EXISTING_CITY_2, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\' and \'' + NOT_EXISTING_CITY_2 + '\'!"}')
    }

    void "test get route with equal start and destination place"() {
        when:
        controller.getRoute(LEIPZIG, LEIPZIG, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to generate route, given places are equal."}')
    }

    void "get POIs works"() {
        when:
        List<PointOfInterest> pois = PointOfInterest.getPOIsInRouteArea([TAUCHA_POI_COORDS], 0.02)
        println ListUtils.reducePOIToReadableFormat(pois)

        then:
        PointOfInterest poi = pois.find()
        poi.lat == TAUCHA_POI_COORDS.lat
        poi.lon == TAUCHA_POI_COORDS.lon
    }
}
