package net.gegy1000.terrarium.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DataFailWarningMessage implements IMessage {
    private int failCount;

    public DataFailWarningMessage() {
    }

    public DataFailWarningMessage(int failCount) {
        this.failCount = failCount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.failCount = buf.readUnsignedShort();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(this.failCount & 0xFFFF);
    }

    public static class Handler implements IMessageHandler<DataFailWarningMessage, IMessage> {
        @Override
        public IMessage onMessage(DataFailWarningMessage message, MessageContext ctx) {
            Terrarium.PROXY.scheduleTask(ctx, () -> Terrarium.PROXY.openWarnToast(message.failCount));
            return null;
        }
    }
}
