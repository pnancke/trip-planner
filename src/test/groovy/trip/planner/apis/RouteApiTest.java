package trip.planner.apis;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

import static trip.planner.apis.RouteApi.ROUTE_API_BASE_URI;

/**
 * @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
 *         On 15.04.16 - 22:05
 */
public class RouteApiTest {

    private static final String END = "end-7654321";
    private static final String START = "start-1234567";

    @Test
    public void testBuildRequestURI() throws Exception {
        RouteApi api = RouteApi.fromStartToEnd(START, END);
        URI expected = new URI(ROUTE_API_BASE_URI + "?" + "start=" + START + "&" + "end=" + END);
        Assert.assertEquals(expected, api.buildRequestURI());
    }
}
