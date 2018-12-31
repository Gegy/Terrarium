package net.gegy1000.terrarium.server.world.pipeline.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public abstract class RegionComponentType<T extends TiledDataAccess> {
    public static final RegionComponentType<ShortRasterTile> HEIGHT = new RegionComponentType<ShortRasterTile>(new Identifier(Terrarium.MODID, "height"), ShortRasterTile.class) {
        @Override
        public ShortRasterTile createDefaultData(int width, int height) {
            short[] data = new short[width * height];
            return new ShortRasterTile(data, width, height);
        }
    };

    public static final RegionComponentType<UnsignedByteRasterTile> SLOPE = new RegionComponentType<UnsignedByteRasterTile>(new Identifier(Terrarium.MODID, "slope"), UnsignedByteRasterTile.class) {
        @Override
        public UnsignedByteRasterTile createDefaultData(int width, int height) {
            byte[] data = new byte[width * height];
            return new UnsignedByteRasterTile(data, width, height);
        }
    };

    public static final RegionComponentType<BiomeRasterTile> BIOME = new RegionComponentType<BiomeRasterTile>(new Identifier(Terrarium.MODID, "biome"), BiomeRasterTile.class) {
        @Override
        public BiomeRasterTile createDefaultData(int width, int height) {
            Biome[] data = ArrayUtils.defaulted(new Biome[width * height], Biomes.DEFAULT);
            return new BiomeRasterTile(data, width, height);
        }
    };

    private final Identifier identifier;
    private final Class<T> type;

    public RegionComponentType(Identifier identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public abstract T createDefaultData(int width, int height);

    public final Class<T> getType() {
        return this.type;
    }

    public Identifier getIdentifier() {
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
