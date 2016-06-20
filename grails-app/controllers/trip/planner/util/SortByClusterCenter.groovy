package trip.planner.util

import trip.planner.osm.api.Point
import trip.planner.osm.model.PointCluster

import static trip.planner.util.ClusterHelper.distance

public class SortByClusterCenter implements Comparator<PointCluster> {
    Point currentLoc

    public SortByClusterCenter(Point current) {
        currentLoc = current
    }

    @Override
    public int compare(final PointCluster cluster1, final PointCluster cluster2) {
        double distanceToPlace1 = distance(currentLoc, cluster1.clusterCenter)
        double distanceToPlace2 = distance(currentLoc, cluster2.clusterCenter)
        return (int) (distanceToPlace1 - distanceToPlace2)
    }
}
