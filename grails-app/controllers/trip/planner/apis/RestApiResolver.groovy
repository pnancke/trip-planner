package trip.planner.apis

/**
 @author <a href="mailto:mattthias.zober@outlook.de">Matthias Zober</a>
  *         On 13.04.16 - 23:55
 */
class RestApiResolver {

    static def getResponse(BaseApi api) {
        api.buildRequestURI()
        //TODO: make a useful request to the given api
        //TODO: parse the response and return an object (representation of the place or the route)
    }
}
