package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
import net.minecraft.util.ResourceLocation;

public abstract class RegionComponentType<T extends Data> {
    public static final RegionComponentType<ShortRaster> HEIGHT = new RegionComponentType<ShortRaster>(new ResourceLocation(Terrarium.MODID, "height"), ShortRaster.class) {
        @Override
        public ShortRaster createDefaultData(int width, int height) {
            short[] data = new short[width * height];
            return new ShortRaster(data, width, height);
        }
    };

    public static final RegionComponentType<UnsignedByteRaster> SLOPE = new RegionComponentType<UnsignedByteRaster>(new ResourceLocation(Terrarium.MODID, "slope"), UnsignedByteRaster.class) {
        @Override
        public UnsignedByteRaster createDefaultData(int width, int height) {
            byte[] data = new byte[width * height];
            return new UnsignedByteRaster(data, width, height);
        }
    };

    public static final RegionComponentType<CoverRaster> COVER = new RegionComponentType<CoverRaster>(new ResourceLocation(Terrarium.MODID, "cover"), CoverRaster.class) {
        @Override
        public CoverRaster createDefaultData(int width, int height) {
            CoverType[] data = ArrayUtils.defaulted(new CoverType[width * height], TerrariumCoverTypes.PLACEHOLDER);
            return new CoverRaster(data, width, height);
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

    public RegionComponent<T> createDefaultComponent(int width, int height) {
        return new RegionComponent<>(this, this.createDefaultData(width, height));
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
