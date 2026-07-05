package model;

import java.awt.*;
import java.util.List;

public abstract class Shape {
    public enum Status {Normal, Select, Hover}
    protected Status currentStatus = Status.Normal;
    private boolean selected;
    private boolean hovered;
    protected int x, y, width, height;
    protected int depth;
    public Color fColor = Color.GRAY;
    public Color dColor = Color.BLACK;
    private String labelName = "";

    public Shape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.depth = 99;
    }

    public abstract void draw(Graphics g);

    public abstract List<Port> getPorts();

    public abstract boolean containsPoint(int px, int py);

    public abstract boolean isBasicObject();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {return width;}

    public int getHeight() {return height;}

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getDepth() {
        return depth;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName == null ? "" : labelName;
    }

    public Color getFillColor() {
        return fColor;
    }

    public void setFillColor(Color fillColor) {
        if (fillColor != null) {
            this.fColor = fillColor;
        }
    }

    public void setDepth(int depth) {
        if (depth < 0) {
            this.depth = 0;
        } else if (depth > 99) {
            this.depth = 99;
        } else {
            this.depth = depth;
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateStatus();
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
        updateStatus();
    }

    public void drawPorts(Graphics g) {
        if (!isSelected() && !isHovered()) {
            return;
        }

        g.setColor(Color.BLACK);
        int size = 8;
        // 連接點中心偏移量，這樣連接點的中心就會對齊到形狀的邊界，而不是左上角
        int half = size / 2;

        for (Port port : getPorts()) {
            g.fillRect(port.getX() - half, port.getY() - half, size, size);
        }
    }

    private void updateStatus() {
        if (selected) {
            currentStatus = Status.Select;
        } else if (hovered) {
            currentStatus = Status.Hover;
        } else {
            currentStatus = Status.Normal;
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    protected void drawLabel(Graphics g) {
        if (labelName == null || labelName.trim().isEmpty()) {
            return;
        }

        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(labelName);
        int textX = x + (width - textW) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();

        // 根據背景顏色決定文字顏色，確保文字在不同背景下都清晰可見
        int luminance = (fColor.getRed() * 299 + fColor.getGreen() * 587 + fColor.getBlue() * 114) / 1000;
        g.setColor(luminance < 128 ? Color.WHITE : Color.BLACK);
        g.drawString(labelName, textX, textY);
    }
}