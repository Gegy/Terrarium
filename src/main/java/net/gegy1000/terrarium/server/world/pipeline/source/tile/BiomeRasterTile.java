package net.gegy1000.terrarium.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Arrays;

public class BiomeRasterTile implements RasterDataAccess<Biome>, TiledDataAccess {
    private final Biome[] biomes;
    private final int offsetX;
    private final int offsetZ;
    private final int width;
    private final int height;

    public BiomeRasterTile(Biome[] biomes, int offsetX, int offsetZ, int width, int height) {
        if (biomes.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match biome length!");
        }
        this.biomes = biomes;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.width = width;
        this.height = height;
    }

    public BiomeRasterTile(DataView view) {
        this.biomes = new Biome[view.getWidth() * view.getHeight()];
        this.offsetX = 0;
        this.offsetZ = 0;
        this.width = view.getWidth();
        this.height = view.getHeight();
    }

    public BiomeRasterTile(Biome[] biomes, int width, int height) {
        this(biomes, 0, 0, width, height);
    }

    public BiomeRasterTile(int width, int height) {
        this(ArrayUtils.defaulted(new Biome[width * height], Biomes.DEFAULT), width, height);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void set(int x, int z, Biome value) {
        this.biomes[(x - this.offsetX) + (z - this.offsetZ) * this.width] = value;
    }

    @Override
    public Biome get(int x, int z) {
        return this.biomes[(x - this.offsetX) + (z - this.offsetZ) * this.width];
    }

    @Override
    public Biome[] getData() {
        return this.biomes;
    }

    @Override
    public BiomeRasterTile copy() {
        return new BiomeRasterTile(Arrays.copyOf(this.biomes, this.biomes.length), this.width, this.height);
    }
}
