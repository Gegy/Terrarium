package net.gegy1000.terrarium.client.preview;

import net.gegy1000.gengen.core.impl.vanilla.ColumnGeneratorImpl;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import java.io.File;

public class PreviewDummyWorld extends World {
    private final ComposableChunkGenerator generator;

    public PreviewDummyWorld(WorldType worldType, GenerationSettings settings) {
        super(new SaveHandler(), new WorldInfo(createSettings(worldType, settings), "terrarium_preview"), new WorldProviderSurface(), new Profiler(), true);

        int dimension = this.provider.getDimension();
        this.provider.setWorld(this);
        this.provider.setDimension(dimension);

        this.generator = new ComposableChunkGenerator(this);
        this.chunkProvider = this.createChunkProvider();

        this.initCapabilities();
    }

    private static WorldSettings createSettings(WorldType worldType, GenerationSettings settings) {
        WorldSettings worldSettings = new WorldSettings(0, GameType.ADVENTURE, false, false, worldType);
        worldSettings.setGeneratorOptions(settings.serializeString());
        return worldSettings;
    }

    public ComposableChunkGenerator getCubeGenerator() {
        return this.generator;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ChunkCache(new ColumnGeneratorImpl(this, this.generator));
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    private static class ChunkCache extends ChunkProviderServer {
        public ChunkCache(IChunkGenerator generator) {
            super(null, null, generator);
        }

        @Override
        public Chunk getLoadedChunk(int x, int z) {
            return null;
        }

        @Override
        public Chunk provideChunk(int x, int z) {
            return null;
        }

        @Override
        public boolean tick() {
            return false;
        }

        @Override
        public String makeString() {
            return "PreviewDummyWorld.ChunkCache";
        }

        @Override
        public boolean isChunkGeneratedAt(int x, int z) {
            return false;
        }
    }

    private static class SaveHandler implements ISaveHandler {
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() {
        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider provider) {
            return null;
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {
        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            return null;
        }

        @Override
        public void flush() {
        }

        @Override
        public File getWorldDirectory() {
            return null;
        }

        @Override
        public File getMapFileFromName(String mapName) {
            return null;
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            return null;
        }
    }
}
