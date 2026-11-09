# vscalc：datatest 侧补法方案（round-1）

> 状态：草案，未落实。单元测试部分见 `02-unit-test-case-list.md`，本文只管 datatest。
> 标注：【已验证】读了代码或运行了脚本；【未验证】没有运行或没有查证。

## 总览：四件事，由小到大

| # | 事项 | 做法 | 量级 | 先做？ |
|---|---|---|---|---|
| A | 推广严格基类 | `VS_CalcField_Spec` 的 22 处 `new ViewsheetTest(` 换成 `new DcViewsheetTest(` | 约 23 行 | **先做** |
| B | 补孤儿 vs | 给 `aggregate_trendcomparison` 加一个 spec feature 和基线 | 约 12 行加 7 个基线文件 | 紧随其后 |
| C | 补薄弱格子的新用例 | 新做 vs 并入 backup zip，再加 feature | 大，取决于做几个 | 之后 |
| D | 选择类组件自身内容的断言 | 扩展基类，让它也导出这些组件的数据 | 中，依赖未验证的接口 | 最后评估 |

## A. 推广严格基类

**怎么改：** 参照已有样例 `Crosstab_DateComparsion_Spec`：加 `import inetsoft.test.modules.DcViewsheetTest`，保留 `static ViewsheetTest vstest` 的声明不变，把 `new ViewsheetTest(` 换成 `new DcViewsheetTest(`。【已验证】

**预期影响：** 静态预测 22 个用例的基线文件与预期导出完全一致，从文件集合的角度**预计 0 个用例会失败**（`evidence/strictcheck-VS_CalcField_Spec.md`）。所以这一步在 vscalc 上是"保险"，不是"修复已有失败"。【已验证，但只是静态预测】

**不知道的：** 导出过程中的异常，目前被 `onExportFailure` 吞掉，换基类后才会变成失败。有一个**不用改代码**就能量出来的办法：先用现有基类跑一遍，在输出里搜 `execute viewsheet failed` 和 `export PNG failed`，有多少条就是多少个被吞掉的异常。【未验证】

**我的推测（未验证）：** 依赖 DB2、MySQL 的用例（`testDB2`、`testQuery` 等）在数据库没起来时，今天很可能"空转通过"，换基类后会真正失败。这是对的，但会让本地运行变嘈杂。

**运行前提：** license key（`-Dinetsoft.license.key=...` 或环境变量 `INETSOFT_LICENSE_KEY`）；数据库用例需要 `-Duse.testcontainers=true`（需要 Docker）；先 `./mvnw package -pl commons -DskipTests`，再 `./mvnw test -pl vsothers -Pvscalc`。【已验证，来自 README 与 CI 配置】

## B. 补孤儿 vs `aggregate_trendcomparison`

vs 已经在 zip 里，有 Chart1、Crosstab1、RangeSlider1，3 个计算字段（两个聚合型 Average/Sum，一个明细型 date），书签 `(Home)` 和 `bk1`，数据源 Query1。【已验证】只缺 spec feature 和基线。照着同类用例写：

```groovy
def 'aggregate_trendcomparison'() {
   caseName = specificationContext.currentIteration.name
   vstest = new DcViewsheetTest('1^128^__NULL__^vs-calcfield/aggregate_trendcomparison', caseName)
   vstest.executeVS(null, ['(Home)', 'bk1'] as String[])
   vstest.exportAsPNG(['(Home)', 'bk1'] as String[], null, true, false)

   expect:
   verifyAll {
      vstest.compareData(null)
      vstest.compareImage(null)
   }
}
```

**预测会导出 7 个文件：** `Chart1.png/.txt`、`Crosstab1.txt`、`bk1_Chart1.png/.txt`、`bk1_Crosstab1.txt`，加整页 `aggregate_trendcomparison.png`。【静态预测】

**生成基线：** `-Dupdate.baseline=true`（只在严格基类下可用）；PNG 只在缺失时才写。【已验证，见 README 第 3 节】

**必须注意：** 新基线是产品自己的输出，**只能防回归，不能证明正确**。每个新 `.txt` 基线至少抽几行，用独立方式（对数据源直接查询）核对一次再接受，核对结果写进提交说明。另外 PNG 在不同平台像素不同，要在和 CI 相同的平台上生成。

## C. 补薄弱格子的新用例

候选（来自 `01-…` 第 12 节的矩阵，【已验证】为"本批 vs 里没有"，不等于产品不支持）：

1. 聚合型计算字段结果为 `timeInstant`、`boolean`（目前 0 个）。
2. 明细型条件分支表达式、null 操作数、除零（**期望值取决于产品语义，等开发回答，见 02 第 3 节**）。
3. Chart 的 `shape`（只有 2 个 vs）、`text`、`size`（各 4 个）视觉绑定。
4. 各类 condition 位置：CalcTable 1 个、Table 2 个、Crosstab 3 个 vs。
5. Gauge 和 Text 的类型和表达式单一（只有 integer 加"其他"）。
6. 图表类型：vscalc 现有 BAR、BAR_STACK、LINE、PIE、FUNNEL、GANTT、MAP、TREE、NETWORK、SUNBURST、TREEMAP，**团队设计表里列的类型都已覆盖**；没有的有 AREA、POINT（散点）、RADAR、STOCK/CANDLE、BOXPLOT、MEKKO、WATERFALL、PARETO、STEP 等。另有一处要核实：`donut chart` 这个 vs 的类型是 PIE（3），不是 DONUT（0x13），需确认是否有意。

**怎么做 vs：** 在 StyleBI 里制作，再导出 backup 并入 zip（团队现有做法）。zip 是整个 storage 的备份、内容是二进制 blob，无法人工 review，所以改完后用 `vs_backup_scan.py inventory` 对比前后，确认只有预期的 vs 变了。手工往 zip 里注入 blob 在原理上可行，但**没有验证过，不建议**。【未验证】

**建议新做小 vs，不改已有 vs：** 改已有 vs 会让已有基线全部变动，混进噪声。

**分工：** 每个新用例只验证"接线"（计算字段在这个位置能取到值、能渲染）。边界和语义放进单元测试，不要为边界值堆 vs。

## D. 选择类组件自身内容的断言

现状：`executeVS0` 只导出 Chart 和表格类；Selection、Calendar、TimeSlider、Gauge、Text 自身的内容只有整页 PNG。它们的过滤效果会经书签进入图表和表的数据，所以真正没有数据断言的是这些组件**自己的内容**（`01-…` 第 12.4a 节）。

**线索：** `commons` 的 `ExportUtil` 里已经有 `exportSelectionList` 和 `printSelectionValue`，说明导出选择列表数据的工具已经有了，`Filter_Properties_Spec` 等 spec 也在用选择类组件。把它接进 `executeVS0` 可能是最短路径。【线索已验证，但"能否从沙箱取到选择列表数据、怎么接"没有验证】

**建议：** 先查清楚 `Filter_*_Spec` 现在怎么断言选择类组件，如果已有可复用的做法就复用；查不到再决定是否值得做。这一项放最后。

## 需要你决定的

1. **CI 的失败姿态。** GitHub 工作流里 5 个测试步骤全是 `continue-on-error: true`，失败不会让工作流变红；Jenkins 的 vscalc 阶段是 `catchError(buildResult: 'UNSTABLE')`，失败只让构建变成 UNSTABLE。也就是说**严格基类只是把"静默通过"变成"测试失败"，要不要让它真正挡住合入，是另一个决定**。【已验证】
2. PNG 基线在哪个平台生成（要和 CI 一致）。
3. 事项 C 要做哪几个。

## 建议的顺序

A（先跑一遍搜被吞掉的异常，再换基类）→ B → 回头看 D 有没有现成做法 → 视情况做 C。
