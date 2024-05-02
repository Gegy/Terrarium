package dev.gegy.terrarium.command;

import dev.gegy.terrarium.backend.util.SuggestionLimiter;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class CommandSuggestionLimiter {
    private final Duration delay;
    private final Map<UUID, SuggestionLimiter> limiterByPlayer = new ConcurrentHashMap<>();

    public CommandSuggestionLimiter(final Duration delay) {
        this.delay = delay;
    }

    public <T> CompletableFuture<Optional<T>> submit(final UUID player, final Supplier<CompletableFuture<T>> scheduler) {
        final SuggestionLimiter limiter = limiterByPlayer.computeIfAbsent(player, this::createLimiterFor);
        return limiter.submit(scheduler);
    }

    private SuggestionLimiter createLimiterFor(final UUID player) {
        final Runnable invalidator = () -> limiterByPlayer.remove(player);
        return new SuggestionLimiter(delay, invalidator);
    }
}
