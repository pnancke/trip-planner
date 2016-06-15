package trip.planner

import com.google.common.base.Joiner
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.NominationApi
import trip.planner.osm.api.Point
import trip.planner.osm.model.PointCluster
import trip.planner.util.ActiveTimer
import trip.planner.util.ListUtils
import trip.planner.util.SortPoints

import java.util.stream.Collectors

import static java.util.Collections.emptyList
import static trip.planner.util.ClusterHelper.extractCoordinateClusterWithoutOutliers

class HomeController {

    private static Log log = LogFactory.getLog(HomeController.class)

    def index() {}

    def getPoi() {
        List<PointOfInterest> pois = PointOfInterest.getPOIsInBBox(new Point(1, 1), new Point(100, 100))
        def each = pois.each { it.toString() }
        render "POIs: " + each
    }

    def getRoute(String start, String destination, int additionalTravelTime, String lang) {
        Point startPoint = extractCoordinates(start)
        Point destPoint = extractCoordinates(destination)
        Double searchArea = 0.02

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

            RestBuilder rest = new RestBuilder()
            for (int i = 1; i < 4; i++) {
                try {
                    List<List<String>> routeCoordinates = getRouteCoordinates(rest, startPoint, destPoint
                            , emptyList(), lang)
                    List<PointCluster> poiCoordinates = getPOIsInRouteArea(ListUtils.mapToPoints(routeCoordinates)
                            , searchArea)
                    def startCoords = [startPoint.lat, startPoint.lon]
                    List<List<String>> routingCoordinatesWithWaypoints = getRouteCoordinates(rest, startPoint, destPoint
                            , poiCoordinates.clusterCenter, lang)

                    def json = new JsonBuilder()
                    json {
                        success true
                        route routingCoordinatesWithWaypoints
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

    public String createErrorMessage(String message) {
        def json = new JsonBuilder()
        json {
            success false
            error message
        }
        json.toString()
    }

    private
    static List<List<String>> getRouteCoordinates(RestBuilder rest, Point start, Point destination, List<Point> via
                                                  , String lang) {
        ActiveTimer timer = new ActiveTimer()
        Collections.sort(via, new SortPoints(start));
        Joiner joiner = Joiner.on(' ').skipNulls()
        String viaString = joiner.join(via.stream().map({ it -> it.lon + "," + it.lat }).collect(Collectors.toList()))

        String url = "http://openls.geog.uni-heidelberg.de/route?start=$start.lon,$start.lat&end=$destination.lon," +
                "$destination.lat&via=$viaString&lang=$lang&distunit=KM&routepref=Car&weighting=Recommended&useTMC=false" +
                "&noMotorways=false&noTollways=false&noUnpavedroads=false&noSteps=false&noFerries=false&instructions=false"
        log.info("Request to routing URL: $url")

        def get = rest.get(url)
        def xml = new XmlSlurper().parseText(get.responseEntity.body.toString())
        def travelTime = xml.'Response'.'DetermineRouteResponse'.'RouteSummary'.'TotalTime'
        log.info "found a route with a travel-time of: $travelTime"
        LinkedList routeCoordinates = xml.'Response'.'DetermineRouteResponse'.'RouteGeometry'.'LineString'.'pos'.list()

        timer.stopAndLog(log, "creating route with waypoints")
        routeCoordinates.collect { it.toString().tokenize(" ") }
    }

    public static List<PointCluster> getPOIsInRouteArea(List<Point> route, Double area) {
        ActiveTimer timer = new ActiveTimer()
        List<PointOfInterest> pois = PointOfInterest.getPOIsInRouteArea(route, area)
        timer.stopAndLog(log, "POI extracting.")
        timer.reset()
        List<PointCluster> poiClusters = extractCoordinateClusterWithoutOutliers(pois)
        timer.stopAndLog(log, "clustering.")

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
