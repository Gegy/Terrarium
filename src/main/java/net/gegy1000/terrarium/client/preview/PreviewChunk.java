package net.gegy1000.terrarium.client.preview;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class PreviewChunk {
    private static final EnumFacing[] PREVIEW_FACES = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    private static final Map<Biome, Integer> BIOME_GRASS_COLORS = new HashMap<>();

    private final ChunkPrimer chunk;
    private final Biome[] biomes;

    private final ChunkPos pos;
    private final int globalX;
    private final int globalZ;

    private final IBlockAccess previewAccess;

    private final Object buildLock = new Object();
    private Future<BufferBuilder> builderResult;

    private Geometry geometry;

    static {
        for (Biome biome : Biome.REGISTRY) {
            try {
                float temperature = MathHelper.clamp(biome.getTemperature(BlockPos.ORIGIN), 0.0F, 1.0F);
                float rainfall = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                int color = ColorizerGrass.getGrassColor(temperature, rainfall);
                BIOME_GRASS_COLORS.put(biome, color);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to get {} grass color", biome.getBiomeName(), e);
            }
        }
    }

    public PreviewChunk(ChunkPrimer chunk, Biome[] biomes, ChunkPos pos, IBlockAccess previewAccess) {
        this.chunk = chunk;
        this.biomes = biomes;
        this.pos = pos;
        this.previewAccess = previewAccess;

        this.globalX = this.pos.getXStart();
        this.globalZ = this.pos.getZStart();
    }

    public void submitBuild(ExecutorService executor, Supplier<BufferBuilder> builderSupplier, Consumer<BufferBuilder> builderReturn) {
        synchronized (this.buildLock) {
            this.builderResult = executor.submit(() -> {
                BufferBuilder builder = builderSupplier.get();
                if (builder == null) {
                    return null;
                }
                this.buildBlocks(builder);
                if (builder.getVertexCount() <= 0) {
                    builder.finishDrawing();
                    builderReturn.accept(builder);
                    return null;
                }
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
        } else {
            this.geometry = new EmptyGeometry();
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
                builder.finishDrawing();
            }
            return new EmptyGeometry();
        }
        // TODO: Switch to VBO is GameSettings prefers it
        return this.buildDisplayList(builder);
    }

    private Geometry buildDisplayList(BufferBuilder builder) {
        int id = GLAllocation.generateDisplayLists(1);

        builder.finishDrawing();
        GlStateManager.glNewList(id, GL11.GL_COMPILE);
        new WorldVertexBufferUploader().draw(builder);
        GlStateManager.glEndList();

        return new DisplayListGeometry(id);
    }

    public void render(int cameraX, int cameraZ) {
        Geometry geometry = this.geometry;
        if (geometry != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.globalX - cameraX, 0.0, this.globalZ - cameraZ);
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

        ChunkPrimer chunk = this.chunk;
        IBlockAccess previewAccess = this.previewAccess;
        int globalX = this.globalX;
        int globalZ = this.globalZ;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = this.biomes[x + z * 16];
                for (int y = 0; y < 256; y++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state.getBlock() != Blocks.AIR) {
                        this.buildBlock(builder, chunk, previewAccess, globalX, globalZ, pos, z, x, biome, y, state);
                    }
                }
            }
        }

        builder.setTranslation(0.0, 0.0, 0.0);
    }

    private void buildBlock(BufferBuilder builder, ChunkPrimer chunk, IBlockAccess previewAccess, int globalX, int globalZ, BlockPos.MutableBlockPos pos, int z, int x, Biome biome, int y, IBlockState state) {
        pos.setPos(globalX + x, y, globalZ + z);

        List<EnumFacing> faces = new ArrayList<>(6);
        for (EnumFacing facing : PREVIEW_FACES) {
            BlockPos offset = pos.offset(facing);
            IBlockState neighbourState;
            if (x > 0 && x < 15 && z > 0 && z < 15 && y > 0 && y < 255) {
                neighbourState = chunk.getBlockState(offset.getX() & 15, offset.getY(), offset.getZ() & 15);
            } else {
                neighbourState = previewAccess.getBlockState(offset);
            }
            if (neighbourState.getBlock() == Blocks.AIR) {
                faces.add(facing);
            }
        }

        if (!faces.isEmpty()) {
            this.buildFaces(builder, faces, state, biome, pos, x, z);
        }
    }

    private void buildFaces(BufferBuilder builder, List<EnumFacing> faces, IBlockState state, Biome biome, BlockPos pos, int x, int z) {
        int color = this.getBlockColor(state, biome, pos);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.setTranslation(x, pos.getY(), z);

        for (EnumFacing face : faces) {
            this.buildFace(builder, face, red, green, blue);
        }
    }

    private int getBlockColor(IBlockState state, Biome biome, BlockPos pos) {
        if (!(state.getBlock() instanceof BlockGrass)) {
            return state.getMapColor(this.previewAccess, pos).colorValue;
        } else {
            return BIOME_GRASS_COLORS.getOrDefault(biome, 0);
        }
    }

    private void buildFace(BufferBuilder builder, EnumFacing facing, int red, int green, int blue) {
        switch (facing) {
            case NORTH:
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 0.0F, -1.0F).endVertex();
                break;
            case SOUTH:
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
                break;
            case WEST:
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(-1.0F, 0.0F, 0.0F).endVertex();
                break;
            case EAST:
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
                break;
            case UP:
                builder.pos(0.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(1.0, 1.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(1.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(0.0, 1.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                break;
            case DOWN:
                builder.pos(0.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(1.0, 0.0, 0.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(1.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
                builder.pos(0.0, 0.0, 1.0).color(red, green, blue, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
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
            GLAllocation.deleteDisplayLists(this.id);
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
