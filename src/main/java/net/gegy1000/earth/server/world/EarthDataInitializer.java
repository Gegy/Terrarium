package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.data.AreaData;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.earth.server.world.data.op.ClimateSampler;
import net.gegy1000.earth.server.world.data.op.OffsetValueOp;
import net.gegy1000.earth.server.world.data.op.PolygonSampler;
import net.gegy1000.earth.server.world.data.op.PolygonToAreaOp;
import net.gegy1000.earth.server.world.data.op.ProduceCoverOp;
import net.gegy1000.earth.server.world.data.op.ProduceLandformsOp;
import net.gegy1000.earth.server.world.data.op.RasterizeAreaOp;
import net.gegy1000.earth.server.world.data.op.ScaleValueOp;
import net.gegy1000.earth.server.world.data.op.WaterOps;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.OceanPolygonSource;
import net.gegy1000.earth.server.world.data.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.data.ColumnDataGenerator;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.data.op.RasterSourceSampler;
import net.gegy1000.terrarium.server.world.data.op.SlopeOp;
import net.gegy1000.terrarium.server.world.data.op.VoronoiScaleOp;
import net.gegy1000.terrarium.server.world.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthDataInitializer implements TerrariumDataInitializer {
    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public ColumnDataGenerator buildDataGenerator() {
        int heightOrigin = this.ctx.settings.getInteger(HEIGHT_OFFSET);

        InterpolationScaleOp heightScaleOp = this.selectSrtmScaleOp(this.ctx.settings);

        SrtmHeightSource heightSource = new SrtmHeightSource(this.ctx.srtmRaster);

        DataOp<ShortRaster> heightSampler = RasterSourceSampler.sampleShort(heightSource);
        DataOp<ShortRaster> heights = heightScaleOp.scaleShortsFrom(heightSampler, this.ctx.srtmRaster).cached(ShortRaster::copy);

        DataOp<UByteRaster> slope = SlopeOp.from(heights, (float) this.ctx.worldScale);

        LandCoverSource landCoverSource = new LandCoverSource(this.ctx.landcoverRaster);
        DataOp<UByteRaster> coverId = RasterSourceSampler.sampleUnsignedByte(landCoverSource);
        coverId = VoronoiScaleOp.scaleUBytesFrom(coverId, this.ctx.landcoverRaster, UByteRaster::create).cached(UByteRaster::copy);

        OceanPolygonSource oceanPolygonSource = new OceanPolygonSource(this.ctx.lngLatCoordinates);

        DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(oceanPolygonSource, this.ctx.lngLatCoordinates);
        DataOp<AreaData> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCoordinates);
        DataOp<BitRaster> oceanMask = RasterizeAreaOp.apply(oceanArea);

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(heights, coverId);
        landforms = WaterOps.applyWaterMask(landforms, oceanMask).cached(EnumRaster::copy);

        DataOp<EnumRaster<Cover>> cover = ProduceCoverOp.produce(coverId);

        heights = new ScaleValueOp(this.ctx.settings.getDouble(HEIGHT_SCALE) * this.ctx.worldScale).applyShort(heights);
        heights = new OffsetValueOp(heightOrigin).apply(heights);

        int seaLevel = heightOrigin + 1;
        int seaDepth = this.ctx.settings.getInteger(SEA_DEPTH);

        DataOp<ShortRaster> waterLevel = WaterOps.produceWaterLevel(landforms, seaLevel).cached(ShortRaster::copy);

        cover = WaterOps.applyToCover(cover, landforms);
        heights = WaterOps.applyToHeight(heights, landforms, waterLevel, seaDepth);

        Season season = this.ctx.settings.get(SEASON);
        ClimateSampler climateSampler = new ClimateSampler(season.getClimateRaster());

        DataOp<ShortRaster> monthlyRainfall = climateSampler.monthlyRainfall();
        monthlyRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(monthlyRainfall, this.ctx.climateRaster);

        DataOp<FloatRaster> averageTemperature = climateSampler.averageTemperature();
        averageTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(averageTemperature, this.ctx.climateRaster);

        return ColumnDataGenerator.builder()
                .with(EarthDataKeys.HEIGHT, heights)
                .with(EarthDataKeys.SLOPE, slope)
                .with(EarthDataKeys.COVER, cover)
                .with(EarthDataKeys.LANDFORM, landforms)
                .with(EarthDataKeys.WATER_LEVEL, waterLevel)
                .with(EarthDataKeys.AVERAGE_TEMPERATURE, averageTemperature)
                .with(EarthDataKeys.MONTHLY_RAINFALL, monthlyRainfall)
                .build();
    }

    private InterpolationScaleOp selectSrtmScaleOp(GenerationSettings properties) {
        double scale = properties.getDouble(WORLD_SCALE);
        if (scale >= 90.0) {
            return InterpolationScaleOp.NEAREST;
        } else if (scale >= 45.0) {
            return InterpolationScaleOp.LINEAR;
        } else if (scale >= 25.0) {
            return InterpolationScaleOp.COSINE;
        } else {
            return InterpolationScaleOp.CUBIC;
        }
    }
}
