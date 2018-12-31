package net.gegy1000.earth.server.message;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class EarthMapGuiMessage {
    private static final Identifier IDENTIFIER = new Identifier(TerrariumEarth.MODID, "map_gui");

    public static void registerTo(CustomPayloadPacketRegistry registry) {
        registry.register(IDENTIFIER, (ctx, buf) -> {
            if (ctx.getPacketEnvironment() != EnvType.CLIENT) {
                return;
            }
            double latitude = buf.readDouble();
            double longitude = buf.readDouble();
            Type type = Type.values()[buf.readUnsignedByte() % Type.values().length];
            ctx.getTaskQueue().execute(() -> TerrariumEarth.proxy.openMapGui(type, latitude, longitude));
        });
    }

    public static CustomPayloadClientPacket create(double latitude, double longitude, Type type) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeDouble(latitude);
        buf.writeDouble(longitude);
        buf.writeByte(type.ordinal() & 0xFF);
        return new CustomPayloadClientPacket(IDENTIFIER, buf);
    }

    public enum Type {
        LOCATE,
        TELEPORT
    }
}
