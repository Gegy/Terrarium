package net.gegy1000.earth.server.world.cover;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.type.BareCover;
import net.gegy1000.earth.server.world.cover.type.BeachCover;
import net.gegy1000.earth.server.world.cover.type.BroadleafEvergreenCover;
import net.gegy1000.earth.server.world.cover.type.ClosedBroadleafDeciduousCover;
import net.gegy1000.earth.server.world.cover.type.ClosedNeedleleafEvergreenCover;
import net.gegy1000.earth.server.world.cover.type.CroplandWithVegetationCover;
import net.gegy1000.terrarium.server.world.cover.generator.PlaceholderCover;
import net.gegy1000.earth.server.world.cover.type.FloodedGrasslandCover;
import net.gegy1000.earth.server.world.cover.type.ForestShrublandWithGrassCover;
import net.gegy1000.earth.server.world.cover.type.FreshFloodedForestCover;
import net.gegy1000.earth.server.world.cover.type.GrassWithForestShrublandCover;
import net.gegy1000.earth.server.world.cover.type.GrasslandCover;
import net.gegy1000.earth.server.world.cover.type.IrrigatedCropsCover;
import net.gegy1000.earth.server.world.cover.type.MixedBroadNeedleleafCover;
import net.gegy1000.earth.server.world.cover.type.OpenBroadleafDeciduousCover;
import net.gegy1000.earth.server.world.cover.type.OpenNeedleleafCover;
import net.gegy1000.earth.server.world.cover.type.RainfedCropsCover;
import net.gegy1000.earth.server.world.cover.type.SalineFloodedForestCover;
import net.gegy1000.earth.server.world.cover.type.ShrublandCover;
import net.gegy1000.earth.server.world.cover.type.SnowCover;
import net.gegy1000.earth.server.world.cover.type.SparseVegetationCover;
import net.gegy1000.earth.server.world.cover.type.UrbanCover;
import net.gegy1000.earth.server.world.cover.type.VegetationWithCroplandCover;
import net.gegy1000.earth.server.world.cover.type.WaterCover;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthCoverRegistry {
    public static final CoverType BARE = new BareCover();
    public static final CoverType BEACH = new BeachCover();
    public static final CoverType BROADLEAF_EVERGREEN = new BroadleafEvergreenCover();
    public static final CoverType CLOSED_BROADLEAF_DECIDUOUS = new ClosedBroadleafDeciduousCover();
    public static final CoverType CLOSED_NEEDLELEAF_EVERGREEN = new ClosedNeedleleafEvergreenCover();
    public static final CoverType CROPLAND_WITH_VEGETATION = new CroplandWithVegetationCover();
    public static final CoverType DEBUG = new PlaceholderCover();
    public static final CoverType FLOODED_GRASSLAND = new FloodedGrasslandCover();
    public static final CoverType FOREST_SHRUBLAND_WITH_GRASS = new ForestShrublandWithGrassCover();
    public static final CoverType FRESH_FLOODED_FOREST = new FreshFloodedForestCover();
    public static final CoverType GRASSLAND = new GrasslandCover();
    public static final CoverType GRASS_WITH_FOREST_SHRUBLAND = new GrassWithForestShrublandCover();
    public static final CoverType IRRIGATED_CROPS = new IrrigatedCropsCover();
    public static final CoverType MIXED_BROAD_NEEEDLELEAF = new MixedBroadNeedleleafCover();
    public static final CoverType OPEN_BROADLEAF_DECIDUOUS = new OpenBroadleafDeciduousCover();
    public static final CoverType OPEN_NEEDLELEAF = new OpenNeedleleafCover();
    public static final CoverType RAINFED_CROPS = new RainfedCropsCover();
    public static final CoverType SALINE_FLOODED_FOREST = new SalineFloodedForestCover();
    public static final CoverType SHRUBLAND = new ShrublandCover();
    public static final CoverType SNOW = new SnowCover();
    public static final CoverType SPARSE_VEGETATION = new SparseVegetationCover();
    public static final CoverType URBAN = new UrbanCover();
    public static final CoverType VEGETATION_WITH_CROPLAND = new VegetationWithCroplandCover();
    public static final CoverType WATER = new WaterCover();

    @SubscribeEvent
    public static void onRegisterCover(CoverRegistry.TypeEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "bare"), BARE);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "beach"), BEACH);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "broadleaf_evergreen"), BROADLEAF_EVERGREEN);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "closed_broadleaf_deciduous"), CLOSED_BROADLEAF_DECIDUOUS);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "closed_needleleaf_evergreen"), CLOSED_NEEDLELEAF_EVERGREEN);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "cropland_with_vegetation"), CROPLAND_WITH_VEGETATION);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "debug"), DEBUG);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "flooded_grassland"), FLOODED_GRASSLAND);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "forest_shrubland_with_grass"), FOREST_SHRUBLAND_WITH_GRASS);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "fresh_flooded_forest"), FRESH_FLOODED_FOREST);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "grassland"), GRASSLAND);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "grass_with_forest_shrubland"), GRASS_WITH_FOREST_SHRUBLAND);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "irrigated_crops"), IRRIGATED_CROPS);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "mixed_broad_needleleaf"), MIXED_BROAD_NEEEDLELEAF);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "open_broadleaf_deciduous"), OPEN_BROADLEAF_DECIDUOUS);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "open_needleleaf"), OPEN_NEEDLELEAF);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "rainfed_crops"), RAINFED_CROPS);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "saline_flooded_forest"), SALINE_FLOODED_FOREST);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "shrubland"), SHRUBLAND);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "snow"), SNOW);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "sparse_vegetation"), SPARSE_VEGETATION);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "urban"), URBAN);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "vegetation_with_cropland"), VEGETATION_WITH_CROPLAND);
        event.register(new ResourceLocation(TerrariumEarth.MODID, "water"), WATER);
    }

    @SubscribeEvent
    public static void onRegisterContexts(CoverRegistry.ContextEvent event) {
        event.register(new ResourceLocation(TerrariumEarth.MODID, "earth_context"), new EarthCoverContext.Parser());
    }

    public enum Glob {
        IRRIGATED_CROPS(11, EarthCoverRegistry.IRRIGATED_CROPS),
        RAINFED_CROPS(14, EarthCoverRegistry.RAINFED_CROPS),
        CROPLAND_WITH_VEGETATION(20, EarthCoverRegistry.CROPLAND_WITH_VEGETATION),
        VEGETATION_WITH_CROPLAND(30, EarthCoverRegistry.VEGETATION_WITH_CROPLAND),
        BROADLEAF_EVERGREEN(40, EarthCoverRegistry.BROADLEAF_EVERGREEN),
        CLOSED_BROADLEAF_DECIDUOUS(50, EarthCoverRegistry.CLOSED_BROADLEAF_DECIDUOUS),
        OPEN_BROADLEAF_DECIDUOUS(60, EarthCoverRegistry.OPEN_BROADLEAF_DECIDUOUS),
        CLOSED_NEEDLELEAF_EVERGREEN(70, EarthCoverRegistry.CLOSED_NEEDLELEAF_EVERGREEN),
        OPEN_NEEDLELEAF(90, EarthCoverRegistry.OPEN_NEEDLELEAF),
        MIXED_BROAD_NEEDLELEAF(100, EarthCoverRegistry.MIXED_BROAD_NEEEDLELEAF),
        FOREST_SHRUBLAND_WITH_GRASS(110, EarthCoverRegistry.FOREST_SHRUBLAND_WITH_GRASS),
        GRASS_WITH_FOREST_SHRUBLAND(120, EarthCoverRegistry.GRASS_WITH_FOREST_SHRUBLAND),
        SHRUBLAND(130, EarthCoverRegistry.SHRUBLAND),
        GRASSLAND(140, EarthCoverRegistry.GRASSLAND),
        SPARSE_VEGETATION(150, EarthCoverRegistry.SPARSE_VEGETATION),
        FRESH_FLOODED_FOREST(160, EarthCoverRegistry.FRESH_FLOODED_FOREST),
        SALINE_FLOODED_FOREST(170, EarthCoverRegistry.SALINE_FLOODED_FOREST),
        FLOODED_GRASSLAND(180, EarthCoverRegistry.FLOODED_GRASSLAND),
        URBAN(190, EarthCoverRegistry.URBAN),
        BARE(200, EarthCoverRegistry.BARE),
        WATER(210, EarthCoverRegistry.WATER),
        SNOW(220, EarthCoverRegistry.SNOW),
        NO_DATA(0, EarthCoverRegistry.BARE);

        public static final Glob[] TYPES = Glob.values();
        private static final Byte2ObjectArrayMap<Glob> GLOB_IDS = new Byte2ObjectArrayMap<>();

        private final byte id;
        private final CoverType coverType;

        Glob(int id, CoverType coverType) {
            this.id = (byte) (id & 0xFf);
            this.coverType = coverType;
        }

        public int getId() {
            return this.id;
        }

        public CoverType getCoverType() {
            return this.coverType;
        }

        public static Glob get(int id) {
            return GLOB_IDS.getOrDefault((byte) (id & 0xFF), NO_DATA);
        }

        static {
            for (Glob glob : TYPES) {
                GLOB_IDS.put(glob.id, glob);
            }
        }
    }
}
