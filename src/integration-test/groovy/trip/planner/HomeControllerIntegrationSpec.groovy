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

@TestFor(HomeController)
@Integration
@Rollback
class HomeControllerIntegrationSpec extends Specification {

    private static final String LEIPZIG = "Leipzig"
    private static final String TAUCHA = "Taucha"
    private static final String NOT_EXISTING_CITY_1 = "not-existing-15451245"
    private static final String NOT_EXISTING_CITY_2 = "not-existing-10092454"
    private static final String LANG_DE = "de"

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

    void "test get route starts with start point"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().startsWith('{"success":true,"route":[{"lat":51.34121,"lon":12.38284},')
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

    void "test get route contains cluster range"() {
        when:
        controller.getRoute(LEIPZIG, TAUCHA, 0, LANG_DE)

        def response = controller.response

        then:
        response.content.toString().contains('{"clusterRange":')
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
}
