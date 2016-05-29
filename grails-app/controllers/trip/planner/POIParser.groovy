package trip.planner

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.mapper.CannotResolveClassException
import trip.planner.osm.api.POIApi
import trip.planner.osm.model.Node
import trip.planner.osm.model.POIRequest
import trip.planner.osm.model.Tag

import java.util.concurrent.TimeoutException

import static trip.planner.osm.api.LoggingHelper.LOGGING_HELPER

class POIParser {

    public static
    final String FALSE_QUERY_MESSAGE = "The value of attribute \"n\" of the element \"bbox-query\" must " +
            "always be greater or equal than the value of attribute \"s\"."
    public static final String UNAVAILABLE_MESSAGE = "Service is still running, please wait."
    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."
    public static final long FIVE_SECONDS = 5000L

    static List<Node> parse(POIApi api) {
        LOGGING_HELPER.startTimer()

        for (int i = 0; i < 4; i++) {
            if (api.doRequest()) {
                String term = api.xmlContent
                if (term.contains(FALSE_QUERY_MESSAGE)) {
                    throw new IllegalArgumentException("False query input parameter: $FALSE_QUERY_MESSAGE")
                }

                try {
                    POIRequest request = parsePOIRequest(term)

                    if (request.remark != null) {
                        throw new TimeoutException(request.remark)
                    }

                    if (request.nodes == null) {
                        throw new NoSuchElementException("No nodes found in given bbox.")
                    }

                    LOGGING_HELPER.logTime(POIParser.class.getSimpleName())

                    return request.nodes
                } catch (CannotResolveClassException crce) {
                    throw new IllegalArgumentException("Cannot parse: $term. Cause from parser: " + crce.message)
                }
            } else {
                Thread.sleep(FIVE_SECONDS)
            }
        }

        LOGGING_HELPER.logTime(POIParser.class.getSimpleName())

        if (api.xmlContent.contains(ANOTHER_SERVICE_MESSAGE)) {
            throw new RuntimeException(UNAVAILABLE_MESSAGE)
        } else {
            throw new RuntimeException("Unknown error with: $api.xmlContent")
        }
    }

    private static POIRequest parsePOIRequest(String term) {
        XStream xstream = new XStream();
        xstream.ignoreUnknownElements()
        xstream.processAnnotations(POIRequest.class);
        xstream.processAnnotations(Node.class);
        xstream.processAnnotations(Tag.class);
        POIRequest request = (POIRequest) xstream.fromXML(term);
        request
    }
}
