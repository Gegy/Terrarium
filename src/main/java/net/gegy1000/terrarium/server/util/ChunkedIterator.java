package net.gegy1000.terrarium.server.util;

import com.google.common.collect.AbstractIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class ChunkedIterator<T> extends AbstractIterator<Collection<T>> {
    private final Iterator<T> iterator;
    private final int chunkSize;

    private final List<T> buffer;

    private ChunkedIterator(Iterator<T> iterator, int chunkSize) {
        this.iterator = iterator;
        this.chunkSize = chunkSize;
        this.buffer = new ArrayList<>(chunkSize);
    }

    public static <T> Iterator<Collection<T>> of(Iterator<T> iterator, int chunkSize) {
        return new ChunkedIterator<>(iterator, chunkSize);
    }

    public static <T> Iterable<Collection<T>> of(Iterable<T> iterable, int chunkSize) {
        return () -> new ChunkedIterator<>(iterable.iterator(), chunkSize);
    }

    @Override
    protected Collection<T> computeNext() {
        this.buffer.clear();

        for (int i = 0; i < this.chunkSize; i++) {
            if (!this.iterator.hasNext()) {
                break;
            }
            this.buffer.add(this.iterator.next());
        }

        if (this.buffer.isEmpty()) {
            return this.endOfData();
        } else {
            return this.buffer;
        }
    }
}
