package net.gegy1000.earth.server.world.compatibility;

import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class ColumnCompatibilityWorld extends World implements AutoCloseable {
    private static final WorldGenerator NOOP_GENERATOR = new WorldGenerator() {
        @Override
        public boolean generate(World world, Random rand, BlockPos position) {
            return false;
        }
    };

    private final World parent;
    private final IChunkGenerator generator;

    private ChunkPos columnPos;
    private BlockPos columnDecoratePos;
    private int minY;

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public ColumnCompatibilityWorld(World parent) {
        super(parent.getSaveHandler(), parent.getWorldInfo(), parent.provider, parent.profiler, parent.isRemote);
        this.parent = parent;
        this.generator = getChunkGenerator(parent);

        this.chunkProvider = this.createChunkProvider();
    }

    private static IChunkGenerator getChunkGenerator(World world) {
        IChunkProvider provider = world.getChunkProvider();
        if (provider instanceof ChunkProviderServer) {
            return ((ChunkProviderServer) provider).chunkGenerator;
        }
        return null;
    }

    public void setupAt(ChunkPos columnPos, int minY) {
        this.getChunkProvider().clear();
        this.columnPos = columnPos;
        this.columnDecoratePos = new BlockPos(this.columnPos.getXStart(), 0, this.columnPos.getZStart());
        this.minY = minY;
    }

    public boolean fireDecorateEvent(Random random, DecorateBiomeEvent.Decorate.EventType type) {
        return TerrainGen.decorate(this, random, this.columnPos, type);
    }

    public boolean firePopulateEvent(Random random, PopulateChunkEvent.Populate.EventType type) {
        if (this.generator == null) return false;
        return TerrainGen.populate(this.generator, this, random, this.columnPos.x, this.columnPos.z, false, type);
    }

    public boolean fireOreGenEvent(Random random, boolean pre) {
        if (pre) {
            return MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(this, random, this.columnDecoratePos));
        } else {
            return MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(this, random, this.columnDecoratePos));
        }
    }

    public boolean fireOreGenEvent(Random random, GenerateMinable.EventType type) {
        return TerrainGen.generateOre(this, random, NOOP_GENERATOR, this.columnDecoratePos, type);
    }

    public void runModdedGenerators() {
        if (this.generator == null) return;

        IChunkProvider chunkProvider = this.parent.getChunkProvider();
        ModGeneratorCompatibility.runGenerators(this, this.columnPos, this.generator, chunkProvider);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new ChunkProvider();
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
        return this.parent.isBlockLoaded(pos, allowEmpty);
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        this.mutablePos.setPos(x << 4, this.minY, z << 4);
        return this.parent.isBlockLoaded(this.mutablePos, allowEmpty);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        return this.parent.setBlockState(this.translatePos(pos), newState, flags);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return this.parent.getBlockState(this.translatePos(pos));
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return this.parent.getBiome(this.translatePos(pos));
    }

    @Override
    public int getHeight(int x, int z) {
        return this.untranslateY(this.parent.getHeight(x, z));
    }

    @Override
    public int getHeight() {
        return this.untranslateY(this.parent.getHeight());
    }

    @Override
    public int getActualHeight() {
        return this.untranslateY(this.parent.getActualHeight());
    }

    @Override
    public BlockPos getPrecipitationHeight(BlockPos pos) {
        BlockPos result = this.parent.getPrecipitationHeight(this.translatePos(pos));
        int y = this.untranslateY(result.getY());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    @Override
    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        BlockPos result = this.parent.getTopSolidOrLiquidBlock(this.translatePos(pos));
        int y = this.untranslateY(result.getY());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    @Override
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        this.parent.setLightFor(type, this.translatePos(pos), lightValue);
    }

    @Override
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        return this.parent.getLightFor(type, this.translatePos(pos));
    }

    @Override
    public float getLightBrightness(BlockPos pos) {
        return this.parent.getLightBrightness(this.translatePos(pos));
    }

    @Override
    public int getSeaLevel() {
        return this.untranslateY(this.parent.getSeaLevel());
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        this.translateEntity(entity);
        return this.parent.spawnEntity(entity);
    }

    @Override
    public void onEntityAdded(Entity entity) {
        this.translateEntity(entity);
        this.parent.onEntityAdded(entity);
    }

    @Override
    public void onEntityRemoved(Entity entity) {
        this.parent.onEntityRemoved(entity);
    }

    @Override
    public void addEventListener(IWorldEventListener listener) {
        this.parent.addEventListener(listener);
        super.addEventListener(listener);
    }

    @Override
    public void removeEventListener(IWorldEventListener listener) {
        this.parent.removeEventListener(listener);
        super.removeEventListener(listener);
    }

    @Override
    public List<AxisAlignedBB> getCollisionBoxes(@Nullable Entity entity, AxisAlignedBB aabb) {
        aabb = aabb.offset(0.0, this.minY, 0.0);
        return this.parent.getCollisionBoxes(entity, aabb);
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return (ChunkProvider) super.getChunkProvider();
    }

    @Nullable
    @Override
    public MinecraftServer getMinecraftServer() {
        return this.parent.getMinecraftServer();
    }

    @Override
    public VillageCollection getVillageCollection() {
        return this.parent.getVillageCollection();
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return this.parent.getBiomeProvider();
    }

    @Override
    public BlockPos getSpawnPoint() {
        return this.parent.getSpawnPoint();
    }

    @Override
    public EnumDifficulty getDifficulty() {
        return this.parent.getDifficulty();
    }

    @Override
    public GameRules getGameRules() {
        return this.parent.getGameRules();
    }

    @Override
    public LootTableManager getLootTableManager() {
        return this.parent.getLootTableManager();
    }

    @Override
    public ISaveHandler getSaveHandler() {
        return this.parent.getSaveHandler();
    }

    @Nullable
    @Override
    public MapStorage getMapStorage() {
        return this.parent.getMapStorage();
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return this.parent.getPerWorldStorage();
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

    @Override
    public void close() {
        this.getChunkProvider().clear();
    }

    private BlockPos translatePos(BlockPos pos) {
        this.mutablePos.setPos(pos);
        this.mutablePos.setY(pos.getY() + this.minY);
        return this.mutablePos;
    }

    private int untranslateY(int y) {
        return MathHelper.clamp(y - this.minY, 0, 255);
    }

    private void translateEntity(Entity entity) {
        if (entity.world != this.parent) {
            entity.setWorld(this.parent);
            entity.setPosition(entity.posX, entity.posY + this.minY, entity.posZ);
        }
    }

    class ChunkProvider implements IChunkProvider {
        private final Long2ObjectOpenHashMap<OffsetChunk> chunks = new Long2ObjectOpenHashMap<>(9);

        void clear() {
            this.chunks.clear();
        }

        private OffsetChunk loadChunk(int x, int z) {
            Chunk parentChunk = ColumnCompatibilityWorld.this.parent.getChunk(x, z);
            return new OffsetChunk(parentChunk, ColumnCompatibilityWorld.this.minY);
        }

        @Nullable
        @Override
        public Chunk getLoadedChunk(int x, int z) {
            return this.chunks.get(ChunkPos.asLong(x, z));
        }

        @Override
        public Chunk provideChunk(int x, int z) {
            Chunk loadedChunk = this.getLoadedChunk(x, z);
            if (loadedChunk != null) {
                return loadedChunk;
            }

            OffsetChunk chunk = this.loadChunk(x, z);
            this.chunks.put(ChunkPos.asLong(x, z), chunk);
            return chunk;
        }

        @Override
        public boolean tick() {
            return false;
        }

        @Override
        public boolean isChunkGeneratedAt(int x, int z) {
            return ColumnCompatibilityWorld.this.parent.isChunkGeneratedAt(x, z);
        }

        @Override
        public String makeString() {
            return "ColumnCompatibilityChunkProvider";
        }
    }

    class OffsetChunk extends Chunk {
        private final Chunk parent;

        private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        OffsetChunk(Chunk parent, int minY) {
            super(parent.getWorld(), parent.x, parent.z);
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
            return this.parent.setBlockState(ColumnCompatibilityWorld.this.translatePos(pos), state);
        }

        @Override
        public IBlockState getBlockState(int x, int y, int z) {
            if (y < 0 || y >= 256) {
                return Blocks.AIR.getDefaultState();
            }
            return this.parent.getBlockState(x, y + ColumnCompatibilityWorld.this.minY, z);
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType mode) {
            return this.parent.getTileEntity(ColumnCompatibilityWorld.this.translatePos(pos), mode);
        }

        @Override
        public void addTileEntity(TileEntity tileEntity) {
            tileEntity.setPos(ColumnCompatibilityWorld.this.translatePos(tileEntity.getPos()).toImmutable());
            this.parent.addTileEntity(tileEntity);
        }

        @Override
        public void addTileEntity(BlockPos pos, TileEntity tileEntity) {
            pos = ColumnCompatibilityWorld.this.translatePos(pos).toImmutable();
            this.parent.addTileEntity(pos, tileEntity);
        }

        @Override
        public void removeTileEntity(BlockPos pos) {
            this.parent.removeTileEntity(ColumnCompatibilityWorld.this.translatePos(pos));
        }

        @Override
        public Map<BlockPos, TileEntity> getTileEntityMap() {
            return Collections.emptyMap();
        }

        @Override
        public int getHeightValue(int x, int z) {
            return ColumnCompatibilityWorld.this.untranslateY(this.parent.getHeightValue(x, z));
        }

        @Override
        public BlockPos getPrecipitationHeight(BlockPos pos) {
            BlockPos parent = this.parent.getPrecipitationHeight(pos);
            int y = ColumnCompatibilityWorld.this.untranslateY(parent.getY());
            return new BlockPos(parent.getX(), y, parent.getZ());
        }

        @Override
        public boolean canSeeSky(BlockPos pos) {
            return this.parent.canSeeSky(ColumnCompatibilityWorld.this.translatePos(pos));
        }

        @Override
        public int getLightFor(EnumSkyBlock type, BlockPos pos) {
            return this.parent.getLightFor(type, ColumnCompatibilityWorld.this.translatePos(pos));
        }

        @Override
        public int getLightSubtracted(BlockPos pos, int amount) {
            return this.parent.getLightSubtracted(ColumnCompatibilityWorld.this.translatePos(pos), amount);
        }

        @Override
        public Biome getBiome(BlockPos pos, BiomeProvider provider) {
            return this.parent.getBiome(ColumnCompatibilityWorld.this.translatePos(pos), provider);
        }

        @Override
        public byte[] getBiomeArray() {
            // CubicChunks calls getBiomeArray on initialization before parent is set
            if (this.parent == null) return super.getBiomeArray();

            return this.parent.getBiomeArray();
        }

        @Override
        public void addEntity(Entity entity) {
            ColumnCompatibilityWorld.this.translateEntity(entity);
            this.parent.addEntity(entity);
        }

        @Override
        public void removeEntity(Entity entity) {
            this.parent.removeEntity(entity);
        }

        @Override
        public void removeEntityAtIndex(Entity entity, int index) {
            index -= MathHelper.floor(ColumnCompatibilityWorld.this.minY / 16.0);
            if (index < 0 || index >= 16) {
                return;
            }
            this.parent.removeEntityAtIndex(entity, index);
        }

        @Override
        public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
            aabb = aabb.offset(0.0, ColumnCompatibilityWorld.this.minY, 0.0);
            this.parent.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
        }

        @Override
        public void getEntitiesWithinAABBForEntity(@Nullable Entity entity, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter) {
            aabb = aabb.offset(0.0, ColumnCompatibilityWorld.this.minY, 0.0);
            this.parent.getEntitiesWithinAABBForEntity(entity, aabb, listToFill, filter);
        }

        @Override
        public boolean isEmpty() {
            return this.parent.isEmpty();
        }

        @Override
        public boolean isEmptyBetween(int startY, int endY) {
            return this.parent.isEmptyBetween(startY + ColumnCompatibilityWorld.this.minY, endY + ColumnCompatibilityWorld.this.minY);
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
}
