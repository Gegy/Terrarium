package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EarthPanoramaMessage implements IMessage {
    public EarthPanoramaMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<EarthPanoramaMessage, IMessage> {
        @Override
        public IMessage onMessage(EarthPanoramaMessage message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumEarth.PROXY.displayPanorama());
            }
            return null;
        }
    }
}
