package dev.gegy.terrarium.client.screen;

import com.mojang.logging.LogUtils;
import dev.gegy.terrarium.backend.earth.EarthConfiguration;
import dev.gegy.terrarium.backend.projection.cylindrical.Mercator;
import dev.gegy.terrarium.backend.util.LogarithmicSliderMapper;
import dev.gegy.terrarium.world.generator.chunk.EarthChunkGenerator;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.function.Consumer;

public class ConfigureEarthScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component TITLE = Component.translatable("gui.terrarium.configure.title");

    private static final int CONTENT_SPACING = 8;

    private static final LogarithmicSliderMapper SCALE_SLIDER_MAPPER = new LogarithmicSliderMapper(0, 5, 2, 5);
    private static final LogarithmicSliderMapper HEIGHT_SCALE_SLIDER_MAPPER = new LogarithmicSliderMapper(-1, 2, 1, 1);

    private final Screen parentScreen;
    private final Consumer<EarthConfiguration> applyConfiguration;
    private final ConfigurationState configurationState;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private ConfigureEarthScreen(final Screen parentScreen, final Consumer<EarthConfiguration> applyConfiguration, final EarthConfiguration configuration) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.applyConfiguration = applyConfiguration;
        configurationState = new ConfigurationState(configuration);
    }

    public static Screen create(final CreateWorldScreen worldScreen, final WorldCreationContext context) {
        final ChunkGenerator generator = context.selectedDimensions().overworld();
        if (generator instanceof final EarthChunkGenerator earth) {
            return new ConfigureEarthScreen(
                    worldScreen,
                    newConfiguration -> worldScreen.getUiState().updateDimensions((registries, dimensions) ->
                            dimensions.replaceOverworldGenerator(registries, earth.withConfiguration(newConfiguration))
                    ),
                    earth.configuration()
            );
        } else {
            LOGGER.error("Earth world preset did not have chunk generator of expected type, got: {}", generator);
            // No reasonable way to continue, so just do nothing
            return worldScreen;
        }
    }

    @Override
    protected void init() {
        layout.addToHeader(new StringWidget(TITLE, font));

        final LinearLayout contents = layout.addToContents(LinearLayout.vertical()).spacing(CONTENT_SPACING);
        contents.addChild(new SliderWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, SCALE_SLIDER_MAPPER.toSlider(configurationState.scale)) {
            @Override
            protected void updateMessage() {
                final int meters = Mth.floor(SCALE_SLIDER_MAPPER.fromSlider(value));
                if (meters >= 10_000) {
                    setMessage(Component.literal("Scale: 1:" + (meters / 1000) + "km"));
                } else {
                    setMessage(Component.literal("Scale: 1:" + meters + "m"));
                }
            }

            @Override
            protected void applyValue() {
                configurationState.scale = Mth.floor(SCALE_SLIDER_MAPPER.fromSlider(value));
            }
        });
        contents.addChild(new SliderWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT, HEIGHT_SCALE_SLIDER_MAPPER.toSlider(configurationState.heightScale)) {
            @Override
            protected void updateMessage() {
                final String scale = String.format(Locale.ROOT, "%.1f", HEIGHT_SCALE_SLIDER_MAPPER.fromSlider(value));
                setMessage(Component.literal("Height Scale: " + scale + "x"));
            }

            @Override
            protected void applyValue() {
                configurationState.heightScale = (float) HEIGHT_SCALE_SLIDER_MAPPER.fromSlider(value);
            }
        });

        final LinearLayout footer = layout.addToFooter(LinearLayout.horizontal()).spacing(CONTENT_SPACING);

        footer.addChild(Button.builder(CommonComponents.GUI_DONE, b -> {
            applyConfiguration.accept(configurationState.buildConfiguration());
            onClose();
        }).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose()).build());

        repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parentScreen);
    }

    private static class ConfigurationState {
        private int scale;
        private float heightScale;
        private int heightOffset;

        public ConfigurationState(final EarthConfiguration configuration) {
            scale = Mth.floor(configuration.projection().idealMetersPerBlock());
            heightScale = configuration.heightScale();
            heightOffset = configuration.heightOffset();
        }

        public EarthConfiguration buildConfiguration() {
            return new EarthConfiguration(
                    new Mercator(scale),
                    heightScale,
                    heightOffset
            );
        }
    }

    private static abstract class SliderWidget extends AbstractSliderButton {
        public SliderWidget(final int width, final int height, final double value) {
            super(0, 0, width, height, CommonComponents.EMPTY, value);
            updateMessage();
        }
    }
}
