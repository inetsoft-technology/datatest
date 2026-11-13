# vsscript 测试升级可行性方案

> 日期：2026-11-13　模块：`vsscript`
> 依据：`docs/test-gap-assessment/vsscript.md`（缺口评估，下称"评估文档"）及后续讨论
> 状态：方案阶段，**未落实任何代码**。文中"已确认"指本次读过源码，"未验证"指尚未查证。

## 1. 背景与问题

vsscript 的特性是"在 viewsheet 组件上执行用户脚本"。现状（来自评估文档）：

- 11 个 spec、115 个用例、246 张基线 PNG；**验证只有整页 PNG 像素比对一条路**，没有数据层断言。
- 本机 114/115 个用例因渲染差异失败（中位数 0.95%，阈值 0.001%），无法判定真假；CI 里该步骤 `continue-on-error: true`。
- 脚本报错是否进 PNG：未验证；事件脚本（onClick、onSubmit、confirm）、数据函数（appendRow、setCellValue…）、全局函数（CALC.*、union…）零用例。
- Redmine 近两年的脚本类 bug 集中在：全局函数边界（31 条里 27 条是 2025 年起）、引擎/异常（86 条）、触发时机（67 条）——**PNG 都看不到**。

**核心判断：** 脚本的效果本质是"组件状态/数据的变化"，不是像素。验证手段选错了，不是用例数量不够。

## 2. 目标与非目标

**目标**
1. 让 vsscript 的判定不再依赖渲染环境（本机和 CI 结果一致、可信）。
2. 覆盖 PNG 够不到的四类：脚本报错、事件触发、数据/全局函数、导出差异。
3. 用低成本方式补齐 43 个零命中属性和 5 个无用例组件类型。

**非目标**
- 不删除现有 PNG 用例，只收窄其职责。
- 不做前端（Viewer）交互的全面自动化。
- 不在本方案内修产品 bug。

## 3. 方案总览

| # | 方案 | 解决什么 | 放在哪 | 成本 | 优先级 |
|---|---|---|---|---|---|
| A | 机制修复（G1） | 比对清单与基线脱节 | datatest vsscript | 极低 | P0 |
| B | 状态断言，不经渲染 | G2、G4、G5；脚本报错 | datatest vsscript（新 harness） | 中 | P0 |
| C | 属性往返测试（反射批量生成） | G4 的 43 个零命中属性 | datatest 或产品单测 | 中 | P1 |
| D | 差分测试（脚本设置 vs 静态设置） | 不依赖基线的正确性 | datatest vsscript | 中 | P2 |
| E | 全局函数与数据函数单测 | G6 数据函数、G7 | 产品 Java 单测 | 中 | P0 |
| F | 事件脚本 + 命令录制 | click、warning、confirm | datatest（服务端） | 中高 | P1 |
| G | 导出结构化断言 | 脚本对导出的影响、Viewer/导出差异 | datatest vsexport | 中 | P1 |
| H | 少量真浏览器冒烟 | 前端弹窗与点击 | Playwright | 高 | P3 |
| I | PNG 收窄 | 渲染可见的代表用例 | datatest vsscript | 低 | 随 A |

### A. 机制修复（评估文档 G1，零成本）
- `ChartProperties_Spec.groovy:268-270` 把 `LengendsProperty1` 写了两遍，应有一个改为 `LengendsProperty2`。
- 11 张无人引用的基线：确认后删除或补用例（其中 10 张是改名后的陈旧文件）。
- `Viewsheet_Spec` 的无参 `compareImage()` 只比"实际导出"，改成点名（先确认 3 个用例各导出哪些文件）。
- 本机无法判定基线，**需用 CI 基线核对**。

### B. 状态断言，不经渲染（核心方案）
- **做法：** 在 `VSScriptTest` 之外新增 harness：打开 `RuntimeViewsheet`，执行脚本，直接读 assembly 的 info / scriptable 属性值，与期望 JSON 比较；不调用 `exportVS`。vsothers 等模块的 `ViewsheetTest` 已有数据层断言写法，可复用。
- **效果：** 确定性强，不受渲染差异影响。脚本报错**不能**靠刷新后捕获（刷新路径会吞掉 `ScriptException`），要在测试里直接 `scope.execute` 脚本并断言异常，见 §10.1。
- **适用：** 组件属性 setter/getter、取值串解析、非 Chart 组件的脚本接线。
- **前置（已查，§10.1）：** 刷新路径里脚本异常被静默吞掉，无异常、无 `MessageCommand`、测试环境日志也不可见；直接执行会抛 `ScriptException`。

### C. 属性往返测试（反射生成）
- 从各 `*VSAScriptable` 的 `addProperty` / `addFunctionProperty` 声明（共 282 个）自动生成参数化用例：脚本 `comp.prop = v`，读回应等于 `v`（Spock `where:` 或 JUnit `@ParameterizedTest`）。
- 新增属性自动被拉入，避免手写遗漏。
- 注意：Chart 的 EGraph/GraphElement 脚本对象和 `bindingInfo` 不在这批类里（评估文档 §6），需另行处理；`pop*`、`applyChanges`、`addAction` 的合法取值需开发确认。

### D. 差分测试
- 同一属性，一份由脚本设置，一份在 vs 中静态设置，两者生成的 assembly info 应一致。
- 评估文档已发现 zip 资产里很多属性有静态版本（如 `combinedTooltip`、`dateComparisonEnabled`、`padding`、`tooltipVisible`、`popLocation`），可直接作为对照。
- 价值：不依赖"基线是否人工核对过"（评估文档里该点未查）。

### E. 全局函数与数据函数单测（产品 Java 单测）
- 全局函数：`CALC.*`、`inArray`、`split`、`rtrim`、`union`、`formatNumber`、`mapList` 等，参数化测试；期望值用 `java.time`、`BigDecimal` 等独立算出（空串、全空白、重复数、舍入模式、闰年）。
- 数据函数：`appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`、`addImage`、`createConnection`、`delayVisibility`、`isCancelled`，放在 `ViewsheetScope` 的 harness 里（产品已有 `ViewsheetScopeTest` 14 个用例，**是否有现成入口需确认**）。
- 产品 `report/script/viewsheet/` 已有 47 个测试类、369 个用例，只补评估文档 §4 列出的"名字匹配不到测试"的类（约 99 个用例，设计值）。
- 待决：全局函数是否归 vsscript 评估范围（更像脚本引擎）。

### F. 事件脚本 + 命令录制（click、warning、confirm）
**已确认：**
- 脚本的提示走服务端命令：`ViewsheetScope.java:289` `JavaScriptEngine.alert(msg, MessageCommand.Type.INFO)`。
- `confirmEvent` 在 `ViewsheetScope.java:524` 注册，同样走服务端。
- `commons` 已有 `MessageTestUtils.withMockMessageContext`，其 `CommandDispatcher` 当前是 no-op（`MessageTestUtils.java:159-180`）。

**做法：**
1. 把 no-op dispatcher 换成"记录所有 `sendCommand` 调用"的版本。
2. 用 `ActionEventsUtil` / 控制器发 click、submit 等事件（服务端入口，commons 已有用法）。
3. 断言两件事：**命令序列**（有无 WARNING/INFO、类型与文本、有无多余报错）和**组件状态**（click 之后属性是否已变，即方案 B）。

**可覆盖：** `onClick`、`onSubmit`、`resetSubmitForms`、`confirmEvent` 的确认/取消两条分支、嵌套 vs 的 `onClick`（bug 79699）、`appendRow`/`saveWorksheet` 后的写回结果。

**未验证（动手前必须先查）：**
- 脚本异常是否也经 `MessageCommand`，还是只写日志。
- click 事件在 `ActionEventsUtil` 里具体对应哪些方法。

### G. 导出结构化断言
- **Excel/CSV：** POI 或直接读 CSV，断言单元格值、格式、合并区域、隐藏行列。
- **PDF：** `ExportUtil` 已带 PDFBox 2.0.22，提取文本及位置，断言文字出现与否、是否被隐藏。
- **格式间对比：** 同一 vs 导出多种格式，断言该一致的一致、该不同的不同。针对 §7 中 12 条带 export/portal/viewer 的 bug（PNG 导出看不到这类差异）。
- **`exportFormat`：** 脚本按导出格式分支，该函数零命中，适合用"同 vs 多格式分别断言"。
- **位置：** 放 vsexport 模块，复用其导出基础设施，不在 vsscript 重建。

### H. 真浏览器冒烟（仅剩前端自身行为）
- 弹窗实际样式、点击命中区域、前端对 `MessageCommand` 的渲染。
- 用 Playwright 做少量用例，不铺开。

### I. PNG 收窄
- 仅保留"渲染确实可见"的代表用例（每个组件类型一个）。
- 基线统一在 CI 环境生成；阈值或比较方式（如感知比较）待评估。
- 补 ComboBox、Submit、Thermometer、Cylinder、SlidingScale 各 1 个代表用例（Thermometer/Cylinder/SlidingScale 在 zip 里没有资产，需新建 vs；Submit 可复用 `Manual/Form/Forms_Submit`、`Manual/OutPut/Submit_Action`）。

## 4. 缺口与方案对应

| 缺口（评估文档） | 方案 |
|---|---|
| G1 清单与基线脱节 | A |
| G2 只有 PNG、脚本报错被吞 | B、F |
| G3 CI `continue-on-error` | 先看 CI 实际通过情况；B/E 落地后可对新增部分单独收紧 |
| G4 43 个零命中属性、5 个无用例组件 | B、C、I |
| G5 Chart 占 57%，其余组件偏薄 | B、C |
| G6 事件脚本与数据函数零命中 | E（数据函数）、F（事件） |
| G7 全局函数零命中 | E |
| G8 68 个未引用 vs | 机制清理（A 同批确认） |
| 导出相关 bug（12 条） | G |

## 5. 实施顺序

1. **第 1 步（几乎无风险）：** A。
2. **第 2 步（并行）：**
   - 先查清两个未验证项：脚本异常处理路径、`ActionEventsUtil` 的 click 入口。
   - E：全局函数参数化单测（不依赖任何 harness 未知项）。
3. **第 3 步：** B 的最小原型。选 Thermometer/Cylinder 这类零命中组件，用状态断言 + 反射生成写一个不依赖 PNG 的 spec，跑通后再决定推广范围。
4. **第 4 步：** F 的最小用例：click 触发 `alert`，断言收到 WARNING。
5. **第 5 步：** C、D 推广；G 在 vsexport 补用例。
6. **第 6 步：** H、I 收尾。

每步完成后在评估文档里更新状态；第 3、4 步的原型是继续推进的闸门。

## 6. 风险与待确认

**风险**
- 状态断言依赖产品内部对象（assembly info），产品重构会导致测试脆弱；用 scriptable 的公开属性读取可降低风险。
- 反射生成用例可能产生"取值非法"的假失败，需维护取值表。
- `CommandDispatcher` 录制依赖 Spring 上下文，加载成本需在原型里测。
- 评估文档中的统计是静态文本命中，零命中只代表字符串没出现，不代表特性没被覆盖。

**待开发确认**
- ~~脚本抛异常时：弹消息、记日志，还是中断导出？~~ 已查（§10.1）：刷新路径静默吞掉；是否为产品有意设计，需开发确认。
- `Input` 基类声明的属性是否被子类测试覆盖？
- `pop*`、`applyChanges`、`addAction` 的预期行为与合法取值。
- `ViewsheetScope` 数据函数是否有现成测试入口。

**待用户决定**
- G3：CI 是否收紧 `continue-on-error`。
- 基线是否人工核对过。
- 全局函数是否归 vsscript 评估范围。
- 新 harness 放 datatest 还是产品仓库（取决于 Spring 上下文重量）。

## 7. 验收标准

- 本机与 CI 对"状态断言"类用例结果一致，不再出现渲染噪声。
- 脚本报错、click 触发 warning 至少各有一个自动化用例。
- 43 个零命中属性被反射生成用例覆盖，或被明确标记为"需开发确认"。
- 全局函数（`CALC.*`、`union`、`formatNumber` 等）有独立期望值的参数化单测。
- PNG 用例清单与基线一一对应，无陈旧基线、无被漏比的文件。

## 8. 可行性探索结果（分支 `vsscript-state-assert-spike`，只读源码，未编译、未运行）

| 项 | 结论 | 依据 |
|---|---|---|
| 不经渲染读状态（方案 B） | 可行 | `VSScriptTest` 在 `commons/.../modules/` 里；它自己已经拿到 `RuntimeViewsheet` 和 `Viewsheet`，直接 `setScript` 再 `refreshViewsheet`，只有最后一步才 `exportVS`。去掉最后一步、改读 assembly 即可 |
| 点击事件入口（方案 F） | 可行，且比预想简单 | 产品 `OnClickService.onClick(vsId, name, x, y, submitEvent, linkUri, principal, dispatcher)` 是 Spring Bean，`CommandDispatcher` 作为参数传入，测试可直接调用并传自己的 dispatcher |
| 录制命令 | 可行 | `MessageTestUtils.createNoOpCommandDispatcher` 里 `sendCommand` 可覆盖，改成往 List 里记即可 |
| `alert` / `confirm` 的路径 | 已确认 | `JavaScriptEngine.alert` → `CoreTool.addUserMessage`；`onClick` 结束后 `Tool.getUserMessage()` → `MessageCommand.fromUserMessage` 发出；`confirm` 走 `Tool.getConfirmMessage()` → `MessageCommand(Type.CONFIRM)` |
| `onClick` 脚本异常 | 不会被吞 | `OnClickService.process0` 里 `scope.execute(...)` 没有 try/catch（仅 finally），异常会抛给调用方，测试里可直接断言 |

**还缺的东西：**
- `ActionEventsUtil` 和 `RuntimeViewsheetResource` 里没有 click 相关的封装，需要新增（`springBean(OnClickService)` 后直接调用）。
- `RuntimeViewsheetResource.springBean` 是 private，需要确认能否在新基类里拿到 `OnClickService`（可新增一个公开方法，不动原有行为）。
- 点击要求 assembly 是 `ClickableOutputVSAssemblyInfo` / `ClickableInputVSAssemblyInfo` 且 `isScriptEnabled()`（`OnClickService:200-208`），不满足时静默返回，用例要选对组件，并断言"确实触发了"。
- 非点击路径（`ONINIT`、`ONREFRESH`、组件属性脚本）里脚本异常的处理仍未验证。
- 整个探索没有编译或运行过任何代码，下一步需用最小用例实际跑通。

## 9. 原型运行结果（2026-11-13，本机，分支 `vsscript-state-assert-spike`）

运行：`./mvnw test -pl vsscript -Dtest='ScriptStateSpike_Spec,ScriptStateChartCrosstab_Spec'`，两个 spec 共 20 个用例，全部通过，每个 spec 约 15 秒。不启动任何服务，不导出 PNG 比对。代码在 `vsscript/src/test/groovy/inetsoft/test/vsscript/state/`（`ScriptStateBase`、`RecordingDispatcher`、两个 spec），没有改 commons 里的任何原有文件。

### 9.1 各类方案的验证结果

| 方案 | 用例 | 结果 |
|---|---|---|
| B 状态断言 | `Text1` 脚本 `value="hello"`，读回 | 通过 |
| C 属性往返 | `Text1.autoSize/wrapping` 各 true/false；`Crosstab2.titleVisible/wrapping/tableStyle`；`Chart1.titleVisible/tooltipVisible` | 通过（共 11 个），true/false 能区分 |
| D 差分 | 脚本读到的 `autoSize` 与 assembly info 对比 | 通过，但较弱（两者是同一对象），真正的"脚本设置 vs 静态设置"差分未做 |
| E 全局函数 | `formatNumber(1234.567,"#,##0.00","")` 对比 `DecimalFormat` | 通过 |
| F 点击 + alert | `OnClickService.onClick` 触发 `alert("clicked","warning")`，录到 1 条 `MessageCommand`，类型 WARNING、文本 `clicked` | 通过 |
| G 导出 | 脚本改值后导出 PDF，PDFBox 提取文字包含该值 | 通过 |

### 9.2 Crosstab 与 Chart（复用现有 PNG 用例的资产）

- **Crosstab**（`Table_Crosstab1`）：`Crosstab1.data.length` 读到 30，`data[1][2]` 为 2，`table[1][2]` 为 3；用脚本把这三个值拼进 `Text1`，读回 `"3, 2, 30"`，与直接读取一致。现有 PNG 用例只能把它画进 Text 再比图，现在可以直接断言数值。
- **Chart**（`ChartBandingInfo`）：脚本设置 `bindingInfo.xFields/yFields/setColorField` 后，读回 `Chart1.bindingInfo.xFields = [Region]`、`yFields = [Paid]`、`Chart1.colorField = 'State'`，均为具体值，不是"非空"。
- **读取方式有坑：** `bindingInfo` 上只有 setter（`VSChartBindingScriptable`），读颜色字段要用 `Chart1.colorField`（`ChartVSAScriptable.java:257`，返回运行时字段名）。第一次按 `bindingInfo.colorField` 读得到 null，用例失败，属于原型的读法错误，不是产品 bug。后续批量生成用例时，每类属性要先确认"写入口"和"读入口"各是什么。

### 9.3 探索中发现的约束

- `OnClickService` 与 `VSInputService` 不是测试 Spring 上下文里的 Bean（上下文只带 `OpenViewsheetController` 等少数几个），点击用例里手动 `new OnClickService(coreLifecycleService, null, viewsheetService)`。`VSInputService` 传 null 只对普通点击成立，要测表单提交（`submitEvent`）需另外处理。
- 数值读回是 `Double`（如 `30.0`），断言时需注意与整数比较。

### 9.4 仍未验证

- 非点击路径（`ONINIT`、`ONREFRESH`、组件属性脚本）里脚本抛异常时产品的行为。
- 复杂组件的更多属性（Chart 的 EGraph/GraphElement 脚本对象、`bindingInfo` 的读入口）、Crosstab 的 `cellFormat` 等需要读取格式对象的属性。
- 真正的差分用例（脚本设置 vs 资产里静态设置）。
- Excel 导出，以及脚本里按导出格式分支（`exportFormat`）。
- 这些用例都是单次手工运行的结果，还没有放进 CI。

## 10. 第二轮探路结果（探路项 1–6，2026-11-13）

探路代码：`vsscript/src/test/groovy/inetsoft/test/vsscript/state/ScriptStateProbe_Spec.groovy`（只打印观察结果 `PROBE ...`，不做断言，10 个用例都能运行）。

### 10.1 脚本异常（探路 1）：最重要的发现

| 触发方式 | 调用方能否发现 | 观察 |
|---|---|---|
| 组件脚本 / `ONINIT` / `ONREFRESH` 经刷新执行 | **不能** | 不抛异常；刷新期间没有 `MessageCommand`；vs 仍可用；报错语句**之前**的效果保留（`value="changed"; value=undefinedFn()` 之后值是 `changed`，`ONREFRESH` 里的 `from-onload` 也保留） |
| 直接 `scope.execute(script, assemblyName)` | **能** | 抛 `ScriptException`，消息带中文提示，cause 里有具体错误：`ReferenceError: undefinedFn is not defined (line 1)`、`boom-direct (line 1)`；报错前的效果同样保留 |

- 源码印证：`ViewsheetSandbox.java:1685` 对 `ConfirmException | ScriptException` 直接 `// ignore`，所以刷新路径里脚本错误是被吞的。
- **日志也看不到：** 测试环境没有 SLF4J 绑定（控制台提示 `Failed to load class "org.slf4j.impl.StaticLoggerBinder"`），所有日志被丢弃；直接引用 logback 类还会因版本不匹配报 `ClassNotFoundException: org.slf4j.spi.LoggingEventAware`。能否通过加绑定让日志可见：未验证。
- **含义：** 现有 PNG 用例即使脚本报错也可能"通过"（基线若是那种状态）。方案 B 的可行做法是**测试里直接 `scope.execute` 每段脚本并断言不抛 `ScriptException`**；点击路径本来就会抛（见 §8）。
- **新想法（未验证）：** 对 zip 里 130 个 vs 的所有脚本批量做"直接执行、断言不抛"的脚本健康检查，成本低、覆盖广。需确认直接执行与刷新时执行的上下文（如 `onClick` 会设置 `setUseDValue`）差异。

### 10.2 真正的差分（探路 2）

- 脚本路径（`Text1` 脚本 `autoSize = true`）与 Java 静态路径（`Text2` 的 `info.setAutoSizeValue(true)`）读回一致，均为 `true`。
- **坑：** 静态路径必须用 `*Value` 系列 setter（DValue）。第一次用 `setAutoSize(true)`（运行时值），刷新后被重置为 `false`，差分一度显示"不一致"，属于探路写法错误。
- 局限：`Text` 的 `wrapping` 的 setter 不在 `TextVSAssemblyInfo` 上（`TextVSAScriptable` 的 `addProperty("wrapping","isWrapping","setWrapping",…)` 指向别处），没做；也还没用"zip 资产里已有静态属性"的组件做对照。

### 10.3 Chart 深层对象（探路 3）：可读

脚本设置 `graph = new EGraph()`、`PointElement`、`setHint`、`LinearScale.setMax` 之后，状态断言全部能读到：
- `Chart1.graph` 是 `EGraph`；`getElementCount()` = 1；`getElement(0)` 是 `PointElement`，`getHint(GraphElement.HINT_ALPHA)` = `0.5`。
- `Chart1.graph.getScale("Quantity").getMax()` = `400`。
- `Chart1.dataset.getRowCount()` = 3。

即 Chart 的 EGraph 脚本也不必依赖 PNG（数值读回为 `Double`，断言时注意）。

### 10.4 格式类对象（探路 4）：大部分可读

- `Crosstab1.cellFormat[2][2]` → `ExtendedDecimalFormat[#,###.00]`；`colFormat[0]` → `ExtendedDateFormat[yyyy/MM/dd]`；`shrink` → `true`；`data[2][2]` → 1，`table[2][2]` → 2。
- `Crosstab1.highlighted` 返回 `TableHighlightedArray` 对象，内部没有继续探。
- `setPresenter(3,3,"HTMLPresenter")` 设置不报错，但 `Crosstab1.getPresenter(3,3)` 读取抛 `ScriptException`，没有找到读回方式（可能不存在该读取函数，未查源码确认）。

### 10.5 属性自环往返（探路 5）：批量生成可行，成本比预想低

用 `getVSAScriptable(name).getMemberKeys()` 枚举属性（脚本引擎是 Graal，不是 Rhino；`getIds()` 不存在），对每个标量属性做"读当前值 → 原样写回 → 再读"，不需要手写取值表：

| 组件 | 枚举到的属性 | 标量且往返一致 | 不一致 | 写入报错 | 非标量或 null（跳过） |
|---|---|---|---|---|---|
| Text1 | 36 | 16 | 0 | 0 | 20 |
| Crosstab1 | 57 | 22 | 0 | 1（`data.size`，只读） | 34 |
| Chart1 | 146 | 57 | 0 | 0 | 89 |

- 其中包含了评估文档里 43 个零命中属性的一部分：Text 的 `embedAsURL`、`popAlpha`、`popLocation`、`scaleVertical`；Crosstab 的 `keepRowHeightOnPrint`、`tipOnClick`；Chart 的 `combinedTooltip`、`dateComparisonEnabled`、`tipOnClick`、`separatedStyle`。
- **局限：** "原样写回"只能发现 setter/getter 崩溃和类型转换问题，发现不了"写入映射错了字段"。后续需对布尔值取反、数值加一等再写一遍，这需要最小的类型规则，不是完整取值表。
- 非标量（对象、数组、函数）和 null 的属性（共 143 个）这一轮没覆盖。

### 10.6 Excel（探路 6，仅探路）

- 导出成功（约 25 KB）。**测试 classpath 里没有 Apache POI**（datatest 的 pom 无依赖，本机 Maven 仓库也没有），但 xlsx 是 zip 包里的 XML，用 JDK 的 `ZipFile` 加正则就能读。
- **`Text` 组件在 Excel 里是图片**（`xl/drawings` + `xl/media/*.png`），文字值 `xl-marker-456` 不在 `sharedStrings.xml` 也不在单元格里。所以"脚本改 Text 值是否进 Excel"不能按单元格断言。
- **Crosstab 在 Excel 里是单元格：** 第一个 sheet 解析出 179 个单元格、整个包共享字符串 59 条，数值单元格可读（如 `B2=0`、`B3=1`、`D3=2`、`F3=3`）。导出里共有 5 个 worksheet，原因没查。
- 还没把导出的单元格与脚本读到的数据（`Crosstab1.data`，30 行）逐项对比，需要写一个小的 sheet 解析辅助，或确认能否加 POI 测试依赖。

### 10.7 本轮后的结论调整

- 方案 B 里"脚本报错"不能再沿用"刷新后查 `MessageCommand`"的设想，应改为**直接执行脚本并断言异常**（§10.1）。
- Chart 的深层对象（EGraph、scale、dataset）可以读，Chart 占 57% 的脚本用例不必继续只靠 PNG。
- 方案 C 的批量生成可行，且 Text/Crosstab/Chart 三类已验证；下一步需要加"取反再写"的类型规则。
- 仍未验证：日志可见性、直接执行与刷新执行的上下文差异、`highlighted` 内部、Excel 与脚本数据的逐项对比、表单提交路径、数据函数。
