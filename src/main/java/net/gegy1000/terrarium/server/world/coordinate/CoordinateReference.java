package net.gegy1000.terrarium.server.world.coordinate;

public interface CoordinateReference {
    CoordinateReference BLOCK = new CoordinateReference() {
        @Override
        public double blockX(double x, double z) {
            return x;
        }

        @Override
        public double blockZ(double x, double z) {
            return z;
        }

        @Override
        public double x(double blockX, double blockZ) {
            return blockX;
        }

        @Override
        public double z(double blockX, double blockZ) {
            return blockZ;
        }
    };

    double blockX(double x, double z);

    double blockZ(double x, double z);

    double x(double blockX, double blockZ);

    double z(double blockX, double blockZ);
}
