package net.gegy1000.earth.server.world.compatibility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.io.File;

public final class CompatibilitySaveHandler implements ISaveHandler, IPlayerFileData {
    private final WorldServer parent;

    public CompatibilitySaveHandler(WorldServer parent) {
        this.parent = parent;
    }

    @Override
    public WorldInfo loadWorldInfo() {
        return this.parent.getWorldInfo();
    }

    @Override
    public void checkSessionLock() {
    }

    @Override
    public IChunkLoader getChunkLoader(WorldProvider provider) {
        return VoidChunkLoader.INSTANCE;
    }

    @Override
    public void saveWorldInfoWithPlayer(WorldInfo info, NBTTagCompound compound) {
    }

    @Override
    public void saveWorldInfo(WorldInfo info) {
    }

    @Override
    public IPlayerFileData getPlayerNBTManager() {
        return this;
    }

    @Override
    public void flush() {
    }

    @Override
    public File getWorldDirectory() {
        return this.parent.getSaveHandler().getWorldDirectory();
    }

    @Override
    public File getMapFileFromName(String name) {
        return this.parent.getSaveHandler().getMapFileFromName(name);
    }

    @Override
    public TemplateManager getStructureTemplateManager() {
        return this.parent.getStructureTemplateManager();
    }

    @Override
    public void writePlayerData(EntityPlayer player) {
    }

    @Nullable
    @Override
    public NBTTagCompound readPlayerData(EntityPlayer player) {
        return null;
    }

    @Override
    public String[] getAvailablePlayerDat() {
        return new String[0];
    }
}
