package rreactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rreactor {

    protected static LogLevel logLevel = LogLevel.LOG;

    private static boolean isBusy = false;

    private static List<Kimono<?>> executionQueue = new ArrayList<>();

    protected static void registerKimonoForExecution(Kimono<?> kimono) {
        writeLog("registerKimonoForExecution()");
        executionQueue.add(kimono);
        doOnAnyUpdate();
    }

    protected static void executionCompletedForKimono(Kimono<?> kimono) {
        writeLog("executionCompletedForKimono()");
        isBusy = false;
        doOnAnyUpdate();
    }

    private static void doOnAnyUpdate() {
        writeLog("doOnAnyUpdate() isBusy: %s, QLength: %s", isBusy, executionQueue.size());
        if (isBusy) return;
        if (executionQueue.isEmpty()) return;
        isBusy = true;
        var top = executionQueue.remove(0);
        top.executeInternally();
    }

    private static void writeLog(String pattern, Object... value) {
        if (logLevel == LogLevel.LOG) {
            var prefix = String.format("t:%s c:%s ", Thread.currentThread().getName(), "REACTOR");
            System.out.println(String.format(prefix + pattern, value));
        }
    }

}
