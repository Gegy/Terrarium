package net.gegy1000.earth.server.world.cover;

import com.google.common.collect.Iterators;
import net.gegy1000.earth.server.world.EarthCoverPriming;
import net.gegy1000.earth.server.world.EarthCoverDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

public enum Cover implements CoverSelector {
    NO(0),
    RAINFED_CROPLAND(10),
    HERBACEOUS_COVER(11),
    TREE_OR_SHRUB_COVER(12),
    IRRIGATED_CROPLAND(20),
    CROPLAND_WITH_VEGETATION(30),
    VEGETATION_WITH_CROPLAND(40),
    BROADLEAF_EVERGREEN(50),
    BROADLEAF_DECIDUOUS(60),
    BROADLEAF_DECIDUOUS_CLOSED(61),
    BROADLEAF_DECIDUOUS_OPEN(62),
    NEEDLEAF_EVERGREEN(70),
    NEEDLEAF_EVERGREEN_CLOSED(71),
    NEEDLEAF_EVERGREEN_OPEN(72),
    NEEDLEAF_DECIDUOUS(80),
    NEEDLEAF_DECIDUOUS_CLOSED(81),
    NEEDLEAF_DECIDUOUS_OPEN(82),
    MIXED_LEAF_TYPE(90),
    TREE_AND_SHRUB_WITH_HERBACEOUS_COVER(100),
    HERBACEOUS_COVER_WITH_TREE_AND_SHRUB(110),
    SHRUBLAND(120),
    SHRUBLAND_EVERGREEN(121),
    SHRUBLAND_DECIDUOUS(122),
    GRASSLAND(130),
    LICHENS_AND_MOSSES(140),
    SPARSE_VEGETATION(150),
    SPARSE_TREE(151),
    SPARSE_SHRUB(152),
    SPARSE_HERBACEOUS_COVER(153),
    FRESH_FLOODED_FOREST(160),
    SALINE_FLOODED_FOREST(170),
    FLOODED_VEGETATION(180),
    URBAN(190),
    BARE(200),
    BARE_CONSOLIDATED(201),
    BARE_UNCONSOLIDATED(202),
    WATER(210),
    PERMANENT_SNOW(220);

    private static final Cover[] ID_TO_COVER = new Cover[256];

    public final int id;

    private final Collection<Consumer<EarthCoverPriming.Builder>> primers = new ArrayList<>();
    private final Collection<Consumer<EarthCoverDecoration.Builder>> decorators = new ArrayList<>();

    Cover(int id) {
        this.id = id;
    }

    public void decorate(Consumer<EarthCoverDecoration.Builder> configurator) {
        this.decorators.add(configurator);
    }

    public void prime(Consumer<EarthCoverPriming.Builder> configurator) {
        this.primers.add(configurator);
    }

    public void configureDecorator(EarthCoverDecoration.Builder builder) {
        for (Consumer<EarthCoverDecoration.Builder> configurator : this.decorators) {
            configurator.accept(builder);
        }
    }

    public void configurePrimer(EarthCoverPriming.Builder builder) {
        for (Consumer<EarthCoverPriming.Builder> configurator : this.primers) {
            configurator.accept(builder);
        }
    }

    public boolean is(CoverMarker marker) {
        return marker.contains(this);
    }

    public static Cover byId(int id) {
        return ID_TO_COVER[id];
    }

    static {
        Arrays.fill(ID_TO_COVER, NO);
        for (Cover cover : Cover.values()) {
            ID_TO_COVER[cover.id] = cover;
        }
    }

    @Override
    public boolean contains(Cover cover) {
        return cover == this;
    }

    @Override
    public Iterator<Cover> iterator() {
        return Iterators.singletonIterator(this);
    }
}
