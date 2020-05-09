package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.earth.server.world.data.op.AddNoiseOp;
import net.gegy1000.earth.server.world.data.op.ClimateSampler;
import net.gegy1000.earth.server.world.data.op.PolygonSampler;
import net.gegy1000.earth.server.world.data.op.PolygonToAreaOp;
import net.gegy1000.earth.server.world.data.op.ProduceLandformsOp;
import net.gegy1000.earth.server.world.data.op.RasterizeAreaOp;
import net.gegy1000.earth.server.world.data.op.ResampleZoomRasters;
import net.gegy1000.earth.server.world.data.op.TransformTerrainElevation;
import net.gegy1000.earth.server.world.data.op.WaterOps;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.OceanPolygonSource;
import net.gegy1000.earth.server.world.data.source.SoilSources;
import net.gegy1000.earth.server.world.data.source.StdSource;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.coordinate.CoordReferenced;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.data.op.SampleRaster;
import net.gegy1000.terrarium.server.world.data.op.SlopeOp;
import net.gegy1000.terrarium.server.world.data.op.VoronoiScaleOp;
import net.gegy1000.terrarium.server.world.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.awt.geom.Area;
import java.util.Random;
import java.util.function.Function;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

public final class EarthDataInitializer implements TerrariumDataInitializer {
    public static final Zoomable<StdSource<FloatRaster>> ELEVATION_SOURCE = ElevationSource.source();
    public static final Zoomable<StdSource<EnumRaster<Cover>>> LAND_COVER_SOURCE = LandCoverSource.source();

    public static final Zoomable<StdSource<ShortRaster>> CATION_EXCHANGE_CAPACITY_SOURCE = SoilSources.cationExchangeCapacity();
    public static final Zoomable<StdSource<ShortRaster>> ORGANIC_CARBON_CONTENT_SOURCE = SoilSources.organicCarbonContent();
    public static final Zoomable<StdSource<UByteRaster>> PH_SOURCE = SoilSources.ph();
    public static final Zoomable<StdSource<UByteRaster>> CLAY_CONTENT_SOURCE = SoilSources.clayContent();
    public static final Zoomable<StdSource<UByteRaster>> SILT_CONTENT_SOURCE = SoilSources.siltContent();
    public static final Zoomable<StdSource<UByteRaster>> SAND_CONTENT_SOURCE = SoilSources.sandContent();
    public static final Zoomable<StdSource<EnumRaster<SoilSuborder>>> SOIL_CLASS_SOURCE = SoilSources.soilClass();

    public static final OceanPolygonSource OCEAN_SOURCE = new OceanPolygonSource();

    private static final NoiseGeneratorPerlin TEMPERATURE_NOISE = new NoiseGeneratorPerlin(new Random(12345), 1);

    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    private int selectStandardZoom(double worldScale) {
        double zoom = StdSource.zoomForScale(worldScale);
        return Math.max((int) Math.round(zoom), 0);
    }

    private <T extends NumberRaster<?>> DataOp<T> sampleStdInterpolated(
            double worldScale,
            Zoomable<StdSource<T>> zoomableSource,
            Function<DataView, T> createRaster
    ) {
        int maxZoom = this.selectStandardZoom(worldScale);
        return new ResampleZoomRasters<T>()
                .from(zoomableSource.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.stdRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(source -> SampleRaster.sample(source, createRaster))
                .resample((sample, crs) -> {
                    return InterpolationScaleOp.appropriateForScale(crs.avgScale())
                            .scaleFrom(sample, crs, createRaster);
                })
                .atZoom(maxZoom)
                .create();
    }

    private <E extends Enum<E>> DataOp<EnumRaster<E>> sampleStdEnum(
            double worldScale,
            Zoomable<StdSource<EnumRaster<E>>> zoomableSource,
            E defaultValue
    ) {
        int maxZoom = this.selectStandardZoom(worldScale);
        return new ResampleZoomRasters<EnumRaster<E>>()
                .from(zoomableSource.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.stdRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(source -> SampleRaster.sampleEnum(source, defaultValue))
                .resample((sample, crs) -> VoronoiScaleOp.scaleEnumsFrom(sample, crs, defaultValue))
                .atZoom(maxZoom)
                .create();
    }

    private DataOp<FloatRaster> elevation(double worldScale) {
        return this.sampleStdInterpolated(worldScale, ELEVATION_SOURCE, FloatRaster::create);
    }

    private DataOp<EnumRaster<Cover>> landcover(double worldScale) {
        return this.sampleStdEnum(worldScale, LAND_COVER_SOURCE, Cover.NO);
    }

    private DataOp<ShortRaster> soilShort(double worldScale, Zoomable<StdSource<ShortRaster>> source) {
        return this.sampleStdInterpolated(worldScale, source, ShortRaster::create);
    }

    private DataOp<UByteRaster> soilUByte(double worldScale, Zoomable<StdSource<UByteRaster>> source) {
        return this.sampleStdInterpolated(worldScale, source, UByteRaster::create);
    }

    private DataOp<EnumRaster<SoilSuborder>> soilClass(double worldScale) {
        return this.sampleStdEnum(worldScale, SOIL_CLASS_SOURCE, SoilSuborder.NO);
    }

    private DataOp<BitRaster> oceanMask(double worldScaleMeters) {
        double coastDeviationMeters = 500.0;
        double sampleExpand = coastDeviationMeters / worldScaleMeters;

        DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(OCEAN_SOURCE, this.ctx.lngLatCrs, sampleExpand);
        DataOp<Area> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCrs);
        return RasterizeAreaOp.apply(oceanArea);
    }

    private AddNoiseOp temperatureNoise() {
        return new AddNoiseOp(TEMPERATURE_NOISE, 0.05, 1.5);
    }

    @Override
    public void setup(DataGenerator.Builder builder) {
        double worldScale = this.ctx.settings.getDouble(WORLD_SCALE);
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        int seaLevel = heightOffset + 1;

        DataOp<FloatRaster> elevation = this.elevation(worldScale).cached(FloatRaster::copy);
        DataOp<UByteRaster> slope = SlopeOp.from(elevation, 1.0F / (float) worldScale);

        DataOp<EnumRaster<Cover>> cover = this.landcover(worldScale);

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(elevation, cover);

        double terrestrialHeightScale = this.ctx.settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) / worldScale;
        double oceanicHeightScale = this.ctx.settings.getDouble(OCEANIC_HEIGHT_SCALE) / worldScale;

        DataOp<ShortRaster> terrainHeight = new TransformTerrainElevation(
                terrestrialHeightScale,
                oceanicHeightScale,
                heightOffset
        ).apply(elevation).cached(ShortRaster::copy);

        if (worldScale <= 90.0) {
            DataOp<BitRaster> oceanMask = this.oceanMask(worldScale);
            landforms = WaterOps.applyWaterMask(landforms, oceanMask).cached(EnumRaster::copy);
        }

        DataOp<ShortRaster> waterLevel = WaterOps.produceWaterLevel(terrainHeight, landforms, seaLevel);

        terrainHeight = WaterOps.applyToHeight(terrainHeight, landforms, seaLevel);
        cover = WaterOps.applyToCover(cover, landforms);

        SharedEarthData sharedData = SharedEarthData.instance();
        ClimateSampler climateSampler = new ClimateSampler(sharedData.get(SharedEarthData.CLIMATIC_VARIABLES));

        DataOp<ShortRaster> annualRainfall = climateSampler.annualRainfall();
        annualRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(annualRainfall, this.ctx.climateRasterCrs);

        DataOp<FloatRaster> meanTemperature = climateSampler.meanTemperature();
        meanTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(meanTemperature, this.ctx.climateRasterCrs);
        meanTemperature = this.temperatureNoise().applyFloats(meanTemperature);

        DataOp<FloatRaster> minTemperature = climateSampler.minTemperature();
        minTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(minTemperature, this.ctx.climateRasterCrs);
        minTemperature = this.temperatureNoise().applyFloats(minTemperature);

        DataOp<UByteRaster> cationExchangeCapacity = this.soilShort(worldScale, CATION_EXCHANGE_CAPACITY_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<ShortRaster> organicCarbonContent = this.soilShort(worldScale, ORGANIC_CARBON_CONTENT_SOURCE);
        DataOp<UByteRaster> soilPh = this.soilUByte(worldScale, PH_SOURCE);
        DataOp<UByteRaster> clayContent = this.soilUByte(worldScale, CLAY_CONTENT_SOURCE);
        DataOp<UByteRaster> siltContent = this.soilUByte(worldScale, SILT_CONTENT_SOURCE);
        DataOp<UByteRaster> sandContent = this.soilUByte(worldScale, SAND_CONTENT_SOURCE);

        DataOp<EnumRaster<SoilSuborder>> soilClass = this.soilClass(worldScale);

        builder.put(EarthData.TERRAIN_HEIGHT, terrainHeight);
        builder.put(EarthData.ELEVATION_METERS, elevation);
        builder.put(EarthData.SLOPE, slope);
        builder.put(EarthData.COVER, cover);
        builder.put(EarthData.LANDFORM, landforms);
        builder.put(EarthData.WATER_LEVEL, waterLevel);
        builder.put(EarthData.MEAN_TEMPERATURE, meanTemperature);
        builder.put(EarthData.MIN_TEMPERATURE, minTemperature);
        builder.put(EarthData.ANNUAL_RAINFALL, annualRainfall);
        builder.put(EarthData.CATION_EXCHANGE_CAPACITY, cationExchangeCapacity);
        builder.put(EarthData.ORGANIC_CARBON_CONTENT, organicCarbonContent);
        builder.put(EarthData.SOIL_PH, soilPh);
        builder.put(EarthData.CLAY_CONTENT, clayContent);
        builder.put(EarthData.SILT_CONTENT, siltContent);
        builder.put(EarthData.SAND_CONTENT, sandContent);
        builder.put(EarthData.SOIL_SUBORDER, soilClass);
    }
}
