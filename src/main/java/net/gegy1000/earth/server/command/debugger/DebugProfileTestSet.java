package net.gegy1000.earth.server.command.debugger;

final class DebugProfileTestSet {
    static Location[] get() {
        return new Location[] {
                new Location("Grand Canyon #1", 36.106850, -112.113232),
                new Location("Grand Canyon #2", 36.2154626, -112.2301922),
                new Location("Mount Everest Peak", 27.987974, 86.924938),
                new Location("Australian Outback #1", -22.8202668, 136.2471223)
        };
    }

    static class Location {
        final String name;
        final double latitude;
        final double longitude;

        Location(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
