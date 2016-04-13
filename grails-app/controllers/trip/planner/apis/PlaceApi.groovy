package trip.planner.apis

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 13.04.16 - 23:55
 */
class PlaceApi extends BaseApi {

    public static final String PLACE_API_BASE_URI = "place-uri-1243123"
    //TODO: replace with real place-api-base-uri

    PlaceApi(URI baseUri) {
        super(baseUri)
    }

    static PlaceApi place(String place) {
        PlaceApi api = new PlaceApi(URI.create(PLACE_API_BASE_URI))
        api.addParams("place", place)
        api
    }
}
