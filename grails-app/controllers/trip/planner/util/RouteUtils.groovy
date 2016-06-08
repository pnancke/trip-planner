package trip.planner.util

import grails.plugins.rest.client.RestBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import trip.planner.osm.api.Pair

class RouteUtils {

    private static final Log log = LogFactory.getLog(RouteUtils.class)
    public static final int SECONDS_IN_A_MINUTE = 60

    static Pair<Double, List<List<String>>> getRouteInfos(RestBuilder rest, GString url) {
        def xml = new XmlSlurper().parseText(rest.get(url).text)
        Double travelTime = xml.Document.traveltime.toInteger() / SECONDS_IN_A_MINUTE
        log.info "found a route with a travel-time of: $travelTime min"
        def routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")
        def list = routeCoordinates.collect { it.tokenize(",") }
        new Pair(travelTime, list)
    }
}
