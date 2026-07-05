package controller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import model.Port;
import view.CanvasPanel;

public class CreateLinkMode implements ModeStrategy {
    private final CanvasPanel canvasPanel;
    private final Mode mode;
    private Port startPort;
    private Port currentSnapPort;
    private int currentX, currentY;
    private boolean isLinking;

    public CreateLinkMode(CanvasPanel canvasPanel, ModeManager modeManager, Mode mode) {
        this.canvasPanel = canvasPanel;
        this.mode = mode;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        startPort = canvasPanel.findSnapPortAt(e.getX(), e.getY());
        currentX = e.getX();
        currentY = e.getY();
        currentSnapPort = startPort;

        if (startPort == null) {
            // Alt B.1: 起點不在任何基本物件的 port 上，整次操作無效
            isLinking = false;
            return;
        }

        isLinking = true;
        canvasPanel.selectTopShapeAt(e.getX(), e.getY(), false);
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (!isLinking) return;
        currentX = e.getX();
        currentY = e.getY();
        currentSnapPort = canvasPanel.findSnapPortAt(currentX, currentY);
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (!isLinking || startPort == null) return;

        Port endPort = canvasPanel.findSnapPortAt(e.getX(), e.getY());

        // Alt B.2: 終點不在其他基本物件的 port 上則不建立連結
        if (endPort != null && endPort.getOwner() != startPort.getOwner()) {
            canvasPanel.addLink(mode, startPort, endPort);
        }

        isLinking = false;
        startPort = null;
        currentSnapPort = null;
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseMoved(MouseEvent e) {
        canvasPanel.updateHoveredShapeAt(e.getX(), e.getY());
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseExited(MouseEvent e) {
        currentSnapPort = null;
        canvasPanel.clearHoveredShapes();
        canvasPanel.repaintCanvas();
    }

    @Override
    public void drawOverlay(Graphics g) {
        if (!isLinking || startPort == null) return;
        int endX = (currentSnapPort != null) ? currentSnapPort.getX() : currentX;
        int endY = (currentSnapPort != null) ? currentSnapPort.getY() : currentY;
        g.setColor(Color.BLACK);
        g.drawLine(startPort.getX(), startPort.getY(), endX, endY);
    }
}
