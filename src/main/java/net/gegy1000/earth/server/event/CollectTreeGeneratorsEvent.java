package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.ecology.vegetation.TreeGenerators;
import net.minecraftforge.fml.common.eventhandler.Event;

public final class CollectTreeGeneratorsEvent extends Event {
    private final TreeGenerators generators;

    public CollectTreeGeneratorsEvent(TreeGenerators generators) {
        this.generators = generators;
    }

    public TreeGenerators getGenerators() {
        return this.generators;
    }
}
