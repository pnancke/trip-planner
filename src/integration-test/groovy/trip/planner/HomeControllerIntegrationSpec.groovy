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

    private static final BigDecimal START_LAT = 51.3827
    private static final BigDecimal START_LON = 12.378
    private static final BigDecimal DEST_LAT = 51.3828
    private static final BigDecimal DEST_LON = 12.379

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
        controller.getRoute(START_LON, START_LAT, DEST_LON, DEST_LAT, 0)
        def response = controller.response

        then:
        response.status == HttpStatus.SC_OK
    }

    void "test get route returns route and poi"() {
        when:
        controller.getRoute(START_LON, START_LAT, DEST_LON, DEST_LAT, 0)

        def response = controller.response

        then:
        response.content.toString() == '{"success":true,"route":[["12.377746","51.383011"]' +
                ',["12.378281","51.383181"],["12.378987","51.383395"],["12.379046","51.383322"]' +
                ',["12.379167","51.383172"],["12.379229","51.383101"],["12.379285","51.383029"]' +
                ',["12.379375","51.382919"]],"pois":[[51.3827171,12.3780315]]}'
    }
}
