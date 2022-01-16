package net.gegy1000.earth.client.terrain;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import org.lwjgl.opengl.GL11;

public final class TerrainMesh implements AutoCloseable {
    // TODO: do not use display list!
    private final int displayList;

    TerrainMesh(int displayList) {
        this.displayList = displayList;
    }

    static TerrainMesh upload(BufferBuilder builder) {
        int displayList = GLAllocation.generateDisplayLists(1);

        GlStateManager.glNewList(displayList, GL11.GL_COMPILE);
        new WorldVertexBufferUploader().draw(builder);
        GlStateManager.glEndList();

        return new TerrainMesh(displayList);
    }

    public void render() {
        GlStateManager.callList(this.displayList);
    }

    public void delete() {
        GLAllocation.deleteDisplayLists(this.displayList);
    }

    @Override
    public void close() {
        this.delete();
    }
}
