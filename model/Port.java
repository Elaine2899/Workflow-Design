package model;

public class Port {
	private final Shape owner;
	private final double relativeX;
	private final double relativeY;

	public Port(Shape owner, double relativeX, double relativeY) {
		this.owner = owner;
		this.relativeX = relativeX;
		this.relativeY = relativeY;
	}

	public int getX() {
		return owner.getX() + (int) Math.round(owner.getWidth() * relativeX);
	}

	public int getY() {
		return owner.getY() + (int) Math.round(owner.getHeight() * relativeY);
	}

	public Shape getOwner() {
		return owner;
	}

	public double getRelativeX() {
		return relativeX;
	}

	public double getRelativeY() {
		return relativeY;
	}
}
