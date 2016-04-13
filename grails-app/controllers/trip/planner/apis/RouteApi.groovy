package trip.planner.apis

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 13.04.16 - 23:56
 */
class RouteApi extends BaseApi {

    public static final String ROUTE_API_BASE_URI = "http://openls.georg.uni-heidelberg.de/route"

    private RouteApi(URI baseUri) {
        super(baseUri)
    }

    static RouteApi fromStartToEnd(String start, String end) {
        RouteApi api = new RouteApi(URI.create(ROUTE_API_BASE_URI));
        api.addParams("start", start)
        api.addParams("end", end)
        api
    }
}
