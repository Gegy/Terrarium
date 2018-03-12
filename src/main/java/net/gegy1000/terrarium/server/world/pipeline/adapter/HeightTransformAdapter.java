package net.gegy1000.terrarium.server.world.pipeline.adapter;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HeightTransformAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;
    private final double heightScale;
    private final short heightOffset;

    public HeightTransformAdapter(RegionComponentType<ShortRasterTileAccess> heightComponent, double heightScale, short heightOffset) {
        this.heightComponent = heightComponent;
        this.heightScale = heightScale;
        this.heightOffset = heightOffset;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTileAccess heightTile = data.getOrExcept(this.heightComponent);
        short[] heightBuffer = heightTile.getShortData();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localZ * width;
                int scaledHeight = MathHelper.ceil(heightBuffer[index] * this.heightScale);
                heightBuffer[index] = (short) MathHelper.clamp(scaledHeight + this.heightOffset, 0, 255);
            }
        }
    }

    public static class Parser implements InstanceObjectParser<RegionAdapter> {
        @Override
        public RegionAdapter parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            double heightScale = valueParser.parseDouble(objectRoot, "height_scale");
            short heightOffset = (short) valueParser.parseInteger(objectRoot, "height_offset");

            return new HeightTransformAdapter(heightComponent, heightScale, heightOffset);
        }
    }
}
