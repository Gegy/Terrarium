package dev.gegy.terrarium.backend.earth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.projection.Projection;

public record EarthConfiguration(
        Projection projection,
        float heightScale,
        int heightOffset
) {
    public static final MapCodec<EarthConfiguration> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Projection.CODEC.fieldOf("projection").forGetter(EarthConfiguration::projection),
            Codec.FLOAT.fieldOf("height_scale").forGetter(EarthConfiguration::heightScale),
            Codec.INT.fieldOf("height_offset").forGetter(EarthConfiguration::heightOffset)
    ).apply(i, EarthConfiguration::new));
}
