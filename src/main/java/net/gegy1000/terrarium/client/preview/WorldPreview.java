package net.gegy1000.terrarium.client.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class WorldPreview implements IBlockAccess {
    private static final int VIEW_RANGE = 12;
    private static final int VIEW_WIDTH = VIEW_RANGE * 2 + 1;

    private final ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("preview-build-%d").build());

    private final WorldType worldType;
    private final PreviewDummyWorld world;
    private final TerrariumWorldData worldData;

    private final BlockingQueue<BufferBuilder> builderQueue;

    private PreviewChunkGenerator generator;

    private BlockPos centerBlockPos = BlockPos.ORIGIN;

    private final Long2ObjectMap<PreviewColumnData> columnMap = new Long2ObjectOpenHashMap<>(VIEW_WIDTH * VIEW_WIDTH);
    private final Long2ObjectMap<PreviewChunkData> chunkMap = new Long2ObjectOpenHashMap<>(VIEW_WIDTH * VIEW_WIDTH * VIEW_WIDTH);

    private final Set<CubicPos> generatedChunks = new HashSet<>();
    private final List<PreviewChunk> previewChunks = new ArrayList<>(VIEW_WIDTH * VIEW_WIDTH * VIEW_WIDTH);

    private final Object lock = new Object();

    public WorldPreview(WorldType worldType, GenerationSettings settings, BufferBuilder[] builders) {
        this.worldType = worldType;

        this.builderQueue = new ArrayBlockingQueue<>(builders.length);
        Collections.addAll(this.builderQueue, builders);

        TerrariumWorldData.PREVIEW_WORLD.set(true);
        try {
            this.world = new PreviewDummyWorld(this.worldType, settings);
            this.worldData = TerrariumWorldData.get(this.world);
        } finally {
            TerrariumWorldData.PREVIEW_WORLD.set(false);
        }

        this.executor.submit(this::initiateGeneration);
    }

    private void initiateGeneration() {
        BlockPos spawnPosition = this.worldData.getSpawnPosition().toBlockPos();

        int spawnChunkX = spawnPosition.getX() >> 4;
        int spawnChunkZ = spawnPosition.getZ() >> 4;

        try {
            short averageHeight = this.computeAverageHeight(spawnChunkX, spawnChunkZ);

            BlockPos centerChunkPos = new BlockPos(spawnChunkX, averageHeight >> 4, spawnChunkZ);
            this.centerBlockPos = new BlockPos((centerChunkPos.getX() << 4) + 8, averageHeight, (centerChunkPos.getZ() << 4) + 8);

            this.generator = new PreviewChunkGenerator(centerChunkPos, this.world.getCubeGenerator(), VIEW_RANGE);
            this.generator.setCubeHandler(this::handleGeneratedCube);
            this.generator.setColumnHandler(this::handleGeneratedColumn);

            this.generator.initiate();
        } catch (Exception e) {
            Terrarium.LOGGER.error("Failed to generate preview chunks", e);
        }
    }

    private void handleGeneratedCube(CubicPos localPos, PreviewChunkData chunkData) {
        long key = getCubeKey(localPos.getX(), localPos.getY(), localPos.getZ());
        this.chunkMap.put(key, chunkData);

        this.notifyUpdate(localPos);

        for (EnumFacing facing : PreviewChunk.PREVIEW_FACES) {
            CubicPos neighborPos = localPos.offset(facing.getOpposite());
            if (this.containsChunk(neighborPos) && this.chunkMap.containsKey(getCubeKey(neighborPos))) {
                this.notifyUpdate(neighborPos);
            }
        }
    }

    private void handleGeneratedColumn(ChunkPos localPos, PreviewColumnData columnData) {
        this.columnMap.put(ChunkPos.asLong(localPos.x, localPos.z), columnData);
    }

    private void notifyUpdate(CubicPos pos) {
        if (!this.generatedChunks.contains(pos) && this.hasRequiredNeighbors(pos)) {
            this.generatedChunks.add(pos);
            PreviewChunkData data = this.chunkMap.get(getCubeKey(pos));
            PreviewColumnData columnData = this.columnMap.get(ChunkPos.asLong(pos.getX(), pos.getZ()));
            this.submitChunk(pos, data, columnData);
        }
    }

    private void submitChunk(CubicPos pos, PreviewChunkData data, PreviewColumnData columnData) {
        PreviewChunk chunk = new PreviewChunk(data, columnData, pos, this);
        chunk.submitBuild(this.executor, this::takeBuilder, this::returnBuilder);
        synchronized (this.lock) {
            this.previewChunks.add(chunk);
        }
    }

    private boolean hasRequiredNeighbors(CubicPos pos) {
        for (EnumFacing facing : PreviewChunk.PREVIEW_FACES) {
            CubicPos neighborPos = pos.offset(facing);
            if (this.containsChunk(neighborPos) && !this.chunkMap.containsKey(getCubeKey(neighborPos))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsChunk(CubicPos pos) {
        return pos.getX() >= -VIEW_RANGE && pos.getY() >= -VIEW_RANGE && pos.getZ() >= -VIEW_RANGE
                && pos.getX() <= VIEW_RANGE && pos.getY() <= VIEW_RANGE && pos.getZ() <= VIEW_RANGE;
    }

    private short computeAverageHeight(int chunkX, int chunkZ) {
        int viewWidthBlocks = VIEW_WIDTH << 4;

        int originX = (chunkX - VIEW_RANGE) << 4;
        int originZ = (chunkZ - VIEW_RANGE) << 4;

        ShortRasterTile tile = new ShortRasterTile(viewWidthBlocks, viewWidthBlocks);

        RegionGenerationHandler regionHandler = this.worldData.getRegionHandler();
        regionHandler.fillRaster(RegionComponentType.HEIGHT, tile, originX, originZ);

        long total = 0;

        short[] shortData = tile.getShortData();
        for (short value : shortData) {
            total += value;
        }

        return (short) (total / shortData.length);
    }

    public void renderChunks() {
        synchronized (this.lock) {
            this.performUploads(this.previewChunks);
            for (PreviewChunk chunk : this.previewChunks) {
                chunk.render();
            }
        }
    }

    private void performUploads(List<PreviewChunk> previewChunks) {
        long startTime = System.nanoTime();

        Iterator<PreviewChunk> iterator = previewChunks.iterator();
        while (System.nanoTime() - startTime < 5000000 && iterator.hasNext()) {
            PreviewChunk chunk = iterator.next();
            if (chunk.isUploadReady()) {
                this.returnBuilder(chunk.performUpload());
            }
        }
    }

    public void delete() {
        synchronized (this.lock) {
            for (PreviewChunk chunk : this.previewChunks) {
                chunk.cancelGeneration();
                chunk.delete();
            }
        }

        this.generator.close();
        this.executor.shutdownNow();

        this.worldData.getRegionHandler().close();
    }

    public BufferBuilder takeBuilder() {
        try {
            return this.builderQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void returnBuilder(BufferBuilder builder) {
        if (builder != null) {
            if (this.builderQueue.contains(builder)) {
                throw new IllegalArgumentException("Cannot return already returned builder!");
            }
            this.builderQueue.add(builder);
        }
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return lightValue;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkY = pos.getY() >> 4;
        int chunkZ = pos.getZ() >> 4;
        PreviewChunkData chunk = this.chunkMap.get(getCubeKey(chunkX, chunkY, chunkZ));
        if (chunk != null) {
            return chunk.get(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
        }
        return Blocks.STONE.getDefaultState();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.DEFAULT;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return this.worldType;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return this.getBlockState(pos).isFullCube();
    }

    private static long getCubeKey(int x, int y, int z) {
        return ((long) x & 0xFFFFF) << 40 | ((long) y & 0xFFFFF) << 20 | ((long) z & 0xFFFFF);
    }

    private static long getCubeKey(CubicPos pos) {
        return getCubeKey(pos.getX(), pos.getY(), pos.getZ());
    }
}
