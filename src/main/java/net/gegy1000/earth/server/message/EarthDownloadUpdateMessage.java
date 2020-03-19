package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EarthDownloadUpdateMessage implements IMessage {
    private long count;
    private long total;

    public EarthDownloadUpdateMessage() {
    }

    public EarthDownloadUpdateMessage(long count, long total) {
        this.count = count;
        this.total = total;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.count = buf.readLong();
        this.total = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.count);
        buf.writeLong(this.total);
    }

    public static class Handler implements IMessageHandler<EarthDownloadUpdateMessage, IMessage> {
        @Override
        public IMessage onMessage(EarthDownloadUpdateMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumEarth.PROXY.updateDownload(message.count, message.total));
            }
            return null;
        }
    }
}
