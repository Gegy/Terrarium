package net.gegy1000.terrarium.client.preview;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.block.GrassColorHandler;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class PreviewChunk {
    private static final Direction[] PREVIEW_FACES = new Direction[] { Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
    private static final Map<Biome, Integer> BIOME_GRASS_COLORS = new HashMap<>();

    private final Chunk chunk;

    private final ChunkPos pos;
    private final int globalX;
    private final int globalZ;

    private final BlockView previewAccess;

    private final Object buildLock = new Object();
    private Future<BufferBuilder> builderResult;

    private Geometry geometry;

    static {
        for (Biome biome : Biome.BIOMES) {
            try {
                float temperature = MathHelper.clamp(biome.getTemperature(BlockPos.ORIGIN), 0.0F, 1.0F);
                float rainfall = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                int color = GrassColorHandler.getColor(temperature, rainfall);
                BIOME_GRASS_COLORS.put(biome, color);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get {} grass color", biome.getTextComponent().getFormattedText(), e);
            }
        }
    }

    public PreviewChunk(Chunk chunk, ChunkPos pos, BlockView previewAccess) {
        this.chunk = chunk;
        this.pos = pos;
        this.previewAccess = previewAccess;

        this.globalX = this.pos.getXStart();
        this.globalZ = this.pos.getZStart();
    }

    public void executeBuild(ExecutorService executor, Supplier<BufferBuilder> builderSupplier) {
        synchronized (this.buildLock) {
            this.builderResult = executor.submit(() -> {
                BufferBuilder builder = builderSupplier.get();
                if (builder == null) {
                    return null;
                }
                this.buildBlocks(builder);
                return builder;
            });
        }
    }

    public BufferBuilder performUpload() {
        BufferBuilder completedBuilder = this.getCompletedBuilder();
        if (completedBuilder != null) {
            Geometry oldGeometry = this.geometry;
            if (oldGeometry != null) {
                oldGeometry.delete();
            }
            this.geometry = this.buildGeometry(completedBuilder);
        }
        return completedBuilder;
    }

    public BufferBuilder getCompletedBuilder() {
        Future<BufferBuilder> result;
        synchronized (this.buildLock) {
            result = this.builderResult;
        }

        if (result != null && result.isDone()) {
            try {
                BufferBuilder builder = result.get();
                synchronized (this.buildLock) {
                    this.builderResult = null;
                }

                return builder;
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to generate preview chunk geometry at {}", this.pos, e);
            }
        }

        return null;
    }

    public boolean isUploadReady() {
        synchronized (this.buildLock) {
            Future<BufferBuilder> result = this.builderResult;
            return result != null && result.isDone();
        }
    }

    private Geometry buildGeometry(BufferBuilder builder) {
        if (builder == null || builder.getVertexCount() == 0) {
            if (builder != null) {
                builder.end();
            }
            return new EmptyGeometry();
        }
        // TODO: Switch to VBO is GameSettings prefers it
        return this.buildDisplayList(builder);
    }

    private Geometry buildDisplayList(BufferBuilder builder) {
        int id = GlAllocationUtils.genLists(1);

        builder.end();
        GlStateManager.newList(id, GL11.GL_COMPILE);
        new BufferRenderer().draw(builder);
        GlStateManager.endList();

        return new DisplayListGeometry(id);
    }

    public void render(int cameraX, int cameraZ) {
        Geometry geometry = this.geometry;
        if (geometry != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(this.globalX - cameraX, 0.0F, this.globalZ - cameraZ);
            geometry.render();
            GlStateManager.popMatrix();
        }
    }

    public void delete() {
        Geometry geometry = this.geometry;
        if (geometry != null) {
            geometry.delete();
        }
    }

    public void cancelGeneration() {
        synchronized (this.buildLock) {
            Future<BufferBuilder> builderResult = this.builderResult;
            if (builderResult != null && !builderResult.isDone()) {
                builderResult.cancel(true);
            }
        }
    }

    public void buildBlocks(BufferBuilder builder) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        Chunk chunk = this.chunk;
        BlockView previewAccess = this.previewAccess;
        int globalX = this.globalX;
        int globalZ = this.globalZ;

        Biome[] biomes = chunk.getBiomeArray();

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = biomes[x + z * 16];
                for (int y = 0; y < 256; y++) {
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (state.getBlock() != Blocks.AIR) {
                        this.buildBlock(builder, chunk, previewAccess, globalX, globalZ, pos, z, x, biome, y, state);
                    }
                }
            }
        }

        builder.setOffset(0.0, 0.0, 0.0);
    }

    private void buildBlock(BufferBuilder builder, Chunk chunk, BlockView previewAccess, int globalX, int globalZ, BlockPos.Mutable pos, int z, int x, Biome biome, int y, BlockState state) {
        pos.set(globalX + x, y, globalZ + z);

        List<Direction> faces = new ArrayList<>(6);
        for (Direction direction : PREVIEW_FACES) {
            BlockPos offset = pos.offset(direction);
            BlockState neighbourState;
            if (x > 0 && x < 15 && z > 0 && z < 15 && y > 0 && y < 255) {
                neighbourState = chunk.getBlockState(offset);
            } else {
                neighbourState = previewAccess.getBlockState(offset);
            }
            if (neighbourState.getBlock() == Blocks.AIR) {
                faces.add(direction);
            }
        }

        if (!faces.isEmpty()) {
            this.buildFaces(builder, faces, state, biome, pos, x, z);
        }
    }

    private void buildFaces(BufferBuilder builder, List<Direction> faces, BlockState state, Biome biome, BlockPos pos, int x, int z) {
        int color = this.getBlockColor(state, biome, pos);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.setOffset(x, pos.getY(), z);

        for (Direction face : faces) {
            this.buildFace(builder, face, red, green, blue);
        }
    }

    private int getBlockColor(BlockState state, Biome biome, BlockPos pos) {
        if (!(state.getBlock() instanceof GrassBlock)) {
            return state.getMaterialColor(this.previewAccess, pos).color;
        } else {
            return BIOME_GRASS_COLORS.getOrDefault(biome, 0);
        }
    }

    private void buildFace(BufferBuilder builder, Direction direction, int red, int green, int blue) {
        switch (direction) {
            case NORTH:
                builder.vertex(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).next();
                builder.vertex(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).next();
                builder.vertex(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).next();
                builder.vertex(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).next();
                break;
            case SOUTH:
                builder.vertex(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).next();
                builder.vertex(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).next();
                builder.vertex(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).next();
                builder.vertex(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).next();
                break;
            case WEST:
                builder.vertex(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).next();
                builder.vertex(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).next();
                builder.vertex(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).next();
                builder.vertex(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).next();
                break;
            case EAST:
                builder.vertex(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).next();
                builder.vertex(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).next();
                builder.vertex(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).next();
                builder.vertex(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).next();
                break;
            case UP:
                builder.vertex(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                break;
            case DOWN:
                builder.vertex(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                builder.vertex(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).next();
                break;
        }
    }

    private interface Geometry {
        void render();

        void delete();
    }

    private class DisplayListGeometry implements Geometry {
        private final int id;

        private DisplayListGeometry(int id) {
            this.id = id;
        }

        @Override
        public void render() {
            GlStateManager.callList(this.id);
        }

        @Override
        public void delete() {
            GlAllocationUtils.deleteSingletonList(this.id);
        }
    }

    private class EmptyGeometry implements Geometry {
        @Override
        public void render() {
        }

        @Override
        public void delete() {
        }
    }
}
