package net.gegy1000.earth.server.world.data.source;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.earth.server.world.data.index.DataIndex1;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.earth.server.world.data.source.cache.FileTileCache;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.data.source.TerrariumCacheDirs;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import org.tukaani.xz.SingleXZInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class OceanPolygonSource extends TiledDataSource<PolygonData> {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static final Path CACHE_ROOT = TerrariumCacheDirs.GLOBAL_ROOT.resolve("ocean");
    private static final FileTileCache<Vec2i> CACHE = new FileTileCache<>(pos -> CACHE_ROOT.resolve(pos.x + "/" + pos.y));

    private static final CachingInput<Vec2i> CACHING_INPUT = new CachingInput<>(CACHE);

    public OceanPolygonSource() {
        super(1.0);
    }

    @Override
    public Optional<PolygonData> load(Vec2i pos) throws IOException {
        SharedEarthData sharedData = SharedEarthData.instance();
        DataIndex1 remoteIndex = sharedData.get(SharedEarthData.REMOTE_INDEX);
        if (remoteIndex == null) {
            return Optional.empty();
        }

        String url = remoteIndex.oceans.getUrlFor(pos);
        if (url == null) {
            return Optional.empty();
        }

        InputStream sourceInput = CACHING_INPUT.getInputStream(pos, p -> {
            return httpGet(new URL(url));
        });

        try (InputStream input = new SingleXZInputStream(new BufferedInputStream(sourceInput))) {
            return Optional.of(this.parseStream(input));
        }
    }

    private PolygonData parseStream(InputStream input) throws IOException {
        DataInputStream data = new DataInputStream(input);

        int polygonCount = data.readInt();

        Collection<MultiPolygon> polygons = new ArrayList<>(polygonCount);
        for (int i = 0; i < polygonCount; i++) {
            polygons.add(readMultiPolygon(data));
        }

        return new PolygonData(polygons);
    }

    private static MultiPolygon readMultiPolygon(DataInputStream input) throws IOException {
        int polygonCount = input.readInt();

        Polygon[] polygons = new Polygon[polygonCount];
        for (int i = 0; i < polygonCount; i++) {
            polygons[i] = readPolygon(input);
        }

        return GEOMETRY_FACTORY.createMultiPolygon(polygons);
    }

    private static Polygon readPolygon(DataInputStream input) throws IOException {
        LinearRing exteriorRing = readLinearRing(input);

        int interiorRingCount = input.readInt();
        LinearRing[] interiorRings = new LinearRing[interiorRingCount];
        for (int i = 0; i < interiorRingCount; i++) {
            interiorRings[i] = readLinearRing(input);
        }

        return GEOMETRY_FACTORY.createPolygon(exteriorRing, interiorRings);
    }

    private static LinearRing readLinearRing(DataInputStream input) throws IOException {
        int coordinateCount = input.readInt();

        double minX = input.readDouble();
        double minY = input.readDouble();
        double maxX = input.readDouble();
        double maxY = input.readDouble();

        double width = maxX - minX;
        double height = maxY - minY;

        double lastX = minX;
        double lastY = minY;

        Coordinate[] coordinates = new Coordinate[coordinateCount];
        for (int i = 0; i < coordinateCount; i++) {
            short packedX = input.readShort();
            short packedY = input.readShort();

            double deltaX = (double) packedX / Short.MAX_VALUE * width;
            double deltaY = (double) packedY / Short.MAX_VALUE * height;

            double x = deltaX + lastX;
            double y = deltaY + lastY;

            lastX = x;
            lastY = y;

            coordinates[i] = new Coordinate(x, y);
        }

        // precision is lost through delta-encoding, causing an error to be thrown
        coordinates[coordinates.length - 1] = coordinates[0];

        return GEOMETRY_FACTORY.createLinearRing(coordinates);
    }
}
