package view;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import controller.Mode;

public class ToolbarPanel extends JPanel {

    public interface ToolbarActionListener {
        void onAction(Mode mode);
        void onTemporaryCreatePressed(Mode mode);
    }

    private final Map<Mode, JButton> buttonMap = new HashMap<>();
    private Mode activeMode = Mode.SELECT;

    public ToolbarPanel(ToolbarActionListener listener) {
        setLayout(new GridLayout(6, 1));

        add(createButton("Select",         Mode.SELECT,         listener, false));
        add(createButton("Association",    Mode.ASSOCIATION,    listener, false));
        add(createButton("Generalization", Mode.GENERALIZATION, listener, false));
        add(createButton("Composition",    Mode.COMPOSITION,    listener, false));
        add(createButton("Rectangle",      Mode.RECT,           listener, true));
        add(createButton("Oval",           Mode.OVAL,           listener, true));

        setActiveMode(Mode.SELECT);
    }

    private JButton createButton(String text, Mode mode, ToolbarActionListener listener, boolean temporaryCreate) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        if (temporaryCreate) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    listener.onTemporaryCreatePressed(mode);
                }
            });
        } else {
            button.addActionListener(e -> listener.onAction(mode));
        }

        buttonMap.put(mode, button);
        return button;
    }

    public void setActiveMode(Mode mode) {
        this.activeMode = mode;
        for (Map.Entry<Mode, JButton> entry : buttonMap.entrySet()) {
            boolean isActive = entry.getKey() == mode;
            JButton btn = entry.getValue();
            btn.setBackground(isActive ? Color.BLACK : Color.LIGHT_GRAY);
            btn.setForeground(isActive ? Color.WHITE : Color.BLACK);
        }
    }
}
