package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.cover.EarthCoverBiomes;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.world.biome.Biome;

import java.util.Collection;

public class WaterBankPopulatorLayer implements DataLayer<ShortRasterTile> {
    private final DataLayer<BiomeRasterTile> biomeLayer;
    private final DataLayer<ShortRasterTile> heightLayer;

    public WaterBankPopulatorLayer(DataLayer<BiomeRasterTile> biomeLayer, DataLayer<ShortRasterTile> heightLayer) {
        this.biomeLayer = biomeLayer;
        this.heightLayer = heightLayer;
    }

    @Override
    public ShortRasterTile apply(LayerContext context, DataView view) {
        ShortRasterTile tile = new ShortRasterTile(view);
        BiomeRasterTile biomeTile = context.apply(this.biomeLayer, view);
        ShortRasterTile heightTile = context.apply(this.heightLayer, view);

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                Biome biome = biomeTile.get(localX, localY);
                if (biome == EarthCoverBiomes.WATER) {
                    short height = heightTile.getShort(localX, localY);
                    int bankType = height <= 1 ? OsmWaterLayer.OCEAN : OsmWaterLayer.RIVER;
                    tile.setShort(localX, localY, (short) bankType);
                }
            }
        }

        return tile;
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        Collection<DataTileKey<?>> requiredData = this.biomeLayer.getRequiredData(context, view);
        requiredData.addAll(this.heightLayer.getRequiredData(context, view));
        return requiredData;
    }
}
