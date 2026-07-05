package controller;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Color;
import model.Shape;
import view.CanvasPanel;
import view.MenuBar;

public class EditController implements MenuBar.EditMenuListener {
    private final Component parent;
    private final CanvasPanel canvasPanel;

    public EditController(Component parent, CanvasPanel canvasPanel) {
        this.parent = parent;
        this.canvasPanel = canvasPanel;
    }

    @Override
    public void onGroupRequested() {
        if (canvasPanel.getCurrentMode() != Mode.SELECT) return;
        canvasPanel.groupSelectedObjects();
    }

    @Override
    public void onUngroupRequested() {
        if (canvasPanel.getCurrentMode() != Mode.SELECT) return;
        canvasPanel.ungroupSelectedObject();
    }

    @Override
    public void onLabelRequested() {
        if (canvasPanel.getCurrentMode() != Mode.SELECT) return;

        Shape target = canvasPanel.getSingleSelectedBasicShape();
        if (target == null) return;

        JTextField labelField = new JTextField(target.getLabelName(), 16);
        Color[] chosenColor = new Color[]{target.getFillColor()};

        JPanel colorPreview = new JPanel();
        colorPreview.setBackground(chosenColor[0]);
        colorPreview.setOpaque(true);

        JButton chooseColorButton = new JButton("Choose...");
        chooseColorButton.addActionListener(e -> {
            Color picked = JColorChooser.showDialog(parent, "Label Color", chosenColor[0]);
            if (picked != null) {
                chosenColor[0] = picked;
                colorPreview.setBackground(picked);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Name:"));
        panel.add(labelField);
        panel.add(new JLabel("Color:"));
        panel.add(colorPreview);
        panel.add(chooseColorButton);

        Object[] options = {"Cancel", "OK"};
        int result = JOptionPane.showOptionDialog(
                parent, panel, "Customize Label Style",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == 1) {
            target.setLabelName(labelField.getText());
            target.setFillColor(chosenColor[0]);
            canvasPanel.repaintCanvas();
        }
    }
}
