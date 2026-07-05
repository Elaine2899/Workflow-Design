package model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CanvasModel {
    private static final int PORT_HIT_RADIUS = 6;
    private static final int PORT_SNAP_RADIUS = 28;

    private final List<Shape> shapes = new ArrayList<>();
    private final List<ConnectionLink> links = new ArrayList<>();

    // --- Shape / Link 存取 ---

    public List<Shape> getShapes() {
        return Collections.unmodifiableList(shapes);
    }

    public List<ConnectionLink> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        bringToFront(shape);
    }

    public void addLink(String type, Port startPort, Port endPort) {
        links.add(new ConnectionLink(type, startPort, endPort));
    }

    // --- 選取狀態 ---

    public void selectTopShapeAt(int mx, int my, boolean bringSelectedToFront) {
        Shape selected = findTopShapeAt(mx, my);
        for (Shape s : shapes) {
            s.setSelected(s == selected);
        }
        if (bringSelectedToFront && selected != null) {
            bringToFront(selected);
        }
    }

    public void updateHoveredShapeAt(int mx, int my) {
        Shape hovered = findTopShapeAt(mx, my);
        for (Shape s : shapes) {
            s.setHovered(s == hovered);
        }
    }

    public void clearHoveredShapes() {
        for (Shape s : shapes) {
            s.setHovered(false);
        }
    }

    public void clearSelection() {
        for (Shape s : shapes) {
            s.setSelected(false);
        }
    }

    public List<Shape> getSelectedShapes() {
        List<Shape> selected = new ArrayList<>();
        for (Shape s : shapes) {
            if (s.isSelected()) selected.add(s);
        }
        return selected;
    }

    public Shape getSingleSelectedBasicShape() {
        Shape candidate = null;
        for (Shape s : shapes) {
            if (!s.isSelected() || !s.isBasicObject()) continue;
            if (candidate != null) return null;
            candidate = s;
        }
        return candidate;
    }

    // --- 移動 ---

    public void moveSelectedShapesBy(int dx, int dy) {
        for (Shape s : shapes) {
            if (s.isSelected()) s.move(dx, dy);
        }
    }

    // --- 框選 ---

    public int selectBasicShapesInside(int x1, int y1, int x2, int y2) {
        Rectangle area = normalizeRect(x1, y1, x2, y2);
        int count = 0;
        for (Shape s : shapes) {
            boolean shouldSelect = isGroupableObject(s) && area.contains(s.getBounds());
            s.setSelected(shouldSelect);
            if (shouldSelect) count++;
        }
        return count;
    }

    // --- Group / Ungroup ---

    public boolean groupSelectedObjects() {
        List<Shape> selected = getSelectedShapes();
        if (selected.size() < 2) return false;
        CompositeObject composite = new CompositeObject(selected);
        shapes.removeAll(selected);
        clearSelection();
        shapes.add(composite);
        composite.setSelected(true);
        bringToFront(composite);
        return true;
    }

    public boolean ungroupSelectedObject() {
        List<Shape> selected = getSelectedShapes();
        if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject)) return false;
        CompositeObject composite = (CompositeObject) selected.get(0);
        shapes.remove(composite);
        for (Shape child : composite.getChildren()) {
            child.setSelected(true);
            shapes.add(child);
        }
        normalizeDepthByCurrentOrder();
        return true;
    }

    // --- Hit testing ---

    public Shape findTopShapeAt(int mx, int my) {
        for (Shape s : getShapesByDepthAscending()) {
            if (s.containsPoint(mx, my)) return s;
        }
        return null;
    }

    // --- Port 查詢 ---

    public Port findSelectedBasicPortAt(int mx, int my) {
        for (Shape s : getShapesByDepthAscending()) {
            if (!s.isSelected() || !s.isBasicObject()) continue;
            for (Port p : s.getPorts()) {
                if (isInPortRange(mx, my, p)) return p;
            }
        }
        return null;
    }

    public Port findSnapPortAt(int mx, int my) {
        Port nearest = findNearestPortInRadius(mx, my, PORT_SNAP_RADIUS);
        if (nearest != null) return nearest;
        Shape top = findTopShapeAt(mx, my);
        if (top == null || !top.isBasicObject()) return null;
        return findNearestPortOnShape(top, mx, my);
    }

    // --- 型別判斷 ---

    public boolean isGroupableObject(Shape shape) {
        return shape.isBasicObject() || shape instanceof CompositeObject;
    }

    // --- Depth 管理 ---

    public void bringToFront(Shape target) {
        List<Shape> ordered = getShapesByDepthAscending();
        ordered.remove(target);
        ordered.add(0, target);
        applyDepthOrder(ordered);
    }

    public List<Shape> getShapesByDepthDescending() {
        List<Shape> ordered = new ArrayList<>(shapes);
        ordered.sort(Comparator.comparingInt(Shape::getDepth).reversed());
        return ordered;
    }

    public List<Shape> getShapesByDepthAscending() {
        List<Shape> ordered = new ArrayList<>(shapes);
        ordered.sort(Comparator.comparingInt(Shape::getDepth));
        return ordered;
    }

    // --- Private helpers ---

    private void normalizeDepthByCurrentOrder() {
        applyDepthOrder(getShapesByDepthAscending());
    }

    private void applyDepthOrder(List<Shape> ordered) {
        int depth = 0;
        for (Shape s : ordered) {
            s.setDepth(depth);
            if (depth < 99) depth++;
        }
    }

    private boolean isInPortRange(int mx, int my, Port port) {
        return Math.abs(mx - port.getX()) <= PORT_HIT_RADIUS
            && Math.abs(my - port.getY()) <= PORT_HIT_RADIUS;
    }

    private Port findNearestPortInRadius(int mx, int my, int radius) {
        int radiusSq = radius * radius;
        int bestDistSq = Integer.MAX_VALUE;
        Port best = null;
        for (Shape s : getShapesByDepthAscending()) {
            if (!s.isBasicObject()) continue;
            for (Port p : s.getPorts()) {
                int dx = mx - p.getX();
                int dy = my - p.getY();
                int distSq = dx * dx + dy * dy;
                if (distSq <= radiusSq && distSq < bestDistSq) {
                    bestDistSq = distSq;
                    best = p;
                }
            }
        }
        return best;
    }

    private Port findNearestPortOnShape(Shape shape, int mx, int my) {
        Port best = null;
        int bestDistSq = Integer.MAX_VALUE;
        for (Port p : shape.getPorts()) {
            int dx = mx - p.getX();
            int dy = my - p.getY();
            int distSq = dx * dx + dy * dy;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = p;
            }
        }
        return best;
    }

    private Rectangle normalizeRect(int x1, int y1, int x2, int y2) {
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2),
                             Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
}
