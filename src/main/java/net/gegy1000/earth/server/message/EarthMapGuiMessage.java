package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.gegy1000.earth.client.gui.EarthTeleportGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EarthMapGuiMessage implements IMessage {
    private double latitude;
    private double longitude;
    private Type type;

    public EarthMapGuiMessage() {
    }

    public EarthMapGuiMessage(double latitude, double longitude, Type type) {
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

    public static class Handler implements IMessageHandler<EarthMapGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(EarthMapGuiMessage message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> {
                switch (message.type) {
                    case LOCATE:
                        mc.displayGuiScreen(new EarthLocateGui(message.latitude, message.longitude));
                        break;
                    case TELEPORT:
                        mc.displayGuiScreen(new EarthTeleportGui(message.latitude, message.longitude));
                }
            });
            return null;
        }
    }

    public enum Type {
        LOCATE,
        TELEPORT
    }
}
