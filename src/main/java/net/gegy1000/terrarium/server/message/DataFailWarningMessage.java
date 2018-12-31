package net.gegy1000.terrarium.server.message;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.toast.DataFailToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class DataFailWarningMessage {
    private static final Identifier IDENTIFIER = new Identifier(Terrarium.MODID, "fail_warn");

    public static void registerTo(CustomPayloadPacketRegistry registry) {
        registry.register(IDENTIFIER, (ctx, buf) -> {
            if (ctx.getPacketEnvironment() == EnvType.CLIENT) {
                int failCount = buf.readUnsignedShort();
                ctx.getTaskQueue().execute(() -> displayFailToast(failCount));
            }
        });
    }

    private static void displayFailToast(int failCount) {
        MinecraftClient.getInstance().getToastManager().add(new DataFailToast(failCount));
    }

    public static CustomPayloadClientPacket create(int failCount) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeShort(failCount & 0xFFFF);
        return new CustomPayloadClientPacket(IDENTIFIER, buf);
    }
}
