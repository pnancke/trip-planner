package trip.planner

import com.google.common.base.Joiner
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.BBox
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Point
import trip.planner.osm.model.Node
import trip.planner.osm.model.PointCluster
import trip.planner.util.ActiveTimer
import trip.planner.util.ListUtils
import trip.planner.util.NominationUtils
import trip.planner.util.POIUtils

import static java.util.Collections.emptyList
import static trip.planner.util.ClusterHelper.extractCoordinateClusterWithoutOutliers

class HomeController {

    private static Log log = LogFactory.getLog(HomeController.class)

    def index() {}

    def getRoute(String start, String destination, int additionalTravelTime, String lang) {
        Point startPoint = NominationUtils.extractCoordinates(start)
        Point destPoint = NominationUtils.extractCoordinates(destination)

        if (startPoint == null && destPoint == null) {
            render createErrorMessage("Error: Unable to find '$start' and '$destination'!")
        } else if (startPoint == null) {
            render createErrorMessage("Error: Unable to find '$start'!")
        } else if (destPoint == null) {
            render createErrorMessage("Error: Unable to find '$destination'!")
        } else {
            renderRouteRequest(startPoint, destPoint, additionalTravelTime, lang)
        }
    }

    private void renderRouteRequest(Point startPoint, Point destPoint, int additionalTravelTime, String lang) {
        if (startPoint.equals(destPoint)) {
            render createErrorMessage("Error: Unable to generate route, given places are equal.")
            return
        }
        RestBuilder rest = new RestBuilder()
        for (int i = 1; i < 4; i++) {
            try {
                ActiveTimer timer = new ActiveTimer()
                List<List<String>> routeCoordinates = getRouteCoordinates(rest, startPoint, destPoint, emptyList(), lang)
                //TODO replace 100 with actual travel time
                List<BBox> bboxes = POIUtils.calcResultingBBox(ListUtils.mapToPoints(routeCoordinates), 100)

                log.info "calculate ${bboxes.size()} poi-bboxes"
                List<PointCluster> poiCoordinates = new ArrayList<>()
                bboxes.each {
                    List<PointCluster> pois = getPOIs(startPoint, destPoint)
                    if (!pois.isEmpty()) {
                        poiCoordinates.addAll(pois)
                    }
                }
                timer.stopAndLog(log, "routing and clustering")

                List<List<String>> routingCoordinatesWithWaypoints = getRouteCoordinates(rest, startPoint, destPoint, poiCoordinates.clusterCenter, lang)
                def startCoords = [startPoint.lat, startPoint.lon]
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

    public String createErrorMessage(String message) {
        def json = new JsonBuilder()
        json {
            success false
            error message
        }
        json.toString()
    }

    private static List<List<String>> getRouteCoordinates(RestBuilder rest, Point start,
                                                          Point destination, List<Point> via, String lang) {
        ActiveTimer timer = new ActiveTimer()
        List<String> visStrings = new ArrayList<>()
        via.each {
            visStrings.add(it.lon + "," + it.lat)
        }
        Joiner joiner = Joiner.on(' ').skipNulls()
        String viaStrings = joiner.join(visStrings)

        String url = "http://openls.geog.uni-heidelberg.de/route?start=$start.lon,$start.lat&end=$destination.lon," +
                "$destination.lat&via=$viaStrings&lang=$lang&distunit=KM&routepref=Car&weighting=Recommended&useTMC=false" +
                "&noMotorways=false&noTollways=false&noUnpavedroads=false&noSteps=false&noFerries=false&instructions=false"
        log.info("Routing URL with waypoints: $url")

        def get = rest.get(url)
        def xml = new XmlSlurper().parseText(get.responseEntity.body.toString())
        def travelTime = xml.'Response'.'DetermineRouteResponse'.'RouteSummary'.'TotalTime'
        log.info "found a route with a travel-time of: $travelTime"
        LinkedList routeCoordinates = xml.'Response'.'DetermineRouteResponse'.'RouteGeometry'.'LineString'.'pos'.list()

        timer.stopAndLog(log, "creating route with waypoints")
        routeCoordinates.collect { it.toString().tokenize(" ") }
    }

    private static List<PointCluster> getPOIs(Point startLatLon, Point destLatLon) {
        ActiveTimer timer = new ActiveTimer()
        List<Node> nodes = new POIParser().parse(new POIApi(new BBox(startLatLon.lon, startLatLon.lat,
                destLatLon.lon, destLatLon.lat)))
        timer.stopAndLog(log, "POI parsing.")
        timer.reset()
        List<PointCluster> poiClusters = extractCoordinateClusterWithoutOutliers(nodes)
        timer.stopAndLog(log, "clustering.")

        log.info "found ${nodes.size()} nodes"
        log.info "after clustering, ${poiClusters.size()} cluster are given"
        poiClusters
    }
}
