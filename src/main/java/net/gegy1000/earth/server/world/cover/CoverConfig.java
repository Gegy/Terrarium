package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.biome.CoverMarker;
import net.gegy1000.earth.server.world.cover.carver.CoverCarver;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public final class CoverConfig {
    private final EnumSet<CoverMarker> markers = EnumSet.noneOf(CoverMarker.class);
    private final Collection<CoverCarver> carvers = new ArrayList<>();
    private final Collection<CoverDecorator> decorators = new ArrayList<>();

    public void mark(CoverMarker... markers) {
        Collections.addAll(this.markers, markers);
    }

    public void carve(CoverCarver carver) {
        this.carvers.add(carver);
    }

    public void decorate(CoverDecorator decorator) {
        this.decorators.add(decorator);
    }

    public Set<CoverMarker> markers() {
        return this.markers;
    }

    public Stream<CoverCarver> carvers() {
        return this.carvers.stream();
    }

    public Stream<CoverDecorator> decorators() {
        return this.decorators.stream();
    }
}
