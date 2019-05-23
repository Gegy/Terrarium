package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverId;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.earth.server.world.pipeline.EarthDataKeys;
import net.gegy1000.earth.server.world.pipeline.data.AreaData;
import net.gegy1000.earth.server.world.pipeline.data.ClimateSampler;
import net.gegy1000.earth.server.world.pipeline.data.HeightNoiseTransformOp;
import net.gegy1000.earth.server.world.pipeline.data.HeightTransformOp;
import net.gegy1000.earth.server.world.pipeline.data.PolygonData;
import net.gegy1000.earth.server.world.pipeline.data.PolygonSampler;
import net.gegy1000.earth.server.world.pipeline.data.PolygonToAreaOp;
import net.gegy1000.earth.server.world.pipeline.data.ProduceCoverOp;
import net.gegy1000.earth.server.world.pipeline.data.ProduceLandformsOp;
import net.gegy1000.earth.server.world.pipeline.data.ProduceSoilOp;
import net.gegy1000.earth.server.world.pipeline.data.RasterizeAreaOp;
import net.gegy1000.earth.server.world.pipeline.data.TransformSlopeNoiseOp;
import net.gegy1000.earth.server.world.pipeline.data.WaterOps;
import net.gegy1000.earth.server.world.pipeline.source.LandCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.OceanPolygonSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataGenerator;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.ProduceSlopeOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.RasterSourceSampler;
import net.gegy1000.terrarium.server.world.pipeline.data.op.VoronoiScaleOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthDataInitializer implements TerrariumDataInitializer {
    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public ColumnDataGenerator buildDataGenerator() {
        int heightOrigin = this.ctx.settings.getInteger(HEIGHT_ORIGIN);
        InterpolationScaleOp heightScaleOp = this.selectScaleOp(this.ctx.settings);

        SrtmHeightSource heightSource = new SrtmHeightSource(this.ctx.srtmRaster, "srtm_heights");

        DataOp<ShortRaster> heightSampler = RasterSourceSampler.sampleShort(heightSource);
        DataOp<ShortRaster> heights = heightScaleOp.scaleShortsFrom(heightSampler, this.ctx.srtmRaster);

        DataOp<UnsignedByteRaster> slope = ProduceSlopeOp.produce(heightSampler);
        slope = InterpolationScaleOp.LINEAR.scaleFrom(slope, this.ctx.srtmRaster, UnsignedByteRaster::create);
        slope = new TransformSlopeNoiseOp(0.5).apply(slope);

        LandCoverSource landCoverSource = new LandCoverSource(this.ctx.landcoverRaster, "landcover");
        DataOp<EnumRaster<CoverId>> coverId = RasterSourceSampler.sampleEnum(landCoverSource, CoverId.NO_DATA);
        coverId = VoronoiScaleOp.scaleFrom(coverId, this.ctx.landcoverRaster, view -> EnumRaster.create(CoverId.NO_DATA, view));

        OceanPolygonSource oceanPolygonSource = new OceanPolygonSource(this.ctx.lngLatCoordinates, "ocean");

        DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(oceanPolygonSource, this.ctx.lngLatCoordinates);
        DataOp<AreaData> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCoordinates);
        DataOp<BitRaster> oceanMask = RasterizeAreaOp.apply(oceanArea);

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(heights, coverId);
        landforms = WaterOps.applyWaterMask(landforms, oceanMask);

        DataOp<EnumRaster<Cover>> cover = ProduceCoverOp.produce(coverId);

        DataOp<ObjRaster<SoilConfig>> soil = ProduceSoilOp.produce(coverId);

        heights = new HeightNoiseTransformOp(2, 0.04, this.ctx.settings.getDouble(NOISE_SCALE))
                .apply(heights, landforms);

        heights = new HeightTransformOp(this.ctx.settings.getDouble(HEIGHT_SCALE) * this.ctx.worldScale, heightOrigin)
                .apply(heights);

        int seaLevel = heightOrigin + 1;
        int seaDepth = this.ctx.settings.getInteger(SEA_DEPTH);

        DataOp<ShortRaster> waterLevel = WaterOps.produceWaterLevel(landforms, seaLevel);

        cover = WaterOps.applyToCover(cover, landforms);
        heights = WaterOps.applyToHeight(heights, landforms, waterLevel, seaDepth);

        ClimateSampler climateSampler = new ClimateSampler(TerrariumEarth.getClimateDataset());

        DataOp<ShortRaster> annualRainfall = climateSampler.annualRainfall();
        annualRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(annualRainfall, this.ctx.climateRaster);

        DataOp<FloatRaster> averageTemperature = climateSampler.averageTemperature();
        averageTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(averageTemperature, this.ctx.climateRaster);

        return ColumnDataGenerator.builder()
                .with(EarthDataKeys.HEIGHT, heights)
                .with(EarthDataKeys.SLOPE, slope)
                .with(EarthDataKeys.COVER, cover)
                .with(EarthDataKeys.LANDFORM, landforms)
                .with(EarthDataKeys.WATER_LEVEL, waterLevel)
                .with(EarthDataKeys.AVERAGE_TEMPERATURE, averageTemperature)
                .with(EarthDataKeys.ANNUAL_RAINFALL, annualRainfall)
                .with(EarthDataKeys.SOIL, soil)
                .build();
    }

    private InterpolationScaleOp selectScaleOp(GenerationSettings properties) {
        double scale = 1.0 / properties.getDouble(WORLD_SCALE);
        if (scale >= 45.0) {
            return InterpolationScaleOp.LINEAR;
        } else if (scale >= 20.0) {
            return InterpolationScaleOp.COSINE;
        } else {
            return InterpolationScaleOp.CUBIC;
        }
    }
}
