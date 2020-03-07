package net.gegy1000.terrarium.client.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: use bigger chunks for render
@SideOnly(Side.CLIENT)
public class WorldPreview implements IBlockAccess {
    private static final int VIEW_RANGE = 12;
    private static final int VIEW_SIZE = VIEW_RANGE * 2 + 1;

    private final ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("preview-build-%d").build());

    private final WorldType worldType;
    private final PreviewDummyWorld world;
    private final TerrariumWorld terrarium;

    private final BlockingQueue<BufferBuilder> builderQueue;

    private PreviewChunkGenerator generator;

    private BlockPos centerBlockPos = BlockPos.ORIGIN;

    private final Long2ObjectMap<PreviewColumnData> columnMap = new Long2ObjectOpenHashMap<>(VIEW_SIZE * VIEW_SIZE);
    private final Long2ObjectMap<PreviewChunkData> chunkMap = new Long2ObjectOpenHashMap<>(VIEW_SIZE * VIEW_SIZE * VIEW_SIZE);

    private final Set<CubicPos> generatedChunks = new HashSet<>();
    private final LinkedList<PreviewChunk> buildingChunks = new LinkedList<>();
    private final List<PreviewChunk> renderedChunks = new ArrayList<>(VIEW_SIZE * VIEW_SIZE * VIEW_SIZE);

    private PreviewHeightMesh heightMesh;

    private final Object buildMutex = new Object();
    private final Object renderMutex = new Object();

    public WorldPreview(WorldType worldType, GenerationSettings settings, BufferBuilder[] builders) {
        this.worldType = worldType;

        this.builderQueue = new ArrayBlockingQueue<>(builders.length);
        Collections.addAll(this.builderQueue, builders);

        TerrariumWorld.PREVIEW_WORLD.set(true);
        try {
            this.world = new PreviewDummyWorld(this.worldType, settings);
            this.terrarium = TerrariumWorld.get(this.world);
            if (this.terrarium == null) {
                throw new IllegalStateException("Terrarium World Capability not present on preview world");
            }
        } finally {
            TerrariumWorld.PREVIEW_WORLD.set(false);
        }

        this.executor.submit(this::initiateGeneration);
    }

    private void initiateGeneration() {
        BlockPos spawnPosition = this.terrarium.getSpawnPosition().toBlockPos();

        int spawnChunkX = spawnPosition.getX() >> 4;
        int spawnChunkZ = spawnPosition.getZ() >> 4;

        try {
            int viewSizeBlocks = VIEW_SIZE << 4;

            int originX = (spawnChunkX - VIEW_RANGE) << 4;
            int originZ = (spawnChunkZ - VIEW_RANGE) << 4;

            ShortRaster heightRaster = this.sampleHeightRaster(originX, originZ, viewSizeBlocks);

            this.heightMesh = new PreviewHeightMesh(heightRaster);
            this.heightMesh.submitTo(this.executor, 4);

            short averageHeight = this.computeAverageHeight(heightRaster);

            BlockPos centerChunkPos = new BlockPos(spawnChunkX, averageHeight >> 4, spawnChunkZ);
            this.centerBlockPos = new BlockPos((centerChunkPos.getX() << 4) + 8, averageHeight, (centerChunkPos.getZ() << 4) + 8);

            this.generator = new PreviewChunkGenerator(centerChunkPos, this.world.getCubeGenerator(), this.world.getBiomeProvider(), VIEW_RANGE);
            this.generator.setCubeHandler(this::handleGeneratedCube);
            this.generator.setColumnHandler(this::handleGeneratedColumn);

            this.generator.initiate();
        } catch (CancellationException e) {
            // We can safely ignore
        } catch (Throwable t) {
            Terrarium.LOGGER.error("Failed to generate preview chunks", t);
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
            if (this.executor.isShutdown() || this.executor.isTerminated()) {
                return;
            }
            this.submitChunk(pos, data, columnData);
        }
    }

    private void submitChunk(CubicPos pos, PreviewChunkData data, PreviewColumnData columnData) {
        PreviewChunk chunk = new PreviewChunk(data, columnData, pos, this);
        chunk.submitTo(this.executor, this::takeBuilder, this::returnBuilder);
        synchronized (this.buildMutex) {
            this.buildingChunks.add(chunk);
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

    private ShortRaster sampleHeightRaster(int originX, int originZ, int size) {
        DataGenerator dataGenerator = this.terrarium.getDataGenerator();

        DataView view = DataView.square(originX, originZ, size);
        return dataGenerator.generateOne(view, EarthDataKeys.TERRAIN_HEIGHT)
                .orElseGet(() -> ShortRaster.create(view));
    }

    private short computeAverageHeight(ShortRaster heightTile) {
        long total = 0;
        long maxHeight = 0;

        short[] shortData = heightTile.getData();
        for (short value : shortData) {
            if (value > maxHeight) {
                maxHeight = value;
            }
            total += value;
        }

        long averageHeight = total / shortData.length;
        return (short) ((averageHeight + maxHeight + maxHeight) / 3);
    }

    public void renderHeightMesh() {
        if (this.heightMesh == null) {
            return;
        }

        int offsetHorizontal = VIEW_RANGE << 4;
        int offsetVertical = (this.centerBlockPos.getY() >> 4) << 4;

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(-offsetHorizontal, -offsetVertical, -offsetHorizontal);
        this.heightMesh.render();
        GlStateManager.popMatrix();
    }

    public void renderChunks() {
        this.performUploads();

        synchronized (this.renderMutex) {
            this.renderedChunks.forEach(PreviewChunk::render);
        }
    }

    private PreviewChunk takeNextReadyChunk() {
        synchronized (this.buildMutex) {
            Iterator<PreviewChunk> iterator = this.buildingChunks.iterator();
            while (iterator.hasNext()) {
                PreviewChunk chunk = iterator.next();
                if (chunk.isUploadReady()) {
                    iterator.remove();
                    return chunk;
                }
            }
        }
        return null;
    }

    private void performUploads() {
        if (this.heightMesh != null) {
            this.heightMesh.performUpload();
        }

        if (this.buildingChunks.isEmpty()) {
            return;
        }

        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < (1000 / 30) * 1000000) {
            PreviewChunk chunk = this.takeNextReadyChunk();
            if (chunk == null) {
                break;
            }

            this.returnBuilder(chunk.performUpload());

            synchronized (this.renderMutex) {
                this.renderedChunks.add(chunk);
            }
        }
    }

    public void delete() {
        synchronized (this.buildMutex) {
            for (PreviewChunk chunk : this.buildingChunks) {
                chunk.cancelGeneration();
                chunk.delete();
            }
            this.buildingChunks.clear();
        }

        if (this.generator != null) {
            this.generator.close();
        }

        this.executor.shutdownNow();

        this.terrarium.getDataCache().close();
        DataSourceReader.INSTANCE.cancelLoading();
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
