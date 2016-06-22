package trip.planner.osm.model

import trip.planner.osm.api.Point


class RouteSegment {
    List<Point> route
    Point start
    Point destination

    RouteSegment(Point start, Point destination, List<Point> route) {
        this.route = route
        this.start = start
        this.destination = destination
    }

    void setRoute(List<Point> route) {
        this.route = route
    }

    RouteSegment(Point start, Point destination) {
        this.start = start
        this.destination = destination
    }

    @Override
    public String toString() {
        return "RouteSegment{" +
                "route=" + route +
                ", start=" + start +
                ", destination=" + destination +
                '}';
    }
}
