package download

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.mapper.CannotResolveClassException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import trip.planner.osm.api.POIApi
import trip.planner.osm.model.POIRequest

class POIDownloadParser {

    public static
    final String FALSE_QUERY_MESSAGE = "The value of attribute \"n\" of the element \"bbox-query\" must " +
            "always be greater or equal than the value of attribute \"s\"."
    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."
    public static final long TEN_SECONDS = 10000L
    public static final String POI_XML_PATH = "./grails-app/controllers/download/pois.xml"
    public static final String SAMPLE_POI_PATH = "./grails-app/controllers/download/pois-sample.xml"
    public static final String COORD_FILE_PATH = "./grails-app/controllers/download/coords"

    private static Log log = LogFactory.getLog(POIDownloadParser.class)

    private File file

    POIDownloadParser() {
        prepareFile()
    }

    /**
     *
     * @param api
     * @return success - if download succeeds
     */
    public boolean parse(POIApi api) {
        log.info "Begin downloading of poi-request with bbox: $api.BBox"
        for (int i = 0; i < 4; i++) {
            if (api.doRequest()) {
                String term = api.xmlContent
                if (term.contains(FALSE_QUERY_MESSAGE)) {
                    throw new IllegalArgumentException("False query input parameter: $FALSE_QUERY_MESSAGE")
                }

                try {
                    POIRequest request = parsePOIDownloadRequest(term)
                    if (api.xmlContent.contains(ANOTHER_SERVICE_MESSAGE)) {
                        log.info "another service error, try $i"
                        continue
                    }

                    if (request.remark != null) {
                        log.info "timeout, try $i"
                        continue
                    }
                    api.getWriter().call(file, term)
                    return true
                } catch (CannotResolveClassException crce) {
                    log.info "Cannot parse: $term. Cause from download.downloader: $crce.message, try $i"
                    Thread.sleep(TEN_SECONDS)
                }
            } else {
                log.info "Wait 10 s then try again."
                Thread.sleep(TEN_SECONDS)
            }
        }
        if (api.xmlContent.contains(ANOTHER_SERVICE_MESSAGE)) {
            log.info "another service error"
        } else {
            log.info "Unknown error with: $api.xmlContent"
        }
        return false
    }

    private static POIRequest parsePOIDownloadRequest(String term) {
        XStream xstream = new XStream();
        xstream.ignoreUnknownElements()
        xstream.processAnnotations(POIRequest.class);
        POIRequest request = (POIRequest) xstream.fromXML(term);
        request
    }

    public boolean prepareFile() {
        file = new File(POI_XML_PATH)
        if (!this.file.exists()) {
            return this.file.createNewFile()
        }
        return true;
    }

    public static boolean cleanPoiFile() {
        try {
            new PrintWriter(POI_XML_PATH).close()
            return true
        } catch (Exception e) {
            log.info "Error by cleaning poi-file, reason: " + e.getMessage()
            return false;
        }
    }
}
