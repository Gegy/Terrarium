package net.gegy1000.terrarium.client.preview;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

@SideOnly(Side.CLIENT)
public class PreviewDummyWorld extends World {
    private static final WorldSettings SETTINGS = new WorldSettings(0, GameType.ADVENTURE, false, false, Terrarium.EARTH_TYPE);

    public PreviewDummyWorld() {
        super(new SaveHandler(), new WorldInfo(SETTINGS, "terrarium_preview"), new WorldProviderSurface(), new Profiler(), false);

        int dimension = this.provider.getDimension();
        this.provider.setWorld(this);
        this.provider.setDimension(dimension);
        this.chunkProvider = this.createChunkProvider();

        this.initCapabilities();
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ChunkCache();
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }

    private static class ChunkCache implements IChunkProvider {
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
