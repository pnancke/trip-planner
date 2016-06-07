package download

import trip.planner.osm.api.BBox
import trip.planner.osm.api.Point

import java.util.stream.Collectors

class CoordReader {
    static List<BBox> readCoords() {
        File file = new File(POIDownloadParser.COORD_FILE_PATH)

        Closure<Boolean> noCommentsAndNewLines = { s -> !s.contains("/") && !s.isEmpty() }
        List<Point> points = file.readLines().stream()
                .filter(noCommentsAndNewLines)
                .map({ string -> string.split(", ") })
                .map({ array -> new Point(Double.valueOf(array[1]), Double.valueOf(array[0]))
        }).collect(Collectors.toList())

        List<BBox> resultingBBoxes = new ArrayList<>()
        for (int i = 0; i < points.size() - 1; i = i + 2) {
            resultingBBoxes.add(new BBox(points.get(i), points.get(i + 1)))
        }
        resultingBBoxes
    }
}
