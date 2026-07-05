# SelectMode 五個狀態

## 狀態圖

```
         程式啟動
              ↓
           IDLE
              │
    ──────────┼──────────────────────
    按到 port  │按到 shape  │按到空白
              ↓            ↓         ↓
          RESIZING      DRAGGING   PENDING_AREA
              │            │         │
           拖曳           拖曳      開始拖曳
              │            │         ↓
              │            │    AREA_SELECTING
              │            │         │
    ──────────┴────────────┴─────────┘
                    放開滑鼠
                       ↓
                     IDLE
```

---

## IDLE — 什麼都沒發生

初始狀態。`onMouseMoved` 在這個狀態下持續執行，讓 hover 效果（顯示 port）正常運作。

---

## RESIZING — 縮放物件

**觸發條件**（`onMousePressed`）：

```java
Port selectedPort = canvasPanel.findSelectedBasicPortAt(startX, startY);
if (selectedPort != null && selectedPort.getOwner().isBasicObject()) {
    state = SelectState.RESIZING;
    activeResizePort = selectedPort;         // 記住拖的是哪個 port
    resizeTarget = selectedPort.getOwner();  // 記住是哪個形狀
    resizeStartBounds = resizeTarget.getBounds(); // 記住按下當下的邊界
}
```

兩個條件缺一不可：
1. 點到的位置有 port（已選取的基本物件的 port）
2. port 的主人是 basic object（Composite 不能 resize）

**為什麼記錄 `resizeStartBounds`？**
每次拖曳都從**初始邊界**重新計算，不是從上一幀累加，避免誤差累積。

**拖曳時（`resizeToPort`）：**
- 依照 port 的 `relativeX/Y` 判斷哪條邊是「固定邊」
- 例如拖左上角（rx=0.0, ry=0.0）：固定邊是右邊和下邊
- 計算新的 left/top/width/height，強制 `≥ MIN_SIZE(20px)`
- 支援反向拖曳（越過對角）：用 `Math.abs` + 重新計算 newLeft/newTop

---

## DRAGGING — 移動物件

**觸發條件**：點到畫布上有形狀的位置（不是 port）。

```java
Shape hitShape = canvasPanel.findTopShapeAt(startX, startY);
if (hitShape != null) {
    canvasPanel.selectTopShapeAt(startX, startY, true); // 選取並 bringToFront
    state = SelectState.DRAGGING;
}
```

**拖曳時**：用**增量**移動，不是絕對座標：

```java
int dx = e.getX() - lastDragX;  // 這幀和上幀的差
int dy = e.getY() - lastDragY;
canvasPanel.moveSelectedShapesBy(dx, dy);
lastDragX = e.getX();            // 更新，下幀繼續算差
```

為什麼用增量？因為可能同時移動多個選取的形狀，每個都要移動同樣的 dx/dy。

---

## PENDING_AREA — 等待確認框選

**觸發條件**：點到空白處（沒有形狀）。

這是個「**過渡狀態**」，代表：
- 已按下滑鼠
- 但還沒開始拖曳
- 尚未確定是「點一下清除選取」還是「要框選」

```java
canvasPanel.clearSelection(); // 先清掉選取
state = SelectState.PENDING_AREA;
```

---

## AREA_SELECTING — 框選進行中

**觸發條件**：從 `PENDING_AREA` 開始拖曳。

```java
if (state == SelectState.PENDING_AREA) {
    canvasPanel.clearSelection();
    state = SelectState.AREA_SELECTING;
}
currentX = e.getX();
currentY = e.getY();
canvasPanel.repaintCanvas(); // 觸發 drawOverlay 畫虛線框
```

`drawOverlay` 只在這個狀態執行：
- 畫藍色半透明 highlight（落在框選區的形狀）
- 畫黑色虛線框

**放開滑鼠時：**
```java
canvasPanel.selectBasicShapesInside(startX, startY, currentX, currentY);
state = SelectState.IDLE;
```
把完全落在選取矩形內的 groupable 物件全部設為 selected。

---

## 為什麼需要 PENDING_AREA？

如果沒有它，點空白處「清除選取」和「開始框選」的邏輯會衝突：

```
沒有 PENDING_AREA 的問題：
  mousePressed 空白 → 立刻進 AREA_SELECTING
  使用者只是輕點（沒拖曳）→ mouseReleased → 試圖框選，但沒有框選矩形

有 PENDING_AREA：
  mousePressed 空白 → PENDING_AREA（先等等）
  如果有拖曳     → AREA_SELECTING（確定要框選了）
  如果直接放開   → 回 IDLE（只是輕點，什麼都不做）
```

---

## 各狀態對應的方法行為總覽

| 狀態 | onMouseDragged | onMouseReleased | drawOverlay |
|---|---|---|---|
| IDLE | 無作用 | 無作用 | 無 |
| RESIZING | resizeToPort | resizeToPort + 清除狀態 | 無 |
| DRAGGING | moveSelectedShapesBy | 回 IDLE | 無 |
| PENDING_AREA | 轉 AREA_SELECTING | 回 IDLE | 無 |
| AREA_SELECTING | 更新 currentX/Y | selectBasicShapesInside + 回 IDLE | 畫虛線框 + highlight |
