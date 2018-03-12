package net.gegy1000.terrarium.server.world;

// TODO: Earth specific
public enum LatitudinalZone {
    TROPICS(0.0, 23.0),
    SUBTROPICS(23.0, 35.0),
    TEMPERATE(35.0, 66.0),
    FRIGID(66.0, 90.0);

    public static final LatitudinalZone[] ZONES = values();

    private final double lowerLatitude;
    private final double upperLatitude;

    LatitudinalZone(double lowerLatitude, double upperLatitude) {
        this.lowerLatitude = lowerLatitude;
        this.upperLatitude = upperLatitude;
    }

    public static LatitudinalZone get(double latitude) {
        double absoluteLatitude = Math.abs(latitude);
        for (LatitudinalZone zone : ZONES) {
            if (absoluteLatitude >= zone.lowerLatitude && absoluteLatitude < zone.upperLatitude) {
                return zone;
            }
        }
        return LatitudinalZone.TEMPERATE;
    }
}
