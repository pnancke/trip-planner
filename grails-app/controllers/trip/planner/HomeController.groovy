package trip.planner

import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.DoubleVector
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.data.type.TypeUtil
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.relation.Relation
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.*
import trip.planner.osm.model.Node
import trip.planner.util.ActiveTimer
import trip.planner.util.RouteHelper

import static trip.planner.osm.api.ElkiWrapper.extractClusters
import static trip.planner.osm.api.ElkiWrapper.filterOutliers

class HomeController {

    private static int K_MEANS_CLUSTER_SIZE = 10
    private static final int MAX_K_MEANS_ITERATIONS = 50
    private static final BigDecimal MIN_MEAN_PERCENTAGE_CLUSTER_SIZE = 0.8
    private static Log log = LogFactory.getLog(HomeController.class)

    def index() {}

    def getRoute(String start, String destination, int additionalTravelTime) {
        Point startPoint = extractCoordinates(start)
        Point destPoint = extractCoordinates(destination)

        if (startPoint == null && destPoint == null) {
            render createErrorMessage("Error: Unable to find '$start' and '$destination'!")
        } else if (startPoint == null) {
            render createErrorMessage("Error: Unable to find '$start'!")
        } else if (destPoint == null) {
            render createErrorMessage("Error: Unable to find '$destination'!")
        } else {
            RestBuilder rest = new RestBuilder()
            def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$startPoint.lat&flon=$startPoint.lon" +
                    "&tlat=$destPoint.lat&tlon=$destPoint.lon"
            for (int i = 1; i < 4; i++) {
                try {
                    ActiveTimer timer = new ActiveTimer()
                    List<List<String>> routeCoordinates = getRouteCoordinates(rest, url)
                    List<BBox> bboxes = POIApi.calcResultingBBoxes(
                            RouteHelper.mapToPoints(routeCoordinates))
                    log.info "calculate ${bboxes.size()} poi-bboxes"
                    List<double[]> poiCoordinates = new ArrayList<>()
                    bboxes.each {
                        List<double[]> pois = getPOIs(it)
                        if (!pois.isEmpty()) {
                            poiCoordinates.addAll(pois)
                        }
                    }
                    timer.stopAndLog(log, "routing and clustering")
                    def startCoords = [startPoint.lat, startPoint.lon]
                    def json = new JsonBuilder()
                    json {
                        success true
                        route routeCoordinates
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

    private String createErrorMessage(String message) {
        def json = new JsonBuilder()
        json {
            success false
            error message
        }
        json.toString()
    }

    private static List<List<String>> getRouteCoordinates(RestBuilder rest, GString url) {
        def xml = new XmlSlurper().parseText(rest.get(url).text)
        Integer travelTime = xml.Document.traveltime.toInteger()
        log.info "found a route with a travel-time of: $travelTime"
        def routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")
        def list = routeCoordinates.collect { it.tokenize(",") }
        list
    }

    private static List<double[]> getPOIs(BBox bbox) {
        ActiveTimer timer = new ActiveTimer()
        List<Node> nodes = new POIParser().parse(new POIApi(bbox))
        timer.stopAndLog(log, "POI parsing.")
        if (nodes.isEmpty()) {
            log.info "no pois to cluster"
            return Collections.emptyList()
        }
        timer.reset()
        List<double[]> poiCoordinates = extractCoordinatesWithoutOutliers(nodes)
        timer.stopAndLog(log, "clustering.")

        log.info "found ${nodes.size()} nodes"
        log.info "after clustering, ${poiCoordinates.size()} nodes are given"
        poiCoordinates
    }

    private static Point extractCoordinates(String start) {
        def nominationApi = new NominationApi(start)
        nominationApi.doRequest()
        def latLon = nominationApi.point
        latLon
    }

    private static List<double[]> extractCoordinatesWithoutOutliers(List<Node> nodes) {
        Pair<Database, List<Cluster<KMeansModel>>> pair =
                extractClusters(nodes, K_MEANS_CLUSTER_SIZE, MAX_K_MEANS_ITERATIONS)
        Database db = pair.getA()
        List<Cluster> clusters = pair.getB()
        List<Cluster> filteredClusters = filterOutliers(clusters,
                K_MEANS_CLUSTER_SIZE,
                MIN_MEAN_PERCENTAGE_CLUSTER_SIZE)

        filteredClusters.each { it.getIDs() }
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        List<double[]> poiCoordinates = new ArrayList<>()
        filteredClusters.each { Cluster<KMeansModel> clu ->
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                DoubleVector v = rel.get(it) as DoubleVector
                poiCoordinates.add(v.getValues())
            }
        }
        poiCoordinates
    }
}
