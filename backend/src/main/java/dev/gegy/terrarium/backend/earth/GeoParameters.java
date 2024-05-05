package dev.gegy.terrarium.backend.earth;

import dev.gegy.terrarium.backend.earth.cover.Cover;
import dev.gegy.terrarium.backend.earth.soil.SoilSuborder;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;

import java.util.Arrays;
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
}
