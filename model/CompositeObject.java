package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeObject extends Shape {
	private final List<Shape> children;

	public CompositeObject(List<Shape> children) {
		super(0, 0, 0, 0);
		this.children = new ArrayList<>(children);
		// 子物件進入群組後由最外層統一呈現互動狀態，避免內外層同時顯示框線。
		clearChildInteractionStates();
		refreshBounds();
	}

	public List<Shape> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public void draw(Graphics g) {
		for (Shape child : children) {
			child.draw(g);
		}

		if (isSelected() || isHovered()) {
			refreshBounds();
			Graphics2D g2 = (Graphics2D) g;
			Stroke oldStroke = g2.getStroke();
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
			g2.drawRect(x, y, width, height);
			g2.setStroke(oldStroke);
		}
	}

	@Override
	public List<Port> getPorts() {
		return Collections.emptyList();
	}

	@Override
	public boolean containsPoint(int px, int py) {
		refreshBounds();
		return getBounds().contains(px, py);
	}

	@Override
	public Rectangle getBounds() {
		refreshBounds();
		return super.getBounds();
	}

	@Override
	public boolean isBasicObject() {
		return false;
	}

	@Override
	public void move(int dx, int dy) {
		for (Shape child : children) {
			child.move(dx, dy);
		}
		refreshBounds();
	}

	private void clearChildInteractionStates() {
		for (Shape child : children) {
			clearInteractionStateRecursively(child);
		}
	}

	private void clearInteractionStateRecursively(Shape shape) {
		shape.setSelected(false);
		shape.setHovered(false);

		if (shape instanceof CompositeObject composite) {
			for (Shape nested : composite.getChildren()) {
				clearInteractionStateRecursively(nested);
			}
		}
	}

	private void refreshBounds() {
		if (children.isEmpty()) {
			x = 0;
			y = 0;
			width = 0;
			height = 0;
			return;
		}

		Rectangle bounds = new Rectangle(children.get(0).getBounds());
		for (int i = 1; i < children.size(); i++) {
			bounds = bounds.union(children.get(i).getBounds());
		}

		x = bounds.x;
		y = bounds.y;
		width = bounds.width;
		height = bounds.height;
	}
}
