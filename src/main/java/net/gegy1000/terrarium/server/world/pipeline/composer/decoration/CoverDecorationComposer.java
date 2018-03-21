package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverTypeRegistry;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class CoverDecorationComposer implements DecorationComposer {
    private static final long DECORATION_SEED = 2492037454623254033L;

    private final Random random;
    private final PseudoRandomMap coverMap;

    private final RegionComponentType<CoverRasterTileAccess> coverComponent;

    private final Set<CoverType> coverTypes = new HashSet<>();
    private final Map<CoverType, CoverDecorationGenerator> generators = new HashMap<>();

    public CoverDecorationComposer(
            World world, TerrariumWorldData worldData,
            RegionComponentType<CoverRasterTileAccess> coverComponent,
            RegionComponentType<ShortRasterTileAccess> heightComponent,
            RegionComponentType<ByteRasterTileAccess> slopeComponent,
            Collection<CoverType> bundledCover
    ) {
        this.random = new Random(world.getSeed() ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(world.getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        GenerationRegionHandler regionHandler = worldData.getRegionHandler();
        CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(coverComponent);
        ShortRasterTileAccess heightRaster = regionHandler.getCachedChunkRaster(heightComponent);
        ByteRasterTileAccess slopeRaster = regionHandler.getCachedChunkRaster(slopeComponent);
        CoverGenerationContext context = new CoverGenerationContext(world, heightRaster, coverRaster, slopeRaster);

        for (CoverType coverType : bundledCover) {
            this.generators.put(coverType, coverType.createDecorationGenerator(context));
        }
    }

    @Override
    public void decorateChunk(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);

        this.coverTypes.clear();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                this.coverTypes.add(coverRaster.get(localX, localZ));
            }
        }

        this.coverMap.initPosSeed(globalX, globalZ);
        long randomSeed = this.coverMap.next();

        for (CoverType type : this.coverTypes) {
            CoverDecorationGenerator generator = this.generators.get(type);
            if (generator != null) {
                this.random.setSeed(randomSeed);
                // TODO: Handle latitudinal zone
                generator.decorate(globalX + 8, globalZ + 8, this.random);
            }
        }
    }

    public static class Parser implements InstanceObjectParser<DecorationComposer> {
        @Override
        public DecorationComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            RegionComponentType<ByteRasterTileAccess> slopeComponent = valueParser.parseComponentType(objectRoot, "slope_component", ByteRasterTileAccess.class);
            Collection<CoverType> coverBundle = valueParser.parseIdBundle(objectRoot, "cover_bundle", CoverTypeRegistry.getRegistry());
            return new CoverDecorationComposer(world, worldData, coverComponent, heightComponent, slopeComponent, coverBundle);
        }
    }
}
