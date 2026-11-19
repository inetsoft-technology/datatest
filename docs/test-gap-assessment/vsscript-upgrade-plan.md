# vsscript 测试升级方案

> 日期：2026-11-13　模块：`vsscript`　分支：`vsscript-state-assert-spike`
> 依据：`vsscript.md`（缺口评估）+ 三轮原型/探路。**已确认**＝读过源码或在本机跑通；**未验证**＝没查或没跑。
> **接手请先读 `vsscript-state-guide.md`**（怎么跑、代码结构、写用例的配方、已踩的坑）。
> 实施方式：由 AI 按批次推进，每批产出一个可运行的 spec，结果写回本文。不估人天，只标相对大小（小/中/大）。
> **更新（2026-11-17）：** D1 已跑（§5.1），Submit 的"2 次 alert"已查明（§5.7），已提 4 个单并补回归用例（§5.10），套件当时为 59 个用例（5 个 pending），26 个有效变异全部翻红，开发对 #81530 的真实修复也按预期触发了报警（§5.9、§5.10）。**2026-11-18：** 修复陆续合并后，与 Java 重复的 16 个用例（#81531、#81534、#81540、#81550 的三件套）已删除，套件现为 44 个用例、没有 pending（`state/` 下 6 个 spec，含总检）；下面 §5.8–§5.10 是当时的记录，不再改。

## 1. 结论

vsscript 现在只有"整页 PNG 像素比对"一条验证路径，而脚本的效果本质是**状态**不是像素。原型证明：不启动服务、不导出 PNG，就能直接读脚本效果、触发点击、录到提示消息、读到 Chart 的 `EGraph` 和 Crosstab 的数据、解析 PDF/Excel 导出。

三条原则：
1. **与单测分工，不重复。** 产品仓库已有单测专门覆盖单个脚本类（`report/script/viewsheet/` 48 个文件 369 个用例，`util/script/Calc*Test` 约 249 个用例）。datatest 只测单测做不到的：**真实 vs 资产 + 完整运行时**里的脚本行为。
2. **PNG 只留给渲染。** 需要像素才能验证的用例放单独目录，不再承担状态验证。
3. **旧用例不急着删。** 新用例稳定并证明"能失败"之后再处理（§6）。

## 1a. 范围与放置规则的调整（2026-11-18，领导定）

**目标：** 升级 vsscript 的测试，让自动化覆盖更全，发现更多问题和遗漏。**范围：**只做探针和现有框架的升级；需要新增 Dashboard（资产）的，写进 §12 的 TODO，暂不做。

**一个发现或一条回归测试放哪里，按这个顺序判断：**
1. Java 单测已经覆盖 → 不在 datatest 加用例。
2. 还没有，但 Java 单测能写 → 补 Java 单测（bug 还没修的，用 `@Disabled("Bug #xxxxx ...")` 写期望行为，修好后去掉）。
3. 只有 Java 单测机制够不到的才放 datatest：需要真实打开的 vs 与资产、刷新/提交流程、真实绘图、对全部资产的检查。

**怎么用：** 写用例之前先在 `community/core/src/test` 里搜同一函数，再用 `gh pr list --repo inetsoft-technology/stylebi --state all --search "<单号>"` 查有没有修复 PR；探针只用来**发现**，结论落到上面的哪一层由这个顺序决定。单测的输入要能区分错误实现（旧的 CALC 单测只用 p=0.5、只有 null、只有整数，所以没抓到 #81609 的 4 个函数缺陷）。

**到目前为止的结果（来自这套方法）：** 提了 16 个单（#81530、#81531、#81534、#81540、#81550、#81551、#81609、#81610、#81619、#81626、#81627、#81633、#81635、#81651、#81652、#81657），其中 13 个已被开发修复并合并，修复 PR 都带了 Java 测试。所以这些回归的“家”在 Java；datatest 的价值是**发现**，以及用真实路径**核实** Java 夹具里的疑点（#81626、#81627 就是这样成立的）。

**由此推出的调整：**
- **D4（把现有 PNG 用例改成状态断言）和 D9（渲染用例迁到 `render/`）决定不做**，收益低。
- D5、D6、D8 先判断 Java 单测能不能覆盖，能的转 Java。
- D2（固定异常契约）等开发给出语义再做。
- 被吞错误总检（`state/ScriptStateRefreshTripwire_Spec`，§5.1）是 D1 的延伸，必须放 datatest：对全部 130 个 vs 走真实刷新，记录命令、用户消息和 WARN/ERROR 日志，出现白名单之外的事件就失败。
- **#230 里已合并的 `ScriptStateKnownBugs_Spec` 中，与 Java 重复且已过期的 16 个用例已于 2026-11-18 清理**（#81531、#81534、#81540、#81550 的三件套：对照、`[bug present]`、`@PendingFeature`；修复已合并并带了 Java 测试，core 含修复后它们会变红）。保留的 10 个是 #80708 / #80709 / #80711 / Crosstab 的回归，在真实资产上读 `bindingInfo`，它们都通过，暂不删。

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
| D4 | ~~**真实数据读回**：Crosstab 的 `data/table`、Chart 的 `EGraph/scale/dataset`、格式对象；把现有 PNG 用例改成状态断言版~~ **决定不做（2026-11-18，见 §1a）** | G5；现有用例"画进 Text 再比图" | 中 | §5.3 |
| D5 | **触发顺序与跨组件**：`ONINIT`/`ONLOAD`/组件脚本的先后，Text 读取 Crosstab 等 | 触发时机类 bug（67 条） | 中 | 未验证 |
| D6 | **差分**：脚本设置 vs 资产静态属性，两者读回应一致 | 不依赖基线的正确性 | 小 | §5.4 |
| D7 | **导出一致性**：同一 vs 多格式导出（PDF 文字、Excel 单元格），脚本效果应进入导出 | Viewer 与导出的差异（12 条 bug） | 中 | §5.6 |
| D8 | **数据函数端到端**：`appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`（单测已有 `ViewsheetScopeTest` 14 个用例，这里只测经真实 vs 的路径） | 写回类 bug | 中 | 入口未验证 |
| D9 | ~~**渲染用例（PNG）**：只保留图例位置、颜色、布局这类像素可见的，放 `render/`~~ **决定不做（2026-11-18，见 §1a）** | 渲染回归 | 小（迁移） | §6 |

## 4. 优先级

权重＝评估文档 §7 的 bug 数（798 条，按标题关键词分类，只是上限、不是精确值）。

| 顺序 | 方向 | 权重 | 理由 |
|---|---|---|---|
| **1** | **D3 的 `onSubmit` 部分（Submit 组件）** | 触发 67 + 参数 30 中的一小块 | JaCoCo（§11）显示 `SubmitVSAScriptable` 是 0 行，zip 里已有 9 个 Submit 组件，只缺用例；最便宜、目标最明确；同时补上手册里"`onSubmit` 需接 `VSInputService`"的待办 |
| **2** | **D1 + D2** | 引擎/异常 86 | 权重高；只有真实资产才能测，单测做不了；做完立即得到"现有脚本有多少在静默报错"的数字。**注意：它不会增加覆盖率**（§11） |
| 3 | D3 其余（`confirm` 取消分支、嵌套 vs、`parameter`） | 同上 | 点击入口已跑通 |
| 4 | 新资产 / 新脚本补 0 覆盖类（§10 来源 A、B、C；`VSEGraphScriptable`、数据库脚本类） | 按 §11 清单 | 真正扩大覆盖面的只有这条 |
| 5 | D6、D7 | 属性不生效 69 | 小到中，D7 需要 Excel 解析辅助 |
| 6 | D5、D8 | 11–67 | 入口未验证，先各做小探路 |
| 7 | D4、D9 | Chart 126、表 113、选择输入 97 | **已降级**：D4 把旧用例改成状态断言只提高断言可靠性，JaCoCo 显示不扩大覆盖（合并后仅 +1.7 个百分点）；D9 迁移放在最后 |

> 调整记录（2026-11-13）：原顺序是 D1+D2 → D4 → D3；JaCoCo 结果出来后改为先做 Submit，再做 D1+D2，D4 降级。

## 5. 已验证事实

### 5.1 脚本异常（最重要）
| 触发方式 | 调用方能否发现 | 观察 |
|---|---|---|
| 组件脚本 / `ONINIT` / `ONLOAD`（经刷新执行） | **不能** | 不抛异常，无 `MessageCommand`，vs 仍可用；报错语句之前的效果保留 |
| 直接 `scope.execute(script, 组件名)` | 能 | 抛 `ScriptException`，cause 含具体错误，如 `ReferenceError: undefinedFn is not defined (line 1)` |
| 点击（`OnClickService.onClick`） | 能 | 无 try/catch，异常抛给调用方 |

- 源码：`ViewsheetSandbox.java:1685` 对 `ConfirmException | ScriptException` 直接 `// ignore`。
- 测试环境没有 SLF4J 绑定（`Failed to load class "org.slf4j.impl.StaticLoggerBinder"`），日志被丢弃；直接引用 logback 类会因版本不匹配 `ClassNotFoundException`。**加绑定能让日志可见：已验证（2026-11-18）。** 不能用 logback-classic / slf4j-api 2.x 当测试依赖（干净构建时 `inetsoft-runner-maven-plugin:configure` 会失败）；改为在测试里自带一个 slf4j 1.7 的绑定（`src/test/groovy/org/slf4j/impl/StaticLoggerBinder.groovy` + `support/LogRecorder.groovy`），不加依赖。用它做了“被吞错误总检”（`state/ScriptStateRefreshTripwire_Spec`）：130 个 vs 走真实刷新，8 个有事件，其中 1 个是真实发现（`Relation`，#81633）。
- 含义：现有 PNG 用例遇到脚本报错，基线若恰好是那种状态也会"通过"。
- **D1 结果（2026-11-17，探路类 `ScriptStateHealthProbe`）：** 130 个 vs 全部能打开，83 个带脚本，共执行 213 段（组件 179、`ONLOAD` 18、`ONINIT` 16），3 段报错，**全是误报**：2 段读 `parameter.state`（直接执行没传参数，且对应用例被 `@Ignore`），1 段读父视图表 `pviewsheet`（内嵌 vs 单独打开没有父）。**现有资产里没有真实的脚本错误。** `ViewsheetInfo` 只有 `onInit`/`onLoad`，没有 `onRefresh`。
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
- vsscript 测试 classpath 上**没有 Apache POI**（pom 无依赖，`import` 编译失败）。本机 Maven 仓库（`~/.m2/settings.xml` 里的 `localRepository`，不一定是 `~/.m2`）里有 POI 5.4.0，加测试依赖应可行（未试）；不加的话 xlsx 可用 JDK `ZipFile` + 正则读。
- PDF：PDFBox 提取文字，脚本改的 Text 值可见（通过）。
- Excel：`Text` 组件导出为图片（`xl/drawings` + `xl/media/*.png`），不能按单元格断言；Crosstab 是单元格（第一个 sheet 179 个单元格、59 条共享字符串）。导出包含 5 个 worksheet，原因未查；还没把单元格与脚本数据逐项对比。

### 5.7 Submit 与表单提交
- `SubmitVSAScriptable` 只声明一个属性：`refreshAfterSubmit`（`isRefresh/setRefresh`）。zip 里 Submit 的资产：`Manual/Form/Forms_Submit`（Slider1、Spinner1、CheckBox1、ComboBox1、TextInput1、Submit1、TableView1、RadioButton1/2）、`Manual/OutPut/Submit_Action`（Submit1 默认 `refreshAfterSubmit=true`，Submit2 为 `false`）。这些 Submit 在资产里都没有脚本。
- 提交路径：`OnClickService.onClick(..., submitEvent, ...)` → `VSInputService.multiApplySelection`。**`VSInputService` 构造需要 10 个依赖，但这条路径只用 `VSObjectService`、`CoreLifecycleService`、`ViewsheetService`（测试上下文里有真实 Bean），其余 7 个只在编辑对话框里用，传 null 即可。**
- `InputValue`、`VSSubmitEvent` 是接口，Groovy 里用 Map 强转构造：`[assemblyName: {n}, value: {v}] as InputValue`。
- 验证结果：提交 `TextInput1='abc'`、`Spinner1=7`、`Slider1=3` 后，`TextInput1.value`='abc'，`Spinner1.selectedObject`=7（原为 0），`Slider1.selectedObject`=3（原为 67）；不发值则不变（对照用例）。Submit 脚本里能读到刚提交的值（`alert("submitted:"+Spinner1.selectedObject)` → `submitted:7`）。
- **读入口因组件而异：** TextInput 读 `value`（`selectedObject` 读到 undefined/null）；Spinner、Slider 读 `selectedObject`。
- **已查明（2026-11-17，用一个数 alert 次数的探路类，已删除，数字就在下面）：** 这条是 Submit 的**组件脚本**，不是 onClick。提交流程先在"输入应用"阶段重算输入组件，再在刷新里执行，所以组件脚本共 2 次（放在无关的 Slider 上也一样；关掉 `refreshAfterSubmit` 后无关组件降为 1 次，Submit 自己的脚本仍是 2 次）；普通刷新只执行 1 次。**onClick 脚本只执行 1 次**（`Submit_onClick_script_runs_once`，用 Spinner 累加当计数器）。是否有意需开发确认，不当作 bug；`Submit_script_alert_is_recorded_twice` 钉住次数 2。

### 5.8 bug 驱动首轮（Redmine，2026-11-13）：状态断言**抓到了真实 bug**
- **取数：** 仓库里的 `redmine_fetch.py` 需要环境变量 `REDMINE_URL`/`REDMINE_KEY`（本机未设置），且只存标题等精简字段，没有描述。改用会话里的 Redmine MCP 工具 `getIssues`（带 `description`）。**过滤写法：`subject` 必须写成 `~script`**（带 `~` 才是"包含"，写 `script` 返回空）；一次最多取到的条数受输出大小限制，需分页。
- **筛选：** 取最近更新的 40 条标题含 `script` 的 bug，其中 Bug 且 Resolved/Closed 且描述里带可复现脚本的有 8 条；挑了 3 条 Chart 脚本 bug（描述里有明确的复现脚本、预期、根因）。
- **结果（当前产品检出 `stylebi-main` 分支 `bug-81309`）：3 个 bug 全部复现。**

| Redmine | 现象（当前版本） | 对照（正常路径） |
|---|---|---|
| #80709 | `bindingInfo.isTopNReverse("State", ...)` 抛 `ScriptException: Failed to invoke script function`，源码 `VSChartBindingScriptable.java:709` 的 `getRankingOptionValue().equals(...)` 未判空 | `getTopN` 同一维度返回 0 |
| #80708 | `bindingInfo.getGroupOrder("Date", ...)` 抛 `ScriptException` | 设置后 `YEAR`→5、`MONTH`→3 正常 |
| #80711 | `setGroupOrder("Date", Chart.NONE_DATE_GROUP, ...)` 后日期级别为 `null`（应为 `0`），设置被忽略 | 同上 |

- 这三个 bug 在 Redmine 里状态是 Closed，但**当前检出的源码里仍然存在**；#80709 的描述写明修复在 13.8 线，这份检出不含它。是否已合入哪个分支，没有核对。
- **用例（当时的记录；#81531、#81534、#81540、#81550 的用例已于 2026-11-18 删除，见 §1a）：** `ScriptStateKnownBugs_Spec`（当时 9 个）：每个 bug 三个——对照（正常路径）、`[bug present]`（精确断言当前失败特征：#80709/#80708 的失败消息，#80711 的日期级别为 `null`）、`@PendingFeature`（期望行为）。`@PendingFeature` 在**任何**失败下都算通过，单独用分不出"bug 存在"和"测试写错"，所以才加另外两个；bug 修复后 `[bug present]` 会变红、pending 会"意外通过"而失败，两处都提示解除。
- **意义：** 这是第一次拿到"状态用例能抓到真实 bug"的证据，回应了"42 个用例全绿、没有 bug"的疑问。注意边界：这 3 个 bug 是**读写 `bindingInfo` 的接口抛异常**，直接执行脚本就能暴露；**没有**对"渲染错了"类的 bug 做验证。
- **局限：** 只做了 3 条；#80989（`y2Axis`）、#80985（箱线图 `setBorderColor`）已筛出未做；"修复前版本变红"没做（这 3 条在当前版本本来就失败，无需修复前构建）。

### 5.9 变异检查：用例能抓到"产品被改坏"（2026-11-13，本机）
**做法：** 对 6 处产品代码各做一个最小改动，编译该类，临时替换本机 Maven 仓库里 `inetsoft-core` 的 jar，跑对应 spec，看哪些用例变红，再恢复 jar（每项后都恢复，结束时校验 sha256 与原件一致）。先做一个**对照**：用同一份源码不改动地重新编译替换，spec 必须保持全绿，以此证明"替换机制"和"源码与 jar 一致"成立。工具：`.claude/workflow-shared/datatest-gap/vsscript_mutation_check.py`（用法见手册 §6）。

| 变异（产品代码） | spec | 预期变红 | 实际变红 |
|---|---|---|---|
| 对照：重新编译 `SubmitVSAScriptable`，不改 | `ScriptStateSubmit_Spec` | 无 | 无，5/5 通过 |
| M1 `SubmitVSAScriptable` 去掉 `refreshAfterSubmit` 属性 | `ScriptStateSubmit_Spec` | `Submit_refreshAfterSubmit_roundtrip` | 该用例（1 个） |
| M2 `TextVSAScriptable` 去掉 `autoSize` 属性 | `ScriptStateSpike_Spec` | `C_roundtrip_autoSize`、`D_script_vs_assembly_info` | 这两个（`C_roundtrip_autoSize` 的 true/false 两行都红，共 3 个失败） |
| M3 `ChartVSAScriptable` 的 `colorField` 读出 `null` | `ScriptStateChartCrosstab_Spec` | `Chart_binding_roundtrip` | 该用例（1 个） |
| M4 `VSInputService.multiApplySelection` 不再应用输入值 | `ScriptStateSubmit_Spec` | `Submit_applies_pending_input_values`、`Submit_script_runs_after_values_are_applied` | 这两个 + `Submit_script_alert_is_recorded_twice`（脚本读到的值变了，消息文本随之变化，属合理连带，共 3 个） |
| M5 `JavaScriptEngine.alert` 级别强制为 INFO | `ScriptStateSpike_Spec` | `F_click_alert` | 该用例（1 个） |
| M6 "修复" #80709（`isTopNReverse` 判空） | `ScriptStateKnownBugs_Spec` | `[bug present]` 变红 + pending 意外通过 | 两个都失败，另两个 bug 的用例保持 skipped/通过 |

**结论：** 6 个变异全部被预期的用例抓到，没有漏报；M6 还证明 `[bug present]` + `@PendingFeature` 的设计在 bug 被修复时会给出信号。

**补充（2026-11-17）——机制与结果：** 改用"覆盖类"：把改过的 `.class` 编译后放进 `vsscript/target/test-classes/`（类路径上排在产品 jar 之前），**不碰本机 Maven 仓库**，用后删除并校验无残留；先把未改源码按 `git` 重编译，与 jar 逐类 `javap -c` 比对（两批分别 10、29 个类，全部一致）。第 1 批 11 个（还原 #8004 的修复 4 个、模拟修复 #80708/#80709/#81530/#81531/#81540 共 7 个）、第 2 批 13 个（`Spike`/`ChartCrosstab`/`Submit` 里的 Text、Crosstab、Chart 属性，`formatNumber`，PDF 导出，onClick 执行次数）全部按预期翻红；还原 #8004 补上了"修复前会失败"的证明。**`formatNumber` 第一次没翻**（补丁落在测试走不到的分支），改到实际路径后翻红：补丁要打在用例真正走的路径上。**真实修复也当作一次变异跑过：** #81530 的实际修复（`f0aad94be6`）覆盖进去后，预期的 8 个用例全部翻转、没有连带失败，随后把它们改成修复后的预期（修复前的 jar 上这 5 个回归用例会红，覆盖修复后 54 通过 / 5 pending / 0 失败）。**仍未做变异：** `#81534` 的 `[bug present]` 与 113 个类名的守护用例（没找到补丁点）；`Submit_without_values_changes_nothing` 本身是对照。变异脚本在会话临时目录，未入库。
**局限：** 变异是我选的小改动，容易被抓；它证明"断言不是摆设"，不证明能抓住所有真实缺陷。

### 5.10 提单与回归补充（2026-11-17）
- **已提单（stylebi，类别 Viewsheet Execution）：** #81530（Crosstab `getTopN`/`isTopNReverse`/`getGroupOrder`、Chart `isTopNReverse`/`getGroupOrder` 在未设置字段上抛异常；Crosstab `getTopN` 此前无单，#80708/#80709 只修在 13.8 线；**已于同日修复，PR #8301，community `f0aad94be6`**，相关用例已改成回归，需要含该提交的 `inetsoft-core`）、#81531（Chart `isTopNReverse(field, 0)` 返回错误的 false，#78585 第 2 项只修在 wiz 线）、#81534（编辑器列出的 `Chart.FIRST_FORMULA`/`LAST_FORMULA`/`DASH_MASK`/`WIDTH_MASK` 在脚本里 undefined，#81033 只修在 13.8 线）、#81540（`xAxis` 的 getter 读到 Y 轴描述符）。
- **§5.8 的三个 bug：** #80711 已由 #81032（PR #8004）移植，用例转为回归；#80708/#80709 见 #81530。**"Closed"不等于 stylebi 已修，修复常在 13.8 或 wiz 线**；补漏办法是取这些线上的脚本类 bug 在 stylebi 复测。
- **已修好的端到端回归：** #81032 的箱线图边框色（取真实 `VGraph` 绘制并数像素）和 `y2Axis` 不泄漏到主度量（读运行时描述符），见 `ScriptStateChartScriptFixes_Spec`。
- **后续补漏又提 2 个单：** #81550（脚本写入的区间数超过设计数时，Gauge 属性对话框第 4、5 格显示 0，点"确定"会存下，#78585 第 1 项只修在 wiz 线）、#81551（Low：脚本函数报错的消息不带原因，#78585 第 3 项；wiz 线的做法有暴露 JDBC/SQL 细节的顾虑，由开发定）；并给 #81531 补了 `getGroupOrder(field, 0)` 同样不对称的说明。#81430 里已写进展记录。
- **线索未提单：** 分离图上 `yAxis`/`y2Axis` 的写入落在各度量描述符上，读回读的是图表级描述符，所以读不回；判断为"分发写入"的设计结果。
- **界面验证：** 用独立容器（社区镜像 2.1.202611170332，其 `inetsoft-core` 与本机、企业镜像的 jar SHA-256 相同）+ Composer Console，脚本用 try/catch + `alert()` 读结果。8081 是共用 Mongo 的 3 节点集群且常被别人重启，不要在上面验证。

## 6. 目录与旧用例

```
vsscript/src/test/groovy/inetsoft/test/vsscript/
├── cases/    现有 115 个 PNG 用例，暂不动
├── state/    新：状态断言用例，只放 *_Spec（D1–D8）
├── support/  共用工具（ScriptStateBase、RecordingDispatcher），不是用例
├── probe/    探路类（*Probe，默认不跑，只打印）
└── render/   新：必须用 PNG 才能验证的用例（D9）
```
- **基线路径风险：** `VSScriptTest.initHome` 用类的包名推出 `expectData` 子目录。往 `render/` 迁移用例必须同步移动基线，否则找不到基线。**只读了代码，没实际跑过迁移。**
- **旧用例只有同时满足三条才删：**
  1. 有对应的状态断言版，覆盖同样的属性（用生成的覆盖清单核对）。
  2. **新用例能失败：** 在产品里故意改坏一个 setter/映射，确认新用例变红。§5.9 已对 6 处做了变异检查，全部被抓到；未做的见该节清单。
  3. 在 CI 稳定运行一段时间（周期待定）。
- 渲染可见的用例不删，迁到 `render/`。现有 PNG 用例本身并不稳（本机 114/115 失败，CI 里 `continue-on-error: true`），所以删的是"从未真正当过门禁"的用例。

## 7. 风险与待确认

- 直接执行脚本与刷新时执行的上下文可能不同（如 `onClick` 会设置 `setUseDValue`），D1 可能出假失败，需在原型里先量。
- 读写入口不对称（§5.3），非标量属性每个都要先查入口。
- bug 权重是关键词分类的上限，只能当线索。
- **待开发确认：** 刷新路径吞掉脚本异常是否产品有意设计；`pop*`、`applyChanges`、`addAction` 的预期行为。
- **待决定：** CI 里是否收紧 `continue-on-error`；基线是否人工核对过；稳定运行多久后开始处理旧用例。

## 8. 下一步

**第 1 步：Submit（D3 的 `onSubmit` 部分）——已完成（2026-11-13）**
- 代码：`state/ScriptStateSubmit_Spec.groovy`（现 8 个用例，全过）；探路 `probe/ScriptStateSubmitProbe.groovy`（不属于套件）。结果见 §5.7。
- JaCoCo 增量：`SubmitVSAScriptable` 0/14 → 10/14 行（分支 1/8）；`report.script.viewsheet` 合并覆盖 52.8% → 53.1%。
- 剩余：`SubmitVSAScriptable` 另 4 行及分支未跑到，原因未查；Submit 的其他场景（`confirm`、多个 Submit、`refreshAfterSubmit=false` 的刷新差异）未做。

**新增（2026-11-18）：被吞错误总检**已做成用例（`state/ScriptStateRefreshTripwire_Spec`，用 `support/LogRecorder` 和 `org/slf4j/impl/StaticLoggerBinder` 读日志，不加依赖）；与不带新文件的基线逐用例对比，已有用例的结果没有变化。放置规则和 D4/D9 的决定见 §1a，需要新资产的内容见 §12。

**第 2 步：D1 + D2**
- D1 已完成（2026-11-17，探路类 `ScriptStateHealthProbe`，结果见 §5.1）：130 个 vs、213 段脚本，3 段报错全是误报。
- D2（把"刷新吞异常 / 直接执行抛 / 点击抛"固定成用例）未做；`ScriptStateKnownBugs_Spec` 已用直接执行的方式断言了若干异常。

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

## 10. 资产里的脚本不够时怎么办

**事实（2026-11-13，数 zip）：** `vsscriptStorage.zip` 里 130 个 vs，只有 **200 个 `<script>` 节点**（另有非空 `ONINIT` 16 段、`ONLOAD` 18 段）；ComboBox 12 个、Submit 9 个有（Thermometer、Cylinder、SlidingScale 已废弃、仅为向后兼容保留，不计入缺口）。所以 D1（批量执行资产里的脚本）只是"体检"，不等于覆盖；资产没写到的脚本能力它发现不了。datatest 里原本也没有覆盖率工具。

**处理办法：先把"没覆盖"量准，再用三个与单测不同的来源补。**

| 步骤 | 做什么 | 与单测的区别 | 状态 |
|---|---|---|---|
| 0 | **JaCoCo 量覆盖**：对 `inetsoft.report.script.viewsheet.*`、`inetsoft.util.script.*` 统计行/分支覆盖，得到"哪些代码没被跑到"的客观清单，替代文本命中统计 | 目标清单来自代码 | **已跑通，结果见 §11** |
| A | **bug 驱动**：从 Redmine 近两年脚本类 bug 的描述里抽出真实脚本做回归（评估文档只按标题分类，没读过描述） | 真实 vs 触发过的问题 | **首轮已做（§5.8）**：40 条里 8 条带可用脚本，做了 3 条，全部复现；很多 bug 只有现象没有脚本，需逐条判断 |
| B | **生命周期矩阵**：属性（从代码枚举，按类型生成合法值）× 触发位置（组件脚本、`ONINIT`、`ONLOAD`、`onClick`）× 观察点（刷新后保留、导出体现、书签往返、重复执行幂等），断言系统不变量，不断言 getter/setter | 测运行时行为，不测取值 | 未做；非标量属性难自动生成，只能覆盖标量约一半 |
| C | **造资产**：只在现有资产无法触发某个 0 覆盖类时才需要（如 `VSEGraphScriptable`、数据库脚本类）；缺失的组件类型已不需要（Thermometer/Cylinder/SlidingScale 已废弃） | 补输入，不补断言方式 | 未验证内存构造的 vs 能否打开成 `RuntimeViewsheet` |

**顺序：** 0 → D1+D2（定位成体检）→ A → B（在 0 量出的缺口上生成）→ C（只补缺的类型）。

**局限：** 覆盖率只能量出"跑没跑到"，量不出"断言对不对"，仍需"故意改坏产品看用例变红"（手册 §6）；A 取决于 bug 描述里有没有可用脚本；B 的合法值对对象、数组、格式类属性很难自动生成。

## 11. JaCoCo 覆盖率结果（2026-11-13，本机）

**做法：** 不改任何项目文件，用环境变量 `JAVA_TOOL_OPTIONS` 给测试 JVM 挂 JaCoCo 0.8.12 agent（`vsscript/pom.xml` 里 `argLine` 写死，所以不能走 `-DargLine`），产品类文件读本机 Maven 仓库里的 `inetsoft-core-2.1.0-SNAPSHOT.jar`，用 `org.jacoco.cli` 生成报告。复现命令见手册 §2。

| 范围 | 现有 115 个 PNG 用例 | 新增 30 个 state 用例（当时的原型，含探路） | 合并 |
|---|---|---|---|
| `report.script.viewsheet`（75 个类，5356 行） | 2738（**51.1%**），分支 37.4% | 1279（23.9%），分支 17.7% | 2828（**52.8%**），分支 39.3% |
| `util.script`（116 个类，9722 行） | 3424（35.2%），分支 25.9% | 2690（27.7%），分支 18.3% | 3491（35.9%），分支 26.6% |

**读法（注意）：**
- 旧用例本机 114/115 因像素差异失败，但脚本照样执行，**覆盖率统计的是"执行到了"，不是"断言通过"**；它说明脚本路径跑过，不说明验证过。
- **合并后只比旧用例多 1.7 个百分点。** 我的 state 用例复用了旧用例的资产，所以跑到的代码大部分重合。结论：**D1/D2/D4 这类基于现有资产的用例，只能把"已跑到的代码"换成更可靠的断言，不会扩大覆盖面；扩大覆盖面只能靠新资产、新脚本**（§10 的来源 A/B/C）。
- 现有资产合起来对 `report.script.viewsheet` 只覆盖约一半，对 `util.script`（脚本引擎和计算函数）只覆盖约三分之一；后者大部分是计算函数，已由产品单测覆盖，不计入本方案缺口。

**合并后仍为 0 行的类（`report.script.viewsheet`，14 个）：**
`SubmitVSAScriptable`、`VSEGraphScriptable`、`DBScriptable`、`PreparedStatementScriptable`、`CallableStatementScriptable`，以及内部类 `PViewsheetScriptable.AttributeScriptable`、`TableDataVSAScriptable.TableFormRowScriptable`、`VSAScriptable.TipDataCondition`、`VSAScriptable.TipDataConditionArray`。
- `SubmitVSAScriptable`：zip 里有 9 个 Submit 组件，但没有任何用例引用，所以仍是 0。
- `ThermometerVSAScriptable`、`CylinderVSAScriptable`、`SlidingScaleVSAScriptable` 也是 0，但这三个组件已废弃（仅为向后兼容保留），**忽略，不计入缺口**；下面的"14 个"实际有效目标是 11 个。
- `VSEGraphScriptable`、`DB/PreparedStatement/CallableStatement`：数据库脚本接口，现有用例没碰。

**合并后覆盖最低的类（行数 ≥ 40）：** `ScalarCoercion` 1/59、`VSPropertyDescriptor` 13/46、`ViewsheetVSAScriptable` 40/124、`TabVSAScriptable` 35/79、`ImageVSAScriptable` 35/64、`TableDataVSAScriptable` 310/564、`TextVSAScriptable` 42/75、`ChartVSAScriptable` 200/357。
- `ScalarCoercion` 几乎没跑到（2%）：它和脚本值类型转换有关，是值得先看的类。
- `ViewsheetVSAScriptable`（vs 级脚本对象，如 `thisViewsheet`、`onInit`）32%，与评估文档"vs 级函数零命中"一致。

**增量记录：** Submit 用例落地后（§8 第 1 步）：`SubmitVSAScriptable` 0/14 → 10/14 行；`report.script.viewsheet` 合并 52.8% → 53.1%。

**对方案的影响：**
1. 优先级调整：**新资产和新脚本比"把旧用例改成状态断言"更能提升覆盖**。D4（改写旧用例）降到"提高断言可靠性"，不再算覆盖面工作。
2. 有效的 0 覆盖目标是 11 个（去掉 3 个废弃组件）。组件脚本类只剩 `SubmitVSAScriptable`，zip 里已有 Submit 资产，只缺用例，不需要造资产；`VSEGraphScriptable` 和三个数据库脚本类才需要新脚本或新资产（§10 来源 C 的范围随之缩小）。
3. 之后每批补充都用同样的方法量一次，记录增量，不再用文本命中统计。
4. 局限：只统计 `inetsoft.report.script.*` 和 `inetsoft.util.script.*`；行覆盖不等于逻辑覆盖；产品 jar 版本与源码若不一致，行号会偏。

## 12. TODO：需要新 Dashboard 或资产的内容（2026-11-18，暂不做）

范围决定：这一轮只做探针和现有框架的升级，不新增 Dashboard。下面这些做起来需要新资产（或改资产），先记在这里；每一项的优先级和是否值得做，到时候再按 §1a 的顺序判断（先看 Java 能不能做）。

| 要补的资产或改动 | 为什么需要 | 对应的 bug / 缺口 |
|---|---|---|
| 嵌套两层的 embedded 仪表板（根 → Mid → Inner，Inner 里有带 `onClick` 脚本的 Text） | 现有资产里没有任何一个嵌入了 viewsheet，测不到嵌套的 `onClick` | #79699（13.8）；D3 的“嵌套 vs 的 `onClick`” |
| 带两个同时命中的高亮的输出组件（Text / Gauge） | `highlighted[name]` 在多个高亮同时命中时的行为 | #80875（13.8） |
| 真实流程回归的资产：`setAction`、RadioButton 切换后 `selectedObject` 刷新、脚本绑定 shape 图片（含 SVG）、脚本设 `SelectionTree.expandAll` | 原单的附件资产我们没有，需要自己造 | #72673、#72722、#74224、#74225、#72666 |
| 脚本库函数进测试存储（`createBulletGraph` 等 5 个） | 测试存储里没有脚本库，这些函数在脚本里是 `undefined`，测不到 | #76850、#76851 |
| 含更多 `onInit` / `onLoad` / 事件脚本的资产 | 现在只有 16 个 ONINIT、18 个 ONLOAD，没有 ONREFRESH；D5（触发顺序）和 D3 其余需要 | D5、D3 |
| 覆盖 0 的脚本类的资产或脚本：`VSEGraphScriptable`、`DBScriptable`、`PreparedStatementScriptable`、`CallableStatementScriptable`、`ScalarCoercion` | §10 来源 C | §11 的 JaCoCo 清单 |
| 可选，改现有资产：`ScriptAuto/Selection/Relation` 的 onInit 改到 onLoad；`Manual/OtherScript/scope3` 的列绑定表达式、`Manual/Selection/Selection_Calendar` 的列（总检里的已知警告） | 总检白名单里这三个是资产配置问题；改资产会动 PNG 基线，要先评估 | #81633（Relation）；总检的 `EXPECTED` |

## 附：原型代码

`vsscript/src/test/groovy/inetsoft/test/vsscript/`（用例在 `state/`，共用工具在 `support/`，探路类在 `probe/`）：`ScriptStateBase`（开 vs、设脚本、读值、录命令刷新）、`RecordingDispatcher`、`ScriptStateSpike_Spec`（10 个）、`ScriptStateChartCrosstab_Spec`（10 个）、`ScriptStateProbe_Spec`（10 个探路，只打印不断言）。运行：`./mvnw test -pl vsscript -Dtest='ScriptState*_Spec'`（先 `./mvnw package -pl commons -DskipTests`），本机 30 个全部通过。
