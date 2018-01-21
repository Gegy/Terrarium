package net.gegy1000.terrarium.server.world.generator.debug;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.TerrariumBiomeProvider;
import net.minecraft.world.World;

public class CoverDebugBiomeProvider extends TerrariumBiomeProvider {
    public CoverDebugBiomeProvider(World world) {
        super(world);
    }

    @Override
    protected void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                coverBuffer[localX + localZ * 16] = DebugMap.getCover(localX + globalX, localZ + globalZ).getCoverType();
            }
        }
    }

    @Override
    protected LatitudinalZone getZone(int x, int z) {
        return DebugMap.getCover(x, z).getZone();
    }
}
