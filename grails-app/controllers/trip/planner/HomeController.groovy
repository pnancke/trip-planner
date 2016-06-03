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

    def getRoute(double startLon, double startLat, double endLon, double endLat, int additionalTravelTime) {
        RestBuilder rest = new RestBuilder()
        def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$startLat&flon=$startLon&tlat=$endLat&tlon=$endLon"
        def json = new JsonBuilder()
        for (int i = 1; i < 4; i++) {
            try {
                def xml = new XmlSlurper().parseText(rest.get(url).text)
                Integer travelTime = xml.Document.traveltime.toInteger()
                log.info "found a route with a travel-time of: $travelTime"
                def routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")

                ActiveTimer timer = new ActiveTimer()
                ArrayList<Node> nodes = new POIParser().parse(new POIApi(startLon, startLat, endLon, endLat))
                timer.stopAndLog(log, "POI parsing.")
                timer.reset()
                ArrayList<double[]> poiCoordinates = extractCoordinatesWithoutOutliers(nodes)
                timer.stopAndLog(log, "clustering.")

                log.info "found ${nodes.size()} nodes"
                log.info "after clustering, ${poiCoordinates.size()} nodes are given"
                json {
                    success true
                    route routeCoordinates.collect { it.tokenize(",") }
                    pois poiCoordinates
                }
                render json.toString()
                return
            } catch (RestClientException e) {
                log.error("RestClientException while calling routing api! Try:" + i, e)
                i++
            }
        }
        json {
            success false
            error 'Error: Unable to generate route. Please try again later!'
        }
        render json.toString()
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
