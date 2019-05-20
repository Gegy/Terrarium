package net.gegy1000.earth.server.world.soil;

public final class EdaphicComponents {
    private final double alkalinity;
    private final double waterCapacity;
    private final double drainage;
    private final double aeration;

    public EdaphicComponents(double alkalinity, double waterCapacity, double drainage, double aeration) {
        this.alkalinity = alkalinity;
        this.waterCapacity = waterCapacity;
        this.drainage = drainage;
        this.aeration = aeration;
    }

    public double getAlkalinity() {
        return this.alkalinity;
    }

    public double getWaterCapacity() {
        return this.waterCapacity;
    }

    public double getDrainage() {
        return this.drainage;
    }

    public double getAeration() {
        return this.aeration;
    }
}
