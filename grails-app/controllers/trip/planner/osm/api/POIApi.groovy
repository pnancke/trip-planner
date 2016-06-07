package trip.planner.osm.api

import com.google.common.base.Preconditions
import grails.plugins.rest.client.RestBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import trip.planner.util.ActiveTimer

import java.util.stream.Collectors

class POIApi implements Api {

    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."
    private static Log log = LogFactory.getLog(POIApi.class)

    private String url
    private RestBuilder rest = new RestBuilder()
    private ResponseEntity response
    private BBox bbox
    private Closure<File> writer

    String info

    String xmlContent
    HttpStatus status
    public POIApi(BBox bbox) {
        Preconditions.checkNotNull(bbox)
        this.bbox = bbox
        url = "http://www.overpass-api.de/api/xapi?node[tourism=attraction][name=*][bbox=${bbox.toString()}]"
    }

    @Override
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

    public BBox getBBox() {
        this.bbox
    }

    /**
     * @return closure
     * that writes the xml contant to a file and return if the process succeeds or not
     */
    Closure<File> getWriter() {
        Preconditions.checkNotNull(writer)
        return writer
    }

    void setWriter(Closure<File> fileWriteClos) {
        Preconditions.checkNotNull(fileWriteClos)
        this.writer = fileWriteClos
    }

    public static BBox calcBBoxOfRoute(List<Point> route) {
        List<Double> lats = route.stream().map({ point -> point.lat }).collect(Collectors.toList())
        List<Double> lons = route.stream().map({ point -> point.lon }).collect(Collectors.toList())
        new BBox(lons.min(), lats.min(), lons.max(), lats.max())
    }

    public static List<BBox> calcResultingBBoxes(List<Point> route) {
        BBox bbox = calcBBoxOfRoute(route)
        List<BBox> bboxes = bbox.splitIntoPieces(10)
        List resultingBBoxes = bboxes.stream().filter({
            box ->
                route.stream().anyMatch(
                        { r -> box.contains(r) })
        }).collect(Collectors.toList())
        resultingBBoxes
    }
}
