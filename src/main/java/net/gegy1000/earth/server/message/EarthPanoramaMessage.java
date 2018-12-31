package net.gegy1000.earth.server.message;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class EarthPanoramaMessage {
    private static final Identifier IDENTIFIER = new Identifier(TerrariumEarth.MODID, "panorama");

    public static void registerTo(CustomPayloadPacketRegistry registry) {
        registry.register(IDENTIFIER, (ctx, buf) -> {
            if (ctx.getPacketEnvironment() == EnvType.CLIENT) {
                ctx.getTaskQueue().execute(() -> TerrariumEarth.proxy.displayPanorama());
            }
        });
    }

    public static CustomPayloadClientPacket create() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        return new CustomPayloadClientPacket(IDENTIFIER, buf);
    }
}
