package trip.planner.osm.api

import org.grails.core.util.StopWatch

import java.util.logging.Logger

enum LoggingHelper {
    LOGGING_HELPER;

    private static final LOGGER = Logger.getLogger(LoggingHelper.class.getName());
    private StopWatch watch = new StopWatch();

    static def infoLog(def inf) {
        LOGGER.info(inf)
    }

    def startTimer() {
        watch.start()
    }

    def logTime(String className) {
        watch.stop()
        LOGGER.info "$className needs: $watch.totalTimeSeconds s"
    }
}
