package controller;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import view.CanvasPanel;

public class ModeManager {
    private final Map<Mode, ModeStrategy> modeMap = new HashMap<>();
    private ModeStrategy currentMode;
    private Mode restoreModeAfterTemporaryCreate;

    public ModeManager(CanvasPanel canvasPanel) {
        modeMap.put(Mode.SELECT,         new SelectMode(canvasPanel, this));
        modeMap.put(Mode.RECT,           new CreateObjectMode(canvasPanel, this, Mode.RECT));
        modeMap.put(Mode.OVAL,           new CreateObjectMode(canvasPanel, this, Mode.OVAL));
        modeMap.put(Mode.ASSOCIATION,    new CreateLinkMode(canvasPanel, this, Mode.ASSOCIATION));
        modeMap.put(Mode.GENERALIZATION, new CreateLinkMode(canvasPanel, this, Mode.GENERALIZATION));
        modeMap.put(Mode.COMPOSITION,    new CreateLinkMode(canvasPanel, this, Mode.COMPOSITION));
        currentMode = modeMap.get(Mode.SELECT);
    }

    public void switchMode(Mode mode) {
        ModeStrategy target = modeMap.get(mode);
        if (target != null) currentMode = target;
    }

    public void beginTemporaryObjectCreate(Mode mode) {
        restoreModeAfterTemporaryCreate = currentMode.getMode();
        // 記住現在是什麼模式
        switchMode(mode); // 切換到 RECT 或 OVAL
    }

    public boolean isTemporaryObjectCreateActive() {
        return restoreModeAfterTemporaryCreate != null;
    }

    public void finishObjectCreateAttempt(boolean drawSucceeded) {
        if (isTemporaryObjectCreateActive()) {
            Mode restoreMode = restoreModeAfterTemporaryCreate;
            restoreModeAfterTemporaryCreate = null;
            switchMode(restoreMode); // 還原到之前的模式
            return;
        }
        if (drawSucceeded) {
            switchMode(Mode.SELECT); // 直接回 Select
        }
    }

    public Mode getCurrentMode() {
        return currentMode.getMode();
    }

    public void onMousePressed(MouseEvent e)  { currentMode.onMousePressed(e); }
    public void onMouseReleased(MouseEvent e) { currentMode.onMouseReleased(e); }
    public void onMouseDragged(MouseEvent e)  { currentMode.onMouseDragged(e); }
    public void onMouseMoved(MouseEvent e)    { currentMode.onMouseMoved(e); }
    public void onMouseExited(MouseEvent e)   { currentMode.onMouseExited(e); }
    public void drawOverlay(Graphics g)       { currentMode.drawOverlay(g); }
}
