package net.gegy1000.earth.server.world.cover;

public final class CoverSelectors {
    private static final CoverSelector DECIDUOUS = CoverMarkers.DECIDUOUS.or(CoverMarkers.EVERGREEN.not());
    private static final CoverSelector EVERGREEN = CoverMarkers.EVERGREEN.or(CoverMarkers.DECIDUOUS.not());
    private static final CoverSelector NEEDLELEAF = CoverMarkers.NEEDLELEAF.or(CoverMarkers.BROADLEAF.not());
    private static final CoverSelector BROADLEAF = CoverMarkers.BROADLEAF.or(CoverMarkers.NEEDLELEAF.not());

    private static final CoverSelector BROADLEAF_EVERGREEN = BROADLEAF.and(EVERGREEN);
    private static final CoverSelector BROADLEAF_DECIDUOUS = BROADLEAF.and(DECIDUOUS);
    private static final CoverSelector NEEDLELEAF_EVERGREEN = NEEDLELEAF.and(EVERGREEN);
    private static final CoverSelector NEEDLELEAF_DECIDUOUS = NEEDLELEAF.and(DECIDUOUS);

    public static CoverSelector broadleaf() {
        return BROADLEAF;
    }

    public static CoverSelector needleleaf() {
        return NEEDLELEAF;
    }

    public static CoverSelector evergreen() {
        return EVERGREEN;
    }

    public static CoverSelector deciduous() {
        return DECIDUOUS;
    }

    public static CoverSelector broadleafEvergreen() {
        return BROADLEAF_EVERGREEN;
    }

    public static CoverSelector broadleafDeciduous() {
        return BROADLEAF_DECIDUOUS;
    }

    public static CoverSelector needleleafEvergreen() {
        return NEEDLELEAF_EVERGREEN;
    }

    public static CoverSelector needleleafDeciduous() {
        return NEEDLELEAF_DECIDUOUS;
    }
}
