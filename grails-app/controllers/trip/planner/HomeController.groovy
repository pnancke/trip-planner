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
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.NominationApi
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Pair
import trip.planner.osm.model.Node
import trip.planner.util.ActiveTimer

import static trip.planner.osm.api.ElkiWrapper.extractClusters
import static trip.planner.osm.api.ElkiWrapper.filterOutliers

class HomeController {

    private static int K_MEANS_CLUSTER_SIZE = 10
    private static final int MAX_K_MEANS_ITERATIONS = 50
    private static final BigDecimal MIN_MEAN_PERCENTAGE_CLUSTER_SIZE = 0.8

    def index() {}

    def getRoute(String start, String destination, int additionalTravelTime) {
        Pair<Double, Double> startLatLon = extractCoordinates(start)
        Pair<Double, Double> destLatLon = extractCoordinates(destination)

        if (startLatLon == null && destLatLon == null) {
            render createErrorMessage("Error: Unable to find '$start' and '$destination'!")
        } else if (startLatLon == null) {
            render createErrorMessage("Error: Unable to find '$start'!")
        } else if (destLatLon == null) {
            render createErrorMessage("Error: Unable to find '$destination'!")
        } else {
            RestBuilder rest = new RestBuilder()
            def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$startLatLon.a&flon=$startLatLon.b" +
                    "&tlat=$destLatLon.a&tlon=$destLatLon.b"
            for (int i = 1; i < 4; i++) {
                try {
                    ArrayList<String> routeCoordinates = getRouteCoordinates(rest, url)
                    ArrayList<double[]> poiCoordinates = getPOIs(startLatLon, destLatLon)
                    def startCoords = [startLatLon.a, startLatLon.b]
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

    private static ArrayList<String> getRouteCoordinates(RestBuilder rest, GString url) {
        def xml = new XmlSlurper().parseText(rest.get(url).text)
        Integer travelTime = xml.Document.traveltime.toInteger()
        log.info "found a route with a travel-time of: $travelTime"
        def routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")
        def list = routeCoordinates.collect { it.tokenize(",") }
        list
    }

    private static ArrayList<double[]> getPOIs(Pair<Double, Double> startLatLon, Pair<Double, Double> destLatLon) {
        ActiveTimer timer = new ActiveTimer()
        ArrayList<Node> nodes = new POIParser().parse(new POIApi(startLatLon.b, startLatLon.a, destLatLon.b, destLatLon.a))
        timer.stopAndLog(log, "POI parsing.")
        timer.reset()
        ArrayList<double[]> poiCoordinates = extractCoordinatesWithoutOutliers(nodes)
        timer.stopAndLog(log, "clustering.")

        log.info "found ${nodes.size()} nodes"
        log.info "after clustering, ${poiCoordinates.size()} nodes are given"
        poiCoordinates
    }

    private static Pair<Double, Double> extractCoordinates(String start) {
        def nominationApi = new NominationApi(start)
        nominationApi.doRequest()
        def latLon = nominationApi.latLon
        latLon
    }

    private static ArrayList<double[]> extractCoordinatesWithoutOutliers(ArrayList<Node> nodes) {
        Pair<Database, ArrayList<Cluster<KMeansModel>>> pair =
                extractClusters(nodes, K_MEANS_CLUSTER_SIZE, MAX_K_MEANS_ITERATIONS)
        Database db = pair.getA()
        ArrayList<Cluster> clusters = pair.getB()
        ArrayList<Cluster> filteredClusters = filterOutliers(clusters,
                K_MEANS_CLUSTER_SIZE,
                MIN_MEAN_PERCENTAGE_CLUSTER_SIZE)

        filteredClusters.each { it.getIDs() }
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        ArrayList<double[]> poiCoordinates = new ArrayList<>()
        filteredClusters.each { Cluster<KMeansModel> clu ->
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                DoubleVector v = rel.get(it) as DoubleVector
                poiCoordinates.add(v.getValues())
            }
        }
        poiCoordinates
    }
}
