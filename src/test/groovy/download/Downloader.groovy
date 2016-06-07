package download

import spock.lang.Ignore
import spock.lang.Specification
import trip.planner.osm.api.BBox
import trip.planner.osm.api.POIApi
import trip.planner.osm.api.Point
import trip.planner.util.FileClosures

import java.util.stream.Collectors


class Downloader extends Specification {
    private List<BBox> bboxes
    private static final BBox BERLIN_BBOX = new BBox(new Point(13.35869, 52.48654), new Point(13.50975, 52.53544))

    def setup() {
        bboxes = CoordReader.readCoords()
    }

    @Ignore
    def "download mini bbox"() {
        when:
        POIDownloadParser parser = new POIDownloadParser()
        POIDownloadParser.cleanPoiFile()

        POIApi api = new POIApi(BERLIN_BBOX)
        api.setWriter(FileClosures.ONE_BBOX)
        parser.parse(api)
        then:
        true
    }

    @Ignore
    def "download multiple bboxes"() {
        when:
        POIDownloadParser parser = new POIDownloadParser()
        POIDownloadParser.cleanPoiFile()

        List<POIApi> apis = BERLIN_BBOX.splitIntoPieces(2).stream().map({ it -> new POIApi(it) })
                .collect(Collectors.toList())
        FileClosures.convertToFileWritable(apis).each {
            parser.parse(it)
        }
        then:
        true
    }

    def "download"() {
        when:
        POIDownloadParser parser = new POIDownloadParser()
        POIDownloadParser.cleanPoiFile()
        bboxes.each {
            List<POIApi> apis = it.splitWithDifference(1.0).stream().map({ box -> new POIApi(box) })
                    .collect(Collectors.toList())
            FileClosures.convertToFileWritable(apis).each {
                parser.parse(it)
            }
        }

        then:
        true
    }

    def "clean poi-file"() {
        when:
        POIDownloadParser.cleanPoiFile()
        then:
        true
    }
}
