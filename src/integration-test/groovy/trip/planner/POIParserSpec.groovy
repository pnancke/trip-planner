package trip.planner

import org.springframework.http.HttpStatus
import spock.lang.Ignore
import spock.lang.Specification
import trip.planner.osm.api.BBox
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Point
import trip.planner.osm.model.Node

class POIParserSpec extends Specification {

    private static final BBox BERLIN_BBOX = new BBox(new Point(13.35869, 52.48654), new Point(13.50975, 52.53544))
    private static final BBox EIFFEL_BBOX = new BBox(new Point(2.291188, 48.856867), new Point(2.296391, 48.860467))
    private static final BBox ANOTHER_BBOX = new BBox(new Point(12.291503, 51.197322), new Point(13.508238, 52.679259))
    private static final BBox GERMANY_BBOX = new BBox(new Point(7.469916, 47.718927), new Point(13.418928, 54.393428))
    private static final BBox OCEAN_BBOX = new BBox(new Point(2.193275, 54.820783), new Point(2.211038, 54.827618))

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
    def "a big bbox works"() {
        when:
        def api = new POIApi(GERMANY_BBOX)
        List<Node> nodes = POIParser.parse(api)

        then: "a list of valid nodes"
        validateRequest(api, nodes)
        nodes.stream().allMatch({ x -> x.id != null && x.lat != null && x.lon != null })
    }

    @Ignore
    def "many requests works"() {
        when:
        List<Node> first = doRequestAndParse(ANOTHER_BBOX)
        List<Node> second = doRequestAndParse(EIFFEL_BBOX)
        List<Node> third = doRequestAndParse(BERLIN_BBOX)

        then:
        first != null
        second != null
        third != null
    }

    private static List<Node> doRequestAndParse(BBox bbox) {
        POIApi api = new POIApi(bbox)
        POIParser.parse(api)
    }

    private static void validateRequest(POIApi api, List<Node> nodes) {
        api.status == HttpStatus.OK
        nodes.size() > 0
    }
}
