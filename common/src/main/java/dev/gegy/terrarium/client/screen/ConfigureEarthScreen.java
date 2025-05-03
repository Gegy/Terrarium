package dev.gegy.terrarium.client.screen;

import com.mojang.logging.LogUtils;
import dev.gegy.terrarium.backend.earth.EarthConfiguration;
import dev.gegy.terrarium.world.generator.chunk.EarthChunkGenerator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class ConfigureEarthScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component TITLE = Component.translatable("gui.terrarium.configure.title");

    private static final int CONTENT_SPACING = 8;

    private final Screen parentScreen;
    private final Consumer<EarthConfiguration> applyConfiguration;
    private EarthConfiguration configuration;

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private ConfigureEarthScreen(final Screen parentScreen, final Consumer<EarthConfiguration> applyConfiguration, final EarthConfiguration configuration) {
        super(TITLE);
        this.parentScreen = parentScreen;
        this.applyConfiguration = applyConfiguration;
        this.configuration = configuration;
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

        final LinearLayout footer = layout.addToFooter(LinearLayout.horizontal()).spacing(CONTENT_SPACING);

        footer.addChild(Button.builder(CommonComponents.GUI_DONE, b -> {
            applyConfiguration.accept(configuration);
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
}
