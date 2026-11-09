# 测试质量升级讨论记录（2026-11-09）

> 状态：讨论进行中。本文记录已确认的事实、推测、待讨论项和 TODO；**结论汇总见第 16 节**，单元测试用例清单见 `02-unit-test-case-list.md`（草案，99 个用例，未运行验证）。
> 分支：Stateless-Sessions1。已有文档 `AI_UPGRADE_PLAN.md`、`GITHUB_UPGRADE_PLAN.md` 与本文互补，本文聚焦"测试质量"。

## 1. 目标与核心问题

用户希望：**测试点更全、测得更准**。没有具体的漏测或误报案例，核心担忧有两点：

1. **dashboard（viewsheet）创建质量、测试点质量不清楚。** 大量用例依赖预先创建的 dashboard，但无法回答"这个 dashboard 好不好"。
2. **依赖 PNG 导出做比对，image 比较不稳定。**

## 2. 摸底方法（分三层，成本从低到高）

| 层 | 做法 | 是否需要启动环境 | 回答的问题 |
|---|---|---|---|
| 一 静态解析 | 直接解析 backup zip 里的 viewsheet XML，统计组件、绑定、格式、高亮等，并与 spec 对照 | 否 | dashboard 配置了什么，哪些特性没覆盖 |
| 二 无界面运行 | 借用 `RuntimeViewsheetResource` 在代码里打开 vs | 是（需 restore 和数据源，如 DB2、MySQL） | 能不能跑通，数据是不是空 |
| 三 浏览器打开 | 只抽样核对 | 是 | 核对前两层的疑点 |

建议顺序：先做第一层，第二层视环境而定，第三层只做核对。

## 3. 数据来源：backup zip 的结构（已验证）

以 `vsothers/src/test/resources/config/vscalc-storage.zip` 为样本（只读，未改动）：

- 它是 StyleBI 的 blob 存储：`key-value-index.json`、`blob-index.json` 加上 `blobs/xx/<digest>`。
- `blob-index.json` 把逻辑路径映射到摘要。blob 是**明文 XML**，可直接解析，不需要 restore。
- 键 `1^128^__NULL__^vs-calcfield/<name>^host-org` 是 viewsheet（类型 128）。
- 键 `4^16384^admin~;~host-org^…` 是 **VSBookmark（书签）存储**，**不是 autosave**（先前写成 autosave 是错的，2026-11-09 更正）。书签值是十六进制编码的 viewsheet 状态 XML，保存了选择状态等。统计 viewsheet 时要排除它们，分析选择类组件时要读它们（见 12.4a）。
- 另有 ws、query、数据模型（DB2、Examples/Orders）等资产。

zip 内 23 个 viewsheet 的静态统计：

- 组件总数：Chart 34、Crosstab 16、TimeSlider 15、CalcTable 13、SelectionList 11、CurrentSelection 9、Table 7、SelectionTree 6、Calendar 6、Text 5、Gauge 4、RadioButton 2、CheckBox 1。
- **没有任何脚本**（`<script` 为 0）。
- 23 个 vs 里只有 8 个有 highlight。
- 每个 vs 的组件数从 3 到 14 不等。

## 4. spec 与 vs 的对应（vscalc 模块）

- `VS_CalcField_Spec.groovy`（405 行）共 22 个用例，一个用例对应一个 vs。
- zip 里有 23 个 vs，**`aggregate_trendcomparison` 没有用例，也没有基线**（孤儿 vs）。
- 用例结构几乎相同：`executeVS(bookmarks)` →（可选）`exportAsPNG` → `compareData` + `compareImage`。
- 用例意图只写在注释里（如 "check calc field used in filter assembly"），没有体现在断言里。
- 基线位置：`vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec/<case>/`。

## 5. 比对机制（已读源码）

对前一版猜测的纠正：**不是只靠 PNG，每个用例同时做 TXT 数据比对和 PNG 图片比对。**

- `ViewsheetTest.executeVS0`：每个书签执行一次，遍历 vs 的组件。
  - Chart：导出数据集为 `.txt`（经 `sortVSDataSet` 排序），并用 `pair.getImage(true, 72)` 在**进程内**渲染 `.png`，不是浏览器截图。
  - Table、Crosstab、CalcTable：导出 `TableLens` 为 `.txt`。
  - 跳过：不可见组件、GroupContainer、Tab、CurrentSelection、Shape。
- `CompareUtil.PNGCompare`：用 image-comparison 库，默认允许差异像素 0.001%，`compareImageWithAllowedDiff` 可用 0.01%。
- 两种异常都会失败：尺寸不一致、像素不同，会生成 `_diff.png`。

## 6. 已确认的缺口（有代码依据）

| # | 缺口 | 位置 | 说明 |
|---|---|---|---|
| G1 | 比对是遍历"实际导出文件"，不是遍历"基线文件" | `CompareUtil.groovy:472` | 导出少了文件时没有东西可比，用例空转通过 |
| G2 | 导出失败被吞掉 | `ViewsheetTest.groovy:177` | `onExportFailure` 默认只打印堆栈 |
| G3 | 孤儿 vs | 见第 4 节 | `aggregate_trendcomparison` 不会被测试 |
| G4 | 格式不匹配的文件被静默忽略 | `CompareUtil.groovy:480-490` | `processFile` 返回 null，多出来的文件不报错 |
| G5 | 用例类型单一 | 见第 4 节 | 只有整体导出后比对，没有单点断言、参数组合、边界和空数据 |

## 7. 已有的修复：`DcViewsheetTest`（后续发现）

`commons/src/test/groovy/inetsoft/test/modules/DcViewsheetTest.groovy` 继承 `ViewsheetTest`，**已经修了 G1 和 G2**（提交 `7fc5da26`，#189，日期对比用例用）：

- 导出失败直接抛异常，让用例失败（修 G2）。
- 没有指定文件名时，基线目录里的每个文件都必须被本次运行导出，否则抛 `AssertionError`（修 G1）。
- 提供 `-Dupdate.baseline=true`：把导出文件复制到 `src/test/resources/expectData`，而不是比对；PNG 默认只写缺失的基线，要覆盖需 `-Dupdate.baseline.png=true`（因为不同平台渲染不同）。
- 修正了 `exportAsPDF` 默认书签 `Home` 已不存在（现为 `(Home)`），导致 PDF 无页面的问题。

**使用范围**（`new ...ViewsheetTest(` 调用次数，来自 grep 计数，是近似值）：

- 全部约 486 次、分布在 26 个 spec 文件。
- 使用严格版 `DcViewsheetTest` 的只有 2 个文件：`DateComparison_Spec`（chart，约 48 次）、`Crosstab_DateComparsion_Spec`（vsothers，约 28 次），约占 16%。
- 其余约 84%（包括 `VS_CalcField_Spec`）仍用宽松的 `ViewsheetTest`，G1、G2 仍然存在。

## 8. `exportData` 与清理（部分验证）

- 导出目录是 `<module>/target/test-classes/exportData/<suite>/<case>/`，基线在 `expectData`（`src/test/resources`）。
- 各模块 pom 都有 `<exclude>exportData/**</exclude>`（打包时排除）。
- 我没有在 `commons/src` 里找到任何清理 `exportData` 的代码。清理只能依赖 `mvn clean` 或全新的工作区。
- Jenkins 和 GitHub workflow 跑的是 `./mvnw test -pl vsothers -Pvscalc`，**命令里没有 `clean`**。
- **结论（推测）：** 如果 CI 工作区会复用，`target/` 下上一次的导出会残留，可能掩盖"本次没导出"的情况。这条对 `DcViewsheetTest` 也成立，因为它也是检查 `exportDir` 里文件是否存在。需要确认 Jenkins 是否每次全新检出。

## 9. 尚未验证（推测，不是结论）

- **基线如何产生。** 如果是用产品跑一遍后存下来，则只能发现回归，不能证明正确。需要看 git 历史，本次按要求暂缓。
- Jenkins 工作区是否每次全新，`target/` 是否会残留。
- 文本比较（`memoryCompare`、`streamCompare`）有没有排序、浮点容差、时间字段的处理。
- 以上结论只覆盖 vscalc 的一个 spec 加 `ViewsheetTest` 基类，其余模块的断言细节没有逐个看过，不能外推。

## 10. 初步方向（待讨论，未决定）

G1、G2 已有现成修复，所以重点不是"重新设计"，而是**推广**：

1. 把 `DcViewsheetTest` 的严格行为推广到其余约 84% 的用例（升级基类，或让各 spec 改用）。需要评估：推广后会暴露多少此前空转通过的用例，因此要分批、并先在本地跑一遍。
2. 做 **vs ↔ spec ↔ 基线** 的一致性检查，自动发现孤儿 vs（如 `aggregate_trendcomparison`）。
3. 用静态解析产出"特性覆盖矩阵"，回答问题 1。
4. 第二层（无界面运行）视环境可用性再定。
5. AI 视觉比对降级：PNG 是进程内渲染，且与 TXT 数据比对并存，PNG 不稳定的实际范围需要先量化，再决定要不要投入。

## 11. 待讨论项

- [x] G1、G2 的现状（已有 `DcViewsheetTest`，覆盖约 16%）
- [x] `exportData` 清理机制（无代码清理，靠 `mvn clean`）
- [ ] Jenkins 工作区是否全新
- [ ] 文本比较的细节
- [ ] 严格模式推广的方式和批次
- [ ] 其他模块是否有相同模式（chart、vsexport、vsothers 的其他 spec）
- [ ] 覆盖矩阵的维度怎么定
- [ ] PNG 不稳定的实际范围（需要历史失败数据）
- [ ] 基线来源（需要查 git 历史，暂缓）

## 12. vscalc 覆盖矩阵分析（三个维度，静态，不含交互）

范围：只看 `vscalc-storage.zip` 的 23 个 vs、98 个计算字段定义。方法：解析 vs 级的 `allcalc`（定义）+ 按名字扫描各组件里的引用。**这是第①级（配置覆盖）的结果**；第②级（是否被断言）只做了粗略对照。交互方式（筛选、参数、钻取、书签）按约定暂不考虑。

### 12.1 维度 A：计算字段类型（2026-11-09 更正）

> **更正：** 本节先前版本把 `baseOnDetail` 的含义标反，并据此得出"基于明细的聚合计算字段覆盖为 0"，该结论**已撤回**。
> 依据产品源码（`stylebi-main`，`CalculateRef.java`）：默认 `baseOnDetail = true`；序列化时**只在值为 false 时才写出该属性**（约 112 行）；解析时 `val == null || !"false".equals(val)`（约 140 行）。
> 因此：**XML 中没有该属性 = 基于明细（逐行计算）；`baseOnDetail="false"` = 对聚合结果再计算（AGG_CALC）。**
> 注意源码分支是 `test-cluster-p1-52-second-rename`，与测试实际使用的版本可能不同，但这个语义是基础设计，预期稳定。

| 种类 | date | string | double | integer | time | timeInstant | boolean | 合计 |
|---|---|---|---|---|---|---|---|---|
| 基于明细（逐行） | 13 | 18 | 9 | 11 | 2 | 4 | 1 | 58 |
| 基于聚合结果 | 6 | 3 | 25 | 5 | 1 | 0 | 0 | 40 |

两类都有覆盖。聚合型里没有 timeInstant 和 boolean，string 只有 3 个、integer 只有 5 个。

表达式不是只有 `field[...]` 或 `Sum()`，实际是脚本表达式，已出现的形式：

- 明细型（50 种不同表达式）：直接取列 `field['x']`；字段间四则运算 `field['a']/field['b']`、`field['a']*field['b']`；字符串拼接 `field['a']+"_"+field['b']`、`concat(...)`；日期函数 `CALC.quarter(...)*2`、`CALC.edate(...,5)`、`dateAdd('m',5,...)`；引用参数 `parameter.RadioButton1`、`parameter.CheckBox1[0]`；引用另一个计算字段 `field['Calc_double']`。
- 聚合型（36 种不同表达式）：单个聚合 `field['Sum([x])']`；带参数的聚合 `NthLargest/NthSmallest/NthMostFrequent/PthPercentile(x, n)`、`First/Last(x, y)`、`WeightedAverage(x, w)`、`Correlation(x, y)`；多个聚合做四则运算；聚合加常数 `Mode(...)+10`；条件分支 `if (field['Count([Company])'] > 1500) {...} else {...}`。
- 尚未出现：明细型里的条件分支、数值函数（如 Math.round）、字符串截取等；聚合型里的 timeInstant、boolean 结果。这些是"本批 vs 里没有"，**不等于产品不支持**，产品支持的完整范围需要另行核实。

### 12.2 维度 B：使用位置（覆盖的 vs 个数 / 23）

| 组件:位置 | vs 数 | 观察 |
|---|---|---|
| Chart:x / y | 14 / 16 | 充分 |
| Chart:bound-dim / bound-aggr | 18 / 19 | 充分 |
| Chart:visual:color / size / text / **shape** | 7 / 4 / 4 / **2** | shape 很薄 |
| Chart:axis-format | 13 | |
| Crosstab:dim / aggr / condition | 12 / 12 / 3 | condition 薄 |
| CalcTable:cell-binding / aggr / dim | 9 / 9 / 8 | |
| CalcTable:condition | 1 | 很薄 |
| Table:column / condition | 6 / 2 | condition 薄 |
| Calendar | 6 | |
| SelectionList | 6（另有 title-text 1） | |
| SelectionTree | 4 + 2 | |
| TimeSlider | 3（另有 title-text 2） | |
| Gauge | 3，且全部是 integer + 表达式类别"其他" | 单一 |
| Text | 3 | |

- 没有任何计算字段被引用到 RadioButton、CheckBox。
- 本批 vs **没有任何脚本**，所以"脚本中引用计算字段"没有覆盖。
- "定义了但没有任何组件引用"的计算字段：0 个。
- 候选但本次无法判断的位置（需读产品源码确认能不能绑定计算字段）：排序、TopN、超链接、高亮、趋势线、目标线等。

### 12.3 维度 C：数据源（只到"绑定的表名"这一层）

| 绑定的表 | vs |
|---|---|
| Query1 | aggregate_nthpth、aggregate_trendcomparison（孤儿）、gantt chart、parent-child tree、tree chart、treemap chart（及 aggregate_with 的 query1） |
| OrdersAndReturns | aggregate_date、calc field1、calc_data group、chart |
| Order Model | aggregate_discrete、aggregate_distinct count、calc_drillfilter、funnel chart |
| type / Query2 | filter、aggregate_time |
| VsDataModel / VsDataQuery | testDB2、testModel / testQuery |
| customers / CUSTOMERS | parameter in calc field / donut chart |
| SQL Query1 + snapshot | testWorksheet |

所有 vs 的 `source type` 都是 7，因此无法区分底层是 DB2、MySQL、embedded 还是 snapshot。要分类，需要继续解析 ws 资产（`1^2^…^ws`）。

### 12.4 第②级：是否被断言（关键发现）

`ViewsheetTest.executeVS0` 只导出两类组件：**Chart**（TXT + PNG）和 **TableData**（Table、Crosstab、CalcTable，TXT）。以下组件不会导出任何数据：

> Calendar、SelectionList、SelectionTree、TimeSlider、Gauge、Text、RadioButton、CheckBox（以及 CurrentSelection、Shape、Tab、Group）

结果：这些组件**自身的内容**（选择列表列出的值、范围条、仪表盘读数、文本）**没有数据层断言**，只能靠整页 PNG（`exportAsPNG`）像素比对来覆盖。**更正（见 12.4a）：** 书签保存了这些组件的选择状态，它们对图表和表的**过滤效果**会进入被过滤组件的导出数据，所以先前说成"完全没有数据断言"太绝对。VS_CalcField_Spec 中 22 个用例有 19 个调用了 `exportAsPNG`；没有调用的 3 个是 `chart`、`calc_drillfilter`、`aggregate_distinct count`，它们都不含上述组件，不受影响。

这与第 6 节的发现互相印证：`parent-child tree`（SelectionTree + Table）的 1 个计算字段在基线 txt 中出现 0 次。

### 12.4a 书签：选择状态如何进入导出数据（2026-11-09 补充，更正 12.4）

**事实（已验证）：**

- zip 里有 19 个书签存储、共 40 个书签，每个 vs 通常有 `(Home)`（隐式，不存储）和 `bk1` 等。`VS_CalcField_Spec` 里 `executeVS(null, ['(Home)','bk1'])` 引用的书签在 zip 里**都存在**（用 `strictcheck` 核对，无缺失）。
- 书签值是十六进制编码的 viewsheet 状态 XML。选择状态存在：`SelectionValue` 的 `state` 位（产品源码 `SelectionValue.java:40-52`：`SELECTED=1`、`INCLUDED=2`、`EXCLUDED=4`、`COMPATIBLE=8`，所以 `state=9` 是已选中且兼容）、Calendar 的 `state_dates`（如 `m2019-1` 到 `m2020-9`）、RadioButton 的 `state_selectedObject`（如 `Month`）。
- 绑定了计算字段的选择类组件（含 Calendar）× 书签共 42 对，其中 **23 对带有已应用的选择状态**（有选中值或日历范围），分布在 11 个 vs（脚本输出，可复现）。例如 `calc field1` 的 SelectionList、SelectionTree，`donut chart`、`filter`、`funnel chart`、`gantt chart`、`parameter in calc field`、`parent-child tree`，以及 `aggregate_date`、`aggregate_discrete`、`aggregate_with`、`treemap chart` 的 Calendar。

**结论：** 选择类组件的**过滤效果**会通过书签进入图表和表的导出数据，是被数据断言间接覆盖的。没有数据断言的是这些组件**自身的内容**（列出的值、范围条、读数、文本）。

**无法静态判断的：** 一个选择是否真的缩小了数据范围。书签可能只保存被选中的项，所以"全选"和"只存了选中项"无法区分；树的父节点状态也干扰计数。我按"选中数小于总数"粗算只有 6 个像是缩小了范围，但这个数字不可靠，**不要引用**。需要运行或读书签的过滤条件才能确认。

**严格模式的静态预测（`evidence/strictcheck-VS_CalcField_Spec.md`）：** 按导出命名规则（`[书签名_]<组件名>.txt/png`，Home 书签不加前缀；整页 PNG 用 vs 名）预测的导出文件，与 22 个用例的基线文件**完全一致**（0 个多余、0 个缺失）。这说明：① 命名规则理解正确；② 切换到 `DcViewsheetTest` 后，从文件集合角度预计不会暴露失败；③ 严格模式在 vscalc 上是"保险"，不是"修复已有失败"。局限：预测不含运行时异常（导出异常目前被吞掉，这正是严格模式要抓的），也不含可见性脚本。

### 12.5 本轮结论

1. （已撤回）原结论"基于明细的聚合计算字段覆盖为 0"是 `baseOnDetail` 语义标反造成的，实际明细型 58 个、聚合型 40 个，均有覆盖。真正偏薄的是：聚合型缺 timeInstant 和 boolean、明细型缺条件分支表达式。
2. **选择类和展示类组件（Selection、Calendar、TimeSlider、Gauge、Text）自身的内容没有数据断言，只有像素比对。** 它们的过滤效果经书签进入图表和表的导出数据，但是否真的缩小了数据范围静态上无法判断（12.4a）。这仍是有价值的缺口，也和"PNG 不稳定"相关：这些组件自身的显示靠 PNG 兜底。
3. 薄弱格子：Chart visual:shape、CalcTable/Table/Crosstab 的 condition、boolean 和 time 类型、Gauge 的类型和表达式单一。
4. 本节统计的局限：位置识别按 XML 层级粗分，`bound`、`other` 含义宽；名字匹配可能有少量误差；只覆盖 vscalc 一个 spec，其他模块未做。

## 13. 分层决定：逻辑下沉到 Java 单元测试，放产品仓库

**决定（用户 2026-11-09）：** 单元测试放入**产品仓库**，由开发评审合入。

分工：

- **单元层（产品仓库，JUnit）**：证明"算得对"。用几行手算数据 + 独立算出的期望值，覆盖聚合函数、表达式求值、数据类型矩阵、序列化往返。这能解决第 7 节提到的"基线只是产品自己的输出，不能证明正确"的问题。
- **datatest（本仓库）**：证明"接得对"。计算字段在各组件、各绑定位置是否正确取值和渲染，以及不同数据源。每个格子一个代表性用例即可，不必为边界值堆 vs。
- 风险：单元测试验证的是函数，不一定走和 vs 运行时相同的路径，所以每种计算字段类型仍需至少一个端到端用例。

## 14. Java 单元测试覆盖分析（产品仓库）

**被分析代码：** `E:\inetsoft\sr14_0\stylebi-main\community\core`，分支 `test-cluster-p1-52-second-rename`。这个分支和 datatest 实际运行的产品版本可能不同。
**测试规模：** `core/src/test` 下 2172 个测试文件，其中 `report/filter` 下 62 个。
**方法与局限：** 按类名/文件名/测试方法名做静态匹配，**没有逐个读测试内容**。方法名关键词只是粗略代理，不等于质量。

### 14.1 计算字段的构成（单元测试要对应的代码）

| 层 | 代码 |
|---|---|
| 聚合函数枚举 | `uql/asset/AggregateFormula`（26 个值） |
| 聚合函数实现 | `report/filter/*Formula`（30 个类）；MV 版本 `mv/formula/*Formula`（15 个类） |
| 聚合型计算字段 | `report/filter/CalcFieldFormula`（表达式作用于聚合结果） |
| 明细型计算字段 | `uql/erm/ExpressionRef` + `CalculateRef`，在 `AssetQuery` 里按行求值（脚本） |
| `CALC.*` 日期函数 | `util/script/CalcDateTime`（44 个公开方法） |
| 其他脚本函数 | `report/script/formula/FormulaFunctions`、`util/script/CalcStat` |

### 14.2 聚合函数：哪些没有专门测试

`report/filter` 版本**没有专门测试**的类（8 个）：

| 类 | 备注 |
|---|---|
| Correlation | 只有 MV 版 `CompositeCorrelationFormulaTest` |
| Covariance | 只有 MV 版 `CompositeCovarianceFormulaTest` |
| PopulationStandardDeviation | 只有 MV 版和 DB2 SQL 相关测试 |
| WeightedAverage | 只在 `CalcStatTest`、`CalcTableScopeTest` 里被间接提到 |
| DistinctCount | 同名测试 `DistinctCountFormulaTest` **属于 `mv/formula`** |
| Mode | 同上，属于 `mv/formula` |
| Median | 同上，属于 `mv/formula` |
| NthMostFrequent | 同上，属于 `mv/formula` |

（另有 `Percentage` 是接口，不计。）

**更正说明：** 分析中途发现 DistinctCount、Mode、Median、NthMostFrequent 四个同名类同时存在于 `report/filter` 和 `mv/formula`，按文件名匹配会把 MV 版本的测试算到 `report/filter` 头上；已通过路径核实更正。

**vscalc 正在使用、却没有 `report/filter` 级专门测试的聚合函数（5 个）：** Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent。

**未验证：** vs 运行时到底走 `report/filter` 版本还是 `mv/formula` 版本；`CalcTableScopeTest`（含 testCountAndDistinct、testMedian、testMode）是否能算作间接覆盖。

有专门测试的类（Sum、Count、Max、Min、Average、First、Last、NthLargest、NthSmallest、PthPercentile、Product、Variance、StandardDeviation、PopulationVariance、Concat、None、Null、Default、CubeMeasure、CalcField）每个有 24 到 39 个测试方法，数量充足。

### 14.3 已有测试的边界场景（按测试方法名粗查）

| 场景 | 结果 |
|---|---|
| null | 覆盖充分（多数公式 9 到 15 个测试名含 null） |
| 空集 | 有（3 到 5 个） |
| 单值 | 有，但较少（Sum、Average 7 个，其余 1 到 3 个） |
| **并列（tie）** | **几乎没有**：只有 NthLargest、NthSmallest 各 1 个；NthMostFrequent、Mode 并列场景需要重点确认 |
| 负数 | 很少（Sum、Max、Min、NthLargest 各 1 个） |
| NaN / Infinity | **没有** |
| 数据类型 | Sum、Average、Product、Concat 较多（6 到 12 个），First/Last/StandardDeviation 只有 2 个 |

### 14.4 聚合型计算字段（`CalcFieldFormula`）

`CalcFieldFormulaTest` 有 33 个测试，但按方法名看：构造校验、null、克隆、getter/setter、重置占了绝大部分。**只有 1 个验证表达式求值结果**，即 `getResult_withSumChildFormula_scriptEvaluatesCorrectly`，而且表达式只是聚合名本身（`"SUM"`），两个值 3 和 7 求和得 10，**没有算术、多个聚合组合、条件分支**。

这正是 vscalc 里用到的形式：`Sum/Sum`、`Average*Average`、`Correlation+Correlation`、`Mode+10`、`if (Count > 1500) {...} else {...}`。

### 14.5 明细型计算字段（`ExpressionRef` 求值）

44 个测试文件提到 `ExpressionRef`/`CalculateRef`，其中 26 个有名字含 calc/expression/script 的测试。从方法名看，这些大多是**缺陷驱动的结构性测试**：

- 并发、锁、缓存：`VSAQueryCalcFieldConcurrencyTest`、`EmbeddedTableScriptLockCycleTest`、`ViewsheetResetWSBoundTableTest`
- 序列化往返：`ScriptCdataRoundTripTest`
- 脚本语法与停止：`LexicalDeclarationScriptTest`、`WorksheetScriptStopTest`
- 服务操作：`ModifyCalculateFieldServiceTest`（重命名、删除、改类型；含 `baseOnDetail`）
- 查询模式：`CrosstabCalcFieldQueryViewTest`

**没有看到系统性的值级测试**：按行计算的四则运算（含除零）、字符串拼接（含 null）、`parameter.X` 替换、声明的 dataType 与结果不一致、引用另一个计算字段。需要抽样读几个（如 `EndUserScriptJavaAccessTest.calcFieldStillEvaluatesNormalScript`、`EmbeddedTableScriptLockCycleTest.formulaReads...`）确认，这是"没有看到"，不是"确认没有"。

### 14.6 日期函数：覆盖较好（对前一轮搜索偏差的更正）

- `CalcDateTime` 有 44 个公开方法，`CalcDateTimeTest` 有 41 个测试，`edate`、`quarter`、`year`、`day`、`month` 等都有对应测试（`edate` 只有 1 个，`eomonth` 1 个）。
- `dateAdd` 在 6 个测试文件里被使用。
- 之前我按字面量 `CALC.edate` 搜索得到"0 次"，那是因为测试直接调用 `CalcDateTime` 方法，**不是缺口**，此结论已撤回。
- 零测试的方法名：`compare`、`maxDate`（`minDate` 有）、`monthname`、`weekdayname`。vscalc 未用到这些。

### 14.7 本节结论与优先级

| 优先级 | 缺口 | 理由 |
|---|---|---|
| 高 | 聚合型计算字段的表达式求值（多聚合组合、算术、条件分支、对 null/0 的处理） | vscalc 大量使用；现有 33 个测试只有 1 个值级用例且表达式极简 |
| 高 | Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent 的 `report/filter` 版本 | vscalc 在用，无专门测试 |
| 高 | 明细型计算字段的值级语义（算术/拼接/null/参数/类型） | 现有测试偏结构性 |
| 中 | 并列、负数、NaN/Infinity 等边界场景 | 在已有公式测试里几乎缺失 |
| 中 | boolean/time/timeInstant 作为计算字段结果类型 | vs 里本身很薄 |
| 低 | 日期函数 | 覆盖较好，仅 `edate`、`eomonth` 偏薄 |

### 14.8 待办与待确认

- [x] 确认 vs 运行时走 `report/filter` 版本的公式（见 14.9，已确认）
- [ ] 抽样读 `EndUserScriptJavaAccessTest`、`EmbeddedTableScriptLockCycleTest` 等，确认明细型是否真缺值级测试
- [ ] 读 `CalcTableScopeTest`（testCountAndDistinct、testMedian、testMode），判断能否算间接覆盖
- [ ] 核对 datatest 实际使用的产品版本与分析所用分支的差异
- [ ] 产品仓库单元测试的编写规范（框架、命名、目录），确认后再设计用例

### 14.9 已确认：vs 运行时走哪个版本的公式（2026-11-09）

**结论：普通 vs 运行（不使用 MV）走 `inetsoft.report.filter.*Formula`，不走 `inetsoft.mv.formula.*`。** 因此 14.2 中"`report/filter` 版本没有专门测试"的缺口是真实缺口，不是误判；MV 版本的测试不能替代它们。

依据（均来自产品源码，`community/core/src/main/java/inetsoft`）：

1. `report/internal/Util.createFormula(table, name)`（约 1076 行）：普通名字（如 `Correlation`、`Mode`、`DistinctCount`、`NthMostFrequent`、`WeightedAverage`）一律拼成 `inetsoft.report.filter.<Name>Formula`；只有以 `Sum` 开头且以 `Formula` 结尾的名字（`SumSQ`、`SumWT`、`Sum2`）才走 `inetsoft.mv.formula.`。
2. 各条运行时路径都调用 `Util.createFormula`：
   - Crosstab：`AbstractCrosstabVSAQuery.java:2137`
   - 查询聚合：`AssetQuery.java:3780`
   - Chart 相关：`GraphUtil.java:1194`（以及 `MovingCalc`、`RunningTotalCalc`）
   - 聚合型计算字段：`AssetQuery.java:3779` 起，直接 `new CalcFieldFormula(expression, names, forms, cols, env, scope)`，其子聚合 `forms` 同样来自这条路径。
3. `mv.formula` 的 `Composite*Formula`（Average、Covariance、Variance、PopulationVariance、PopulationStandardDeviation、StandardDeviation、Correlation）只通过 `CompositeAggregateRef.getCompositeFormula` 创建，而 `new CompositeAggregateRef` 只出现在两个 MV 转换器里：`mv/trans/NamedGroupTransformer.java:391` 和 `mv/trans/SelectionUpTransformer.java:1135`。也就是说只有物化视图改写时才会用。
4. `mv.formula` 的其余类（DistinctCount、Median、Mode、NthMostFrequent）被 `mv/data/GroupedTableBlock`、`mv/data/MVRow` 引用，也是 MV 数据路径。
5. vscalc 的 zip 里没有任何 MV 相关条目（blob 和 kv 里都没有）。datatest 里 MV 是单独的 `mv` 模块（`wsmv` 等 profile）在测。

**影响：** 单元测试应针对 `report/filter` 版本编写；对应的 MV 版本已有测试，但那是另一条路径。

**两点保留：**

- Chart 主聚合路径我只确认到：`ChartVSAQuery` 构造 `AggregateInfo` 并通过 `VSAggregateRef.createAggregateRef` 生成聚合表，交给 `AssetQuery` 执行（`ChartVSAQuery.java:754` 的注释提到 `AssetQuery.createAssetQuery -> MVTransformer.transform`，即 MV 转换只在命中 MV 时发生）。我**没有逐行追踪 chart 的所有分支**。
- `GraphUtil` 里有一个回退：对 correlation、covariance、weightedAvg，如果找不到第二列，会退化成 `AverageFormula`，用于"对聚合值再排序分组"。这是一个特殊路径，单元测试要不要覆盖需要另行判断。
- 分析所用分支是 `test-cluster-p1-52-second-rename`，datatest 实际用的产品版本可能不同；这条分发逻辑是基础设计，预期稳定，但仍建议在目标版本上复核。

## 15. TODO 清单（2026-11-09 记录）

### 15.1 已决定

- [x] 逻辑下沉到 Java 单元测试，**放入产品仓库**（第 13 节）
- [x] 单元测试针对 `report/filter` 版本的公式（第 14.9 节已确认）
- [x] 生成单元测试用例清单：见 `02-unit-test-case-list.md`（草案，共 99 个用例）

### 15.2 单元测试（产品仓库）

- [ ] 与开发确认用例清单中的 7 个语义问题（见清单第 3 节），其中 null 语义、空集语义、除零与 NaN 影响最大
- [ ] 确认产品仓库单元测试的规范：框架、命名、目录、`@Tag` 约定
- [ ] 第一阶段：聚合型计算字段求值（A 组）、WeightedAverage（D 组）、Correlation（E 组）、工厂一致性（I 组），41 个用例
- [ ] 第二阶段：NthMostFrequent/Mode（B 组）、DistinctCount（C 组），26 个用例
- [ ] 第三阶段（F、G 组，共 24 个用例）前置：读 `EndUserScriptJavaAccessTest`、`EmbeddedTableScriptLockCycleTest` 确认明细型计算字段的测试入口，再做 F、G 组
- [ ] 第四阶段：已有公式的通用边界补充（J 组，8 个用例）
- [ ] 运行验证清单中带 ⚠️ 的 8 个疑似缺陷（B6b、B10、C9、E4、E8、E9、I3、J7），确认是真缺陷还是读错
- [ ] 抽样读 `CalcTableScopeTest`（testCountAndDistinct、testMedian、testMode），判断能否算对这几个公式的间接覆盖
- [ ] 在 datatest 实际使用的产品版本上复核第 14.9 节的结论

### 15.3 datatest（本仓库）

- [ ] 把 `DcViewsheetTest` 的严格行为（导出失败即失败、基线文件必须被导出）推广到其余约 84% 的用例，先本地跑一遍统计暴露的失败数量，再分批合入
- [ ] 确认 Jenkins 工作区是否每次全新，`target/` 残留是否会掩盖导出缺失
- [ ] 为 `aggregate_trendcomparison` 补用例，或确认它是否应删除
- [ ] 选择类与展示类组件（Selection、Calendar、TimeSlider、Gauge、Text）上的计算字段：生成具体清单，评估补数据断言还是继续依赖 PNG
- [ ] 补薄弱格子：Chart 的 shape 视觉绑定、各类 condition 位置、boolean/time/timeInstant 类型的聚合型计算字段、明细型里的条件分支表达式
- [ ] 把静态解析脚本整理成可重复运行的检查工具：孤儿 vs、vs 与 spec 与基线一致性、覆盖矩阵
- [ ] 解析 ws 资产，把数据源维度从"表名"细化到底层类型（DB2、MySQL、embedded、snapshot）
- [ ] 其余模块（chart、vsexport、vsothers 其他 spec）是否存在同样模式

### 15.4 暂缓

- [ ] 查 git 历史确认基线如何产生（回归基线还是独立标准答案）——用户要求暂缓
- [ ] AI 视觉比对——等量化 PNG 不稳定的实际范围后再决定
- [ ] 第二层无界面运行验证——视环境可用性
- [ ] 交互相关覆盖——datatest 是静态的，暂不考虑

## 16. 结论汇总（截至 2026-11-09）

### 16.1 要解决的两个问题，目前的回答

| 问题 | 目前的结论 |
|---|---|
| 1. dashboard 创建质量、测试点质量不清楚 | 能做三级判断：①配置到了吗（静态解析 vs XML，可自动）；②断言到了吗（对照基线，大部分可自动）；③断言对吗（现有基线只是产品自己的输出，**不能证明正确**，需要独立的标准答案，即单元测试的手算期望值）。vscalc 的静态矩阵见第 12 节 |
| 2. 依赖 PNG，image 比较不稳定 | 范围比预期小：每个用例同时做 TXT 数据比对和 PNG 比对，chart 的 PNG 是**进程内渲染**，不是浏览器截图。这些组件（Selection、Calendar、TimeSlider、Gauge、Text 等）**自身的显示内容**只能靠 PNG 兜底；它们的过滤效果经书签进入图表和表的数据（第 12.4、12.4a 节）。PNG 不稳定的实际范围尚未量化 |

### 16.2 主要发现（按价值排序）

1. **严格验证已有现成实现但只覆盖约 16%。** `DcViewsheetTest` 已修了"导出失败被吞掉"和"基线文件缺失不报错"，只有 2 个 spec 文件在用，其余约 84% 仍是宽松版（第 6、7 节）。**推广它是见效最快的改进。**
2. **选择类和展示类组件自身的内容没有数据断言**，只有整页 PNG 像素比对；它们的过滤效果经书签由图表和表的数据覆盖，但是否缩小了数据范围无法静态判断（第 12.4、12.4a 节）。
3. **单元层缺少"算得对"的验证：**
   - 聚合型计算字段（`CalcFieldFormula`）的 33 个测试里只有 1 个验证表达式求值，且表达式只是聚合名本身；
   - `report/filter` 下 8 个公式没有专门测试：Correlation、Covariance、PopulationStandardDeviation、WeightedAverage、DistinctCount、Mode、Median、NthMostFrequent，其中 vscalc 正在使用的有 5 个：Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent；
   - 已有的同名测试属于 `mv/formula`，是物化视图路径，**不能替代**。vs 运行时走 `report/filter`（第 14.9 节）。
4. **孤儿 vs：** `aggregate_trendcomparison` 没有用例也没有基线。
5. **`exportData` 没有清理代码，CI 命令不带 `clean`**，残留导出可能掩盖缺失（需确认 Jenkins 工作区是否全新）。
6. **读源码发现的疑似问题（均未运行确认，可能是读错）：**
   - `NthMostFrequentFormula` 无参构造时 n 保持 0，取结果时疑似数组越界；
   - `CorrelationFormula` 在大偏移数值下可能因一次遍历求和公式而算错；
   - `DistinctCountFormula` 的百分比只处理 `Integer` 类型总数，`Long` 会被悄悄忽略；
   - `CorrelationFormula.addValue(double[])` 的下标约定与 `WeightedAverageFormula` 不一致，且长度校验不足。

### 16.3 已撤回或更正的结论（请勿再引用旧说法）

| 旧说法 | 更正 |
|---|---|
| "几乎都靠 PNG 比对" | 每个用例同时做 TXT 数据比对和 PNG 比对；chart 的 PNG 是进程内渲染 |
| "基于明细的聚合计算字段覆盖为 0（0/98）" | **标反了。** `baseOnDetail` 默认 true，只在 false 时才写出属性；无属性 = 基于明细（58 个），`baseOnDetail="false"` = 聚合型（40 个），两类都有覆盖（第 12.1 节） |
| "计算字段只能用 `field[...]` 或 `Sum()`" | 实际是脚本表达式：四则运算、字符串拼接、`CALC.*`、`dateAdd`、引用参数、引用另一个计算字段、条件分支都已出现 |
| "`CALC.edate`、`quarter` 等在单元测试里 0 次出现" | 搜索方法有偏差：测试直接调用 `CalcDateTime`（44 个方法对应 41 个测试），日期函数覆盖较好，不是缺口 |
| "DistinctCount、Mode、Median、NthMostFrequent 已有专门测试" | 那些测试属于 `mv/formula`，`report/filter` 版本没有（同名类跨包） |
| "`4^16384` 是 autosave 副本" | 是 VSBookmark（书签）存储，保存选择状态（12.4a） |
| "选择类组件上的计算字段完全没有数据断言，只有 PNG" | 过于绝对：书签的选择状态会进入图表和表的导出数据；没有数据断言的是这些组件自身的内容。是否真的缩小了数据范围静态上无法判断（12.4a） |

### 16.4 方案方向

分层：**单元层（产品仓库）证明"算得对"，datatest 证明"接得对"。**

1. 先推广 `DcViewsheetTest` 的严格行为（分批、先本地统计会暴露多少此前空转通过的用例）。
2. 把静态解析做成可重复运行的检查：孤儿 vs、vs 与 spec 与基线一致性、覆盖矩阵。
3. 产品仓库补单元测试，按 `02-unit-test-case-list.md` 分四阶段推进。
4. 选择类和展示类组件的计算字段：评估补数据断言还是继续依赖 PNG。
5. AI 视觉比对降级，等量化 PNG 不稳定的实际范围后再决定。

### 16.5 需要开发回答的语义问题（影响单元测试期望值怎么写）

1. null 语义不统一：`NthMostFrequent`/`Mode` 把 null 当作一个值计数，`DistinctCount`、`Sum` 等忽略 null。是否有意？
2. 空集返回值不统一：`DistinctCount` 返回 0 且 `isNull()=false`，其他公式返回 null。
3. 除零与 NaN：聚合型表达式除零；`Correlation` 方差为 0 时 `getResult` 返回 null 而 `getDoubleResult` 返回 NaN。
4. `Mode` 并列按值升序，是否为有意的契约。
5. Integer 与 Double 混合时是否视为同一值。
6. `CorrelationFormula.addValue(double[])` 的下标约定及是否有调用方。
7. 日期函数月末语义是否遵循 Excel。

### 16.6 本次分析的整体局限

- 单元测试覆盖分析按类名、文件名、测试方法名做静态匹配，**没有逐个读测试内容**；方法名关键词只是粗略代理。
- 只覆盖 vscalc 一个 spec 和 `ViewsheetTest` 基类；其他模块的断言细节没有逐个看过，不能外推。
- 产品源码分析用的是 `test-cluster-p1-52-second-rename` 分支，datatest 实际用的产品版本可能不同。
- 用例清单中的期望值未运行验证；标 📝 的只能防止行为意外变化，不能证明行为正确。
- 基线来源（回归基线还是独立标准答案）按要求暂缓，未查 git 历史，这直接影响对"测得准"的最终判断。
