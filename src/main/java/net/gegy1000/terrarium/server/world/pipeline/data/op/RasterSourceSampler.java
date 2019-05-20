package net.gegy1000.terrarium.server.world.pipeline.data.op;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.Raster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTileEntry;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public final class RasterSourceSampler {
    public static DataOp<ShortRaster> sampleShort(TiledDataSource<ShortRaster> source) {
        return sample(source, ShortRaster::create);
    }

    public static DataOp<ByteRaster> sampleByte(TiledDataSource<ByteRaster> source) {
        return sample(source, ByteRaster::create);
    }

    public static <T> DataOp<ObjRaster<T>> sampleObj(TiledDataSource<ObjRaster<T>> source, T value) {
        return sample(source, view -> ObjRaster.create(value, view));
    }

    public static <T extends Enum<T>> DataOp<EnumRaster<T>> sampleEnum(TiledDataSource<EnumRaster<T>> source, T value) {
        return sample(source, view -> EnumRaster.create(value, view));
    }

    public static <T extends Raster<?>> DataOp<T> sample(TiledDataSource<T> source, Function<DataView, T> function) {
        Coordinate tileSize = source.getTileSize();
        int tileWidth = MathHelper.floor(tileSize.getX());
        int tileHeight = MathHelper.floor(tileSize.getZ());

        return DataOp.of((engine, view) -> {
            DataSourceHandler sourceHandler = engine.getSourceHandler();

            int minTileX = Math.floorDiv(view.getX(), tileWidth);
            int maxTileX = Math.floorDiv((view.getX() + view.getWidth()), tileWidth);
            int minTileY = Math.floorDiv(view.getY(), tileHeight);
            int maxTileY = Math.floorDiv((view.getY() + view.getHeight()), tileHeight);

            DataTilePos minTile = new DataTilePos(minTileX, minTileY);
            DataTilePos maxTile = new DataTilePos(maxTileX, maxTileY);

            return sourceHandler.getTiles(source, minTile, maxTile).thenApply(tiles -> {
                T result = function.apply(view);
                for (DataTileEntry<T> tileEntry : tiles) {
                    sampleFromTile(tileEntry.getData(), tileEntry.getPos(), result, view);
                }
                return result;
            });
        });
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private static <T extends Raster<?>> void sampleFromTile(T sourceTile, DataTilePos pos, T result, DataView view) {
        Object sourceRaw = sourceTile.getData();
        Object resultRaw = result.getData();

        int minTilePosX = pos.getTileX() * sourceTile.getWidth();
        int minTilePosY = pos.getTileZ() * sourceTile.getHeight();

        int minSampleX = Math.max(0, view.getX() - minTilePosX);
        int minSampleY = Math.max(0, view.getY() - minTilePosY);
        int maxSampleX = Math.min(sourceTile.getWidth(), (view.getX() + view.getWidth()) - minTilePosX);
        int maxSampleY = Math.min(sourceTile.getHeight(), (view.getY() + view.getHeight()) - minTilePosY);

        for (int localY = minSampleY; localY < maxSampleY; localY++) {
            int resultY = (localY + minTilePosY) - view.getY();

            int localX = minSampleX;
            int resultX = (localX + minTilePosX) - view.getX();

            int sourceIndex = localX + localY * sourceTile.getWidth();
            int resultIndex = resultX + resultY * result.getWidth();

            System.arraycopy(sourceRaw, sourceIndex, resultRaw, resultIndex, maxSampleX - minSampleX);
        }
    }
}
