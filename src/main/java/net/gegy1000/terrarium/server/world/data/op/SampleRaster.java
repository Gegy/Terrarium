package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.gegy1000.terrarium.server.world.data.source.DataTileResult;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Function;

public final class SampleRaster {
    public static DataOp<ShortRaster> sampleShort(TiledDataSource<ShortRaster> source) {
        return sample(source, ShortRaster::create);
    }

    public static DataOp<ByteRaster> sampleByte(TiledDataSource<ByteRaster> source) {
        return sample(source, ByteRaster::create);
    }

    public static DataOp<UByteRaster> sampleUByte(TiledDataSource<UByteRaster> source) {
        return sample(source, UByteRaster::create);
    }

    public static <T> DataOp<ObjRaster<T>> sampleObj(TiledDataSource<ObjRaster<T>> source, T value) {
        return sample(source, view -> ObjRaster.create(value, view));
    }

    public static <T extends Enum<T>> DataOp<EnumRaster<T>> sampleEnum(TiledDataSource<EnumRaster<T>> source, T value) {
        return sample(source, view -> EnumRaster.create(value, view));
    }

    public static <T extends Raster<?>> DataOp<T> sample(TiledDataSource<T> source, Function<DataView, T> function) {
        return DataOp.of(view -> {
            int tileWidth = MathHelper.floor(source.getTileWidth());
            int tileHeight = MathHelper.floor(source.getTileHeight());

            return DataSourceReader.INSTANCE.getTiles(source, view).thenApply(tiles -> {
                T resultRaster = function.apply(view);

                for (DataTileResult<T> tileResult : tiles) {
                    if (!tileResult.data.isPresent()) return Optional.empty();

                    T sourceRaster = tileResult.data.get();
                    Vec2i tilePos = tileResult.pos;

                    DataView sourceView = DataView.rect(
                            tilePos.x * tileWidth, tilePos.y * tileHeight,
                            tileWidth, tileHeight
                    );
                    Raster.rasterCopy(sourceRaster, sourceView, resultRaster, view);
                }

                return Optional.of(resultRaster);
            });
        });
    }
}
