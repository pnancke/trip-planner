package trip.planner.osm.api

import trip.planner.util.ClusterHelper

import java.util.stream.Collectors
import java.util.stream.IntStream

class Circle {

    List<Point> route
    private Point center
    private double r

    public Circle(Point center, Double r) {
        this.r = r
        this.center = center
        route = generateCircleAround(center, r)
    }

    public static List<Point> generateCircleAround(Point p, Double r) {
        List<Point> points = new ArrayList<>()
        for (double i = 0; i < 3.14; i = i + 0.2) {
            points.add(new Point(p.lat + r * Math.cos(i), p.lon + r * Math.sin(i)))
            points.add(new Point(p.lat - r * Math.cos(i), p.lon - r * Math.sin(i)))
        }
        points.unique()
    }

    public static List<Point> generateOpenCircle(Point start, Point end, Point p, Double r) {
        List<Point> points = generateCircleInPolygonOrder(p, r)
        Pair<Integer, Integer> nearestIndices = extractNearestIndices(start, end, points)
        List<Point> openCircleShape = new ArrayList<>()

        List<Integer> beginning = IntStream.range(nearestIndices.a, points.size() - 1).boxed()
                .collect(Collectors.toList())
        openCircleShape.addAll(points.getAt(beginning))

        List<Integer> ending = IntStream.range(0, nearestIndices.b).boxed().collect(Collectors.toList())
        openCircleShape.addAll(points.getAt(ending))

        openCircleShape
    }

    public static Pair<Integer, Integer> extractNearestIndices(Point start, Point end, List<Point> points) {
        int one = points.indexOf(ClusterHelper.nearest(start, points))
        int two = points.indexOf(ClusterHelper.nearest(end, points))
        int begin
        int dest
        if (one > two) {
            begin = one
            dest = two
        } else {
            begin = two
            dest = one
        }
        new Pair<Integer, Integer>(begin, dest)
    }

    private static ArrayList<Point> generateCircleInPolygonOrder(Point p, double r) {
        List<Point> points = new ArrayList<>()
        for (double i = 0; i < 3.14; i = i + 0.2) {
            points.add(new Point(p.lat - r * Math.cos(i), p.lon + r * Math.sin(i)))
        }
        for (double i = 0; i < 3.14; i = i + 0.2) {
            points.add(new Point(p.lat + r * Math.cos(i), p.lon - r * Math.sin(i)))
        }
        points.unique()
        points
    }

    public boolean contains(Point point) {
        double distance = ClusterHelper.physicalDistance(center, point)
        distance <= r
    }
}
