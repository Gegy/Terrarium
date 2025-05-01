package dev.gegy.terrarium.map;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface MapDecoration {
    double latitude();

    double longitude();

    int pickRadius();

    ClickResponse clicked(MouseEvent event);

    void draw(Graphics2D graphics, int x, int y);

    enum ClickResponse {
        NONE,
        REMOVE,
    }
}
