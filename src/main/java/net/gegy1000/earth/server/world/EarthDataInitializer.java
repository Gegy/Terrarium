package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.pipeline.EarthDataKeys;
import net.gegy1000.earth.server.world.pipeline.data.ClimateSampler;
import net.gegy1000.earth.server.world.pipeline.data.HeightNoiseTransformer;
import net.gegy1000.earth.server.world.pipeline.data.HeightTransformer;
import net.gegy1000.earth.server.world.pipeline.data.OsmSampler;
import net.gegy1000.earth.server.world.pipeline.data.SlopeNoiseTransformer;
import net.gegy1000.earth.server.world.pipeline.data.SoilProducer;
import net.gegy1000.earth.server.world.pipeline.data.WaterProducer;
import net.gegy1000.earth.server.world.pipeline.source.LandCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.earth.server.world.soil.SoilConfig;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataGenerator;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.DataMergeOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.RasterSourceSampler;
import net.gegy1000.terrarium.server.world.pipeline.data.op.ProduceSlopeOp;
import net.gegy1000.terrarium.server.world.pipeline.data.op.VoronoiScaleOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

        LandCoverSource landCoverSource = new LandCoverSource(this.ctx.landcoverRaster, "landcover");
        DataOp<ObjRaster<Cover>> coverClassification = RasterSourceSampler.sampleObj(landCoverSource, Cover.NO_DATA);
        coverClassification = VoronoiScaleOp.scaleFrom(coverClassification, this.ctx.landcoverRaster, view -> ObjRaster.create(Cover.NO_DATA, view));

        DataOp<ObjRaster<SoilConfig>> soil = SoilProducer.produce(coverClassification);

        DataOp<OsmData> osm = this.createOsmProducer();

        DataOp<ShortRaster> waterBank = WaterProducer.produceBanks(heights, coverClassification);
//        waterBank = OsmWaterProcessor.mergeOsmCoastlines(waterBank, osm, this.ctx.earthCoordinates); TODO

//            waterBankLayer = new OsmWaterBodyLayer(waterBankLayer, osmProducer, this.context.earthCoordinates);

        DataOp<WaterRaster> water = WaterProducer.produceWater(waterBank);

        heights = new HeightNoiseTransformer(2, 0.04, this.ctx.settings.getDouble(NOISE_SCALE))
                .apply(heights, water);

        heights = new HeightTransformer(this.ctx.settings.getDouble(HEIGHT_SCALE) * this.ctx.worldScale, heightOrigin)
                .apply(heights);

        slope = new SlopeNoiseTransformer(0.5).apply(slope);

        ClimateSampler climateSampler = new ClimateSampler(TerrariumEarth.getClimateDataset());

        DataOp<ShortRaster> annualRainfall = climateSampler.annualRainfall();
        annualRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(annualRainfall, this.ctx.climateRaster);

        DataOp<FloatRaster> averageTemperature = climateSampler.averageTemperature();
        averageTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(averageTemperature, this.ctx.climateRaster);

        return ColumnDataGenerator.builder()
                .with(EarthDataKeys.HEIGHT, heights)
                .with(EarthDataKeys.SLOPE, slope)
                .with(EarthDataKeys.COVER, coverClassification)
                .with(EarthDataKeys.OSM, osm)
                .with(EarthDataKeys.WATER, water)
                .with(EarthDataKeys.AVERAGE_TEMPERATURE, averageTemperature)
                .with(EarthDataKeys.ANNUAL_RAINFALL, annualRainfall)
                .with(EarthDataKeys.SOIL, soil)
                .build();
    }

    private DataOp<OsmData> createOsmProducer() {
        List<OverpassSource> sources = new ArrayList<>();

        sources.add(new OverpassSource(
                this.ctx.earthCoordinates,
                0.3,
                "osm/coastline",
                new ResourceLocation(TerrariumEarth.MODID, "query/coastline_overpass_query.oql"),
                1
        ));
            /*sources.add(new OverpassSource(
                    this.context.earthCoordinates,
                    0.3,
                    "osm/outline",
                    new ResourceLocation(TerrariumEarth.MODID, "query/outline_overpass_query.oql"),
                    12
            ));
            sources.add(new OverpassSource(
                    this.context.earthCoordinates,
                    0.15,
                    "osm/natural",
                    new ResourceLocation(TerrariumEarth.MODID, "query/natural_overpass_query.oql"),
                    1
            ));
            sources.add(new OverpassSource(
                    this.context.earthCoordinates,
                    0.1,
                    "osm/general",
                    new ResourceLocation(TerrariumEarth.MODID, "query/general_overpass_query.oql"),
                    6
            ));
            sources.add(new OverpassSource(
                    this.context.earthCoordinates,
                    0.05,
                    "osm/detailed",
                    new ResourceLocation(TerrariumEarth.MODID, "query/detail_overpass_query.oql"),
                    4
            ));*/

        Collection<DataOp<OsmData>> samplers = sources.stream()
                .filter(OverpassSource::shouldSample)
                .map(source -> OsmSampler.sample(source, this.ctx.earthCoordinates))
                .collect(Collectors.toList());

        if (!samplers.isEmpty()) {
            return DataMergeOp.merge(samplers);
        } else {
            return DataOp.completed(new OsmData());
        }
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
