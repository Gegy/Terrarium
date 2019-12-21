package net.gegy1000.earth.server.world.cover;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.stream.Stream;

public class CoverMarker implements CoverSelector {
    private final EnumSet<Cover> contained = EnumSet.noneOf(Cover.class);

    protected CoverMarker() {
    }

    public CoverMarker add(Cover... covers) {
        Collections.addAll(this.contained, covers);
        return this;
    }

    public CoverMarker addAll(CoverSelector selector) {
        selector.forEach(this::add);
        return this;
    }

    @Override
    public boolean contains(Cover cover) {
        return this.contained.contains(cover);
    }

    @Override
    public Iterator<Cover> iterator() {
        return this.contained.iterator();
    }

    @Override
    public Stream<Cover> stream() {
        return this.contained.stream();
    }
}
