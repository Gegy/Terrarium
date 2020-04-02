package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EarthOpenMapMessage implements IMessage {
    private double latitude;
    private double longitude;
    private Type type;

    public EarthOpenMapMessage() {
    }

    public EarthOpenMapMessage(double latitude, double longitude, Type type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.latitude = buf.readDouble();
        this.longitude = buf.readDouble();
        this.type = Type.values()[buf.readUnsignedByte() % Type.values().length];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.latitude);
        buf.writeDouble(this.longitude);
        buf.writeByte(this.type.ordinal() & 0xFF);
    }

    public static class Handler implements IMessageHandler<EarthOpenMapMessage, IMessage> {
        @Override
        public IMessage onMessage(EarthOpenMapMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumEarth.PROXY.openMapGui(message.type, message.latitude, message.longitude));
            }
            return null;
        }
    }

    public enum Type {
        LOCATE,
        PRELOAD,
    }
}
