# vsscript 测试缺口与补充方案

> 日期：2026-11-13　模块：`vsscript`，spec `vsscript/src/test/groovy/inetsoft/test/vsscript/cases/**/*_Spec.groovy`（11 个）
> datatest 基线：`Stateless-Sessions1`（`4a552ce8`；评估在从它切出的分支上做，HEAD `080da329`）；产品源码：`main`（`9d54035b32`，2.1.0-SNAPSHOT）

## 1. 结论

vsscript 的特性是"在 viewsheet 组件上执行用户脚本，再对整页 PNG 比对"。115 个用例、246 张基线，**验证只有 PNG 像素比对一条路**（`vsscript` 不用 `ViewsheetTest`，没有数据层断言），且 57% 的组件脚本用例落在 Chart 上。三个最大的问题：

1. **机制有漏洞（可以直接改）：** `LengendsProperty2` 导出了、基线也在，但比对清单里把 `LengendsProperty1` 写了两遍，这张图从来没被比；另有 11 张基线没有任何用例引用；`Viewsheet_Spec` 用无参 `compareImage()`，比的是"实际导出"，导出缺失不会被发现。
2. **功能面差集：** 产品脚本对象声明了 282 个属性/函数（`addProperty` 等），其中 43 个在 spec 与 zip 内所有脚本里没有任何文本命中；`Thermometer`/`Cylinder`/`SlidingScale` 在 zip 里一个资产都没有；`ComboBox`、`Submit` 没有任何组件脚本用例；视图表级函数（`appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`、`addImage`…）和全局脚本函数（`CALC.*`、`inArray`、`split`…）零用例，而最近 Redmine 里这两类 bug 最密集。
3. **本机无法判定通过与否：** 本机跑 115 个用例 114 个失败，全部是 PNG 像素差异（中位数 0.95%，阈值 0.001%），是渲染环境差异，不是产品错误。CI（GitHub 工作流）里该步骤 `continue-on-error: true`，失败不会让流水线变红。

先做：G1（修清单，零成本）→ G4/G7 里"期望值可独立算出"的部分放进产品单测 → 少量 dashboard 补组件类型的接线用例。

## 2. 覆盖情况

**规模：** 11 个 spec，115 个用例（114 个运行，`TestCase-Script_Order_OneWay4` 被 `@Ignore`，`ScriptEngine_Spec.groovy:84`），`printVS` 调用 236 次；基线 PNG 246 张（`expectData/cases/**`）；`vsscriptStorage.zip` 里 130 个 viewsheet，485 个组件，其中 spec 引用了 62 个，**68 个未被引用**（其中 66 个在 `Manual/*` 下，spec 注释写明"manual case covered"；`ScriptAuto/` 下的 2 个：`VS_Self/vsAlias`、`ScriptEngine/selectedObjec`）。所有用例的 vs id 都是字面量，没有"Not mapped"，所以孤儿列表可信（口径：我自写的扫描器，见附录）。zip 内书签/选择状态未统计。

**覆盖矩阵**（维度：组件类型 × 脚本能力 × 触发位置，已与用户确认；数据来自仓库内的 `vsscript_scan.py`，正则读 spec，静态）：

| 触发位置 | 用例里的脚本数 |
|---|---|
| 组件自身脚本（`HANDLER: <组件名>`） | 212 |
| viewsheet `ONINIT` | 14 |
| viewsheet `ONREFRESH`（对应 onLoad） | 6 |
| 组件事件脚本（onClick、onSubmit 等）、zip 里保存的 vs 级脚本 | 0（只查了 `onInit/onLoad` 标签，别的标签未查） |

| 组件类型 | 组件脚本用例数 | 产品脚本对象声明的属性数（含继承） | 备注 |
|---|---|---|---|
| Chart | 120 | 51 | 占组件脚本的 57% |
| Text | 23 | 35 | |
| TimeSlider | 12 | 42 | |
| Crosstab / CalcTable / Table | 9 / 7 / 5 | 48 / 42 / 46 | |
| RadioButton、CheckBox、Slider、TextInput、GroupContainer、Image、CurrentSelection | 各 3 | | |
| Rectangle、Oval、Tab、Gauge、SelectionList、SelectionTree | 各 2 | | |
| Spinner、Line、Calendar | 各 1 | | |
| **ComboBox、Submit、Thermometer、Cylinder、SlidingScale** | **0** | 40 / 27 / 33 / 33 / 33 | 后三者 zip 里没有资产；Submit 只在未引用的 `Manual/*` vs 里 |

**验证机制：**

- 比对只走 PNG：`VSScriptTest.groovy:107` 调 `exportVS(EXPORT_TYPE_PNG…)`；`VSScriptTest.groovy:150` → `CompareUtil.groovy:456-478`。
- 阈值 `allowedPixelPercent` 默认 0.001%（`CompareUtil.groovy:122`）；尺寸不同单独报错（`:145-148`）；基线或导出缺失时 `readImageFromResources` 抛异常，被 `catch` 记成失败（`:165-168`），所以**点名的**文件缺失会让用例失败。
- 比对遍历"实际导出"还是"基线清单"：点名版 `compareImage([...])` 只比点名的文件；无参 `compareImage()` 列的是 `exportData` 目录（`CompareUtil.groovy:471-475`）——**只对"实际导出"比对**。用无参形式的是 `Viewsheet_Spec` 的 3 个用例。
- 为了隔离组件，harness 用脚本 `visible='hide'` 覆盖非目标组件的脚本（`VSScriptTest.groovy:89-97`，用 `setScript` 是**替换**）；设 `assemblyNames=null` 则不隐藏。另外为 calctable 补加了 Home 书签（`:99-100`）。
- 导出失败是否让用例失败：`exportVS` 抛异常会直接让用例失败（无 try/catch，`:106-111`）。脚本本身报错是否会进 PNG 或抛出：**未验证**（没读产品对脚本异常的处理，harness 也没有读脚本错误的代码）。
- `exportData` 清理：`createFile` 只删同名旧文件（`VSScriptTest.groovy:132-134`）；GitHub 工作流在准备阶段 `./mvnw clean`（`.github/workflows/test-css-vsscript.yml:71`）。**Jenkinsfile 里没有 vsscript**（`grep` 无结果），CI 只有 GitHub 工作流。
- CI 对失败的处理：vsscript 测试步骤 `continue-on-error: true`（`test-css-vsscript.yml:80`）。是否有后续步骤把失败变成红灯：没查。
- 基线性质：基线是回归基线（用同一产品生成的截图），不是独立标准答案；是否人工逐张核对过：未查。
- **本机实际运行（2026-11-13，`./mvnw package -pl commons -DskipTests` 后 `./mvnw test -pl vsscript`，未加 license 参数）：** 115 个用例，114 个失败，1 个跳过；比对输出 238 个像素差异百分比，只有 8 个 ≤ 0.001%；中位数 0.95%，最大 11.67%；18 次"尺寸不同"。**零个"Compare Failed with exception"**，即没有点名文件缺失或导出异常。失败全部是像素差异，属本机与 CI 渲染不同（README 已提示），本机不能作为通过/失败的依据。"导出了但没有基线"：0 个（点名文件 235 个都有基线）。
- 基线文件与引用的对账（运行结果 + spec 文本）：点名 235 个文件，其中 1 个未导出（被忽略的用例）；**基线 246 张，其中 11 张没有任何点名引用**：`BandingField_Chartmap`、`coordinateScale_coordianteTranspose`、`LengendsProperty2`（见 G1，这张导出了但没被比）、`barCornerRadius1/2/3`、`FillBlank_fillTimeGap`、`EGraph4_lenlayout&titlespec`、`Form RCB_binding_script1`、`Tab_bottomTabs`、`VS_embedded_VS`。其中除 `LengendsProperty2` 外，另 10 张是用例改名后留下的陈旧文件，`barCornerRadius` 与 `bottomTabs` 在现有 spec 里搜不到（`grep` 无结果），可能是当年被删的用例。

**断言层：** PNG 整页比对。脚本的"读取"只能通过把值写进 `Text` 组件再截图看到（例：`Text1.value = temp`）；数据层（返回值、行列数）、脚本报错、`parameter`/`thisViewsheet` 之外的运行时对象都不可见；点击/提交类事件脚本无法在静态导出里触发（`Manual/Form/Forms_Submit`、`Manual/OtherScript/sConfirm`、`saveworksheet`、`appendRows` 等 vs 在 zip 里但没有用例，静态判断需要交互）。

**功能面差集（步骤 3b-A）：** 能力清单来自产品 `inetsoft/report/script/viewsheet/*VSAScriptable.java` 里的 `addProperty("名", …)` / `addFunctionProperty(cls, "名", …)`（`VSAScriptable.java:379-405, 728-741` 等；运行时由 `ViewsheetScope.java:764-862` 创建，已确认各组件→脚本类的对应）。统计口径：属性名在 spec 里所有脚本文本、spec 源码、zip 里所有组件 `<script>` 节点的**文本命中**（单词边界；命中是上限，不等于验证过；`value`、`field` 这类常见词偏高）。声明合计 282 个，**零文本命中 43 个**；另有 20 个仅出现在 zip 资产脚本里（spec 脚本没有，但 vs 被引用时会被渲染）。

| 声明类 | 零命中的属性 | 产品单测里有无同名命中（仅名字匹配，未读内容） |
|---|---|---|
| ChartVSAScriptable | `combinedTooltip`、`dateComparisonEnabled`、`padding`、`singleStyle`、`tipOnClick` | 前四项在 `ChartVSAScriptableTest` 有名字，`padding` 无 |
| TableDataVSAScriptable | `col`、`field`、`row`、`keepRowHeightOnPrint`、`tipOnClick` | `keepRowHeightOnPrint`/`tipOnClick` 在 `TableVSAScriptableTest` 有名字；`col/row/field` 太常见，不判断 |
| TextVSAScriptable | `embedAsURL`、`scaleVertical`、`popAlpha`、`popComponent`、`popLocation` | 前两项有（`TextVSAScriptableTest`）；`pop*` 无 |
| ImageVSAScriptable | `popAlpha`、`popComponent`、`popLocation` | 无 |
| InputVSAScriptable | `labelGap`、`labelPosition`、`labelText` | `labelGap` 仅在 `TabVSAScriptableTest`；其余无 |
| ComboBox | `dateFormatPattern`、`queryDateFormat` | 有（`ComboBoxVSAScriptableTest`） |
| 选择类 | `quickSwitchAllowed`（List、Tree）、`selectFirstItemOnLoad`（CheckBox、List、Tree）、`empty`（Container）、`rangeSize`（RangeSlider） | 前两项有；`rangeSize` 无 |
| Slider | `snap`、`tickLabelVisible` | 有 |
| Shape | `shadowInfo` | 无 |
| Tab | `bottomTabs` | 有（`TabVSAScriptableTest`） |
| Table | `applyChanges` | 无 |
| VSAScriptable | `addAction`、`scaledSize` | `addAction` 有（`ChartVSAScriptableTest`）；`scaledSize` 无 |
| VSChartBindingScriptable | `colors`、`lines`、`sizes` | 无（`colors` 的命中在无关类） |
| ViewsheetVSAScriptable | `updateTime` | 有 |

交叉核对（3c）：用另一个口径重查了 14 个零命中项在整个 datatest 仓库的 `.groovy/.java` 里——除 `applyChanges`（1 个文件，未看是哪个模块）外都是 0；zip 里按原始 XML 查，`combinedTooltip`、`dateComparisonEnabled`、`padding`、`tooltipVisible`、`popLocation` 等在**资产的静态属性**里出现（即渲染覆盖了"属性设置的效果"，但脚本这条路径没覆盖），`singleStyle`、`tipOnClick`、`bottomTabs`、`snap`、`tickLabelVisible`、`quickSwitchAllowed`、`shadowInfo`、`scaleVertical`、`updateTime` 等在 zip 里也是 0。所以"零命中"说的是脚本路径，不是特性本身。

视图表级对象（`ViewsheetScope.java:91-106, 524-559`）在 spec 与 zip 脚本里的使用：`runQuery` 1、`toList` 2、`viewsheetPath` 1、`viewsheetAlias` 1、`parameter` 3（spec）/61（资产）、`thisViewsheet` 3/17、`thisParameter` 2、`_USER_`/`_ROLES_` 仅在资产（3/2）；**零命中：** `addImage`、`createConnection`、`refreshData`、`isCancelled`、`appendRow`、`setCellValue`、`saveWorksheet`、`delayVisibility`、`viewsheetUser`、`exportFormat`、`confirmEvent`、`param`、`event/OLD/CHANGED/ADDED/DELETED`、`_GROUPS_`。全局脚本函数（`CALC.*`、`inArray`、`split`、`rtrim`、`union`、`formatNumber`、`mapList`）在 spec 与资产脚本里同样零命中；这些函数在别的 datatest 模块或产品单测里是否被覆盖：**未查**。

**生成型模块补充：** 不适用。vsscript 的核心是"执行脚本"，不是"由 A 生成 B"，没有做 B/C/E。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 比对清单与基线脱节：`ChartProperties_Spec.groovy:268-270` 把 `LengendsProperty1` 写了两遍，`LengendsProperty2` 导出了且有基线却从不被比；11 张基线无人引用；`Viewsheet_Spec` 无参 `compareImage()` 只比实际导出 | Chart2 的图例脚本回归不会报；`Viewsheet_Spec` 里某个导出丢失不会报；陈旧基线遮盖了"用例被删"的事实 | 机制（改清单；`Viewsheet_Spec` 改成点名） | 高 |
| G2 | 阈值 0.001% 且只有 PNG：产品行为对了但渲染变了会误报，渲染没变但数据层错了（脚本报错被吞）不会报 | 脚本抛异常后组件保持原样，若基线也是那种状态则永远通过（脚本异常处理**未验证**，需开发确认） | 机制 + 单测 | 中 |
| G3 | CI 里 vsscript 步骤 `continue-on-error: true`，本机 114/115 失败无法分辨真假 | 真回归和渲染噪声混在一起，没人盯 | 机制（先看 CI 实际通过情况，再决定要不要收紧） | 中 |
| G4 | 43 个脚本属性零命中（§2 表）：`Thermometer`/`Cylinder`/`SlidingScale` 无资产；`ComboBox`、`Submit` 无组件脚本用例 | 这些属性的 setter/getter 写错、类型强转错、取值串解析错不会被发现 | 单测为主；每类只加 1 个 dashboard 代表用例 | 高 |
| G5 | Chart 占组件脚本 57%（120/212），Calendar/Spinner/Line 各 1 个 | 非 Chart 组件的脚本回归几乎只靠 1–3 个用例 | dashboard（补 vs/扩展） | 中 |
| G6 | 触发位置只有组件脚本、`ONINIT`、`ONREFRESH`：无事件脚本（onClick/onSubmit/confirm）、无 vs 级脚本存于资产；`appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`、`addImage`、`createConnection`、`delayVisibility`、`event/OLD/CHANGED` 零命中 | 写表单/写回/事件类脚本的回归不会被发现 | 单测（`ViewsheetScope` 已有 14 个用例，补接口）；交互部分不属于静态 datatest，另行规划 | 高 |
| G7 | 全局脚本函数（`CALC.*`、`inArray`、`split`、`rtrim`、`union`、`formatNumber`、`mapList` 等）在 vsscript 零命中；2025 年起 31 条里 27 条是这一类（见 §7） | 函数返回值的边界错误（空串、重复数、舍入、闰年…） | 单测（期望值可独立算出） | 高 |
| G8 | 68 个 vs 未被引用，其中 `ScriptAuto/VS_Self/vsAlias`、`ScriptAuto/ScriptEngine/selectedObjec` 是自动化目录下的孤儿 | 资产存在但没跑，说明用例被删或漏写 | 机制（确认删除或补用例） | 低 |

方向判断：属性取值、函数返回值 → 单测；组件与脚本绑定的渲染 → dashboard；清单与基线脱节 → 改机制。

> **后续（2026-11-13）：** 对"PNG 之外的验证方式"做了方案与原型，见 `docs/test-gap-assessment/vsscript-upgrade-plan.md`。结论：datatest 只做单测做不到的（真实 vs 资产 + 运行时的脚本健康检查、异常契约、事件端到端、真实数据读回、导出一致性），属性取值与全局函数边界交给产品单测；关键发现是刷新路径会静默吞掉脚本异常。

## 4. Java 单测补充

产品里 `report/script/viewsheet/` 已有 369 个用例（47 个测试类，按 `@Test`/`@ParameterizedTest` 计数），所以这里只列**名字匹配不到测试**或**用例数远少于声明属性数**的类。统计口径：测试类名以被测类名开头，属性名文本命中，未读用例内容。

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `OvalVSAScriptable`、`CylinderVSAScriptable`、`ThermometerVSAScriptable`、`SlidingScaleVSAScriptable`（均无同名测试类，运行时工厂 `ViewsheetScope.java:782-791,833`） | 每类的继承属性 get/set 往返（Shape 4、RangeOutput 7 个声明属性） | 8 | 低 | 否 |
| `RangeOutputVSAScriptable`、`ShapeVSAScriptable`、`SelectionVSAScriptable`、`InputVSAScriptable`（基类，无同名测试） | 基类声明属性：`shadowInfo`、`labelGap/Position/Text`、…… | 12 | 低 | `Input` 基类是否已被子类测试覆盖需开发确认 |
| `TextVSAScriptable`、`ImageVSAScriptable` | `popComponent`、`popAlpha`、`popLocation` | 6 | 低 | `pop*` 的合法取值串 |
| `RangeSliderVSAScriptable` | `rangeSize` | 3 | 低 | 取值边界 |
| `TableVSAScriptable` | `applyChanges` | 3 | 中 | 是否要求表可编辑 |
| `CalcTableVSAScriptable`、`CrosstabVSAScriptable`（各仅 3 个、2 个用例，对应 42、48 个含继承属性） | 声明属性逐项 + `dateComparisonEnabled` | 10 | 中 | 否 |
| `VSAScriptable`（`scaledSize`、`addAction`） | getter、动作注册 | 4 | 低 | `addAction` 语义 |
| `VSChartBindingScriptable` | `colors`、`lines`、`sizes` | 6 | 中 | 否 |
| `ViewsheetScope` 的 `appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`、`addImage`、`createConnection`、`delayVisibility`、`isCancelled` | 参数校验、写权限、返回值 | 16 | 高 | 测试入口：这些函数依赖运行时 vs，是否有现成 harness（`ViewsheetScopeTest`）需确认 |
| 全局脚本函数（`CALC.*`、`inArray`、`split`、`rtrim`、`union`、`formatNumber`、`mapList`） | 空串/全空白/重复数/舍入模式/闰年等边界 | 30 | 中 | 这些函数所在类与已有测试没有逐个核对，先确认再补 |

用例数是设计出来的数量；难度：低 = 纯单元，中 = 需脚本环境，高 = 测试入口未确认。

## 5. Dashboard 补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 无用例的组件类型：ComboBox、Submit、Thermometer、Cylinder、SlidingScale | 新建 vs（每类型 1 个，zip 里已有 `Manual/Form/Forms_Submit`、`Manual/OutPut/Submit_Action` 可复用） | 脚本改标题/值/可见性后组件渲染 | 无 |
| 属性零命中里属于"渲染可见"的：`popComponent/popLocation`（Text、Image）、`shadowInfo`、`labelPosition/labelText`、`bottomTabs`、`snap/tickLabelVisible`、`scaleVertical` | 在已有 vs 上扩展脚本 | 渲染效果 | 属性确实影响渲染的部分需开发确认 |
| Chart 以外各组件的"每个声明属性设一次"基线 | 新建 vs，非 Chart 组件各 1 个用例（Calendar、Spinner、Line 目前各 1 个） | 脚本→渲染的接线 | 无 |
| `LengendsProperty2` 清单修正 | 改 `ChartProperties_Spec.groovy:268-270`（G1） | 既有基线生效 | 本机需用 CI 基线核对 |
| `Viewsheet_Spec` 无参比对 | 改成点名 `compareImage(['TestCase-VS_Base_format', …])` | 导出缺失可被发现 | 先确认 3 个用例各导出哪些文件 |

## 6. 局限与待确认

- 扫描器 `.claude/workflow-shared/datatest-gap/vsscript_scan.py` 是正则读 spec，只认 `HANDLER:/COMMAND:` 写法；属性清单只含各 `*VSAScriptable` 里 `addProperty` 声明的名字，**不含** Chart 的 EGraph/GraphElement 脚本对象和 `bindingInfo` 方法（它们走别的类），所以 Chart 的功能面差集没统计；属性文本命中是上限（`value`、`field` 等常见词会偏高），零命中则只代表字符串没出现（动态拼接的属性名、`eval` 看不到）。
- `vs_backup_scan.py` 的 `strictcheck`/`crosscheck` 没有用：vsscript 用的是 `VSScriptTest`（`printVS`），不是 `ViewsheetTest`，脚本不适用；孤儿统计是自写代码的结果。
- 未运行：CI 实际通过情况；脚本异常是否进入 PNG（G2）。本机运行未传 license 参数（有渲染结果，说明可运行）。
- 需要开发回答：脚本抛异常时产品是弹消息、记日志还是直接中断导出？`Input` 基类声明的属性是否被子类测试覆盖？`pop*`、`applyChanges`、`addAction` 的预期行为。
- 需要用户决定：G3（CI 是否收紧 `continue-on-error`）；基线是否人工核对过；§4 里全局脚本函数是否归 vsscript 评估范围（它们更像"脚本引擎"，可能另有归属）。
- 团队设计文档（`docs/vs/VSOtherAssembly/VSOutPutInputOthers.xlsx` 等）：未读。

## 7. 已知 bug 对照（步骤 3b-D）

**样本与口径（先读这个）：**
- Redmine 里按标题含 `script`（`subject ~ script`，所有状态）取到 **2000 条**，更新时间从 2026-11 回溯到 2018-11。**还没取完**（取到第 2000 条时仍有数据，没拿到服务端总数），所以这是"最近更新的 2000 条"，不是全量。
- 其中 Bug 类 1702 条。再用 `bug_classify.py` 按分类过滤（去掉 AI Plugin、Security、Enterprise Manager、Scheduler 等）、按标题关键词去掉编辑器 UI、Report 脚本（band/section）、部署脚本、前端测试等噪声，**剩 798 条**当作"viewsheet 脚本相关"。
- 根因类别是**按标题关键词分的，没读描述**。每类抽样 7 条人工看，大约三分之二确实相关，类与类之间有混入（例如 `ScriptEntry.Builder`、`Data Folder Browser` 被分到了不对的类）。**数字是上限，只能当线索。**
- "vsscript 现状"一列是按类别对照 §2 的统计，不是逐条核对触发条件。

| 根因类别（标题关键词归类） | bug 数 | 其中 2025 年起 | vsscript 现状 |
|---|---|---|---|
| Chart 脚本 API（bindingInfo、SchemaElement、EGraph、轴/图例） | 126 | 30 | 有：Chart 组件脚本 120 个、spec 里 57 个用例；触发条件未核对 |
| 表/交叉表/自由表脚本（单元格、列宽、公式） | 113 | 17 | 偏薄：Table 5、Crosstab 9、CalcTable 7 个组件脚本 |
| 选择/输入组件脚本（selectedObject、values/labels、Calendar、Slider） | 97 | 16 | 偏薄：共 33 个组件脚本（TimeSlider 12，Spinner 1、Calendar 1，ComboBox 0） |
| 引擎/沙箱/异常（GraalJS、Rhino、锁、超时、报错日志） | 86 | 31 | **无**：没有任何用例断言脚本报错、超时、并发（G2） |
| 属性设置不生效（visible、format、hyperlink、size…） | 69 | 11 | 部分：基础属性有（Text 23 个、`VS_Base`）；其中 12 条标题带 export/portal/viewer，PNG 导出看不到 Viewer 与导出的差异 |
| 触发时机/顺序（onInit、onRefresh、onClick、onLoad） | 67 | 27 | 部分：`ONINIT` 14、`ONREFRESH` 6；`onClick` 等事件脚本 0（G6） |
| 全局函数与计算函数边界（CALC、字符串/日期/数组函数） | 31 | 27 | **无**：零命中（G7）；近两年集中爆发 |
| 参数与嵌入 viewsheet（parameter、thisParameter、embedded） | 30 | 16 | 偏薄：`parameter` 3 处、嵌入只有 `VS_embedded` 1 个用例 |
| 请求新增脚本属性（"add script for…"，记在 Bug 里） | 13 | 8 | 未核对：`smoothLines`、`layoutDirection`、`snap`（最近一批 Epic #74519）在 spec 里搜不到；这些属性的产品实现位置我没定位 |
| 数据访问函数（runQuery、getImage、appendRow、refreshData） | 11 | 0 | **无**：`runQuery` 1 处，其余 0（G6） |
| 未归类 | 155 | 32 | 不判断 |

按根因类别看，没有对应验证的是：**引擎/沙箱/异常、全局函数边界、数据访问函数、事件触发**。有用例但相对 bug 数偏薄的是：选择/输入组件（97 条对 33 个脚本）、表类（113 条对 21 个脚本）。

最近两周的具体条目（来自第一次的 100 条搜索，人工挑选，未重新核对）：`union()` 重复数 81011/81164、`toList/mapList` 81014/81259/81260、`formatNumber` 舍入 81233、Chart `StackRange` 80993、`y2Axis` 80989、`bindingInfo.setGroupOrder/getGroupOrder/isTopNReverse` 80711/80708/80709、箱线图 `setBorderColor` 80985/81032、Crosstab `.data[row][col]` 81194、`highlighted[name]` 80875、嵌套 vs `onClick` 79699、`resetSubmitForms()` 80278。

## 附：数据来源与复现

```
# 本机运行（先构建 commons）
./mvnw package -pl commons -DskipTests
./mvnw test -pl vsscript

# 扫描器：python .claude/workflow-shared/datatest-gap/vsscript_scan.py <cases|zip|api|usage|zero|declzero>（需要环境变量 PRODUCT_CORE=产品的 community/core 目录，`cases`、`zip` 不需要）
# 口径：spec 脚本 = 各用例 COMMAND 字符串；资产脚本 = zip 里各组件 assemblyInfo/script；
#       API = 产品 *VSAScriptable.java 里 addProperty("名"…) 与 addFunctionProperty(cls,"名"…)，沿 extends 链合并
# 产品单测盘点：E:\inetsoft\sr14_0\stylebi-main\community\core\src\test\**\report\script\viewsheet\*.java

# Redmine：取数与分类（§7）
# 取全量需要 REDMINE_URL、REDMINE_KEY 环境变量；脚本未在真实服务器上运行过，首次运行请核对 total_count
python .claude/workflow-shared/datatest-gap/redmine_fetch.py --subject script --out issues.json
python .claude/workflow-shared/datatest-gap/bug_classify.py issues.json [--list <类别前缀> <条数>]
```
