package net.gegy1000.earth.client.gui.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import futures.future.Future;
import net.gegy1000.earth.client.terrain.TerrainMesh;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.EarthInitContext;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.gegy1000.earth.server.world.EarthWorldType.SPAWN_LATITUDE;
import static net.gegy1000.earth.server.world.EarthWorldType.SPAWN_LONGITUDE;

@SideOnly(Side.CLIENT)
public class WorldPreview {
    private static final int VIEW_RANGE = 16 * 16;
    private static final int VIEW_SIZE = VIEW_RANGE * 2 + 1;

    private static final int VIEW_GRANULARITY = 2;

    private static final BufferBuilder BUILDER = new BufferBuilder(TerrainMesh.computeBufferSize(VIEW_SIZE, VIEW_SIZE, VIEW_GRANULARITY));

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("terrarium-preview-build")
                    .build()
    );

    private final TerrainMesh mesh;
    private final Vec3d translation;

    private WorldPreview(TerrainMesh mesh, Vec3d translation) {
        this.mesh = mesh;
        this.translation = translation;
    }

    public static Future<WorldPreview> generate(TerrariumWorldType worldType, GenerationSettings settings) {
        TerrariumDataInitializer dataInitializer = worldType.createDataInitializer(settings);
        EarthInitContext ctx = EarthInitContext.from(settings);

        DataGenerator.Builder dataGenerator = DataGenerator.builder();
        dataInitializer.setup(dataGenerator);

        double latitude = settings.getDouble(SPAWN_LATITUDE);
        double longitude = settings.getDouble(SPAWN_LONGITUDE);
        Coordinate spawnCoordinate = new Coordinate(ctx.lngLatCrs, longitude, latitude);

        return generate(dataGenerator.build(), spawnCoordinate.toBlockPos());
    }

    private static Future<WorldPreview> generate(DataGenerator dataGenerator, BlockPos spawnPos) {
        try {
            // make sure this builder is not poisoned
            BUILDER.finishDrawing();
        } catch (IllegalStateException e) {
            // ignore
        }

        int originX = (spawnPos.getX() - VIEW_RANGE);
        int originZ = (spawnPos.getZ() - VIEW_RANGE);

        return sampleData(dataGenerator, originX, originZ, VIEW_SIZE)
                .andThen(data -> Future.spawnBlocking(EXECUTOR, () -> {
                    ShortRaster heightRaster = data.getOrExcept(EarthDataKeys.TERRAIN_HEIGHT);
                    Vec3d translation = new Vec3d(
                            -heightRaster.getWidth() / 2.0,
                            -computeOriginHeight(heightRaster),
                            -heightRaster.getHeight() / 2.0
                    );

                    TerrainMesh mesh = TerrainMesh.build(data, BUILDER, VIEW_GRANULARITY);
                    return new WorldPreview(mesh, translation);
                }));
    }

    private static Future<ColumnData> sampleData(DataGenerator dataGenerator, int originX, int originZ, int size) {
        DataView view = DataView.square(originX, originZ, size);
        return dataGenerator.generateOnly(view, TerrainMesh.REQUIRED_DATA);
    }

    public void upload() {
        this.mesh.upload();
    }

    public void render() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.translation.x, this.translation.y, this.translation.z);
        this.mesh.render();
        GlStateManager.popMatrix();
    }

    public void delete() {
        this.mesh.delete();
    }

    private static short computeOriginHeight(ShortRaster heightRaster) {
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
}
