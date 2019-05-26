package concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduleExecutorServiceTest {

    public void beepForAnHour() {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable beeper = () -> System.out.println("beep");
        final ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 10, 10, TimeUnit.SECONDS);
        scheduler.schedule(() -> beeperHandle.cancel(true), 60 * 60, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        new ScheduleExecutorServiceTest().beepForAnHour();
    }
}
