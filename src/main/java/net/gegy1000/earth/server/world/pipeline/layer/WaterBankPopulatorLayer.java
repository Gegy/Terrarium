package net.gegy1000.earth.server.world.pipeline.layer;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.layer.LayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

import java.util.Collection;

public class WaterBankPopulatorLayer implements DataLayer<ShortRasterTile> {
    private final DataLayer<CoverRasterTile> coverLayer;
    private final DataLayer<ShortRasterTile> heightLayer;

    public WaterBankPopulatorLayer(DataLayer<CoverRasterTile> coverLayer, DataLayer<ShortRasterTile> heightLayer) {
        this.coverLayer = coverLayer;
        this.heightLayer = heightLayer;
    }

    @Override
    public ShortRasterTile apply(LayerContext context, DataView view) {
        ShortRasterTile tile = new ShortRasterTile(view);
        CoverRasterTile coverTile = context.apply(this.coverLayer, view);
        ShortRasterTile heightTile = context.apply(this.heightLayer, view);

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

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        Collection<DataTileKey<?>> requiredData = this.coverLayer.getRequiredData(context, view);
        requiredData.addAll(this.heightLayer.getRequiredData(context, view));
        return requiredData;
    }
}
