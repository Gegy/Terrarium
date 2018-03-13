package net.gegy1000.terrarium.server.world.cover.generator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobFilterPrimer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.layer.GenLayer;

import java.util.Map;
import java.util.Random;

public abstract class MultiCoverGenerator extends CoverGenerator {
    private final Entry[] entries;

    private final Int2ObjectMap<CoverGenerator> generatorMap = new Int2ObjectOpenHashMap<>();

    private GenLayer globSelector;

    public MultiCoverGenerator(CoverType type, Entry... entries) {
        super(type);
        this.entries = entries;
    }

    @Override
    public void initialize(World world, CoverRasterTileAccess coverRaster, ShortRasterTileAccess heightRaster, ByteRasterTileAccess slopeRater, IBlockState[] coverBlockBuffer, IBlockState[] fillerBlockBuffer, boolean debug) {
        super.initialize(world, coverRaster, heightRaster, slopeRater, coverBlockBuffer, fillerBlockBuffer, debug);

        for (Entry entry : this.entries) {
            CoverGenerator generator = entry.type.createGenerator();
            this.generatorMap.put(entry.type.getId(), generator);

            CoverRasterTileAccess newCoverRaster = new CoverRasterTileAccess(16, 16);
            IBlockState[] newCoverBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            IBlockState[] newFillerBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            generator.initialize(world, newCoverRaster, heightRaster, slopeRater, newCoverBuffer, newFillerBuffer, debug);
        }
    }

    @Override
    protected void createLayers(boolean debug) {
        SelectWeightedLayer.Entry[] layerEntries = new SelectWeightedLayer.Entry[this.entries.length];
        for (int i = 0; i < this.entries.length; i++) {
            Entry entry = this.entries[i];
            layerEntries[i] = new SelectWeightedLayer.Entry(entry.type.getId(), entry.weight);
        }

        this.globSelector = this.zoom(new SelectWeightedLayer(1, layerEntries));
        this.globSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);
        CoverGenerator sampledType = this.generatorMap.get(sampledTypes[136]);
        if (sampledType != null) {
            sampledType.decorate(random, zone, x, z);
        }
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);

        for (Map.Entry<Integer, CoverGenerator> entry : this.generatorMap.entrySet()) {
            CoverGenerator generator = entry.getValue();
            this.populateGeneratorBuffer(generator);
            generator.coverDecorate(new GlobFilterPrimer(primer, sampledTypes, entry.getKey()), random, x, z);
        }
    }

    @Override
    public void getCover(Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);

        for (CoverGenerator generator : this.generatorMap.values()) {
            this.populateGeneratorBuffer(generator);
            generator.getCover(random, x, z);
        }

        this.iterate(point -> {
            int index = point.index;
            CoverGenerator sampledType = this.generatorMap.get(sampledTypes[index]);
            IBlockState state = sampledType != null ? sampledType.coverBlockBuffer[index] : null;
            if (state == null) {
                state = Blocks.STONE.getDefaultState();
            }
            this.coverBlockBuffer[index] = state;
        });
    }

    @Override
    public void getFiller(Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);

        for (CoverGenerator generator : this.generatorMap.values()) {
            this.populateGeneratorBuffer(generator);
            generator.getFiller(random, x, z);
        }

        this.iterate(point -> {
            int index = point.index;
            CoverGenerator sampledType = this.generatorMap.get(sampledTypes[index]);
            IBlockState state = sampledType != null ? sampledType.fillerBlockBuffer[index] : null;
            if (state == null) {
                state = Blocks.STONE.getDefaultState();
            }
            this.fillerBlockBuffer[index] = state;
        });
    }

    protected abstract GenLayer zoom(GenLayer layer);

    private void populateGeneratorBuffer(CoverGenerator generator) {
        for (int i = 0; i < this.coverBuffer.length; i++) {
            CoverType type = this.coverBuffer[i];
            generator.coverBuffer[i] = type == this.type ? generator.getType() : CoverType.NO_DATA;
        }
    }

    public static class Entry {
        private final CoverType type;
        private final int weight;

        public Entry(CoverType type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }
}
