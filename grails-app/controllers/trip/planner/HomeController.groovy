package trip.planner

import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonBuilder
import org.springframework.web.client.RestClientException

class HomeController {

    def index() {}

    def getRoute(double startLon, double startLat, double endLon, double endLat) {
        RestBuilder rest = new RestBuilder()
        def url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=$startLat&flon=$startLon&tlat=$endLat&&tlon=$endLon"
        def json = new JsonBuilder()
        for (int i = 1; i < 4; i++) {
            try {
                def xml = new XmlSlurper().parseText(rest.get(url).text)
                def coordinates = xml.Document.Folder.Placemark.LineString.coordinates.text().trim().tokenize("\n")
                def size = coordinates.size()
                json {
                    success true
                    count size
                    data coordinates.collect { it.tokenize(",") }
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
