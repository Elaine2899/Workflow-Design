# UML Class Diagram 線條與箭頭說明

## 速查表

| 關係 | Mermaid 語法 | 線條 | 箭頭 | 記憶法 |
|---|---|---|---|---|
| 繼承 Inheritance / Generalization | `<\|--` | 實線 | 空心三角 | 是一種（is-a） |
| 實作 Realization | `<\|..` | 虛線 | 空心三角 | 承諾介面 |
| 組合 Composition | `*--` | 實線 | 實心菱形 | 生死與共 |
| 聚合 Aggregation | `o--` | 實線 | 空心菱形 | 借來的 |
| 關聯 Association | `-->` | 實線 | 開放箭頭 | 長期持有 |
| 依賴 Dependency | `..>` | 虛線 | 開放箭頭 | 短暫使用 |

---

## 各關係詳細說明

### 1. 繼承 Inheritance / Generalization `<|--`

```
Shape <|-- RectShape
```

- **意思**：RectShape 繼承 Shape（is-a 關係）
- **生命週期**：子類存在時父類不一定存在（各自獨立）
- **程式碼對應**：`class RectShape extends Shape`
- **方向**：箭頭指向父類

---

### 2. 實作 Realization `<|..`

```
ModeStrategy <|.. SelectMode
```

- **意思**：SelectMode 實作 ModeStrategy 介面
- **生命週期**：各自獨立
- **程式碼對應**：`class SelectMode implements ModeStrategy`
- **方向**：箭頭指向介面
- **與繼承的差別**：線條是虛線（因為介面沒有實體，是「契約」不是「實體」）

---

### 3. 組合 Composition `*--`

```
CanvasPanel *-- CanvasModel
```

- **意思**：CanvasPanel 擁有 CanvasModel，CanvasPanel 消滅時 CanvasModel 也跟著消滅
- **生命週期**：整體（CanvasPanel）掌控部分（CanvasModel）的生死
- **程式碼對應**：`private final CanvasModel model = new CanvasModel();`（在欄位宣告時直接 new）
- **方向**：實心菱形在「擁有者」那端
- **記憶法**：菱形填滿 = 完全擁有，不能獨立存在

---

### 4. 聚合 Aggregation `o--`

```
CanvasModel o-- Shape
```

- **意思**：CanvasModel 管理 Shape，但 Shape 可以獨立存在（外部傳進來的）
- **生命週期**：整體消滅，部分不一定跟著消滅
- **程式碼對應**：`shapes.add(shape)`（shape 是外部建立後加入的）
- **方向**：空心菱形在「容器」那端
- **記憶法**：菱形空心 = 借來管理，不是我生的

---

### 5. 關聯 Association `-->`

```
ConnectionLink --> Port
```

- **意思**：ConnectionLink 長期持有 Port 的參考
- **生命週期**：不管生死，只是「有一個參考」
- **程式碼對應**：`private Port startPort;`（欄位持有，但不負責 new 也不負責消滅）
- **方向**：箭頭指向被持有的那方
- **記憶法**：實線 = 長期關係（欄位層級）

---

### 6. 依賴 Dependency `..>`

```
SelectMode ..> CanvasPanel
```

- **意思**：SelectMode 短暫使用 CanvasPanel（方法參數、區域變數）
- **生命週期**：使用完就結束，沒有欄位持有
- **程式碼對應**：constructor 傳入後存為欄位，或只在方法內用
- **方向**：箭頭指向被使用的那方
- **記憶法**：虛線 = 短暫/弱關係

---

## 組合 vs 聚合 vs 關聯 的分界

這三個最容易混淆，判斷方式：

| 問題 | 組合 | 聚合 | 關聯 |
|---|---|---|---|
| 是在自己的欄位 new 出來的？ | 是 | 否（外部傳入） | 否 |
| 消滅時會一起消滅嗎？ | 是 | 不一定 | 不一定 |
| 有欄位持有參考？ | 是 | 是 | 是 |

> 實務上組合/聚合的界線模糊，很多人統一用關聯（`-->`）表示「欄位持有」，用依賴（`..>`）表示「方法內使用」，這樣也可以接受。

---

## 本專案的實際對應

| Mermaid | 對應程式碼 |
|---|---|
| `Shape <\|-- RectShape` | `class RectShape extends Shape` |
| `ModeStrategy <\|.. SelectMode` | `class SelectMode implements ModeStrategy` |
| `CanvasPanel *-- CanvasModel` | `private final CanvasModel model = new CanvasModel()` |
| `CanvasPanel *-- ModeManager` | `private final ModeManager modeManager` |
| `CompositeObject o-- Shape` | `private List<Shape> children`（children 是外部 shape） |
| `SelectMode ..> CanvasPanel` | constructor 傳入，存為欄位使用 |
| `ConnectionLink --> Port` | `private Port startPort, endPort` |
