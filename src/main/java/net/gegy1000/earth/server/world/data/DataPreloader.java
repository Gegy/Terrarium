package net.gegy1000.earth.server.world.data;

import com.mojang.authlib.GameProfile;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.message.DisplayDownloadMessage;
import net.gegy1000.earth.server.message.UpdateDownloadMessage;
import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.ChunkedIterator;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = TerrariumEarth.ID)
public final class DataPreloader {
    private static final int BATCH_SIZE = 1000;
    private static DataPreloader active;

    private final DataGenerator generator;
    private final ChunkPos min;
    private final ChunkPos max;

    private final long total;

    private final Collection<EntityPlayerMP> watchers = new HashSet<>();

    private boolean started;
    private final AtomicBoolean canceled = new AtomicBoolean();

    private DataPreloader(DataGenerator generator, ChunkPos min, ChunkPos max) {
        this.generator = generator;
        this.min = min;
        this.max = max;

        long width = (max.x - min.x) + 1;
        long height = (max.z - min.z) + 1;
        this.total = width * height;
    }

    public static boolean checkPermission(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;

        PlayerList players = server.getPlayerList();
        GameProfile profile = player.getGameProfile();
        if (!players.canSendCommands(profile)) {
            return false;
        }

        UserListOpsEntry op = players.getOppedPlayers().getEntry(profile);
        if (op != null) {
            return op.getPermissionLevel() >= 4;
        } else {
            return server.getOpPermissionLevel() >= 4;
        }
    }

    public static DataPreloader open(TerrariumWorld terrarium, ChunkPos min, ChunkPos max) {
        return new DataPreloader(terrarium.getDataGenerator(), min, max);
    }

    public static Optional<DataPreloader> active() {
        return Optional.ofNullable(active);
    }

    public static boolean start(DataPreloader preloader) {
        if (active != null) return false;

        active = preloader;

        Thread thread = new Thread(() -> {
            try {
                preloader.drive();
            } finally {
                active = null;
            }
        });
        thread.setName("data-preloader");
        thread.setDaemon(true);

        thread.start();

        return true;
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;

            DataPreloader active = DataPreloader.active;
            if (active != null) {
                active.removeWatcher(player);
            }
        }
    }

    public void addWatcher(EntityPlayerMP player) {
        synchronized (this.watchers) {
            this.watchers.add(player);
        }

        if (this.started) {
            TerrariumEarth.NETWORK.sendTo(new DisplayDownloadMessage(0, this.total), player);
        }
    }

    public void removeWatcher(EntityPlayerMP player) {
        synchronized (this.watchers) {
            this.watchers.remove(player);
        }
    }

    private void drive() {
        long count = 0;

        Iterable<Collection<BlockPos>> chunks = ChunkedIterator.of(BlockPos.getAllInBox(
                this.min.x, 0, this.min.z,
                this.max.x, 0, this.max.z
        ), BATCH_SIZE);

        this.notifyWatchers(new DisplayDownloadMessage(count, this.total));

        this.started = true;

        for (Collection<BlockPos> chunk : chunks) {
            for (BlockPos column : chunk) {
                // TODO: ensure the preloading isn't actually doing any work
                DataView view = DataView.ofSquare(column.getX() << 4, column.getZ() << 4, 16);
                this.generator.generate(view);
            }

            CurrentThreadExecutor.blockOn(DataSourceReader.INSTANCE.finishLoading());

            count += chunk.size();
            this.notifyWatchers(new UpdateDownloadMessage(count));

            if (this.canceled.get()) {
                return;
            }
        }
    }

    private void notifyWatchers(IMessage message) {
        synchronized (this.watchers) {
            for (EntityPlayerMP player : this.watchers) {
                TerrariumEarth.NETWORK.sendTo(message, player);
            }
        }
    }

    public void cancel() {
        this.canceled.set(true);
    }
}
