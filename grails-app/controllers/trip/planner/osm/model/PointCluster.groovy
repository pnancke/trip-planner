package trip.planner.osm.model

import trip.planner.osm.api.Point


class PointCluster {

    ArrayList<Point> points
    public Point clusterCenter

    public PointCluster() {
        points = new ArrayList<Point>()
    }

    public void addPoint(double[] point){
        this.points.add(new Point(point[1], point[0]))
    }

    void setClusterCenter(Point clusterCenter) {
        this.clusterCenter = clusterCenter
    }

    Point getClusterCenter() {

        return clusterCenter
    }

    public int size(){
        this.points.size()
    }

    @Override
    public String toString() {
        return "PointCluster{" +
                "clusterCenter=" + clusterCenter.lat + ","  + clusterCenter.lon+
                ", Points=" + points +
                '}';
    }
}
