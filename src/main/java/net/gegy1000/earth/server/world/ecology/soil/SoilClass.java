package net.gegy1000.earth.server.world.ecology.soil;

import java.util.Arrays;

public enum SoilClass {
    NO(255),
    OCEAN(0),
    SHIFTING_SAND(1),
    ROCK(2),
    ICE(3),
    HISTELS(5),
    TURBELS(6),
    ORTHELS(7),
    FOLISTS(10),
    FIBRISTS(11),
    HEMISTS(12),
    SAPRISTS(13),
    AQUODS(15),
    CRYODS(16),
    HUMODS(17),
    ORTHODS(18),
    GELODS(19),
    AQUANDS(20),
    CRYANDS(21),
    TORRANDS(22),
    XERANDS(23),
    VITRANDS(24),
    USTANDS(25),
    UDANDS(26),
    GELANDS(27),
    AQUOX(30),
    TORROX(31),
    USTOX(32),
    PEROX(33),
    UDOX(34),
    AQUERTS(40),
    CRYERTS(41),
    XERERTS(42),
    TORRERTS(43),
    USTERTS(44),
    UDERTS(45),
    CRYIDS(50),
    SALIDS(51),
    DURIDS(52),
    GYPSIDS(53),
    ARGIDS(54),
    CALCIDS(55),
    CAMBIDS(56),
    AQUULTS(60),
    HUMULTS(61),
    UDULTS(62),
    USTULTS(63),
    XERULTS(64),
    BOROLLS(69),
    ALBOLLS(70),
    AQUOLLS(71),
    RENDOLLS(72),
    XEROLLS(73),
    CRYOLLS(74),
    USTOLLS(75),
    UDOLLS(76),
    GELOLLS(77),
    AQUALFS(80),
    CRYALFS(81),
    USTALFS(82),
    XERALFS(83),
    UDALFS(84),
    UDEPTS(85),
    GELEPTS(86),
    OCHREPTS(89),
    AQUEPTS(90),
    ANTHREPTS(91),
    CRYEPTS(92),
    USTEPTS(93),
    XEREPTS(94),
    AQUENTS(95),
    ARENTS(96),
    PSAMMENTS(97),
    FLUVENTS(98),
    ORTHENTS(99);

    private static final SoilClass[] ID_TO_SOIL = new SoilClass[256];

    public final int id;

    SoilClass(int id) {
        this.id = id;
    }

    public static SoilClass byId(int id) {
        return ID_TO_SOIL[id];
    }

    public boolean isMaybeRock() {
        return this == ORTHENTS;
    }

    public boolean isSandy() {
        switch (this) {
            case SHIFTING_SAND:
            case CRYIDS:
            case SALIDS:
            case DURIDS:
            case GYPSIDS:
            case ARGIDS:
            case CALCIDS:
            case CAMBIDS:
            case PSAMMENTS:
                return true;
            default:
                return false;
        }
    }

    public boolean isRock() {
        return this == SoilClass.ROCK;
    }

    public boolean isIce() {
        return this == ICE;
    }

    public boolean isPermafrost() {
        return this == HISTELS || this == TURBELS || this == ORTHELS;
    }

    static {
        Arrays.fill(ID_TO_SOIL, NO);
        for (SoilClass soil : SoilClass.values()) {
            ID_TO_SOIL[soil.id] = soil;
        }
    }
}
