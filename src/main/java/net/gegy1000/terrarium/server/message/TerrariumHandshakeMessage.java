package net.gegy1000.terrarium.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumUserTracker;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TerrariumHandshakeMessage implements IMessage {
    private String settings;

    public TerrariumHandshakeMessage() {
    }

    public TerrariumHandshakeMessage(GenerationSettings settings) {
        this.settings = settings.serializeString();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean()) {
            this.settings = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.settings != null);
        if (this.settings != null) {
            ByteBufUtils.writeUTF8String(buf, this.settings);
        }
    }

    public static class Handler implements IMessageHandler<TerrariumHandshakeMessage, IMessage> {
        @Override
        public IMessage onMessage(TerrariumHandshakeMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                MinecraftServer server = player.getServer();
                if (server == null) {
                    return null;
                }
                TerrariumWorld world = server.getWorld(0).getCapability(TerrariumCapabilities.world(), null);
                if (world != null) {
                    Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumUserTracker.markPlayerUsingTerrarium(player));
                    return new TerrariumHandshakeMessage(world.getSettings());
                }
            } else {
                Terrarium.PROXY.scheduleTask(ctx, () -> TerrariumUserTracker.provideSettings(Terrarium.PROXY.getWorld(ctx), message.settings));
            }
            return null;
        }
    }
}
