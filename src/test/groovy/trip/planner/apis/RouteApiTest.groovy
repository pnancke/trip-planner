package trip.planner.apis

import grails.test.mixin.TestFor
import spock.lang.Specification

import static trip.planner.apis.RouteApi.ROUTE_API_BASE_URI

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 16.04.16 - 15:33
 */
@TestFor(RouteApi)
class RouteApiTest extends Specification {

    private static final String END = "end-7654321";
    private static final String START = "start-1234567";

    void "test building route-api-URI"() throws Exception {
        RouteApi api = RouteApi.fromStartToEnd(START, END);
        URI expected = new URI(ROUTE_API_BASE_URI + "?" + "start=" + START + "&" + "end=" + END);
        assertEquals(expected, api.buildRequestURI());
    }
}
