package view;

import controller.Mode;
import controller.ModeManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import model.CanvasModel;
import model.CompositeObject;
import model.ConnectionLink;
import model.OvalShape;
import model.Port;
import model.RectShape;
import model.Shape;

public class CanvasPanel extends JPanel {

    public interface ObjectCreateListener {
        void onObjectCreateFinished(Mode mode, boolean drawSucceeded);
    }

    private final CanvasModel model = new CanvasModel();
    private final ModeManager modeManager;
    private int lastX, lastY, currentX, currentY;
    private boolean isDrawing = false;
    private ObjectCreateListener objectCreateListener;

    public CanvasPanel() {
        setBackground(Color.WHITE);
        modeManager = new ModeManager(this);

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { modeManager.onMousePressed(e); }
            @Override public void mouseMoved(MouseEvent e)    { modeManager.onMouseMoved(e); }
            @Override public void mouseExited(MouseEvent e)   { modeManager.onMouseExited(e); }
            @Override public void mouseReleased(MouseEvent e) { modeManager.onMouseReleased(e); }
            @Override public void mouseDragged(MouseEvent e)  { modeManager.onMouseDragged(e); }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    // --- 繪製 ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (ConnectionLink link : model.getLinks()) {
            link.draw(g2);
        }
        for (Shape shape : model.getShapesByDepthDescending()) {
            shape.draw(g);
            shape.drawPorts(g);
        }
        modeManager.drawOverlay(g);
    }

    public void drawMarqueeHighlight(Graphics2D g2, int x1, int y1, int x2, int y2) {
        Rectangle area = normalizeRect(x1, y1, x2, y2);
        Color oldColor = g2.getColor();
        g2.setColor(new Color(60, 150, 255, 90));
        for (Shape shape : model.getShapes()) {
            if (!model.isGroupableObject(shape) || !area.contains(shape.getBounds())) continue;
            if (shape instanceof RectShape) {
                g2.fillRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            } else if (shape instanceof OvalShape) {
                g2.fillOval(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
            } else if (shape instanceof CompositeObject) {
                Rectangle b = shape.getBounds();
                g2.fillRect(b.x, b.y, b.width, b.height);
            }
        }
        g2.setColor(oldColor);
    }

    // --- 物件建立 ---

    public Shape createPreviewShape(Mode mode) {
        int x = Math.min(lastX, currentX);
        int y = Math.min(lastY, currentY);
        int w = Math.abs(lastX - currentX);
        int h = Math.abs(lastY - currentY);
        if (w <= 0 || h <= 0) return null;
        if (mode == Mode.RECT) return new RectShape(x, y, w, h);
        if (mode == Mode.OVAL) return new OvalShape(x, y, w, h);
        return null;
    }

    public boolean createAndAddShape(Mode mode) {
        Shape shape = createPreviewShape(mode);
        if (shape == null) return false;
        model.addShape(shape);
        return true;
    }

    // --- 委派給 CanvasModel ---

    public void selectTopShapeAt(int mx, int my, boolean bringToFront) {
        model.selectTopShapeAt(mx, my, bringToFront);
    }
    public void updateHoveredShapeAt(int mx, int my) { model.updateHoveredShapeAt(mx, my); }
    public void clearHoveredShapes()                 { model.clearHoveredShapes(); }
    public void clearSelection()                     { model.clearSelection(); }
    public void moveSelectedShapesBy(int dx, int dy) { model.moveSelectedShapesBy(dx, dy); }
    public int  selectBasicShapesInside(int x1, int y1, int x2, int y2) {
        return model.selectBasicShapesInside(x1, y1, x2, y2);
    }
    public Shape findTopShapeAt(int mx, int my)               { return model.findTopShapeAt(mx, my); }
    public Port  findSelectedBasicPortAt(int mx, int my)      { return model.findSelectedBasicPortAt(mx, my); }
    public Port  findSnapPortAt(int mx, int my)               { return model.findSnapPortAt(mx, my); }
    public Shape getSingleSelectedBasicShape()                { return model.getSingleSelectedBasicShape(); }
    public boolean groupSelectedObjects()                     { return model.groupSelectedObjects(); }
    public boolean ungroupSelectedObject()                    { return model.ungroupSelectedObject(); }
    public boolean isBasicObjectShape(Shape shape)            { return shape.isBasicObject(); }
    public void addLink(Mode mode, Port startPort, Port endPort) {
        // Mode 轉小寫字串供 ConnectionLink 內部 switch 使用
        model.addLink(mode.name().toLowerCase(), startPort, endPort);
    }

    // --- 模式管理 ---

    public void switchMode(Mode mode)                  { modeManager.switchMode(mode); }
    public void beginTemporaryObjectCreate(Mode mode)  { modeManager.beginTemporaryObjectCreate(mode); }
    public boolean isTemporaryObjectCreateActive()     { return modeManager.isTemporaryObjectCreateActive(); }
    public Mode getCurrentMode()                       { return modeManager.getCurrentMode(); }

    // --- 繪製狀態 ---

    public void setAnchorPoint(int x, int y) {
        this.lastX = x; this.lastY = y; this.currentX = x; this.currentY = y;
    }
    public void setCurrentPoint(int x, int y) { this.currentX = x; this.currentY = y; }
    public void setDrawing(boolean drawing)    { this.isDrawing = drawing; }
    public boolean isDrawing()                 { return isDrawing; }
    public void repaintCanvas()                { repaint(); }

    // --- Listener ---

    public void setObjectCreateListener(ObjectCreateListener listener) {
        this.objectCreateListener = listener;
    }
    public void notifyObjectCreateFinished(Mode mode, boolean drawSucceeded) {
        if (objectCreateListener != null) objectCreateListener.onObjectCreateFinished(mode, drawSucceeded);
    }

    private Rectangle normalizeRect(int x1, int y1, int x2, int y2) {
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2),
                             Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}
