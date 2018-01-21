package net.gegy1000.terrarium.server.world.generator.debug;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.TerrariumChunkGenerator;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoverDebugChunkGenerator extends TerrariumChunkGenerator {
    public CoverDebugChunkGenerator(World world, long seed) {
        super(world, seed, true);
    }

    @Override
    protected void populateHeightRegion(int[] heightBuffer, int chunkX, int chunkZ) {
        Arrays.fill(heightBuffer, 62);
    }

    @Override
    protected void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                coverBuffer[localX + localZ * 16] = DebugMap.getCover(localX + globalX, localZ + globalZ).getCoverType();
            }
        }
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        super.populate(chunkX, chunkZ);

        int originX = (chunkX << 4) + 8;
        int originZ = (chunkZ << 4) + 8;

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int globalX = originX + x;
                int globalZ = originZ + z;

                String[] sign = DebugMap.getSign(globalX, globalZ);
                if (sign != null) {
                    BlockPos pos = new BlockPos(globalX, 63, globalZ);
                    this.world.setBlockState(pos, Blocks.STANDING_SIGN.getDefaultState());

                    TileEntity entity = this.world.getTileEntity(pos);
                    if (entity instanceof TileEntitySign) {
                        TileEntitySign signEntity = (TileEntitySign) entity;
                        for (int i = 0; i < sign.length; i++) {
                            signEntity.signText[i] = new TextComponentString(sign[i]);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected LatitudinalZone getLatitudinalZone(int x, int z) {
        return DebugMap.getCover(x, z).getZone();
    }

    @Override
    protected int getOceanHeight() {
        return 0;
    }

    @Override
    protected boolean shouldDecorate() {
        return true;
    }

    @Override
    protected boolean shouldFastGenerate() {
        return false;
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return false;
    }
}
