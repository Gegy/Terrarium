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

import static net.gegy1000.earth.server.world.EarthWorldType.*;

final class EarthDataInitializer implements TerrariumDataInitializer {
    private final EarthInitContext ctx;

    EarthDataInitializer(EarthInitContext ctx) {
        this.ctx = ctx;
    }

    private DataOp<ShortRaster> elevation(double worldScale) {
        int elevationZoom = this.selectElevationZoom(worldScale);

        Zoomable<ElevationSource> elevationSource = this.ctx.elevationRasterCrs.map(ElevationSource::new);

        return new ResampleZoomRasters<ShortRaster>()
                .from(elevationSource).sample(SampleRaster::sampleShort)
                .atStandardZoom(elevationZoom)
                .create(ShortRaster::create);
    }

    private DataOp<EnumRaster<Cover>> landcover() {
        LandCoverSource landCoverSource = new LandCoverSource(this.ctx.landcoverRasterCrs);

        DataOp<EnumRaster<Cover>> cover = SampleRaster.sampleEnum(landCoverSource, Cover.NO);
        cover = VoronoiScaleOp.scaleEnumsFrom(cover, this.ctx.landcoverRasterCrs, Cover.NO)
                .cached(EnumRaster::copy);

        return cover;
    }

    private DataOp<BitRaster> oceanMask() {
        OceanPolygonSource oceanPolygonSource = new OceanPolygonSource(this.ctx.lngLatCrs);

        DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(oceanPolygonSource, this.ctx.lngLatCrs);
        DataOp<AreaData> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCrs);
        return RasterizeAreaOp.apply(oceanArea);
    }

    private DataOp<ShortRaster> genericSoil(double worldScale, Zoomable.Map<CoordinateReference, SoilSource> createSource) {
        int soilZoom = this.selectSoilZoom(worldScale);
        Zoomable<SoilSource> soilSource = this.ctx.soilRasterCrs.map(createSource);

        return new ResampleZoomRasters<ShortRaster>()
                .from(soilSource).sample(SampleRaster::sampleShort)
                .atStandardZoom(soilZoom)
                .create(ShortRaster::create);
    }

    private int selectElevationZoom(double worldScale) {
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
    public ColumnDataGenerator buildDataGenerator() {
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

        DataOp<FloatRaster> averageTemperature = climateSampler.averageTemperature();
        averageTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(averageTemperature, this.ctx.climateRasterCrs);

        DataOp<UByteRaster> cationExchangeCapacity = this.genericSoil(worldScale, SoilSource::cationExchangeCapacity)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<ShortRaster> organicCarbonContent = this.genericSoil(worldScale, SoilSource::organicCarbonContent);
        DataOp<UByteRaster> soilPh = this.genericSoil(worldScale, SoilSource::ph)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> clayContent = this.genericSoil(worldScale, SoilSource::clayContent)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> siltContent = this.genericSoil(worldScale, SoilSource::siltContent)
                .map((raster, view) -> UByteRaster.copyFrom(raster));
        DataOp<UByteRaster> sandContent = this.genericSoil(worldScale, SoilSource::sandContent)
                .map((raster, view) -> UByteRaster.copyFrom(raster));

        return ColumnDataGenerator.builder()
                .with(EarthDataKeys.TERRAIN_HEIGHT, terrainHeight)
                .with(EarthDataKeys.ELEVATION_METERS, elevation)
                .with(EarthDataKeys.SLOPE, slope)
                .with(EarthDataKeys.COVER, cover)
                .with(EarthDataKeys.LANDFORM, landforms)
                .with(EarthDataKeys.WATER_LEVEL, waterLevel)
                .with(EarthDataKeys.MEAN_TEMPERATURE, averageTemperature)
                .with(EarthDataKeys.ANNUAL_RAINFALL, annualRainfall)
                .with(EarthDataKeys.CATION_EXCHANGE_CAPACITY, cationExchangeCapacity)
                .with(EarthDataKeys.ORGANIC_CARBON_CONTENT, organicCarbonContent)
                .with(EarthDataKeys.SOIL_PH, soilPh)
                .with(EarthDataKeys.CLAY_CONTENT, clayContent)
                .with(EarthDataKeys.SILT_CONTENT, siltContent)
                .with(EarthDataKeys.SAND_CONTENT, sandContent)
                .build();
    }
}
