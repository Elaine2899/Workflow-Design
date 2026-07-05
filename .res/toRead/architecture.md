# 專案架構導覽

## 整體架構：MVC 三層

```
view/          → 畫面 + 使用者輸入
controller/    → 邏輯 + 模式切換
model/         → 資料 + 形狀定義
```

---

## 程式啟動

`Main.java` 只做一件事：建立 `MainFrame` 並顯示。

```java
SwingUtilities.invokeLater(() -> {
    MainFrame frame = new MainFrame();
    frame.setVisible(true);
});
```

---

## 第一層：MainFrame 組裝畫面

`MainFrame` 把三個元件組在一起：

```
MainFrame
├── ToolbarPanel     左側按鈕列
├── CanvasPanel      中間畫布
└── MenuBar          上方選單（File / Edit）
```

`EditController` 也由 MainFrame 建立，處理 Edit 選單的 Group / Ungroup / Label。

**MainFrame 自己不做邏輯**，負責：
1. 把元件排版放好
2. 把 ToolbarPanel 的按鈕事件轉給 CanvasPanel
3. 聽 CanvasPanel 說「物件畫完了」，更新按鈕狀態

---

## 第二層：CanvasPanel 是樞紐

`CanvasPanel` 站在 View 和 Controller 的交界：

```
CanvasPanel
├── CanvasModel      資料（shapes + links）
└── ModeManager      目前是哪個模式
```

滑鼠事件進來，`CanvasPanel` 全部交給 `ModeManager`：

```java
mousePressed  → modeManager.onMousePressed(e)
mouseDragged  → modeManager.onMouseDragged(e)
mouseReleased → modeManager.onMouseReleased(e)
```

繪製時，從 `CanvasModel` 拿資料畫出來，再請 `ModeManager` 畫 overlay（框選虛線框、拉線預覽）。

---

## 第三層：ModeManager + Strategy Pattern

`ModeManager` 持有六個 Strategy，對應六個按鈕：

```
modeMap
├── SELECT         → SelectMode
├── RECT           → CreateObjectMode（mode=RECT）
├── OVAL           → CreateObjectMode（mode=OVAL）
├── ASSOCIATION    → CreateLinkMode（mode=ASSOCIATION）
├── GENERALIZATION → CreateLinkMode（mode=GENERALIZATION）
└── COMPOSITION    → CreateLinkMode（mode=COMPOSITION）
```

切換按鈕 = 換掉 `currentMode`，之後所有滑鼠事件都由新的 Strategy 處理。

**為什麼用 Strategy Pattern？**
新增第七種模式時，只需新增一個實作 `ModeStrategy` 的 class，`ModeManager` 和 `CanvasPanel` 完全不用改。這是 Open/Closed Principle。

---

## 第四層：CanvasModel 管資料

`CanvasModel` 是純資料層，不碰任何 UI：

```
CanvasModel
├── List<Shape> shapes
├── List<ConnectionLink> links
└── 方法：hit testing、depth 管理、group/ungroup、port 查詢
```

**為什麼抽出 CanvasModel？**
原本 `CanvasPanel` 同時管資料和 UI（God Class），違反 SRP。
抽出後：
- `CanvasPanel` → 只管繪製與事件轉發
- `CanvasModel` → 只管資料與業務邏輯

---

## 第五層：Shape 的繼承樹（Composite Pattern）

```
Shape（abstract）
├── RectShape        矩形，8 個 Port
├── OvalShape        橢圓，4 個 Port
└── CompositeObject  群組，0 個 Port，children 是 List<Shape>
```

`CompositeObject` 繼承 `Shape`，讓群組本身也可以被放進另一個群組，形成無限巢狀的樹狀結構（符合規格：composite 可包 composite）。

**Port 的座標**動態計算：
```
getX() = owner.getX() + round(owner.getWidth() * relativeX)
getY() = owner.getY() + round(owner.getHeight() * relativeY)
```
形狀移動或縮放後，Port 自動跟隨，`ConnectionLink` 也就自動更新位置，不需額外通知。

---

## 一筆操作的完整路徑

以「按下 Rect 按鈕然後畫一個矩形」為例：

```
1. 使用者按下 Rect 按鈕
   ToolbarPanel
   → MainFrame.onTemporaryCreatePressed(RECT)
   → CanvasPanel.beginTemporaryObjectCreate(RECT)
   → ModeManager 儲存目前模式，切換到 CreateObjectMode(RECT)

2. 使用者在 Canvas 拖曳
   mousePressed  → CreateObjectMode.onMousePressed  → setAnchorPoint
   mouseDragged  → CreateObjectMode.onMouseDragged  → setCurrentPoint + repaint
   （每次 repaint，drawOverlay 呼叫 createPreviewShape 畫預覽框）

3. 使用者放開滑鼠
   mouseReleased → CreateObjectMode.onMouseReleased
   → createAndAddShape(RECT) → CanvasModel.addShape(new RectShape)
   → finishObjectCreateAttempt → ModeManager 還原回原本模式
   → notifyObjectCreateFinished → MainFrame 更新按鈕狀態
```

---

## 深入閱讀

- [SelectMode 五個狀態的運作](../controller/SelectMode.java)
- [Port + Resize 幾何計算](../controller/SelectMode.java) — `resizeToPort` 方法
- [CompositeObject group/ungroup 流程](../model/CanvasModel.java) — `groupSelectedObjects` / `ungroupSelectedObject`
- [depth 管理](../model/CanvasModel.java) — `bringToFront` / `getShapesByDepthAscending`
