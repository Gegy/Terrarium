package net.gegy1000.terrarium.server.map.system.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.CoverTileAccess;
import net.gegy1000.terrarium.server.map.source.height.HeightTileAccess;
import net.gegy1000.terrarium.server.map.source.height.SlopeTileAccess;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.minecraft.util.ResourceLocation;

public class TerrariumComponentTypes {
    public static final RegionComponentType<HeightTileAccess> HEIGHT = new AbstractComponentType<HeightTileAccess>(
            new ResourceLocation(Terrarium.MODID, "height")
    ) {
        @Override
        public HeightTileAccess createDefaultData(int width, int height) {
            return new HeightTileAccess(new short[width * height], width, height);
        }
    };

    public static final RegionComponentType<SlopeTileAccess> SLOPE = new AbstractComponentType<SlopeTileAccess>(
            new ResourceLocation(Terrarium.MODID, "slope")
    ) {
        @Override
        public SlopeTileAccess createDefaultData(int width, int height) {
            return new SlopeTileAccess(new byte[width * height], width, height);
        }
    };

    public static final RegionComponentType<CoverTileAccess> COVER = new AbstractComponentType<CoverTileAccess>(
            new ResourceLocation(Terrarium.MODID, "cover")
    ) {
        @Override
        public CoverTileAccess createDefaultData(int width, int height) {
            return new CoverTileAccess(ArrayUtils.defaulted(new CoverType[width * height], CoverType.NO_DATA), width, height);
        }
    };

    public static final RegionComponentType<OverpassTileAccess> OVERPASS = new AbstractComponentType<OverpassTileAccess>(
            new ResourceLocation(Terrarium.MODID, "overpass")
    ) {
        @Override
        public OverpassTileAccess createDefaultData(int width, int height) {
            return new OverpassTileAccess();
        }
    };

    private static abstract class AbstractComponentType<T> implements RegionComponentType<T> {
        private final ResourceLocation identifier;

        private AbstractComponentType(ResourceLocation identifier) {
            this.identifier = identifier;
        }

        @Override
        public ResourceLocation getIdentifier() {
            return this.identifier;
        }
    }
}
