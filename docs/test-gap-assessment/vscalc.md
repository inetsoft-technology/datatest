# vscalc 测试缺口与补充方案

> 模块：`vsothers`，profile `-Pvscalc`，spec `VS_CalcField_Spec`
> datatest 基线：`Stateless-Sessions1`（主分支）；产品源码：`main`（2.1.0-SNAPSHOT，与 datatest `pom.xml` 版本一致）。Java 单测结果见 [vscalc-java-unit-tests.md](vscalc-java-unit-tests.md)。

## 1. 结论

vscalc 的 dashboard 覆盖面不窄：22 个用例覆盖了 11 种图表类型、明细型和聚合型两类计算字段、各种聚合函数，团队设计表里列的缺口基本都已落实。**问题不在"有没有用例"，而在"用例能不能发现错误"：**

1. **验证机制会静默通过**：导出失败只打印堆栈、基线文件缺失不报错，而且 CI 失败也不会让流水线变红。
2. **基线只是产品自己的输出**，只能发现回归，不能证明"算得对"。产品仓库已补上：5 个聚合函数的单测已合入 `main`，聚合型/明细型计算字段求值、`CalculateRef`、公式工厂的单测在 PR #8000/#8052（144 行矩阵，353 个用例），并查出 10 个新缺陷（见 [vscalc-java-unit-tests.md](vscalc-java-unit-tests.md)）。

G1 已完成：PR #203 直接修改了 `ViewsheetTest`，把 `DcViewsheetTest` 的严格逻辑合进基类，一次覆盖 chart、vsothers 共 31 个 spec，严格即默认，不加开关。dashboard 侧 G2（孤儿 vs `aggregate_trendcomparison`）、G7（含 G9）已完成，剩薄弱场景 G8（暂不做）；Java 单测侧（G3–G6）剩等开发回答的语义（[#80953](https://issues.inetsoft.com/issues/80953)）。

## 2. 覆盖情况

**规模：** 22 个用例对应 22 个 vs，zip 里共 23 个 vs，**`aggregate_trendcomparison` 没有用例也没有基线**。40 个书签（保存选择状态），spec 引用的书签都存在。

**覆盖矩阵**（维度：计算字段类型 × 使用位置 × 数据源，已与用户确认）：

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
| Gauge / Text | 3 / 3 | 类型和表达式单一（仅 integer，表达式均为"其他"） |

统计口径：只算实际绑定，不算 `<ainfo>`。`<ainfo>` 是绑定面板为数据源列出的候选字段，数据源上的计算字段不管有没有绑定都会出现在里面（如 `calc_data group` 的 Chart4，候选 12 个维度、6 个度量，实际只绑定 1 个 x、1 个 y）。`vs_backup_scan.py` 跳过 `<ainfo>`，并按 `rowheaders`/`colheaders` 识别 Crosstab 的行列。

数据源：绑定的表有 Query1（6 个 vs）、OrdersAndReturns（4）、Order Model（4）等，其余基本一对一；底层是 DB2、MySQL 还是 embedded 未细分。
图表类型已有 BAR、BAR_STACK、LINE、PIE、FUNNEL、GANTT、MAP、TREE、NETWORK、SUNBURST、TREEMAP；没有 AREA、POINT、RADAR、STOCK/CANDLE、BOXPLOT、MEKKO、WATERFALL、PARETO、STEP 等。

**验证机制：**

- 比对遍历的是"实际导出的文件"，不是"基线清单"：`CompareUtil.groovy:472`。导出少了文件时没有东西可比，用例照样通过。
- 导出失败只打印堆栈：`ViewsheetTest.groovy:177`（`onExportFailure`）。
- Stateless-Sessions1 上已有严格版基类 `DcViewsheetTest`（`commons/.../modules/DcViewsheetTest.groovy`；`main` 上没有）：导出失败抛异常（`:22`），不给文件名时要求基线文件必须全部被导出（`prepareCompare`），并支持 `-Dupdate.baseline=true`。但使用它的 spec 只有 2 个（`DateComparison_Spec`、`Crosstab_DateComparsion_Spec`）。vscalc 用的是宽松版。严格逻辑已经写好，不用开发新框架，只需要合进基类（第 3 节 G1 方案）。
- `exportData` 没有清理代码，但 CI 都会先清：Jenkins 准备阶段执行 `./mvnw clean -pl vsothers`（`Jenkinsfile:37`），GitHub 工作流先执行 `./mvnw clean`（`test-vsothers.yml:71`）。上一次导出的残留文件不是问题（本地手工重跑除外）。
- CI 对失败的处理：GitHub 工作流 5 个测试步骤全是 `continue-on-error: true`；Jenkins 的 vscalc 阶段是 `catchError(buildResult: 'UNSTABLE')`，失败只让构建变 UNSTABLE。
- 静态预测：22 个用例的基线文件与预期导出文件**完全一致**，所以换严格基类预计不会因文件集合而失败（未运行）。
- 基线来源：回归基线，不是独立标准答案。所有 txt 基线都来自 `c6208b58`（2024-08-21）一次生成，之后再没改过；此后 6 个 "update exp" 提交只改 PNG。2026-03 改用 testcontainer（`d474bfeb`）后 txt 也没变。抽样独立核对未发现错误结果被固化，见下文"核对结果"。

**断言层：** 只有 Chart（TXT + PNG）和 Table、Crosstab、CalcTable（TXT）会导出数据。Selection、Calendar、TimeSlider、Gauge、Text 自身的内容只能靠整页 PNG（22 个用例中 19 个调用了 `exportAsPNG`）。但书签保存了选择状态，它们的**过滤效果**会进入被过滤的图表和表的数据：绑定计算字段的选择类组件 × 书签共 42 对，其中 23 对带有已应用的选择状态，分布在 11 个 vs。
另有 6 个 vs 的个别计算字段名没有出现在基线 txt 里：`parent-child tree` 0/1、`calc_data group` 3/6、`filter` 4/5、`testDB2` 5/6、`testModel` 6/7、`testQuery` 7/8。已逐个核对，真正缺断言的只有 3 个，且都属于 G7（见下文）。

**核对结果（[#80957](https://issues.inetsoft.com/issues/80957) 第 2、3 节）：**

基线抽样独立核对：用 datatest 自带的数据库（`access` 为 H2 `docker/src/test/resources/H2Data/orders.mv.db`，`Examples/Orders` 为 Derby `docker/src/test/resources/libs/orders_133.jar`）按 worksheet/模型定义写 SQL，Python 手算，结果与基线**全部一致**：

| vs | 核对内容 | 结果 |
|---|---|---|
| `aggregate_with` | query1（`orderno < 10` 的 18 行）按年 2001–2003：Correlation、WeightedAverage 与 `Calc_weight`、Sum(Calc_double) 与同比变化、Covariance、`Calc_first`、`Calc_last` | 与 Chart3、Crosstab1、FreehandTable1 一致；Covariance 是总体口径（除以 n） |
| `aggregate_date` | Crosstab1 每个销售员的 `calc_date_max`、`calc_date_min`、`calc_date_count` | max 2020-12、min 2017-03 一致；count 按 Orders（7 列分组）与 Returns（5 列分组）以 City/State/Company/Category 做 FULL JOIN 推算为 769/1035/909/682，与基线一致 |

已固化的错误基线（1 处）：`testWorksheet` 的 FreehandTable1、zoom_FreehandTable1 按 `c_date = CALC.edate(field['Date'], 10)` 的月份分组，数据来自 worksheet `foCalc` 的 snapshot。snapshot 里 2009-04-30 加 10 个月，旧 `edate` 跨年时日期溢出，算成 2010-03-02，基线里因此有 `2010 Mar` 一行；[#80953](https://issues.inetsoft.com/issues/80953) 修复后（stylebi `380cf76378`）结果为 2010-02-28。这张表不显示落在 2 月的值，所以该行消失。新结果正确，两个基线已删去这一行。

G9（上述 8 个字段）：

| vs | 字段 | 用在哪里 | 结论 |
|---|---|---|---|
| `calc_data group` | `calc_date` | Crosstab2 的列维度 | 已断言：表头显示为分组值 2020/2019/2018/Others |
| `calc_data group` | `CalcField2`、`Calc_mindate` | 没有组件绑定 | 不需要断言 |
| `testModel` | `NthSmallest` | 没有组件绑定 | 不需要断言（`BaseFormula` 通过 `TopFormula = Mode([BaseFormula])+10` 间接使用） |
| `testQuery` | `edate` | FreehandTable2 的 5 个日期层级列、FreehandTable3 的 `CalcField` 列 | 已断言 |
| `filter` | `max_time` | SelectionList1 的度量 | **缺断言**，只有 PNG，归 G7 |
| `parent-child tree` | `CalcField1` | SelectionTree1 的度量 | **缺断言**，只有 PNG，归 G7 |
| `testDB2` | `c_date` | RangeSlider2/3，且都是全选（`full="true"`），不过滤数据 | **缺断言**，只有 PNG，归 G7 |

书签选择状态：确实缩小了数据。56 对 (Home)/书签（含 `zoom`）导出中 51 对不同，多数行数变少。5 对相同，原因都正常：`aggregate_nthpth` Chart1 和 `chart` bk1 的 Chart2 是刷选的源图（刷选不过滤源图自己）；`chart` 的 bk2 没有选择状态；`aggregate_time` Crosstab2 绑定 Query2，选择作用在 type 上（推断）；`testWorksheet` FreehandTable1 绑定 snapshot，刷选在 SQL Query1 上。`chart` bk1 的 Chart3 只剩表头也正常：Chart3 本身只有 2019、2020，刷选的是 2018。

donut chart：有意为之。产品的 donut 就是 `chartType=PIE`（3）加 `donut="true"`，再加一个 `Total@` 度量（与产品 `DonutChartFilter.createChartInfo` 生成的结构一致）。基线中 Total 行 909 等于 14 个扇区之和。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 导出失败、基线缺失不报错；一个组件失败后面的组件都不导出；CI 失败不挡合入 | 某个组件导出抛异常或少导出文件，用例仍然通过 | 机制：直接改 `ViewsheetTest`，把 `DcViewsheetTest` 的严格逻辑合进去（见下）。**已完成：PR #203** | 高 |
| G2 | 孤儿 vs `aggregate_trendcomparison` | 趋势/比较类聚合的计算字段结果出错，无人发现 | dashboard：补用例。**已完成**（第 5 节） | 高 |
| G3 | 聚合型计算字段求值只有 1 个值级单测，且表达式只是聚合名本身 | `Sum/Sum`、`Average*Average`、`Mode+10`、`if…else` 这类表达式算错 | 单测。**已落实：PR #8000/#8052（未合入 `main`），查出 #81202、#81203、#81212** | 高 |
| G4 | Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent 在 `report/filter` 无专门单测（已有的同名测试属于 MV 路径，不能替代） | 这些聚合函数实现改错，只有 vs 回归基线可能发现 | 单测。**已完成：[stylebi#7995](https://github.com/inetsoft-technology/stylebi/pull/7995) 已合入 `main`** | 高 |
| G5 | 明细型计算字段的值级语义没有系统测试 | 四则运算、除零、null、拼接、日期、参数替换出错 | 单测 + dashboard 代表用例。**单测已落实：PR #8000/#8052，查出 #81204、#81206、#81214 等；dashboard 代表用例未做** | 中 |
| G6 | 基线只是产品自己的输出 | 已有的错误结果被固化为基线，永远不报错 | 单测的手算期望值（**已做**：144 行矩阵，预言来源以规格、手算为主）；新基线抽样独立核对（未做） | 中 |
| G7 | 选择类、展示类组件自身内容只有 PNG | 选择列表里计算字段的取值列表出错，但在 PNG 容差内 | dashboard：扩展基类。实际受影响的字段：`filter` 的 `max_time`、`parent-child tree` 的 `CalcField1`、`testDB2` 的 `c_date`。**已完成**（第 5 节） | 中 |
| G8 | 薄弱格子：聚合型 timeInstant/boolean、shape/text/size、condition 位置、Gauge/Text 类型、新图表类型 | 计算字段在这些位置取值或渲染出错 | dashboard：新建小 vs | 中 |
| G9 | 上述 6 个 vs 的个别计算字段没有数据断言 | 这些字段的结果出错，数据比对发现不了 | 已核对（第 2 节）：2 个已断言（`calc_date`、`edate`）、3 个没有绑定，真正缺的 3 个都在选择类组件上，并入 G7，不单独补 | 已并入 G7 |

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

**严格基类的运行结果**（PNG 像素差异因渲染环境与 CI 不同，不计入）：

- vscalc：22 个用例，数据比对全部通过，没有导出异常，没有未导出的基线文件；22 个失败全是 PNG 像素差异（文字抗锯齿/字体）。
- vspara：查出 3 个用例。`Para_basequerysql` 导出异常 `Variable in VPM condition missing: Ppid`（`MySQL1` 的 VPM `VPMP1` 要求变量 `Ppid`，spec 未传），以前被吞掉、导出目录为空而假通过；`Para_query`、`nullVSTest2` 是静态预测过的 7 个陈旧基线文件。其余失败为 PNG 像素差异和 `VSExportTest` 的 HTML 比对（与本改动无关）。

**风险与未知：**

- 其他模块会失败多少，静态上算不出来，要各模块自己跑。`vs_backup_scan.py strictcheck` 只适配 vscalc 的写法：没有 `executeVS` 也按 `(Home)` 推算组件文件；`exportAsPNG` 只认第一个参数是书签列表的写法，认不出 `exportAsPNG(null, [...])`；不认识 `exportAsPDF` 生成的 `-0.png` 页图；不分析 `VSCalcTest` 等子类。拿去跑 chart 等 spec 会大量误判，所以不采用。已实际运行的：vscalc 0 个问题，vspara 3 个用例（见上）。
- 改的是 `commons` 的测试代码，chart、vsothers 都依赖它的 test-jar，改完要先重新构建 `commons`，再跑下游模块。
- 光改基类挡不住合入：GitHub 工作流仍是 `continue-on-error`，Jenkins 仍是 UNSTABLE（第 6 节，由你决定）。

## 4. Java 单测补充（产品仓库 `core`）

**已基本落实**，详细结果见 [vscalc-java-unit-tests.md](vscalc-java-unit-tests.md)。

- **G4**（Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent）：已合入 `main`（[stylebi#7995](https://github.com/inetsoft-technology/stylebi/pull/7995)，从 13.8 `epic-test-coverage` 移植）。
- **G3、G5、`CalculateRef`、公式工厂、通用边界**：在 [stylebi#8000](https://github.com/inetsoft-technology/stylebi/pull/8000)（CI 通过，未合入 `main`），加固在 [stylebi#8052](https://github.com/inetsoft-technology/stylebi/pull/8052)。
- 结果：矩阵 144 行，通过 113、不一致 20、没写 10；13 个测试类 353 个用例，0 失败、19 个跳过（`@Disabled` 的不一致用例）；变异检查全部抓到（38 + 5）。
- 开出 10 个 Redmine bug（[#81202](https://issues.inetsoft.com/issues/81202)–[#81206](https://issues.inetsoft.com/issues/81206)、[#81211](https://issues.inetsoft.com/issues/81211)–[#81215](https://issues.inetsoft.com/issues/81215)），关联 [#81200](https://issues.inetsoft.com/issues/81200)、[#81197](https://issues.inetsoft.com/issues/81197)；[#81207](https://issues.inetsoft.com/issues/81207) 判为非缺陷。
- 本文原先设计的 99 个用例、G3/G5 的测试入口分析（`CalcFieldFormula` 的聚合名映射与列位置；明细型的 L1 表达式列 / L2 worksheet / L3 viewsheet 三层入口）已落成测试，设计细节不再保留在这里；注意点留给 datatest 的有两条：
  - 明细型的 15 个 SQL 表达式计算字段：产品单测只能测到"查询不能合并、当脚本执行"的路径；合并后由数据库计算，除零、null 拼接、整数除法的结果可能与脚本路径不同，只能靠真实数据源或 datatest 发现。实测 H2 `NULL+10`/`NULL*2` 都是 null，脚本路径是 10/0；`concat(a,b)` 在脚本作用域里没有绑定，退回脚本大概率报错（未实测）。两条路径不一致待开发确认（[#81223](https://issues.inetsoft.com/issues/81223)）。
  - 明细型除零（`x/0` 得 Infinity）、null 参与运算是 JavaScript 行为，按现状钉住；null 参与运算（`null+10` 得 10、`"a"+null` 得 `"anull"`）是否应传播 null 待开发确认（[#81197](https://issues.inetsoft.com/issues/81197)）。

## 5. Dashboard 补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 验证机制（G1） | 不改 spec，改 `ViewsheetTest`（第 3 节 G1 方案）。就 vscalc 而言，`strictcheck` 静态预测 22 个用例的基线与导出文件集合完全一致（基线多出 0 个、导出缺基线 0 个），所以预计不会因文件集合失败 | 导出失败、基线缺失不再静默通过 | 各模块自行运行并处理变红的用例 |
| 孤儿 vs（G2） | **已完成**：用例 `aggregate_trendcomparison`，书签 `(Home)`、`bk1`，4 个 txt 基线（Chart1、Crosstab1）。只比数据：PNG 基线要等第 6 节"PNG 在哪个平台生成"定了再加 | 趋势/比较聚合的计算字段 | 基线用 `-Dupdate.baseline=true` 生成，按内部关系核对一致：`Calc_agg` 等于 Average(CUSTOMER_ID)；% of total、Value of previous 按图表可见行手算一致；Crosstab 的 Change from previous 与 Moving Average of 5（前后各 2 个，居中窗口）可由同一组 Sum 反推，14 行全部一致 |
| 聚合型 timeInstant、boolean（G8） | 新建小 vs | 目前 0 个 | 在 StyleBI 里制作并导出 backup 并入 zip |
| Chart 的 shape / text / size 视觉绑定（G8） | 新建小 vs | 计算字段用于视觉绑定 | 同上 |
| Table / Crosstab / CalcTable 的 condition 位置（G8） | 新建小 vs | 计算字段用于条件 | 同上 |
| Gauge / Text 非 integer 类型（G8） | 新建小 vs | 计算字段用于 Gauge、Text | 同上 |
| 新图表类型（G8） | 新建小 vs（AREA、POINT、RADAR、WATERFALL 等择要） | 计算字段在这些图表上取值与渲染 | 同上；先确认产品允许这些图表绑定计算字段 |
| 选择类组件自身内容（G7） | **已完成**：`executeVS(params, bks, true)` 额外把 SelectionList / SelectionTree（值、状态、度量）、RangeSlider（值个数、首尾值、选中范围）、Calendar（范围、选中日期）导出为 txt（`ExportUtil.exportSelection`）。默认关闭，其他 spec 不受影响。vscalc 里有选择类组件绑定计算字段的 12 个用例打开，新增 53 个基线 | 选择列表的取值列表、度量等 | 原有 `exportSelectionList` 不含度量，没有复用。抽样核对：`parent-child tree` 的 `CalcField1`（Sum(label_number)）13 个节点与 worksheet 嵌入数据逐个一致；`filter` 的 `max_time` 按自身分组取 Max，等于值本身 |
| 个别计算字段未入基线（G9） | 已核对，并入 G7（第 2 节），随 G7 完成 | | 无 |

做法原则：**新建小 vs，不改已有 vs**（改已有 vs 会让已有基线整体变动，混进噪声）；每个新用例只验证"接线"，边界和语义放在产品单测里（[vscalc-java-unit-tests.md](vscalc-java-unit-tests.md)），不为边界值堆 vs。zip 是整个 storage 的二进制备份，改完后用 `vs_backup_scan.py inventory` 前后对比，确认只有预期的 vs 变了。

## 6. 局限与待确认

**需要开发回答（决定单测期望值怎么写）：** 已汇总到 **[#80953](https://issues.inetsoft.com/issues/80953)**，Redmine 查过的结果：

| # | 问题 | 状态 |
|---|---|---|
| 1 | null 语义：`Count`/`DistinctCount` 忽略 null；`NthMostFrequent`/`Mode` 把 null 当一个值计数 | 前半已确认有意（[#38229](https://issues.inetsoft.com/issues/38229)："it's how sql implements count"；[#35712](https://issues.inetsoft.com/issues/35712)）；Mode 一侧待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 1） |
| 2 | 空集：`DistinctCount` 返回 0 且 `isNull()=false`，其他公式返回 null | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 2），按 [#38229](https://issues.inetsoft.com/issues/38229) 推断 0 是有意的 |
| 3 | 除零与 NaN：`Correlation` 方差为 0 或一对数据（`getResult` 为 null、`getDoubleResult` 为 NaN）；`WeightedAverage` 权重和为 0；聚合型表达式除零 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 3） |
| 4 | `Mode` 并列按值升序 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 4）；13.8 测试方认为"随意"（[#80738](https://issues.inetsoft.com/issues/80738) 附注） |
| 5 | Integer 与 Double 混合时是否为同一值 | 待答（[#80953](https://issues.inetsoft.com/issues/80953) 问题 5） |
| 6 | `CorrelationFormula.addValue(double[])` 的下标约定 | 由已有的 [#80700](https://issues.inetsoft.com/issues/80700) 跟进 |
| 7 | 日期月末语义 | **已修**：[stylebi#7968](https://github.com/inetsoft-technology/stylebi/pull/7968) 之后 `edate`/`dateAdd` 都用 `Calendar.add`，截到月末（原先 2000-12-31 +2 得 2001-03-03 的跨年顺延已不存在；#8000 的测试断言截断值 2001-02-28、2001-03-15） |

**产品单测的状态：** 问题 1–5、3a、3c 开发仍只给倾向、没有决定（Q1 Mode 忽略 null 有兼容性风险；Q2 空 `DistinctCount` 保持 0；Q5 不要钉）；相关 6 行 `hold` 没写，其余按现状钉住并标"pending"，见 [vscalc-java-unit-tests.md](vscalc-java-unit-tests.md) §4、§5。

**单测新发现的缺陷**（10 个，详见 [vscalc-java-unit-tests.md](vscalc-java-unit-tests.md) §3）：[#81202](https://issues.inetsoft.com/issues/81202) 聚合名映射（小写名、`a b`/`a_b` 冲突、`ß`）、[#81203](https://issues.inetsoft.com/issues/81203) tr_TR 的 `toUpperCase/toLowerCase`、[#81204](https://issues.inetsoft.com/issues/81204) `CALC.quarter(null)`（Normal），其余 Low；另 [#81200](https://issues.inetsoft.com/issues/81200)（声明 Integer 被截断）。

13.8 的测试已按现有行为固定了 Mode 计 null、并列取小值、空 Mode 为 null、Correlation 方差为 0 返回 null、WeightedAverage 权重和为 0 返回 null（[#80953](https://issues.inetsoft.com/issues/80953) 问题 1、2 的 Mode 一侧、3a、3b、4，见该单备注）；若开发同意"与 13.8 一致"，只剩计算字段除零（问题 3c）、Integer 与 Double（问题 5）需要答复（日期月末已修）。

开发对边界语义的倾向：实际报表碰不到的边界结果保持现状（[#80488](https://issues.inetsoft.com/issues/80488)、[#80541](https://issues.inetsoft.com/issues/80541) 均以兼容性为由拒绝）。

**疑似缺陷（已运行确认；#80950、#80951、#80952、#80584、#80974 均已在 `main` 修复）：**

| 缺陷 | 结果 | 正常使用能否碰到 | 单号 |
|---|---|---|---|
| `CorrelationFormula` 一次遍历求和，大数值丢精度 | `y = 3x + 7`：x 在 1e7 时 0.99987，1e8 时 0.962，1e9 时为 null（应为 1） | 能，交叉表/图表/汇总的 `Object[]` 路径 | [#80950](https://issues.inetsoft.com/issues/80950)（Normal，**已修**） |
| `CorrelationFormula.addValue(double[])` 读 `v[2]` | 长度 2 的数组抛 `ArrayIndexOutOfBoundsException`；`CalcFieldFormula:157` 传的正是 `{x, y}` | 未找到会调用 `CalcFieldFormula.addValue(double[])` 的路径 | 已有 [#80700](https://issues.inetsoft.com/issues/80700) |
| `NthMostFrequentFormula()` 无参构造 n 为 0 | `getResult` 抛 `IndexOutOfBoundsException: Index -1` | 不能：`CalcTableLens` 只做 `instanceof`，交叉表合并单元格改用 `SumFormula`，目标线界面选不到 | [#80951](https://issues.inetsoft.com/issues/80951)（Low，**已修**） |
| `DistinctCountFormula` 百分比只认 `Integer` 总数 | 总数为 Long/Double/Short 时返回原始计数 | 不能：总数来自同一公式的 `getOriginalResult()`，恒为 Integer | [#80952](https://issues.inetsoft.com/issues/80952)（Low，**已修**）；13.8 的 `countPercentageNeedsIntegerTotal` 把它当作规则，已在单里请开发二选一 |

**需要你决定：**
- 严格模式要不要真正挡住合入：目前 GitHub 工作流 `continue-on-error`、Jenkins 为 UNSTABLE，改基类只是把"静默通过"变成"测试失败"。
- PNG 基线在哪个平台生成，要和 CI 一致。
- G8 里要做哪几个新场景。

**没验证的：**
- 严格模式已跑过 vscalc、vspara 和 chart 的两个 spec（见第 3 节）；其余 spec 由各模块自己运行处理。
- 单测盘点按类名、文件名、方法名静态匹配，没有逐个读测试内容。
- 基线独立核对只抽了 `aggregate_with`、`aggregate_date` 两个 vs；DB2、MySQL 上的 vs（`testDB2`、`testModel`、`testQuery` 等）未核对。
- 只覆盖 vscalc 一个 spec，其他模块未评估。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vscalc-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
```

基线独立核对的取数 SQL（数据库文件先复制出来再连；H2 文件格式 2，用 h2 2.1.214，用户名为空；Derby 用 10.14.2.0，classpath 加 `orders_133.jar`）：

```sql
-- aggregate_with / query1（H2, jdbc:h2:<copy>/orders;ACCESS_MODE_DATA=r）；Calc_double = quantity / customer_id，按 YEAR(orderdate) 分组手算
SELECT od.QUANTITY, o.CUSTOMER_ID, o.ORDERDATE, c.COMPANY FROM ORDERDETAILS od, ORDERS o, CUSTOMERS c
 WHERE od.ORDERNO < 10 AND od.ORDERNO = o.ORDERNO AND o.CUSTOMER_ID = c.CUSTOMER_ID;
-- aggregate_date / OrdersAndReturns（Derby, jdbc:derby:classpath:orders_133;user=SA）
-- Orders 分组：DISTINCT City, State, Company, 年月(ORDER_DATE), LAST_NAME, CATEGORY_NAME（ORDERS-CUSTOMERS-REGIONS-SALES_EMPLOYEES-ORDER_DETAILS-PRODUCTS-CATEGORIES）
-- Returns 分组：DISTINCT City, State, Company, CATEGORY_NAME, 年月(ORDER_DATE)（RETURNS-ORDERS-CUSTOMERS，RETURNS-PRODUCTS-CATEGORIES）
-- calc_date_count(销售员) = Σ 该销售员的 Orders 分组 × max(1, 同 City/State/Company/Category 的 Returns 分组数)
```
