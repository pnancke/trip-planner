package trip.planner

import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import groovyx.gpars.GParsPool
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.NominationApi
import trip.planner.osm.api.Point
import trip.planner.osm.model.PointCluster
import trip.planner.osm.model.RouteSegment
import trip.planner.osm.model.WaypointRoute
import trip.planner.util.ActiveTimer
import trip.planner.util.SortByClusterCenter

import static trip.planner.util.ClusterHelper.extractCoordinateClusterWithoutOutliers
import static trip.planner.util.ListUtils.mapToPoints

class HomeController {

    private static Log log = LogFactory.getLog(HomeController.class)
    private static final int THREAD_COUNT = 3

    def index() {}

    def getRoute(String start, String destination, int additionalTravelTime, String lang) {
        Point startPoint = extractCoordinates(start)
        Point destPoint = extractCoordinates(destination)
        Double searchArea = 0.06

        if (startPoint == null && destPoint == null) {
            render createErrorMessage("Error: Unable to find '$start' and '$destination'!")
        } else if (startPoint == null) {
            render createErrorMessage("Error: Unable to find '$start'!")
        } else if (destPoint == null) {
            render createErrorMessage("Error: Unable to find '$destination'!")
        } else {
            if (startPoint.equals(destPoint)) {
                render createErrorMessage("Error: Unable to generate route, given places are equal.")
                return
            }

            for (int i = 1; i < 4; i++) {
                try {
                    RouteSegment routeCoordinates = getRouteCoordinates(startPoint, destPoint)

                    List<PointCluster> poiCoordinates = getPOIsInRouteArea(routeCoordinates.route, searchArea)
                    def startCoords = [startPoint.lat, startPoint.lon]
                    WaypointRoute waypointRoute = generateWaypointRoute(poiCoordinates, startPoint)

                    def json = new JsonBuilder()
                    json {
                        success true
                        route waypointRoute.generateRoute()
                        pois poiCoordinates
                        startCoordinates startCoords
                    }
                    render json.toString()
                    return
                } catch (RestClientException e) {
                    log.error("RestClientException while calling routing api! Try:" + i, e)
                    i++
                }
            }
            render createErrorMessage("Error: Unable to generate route. Please try again later!")
        }
    }

    private static WaypointRoute generateWaypointRoute(List<PointCluster> poiCoordinates, Point start) {
        Collections.sort(poiCoordinates, new SortByClusterCenter(start));
        WaypointRoute waypointRoute = new WaypointRoute(poiCoordinates.clusterCenter)
        List<RouteSegment> routeSegments = waypointRoute.getRouteSegments()
        ActiveTimer timer = new ActiveTimer()
        GParsPool.withPool(THREAD_COUNT) {
            routeSegments.eachWithIndexParallel { RouteSegment routeSegment, int i ->
                def coordinates = getRouteCoordinates(routeSegment.start, routeSegment.destination)
                waypointRoute.setRouteSegment(coordinates, i)
            }
        }

        timer.stopAndLog(log, "Generating route with waypoints")
        waypointRoute
    }

    public String createErrorMessage(String message) {
        def json = new JsonBuilder()
        json {
            success false
            error message
        }
        json.toString()
    }

    private static RouteSegment getRouteCoordinates(Point start, Point destination) {
        ActiveTimer timer = new ActiveTimer()
        RestBuilder rest = new RestBuilder()
        def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$start.lat&flon=$start.lon" +
                "&tlat=$destination.lat&tlon=$destination.lon"
        def xml = new XmlSlurper().parseText(rest.get(url).text)
        Integer travelTime = xml.Document.traveltime.toInteger()
        log.info "found a route with a travel-time of: $travelTime"
        List<String> routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")

        List<List<String>> collect = routeCoordinates.collect { it.tokenize(",") }

        RouteSegment routeSegment = new RouteSegment(start, destination, mapToPoints(collect))
        timer.stopAndLog(log, "Route REST call")
        routeSegment
    }

    public static List<PointCluster> getPOIsInRouteArea(List<Point> route, Double area) {
        ActiveTimer timer = new ActiveTimer()
        List<PointOfInterest> pois = PointOfInterestService.poisFoundInRouteArea(route, area)
        timer.stopAndLog(log, "POI extracting")
        if (pois.isEmpty()) {
            return []
        }
        timer.reset()
        List<PointCluster> poiClusters = extractCoordinateClusterWithoutOutliers(pois)
        timer.stopAndLog(log, "Clustering")

        log.info "found ${pois.size()} nodes"
        log.info "after clustering, ${poiClusters.size()} cluster are given"

        poiClusters
    }

    private static Point extractCoordinates(String place) {
        def nominationApi = new NominationApi(place)
        nominationApi.doRequest()
        def point = nominationApi.point
        point
    }
}
