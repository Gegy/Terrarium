package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.gegy1000.terrarium.server.world.data.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.data.source.DataTileEntry;
import net.gegy1000.terrarium.server.world.data.source.DataTilePos;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public final class RasterSourceSampler {
    public static DataOp<ShortRaster> sampleShort(TiledDataSource<ShortRaster> source) {
        return sample(source, ShortRaster::create);
    }

    public static DataOp<ByteRaster> sampleByte(TiledDataSource<ByteRaster> source) {
        return sample(source, ByteRaster::create);
    }

    public static DataOp<UByteRaster> sampleUnsignedByte(TiledDataSource<UByteRaster> source) {
        return sample(source, UByteRaster::create);
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

        return DataOp.of(view -> {
            int minTileX = Math.floorDiv(view.getX(), tileWidth);
            int maxTileX = Math.floorDiv((view.getX() + view.getWidth()), tileWidth);
            int minTileY = Math.floorDiv(view.getY(), tileHeight);
            int maxTileY = Math.floorDiv((view.getY() + view.getHeight()), tileHeight);

            DataTilePos minTile = new DataTilePos(minTileX, minTileY);
            DataTilePos maxTile = new DataTilePos(maxTileX, maxTileY);

            return DataSourceHandler.INSTANCE.getTiles(source, minTile, maxTile).thenApply(tiles -> {
                T resultRaster = function.apply(view);
                for (DataTileEntry<T> tileEntry : tiles) {
                    DataTilePos tilePos = tileEntry.getPos();
                    DataView sourceView = DataView.rect(
                            tilePos.getX() * tileWidth, tilePos.getZ() * tileHeight,
                            tileWidth, tileHeight
                    );

                    T sourceRaster = tileEntry.getData();
                    Raster.rasterCopy(sourceRaster, sourceView, resultRaster, view);
                }

                return resultRaster;
            });
        });
    }
}
