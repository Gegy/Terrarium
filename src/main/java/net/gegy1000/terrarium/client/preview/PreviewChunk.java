package net.gegy1000.terrarium.client.preview;

import net.gegy1000.cubicglue.util.CubicPos;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
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
    public static final EnumFacing[] PREVIEW_FACES = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    private static final Map<Biome, Integer> BIOME_GRASS_COLORS = new HashMap<>();

    private final PreviewChunkData chunkData;
    private final PreviewColumnData column;

    private final CubicPos pos;

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

    public PreviewChunk(PreviewChunkData chunkData, PreviewColumnData column, CubicPos pos, IBlockAccess previewAccess) {
        this.chunkData = chunkData;
        this.column = column;
        this.pos = pos;
        this.previewAccess = previewAccess;
    }

    public void submitBuild(ExecutorService executor, Supplier<BufferBuilder> builderSupplier, Consumer<BufferBuilder> builderReturn) {
        synchronized (this.buildLock) {
            if (this.shouldSkip()) {
                this.geometry = new EmptyGeometry();
                return;
            }

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

    private boolean shouldSkip() {
        return this.chunkData.isEmpty();
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

    public void render() {
        Geometry geometry = this.geometry;

        if (geometry != null) {
            int offsetX = this.pos.getMinX();
            int offsetY = this.pos.getMinY();
            int offsetZ = this.pos.getMinZ();

            GlStateManager.pushMatrix();
            GlStateManager.translate(offsetX, offsetY, offsetZ);
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

        int globalX = this.pos.getMinX();
        int globalY = this.pos.getMinY();
        int globalZ = this.pos.getMinZ();

        if (!this.chunkData.isFilled()) {
            this.buildAllBlocks(builder, globalX, globalY, globalZ);
        } else {
            this.buildEdgeBlocks(builder, globalX, globalY, globalZ);
        }

        builder.setTranslation(0.0, 0.0, 0.0);
    }

    private void buildAllBlocks(BufferBuilder builder, int globalX, int globalY, int globalZ) {
        PreviewChunkData chunkData = this.chunkData;
        Biome[] biomes = this.column.getBiomes();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = biomes[x + z * 16];
                for (int y = 0; y < 16; y++) {
                    IBlockState state = chunkData.get(x, y, z);
                    if (state.getBlock() != Blocks.AIR) {
                        pos.setPos(globalX + x, globalY + y, globalZ + z);
                        this.buildBlock(builder, pos, x, y, z, biome, state);
                    }
                }
            }
        }
    }

    private void buildEdgeBlocks(BufferBuilder builder, int globalX, int globalY, int globalZ) {
        PreviewChunkData chunkData = this.chunkData;
        Biome[] biomes = this.column.getBiomes();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                Biome biome = biomes[x + z * 16];
                for (int y = 0; y < 16; y++) {
                    if (x <= 0 || x >= 15 || z <= 0 || z >= 15 || y <= 0 || y >= 15) {
                        IBlockState state = chunkData.get(x, y, z);
                        if (state.getBlock() != Blocks.AIR) {
                            pos.setPos(globalX + x, globalY + y, globalZ + z);
                            this.buildBlock(builder, pos, x, y, z, biome, state);
                        }
                    }
                }
            }
        }
    }

    private void buildBlock(BufferBuilder builder, BlockPos globalPos, int x, int y, int z, Biome biome, IBlockState state) {
        PreviewChunkData chunkData = this.chunkData;
        IBlockAccess previewAccess = this.previewAccess;

        List<EnumFacing> faces = new ArrayList<>(6);
        for (EnumFacing facing : PREVIEW_FACES) {
            BlockPos offset = globalPos.offset(facing);
            IBlockState neighbourState;
            if (x > 0 && x < 15 && z > 0 && z < 15 && y > 0 && y < 15) {
                neighbourState = chunkData.get(offset.getX() & 15, offset.getY() & 15, offset.getZ() & 15);
            } else {
                neighbourState = previewAccess.getBlockState(offset);
            }
            if (neighbourState.getBlock() == Blocks.AIR) {
                faces.add(facing);
            }
        }

        if (!faces.isEmpty()) {
            this.buildFaces(builder, faces, state, biome, globalPos, x, y, z);
        }
    }

    private void buildFaces(BufferBuilder builder, List<EnumFacing> faces, IBlockState state, Biome biome, BlockPos globalPos, int x, int y, int z) {
        int color = this.getBlockColor(state, biome, globalPos);
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.setTranslation(x, y, z);

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
