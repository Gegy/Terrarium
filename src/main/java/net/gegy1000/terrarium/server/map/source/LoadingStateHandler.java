package net.gegy1000.terrarium.server.map.source;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LoadingStateHandler {
    private static final long STATE_LIFETIME = 1000;

    private static final List<StateEntry> STATE_BUFFER = new LinkedList<>();

    private static final Object LOCK = new Object();

    public static void putState(LoadingState state) {
        synchronized (LOCK) {
            STATE_BUFFER.add(new StateEntry(state, System.currentTimeMillis()));
        }
    }

    public static LoadingState checkState() {
        LoadingStateHandler.removeExpired();
        synchronized (LOCK) {
            Map<LoadingState, Integer> stateCounts = new EnumMap<>(LoadingState.class);
            for (StateEntry entry : STATE_BUFFER) {
                int weight = stateCounts.getOrDefault(entry.state, 0);
                stateCounts.put(entry.state, weight + entry.state.getWeight());
            }
            LoadingState relevantState = null;
            int relevantWeight = 0;
            for (Map.Entry<LoadingState, Integer> entry : stateCounts.entrySet()) {
                int weight = entry.getValue();
                if (weight > relevantWeight) {
                    relevantState = entry.getKey();
                    relevantWeight = weight;
                }
            }
            return relevantState;
        }
    }

    private static void removeExpired() {
        synchronized (LOCK) {
            STATE_BUFFER.removeIf(StateEntry::hasExpired);
        }
    }

    private static class StateEntry {
        private final LoadingState state;
        private final long time;

        private StateEntry(LoadingState state, long time) {
            this.state = state;
            this.time = time;
        }

        public boolean hasExpired() {
            return System.currentTimeMillis() - this.time > STATE_LIFETIME;
        }
    }
}
