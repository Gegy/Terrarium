package dev.gegy.terrarium.backend.projection;

import com.mojang.serialization.Codec;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.projection.cylindrical.Equirectangular;
import dev.gegy.terrarium.backend.projection.cylindrical.Mercator;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.IntLikeRaster;
import dev.gegy.terrarium.backend.util.Util;

import java.util.concurrent.Executor;

public interface Projection {
    Codec<Projection> CODEC = Type.CODEC.dispatch(Projection::type, type -> type.codec);

    Type type();

    float idealMetersPerBlock();

    double blockX(double lat, double lon);

    double blockZ(double lat, double lon);

    double lat(double blockX, double blockZ);

    double lon(double blockX, double blockZ);

    <V extends IntLikeRaster> GeoLayer<V> createInterpolatedLayer(LeveledRasterSampler<V> leveledSampler, Executor executor);

    <E extends Enum<E>, V extends EnumRaster<E>> GeoLayer<V> createVoronoiLayer(LeveledRasterSampler<V> leveledSampler, Executor executor);

    enum Type {
        EQUIRECTANGULAR("equirectangular", Equirectangular.CODEC),
        MERCATOR("mercator", Mercator.CODEC),
        ;

        public static final Codec<Type> CODEC = Util.stringLookupCodec(values(), type -> type.key);

        private final String key;
        private final Codec<? extends Projection> codec;

        Type(final String key, final Codec<? extends Projection> codec) {
            this.key = key;
            this.codec = codec;
        }
    }
}
