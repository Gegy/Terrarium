package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.data.AreaData;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.earth.server.world.data.op.ClimateSampler;
import net.gegy1000.earth.server.world.data.op.OffsetValueOp;
import net.gegy1000.earth.server.world.data.op.PolygonSampler;
import net.gegy1000.earth.server.world.data.op.PolygonToAreaOp;
import net.gegy1000.earth.server.world.data.op.ProduceLandformsOp;
import net.gegy1000.earth.server.world.data.op.RasterizeAreaOp;
import net.gegy1000.earth.server.world.data.op.ResampleZoomRasters;
import net.gegy1000.earth.server.world.data.op.ScaleTerrainElevationOp;
import net.gegy1000.earth.server.world.data.op.WaterOps;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.OceanPolygonSource;
import net.gegy1000.earth.server.world.data.source.SoilSource;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.coordinate.CoordReferenced;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.ColumnDataGenerator;
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

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthDataInitializer implements TerrariumDataInitializer {
    private static final Zoomable<ElevationSource> ELEVATION_SOURCE = Zoomable.create(ElevationSource.zoomLevels(), ElevationSource::new);
    private static final LandCoverSource LAND_COVER_SOURCE = new LandCoverSource();

    private static final Zoomable<SoilSource> CATION_EXCHANGE_CAPACITY_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::cationExchangeCapacity);
    private static final Zoomable<SoilSource> ORGANIC_CARBON_CONTENT_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::organicCarbonContent);
    private static final Zoomable<SoilSource> PH_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::ph);
    private static final Zoomable<SoilSource> CLAY_CONTENT_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::clayContent);
    private static final Zoomable<SoilSource> SILT_CONTENT_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::siltContent);
    private static final Zoomable<SoilSource> SAND_CONTENT_SOURCE = Zoomable.create(SoilSource.zoomLevels(), SoilSource::sandContent);

    private static final OceanPolygonSource OCEAN_SOURCE = new OceanPolygonSource();

    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    private DataOp<ShortRaster> elevation(double worldScale) {
        int elevationZoom = this.selectElevationZoom(worldScale);

        return new ResampleZoomRasters<ShortRaster>()
                .from(ELEVATION_SOURCE.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.elevationRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(SampleRaster::sampleShort)
                .atStandardZoom(elevationZoom)
                .create(ShortRaster::create);
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

    private DataOp<ShortRaster> genericSoil(double worldScale, Zoomable<SoilSource> zoomableSource) {
        int soilZoom = this.selectSoilZoom(worldScale);

        return new ResampleZoomRasters<ShortRaster>()
                .from(zoomableSource.map((zoom, source) -> {
                    CoordinateReference crs = this.ctx.soilRasterCrs.forZoom(zoom);
                    return new CoordReferenced<>(source, crs);
                }))
                .sample(SampleRaster::sampleShort)
                .atStandardZoom(soilZoom)
                .create(ShortRaster::create);
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

    private int selectSoilZoom(double worldScale) {
        return worldScale > 250.0 ? 0 : 1;
    }

    @Override
    public void setup(ColumnDataGenerator.Builder builder) {
        double worldScale = this.ctx.settings.getDouble(WORLD_SCALE);
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        int seaLevel = heightOffset + 1;

        DataOp<ShortRaster> elevation = this.elevation(worldScale);
        DataOp<UByteRaster> slope = SlopeOp.from(elevation, 1.0F / (float) worldScale);

        DataOp<EnumRaster<Cover>> cover = this.landcover();

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(elevation, cover);

        double terrestrialHeightScale = this.ctx.settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) / worldScale;
        double oceanicHeightScale = this.ctx.settings.getDouble(OCEANIC_HEIGHT_SCALE) / worldScale;

        DataOp<ShortRaster> terrainHeight = new ScaleTerrainElevationOp(terrestrialHeightScale, oceanicHeightScale).apply(elevation);
        terrainHeight = new OffsetValueOp(heightOffset).apply(terrainHeight);

        if (worldScale <= 130.0) {
            DataOp<BitRaster> oceanMask = this.oceanMask();

            landforms = WaterOps.applyWaterMask(landforms, oceanMask).cached(EnumRaster::copy);
            terrainHeight = WaterOps.applyToHeight(terrainHeight, landforms, seaLevel);
        }

        cover = WaterOps.applyToCover(cover, landforms);

        DataOp<ShortRaster> waterLevel = WaterOps.produceWaterLevel(landforms, seaLevel);

        SharedEarthData sharedData = SharedEarthData.instance();
        ClimateSampler climateSampler = new ClimateSampler(sharedData.get(SharedEarthData.CLIMATIC_VARIABLES));

        DataOp<ShortRaster> annualRainfall = climateSampler.annualRainfall();
        annualRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(annualRainfall, this.ctx.climateRasterCrs);

        DataOp<FloatRaster> meanTemperature = climateSampler.meanTemperature();
        meanTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(meanTemperature, this.ctx.climateRasterCrs);

        DataOp<FloatRaster> minTemperature = climateSampler.minTemperature();
        minTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(minTemperature, this.ctx.climateRasterCrs);

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
    }
}
