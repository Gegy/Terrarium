package net.gegy1000.terrarium.server.world.coordinate;

public interface CoordinateState {
    CoordinateState BLOCK = new CoordinateState() {
        @Override
        public double getBlockX(double x, double z) {
            return x;
        }

        @Override
        public double getBlockZ(double x, double z) {
            return z;
        }

        @Override
        public double getX(double blockX, double blockZ) {
            return blockX;
        }

        @Override
        public double getZ(double blockX, double blockZ) {
            return blockZ;
        }
    };

    double getBlockX(double x, double z);

    double getBlockZ(double x, double z);

    double getX(double blockX, double blockZ);

    double getZ(double blockX, double blockZ);
}
