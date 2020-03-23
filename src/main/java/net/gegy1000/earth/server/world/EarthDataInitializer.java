package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.data.AreaData;
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
import net.gegy1000.earth.server.world.ecology.soil.SoilClass;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.coordinate.CoordReferenced;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.data.op.SampleRaster;
import net.gegy1000.terrarium.server.world.data.op.SlopeOp;
import net.gegy1000.terrarium.server.world.data.op.VoronoiScaleOp;
import net.gegy1000.terrarium.server.world.data.raster.BitRaster;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthDataInitializer implements TerrariumDataInitializer {
    private static final Zoomable<StdSource<ShortRaster>> ELEVATION_SOURCE = ElevationSource.source();
    private static final LandCoverSource LAND_COVER_SOURCE = new LandCoverSource();

    private static final Zoomable<StdSource<ShortRaster>> CATION_EXCHANGE_CAPACITY_SOURCE = SoilSources.cationExchangeCapacity();
    private static final Zoomable<StdSource<ShortRaster>> ORGANIC_CARBON_CONTENT_SOURCE = SoilSources.organicCarbonContent();
    private static final Zoomable<StdSource<ShortRaster>> PH_SOURCE = SoilSources.ph();
    private static final Zoomable<StdSource<ShortRaster>> CLAY_CONTENT_SOURCE = SoilSources.clayContent();
    private static final Zoomable<StdSource<ShortRaster>> SILT_CONTENT_SOURCE = SoilSources.siltContent();
    private static final Zoomable<StdSource<ShortRaster>> SAND_CONTENT_SOURCE = SoilSources.sandContent();
    private static final Zoomable<StdSource<EnumRaster<SoilClass>>> SOIL_CLASS_SOURCE = SoilSources.soilClass();

    private static final NoiseGeneratorPerlin TEMPERATURE_NOISE = new NoiseGeneratorPerlin(new Random(12345), 1);

    private static final OceanPolygonSource OCEAN_SOURCE = new OceanPolygonSource();

    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    private DataOp<ShortRaster> elevation(double worldScale) {
        int maxZoom = this.selectStandardZoom(worldScale);
        return new ResampleZoomRasters<ShortRaster>()
                .from(ELEVATION_SOURCE.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.stdRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(SampleRaster::sampleShort)
                .resample((sample, crs) -> {
                    return InterpolationScaleOp.appropriateForScale(crs.avgScale())
                            .scaleShortsFrom(sample, crs);
                })
                .atZoom(maxZoom)
                .create();
    }

    private DataOp<EnumRaster<Cover>> landcover() {
        DataOp<EnumRaster<Cover>> cover = SampleRaster.sampleEnum(LAND_COVER_SOURCE, Cover.NO);
        cover = VoronoiScaleOp.scaleEnumsFrom(cover, this.ctx.landcoverRasterCrs, Cover.NO)
                .cached(EnumRaster::copy);

        return cover;
    }

    private DataOp<BitRaster> oceanMask() {
        DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(OCEAN_SOURCE, this.ctx.lngLatCrs);
        DataOp<AreaData> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCrs);
        return RasterizeAreaOp.apply(oceanArea);
    }

    private DataOp<ShortRaster> genericSoil(
            double worldScale,
            Zoomable<StdSource<ShortRaster>> zoomableSource
    ) {
        int maxZoom = this.selectStandardZoom(worldScale);
        return new ResampleZoomRasters<ShortRaster>()
                .from(zoomableSource.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.stdRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(SampleRaster::sampleShort)
                .resample((sample, crs) -> {
                    return InterpolationScaleOp.appropriateForScale(crs.avgScale())
                            .scaleShortsFrom(sample, crs);
                })
                .atZoom(maxZoom)
                .create();
    }

    private DataOp<EnumRaster<SoilClass>> soilClass(double worldScale) {
        int maxZoom = this.selectStandardZoom(worldScale);
        return new ResampleZoomRasters<EnumRaster<SoilClass>>()
                .from(SOIL_CLASS_SOURCE.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.stdRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(source -> SampleRaster.sampleEnum(source, SoilClass.NO))
                .resample((sample, crs) -> VoronoiScaleOp.scaleEnumsFrom(sample, crs, SoilClass.NO))
                .atZoom(maxZoom)
                .create();
    }

    private int selectStandardZoom(double worldScale) {
        double zoom = StdSource.zoomForScale(worldScale);
        return (int) Math.round(zoom);
    }

    private int selectElevationZoom(double worldScale) {
        worldScale = MathHelper.floor(worldScale);
        if (worldScale > 300.0) {
            return 0;
        } else if (worldScale > 80.0) {
            return 1;
        } else if (worldScale > 30.0) {
            return 2;
        } else {
            return 3;
        }
    }

    private AddNoiseOp temperatureNoise() {
        return new AddNoiseOp(TEMPERATURE_NOISE, 0.05, 1.5);
    }

    @Override
    public void setup(DataGenerator.Builder builder) {
        double worldScale = this.ctx.settings.getDouble(WORLD_SCALE);
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        int seaLevel = heightOffset + 1;

        DataOp<ShortRaster> elevation = this.elevation(worldScale).cached(ShortRaster::copy);
        DataOp<UByteRaster> slope = SlopeOp.from(elevation, 1.0F / (float) worldScale);

        DataOp<EnumRaster<Cover>> cover = this.landcover();

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(elevation, cover);

        double terrestrialHeightScale = this.ctx.settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) / worldScale;
        double oceanicHeightScale = this.ctx.settings.getDouble(OCEANIC_HEIGHT_SCALE) / worldScale;

        DataOp<ShortRaster> terrainHeight = new TransformTerrainElevation(
                terrestrialHeightScale,
                oceanicHeightScale,
                heightOffset
        ).apply(elevation).cached(ShortRaster::copy);

        if (worldScale <= 90.0) {
            DataOp<BitRaster> oceanMask = this.oceanMask();
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

        DataOp<UByteRaster> cationExchangeCapacity = this.genericSoil(worldScale, CATION_EXCHANGE_CAPACITY_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<ShortRaster> organicCarbonContent = this.genericSoil(worldScale, ORGANIC_CARBON_CONTENT_SOURCE);
        DataOp<UByteRaster> soilPh = this.genericSoil(worldScale, PH_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> clayContent = this.genericSoil(worldScale, CLAY_CONTENT_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> siltContent = this.genericSoil(worldScale, SILT_CONTENT_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> sandContent = this.genericSoil(worldScale, SAND_CONTENT_SOURCE)
                .map((raster, view) -> UByteRaster.copyFrom(raster));

        DataOp<EnumRaster<SoilClass>> soilClass = this.soilClass(worldScale);

        builder.put(EarthDataKeys.TERRAIN_HEIGHT, terrainHeight);
        builder.put(EarthDataKeys.ELEVATION_METERS, elevation);
        builder.put(EarthDataKeys.SLOPE, slope);
        builder.put(EarthDataKeys.COVER, cover);
        builder.put(EarthDataKeys.LANDFORM, landforms);
        builder.put(EarthDataKeys.WATER_LEVEL, waterLevel);
        builder.put(EarthDataKeys.MEAN_TEMPERATURE, meanTemperature);
        builder.put(EarthDataKeys.MIN_TEMPERATURE, minTemperature);
        builder.put(EarthDataKeys.ANNUAL_RAINFALL, annualRainfall);
        builder.put(EarthDataKeys.CATION_EXCHANGE_CAPACITY, cationExchangeCapacity);
        builder.put(EarthDataKeys.ORGANIC_CARBON_CONTENT, organicCarbonContent);
        builder.put(EarthDataKeys.SOIL_PH, soilPh);
        builder.put(EarthDataKeys.CLAY_CONTENT, clayContent);
        builder.put(EarthDataKeys.SILT_CONTENT, siltContent);
        builder.put(EarthDataKeys.SAND_CONTENT, sandContent);
        builder.put(EarthDataKeys.SOIL_CLASS, soilClass);
    }
}
