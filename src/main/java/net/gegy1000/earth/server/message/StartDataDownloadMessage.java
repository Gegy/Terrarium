package net.gegy1000.earth.server.message;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.server.world.data.DataPreloadManager;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class StartDataDownloadMessage implements IMessage {
    private ChunkPos min;
    private ChunkPos max;

    public StartDataDownloadMessage() {
    }

    public StartDataDownloadMessage(ChunkPos min, ChunkPos max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.min = new ChunkPos(buf.readInt(), buf.readInt());
        this.max = new ChunkPos(buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.min.x);
        buf.writeInt(this.min.z);
        buf.writeInt(this.max.x);
        buf.writeInt(this.max.z);
    }

    public static class Handler implements IMessageHandler<StartDataDownloadMessage, IMessage> {
        @Override
        public IMessage onMessage(StartDataDownloadMessage message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                Terrarium.PROXY.scheduleTask(ctx, () -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    if (!DataPreloadManager.checkPermission(player)) {
                        return;
                    }

                    World world = player.world;
                    TerrariumWorld terrarium = TerrariumWorld.get(world);
                    if (terrarium != null) {
                        DataPreloadManager manager = DataPreloadManager.open(terrarium, message.min, message.max);
                        manager.addWatcher(player);
                        manager.start();
                    }
                });
            }

            return null;
        }
    }
}
