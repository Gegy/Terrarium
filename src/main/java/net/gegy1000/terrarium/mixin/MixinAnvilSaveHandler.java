package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.api.RegionSaveHandler;
import net.minecraft.world.ChunkSaveHandlerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(ChunkSaveHandlerImpl.class)
public class MixinAnvilSaveHandler implements RegionSaveHandler {
    @Shadow(aliases = "field_12999")
    @Final
    private File root;

    @Override
    public File getSaveRoot() {
        return this.root;
    }
}
