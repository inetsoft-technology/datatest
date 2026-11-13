# vscalc 产品仓库 Java 单测结果

> 来源：stylebi-main 单测流程产出（`unit-test-workflow/vscalc/`：`report.md`、`decisions.md`、`matrix.md`、`bugs.csv`）。产品源码 `main` + PR #8000；加固在 PR [stylebi#8052](https://github.com/inetsoft-technology/stylebi/pull/8052)，叠在 #8000 上。仅改测试，13 个文件（11 改 2 新）。
> 数字和判断取自上述报告，未重跑。矩阵期望值来自代码、注释、孪生实现和手算，不来自 datatest 文档。
> 缺口全貌见 [vscalc.md](vscalc.md)（§4 是本文的摘要入口）。

## 1. 结论

- 矩阵 **144 行**：**通过 113**（含 14 个钉住现状的 pin）、**不一致 20**（全部已判定）、**没写 10**（7 个等开发回答、3 个丢弃）。
- 13 个测试类合在一次 Maven 调用里：**353 个用例，0 失败，19 个跳过**（就是被 `@Disabled` 的"不一致"用例）。每个类单独跑通过；`forkCount=1` 配 `alphabetical`、`reversealphabetical` 都通过。
- 变异检查：每个 pin 行和每个高优先级、陷阱行都被抓到（38 + 5 个追加）。等价变异 1 个（M-E2，递归不可观察）；豁免 1 个（M-C15，标志效果已由 `RestrictedScriptSurfaceTest.calculatedFieldRunsRestricted` 覆盖）。
- 现有测试按内容复核：没有手算期望值写错的；弱断言、可交换的表达式已换掉（W1–W13）。
- 开出 **10 个 Redmine bug**（#81202–#81206、#81211–#81215；#81202、#81203、#81204 为 Normal，其余 Low），另有他人开的 #81200、#81197 关联，见 §3。
- `vscalc.md` 原先"G3、G5、`CalculateRef`、公式工厂、通用边界还没有单测"**不再成立**：这些已在 #8000 里，G4（Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent）已合入 main（[stylebi#7995](https://github.com/inetsoft-technology/stylebi/pull/7995)）。

## 2. 单测落在哪里

按矩阵"home"列归并统计（含不一致和没写的行；一行可能落在两个类里，各类的数字是近似值，精确数以 `matrix.md` 为准）：

| 缩写 | 测试类（`core/src/test/java/inetsoft/...`） | 被测 | 通过 | 不一致 | 没写 |
|---|---|---|---|---|---|
| CF | `report/filter/CalcFieldFormulaTest` | 聚合型计算字段求值：聚合名映射、多聚合运算、除零、百分比、clone、序列化、受限脚本 | 34 | 3 | 2 |
| FW / FC | `WeightedAverageFormulaTest`、`CorrelationFormulaTest`（+ `CovarianceFormulaTest`） | 两列公式：权重和为 0、方差为 0、一对数据、大数值精度、`Object[]` 长度 1 | 9 | 2 | 3 |
| FN / FM | `NthMostFrequentFormulaTest`、`ModeFormulaTest` | 频次、并列、n 越界、null、`clone` 类型 | 4 | 2 | 3 |
| FD | `DistinctCountFormulaTest` | 去重、null、百分比、`-0.0`、Timestamp/Date | 3 | 2 | 2 |
| PP | `report/composition/execution/PostProcessorTest` | 明细型行级计算：算术、日期函数（含 `edate`/`dateAdd` 月末）、声明类型转换、null 日期 | 25 | 2 | 1 |
| AE | `AssetQueryExpressionColumnTest` | `addExpression` 端到端：互相引用、`field['x']` 捷径、参数、缺列、同名、循环引用 | 12 | 1 | 0 |
| VQ | `VSCalcFieldQueryTest`（新） | `VSAQuery.appendCalcFields` 真实 viewsheet 夹具（V、K、T9、T10、A12、A13 行共用）；含聚合型的每种聚合子公式（J 节） | 14 | 3 | 0 |
| CR | `uql/viewsheet/CalculateRefTest` | `baseOnDetail` 默认值、XML 往返（CDATA、控制字符） | 5 | 0 | 0 |
| UF | `report/internal/UtilCreateFormulaTest` | `Util.createFormula`：26 个枚举值都能创建、n 边界 | 8 | 1 | 0 |
| ER | `uql/erm/ExpressionRefScriptExpressionTest`（新） | `ExpressionRef.getScriptExpression` / `getSQLExpression` 标记解析（带 Spring 上下文，否则静态初始化抛 `ExceptionInInitializerError`） | 0 | 1 | 0 |

预言来源（按行数，近似）：规格 65、手算 38、钉住现状 21、孪生 12、性质 3。

**原先设计的 99 个用例对照：** G3 的 14 个（聚合型表达式求值）、G5 的 17 个（明细型 L1/L2 入口）、`CalculateRef` 7 个、公式工厂 3 个、通用边界 8 个都有对应行；G5 的 L3 层落在 `VSCalcFieldQueryTest`（真实 viewsheet 夹具，用 `vs.addAggrField` 注册聚合字段）。

## 3. 开出的 Redmine bug

| bug | 行 | 问题 | 优先级 | 对应测试 |
|---|---|---|---|---|
| [#81202](https://issues.inetsoft.com/issues/81202) | A13、C7、C8 | 聚合名映射：小写聚合名得到错误值；`Sum([a b])` 与 `Sum([a_b])` 冲突；`ß` 这类大写后长度变化的名字抛异常 | Normal | `VSCalcFieldQueryTest#aggregateNameInOtherCase_…`、`CalcFieldFormulaTest#nameCollision_spaceAndUnderscore`、`#lengthChangingUpperCase` |
| [#81203](https://issues.inetsoft.com/issues/81203) | L1、L2 | `tr_TR` 下 `CalcFieldFormula`、`ExpressionRef` 的 `toUpperCase/toLowerCase` 没用 `Locale.ROOT`（`field` 变成带点大写 I，`FIELD is not defined`） | Normal | 同两个用例在 tr_TR 下失败，默认环境通过；无单独测试 |
| [#81204](https://issues.inetsoft.com/issues/81204) | P9b | `CALC.quarter(null)` 返回 1，应与 `month(null)` 一样返回 -1 | Normal | `PostProcessorTest#nullDate_quarterIsMinusOne` |
| [#81205](https://issues.inetsoft.com/issues/81205) | W8 | `ModeFormula.clone()` 返回 `NthMostFrequentFormula` | Low | `ModeFormulaTest#clone_keepsTheFormulaType` |
| [#81206](https://issues.inetsoft.com/issues/81206) | E3 | 两个表达式列互相引用，以 `StackOverflowError` 结束 | Low | `AssetQueryExpressionColumnTest#circularReference_isAHandledFailure` |
| [#81211](https://issues.inetsoft.com/issues/81211) | U6b | `Util.createFormula` 把负的 n 变成 `SumFormula` | Low | `UtilCreateFormulaTest#negativeN` |
| [#81212](https://issues.inetsoft.com/issues/81212) | C5 | `CalcFieldFormula.setTotal` 不清缓存的结果（真实流程里能否碰到未核实） | Low | `CalcFieldFormulaTest#setTotalAfterGetResult_recomputes` |
| [#81213](https://issues.inetsoft.com/issues/81213) | D2b、D5 | `DistinctCountFormula`：`-0.0` 与 `0.0`、Timestamp 与 Date 判等（**已向开发请求反馈**） | Low | `DistinctCountFormulaTest#negativeZero_…`、`#timestampAndDateOfTheSameInstant_countOnce` |
| [#81214](https://issues.inetsoft.com/issues/81214) | X1f | `/*script*/` 标记后没有正文时抛异常 | Low | `ExpressionRefScriptExpressionTest#overlappingMarkers_doNotThrow` |
| [#81215](https://issues.inetsoft.com/issues/81215) | N1 | `ModeFormula` 文本结果的 `getDoubleResult()` 抛异常（目前没有调用方） | Low | `NthMostFrequentFormulaTest#doubleResultOfText_…` |

**他人开的、与本批相关：**

- [#81200](https://issues.inetsoft.com/issues/81200)（P10）：声明为 Integer 的列遇到超过 int 范围的值被静默截断；对应用例 `PostProcessorTest#integerBeyondInt_isNotClamped`（`@Disabled`，注明单号）。
- [#81197](https://issues.inetsoft.com/issues/81197)：行级计算字段的 null 运算；与 null 的 pin 和 #81204 有关。
- [#81207](https://issues.inetsoft.com/issues/81207)（V5）**不是缺陷**：viewsheet 引用变得可见是有意的惯用法（`VSAQuery:1583`、`VSUtil:4427`、`VSUtil:4450`、`TableConditionUtil:1343`），建议关闭（需要负责人填 Defect Type）；对应测试是启用的 pin。

**没开 bug：** K5、K6、K7（自由表格里的计算字段，来自手工搭的沙箱而不是活的 viewsheet，属夹具类问题）；F1、F2（`Object[]` 长度 1：产品调用方总传 2 个成员，不可达）。`@Disabled` 的用例都带 bug 号，K5–K7、F1、F2 标"not filed"。

**撤回的判断：**

- W10（`NthMostFrequentFormulaTest` 缺 Spring 上下文）：没复现；`Catalog` 用的是 `ConfigurationContext.getContext()`，不是 Spring bean。
- C16、C17（`CalcFieldFormula.addValue(double[])`）：没有产品调用方（`SummaryFilter:501`、`CrossTabFilter:4691-4701`、`AssetQuery:3776`、`AbstractCrosstabVSAQuery:2208` 都传 `Object[]`），也没有独立规格。
- `edate` 跨年：[stylebi#7968](https://github.com/inetsoft-technology/stylebi/pull/7968) 之后 `Calendar.add` 截到月末，测试断言截断值（2001-02-28、2001-03-15），不再有跨年顺延的 pin。

## 4. 钉住现状（class B，待开发确认）

这些行按**当前行为**写成启用的测试，注释标"pending"；开发改了语义就改测试：

V5 viewsheet 引用变得可见（有意）；U1 未知公式名回落为 `SumFormula`；W11 表达式失败时整表被样例行替换；N2 大小写不同的并列；P21 声明为 Integer 时截断；P12b `'yes'` 转 Boolean 为 false；E4 缺列得 null；E5 同名表达式被丢弃；T9 聚合型计算字段的声明类型被忽略；A5 `PthPercentile(50)` 与 `Median` 不同；K4 除零得空单元格；P9c `year/month/day/hour(null)` 为 -1；F6b 带千分位的数字文本被跳过。

## 5. 仍悬着的开发问题

| 问题 | 行 | 说明 |
|---|---|---|
| [#80953](https://issues.inetsoft.com/issues/80953) 问题 1–5、3a、3c | F3、F4、N4、N5、D4、D6 | 开发只给了倾向，没有决定（Q1 Mode 忽略 null——有兼容性风险；Q2 空 `DistinctCount` 保持 0；Q5 不要钉）。3c（聚合型除零）和文本结果按现状钉在 #8000；这 6 行 `hold`，没写，pin 保持"pending" |
| 时区环境矩阵 | P8 | 没写：输入没有和输出一起打印，独立规则是 `GregorianCalendar` 墙钟时间 |

环境矩阵：UTC/en/US、America/Los_Angeles/en/GB、Asia/Kolkata/de/DE 通过；Pacific/Auckland/tr/TR 只有 #81203 的两个 locale 用例失败。surefire 的 `argLine` 固定 en_US/New_York，其他设置是通过临时覆盖加上日志探针证明生效的。

## 6. 弱点和没验证的

- **E5**（同名表达式被丢弃）：只被一个"推断的"变异抓到（`AssetQuery.addExpression` 的表头），真正丢弃的位置没找到。
- **T9**（聚合型计算字段声明类型被忽略）：没有单点变异，用 M-T9b 代替。
- **K5、K6、K7**：来自手搭沙箱，不是活的 viewsheet。
- 所有缺陷都只在单测层确认，**没有在 Composer UI 里复现**。
- 代码评审：无阻塞项；非阻塞项已修，剩下：`@Disabled` 里补 bug 号（已在开单后做）、x1f 的返回值断言（需要开发决定）、共享的 `TestConfig`（另行清理）。
- 聚合型计算字段夹具必须用 `vs.addAggrField` 注册聚合；否则 Average/Count/DistinctCount/Mode/Median/Product/方差类子公式会得到空单元格和 `field is not defined`（decisions 里的 D-A12 已查明是夹具问题，A12 通过）。

## 7. 对 datatest 的启示

- dashboard 用例不必再为"公式算对"负责：值级语义已有 144 行单测矩阵，vscalc 的 vs 只验证接线（见 [vscalc.md](vscalc.md) §5）。
- 产品单测未碰、仍归 datatest 的：基线来源（回归基线还是独立标准答案）、选择类组件状态、`donut chart` 的类型（PIE 还是 DONUT）。
- tr_TR 环境问题（#81203）：datatest 在土耳其语 locale 的机器上跑聚合型计算字段，可能出现 `FIELD is not defined`；排查时先看这个单。
