package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class ErrorBroadcastHandler {
    private static final long FAIL_NOTIFICATION_INTERVAL = 8000;
    private static final int FAIL_NOTIFICATION_THRESHOLD = 5;

    private static final AtomicInteger FAIL_COUNT = new AtomicInteger();
    private static long lastFailNotificationTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        lastFailNotificationTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        long time = System.currentTimeMillis();

        if (time - lastFailNotificationTime > FAIL_NOTIFICATION_INTERVAL) {
            int failCount = FAIL_COUNT.get();
            if (failCount > FAIL_NOTIFICATION_THRESHOLD) {
                broadcastFailNotification(failCount);
                FAIL_COUNT.set(0);
            }
            lastFailNotificationTime = time;
        }
    }

    private static void broadcastFailNotification(int failCount) {
        DataFailWarningMessage message = new DataFailWarningMessage(failCount);
        for (EntityPlayer player : TerrariumUserTracker.getTerrariumUsers()) {
            Terrarium.NETWORK.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public static void recordFailure() {
        FAIL_COUNT.getAndIncrement();
    }
}
