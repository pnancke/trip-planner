package trip.planner.util

import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.DoubleVector
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.data.type.TypeUtil
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.relation.Relation
import trip.planner.PointOfInterest
import trip.planner.osm.api.Pair
import trip.planner.osm.api.Point
import trip.planner.osm.model.PointCluster

import java.util.stream.Collectors

import static trip.planner.osm.api.ElkiWrapper.extractClusters

class ClusterHelper {

    private static int K_MEANS_CLUSTER_SIZE = 10
    private static final int MAX_K_MEANS_ITERATIONS = 50
    private static final BigDecimal MIN_MEAN_PERCENTAGE_CLUSTER_SIZE = 0.8
    private static final Double MAX_DISTANCE_TO_CLUSTER_CENTER_IN_KILOMETRES = 5.0
    private static final BigDecimal KILOMETRES_PER_MILE = 1.609344
    private static final int MINUTES_IN_DEGREE = 60
    private static final BigDecimal NAUTICAL_MILE = 1.1515
    private static final BigDecimal SEMICIRCLE_DEGREES = 180.0

    public static double distance(Point a, Point b) {
        physicalDistance(a, b) * MINUTES_IN_DEGREE * NAUTICAL_MILE * KILOMETRES_PER_MILE
    }

    public static double physicalDistance(Point a, Point b) {
        double theta = a.lon - b.lon
        double dist = Math.sin(deg2rad(a.lat)) * Math.sin(deg2rad(b.lat)) + Math.cos(deg2rad(a.lat)) * Math.cos(deg2rad(b.lat)) * Math.cos(deg2rad(theta))

        dist = Math.acos(dist)
        rad2deg(dist)
    }

    private static double rad2deg(double rad) {
        return (rad * SEMICIRCLE_DEGREES / Math.PI)
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / SEMICIRCLE_DEGREES)
    }

    public static List<Point> convert(List<PointOfInterest> pointOfInterests) {
        pointOfInterests.stream().map({ poi -> new Point(poi.point.x, poi.point.y) }).collect(Collectors.toList())
    }

    public static List<PointCluster> convert(Pair<Database, List<Cluster<KMeansModel>>> pair) {
        List<PointCluster> pointClusters = new ArrayList<PointCluster>()
        Database db = pair.getA()
        Relation<NumberVector> rel = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        List<Cluster<KMeansModel>> kMeansModelCluster = pair.getB()
        kMeansModelCluster.each { Cluster<KMeansModel> clu ->
            def clusterCenter = clu.getModel().getPrototype()
            def cluster = new PointCluster(new Point(clusterCenter.get(0), clusterCenter.get(1)))
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                DoubleVector v = rel.get(it) as DoubleVector
                cluster.addPoint(v.getValues())
            }
            pointClusters.add(cluster)
        }
        pointClusters
    }

    public static List<PointCluster> extractCoordinateClusterWithoutOutliers(List<PointOfInterest> pointOfInterests) {
        Pair<Database, ArrayList<Cluster<KMeansModel>>> pair =
                extractClusters(convert(pointOfInterests), K_MEANS_CLUSTER_SIZE, MAX_K_MEANS_ITERATIONS)
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
            if (cluster.clusterRange > MAX_DISTANCE_TO_CLUSTER_CENTER_IN_KILOMETRES) {
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

    static Point nearest(Point p, List<Point> route) {
        route.min {
            Point a, Point b ->
                physicalDistance(p, a) <=> physicalDistance(p, b)
        }
    }

    private static List<PointCluster> filterOutliers(List<PointCluster> clusters,
                                                     int partitionSize,
                                                     double minMeanPercentageClusterSize) {
        int allPointsCount = 0
        clusters.each { allPointsCount += it.size() }
        int averageSize = allPointsCount / partitionSize
        clusters.stream().filter { it -> it.size() > minMeanPercentageClusterSize * averageSize }
                .collect(Collectors.toList())
    }
}
