package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.cover.carver.CoverCarver;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecorator;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public final class CoverConfig {
    private final Collection<BiomeDictionary.Type> classifications = new ArrayList<>();
    private final Collection<CoverCarver> carvers = new ArrayList<>();
    private final Collection<CoverDecorator> decorators = new ArrayList<>();

    public void classify(BiomeDictionary.Type... types) {
        Collections.addAll(this.classifications, types);
    }

    public void carve(CoverCarver carver) {
        this.carvers.add(carver);
    }

    public void decorate(CoverDecorator decorator) {
        this.decorators.add(decorator);
    }

    public Stream<BiomeDictionary.Type> classifications() {
        return this.classifications.stream();
    }

    public Stream<CoverCarver> carvers() {
        return this.carvers.stream();
    }

    public Stream<CoverDecorator> decorators() {
        return this.decorators.stream();
    }
}
