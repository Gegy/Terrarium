package net.gegy1000.earth.server.world.pipeline.adapter;

import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import net.gegy1000.earth.server.world.cover.EarthCoverBiomes;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.rasterization.OsmShapeProducer;
import net.gegy1000.terrarium.server.world.rasterization.RasterCanvas;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.biome.Biome;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class OsmAreaCoverAdapter implements RegionAdapter {
    private final CoordinateState geoCoordinateState;
    private final RegionComponentType<OsmTile> osmComponent;
    private final RegionComponentType<BiomeRasterTile> biomeComponent;

    private final List<Type> polygonTypes = new ArrayList<>();
    private final List<Biome> biomes = new ArrayList<>();

    public OsmAreaCoverAdapter(CoordinateState geoCoordinateState, RegionComponentType<OsmTile> osmComponent, RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.geoCoordinateState = geoCoordinateState;
        this.osmComponent = osmComponent;
        this.biomeComponent = biomeComponent;

        this.addBiome(EarthCoverBiomes.OPEN_BROADLEAF_DECIDUOUS, this::isWoodedArea);
        this.addBiome(EarthCoverBiomes.SHRUBLAND, this::isScrubArea);
        this.addBiome(EarthCoverBiomes.SCREE, this::isScree);
        this.addBiome(EarthCoverBiomes.FLOODED_GRASSLAND, this::isWetlandArea);
        this.addBiome(EarthCoverBiomes.IRRIGATED_CROPS, this::isFarmlandArea);
        this.addBiome(EarthCoverBiomes.FLOWER_FIELD, this::isFlowerField);
        this.addBiome(EarthCoverBiomes.SNOW, this::isGlacierArea);
    }

    private void addBiome(Biome biome, Predicate<OsmEntity> filter) {
        this.polygonTypes.add(new Type(this.polygonTypes.size(), filter));
        this.biomes.add(biome);
    }

    private boolean isFarmlandArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "landuse", "farmland");
    }

    // TODO: This causes issues in that it can replace areas that are already forest.
    // All data should come in tiles with this pre-processed
    private boolean isWoodedArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "natural", "wood")
                || OsmDataParser.hasTag(entity, "landuse", "forest");
    }

    private boolean isWetlandArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "natural", "wetland");
    }

    private boolean isScrubArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "natural", "scrub");
    }

    private boolean isGlacierArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "natural", "glacier");
    }

    private boolean isFlowerField(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "leisure", "garden")
                || OsmDataParser.hasTag(entity, "landuse", "meadow")
                || OsmDataParser.hasTag(entity, "leisure", "park");
    }

    private boolean isScree(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "natural", "scree")
                || OsmDataParser.hasTag(entity, "natural", "bare_rock")
                || OsmDataParser.hasTag(entity, "natural", "shingle")
                || OsmDataParser.hasTag(entity, "landuse", "quarry");
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        OsmTile osmTile = data.getOrExcept(this.osmComponent);
        BiomeRasterTile biomeTile = data.getOrExcept(this.biomeComponent);

        Collection<Result> polygons = this.collectPolygons(new DataView(x, z, width, height), osmTile);

        if (!polygons.isEmpty()) {
            RasterCanvas canvas = new RasterCanvas(width, height);
            canvas.setOrigin(x, z);

            for (Result result : polygons) {
                canvas.setColor(result.id + 1);
                for (MultiPolygon polygon : result.polygons) {
                    Area shape = OsmShapeProducer.toShape(polygon, this.geoCoordinateState);
                    Rectangle bounds = shape.getBounds();
                    if (bounds.getWidth() > 2 && bounds.getHeight() > 2) {
                        canvas.fill(shape);
                    }
                }
            }

            for (int localZ = 0; localZ < height; localZ++) {
                for (int localX = 0; localX < width; localX++) {
                    int value = canvas.getData(localX, localZ);
                    if (value != 0) {
                        Biome biome = this.biomes.get(value - 1);
                        biomeTile.set(localX, localZ, biome);
                    }
                }
            }
        }
    }

    private Collection<Result> collectPolygons(DataView view, OsmTile osmTile) {
        List<Result> results = new ArrayList<>();
        for (Type type : this.polygonTypes) {
            Collection<MultiPolygon> coverPolygons = osmTile.collectPolygons(view, this.geoCoordinateState, type.filter);
            if (!coverPolygons.isEmpty()) {
                results.add(new Result(type.id, coverPolygons));
            }
        }
        return results;
    }

    private static class Type {
        private final int id;
        private final Predicate<OsmEntity> filter;

        private Type(int id, Predicate<OsmEntity> filter) {
            this.id = id;
            this.filter = filter;
        }
    }

    private static class Result {
        private final int id;
        private final Collection<MultiPolygon> polygons;

        private Result(int id, Collection<MultiPolygon> polygons) {
            this.id = id;
            this.polygons = polygons;
        }
    }
}
