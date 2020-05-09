package net.gegy1000.earth.server.world.ecology.soil;

import java.util.Arrays;

public enum SoilSuborder {
    NO(255, SoilOrder.NO),
    OCEAN(0, SoilOrder.NO),
    SHIFTING_SAND(1, SoilOrder.NO),
    ROCK(2, SoilOrder.NO),
    ICE(3, SoilOrder.NO),
    HISTELS(5, SoilOrder.GELISOL),
    TURBELS(6, SoilOrder.GELISOL),
    ORTHELS(7, SoilOrder.GELISOL),
    FOLISTS(10, SoilOrder.HISTOSOL),
    FIBRISTS(11, SoilOrder.HISTOSOL),
    HEMISTS(12, SoilOrder.HISTOSOL),
    SAPRISTS(13, SoilOrder.HISTOSOL),
    AQUODS(15, SoilOrder.SPODOSOL),
    CRYODS(16, SoilOrder.SPODOSOL),
    HUMODS(17, SoilOrder.SPODOSOL),
    ORTHODS(18, SoilOrder.SPODOSOL),
    GELODS(19, SoilOrder.SPODOSOL),
    AQUANDS(20, SoilOrder.ANDISOL),
    CRYANDS(21, SoilOrder.ANDISOL),
    TORRANDS(22, SoilOrder.ANDISOL),
    XERANDS(23, SoilOrder.ANDISOL),
    VITRANDS(24, SoilOrder.ANDISOL),
    USTANDS(25, SoilOrder.ANDISOL),
    UDANDS(26, SoilOrder.ANDISOL),
    GELANDS(27, SoilOrder.ANDISOL),
    AQUOX(30, SoilOrder.OXISOL),
    TORROX(31, SoilOrder.OXISOL),
    USTOX(32, SoilOrder.OXISOL),
    PEROX(33, SoilOrder.OXISOL),
    UDOX(34, SoilOrder.OXISOL),
    AQUERTS(40, SoilOrder.VERTISOL),
    CRYERTS(41, SoilOrder.VERTISOL),
    XERERTS(42, SoilOrder.VERTISOL),
    TORRERTS(43, SoilOrder.VERTISOL),
    USTERTS(44, SoilOrder.VERTISOL),
    UDERTS(45, SoilOrder.VERTISOL),
    CRYIDS(50, SoilOrder.ARIDISOL),
    SALIDS(51, SoilOrder.ARIDISOL),
    DURIDS(52, SoilOrder.ARIDISOL),
    GYPSIDS(53, SoilOrder.ARIDISOL),
    ARGIDS(54, SoilOrder.ARIDISOL),
    CALCIDS(55, SoilOrder.ARIDISOL),
    CAMBIDS(56, SoilOrder.ARIDISOL),
    AQUULTS(60, SoilOrder.ULTISOL),
    HUMULTS(61, SoilOrder.ULTISOL),
    UDULTS(62, SoilOrder.ULTISOL),
    USTULTS(63, SoilOrder.ULTISOL),
    XERULTS(64, SoilOrder.ULTISOL),
    BOROLLS(69, SoilOrder.MOLLISOL),
    ALBOLLS(70, SoilOrder.MOLLISOL),
    AQUOLLS(71, SoilOrder.MOLLISOL),
    RENDOLLS(72, SoilOrder.MOLLISOL),
    XEROLLS(73, SoilOrder.MOLLISOL),
    CRYOLLS(74, SoilOrder.MOLLISOL),
    USTOLLS(75, SoilOrder.MOLLISOL),
    UDOLLS(76, SoilOrder.MOLLISOL),
    GELOLLS(77, SoilOrder.ALFISOL),
    AQUALFS(80, SoilOrder.ALFISOL),
    CRYALFS(81, SoilOrder.ALFISOL),
    USTALFS(82, SoilOrder.ALFISOL),
    XERALFS(83, SoilOrder.ALFISOL),
    UDALFS(84, SoilOrder.ALFISOL),
    UDEPTS(85, SoilOrder.INCEPTISOL),
    GELEPTS(86, SoilOrder.INCEPTISOL),
    OCHREPTS(89, SoilOrder.INCEPTISOL),
    AQUEPTS(90, SoilOrder.INCEPTISOL),
    ANTHREPTS(91, SoilOrder.INCEPTISOL),
    CRYEPTS(92, SoilOrder.INCEPTISOL),
    USTEPTS(93, SoilOrder.INCEPTISOL),
    XEREPTS(94, SoilOrder.INCEPTISOL),
    AQUENTS(95, SoilOrder.ENTISOL),
    ARENTS(96, SoilOrder.ENTISOL),
    PSAMMENTS(97, SoilOrder.ENTISOL),
    FLUVENTS(98, SoilOrder.ENTISOL),
    ORTHENTS(99, SoilOrder.ENTISOL);

    private static final SoilSuborder[] ID_TO_SOIL = new SoilSuborder[256];

    public final int id;
    public final SoilOrder order;

    SoilSuborder(int id, SoilOrder order) {
        this.id = id;
        this.order = order;
    }

    public static SoilSuborder byId(int id) {
        return ID_TO_SOIL[id];
    }

    static {
        Arrays.fill(ID_TO_SOIL, NO);
        for (SoilSuborder soil : SoilSuborder.values()) {
            ID_TO_SOIL[soil.id] = soil;
        }
    }
}
