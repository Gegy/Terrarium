package dev.gegy.terrarium.backend.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Helper to request asynchronous requests, where no new requests will be submitted until the last one finishes executing.
 * Any requests submitted while still waiting for another will be skipped.
 * <p>
 * A delay can additionally be specified: if set, requests will never be processed until after at least this delay has
 * passed since the latest request was submitted.
 */
public class SuggestionLimiter {
    private final Runnable idleListener;
    private final Duration delay;

    private CompletableFuture<?> pendingFuture = CompletableFuture.completedFuture(null);
    private final Queue<Request<?>> requests = new LinkedBlockingQueue<>();

    private volatile Instant resumeAt = Instant.EPOCH;

    public SuggestionLimiter(final Duration delay, final Runnable idleListener) {
        this.delay = delay;
        this.idleListener = idleListener;
    }

    public <T> CompletableFuture<Optional<T>> submit(final Supplier<CompletableFuture<T>> scheduler) {
        final Request<T> request = new Request<>(scheduler);
        requests.add(request);
        resumeAt = Instant.now().plus(delay);
        if (pendingFuture.isDone()) {
            pendingFuture = scheduleNextWithDelay();
        }
        return request.listener;
    }

    private CompletableFuture<Void> scheduleNextWithDelay() {
        if (requests.isEmpty()) {
            signalIdle();
            return CompletableFuture.completedFuture(null);
        }
        final long resumeAfter = Duration.between(Instant.now(), resumeAt).toMillis();
        if (resumeAfter <= 1) {
            return scheduleNext();
        }
        return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor(resumeAfter, TimeUnit.MILLISECONDS))
                .thenCompose(ignored -> {
                    // Another request has come in and pushed our resume time further, so continue to wait
                    if (resumeAt.isAfter(Instant.now())) {
                        return scheduleNextWithDelay();
                    }
                    return scheduleNext();
                });
    }

    private CompletableFuture<Void> scheduleNext() {
        Request<?> lastRequest = requests.poll();
        if (lastRequest == null) {
            signalIdle();
            return CompletableFuture.completedFuture(null);
        }
        // Only take the latest request, and signal that any earlier ones have been skipped
        while (!requests.isEmpty()) {
            final Request<?> request = requests.poll();
            lastRequest.skip();
            lastRequest = request;
        }
        return lastRequest.schedule().thenCompose(ignored -> scheduleNextWithDelay());
    }

    private void signalIdle() {
        idleListener.run();
    }

    private static class Request<T> {
        private final Supplier<CompletableFuture<T>> scheduler;
        private final CompletableFuture<Optional<T>> listener = new CompletableFuture<>();

        private Request(final Supplier<CompletableFuture<T>> scheduler) {
            this.scheduler = scheduler;
        }

        public void skip() {
            listener.complete(Optional.empty());
        }

        public CompletableFuture<?> schedule() {
            if (listener.isCancelled()) {
                return CompletableFuture.completedFuture(null);
            }
            final CompletableFuture<T> future = scheduler.get();
            return future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    listener.completeExceptionally(throwable);
                } else {
                    listener.complete(Optional.of(result));
                }
            });
        }
    }
}
