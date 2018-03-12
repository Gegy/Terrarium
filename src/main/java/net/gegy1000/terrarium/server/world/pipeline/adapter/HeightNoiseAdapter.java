package net.gegy1000.terrarium.server.world.pipeline.adapter;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

import java.util.Random;

// TODO: Handle this better -- some generators may want to be completely built off of noise generation!
public class HeightNoiseAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTileAccess> heightComponent;

    private final NoiseGeneratorOctaves heightNoise;
    private final double noiseMax;
    private final double noiseScaleXZ;
    private final double noiseScaleY;

    public HeightNoiseAdapter(RegionComponentType<ShortRasterTileAccess> heightComponent, Random random, int octaveCount, double noiseScaleXZ, double noiseScaleY) {
        this.heightComponent = heightComponent;
        this.heightNoise = new NoiseGeneratorOctaves(random, octaveCount);

        double max = 0.0;
        double scale = 1.0;
        for (int i = 0; i < octaveCount; i++) {
            max += scale * 2;
            scale /= 2.0;
        }
        this.noiseMax = max;

        this.noiseScaleXZ = noiseScaleXZ;
        this.noiseScaleY = noiseScaleY;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTileAccess heightTile = data.getOrExcept(this.heightComponent);

        short[] heightBuffer = heightTile.getShortData();

        double[] noise = new double[width * height];
        this.heightNoise.generateNoiseOctaves(noise, z, x, width, height, this.noiseScaleXZ, this.noiseScaleXZ, 0.0);

        for (int i = 0; i < noise.length; i++) {
            heightBuffer[i] += (noise[i] + this.noiseMax) / (this.noiseMax * 2.0) * this.noiseScaleY * 35.0;
        }
    }

    public static class Parser implements InstanceObjectParser<RegionAdapter> {
        @Override
        public RegionAdapter parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<ShortRasterTileAccess> heightComponent = valueParser.parseComponentType(objectRoot, "height_component", ShortRasterTileAccess.class);
            int octaveCount = valueParser.parseInteger(objectRoot, "octaves");
            double noiseScaleXZ = valueParser.parseDouble(objectRoot, "noise_xz_scale");
            double noiseScaleY = valueParser.parseDouble(objectRoot, "noise_y_scale");

            Random random = new Random(world.getWorldInfo().getSeed());
            return new HeightNoiseAdapter(heightComponent, random, octaveCount, noiseScaleXZ, noiseScaleY);
        }
    }
}
