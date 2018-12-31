package net.gegy1000.terrarium.server.world.pipeline.source;

import net.fabricmc.fabric.events.TickEvent;
import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.event.WorldEvent;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.gegy1000.terrarium.server.message.LoadingStateMessage;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.LinkedList;

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

    public static void register() {
        TickEvent.SERVER.register(server -> onTick());
        WorldEvent.LOAD.register(LoadingStateHandler::onWorldLoad);
        WorldEvent.UNLOAD.register(LoadingStateHandler::onWorldUnload);
    }

    public static void registerClient() {
        ClientTickEvent.CLIENT.register(client -> onTick());
    }

    private static void onWorldLoad(World world) {
        CURRENT_STATE.clear();
        remoteState = null;
        lastBroadcastState = null;

        long time = System.currentTimeMillis();
        lastStateBroadcastTime = time;
        lastFailNotificationTime = time;
    }

    private static void onWorldUnload(World world) {
        CURRENT_STATE.clear();
        remoteState = null;
        lastBroadcastState = null;
    }

    private static void onTick() {
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
        CustomPayloadClientPacket packet = DataFailWarningMessage.create(failCount);
        for (PlayerEntity player : TerrariumHandshakeTracker.getFriends()) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
        }
    }

    private static void broadcastCurrentState(LoadingState state) {
        CustomPayloadClientPacket packet = LoadingStateMessage.create(state);
        for (PlayerEntity player : TerrariumHandshakeTracker.getFriends()) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(packet);
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
