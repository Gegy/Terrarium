package futures.future;

import futures.Waker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

final class JoinAll<T> implements Future<Collection<T>> {
    private final Collection<Future<T>> futures;
    private final Collection<T> result;

    JoinAll(Collection<Future<T>> futures) {
        this.futures = futures;
        this.result = new ArrayList<>(futures.size());
    }

    @Override
    public Collection<T> poll(Waker waker) {
        Iterator<Future<T>> iterator = this.futures.iterator();
        while (iterator.hasNext()) {
            Future<T> future = iterator.next();
            T poll = future.poll(waker);
            if (poll != null) {
                this.result.add(poll);
                iterator.remove();
            }
        }
        if (this.futures.isEmpty()) {
            return this.result;
        } else {
            return null;
        }
    }
}
