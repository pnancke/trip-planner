package trip.planner.osm.model

import com.google.common.base.Preconditions
import trip.planner.osm.api.Point

import static trip.planner.util.ClusterHelper.distance


class PointCluster {

    ArrayList<Point> points
    public Point clusterCenter
    Double clusterRange

    public PointCluster(Point clusterCenter) {
        Preconditions.checkNotNull(clusterCenter)
        points = new ArrayList<Point>()
        this.clusterCenter = clusterCenter
        this.clusterRange = 0
    }

    public void addPoint(double[] point) {
        this.points.add(new Point(point[0], point[1]))
        calculateClusterRange()
    }

    void setClusterCenter(Point clusterCenter) {
        Preconditions.checkNotNull(clusterCenter)
        this.clusterCenter = clusterCenter
        calculateClusterRange()
    }

    Point getClusterCenter() {
        return clusterCenter
    }

    public int size() {
        this.points.size()
    }

    private void calculateClusterRange(){
        for (point in this.points) {
            double currDistance = distance(point, this.clusterCenter)
            if (currDistance > clusterRange) {
                this.clusterRange = currDistance
            }
        }
    }

    @Override
    public String toString() {
        return "PointCluster{" +
                "points=" + points +
                ", clusterCenter=" + clusterCenter +
                ", clusterRange=" + clusterRange +
                '}';
    }
}
