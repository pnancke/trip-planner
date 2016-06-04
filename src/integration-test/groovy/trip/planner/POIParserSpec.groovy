package trip.planner

import org.grails.core.util.StopWatch
import org.springframework.http.HttpStatus
import spock.lang.Ignore
import spock.lang.Specification
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Pair
import trip.planner.osm.api.Point
import trip.planner.osm.model.Node

import java.util.concurrent.TimeoutException

class POIParserSpec extends Specification {

    public static final Pair BERLIN_BBOX = new Pair(new Point(13.35869, 52.48654), new Point(13.50975, 52.53544))
    public static final Pair EIFFEL_BBOX = new Pair(new Point(2.291188, 48.856867), new Point(2.296391, 48.860467))
    public static final Pair ANOTHER_BBOX = new Pair(new Point(12.291503, 51.197322), new Point(13.508238, 52.679259))
    public static final Pair GERMANY_BBOX = new Pair(new Point(7.469916, 47.718927), new Point(13.418928, 54.393428))
    public static final Pair OCEAN_BBOX = new Pair(new Point(2.193275, 54.820783), new Point(2.211038, 54.827618))
    public static final int HUNDRED_SECONDS = 100

    private StopWatch watch = new StopWatch();

    def "parsing from osm-xapi works"() {
        given: "a request to the POIApi"
        def api = new POIApi(BERLIN_BBOX)
        List<Node> nodes = POIParser.parse(api)

        expect: "a list of valid nodes"
        validateRequest(api, nodes)
        nodes.stream().allMatch({ x -> x.id != null && x.lat != null && x.lon != null })
    }

    def "found eiffel-tower"() {
        given: "eiffel-tower coords"
        def api = new POIApi(EIFFEL_BBOX)
        List<Node> nodes = POIParser.parse(api)

        expect: "existing eiffel-tower-node"
        validateRequest(api, nodes)
        nodes.stream().filter({ node -> node.tags != null }).anyMatch({ x ->
            x.tags.stream().filter(
                    { tag -> tag.getV() == "Bureau de Gustave Eiffel" }).findAny().isPresent()
        })
    }

    def "bbox with no nodes succeeds"() {
        when:
        List<Node> list = doRequestAndParse(OCEAN_BBOX)

        then:
        list.isEmpty()
    }

    @Ignore
    def "too big bbox returns TimeoutException"() {
        when:
        doRequestAndParse(GERMANY_BBOX)

        then:
        thrown(TimeoutException)
    }

    @Ignore
    def "three requests take less than hundred seconds without service error"() {
        when:
        watch.start()
        doRequestAndParse(ANOTHER_BBOX)
        doRequestAndParse(EIFFEL_BBOX)
        doRequestAndParse(BERLIN_BBOX)

        double time = stopTime()

        then:
        time < HUNDRED_SECONDS
    }

    private static List<Node> doRequestAndParse(Pair bbox) {
        POIApi api2 = new POIApi(bbox)
        POIParser.parse(api2)
    }

    private double stopTime() {
        watch.stop()
        def time = watch.totalTimeSeconds
        time
    }

    private static void validateRequest(POIApi api, List<Node> nodes) {
        api.status == HttpStatus.OK
        nodes.size() > 0
    }
}
