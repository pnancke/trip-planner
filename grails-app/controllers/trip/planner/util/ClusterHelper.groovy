package trip.planner.util

import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.DoubleVector
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.data.type.TypeUtil
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.relation.Relation
import trip.planner.osm.api.Pair
import trip.planner.osm.api.Point
import trip.planner.osm.model.Node
import trip.planner.osm.model.PointCluster

import static trip.planner.osm.api.ElkiWrapper.extractClusters
import static trip.planner.osm.api.ElkiWrapper.filterOutliers

class ClusterHelper {

    private static int K_MEANS_CLUSTER_SIZE = 10
    private static final int MAX_K_MEANS_ITERATIONS = 50
    private static final BigDecimal MIN_MEAN_PERCENTAGE_CLUSTER_SIZE = 0.8
    public static final int MAX_DISTANCE_TO_CLUSTER_CENTER = 10

    public static double distance(Point a, Point b) {
        double theta = a.lon - b.lon
        double dist = Math.sin(deg2rad(a.lat)) * Math.sin(deg2rad(b.lat)) + Math.cos(deg2rad(a.lat)) * Math.cos(deg2rad(b.lat)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515 * 1.609344
        dist
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI)
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0)
    }

    public static List<Point> convert(List<Node> nodes) {
        List<Point> points = new ArrayList<>()
        nodes.each {
            points.add(new Point(it.lon, it.lat))
        }
        points
    }

    public static List<PointCluster> convert(Pair<Database, List<Cluster<KMeansModel>>> pair) {
        List<PointCluster> pointClusters = new ArrayList<PointCluster>()
        Database db = pair.getA()
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        List<Cluster<KMeansModel>> kMeansModelCluster = pair.getB()
        kMeansModelCluster.each { Cluster<KMeansModel> clu ->
            def cluster = new PointCluster()
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                DoubleVector v = rel.get(it) as DoubleVector
                cluster.addPoint(v.getValues())
            }
            def clusterCenter = clu.getModel().getPrototype()
            cluster.setClusterCenter(new Point(clusterCenter.get(1), clusterCenter.get(0)))
            pointClusters.add(cluster)
        }
        pointClusters
    }

    public static List<PointCluster> extractCoordinateClusterWithoutOutliers(List<Node> nodes) {
        Pair<Database, List<Cluster<KMeansModel>>> pair =
                extractClusters(convert(nodes), K_MEANS_CLUSTER_SIZE, MAX_K_MEANS_ITERATIONS)
        List<PointCluster> unfilteredClusters = convert(pair)
        List<PointCluster> filteredClusters = filterOutliers(unfilteredClusters,
                K_MEANS_CLUSTER_SIZE,
                MIN_MEAN_PERCENTAGE_CLUSTER_SIZE)

        List<PointCluster> splittedClusters = splitClusters(filteredClusters)
        splittedClusters
    }

    private static List<PointCluster> splitClusters(List<PointCluster> filteredClusters) {
        List<PointCluster> filteredClustersByRange = new ArrayList<PointCluster>()
        Queue<PointCluster> queue = new LinkedList<PointCluster>(filteredClusters);
        while (!queue.isEmpty()) {
            PointCluster cluster = queue.remove()
            int maxPointClusterDistance = 0
            for (point in cluster.points) {
                double currDistance = distance(point, cluster.clusterCenter)
                if (currDistance > maxPointClusterDistance) {
                    maxPointClusterDistance = currDistance
                }
            }
            if (maxPointClusterDistance > MAX_DISTANCE_TO_CLUSTER_CENTER) {
                def newClusters = convert(extractClusters(cluster.points, K_MEANS_CLUSTER_SIZE, MAX_K_MEANS_ITERATIONS))
                for (it in newClusters) {
                    queue.add(it)
                }
            } else {
                filteredClustersByRange.add(cluster)
            }
        }
        filteredClustersByRange
    }
}
