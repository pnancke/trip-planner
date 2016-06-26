package trip.planner.osm.api

import grails.plugins.rest.client.RestBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import trip.planner.util.ActiveTimer

class NominationApi {

    private static Log log = LogFactory.getLog(NominationApi.class)
    private String url
    private RestBuilder rest = new RestBuilder()
    private ResponseEntity response

    String info
    String xmlContent
    HttpStatus status
    Point point

    NominationApi(String q) {
        url = "http://nominatim.openstreetmap.org/search?q=$q&format=xml&addressdetails=0&limit=1"
    }

    /**
     *
     * @return success - if the NominationApi xmlContent is valid or not
     */
    boolean doRequest() {
        ActiveTimer timer = new ActiveTimer()

        log.info "NominationApi Request to: $url"
        response = rest.get(this.url).responseEntity
        this.xmlContent = response.metaPropertyValues.get(2).value
        this.status = response.metaPropertyValues.get(3).value as HttpStatus

        def xml = new XmlSlurper().parseText(this.xmlContent)

        String latString = xml.place.@lat
        String lonString = xml.place.@lon

        boolean success
        if (latString.isEmpty() || lonString.isEmpty()) {
            success = false
        } else {
            Double lat = Double.parseDouble(latString)
            Double lon = Double.parseDouble(lonString)
            this.point = new Point(lat, lon)
            success = status == HttpStatus.OK && !lat.naN && !lon.naN
        }
        timer.stopAndLog(log)
        success
    }
}
