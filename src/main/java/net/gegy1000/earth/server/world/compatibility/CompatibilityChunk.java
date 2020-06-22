package net.gegy1000.earth.server.world.compatibility;

import com.google.common.base.Predicate;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class CompatibilityChunk extends Chunk {
    private final ColumnCompatibilityWorld compatibilityWorld;
    private final Chunk parent;

    CompatibilityChunk(ColumnCompatibilityWorld compatibilityWorld, Chunk parent) {
        super(parent.getWorld(), parent.x, parent.z);
        this.compatibilityWorld = compatibilityWorld;
        this.parent = parent;
    }

    @Override
    protected void populate(IChunkGenerator generator) {
    }

    @Override
    public void populate(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
    }

    @Override
    protected void generateHeightMap() {
    }

    @Override
    public void generateSkylightMap() {
    }

    @Override
    public void checkLight() {
    }

    @Override
    public void enqueueRelightChecks() {
    }

    @Override
    public void resetRelightChecks() {
    }

    @Override
    public void onTick(boolean skipRecheckGaps) {
    }

    @Override
    public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
        Terrarium.LOGGER.warn("Unsupported setStorageArrays on compatibility chunk");
    }

    @Override
    public void setBiomeArray(byte[] biomeArray) {
        Terrarium.LOGGER.warn("Unsupported setBiomeArray on compatibility chunk");
    }

    @Override
    public void setHeightMap(int[] newHeightMap) {
        Terrarium.LOGGER.warn("Unsupported setHeightMap on compatibility chunk");
    }

    @Nullable
    @Override
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        return this.parent.setBlockState(this.compatibilityWorld.translatePos(pos), state);
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return this.parent.getBlockState(x, y + this.compatibilityWorld.minY, z);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType mode) {
        return this.parent.getTileEntity(this.compatibilityWorld.translatePos(pos), mode);
    }

    @Override
    public void addTileEntity(TileEntity tileEntity) {
        tileEntity.setPos(this.compatibilityWorld.translatePos(tileEntity.getPos()).toImmutable());
        this.parent.addTileEntity(tileEntity);
    }

    @Override
    public void addTileEntity(BlockPos pos, TileEntity tileEntity) {
        pos = this.compatibilityWorld.translatePos(pos).toImmutable();
        this.parent.addTileEntity(pos, tileEntity);
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        this.parent.removeTileEntity(this.compatibilityWorld.translatePos(pos));
    }

    @Override
    public Map<BlockPos, TileEntity> getTileEntityMap() {
        return Collections.emptyMap();
    }

    @Override
    public int getHeightValue(int x, int z) {
        return this.compatibilityWorld.untranslateY(this.parent.getHeightValue(x, z));
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        BlockPos parent = this.parent.getPrecipitationHeight(pos);
        int y = this.compatibilityWorld.untranslateY(parent.getY());
        return new BlockPos(parent.getX(), y, parent.getZ());
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return this.parent.canSeeSky(this.compatibilityWorld.translatePos(pos));
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return this.parent.getLightFor(type, this.compatibilityWorld.translatePos(pos));
    }

    @Override
    public int getLightSubtracted(BlockPos pos, int amount) {
        return this.parent.getLightSubtracted(this.compatibilityWorld.translatePos(pos), amount);
    }

    @Override
    public Biome getBiome(BlockPos pos, BiomeProvider provider) {
        return this.parent.getBiome(this.compatibilityWorld.translatePos(pos), provider);
    }

    @Override
    public byte[] getBiomeArray() {
        // CubicChunks calls getBiomeArray on initialization before parent is set
        if (this.parent == null) return super.getBiomeArray();

        return this.parent.getBiomeArray();
    }

    @Override
    public void addEntity(Entity entity) {
        this.compatibilityWorld.translateEntity(entity);
        this.parent.addEntity(entity);
    }

    @Override
    public void removeEntity(Entity entity) {
        this.parent.removeEntity(entity);
    }

    @Override
    public void removeEntityAtIndex(Entity entity, int index) {
        index -= MathHelper.floor(this.compatibilityWorld.minY / 16.0);
        if (index < 0 || index >= 16) {
            return;
        }
        this.parent.removeEntityAtIndex(entity, index);
    }

    @Override
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        aabb = aabb.offset(0.0, this.compatibilityWorld.minY, 0.0);
        this.parent.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
    }

    @Override
    public void getEntitiesWithinAABBForEntity(@Nullable Entity entity, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter) {
        aabb = aabb.offset(0.0, this.compatibilityWorld.minY, 0.0);
        this.parent.getEntitiesWithinAABBForEntity(entity, aabb, listToFill, filter);
    }

    @Override
    public boolean isEmpty() {
        return this.parent.isEmpty();
    }

    @Override
    public boolean isEmptyBetween(int startY, int endY) {
        return this.parent.isEmptyBetween(startY + this.compatibilityWorld.minY, endY + this.compatibilityWorld.minY);
    }

    @Override
    public boolean isPopulated() {
        return this.parent.isPopulated();
    }

    @Override
    public boolean isTerrainPopulated() {
        return this.parent.isTerrainPopulated();
    }

    @Override
    public boolean isLightPopulated() {
        return this.parent.isLightPopulated();
    }

    @Override
    public boolean wasTicked() {
        return this.parent.wasTicked();
    }

    @Override
    public void markDirty() {
        this.parent.markDirty();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return this.parent.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return this.parent.getCapability(capability, facing);
    }
}
