# CompositeObject：Group / Ungroup 流程

## Group 流程

### 觸發路徑

```
使用者點 Edit > Group
→ EditController.onGroupRequested()
→ canvasPanel.groupSelectedObjects()
→ CanvasModel.groupSelectedObjects()
```

### CanvasModel.groupSelectedObjects()

```java
public boolean groupSelectedObjects() {
    List<Shape> selected = getSelectedShapes();
    if (selected.size() < 2) return false;        // D.1：不足兩個不動作
    CompositeObject composite = new CompositeObject(selected);
    shapes.removeAll(selected);                    // 把子物件從頂層移除
    clearSelection();
    shapes.add(composite);                         // 加入新的 composite
    composite.setSelected(true);                   // composite 本身被選取
    bringToFront(composite);                       // 置頂
    return true;
}
```

### shapes 列表的變化

```
Group 前：shapes = [RectA, OvalB, RectC]（RectA 和 OvalB 被選取）

Group 後：shapes = [RectC, Composite]
                          └── children: [RectA, OvalB]
```

`RectA` 和 `OvalB` 從頂層消失，被包進 `Composite` 的 children。

---

### CompositeObject 建構子做了什麼

```java
public CompositeObject(List<Shape> children) {
    super(0, 0, 0, 0);                  // 先給 0，等等再算
    this.children = new ArrayList<>(children);
    clearChildInteractionStates();      // 清掉所有子物件的 selected/hovered
    refreshBounds();                    // 計算真正的 x/y/width/height
}
```

**為什麼要 `clearChildInteractionStates`？**
子物件進入群組前可能處於 selected 狀態（因為使用者剛框選它們）。
若不清除，選取群組時內部子物件也會各自顯示框線，造成視覺混亂。
群組後改由最外層的 CompositeObject 統一呈現互動狀態。

**`clearChildInteractionStates` 會遞迴：**
```java
private void clearInteractionStateRecursively(Shape shape) {
    shape.setSelected(false);
    shape.setHovered(false);
    if (shape instanceof CompositeObject composite) {
        for (Shape nested : composite.getChildren()) {
            clearInteractionStateRecursively(nested); // 繼續往深處清
        }
    }
}
```
支援 composite 包 composite 的情況（規格允許無限巢狀）。

---

### refreshBounds 怎麼算邊界

```java
private void refreshBounds() {
    Rectangle bounds = new Rectangle(children.get(0).getBounds());
    for (int i = 1; i < children.size(); i++) {
        bounds = bounds.union(children.get(i).getBounds()); // 聯集
    }
    x = bounds.x;
    y = bounds.y;
    width = bounds.width;
    height = bounds.height;
}
```

用 `Rectangle.union()` 依序合併每個 child 的邊界矩形，最終得到「能包住所有子物件的最小矩形」，符合規格的 Composite 定義。

**refreshBounds 何時被呼叫？**
| 方法 | 原因 |
|---|---|
| 建構子 | 初始化邊界 |
| `draw()` | 選取/hover 時畫外框前先更新 |
| `containsPoint()` | hit testing 前確保邊界正確 |
| `getBounds()` | 回傳前確保正確 |
| `move()` | 子物件移動後邊界跟著變 |

---

## Ungroup 流程

### 觸發路徑

```
使用者點 Edit > Ungroup
→ EditController.onUngroupRequested()
→ canvasPanel.ungroupSelectedObject()
→ CanvasModel.ungroupSelectedObject()
```

### CanvasModel.ungroupSelectedObject()

```java
public boolean ungroupSelectedObject() {
    List<Shape> selected = getSelectedShapes();
    // D.2：必須是唯一一個選取且是 CompositeObject
    if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject)) return false;
    CompositeObject composite = (CompositeObject) selected.get(0);
    shapes.remove(composite);                          // 移除 composite
    for (Shape child : composite.getChildren()) {
        child.setSelected(true);                       // 子物件變為選取狀態
        shapes.add(child);                             // 加回頂層
    }
    normalizeDepthByCurrentOrder();                    // 重新整理 depth
    return true;
}
```

### shapes 列表的變化

```
Ungroup 前：shapes = [RectC, Composite]
                            └── children: [RectA, OvalB]

Ungroup 後：shapes = [RectC, RectA, OvalB]
            （三個都在頂層，RectA 和 OvalB 處於 selected 狀態）
```

### 只解構最外一層

規格說「解構最外一層」，實作正確對應：
```
Composite1
└── Composite2
    ├── RectA
    └── OvalB

Ungroup Composite1 後：
shapes 頂層 = [Composite2]
              └── children: [RectA, OvalB]（Composite2 還在）
```
`children` 直接加回頂層，不遞迴往下拆。

---

## 規格對應檢查

| 規格 | 實作 |
|---|---|
| D.1：只有 1 個物件時 Group 不動作 | `selected.size() < 2 → return false` |
| D.2：大於 2 個物件時 Ungroup 不動作 | `selected.size() != 1 → return false` |
| D.2：非 Composite 時 Ungroup 不動作 | `!(selected.get(0) instanceof CompositeObject) → return false` |
| Composite 可包 Composite | `clearInteractionStateRecursively` 遞迴處理，`refreshBounds` 呼叫子物件的 `getBounds`（也會遞迴） |
| Composite 的範圍 = 最小包圍矩形 | `Rectangle.union()` 依序合併所有子物件邊界 |
