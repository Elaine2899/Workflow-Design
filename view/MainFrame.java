package view;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import controller.EditController;
import controller.Mode;

public class MainFrame extends JFrame {
    private Mode selectedMode = Mode.SELECT;
    private Mode temporaryCreateMode = null;
    private ToolbarPanel toolbarPanel;

    public MainFrame() {
        super("Workflow Design 編輯器");

        CanvasPanel canvasPanel = new CanvasPanel();
        EditController editController = new EditController(this, canvasPanel);
        setJMenuBar(new MenuBar(editController));

        setLayout(new BorderLayout());
        toolbarPanel = new ToolbarPanel(new ToolbarPanel.ToolbarActionListener() {
            @Override
            public void onAction(Mode mode) {
                canvasPanel.switchMode(mode);
                selectedMode = mode;
                temporaryCreateMode = null;
                toolbarPanel.setActiveMode(selectedMode);
            }

            @Override
            public void onTemporaryCreatePressed(Mode mode) {
                temporaryCreateMode = mode;
                canvasPanel.beginTemporaryObjectCreate(mode);
                toolbarPanel.setActiveMode(mode);
            }
        });

        canvasPanel.setObjectCreateListener((mode, drawSucceeded) -> {
            if (temporaryCreateMode != null && temporaryCreateMode == mode) {
                temporaryCreateMode = null;
                toolbarPanel.setActiveMode(selectedMode);
            }
        });

        toolbarPanel.setActiveMode(selectedMode);

        add(toolbarPanel, BorderLayout.WEST);
        add(canvasPanel, BorderLayout.CENTER);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
