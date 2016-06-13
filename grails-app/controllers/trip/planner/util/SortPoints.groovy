package trip.planner.util

import trip.planner.osm.api.Point

import static trip.planner.util.ClusterHelper.distance

public class SortPoints implements Comparator<Point> {
    Point currentLoc

    public SortPoints(Point current) {
        currentLoc = current
    }

    @Override
    public int compare(final Point point1, final Point point2) {
        double distanceToPlace1 = distance(currentLoc, point1)
        double distanceToPlace2 = distance(currentLoc, point2)
        return (int) (distanceToPlace1 - distanceToPlace2)
    }
}
