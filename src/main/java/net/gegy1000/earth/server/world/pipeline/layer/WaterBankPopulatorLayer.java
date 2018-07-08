package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public class WaterBankPopulatorLayer implements DataLayerProducer<ShortRasterTile> {
    private final DataLayerProducer<CoverRasterTile> coverLayer;
    private final DataLayerProducer<ShortRasterTile> heightLayer;

    public WaterBankPopulatorLayer(DataLayerProducer<CoverRasterTile> coverLayer, DataLayerProducer<ShortRasterTile> heightLayer) {
        this.coverLayer = coverLayer;
        this.heightLayer = heightLayer;
    }

    @Override
    public void reset() {
        this.coverLayer.reset();
        this.heightLayer.reset();
    }

    @Override
    public ShortRasterTile apply(DataView view) {
        ShortRasterTile tile = new ShortRasterTile(view);
        CoverRasterTile coverTile = this.coverLayer.apply(view);
        ShortRasterTile heightTile = this.heightLayer.apply(view);

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                CoverType cover = coverTile.get(localX, localY);
                if (cover == EarthCoverTypes.WATER) {
                    short height = heightTile.getShort(localX, localY);
                    int bankType = height <= 1 ? OsmWaterLayer.OCEAN : OsmWaterLayer.RIVER;
                    tile.setShort(localX, localY, (short) bankType);
                }
            }
        }

        return tile;
    }
}
