package trip.planner.osm.model

import trip.planner.osm.api.Point


class WaypointRoute {

    List<Point> waypoints
    RouteSegment[] routeSegments

    WaypointRoute(List<Point> waypoints) {
        this.waypoints = waypoints
        routeSegments = new RouteSegment[waypoints.size()]
        this.routeSegments = generateRouteSegments(waypoints)
    }

    void setRouteSegment(RouteSegment routeSegment, int numberOfRouteSegment) {
        routeSegments[numberOfRouteSegment] = routeSegment
    }

    List<RouteSegment> generateRoute() {
        return this.routeSegments.toList().route.collectMany { it }
    }

    static List<RouteSegment> generateRouteSegments(List<Point> wayPoints) {
        List<RouteSegment> routeSegments = new ArrayList<>()
        for (int i = 0; i < wayPoints.size() - 1; i++) {
            routeSegments.add(new RouteSegment(wayPoints.get(i), wayPoints.get(i + 1)))
        }
        routeSegments
    }

    List<RouteSegment> getRouteSegments(){
        this.routeSegments.toList()
    }

    @Override
    public String toString() {
        return "WaypointRoute{" +
                "waypoints=" + waypoints +
                ", routeSegments=" + Arrays.toString(routeSegments) +
                '}';
    }
}
