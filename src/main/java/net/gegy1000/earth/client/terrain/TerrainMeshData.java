package net.gegy1000.earth.client.terrain;

import com.google.common.collect.Sets;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.terrarium.client.render.TerrariumVertexFormats;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.awt.Color;
import java.util.Set;

public final class TerrainMeshData {
    public static final Set<DataKey<?>> REQUIRED_DATA = Sets.newHashSet(
            EarthData.TERRAIN_HEIGHT,
            EarthData.SLOPE,
            EarthData.MIN_TEMPERATURE,
            EarthData.COVER
    );

    private static final Vector3f NORMAL_STORE = new Vector3f();

    private final BufferBuilder buffer;

    private TerrainMeshData(BufferBuilder buffer) {
        this.buffer = buffer;
    }

    public TerrainMesh upload() {
        return TerrainMesh.upload(this.buffer);
    }

    public static TerrainMeshData build(DataSample data, int granularity, Vec3d translation) {
        BufferBuilder builder = new BufferBuilder(computeBufferSize(data.getView(), granularity));

        builder.begin(GL11.GL_QUADS, TerrariumVertexFormats.POSITION_COLOR_NORMAL);

        ShortRaster heightRaster = data.getOrDefault(EarthData.TERRAIN_HEIGHT);
        UByteRaster slopeRaster = data.getOrDefault(EarthData.SLOPE);
        FloatRaster minTemperatureRaster = data.getOrDefault(EarthData.MIN_TEMPERATURE);
        EnumRaster<Cover> coverRaster = data.getOrDefault(EarthData.COVER);

        int width = heightRaster.width();
        int height = heightRaster.height();
        short[] heightBuffer = heightRaster.asRawData();

        int strideX = granularity;
        int strideY = width * granularity;

        builder.setTranslation(translation.x, translation.y, translation.z);

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

        return new TerrainMeshData(builder);
    }

    private static int computeBufferSize(DataView view, int granularity) {
        int vertexWidth = TerrariumVertexFormats.POSITION_COLOR_NORMAL.getSize();
        int quadCountX = (view.width() - granularity) / granularity;
        int quadCountZ = (view.height() - granularity) / granularity;
        int vertexCount = (quadCountX * quadCountZ) * 4;
        return vertexCount * vertexWidth;
    }

    private static Vector3f computeNormal(int topLeft, int topRight, int bottomLeft) {
        NORMAL_STORE.set(topLeft - topRight, 1, topLeft - bottomLeft);
        NORMAL_STORE.normalize();
        return NORMAL_STORE;
    }
}
