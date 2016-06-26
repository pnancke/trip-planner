package trip.planner.osm.api


class Polygon {

    private List<Point> points

    Polygon(List<Point> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Empty polygon.")
        }

        if (points.first() != points.last()) {
            throw new IllegalArgumentException("Invalid points for polygon. Please add the begin-point to close the polygon.")
        }
        this.points = points
    }

    static String coords(List<Double> coords) {
        if (coords.isEmpty()) {
            throw new IllegalArgumentException("Empty polygon.")
        }
        if (coords.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid coords for polygon. Please add or remove one Double value.")
        }
        if (coords.first() != coords.get(coords.size() - 2) || coords.get(1) != coords.last()) {
            throw new IllegalArgumentException("Invalid coords for polygon. Please add the begin-point to close the polygon.")
        }

        String result = 'Polygon(('
        coords.eachWithIndex { Double entry, int i ->
            if (i % 2 == 1) {
                if (coords.size() - 1 == i) {
                    result += entry + '))'
                } else {
                    result += entry + ','
                }
            } else {
                result += entry + ' '
            }
        }
        result
    }

    private String pointsToString() {
        points.stream().map({ p ->
            p.toShortString()
        }).inject({ a, b -> a + ',' + b })
    }

    @Override
    public String toString() {
        if (Objects.nonNull(points)) {
            return 'Polygon((' + pointsToString() + '))'
        } else {
            return ''
        }
    }
}
