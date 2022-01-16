package net.gegy1000.earth.client.gui.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.earth.client.terrain.TerrainMeshData;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.EarthInitContext;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.gegy1000.earth.server.world.EarthProperties.SPAWN_LATITUDE;
import static net.gegy1000.earth.server.world.EarthProperties.SPAWN_LONGITUDE;

public final class PreviewMeshBuilder {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("terrarium-preview-build")
                    .build()
    );

    public static Future<TerrainMeshData> build(TerrariumWorldType worldType, GenerationSettings settings) {
        TerrariumDataInitializer dataInitializer = worldType.createDataInitializer(settings);
        EarthInitContext ctx = EarthInitContext.from(settings);

        DataGenerator.Builder dataGenerator = DataGenerator.builder();
        dataInitializer.setup(dataGenerator);

        double latitude = settings.getDouble(SPAWN_LATITUDE);
        double longitude = settings.getDouble(SPAWN_LONGITUDE);
        Coordinate spawnCoordinate = ctx.lngLatCrs.coord(longitude, latitude);

        BlockPos topLeft = ctx.lngLatCrs.coord(-180.0, 90.0).toBlockPos()
                .add(new BlockPos(4, 0, 4));
        BlockPos bottomRight = ctx.lngLatCrs.coord(180.0, -90.0).toBlockPos()
                .subtract(new BlockPos(4, 0, 4));

        return build(dataGenerator.build(), spawnCoordinate.toBlockPos(), topLeft, bottomRight);
    }

    private static Future<TerrainMeshData> build(DataGenerator dataGenerator, BlockPos spawnPos, BlockPos minCorner, BlockPos maxCorner) {
        int minX = Math.max(spawnPos.getX() - WorldPreview.RADIUS, minCorner.getX());
        int minZ = Math.max(spawnPos.getZ() - WorldPreview.RADIUS, minCorner.getZ());
        int maxX = Math.min(spawnPos.getX() + WorldPreview.RADIUS, maxCorner.getX());
        int maxZ = Math.min(spawnPos.getZ() + WorldPreview.RADIUS, maxCorner.getZ());

        int width = maxX - minX + 1;
        int height = maxZ - minZ + 1;
        return sampleData(dataGenerator, minX, minZ, width, height)
                .andThen(data -> Future.spawnBlocking(EXECUTOR, () -> buildMesh(data)));
    }

    private static TerrainMeshData buildMesh(ColumnData data) {
        ShortRaster heightRaster = data.getOrDefault(EarthData.TERRAIN_HEIGHT);

        Vec3d translation = new Vec3d(
                -heightRaster.getWidth() / 2.0,
                -computeOriginHeight(heightRaster),
                -heightRaster.getHeight() / 2.0
        );

        return TerrainMeshData.build(data, WorldPreview.GRANULARITY, translation);
    }

    private static Future<ColumnData> sampleData(DataGenerator dataGenerator, int x, int z, int width, int height) {
        DataView view = DataView.rect(x, z, width, height);
        return dataGenerator.generate(view, TerrainMeshData.REQUIRED_DATA);
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
