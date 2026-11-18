# vsscript 产品仓库 Java 单测结果（community PR #8213）

> 来源：产品 PR [inetsoft-technology/stylebi#8213](https://github.com/inetsoft-technology/stylebi/pull/8213)「Add unit tests for viewsheet script properties, ViewsheetScope functions and global script functions」，分支 `test-vsscript-unit-analysis`，状态 **已合入 `main`**（`82bf34a268`，2026-11-17 11:05；下文是合入前核对的结果），只改测试代码，不新增测试类。
> 核对时的产品 `main`（本机）`b6f680a11`；PR head `1542238578`（已多次合并 `main`）。PR 描述里的数字取自描述，**未重跑**；"新增方法数""同名命中"是我对 PR 与 `b6f680a11` 的 diff 做的静态统计，口径见 §6。
> 缺口全貌见 [vsscript.md](vsscript.md)；datatest 侧方案见 [vsscript-upgrade-plan.md](vsscript-upgrade-plan.md)。

## 1. 结论

- PR 在 **21 个已有测试类**上补边界、特殊值、locale、差分用例。合并最新 `main` 后，这 21 个类 **1875 个用例，0 失败，0 错误，14 个跳过**（PR 描述）。此前还做过：每个类单独运行、`alphabetical` / `reversealphabetical` 顺序、4 套时区/locale（`JavaScriptEngineTest`、`CalcDateTimeTest`）；**对产品代码做 32 个变异，32 个被新测试抓住**（PR 描述）。
- 按新增行里的方法声明统计，新增约 **95 个测试方法**（下界：参数化用例与"在原方法里加断言"没算进去；diff 里含 `main` 合并带入的少量改动）。名字带 `Pins` 的是"钉住当前行为"的用例，开发给约定后可能要改（见 §5）。
- **它补掉了 vsscript.md §4 / G4 / G6 / G7 里大部分"名字匹配不到测试"的项**：`pop*`、`rangeSize`、`applyChanges`、`scaledSize`、`shadowInfo`、`labelPosition/labelText`、`ViewsheetScope` 的 `appendRow/setCellValue/saveWorksheet/refreshData/addImage/delayVisibility/isCancelled/addAction`，以及全局函数 `formatNumber`、`rtrim`、`split`、`union`、`toList`、`dateAdd/dateDiff`、`monthname/weekdayname`、`product/average/mode`（对照表见 §3）。
- 开出/关联 **8 个 Redmine bug**：7 个已在 `main` 修复且测试已启用（#81460–#81466），1 个仍开放、测试 `@Disabled`（#81467，待开发确认语义）。
- **没有补的**（仍是缺口）：事件脚本（onClick/onSubmit/confirm）、`viewsheetUser`、`exportFormat`、`confirmEvent`、`param`、Chart 的 EGraph 脚本对象、以及一切"真实 vs 资产 + 运行时"的东西——单测做不到，仍归 datatest（见 §4）。

## 2. 单测落在哪里

| 测试类（`core/src/test/java/inetsoft/…`） | PR 新增行 | 新增方法（静态计数） | 补了什么（按方法名归纳） |
|---|---|---|---|
| `report/script/viewsheet/ViewsheetScopeTest` | +231 | 18 | `appendRow`（非数组、全 null、布尔列、小数入 int 列〔Pins〕）、`setCellValue`（越界列、非法值、null）、未知表名报错、`saveWorksheet`（写回内嵌表）、`refreshData`（刷新时间戳）、`addImage`（PNG 字节、null/非图片）、`delayVisibility`、`isCancelled`、`addAction`（重复三元组只记一次） |
| `util/script/JavaScriptEngineTest` | +151 | 16 | `formatNumber`（每种 BigDecimal 舍入模式、二进制不精确值、负零〔Pins〕、`ROUND_UNNECESSARY`、默认 locale 分隔符、百分号格式）、`rtrim`、`split`（多字符分隔符、空字段、count 越界/负数、原始 double 数组、null/空/空白）、`dateAdd` 月末夹取与闰日、`dateDiff` 的月/季边界 |
| `report/script/formula/FormulaFunctionsTest` | +97 | 12 | `union`（null 参数、不同数字类型、不跨类型/不折叠大小写、Timestamp 与 Date 顺序）、`toList`（distinct 不混 Integer/Double、与输入顺序无关）、`date` 周选项随周起始日 |
| `report/script/viewsheet/VSChartBindingScriptableTest` | +81 | 4 | 按字段名赋 frame/colorFrame（设计态与运行态、多样式图、未绑定名〔Pins〕） |
| `report/script/viewsheet/TextVSAScriptableTest` | +57 | 7 | `popAlpha/popLocation` 脚本值在 reset 后回到设计值、`popLocation` 非法值（Pins）、`popComponent`、clone 不共享 `popLocation`、`scaledSize` |
| `report/script/viewsheet/CheckBoxVSAScriptableTest` | +56 | 5 | 选项/值边界（具体方法名未逐个读） |
| `report/script/viewsheet/GaugeVSAScriptableTest` | +53 | 4 | 同上 |
| `report/script/viewsheet/RectangleVSAScriptableTest` | +42 | 2 | `shadowInfo` 往返与 null 回落默认；`Oval` 类名与继承属性 |
| `report/script/viewsheet/ImageVSAScriptableTest` | +40 | 5 | `popAlpha/popLocation` reset、`alpha` 夹取 0–100、`popComponent`、clone 不共享 |
| `util/script/CalcDateTimeTest` | +65 | 3 | `monthname` 随线程 locale、`weekdayname` 英文 locale |
| `util/script/graal/GraalJavaScriptEngineGlobalsTest` | +39 | 4 | 在 Graal 脚本里：`toList` 排序可用于循环、函数组合、`union` 数字/日期去重 |
| `report/script/viewsheet/ComboBoxVSAScriptableTest` | +30 | 2 | ComboBox 脚本属性（vsscript 评估时 ComboBox 在 datatest 零用例） |
| `report/script/viewsheet/RangeSliderVSAScriptableTest` | +27 | 3 | `rangeSize` reset、小数与大值、经属性赋值读回 double |
| `report/script/viewsheet/SelectionListVSAScriptableTest` | +26 | 2 | 选择列表属性边界 |
| `util/script/CalcStatTest` | +20 | 2 | `average` 跳过非数值、`mode` |
| `report/script/viewsheet/CalcTableVSAScriptableTest` | +14 | 1 | — |
| `report/script/viewsheet/CrosstabVSAScriptableTest` | +10 | 1 | — |
| `report/script/formula/FormulaFunctions2Test` | +8 | 1（Pins） | — |
| `report/script/viewsheet/TableVSAScriptableTest` | +8 | 1 | `applyChanges` 写回表单数据且只写一次 |
| `report/script/viewsheet/ViewsheetScriptErrorLineTest` | +8 | 1 | 根 assembly 的动态值脚本错误不会逃出 reset |
| `util/script/CalcMathTest` | +7 | 1 | `product` 跳过非数值 |

`@Disabled`：`JavaScriptEngineTest` 共 7 个，其中 5 个（"Task 5.3"，架构变更）在 `b6f680a11` 就有，**本 PR 新增 2 个**：`split` 多字符分隔符（#79674，修复版本 13.8，不在此树）和 #81467。`CalcTableScopeTest` 的 7 个 `@Disabled("Bug #81084 …")` 在 `main` 上已有，不属于本 PR，只记在这里供对账。

## 3. 对照 vsscript.md 的缺口

对照口径：属性名在 `core/src/test/java/inetsoft/report/script` 与 `util/script` 下的**文本命中**，`b6f680a11` 与 PR head 各查一次（名字匹配，上限；"命中"不等于验证过）。

| vsscript.md 里的缺口 | `main`（`b6f680a11`） | PR #8213 后 | 判断 |
|---|---|---|---|
| `popAlpha/popComponent/popLocation`（Text、Image） | 0 | 各 2 个文件 | 已补（G4） |
| `rangeSize`（RangeSlider） | 0 | 1 | 已补 |
| `applyChanges`（Table） | 0 | 1 | 已补 |
| `scaledSize`、`shadowInfo` | 0 | 各 1 | 已补 |
| `labelPosition`、`labelText`（Input 基类） | 0 | 各 1 | 已补；`labelGap` 在 `main` 已有 1 个文件 |
| `colors`、`lines`、`sizes`（`VSChartBindingScriptable`） | 0（命中在无关类） | 属性名字面仍无命中；新增的是 `frame`/`colorFrame` 按字段名赋值 | **部分**：测的是相关路径，不是这三个属性名本身；是否等价需开发确认 |
| `appendRow`、`setCellValue`、`saveWorksheet`、`refreshData`、`addImage`、`delayVisibility`、`isCancelled`（G6） | 0 或仅在无关类 | 都在 `ViewsheetScopeTest` | 已补（G6 的单测部分） |
| `createConnection` | 2 个文件有命中 | 2 | 无变化（命中是否指向该函数未读） |
| `viewsheetUser`、`exportFormat`、`confirmEvent`、`param` | 0 | 0 | **仍缺**（G6） |
| `event/OLD/CHANGED/ADDED/DELETED`、`_GROUPS_`/`_USER_`/`_ROLES_` | 有命中 | 无变化 | 命中是否真正测函数语义未读 |
| 全局函数 `split`、`rtrim`、`union`、`formatNumber`、`inArray`、`mapList`、`CALC.*`（G7） | `inArray` 2、`mapList` 4、`CALC.` 10 个文件有命中 | 新增 `split`、`rtrim`、`union`、`formatNumber`、`dateAdd/dateDiff` 的边界用例 | 已补主要边界；`inArray`、`mapList`、`CALC.*` 在 `main` 已有测试，本 PR 没有专门新增 |
| `combinedTooltip`、`dateComparisonEnabled`、`singleStyle`、`tipOnClick`、`embedAsURL`、`scaleVertical`、`selectFirstItemOnLoad`、`quickSwitchAllowed`、`bottomTabs` | 已有命中 | 无变化 | vsscript.md 当时就写"有名字"，本 PR 不影响 |
| `ComboBox` 无用例（datatest 侧） | — | 单测有 2 个新用例 | 单测侧补了；**datatest 侧仍是零**（G4 的 dashboard 部分） |
| 引擎/异常（G2） | — | `ViewsheetScriptErrorLineTest` +1（动态值脚本错误不逃出 reset）；`JavaScriptEngineTest` 的 7 个 `@Disabled` | 极少；脚本异常在**刷新路径被静默吞掉**这件事（见 upgrade-plan）仍没有单测，也没有开发约定 |

## 4. 对 datatest 方向的影响

- **G4（43 个零命中属性）、G6（`ViewsheetScope` 表/写回函数）、G7（全局函数边界）的"单测"部分基本已被 PR 吸收**，vsscript.md §4 里对应行改为"已由 #8213 覆盖（待合入）"，不再作为待办。合入前不要把它们当成"已在 `main`"。
- **datatest 仍要做的**（单测做不到）：真实 vs 资产里脚本健康检查、异常契约、事件端到端、数据读回、导出一致性（upgrade-plan §2 的分工）；组件类型接线用例（ComboBox、Submit、Calendar、Spinner、Line）；G1 的清单与基线机制问题（与 PR 无关）。
- **PR 的价值之一是给 datatest 的"属性取值"类用例免了重复造**：不再为 `pop*`、`rangeSize` 等属性新建 dashboard 用例，只留"渲染可见"的代表用例（`vsscript.md` §5 第 2 行相应收窄）。
- **新开的 bug 反映哪些类别仍在出**：#81460/#81461/#81465（`union`、`toList` 去重）、#81466（`monthname` locale）、#81462（Date 类型输入保留毫秒）、#81463/#81464（Text/Image 的 `popAlpha`、`popLocationValue`）——全部是单测级、值可独立算出的问题，与 vsscript.md §7 里"全局函数边界 31 条，近两年 27 条"的判断一致。

## 5. 已开 bug 与待开发回答

| Bug | 内容 | 状态（PR 描述） | 测试 |
|---|---|---|---|
| #81460 | `union()` 有 null 参数时跳过去重 | 已在 `main` 修复 | 已启用 |
| #81461 | `toList()` distinct 把 Double 1.5 当成 Integer 1 的重复 | 同上 | 已启用 |
| #81462 | 带时间的 Date 赋给 Date 类型输入保留毫秒 | 同上 | 已启用 |
| #81463 | 脚本设的 `popAlpha` 对 Text、Image 不会重置 | 同上 | 已启用 |
| #81464 | Text、Image 的 info 克隆共享 `popLocationValue` | 同上 | 已启用 |
| #81465 | Timestamp 与 Date 的 `union()` 依赖参数顺序 | 同上 | 已启用 |
| #81466 | `monthname()` 保留线程第一次用的 locale | 同上 | 已启用 |
| #81467 | `dateDiff("q")` 季度边界语义 | **开放，[Dev feedback needed]** | `@Disabled`（`JavaScriptEngineTest:797`） |

"钉住现状"（名字带 `Pins`）的新增用例 5 个（按方法名统计）：`FormulaFunctions2Test` 1、`TextVSAScriptableTest`（`popLocation` 非法值大小写敏感拒绝）、`VSChartBindingScriptableTest`（未绑定名宽松存储）、`ViewsheetScopeTest`（`appendRow` 小数入 int 列截断）、`JavaScriptEngineTest`（`formatNumber` 极小负数得到负零）。这些是否是期望行为**需要开发确认**；确认前它们只保证"不再无意变化"，不保证正确。

## 6. 没验证的、口径

- PR 未合入：以上结论针对 PR head，不是 `main`；PR 的测试数、变异数取自 PR 描述，**没有重跑**（本机没有跑产品单测）。
- "新增方法"用 `void 名(` 的 diff 行统计，含合并 `main` 带入的少量改动（该 diff 对 `b6f680a11` 取，PR head 已多次合并 `main`）；`CheckBox`、`Gauge`、`SelectionList`、`CalcTable`、`Crosstab` 的新增用例我只看了数量，没读内容，补什么写的是"未逐个读"。
- 属性命中是文本匹配（单词边界以外的子串也算），上限；"有命中"不代表测了该属性的 setter/getter 往返。
- 手头没有 PR 作者的分析报告（`unit-test-workflow/vsscript/` 不在本机的 `stylebi-main`，也不在 PR 树里），所以"为什么选这些边界""哪些判定是 B/C 类"没法核对，只用了 PR 描述与测试代码。
- 未看：这些单测里 `ViewsheetScope` 用的是什么 harness（是否能代表真实运行时 vs），影响"单测已覆盖 G6"的可信度，需读 `ViewsheetScopeTest` 的 setup。

## 附：复现

```
# 取 PR 到产品 community 仓库（不改当前分支）
git -C E:/inetsoft/sr14_0/stylebi-main/community fetch origin pull/8213/head:pr8213
# 文件清单与行数
gh pr view 8213 --repo inetsoft-technology/stylebi --json title,body,state,files
# 新增方法（静态）
git -C <community> diff -U0 b6f680a11...pr8213 -- <file> | grep -E '^\+.*void\s+\w+\('
# 属性名命中：b6f680a11 与 pr8213 各一次
git -C <community> grep -l -F -- popAlpha <rev> -- core/src/test/java/inetsoft/report/script core/src/test/java/inetsoft/util/script
```
