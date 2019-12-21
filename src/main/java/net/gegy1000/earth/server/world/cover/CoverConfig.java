package net.gegy1000.earth.server.world.cover;

import net.gegy1000.earth.server.world.cover.carver.CoverCarver;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecorator;
import net.gegy1000.earth.server.world.cover.decorator.CoverDecoratorType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CoverConfig {
    private final Map<CoverDecoratorType<?>, CoverDecorator> decorators = new LinkedHashMap<>();
    private final Collection<CoverCarver> carvers = new ArrayList<>();

    public <T extends CoverDecorator> void addDecorator(CoverDecoratorType<T> type, T decorator) {
        this.decorators.put(type, decorator);
    }

    @SuppressWarnings("unchecked")
    public <T extends CoverDecorator> void configureDecorator(CoverDecoratorType<T> type, Consumer<T> configurator) {
        CoverDecorator decorator = this.decorators.get(type);
        if (decorator != null) {
            configurator.accept((T) decorator);
        }
    }

    public void addCarver(CoverCarver carver) {
        this.carvers.add(carver);
    }

    public Stream<CoverCarver> carvers() {
        return this.carvers.stream();
    }

    public Stream<CoverDecorator> decorators() {
        return this.decorators.values().stream();
    }
}
