package net.gegy1000.earth.client.gui.preview;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.earth.server.world.EarthInitContext;
import net.gegy1000.terrarium.server.world.TerrariumDataInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.gegy1000.earth.server.world.EarthWorldType.SPAWN_LATITUDE;
import static net.gegy1000.earth.server.world.EarthWorldType.SPAWN_LONGITUDE;

@SideOnly(Side.CLIENT)
public class WorldPreview {
    private static final int VIEW_RANGE = 16 * 16;
    private static final int VIEW_SIZE = VIEW_RANGE * 2 + 1;

    private static final int VIEW_GRANULARITY = 2;

    private static final BufferBuilder BUILDER = new BufferBuilder(PreviewMesh.computeBufferSize(VIEW_SIZE, VIEW_SIZE, VIEW_GRANULARITY));

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("terrarium-preview-build")
                    .build()
    );

    private final PreviewMesh mesh;

    private WorldPreview(PreviewMesh mesh) {
        this.mesh = mesh;
    }

    public static CompletableFuture<WorldPreview> generate(TerrariumWorldType worldType, GenerationSettings settings) {
        return CompletableFuture.supplyAsync(() -> {
            TerrariumDataInitializer dataInitializer = worldType.createDataInitializer(settings);

            EarthInitContext ctx = EarthInitContext.from(settings);

            DataGenerator.Builder dataGenerator = DataGenerator.builder();
            dataInitializer.setup(dataGenerator);

            double latitude = settings.getDouble(SPAWN_LATITUDE);
            double longitude = settings.getDouble(SPAWN_LONGITUDE);
            Coordinate spawnCoordinate = new Coordinate(ctx.lngLatCrs, longitude, latitude);

            PreviewMesh mesh = WorldPreview.generateMesh(dataGenerator.build(), spawnCoordinate.toBlockPos());
            return new WorldPreview(mesh);
        }, EXECUTOR);
    }

    private static PreviewMesh generateMesh(DataGenerator dataGenerator, BlockPos spawnPos) {
        int originX = (spawnPos.getX() - VIEW_RANGE);
        int originZ = (spawnPos.getZ() - VIEW_RANGE);
        ShortRaster heightRaster = sampleHeightRaster(dataGenerator, originX, originZ, VIEW_SIZE);

        return PreviewMesh.build(heightRaster, BUILDER, VIEW_GRANULARITY);
    }

    private static ShortRaster sampleHeightRaster(DataGenerator dataGenerator, int originX, int originZ, int size) {
        DataView view = DataView.square(originX, originZ, size);
        return dataGenerator.generateOne(view, EarthDataKeys.TERRAIN_HEIGHT)
                .orElseGet(() -> ShortRaster.create(view));
    }

    public void upload() {
        this.mesh.upload();
    }

    public void render() {
        this.mesh.render();
    }

    public void delete() {
        this.mesh.delete();
    }
}
