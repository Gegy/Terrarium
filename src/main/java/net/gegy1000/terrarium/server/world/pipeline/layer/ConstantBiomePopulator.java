package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConstantBiomePopulator implements DataLayer<BiomeRasterTile> {
    private final Biome value;

    public ConstantBiomePopulator(Biome value) {
        this.value = value;
    }

    @Override
    public BiomeRasterTile apply(LayerContext context, DataView view) {
        Biome[] data = new Biome[view.getWidth() * view.getHeight()];
        Arrays.fill(data, this.value);
        return new BiomeRasterTile(data, view.getWidth(), view.getHeight());
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        return Collections.emptyList();
    }
}
