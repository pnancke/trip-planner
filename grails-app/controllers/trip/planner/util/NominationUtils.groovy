package trip.planner.util

import trip.planner.osm.api.NominationApi
import trip.planner.osm.api.Point


class NominationUtils {

    public static Point extractCoordinates(String start) {
        def nominationApi = new NominationApi(start)
        nominationApi.doRequest()
        def latLon = nominationApi.point
        latLon
    }
}
