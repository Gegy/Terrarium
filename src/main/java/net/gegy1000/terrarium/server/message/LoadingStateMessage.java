package net.gegy1000.terrarium.server.message;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingState;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class LoadingStateMessage {
    private static final Identifier IDENTIFIER = new Identifier(Terrarium.MODID, "load_state");

    public static void registerTo(CustomPayloadPacketRegistry registry) {
        registry.register(IDENTIFIER, (ctx, buf) -> {
            if (ctx.getPacketEnvironment() == EnvType.CLIENT) {
                boolean hasState = buf.readBoolean();
                LoadingState state = hasState ? LoadingState.values()[buf.readInt()] : null;
                ctx.getTaskQueue().execute(() -> LoadingStateHandler.updateRemoteState(state));
            }
        });
    }

    public static CustomPayloadClientPacket create(LoadingState state) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(state != null);
        if (state != null) {
            buf.writeInt(state.ordinal());
        }
        return new CustomPayloadClientPacket(IDENTIFIER, buf);
    }
}
