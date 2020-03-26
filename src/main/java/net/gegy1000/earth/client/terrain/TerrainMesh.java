package net.gegy1000.earth.client.terrain;

import com.google.common.collect.Sets;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.awt.Color;
import java.util.Set;

public final class TerrainMesh {
    public static final Set<DataKey<?>> REQUIRED_DATA = Sets.newHashSet(
            EarthDataKeys.TERRAIN_HEIGHT,
            EarthDataKeys.SLOPE,
            EarthDataKeys.MIN_TEMPERATURE,
            EarthDataKeys.COVER
    );

    private static final Vector3f NORMAL_STORE = new Vector3f();

    private BufferBuilder buffer;
    private int displayList = -1;

    private TerrainMesh(BufferBuilder buffer) {
        this.buffer = buffer;
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

    public static TerrainMesh build(ColumnData data, BufferBuilder builder, int granularity) {
        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        ShortRaster heightRaster = data.getOrDefault(EarthDataKeys.TERRAIN_HEIGHT);
        UByteRaster slopeRaster = data.getOrDefault(EarthDataKeys.SLOPE);
        FloatRaster minTemperatureRaster = data.getOrDefault(EarthDataKeys.MIN_TEMPERATURE);
        EnumRaster<Cover> coverRaster = data.getOrDefault(EarthDataKeys.COVER);

        int width = heightRaster.getWidth();
        int height = heightRaster.getHeight();
        short[] heightBuffer = heightRaster.getData();

        int strideX = granularity;
        int strideY = width * granularity;

        for (int localZ = 0; localZ < height - granularity; localZ += granularity) {
            for (int localX = 0; localX < width - granularity; localX += granularity) {
                int index = localX + localZ * width;

                int topLeft = heightBuffer[index];
                int topRight = heightBuffer[index + strideX];
                int bottomLeft = heightBuffer[index + strideY];
                int bottomRight = heightBuffer[index + strideX + strideY];

                Cover cover = coverRaster.get(localX, localZ);
                int slope = slopeRaster.get(localX, localZ);
                float minTemperature = minTemperatureRaster.get(localX, localZ);
                Color color = TerrainColorizer.get(cover, slope, minTemperature);

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

        builder.finishDrawing();

        return new TerrainMesh(builder);
    }

    public static int computeBufferSize(int width, int height, int granularity) {
        int vertexWidth = TerrariumVertexFormats.POSITION_COLOR_NORMAL.getSize();
        int quadCountX = (width - granularity) / granularity;
        int quadCountZ = (height - granularity) / granularity;
        int vertexCount = (quadCountX * quadCountZ) * 4;
        return vertexCount * vertexWidth;
    }

    private static Vector3f computeNormal(int topLeft, int topRight, int bottomLeft) {
        NORMAL_STORE.set(topLeft - topRight, 1, topLeft - bottomLeft);
        NORMAL_STORE.normalize();
        return NORMAL_STORE;
    }

    public void delete() {
        if (this.displayList != -1) {
            GLAllocation.deleteDisplayLists(this.displayList);
            this.displayList = -1;
        }
        this.buffer = null;
    }
}
