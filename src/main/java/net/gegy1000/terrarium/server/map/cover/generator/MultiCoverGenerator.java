package net.gegy1000.terrarium.server.map.cover.generator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobFilterPrimer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
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
    public void initialize(World world, CoverType[] globBuffer, int[] heightBuffer, IBlockState[] coverBuffer, IBlockState[] fillerBuffer, boolean debug) {
        super.initialize(world, globBuffer, heightBuffer, coverBuffer, fillerBuffer, debug);

        for (Entry entry : this.entries) {
            CoverGenerator generator = entry.type.createGenerator();
            this.generatorMap.put(entry.type.getId(), generator);

            CoverType[] newGlobBuffer = ArrayUtils.defaulted(new CoverType[256], CoverType.NO_DATA);
            IBlockState[] newCoverBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            IBlockState[] newFillerBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            generator.initialize(world, newGlobBuffer, heightBuffer, newCoverBuffer, newFillerBuffer, debug);
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
            IBlockState state = sampledType != null ? sampledType.coverBuffer[index] : null;
            if (state == null) {
                state = Blocks.STONE.getDefaultState();
            }
            this.coverBuffer[index] = state;
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
            IBlockState state = sampledType != null ? sampledType.fillerBuffer[index] : null;
            if (state == null) {
                state = Blocks.STONE.getDefaultState();
            }
            this.fillerBuffer[index] = state;
        });
    }

    protected abstract GenLayer zoom(GenLayer layer);

    private void populateGeneratorBuffer(CoverGenerator generator) {
        for (int i = 0; i < this.globBuffer.length; i++) {
            CoverType type = this.globBuffer[i];
            generator.globBuffer[i] = type == this.type ? generator.getType() : CoverType.NO_DATA;
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
