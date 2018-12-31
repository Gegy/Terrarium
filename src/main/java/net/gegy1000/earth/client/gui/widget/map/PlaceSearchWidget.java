package net.gegy1000.earth.client.gui.widget.map;

import net.gegy1000.earth.api.WidgetArea;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.Point2d;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlaceSearchWidget extends TextFieldWidget {
    private static final int SUGGESTION_COUNT = 3;
    private static final int SUGGESTION_HEIGHT = 20;

    private final Geocoder geocoder;
    private final SearchHandler searchHandler;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final List<String> suggestions = new ArrayList<>(SUGGESTION_COUNT);
    private Future<String[]> queriedSuggestions;

    private State state = State.OK;

    private boolean pause;
    private String lastSearchText;

    public PlaceSearchWidget(int id, int x, int y, int width, int height, Geocoder geocoder, SearchHandler searchHandler) {
        super(id, MinecraftClient.getInstance().fontRenderer, x, y, width, height);
        this.geocoder = geocoder;
        this.searchHandler = searchHandler;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.queriedSuggestions != null && this.queriedSuggestions.isDone()) {
            this.suggestions.clear();
            try {
                String[] result = this.queriedSuggestions.get();
                this.suggestions.addAll(Arrays.asList(result).subList(0, Math.min(result.length, SUGGESTION_COUNT)));
                if (this.suggestions.isEmpty()) {
                    this.state = State.NOT_FOUND;
                }
                this.queriedSuggestions = null;
            } catch (InterruptedException | ExecutionException e) {
                Terrarium.LOGGER.error("Failed to get queried suggestions", e);
            }
        }

        String text = this.getText().trim();
        if (!this.pause && !text.isEmpty()) {
            if (!text.equals(this.lastSearchText) && this.queriedSuggestions == null) {
                this.suggestions.clear();
                try {
                    this.queriedSuggestions = this.executor.submit(() -> this.geocoder.suggest(text));
                } catch (Exception e) {
                    Terrarium.LOGGER.error("Failed to get geocoder suggestions", e);
                }
                this.lastSearchText = text;
            }
        } else {
            this.suggestions.clear();
        }
    }

    @Override
    public boolean hasBorder() {
        return false;
    }

    @Override
    public int method_1859() {
        return super.method_1859() - 8;
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (this.isVisible()) {
            WidgetArea area = (WidgetArea) this;
            int x = area.getWidgetX();
            int y = area.getWidgetY();
            int width = area.getWidgetWidth();
            int height = area.getWidgetHeight();

            drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFFA0A0A0);
            drawRect(x, y, x + width, y + height, this.state.getBackgroundColor());

            super.render(mouseX, mouseY, delta);

            if (!this.suggestions.isEmpty() && this.isFocused()) {
                FontRenderer fontRenderer = MinecraftClient.getInstance().fontRenderer;
                int suggestionBoxHeight = SUGGESTION_HEIGHT * this.suggestions.size() + 2;
                int suggestionOriginY = y + height;
                drawRect(x - 1, suggestionOriginY, x + width + 1, suggestionOriginY + suggestionBoxHeight, 0xFFA0A0A0);

                for (int i = 0; i < this.suggestions.size(); i++) {
                    String suggestion = fontRenderer.wrapStringToWidth(this.suggestions.get(i), width - 8);
                    int suggestionY = suggestionOriginY + i * SUGGESTION_HEIGHT + 1;

                    if (mouseX >= x && mouseY >= suggestionY && mouseX <= x + width && mouseY <= suggestionY + SUGGESTION_HEIGHT) {
                        drawRect(x, suggestionY, x + width, suggestionY + SUGGESTION_HEIGHT, 0xFF5078A0);
                    }

                    fontRenderer.drawWithShadow(suggestion, x + 4, suggestionY + (SUGGESTION_HEIGHT - fontRenderer.fontHeight) / 2.0F, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isVisible() && this.isFocused()) {
            if (!this.suggestions.isEmpty()) {
                WidgetArea area = (WidgetArea) this;
                int suggestionOriginY = area.getWidgetY() + area.getWidgetHeight();

                for (int i = 0; i < this.suggestions.size(); i++) {
                    int suggestionX = area.getWidgetX();
                    int suggestionY = suggestionOriginY + i * SUGGESTION_HEIGHT + 1;

                    if (mouseX >= suggestionX && mouseY >= suggestionY && mouseX <= suggestionX + area.getWidgetWidth() && mouseY <= suggestionY + SUGGESTION_HEIGHT) {
                        this.setText(this.suggestions.get(i));
                        this.state = State.FOUND;
                        this.handleAccept();
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (!this.isFocused()) {
            return false;
        }

        this.pause = false;

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.handleAccept();
            this.suggestions.clear();
            return true;
        }

        if (super.keyPressed(keyCode, scanCode, mods)) {
            this.state = State.OK;
        }

        return false;
    }

    public void onClosed() {
        this.executor.shutdownNow();
    }

    private void handleAccept() {
        String text = this.getText();
        try {
            Point2d coordinate = this.geocoder.get(text);
            if (coordinate != null) {
                this.searchHandler.handle(coordinate.x, coordinate.y);
                this.state = State.FOUND;
            } else {
                this.state = State.NOT_FOUND;
            }
            this.pause = true;
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to find searched place {}", text, e);
        }
    }

    public interface SearchHandler {
        void handle(double latitude, double longitude);
    }

    public enum State {
        OK(0xFF000000),
        FOUND(0xFF004600),
        NOT_FOUND(0xFF460000);

        private final int backgroundColor;

        State(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public int getBackgroundColor() {
            return this.backgroundColor;
        }
    }
}
