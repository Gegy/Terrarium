package dev.gegy.terrarium.mixin.geo_chunk;

import com.mojang.datafixers.DataFixer;
import dev.gegy.terrarium.world.GeoProviderHolder;
import dev.gegy.terrarium.world.generator.chunk.GeoChunkGenerator;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Shadow
    @Final
    @Mutable
    private RandomState randomState;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initRandomState(final ServerLevel level, final LevelStorageSource.LevelStorageAccess storageAccess, final DataFixer dataFixer, final StructureTemplateManager templateManager, final Executor executor, final BlockableEventLoop<Runnable> mainThreadExecutor, final LightChunkGetter lightChunkGetter, final ChunkGenerator generator, final ChunkProgressListener progressListener, final ChunkStatusUpdateListener chunkStatusListener, final Supplier<DimensionDataStorage> overworldDataStorage, final TicketStorage ticketStorage, final int serverViewDistance, final boolean sync, final CallbackInfo ci) {
        if (generator instanceof final GeoChunkGenerator geoGenerator) {
            GeoProviderHolder.inject(randomState, geoGenerator.createGeoProvider());
        }
    }
}
