package net.gegy1000.terrarium.server.world.decorator;

import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.world.World;

import java.util.Random;

public interface TerrariumWorldDecorator {
    void decorate(World world, Random random, CoverType[] coverBuffer, byte[] slopeBuffer, int x, int z);
}
