package net.gegy1000.earth.server.world.ecology;

import net.gegy1000.earth.server.world.soil.EdaphicProperties;

// TODO: Debug utils to display view components in-world
public final class AbioticProperties {
    // TODO: simulate water drainage
    private EdaphicProperties edaphic;

    private float temperature;

    private short altitude;
    private byte slope;

//    public void setEdaphic(SoilClassification soil) {
//        this.edaphic = soil.getEdaphicFactors();
//    }

    public void setPhysiographic(short altitude, byte slope) {
        this.altitude = altitude;
        this.slope = slope;
    }

    // TODO: Water
    public void setClimatic(float temperature) {
        this.temperature = temperature;
    }

    public EdaphicProperties getEdaphic() {
        return this.edaphic;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public short getAltitude() {
        return this.altitude;
    }

    public byte getSlope() {
        return this.slope;
    }
}
