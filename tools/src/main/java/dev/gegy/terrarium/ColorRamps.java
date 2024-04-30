package dev.gegy.terrarium;

public class ColorRamps {
    public static final ColorRamp ELEVATION = ColorRamp.builder()
            .color(-5000.0f, 0, 0, 200)
            .color(-20.0f, 0, 255, 255)
            .color(0.0f, 0, 132, 53)
            .color(0.125f * 4000.0f, 51, 204, 0)
            .color(0.25f * 4000.0f, 244, 240, 113)
            .color(0.5f * 4000.0f, 244, 189, 69)
            .color(0.75f * 4000.0f, 153, 100, 43)
            .color(5000.0f, 255, 255, 255)
            .build();

    public static final ColorRamp PH = ColorRamp.builder()
            .color(0.0f / 14.0f * 255.0f, 0xe02030)
            .color(3.0f / 14.0f * 255.0f, 0xffc030)
            .color(7.0f / 14.0f * 255.0f, 0x10a020)
            .color(11.0f / 14.0f * 255.0f, 0x1050d0)
            .color(14.0f / 14.0f * 255.0f, 0x402090)
            .build();

    public static final ColorRamp SOIL = ColorRamp.builder()
            .color(0.0f, 0x000000)
            .color(255.0f, 0xffffff)
            .build();
}
