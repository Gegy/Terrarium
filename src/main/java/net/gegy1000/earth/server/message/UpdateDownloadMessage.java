package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class UpdateDownloadMessage implements IMessage {
    private int id;
    private long count;
    private long total;

    public UpdateDownloadMessage() {
    }

    public UpdateDownloadMessage(int id, long count, long total) {
        this.id = id;
        this.count = count;
        this.total = total;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = buf.readInt();
        this.count = buf.readLong();
        this.total = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.id);
        buf.writeLong(this.count);
        buf.writeLong(this.total);
    }

    public static class Handler implements IMessageHandler<UpdateDownloadMessage, IMessage> {
        @Override
        public IMessage onMessage(UpdateDownloadMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumEarth.PROXY.updateDownload(message.id, message.count, message.total));
            }
            return null;
        }
    }
}
