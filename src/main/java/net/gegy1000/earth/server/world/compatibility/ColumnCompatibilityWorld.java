package net.gegy1000.earth.server.world.compatibility;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.earth.server.world.compatibility.hooks.DimensionManagerHooks;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.TemplateManager;
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
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class ColumnCompatibilityWorld extends WorldServer implements AutoCloseable {
    private static final WorldGenerator NOOP_GENERATOR = new WorldGenerator() {
        @Override
        public boolean generate(World world, Random rand, BlockPos position) {
            return false;
        }
    };

    static final ThreadLocal<WorldServer> CONSTRUCTION_PARENT = new ThreadLocal<>();

    final WorldServer parent;
    final IChunkGenerator generator;

    ChunkPos columnPos;
    BlockPos columnDecoratePos;
    int minY;

    final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    private final Long2ObjectMap<TileEntity> accessedBlockEntities = new Long2ObjectOpenHashMap<>();

    private boolean tickWarning;

    private ColumnCompatibilityWorld(WorldServer parent) {
        super(parent.getMinecraftServer(), new CompatibilitySaveHandler(parent), parent.getWorldInfo(), parent.provider.getDimension(), parent.profiler);
        this.parent = parent;
        this.generator = getChunkGenerator(parent);

        this.chunkProvider = this.createChunkProvider();

        this.init();
    }

    public static ColumnCompatibilityWorld create(WorldServer parent) {
        try {
            CONSTRUCTION_PARENT.set(parent);
            return new ColumnCompatibilityWorld(parent);
        } finally {
            DimensionManagerHooks.restoreWorldMapping(parent);
            CONSTRUCTION_PARENT.remove();
        }
    }

    private static IChunkGenerator getChunkGenerator(World world) {
        IChunkProvider provider = world.getChunkProvider();
        if (provider instanceof ChunkProviderServer) {
            return ((ChunkProviderServer) provider).chunkGenerator;
        }
        return null;
    }

    @Override
    public void tick() {
        DimensionManagerHooks.restoreWorldMapping(this.parent);

        if (this.tickWarning) return;
        this.tickWarning = true;

        Terrarium.LOGGER.error("Tried to tick Terrarium compatibility world implementation! Trying to reset Forge dimension list...", new IllegalAccessException());
    }

    public void setupAt(ChunkPos columnPos, int minY) {
        this.close();

        this.columnPos = columnPos;
        this.columnDecoratePos = new BlockPos(this.columnPos.getXStart(), 0, this.columnPos.getZStart());
        this.minY = minY;
    }

    public int getMinY() {
        return this.minY;
    }

    public boolean fireDecorateEvent(Random random, DecorateBiomeEvent.Decorate.EventType type) {
        return TerrainGen.decorate(this, random, this.columnPos, type);
    }

    public boolean firePopulateEvent(Random random, boolean pre) {
        if (this.generator == null) return false;
        if (pre) {
            return MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this.generator, this, random, this.columnPos.x, this.columnPos.z, false));
        } else {
            return MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this.generator, this, random, this.columnPos.x, this.columnPos.z, false));
        }
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
    public World init() {
        this.mapStorage = this.parent.getMapStorage();
        this.villageCollection = this.parent.getVillageCollection();
        this.perWorldStorage = this.parent.getPerWorldStorage();
        this.worldScoreboard = this.parent.getScoreboard();
        this.lootTable = this.parent.getLootTableManager();
        this.functionManager = this.parent.getFunctionManager();
        this.advancementManager = this.parent.getAdvancementManager();
        return this;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return new CompatibilityChunkProvider(this);
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
    public void setTileEntity(BlockPos pos, @Nullable TileEntity entity) {
        this.accessedBlockEntities.remove(pos.toLong());

        BlockPos worldPos = this.translatePos(pos);
        if (entity != null) {
            entity.setWorld(this.parent);
            entity.setPos(worldPos);
            this.parent.setTileEntity(worldPos, entity);
        } else {
            this.parent.setTileEntity(worldPos, null);
        }
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        BlockPos worldPos = this.translatePos(pos);
        TileEntity worldEntity = this.parent.getTileEntity(worldPos);
        if (worldEntity == null) {
            return null;
        }

        TileEntity entity = this.accessedBlockEntities.get(pos.toLong());
        if (entity == null) {
            entity = this.createCompatibilityBlockEntity(pos, worldEntity);
            this.accessedBlockEntities.put(pos.toLong(), entity);
        }

        return entity;
    }

    private TileEntity createCompatibilityBlockEntity(BlockPos pos, TileEntity worldEntity) {
        NBTTagCompound nbt = worldEntity.serializeNBT();
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
        return TileEntity.create(this, nbt);
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

    @Nullable
    @Override
    public Entity getEntityByID(int id) {
        return this.parent.getEntityByID(id);
    }

    @Nullable
    @Override
    public Entity getEntityFromUuid(UUID uuid) {
        return this.parent.getEntityFromUuid(uuid);
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

    @Nullable
    @Override
    public BlockPos getSpawnCoordinate() {
        BlockPos parent = this.parent.getSpawnCoordinate();
        if (parent == null) return null;

        return new BlockPos(parent.getX(), this.untranslateY(parent.getY()), parent.getZ());
    }

    @Nullable
    @Override
    public BlockPos findNearestStructure(String name, BlockPos origin, boolean findUnexplored) {
        BlockPos parent = this.parent.findNearestStructure(name, origin, findUnexplored);
        if (parent == null) return null;

        return new BlockPos(parent.getX(), this.untranslateY(parent.getY()), parent.getZ());
    }

    @Override
    public CompatibilityChunkProvider getChunkProvider() {
        return (CompatibilityChunkProvider) super.getChunkProvider();
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
    public TemplateManager getStructureTemplateManager() {
        return this.parent.getStructureTemplateManager();
    }

    @Override
    public EntityTracker getEntityTracker() {
        return this.parent.getEntityTracker();
    }

    @Override
    public MapStorage getPerWorldStorage() {
        return this.parent.getPerWorldStorage();
    }

    @Override
    public PlayerChunkMap getPlayerChunkMap() {
        return this.parent.getPlayerChunkMap();
    }

    @Override
    public Teleporter getDefaultTeleporter() {
        return this.parent.getDefaultTeleporter();
    }

    @Override
    public AdvancementManager getAdvancementManager() {
        return this.parent.getAdvancementManager();
    }

    @Override
    public FunctionManager getFunctionManager() {
        return this.parent.getFunctionManager();
    }

    @Override
    public File getChunkSaveLocation() {
        if (this.parent == null) {
            // this is called during WorldServer.<init>, so we have to use this hacky solution
            return CONSTRUCTION_PARENT.get().getChunkSaveLocation();
        }
        return this.parent.getChunkSaveLocation();
    }

    @Override
    public void saveAllChunks(boolean all, @Nullable IProgressUpdate callback) {
    }

    @Override
    public void flushToDisk() {
    }

    @Override
    public void flush() {
    }

    @Override
    protected void saveLevel() {
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

        for (TileEntity entity : this.accessedBlockEntities.values()) {
            this.updateAccessedBlockEntity(entity);
        }

        this.accessedBlockEntities.clear();
    }

    private void updateAccessedBlockEntity(TileEntity entity) {
        BlockPos worldPos = this.translatePos(entity.getPos());
        entity.setWorld(this.parent);
        entity.setPos(worldPos);
        this.parent.setTileEntity(worldPos, entity);
    }

    BlockPos translatePos(BlockPos pos) {
        this.mutablePos.setPos(pos);
        this.mutablePos.setY(pos.getY() + this.minY);
        return this.mutablePos;
    }

    int untranslateY(int y) {
        return MathHelper.clamp(y - this.minY, 0, 255);
    }

    void translateEntity(Entity entity) {
        if (entity.world != this.parent) {
            entity.setWorld(this.parent);
            entity.setPosition(entity.posX, entity.posY + this.minY, entity.posZ);
        }
    }
}
