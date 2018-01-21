package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.world.World;

public class EarthBiomeProvider extends TerrariumBiomeProvider {
    private final EarthGenerationSettings settings;

    private final Lazy<EarthGenerationHandler> generationHandler = new Lazy<>(() -> {
        TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (capability != null) {
            return capability.getGenerationHandler();
        }
        throw new RuntimeException("Tried to load EarthGenerationHandler before it was present");
    });

    public EarthBiomeProvider(World world) {
        super(world);
        this.settings = EarthGenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());
    }

    @Override
    protected void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ) {
        this.generationHandler.get().populateCoverRegion(coverBuffer, chunkX, chunkZ);
    }

    @Override
    protected LatitudinalZone getZone(int x, int z) {
        int offset = this.random.nextInt(128) - this.random.nextInt(128);
        Coordinate chunkCoordinate = Coordinate.fromBlock(this.settings, x, z + offset);
        return LatitudinalZone.get(chunkCoordinate);
    }
}
