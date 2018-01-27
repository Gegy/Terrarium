package net.gegy1000.terrarium.server.map.system.sampler;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.gegy1000.terrarium.server.map.source.osm.OverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class OverpassSampler implements DataSampler<OverpassTileAccess> {
    private final OverpassSource overpassSource;

    public OverpassSampler(OverpassSource overpassSource) {
        this.overpassSource = overpassSource;
    }

    @Override
    public OverpassTileAccess sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        DataTilePos minTilePos = this.getTilePos(Coordinate.fromBlock(settings, x, z));
        DataTilePos maxTilePos = this.getTilePos(Coordinate.fromBlock(settings, x + width, z + height));

        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>();

        for (int tileZ = minTilePos.getTileY(); tileZ <= maxTilePos.getTileY(); tileZ++) {
            for (int tileX = minTilePos.getTileX(); tileX <= maxTilePos.getTileX(); tileX++) {
                OverpassTileAccess tile = this.overpassSource.getTile(new DataTilePos(tileX, tileZ));
                if (tile != null) {
                    nodes.putAll(tile.getNodes());
                    ways.putAll(tile.getWays());
                }
            }
        }

        return new OverpassTileAccess(nodes, ways);
    }

    @Override
    public boolean shouldSample() {
        return this.overpassSource.shouldSample();
    }

    private DataTilePos getTilePos(Coordinate coordinate) {
        int tileX = MathHelper.floor(coordinate.getLongitude() / this.overpassSource.getTileSize());
        int tileZ = MathHelper.ceil(-coordinate.getLatitude() / this.overpassSource.getTileSize());
        return new DataTilePos(tileX, tileZ);
    }
}
