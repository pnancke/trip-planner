package trip.planner.osm.api

import com.google.common.base.Preconditions
import grails.plugins.rest.client.RestBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import trip.planner.util.ActiveTimer

class POIApi {

    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."

    private static Log log = LogFactory.getLog(POIApi.class)

    private String url
    private RestBuilder rest = new RestBuilder()
    private ResponseEntity response

    String info
    String xmlContent
    HttpStatus status

    private double startLat
    private double destinationLat
    private double startLon
    private double destinationLon

    POIApi(double startLon, double startLat, double destinationLon, double destinationLat) {
        if (validateCoords(startLon, startLat, destinationLon, destinationLat)) {
            double tmpLon = startLon
            startLon = destinationLon
            destinationLon = tmpLon

            double tmpLat = startLat
            startLat = destinationLat
            destinationLat = tmpLat
        }
        this.startLat = startLat
        this.destinationLat = destinationLat
        this.startLon = startLon
        this.destinationLon = destinationLon
        url = "http://www.overpass-api.de/api/xapi?node[tourism=attraction][name=*][bbox=$startLon,$startLat,$destinationLon,$destinationLat]"
    }

    POIApi(Point start, Point destination) {
        this(start.lon, start.lat, destination.lon, destination.lat)
    }

    POIApi(Pair<Point, Point> pointPair) {
        this(pointPair.a.lon, pointPair.a.lat, pointPair.b.lon, pointPair.b.lat)
    }

    /**
     * @param startLon
     * @param startLat
     * @param destinationLon
     * @param destinationLat
     * @return swapIsNecessary
     */
    static boolean validateCoords(double startLon, double startLat, double destinationLon, double destinationLat) {
        Preconditions.checkNotNull(startLon)
        Preconditions.checkNotNull(startLat)
        Preconditions.checkNotNull(destinationLon)
        Preconditions.checkNotNull(destinationLat)

        if (startLat == destinationLat && destinationLon == startLon) {
            throw new IllegalArgumentException("The start-position equals the destination-position.")
        }

        return !(startLon < destinationLon && startLat < destinationLat)
    }

    /**
     *
     * @return success - if the POIApi xmlContent is valid or not
     */
    boolean doRequest() {
        ActiveTimer timer = new ActiveTimer()

        log.info "POIApi Request to: $url"
        response = rest.get(this.url).responseEntity
        this.info = response.metaPropertyValues.get(0).value
        this.xmlContent = response.metaPropertyValues.get(2).value
        this.status = response.metaPropertyValues.get(3).value as HttpStatus
        boolean success = !xmlContent.contains(ANOTHER_SERVICE_MESSAGE)

        timer.stopAndLog(log)
        success
    }

    public Pair<Point, Point> getBBox() {
        return new Pair<Point, Point>(new Point(startLon, startLat)
                , new Point(destinationLon, destinationLat))
    }
}
