package trip.planner

import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import grails.util.GrailsWebMockUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.hibernate5.HibernateQueryException
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification
import trip.planner.osm.api.Point
import trip.planner.osm.api.Polygon

import static trip.planner.FileReader.*
import static trip.planner.PointOfInterestService.*

@TestFor(HomeController)
@Integration
@Rollback
class PointOfInterestIntegrationSpec extends Specification {

    private static final int SIZE_OF_GERMANY = 16919
    private static final String CONTAINS_POLYGON_STATEMENT = "from PointOfInterest where " +
            "st_contains(GeomFromText(?), point) = TRUE"
    private static final int SIZE_OF_POIS_TO_MUNICH = 236
    private static final Point LEIPZIG = new Point(51.3, 12.38)
    private static final Point MARKKLEEBERG = new Point(51.276857, 12.372894)
    private static final Point TAUCHA = new Point(51.381478, 12.482528)
    private static final int SIZE_OF_POIS_IN_LE_MBERG_TAUCHA_ROUTE = 5
    private static final int SIZE_OF_POIS_IN_LE_TAUCHA = 4
    private static final Double SEARCH_AREA = 0.06

    @Autowired
    WebApplicationContext ctx

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest(ctx)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void "force index request fails on unexpected token"() {
        when:
        String query = "from trip.planner.PointOfInterest force index (point)" +
                " where st_contains(GeomFromText('Polygon((0 0, 0 100, 100 100,100 0,0 0))'), point) = TRUE"
        execute(query)

        then:
        def ex = thrown(HibernateQueryException)
        ex.message == "unexpected token: index near line 1, column 41 [" + query + "];" +
                " nested exception is org.hibernate.hql.internal.ast.QuerySyntaxException: " +
                "unexpected token: index near line 1, column 41 [" + query + "]"
    }

    void "get pois with route area works with where"() {
        when:
        List<PointOfInterest> pois = containsPolygon(CONTAINS_POLYGON_STATEMENT,
                Polygon.coords([0, 0, 0, 100, 100, 100, 100, 0, 0, 0]))

        then:
        !pois.isEmpty()
        pois.size() == SIZE_OF_GERMANY
    }

    void "complex poi request works with where"() {
        when:
        List<PointOfInterest> pois = containsPolygon(CONTAINS_POLYGON_STATEMENT,
                new Polygon(extractRoute(MUNICH_POLYGON)).toString())

        then:
        !pois.isEmpty()
        pois.size() == SIZE_OF_POIS_TO_MUNICH
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
        pois.size() == SIZE_OF_POIS_IN_LE_TAUCHA
    }
}
