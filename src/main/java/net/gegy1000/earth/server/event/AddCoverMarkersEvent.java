package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarker;
import net.minecraftforge.fml.common.eventhandler.Event;

public class AddCoverMarkersEvent extends Event {
    public void mark(CoverMarker marker, Cover... covers) {
        marker.add(covers);
    }
}
