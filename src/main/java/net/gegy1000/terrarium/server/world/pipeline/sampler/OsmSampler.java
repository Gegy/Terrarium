package net.gegy1000.terrarium.server.world.pipeline.sampler;

import com.google.gson.JsonObject;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.earth.tile.OsmTileAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class OsmSampler implements DataSampler<OsmTileAccess> {
    private final TiledDataSource<OsmTileAccess> overpassSource;
    private final CoordinateState coordinateState;

    public OsmSampler(TiledDataSource<OsmTileAccess> overpassSource, CoordinateState coordinateState) {
        this.overpassSource = overpassSource;
        this.coordinateState = coordinateState;
    }

    @Override
    public OsmTileAccess sample(GenerationSettings settings, int x, int z, int width, int height) {
        DataTilePos blockMinTilePos = this.getTilePos(Coordinate.fromBlock(x, z));
        DataTilePos blockMaxTilePos = this.getTilePos(Coordinate.fromBlock(x + width, z + height));

        DataTilePos minTilePos = DataTilePos.min(blockMinTilePos, blockMaxTilePos);
        DataTilePos maxTilePos = DataTilePos.max(blockMinTilePos, blockMaxTilePos);

        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>();

        for (int tileZ = minTilePos.getTileZ(); tileZ <= maxTilePos.getTileZ(); tileZ++) {
            for (int tileX = minTilePos.getTileX(); tileX <= maxTilePos.getTileX(); tileX++) {
                OsmTileAccess tile = this.overpassSource.getTile(new DataTilePos(tileX, tileZ));
                if (tile != null) {
                    nodes.putAll(tile.getNodes());
                    ways.putAll(tile.getWays());
                }
            }
        }

        return new OsmTileAccess(nodes, ways);
    }

    @Override
    public boolean shouldSample() {
        return this.overpassSource.shouldSample();
    }

    @Override
    public Class<OsmTileAccess> getSamplerType() {
        return OsmTileAccess.class;
    }

    private DataTilePos getTilePos(Coordinate coordinate) {
        coordinate = coordinate.to(this.coordinateState);

        Coordinate tileSize = this.overpassSource.getTileSize();
        int tileX = MathHelper.floor(coordinate.getX() / tileSize.getX());
        int tileZ = MathHelper.floor(coordinate.getZ() / tileSize.getZ());
        return new DataTilePos(tileX, tileZ);
    }

    public static class Parser implements InstanceObjectParser<DataSampler<?>> {
        @Override
        public DataSampler<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            TiledDataSource<OsmTileAccess> source = valueParser.parseTiledSource(objectRoot, "source", OsmTileAccess.class);
            CoordinateState coordinateState = valueParser.parseCoordinateState(objectRoot, "lat_lng_coordinate");
            return new OsmSampler(source, coordinateState);
        }
    }
}
