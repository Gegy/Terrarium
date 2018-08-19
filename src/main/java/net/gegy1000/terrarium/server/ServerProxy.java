package net.gegy1000.terrarium.server;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerProxy {
    public void onPreInit() {
    }

    public void onInit() {
    }

    public void onPostInit() {
    }

    public boolean hasServer() {
        return true;
    }

    public void openWarnToast(int failCount) {
    }

    public void scheduleTask(MessageContext ctx, Runnable task) {
        WorldServer world = (WorldServer) ctx.getServerHandler().player.world;
        world.addScheduledTask(task);
    }

    public World getWorld(MessageContext ctx) {
        return ctx.getServerHandler().player.world;
    }
}
