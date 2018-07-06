package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.MergableTile;

import java.util.Collection;

public class MergeDataLayer<T extends MergableTile<T>> implements DataLayerProducer<T> {
    private final DataLayerProducer<T>[] inputs;

    @SuppressWarnings("unchecked")
    private MergeDataLayer(Collection<DataLayerProducer<T>> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge from no inputs!");
        }
        this.inputs = inputs.toArray(new DataLayerProducer[0]);
    }

    public static <T extends MergableTile<T>> DataLayerProducer<T> from(Collection<DataLayerProducer<T>> inputs) {
        return new MergeDataLayer<>(inputs);
    }

    @Override
    public void reset() {
        for (DataLayerProducer<T> input : this.inputs) {
            input.reset();
        }
    }

    @Override
    public T apply(DataView view) {
        T result = this.inputs[0].apply(view);
        for (int i = 1; i < this.inputs.length; i++) {
            result = result.merge(this.inputs[i].apply(view));
        }
        return result;
    }
}
