package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OvalShape extends  Shape {
    private final List<Port> ports;

    public OvalShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.fColor = super.fColor;
        this.dColor = super.dColor;
        this.ports = createPorts();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fColor);
        g.fillOval(x, y, width, height);
        g.setColor(dColor);
        g.drawOval(x, y, width, height);
        drawLabel(g);
    }

    @Override
    public boolean containsPoint(int px, int py) {
        if (width <= 0 || height <= 0) {
            return false;
        }

        double rx = width / 2.0;
        double ry = height / 2.0;
        double cx = x + rx;
        double cy = y + ry;
        double nx = (px - cx) / rx;
        double ny = (py - cy) / ry;
        return nx * nx + ny * ny <= 1.0;
    }

    @Override
    public List<Port> getPorts() {
        return ports;
    }

    @Override
    public boolean isBasicObject() {
        return true;
    }

    private List<Port> createPorts() {
        List<Port> list = new ArrayList<>();

        // 4 ports: 上、右、下、左
        list.add(new Port(this, 0.5, 0.0));
        list.add(new Port(this, 1.0, 0.5));
        list.add(new Port(this, 0.5, 1.0));
        list.add(new Port(this, 0.0, 0.5));

        return Collections.unmodifiableList(list);
    }
}