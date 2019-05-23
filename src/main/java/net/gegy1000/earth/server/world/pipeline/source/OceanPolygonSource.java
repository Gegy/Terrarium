package net.gegy1000.earth.server.world.pipeline.source;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import net.gegy1000.earth.server.world.pipeline.data.PolygonData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.tukaani.xz.SingleXZInputStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public class OceanPolygonSource extends TiledDataSource<PolygonData> {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final double TILE_SIZE = 1.0;

    public OceanPolygonSource(CoordinateState latLngCoordinateState, String cacheRoot) {
        super(new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(latLngCoordinateState, TILE_SIZE, TILE_SIZE));
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getOceanEndpoint(), this.getCachedName(key)));
        return url.openStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new SingleXZInputStream(stream);
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(EarthRemoteData.info.getOceanQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public PolygonData getDefaultResult() {
        return PolygonData.EMPTY;
    }

    @Override
    public SourceResult<PolygonData> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            int polygonCount = input.readInt();

            Collection<MultiPolygon> polygons = new ArrayList<>(polygonCount);
            for (int i = 0; i < polygonCount; i++) {
                polygons.add(readMultiPolygon(input));
            }

            return SourceResult.success(new PolygonData(polygons));
        }
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

        com.vividsolutions.jts.geom.Coordinate[] coordinates = new com.vividsolutions.jts.geom.Coordinate[coordinateCount];
        for (int i = 0; i < coordinateCount; i++) {
            short packedX = input.readShort();
            short packedY = input.readShort();

            double deltaX = (double) packedX / Short.MAX_VALUE * width;
            double deltaY = (double) packedY / Short.MAX_VALUE * height;

            double x = deltaX + lastX;
            double y = deltaY + lastY;

            lastX = x;
            lastY = y;

            coordinates[i] = new com.vividsolutions.jts.geom.Coordinate(x, y);
        }

        // precision is lost through delta-encoding, causing an exception to be thrown
        coordinates[coordinates.length - 1] = coordinates[0];

        return GEOMETRY_FACTORY.createLinearRing(coordinates);
    }
}
