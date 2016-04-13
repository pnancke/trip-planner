package trip.planner.apis;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

import static trip.planner.apis.PlaceApi.PLACE_API_BASE_URI;

/**
 * @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
 *         On 15.04.16 - 21:40
 */
public class PlaceApiTest {

    @Test
    public void testBuildRequestURI() throws Exception {
        PlaceApi api = PlaceApi.place("Leipzig");
        URI expected = new URI(PLACE_API_BASE_URI + "?" + "place=Leipzig");
        Assert.assertEquals(expected, api.buildRequestURI());
    }
}
