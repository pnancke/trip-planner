package trip.planner

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.mapper.CannotResolveClassException
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Pair
import trip.planner.osm.api.PlaceValidator
import trip.planner.osm.api.Point
import trip.planner.osm.model.Node
import trip.planner.osm.model.POIRequest
import trip.planner.osm.model.Tag

import java.util.concurrent.TimeoutException
import java.util.stream.Collectors

import static trip.planner.osm.api.LoggingHelper.LOGGING_HELPER

class POIParser {

    public static
    final String FALSE_QUERY_MESSAGE = "The value of attribute \"n\" of the element \"bbox-query\" must " +
            "always be greater or equal than the value of attribute \"s\"."
    public static final String UNAVAILABLE_MESSAGE = "Service is still running, please wait."
    public static final String ANOTHER_SERVICE_MESSAGE = "Another request from your IP is still running."
    public static final long TEN_SECONDS = 10000L
    public static final int TWO_HUNDRED_SECONDS = 200
    public static final int SIXTY_SECONDS = 60

    static List<Node> parse(POIApi api) {
        int timerIndex = LOGGING_HELPER.newTimer()
        LOGGING_HELPER.startTimer(timerIndex)
        List<Pair<Point, Point>> split = PlaceValidator.split(api)

        if (split.size() == 1) {
            return handleRequest({ api.doRequest() }, timerIndex, api)
        } else {
            BigDecimal minutes = (split.size() * TWO_HUNDRED_SECONDS) / SIXTY_SECONDS
            LOGGING_HELPER.infoLog "Split big bbox into " + split.size() + " pieces.\n" +
                    "In worst the whole calculation needs $minutes m."

            return split.stream().flatMap({ area -> handleRequest({ api.doRequest(area) }, timerIndex, api) })
                    .collect(Collectors.toList())
        }
    }

    private static List<Node> handleRequest(Closure<Boolean> doRequestClosure, int timerIndex, POIApi api) {
        for (int i = 0; i < 4; i++) {
            if (doRequestClosure.call()) {
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

                    LOGGING_HELPER.logTime(POIParser.class.getSimpleName(), timerIndex)

                    return request.nodes
                } catch (CannotResolveClassException crce) {
                    throw new IllegalArgumentException("Cannot parse: $term. Cause from parser: " + crce.message)
                }
            } else {
                LOGGING_HELPER.infoLog("Wait 10 s then try again.")
                Thread.sleep(TEN_SECONDS)
            }
        }

        LOGGING_HELPER.logTime(POIParser.class.getSimpleName(), timerIndex)

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
