package net.gegy1000.earth.server.world.composer.structure;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.compatibility.ColumnCompatibilityWorld;
import net.gegy1000.earth.server.world.composer.structure.data.LossyColumnSet;
import net.gegy1000.earth.server.world.composer.structure.data.StructureStartMap;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// behaviour is largely cloned from MapGenStructure and MapGenBase, with the addition of much disgusting hackery
// to make generation work in a cube-based context
public final class ColumnStructureComposer implements StructureComposer {
    public static final long SEED = 5805869966374122221L;

    private static final int COLUMN_RANGE = 8;
    private static final int COMPAT_SURFACE_Y = 64;

    private final World world;
    private final String structureName;
    private final StructurePlacement placement;
    private final StartConstructor startConstructor;
    private final HeightFunction surfaceFunction;
    private final Map<EnumCreatureType, List<Biome.SpawnListEntry>> creatures;

    private final SpatialRandom random;
    private final ColumnCompatibilityWorld compatibilityWorld;

    private final StructureStartMap structureStarts = new StructureStartMap(1024, 0.75F);
    private MapGenStructureData structureData;

    private final LossyColumnSet preparedColumnCache = new LossyColumnSet(128);
    private final LossyColumnSet preparedBoundsCache = new LossyColumnSet(128);

    private final StructureBoundingBox mutableBounds = new StructureBoundingBox();
    private final HookedBoundingBox hookedBounds = new HookedBoundingBox();

    private ColumnStructureComposer(
            World world,
            String structureName,
            StructurePlacement placement,
            StartConstructor startConstructor,
            HeightFunction surfaceFunction,
            Map<EnumCreatureType, List<Biome.SpawnListEntry>> creatures
    ) {
        this.world = world;
        this.structureName = structureName;
        this.placement = placement;
        this.startConstructor = startConstructor;
        this.surfaceFunction = surfaceFunction;

        this.compatibilityWorld = new ColumnCompatibilityWorld(world);

        this.random = new SpatialRandom(world, SEED);
        this.creatures = creatures;
    }

    public static Builder builder() {
        return new Builder();
    }

    private MapGenStructureData prepareStructureData() {
        if (this.structureData != null) return this.structureData;

        MapStorage storage = this.world.getPerWorldStorage();
        this.structureData = (MapGenStructureData) storage.getOrLoadData(MapGenStructureData.class, this.structureName);

        if (this.structureData == null) {
            this.structureData = new MapGenStructureData(this.structureName);
            storage.setData(this.structureName, this.structureData);
        } else {
            this.deserializeStructureData(this.structureData.getTagCompound());
        }

        return this.structureData;
    }

    private void deserializeStructureData(NBTTagCompound root) {
        this.preparedColumnCache.clear();
        this.structureStarts.clear();

        for (String key : root.getKeySet()) {
            NBTBase tag = root.getTag(key);

            if (tag.getId() == Constants.NBT.TAG_COMPOUND) {
                NBTTagCompound compound = (NBTTagCompound) tag;

                if (compound.hasKey("ChunkX") && compound.hasKey("ChunkZ")) {
                    int chunkX = compound.getInteger("ChunkX");
                    int chunkZ = compound.getInteger("ChunkZ");

                    StructureStart start = MapGenStructureIO.getStructureStart(compound, this.world);
                    if (start != null && start.getChunkPosX() == chunkX && start.getChunkPosZ() == chunkZ) {
                        this.structureStarts.put(start);
                    } else {
                        TerrariumEarth.LOGGER.warn("Failed to deserialize structure start at ({}; {})", chunkX, chunkZ);
                    }
                }
            }
        }
    }

    private void writeStructureStart(StructureStart start) {
        MapGenStructureData data = this.prepareStructureData();

        int chunkX = start.getChunkPosX();
        int chunkZ = start.getChunkPosZ();
        data.writeInstance(start.writeStructureComponentsToNBT(chunkX, chunkZ), chunkX, chunkZ);
        data.markDirty();
    }

    @Override
    public final void prepareStructures(TerrariumWorld terrarium, CubicPos pos) {
        this.prepareColumns(terrarium, pos);
    }

    @Override
    public final void primeStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPrimeWriter writer) {
        this.prepareColumns(terrarium, pos);
    }

    private void prepareColumns(TerrariumWorld terrarium, CubicPos pos) {
        Profiler profiler = ThreadedProfiler.get();

        try (Profiler.Handle prepareColumns = profiler.push("prepare_columns")) {
            int originX = pos.getX();
            int originZ = pos.getZ();

            for (int x = originX - COLUMN_RANGE; x <= originX + COLUMN_RANGE; x++) {
                for (int z = originZ - COLUMN_RANGE; z <= originZ + COLUMN_RANGE; z++) {
                    this.prepareColumn(profiler, x, z);
                }
            }
        }
    }

    private void prepareColumn(Profiler profiler, int chunkX, int chunkZ) {
        // skip checking this column for structures if it has already been checked
        if (this.preparedColumnCache.set(chunkX, chunkZ)) {
            return;
        }

        this.prepareStructureData();

        if (this.structureStarts.contains(chunkX, chunkZ)) {
            return;
        }

        boolean canSpawn;
        try (Profiler.Handle testSpawn = profiler.push("test_spawn")) {
            canSpawn = this.placement.canSpawnAt(this.world, chunkX, chunkZ);
        }

        if (canSpawn) {
            StructureStart start;
            try (Profiler.Handle makeStart = profiler.push("make_start")) {
                this.random.setSeed(chunkX, chunkZ);
                start = this.makeStart(chunkX, chunkZ);
            }

            this.structureStarts.put(start);

            if (start.isSizeableStructure()) {
                this.writeStructureStart(start);
            }
        }
    }

    private StructureStart makeStart(int chunkX, int chunkZ) {
        ChunkPos columnPos = new ChunkPos(chunkX, chunkZ);
        ColumnCompatibilityWorld compatibilityWorld = this.getCompatibilityWorldFor(columnPos);

        return this.startConstructor.makeStart(compatibilityWorld, this.random, chunkX, chunkZ);
    }

    private ColumnCompatibilityWorld getCompatibilityWorldFor(ChunkPos columnPos) {
        int minY = this.getColumnOffsetFor(columnPos);
        return this.getCompatibilityWorldFor(columnPos, minY);
    }

    private ColumnCompatibilityWorld getCompatibilityWorldFor(ChunkPos columnPos, int minY) {
        this.compatibilityWorld.setupAt(columnPos, minY);
        return this.compatibilityWorld;
    }

    private int getColumnOffsetFor(ChunkPos columnPos) {
        int surfaceY = this.surfaceFunction.apply(columnPos.getXStart() + 8, columnPos.getZStart() + 8);
        surfaceY = Math.max(surfaceY, this.world.getSeaLevel());

        return surfaceY - COMPAT_SURFACE_Y;
    }

    @Override
    public final void populateStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        this.prepareStructureData();

        Profiler profiler = ThreadedProfiler.get();

        this.random.setSeed(pos.getCenterX(), pos.getCenterZ());

        ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());

        for (StructureStart start : this.structureStarts) {
            ChunkPos startColumnPos = new ChunkPos(start.getChunkPosX(), start.getChunkPosZ());
            int columnOffsetY = this.getColumnOffsetFor(startColumnPos);

            StructureBoundingBox cubeBounds = this.getCubeBounds(pos, columnOffsetY);

            // TODO: isValidForPostProcess (how do? how is it used in vanilla?)
            if (start.isSizeableStructure() /*&& start.isValidForPostProcess(columnPos)*/ && start.getBoundingBox().intersectsWith(cubeBounds)) {
                ColumnCompatibilityWorld compatibilityWorld = this.getCompatibilityWorldFor(startColumnPos, columnOffsetY);

                // village generation only sets its components' correct bounding boxes when addComponentParts is called
                // this is usually fine, however when we're generating cubes: the bounds intersection check needs to
                // consider y-values, which means the component needs to know the y-coordinate that it will generate at.

                // here we call addComponentParts while disabling any updates to the world
                if (!this.preparedBoundsCache.set(pos.getX(), pos.getZ())) {
                    this.hookedBounds.set(cubeBounds);
                    this.hookedBounds.minY = Integer.MIN_VALUE;
                    this.hookedBounds.maxY = Integer.MIN_VALUE;

                    for (StructureComponent component : start.getComponents()) {
                        component.addComponentParts(compatibilityWorld, this.random, this.hookedBounds);
                    }
                }

                this.hookedBounds.set(cubeBounds);
                start.generateStructure(compatibilityWorld, this.random, this.hookedBounds);
                start.notifyPostProcessAt(columnPos);

                this.writeStructureStart(start);
            }
        }
    }

    private StructureBoundingBox getCubeBounds(CubicPos pos, int offset) {
        this.mutableBounds.minX = pos.getCenterX();
        this.mutableBounds.minY = pos.getCenterY() - offset;
        this.mutableBounds.minZ = pos.getCenterZ();
        this.mutableBounds.maxX = this.mutableBounds.minX + 15;
        this.mutableBounds.maxY = this.mutableBounds.minY + 15;
        this.mutableBounds.maxZ = this.mutableBounds.minZ + 15;
        return this.mutableBounds;
    }

    @Override
    public final boolean isInsideStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos) {
        if (!this.structureName.equals(name)) return false;

        for (StructureStart start : this.structureStarts) {
            if (start.isSizeableStructure() && start.getBoundingBox().isVecInside(pos)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos, boolean findUnexplored) {
        if (this.structureName.equals(name)) {
            return this.placement.getClosestTo(world, pos, findUnexplored);
        }
        return null;
    }

    @Nullable
    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(TerrariumWorld terrarium, World world, EnumCreatureType type, BlockPos pos) {
        if (this.isInsideStructure(terrarium, world, this.structureName, pos)) {
            return this.creatures.get(type);
        }
        return null;
    }

    public interface StartConstructor {
        StructureStart makeStart(World world, Random random, int chunkX, int chunkZ);
    }

    public static class Builder {
        private String structureName;
        private StructurePlacement placement;
        private StartConstructor startConstructor;
        private HeightFunction surfaceFunction = (x, z) -> 64;

        private final Map<EnumCreatureType, List<Biome.SpawnListEntry>> creatures = new EnumMap<>(EnumCreatureType.class);

        Builder() {
        }

        public Builder setStructureName(String structureName) {
            this.structureName = structureName;
            return this;
        }

        public Builder setPlacement(StructurePlacement placement) {
            this.placement = placement;
            return this;
        }

        public Builder setStartConstructor(StartConstructor startConstructor) {
            this.startConstructor = startConstructor;
            return this;
        }

        public Builder setSurfaceFunction(HeightFunction surfaceFunction) {
            this.surfaceFunction = surfaceFunction;
            return this;
        }

        public Builder addCreatures(EnumCreatureType type, Biome.SpawnListEntry... entries) {
            List<Biome.SpawnListEntry> creatures = this.creatures.computeIfAbsent(type, t -> new ArrayList<>());
            Collections.addAll(creatures, entries);
            return this;
        }

        public ColumnStructureComposer build(World world) {
            return new ColumnStructureComposer(
                    world,
                    Preconditions.checkNotNull(this.structureName, "no structure name"),
                    Preconditions.checkNotNull(this.placement, "no placement"),
                    Preconditions.checkNotNull(this.startConstructor, "no start constructor"),
                    this.surfaceFunction,
                    this.creatures
            );
        }
    }

    static class HookedBoundingBox extends StructureBoundingBox {
        @Override
        public boolean isVecInside(Vec3i vec) {
            // some structure generation does a check to see if an x, z is within the bounds; however it hardcodes y=64
            // this is a horrible hack, but the best option I could find that is not asm
            if (vec.getY() == 64) {
                boolean horizontalCheck = vec.getX() >= this.minX && vec.getZ() >= this.minZ
                        && vec.getX() <= this.maxX && vec.getZ() <= this.maxZ;

                if (!horizontalCheck) return false;

                Class caller = GetCaller.INSTANCE.getCaller();
                if (isBoundCheckSpecialCase(caller)) {
                    return true;
                }
            }

            return super.isVecInside(vec);
        }

        private static boolean isBoundCheckSpecialCase(Class caller) {
            if (caller == StructureVillagePieces.Path.class || caller == StructureVillagePieces.Village.class) {
                return true;
            }

            // target ComponentScatteredFeaturePieces$Feature
            return caller.isMemberClass()
                    && caller.getEnclosingClass() == ComponentScatteredFeaturePieces.class
                    && caller != ComponentScatteredFeaturePieces.SwampHut.class;
        }

        public void set(StructureBoundingBox from) {
            this.minX = from.minX;
            this.minY = from.minY;
            this.minZ = from.minZ;
            this.maxX = from.maxX;
            this.maxY = from.maxY;
            this.maxZ = from.maxZ;
        }
    }

    // hacky code to access caller class context (with better performance than Thread#getStacktrace)
    static class GetCaller extends SecurityManager {
        static final GetCaller INSTANCE = new GetCaller();

        private GetCaller() {
        }

        public Class getCaller() {
            Class[] ctx = this.getClassContext();
            return ctx[2];
        }
    }
}
