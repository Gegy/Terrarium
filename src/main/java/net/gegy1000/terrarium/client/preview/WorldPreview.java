package net.gegy1000.terrarium.client.preview;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.gegy1000.terrarium.server.world.generator.EarthChunkGenerator;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

@SideOnly(Side.CLIENT)
public class WorldPreview implements IBlockAccess {
    private static final int VIEW_RANGE = 12;

    private final EarthGenerationSettings settings;
    private final ExecutorService executor;

    private final BufferBuilder[] builders;
    private final BlockingQueue<BufferBuilder> builderQueue;

    private final EarthChunkGenerator generator;

    private final ChunkPos centerPos;
    private final BlockPos centerBlockPos;

    private final Long2ObjectMap<ChunkPrimer> chunkMap = new Long2ObjectOpenHashMap<>(VIEW_RANGE * 2 * VIEW_RANGE * 2);

    private List<PreviewChunk> previewChunks = null;
    private int heightOffset = 64;

    public WorldPreview(EarthGenerationSettings settings, ExecutorService executor, BufferBuilder[] builders) {
        this.settings = settings;
        this.executor = executor;
        this.builders = builders;

        this.builderQueue = new ArrayBlockingQueue<>(builders.length);
        Collections.addAll(this.builderQueue, builders);

        this.generator = new EarthChunkGenerator(new PreviewDummyWorld(), 0, settings.serialize(), true);
        this.centerPos = new ChunkPos(Coordinate.fromLatLng(settings, settings.spawnLatitude, settings.spawnLongitude).toBlockPos(0));
        this.centerBlockPos = new BlockPos(this.centerPos.x << 4, 0, this.centerPos.z << 4);

        this.executor.submit(() -> {
            List<PreviewChunk> chunks = this.generateChunks();
            for (PreviewChunk chunk : chunks) {
                chunk.executeBuild(executor, this::takeBuilder);
            }
            this.previewChunks = chunks;
        });
    }

    public void render() {
        List<PreviewChunk> previewChunks = this.previewChunks;
        if (previewChunks != null) {
            this.performUploads(previewChunks);

            for (PreviewChunk chunk : previewChunks) {
                chunk.render();
            }
        }
    }

    private void performUploads(List<PreviewChunk> previewChunks) {
        long startTime = System.nanoTime();
        for (PreviewChunk chunk : previewChunks) {
            if (chunk.isUploadReady()) {
                this.returnBuilder(chunk.performUpload());
                if (System.nanoTime() - startTime > 3000000) {
                    break;
                }
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
    }

    public BufferBuilder takeBuilder() {
        try {
            return this.builderQueue.take();
        } catch (InterruptedException e) {
            return new BufferBuilder(8);
        }
    }

    public void returnBuilder(BufferBuilder builder) {
        if (builder != null && !this.builderQueue.contains(builder)) {
            this.builderQueue.add(builder);
        }
    }

    private List<PreviewChunk> generateChunks() {
        int totalHeight = 0;

        List<ChunkPos> chunkPositions = new ArrayList<>();
        for (int z = -VIEW_RANGE; z <= VIEW_RANGE; z++) {
            for (int x = -VIEW_RANGE; x <= VIEW_RANGE; x++) {
                ChunkPos pos = new ChunkPos(this.centerPos.x + x, this.centerPos.z + z);
                ChunkPrimer chunk = this.generator.generatePrimer(pos.x, pos.z);
                this.chunkMap.put(ChunkPos.asLong(pos.x, pos.z), chunk);

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
            ChunkPrimer chunk = this.chunkMap.get(ChunkPos.asLong(pos.x, pos.z));
            totalHeight += chunk.findGroundBlockIdx(8, 8) + 16;
            previewChunks.add(new PreviewChunk(chunk, pos, this));
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
        ChunkPrimer chunk = this.chunkMap.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (chunk != null) {
            return chunk.getBlockState(pos.getX() & 15, pos.getY() & 255, pos.getZ() & 15);
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
        return Terrarium.EARTH_TYPE;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return this.getBlockState(pos).isFullCube();
    }

    public BlockPos getCenterBlockPos() {
        return this.centerBlockPos;
    }

    public int getHeightOffset() {
        return this.heightOffset;
    }
}
