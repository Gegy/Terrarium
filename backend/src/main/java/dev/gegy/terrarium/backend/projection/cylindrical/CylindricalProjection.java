package dev.gegy.terrarium.backend.projection.cylindrical;

import dev.gegy.terrarium.backend.GeoView;
import dev.gegy.terrarium.backend.layer.GeoLayer;
import dev.gegy.terrarium.backend.layer.LeveledRasterSampler;
import dev.gegy.terrarium.backend.layer.RasterSampler;
import dev.gegy.terrarium.backend.projection.Projection;
import dev.gegy.terrarium.backend.raster.EnumRaster;
import dev.gegy.terrarium.backend.raster.IntLikeRaster;
import dev.gegy.terrarium.backend.raster.Raster;

import java.util.concurrent.Executor;

public interface CylindricalProjection extends Projection {
    double blockX(double lon);

    double blockZ(double lat);

    double lon(double blockX);

    double lat(double blockZ);

    default <V extends Raster> GeoLayer<V> createResamplingLayer(final ResamplerFactory<V> resamplerFactory, final LeveledRasterSampler<V> leveledSampler, final Executor executor) {
        return view -> {
            final double lon0 = lon(view.x0());
            final double lon1 = lon(view.x1() + 0.5);
            final double lat0 = lat(view.z1() + 0.5);
            final double lat1 = lat(view.z0());

            final double blocksPerDegreeX = view.width() / (lon1 - lon0);
            final double blocksPerDegreeY = view.height() / (lat1 - lat0);
            final RasterSampler<V> sampler = leveledSampler.choose(blocksPerDegreeX, blocksPerDegreeY);

            final double x0 = (lon0 + 180.0) / 360.0 * sampler.width();
            final double z0 = (90.0 - lat1) / 180.0 * sampler.height();
            final double x1 = (lon1 + 180.0) / 360.0 * sampler.width();
            final double z1 = (90.0 - lat0) / 180.0 * sampler.height();

            // Big approximation: the scale won't necessarily be the same over the full sampled area, but it's good enough for now
            // TODO: This means we can't sample big areas at once, so it might become relevant in the future
            final double blockToMapScaleX = (x1 - x0) / view.width();
            final double blockToMapScaleZ = (z1 - z0) / view.height();

            final Resampler<? super V> resampler = resamplerFactory.create(Math.min(blockToMapScaleX, blockToMapScaleZ));

            final GeoView mapView = resampler.extend(new GeoView(
                    (int) Math.floor(x0),
                    (int) Math.floor(z0),
                    (int) Math.floor(x1),
                    (int) Math.floor(z1)
            ));
            final float offsetX = (float) (x0 - mapView.x0());
            final float offsetZ = (float) (z0 - mapView.z0());

            return sampler.get(mapView).thenApplyAsync(
                    r -> r.map(source -> {
                        final V target = Raster.type(source).create(view.shape());
                        resampler.resample(source, target, (float) blockToMapScaleX, (float) blockToMapScaleZ, offsetX, offsetZ, mapView.x0(), mapView.z0());
                        return target;
                    }),
                    executor
            );
        };
    }

    @Override
    default double blockX(final double lat, final double lon) {
        return blockX(lon);
    }

    @Override
    default double blockZ(final double lat, final double lon) {
        return blockZ(lat);
    }

    @Override
    default double lat(final double blockX, final double blockZ) {
        return lat(blockZ);
    }

    @Override
    default double lon(final double blockX, final double blockZ) {
        return lon(blockX);
    }

    @Override
    default <V extends IntLikeRaster> GeoLayer<V> createInterpolatedLayer(final LeveledRasterSampler<V> leveledSampler, final Executor executor) {
        return createResamplingLayer(
                InterpolationMode::choose,
                leveledSampler,
                executor
        );
    }

    @Override
    default <E extends Enum<E>, V extends EnumRaster<E>> GeoLayer<V> createVoronoiLayer(final LeveledRasterSampler<V> leveledSampler, final Executor executor) {
        final Voronoi voronoi = new Voronoi(0.45f, 2016969737595986194L);
        return createResamplingLayer(
                scale -> voronoi,
                leveledSampler,
                executor
        );
    }

    interface ResamplerFactory<V extends Raster> {
        Resampler<? super V> create(double scale);
    }
}
