package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class DisplayDownloadMessage implements IMessage {
    private long count;
    private long total;

    public DisplayDownloadMessage() {
    }

    public DisplayDownloadMessage(long count, long total) {
        this.count = count;
        this.total = total;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        this.count = buffer.readVarLong();
        this.total = buffer.readVarLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarLong(this.count);
        buffer.writeVarLong(this.total);
    }

    public static class Handler implements IMessageHandler<DisplayDownloadMessage, IMessage> {
        @Override
        public IMessage onMessage(DisplayDownloadMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumEarth.PROXY.openDownload(message.count, message.total));
            }
            return null;
        }
    }
}
