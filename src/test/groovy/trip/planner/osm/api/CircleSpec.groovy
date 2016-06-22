package trip.planner.osm.api

import spock.lang.Specification

import static trip.planner.util.ClusterHelper.physicalDistance

class CircleSpec extends Specification {

    private static final BigDecimal A_HALF = 0.5
    private static final BigDecimal DISTANCE_BETWEEN_CIRCLE_POINTS = 0.07819410612328302
    private static final int TWENTY_TWO = 22

    def "circle contains point - works"() {
        when:
        Circle circle = new Circle(new Point(50, 10), 0.06)

        then:
        circle.contains(new Point(50, 10))
    }

    def "circle contains not - works"() {
        when:
        Circle circle = new Circle(new Point(50, 10), 0.06)

        then:
        !circle.contains(new Point(51, 11))
    }

    def "circle contains self"() {
        when:
        Circle circle = new Circle(new Point(50, 10), 0.06)

        then:
        circle.contains(circle.getRoute().get(0))
    }

    def "generate open circle works"() {
        when:
        Point start = new Point(50, 10.5)
        Point end = new Point(51, 10.5)
        Point center = new Point(50.5, 10)
        List<Point> openCircle = Circle.generateOpenCircle(start, end, center, A_HALF)
        List<Point> circle = Circle.generateCircleAround(center, A_HALF)

        then:
        openCircle.size() < circle.size()
        openCircle.size() == TWENTY_TWO
        physicalDistance(openCircle.get(0), openCircle.get(openCircle.size() - 1)) > DISTANCE_BETWEEN_CIRCLE_POINTS
    }

    def "generate open circle with same points"() {
        when:
        Point start = new Point(50, 10.5)
        Point end = new Point(50, 10.5)
        Point center = new Point(50.5, 10)
        List<Point> openCircle = Circle.generateOpenCircle(start, end, center, A_HALF)
        List<Point> circle = Circle.generateCircleAround(center, A_HALF)

        then:
        openCircle.size() < circle.size()
        openCircle.size() == 31
    }
}
