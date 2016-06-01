package trip.planner.osm.api

import org.grails.core.util.StopWatch

import java.util.logging.Logger

enum LoggingHelper {
    LOGGING_HELPER;

    private static final LOGGER = Logger.getLogger(LoggingHelper.class.getName());
    private StopWatch watch = new StopWatch();
    private List<StopWatch> watches = new ArrayList<>()

    static def infoLog(def inf) {
        LOGGER.info(inf)
    }

    /**
     * create a new timer
     * @return the index of the timer
     */
    int newTimer() {
        StopWatch tmpWatch = new StopWatch()
        watches.add(tmpWatch)
        watches.indexOf(tmpWatch)
    }

    /**
     * this method uses the standard watch with state
     */
    def startTimer() {
        watch.start()
    }

    def startTimer(int index) {
        StopWatch tmpWatch = watches.get(index)
        tmpWatch.start()
    }

    /**
     * this method uses the standard watch with its state
     */
    def logTime(String className) {
        watch.stop()
        LOGGER.info "$className needs: $watch.lastTaskTimeMillis ms"
    }

    def logTime(String className, int index) {
        StopWatch tmpWatch = watches.get(index)
        tmpWatch.stop()
        LOGGER.info "$className needs: $tmpWatch.lastTaskTimeMillis ms"
    }
}
