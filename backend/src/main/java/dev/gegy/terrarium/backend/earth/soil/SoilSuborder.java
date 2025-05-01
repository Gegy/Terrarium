package dev.gegy.terrarium.backend.earth.soil;

import com.mojang.serialization.Codec;
import dev.gegy.terrarium.backend.util.Util;

import java.util.Arrays;

public enum SoilSuborder {
    NONE(255, "none", SoilOrder.NONE),
    OCEAN(0, "ocean", SoilOrder.NONE),
    SHIFTING_SAND(1, "shifting_sand", SoilOrder.NONE),
    ROCK(2, "rock", SoilOrder.NONE),
    ICE(3, "ice", SoilOrder.NONE),
    HISTELS(5, "histels", SoilOrder.GELISOL),
    TURBELS(6, "turbels", SoilOrder.GELISOL),
    ORTHELS(7, "orthels", SoilOrder.GELISOL),
    FOLISTS(10, "folists", SoilOrder.HISTOSOL),
    FIBRISTS(11, "fibrists", SoilOrder.HISTOSOL),
    HEMISTS(12, "hemists", SoilOrder.HISTOSOL),
    SAPRISTS(13, "saprists", SoilOrder.HISTOSOL),
    AQUODS(15, "aquods", SoilOrder.SPODOSOL),
    CRYODS(16, "cryods", SoilOrder.SPODOSOL),
    HUMODS(17, "humods", SoilOrder.SPODOSOL),
    ORTHODS(18, "orthods", SoilOrder.SPODOSOL),
    GELODS(19, "gelods", SoilOrder.SPODOSOL),
    AQUANDS(20, "aquands", SoilOrder.ANDISOL),
    CRYANDS(21, "cryands", SoilOrder.ANDISOL),
    TORRANDS(22, "torrands", SoilOrder.ANDISOL),
    XERANDS(23, "xerands", SoilOrder.ANDISOL),
    VITRANDS(24, "vitrands", SoilOrder.ANDISOL),
    USTANDS(25, "ustands", SoilOrder.ANDISOL),
    UDANDS(26, "udands", SoilOrder.ANDISOL),
    GELANDS(27, "gelands", SoilOrder.ANDISOL),
    AQUOX(30, "aquox", SoilOrder.OXISOL),
    TORROX(31, "torrox", SoilOrder.OXISOL),
    USTOX(32, "ustox", SoilOrder.OXISOL),
    PEROX(33, "perox", SoilOrder.OXISOL),
    UDOX(34, "udox", SoilOrder.OXISOL),
    AQUERTS(40, "aquerts", SoilOrder.VERTISOL),
    CRYERTS(41, "cryerts", SoilOrder.VERTISOL),
    XERERTS(42, "xererts", SoilOrder.VERTISOL),
    TORRERTS(43, "torrerts", SoilOrder.VERTISOL),
    USTERTS(44, "usterts", SoilOrder.VERTISOL),
    UDERTS(45, "uderts", SoilOrder.VERTISOL),
    CRYIDS(50, "cryids", SoilOrder.ARIDISOL),
    SALIDS(51, "salids", SoilOrder.ARIDISOL),
    DURIDS(52, "durids", SoilOrder.ARIDISOL),
    GYPSIDS(53, "gypsids", SoilOrder.ARIDISOL),
    ARGIDS(54, "argids", SoilOrder.ARIDISOL),
    CALCIDS(55, "calcids", SoilOrder.ARIDISOL),
    CAMBIDS(56, "cambids", SoilOrder.ARIDISOL),
    AQUULTS(60, "aquults", SoilOrder.ULTISOL),
    HUMULTS(61, "humults", SoilOrder.ULTISOL),
    UDULTS(62, "udults", SoilOrder.ULTISOL),
    USTULTS(63, "ustults", SoilOrder.ULTISOL),
    XERULTS(64, "xerults", SoilOrder.ULTISOL),
    BOROLLS(69, "borolls", SoilOrder.MOLLISOL),
    ALBOLLS(70, "albolls", SoilOrder.MOLLISOL),
    AQUOLLS(71, "aquolls", SoilOrder.MOLLISOL),
    RENDOLLS(72, "rendolls", SoilOrder.MOLLISOL),
    XEROLLS(73, "xerolls", SoilOrder.MOLLISOL),
    CRYOLLS(74, "cryolls", SoilOrder.MOLLISOL),
    USTOLLS(75, "ustolls", SoilOrder.MOLLISOL),
    UDOLLS(76, "udolls", SoilOrder.MOLLISOL),
    GELOLLS(77, "gelolls", SoilOrder.ALFISOL),
    AQUALFS(80, "aqualfs", SoilOrder.ALFISOL),
    CRYALFS(81, "cryalfs", SoilOrder.ALFISOL),
    USTALFS(82, "ustalfs", SoilOrder.ALFISOL),
    XERALFS(83, "xeralfs", SoilOrder.ALFISOL),
    UDALFS(84, "udalfs", SoilOrder.ALFISOL),
    UDEPTS(85, "udepts", SoilOrder.INCEPTISOL),
    GELEPTS(86, "gelepts", SoilOrder.INCEPTISOL),
    OCHREPTS(89, "ochrepts", SoilOrder.INCEPTISOL),
    AQUEPTS(90, "aquepts", SoilOrder.INCEPTISOL),
    ANTHREPTS(91, "anthrepts", SoilOrder.INCEPTISOL),
    CRYEPTS(92, "cryepts", SoilOrder.INCEPTISOL),
    USTEPTS(93, "ustepts", SoilOrder.INCEPTISOL),
    XEREPTS(94, "xerepts", SoilOrder.INCEPTISOL),
    AQUENTS(95, "aquents", SoilOrder.ENTISOL),
    ARENTS(96, "arents", SoilOrder.ENTISOL),
    PSAMMENTS(97, "psamments", SoilOrder.ENTISOL),
    FLUVENTS(98, "fluvents", SoilOrder.ENTISOL),
    ORTHENTS(99, "orthents", SoilOrder.ENTISOL);

    public static final Codec<SoilSuborder> CODEC = Util.stringLookupCodec(values(), SoilSuborder::getName);

    private static final SoilSuborder[] LOOKUP = new SoilSuborder[256];

    private final int id;
    private final String name;
    private final SoilOrder order;

    SoilSuborder(final int id, final String name, final SoilOrder order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public SoilOrder order() {
        return order;
    }

    public static SoilSuborder byId(final int id) {
        if (id < 0 || id >= LOOKUP.length) {
            return SoilSuborder.NONE;
        }
        return LOOKUP[id];
    }

    public String getName() {
        return name;
    }

    static {
        Arrays.fill(LOOKUP, NONE);
        for (final SoilSuborder soil : SoilSuborder.values()) {
            LOOKUP[soil.id] = soil;
        }
    }
}
