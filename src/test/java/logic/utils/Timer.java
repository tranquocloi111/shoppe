package logic.utils;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.TimeUnit;

public class Timer {

    private static ThreadLocal<StopWatch> swTL = new ThreadLocal<>();

    public static void start() {
        if (swTL.get() == null) {
            swTL.set(new StopWatch());
        }
        swTL.get().start();
    }

    public static long stop(TimeUnit unit) {
        swTL.get().stop();
        long time = swTL.get().getTime(unit);
        swTL.get().reset();
        return time;
    }

    public static long stop() {
        return stop(TimeUnit.MILLISECONDS);
    }
}
