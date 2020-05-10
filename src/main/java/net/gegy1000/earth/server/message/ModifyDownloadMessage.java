package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.server.world.data.DataPreloader;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class ModifyDownloadMessage implements IMessage {
    private boolean cancel;
    private boolean unwatch;

    public ModifyDownloadMessage() {
    }

    public ModifyDownloadMessage(boolean cancel, boolean unwatch) {
        this.cancel = cancel;
        this.unwatch = unwatch;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.cancel = buf.readBoolean();
        this.unwatch = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.cancel);
        buf.writeBoolean(this.unwatch);
    }

    public static class Handler implements IMessageHandler<ModifyDownloadMessage, IMessage> {
        @Override
        public IMessage onMessage(ModifyDownloadMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (!DataPreloader.checkPermission(player)) {
                        return;
                    }

                    DataPreloader.active().ifPresent(preloader -> {
                        if (message.cancel) {
                            preloader.cancel();
                        }
                        if (message.unwatch) {
                            preloader.removeWatcher(player);
                        }
                    });
                });
            }

            return null;
        }
    }
}
