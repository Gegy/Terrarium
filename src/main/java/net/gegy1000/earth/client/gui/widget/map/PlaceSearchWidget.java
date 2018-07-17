package net.gegy1000.earth.client.gui.widget.map;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import javax.vecmath.Vector2d;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlaceSearchWidget extends GuiTextField {
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
        super(id, Minecraft.getMinecraft().fontRenderer, x, y, width, height);
        this.geocoder = geocoder;
        this.searchHandler = searchHandler;
    }

    public void update() {
        super.updateCursorCounter();

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
    public boolean getEnableBackgroundDrawing() {
        return false;
    }

    @Override
    public int getWidth() {
        return this.width - 8;
    }

    public void draw(int mouseX, int mouseY) {
        if (this.getVisible()) {
            drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFFA0A0A0);
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, this.state.getBackgroundColor());

            super.drawTextBox();

            if (!this.suggestions.isEmpty() && this.isFocused()) {
                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

                int suggestionBoxHeight = SUGGESTION_HEIGHT * this.suggestions.size() + 2;
                int suggestionOriginY = this.y + this.height;
                drawRect(this.x - 1, suggestionOriginY, this.x + this.width + 1, suggestionOriginY + suggestionBoxHeight, 0xFFA0A0A0);

                for (int i = 0; i < this.suggestions.size(); i++) {
                    String suggestion = fontRenderer.trimStringToWidth(this.suggestions.get(i), this.width - 8);
                    int suggestionX = this.x;
                    int suggestionY = suggestionOriginY + i * SUGGESTION_HEIGHT + 1;

                    if (mouseX >= suggestionX && mouseY >= suggestionY && mouseX <= suggestionX + this.width && mouseY <= suggestionY + SUGGESTION_HEIGHT) {
                        drawRect(suggestionX, suggestionY, suggestionX + this.width, suggestionY + SUGGESTION_HEIGHT, 0xFF5078A0);
                    }

                    fontRenderer.drawStringWithShadow(suggestion, suggestionX + 4, suggestionY + (SUGGESTION_HEIGHT - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.getVisible() && this.isFocused()) {
            if (!this.suggestions.isEmpty()) {
                int suggestionOriginY = this.y + this.height;

                for (int i = 0; i < this.suggestions.size(); i++) {
                    int suggestionX = this.x;
                    int suggestionY = suggestionOriginY + i * SUGGESTION_HEIGHT + 1;

                    if (mouseX >= suggestionX && mouseY >= suggestionY && mouseX <= suggestionX + this.width && mouseY <= suggestionY + SUGGESTION_HEIGHT) {
                        this.setText(this.suggestions.get(i));
                        this.state = State.FOUND;
                        this.handleAccept();
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        if (!this.isFocused()) {
            return false;
        }

        this.pause = false;

        if (keyCode == Keyboard.KEY_RETURN) {
            this.handleAccept();
            this.suggestions.clear();
            return true;
        }

        if (super.textboxKeyTyped(typedChar, keyCode)) {
            this.state = State.OK;
        }

        return false;
    }

    public void onGuiClosed() {
        this.executor.shutdownNow();
    }

    private void handleAccept() {
        String text = this.getText();
        try {
            Vector2d coordinate = this.geocoder.get(text);
            if (coordinate != null) {
                this.searchHandler.handle(coordinate.getX(), coordinate.getY());
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
