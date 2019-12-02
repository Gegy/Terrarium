package net.gegy1000.terrarium.client.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreviewChunkGenerator {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("preview-gen").build());

    private final BlockPos originChunkPos;
    private final ComposableChunkGenerator chunkGenerator;
    private final BiomeProvider biomeProvider;

    private final int chunkRadius;

    private CubeHandler cubeHandler;
    private ColumnHandler columnHandler;

    public PreviewChunkGenerator(BlockPos originChunkPos, ComposableChunkGenerator generator, BiomeProvider biomeProvider, int chunkRadius) {
        this.originChunkPos = originChunkPos;
        this.chunkGenerator = generator;
        this.biomeProvider = biomeProvider;
        this.chunkRadius = chunkRadius;
    }

    public void setCubeHandler(CubeHandler generationHandler) {
        this.cubeHandler = generationHandler;
    }

    public void setColumnHandler(ColumnHandler columnHandler) {
        this.columnHandler = columnHandler;
    }

    public void initiate() {
        this.populateColumns();

        List<CubicPos> cubePositions = this.collectCubePositions();
        for (CubicPos pos : cubePositions) {
            this.executor.submit(() -> {
                int globalChunkX = pos.getX() + this.originChunkPos.getX();
                int globalChunkY = pos.getY() + this.originChunkPos.getY();
                int globalChunkZ = pos.getZ() + this.originChunkPos.getZ();
                CubicPos cubicPos = new CubicPos(globalChunkX, globalChunkY, globalChunkZ);

                PreviewChunkWriter writer = new PreviewChunkWriter(cubicPos);
                this.chunkGenerator.primeChunk(cubicPos, writer);

                CubeHandler cubeHandler = this.cubeHandler;
                if (cubeHandler != null) {
                    cubeHandler.onCubeGenerated(pos, writer.build());
                }
            });
        }

        this.executor.shutdown();
    }

    private List<CubicPos> collectCubePositions() {
        List<CubicPos> cubePositions = new ArrayList<>();
        for (int z = -this.chunkRadius; z <= this.chunkRadius; z++) {
            for (int x = -this.chunkRadius; x <= this.chunkRadius; x++) {
                for (int y = -this.chunkRadius; y <= this.chunkRadius; y++) {
                    cubePositions.add(new CubicPos(x, y, z));
                }
            }
        }

        cubePositions.sort(Comparator.comparing(pos -> {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            return x * x + y * y + z * z;
        }));
        return cubePositions;
    }

    private void populateColumns() {
        ColumnHandler columnHandler = this.columnHandler;
        if (columnHandler == null) return;

        for (int z = -this.chunkRadius; z <= this.chunkRadius; z++) {
            for (int x = -this.chunkRadius; x <= this.chunkRadius; x++) {
                ChunkPos columnPos = new ChunkPos(x + this.originChunkPos.getX(), z + this.originChunkPos.getZ());
                Biome[] biomes = this.biomeProvider.getBiomes(null, columnPos.getXStart(), columnPos.getZStart(), 16, 16);
                columnHandler.onColumnGenerated(new ChunkPos(x, z), new PreviewColumnData(biomes));
            }
        }
    }

    public void close() {
        this.executor.shutdownNow();
    }

    public interface CubeHandler {
        void onCubeGenerated(CubicPos localPos, PreviewChunkData data);
    }

    public interface ColumnHandler {
        void onColumnGenerated(ChunkPos localPos, PreviewColumnData data);
    }
}
