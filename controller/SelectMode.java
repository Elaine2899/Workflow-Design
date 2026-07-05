package controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import model.Port;
import model.Shape;
import view.CanvasPanel;

public class SelectMode implements ModeStrategy {
    private static final int MIN_SIZE = 20;

    private enum SelectState { IDLE, PENDING_AREA, AREA_SELECTING, DRAGGING, RESIZING }

    private final CanvasPanel canvasPanel;
    private int startX, startY, currentX, currentY;
    private int lastDragX, lastDragY;
    private SelectState state = SelectState.IDLE;
    private Shape resizeTarget;
    private Port activeResizePort;
    // 縮放從按下當下的邊界開始計算，避免拖曳時誤差累積
    private Rectangle resizeStartBounds;

    public SelectMode(CanvasPanel canvasPanel, ModeManager modeManager) {
        this.canvasPanel = canvasPanel;
    }

    @Override
    public Mode getMode() {
        return Mode.SELECT;
    }

    @Override
    public void onMousePressed(MouseEvent e) {
        canvasPanel.setAnchorPoint(e.getX(), e.getY());
        startX = e.getX();
        startY = e.getY();
        currentX = startX;
        currentY = startY;
        lastDragX = startX;
        lastDragY = startY;

        Port selectedPort = canvasPanel.findSelectedBasicPortAt(startX, startY);
        if (selectedPort != null && selectedPort.getOwner().isBasicObject()) {
            state = SelectState.RESIZING;
            activeResizePort = selectedPort;
            resizeTarget = selectedPort.getOwner();
            resizeStartBounds = resizeTarget.getBounds();
            canvasPanel.setDrawing(false);
            return;
        }

        Shape hitShape = canvasPanel.findTopShapeAt(startX, startY);
        if (hitShape != null) {
            canvasPanel.selectTopShapeAt(startX, startY, true);
            state = SelectState.DRAGGING;
            canvasPanel.setDrawing(false);
            canvasPanel.repaintCanvas();
            return;
        }

        canvasPanel.clearSelection();
        canvasPanel.updateHoveredShapeAt(startX, startY);
        state = SelectState.PENDING_AREA;
        canvasPanel.setDrawing(false);
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseDragged(MouseEvent e) {
        if (state == SelectState.RESIZING) {
            resizeToPort(e.getX(), e.getY());
            canvasPanel.repaintCanvas();
            return;
        }

        if (state == SelectState.DRAGGING) {
            int dx = e.getX() - lastDragX;
            int dy = e.getY() - lastDragY;
            if (dx != 0 || dy != 0) {
                canvasPanel.moveSelectedShapesBy(dx, dy);
                lastDragX = e.getX();
                lastDragY = e.getY();
                canvasPanel.repaintCanvas();
            }
            return;
        }

        if (state != SelectState.PENDING_AREA && state != SelectState.AREA_SELECTING) return;

        if (state == SelectState.PENDING_AREA) {
            canvasPanel.clearSelection();
            state = SelectState.AREA_SELECTING;
        }

        currentX = e.getX();
        currentY = e.getY();
        canvasPanel.repaintCanvas();
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        if (state == SelectState.RESIZING) {
            resizeToPort(e.getX(), e.getY());
            state = SelectState.IDLE;
            resizeTarget = null;
            activeResizePort = null;
            resizeStartBounds = null;
            canvasPanel.repaintCanvas();
            return;
        }

        if (state == SelectState.DRAGGING) {
            state = SelectState.IDLE;
            canvasPanel.repaintCanvas();
            return;
        }

        if (state != SelectState.AREA_SELECTING) {
            state = SelectState.IDLE;
            return;
        }

        currentX = e.getX();
        currentY = e.getY();
        canvasPanel.selectBasicShapesInside(startX, startY, currentX, currentY);
        state = SelectState.IDLE;
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
        if (state != SelectState.AREA_SELECTING) return;

        int x = Math.min(startX, currentX);
        int y = Math.min(startY, currentY);
        int w = Math.abs(currentX - startX);
        int h = Math.abs(currentY - startY);

        Graphics2D g2 = (Graphics2D) g;
        canvasPanel.drawMarqueeHighlight(g2, startX, startY, currentX, currentY);
        Stroke oldStroke = g2.getStroke();
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
        g2.drawRect(x, y, w, h);
        g2.setStroke(oldStroke);
    }

    private void resizeToPort(int mouseX, int mouseY) {
        if (resizeTarget == null || activeResizePort == null || resizeStartBounds == null) return;

        double rx = activeResizePort.getRelativeX();
        double ry = activeResizePort.getRelativeY();

        int left   = resizeStartBounds.x;
        int right  = resizeStartBounds.x + resizeStartBounds.width;
        int top    = resizeStartBounds.y;
        int bottom = resizeStartBounds.y + resizeStartBounds.height;

        int newLeft   = left;
        int newTop    = top;
        int newWidth  = resizeStartBounds.width;
        int newHeight = resizeStartBounds.height;

        if (rx == 0.0 || rx == 1.0) {
            // fixedX 是拖曳時保持不動的那條邊
            int fixedX = (rx == 0.0) ? right : left;
            int width = Math.max(Math.abs(mouseX - fixedX), MIN_SIZE);
            newWidth = width;
            newLeft = (mouseX >= fixedX) ? fixedX : fixedX - width;
        }

        if (ry == 0.0 || ry == 1.0) {
            // fixedY 是拖曳時保持不動的那條邊
            int fixedY = (ry == 0.0) ? bottom : top;
            int height = Math.max(Math.abs(mouseY - fixedY), MIN_SIZE);
            newHeight = height;
            newTop = (mouseY >= fixedY) ? fixedY : fixedY - height;
        }

        resizeTarget.setBounds(newLeft, newTop, newWidth, newHeight);
    }
}
