package futures.future;

import futures.Waker;

import java.util.HashMap;
import java.util.Map;

final class JoinAllMap<K, V> implements Future<Map<K, V>> {
    private final Map<K, Future<V>> futures;
    private final Map<K, V> result;

    JoinAllMap(Map<K, Future<V>> futures) {
        this.futures = futures;
        this.result = new HashMap<>(futures.size());
    }

    @Override
    public Map<K, V> poll(Waker waker) {
        for (Map.Entry<K, Future<V>> entry : this.futures.entrySet()) {
            if (this.result.containsKey(entry.getKey())) {
                continue;
            }

            V poll = entry.getValue().poll(waker);
            if (poll != null) {
                this.result.put(entry.getKey(), poll);
            }
        }

        if (this.result.size() >= this.futures.size()) {
            return this.result;
        } else {
            return null;
        }
    }
}
