package net.gegy1000.earth.client.gui.preview;

import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.awt.Color;

public final class PreviewMesh {
    private static final Vector3f NORMAL_STORE = new Vector3f();

    private BufferBuilder buffer;
    private int displayList = -1;

    private PreviewMesh(BufferBuilder buffer) {
        this.buffer = buffer;
    }

    public static PreviewMesh build(ShortRaster heightRaster, int granularity) {
        int bufferSize = PreviewMesh.computeBufferSize(heightRaster, granularity);
        BufferBuilder builder = new BufferBuilder(bufferSize);

        PreviewMesh.build(heightRaster, builder, granularity);

        return new PreviewMesh(builder);
    }

    public void upload() {
        if (this.buffer == null) return;

        this.displayList = GLAllocation.generateDisplayLists(1);

        GlStateManager.glNewList(this.displayList, GL11.GL_COMPILE);
        new WorldVertexBufferUploader().draw(this.buffer);
        GlStateManager.glEndList();

        this.buffer = null;
    }

    public void render() {
        if (this.displayList != -1) {
            GlStateManager.callList(this.displayList);
        }
    }

    private static void build(ShortRaster heightRaster, BufferBuilder builder, int granularity) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        short meanHeight = computeMeanHeight(heightRaster);

        int width = heightRaster.getWidth();
        int height = heightRaster.getHeight();
        short[] heightBuffer = heightRaster.getData();

        int strideX = granularity;
        int strideY = width * granularity;

        builder.setTranslation(
                -heightRaster.getWidth() / 2,
                -meanHeight,
                -heightRaster.getHeight() / 2
        );

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

                Vector3f normal = computeNormal(topLeft, topRight, bottomLeft);
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
        builder.finishDrawing();
    }

    private static int computeBufferSize(ShortRaster heightRaster, int granularity) {
        int vertexWidth = TerrariumVertexFormats.POSITION_COLOR_NORMAL.getSize();
        int quadCountX = (heightRaster.getWidth() - granularity) / granularity;
        int quadCountZ = (heightRaster.getHeight() - granularity) / granularity;
        int vertexCount = (quadCountX * quadCountZ) * 4;
        return vertexCount * vertexWidth;
    }

    private static Vector3f computeNormal(int topLeft, int topRight, int bottomLeft) {
        NORMAL_STORE.set(topLeft - topRight, 1, topLeft - bottomLeft);
        NORMAL_STORE.normalize();
        return NORMAL_STORE;
    }

    private static short computeMeanHeight(ShortRaster heightRaster) {
        long total = 0;
        long maxHeight = 0;

        short[] shortData = heightRaster.getData();
        for (short value : shortData) {
            if (value > maxHeight) {
                maxHeight = value;
            }
            total += value;
        }

        long averageHeight = total / shortData.length;
        return (short) ((averageHeight + maxHeight + maxHeight) / 3);
    }

    public void delete() {
        if (this.displayList != -1) {
            GLAllocation.deleteDisplayLists(this.displayList);
            this.displayList = -1;
        }
        this.buffer = null;
    }
}
