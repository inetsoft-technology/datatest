# vscalc 测试缺口与补充方案

> 日期：2026-11-09　模块：`vsothers`，profile `-Pvscalc`，spec `VS_CalcField_Spec`
> datatest 基线：`Stateless-Sessions1`（主分支）；产品源码：`main`（`f12b07ca59`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 版本一致）。第 4 节的产品单测盘点已在 `main` 上复核。
> 2026-11-10 更新：G1 已由 PR #203 落实（`ViewsheetTest` 默认严格）；第 4、6 节补充了疑似缺陷的运行结果和 Redmine 单号（产品源码 `origin/main` `2527280fa1`）。

## 1. 结论

vscalc 的 dashboard 覆盖面不窄：22 个用例覆盖了 11 种图表类型、明细型和聚合型两类计算字段、各种聚合函数，团队设计表里列的缺口基本都已落实。**问题不在"有没有用例"，而在"用例能不能发现错误"：**

1. **验证机制会静默通过**：导出失败只打印堆栈、基线文件缺失不报错，而且 CI 失败也不会让流水线变红。
2. **基线只是产品自己的输出**，只能发现回归，不能证明"算得对"。产品仓库里聚合型计算字段的求值只有 1 个值级单测，vscalc 在用的 5 个聚合函数没有专门单测。

G1 已完成：PR #203 直接修改了 `ViewsheetTest`，把 `DcViewsheetTest` 的严格逻辑合进基类，一次覆盖 chart、vsothers 共 31 个 spec，严格即默认，不加开关。接下来给孤儿 vs `aggregate_trendcomparison` 补用例。再补产品仓库单测（9 组，共 99 个用例设计），最后补 dashboard 的薄弱场景。

## 2. 覆盖情况

**规模：** 22 个用例对应 22 个 vs，zip 里共 23 个 vs，**`aggregate_trendcomparison` 没有用例也没有基线**。40 个书签（保存选择状态），spec 引用的书签都存在。

**覆盖矩阵**（维度：计算字段类型 × 使用位置 × 数据源，已与用户确认）：

| 计算字段类型 | date | string | double | integer | time | timeInstant | boolean | 合计 |
|---|---|---|---|---|---|---|---|---|
| 明细型（逐行计算） | 13 | 18 | 9 | 11 | 2 | 4 | 1 | 58 |
| 聚合型（对聚合结果再算） | 6 | 3 | 25 | 5 | 1 | **0** | **0** | 40 |

| 使用位置 | 覆盖的 vs 数（共 23） | 评价 |
|---|---|---|
| Chart 的 x / y / 维度 / 度量 | 14 / 16 / 18 / 19 | 充分 |
| Chart 的 color / size / text / **shape** | 7 / 4 / 4 / **2** | shape 薄 |
| Crosstab 的维度 / 度量 / **条件** | 12 / 12 / **3** | 条件薄 |
| CalcTable 的单元格 / 度量 / 维度 / **条件** | 9 / 9 / 8 / **1** | 条件很薄 |
| Table 的列 / **条件** | 6 / **2** | 条件薄 |
| Calendar / SelectionList / SelectionTree / TimeSlider | 6 / 6 / 5 / 3 | |
| Gauge / Text | 3 / 3 | 类型和表达式单一（仅 integer，表达式均为"其他"） |

数据源：绑定的表有 Query1（6 个 vs）、OrdersAndReturns（4）、Order Model（4）等，其余基本一对一；底层是 DB2、MySQL 还是 embedded 未细分。
图表类型已有 BAR、BAR_STACK、LINE、PIE、FUNNEL、GANTT、MAP、TREE、NETWORK、SUNBURST、TREEMAP；没有 AREA、POINT、RADAR、STOCK/CANDLE、BOXPLOT、MEKKO、WATERFALL、PARETO、STEP 等。

**验证机制：**

- 比对遍历的是"实际导出的文件"，不是"基线清单"：`CompareUtil.groovy:472`。导出少了文件时没有东西可比，用例照样通过。
- 导出失败只打印堆栈：`ViewsheetTest.groovy:177`（`onExportFailure`）。
- Stateless-Sessions1 上已有严格版基类 `DcViewsheetTest`（`commons/.../modules/DcViewsheetTest.groovy`；`main` 上没有）：导出失败抛异常（`:22`），不给文件名时要求基线文件必须全部被导出（`prepareCompare`），并支持 `-Dupdate.baseline=true`。但使用它的 spec 只有 2 个（`DateComparison_Spec`、`Crosstab_DateComparsion_Spec`）。vscalc 用的是宽松版。严格逻辑已经写好，不用开发新框架，只需要合进基类（第 3 节 G1 方案）。
- `exportData` 没有清理代码，但 CI 都会先清：Jenkins 准备阶段执行 `./mvnw clean -pl vsothers`（`Jenkinsfile:37`），GitHub 工作流先执行 `./mvnw clean`（`test-vsothers.yml:71`）。上一次导出的残留文件不是问题（本地手工重跑除外）。
- CI 对失败的处理：GitHub 工作流 5 个测试步骤全是 `continue-on-error: true`；Jenkins 的 vscalc 阶段是 `catchError(buildResult: 'UNSTABLE')`，失败只让构建变 UNSTABLE。
- 静态预测：22 个用例的基线文件与预期导出文件**完全一致**，所以换严格基类预计不会因文件集合而失败（未运行）。
- 基线来源（回归基线还是独立标准答案）：**未查**。

**断言层：** 只有 Chart（TXT + PNG）和 Table、Crosstab、CalcTable（TXT）会导出数据。Selection、Calendar、TimeSlider、Gauge、Text 自身的内容只能靠整页 PNG（22 个用例中 19 个调用了 `exportAsPNG`）。但书签保存了选择状态，它们的**过滤效果**会进入被过滤的图表和表的数据：绑定计算字段的选择类组件 × 书签共 42 对，其中 23 对带有已应用的选择状态，分布在 11 个 vs。
另有 6 个 vs 的个别计算字段名没有出现在基线 txt 里：`parent-child tree` 0/1、`calc_data group` 3/6、`filter` 4/5、`testDB2` 5/6、`testModel` 6/7、`testQuery` 7/8（子串匹配，可能是只做了条件或换了表头，需核对）。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 导出失败、基线缺失不报错；一个组件失败后面的组件都不导出；CI 失败不挡合入 | 某个组件导出抛异常或少导出文件，用例仍然通过 | 机制：直接改 `ViewsheetTest`，把 `DcViewsheetTest` 的严格逻辑合进去（见下）。**已完成：PR #203** | 高 |
| G2 | 孤儿 vs `aggregate_trendcomparison` | 趋势/比较类聚合的计算字段结果出错，无人发现 | dashboard：补用例 | 高 |
| G3 | 聚合型计算字段求值只有 1 个值级单测，且表达式只是聚合名本身 | `Sum/Sum`、`Average*Average`、`Mode+10`、`if…else` 这类表达式算错 | 单测 | 高 |
| G4 | Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent 在 `report/filter` 无专门单测（已有的同名测试属于 MV 路径，不能替代） | 这些聚合函数实现改错，只有 vs 回归基线可能发现 | 单测 | 高 |
| G5 | 明细型计算字段的值级语义没有系统测试 | 四则运算、除零、null、拼接、日期、参数替换出错 | 单测 + dashboard 代表用例 | 中 |
| G6 | 基线只是产品自己的输出 | 已有的错误结果被固化为基线，永远不报错 | 单测的手算期望值；新基线抽样独立核对 | 中 |
| G7 | 选择类、展示类组件自身内容只有 PNG | 选择列表里计算字段的取值列表出错，但在 PNG 容差内 | dashboard：扩展基类 | 中 |
| G8 | 薄弱格子：聚合型 timeInstant/boolean、shape/text/size、condition 位置、Gauge/Text 类型、新图表类型 | 计算字段在这些位置取值或渲染出错 | dashboard：新建小 vs | 中 |
| G9 | 上述 6 个 vs 的个别计算字段没有数据断言 | 这些字段的结果出错，数据比对发现不了 | dashboard：核对后补 | 低 |

### G1 方案：直接改 `ViewsheetTest`

**为什么不只迁移 vscalc：** 宽松逻辑在基类里，所有继承它的 spec 都有同样的问题（vspara 的 G1 也是这个问题）。逐个 spec 迁移，两套基类会长期并存，新 spec 照抄 `new ViewsheetTest(` 还是宽松的。直接改基类，一次覆盖所有 spec。

**影响面**（chart、vsothers 两个模块，都通过 `commons` 的 test-jar 引用基类）：

| 使用方式 | spec 数 | 调用处 |
|---|---|---|
| 直接 `new ViewsheetTest(` | 24（chart 10，vsothers 14） | 410（210 + 200） |
| 子类 `VSCalcTest`（Convert、FreeExecution、Freehand） | 3 | 82 |
| 子类 `VSActionsTest`（Actions）、`VSFormImportTest`（FormTable） | 2 | 7 + 3 |
| `DcViewsheetTest`（两个 DateComparison spec） | 2 | 2 |
| **合计** | **31** | |

这 31 个 spec 中共有 836 处 `compareData`/`compareImage` 调用，其中 834 处没有传文件名，所以"基线文件必须都已导出"的检查几乎对所有用例生效。传了文件名的路径不用改：导出文件缺失时，`CompareUtil.FileCompare` 已经会报 "not found"。

**改什么**（`commons/.../modules/ViewsheetTest.groovy`）：

1. `onExportFailure`（`:177`）默认改为抛异常，不再只打印堆栈。
2. `executeVS0` 的 `try` 包住了整个 `assemblies.each`（`:140-170`），第一个组件抛异常后，后面的组件都不会导出。改成每个组件单独 catch，把失败收集起来，循环结束后一次抛出并列出所有失败的组件；否则即使改成严格模式，每次也只能看到第一个错误。另外 `sandboxOpt.get()`（`:133`）在 `isPresent` 判断之外，没有 sandbox 时会抛 `NoSuchElementException`，应改成明确报错。
3. `compareData`/`compareImage` 不传文件名时，要求基线文件都已导出（逻辑上移自 `DcViewsheetTest.prepareCompare`）。
4. 支持 `-Dupdate.baseline=true` 写回基线（逻辑上移自 `DcViewsheetTest.updateBaseline`；PNG 默认只补缺失的文件，不覆盖已有的）。
5. 用例开始时清空本 case 的 exportData 目录。现在只删除同名文件（`:353`），而第 3 条检查的是导出目录里有没有这个文件：本地重跑时，上次残留的文件会让"少导出"的情况漏检。CI 先执行 `mvn clean`，不受影响。清理放在构造函数里，前提是没有 spec 在同一个 case 里创建两个实例（**未核对**）。
6. `VSFormImportTest:54` 自己也吞异常（`ex.printStackTrace()`），同样要改；`VSCalcTest` 已经会抛异常，`VSActionsTest` 没有 catch。
7. `DcViewsheetTest` 不改。基类新加的方法改名避开了它的同名 private 方法（`checkBaselineExported`、`exportFolder()` 等），它照常编译运行；严格逻辑在它和基类里各执行一次，结果一致。

**上线：** 严格即默认，不加开关。基类提交后，各模块自己跑、自己处理变红的用例（产品 bug 报给开发，陈旧基线删除或用 `update.baseline` 重新生成并抽样核对）。

**本机运行结果**（2026-11-10，PNG 像素差异因本机与 CI 渲染不同，不计入）：

- vscalc：22 个用例，数据比对全部通过，没有导出异常，没有未导出的基线文件；22 个失败全是 PNG 像素差异（文字抗锯齿/字体）。
- vspara：查出 3 个用例。`Para_basequerysql` 导出异常 `Variable in VPM condition missing: Ppid`（`MySQL1` 的 VPM `VPMP1` 要求变量 `Ppid`，spec 未传），以前被吞掉、导出目录为空而假通过；`Para_query`、`nullVSTest2` 是静态预测过的 7 个陈旧基线文件。其余失败为 PNG 像素差异和 `VSExportTest` 的 HTML 比对（与本改动无关）。

**风险与未知：**

- 其他模块会失败多少，静态上算不出来，要各模块自己跑。`vs_backup_scan.py strictcheck` 只适配 vscalc 的写法：没有 `executeVS` 也按 `(Home)` 推算组件文件；`exportAsPNG` 只认第一个参数是书签列表的写法，认不出 `exportAsPNG(null, [...])`；不认识 `exportAsPDF` 生成的 `-0.png` 页图；不分析 `VSCalcTest` 等子类。拿去跑 chart 等 spec 会大量误判，所以不采用。已实际运行的：vscalc 0 个问题，vspara 3 个用例（见上）。
- 改的是 `commons` 的测试代码，chart、vsothers 都依赖它的 test-jar，改完要先重新构建 `commons`，再跑下游模块。
- 光改基类挡不住合入：GitHub 工作流仍是 `continue-on-error`，Jenkins 仍是 UNSTABLE（第 6 节，由你决定）。

## 4. Java 单测补充（产品仓库 `core`）

运行时走 `report/filter` 版本公式，不走 `mv/formula`（`Util.createFormula`，`mv.formula` 只在物化视图改写时启用）。用例数是设计出来的数量，不是已写好的。

**13.8 已有同类测试，先移植：** 13.8 产品线的汇总公式测试覆盖项目（Redmine `[#RPT Summary Formulas]` 系列，Jakub Kidacki）在 `style-intelligence` 仓库的 [`epic-test-coverage`](https://github.com/inetsoft-technology/style-intelligence/tree/epic-test-coverage/java/src/test/java/inetsoft/test/feature/report) 分支上（核对时为 `5b682845`，2026-11-10），共 9 个测试类、约 120 个用例，按 `gap:report.summary-formulas.*` 编号，已证实的缺陷按正确行为写并 `@Disabled("Bug #...")`。公式代码与 StyleBI 是同一份：`NthMostFrequent`、`Mode`、`DistinctCount`、`Correlation` 只差 import 和 `new Double`/`Double.valueOf`；`WeightedAverage` 多了 [#80540](https://issues.inetsoft.com/issues/80540) 的空字符串修复（StyleBI 尚未修，[#80584](https://issues.inetsoft.com/issues/80584)）；13.8 的 `report/filter` 下没有 `CalcFieldFormula`。所以 G4 的主体是把这些测试移植到 StyleBI `core`，移植后只有 `WeightedAverage` 空字符串用例会失败（对应 [#80584](https://issues.inetsoft.com/issues/80584)）。

**期望值怎么写：** 待开发确认的语义统一放在 [#80953](https://issues.inetsoft.com/issues/80953)，答复前按现有行为写并标注"待确认"；已证实的缺陷，建议按正确行为写并 `@Disabled`、注明 bug 号（13.8 覆盖项目的做法，如 [#80700](https://issues.inetsoft.com/issues/80700)、[#80701](https://issues.inetsoft.com/issues/80701)）。

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `CalcFieldFormula` | 多聚合运算、条件分支、除零、null、字符串结果 | 14 | 中（需脚本环境） | 除零返回什么（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3c）；字符串结果 |
| `WeightedAverageFormula` | 基本、null、权重和为 0、负权重 | 13 | 低（纯单元） | 权重和为 0 返回什么（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3b）；空字符串见 [#80540](https://issues.inetsoft.com/issues/80540)/[#80584](https://issues.inetsoft.com/issues/80584) |
| `CorrelationFormula` | 已知值、方差为 0、一对数据、大偏移精度 | 11 | 低 | NaN 处理（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3a）；缺陷：大偏移精度 [#80950](https://issues.inetsoft.com/issues/80950)，`double[]` 下标 [#80700](https://issues.inetsoft.com/issues/80700)，空字符串 [#80701](https://issues.inetsoft.com/issues/80701)/[#80868](https://issues.inetsoft.com/issues/80868) |
| `NthMostFrequentFormula` / `ModeFormula` | 频次、并列、n 越界、null | 15 | 低 | null 语义、并列规则（[#80953](https://issues.inetsoft.com/issues/80953) 问题 1、4）；缺陷：无参构造越界 [#80951](https://issues.inetsoft.com/issues/80951)（Low）；n 越界时忽略默认结果标志，[#80488](https://issues.inetsoft.com/issues/80488) 已拒（保持现状） |
| `DistinctCountFormula` | 去重、null、空集、百分比 | 11 | 低 | 忽略 null 已确认（[#38229](https://issues.inetsoft.com/issues/38229)）；空集返回 0（[#80953](https://issues.inetsoft.com/issues/80953) 问题 2，可先按 0 写）；Integer/Double（问题 5）；缺陷：非 Integer 总数不算百分比 [#80952](https://issues.inetsoft.com/issues/80952)（Low） |
| 明细型计算字段求值（`ExpressionRef`） | 算术、拼接、null、日期函数、参数、类型不一致 | 17 | 中（入口已确认，见下文 G5） | 多项；日期月末（[#80953](https://issues.inetsoft.com/issues/80953) 问题 6，`edate` 跨年不截月末） |
| `CalculateRef` 序列化 | `baseOnDetail` 默认值与往返 | 7 | 低 | 无 |
| 公式工厂一致性 | 26 个枚举值都能创建 `report/filter` 实例 | 3 | 低 | 无 |
| 已有公式的通用边界 | 并列、负数、NaN、整数溢出 | 8 | 低 | NaN、溢出 |
| **合计** | | **99** | | |

**13.8 已覆盖与 StyleBI 需自补：**

| 被测类 | 13.8 已覆盖（移植） | StyleBI 自己补 |
|---|---|---|
| `WeightedAverageFormula` | 基本、缺一半的数据对、权重和为 0、空字符串 | 负权重（13.8 只顺带测了一组） |
| `CorrelationFormula` | 系数、方差为 0、一对数据、缺一半的数据对、`double[]`（[#80700](https://issues.inetsoft.com/issues/80700) 已 `@Disabled`）、空字符串（[#80701](https://issues.inetsoft.com/issues/80701)） | 大数值精度（[#80950](https://issues.inetsoft.com/issues/80950)） |
| `NthMostFrequentFormula` / `ModeFormula` | 按频次、并列和 null、n<1 取 1、n 超出范围、空集 | 无参构造（[#80951](https://issues.inetsoft.com/issues/80951)） |
| `DistinctCountFormula` | 去重、百分比、区分大小写和空字符串、reset、非 Integer 总数（按现有行为，见 [#80952](https://issues.inetsoft.com/issues/80952) 备注） | 空集、Integer 与 Double 混用 |
| `CalcFieldFormula`、明细型计算字段、`CalculateRef` 序列化、公式工厂一致性 | 无 | 全部 |

按优先级：先移植 13.8 的公式测试（G4）；再写 `CalcFieldFormula` 的表达式求值（G3，表里 14 个，期望值手算）和上表右列的补充；明细型求值（G5）入口已确认，见下文。

**G3 具体补什么（`CalcFieldFormula` 表达式求值，14 个）：**

被测的是聚合型计算字段（`baseOnDetail="false"`）在运行时的求值。`AssetQuery` / `AbstractCrosstabVSAQuery` 先找出表达式引用的聚合，每个聚合建一个子公式；每行数据按位置分给子公式（普通公式占 1 列，两列公式 Correlation、WeightedAverage、First、Last 占 2 列，`startPos` / `isformula2`）。取结果时，表达式里的聚合名先做文本替换（大写、非法字符换成 `_`，`generateRuntimeScript`），子公式结果放进作用域（null 换成占位值），再交给脚本引擎执行。

现状：`origin/main` 的 `CalcFieldFormulaTest` 有 33 个用例，几乎都是构造校验、null 标记、reset、clone、百分比；真正校验求值结果的只有 1 个（`initializerlessLet...`，表达式只引用一个 `SUM`）。13.8 没有这个类。

vscalc 实际用到 36 种聚合型表达式，用例按这些形状设计，小数据、期望值手算：

| 类别 | 内容（vscalc 里的例子） | 个数 |
|---|---|---|
| 多个聚合组合运算 | `Sum/Sum`（testQuery、testModel）、`Average*Average`、`Correlation+Correlation`、`Mode+Mode`；聚合加常数 `Mode+10`、`Sum*2` | 5 |
| 条件分支 | `if (Count(Company) > 1500) {...} else {...}` 两个分支，结果是字符串且类型正确 | 2 |
| 两列公式的列位置 | 普通公式与两列公式混用（如 `Sum(a) + WeightedAverage(b, c) + Count(d)`），各子公式取到自己的列；按真实路径 `Object[]` 输入（`double[]` 路径会撞上 [#80700](https://issues.inetsoft.com/issues/80700)） | 2 |
| 聚合名映射 | 带特殊字符的名字（`orders:orderno`、`Month(Date)`）、大小写、同一聚合在表达式里出现两次、日期类型结果（`Max(Month(Date))`） | 2 |
| null 和空分组 | 某个子公式结果为 null；空分组时有无默认结果标志 | 2 |
| 除零 | `Sum(a)/Sum(b)`，Sum(b) 为 0；按现有行为写，标注待确认（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3c） | 1 |

其中"两列公式的列位置"和"聚合名映射"是 `CalcFieldFormula` 自己的逻辑，写错会悄悄算错或取错列，优先补。

写法：加在现有 `CalcFieldFormulaTest` 里，复用它的环境（Spring 上下文 + `AssetQuerySandbox`，`box.getScriptEnv()` / `box.getScope()`）。表达式用运行时的真实形式，即 `field['Sum([price])']`，聚合名传 `"Sum([price])"`；现有测试直接写 `SUM` 的方式测不到名字替换。难度中，主要成本是启动 Spring 上下文。

**G5 测试入口（明细型计算字段求值，17 个）：**

运行时路径：`VSAQuery.appendCalcFields` 把 viewsheet 上 `baseOnDetail` 为 true 的计算字段（`CalculateRef` 包着 `ExpressionRef`）作为列加进绑定的 worksheet 表（聚合型走 G3 的 `CalcFieldFormula`）→ `AssetQuery.addExpression` 收集表达式列，引用了隐藏计算字段的会递归加入（如 `field['calc_quarter'] + field['CUSTOMER_ID']`）→ `PostProcessor.formula` 生成 `FormulaTableLens`，逐行执行脚本，再用 `Tool.getData` 转成声明类型（转换丢数据时保留原值）。捷径：所有表达式都是 `field['x']` 且类型与原列一致时，直接映射列（`ColumnMapFilter`），不执行脚本。只有 `sql=true` 的表达式下推 SQL；vscalc 的 58 个明细型计算字段（vs 文件里共 324 处 `CalculateRef` 引用）`sql` 全为 false 或未设，全部走脚本。

现有测试几乎都在测并发、加锁、超时、swap（`FormulaTableLens*Lock/Stall/Swap/Timeout`、`VSAQueryCalcFieldConcurrencyTest` 等）；`FormulaTableLensTest` 的 5 个用例里只有 1 个是算术。13.8 也没有这一层的值语义测试。

三层入口，产品里都有现成写法：

| 层 | 入口 | 现成例子 | 适合测 | 成本 |
|---|---|---|---|---|
| L1 表达式列 | `new FormulaTableLens(DefaultTableLens, headers, formulas, new GraalJavaScriptEnv(), null)`，`setColType` 声明类型，`XTableUtil.assertEquals` 断言 | `FormulaTableLensTest` | 表达式语义：算术、拼接、null、日期函数、类型转换 | 低 |
| L2 worksheet 端到端 | `EmbeddedTableAssembly` 加表达式列，`new AssetQuerySandbox(ws).getTableLens(name, RUNTIME)` | `WorksheetFormulaVarEndToEndTest` 的 `ws()` / `box()` | `addExpression`：计算字段互相引用、`field['x']` 捷径两个分支、`parameter.X` | 中 |
| L3 viewsheet | `ViewsheetSandbox` + viewsheet 计算字段 | 同一测试类的 `vsTable()`（反射把 ws 接到 vs） | `appendCalcFields`：明细型/聚合型分流、同名列先删再加 | 较高 |

用例分配（例子取自 vscalc 实际表达式，期望值手算）：

| 类别 | vscalc 例子 | 层 | 个数 |
|---|---|---|---|
| 算术，含除零、一侧为 null | `price/customer_id`、`CUSTOMER_ID*REGION_ID`、`TotalPurchased-TotalReturned`、`customer_id+10` | L1 | 4 |
| 字符串拼接，含 null 参与拼接 | `field['State']+"_"+field['City']`、`concat(field['customers:state'],'a')` | L1 | 3 |
| 日期函数 | `CALC.quarter/year/day`、`dateAdd('m',5,...)`、`dateAdd('d',4,...)`、`CALC.edate(...,2)` | L1 | 4 |
| 声明类型与结果类型不一致 | 声明 integer 得 double（`CALC.quarter(...)*2`）、timeInstant、boolean、转换丢数据时保留原值 | L1 | 3 |
| 参数 | `field['city'] + '   ' + parameter.CheckBox1[0]`、`field['customer_id']+parameter.RadioButton1` | L2 | 1 |
| 计算字段互相引用、`field['x']` 捷径两个分支 | `field['calc_quarter'] + field['CUSTOMER_ID']`；36 个纯 `field['x']` 字段 | L2 | 2 |

另可在 L3 补 1～2 个（明细型加到绑定表、聚合型不加），不计入 17 个。

注意：
- `CALC.edate(field['orderdate'], 2)` 是 vscalc 在用的表达式，会碰上 [#80953](https://issues.inetsoft.com/issues/80953) 问题 6 的跨年顺延（12-31 加 2 个月得 03-03），先按现有行为写并标注。
- 明细型的除零、null 参与运算是 JavaScript 行为（`x/0` 得 Infinity，`null + "_"` 得 `"null_"`，`null * 2` 得 0），以及 `Tool.getData` 对 Infinity 的转换，不在 [#80953](https://issues.inetsoft.com/issues/80953) 里（那里问的是聚合型除零）；先按现有行为写，是否补问待定。

## 5. Dashboard 补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 验证机制（G1） | 不改 spec，改 `ViewsheetTest`（第 3 节 G1 方案）。就 vscalc 而言，`strictcheck` 静态预测 22 个用例的基线与导出文件集合完全一致（基线多出 0 个、导出缺基线 0 个），所以预计不会因文件集合失败 | 导出失败、基线缺失不再静默通过 | 各模块自行运行并处理变红的用例 |
| 孤儿 vs（G2） | 给 `aggregate_trendcomparison` 加 feature 和基线：书签 `(Home)`、`bk1`，预计导出 7 个文件 | 趋势/比较聚合的计算字段 | vs 已在 zip 里；基线用 `-Dupdate.baseline=true`（仅严格基类可用），生成后抽样独立核对 |
| 聚合型 timeInstant、boolean（G8） | 新建小 vs | 目前 0 个 | 在 StyleBI 里制作并导出 backup 并入 zip |
| Chart 的 shape / text / size 视觉绑定（G8） | 新建小 vs | 计算字段用于视觉绑定 | 同上 |
| Table / Crosstab / CalcTable 的 condition 位置（G8） | 新建小 vs | 计算字段用于条件 | 同上 |
| Gauge / Text 非 integer 类型（G8） | 新建小 vs | 计算字段用于 Gauge、Text | 同上 |
| 新图表类型（G8） | 新建小 vs（AREA、POINT、RADAR、WATERFALL 等择要） | 计算字段在这些图表上取值与渲染 | 同上；先确认产品允许这些图表绑定计算字段 |
| 选择类组件自身内容（G7） | 扩展 `executeVS0` 导出这些组件的数据 | 选择列表的取值列表等 | `commons` 的 `ExportUtil` 已有 `exportSelectionList`，可能可复用；能否从沙箱取到数据未验证 |
| 个别计算字段未入基线（G9） | 核对那 6 个 vs，确认是只做条件还是漏断言 | | 无 |

做法原则：**新建小 vs，不改已有 vs**（改已有 vs 会让已有基线整体变动，混进噪声）；每个新用例只验证"接线"，边界和语义放在第 4 节的单测里，不为边界值堆 vs。zip 是整个 storage 的二进制备份，改完后用 `vs_backup_scan.py inventory` 前后对比，确认只有预期的 vs 变了。

## 6. 局限与待确认

**需要开发回答（决定单测期望值怎么写）：** 已汇总到 **[#80953](https://issues.inetsoft.com/issues/80953)**，Redmine 查过的结果：

| # | 问题 | 状态 |
|---|---|---|
| 1 | null 语义：`Count`/`DistinctCount` 忽略 null；`NthMostFrequent`/`Mode` 把 null 当一个值计数 | 前半已确认有意（[#38229](https://issues.inetsoft.com/issues/38229)："it's how sql implements count"；[#35712](https://issues.inetsoft.com/issues/35712)）；Mode 一侧待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 1） |
| 2 | 空集：`DistinctCount` 返回 0 且 `isNull()=false`，其他公式返回 null | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 2），按 [#38229](https://issues.inetsoft.com/issues/38229) 推断 0 是有意的 |
| 3 | 除零与 NaN：`Correlation` 方差为 0 或一对数据（`getResult` 为 null、`getDoubleResult` 为 NaN）；`WeightedAverage` 权重和为 0；聚合型表达式除零 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3） |
| 4 | `Mode` 并列按值升序 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 4）；13.8 测试方认为"随意"（[#80738](https://issues.inetsoft.com/issues/80738) 附注） |
| 5 | Integer 与 Double 混合时是否为同一值 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 5） |
| 6 | `CorrelationFormula.addValue(double[])` 的下标约定 | 已有单 [#80700](https://issues.inetsoft.com/issues/80700) 跟进 |
| 7 | 日期月末语义 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 6）。已运行：`dateAdd("m")` 截到月末；`edate` 同年内截月末，跨年顺延（2000-12-31 +2 得 2001-03-03），由处理 [#80953](https://issues.inetsoft.com/issues/80953) 的人决定是否另开缺陷 |

13.8 的测试已按现有行为固定了 Mode 计 null、并列取小值、空 Mode 为 null、Correlation 方差为 0 返回 null、WeightedAverage 权重和为 0 返回 null（[#80953](https://issues.inetsoft.com/issues/80953) 问题 1、2 的 Mode 一侧、3a、3b、4，见该单备注）；若开发同意"与 13.8 一致"，只剩计算字段除零（问题 3c）、Integer 与 Double（问题 5）、日期月末（问题 6）需要答复。

开发对边界语义的倾向：实际报表碰不到的边界结果保持现状（[#80488](https://issues.inetsoft.com/issues/80488)、[#80541](https://issues.inetsoft.com/issues/80541) 均以兼容性为由拒绝）。

**疑似缺陷（已运行确认，`origin/main` `2527280fa1`）：**

| 缺陷 | 结果 | 正常使用能否碰到 | 单号 |
|---|---|---|---|
| `CorrelationFormula` 一次遍历求和，大数值丢精度 | `y = 3x + 7`：x 在 1e7 时 0.99987，1e8 时 0.962，1e9 时为 null（应为 1） | 能，交叉表/图表/汇总的 `Object[]` 路径 | [#80950](https://issues.inetsoft.com/issues/80950)（Normal） |
| `CorrelationFormula.addValue(double[])` 读 `v[2]` | 长度 2 的数组抛 `ArrayIndexOutOfBoundsException`；`CalcFieldFormula:157` 传的正是 `{x, y}` | 未找到会调用 `CalcFieldFormula.addValue(double[])` 的路径 | 已有 [#80700](https://issues.inetsoft.com/issues/80700) |
| `NthMostFrequentFormula()` 无参构造 n 为 0 | `getResult` 抛 `IndexOutOfBoundsException: Index -1` | 不能：`CalcTableLens` 只做 `instanceof`，交叉表合并单元格改用 `SumFormula`，目标线界面选不到 | [#80951](https://issues.inetsoft.com/issues/80951)（Low） |
| `DistinctCountFormula` 百分比只认 `Integer` 总数 | 总数为 Long/Double/Short 时返回原始计数 | 不能：总数来自同一公式的 `getOriginalResult()`，恒为 Integer | [#80952](https://issues.inetsoft.com/issues/80952)（Low）；13.8 的 `countPercentageNeedsIntegerTotal` 把它当作规则，已在单里请开发二选一 |

**需要你决定：**
- 严格模式要不要真正挡住合入：目前 GitHub 工作流 `continue-on-error`、Jenkins 为 UNSTABLE，改基类只是把"静默通过"变成"测试失败"。
- PNG 基线在哪个平台生成，要和 CI 一致。
- G8 里要做哪几个新场景。

**没验证的：**
- 严格模式已在本机跑过 vscalc、vspara 和 chart 的两个 spec（见第 3 节）；其余 spec 由各模块自己运行处理。
- 单测盘点按类名、文件名、方法名静态匹配，没有逐个读测试内容。
- `donut chart` 这个 vs 的类型是 PIE（3），不是 DONUT（0x13），是否有意未核实。
- 选择状态是否真的缩小了数据范围，静态上无法判断（书签可能只保存选中项）。
- 只覆盖 vscalc 一个 spec，其他模块未评估。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vscalc-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
```
