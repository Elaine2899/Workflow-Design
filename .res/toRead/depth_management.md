# Depth 管理方式

## 什麼是 depth？

每個 Shape 有一個 `depth` 值（0–99）。  
**值越小，越在上層**：繪製時優先畫、滑鼠事件優先攔截。

規格要求：「最後選取的物件應該被繪製於最上層」→ 選取時把該物件的 depth 調為最小。

---

## depth 的儲存方式

`depth` 存在每個 `Shape` 物件本身：

```java
// Shape.java
protected int depth;

public void setDepth(int depth) {
    if (depth < 0)       this.depth = 0;
    else if (depth > 99) this.depth = 99;
    else                 this.depth = depth;
}
```

`CanvasModel` 負責統一管理所有 shape 的 depth 順序。

---

## bringToFront：置頂

選取物件時呼叫，讓被選取的物件排到最上層：

```java
public void bringToFront(Shape target) {
    List<Shape> ordered = getShapesByDepthAscending(); // depth 小→大排序
    ordered.remove(target);    // 把 target 從排序中移除
    ordered.add(0, target);    // 插到最前面（最小 depth）
    applyDepthOrder(ordered);  // 重新編號 0, 1, 2...
}
```

### applyDepthOrder

```java
private void applyDepthOrder(List<Shape> ordered) {
    int depth = 0;
    for (Shape s : ordered) {
        s.setDepth(depth);
        if (depth < 99) depth++;
    }
}
```

把排序後的 list 從 0 開始依序編號。  
第一個 shape 得到 depth=0（最上層），後面依序 1, 2, 3...

---

## 繪製順序

```java
// CanvasPanel.paintComponent()
for (Shape shape : model.getShapesByDepthDescending()) {
    shape.draw(g);
    shape.drawPorts(g);
}
```

用 **depth 大→小** 的順序繪製：depth 大的先畫（在底層），depth 小的後畫（蓋在上面）。

```
depth=2 → 先畫（底層）
depth=1 → 再畫
depth=0 → 最後畫（最上層，蓋住其他人）
```

---

## 滑鼠事件的攔截順序

```java
// CanvasModel.findTopShapeAt()
public Shape findTopShapeAt(int mx, int my) {
    for (Shape s : getShapesByDepthAscending()) { // depth 小→大
        if (s.containsPoint(mx, my)) return s;    // 找到就馬上回傳
    }
    return null;
}
```

用 **depth 小→大** 的順序掃描，第一個包含滑鼠座標的 shape 就是最上層的那個。  
確保最上層物件優先接收滑鼠事件。

---

## 兩個排序方向的用途

| 方法 | 順序 | 用途 |
|---|---|---|
| `getShapesByDepthDescending()` | depth 大→小 | 繪製（底層先畫） |
| `getShapesByDepthAscending()` | depth 小→大 | hit testing（最上層優先） |

---

## Ungroup 後的 normalizeDepthByCurrentOrder

Ungroup 時，children 加回頂層 shapes list，但 depth 值可能混亂（原本是子物件，沒有獨立的 depth 排序）。  
呼叫 `normalizeDepthByCurrentOrder` 整理：

```java
private void normalizeDepthByCurrentOrder() {
    applyDepthOrder(getShapesByDepthAscending());
}
```

照目前的順序重新從 0 編號，讓所有 shape 的 depth 值緊密連續。

---

## 完整範例

初始狀態：shapes = [A(depth=0), B(depth=1), C(depth=2)]

使用者點選 C：
```
bringToFront(C)

ordered = [A, B, C]（depth 小→大）
remove C → [A, B]
add(0, C) → [C, A, B]
applyDepthOrder → C=0, A=1, B=2

結果：C 在最上層 ✓
```

繪製順序（depth 大→小）：B(2) → A(1) → C(0)，C 最後畫蓋住其他人。
