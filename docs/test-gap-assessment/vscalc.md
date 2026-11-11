# vscalc 测试缺口与结论

> 模块：`vsothers`，profile `-Pvscalc`，spec `VS_CalcField_Spec`；产品源码 `main`（2.1.0-SNAPSHOT）。跟踪单：[#80957](https://issues.inetsoft.com/issues/80957)。

## 1. 结论

- dashboard 覆盖面够宽：22 个用例覆盖 11 种图表类型、明细型和聚合型两类计算字段、vscalc 用到的各种聚合函数。
- 已有基线是回归基线（产品自己的输出），抽样独立核对后**没有发现错误结果被固化**（第 3 节）。
- 验证机制已严格化（G1）：导出失败、基线文件未被导出都会让用例失败。CI 仍不挡合入（第 7 节）。
- 主要缺口在产品仓库单测：聚合型计算字段的表达式求值（G3）和 vscalc 用到的聚合函数（G4）已补；明细型计算字段的值级语义（G5）待补。
- dashboard 剩余缺口：孤儿 vs（G2）、选择类组件自身内容只有 PNG（G7）、薄弱格子（G8）。

## 2. 覆盖情况

22 个用例对应 22 个 vs，zip 里共 23 个 vs，`aggregate_trendcomparison` 没有用例也没有基线。40 个书签，spec 引用的书签都存在。

| 计算字段类型 | date | string | double | integer | time | timeInstant | boolean | 合计 |
|---|---|---|---|---|---|---|---|---|
| 明细型（逐行计算） | 13 | 18 | 9 | 11 | 2 | 4 | 1 | 58 |
| 聚合型（对聚合结果再算） | 6 | 3 | 25 | 5 | 1 | **0** | **0** | 40 |

| 使用位置 | 覆盖的 vs 数（共 23） | 评价 |
|---|---|---|
| Chart 的 x / y | 14 / 16 | 充分 |
| Chart 的 color / size / text / **shape** | 7 / 4 / 4 / **2** | shape 薄 |
| Crosstab 的行 / 列 / 度量 / **条件** | 8 / 3 / 12 / **3**（行或列共 9） | 条件薄 |
| CalcTable 的单元格 / **条件** | 9 / **1** | 条件很薄 |
| Table 的列 / **条件** | 6 / **2** | 条件薄 |
| Calendar / SelectionList / SelectionTree / TimeSlider | 6 / 6 / 5 / 3 | |
| Gauge / Text | 3 / 3 | 类型和表达式单一（仅 integer） |

图表类型已有 BAR、BAR_STACK、LINE、PIE（含 donut）、FUNNEL、GANTT、MAP、TREE、NETWORK、SUNBURST、TREEMAP；没有 AREA、POINT、RADAR、STOCK/CANDLE、BOXPLOT、MEKKO、WATERFALL、PARETO、STEP 等。

**断言层：** 只有 Chart（TXT + PNG）和 Table、Crosstab、CalcTable（TXT）导出数据。Selection、Calendar、TimeSlider、Gauge、Text 自身的内容只有整页 PNG；它们的过滤效果通过书签进入被过滤组件的数据。

## 3. 核对结论

**基线：** 所有 txt 基线来自同一次生成（`c6208b58`，2024-08-21），之后没有改过，后续提交只更新 PNG。用 datatest 自带的数据库（`access` 为 H2 `orders.mv.db`，`Examples/Orders` 为 Derby `orders_133.jar`）按 worksheet/模型定义写 SQL 手算，与基线全部一致：

| vs | 核对内容 | 结果 |
|---|---|---|
| `aggregate_with` | query1（18 行）按年：Correlation、WeightedAverage 与 `Calc_weight`、Sum 与同比变化、Covariance、`Calc_first`、`Calc_last` | 一致；Covariance 为总体口径（除以 n） |
| `aggregate_date` | Crosstab1 每个销售员的 `calc_date_max` / `min` / `count` | 一致；count 是 Orders 与 Returns 按 City/State/Company/Category 做 FULL JOIN 后的行数（769/1035/909/682） |

**未出现在基线 txt 里的 8 个计算字段（G9）：**

| vs | 字段 | 结论 |
|---|---|---|
| `calc_data group` | `calc_date` | 已断言（Crosstab2 列维度，表头显示为分组值） |
| `testQuery` | `edate` | 已断言（FreehandTable2/3 的日期层级列） |
| `calc_data group` | `CalcField2`、`Calc_mindate` | 没有组件绑定 |
| `testModel` | `NthSmallest` | 没有组件绑定 |
| `filter` | `max_time` | 缺断言：SelectionList1 的度量，只有 PNG（G7） |
| `parent-child tree` | `CalcField1` | 缺断言：SelectionTree1 的度量，只有 PNG（G7） |
| `testDB2` | `c_date` | 缺断言：RangeSlider2/3（全选，不过滤数据），只有 PNG（G7） |

**书签选择状态：** 确实缩小数据。56 对 (Home)/书签导出中 51 对不同；5 对相同均有正常原因（刷选的源图不过滤自己、书签没有选择状态、组件绑定的表与选择作用的表不同）。

**donut chart：** 有意为之。StyleBI 的 donut 存为 PIE（3）+ `donut="true"` + `Total@` 度量；基线的 Total 909 等于各扇区之和。

## 4. 缺口台账

| # | 缺口 | 方向 | 状态 |
|---|---|---|---|
| G1 | 导出失败、基线缺失不报错 | 改 `ViewsheetTest`，严格即默认 | 已完成（datatest PR #203） |
| G2 | 孤儿 vs `aggregate_trendcomparison` | dashboard：补用例 | 待做 |
| G3 | 聚合型计算字段的表达式求值几乎没有值级单测 | 单测 | 已提 stylebi PR [#8000](https://github.com/inetsoft-technology/stylebi/pull/8000) |
| G4 | vscalc 用到的聚合函数在 `report/filter` 测试薄或没有 | 单测（移植 13.8） | 已提 stylebi PR [#7995](https://github.com/inetsoft-technology/stylebi/pull/7995) |
| G5 | 明细型计算字段的值级语义没有系统测试 | 单测 + dashboard 代表用例 | 待做（第 5 节） |
| G6 | 基线只是产品自己的输出 | 单测手算期望值；新基线抽样独立核对 | 已有基线抽样一致（第 3 节） |
| G7 | 选择类、展示类组件自身内容只有 PNG | dashboard：扩展 `executeVS0` 导出这些组件的数据 | 待做；受影响字段见第 3 节 |
| G8 | 薄弱格子：聚合型 timeInstant/boolean、shape/text/size、condition 位置、Gauge/Text 类型、新图表类型 | dashboard：新建小 vs | 待决定做哪些 |
| G9 | 个别计算字段没有数据断言 | — | 已核对，并入 G7 |

## 5. 产品仓库单测（`core`，`report/filter`）

运行时用 `report/filter` 版本的公式，不走 `mv/formula`（只在物化视图改写时启用）。

**G4（PR #7995）：** 13.8 汇总公式测试（style-intelligence `epic-test-coverage`）7 个类按被测类并入 StyleBI 的 `XxxFormulaTest`、`SummaryFilterTest`，新建 `ModeFormulaTest`、`DistinctCountFormulaTest`、`FormulaAgentTest`，共新增 187 个用例（含 StyleBI 补充：WeightedAverage 负权重、DistinctCount 同类型基本类型与对象路径）。按 StyleBI 与 13.8 有意不同之处调整期望值（Average 保留类型返回 Long、DistinctCount 百分比接受任意数字总数、NumericComparer 日期排在数字之前）。Min 不读 `formula.parse.number` 按正确行为写并 `@Disabled`（[#80517](https://issues.inetsoft.com/issues/80517)）。

**G3（PR #8000）：** `CalcFieldFormulaTest` 新增 14 个用例，期望值手算：多聚合运算、if/else、两列公式的列位置、聚合名映射、null 与空分组、除零。聚合名必须用运行时形式：`AssetQuery` 传入的是 `VSUtil.getAggregateString` 生成的 `field['Sum([price])']`（两列公式为 `field['WeightedAverage([b], [c])']`），整段被替换成一个标识符后执行。

**待做：**

| 被测 | 补什么 | 用例数 |
|---|---|---|
| 明细型计算字段求值（G5） | 算术、拼接、null、日期函数、类型转换、参数、计算字段互相引用 | 17 |
| `CalculateRef` 序列化 | `baseOnDetail` 默认值与往返 | 7 |
| 公式工厂一致性 | 26 个枚举值都能创建 `report/filter` 实例 | 3 |
| 已有公式的通用边界 | 并列、负数、NaN、整数溢出（做前先查 #7995 是否已覆盖） | 8 |

**G5 测试入口：** 明细型计算字段由 `VSAQuery.appendCalcFields` 加进 worksheet 表，`AssetQuery.addExpression` 收集表达式列（引用隐藏计算字段的递归加入），`PostProcessor.formula` 生成 `FormulaTableLens` 逐行执行脚本，再用 `Tool.getData` 转成声明类型。所有表达式都是 `field['x']` 且类型不变时走列映射（`ColumnMapFilter`），不执行脚本。vscalc 的 58 个明细型字段中 43 个是脚本表达式（`sql="false"`），15 个是 SQL 表达式（不写 `sql` 属性即为 SQL）；SQL 表达式能合并时由数据库计算，否则同一文本交给 `FormulaTableLens` 执行。

| 层 | 入口 | 现成例子 | 测什么 | 用例 |
|---|---|---|---|---|
| L1 表达式列 | `new FormulaTableLens(DefaultTableLens, headers, formulas, new GraalJavaScriptEnv(), null)` | `FormulaTableLensTest` | 算术（含除零、一侧为 null）4、拼接（含 null）3、日期函数 4、声明类型与结果类型不一致 3 | 14 |
| L2 worksheet 端到端 | `EmbeddedTableAssembly` 加表达式列，`AssetQuerySandbox.getTableLens` | `WorksheetFormulaVarEndToEndTest` | 参数 1、计算字段互相引用与 `field['x']` 捷径 2 | 3 |
| L3 viewsheet | `ViewsheetSandbox` + viewsheet 计算字段 | 同上类的 `vsTable()` | 明细型/聚合型分流（可选） | — |

注意：脚本路径的除零、null 是 JavaScript 行为（`x/0` 为 Infinity，`null + "_"` 为 `"null_"`，`null * 2` 为 0），再经 `Tool.getData` 转换；SQL 表达式合并到数据库后结果可能不同（除零、null 拼接、整数除法），只能在 L2 或 datatest 发现。

## 6. Dashboard 补充

| 场景 | 做法 | 前提 |
|---|---|---|
| 孤儿 vs（G2） | 给 `aggregate_trendcomparison` 加 feature 和基线：书签 `(Home)`、`bk1` | 基线用 `-Dupdate.baseline=true` 生成后抽样独立核对 |
| 选择类组件自身内容（G7） | 扩展 `executeVS0` 导出这些组件的数据 | `ExportUtil.exportSelectionList` 可能可复用，能否从沙箱取到数据未验证 |
| 聚合型 timeInstant / boolean（G8） | 新建小 vs | 在 StyleBI 制作并导出 backup 并入 zip |
| Chart 的 shape / text / size（G8） | 新建小 vs | 同上 |
| Table / Crosstab / CalcTable 的 condition（G8） | 新建小 vs | 同上 |
| Gauge / Text 非 integer 类型（G8） | 新建小 vs | 同上 |
| 新图表类型（G8） | 新建小 vs（AREA、POINT、RADAR、WATERFALL 择要） | 先确认产品允许这些图表绑定计算字段 |

原则：新建小 vs，不改已有 vs（否则已有基线整体变动）；每个新用例只验证接线，边界和语义放在单测；改 zip 前后用 `vs_backup_scan.py inventory` 对比，确认只有预期的 vs 变了。

## 7. 待决定

**公式语义（[#80953](https://issues.inetsoft.com/issues/80953)，答复前测试按现有行为写并标注）：**

| 问题 | 现有行为 | 开发建议 |
|---|---|---|
| 1 Mode / NthMostFrequent 把 null 当一个值 | 计数 | 忽略 null |
| 2 空 DistinctCount | 返回 0，`isNull()` 为 false | 保持 0 |
| 3a Correlation 方差为 0 或只有一对 | `getResult` 为 null，`getDoubleResult` 为 NaN | 两个零分母公式行为一致 |
| 3b WeightedAverage 权重和为 0 | `getResult` 为 null，`getDoubleResult` 为 0 | 同上 |
| 3c 聚合型计算字段除零 | null（脚本的 Infinity/NaN 被 `ScriptValueConverter` 转成 null），单元格为空 | 当作 null |
| 4 Mode 并列 | 取较小值 | 作为约定固定 |
| 5 Integer 与 Double 混合 | 1 和 1.0 算两个值 | 不固定，除非证明有混合类型的列 |

**缺陷：** vscalc 评估涉及的 [#80950](https://issues.inetsoft.com/issues/80950)、[#80951](https://issues.inetsoft.com/issues/80951)、[#80952](https://issues.inetsoft.com/issues/80952)、[#80584](https://issues.inetsoft.com/issues/80584)、`double[]` 下标（[#80974](https://issues.inetsoft.com/issues/80974)）、`edate` 跨年月末（#80953 问题 6）均已修复。未修：[#80517](https://issues.inetsoft.com/issues/80517)（Min 读 `formula.min.number`）。`CountFormula` 的百分比仍只认 `Integer` 总数，与修复后的 DistinctCount 不一致，现有路径碰不到，测试按现状固定。

**需要测试负责人决定：**
- 严格模式要不要挡住合入：GitHub 工作流是 `continue-on-error`，Jenkins 失败只标 UNSTABLE。
- PNG 基线在哪个平台生成：要与 CI 一致，本机渲染不同，vscalc 的 22 个 PNG 比对在本机全部失败。
- G8 做哪几个场景。

**未验证：**
- 基线独立核对只抽了 `aggregate_with`、`aggregate_date`；DB2、MySQL 上的 vs（`testDB2`、`testModel`、`testQuery` 等）未核对。
- 只评估了 vscalc 一个 spec。

## 附：复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vscalc-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
```

基线核对的取数（数据库文件先复制出来再连；H2 用 2.1.214、用户名为空；Derby 用 10.14.2.0，classpath 加 `orders_133.jar`）：

```sql
-- aggregate_with / query1（H2 docker/src/test/resources/H2Data/orders.mv.db）；Calc_double = quantity / customer_id，按年手算
SELECT od.QUANTITY, o.CUSTOMER_ID, o.ORDERDATE, c.COMPANY FROM ORDERDETAILS od, ORDERS o, CUSTOMERS c
 WHERE od.ORDERNO < 10 AND od.ORDERNO = o.ORDERNO AND o.CUSTOMER_ID = c.CUSTOMER_ID;
-- aggregate_date / OrdersAndReturns（Derby jdbc:derby:classpath:orders_133;user=SA）
-- Orders 分组：DISTINCT City, State, Company, 年月(ORDER_DATE), LAST_NAME, CATEGORY_NAME
-- Returns 分组：DISTINCT City, State, Company, CATEGORY_NAME, 年月(ORDER_DATE)
-- calc_date_count(销售员) = Σ 该销售员的 Orders 分组 × max(1, 同 City/State/Company/Category 的 Returns 分组数)
```
