package rreactor;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Rreactor {

    protected static Executor eventLoop = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder()
            .namingPattern("rreactor-%d")
            .daemon(true)
            .priority(Thread.MAX_PRIORITY)
            .build());

    protected static LogLevel logLevel = LogLevel.NONE;

    private static boolean isBusy = false;

    private static List<Kimono<?>> executionQueue = new ArrayList<>();

    protected static void registerKimonoForExecution(Kimono<?> kimono) {
        writeLog("registerKimonoForExecution(%s)", kimono.hashCode());
        executionQueue.add(kimono);
        doOnAnyUpdate();
    }

    protected static void executionCompletedForKimono(Kimono<?> kimono) {
        writeLog("executionCompletedForKimono(%s)", kimono.hashCode());
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
            var prefix = String.format("t:%s c:%s \t", Thread.currentThread().getName(), "REACTOR");
            System.out.println(String.format(prefix + pattern, value));
        }
    }

}
