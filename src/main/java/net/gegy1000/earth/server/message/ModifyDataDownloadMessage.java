package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.server.world.data.DataPreloadManager;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class ModifyDataDownloadMessage implements IMessage {
    private int id;
    private boolean cancel;
    private boolean unwatch;

    public ModifyDataDownloadMessage() {
    }

    public ModifyDataDownloadMessage(int id, boolean cancel, boolean unwatch) {
        this.id = id;
        this.cancel = cancel;
        this.unwatch = unwatch;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readInt();
        this.cancel = buf.readBoolean();
        this.unwatch = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.id);
        buf.writeBoolean(this.cancel);
        buf.writeBoolean(this.unwatch);
    }

    public static class Handler implements IMessageHandler<ModifyDataDownloadMessage, IMessage> {
        @Override
        public IMessage onMessage(ModifyDataDownloadMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (!DataPreloadManager.checkPermission(player)) {
                        return;
                    }

                    DataPreloadManager manager = DataPreloadManager.getActive(message.id);
                    if (manager != null) {
                        if (message.cancel) {
                            manager.cancel();
                        }
                        if (message.unwatch) {
                            manager.removeWatcher(player);
                        }
                    }
                });
            }

            return null;
        }
    }
}
