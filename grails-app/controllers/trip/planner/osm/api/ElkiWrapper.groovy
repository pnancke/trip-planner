package trip.planner.osm.api

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans
import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.Clustering
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.database.Database
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection
import de.lmu.ifi.dbs.elki.distance.distancefunction.geo.LatLngDistanceFunction
import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration
import de.lmu.ifi.dbs.elki.math.geodesy.SphericalVincentyEarthModel
import de.lmu.ifi.dbs.elki.math.random.RandomFactory
import trip.planner.osm.model.Node
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import trip.planner.osm.model.Node
import trip.planner.osm.model.PointCluster

class ElkiWrapper {

    private static final Log log = LogFactory.getLog(ElkiWrapper.class)

    static Pair<Database, List<Cluster<KMeansModel>>> extractClusters(List<Point> points,
                                                                      int kMeansPartitions,
                                                                      int maxKMeansIterations) {
        if (nodes.isEmpty()) {
            log.info "empty node-list are given"
        }
        LoggingConfiguration.setStatistics()
        double[][] data = convert(points)
        StaticArrayDatabase db = createDatabase(data)
        Clustering<KMeansModel> c = createKMeans(kMeansPartitions, maxKMeansIterations).run(db)
        List<Cluster<KMeansModel>> clusters = c.getAllClusters()
        def pair = new Pair<Database, List<Cluster<KMeansModel>>>(db, clusters)
        pair
    }

    public static List<PointCluster> filterOutliers(List<PointCluster> allClusters,
                                                    int kMeansPartitions,
                                                    double minMeanPercentageClusterSize) {

        int allPointsCount = 0
        allClusters.each { allPointsCount += it.size() }
        int averageSize = allPointsCount / kMeansPartitions
        List<PointCluster> filteredClusters = new ArrayList<>()
        allClusters.each {
            if (it.size() > minMeanPercentageClusterSize * averageSize) {
                filteredClusters.add(it)
            }
        }
        filteredClusters
    }

    private static KMeansLloyd<NumberVector> createKMeans(int kMeansPartitions, int maxKMeansIterations) {
        LatLngDistanceFunction dist = new LatLngDistanceFunction(SphericalVincentyEarthModel.STATIC)
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT)
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, kMeansPartitions, maxKMeansIterations, init)
        km
    }

    private static StaticArrayDatabase createDatabase(double[][] data) {
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data)
        Database db = new StaticArrayDatabase(dbc, null)
        db.initialize();
        db
    }

    private static double[][] convert(List<Point> points) {
        double[][] data = new double[points.size()][2]
        for (int i = 0; i < data.length; i++) {
            data[i][0] = points.get(i).getLat()
            data[i][1] = points.get(i).getLon()
        }
        data
    }
}
