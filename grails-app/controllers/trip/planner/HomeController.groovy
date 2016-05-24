package trip.planner

import de.lmu.ifi.dbs.elki.data.Cluster
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import org.springframework.web.client.RestClientException
import trip.planner.osm.api.POIApi
import trip.planner.osm.model.Node

import static trip.planner.osm.api.ElkiWrapper.filterOutliers
import static trip.planner.osm.api.ElkiWrapper.getClusters

class HomeController {

    private static int K_MEANS_CLUSTER_SIZE = 10
    private static final int K_MEANS_ITERATIONS = 5
    private static final BigDecimal MIN_MEAN_PERCENTAGE_CLUSTER_SIZE = 0.8

    def index() {}

    def getRoute(double startLon, double startLat, double endLon, double endLat, int additionalTravelTime) {
        RestBuilder rest = new RestBuilder()
        def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$startLat&flon=$startLon&tlat=$endLat&&tlon=$endLon"
        def json = new JsonBuilder()
        for (int i = 1; i < 4; i++) {
            try {
                def xml = new XmlSlurper().parseText(rest.get(url).text)
                Integer travelTime = xml.Document.traveltime.toInteger()
                def routeCoordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")

                ArrayList<Node> nodes = new POIParser().parse(new POIApi(startLon, startLat, endLon, endLat))
                ArrayList<Node> nodesFiltered = new ArrayList<>()
                for (Node node : nodes) {
                    if (node.hasTagName()) {
                        nodesFiltered.add(node)
                    }
                }

                ArrayList<Cluster> clusters = getClusters(nodesFiltered, K_MEANS_CLUSTER_SIZE, K_MEANS_ITERATIONS)
                ArrayList<Cluster> filteredClusters = filterOutliers(clusters, K_MEANS_CLUSTER_SIZE, MIN_MEAN_PERCENTAGE_CLUSTER_SIZE)

                def size = routeCoordinates.size()
                json {
                    success true
                    count size
                    data routeCoordinates.collect { it.tokenize(",") }
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
            count 0
            error 'Error: Unable to generate route. Please try again later!'
        }
        render json.toString()
    }
}
