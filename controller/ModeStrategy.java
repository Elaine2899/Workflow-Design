package controller;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

public interface ModeStrategy {
    Mode getMode();

    default void onMousePressed(MouseEvent e) {}
    default void onMouseReleased(MouseEvent e) {}
    default void onMouseDragged(MouseEvent e) {}
    default void onMouseMoved(MouseEvent e) {}
    default void onMouseExited(MouseEvent e) {}
    default void drawOverlay(Graphics g) {}
}
