package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.chunk.PseudoRandomMap;
import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.EnumMap;
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
    private final Map<CoverType, CoverGenerator> generators = new EnumMap<>(CoverType.class);

    public CoverDecorationComposer(
            World world, TerrariumWorldData worldData,
            RegionComponentType<CoverRasterTileAccess> coverComponent,
            RegionComponentType<ShortRasterTileAccess> heightComponent,
            RegionComponentType<ByteRasterTileAccess> slopeComponent
    ) {
        this.random = new Random(world.getSeed() ^ DECORATION_SEED);
        this.coverMap = new PseudoRandomMap(world.getSeed(), this.random.nextLong());

        this.coverComponent = coverComponent;

        IBlockState[] coverBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], Blocks.AIR.getDefaultState());
        IBlockState[] fillerBlockBuffer = ArrayUtils.defaulted(new IBlockState[16 * 16], Blocks.AIR.getDefaultState());

        // TODO: Refer to CoverSurfaceComposer
        GenerationRegionHandler regionHandler = worldData.getRegionHandler();
        for (CoverType coverType : CoverType.TYPES) {
            CoverGenerator generator = coverType.createGenerator();
            CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(coverComponent);
            ShortRasterTileAccess heightRaster = regionHandler.getCachedChunkRaster(heightComponent);
            ByteRasterTileAccess slopeRaster = regionHandler.getCachedChunkRaster(slopeComponent);
            generator.initialize(world, coverRaster, heightRaster, slopeRaster, coverBlockBuffer, fillerBlockBuffer, false);
            this.generators.put(coverType, generator);
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
            CoverGenerator generator = this.generators.get(type);
            if (generator != null) {
                this.random.setSeed(randomSeed);
                // TODO: Handle latitudinal zone
                generator.decorate(this.random, LatitudinalZone.TROPICS, globalX + 8, globalZ + 8);
            }
        }
    }

    public static class Parser implements InstanceObjectParser<DecorationComposer> {
        @Override
        public DecorationComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            RegionComponentType<ByteRasterTileAccess> slopeComponent = valueParser.parseComponentType(objectRoot, "slope_component", ByteRasterTileAccess.class);
            return new CoverDecorationComposer(world, worldData, coverComponent, heightComponent, slopeComponent);
        }
    }
}
