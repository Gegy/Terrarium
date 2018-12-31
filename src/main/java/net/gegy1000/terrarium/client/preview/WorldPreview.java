package net.gegy1000.terrarium.client.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelGeneratorType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class WorldPreview implements ExtendedBlockView {
    private static final int VIEW_RANGE = 12;

    private final ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("terrarium-preview-%d").build());

    private final LevelGeneratorType generatorType;
    private final PreviewDummyWorld world;

    private final BlockingQueue<BufferBuilder> builderQueue;

    private final ChunkGenerator<?> chunkGenerator;

    private final ChunkPos centerPos;
    private final BlockPos centerBlockPos;

    private final Long2ObjectMap<Chunk> chunkMap = new Long2ObjectOpenHashMap<>(VIEW_RANGE * 2 * VIEW_RANGE * 2);

    private final TerrariumGeneratorConfig config;

    private List<PreviewChunk> previewChunks = null;
    private int heightOffset = 64;

    public WorldPreview(LevelGeneratorType generatorType, GenerationSettings settings, BufferBuilder[] builders) {
        this.generatorType = generatorType;

        this.builderQueue = new ArrayBlockingQueue<>(builders.length);
        Collections.addAll(this.builderQueue, builders);

        this.world = new PreviewDummyWorld(this.generatorType, settings);
        this.config = TerrariumGeneratorConfig.get(this.world);
        if (this.config == null) {
            throw new IllegalStateException("Failed to get terrarium config from preview world");
        }

        Coordinate spawnPosition = this.config.getSpawnPosition();
        this.chunkGenerator = this.world.getGenerator();
        if (spawnPosition != null) {
            this.centerPos = new ChunkPos(spawnPosition.toBlockPos());
        } else {
            this.centerPos = new ChunkPos(0, 0);
        }

        this.centerBlockPos = new BlockPos(this.centerPos.x << 4, 0, this.centerPos.z << 4);

        this.executor.submit(() -> {
            try {
                List<PreviewChunk> chunks = this.generateChunks();
                for (PreviewChunk chunk : chunks) {
                    if (this.executor.isTerminated() || this.executor.isShutdown()) {
                        break;
                    }
                    chunk.executeBuild(this.executor, this::takeBuilder);
                }
                this.previewChunks = chunks;
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to generate preview chunks", e);
            }
        });
    }

    public void renderChunks() {
        List<PreviewChunk> previewChunks = this.previewChunks;
        if (previewChunks != null) {
            this.performUploads(previewChunks);

            for (PreviewChunk chunk : previewChunks) {
                chunk.render(this.centerBlockPos.getX(), this.centerBlockPos.getZ());
            }
        }
    }

    private void performUploads(List<PreviewChunk> previewChunks) {
        long startTime = System.nanoTime();

        Iterator<PreviewChunk> iterator = previewChunks.iterator();
        while (System.nanoTime() - startTime < 3000000 && iterator.hasNext()) {
            PreviewChunk chunk = iterator.next();
            if (chunk.isUploadReady()) {
                this.returnBuilder(chunk.performUpload());
            }
        }
    }

    public void delete() {
        List<PreviewChunk> previewChunks = this.previewChunks;
        if (previewChunks != null) {
            for (PreviewChunk chunk : previewChunks) {
                chunk.cancelGeneration();
                chunk.delete();
            }
        }
        this.executor.shutdown();
        this.config.getRegionHandler().close();
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

    private List<PreviewChunk> generateChunks() {
        int totalHeight = 0;

        List<ChunkPos> chunkPositions = new ArrayList<>();
        for (int z = -VIEW_RANGE; z <= VIEW_RANGE; z++) {
            for (int x = -VIEW_RANGE; x <= VIEW_RANGE; x++) {
                ChunkPos pos = new ChunkPos(this.centerPos.x + x, this.centerPos.z + z);
                this.chunkMap.put(ChunkPos.toLong(pos.x, pos.z), this.generateChunk(pos));

                chunkPositions.add(pos);
            }
        }

        chunkPositions.sort(Comparator.comparing(pos -> {
            int deltaX = pos.x - this.centerPos.x;
            int deltaZ = pos.z - this.centerPos.z;
            return deltaX * deltaX + deltaZ * deltaZ;
        }));

        List<PreviewChunk> previewChunks = new ArrayList<>();
        for (ChunkPos pos : chunkPositions) {
            Chunk chunk = this.chunkMap.get(ChunkPos.toLong(pos.x, pos.z));
            totalHeight += chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, 8, 8) + 16;
            previewChunks.add(new PreviewChunk(chunk, pos, this));
        }

        this.heightOffset = totalHeight / previewChunks.size();

        return previewChunks;
    }

    private Chunk generateChunk(ChunkPos pos) {
        Chunk chunk = new WorldChunk(this.world, pos.x, pos.z, new Biome[256]);
        this.chunkGenerator.populateBiomes(chunk);
        this.chunkGenerator.populateNoise(this.world, chunk);
        this.chunkGenerator.buildSurface(chunk);
        return chunk;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.getY() > 255 || pos.getY() < 0) {
            return Blocks.AIR.getDefaultState();
        }
        Chunk chunk = this.chunkMap.get(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (chunk != null) {
            return chunk.getBlockState(pos);
        }
        return Blocks.STONE.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (pos.getY() <= 255 && pos.getY() >= 0) {
            Chunk chunk = this.chunkMap.get(ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4));
            if (chunk != null) {
                return chunk.getFluidState(pos);
            }
        }
        return Fluids.EMPTY.getDefaultState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.DEFAULT;
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return 15;
    }

    public int getHeightOffset() {
        return this.heightOffset;
    }
}
