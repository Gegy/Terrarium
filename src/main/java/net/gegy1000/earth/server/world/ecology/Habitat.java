package net.gegy1000.earth.server.world.ecology;

// TODO: Habitat can include soil
public interface Habitat {
    static Habitat everywhere() {
        return (temperature, rainfall) -> 1.0;
    }

    double getSuitability(float temperature, short rainfall);
}
