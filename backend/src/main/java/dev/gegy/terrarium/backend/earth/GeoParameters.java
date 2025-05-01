package dev.gegy.terrarium.backend.earth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GeoParameters {
    public static final Predictor<GeoParameters> ELEVATION = p -> p.elevation;
    public static final Map<Cover, Predictor<GeoParameters>> IS_COVER = Arrays.stream(Cover.values()).collect(Collectors.toMap(
            Function.identity(),
            cover -> parameters -> parameters.cover == cover ? 1.0f : 0.0f
    ));
    public static final Predictor<GeoParameters> CATION_EXCHANGE_CAPACITY = p -> p.cationExchangeCapacity;
    public static final Predictor<GeoParameters> ORGANIC_CARBON_CONTENT = p -> p.organicCarbonContent;
    public static final Predictor<GeoParameters> SOIL_PH = p -> p.soilPh;
    public static final Predictor<GeoParameters> CLAY_CONTENT = p -> p.clayContent;
    public static final Predictor<GeoParameters> SILT_CONTENT = p -> p.siltContent;
    public static final Predictor<GeoParameters> SAND_CONTENT = p -> p.sandContent;
    public static final Map<SoilSuborder, Predictor<GeoParameters>> IS_SOIL_SUBORDER = Arrays.stream(SoilSuborder.values()).collect(Collectors.toMap(
            Function.identity(),
            soilSuborder -> parameters -> parameters.soilSuborder == soilSuborder ? 1.0f : 0.0f
    ));
    public static final Predictor<GeoParameters> MEAN_TEMPERATURE = p -> p.meanTemperature;
    public static final Predictor<GeoParameters> MIN_TEMPERATURE = p -> p.minTemperature;
    public static final Predictor<GeoParameters> ANNUAL_RAINFALL = p -> p.annualRainfall;

    public static final Codec<GeoParameters> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("elevation").forGetter(p -> p.elevation),
            Cover.CODEC.fieldOf("cover").forGetter(p -> p.cover),
            Codec.FLOAT.fieldOf("cation_exchange_capacity").forGetter(p -> p.cationExchangeCapacity),
            Codec.FLOAT.fieldOf("organic_carbon_content").forGetter(p -> p.organicCarbonContent),
            Codec.FLOAT.fieldOf("soil_ph").forGetter(p -> p.soilPh),
            Codec.FLOAT.fieldOf("clay_content").forGetter(p -> p.clayContent),
            Codec.FLOAT.fieldOf("silt_content").forGetter(p -> p.siltContent),
            Codec.FLOAT.fieldOf("sand_content").forGetter(p -> p.sandContent),
            SoilSuborder.CODEC.fieldOf("soil_suborder").forGetter(p -> p.soilSuborder),
            Codec.FLOAT.fieldOf("mean_temperature").forGetter(p -> p.meanTemperature),
            Codec.FLOAT.fieldOf("min_temperature").forGetter(p -> p.minTemperature),
            Codec.FLOAT.fieldOf("annual_rainfall").forGetter(p -> p.annualRainfall)
    ).apply(i, GeoParameters::new));

    private float elevation;
    private Cover cover = Cover.NONE;
    private float cationExchangeCapacity;
    private float organicCarbonContent;
    private float soilPh;
    private float clayContent;
    private float siltContent;
    private float sandContent;
    private SoilSuborder soilSuborder = SoilSuborder.NONE;
    private float meanTemperature;
    private float minTemperature;
    private float annualRainfall;

    public static void forEachFeature(final BiConsumer<String, Predictor<GeoParameters>> consumer) {
        consumer.accept("elevation", ELEVATION);
        IS_COVER.forEach((cover, predictor) -> consumer.accept("is_cover/" + cover.getName(), predictor));
        consumer.accept("cation_exchange_capacity", CATION_EXCHANGE_CAPACITY);
        consumer.accept("organic_carbon_content", ORGANIC_CARBON_CONTENT);
        consumer.accept("ph", SOIL_PH);
        consumer.accept("clay_content", CLAY_CONTENT);
        consumer.accept("silt_content", SILT_CONTENT);
        consumer.accept("sand_content", SAND_CONTENT);
        IS_SOIL_SUBORDER.forEach((soilSuborder, predictor) -> consumer.accept("is_soil_suborder/" + soilSuborder.getName(), predictor));
        consumer.accept("average_temperature", MEAN_TEMPERATURE);
        consumer.accept("min_temperature", MIN_TEMPERATURE);
        consumer.accept("annual_precipitation", ANNUAL_RAINFALL);
    }

    public static List<Predictor<GeoParameters>> allFeatures() {
        final List<Predictor<GeoParameters>> features = new ArrayList<>();
        forEachFeature((name, feature) -> features.add(feature));
        return List.copyOf(features);
    }

    public GeoParameters() {
    }

    public GeoParameters(final float elevation, final Cover cover, final float cationExchangeCapacity, final float organicCarbonContent, final float soilPh, final float clayContent, final float siltContent, final float sandContent, final SoilSuborder soilSuborder, final float meanTemperature, final float minTemperature, final float annualRainfall) {
        set(elevation, cover, cationExchangeCapacity, organicCarbonContent, soilPh, clayContent, siltContent, sandContent, soilSuborder, meanTemperature, minTemperature, annualRainfall);
    }

    public GeoParameters set(final float elevation, final Cover cover, final float cationExchangeCapacity, final float organicCarbonContent, final float soilPh, final float clayContent, final float siltContent, final float sandContent, final SoilSuborder soilSuborder, final float meanTemperature, final float minTemperature, final float annualRainfall) {
        this.elevation = elevation;
        this.cover = cover;
        this.cationExchangeCapacity = cationExchangeCapacity;
        this.organicCarbonContent = organicCarbonContent;
        this.soilPh = soilPh;
        this.clayContent = clayContent;
        this.siltContent = siltContent;
        this.sandContent = sandContent;
        this.soilSuborder = soilSuborder;
        this.meanTemperature = meanTemperature;
        this.minTemperature = minTemperature;
        this.annualRainfall = annualRainfall;
        return this;
    }

    public GeoParameters set(final EarthAttachments attachments, final int x, final int y) {
        return set(
                attachments.elevation().getInt(x, y),
                attachments.landCover().get(x, y),
                attachments.cationExchangeCapacity().getByte(x, y),
                attachments.organicCarbonContent().getShort(x, y),
                attachments.soilPh().getByte(x, y),
                attachments.clayContent().getByte(x, y),
                attachments.siltContent().getByte(x, y),
                attachments.sandContent().getByte(x, y),
                attachments.soilSuborder().get(x, y),
                attachments.meanTemperature().getTemperature(x, y),
                attachments.minTemperature().getTemperature(x, y),
                attachments.annualRainfall().getRainfall(x, y)
        );
    }

    public GeoParameters set(final GeoParameters other) {
        return set(other.elevation, other.cover, other.cationExchangeCapacity, other.organicCarbonContent, other.soilPh, other.clayContent, other.siltContent, other.sandContent, other.soilSuborder, other.meanTemperature, other.minTemperature, other.annualRainfall);
    }

    public float elevation() {
        return elevation;
    }
}
