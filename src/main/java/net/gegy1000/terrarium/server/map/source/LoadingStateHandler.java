package net.gegy1000.terrarium.server.map.source;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LoadingStateHandler {
    private static final long STATE_LIFETIME = 2000;

    private static final List<StateEntry> STATE_BUFFER = new LinkedList<>();

    private static final Object LOCK = new Object();

    public static void putState(LoadingState state) {
        StateEntry entry = LoadingStateHandler.makeState(state);
        LoadingStateHandler.breakState(entry);
    }

    public static StateEntry makeState(LoadingState state) {
        StateEntry entry = new StateEntry(state);
        synchronized (LOCK) {
            STATE_BUFFER.add(entry);
        }
        return entry;
    }

    public static void breakState(StateEntry entry) {
        entry.completed = true;
        entry.completedTime = System.currentTimeMillis();
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

    public static class StateEntry {
        private final LoadingState state;

        private boolean completed;
        private long completedTime;

        private StateEntry(LoadingState state) {
            this.state = state;
        }

        public boolean hasExpired() {
            return this.completed && System.currentTimeMillis() - this.completedTime > STATE_LIFETIME;
        }
    }
}
