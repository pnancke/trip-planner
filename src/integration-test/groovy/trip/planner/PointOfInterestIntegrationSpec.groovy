package trip.planner

import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.util.GrailsWebMockUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification
import trip.planner.osm.api.Point
import trip.planner.osm.api.Polygon

import static trip.planner.FileReader.LE_MUNICH_ROUTE
import static trip.planner.FileReader.extractRoute
import static trip.planner.PointOfInterestService.*

@TestFor(HomeController)
@Integration
@Rollback
class PointOfInterestIntegrationSpec extends Specification {

    private static final int SIZE_OF_GERMANY = 16919
    private static final int SIZE_OF_POIS_TO_MUNICH = 404
    private static final Point LEIPZIG = new Point(51.3, 12.38)
    private static final Point MARKKLEEBERG = new Point(51.276857, 12.372894)
    private static final Point TAUCHA = new Point(51.381478, 12.482528)
    private static final int SIZE_OF_POIS_IN_LE_MBERG_TAUCHA_ROUTE = 51
    private static final int SIZE_OF_POIS_IN_LE_TAUCHA = 44
    private static final Double SEARCH_AREA = 0.06

    @Autowired
    WebApplicationContext ctx

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void "get pois with route area works"() {
        when:
        List<PointOfInterest> pois = containsPolygon(CONTAINS_STATEMENT,
                Polygon.coords([0, 0, 0, 100, 100, 100, 100, 0, 0, 0]))

        then:
        !pois.isEmpty()
        pois.size() == SIZE_OF_GERMANY
    }

    void "found pois from route works"() {
        when:
        List<PointOfInterest> pois = poisFoundInRouteArea(extractRoute(LE_MUNICH_ROUTE), SEARCH_AREA)

        then:
        !pois.isEmpty()
        pois.size() == SIZE_OF_POIS_TO_MUNICH
    }

    void "found pois from simple route works with circles"() {
        when:
        List<PointOfInterest> pois = poisFoundInRouteArea([LEIPZIG, MARKKLEEBERG, TAUCHA], SEARCH_AREA)

        then:
        !pois.isEmpty()
        pois.size() == SIZE_OF_POIS_IN_LE_MBERG_TAUCHA_ROUTE
    }

    void "found pois from simple route works without circles"() {
        when:
        List<PointOfInterest> pois = poisFoundInRouteArea([LEIPZIG, TAUCHA], SEARCH_AREA)

        then:
        !pois.isEmpty()
        pois.size() == 44
    }
}
