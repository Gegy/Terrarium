package net.gegy1000.earth.client.gui.preview;

import net.gegy1000.earth.client.terrain.TerrainMesh;
import net.gegy1000.earth.client.terrain.TerrainMeshData;
import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.justnow.future.Cancelable;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;

import java.util.concurrent.atomic.AtomicInteger;

public final class WorldPreview implements AutoCloseable {
    public static final int RADIUS = 16 * 16;
    public static final int SIZE = RADIUS * 2 + 1;

    public static final int GRANULARITY = 2;

    private final AtomicInteger referenceCount = new AtomicInteger(1);

    private TerrainMesh mesh;
    private Cancelable<TerrainMeshData> meshFuture;

    public void rebuild(TerrariumWorldType worldType, GenerationSettings settings) {
        if (this.meshFuture != null) {
            this.meshFuture.cancel();
            DataSourceReader.INSTANCE.cancelLoading();
        }

        this.meshFuture = Future.cancelable(PreviewMeshBuilder.build(worldType, settings));
    }

    public void render() {
        Cancelable<TerrainMeshData> future = this.meshFuture;
        if (future != null) {
            if (this.advanceMeshFuture(future)) {
                this.meshFuture = null;
            }
        }

        TerrainMesh mesh = this.mesh;
        if (mesh != null) {
            mesh.render();
        }
    }

    private boolean advanceMeshFuture(Future<TerrainMeshData> future) {
        TerrainMeshData data = CurrentThreadExecutor.advance(future);
        if (data != null) {
            this.uploadMesh(data);
            return true;
        } else {
            return false;
        }
    }

    private void uploadMesh(TerrainMeshData data) {
        if (this.mesh != null) {
            this.mesh.delete();
        }
        this.mesh = data.upload();
    }

    public void delete() {
        if (this.mesh != null) {
            this.mesh.delete();
        }
        if (this.meshFuture != null) {
            this.meshFuture.cancel();
        }
        this.referenceCount.set(0);

        DataSourceReader.INSTANCE.clear();
    }

    public WorldPreview retain() {
        this.referenceCount.getAndIncrement();
        return this;
    }

    public void release() {
        if (this.referenceCount.decrementAndGet() <= 0) {
            this.delete();
        }
    }

    public boolean isGenerateInactive() {
        return this.meshFuture == null;
    }

    @Override
    public void close() {
        this.release();
    }
}
