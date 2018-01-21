package net.gegy1000.terrarium.server.world.generator.debug;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebugMap {
    private static final int GRID_SIZE = 32;
    private static final int PADDING = 2;

    private static final int OFFSET_X = 8;
    private static final int OFFSET_Z = 8;

    public static DebugCover getCover(int x, int z) {
        int offsetX = x + OFFSET_X;
        int offsetZ = z + OFFSET_Z;

        int gridOriginX = Math.floorDiv(offsetX, GRID_SIZE);
        int gridOriginZ = Math.floorDiv(offsetZ, GRID_SIZE);

        int localX = offsetX - (gridOriginX * GRID_SIZE);
        int localZ = offsetZ - (gridOriginZ * GRID_SIZE);

        if (localX >= PADDING && localZ >= PADDING && localX < GRID_SIZE - PADDING && localZ < GRID_SIZE - PADDING) {
            DebugCover cover = DebugMap.getGridCover(gridOriginX, gridOriginZ);
            if (cover != null) {
                return cover;
            }
        }

        return new DebugCover(CoverType.DEBUG, LatitudinalZone.TEMPERATE);
    }

    public static String[] getSign(int x, int z) {
        int offsetX = x + OFFSET_X;
        int offsetZ = z + OFFSET_Z;

        int gridOriginX = Math.floorDiv(offsetX, GRID_SIZE);
        int gridOriginZ = Math.floorDiv(offsetZ, GRID_SIZE);

        int localX = offsetX - (gridOriginX * GRID_SIZE);
        int localZ = offsetZ - (gridOriginZ * GRID_SIZE);

        if (localX == GRID_SIZE / 2 && localZ == GRID_SIZE - PADDING) {
            DebugCover cover = DebugMap.getGridCover(gridOriginX, gridOriginZ);
            if (cover != null) {
                return DebugMap.createSignText(cover);
            }
        }

        return null;
    }

    private static String[] createSignText(DebugCover cover) {
        List<String> lines = new ArrayList<>();

        String zoneName = cover.getZone().name().toLowerCase(Locale.ENGLISH);
        String coverType = cover.getCoverType().name().toLowerCase(Locale.ENGLISH);
        String[] coverWords = coverType.split("_");

        lines.add(zoneName);

        StringBuilder currentLine = new StringBuilder();
        for (String coverWord : coverWords) {
            if (currentLine.length() + coverWord.length() + 1 >= 16) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            currentLine.append(coverWord).append(' ');
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines.toArray(new String[Math.min(lines.size(), 4)]);
    }

    private static DebugCover getGridCover(int gridX, int gridZ) {
        int effectiveGridX = gridX + CoverType.TYPES.length / 2;
        int effectiveGridZ = gridZ + LatitudinalZone.ZONES.length / 2;

        if (effectiveGridX >= 0 && effectiveGridX < CoverType.TYPES.length && effectiveGridZ >= 0 && effectiveGridZ < LatitudinalZone.ZONES.length) {
            CoverType coverType = CoverType.TYPES[effectiveGridX];
            LatitudinalZone zone = LatitudinalZone.ZONES[effectiveGridZ];
            return new DebugCover(coverType, zone);
        }

        return null;
    }

    public static class DebugCover {
        private final CoverType coverType;
        private final LatitudinalZone zone;

        public DebugCover(CoverType coverType, LatitudinalZone zone) {
            this.coverType = coverType;
            this.zone = zone;
        }

        public CoverType getCoverType() {
            return this.coverType;
        }

        public LatitudinalZone getZone() {
            return this.zone;
        }
    }
}
