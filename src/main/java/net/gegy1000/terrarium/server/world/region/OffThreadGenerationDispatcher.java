package net.gegy1000.terrarium.server.world.region;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OffThreadGenerationDispatcher implements RegionGenerationDispatcher {
    private final Function<RegionTilePos, GenerationRegion> generator;

    private final ExecutorService regionLoadService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("terrarium-region-loader").setDaemon(true).build());

    private final Map<RegionTilePos, RegionFuture> queuedRegions = new HashMap<>();

    public OffThreadGenerationDispatcher(Function<RegionTilePos, GenerationRegion> generator) {
        this.generator = generator;
    }

    @Override
    public void setRequiredRegions(Collection<RegionTilePos> regions) {
        for (RegionTilePos pos : regions) {
            if (!this.queuedRegions.containsKey(pos)) {
                this.enqueueRegion(pos);
            }
        }

        Set<RegionTilePos> untrackedLoadingRegions = this.queuedRegions.keySet().stream()
                .filter(pos -> !regions.contains(pos))
                .collect(Collectors.toSet());

        for (RegionTilePos pos : untrackedLoadingRegions) {
            RegionFuture region = this.queuedRegions.remove(pos);
            region.cancel();
        }
    }

    @Override
    public Collection<GenerationRegion> collectCompletedRegions() {
        if (this.queuedRegions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<RegionTilePos, GenerationRegion> completedRegions = new HashMap<>();
        for (RegionFuture future : this.queuedRegions.values()) {
            if (future.isComplete()) {
                GenerationRegion region = future.getGeneratedRegion();
                completedRegions.put(future.pos, region);
            }
        }

        completedRegions.keySet().forEach(this.queuedRegions::remove);

        return completedRegions.values();
    }

    @Override
    public GenerationRegion get(RegionTilePos pos) {
        RegionFuture regionFuture = this.queuedRegions.get(pos);
        if (regionFuture == null) {
            regionFuture = this.enqueueRegion(pos);
        }

        GenerationRegion generatedRegion = regionFuture.getGeneratedRegion();
        this.queuedRegions.remove(regionFuture.pos);

        return generatedRegion;
    }

    @Override
    public void close() {
        this.regionLoadService.shutdownNow();
    }

    private RegionFuture enqueueRegion(RegionTilePos pos) {
        RegionFuture regionFuture = new RegionFuture(pos);
        regionFuture.submitTo(this.regionLoadService);
        this.queuedRegions.put(pos, regionFuture);
        return regionFuture;
    }

    private class RegionFuture {
        private final RegionTilePos pos;

        private Future<GenerationRegion> future;

        private RegionFuture(RegionTilePos pos) {
            this.pos = pos;
        }

        public void cancel() {
            if (this.future != null) {
                this.future.cancel(true);
            }
        }

        public boolean isComplete() {
            return this.future.isDone() || this.future.isCancelled();
        }

        @Nullable
        public GenerationRegion getGeneratedRegion() {
            try {
                return this.future.get();
            } catch (InterruptedException | ExecutionException e) {
                Terrarium.LOGGER.error("Failed to retrieve generated region", e);
                return null;
            }
        }

        public void submitTo(ExecutorService service) {
            this.future = service.submit(() -> OffThreadGenerationDispatcher.this.generator.apply(this.pos));
        }
    }
}
