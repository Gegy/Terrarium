package net.gegy1000.earth.server.world.coordinate;

import net.gegy1000.earth.server.world.pipeline.layer.DebugMap;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.minecraft.util.math.MathHelper;

public class DebugLatLngCoordinateState implements CoordinateState {
    @Override
    public double getBlockX(double x, double z) {
        return 0;
    }

    @Override
    public double getBlockZ(double x, double z) {
        return z;
    }

    @Override
    public double getX(double blockX, double blockZ) {
        DebugMap.DebugCover cover = DebugMap.getCover(MathHelper.floor(blockX), MathHelper.floor(blockZ));
        return cover.getZone().getCenterLatitude();
    }

    @Override
    public double getZ(double blockX, double blockZ) {
        return blockX;
    }
}
