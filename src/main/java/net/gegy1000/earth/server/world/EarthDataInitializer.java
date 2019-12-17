package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.util.Zoomable;
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
import net.gegy1000.earth.server.world.data.op.ResampleZoomRasters;
import net.gegy1000.earth.server.world.data.op.ScaleTerrainElevationOp;
import net.gegy1000.earth.server.world.data.op.WaterOps;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.OceanPolygonSource;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
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

    @Override
    public ColumnDataGenerator buildDataGenerator() {
        double worldScale = this.ctx.settings.getDouble(WORLD_SCALE);
        int heightOffset = this.ctx.settings.getInteger(HEIGHT_OFFSET);
        int seaLevel = heightOffset + 1;

        int elevationZoom = this.selectElevationZoom(worldScale);

        Zoomable<ElevationSource> elevationSource = this.ctx.elevationRasterCrs.map(ElevationSource::new);

        DataOp<ShortRaster> elevation = new ResampleZoomRasters<ShortRaster>()
                .from(elevationSource).sample(SampleRaster::sampleShort)
                .atStandardZoom(elevationZoom)
                .create(ShortRaster::create);

        DataOp<UByteRaster> slope = SlopeOp.from(elevation, 1.0F / (float) worldScale);

        LandCoverSource landCoverSource = new LandCoverSource(this.ctx.landcoverRasterCrs);

        DataOp<UByteRaster> coverId = SampleRaster.sampleUByte(landCoverSource);
        coverId = VoronoiScaleOp.scaleUBytesFrom(coverId, this.ctx.landcoverRasterCrs, UByteRaster::create)
                .cached(UByteRaster::copy);

        DataOp<EnumRaster<Landform>> landforms = ProduceLandformsOp.produce(elevation, coverId);

        DataOp<EnumRaster<Cover>> cover = ProduceCoverOp.produce(coverId);

        double terrestrialHeightScale = this.ctx.settings.getDouble(TERRESTRIAL_HEIGHT_SCALE) / worldScale;
        double oceanicHeightScale = this.ctx.settings.getDouble(OCEANIC_HEIGHT_SCALE) / worldScale;
        elevation = new ScaleTerrainElevationOp(terrestrialHeightScale, oceanicHeightScale).apply(elevation);
        elevation = new OffsetValueOp(heightOffset).apply(elevation);

        if (worldScale <= 100.0) {
            OceanPolygonSource oceanPolygonSource = new OceanPolygonSource(this.ctx.lngLatCrs);

            // TODO: this causes weird holes in the water when it does not match up with the heightmap
            //  potentially needs flood fill like is done with cover
            DataOp<PolygonData> oceanPolygons = PolygonSampler.sample(oceanPolygonSource, this.ctx.lngLatCrs);
            DataOp<AreaData> oceanArea = PolygonToAreaOp.apply(oceanPolygons, this.ctx.lngLatCrs);
            DataOp<BitRaster> oceanMask = RasterizeAreaOp.apply(oceanArea);

            landforms = WaterOps.applyWaterMask(landforms, oceanMask).cached(EnumRaster::copy);
            elevation = WaterOps.forceSeaFloorBelowSurface(elevation, landforms, seaLevel);
        }

        cover = WaterOps.applyToCover(cover, landforms);

        DataOp<ShortRaster> waterLevel = WaterOps.produceWaterLevel(landforms, seaLevel);

        Season season = this.ctx.settings.get(SEASON);
        ClimateSampler climateSampler = new ClimateSampler(season.getClimateRaster());

        DataOp<ShortRaster> monthlyRainfall = climateSampler.monthlyRainfall();
        monthlyRainfall = InterpolationScaleOp.LINEAR.scaleShortsFrom(monthlyRainfall, this.ctx.climateRasterCrs);

        DataOp<FloatRaster> averageTemperature = climateSampler.averageTemperature();
        averageTemperature = InterpolationScaleOp.LINEAR.scaleFloatsFrom(averageTemperature, this.ctx.climateRasterCrs);

        return ColumnDataGenerator.builder()
                .with(EarthDataKeys.HEIGHT, elevation)
                .with(EarthDataKeys.SLOPE, slope)
                .with(EarthDataKeys.COVER, cover)
                .with(EarthDataKeys.LANDFORM, landforms)
                .with(EarthDataKeys.WATER_LEVEL, waterLevel)
                .with(EarthDataKeys.AVERAGE_TEMPERATURE, averageTemperature)
                .with(EarthDataKeys.MONTHLY_RAINFALL, monthlyRainfall)
                .build();
    }

    private int selectElevationZoom(double worldScale) {
        if (worldScale > 300.0) {
            return 0;
        } else if (worldScale > 80.0) {
            return 1;
        } else if (worldScale > 30.0) {
            return 2;
        } else {
            // TODO: Zoom level 3
//            return 3;
            return 2;
        }
    }
}
