package net.gegy1000.terrarium.client.event;

import net.fabricmc.fabric.util.HandlerList;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.client.gui.Gui;

import java.util.function.Function;

public class GuiChangeEvent {
    public static final HandlerRegistry<Function<Gui, Gui>> HANDLERS = new HandlerList<>(Function.class);

    private GuiChangeEvent() {
    }

    public static Gui dispatch(HandlerRegistry<Function<Gui, Gui>> registry, Gui gui) {
        HandlerList<Function<Gui, Gui>> handlerList = (HandlerList<Function<Gui, Gui>>) registry;
        for (Function<Gui, Gui> handler : handlerList.getBackingArray()) {
            gui = handler.apply(gui);
        }
        return gui;
    }
}
