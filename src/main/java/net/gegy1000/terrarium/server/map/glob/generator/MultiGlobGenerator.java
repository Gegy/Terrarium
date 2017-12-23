package net.gegy1000.terrarium.server.map.glob.generator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobFilterPrimer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.layer.GenLayer;

import java.util.Map;
import java.util.Random;

public abstract class MultiGlobGenerator extends GlobGenerator {
    private final Entry[] entries;

    private final Int2ObjectMap<GlobGenerator> generatorMap = new Int2ObjectOpenHashMap<>();

    private GenLayer globSelector;

    public MultiGlobGenerator(GlobType type, Entry... entries) {
        super(type);
        this.entries = entries;
    }

    @Override
    public void initialize(World world, GlobType[] globBuffer, int[] heightBuffer, IBlockState[] coverBuffer, IBlockState[] fillerBuffer) {
        super.initialize(world, globBuffer, heightBuffer, coverBuffer, fillerBuffer);

        for (Entry entry : this.entries) {
            GlobGenerator generator = entry.type.createGenerator();
            this.generatorMap.put(entry.type.getId(), generator);

            GlobType[] newGlobBuffer = ArrayUtils.defaulted(new GlobType[256], GlobType.NO_DATA);
            IBlockState[] newCoverBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            IBlockState[] newFillerBuffer = ArrayUtils.defaulted(new IBlockState[256], Blocks.STONE.getDefaultState());
            generator.initialize(world, newGlobBuffer, heightBuffer, newCoverBuffer, newFillerBuffer);
        }
    }

    @Override
    protected void createLayers() {
        SelectWeightedLayer.Entry[] layerEntries = new SelectWeightedLayer.Entry[this.entries.length];
        for (int i = 0; i < this.entries.length; i++) {
            Entry entry = this.entries[i];
            layerEntries[i] = new SelectWeightedLayer.Entry(entry.type.getId(), entry.weight);
        }

        this.globSelector = this.zoom(new SelectWeightedLayer(1, layerEntries));
        this.globSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);
        GlobGenerator sampledType = this.generatorMap.get(sampledTypes[136]);
        if (sampledType != null) {
            sampledType.decorate(random, x, z);
        }
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);

        for (Map.Entry<Integer, GlobGenerator> entry : this.generatorMap.entrySet()) {
            GlobGenerator generator = entry.getValue();
            this.populateGeneratorBuffer(generator);
            generator.coverDecorate(new GlobFilterPrimer(primer, sampledTypes, entry.getKey()), random, x, z);
        }
    }

    @Override
    public void getCover(Random random, int x, int z) {
        int[] sampledTypes = this.sampleChunk(this.globSelector, x, z);

        for (GlobGenerator generator : this.generatorMap.values()) {
            this.populateGeneratorBuffer(generator);
            generator.getCover(random, x, z);
        }

        this.iterate(point -> {
            int index = point.index;
            GlobGenerator sampledType = this.generatorMap.get(sampledTypes[index]);
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

        for (GlobGenerator generator : this.generatorMap.values()) {
            this.populateGeneratorBuffer(generator);
            generator.getFiller(random, x, z);
        }

        this.iterate(point -> {
            int index = point.index;
            GlobGenerator sampledType = this.generatorMap.get(sampledTypes[index]);
            IBlockState state = sampledType != null ? sampledType.fillerBuffer[index] : null;
            if (state == null) {
                state = Blocks.STONE.getDefaultState();
            }
            this.fillerBuffer[index] = state;
        });
    }

    protected abstract GenLayer zoom(GenLayer layer);

    private void populateGeneratorBuffer(GlobGenerator generator) {
        for (int i = 0; i < this.globBuffer.length; i++) {
            GlobType type = this.globBuffer[i];
            generator.globBuffer[i] = type == this.type ? generator.getType() : GlobType.NO_DATA;
        }
    }

    public static class Entry {
        private final GlobType type;
        private final int weight;

        public Entry(GlobType type, int weight) {
            this.type = type;
            this.weight = weight;
        }
    }
}
