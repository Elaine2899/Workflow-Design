package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RectShape extends Shape {
    private final List<Port> ports;

    public RectShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.fColor = super.fColor;
        this.dColor = super.dColor;
        this.ports = createPorts();
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(fColor);
        g.fillRect(x, y, width, height);
        g.setColor(dColor);
        g.drawRect(x, y, width, height);
        drawLabel(g);
    }

    @Override
    public boolean containsPoint(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    @Override
    public boolean isBasicObject() {
        return true;
    }

    @Override
    public List<Port> getPorts() {
        return ports;
    }

    private List<Port> createPorts() {
        List<Port> list = new ArrayList<>();

        // 8 ports: 四角 + 上中 + 下中 + 左中 + 右中
        list.add(new Port(this, 0.0, 0.0));
        list.add(new Port(this, 0.5, 0.0));
        list.add(new Port(this, 1.0, 0.0));
        list.add(new Port(this, 1.0, 0.5));
        list.add(new Port(this, 1.0, 1.0));
        list.add(new Port(this, 0.5, 1.0));
        list.add(new Port(this, 0.0, 1.0));
        list.add(new Port(this, 0.0, 0.5));

        return Collections.unmodifiableList(list);
    }
}