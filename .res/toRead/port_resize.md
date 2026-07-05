# Port + Resize 幾何計算

## Port 的座標系統

每個 Port 不儲存絕對座標，只儲存**相對比例**：

```java
private final double relativeX;  // 0.0 = 左邊  0.5 = 中間  1.0 = 右邊
private final double relativeY;  // 0.0 = 上邊  0.5 = 中間  1.0 = 下邊
```

絕對座標動態計算：

```java
getX() = owner.getX() + (int) Math.round(owner.getWidth()  * relativeX)
getY() = owner.getY() + (int) Math.round(owner.getHeight() * relativeY)
```

形狀移動或縮放後，Port 自動跟隨，`ConnectionLink` 也就自動更新，不需額外通知。

---

## RectShape 的 8 個 Port 位置

```
(0.0,0.0)──(0.5,0.0)──(1.0,0.0)
    │                      │
(0.0,0.5)              (1.0,0.5)
    │                      │
(0.0,1.0)──(0.5,1.0)──(1.0,1.0)
```

## OvalShape 的 4 個 Port 位置

```
        (0.5,0.0)
           │
(0.0,0.5)─ ─ ─(1.0,0.5)
           │
        (0.5,1.0)
```

---

## Resize 的核心概念：固定邊

拖曳某個 Port 時，**對面的那條邊保持不動**，稱為「固定邊」：

```
拖曳左邊 port (rx=0.0)  → 固定邊是右邊
拖曳右邊 port (rx=1.0)  → 固定邊是左邊
拖曳上邊 port (ry=0.0)  → 固定邊是下邊
拖曳下邊 port (ry=1.0)  → 固定邊是上邊
拖曳中間 port (rx=0.5)  → 水平方向不變（只有高度變）
```

---

## resizeToPort 程式碼解析

```java
private void resizeToPort(int mouseX, int mouseY) {
    double rx = activeResizePort.getRelativeX();
    double ry = activeResizePort.getRelativeY();

    int left   = resizeStartBounds.x;
    int right  = resizeStartBounds.x + resizeStartBounds.width;
    int top    = resizeStartBounds.y;
    int bottom = resizeStartBounds.y + resizeStartBounds.height;
    ...
}
```

`resizeStartBounds` 是**按下當下**的邊界，每次都從這裡重算，不累加誤差。

### 水平方向計算

```java
if (rx == 0.0 || rx == 1.0) {
    // fixedX 是拖曳時保持不動的那條邊
    int fixedX = (rx == 0.0) ? right : left;
    int width  = Math.max(Math.abs(mouseX - fixedX), MIN_SIZE);
    newWidth   = width;
    newLeft    = (mouseX >= fixedX) ? fixedX : fixedX - width;
}
```

拆解兩個情況：

**情況 A：拖左邊 port（rx=0.0），正常向左拖**
```
fixedX = right（右邊不動）
mouseX < fixedX

newWidth = right - mouseX
newLeft  = fixedX - width = right - (right - mouseX) = mouseX

結果：左邊跟著滑鼠，右邊不動 ✓
```

**情況 B：拖左邊 port（rx=0.0），越過右邊（反向拖）**
```
fixedX = right
mouseX > fixedX（滑鼠越過了右邊界）

newWidth = mouseX - right
newLeft  = fixedX = right（原右邊變成新左邊）

結果：形狀從原右邊延伸到滑鼠位置，規格 Alt F.2 ✓
```

**情況 C：拖右邊 port（rx=1.0），正常向右拖**
```
fixedX = left（左邊不動）
mouseX > fixedX

newWidth = mouseX - left
newLeft  = fixedX = left（左邊不動）

結果：右邊跟著滑鼠，左邊不動 ✓
```

垂直方向（ry）邏輯完全相同。

### 中間 port 不改變對應軸

```java
// rx == 0.5 時，if (rx == 0.0 || rx == 1.0) 不成立
// 水平方向的 newWidth / newLeft 維持原值，只有高度變
```

例如拖上方中間 port（rx=0.5, ry=0.0）：只改高度，寬度不變。

### 最小尺寸保護（Alt F.3）

```java
int width = Math.max(Math.abs(mouseX - fixedX), MIN_SIZE);
```

`MIN_SIZE = 20`，無論怎麼拖，寬度和高度都不會小於 20px。

---

## 完整範例：拖右下角（rx=1.0, ry=1.0）

初始邊界：left=100, top=100, right=200, bottom=200

滑鼠拖到（250, 180）：

```
水平：
  fixedX = left = 100
  width  = |250 - 100| = 150
  newLeft = 100（mouseX >= fixedX）
  newWidth = 150

垂直：
  fixedY = top = 100
  height = |180 - 100| = 80
  newTop  = 100（mouseY >= fixedY）
  newHeight = 80

結果：setBounds(100, 100, 150, 80)
```

---

## 為什麼不用 resize(dw, dh)？

`Shape` 曾有 `resize(dw, dh)` 方法（現已刪除），它只能做：
```java
width += dw;
height += dh;
```

這無法處理「拖左邊或上邊時 x/y 也要跟著變」的情況。
`resizeToPort` 用 `setBounds` 一次設定四個值，才能正確處理所有 8 個 port 方向。
