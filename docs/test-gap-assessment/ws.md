# ws 测试缺口与补充方案

> 模块：`ws`（无 profile，`./mvnw test -pl ws`），spec：`ws/src/test/groovy/inetsoft/test/worksheet/cases/` 下 6 个。datatest 基线分支：`Stateless-Sessions1`。产品：`main`（2.1.0-SNAPSHOT）。状态更新到 2026-11-23。

## 1. 结论

- **验证机制：** `WorksheetTest` 已是严格模式（PR #213）：导出失败、少导出、上传失败都会让用例失败。59 个用例中，除 `InRange_Spec` 和 4 个 `@Ignore` 外都真正比对数据。
- **CI：** `test-ws.yml` 只能手动触发、失败不挡合入；Jenkins 没有 ws 阶段；`InRange_Spec` 在 Linux 上整个跳过，所以**表条件里的 date in 在 CI 上是 0 覆盖**。严格模式之后 CI 没有跑过（最后一次 2026-10-13）。
- **执行路径：** 全部用 `LIVE_MODE`，没有 `RUNTIME_MODE`。`DataComposition_Spec` 的 9 个用例（`checkMergeable CM1`、`CM2`，`merge CM1`～`CM8`）已断言每张表能否下推到 SQL（G5，期望值主要来自设计矩阵，判断已与实际执行的 SQL 核对一致），其余用例只比数据。
- **各库差异：** CI 范围绑 8 个服务器库的 212 张表里 Oracle 占 122 张，SQL Server、Informix、PostgreSQL 很薄，ClickHouse 为 0（`@Ignore`）；dashboard 没有按库对照（G11）。
- **产品单测：** 28 个类、670 个测试（stylebi PR #8046、#8064，已合并），覆盖 lens、unpivot、`RotatedQuery`、排名并列、日期级别与日期条件、数值分段与命名分组、各库日期与统计公式 SQL、上传类型识别与 `CSVLoader`、`CSVInfo`、列类型转换、`XUtil` 与 `PreAssetQuery` 的下推规则。写测试发现的 14 个缺陷全部已修（见 7b）。
- **本机整套重跑（2026-11-23，产品为 `main` `06e8ee1c92`）：** 除 `InRange_Spec`（本机无法改系统日期）和 `SqlQuery`（产品性能缺陷 #82116，见 4b-J）外全部通过。

**接下来：** `SqlQuery` 等 #82116；G8、G11、G12 的 dashboard 新场景待定；产品单测补布尔、日期时间列给变量定类型（与 `queryVariableTypes` 互补）；`exportVSComponentData` 缺陷（见第 6 节）。

## 2. 覆盖情况

**规模：** 6 个 spec、59 个用例（`DataComposition` 10、`InRange` 4、`WSAsset` 2、`WSData` 25、`WSOthers` 9、`WSProperty` 9）。`wsStorage.zip`：94 个 worksheet、4 个 viewsheet、32 个逻辑模型、13 个数据源。CI 实际运行范围（去掉 Linux 上跳过的 `InRange_Spec` 和 4 个 `@Ignore`）：42 个 worksheet、470 张可见表。

**覆盖矩阵**（CI 范围 470 张可见表，单元格为"表数（worksheet 数）"，来自 `ws_scan.py inventory`）：

| 查询类型 | 表 | 前置条件 | 后置条件 | 排名 | 分组聚合 | 表达式 SQL | 表达式脚本 | 范围列 | 排序 | 去重 | 最大行数 | 变量 | mergeable=false |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| BoundQuery（逻辑模型） | 63 | 10 (7) | 1 (1) | 1 (1) | 13 (10) | 6 (5) | 2 (2) | 7 (6) | 12 (4) | 6 (2) | 0 | 0 | 1 (1) |
| PhysicalBoundQuery | 114 | 28 (15) | 3 (3) | 3 (2) | 24 (12) | 22 (11) | 9 (4) | 9 (6) | 10 (6) | 3 (3) | 6 (2) | 2 (2) | 1 (1) |
| SQLBoundQuery | 91 | 14 (11) | 0 | 2 (2) | 15 (9) | 19 (13) | 12 (7) | 12 (9) | 12 (9) | 8 (4) | 4 (2) | 12 (7) | 3 (3) |
| EmbeddedQuery（含快照） | 40 | 0 | 0 | 0 | 7 (7) | 0 | 9 (8) | 6 (5) | 9 (8) | 0 | 0 | 0 | 0 |
| MirrorQuery | 51 | 8 (5) | 3 (3) | 5 (3) | 16 (11) | 7 (5) | 4 (2) | 9 (7) | 3 (3) | 2 (2) | 0 | 2 (2) | 1 (1) |
| JoinQuery | 65 | 10 (8) | 2 (2) | 0 | 17 (10) | 2 (2) | 2 (2) | 13 (10) | 11 (8) | 1 (1) | 0 | 2 (1) | 0 |
| ConcatenatedQuery | 20 | 1 (1) | 0 | 1 (1) | 2 (1) | 1 (1) | 2 (2) | 3 (2) | 1 (1) | 0 | 0 | 0 | 1 (1) |
| RotatedQuery | 15 | 5 (3) | 0 | 0 | 4 (3) | 0 | 1 (1) | 0 | 1 (1) | 0 | 0 | 1 (1) | 0 |
| UnpivotQuery | 11 | 5 (5) | 0 | 1 (1) | 2 (2) | 0 | 3 (2) | 0 | 1 (1) | 0 | 0 | 3 (2) | 0 |

- 统计只含表类 assembly。变量 25 / 17、命名分组 23 / 22、日期范围 2 / 2（全部 / CI 范围 worksheet 里的个数）不在矩阵里。
- 运算符：inner 27、cross 14、union 10、left 8、minus 6、merge join 5、right 5、intersect 5、full 5、`>` 3、`<` 2、`<=` 2、`<>` 2；`>=` join 在 CI 范围为 0。
- 条件运算符：前置 equal 25、greater 24、one of 18、less 16、null 12、between 8、contains 5、starting with 2、like 1；后置 15；排名 top 9、bottom 4。**表条件里的 date in 在 CI 范围为 0**；另有 4 个 date in 在 `postprocess` 的命名分组和日期范围里，是否被表应用未核实。
- 聚合公式 24 种（Sum 59、Count 13、Max 12，其余 1～2 次）；日期分组 16 个级别；范围列 DateRangeRef 139、NumericRangeRef 18；表达式列 SQL 113、脚本 82。
- 数据源（表数）：Oracle 122、MySQL 41、mvtest 27、DB2 21、Examples/Orders 18、SQL Server 12、Informix 9、sqlite 5、PostgreSQL 5、Access 3、securityTest 3、Derby 2、ClickHouse 0。`Examples/Orders` 是嵌入式 Derby；`Access`、`mvtest`、`securityTest` 是 H2。CI 的 8 个服务器库由 `gcp-db-server-iac` 启动：MySQL 8.0.12、PostgreSQL 11、SQL Server 2017、DB2 10.5、Oracle XE 11g、Informix 14.10、Derby 10.15、ClickHouse 22.1。逻辑模型只落在 Oracle、MySQL、DB2。
- 业务设计表 `docs/ws/Worksheet Auto.xlsx`："ws->表"的行中 176 行对应的表被运行；17 行的 worksheet 不在 zip；13 行的表名在 worksheet 里找不到；1 行是隐藏表。
- 断言：每张可见表导出整表 txt（排序后）比对，没有 PNG；隐藏表（13 张）只经依赖它的可见表间接验证。基线是产品输出生成的回归基线，不是独立答案（本轮新增和更新的基线除外，见 4b-J、第 5 节）。

**功能面差集**（产品能力清单对照 worksheet XML；"全部"是 94 个 worksheet，"CI"是 CI 范围 42 个）：

| 能力 | 全部 / CI 用到 | 零覆盖项 |
|---|---|---|
| 表 assembly 类（15） | 11 | `Cube`、`Data`、`QueryBound`、`Tabular`：0 / 0；能否由用户创建未核实 |
| 非表 assembly 类（8） | 5 | `MirrorDateRangeAssembly`、`DefaultConditionAssembly`、`MirrorConditionAssembly`：0 / 0 |
| 日期分组级别（21） | 16 | `week of month`、`day of year`：0 / 0（am/pm、millisecond、am/pm of day 不被 `DateRangeRef` 支持，不算） |
| 聚合公式（27） | 24 | `SumSQ`、`SumWT`、`Aggregate`：内部构件，用户选不到，由 `AggregateFormulaStatSqlTest` 间接覆盖 |
| join / 并集运算（14） | 13 | `>=` join：全部 1 / CI 0 |
| 相对日期条件（`DateCondition` 子类 13） | XML 里 Year 3 / 3、Quarter 2 / 2 | 其余靠 `InRange_Spec` 的变量传入，按名字看几乎全覆盖，该 spec 在 CI 跳过 |
| 条件运算（15） | date in 全部 3 / CI 0；like 1、starting with 2 | `pseudo`、`correlated`：0 / 0 |
| 变量样式（6） | 5 | `DATE_COMBOBOX`：0 / 0 |
| 变量数据类型（13） | CI 5：string 18、boolean 2、timeInstant 3、date 3、time 1 | float、decimal、char、byte、short、long：0；double、integer 只在 CI 外 |
| 列引用 | ExpressionRef 225 / 196、DateRangeRef 148 / 140、NumericRangeRef 18 / 18 | `NamedRangeRef`：0 / 0 |
| 配置项 | `join.table.maxrows`、`crossJoin.maxCellCount` 已设置 | 未设置：`asset.sample.maxrows`、`formula.avg.keep.type`、`query.cache.data`、`asset.date.conditions`、`jdbc.query.filter` |
| 上传 | delimited 文本、xls、xlsx | xlsb 拒绝提示、文件大小与行列上限提示无用例（固定宽度文本不是上传功能，见 4b-G） |

spec 里设置的上传 `fileType` 不起作用：类型由 `TextUtil.detectType` 按文件内容判断。

**SQL 下推分支清单（3b-C）**

入口 `PreAssetQuery.isQueryMergeable`（`:2992`）→ `isQueryMergeable0`（`:3035`）：数据源、作为子查询的 SQL、where、group by、having、order by、select、ranking，任一步不通过就整体退回内存。规则已由产品单测覆盖（`PreAssetQuery*MergeableTest` 5 个类 216 个测试、`XUtilQueryMergeableTest`，见 4b-A）。下表"状态"是 CI 资产会不会走到该分支：有 / 部分 / 无 / 未验证（未核对资产）。

| 阶段 | 分支 | 结果 | CI 资产 | 状态 |
|---|---|---|---|---|
| 数据源 | `EmbeddedQuery`、`DataQuery`、`CubeQuery` | 恒不可合并 | 嵌入表 40 张（含快照 20） | 有 |
| 数据源 | `AssetQuery.java:890`：用户关闭合并，或含运行时 MV | 不可合并 | `mergeable=false` 7 张 | 有 |
| 数据源 | `BoundQuery.java:285`：SQL 表走父类，其余用 `XUtil.isQueryMergeable`；`PhysicalBoundQuery`、`LMBoundQuery` 恒为 true | 默认可合并 | 物理表 114、逻辑模型 63 | 未验证 |
| 数据源 | `MirrorQuery.java:311`：子查询可合并、库支持 `MIRROR_TABLE`、`FROM_SUBQUERY`、`MAXROWS` | 因库而异 | 镜像表 51 | 未验证 |
| 数据源 | `JoinQuery.java:154`：`FROM_SUBQUERY`、`MAXROWS`、`FULL_OUTERJOIN`、`join.table.maxrows`、子查询可合并、同一数据源 | 因库和配置而异 | join 65、full join 5 | 部分 |
| 数据源 | `ConcatenatedQuery.java:319`：union / intersect / minus 需库支持 | 因库而异 | union 10、intersect 5、minus 6 | 未验证 |
| 子查询 | `:3042`：作为子查询且 SQL 不可合并 | 不可合并 | `mergeable=false` 的表被引用时 | 未验证 |
| where | `:3112`、`:3143`：条件列或值不可合并 | 不可合并 | 前置条件表 90 张 | 未验证 |
| where | `:3118`：mongo 的 `ONE_OF` | 不可合并 | 无 mongo | 无 |
| where | `:3124`：相对日期条件 | 可合并 | 表上 date in 为 0 | 无 |
| where | `:3128`：条件不是 `Condition` | 不可合并 | — | 未验证 |
| where | `:3156`-`:3185`：子查询值（数据源不同、子查询不可合并、库不支持 `CONDITION_SUBQUERY`） | 不可合并 | 3 个 worksheet | 部分 |
| group by / having | `:3293`：交叉表聚合 | 不可合并 | 只在 viewsheet 路径 | 无 |
| group by / having | `:3306`、`:3373`：排名带 Group Others、嵌套分组排名、按另一引用排名 | 不可合并 | 排名表 13 | 未验证 |
| group by / having | `:3319`：distinct + 子表 + 列数不等于分组加聚合 | 不可合并 | distinct 表 21 | 未验证 |
| group by / having | `:3414`：时间序列分组 | 不可合并 | — | 未验证 |
| group by / having | `:3485`：命名分组的分组 | 不可合并 | 命名分组 22 个 worksheet | 未验证 |
| group by / having | `:3440`、`:3453`：表达式分组、日期分组函数 | 因库而异 | 日期分组 16 个级别 | 部分 |
| group by / having | `:3351`：需要非分组列 | 改在内存 | — | 未验证 |
| 聚合公式 | `:3498`：百分比 | 不可合并 | 只在 viewsheet | 无 |
| 聚合公式 | `:3516`-`:3524`：MODE、FIRST、LAST、PRODUCT、CONCAT、NTH_*、PTH_PERCENTILE | 不下推 | First 8、Last 2、其余各 1 | 部分 |
| 聚合公式 | `:3529`：MEDIAN | 因库而异 | 1 | 部分 |
| 聚合公式 | `:3534`：Derby 整数列 AVG | 不可合并 | — | 未验证 |
| 聚合公式 | `:3541`：其余按 `supportsOperation` | 因库而异 | — | 未验证 |
| order by | `:3621`：H2 分组输出无序 | 不可合并 | H2 本地库 | 部分 |
| order by | `:3628`、`:3653`：distinct + order by、聚合列 order by | 因库而异 | — | 未验证 |
| order by | `:3641`：FIRST / LAST 加排序 | 不可合并 | — | 未验证 |
| order by | `:3690`：隐藏列上的无括号函数关键字（#78489） | 不可合并 | 无 | 无 |
| select | `:1624`：没有可见列 | 不可合并 | — | 未验证 |
| select | `:1644`：列类型不可合并 | 不可合并 | — | 未验证 |
| select | `:1652`：脚本表达式列 | 不可合并 | 82 | 有 |
| select | `:1664`-`:1675`：范围列按库 `isMergeable()` | 因库而异 | DateRangeRef 139、NumericRangeRef 18 | 部分 |
| select | `:1715`：SQL 表达式引用 `parameter.` | 不可合并 | — | 未验证 |
| select | `:1723`：`field['…']` 引用的列不可合并 | 不可合并 | — | 未验证 |
| ranking | `:3710`：有排名条件 | 排名总在内存 | 13 | 有 |

合计 35 个分支：有 4、部分 7、无 5、未验证 19。资产上覆盖的主要是"恒不可合并"的路径；因库而异的判断资产会不会走到基本没有核对（G5、G11）。

**各库 SQL 支持矩阵**（`sqlhelper.xml` 加 `MySQLHelper`、`SQLServerHelper`、`DerbyHelper` 的覆盖；✓ 表示没有被列为不支持）：

| 能力 | Oracle | MySQL | SQL Server | DB2 | Informix | PostgreSQL | Derby | ClickHouse | H2 |
|---|---|---|---|---|---|---|---|---|---|
| union 下推 | ✓ | ✓（> 4.1） | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ | ✓ |
| intersect、minus 下推 | ✓ | ✗ | ✗ | ✓（列名需一致） | ✗ | ✓ | ✗ | ✓ | ✓ |
| full outer join | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✗ |
| maxrows | ✓ | ✓（WHERE 子查询除外） | ✓ | ✓ | ✗ | ✓ | ✓（WHERE 子查询除外） | ✗ | ✗ |
| FROM 子查询 | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 表达式 group by | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗（`expgrp` 时除外） | ✓ | ✓ |
| 表达式列 group by | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 聚合列、隐藏列 order by | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 多个 distinct | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ |
| correlation、covariance | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| stddev、var 函数名 | ✓ | ✓ | ✓ | – | – | – | – | – | – |
| correl、covar 函数名 | ✓ | – | – | ✓ | – | – | – | – | – |
| 日期函数个数（0 = 全在内存） | 22 | 25 | 22 | 30 | 9 | 24 | 9 | 0 | 0 |

- `day of year`、`week of month` 在任何库都只在内存算，没有跨库差异。Informix、Derby 缺 week、hour、minute、second 等级别。
- `week` 在一周起始日不是周日且设置了 `user.locale.weekGrouping` 时不下推。

**现有覆盖（CI 范围，场景 × 库，表数）：**
- 集合运算：union 在 Oracle 5、DB2 1、H2 4；intersect 只在 H2 3；minus 在 Oracle 2、PostgreSQL 1、H2 3。MySQL、SQL Server 的 union 为 0；不下推的库上 intersect、minus 的内存回退为 0。
- full outer join：Oracle 2、SQL Server 1；MySQL 1、H2 2（内存回退）；其余为 0。
- 统计聚合：MySQL 1、Derby 1、H2 2；Oracle、SQL Server 为 0。
- 日期分组：MySQL 15、DB2 4、Oracle 3、Derby 1、H2 4、sqlite 2；SQL Server、PostgreSQL、Informix 为 0。
- max rows：Oracle 3、MySQL 1、Derby 6；其余为 0。
- distinct：Oracle 12、MySQL 5、Derby 1、H2 1；其余为 0。
- 规模（含派生表和隐藏表）：Oracle 166、MySQL 79、H2 68、Derby 30、DB2 29、SQL Server 17、Informix 11、PostgreSQL 8、sqlite 6、嵌入表 89、ClickHouse 0。

**G11 代表场景（同一场景在不同库走不同路径的才值得跨库跑）：**
1. 集合运算：全部下推（Oracle、PostgreSQL）、只有 union 下推（MySQL、SQL Server）、都不下推（Informix、Derby）各取 union 和 minus。
2. full outer join：内存回退（MySQL、Derby、H2）对下推（Oracle、SQL Server、DB2、PostgreSQL）。
3. 统计聚合：有函数名（Oracle、MySQL、SQL Server）对没有（DB2、PostgreSQL 等）。
4. 日期分组：补 SQL Server、PostgreSQL；Informix、Derby、ClickHouse、H2 各取一个。
5. max rows：支持的库对 Informix、ClickHouse、H2。

**LIVE 模式的边界**（只用 `LIVE_MODE`，G6 暂缓）：
- 输入：每个明细表最多 `asset.sample.maxrows` = 50000 行；RUNTIME 没有这一项。
- 输出：最多 `query.preview.maxrow` = 5000 行；RUNTIME 用 `query.runtime.maxrow`（默认不限）。被截断的基线：`property_crossJoin.maxCellCount` 的 `TestData`、`Query1`，以及 `@Ignore` 的 `specialChar` 7 个。
- 其他差异：聚合与后置条件的使用、行数上限来源、公共列选择、SQL 表达式失败的处理、超时（设计时 2 分钟，运行时 20 分钟）。

**G5 是否下推的断言：** `WorksheetTest.exportMergePlan = true` 时，每张导出的表另写 `<表名>.plan.txt`：`mergeable`（`isQueryMergeable(false)`）、`source`（`isSourceMergeable()`），来源可合并时再写 where、group by、having、sort、select、ranking 各步。每项判断都在表的克隆上新建查询（`createAssetQuery(t, mode, box, false, -1L, true, false)`），不影响导出的数据。来源不可合并时各步都返回 false，所以不写。某一步为 false 表示这一步没有全部下推，能下推的部分仍进 SQL（`merge CM2` 的 `CM2_3`：3 条前置条件里 SQL 表达式列和普通列的 2 条在 WHERE 里，脚本表达式列的 1 条在内存）。LIVE 与 RUNTIME 的判断结果相同。生成的 SQL 文本没有纳入基线。

**判断与实际执行一致：** 对 142 张表按 sandbox 的方式执行查询并读出实际发给数据库的 SQL（一次性核对，未纳入测试）：执行后的判断与 plan 基线全部一致；判断为可合并的表，前置条件、分组、distinct、排序都出现在 SQL 里；判断为不可合并的部分都不在 SQL 里，组合表不可合并时没有合并后的 SQL。datatest 里产品的 SQL 日志不可用（slf4j-api 1.7 与 logback 1.5 不兼容，日志为空实现）。

已在 `checkMergeable CM1`、`CM2` 打开（30 张表）。设计矩阵 `docs/ws/DataCompostion_matrix.xls` 的 `CheckMergeable_TestCaseList` 规划了约 108 个"不可合并"用例（`CM1_1`～`CM1_64`、`CM2_1`～`CM2_44`），资产实现了其中 20 个：

| 结论 | 表 |
|---|---|
| 与设计一致（不可合并，原因相符） | `CM1`（设计 `CM1_4`）、`CM1_7`、`CM1_9`、`CM1_19`（Informix）、`CM1_29`、`CM1_55`（DB2）、`CM1_61`；`CM2_2`（where）、`CM2_8`（where）、`CM2_10`、`CM2_11`、`CM2_15`（group by）、`CM2_20`（having）、`CM2_30`、`CM2_44`（select，Informix） |
| 资产没复现设计场景，按实际（可合并）记录 | `CM1_6`（设计是 FreeformSQL，资产是普通结构化 SQL）、`CM1_32`～`CM1_35`（设计是 Access；名为 Access 的数据源是 H2，产品按 JDBC 地址和驱动判断为 H2，支持集合运算下推） |

另 10 张辅助表（底表、嵌入表、外部表）没有设计预期，按实际记录。

`merge CM1`～`CM8`（112 张表）也已打开。设计 `Merge_Rule`、`Merge_TestCaseList` 描述的是可合并时如何合并进 SQL，只有个别用例写明某一步不可合并。结果：109 张整体可合并；`merge CM2` 的 `CM2_3`（条件不可合并，where 为 false）、`CM2_5`（分组不可合并，group by、having 为 false）、`CM2_2`（select 为 false）。

**期望值的来源：**
- 来自设计、先于结果：用例表的整体是否可合并；`checkMergeable` 设计写明的不可合并原因；merge 系列"默认可合并"和 `CM2_3`、`CM2_5` 的例外。
- 看到结果后对照设计核对：`merge CM2_2`（设计是可见的 SQL 表达式列；表里还有可见的 JS 表达式列 `js_M2_08`，按 `CheckMergeable_Rule` 2.5 与 Pre1 第 3 条 select 不可合并）；各表在主要原因之外的步骤值（如 `checkMergeable CM2_2` 的 select 也为 false）。
- 按实际记录：`CM1_6`、`CM1_32`～`CM1_35`（资产没复现设计场景）和 10 张辅助表。

把一行期望改错用例会失败。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向与状态 | 优先级 |
|---|---|---|---|---|
| G1 | 基类吞异常、只比实际导出的文件 | 打不开、查询异常、上传失败照样通过 | **已完成**（严格模式，#213） | 高 |
| G2 | `WSBuildServer_Spec` 的 40 个 worksheet 不在 zip | 302 个基线代表的场景未验证 | **已完成**：spec 与基线已删除（#213） | 高 |
| G3 | 上传用例假通过；上传无单测 | 列类型识别、分隔符、表头、unpivot 出错 | **已完成**：假通过已修（#213）；`TextUtilTypeDetectionTest`、`CSVLoaderRulesTest`、`CSVInfoDetectionTest`；上传服务不加单测（4b-G） | 高 |
| G4 | 表上的 date in 在 CI 为 0（`InRange_Spec` 只能在保存了管理员凭据的 Windows 上改系统时间运行） | 相对日期范围算错 | 单测已补（`DateConditionBuiltinTest`、`DateConditionRelativeRangesTest`）；dashboard 等产品支持注入参考日期 | 高 |
| G5 | 不记录是否下推 | 本应下推的退回内存；两条路径结果不一致 | 规则单测已补（4b-A）；基类已能记录（`exportMergePlan`），`DataComposition_Spec` 的 `checkMergeable`、`merge` 9 个用例已断言（第 2 节）；其他 spec 未打开 | 中 |
| G6 | 只用 `LIVE_MODE` | 只在运行时出现的错误 | **暂缓** | 低 |
| G7 | lens 取值 | 结果行、表头、null 处理出错 | 单测已补（4b-A）；未做 unpivot 后聚合、MV | 高 |
| G8 | 薄弱格子：`>=` join、表上 date in、LIKE 1、starting with 2、Rotated/Unpivot/Concatenated 的后置条件、ClickHouse | 这些组合的接线出错 | dashboard 新建小 worksheet，范围待定 | 中 |
| G9 | 49 个 worksheet 没有用例运行 | 准备了却没验证的场景 | **已核对**（第 5 节）：只有 `suquery_execute` 的测试点没覆盖，已补 `WSData_Spec.queryVariableTypes`；其余资产保留不删 | 低 |
| G10 | 3 个陈旧基线 | 误以为被验证 | **已完成**：已删除（#213） | 低 |
| G11 | 各库 SQL 差异未区分 | 某个库上生成错误或不支持的 SQL | 生成层单测已补（`DateRangeRefDialectTest`、`AggregateFormulaStatSqlTest`、`SQLHelperOperationSupportTest`，只有 Derby 真正执行）；dashboard 按"场景 × 库"对照待做（需先有 G5） | 中 |
| G12 | 功能面零覆盖（见第 2 节"功能面差集"） | 这些能力出错无人发现 | 能手算 → 单测；接线 → dashboard；表类先向开发确认 | 中 |
| G13 | 已知 bug 的根因类别无验证（第 7 节） | 同类 bug 复发 | 重命名、快照、提示信息需要先有机制 | 中 |

## 4. Java 单测补充（产品仓库 `core`）

| 被测对象 | 现状 |
|---|---|
| lens：join、集合运算、rotate、filter（13.8 移植） | 4 个类、63 个测试 |
| unpivot | `AssetUtilUnpivotTest` 14、`UnpivotQueryTest` 6、`XEmbeddedTableSetDataTypesTest` 23；没做 unpivot 后交叉表 |
| `RotatedQuery` | `RotatedQueryTest` 9 |
| `DateCondition` | `DateConditionBuiltinTest` 39、`DateConditionRelativeRangesTest` 42 |
| `PreAssetQuery.is*Mergeable` | 5 个类、216 个测试；`XUtilQueryMergeableTest` 16 |
| 上传 | `TextUtilTypeDetectionTest` 43、`CSVLoaderRulesTest` 41、`CSVInfoDetectionTest` 30；上传服务不加（4b-G） |
| 排名 | `RankingTiesTest` 6 |
| 日期级别 | `DateRangeRefLevelValuesTest` 5、`DateRangeRefFullWeekLevelsTest` 3、`DateLevelSummaryTest` 5 |
| 数值分段、命名分组 | `NumericRangeRefBoundaryTest` 5、`NamedRangeRefGroupsTest` 12 |
| 各库方言 | `DateRangeRefDialectTest` 38、`AggregateFormulaStatSqlTest` 41、`SQLHelperOperationSupportTest` 13；只有 Derby 真正执行 |
| MV 对 rotate/unpivot | 只读代码，不加单测（4b-I） |
| 合计 | 28 个类、670 个测试（stylebi PR #8046、#8064，已合并），当前没有 `@Disabled` 的缺陷测试 |

### 4b. 已补单测的细节

**A. 测试类清单**

| 类 | PR | 测试数 | 对象 |
|---|---|---|---|
| `JoinLensesTest` | #8046 | 20 | join（13.8 移植） |
| `SetOperationLensesTest` | #8046 | 18 | union、intersect、minus、distinct（13.8 移植） |
| `SortSummaryFilterTest` | #8046 | 15 | 排序、汇总、top N（13.8 移植） |
| `RotateFormulaLensesTest` | #8046 | 10 | rotate、脚本表达式列（13.8 移植） |
| `DateRangeRefDialectTest` | #8046 | 38 | 16 个日期级别 × 各库是否下推，Derby 执行对照 `java.time` |
| `AggregateFormulaStatSqlTest` | #8046 | 41 | 统计公式的组合 SQL，Derby 执行对照 Java |
| `SQLHelperOperationSupportTest` | #8046 | 13 | 各库支持矩阵 |
| `DateRangeRefLevelValuesTest` | #8046 | 5 | 19 个日期级别 × 10 个日期 |
| `DateRangeRefFullWeekLevelsTest` | #8046 | 3 | week of month / quarter 与整周级别 |
| `DateLevelSummaryTest` | #8046 | 5 | 日期级别分组求和 |
| `AssetUtilUnpivotTest` | #8046 | 14 | unpivot 的列名、类型、行序、null、越界、重名 |
| `UnpivotQueryTest` | #8046、#8064 | 6 | worksheet 里 unpivot、改列类型、分组求和 |
| `RotatedQueryTest` | #8046、#8064 | 9 | 旋转表；空名、数字与日期表头、类型规则 |
| `NumericRangeRefBoundaryTest` | #8046 | 5 | 三种包含方式，SQL 与脚本对同一张表 |
| `RankingTiesTest` | #8046 | 6 | 恰好取 N 行、并列按输入顺序 |
| `TextUtilTypeDetectionTest` | #8046 | 43 | 文件类型与单值类型 |
| `CSVLoaderRulesTest` | #8046 | 41 | 列类型合并、表头去重、分隔符、引号、BOM、行列上限 |
| `XUtilQueryMergeableTest` | #8046 | 16 | `XUtil.isQueryMergeable` 的分支 |
| `DateConditionBuiltinTest` | #8046 | 39 | ToDate、最近 N 个月/天、第 N 季度/半年/月、PartDate |
| `CSVInfoDetectionTest` | #8064 | 30 | 分隔符、引号、首行表头、编码猜测 |
| `PreAssetQueryAggregateMergeableTest` | #8064 | 87 | 聚合公式能否下推 |
| `PreAssetQuerySortMergeableTest` | #8064 | 26 | 排序能否下推 |
| `PreAssetQueryConditionMergeableTest` | #8064 | 14 | 条件能否下推 |
| `PreAssetQuerySourceMergeableTest` | #8064 | 70 | Mirror、Join、集合运算、SQL 手写模式、内嵌/旋转/反透视表 |
| `PreAssetQueryGroupMergeableTest` | #8064 | 19 | 分组、选择列、排名、整条查询、结果缓存 |
| `XEmbeddedTableSetDataTypesTest` | #8064 | 23 | 改列类型：文本转数字、千分位、`$`、`%`、括号负数、强制置 null、日期 |
| `DateConditionRelativeRangesTest` | #8064 | 42 | 六个相对日期条件，周起始日周日/周一/周六，内存与下推 SQL |
| `NamedRangeRefGroupsTest` | #8064 | 12 | 命名分组，SQL 与脚本对同一批值 |

- 同一 JVM 整体跑和每个类单独跑结果一致；期望值按代码规则独立推导；关键类做过变异检查。
- 依赖当前时钟的测试用 `DayChange` 扩展，跨日时跳过；`DateConditionRelativeRangesTest` 的夏令时窗口用例会跳过 1 个。默认时区是 `core/pom.xml` 写死的 America/New_York。

**B. 搭建要点**
- `PreAssetQuery` 夹具 `MergeableFixture`：用 API 构造 `UniformSQL`（选择列带表名 `a.k`），`new SQLBoundQuery(RUNTIME_MODE, new AssetQuerySandbox(ws), table, false, false)`，不连库；脚本表达式列要 `setSQL(false)`；库版本由 mock 的 `XRepository.getMetaData` 返回，数据源本身不能配版本。
- worksheet 级测试需要 `AssetDataCache` bean，再 `setActive(true)`、`refreshColumnSelection`。
- `SummaryFilter2` 只合并相邻分组行，调用前必须排序。
- `-q` 加 `-Dsurefire.failIfNoSpecifiedTests=false` 可能一个测试都没跑也返回成功，要看 `Tests run:`。

**C. 与 13.8 的差异**
- 13.8 已修的缺陷 #80298、#80297、#80538，StyleBI 侧对应 #81092、#81091、#80538，均已修。
- StyleBI 新发现：#81093（`TextComparer.isAsciiOnly` 误判，中文不按拼音排序，已修）。
- 有意的行为变化：脚本一律受限模式（#77396）；`FormulaTableLens` 一批计算只报告一次失败。

**D. 写测试发现的缺陷**：见 7b。

**E. 从代码读出的规则**（已写成测试；"设计是否合理"是产品判断）

| 对象 | 规则 | 测试 |
|---|---|---|
| 各"周"日期级别 | `minimalDaysInFirstWeek = 7`：每月第一个不满整周的部分属于上个月最后一周；整周级别取周起始日所在月；`week of quarter` 以季度第一个整周为第 1 周 | `DateRangeRefFullWeekLevelsTest` |
| 没有日期函数的库 | `sqlhelper.xml` 25 个方言里 13 个没有 `sqlfunc`（含 h2、clickhouse、derby embedded），所有日期级别都在内存分组，结果不变 | 已固定 |
| 排名并列 | worksheet 排名固定 `setEqualityKept(false)`，恰好取 N 行，有意设计（#49245）；稳定排序，并列按输入顺序；总在内存算 | `RankingTiesTest` |
| `NumericRangeRef` 边界 | 默认 `LOWER` [下界, 上界)；`UPPER` (下界, 上界]；`NONE` 两端都闭、先匹配先得；空值进 Others | `NumericRangeRefBoundaryTest` |
| `DateCondition` 参考日期 | 不能注入：13 个子类直接用 `System.currentTimeMillis()` | 只能相对当前时间写期望 |
| 上传类型 | `detectType` 只看文件头 8 字节（xlsx、xls，其余按分隔文本）；`TextUtil.getType` 识别日期、时间、整数、小数，没有布尔；`CSVLoader` 把 `true`/`false` 识别为布尔，之后出现其他值变字符串；小数要求整串解析 | `TextUtilTypeDetectionTest`、`CSVLoaderRulesTest` |
| `XUtil.isQueryMergeable` | 非 JDBC、非 UniformSQL、解析失败、lossy、数据源为空、`jdbc.pushdown.blacklist`、手写 SQL 里 `$(@...)` 都不合并 | `XUtilQueryMergeableTest` |
| 各库能力 | 要同时看 `sqlhelper.xml` 和 helper 类：`SQLServerHelper` 集合运算只有 union，`MySQLHelper` union 看版本，`MongoHelper` 不支持 mirror 和集合运算，`DerbyHelper` 管表达式分组 | `PreAssetQuerySourceMergeableTest` |
| 库的版本 | helper 版本只在数据源没有 `version` 时才查 `DBProductVersion`；只影响 Oracle 中位数下推，影响很小 | 已固定 |

**F. 固定了现状、不报的行为**
- 上传类型：`1.2.3` 识别为日期；空字符串识别为 double；`1,234` 和超过 int 的整数识别为 double。
- `CSVLoader`：类型只由开头几行决定，之后不符的值变 null；行数等于 `rowLimit` 时也标记超限。
- `XEmbeddedTable.setDataTypes`：数字只读开头的数字（`12abc` → 12.0、`50%` 在整数列 → 0），用户明确指定类型时可接受。
- 命名分组：整数列里不在任何组的值，脚本侧返回 `7.0`，SQL 侧返回 `7`。
- `NthQuarter` 等的 setter 静默忽略越界值；Sybase `SUMSQ` 名字对不上（内部构件，无影响）；`isSortInfoMergeable()` 会改查询持有的表（生产上是克隆）；Join、Concatenated 的"数据源相同"判断多余。
- 读代码、未运行：`DaysCondition` 的 SQL 用毫秒减 24 小时，跨夏令时差 1 小时。

**G. 上传服务与 `CSVInfo`**
- `ImportCSVDialogService` 不加单测：`loadData` 依赖 `utils/inetsoft-xml-formats` 的 `PoiExcelFileSupport`，core 的测试类路径上没有。
- 固定宽度文本不是上传功能（只用于文本数据源）。
- 读代码、未验证：`process()` 把最大单元格大小（500）当列上限传给 `loadData`；改选项后的预览可能丢列上限提示；xlsb 可能先报 "Failed to parse XLS" 而到不了 xlsb 提示。
- `CSVInfo` 评分规则：前 10 行，列数一致且多于 1 列得"列数 + 100"，不一致得列数，引号未闭合得 0，取最小值，平局取先出现的。每行都有同样个数的引号内逗号时猜测为不处理引号。编码识别 UTF-8、GBK、UTF-16、ASCII 与代码一致。

**H. 没有覆盖的**

| 对象 | 没覆盖 |
|---|---|
| 统计公式、日期方言 | 只在 Derby 执行；其他库只断言生成的文本 |
| `DateRangeRefLevelValuesTest` | 其他 `week.start`、非默认时区、夏令时、`java.sql.Time` |
| `DateLevelSummaryTest` | 完整链路、条件组、cube、Sum 以外的公式 |
| unpivot、旋转表 | 之后再聚合或做交叉表；MV |
| `DateCondition` | 夏令时切换日 |
| `NamedRangeRefGroupsTest` | 日期类、浮点类、非等值命名分组；整条视图表路径 |
| `PreAssetQuery*MergeableTest` | `PhysicalBoundQuery`、`LMBoundQuery`；真实 mongo helper |
| 查询变量定类型 | `SQLBoundTableBuiltinVariableTest` 只测 INTEGER 列，没有布尔、日期时间列（datatest 侧由 `queryVariableTypes` 覆盖） |
| 上传服务 | 预览、unpivot 选项、各种上限提示、Excel 读取 |

**I. MV 对 rotate/unpivot 的处理**（只读代码，不加单测，不报）
- 5 处类型判断：`SelectionUpTransformer`（`:210`、`:216`、`:1630`、`:1635`）、`WSMVSelectionDownTransformer.canMoveDown`、`VSMVAnalyzer`（`:851`）、`MVDef.isColumnsShareable`。
- `isColumnsShareable`：已有 MV 的列包含新定义的全部列，且列数相等或绑定表树里没有嵌入表、unpivot 表（相关 #52564、#60857）。套在数据库表上的旋转表没有特殊处理；重名后缀、空名按行序编号，源数据变了可能指向别的行。没有在真实 MV 上验证，是否有意要问开发。

**J. 在当前产品上重跑 datatest 的结果**（本机，产品 `main` `06e8ee1c92`）

| 用例 | 结果 | 处理 |
|---|---|---|
| `property_crossJoin.maxCellCount` | #81087 修复后行序确定 | `Customer1` 加排序（State、Reseller），`Query1` 基线更新；剩余风险：`All Sales1` 无 ORDER BY |
| `rotate/Query4`、`condition1/Query7`、`grouping1/Query1` | #81102 修复后多出以前被丢的重名列 | 对照来源核对后更新基线 |
| `condition2/Query3` | #81193 修复后与原基线一致（3×2） | 不变 |
| `condition2/Query1` | 3×29 → 3×200：前 29 列不变，后面是重名列，截在 sandbox 列数上限 200（`max.col.count`） | 对照 `query21` 独立推导后更新；剩余风险：同一天的行序依赖数据库 |
| `WSAsset_Spec.varible` 的 `rotation`、`Copy of rotation1` | 6×20 → 6×200，同上 | 给来源 `query11` 加排序（`ORDER_DATE`、`CUSTOMER_ID`、`EMPLOYEE_ID`，顺序完全确定），基线由排序后的 `query11` 独立推导，与输出逐字节一致；同一 worksheet 的其他 12 张表输出不变 |
| `range&formula/Embedded` | `f_byte`（byte，`field['byte']/0.5`）-6、-2 → 250、254 | #81200 有意修改（超出声明类型范围时保留原值）；基线已更新 |
| `WSData_Spec.SqlQuery` | 整套里 312 秒时 sheet 过期，单独运行约 340 秒通过 | 产品性能缺陷 **#82116**（见下）；等修复 |
| `InRange_Spec` | 系统日期没改成（无保存的管理员凭据） | 环境原因 |

- `WSAsset_Spec.varible` 导出里第一次出现的列显示为 `7`、重名列为 `7.00_1`：产品里两者都按 `0.00##` 命名（`7.00`、`7.00_1`），`7` 是 datatest 导出用 `#0.####` 格式化原始 Double 表头的结果，不是产品缺陷。
- **#82116：** 打开 worksheet 收集变量时，`oracle q21`、`sql server q11` 的派生表在 `uniform_sql` 里存成带引号的整段 SQL 文本（`issql=false`，13.7 保存），被当成物理表取列；`JDBCHandler.getTableColumns` 找不到，进入扫描所有 schema 的 `getTables` 和同义词查询，Oracle 上每次几分钟。查询本身用存好的 SQL 执行，数据正确。服务器上有元数据缓存，只有第一次打开慢。

## 5. Dashboard 与机制补充

| 场景 | 做法 | 状态 |
|---|---|---|
| 严格模式（G1） | 导出失败即失败、每张表单独捕获后汇总、基线文件必须都已导出、`-Dupdate.baseline`、清理 case 目录 | 已完成（#213） |
| `WSBuildServer_Spec`（G2） | 删除 spec 与 302 个基线 | 已完成（#213）；找到资产再补回 |
| 上传（G3） | 修好基类后重跑 | 已完成（#213） |
| date in（G4） | 语义放单测；dashboard 留 1～2 个代表用例，不依赖改系统时间 | 等产品支持注入参考日期 |
| 执行路径（G5） | `exportMergePlan` 写 `<表名>.plan.txt` 并纳入基线 | `checkMergeable CM1`、`CM2`，`merge CM1`～`CM8` 已完成；其他 spec 未打开 |
| 运行时取数（G6） | 基类支持 `RUNTIME_MODE` | 暂缓 |
| 薄弱格子（G8） | 新建小 worksheet：`>=` join、date in、LIKE/starting with、Rotated/Unpivot/Concatenated 后置条件、ClickHouse | 范围待定 |
| 各库差异（G11） | 每类代表场景在 8 个库各建一个小 worksheet，基线按库分开 | 需先有 G5；各库造数方式待定；ClickHouse 先确认 `@Ignore` 原因 |
| 功能面零覆盖（G12） | 新建小 worksheet：日期范围镜像、可复用条件 assembly、`DATE_COMBOBOX`、未覆盖的变量类型、`NamedRangeRef`、未设置的配置项 | 范围待定；表类待开发确认 |
| 未运行的 worksheet（G9） | 核对后补用例 | 已完成，见下 |

做法原则：新建小 worksheet，不改已有资产；改 zip 后用 `ws_scan.py inventory` 前后对比。

**未运行的 worksheet（G9）：** 49 个，都没有被其他资产引用。资产保留不删。

| 类别 | 数量 | 结论 |
|---|---|---|
| 迁移前 query 等资产的原件，副本已在被测 worksheet 里（如 `oracle q1` → `Common/sql query` 的 `oracle q11`；`CM8`、`CM8_distinct` → `merge/CM8` 的 `CM8_1_1`～`CM8_1_6`） | 30 | 不是缺口 |
| `Common/nullws`：上传用例的导入目标（`ws_scan.py` 没有建模 `importCSVToEMTable`） | 1 | 在用 |
| `Examples/*`：产品示例 | 11 | 不属于 ws 数据测试 |
| 迁移原件、没有副本：`Sample Queries` 3 个（示例）、`userInfo`（安全模块）、`db2formula`、`suquery_execute` | 6 | 只有 `suquery_execute` 需要补 |
| `DB/clickhouse_sql`：ClickHouse 的 IPv6、Array 表，未用 | 1 | 归 G8、G11，等 `clickhouse` 用例去掉 `@Ignore` 时用上 |

**`WSData_Spec.queryVariableTypes`（补 `suquery_execute`）：** DB2 查询里三个文本输入变量分别与 DECIMAL、boolean（DB2 里是 VARCHAR `true`/`false`）、TIMESTAMP 列比较：`PRICE > $(expr)`、`RESELLER = $(exb)`、`PRODUCE_DATETIME < $(exd)`。此前查询变量只与数值列和一个 MySQL 日期表达式比较过，DB2 上没有。两组参数压在数据边界上：

| 文件前缀 | `expr` / `exb` / `exd` | 结果（CUSTOMER_ID） | 被排除的边界行 |
|---|---|---|---|
| `resellerTrue` | `125` / `true` / `2001-01-01 01:13:13` | 1、3、5、8、14 | 2（价格等于 125）、21 和 22（时间相等） |
| `resellerFalse` | `30` / `false` / `2006-01-01 09:09:09` | 4、13、23 | 19（时间相等）、9 和 10（时间更晚） |

基线由纯 JDBC 读出的 DB2 原始行按条件筛选得出，不来自产品输出；删掉基线一行用例会失败。剩余风险：没有 ORDER BY，行序依赖 DB2。

`WSOthers_Spec` 的 `ws group case check`（`@Ignore`）用到的 `wsCheck/ws1` 不在 zip 里，去掉 `@Ignore` 前要先补回。

## 6. 局限与待确认

**需要开发回答：**
1. `DateCondition` 能否改成可注入 `Clock`（决定 date in 能否不靠改系统时间测试）。
2. worksheet 层哪些组合"应当"下推：现状已写成测试，是否合理是产品判断。
3. 上传类型识别、`NumericRangeRef` 边界、排名并列的规则是否合理（现状已写成测试，见 4b-E）。
4. MV 对套在数据库表上的旋转表不做特殊处理是否有意（4b-I）。

**需要用户决定：**
- `ensure-db-server` 的等待和重试是否足够（严格模式下数据库不可用会让 CI 变红）。
- ws 的 CI 是否改为定时运行、失败挡合入、加入 Jenkins。
- G8、G11、G12 的场景范围；G11 各库造数方式。

**没验证的：**
- 严格模式后 CI 上的结果（只在本机 Windows 跑过）。
- `exportVSComponentData` 遇到既不是图表也不是表格的组件，会把上一个组件的数据再导出一遍，未改。
- 查询是否下推只在 `DataComposition_Spec` 的 9 个用例断言；其他 spec 没有。
- 设计矩阵里约 88 个"不可合并"用例资产没有实现（含 Access、Sybase、Ingres 等 CI 没有的库）。
- `LIVE_MODE` 与 `RUNTIME_MODE` 只确认了行数上限的差别。
- 命名分组、变量、日期范围在 worksheet 里的实际用法；`InRange_Spec` 的范围名与 `DateCondition` 子类只按名字对应。
- 49 个未运行的 worksheet 只看了表结构，没有执行。
- 库 × 处理步骤的交叉统计没有做。
- 依赖数据库行序的基线：`All Sales1`、`condition2/Query1`（同一天的行）、`queryVariableTypes`，本机运行稳定。
## 7. 已知 bug 对照

范围：Redmine "Product"、分类 "Worksheet Execution"、Bug、2025-11-01 至 2026-11-11 创建，共 118 条（`project_id=1`、`category_id=19`、`tracker_id=1`）。按标题粗分；覆盖情况只看 datatest 一侧。

**与数据结果直接相关的 27 条：**

| bug | 根因类别 | datatest 覆盖 |
|---|---|---|
| #81047（已修，stylebi #8051） | 脚本函数名与同表列名冲突 | 否 |
| #79997 | 脚本里与列同名的顶层变量 | 未验证 |
| #77999 | `new Date(field['…'])` | 否 |
| #73000 | 脚本引用另一个数据块 | 否 |
| #75662、#75698 | 公式数据改成分组后取不到 | 未验证 |
| #79841 | SQL 表 WHERE 里的内置变量与提示变量同时存在时 NPE | 部分 |
| #77860 | schema 限定表的表限定列 + 行数上限预览 | 否 |
| #75323、#75322 | 子查询条件取错列、SQL 报错 | 部分 |
| #74534 | 自动生成的笛卡尔 join 导致延迟 | 否 |
| #74213 | 查询没有继承 VPM | 未验证（vpm 模块） |
| #74100 | postgres 执行异常 | 部分 |
| #78225 | th_TH 区域下 Excel 日期早 543 年 | 否 |
| #74328 | 导入 vso 后数据缺失 | 未验证 |
| #80154 | Long 变量默认值是表达式时显示 3.0E9 | 否 |
| #77248 | join / 并集的子查询警告不显示 | 否 |
| #73485、#73375、#73368、#73322、#73134、#73422 | 快照导出导入、快照嵌入表、重启后数据丢失 | 否 |
| #79840、#73364、#73361、#73073 | 重命名与依赖传播 | 否 |

结论：27 条里没有一类有明确覆盖；最薄的是重命名与依赖传播、快照、提示信息、JVM 区域、Long 变量、脚本表达式的几种写法（G13）。

**不在 ws datatest 范围的 80 条**（另 Rejected 11 条不计）：

| 类别 | 条数 | 编号 |
|---|---|---|
| UI / 浏览器控制台 | 24 | #77023、#76941、#75845、#75317、#75314、#75258、#75228、#74920、#74737、#74434、#74407、#74200、#74199、#73997、#73573、#73553、#73551、#73488、#73414、#73269、#73268、#73181、#73093、#73019 |
| XSwapper / 内存交换 | 21 | #77877、#77830、#77710、#77693、#77685、#77683、#77682、#77676、#77655、#77653、#77652、#77649、#77638、#77636、#77635、#77623、#77622、#77608、#77593、#77591、#77579 |
| 并发 / 死锁 | 9 | #77874、#77873、#77681、#77311、#77301、#77273、#77223、#77215、#77158 |
| 数据源认证 / 缓存 | 7 | #77730、#77699、#77692、#77672、#76658、#75755、#75737 |
| 无状态会话 | 7 | #74884、#74739、#73736、#73735、#73631、#73571、#73076 |
| 其他（资产导入、权限、图表绑定） | 6 | #79843、#79842、#78053、#76236、#74108、#73121 |
| Worksheet MV（归 mv 模块） | 3 | #78452、#78340、#78339 |
| 损坏数据导入 | 3 | #77584、#77499、#77412 |
| 已拒绝（不计） | 11 | #78500、#78121、#77729、#77650、#75751、#75536、#74888、#73954、#73928、#73640、#73133 |

### 7b. 补单测和 datatest 时发现的缺陷

全部 Closed（版本 stylebi-2.1.0）。

| bug | 问题 | 修复 | 测试 / datatest |
|---|---|---|---|
| #81087 | cross join 行序取决于哪边先加载完 | `main` | `CrossJoinTableLensRowOrderTest`；`property_crossJoin.maxCellCount` 基线已更新 |
| #81091 | 连接列检查不阻止 hash、merge join（13.8 #80297） | `main` | `JoinLensesTest` |
| #81092 | `join.table.maxrows` 把表头算进上限（13.8 #80298） | `main` | `JoinLensesTest` |
| #81093 | `TextComparer.isAsciiOnly` 误判，中文不按拼音排序 | `main` | `SortSummaryFilterTest` |
| #81095 | 无原生函数的库里，一列为 NULL 时协方差、相关系数算错 | `main` | `AggregateFormulaStatSqlTest` |
| #81096 | 单行相关系数除零 | `main` | `AggregateFormulaStatSqlTest` |
| #81102 | 旋转表第一列重名时丢列丢数据 | `main` | `RotatedQueryTest`；3 个基线已更新 |
| #81105 | 数值分段脚本把 null 放进最后一段 | `main` | `NumericRangeRefBoundaryTest` |
| #81178 | `MonthsCondition` 对 Timestamp 内存判断与 SQL 不一致 | stylebi #8313 | `DateConditionBuiltinTest` |
| #81193 | #81102 修复后旋转表晚出现的名字被列上限截掉（回归） | stylebi #8092 | `RotatedQueryTest`；`condition2` 基线已更新 |
| #81220 | `CSVInfo` 用正则切 `\|` 首行，表头猜错 | stylebi #8226 | `CSVInfoDetectionTest` |
| #81229 | mongo 的 `ONE_OF` 让整个 where 留在内存 | stylebi #8270 | `PreAssetQueryConditionMergeableTest` |
| #81235 | 命名分组值或组名带单引号时 SQL 不转义 | stylebi #8236 | `NamedRangeRefGroupsTest` |
| #81236 | 只含 null 的命名分组生成空 `IN ()` | stylebi #8238 | `NamedRangeRefGroupsTest` |
| #80538 | 非 distinct union 加载中多算一行（原有） | stylebi #8209 | `UnionTableLensTest`、`SetOperationLensesTest`（stylebi #8643） |
| #82116 | 派生表的 SQL 文本被当表名，Oracle 上打开 worksheet 要几分钟（New） | 待修 | `WSData_Spec.SqlQuery` |

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py capability ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py flagcheck ws/src/test/resources/config/wsStorage.zip
python .claude/workflow-shared/datatest-gap/ws_scan.py dbmatrix ws/src/test/resources/config/wsStorage.zip <产品仓库>/community/core/src/main/resources/inetsoft/uql/sqlhelper.xml ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py crosscheck ws/src/test/resources/config/wsStorage.zip ws/src/test/resources/expectData ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
./mvnw package -pl commons -DskipTests && ./mvnw test -pl ws
# 更新基线：加 -Dupdate.baseline=true（导出文件写回 ws/src/test/resources/expectData，不比对）
# 删除过 spec 或基线、改过 wsStorage.zip 后先 ./mvnw clean -pl ws（zip 只在 target/test-resources/server/config-ws 不存在时导入）
```
