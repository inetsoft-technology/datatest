# vsscript 测试升级方案

> 日期：2026-11-13　模块：`vsscript`　分支：`vsscript-state-assert-spike`
> 依据：`vsscript.md`（缺口评估）+ 三轮原型/探路。**已确认**＝读过源码或在本机跑通；**未验证**＝没查或没跑。
> **接手请先读 `vsscript-state-guide.md`**（怎么跑、代码结构、写用例的配方、已踩的坑）。
> 实施方式：由 AI 按批次推进，每批产出一个可运行的 spec，结果写回本文。不估人天，只标相对大小（小/中/大）。

## 1. 结论

vsscript 现在只有"整页 PNG 像素比对"一条验证路径，而脚本的效果本质是**状态**不是像素。原型证明：不启动服务、不导出 PNG，就能直接读脚本效果、触发点击、录到提示消息、读到 Chart 的 `EGraph` 和 Crosstab 的数据、解析 PDF/Excel 导出。

三条原则：
1. **与单测分工，不重复。** 产品仓库已有单测专门覆盖单个脚本类（`report/script/viewsheet/` 48 个文件 369 个用例，`util/script/Calc*Test` 约 249 个用例）。datatest 只测单测做不到的：**真实 vs 资产 + 完整运行时**里的脚本行为。
2. **PNG 只留给渲染。** 需要像素才能验证的用例放单独目录，不再承担状态验证。
3. **旧用例不急着删。** 新用例稳定并证明"能失败"之后再处理（§6）。

## 2. 分工：单测 vs datatest

| | 产品单测（另有专门覆盖与升级） | datatest vsscript（本方案） |
|---|---|---|
| 对象 | 单个 `*VSAScriptable` 类、单个函数 | 真实 vs 资产里的脚本，整个 viewsheet 运行时 |
| 典型内容 | 属性 get/set、取值解析、全局函数边界（`CALC.*`、`union`、`formatNumber`…） | 脚本是否报错、事件端到端、触发顺序、跨组件、真实数据读回、导出一致性、渲染 |
| 本方案处理 | **转交，不做**：逐属性往返、全局函数边界（评估文档 G4 的属性部分、G7） | D1–D9 |

> 说明：原型里的"属性自环往返"（枚举 `getMemberKeys()` 后原样写回）与单测方向相同，**降级为探测工具**——只用来盘点"资产里脚本用到了、却没有被测"的属性，不扩成用例集。

## 3. 方向（datatest 做什么）

| # | 方向 | 解决 | 大小 | 依据 |
|---|---|---|---|---|
| D1 | **脚本健康检查**：zip 里 130 个 vs 的所有脚本，直接 `scope.execute` 并断言不抛 `ScriptException` | 资产里已有脚本在报错却无人知道 | 小 | §5.1 |
| D2 | **异常契约**：把"刷新路径吞异常、点击路径抛异常、报错前的效果保留"固定成用例 | G2；PNG 和单测都看不到 | 小 | §5.1 |
| D3 | **事件端到端**：`onClick`、`confirm`、`onSubmit`、嵌套 vs 的 `onClick`、`parameter` | G6；评估文档里零用例 | 中 | §5.2 |
| D4 | **真实数据读回**：Crosstab 的 `data/table`、Chart 的 `EGraph/scale/dataset`、格式对象；把现有 PNG 用例改成状态断言版 | G5；现有用例"画进 Text 再比图" | 中 | §5.3 |
| D5 | **触发顺序与跨组件**：`ONINIT`/`ONREFRESH`/组件脚本的先后，Text 读取 Crosstab 等 | 触发时机类 bug（67 条） | 中 | 未验证 |
| D6 | **差分**：脚本设置 vs 资产静态属性，两者读回应一致 | 不依赖基线的正确性 | 小 | §5.4 |
| D7 | **导出一致性**：同一 vs 多格式导出（PDF 文字、Excel 单元格），脚本效果应进入导出 | Viewer 与导出的差异（12 条 bug） | 中 | §5.6 |
| D8 | **数据函数端到端**：`appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`（单测已有 `ViewsheetScopeTest` 14 个用例，这里只测经真实 vs 的路径） | 写回类 bug | 中 | 入口未验证 |
| D9 | **渲染用例（PNG）**：只保留图例位置、颜色、布局这类像素可见的，放 `render/` | 渲染回归 | 小（迁移） | §6 |

## 4. 优先级

权重＝评估文档 §7 的 bug 数（798 条，按标题关键词分类，只是上限、不是精确值）。

| 顺序 | 方向 | 权重 | 理由 |
|---|---|---|---|
| **1** | **D1 + D2** | 引擎/异常 86 | 权重高；只有真实资产才能测，单测做不了；成本小；做完立即得到"现有脚本有多少在静默报错"的数字 |
| 2 | D4 | Chart 126、表 113、选择输入 97 | 权重最大，读入口已验证可行 |
| 3 | D3 | 触发 67 + 参数 30 | 点击入口已跑通；submit 需额外接线 |
| 4 | D6、D7 | 属性不生效 69 | 小到中，D7 需要 Excel 解析辅助 |
| 5 | D5、D8 | 11–67 | 入口未验证，先各做小探路 |
| 6 | D9 | 迁移 | 在 D1–D4 稳定后再做 |

## 5. 已验证事实

### 5.1 脚本异常（最重要）
| 触发方式 | 调用方能否发现 | 观察 |
|---|---|---|
| 组件脚本 / `ONINIT` / `ONREFRESH`（经刷新执行） | **不能** | 不抛异常，无 `MessageCommand`，vs 仍可用；报错语句之前的效果保留 |
| 直接 `scope.execute(script, 组件名)` | 能 | 抛 `ScriptException`，cause 含具体错误，如 `ReferenceError: undefinedFn is not defined (line 1)` |
| 点击（`OnClickService.onClick`） | 能 | 无 try/catch，异常抛给调用方 |

- 源码：`ViewsheetSandbox.java:1685` 对 `ConfirmException | ScriptException` 直接 `// ignore`。
- 测试环境没有 SLF4J 绑定（`Failed to load class "org.slf4j.impl.StaticLoggerBinder"`），日志被丢弃；直接引用 logback 类会因版本不匹配 `ClassNotFoundException`。加绑定能否让日志可见：未验证。
- 含义：现有 PNG 用例遇到脚本报错，基线若恰好是那种状态也会"通过"。
- 产品仓库另有 `ScriptStateLint`（词法检查，带单测），与 D1 不同（D1 是真实执行）；是否可复用：未查。

### 5.2 点击与消息
- `JavaScriptEngine.alert` → `CoreTool.addUserMessage` → 点击结束后 `MessageCommand.fromUserMessage` 发出；`confirm` 发 `MessageCommand(Type.CONFIRM)`。
- 测试里手动 `new OnClickService(coreLifecycleService, null, viewsheetService)`（它和 `VSInputService` 都不是测试 Spring 上下文里的 Bean；`VSInputService` 只用于表单提交）。`MessageTestUtils` 的 no-op `CommandDispatcher` 可覆盖 `sendCommand` 改为录制。
- 原型：点击触发 `alert("clicked","warning")`，录到 1 条 WARNING，文本 `clicked`。**未验证：** `onSubmit`（需接 `VSInputService`）。

### 5.3 读回能力
- **Crosstab**（`Table_Crosstab1`）：`data.length`=30、`data[1][2]`=2、`table[1][2]`=3；脚本拼进 Text 读回 `"3, 2, 30"`；`cellFormat[2][2]` → `ExtendedDecimalFormat[#,###.00]`；`colFormat[0]` → `ExtendedDateFormat[yyyy/MM/dd]`；`highlighted` 返回 `TableHighlightedArray`（内部未探）。
- **Chart**：`bindingInfo.xFields/yFields` 读回 `[Region]`、`[Paid]`，`Chart1.colorField`＝`State`；`Chart1.graph` 是 `EGraph`，`getElementCount()`=1，`getHint(HINT_ALPHA)`=0.5，`getScale("Quantity").getMax()`=400，`dataset.getRowCount()`=3。
- 读写入口不对称：`bindingInfo` 只有 setter，读颜色字段要用 `Chart1.colorField`（`ChartVSAScriptable.java:257`）。`setPresenter` 无读回方式。数值读回是 `Double`。
- 脚本引擎是 Graal；属性枚举用 `getVSAScriptable(name).getMemberKeys()`。

### 5.4 差分
脚本路径（`autoSize = true`）与 Java 静态路径（`setAutoSizeValue(true)`）读回一致。**静态路径必须用 `*Value` setter**，`setAutoSize` 是运行时值，刷新后被重置。局限：还没用 zip 资产里已有的静态属性做对照。

### 5.5 属性枚举（探测用，不交付）
`getMemberKeys()` 枚举：Text1 36、Crosstab1 57、Chart1 146 个成员；其中标量 16 / 22 / 57 个，原样写回全部一致（Crosstab 的只读成员 `data.size` 写入报错）。非标量或 null 共 143 个未覆盖。

### 5.6 导出
- 测试里**没有 Apache POI**（pom 无依赖，本机 Maven 仓库也没有）；xlsx 用 JDK `ZipFile` + 正则可读。
- PDF：PDFBox 提取文字，脚本改的 Text 值可见（通过）。
- Excel：`Text` 组件导出为图片（`xl/drawings` + `xl/media/*.png`），不能按单元格断言；Crosstab 是单元格（第一个 sheet 179 个单元格、59 条共享字符串）。导出包含 5 个 worksheet，原因未查；还没把单元格与脚本数据逐项对比。

## 6. 目录与旧用例

```
vsscript/src/test/groovy/inetsoft/test/vsscript/
├── cases/    现有 115 个 PNG 用例，暂不动
├── state/    新：D1–D8（已有原型）
└── render/   新：必须用 PNG 才能验证的用例（D9）
```
- **基线路径风险：** `VSScriptTest.initHome` 用类的包名推出 `expectData` 子目录。往 `render/` 迁移用例必须同步移动基线，否则找不到基线。**只读了代码，没实际跑过迁移。**
- **旧用例只有同时满足三条才删：**
  1. 有对应的状态断言版，覆盖同样的属性（用生成的覆盖清单核对）。
  2. **新用例能失败：** 在产品里故意改坏一个 setter/映射，确认新用例变红。原型 30 个用例一次全过，"能不能抓 bug"目前没有证据。
  3. 在 CI 稳定运行一段时间（周期待定）。
- 渲染可见的用例不删，迁到 `render/`。现有 PNG 用例本身并不稳（本机 114/115 失败，CI 里 `continue-on-error: true`），所以删的是"从未真正当过门禁"的用例。

## 7. 风险与待确认

- 直接执行脚本与刷新时执行的上下文可能不同（如 `onClick` 会设置 `setUseDValue`），D1 可能出假失败，需在原型里先量。
- 读写入口不对称（§5.3），非标量属性每个都要先查入口。
- bug 权重是关键词分类的上限，只能当线索。
- **待开发确认：** 刷新路径吞掉脚本异常是否产品有意设计；`pop*`、`applyChanges`、`addAction` 的预期行为。
- **待决定：** CI 里是否收紧 `continue-on-error`；基线是否人工核对过；稳定运行多久后开始处理旧用例。

## 8. 下一步

先做 **D1 + D2**（`state/` 下，一个 spec）：
1. 取出 zip 里全部 vs 的所有脚本（组件脚本、`ONINIT`、`ONREFRESH`），逐段直接执行，统计抛 `ScriptException` 的数量与分布。
2. 固定 D2 的三条行为。
3. 结果（数字、误报情况、与刷新执行的差异）写回本节。

## 9. 缺口对应与验收标准

**评估文档缺口 → 本方案：**

| 缺口 | 去向 |
|---|---|
| G1 清单与基线脱节（`LengendsProperty2`、11 张陈旧基线、`Viewsheet_Spec` 无参比对） | 机制修复，不在 D1–D9 内；零成本，随时可做 |
| G2 只有 PNG、脚本报错被吞 | D1、D2 |
| G3 CI `continue-on-error` | 待决定（§7）；新用例落地后可对 `state/` 单独收紧 |
| G4 零命中属性、无用例组件 | 属性部分转交单测；组件类型的接线用 D4/D9 各补一个代表用例 |
| G5 Chart 占 57%，其他组件偏薄 | D4 |
| G6 事件脚本与数据函数 | D3、D8 |
| G7 全局函数 | 转交单测（产品已有 `Calc*Test` 约 249 个用例，缺的是边界深度） |
| G8 未引用的 vs | 随 D1 的批量扫描一并统计 |

**验收标准：**
- `state/` 用例在本机与 CI 结果一致，不受渲染差异影响。
- 每个新用例都经过"故意改坏产品后变红"的验证（§6 第 2 条）。
- D1 产出"zip 内脚本抛 `ScriptException` 的数量与分布"，并有已知误报清单。
- 脚本报错、点击触发 warning 至少各有一个稳定用例。
- 旧用例的处理有逐个对应的记录，没有无记录删除。

## 附：原型代码

`vsscript/src/test/groovy/inetsoft/test/vsscript/state/`：`ScriptStateBase`（开 vs、设脚本、读值、录命令刷新）、`RecordingDispatcher`、`ScriptStateSpike_Spec`（10 个）、`ScriptStateChartCrosstab_Spec`（10 个）、`ScriptStateProbe_Spec`（10 个探路，只打印不断言）。运行：`./mvnw test -pl vsscript -Dtest='ScriptState*_Spec'`（先 `./mvnw package -pl commons -DskipTests`），本机 30 个全部通过。
