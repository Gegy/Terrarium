package net.gegy1000.earth.server.world.pipeline.data;

import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTileEntry;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.minecraft.util.math.MathHelper;

public final class OsmSampler {
    public static DataOp<OsmData> sample(OverpassSource source, CoordinateState coordState) {
        return DataOp.of((engine, view) -> {
            DataSourceHandler sourceHandler = engine.getSourceHandler();

            DataView bufferView = view.grow(8, 8, 8, 8);

            DataTilePos blockMinTilePos = getTilePos(source, bufferView.getMinCoordinate().to(coordState));
            DataTilePos blockMaxTilePos = getTilePos(source, bufferView.getMaxCoordinate().to(coordState));

            DataTilePos minTilePos = DataTilePos.min(blockMinTilePos, blockMaxTilePos);
            DataTilePos maxTilePos = DataTilePos.max(blockMinTilePos, blockMaxTilePos);

            return sourceHandler.getTiles(source, minTilePos, maxTilePos)
                    .thenApply(tiles -> {
                        OsmData result = new OsmData();
                        for (DataTileEntry<OsmData> entry : tiles) {
                            result = result.merge(entry.getData());
                        }
                        return result;
                    });
        });
    }

    private static DataTilePos getTilePos(OverpassSource source, Coordinate coordinate) {
        Coordinate tileSize = source.getTileSize();
        int tileX = MathHelper.floor(coordinate.getX() / tileSize.getX());
        int tileZ = MathHelper.floor(coordinate.getZ() / tileSize.getZ());
        return new DataTilePos(tileX, tileZ);
    }
}
