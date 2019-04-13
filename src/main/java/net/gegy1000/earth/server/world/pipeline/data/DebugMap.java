package net.gegy1000.earth.server.world.pipeline.data;

import com.google.common.base.CaseFormat;
import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;

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

        return new DebugCover(TerrariumCoverTypes.DEBUG, ClimaticZone.TEMPERATE);
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
        String coverType = cover.getCoverType().getClass().getSimpleName();
        String[] coverWords = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, coverType).split("_");

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
        int effectiveGridX = gridX + CoverClassification.TYPES.length / 2;
        int effectiveGridZ = gridZ + ClimaticZone.ZONES.length / 2;

        if (effectiveGridX >= 0 && effectiveGridX < CoverClassification.TYPES.length && effectiveGridZ >= 0 && effectiveGridZ < ClimaticZone.ZONES.length) {
            CoverType coverType = CoverClassification.TYPES[effectiveGridX].getCoverType();
            ClimaticZone zone = ClimaticZone.ZONES[effectiveGridZ];
            return new DebugCover(coverType, zone);
        }

        return null;
    }

    public static class DebugCover {
        private final CoverType coverType;
        private final ClimaticZone zone;

        public DebugCover(CoverType coverType, ClimaticZone zone) {
            this.coverType = coverType;
            this.zone = zone;
        }

        public CoverType getCoverType() {
            return this.coverType;
        }

        public ClimaticZone getZone() {
            return this.zone;
        }
    }
}
