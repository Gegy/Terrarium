package net.gegy1000.terrarium.server.world.chunk;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.api.CubicChunkGenerator;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComposableCubeGenerator implements CubicChunkGenerator {
    private final World world;

    private final Lazy<ChunkCompositionProcedure> compositionProcedure;
    private final Lazy<RegionGenerationHandler> regionHandler;

    public ComposableCubeGenerator(World world) {
        this.world = world;

        this.compositionProcedure = new Lazy.WorldCap<>(world, TerrariumWorldData::getCompositionProcedure);
        this.regionHandler = new Lazy<>(() -> {
            TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
            if (capability != null) {
                return capability.getRegionHandler();
            }
            throw new IllegalStateException("Tried to load RegionGenerationHandler before it was present");
        });
    }

    @Override
    public void prime(CubicPos pos, ChunkPrimeWriter writer) {
        RegionGenerationHandler regionHandler = this.regionHandler.get();
        regionHandler.prepareChunk(pos.getMinX(), pos.getMinZ());

        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        compositionProcedure.composeSurface(regionHandler, pos, writer);
    }

    @Override
    public void populate(CubicPos pos, ChunkPopulationWriter writer) {
        RegionGenerationHandler regionHandler = this.regionHandler.get();
        regionHandler.prepareChunk(pos.getCenterX(), pos.getCenterZ());

        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        compositionProcedure.composeDecoration(regionHandler, pos, writer);
    }

    @Override
    public Biome[] populateBiomes(ChunkPos pos, Biome[] buffer) {
        return this.world.getBiomeProvider().getBiomes(buffer, pos.x << 4, pos.z << 4, 16, 16);
    }
}
