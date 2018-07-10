package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.message.TerrariumLoadingStateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class LoadingStateHandler {
    private static final List<StateEntry> STATE_BUFFER = new LinkedList<>();

    private static final Object LOCK = new Object();

    private static final long STATE_TRACK_INTERVAL = 100;

    private static LoadingState localState;
    private static LoadingState remoteState;
    private static long lastCheckTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        localState = null;
        remoteState = null;
        lastCheckTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        long time = System.currentTimeMillis();
        if (time - lastCheckTime > STATE_TRACK_INTERVAL) {
            LoadingState state = LoadingStateHandler.checkState();
            if (!Objects.equals(state, localState)) {
                if (Terrarium.PROXY.hasServer()) {
                    broadcastState(state);
                }
                localState = state;
            }
            lastCheckTime = time;
        }
    }

    private static void broadcastState(LoadingState state) {
        TerrariumLoadingStateMessage message = new TerrariumLoadingStateMessage(state);
        for (EntityPlayer player : TerrariumHandshakeTracker.getFriends()) {
            Terrarium.network.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public static void updateRemoteState(LoadingState state) {
        remoteState = state;
    }

    @Nullable
    public static LoadingState getDisplayState() {
        if (localState != null) {
            return localState;
        }
        return remoteState;
    }

    public static void putState(LoadingState state) {
        StateEntry entry = LoadingStateHandler.makeState(state);
        LoadingStateHandler.breakState(entry);
    }

    public static StateEntry makeState(LoadingState state) {
        StateEntry entry = new StateEntry(state);
        synchronized (LOCK) {
            STATE_BUFFER.add(entry);
        }
        return entry;
    }

    public static void breakState(StateEntry entry) {
        entry.completed = true;
        entry.completedTime = System.currentTimeMillis();
    }

    public static LoadingState checkState() {
        LoadingStateHandler.removeExpired();
        synchronized (LOCK) {
            if (STATE_BUFFER.isEmpty()) {
                return null;
            }
            Map<LoadingState, Integer> stateCounts = new EnumMap<>(LoadingState.class);
            for (StateEntry entry : STATE_BUFFER) {
                int weight = stateCounts.getOrDefault(entry.state, 0);
                stateCounts.put(entry.state, weight + entry.state.getWeight());
            }
            LoadingState relevantState = null;
            int relevantWeight = 0;
            for (Map.Entry<LoadingState, Integer> entry : stateCounts.entrySet()) {
                int weight = entry.getValue();
                if (weight > relevantWeight) {
                    relevantState = entry.getKey();
                    relevantWeight = weight;
                }
            }
            return relevantState;
        }
    }

    private static void removeExpired() {
        synchronized (LOCK) {
            STATE_BUFFER.removeIf(StateEntry::hasExpired);
        }
    }

    public static class StateEntry {
        private final LoadingState state;

        private boolean completed;
        private long completedTime;

        private StateEntry(LoadingState state) {
            this.state = state;
        }

        public boolean hasExpired() {
            return this.completed && System.currentTimeMillis() - this.completedTime > this.state.getLifetime();
        }
    }
}
