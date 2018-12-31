package net.gegy1000.terrarium.server.message;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.fabricmc.fabric.networking.PacketContext;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.CustomPayloadServerPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class TerrariumHandshakeMessage {
    private static final Identifier IDENTIFIER = new Identifier(Terrarium.MODID, "handshake");

    public static void registerTo(CustomPayloadPacketRegistry registry) {
        registry.register(IDENTIFIER, (ctx, buf) -> {
            if (ctx.getPacketEnvironment() == EnvType.CLIENT) {
                World world = ctx.getPlayer().world;
                JsonElement json = new JsonParser().parse(buf.readString(Short.MAX_VALUE));
                ctx.getTaskQueue().execute(() -> {
                    handleClient(world, new Dynamic<>(JsonOps.INSTANCE, json));
                });
            } else {
                ctx.getTaskQueue().execute(() -> handleServer(ctx));
            }
        });
    }

    private static void handleServer(PacketContext ctx) {
        ServerPlayerEntity player = (ServerPlayerEntity) ctx.getPlayer();
        MinecraftServer server = player.getServer();

        TerrariumHandshakeTracker.markPlayerFriendly(player);

        ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
        ChunkGeneratorSettings config = world.getChunkManager().getChunkGenerator().getSettings();
        if (config instanceof TerrariumGeneratorConfig) {
            GenerationSettings settings = ((TerrariumGeneratorConfig) config).getSettings();
            player.networkHandler.sendPacket(createClientbound(settings));
        }
    }

    private static <T> void handleClient(World world, Dynamic<T> settings) {
        TerrariumHandshakeTracker.provideSettings(world, settings);
    }

    public static CustomPayloadClientPacket createClientbound(GenerationSettings settings) {
        JsonElement json = settings.serialize(JsonOps.INSTANCE).getValue();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(new Gson().toJson(json));
        return new CustomPayloadClientPacket(IDENTIFIER, buf);
    }

    public static CustomPayloadServerPacket createServerbound() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        return new CustomPayloadServerPacket(IDENTIFIER, buf);
    }
}
