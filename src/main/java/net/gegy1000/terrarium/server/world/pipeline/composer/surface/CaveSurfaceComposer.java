package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.ComposeChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class CaveSurfaceComposer implements SurfaceComposer {
    private final World world;
    private final MapGenBase caveGenerator;
    private final MapGenBase ravineGenerator;

    public CaveSurfaceComposer(World world) {
        this.world = world;
        this.caveGenerator = TerrainGen.getModdedMapGen(new MapGenCaves(), InitMapGenEvent.EventType.CAVE);
        this.ravineGenerator = TerrainGen.getModdedMapGen(new MapGenRavine(), InitMapGenEvent.EventType.RAVINE);
    }

    @Override
    public void composeSurface(ComposeChunk chunk, RegionGenerationHandler regionHandler) {
        // TODO: Cubic Chunks
//        this.caveGenerator.generate(this.world, chunk.getX(), chunk.getZ(), primer);
//        this.ravineGenerator.generate(this.world, chunk.getX(), chunk.getZ(), primer);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
