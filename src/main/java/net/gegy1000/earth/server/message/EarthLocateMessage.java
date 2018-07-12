package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.client.gui.EarthLocateGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EarthLocateMessage implements IMessage {
    private double latitude;
    private double longitude;

    public EarthLocateMessage() {
    }

    public EarthLocateMessage(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.latitude = buf.readDouble();
        this.longitude = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.latitude);
        buf.writeDouble(this.longitude);
    }

    public static class Handler implements IMessageHandler<EarthLocateMessage, IMessage> {
        @Override
        public IMessage onMessage(EarthLocateMessage message, MessageContext ctx) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> mc.displayGuiScreen(new EarthLocateGui(message.latitude, message.longitude)));
            return null;
        }
    }
}
