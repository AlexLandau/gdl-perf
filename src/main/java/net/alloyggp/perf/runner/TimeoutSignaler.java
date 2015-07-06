package net.alloyggp.perf.runner;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Queues;

public class TimeoutSignaler {
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Queue<Runnable> timeoutActions = Queues.synchronizedQueue(Queues.newArrayDeque());

    public void onTimeout(Runnable runnable) {
        timeoutActions.add(runnable);
        try {
            boolean alreadyTimedOut = latch.await(0, TimeUnit.SECONDS);
            if (alreadyTimedOut) {
                runAllTimeoutActions();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void onTimeoutShutdownNow(ExecutorService executor) {
        onTimeout(() -> {
            executor.shutdownNow();
        });
    }

    public void signalTimeout() {
        latch.countDown();
        runAllTimeoutActions();
    }

    private void runAllTimeoutActions() {
        while (true) {
            Runnable timeoutAction = timeoutActions.poll();
            if (timeoutAction == null) {
                return;
            }
            timeoutAction.run();
        }
    }

    public void onTimeoutDestroyForcibly(Process process) {
        onTimeout(() -> {
            process.destroyForcibly();
        });
    }

}
