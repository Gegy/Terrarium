package net.gegy1000.terrarium.client.preview;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class PreviewHeightMesh {
    private static final Vector3f NORMAL_STORE = new Vector3f();

    private final ShortRaster heightTile;

    private Future<BufferBuilder> heightMeshResult;
    private int heightMeshDisplayList = -1;

    public PreviewHeightMesh(ShortRaster heightTile) {
        this.heightTile = heightTile;
    }

    public void render() {
        if (this.heightMeshDisplayList != -1) {
            GlStateManager.callList(this.heightMeshDisplayList);
        }
    }

    public void performUpload() {
        if (this.heightMeshResult != null && this.heightMeshResult.isDone()) {
            Future<BufferBuilder> result = this.heightMeshResult;
            this.heightMeshResult = null;

            try {
                BufferBuilder builder = result.get();
                this.heightMeshDisplayList = GLAllocation.generateDisplayLists(1);

                builder.finishDrawing();
                GlStateManager.glNewList(this.heightMeshDisplayList, GL11.GL_COMPILE);
                new WorldVertexBufferUploader().draw(builder);
                GlStateManager.glEndList();
            } catch (InterruptedException | ExecutionException e) {
                Terrarium.LOGGER.error("Failed to get height mesh builder result", e);
            }
        }
    }

    public void submitTo(ExecutorService executor, int granularity) {
        this.heightMeshResult = executor.submit(() -> {
            BufferBuilder builder = new BufferBuilder(this.computeBufferSize(granularity));
            this.build(builder, granularity);
            return builder;
        });
    }

    private void build(BufferBuilder builder, int granularity) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        int width = this.heightTile.getWidth();
        int height = this.heightTile.getHeight();
        short[] heightBuffer = this.heightTile.getData();

        int strideX = granularity;
        int strideY = width * granularity;

        for (int localZ = 0; localZ < height - granularity; localZ += granularity) {
            for (int localX = 0; localX < width - granularity; localX += granularity) {
                int index = localX + localZ * width;

                int topLeft = heightBuffer[index];
                int topRight = heightBuffer[index + strideX];
                int bottomLeft = heightBuffer[index + strideY];
                int bottomRight = heightBuffer[index + strideX + strideY];

                Color color = Color.WHITE; // TODO
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                Vector3f normal = this.computeNormal(topLeft, topRight, bottomLeft);
                float nx = normal.x;
                float ny = normal.y;
                float nz = normal.z;

                builder.pos(localX, bottomLeft, localZ + granularity).color(red, green, blue, 255).normal(nx, ny, nz).endVertex();
                builder.pos(localX + granularity, bottomRight, localZ + granularity).color(red, green, blue, 255).normal(nx, ny, nz).endVertex();
                builder.pos(localX + granularity, topRight, localZ).color(red, green, blue, 255).normal(nx, ny, nz).endVertex();
                builder.pos(localX, topLeft, localZ).color(red, green, blue, 255).normal(nx, ny, nz).endVertex();
            }
        }

        builder.setTranslation(0, 0, 0);
    }

    private int computeBufferSize(int granularity) {
        int vertexWidth = TerrariumVertexFormats.POSITION_COLOR_NORMAL.getSize();
        int quadCountX = (this.heightTile.getWidth() - granularity) / granularity;
        int quadCountZ = (this.heightTile.getHeight() - granularity) / granularity;
        int vertexCount = (quadCountX * quadCountZ) * 4;
        return vertexCount * vertexWidth;
    }

    private Vector3f computeNormal(int topLeft, int topRight, int bottomLeft) {
        NORMAL_STORE.set(topLeft - topRight, 1, topLeft - bottomLeft);
        NORMAL_STORE.normalize();
        return NORMAL_STORE;
    }
}
