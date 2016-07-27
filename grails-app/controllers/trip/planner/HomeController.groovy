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
    private static final Double TOO_HIGH_SEARCH_AREA = 100.0

    def index() {}

    def getRoute(String start, String destination, String waypoints, String lang, Double searchArea) {
        ActiveTimer timer = new ActiveTimer()
        Point startPoint = extractCoordinates(start)
        Point destPoint = extractCoordinates(destination)
        if (waypoints == null) {
            waypoints = ""
        }

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
            if (searchArea > TOO_HIGH_SEARCH_AREA) {
                render createErrorMessage("Error: Unable to generate route, given searchArea is too high.")
                return
            }

            Double area = (searchArea * 0.03) / 8.0

            for (int i = 1; i < 4; i++) {
                try {
                    List<PointCluster> routeWithoutPois = new ArrayList<PointCluster>()
                    routeWithoutPois.add(new PointCluster(startPoint))
                    List<String> waypointsSplitted = waypoints.tokenize(',')
                    waypointsSplitted.each { routeWithoutPois.add(new PointCluster(extractCoordinates(it))) }
                    routeWithoutPois.add(new PointCluster(destPoint))

                    WaypointRoute routeCoordinates = generateWaypointRoute(routeWithoutPois, startPoint, destPoint)
                    List<Point> routePoints = new ArrayList<>()
                    routeCoordinates.routeSegments.each { routePoints.addAll(it.route) }
                    List<PointCluster> poiCoordinates = getPOIsInRouteArea(routePoints, area)
                    waypointsSplitted.each { poiCoordinates.add(new PointCluster(extractCoordinates(it))) }
                    List<Point> completeRoute = generateRoute(poiCoordinates, startPoint, destPoint)
                    List<Double> startCoords = [startPoint.lat, startPoint.lon]

                    def json = new JsonBuilder()
                    json {
                        success true
                        route completeRoute
                        pois poiCoordinates
                        startCoordinates startCoords
                    }
                    timer.stopAndLog(log, "Complete Request")
                    render json.toString()
                    return
                } catch (RestClientException e) {
                    log.error("RestClientException while calling routing api! Try:" + i, e)
                    i++
                }
            }
            render createErrorMessage("Error: Unable to generate route. Please try again later!")
            timer.stopAndLog(log, "Complete Request")
        }
    }

    private static List<Point> generateRoute(List<PointCluster> poiCoordinates, Point startPoint, Point destPoint) {
        WaypointRoute waypointRoute = generateWaypointRoute(poiCoordinates, startPoint, destPoint)
        List<Point> route = waypointRoute.generateRoute()
        if (!route.isEmpty()) {
            RouteSegment coordinates = getRouteCoordinates(route.get(route.size() - 1)
                    , destPoint)
            route.addAll(coordinates.route)
        }
        route
    }

    private static WaypointRoute generateWaypointRoute(List<PointCluster> poiCoordinates, Point start, Point dest) {
        Collections.sort(poiCoordinates, new SortByClusterCenter(start))
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