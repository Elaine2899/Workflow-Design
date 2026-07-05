package controller;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import model.Shape;
import view.CanvasPanel;

public class CreateObjectMode implements ModeStrategy {
    private final CanvasPanel canvasPanel;
    private final ModeManager modeManager;
    private final Mode mode;

    public CreateObjectMode(CanvasPanel canvasPanel, ModeManager modeManager, Mode mode) {
        this.canvasPanel = canvasPanel;
        this.modeManager = modeManager;
        this.mode = mode;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        canvasPanel.setAnchorPoint(e.getX(), e.getY());
        canvasPanel.setDrawing(true);
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        canvasPanel.setCurrentPoint(e.getX(), e.getY());
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        canvasPanel.setCurrentPoint(e.getX(), e.getY());
        canvasPanel.setDrawing(false);

        boolean drawSucceeded = canvasPanel.createAndAddShape(mode);
        modeManager.finishObjectCreateAttempt(drawSucceeded);
        canvasPanel.notifyObjectCreateFinished(mode, drawSucceeded);
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseMoved(MouseEvent e) {
        canvasPanel.updateHoveredShapeAt(e.getX(), e.getY());
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseExited(MouseEvent e) {
        canvasPanel.clearHoveredShapes();
        canvasPanel.repaintCanvas();
    }

    @Override
    public void drawOverlay(Graphics g) {
        if (!canvasPanel.isDrawing()) return;
        Shape preview = canvasPanel.createPreviewShape(mode);
        if (preview != null) preview.draw(g);
    }
}
