package net.gegy1000.earth.server.world.compatibility.hooks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DimensionManagerHooks {
    private static Field worldsField;
    private static Field weakWorldsMapField;

    static {
        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);

            worldsField = DimensionManager.class.getDeclaredField("worlds");
            modifiers.setInt(worldsField, worldsField.getModifiers() & ~Modifier.FINAL);
            worldsField.setAccessible(true);

            weakWorldsMapField = DimensionManager.class.getDeclaredField("weakWorldMap");
            modifiers.setInt(weakWorldsMapField, weakWorldsMapField.getModifiers() & ~Modifier.FINAL);
            weakWorldsMapField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to access DimensionManager worlds fields: compatibility mode will not work!", e);
        }
    }

    // to construct a WorldServer, we need to make sure Forge doesn't update the world map
    public static Freeze freeze(MinecraftServer server) {
        State formerState = captureState();
        applyState(makeFrozenState(formerState));

        return new Freeze(server, formerState, server.worlds);
    }

    private static void applyState(State state) {
        if (worldsField == null || weakWorldsMapField == null) {
            throw new UnsupportedOperationException("Cannot freeze world state: try disable compatibility mode?");
        }

        try {
            worldsField.set(null, state.worlds);
            weakWorldsMapField.set(null, state.weakWorldsMap);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Failed to freeze world state: try disable compatibility mode?", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static State captureState() {
        if (worldsField == null || weakWorldsMapField == null) {
            throw new UnsupportedOperationException("Cannot freeze world state: try disable compatibility mode?");
        }

        try {
            Int2ObjectMap<WorldServer> worlds = (Int2ObjectMap<WorldServer>) worldsField.get(null);
            ConcurrentMap<World, World> weakWorldsMap = (ConcurrentMap<World, World>) weakWorldsMapField.get(null);

            return new State(worlds, weakWorldsMap);
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Failed to freeze world state: try disable compatibility mode?", e);
        }
    }

    private static State makeFrozenState(State state) {
        return new State(new Int2ObjectOpenHashMap<>(state.worlds), new ConcurrentHashMap<>(state.weakWorldsMap));
    }

    public static class Freeze implements AutoCloseable {
        private final MinecraftServer server;
        private final State formerState;
        private final WorldServer[] formerWorlds;

        Freeze(MinecraftServer server, State formerState, WorldServer[] formerWorlds) {
            this.server = server;
            this.formerState = formerState;
            this.formerWorlds = formerWorlds;
        }

        @Override
        public void close() {
            applyState(this.formerState);
            this.server.worlds = this.formerWorlds;
        }
    }

    private static class State {
        final Int2ObjectMap<WorldServer> worlds;
        final ConcurrentMap<World, World> weakWorldsMap;

        private State(Int2ObjectMap<WorldServer> worlds, ConcurrentMap<World, World> weakWorldsMap) {
            this.worlds = worlds;
            this.weakWorldsMap = weakWorldsMap;
        }
    }
}
