# OOP 期末專案改善清單

## 評分標準 5：專案熟悉程度（Demo 準備）

### 設計決策（能說出「為什麼這樣設計」）
- [ ] **為什麼用 Strategy Pattern？**
  - 每個模式的滑鼠事件行為完全不同，Strategy 讓每個模式獨立封裝，新增模式不需改動 ModeManager 或 CanvasPanel
- [ ] **為什麼抽出 CanvasModel？**
  - CanvasPanel 原本是 God Class，同時負責資料與 UI；抽出後 CanvasPanel 只管繪製與事件轉發，符合 SRP
- [ ] **為什麼用 Mode enum 取代 String？**
  - 消除魔術字串，讓 IDE 可以做型別檢查，switch/map 的 key 不會打錯
- [ ] **為什麼 isBasicObject 放在 Shape 上？**
  - 原本在 CanvasModel 用 instanceof 判斷違反 OCP，物件自己回答自己是否是基本物件符合多型
- [ ] **為什麼 SelectState 用 enum 而不是四個 boolean？**
  - 四個 boolean 本質上互斥，enum 讓非法狀態（例如同時 dragging + resizing）在型別層面就不存在

### 實作細節（能解釋每個方法做什麼）
- [ ] **Port 的座標如何計算？**
  - `getX() = owner.getX() + round(owner.getWidth() * relativeX)`，形狀移動或縮放後 Port 自動跟隨
- [ ] **depth 如何管理？**
  - depth 值越小越在上層；`bringToFront` 把目標移到排序首位再重新編號 0, 1, 2...
- [ ] **resizeToPort 的固定點邏輯**
  - 拖曳右下角時，fixedX = 左邊界（不動）；拖曳左上角時，fixedX = 右邊界（不動）；`relativeX == 0` 代表左邊 port，固定點是右邊
- [ ] **beginTemporaryObjectCreate / finishObjectCreateAttempt 的流程**
  - 按 Rect/Oval 時儲存當前模式，畫完後 finishObjectCreateAttempt 自動還原，對應規格 Use Case A step 6
- [ ] **CompositeObject.refreshBounds 何時觸發？**
  - draw、containsPoint、getBounds、move 都會呼叫，確保 children 移動後 bounds 永遠正確
- [ ] **框選為何用 isGroupableObject 而非 isBasicObject？**
  - 規格 Use Case C Case 2 允許框選，Composite 物件也應該可以被框選進去

### 功能驗收（Demo 前跑一遍）
- [ ] 建立 Rect / Oval（拖曳，模式自動還原）
- [ ] 建立三種 Link（連不同物件、連同一物件不成立）
- [ ] 點選 / 框選物件（hover 顯示 port、框選含 composite）
- [ ] Group（≥2 個）/ Ungroup（唯一 composite）
- [ ] 移動（含 composite）
- [ ] Resize（8 個 port、反向拖曳、最小尺寸 20px）
- [ ] Label（改名稱 + 改顏色，Cancel 不生效）

---

## 評分標準 3：程式碼可讀性

### 低優先
- [x] **清理「說廢話」的注釋**
  - 移除只重複說明程式碼在做什麼的注釋，保留解釋「為什麼」的注釋
  - 範例（可刪除）：`// 覆寫父類的 draw 方法，先繪製子形狀，再根據選中或懸停狀態繪製邊界框`
  - 範例（應保留）：`// 子形狀全部繪製完後才畫邊框，確保邊框不被子形狀蓋住`

- [x] **`resizeToPort` 加幾何說明**
  - 固定點（fixedX/fixedY）的幾何概念不直觀，加一行說明其含義（已有 `// fixedX 是拖曳時保持不動的那條邊`）


## 評分標準 2：物件導向程度

### 高優先
- [x] **`CanvasPanel` 抽出 `CanvasModel`**
  - `CanvasPanel` 目前是 God Class（500+ 行、30 個方法），同時負責 shape 管理、link 管理、depth 排序、hit testing、port 吸附、選取邏輯、group/ungroup、繪製協調
  - 抽出純資料層 `CanvasModel`，讓 `CanvasPanel` 只負責繪製與事件轉發

- [x] **模式名稱改為 Enum**
  - 目前 `"select"`、`"rect"`、`"oval"` 等字串散落在 `ModeManager`、`MainFrame`、`CanvasPanel` 各處，易打錯且 IDE 無法檢查
  - 改為 `public enum Mode { SELECT, RECT, OVAL, ASSOCIATION, GENERALIZATION, COMPOSITION }`

### 中優先
- [x] **`isBasicObject` 移到 `Shape` 抽象方法**
  - 目前 `CanvasPanel` 用 `instanceof RectShape || instanceof OvalShape` 判斷，違反 Open/Closed Principle
  - 在 `Shape` 加 `public abstract boolean isBasicObject()`，各子類自行回答

- [x] **`SelectMode` 的四個 boolean 改為 Enum**
  - `pendingAreaSelect`、`areaSelecting`、`draggingObject`、`resizingObject` 互斥但無型別保護
  - 改為 `private enum SelectState { IDLE, PENDING_AREA, AREA_SELECTING, DRAGGING, RESIZING }`

---