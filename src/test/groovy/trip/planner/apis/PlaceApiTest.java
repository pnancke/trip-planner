package trip.planner.apis;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static trip.planner.apis.PlaceApi.PLACE_API_BASE_URI;
import static trip.planner.apis.PlaceApi.place;

public class PlaceApiTest {

    @Test
    public void testBuildRequestUri() throws URISyntaxException {
        PlaceApi api = place("Leipzig");
        URI expected = new URI(PLACE_API_BASE_URI + "?" + "place=Leipzig");
        assertEquals(expected, api.buildRequestURI());
    }
}
