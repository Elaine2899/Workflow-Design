package model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;

public class ConnectionLink {
    private final Port startPort;
    private final Port endPort;
    private final String type;

    public ConnectionLink(String type, Port startPort, Port endPort) {
        this.type = type;
        this.startPort = startPort;
        this.endPort = endPort;
    }

    public Port getStartPort() {
        return startPort;
    }

    public Port getEndPort() {
        return endPort;
    }

    public String getType() {
        return type;
    }

    public void draw(Graphics2D g2) {
        int x1 = startPort.getX();
        int y1 = startPort.getY();
        int x2 = endPort.getX();
        int y2 = endPort.getY();

        Stroke oldStroke = g2.getStroke();
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x1, y1, x2, y2);

        double angle = Math.atan2(y2 - y1, x2 - x1);
        switch (type) {
            case "generalization":
                drawHollowTriangle(g2, x2, y2, angle);
                break;
            case "composition":
                drawFilledDiamond(g2, x2, y2, angle);
                break;
            case "association":
                drawOpenArrow(g2, x2, y2, angle);
                break;
        }

        g2.setStroke(oldStroke);
    }

    private void drawOpenArrow(Graphics2D g2, int tipX, int tipY, double angle) {
        int size = 12;
        int xA = tipX - (int) (size * Math.cos(angle - Math.PI / 6));
        int yA = tipY - (int) (size * Math.sin(angle - Math.PI / 6));
        int xB = tipX - (int) (size * Math.cos(angle + Math.PI / 6));
        int yB = tipY - (int) (size * Math.sin(angle + Math.PI / 6));
        g2.drawLine(tipX, tipY, xA, yA);
        g2.drawLine(tipX, tipY, xB, yB);
    }

    private void drawHollowTriangle(Graphics2D g2, int tipX, int tipY, double angle) {
        int length = 14;
        int halfWidth = 7;

        int baseX = tipX - (int) (length * Math.cos(angle));
        int baseY = tipY - (int) (length * Math.sin(angle));

        int leftX = baseX + (int) (halfWidth * Math.cos(angle + Math.PI / 2));
        int leftY = baseY + (int) (halfWidth * Math.sin(angle + Math.PI / 2));
        int rightX = baseX + (int) (halfWidth * Math.cos(angle - Math.PI / 2));
        int rightY = baseY + (int) (halfWidth * Math.sin(angle - Math.PI / 2));

        Polygon triangle = new Polygon();
        triangle.addPoint(tipX, tipY);
        triangle.addPoint(leftX, leftY);
        triangle.addPoint(rightX, rightY);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(triangle);
        g2.setColor(Color.BLACK);
        g2.drawPolygon(triangle);
    }

    private void drawFilledDiamond(Graphics2D g2, int tipX, int tipY, double angle) {
        int length = 14;
        int halfWidth = 7;

        int midX = tipX - (int) (length * Math.cos(angle));
        int midY = tipY - (int) (length * Math.sin(angle));
        int tailX = tipX - (int) (2 * length * Math.cos(angle));
        int tailY = tipY - (int) (2 * length * Math.sin(angle));

        int leftX = midX + (int) (halfWidth * Math.cos(angle + Math.PI / 2));
        int leftY = midY + (int) (halfWidth * Math.sin(angle + Math.PI / 2));
        int rightX = midX + (int) (halfWidth * Math.cos(angle - Math.PI / 2));
        int rightY = midY + (int) (halfWidth * Math.sin(angle - Math.PI / 2));

        Polygon diamond = new Polygon();
        diamond.addPoint(tipX, tipY);
        diamond.addPoint(leftX, leftY);
        diamond.addPoint(tailX, tailY);
        diamond.addPoint(rightX, rightY);
        g2.setColor(Color.BLACK);
        g2.fillPolygon(diamond);
        g2.drawPolygon(diamond);
    }
}