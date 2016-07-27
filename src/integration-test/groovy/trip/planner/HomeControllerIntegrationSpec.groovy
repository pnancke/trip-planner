package trip.planner

import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.util.GrailsWebMockUtil
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(HomeController)
@Integration
@Rollback
class HomeControllerIntegrationSpec extends Specification {

    private static final String LEIPZIG = "Leipzig"
    private static final String TAUCHA = "Taucha"
    private static final String WURZEN = "Wurzen"
    private static final String BRANDIS = "Brandis"
    private static final String NOT_EXISTING_CITY_1 = "not-existing-15451245"
    private static final String NOT_EXISTING_CITY_2 = "not-existing-10092454"
    private static final String LANG_DE = "de"
    private static final String BERLIN = "Berlin"
    private static final String EILENBURG = "Eilenburg"
    private static final Double SEARCH_AREA = 16.0
    private static final int TOO_HIGH_SEARCH_AREA = 101
    private static final String NO_WAYPOINT = ""

    @Autowired
    WebApplicationContext ctx

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void "get route returns OK"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.status == HttpStatus.SC_OK
    }

    void "get route starts with start point"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().startsWith('{"success":true,"route":[{"lat":51.3')
        response.content.toString().contains('"lon":12.38')
    }

    void "get route starts with success true"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().startsWith('{"success":true,')
    }

    void "get route contains start coordinates"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('"startCoordinates":[51.3391827,12.3810549]')
    }

    @Ignore
    void "get route with larger route works"() {
        when:
        controller.getRoute(LEIPZIG, BERLIN, NO_WAYPOINT, LANG_DE, SEARCH_AREA)
        String content = controller.response.content.toString()

        then:
        content.startsWith('{"success":true,"route":[{"lat":51.3')
        content.contains('"lon":12.38')
        content.contains('"startCoordinates":[51.3391827,12.3810549]')
    }

    void "get route contains cluster range"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('{"clusterRange":')
    }

    void "get route with too high search area fails"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, TOO_HIGH_SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to generate route, given searchArea is too high."}')
    }

    void "get route with not existing start place"() {
        when:
        controller.getRoute(NOT_EXISTING_CITY_1, LEIPZIG, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\'!"}')
    }

    void "get route with not existing destination place"() {
        when:
        controller.getRoute(LEIPZIG, NOT_EXISTING_CITY_1, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\'!"}')
    }

    void "get route with not existing start and destination place"() {
        when:
        controller.getRoute(NOT_EXISTING_CITY_1, NOT_EXISTING_CITY_2, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to find \'' + NOT_EXISTING_CITY_1
                + '\' and \'' + NOT_EXISTING_CITY_2 + '\'!"}')
    }

    void "get route with equal start and destination place"() {
        when:
        controller.getRoute(LEIPZIG, LEIPZIG, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().equals('{"success":false,"error":"Error: Unable to generate route, given places are equal."}')
    }

    void "get route contains wiki link"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('wiki":"de:')
    }

    void "get route contains wiki link of small building"() {
        when:
        controller.getRoute(EILENBURG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains("de:Taucha#Bauwerke")
    }

    void "get route uses custom waypoints"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, WURZEN, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('{"lat":51.3709574,"wiki":null,"lon":12.7410574,"poiId":null,"name":null}')
    }

    void "get route uses custom waypoints even without POIs in custom location"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, BRANDIS, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('{"lat":51.3346604,"wiki":null,"lon":12.6089036,"poiId":null,"name":null}')
    }

    void "get route contains destination point"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, NO_WAYPOINT, LANG_DE, SEARCH_AREA)

        def response = controller.response

        then:
        response.content.toString().contains('{"lat":51.379958,"wiki":null,"lon":12.493655,"poiId":null,"name":null}')
    }
}
