package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.gegy1000.terrarium.server.message.LoadingStateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.LinkedList;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class LoadingStateHandler {
    private static final LinkedList<LoadingState> CURRENT_STATE = new LinkedList<>();

    private static final Object LOCK = new Object();

    private static final long FAIL_NOTIFICATION_INTERVAL = 8000;
    private static final int FAIL_NOTIFICATION_THRESHOLD = 5;

    private static final long STATE_BROADCAST_INTERVAL = 500;

    private static LoadingState remoteState;

    private static int failCount;
    private static long lastFailNotificationTime;

    private static LoadingState lastBroadcastState;
    private static long lastStateBroadcastTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        CURRENT_STATE.clear();
        remoteState = null;
        lastBroadcastState = null;

        long time = System.currentTimeMillis();
        lastStateBroadcastTime = time;
        lastFailNotificationTime = time;
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        CURRENT_STATE.clear();
        remoteState = null;
        lastBroadcastState = null;
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        long time = System.currentTimeMillis();

        if (time - lastFailNotificationTime > FAIL_NOTIFICATION_INTERVAL) {
            if (failCount > FAIL_NOTIFICATION_THRESHOLD) {
                broadcastFailNotification(failCount);
                failCount = 0;
            }
            lastFailNotificationTime = time;
        }

        if (time - lastStateBroadcastTime > STATE_BROADCAST_INTERVAL) {
            LoadingState currentState;
            synchronized (LOCK) {
                currentState = !CURRENT_STATE.isEmpty() ? CURRENT_STATE.getLast() : null;
            }
            if (lastBroadcastState != currentState) {
                broadcastCurrentState(currentState);
                lastStateBroadcastTime = time;
            }
        }
    }

    private static void broadcastFailNotification(int failCount) {
        DataFailWarningMessage message = new DataFailWarningMessage(failCount);
        for (EntityPlayer player : TerrariumHandshakeTracker.getFriends()) {
            Terrarium.NETWORK.sendTo(message, (EntityPlayerMP) player);
        }
    }

    private static void broadcastCurrentState(LoadingState state) {
        if (Terrarium.PROXY.hasServer()) {
            LoadingStateMessage message = new LoadingStateMessage(state);
            for (EntityPlayer player : TerrariumHandshakeTracker.getFriends()) {
                Terrarium.NETWORK.sendTo(message, (EntityPlayerMP) player);
            }
        }
        lastBroadcastState = state;
    }

    public static void updateRemoteState(LoadingState state) {
        remoteState = state;
    }

    @Nullable
    public static LoadingState getDisplayState() {
        synchronized (LOCK) {
            if (!CURRENT_STATE.isEmpty()) {
                return CURRENT_STATE.getLast();
            }
        }
        return remoteState;
    }

    public static void pushState(LoadingState state) {
        synchronized (LOCK) {
            CURRENT_STATE.addLast(state);
        }
    }

    public static void popState() {
        synchronized (LOCK) {
            CURRENT_STATE.pollLast();
        }
    }

    public static void countFailure() {
        synchronized (LOCK) {
            failCount++;
        }
    }
}
