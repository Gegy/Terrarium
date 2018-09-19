package net.gegy1000.terrarium.client.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
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
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class WorldPreview implements IBlockAccess {
    private static final int VIEW_RANGE = 12;

    private final ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("terrarium-preview-%d").build());

    private final WorldType worldType;

    private final BlockingQueue<BufferBuilder> builderQueue;

    private final ComposableChunkGenerator chunkGenerator;

    private final ChunkPos centerPos;
    private final BlockPos centerBlockPos;

    private final Long2ObjectMap<ChunkData> chunkMap = new Long2ObjectOpenHashMap<>(VIEW_RANGE * 2 * VIEW_RANGE * 2);

    private final TerrariumWorldData worldData;

    private List<PreviewChunk> previewChunks = null;
    private int heightOffset = 64;

    public WorldPreview(WorldType worldType, GenerationSettings settings, BufferBuilder[] builders) {
        this.worldType = worldType;

        this.builderQueue = new ArrayBlockingQueue<>(builders.length);
        Collections.addAll(this.builderQueue, builders);

        PreviewDummyWorld world;

        TerrariumWorldData.PREVIEW_WORLD.set(true);
        try {
            world = new PreviewDummyWorld(this.worldType, settings);
            this.worldData = TerrariumWorldData.get(world);
        } finally {
            TerrariumWorldData.PREVIEW_WORLD.set(false);
        }

        this.chunkGenerator = world.getGenerator();
        Coordinate spawnPosition = this.worldData.getSpawnPosition();
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

    private List<PreviewChunk> generateChunks() {
        int totalHeight = 0;

        List<ChunkPos> chunkPositions = new ArrayList<>();
        for (int z = -VIEW_RANGE; z <= VIEW_RANGE; z++) {
            for (int x = -VIEW_RANGE; x <= VIEW_RANGE; x++) {
                ChunkPos pos = new ChunkPos(this.centerPos.x + x, this.centerPos.z + z);

                ChunkPrimer chunk = this.chunkGenerator.generatePrimer(pos.x, pos.z);
                Biome[] biomes = Arrays.copyOf(this.chunkGenerator.provideBiomes(pos.x, pos.z), 256);
                this.chunkMap.put(ChunkPos.asLong(pos.x, pos.z), new ChunkData(chunk, biomes));

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
            ChunkData chunk = this.chunkMap.get(ChunkPos.asLong(pos.x, pos.z));
            totalHeight += chunk.primer.findGroundBlockIdx(8, 8) + 16;
            previewChunks.add(new PreviewChunk(chunk.primer, chunk.biomes, pos, this));
        }

        this.heightOffset = totalHeight / previewChunks.size();

        return previewChunks;
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
        if (pos.getY() > 255 || pos.getY() < 0) {
            return Blocks.AIR.getDefaultState();
        }
        ChunkData chunk = this.chunkMap.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (chunk != null) {
            return chunk.primer.getBlockState(pos.getX() & 15, pos.getY() & 255, pos.getZ() & 15);
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

    public int getHeightOffset() {
        return this.heightOffset;
    }

    private class ChunkData {
        private final ChunkPrimer primer;
        private final Biome[] biomes;

        private ChunkData(ChunkPrimer primer, Biome[] biomes) {
            this.primer = primer;
            this.biomes = biomes;
        }
    }
}
