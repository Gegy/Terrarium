package net.gegy1000.earth.server.world.pipeline.sampler;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

public class OsmSampler implements DataSampler<OsmTile> {
    private final TiledDataSource<OsmTile> overpassSource;
    private final CoordinateState coordinateState;

    public OsmSampler(TiledDataSource<OsmTile> overpassSource, CoordinateState coordinateState) {
        this.overpassSource = overpassSource;
        this.coordinateState = coordinateState;
    }

    @Override
    public OsmTile sample(GenerationSettings settings, int x, int z, int width, int height) {
        DataTilePos blockMinTilePos = this.getTilePos(Coordinate.fromBlock(x, z));
        DataTilePos blockMaxTilePos = this.getTilePos(Coordinate.fromBlock(x + width, z + height));

        DataTilePos minTilePos = DataTilePos.min(blockMinTilePos, blockMaxTilePos);
        DataTilePos maxTilePos = DataTilePos.max(blockMinTilePos, blockMaxTilePos);

        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>();

        for (int tileZ = minTilePos.getTileZ(); tileZ <= maxTilePos.getTileZ(); tileZ++) {
            for (int tileX = minTilePos.getTileX(); tileX <= maxTilePos.getTileX(); tileX++) {
                OsmTile tile = this.overpassSource.getTile(new DataTilePos(tileX, tileZ));
                if (tile != null) {
                    nodes.putAll(tile.getNodes());
                    ways.putAll(tile.getWays());
                }
            }
        }

        return new OsmTile(nodes, ways);
    }

    @Override
    public boolean shouldSample() {
        return this.overpassSource.shouldSample();
    }

    @Override
    public Class<OsmTile> getSamplerType() {
        return OsmTile.class;
    }

    private DataTilePos getTilePos(Coordinate coordinate) {
        coordinate = coordinate.to(this.coordinateState);

        Coordinate tileSize = this.overpassSource.getTileSize();
        int tileX = MathHelper.floor(coordinate.getX() / tileSize.getX());
        int tileZ = MathHelper.floor(coordinate.getZ() / tileSize.getZ());
        return new DataTilePos(tileX, tileZ);
    }
}
