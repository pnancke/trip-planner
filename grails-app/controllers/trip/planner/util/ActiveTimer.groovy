package trip.planner.util

import org.apache.commons.logging.Log
import org.grails.core.util.StopWatch

class ActiveTimer {
    private StopWatch watch

    public ActiveTimer() {
        watch = new StopWatch()
        this.watch.start()
    }

    public long stopAndLog(Log log) {
        watch.stop()
        long millis = watch.lastTaskTimeMillis
        log.info "needs:  $millis ms"
        millis
    }

    public long stopAndLog(Log log, String additionalInfo) {
        watch.stop()
        long millis = watch.lastTaskTimeMillis
        log.info "needs:  $millis ms, for $additionalInfo"
        millis
    }

    public void reset() {
        watch.stop()
        watch.start()
    }
}
