package net.gegy1000.terrarium.client.preview;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;

public interface PreviewGeometry {
    PreviewGeometry EMPTY = new Empty();

    void render();

    void delete();

    class DisplayList implements PreviewGeometry {
        private final int id;

        public DisplayList(int id) {
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

    class Empty implements PreviewGeometry {
        private Empty() {
        }

        @Override
        public void render() {
        }

        @Override
        public void delete() {
        }
    }
}
