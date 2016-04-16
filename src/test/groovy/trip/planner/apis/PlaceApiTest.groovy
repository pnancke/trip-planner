package trip.planner.apis

import grails.test.mixin.TestFor
import spock.lang.Specification

import static trip.planner.apis.PlaceApi.PLACE_API_BASE_URI
import static trip.planner.apis.PlaceApi.place

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 16.04.16 - 15:25
 */
@TestFor(PlaceApi)
class PlaceApiTest extends Specification {

    void "test building the place-api-URI"() {
        PlaceApi api = place("Leipzig");
        URI expected = new URI(PLACE_API_BASE_URI + "?" + "place=Leipzig");
        assertEquals(expected, api.buildRequestURI());
    }
}

