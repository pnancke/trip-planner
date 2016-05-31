package trip.planner.osm.api

import com.google.common.base.Preconditions
import grails.plugins.rest.client.RestBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import static trip.planner.osm.api.LoggingHelper.LOGGING_HELPER

class POIApi {

    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."

    private String url
    private double startLon
    private double startLat
    private double destinationLon
    private double destinationLat
    private RestBuilder rest = new RestBuilder()
    private ResponseEntity response

    String info
    String xmlContent
    HttpStatus status

    POIApi(double startLon, double startLat, double destinationLon, double destinationLat) {
        validateCoords(startLon, startLat, destinationLon, destinationLat)
        url = "http://www.overpass-api.de/api/xapi?*[tourism=attraction][bbox=$startLon,$startLat,$destinationLon,$destinationLat]"
    }

    POIApi(Point start, Point destination) {
        this(start.lon, start.lat, destination.lon, destination.lat)
    }

    POIApi(Pair<Point, Point> pointPair) {
        this(pointPair.a.lon, pointPair.a.lat, pointPair.b.lon, pointPair.b.lat)
    }

    def validateCoords(double startLon, double startLat, double destinationLon, double destinationLat) {
        Preconditions.checkNotNull(startLon)
        Preconditions.checkNotNull(startLat)
        Preconditions.checkNotNull(destinationLon)
        Preconditions.checkNotNull(destinationLat)

        if (!(startLon < destinationLon && startLat < destinationLat)) {
            throw new IllegalArgumentException("The start-position is higher or equals to the destination-position.")
        }

        this.startLon = startLon
        this.startLat = startLat
        this.destinationLon = destinationLon
        this.destinationLat = destinationLat
    }

    /**
     *
     * @return success - if the POIApi xmlContent is valid or not
     */
    boolean doRequest() {
        clearFields()
        LOGGING_HELPER.startTimer()

        LOGGING_HELPER.infoLog "POIApi Request to: $url"
        response = rest.get(this.url).responseEntity

        this.info = response.metaPropertyValues.get(0).value
        this.xmlContent = response.metaPropertyValues.get(2).value
        this.status = response.metaPropertyValues.get(3).value as HttpStatus
        boolean success = !xmlContent.contains(ANOTHER_SERVICE_MESSAGE)

        LOGGING_HELPER.logTime(POIApi.class.getSimpleName())
        success
    }

    boolean doRequest(Pair<Point, Point> bbox) {
        this.url = "http://www.overpass-api.de/api/xapi?*[tourism=attraction][bbox=" +
                "$bbox.a.lon,$bbox.a.lat,$bbox.b.lon,$bbox.b.lat]"
        doRequest()
    }

    def clearFields() {
        info = ""
        xmlContent = ""
        status = null
    }

    double getStartLon() {
        return startLon
    }

    double getStartLat() {
        return startLat
    }

    double getDestinationLon() {
        return destinationLon
    }

    double getDestinationLat() {
        return destinationLat
    }
}