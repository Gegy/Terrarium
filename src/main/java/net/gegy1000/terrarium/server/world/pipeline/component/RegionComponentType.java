package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.minecraft.util.ResourceLocation;

public abstract class RegionComponentType<T extends TiledDataAccess> {
    public static final RegionComponentType<ShortRasterTile> HEIGHT = new RegionComponentType<ShortRasterTile>(new ResourceLocation(Terrarium.MODID, "height"), ShortRasterTile.class) {
        @Override
        public ShortRasterTile createDefaultData(int width, int height) {
            short[] data = new short[width * height];
            return new ShortRasterTile(data, width, height);
        }
    };

    public static final RegionComponentType<ByteRasterTile> SLOPE = new RegionComponentType<ByteRasterTile>(new ResourceLocation(Terrarium.MODID, "slope"), ByteRasterTile.class) {
        @Override
        public ByteRasterTile createDefaultData(int width, int height) {
            byte[] data = new byte[width * height];
            return new ByteRasterTile(data, width, height);
        }
    };

    public static final RegionComponentType<CoverRasterTile> COVER = new RegionComponentType<CoverRasterTile>(new ResourceLocation(Terrarium.MODID, "cover"), CoverRasterTile.class) {
        @Override
        public CoverRasterTile createDefaultData(int width, int height) {
            CoverType[] data = ArrayUtils.defaulted(new CoverType[width * height], TerrariumCoverTypes.PLACEHOLDER);
            return new CoverRasterTile(data, width, height);
        }
    };

    private final ResourceLocation identifier;
    private final Class<T> type;

    public RegionComponentType(ResourceLocation identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public abstract T createDefaultData(int width, int height);

    public final Class<T> getType() {
        return this.type;
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    @Override
    public final int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof RegionComponentType && ((RegionComponentType) obj).getIdentifier().equals(this.identifier);
    }
}
