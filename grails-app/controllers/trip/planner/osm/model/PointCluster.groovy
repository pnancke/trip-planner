package trip.planner.osm.model

import com.google.common.base.Preconditions
import trip.planner.osm.api.Point


class PointCluster {

    ArrayList<Point> points
    public Point clusterCenter

    public PointCluster(Point clusterCenter) {
        Preconditions.checkNotNull(clusterCenter)
        points = new ArrayList<Point>()
        this.clusterCenter = clusterCenter
    }

    public void addPoint(double[] point) {
        this.points.add(new Point(point[1], point[0]))
    }

    void setClusterCenter(Point clusterCenter) {
        Preconditions.checkNotNull(clusterCenter)
        this.clusterCenter = clusterCenter
    }

    Point getClusterCenter() {
        return clusterCenter
    }

    public int size() {
        this.points.size()
    }

    @Override
    public String toString() {
        return "PointCluster{" +
                "clusterCenter=" + clusterCenter.lat + "," + clusterCenter.lon +
                ", Points=" + points +
                '}';
    }
}
