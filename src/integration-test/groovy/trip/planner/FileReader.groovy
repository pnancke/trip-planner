package trip.planner

import trip.planner.osm.api.Point

import java.util.stream.Collectors

class FileReader {

    public static final File MUNICH_POLYGON = new File("./src/integration-test/resources/munich_polygon")
    public static final File LE_MUNICH_ROUTE = new File("./src/integration-test/resources/le_munich_route")

    public static String extractLine(File file) {
        String text = file.getText('UTF-8')
        List<String> lines = text.split("\n").toList()
        lines.get(0)
    }

    public static List<Point> extractRoute(File file) {
        String text = file.getText('UTF-8')
        List<String> lines = text.split("\n").toList()
        lines.get(0).tokenize(",").stream().map({
            String coordLine ->
                List<String> coordStrings = coordLine.tokenize(" ")
                new Point(Double.valueOf(coordStrings.get(0)), Double.valueOf(coordStrings.get(1)))
        }).collect(Collectors.toList())
    }
}
