package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.MergableTile;

import java.util.Collection;

public class MergeDataLayer<T extends MergableTile<T>> implements DataLayer<T> {
    private final DataLayer<T>[] inputs;

    @SuppressWarnings("unchecked")
    public MergeDataLayer(Collection<DataLayer<T>> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge from no inputs!");
        }
        this.inputs = inputs.toArray(new DataLayer[0]);
    }

    @Override
    public T apply(LayerContext context, DataView view) {
        T result = context.apply(this.inputs[0], view);
        for (int i = 1; i < this.inputs.length; i++) {
            result = result.merge(context.apply(this.inputs[i], view));
        }
        return result;
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        Collection<DataTileKey<?>> requiredData = this.inputs[0].getRequiredData(context, view);
        for (int i = 1; i < this.inputs.length; i++) {
            requiredData.addAll(this.inputs[i].getRequiredData(context, view));
        }
        return requiredData;
    }
}
