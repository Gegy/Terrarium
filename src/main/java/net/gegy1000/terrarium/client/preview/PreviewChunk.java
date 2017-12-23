package net.gegy1000.terrarium.client.preview;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class PreviewChunk {
    private static final EnumFacing[] PREVIEW_FACES = new EnumFacing[] { EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };

    private final ChunkPrimer chunk;
    private final ChunkPos pos;

    private final IBlockAccess previewAccess;

    private Future<BufferBuilder> builderResult;

    private Geometry geometry;

    public PreviewChunk(ChunkPrimer chunk, ChunkPos pos, IBlockAccess previewAccess) {
        this.chunk = chunk;
        this.pos = pos;
        this.previewAccess = previewAccess;
    }

    public void executeBuild(ExecutorService executor, Supplier<BufferBuilder> builderSupplier) {
        this.builderResult = executor.submit(() -> {
            BufferBuilder builder = builderSupplier.get();
            this.buildBlocks(builder);
            return builder;
        });
    }

    public BufferBuilder performUpload() {
        Future<BufferBuilder> result = this.builderResult;
        if (result != null && result.isDone()) {
            try {
                BufferBuilder builder = result.get();

                Geometry oldGeometry = this.geometry;
                if (oldGeometry != null) {
                    oldGeometry.delete();
                }

                this.geometry = this.buildGeometry(builder);
                this.builderResult = null;

                return builder;
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to generate preview chunk geometry at {}", this.pos, e);
            }
        }

        return null;
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
            geometry.render();
        }
    }

    public void delete() {
        Geometry geometry = this.geometry;
        if (geometry != null) {
            geometry.delete();
        }
    }

    public void buildBlocks(BufferBuilder builder) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        ChunkPrimer chunk = this.chunk;
        IBlockAccess previewAccess = this.previewAccess;
        int globalX = this.pos.x << 4;
        int globalZ = this.pos.z << 4;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state.getBlock() != Blocks.AIR) {
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
                            this.buildFaces(builder, faces, state, pos);
                        }
                    }
                }
            }
        }

        builder.setTranslation(0.0, 0.0, 0.0);
    }

    private void buildFaces(BufferBuilder builder, List<EnumFacing> faces, IBlockState state, BlockPos pos) {
        int color = state.getMapColor(this.previewAccess, pos).colorValue;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        builder.setTranslation(pos.getX(), pos.getY(), pos.getZ());

        for (EnumFacing face : faces) {
            this.buildFace(builder, face, red, green, blue);
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
