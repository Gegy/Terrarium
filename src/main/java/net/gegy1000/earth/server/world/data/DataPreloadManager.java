package net.gegy1000.earth.server.world.data;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.earth.TerrariumEarth;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = TerrariumEarth.ID)
public final class DataPreloadManager {
    private static final int BATCH_SIZE = 1000;
    private static final Int2ObjectMap<DataPreloadManager> ACTIVE = new Int2ObjectOpenHashMap<>();

    private static int currentId;

    private final int id;
    private final DataGenerator generator;
    private final ChunkPos min;
    private final ChunkPos max;

    private final Collection<EntityPlayerMP> watchers = new HashSet<>();

    private final AtomicBoolean canceled = new AtomicBoolean();

    private DataPreloadManager(int id, DataGenerator generator, ChunkPos min, ChunkPos max) {
        this.id = id;
        this.generator = generator;
        this.min = min;
        this.max = max;
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

    public static DataPreloadManager open(TerrariumWorld terrarium, ChunkPos min, ChunkPos max) {
        int id = currentId++;
        return new DataPreloadManager(id, terrarium.getDataGenerator(), min, max);
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            synchronized (ACTIVE) {
                for (DataPreloadManager manager : ACTIVE.values()) {
                    manager.removeWatcher(player);
                }
            }
        }
    }

    @Nullable
    public static DataPreloadManager getActive(int id) {
        return ACTIVE.get(id);
    }

    public void addWatcher(EntityPlayerMP player) {
        synchronized (this.watchers) {
            this.watchers.add(player);
        }
    }

    public void removeWatcher(EntityPlayerMP player) {
        synchronized (this.watchers) {
            this.watchers.remove(player);
        }
    }

    public void start() {
        long width = (this.max.x - this.min.x) + 1;
        long height = (this.max.z - this.min.z) + 1;
        long total = width * height;

        this.notifyWatchers(0, total);

        Thread thread = new Thread(() -> {
            synchronized (ACTIVE) {
                ACTIVE.put(this.id, this);
            }

            try {
                this.drive(total);
            } finally {
                synchronized (ACTIVE) {
                    ACTIVE.remove(this.id);
                }
            }
        });
        thread.setName("data-preload-manager");
        thread.setDaemon(true);

        thread.start();
    }

    private void drive(long total) {
        long count = 0;

        Iterable<Collection<BlockPos>> chunks = ChunkedIterator.of(BlockPos.getAllInBox(
                this.min.x, 0, this.min.z,
                this.max.x, 0, this.max.z
        ), BATCH_SIZE);

        for (Collection<BlockPos> chunk : chunks) {
            for (BlockPos column : chunk) {
                DataView view = DataView.square(column.getX() << 4, column.getZ() << 4, 16);
                this.generator.generate(view);
            }

            CurrentThreadExecutor.blockOn(DataSourceReader.INSTANCE.finishLoading());

            count += chunk.size();
            this.notifyWatchers(count, total);

            if (this.canceled.get()) {
                return;
            }
        }
    }

    private void notifyWatchers(long count, long total) {
        UpdateDownloadMessage message = new UpdateDownloadMessage(this.id, count, total);
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
