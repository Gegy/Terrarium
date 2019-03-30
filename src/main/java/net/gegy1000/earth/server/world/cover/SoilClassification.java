package net.gegy1000.earth.server.world.cover;

public enum SoilClassification {
    NOT_SOIL(0),
    ACRISOLS(1),
    ALEBLUVISOLS(2),
    ALISOLS(3),
    ANDOSOLS(4),
    ARENOSOLS(5),
    CALCISOLS(6),
    CAMBISOLS(7),
    CHERNOZEMS(8),
    CRYOSOLS(9),
    DURISOLS(10),
    FERRALSOLS(11),
    FLUVISOLS(12),
    GLEYSOLS(13),
    GYPSISOLS(14),
    HISTOSOLS(15),
    KASTANOZEMS(16),
    LEPTOSOLS(17),
    LIXISOLS(18),
    LUVISOLS(19),
    NITISOLS(20),
    PHAEOZEMS(21),
    PLANOSOLS(22),
    PLINTHOSOLS(23),
    PODZOLS(24),
    REGOSOLS(25),
    SOLONCHAKS(26),
    SOLONETZ(27),
    STAGNOSOLS(28),
    UMBRISOLS(29),
    VERTISOLS(30);

    public static final SoilClassification[] TYPES = SoilClassification.values();
    public static final SoilClassification[] CLASSIFICATION_IDS = new SoilClassification[256];

    private final byte id;

    SoilClassification(int id) {
        this.id = (byte) (id & 0xFF);
    }

    public int getId() {
        return this.id;
    }

    public static SoilClassification get(int id) {
        SoilClassification classification = CLASSIFICATION_IDS[id & 0xFF];
        if (classification == null) {
            return NOT_SOIL;
        }
        return classification;
    }

    static {
        for (SoilClassification classification : TYPES) {
            CLASSIFICATION_IDS[classification.id & 0xFF] = classification;
        }
    }
}
