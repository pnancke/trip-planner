package trip.planner.apis;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static trip.planner.apis.RouteApi.ROUTE_API_BASE_URI;

public class RouteApiTest {

    private static final String END = "end-7654321";
    private static final String START = "start-1234567";

    @Test
    public void testBuildApiUri() throws URISyntaxException {
        RouteApi api = RouteApi.fromStartToEnd(START, END);
        URI expected = new URI(ROUTE_API_BASE_URI + "?" + "start=" + START + "&" + "end=" + END);
        assertEquals(expected, api.buildRequestURI());
    }
}
