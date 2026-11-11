# ws 测试缺口与补充方案

> 日期：2026-11-10　模块：`ws`（无 profile，`./mvnw test -pl ws`），spec：`ws/src/test/groovy/inetsoft/test/worksheet/cases/` 下 6 个（评估时 7 个，`WSBuildServer_Spec` 已删除）。2026-11-11 更新：G1、G2、G3 的假通过、G10 已在分支 `datatest-ws-strict` 处理（待合并），状态见各节
> datatest 基线：`Stateless-Sessions1`（`f83c650a`）；产品源码：`main`（`origin/main` `f0ef913306`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 一致）。

## 1. 结论

ws 的用例规模不小（评估时 98 个用例、873 个基线文件；删除 `WSBuildServer_Spec` 后 58 个用例、568 个基线 txt），团队设计表 `docs/ws/Worksheet Auto.xlsx` 里的组合也基本都做成了用例。**但评估时真正在验证数据的不到一半（98 个里 44 个）；严格模式处理后，58 个用例里除 `InRange` 和 4 个 `@Ignore` 外的 50 个都真正比对了数据。**

- 断言基类 `WorksheetTest` 吞掉异常、只比实际导出的文件，[#203](https://github.com/inetsoft-technology/datatest/pull/203) 只改了 `ViewsheetTest`，没有覆盖它。本机实际运行：98 个用例里 90 个"通过"，其中 46 个什么都没导出（`WSBuildServer_Spec` 40 个：worksheet 不在 zip 里；上传用例 6 个：基类第 214 行空指针），真正比对了数据的只有 44 个。**已处理（分支 `datatest-ws-strict`，待合并）：** `WorksheetTest` 改为严格模式；上传用例的根因是测试 Spring 配置把 `ImportCSVDialogServiceProxy`、`FileSystemService` 换成了 Mockito mock（预览返回空结果、缓存文件为 null），已改为真实对象；`WSProperty_Spec` 里 5 处 `中文` 被写成乱码（`涓枃`），按乱码找不到 sheet，已改正；`WSBuildServer_Spec` 连同 302 个基线已删除；3 个陈旧基线已删除。
- CI（GitHub，ubuntu）上 `InRange_Spec` 整个跳过（评估时 `WSBuildServer_Spec` 也跳过，已删除），所以**表条件里的 date in（相对日期）在 CI 上是 0 覆盖**；GitHub 工作流只能手动触发，失败也不挡合入；Jenkins 没有 ws 阶段。
- 所有用例都用设计时的 `LIVE_MODE` 取数，从没走过 viewsheet / MV 实际用的 `RUNTIME_MODE`；名为 `checkMergeable` 的用例也只比数据，不检查查询是否下推到 SQL。
- 各库的 SQL 差异没有被区分：CI 用 `gcp-db-server-iac` 启动的 8 个库（MySQL、PostgreSQL、SQL Server、DB2、Oracle、Informix、Derby、ClickHouse），但 CI 范围内绑这 8 个库的 212 张表里 Oracle 占 122 张，ClickHouse 因 `@Ignore` 为 0，PostgreSQL 5、Derby 2；日期分组、top N / max rows、集合运算、统计函数在各库的 SQL 生成，既没有 dashboard 用例按库对照，产品单测也未见专门覆盖（G11）。
- 产品单测：`RotatedQuery`、`UnpivotQuery`、上传与类型识别、worksheet 层的合并判断没有任何测试；intersect、minus、cross join、rotate、max rows 只有序列化测试。13.8 已有可移植的 lens 测试（4 个测试类、70 个）。

已做：`WorksheetTest` 改成和 `ViewsheetTest` 一样的严格模式，并处理了因此变红的假通过和 3 个陈旧基线。接下来：移植 13.8 的 lens 测试，补 unpivot、date in、上传、合并判断的单测，再做各库 SQL 差异（G11）。

## 2. 覆盖情况

**规模：** 评估时 7 个 spec、98 个用例、基线 873 个 txt；删除 `WSBuildServer_Spec`（40 个用例、302 个基线）后为 6 个 spec、58 个用例（`DataComposition` 10、`InRange` 4、`WSAsset` 2、`WSData` 24、`WSOthers` 9、`WSProperty` 9），基线 568 个 txt。`wsStorage.zip`：94 个 worksheet、4 个 viewsheet、32 个逻辑模型、13 个数据源（Oracle、MySQL、DB2、SQL Server、Informix、PostgreSQL、ClickHouse、Derby、Access、sqlite 等）。

- `WSBuildServer_Spec` 引用的 40 个 `BuildServer/...` worksheet **不在 zip 里**，导入 `assets/BuildServer.zip` 的代码已注释（`WSBuildServer_Spec.groovy` `setupSpec`），仓库里也没有这个文件。**已处理：** spec 和 302 个基线已删除（见 G2）。
- 49 个 worksheet 没有被任何用例运行（如 `checkMergeable/CM1_7`、`CM8_distinct`、`CM8_maxrow`、`db2formula`、`oracle q1`），是否作为其他 worksheet 的来源未核对。
- CI 实际运行的范围（去掉类级 `@IgnoreIf({ os.linux })` 的 `InRange_Spec`（评估时另有 `WSBuildServer_Spec`，已删除）和 4 个 `@Ignore` 用例；ws 没有用例级的 `@IgnoreIf`）：42 个 worksheet、470 张可见表。

**覆盖矩阵**（维度：查询类型 × 处理步骤 × 执行路径；第二维度：取数方式 × 数据源；业务设计表作对照，已与用户确认）。下表为 CI 实际运行的 470 张可见表，单元格为"表数（worksheet 数）"，来自 `ws_scan.py inventory`：

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

- **统计口径与交叉核对：** 下面各项只统计**表类 assembly**（`ws_scan.py` 读 `groupInfo`、`preConditions`/`postConditions`/`topNConditions`、`sortInfo`、`normalColumnSelection`）。对全部 94 个 worksheet 的 570 张表，用 `assemblyInfo` 上的标志属性逐表对照：聚合 0 张不一致；`crosstabColumnSelection` 里的范围列和表达式列从不多于 `normalColumnSelection`；表达式有 13 张表标志为 `hasExpression=true`，但表里没有任何表达式节点（标志是派生值，按节点统计的结果成立）；条件 1 张、排序 2 张不一致，量级可忽略。**没有统计的 assembly 类型**（94 个 worksheet 里的个数 / 其中在 CI 范围 worksheet 里的个数）：变量 `DefaultVariableAssembly` 25 / 17、命名分组 `DefaultNamedGroupAssembly` 23 / 22（另有镜像 `MirrorNamedGroupAssembly` 2 / 2）、日期范围 `DefaultDateRangeAssembly` 2 / 2（都在 `postprocess`）、`MirrorVariableAssembly` 1 / 1；另有 22 个 class 为空的 assembly 节点，未识别。命名分组上的条件（含 date in）不在下面的条件运算符统计里。
- 运算符：inner 27、cross 14、union 10、left 8、minus 6、merge join 5、right 5、intersect 5、full 5、`>` 3、`<` 2、`<=` 2、`<>` 2；**`>=` join 只在没被运行的 worksheet 里**。
- 条件运算符：前置 equal 25、greater 24、one of 18、less 16、null 12、between 8、contains 5、starting with 2、like 1；后置共 15；排名 top 9、bottom 4。**表条件里的 date in 在 CI 范围内为 0**（只出现在 `InRange_Spec` 的 worksheet 里）。命名分组 `Date` 和日期范围 `NG_SR`、`NG_FR` 里另有 date in 条件（都在 `postprocess` worksheet，由 CI 范围内的 `DataComposition_Spec` 运行），它们是否被表应用未核实，明细见功能面差集。`ws_scan.py` 只读表上的条件，不读命名分组和日期范围。
- 聚合公式 24 种，Sum 59、Count 13、Max 12，其余多为 1～2 次；日期分组 16 个级别；范围列 DateRangeRef 139、NumericRangeRef 18；表达式列 SQL 113、脚本 82。
- 数据源（表数）：Oracle 122、MySQL 41、mvtest 27、DB2 21、Examples/Orders 18、SQL Server 12、Informix 9、sqlite 5、PostgreSQL 5、Access 3、securityTest 3、Derby 2；ClickHouse 用例被 `@Ignore`。
- CI 的数据库：`gcp-db-server-iac` 用 Docker Compose 启动 8 个库：MySQL 8.0.12、PostgreSQL 11、SQL Server 2017、DB2 10.5、Oracle XE 11g、Informix 14.10、Derby 10.15、ClickHouse 22.1；`test-ws.yml` 测试前执行 `ensure-db-server`。ws 的服务器类数据源正好是这 8 个，Access、sqlite、mvtest、Examples/Orders、securityTest 是本地库。CI 范围内绑这 8 个库的表共 212 张（Oracle 122、MySQL 41、DB2 21、SQL Server 12、Informix 9、PostgreSQL 5、Derby 2、ClickHouse 0）；逻辑模型查询只落在 Oracle、MySQL、DB2 三个库，其余库只有物理表或手写 SQL。来自 `ws_scan.py inventory` 的 Bound sources。
- 执行路径：只能静态看到 `mergeable=false`（用户关掉合并）7 张表；查询实际是否下推到 SQL 由运行时决定，**没有任何用例断言它**（见 G5）。
- 取数方式：全部 58 个用例都是 `LIVE_MODE`（`WorksheetTest.executeWS`、`importCSVToEMTable`），`RUNTIME_MODE` 为 0（见 G6）。
- 业务设计表对照：`Worksheet Auto.xlsx` 中写了"ws->表"的行，176 行对应的表被运行并导出；17 行的 worksheet 不在 zip（如 `DataBaseQuery->leftJoin`），13 行的表名在 worksheet 里找不到（如 `range&formula->product1` 9 行），1 行是隐藏表。

**验证机制：**

以下三条是评估时的状态，行号为修改前的 `WorksheetTest.groovy`；处理情况写在各条末尾。

- 比对遍历的是实际导出的文件，不是基线清单：`WorksheetTest.compareData`（`:310`）调用 `CompareUtil.CompareFileByFeature(null, ...)`，遍历导出目录。少导出的文件不会被发现。**已处理：** `compareData` 先检查基线里的每个文件（含传入的子目录）都已导出，否则失败。
- 导出失败不报错：`executeWS` 三参数版本 `catch(Exception)` 后只打印堆栈（`:100-105`，98 个用例中 97 个用这个版本）；`importCSVToEMTable`（`:176-181`）、`exportVSComponentData`（`:270-272`）同样。一张表出错时，同一 worksheet 里后面的表也不再导出（循环在 `try` 之内）。**已处理：** 导出失败直接让用例失败；每张表单独捕获，全部失败汇总后报告；上传返回里没有 `validator` 也失败。
- `exportData` 只删除同名文件，不清理 case 目录；GitHub 工作流先执行 `./mvnw clean`（`test-ws.yml:71`），本地重跑会有残留。**已处理：** 每个 case 的导出目录在第一次导出前清空；`-Dupdate.baseline=true` 把导出文件写回 `src/test/resources/expectData`，不比对。
- CI：`test-ws.yml` 只有 `workflow_dispatch`（定时触发已注释，`:4-7`），测试步骤 `continue-on-error: true`（`:75`），运行在 `ubuntu-latest`（`:17`）；Jenkins 没有 ws 阶段。
- **本机实际运行**（2026-11-10，Windows，评估时、修改前）：98 个用例，失败 1、跳过 7、通过 90。

| 情况 | 用例 | 原因（日志） |
|---|---|---|
| 通过，但一个文件都没导出 | `WSBuildServer_Spec` 40 个 | 每个都报 `Sheet not found`，异常被吞 |
| 通过，但一个文件都没导出 | `WSProperty_Spec` 上传 6 个（xlsx、xls、txt、csv、数字/日期类型识别） | 24 次上传都在 `WorksheetTest.groovy:214` 报 `NullPointerException: Cannot get property 'message' on null object`（返回结果里没有 `validator`），异常被吞；当时根因未查，见下方“处理后” |
| 通过，基线有文件未导出 | `checkMergeable CM1`：`CM1_3.txt`、`CM1_5.txt`；`top sql query`：`Report/top1.txt` | 前两张表在 worksheet 里不存在；`Report/` 是报表时代的基线，只比对 `VS/` |
| 失败 1、跳过 3 | `InRange_Spec` | 改系统日期的 `runas` 未生效（无保存凭据），按真实日期运行，相对日期结果与基线不符；`@Stepwise` 使后 3 个跳过 |
| 跳过 4 | `specialChar`、`clickhouse`、`ws group case check`、`customer bugs` | `@Ignore` |

  真正比对了数据的用例 44 个（`DataComposition` 10、`WSAsset` 2、`WSData` 22、`WSOthers` 8、`WSProperty` 2）；873 个基线文件中 388 个本次没有被比对。没有"导出了但没有基线"的文件。
- **处理后**（2026-11-11，严格模式，整套重跑）：58 个用例中 50 个通过且基线文件全部已导出（`DataComposition` 10、`WSAsset` 2、`WSData` 22、`WSOthers` 8、`WSProperty` 8），`InRange` 1 失败 3 跳过（改系统日期的 `runas` 未生效，同修改前），4 个 `@Ignore` 跳过。`WSProperty_Spec` 的上传 6 个用例现在真正导出，并与基线一致。`checkMergeable CM1` 在清理陈旧基线后单独重跑通过（整套重跑在清理之前）。`property_crossJoin.maxCellCount` 单独运行会失败（修改前同样），整套运行通过，存在顺序依赖，原因未查。
- 基线来源：`expectData` 共 27 次提交，均为"add exp""update txt"一类，是产品输出生成的回归基线，不是独立标准答案。

**断言层：** 每张可见表导出为整表 txt（排序后），没有 PNG。隐藏表（13 张）不导出，只通过依赖它的可见表间接验证。查询是否下推、生成的 SQL、预览行数上限都不在断言里。

**功能面差集（步骤 3b-A）：**

口径：清单来自产品 `main`（`333566db8`，2026-11-11；路径前缀 `community/core/src/main/java/inetsoft/`）的常量、枚举和类声明，位置写在表里；
次数 = 该能力在 worksheet XML 里出现的次数（含隐藏表），"全部"是 94 个 worksheet，"CI"是 CI 范围的 42 个 worksheet（口径同上文）。
XML 看不到运行时传入的值（变量值、`InRange_Spec` 的日期范围名），这类能力单独说明。统计脚本目前不在仓库里（待决定是否并入 `ws_scan.py`）。

| 能力（产品清单） | 位置 | 全部 / CI | 零覆盖项 | 会放过什么错误 |
|---|---|---|---|---|
| 表 assembly 类（可实例化 16 个） | `uql/asset/*TableAssembly.java` | 用到 11 个 | `CubeTableAssembly`（:36）、`DataTableAssembly`（:35）、`QueryBoundTableAssembly`（:34）、`ScriptedTableAssembly`、`TabularTableAssembly`（:46）：0 / 0 | 这些表类的取数出错；是否能由用户在 worksheet 里创建、CI 有没有对应数据源，**未核实** |
| 非表 assembly 类（8 个） | `uql/asset/*Assembly.java` | 用到 5 个：变量 25 / 17、命名分组 23 / 22、日期范围 2 / 2、镜像变量 1 / 1、镜像命名分组 2 / 2 | `MirrorDateRangeAssembly`、`DefaultConditionAssembly`、`MirrorConditionAssembly`：0 / 0（worksheet 里 `conditionAssemblies` 节点全是空的） | 可复用条件 assembly 与日期范围镜像的求值出错 |
| 日期分组级别（22 个） | `uql/XConstants.java:69-158` | 用到 16 个 | `am/pm`（:93）、`millisecond`（:109）、`week of month`（:130）、`day of year`（:134）、`am/pm of day`（:146）：0 / 0 | 这些级别的分组边界、标签算错 |
| 聚合公式（27 个） | `uql/asset/AggregateFormula.java:43-149` | 用到 24 个（含 none） | `SumSQ`（:141）、`SumWT`（:145）、`Aggregate`（:149）：0 / 0 | 这三个公式的值或结果类型错 |
| join / 并集运算 | `uql/asset/TableAssemblyOperator.java:47-107` | 14 个可选运算用到 13 个 | `>=` join（:75）：全部 1 / CI 0（在没被运行的 worksheet 里） | `>=` 连接条件的取值错 |
| 相对日期条件（`DateCondition` 子类 13 个） | `uql/asset/DateCondition.java:525-2443` | XML 里只看到 `YearCondition` 3 / 3、`QuarterCondition` 2 / 2（`postprocess` 的日期范围和命名分组，CI 运行） | 其余 11 个在 XML 里为 0 / 0；但 `InRange_Spec` 的 4 个 feature 用变量传入范围名（Year to date、Yesterday、Week before last week、This January、Last 3 months、Last 8-14 days、1st quarter this year、1st half of this year 等），按名字看几乎覆盖全部子类，**未逐个对应到子类**；该 spec 在 CI 跳过 | 相对日期范围算错（G4） |
| 条件运算（15 个）及取反、等于标志 | `uql/XCondition.java:38-129` | 表条件里 date in：全部 3 / CI 0；like 1、starting with 2；取反 25 / 22、等于标志（>=、<=）26 / 23 | 命名分组里的条件另计：date in 2 / 2、between 15 / 15 等；`pseudo`、`correlated`：0 / 0 | 见 G8 |
| 变量样式（6 个） | `uql/schema/UserVariable.java:50-70` | NONE、COMBOBOX、LIST、RADIO_BUTTONS、CHECKBOXES 都用到 | `DATE_COMBOBOX`（:70）：0 / 0；另有 17 个变量没写 `style`，按默认处理**未核实** | 日期变量的取值与传参 |
| 变量数据类型（13 个） | `uql/schema/XSchema.java:46-98` | CI 用到 5 个：string 18、boolean 2、timeInstant 3、date 3、time 1 | CI 零：float、decimal、char、byte、short、long（全部也为 0）、double 与 integer（只在 CI 外各 1） | 变量按类型转换并传给 SQL 时出错 |
| 列引用类型 | `uql/asset/*Ref.java` | ExpressionRef 225 / 196、DateRangeRef 148 / 140、NumericRangeRef 18 / 18 | `NamedRangeRef`：0 / 0（命名分组有 23 个，但没有表以列引用方式使用它，是否用别的方式应用**未核实**）；`SQLExpressionRef`：0 / 0 | 命名分组作为分组列、SQL 表达式列出错 |
| 配置项（读取位置） | 见右 | 已设置 2 个：`join.table.maxrows`（`ComposedTableAssembly.java:501`）、`crossJoin.maxCellCount`（`AbstractTableAssembly.java:1951`） | 未设置：`asset.sample.maxrows`（`WorksheetInfo.java:40`）、`formula.avg.keep.type`（`AggregateFormula.java:656`）、`query.cache.data`（`AssetDataCache.java:508`）、`asset.date.conditions`（`DateCondition.java:185`）、`jdbc.query.filter`（`JDBCHandler.java:186`） | 这些开关打开或改值后的行为；`query.preview.maxrow`、`query.analysis.maxrow`、`output.null.to.zero` 在 viewsheet 路径上，归 G6 |
| 上传的文件类型与限制 | `uql/util/filereader/TextFileType.java:36-52`、`web/composer/ws/dialog/ImportCSVDialogService.java` | 类型靠文件内容识别（`:187`、`:256`、`:521`），`resources/files` 里有 delimited 文本、xls、xlsx | 固定宽度文本 `FIXED`（`TextFileType.java:36`）、xlsb 的拒绝提示（`ImportCSVDialogService.java:704`）、文件大小与行列上限提示（`:709`、`:639`）：没有对应文件和用例 | 这些路径的解析和提示出错 |

- `model.fileType()` 在上传服务里没有被读取（类型由 `TextUtil.detectType` 从文件内容判断），所以 spec 里设置的 `fileType`（模板里的 `XLSX`、用例里的 `"DELIMITER"`，产品枚举是 `DELIMITED`）实际不起作用，不要当成"选了某种文件类型"。
- 日期的相对范围（G4）靠变量传入：`condition_inrange` 里 3 张表的 date in 条件 `condition_data` 为空，范围名来自 `InRange_Spec` 传的变量。
- `join` 和 `concatenation` 在 `TableAssemblyOperator` 里是基类标志位（`:47`、`:95`），不是用户可选的运算，不算零覆盖。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | `WorksheetTest` 吞异常、只比实际导出的文件，#203 未覆盖 | worksheet 打不开、某张表查询抛异常、上传失败，用例照样通过（评估时本机有 46 个这样的用例） | 机制：按 #203 的方式改 `WorksheetTest` 为严格模式。**已处理（待合并）** | 高 |
| G2 | `WSBuildServer_Spec` 的 40 个 worksheet 不在 zip，Linux 上又被跳过 | 302 个基线文件代表的客户场景全部未验证，却显示为通过 | 机制/资产：补回资产或删除 spec 与基线。**已决定删除并处理（待合并）** | 高 |
| G3 | 上传用例 6 个假通过；上传服务、`TextUtil.detectType` 无单测 | CSV/Excel 导入的列类型识别、分隔符、首行表头、unpivot 出错 | 机制（同 G1）+ 单测。**假通过已处理（待合并）**，根因是测试配置里的 mock 和 spec 里的乱码；单测未做 | 高 |
| G4 | 表上的 date in 条件在 CI 上 0 覆盖（命名分组里的 1 处另论，见覆盖情况）：`InRange_Spec` 在 Linux 跳过，只能在保存了管理员凭据的 Windows 上改系统时间运行；单测只覆盖本年/上年、本月、本周、本季度 | "年初至今""最近 N 个月/天""第 N 季度"等相对日期范围算错 | 单测（固定参考日期，期望值手算）；dashboard 不再改操作系统时间 | 高 |
| G5 | 执行路径未验证：`checkMergeable` 用例只比数据；`PreAssetQuery.isQueryMergeable` 无单测 | 本应下推的查询退化到内存（性能问题），或下推与内存两条路径结果不一致（null 排序、字符串比较、日期分组） | 单测（合并规则）+ 基类记录合并结果 | 中 |
| G6 | 只用 `LIVE_MODE`，从未走 `RUNTIME_MODE` | 只在运行时出现的错误：viewsheet/MV 读 worksheet 的结果、`runtimeSelected`、预览输入行数上限（`asset.sample.maxrows=50000`，`AssetQuery.java:3957`）以外的行为 | 机制：基类支持 `RUNTIME_MODE`，按查询类型各选代表用例 | 中 |
| G7 | lens 取值无单测：rotate、unpivot 完全没有；intersect、minus、cross join、max rows 只有序列化；full outer join 没有；非等值 join 只测取消；排名 lens 没有取值测试 | 这些操作的结果行、表头、null 处理出错，只有 datatest 回归基线可能发现 | 单测：先移植 13.8，再补缺 | 高 |
| G8 | 薄弱格子：`>=` join、表上的 date in 在 CI 范围为 0；LIKE 1、starting with 2；Rotated/Unpivot/Concatenated 几乎没有后置条件；ClickHouse 被忽略 | 这些组合下的接线出错 | dashboard：新建小 worksheet（tester 衡量） | 中 |
| G9 | 49 个 worksheet 没有用例运行 | 若是有意准备的场景，则对应能力未验证 | 核对后补用例或删除 | 低 |
| G10 | 3 个陈旧基线（`CM1_3`、`CM1_5`、`Report/top1.txt`） | 让人误以为这些对象被验证过 | 随 G1 清理。**已处理（待合并）：** 删除 `CM1_3`、`CM1_5`、`Report/top1.txt` | 低 |
| G11 | 各库 SQL 差异未区分：绑库的表 Oracle 占 122/212，ClickHouse 为 0，PostgreSQL 5、Derby 2；产品单测里引用各库方言类（`OracleSQLHelper`、`MySQLHelper`、`SQLServerHelper`、`DB2SQLHelper`、`InformixSQLHelper`、`PostgreSQLHelper`、`DerbyHelper`、`ClickhouseHelper`）的测试，多是别名、引号、like 大小写、嵌套派生表，日期分组、top N / max rows、集合运算、统计函数的各库生成未见专门测试 | 同一个 worksheet 在某个库上生成错误或不支持的 SQL（日期部分函数、`LIMIT`/`ROWNUM`/`TOP`、minus/intersect/full join、null 排序、字符串比较与大小写），换个库正常；下推与内存两条路径的结果不一致（G5）也会因库而异 | 单测：固定一组查询，断言各库生成的 SQL；dashboard：按"场景 × 库"为每类选代表 worksheet，在 8 个库各跑一遍（需先有 G5 的"是否下推"记录） | 中 |
| G12 | 功能面零覆盖：产品能力清单里 CI 范围 worksheet 零使用的项（日期分组 5/22、聚合公式 3/27、变量数据类型 8/13、列引用 `NamedRangeRef`/`SQLExpressionRef`、`>=` join、5 个表类、3 个非表 assembly、5 个配置项、上传的固定宽度文本与 xlsb），明细见"功能面差集" | 这些能力出错，没有任何用例会发现 | 能手算期望值的 → 单测（日期分组、聚合公式、相对日期、上传）；接线 → dashboard 小 worksheet；表类、命名分组的用法先向开发确认 | 中 |
| G13 | 已知 bug 的根因类别在 datatest 里没有验证：重命名与依赖传播、提示信息（子查询警告）、快照导出导入、JVM 区域（Excel 日期）、Long 变量、脚本表达式的几种写法（函数同名、`new Date(field[…])`、引用其他数据块）；近一年 Worksheet Execution 分类的 Bug 118 条，27 条与数据结果直接相关，见第 7 节 | 同类 bug 复发时没有用例会发现 | 能手算期望值的 → 单测；接线 → dashboard 小 worksheet；重命名、快照、提示信息需要先有机制（重命名与导入导出的调用、警告的断言） | 中 |

方向判断：期望值能独立算出 → 单测（G3、G4、G5 规则、G7）；接线 → dashboard（G8，以及 G4、G6 各少量代表用例）；验证机制本身 → 改基类（G1、G2、G6）。各库 SQL 差异（G11）：生成逻辑用单测，真实库上的取数结果用 dashboard 代表用例。

## 4. Java 单测补充（产品仓库 `core`）

盘点方式：从 `AssetQuery`、`PostProcessor`、各查询类追到实现类，再按实现类名在 `*/src/test/*` 搜测试（`origin/main` `f0ef913306`）；"只有序列化"的结论读过测试方法体，其余只看方法名。

**13.8 已有可移植的测试：** `style-intelligence` 仓库 `epic-test-coverage` 分支 `java/src/test/java/inetsoft/test/feature/` 下：`report/LensesJoinsFeatureTest`（20，含 full outer、非等值运算符、cross join、复合键、merge join）、`report/LensesSetOperationsFeatureTest`（29，union 去重/不去重、intersect、minus、distinct）、`report/LensesTransformsFeatureTest`（21，其中 rotate 5）、`worksheet/OperationsTableLensFeatureTest`（16，summary top N/Others、distinct 排序）。两边 lens 代码逻辑基本相同，但 StyleBI 多了并发与加锁改动（`CrossJoinTableLens`、`SelfJoinTableLens` 差异较大，`RotatedTableLens` 几乎相同），移植需逐个适配。

| 被测类 | 现有测试 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|---|
| `JoinTableLens`、`HashJoinTable`、`MergeJoinTable`、`SelfJoinTableLens`、`CrossJoinTableLens` | 值测试只有 inner/left/right（`JoinTableLensTest`、`MergeJoinTableTest`）；cross join 只有序列化；非等值只有取消 | 移植 13.8 `LensesJoinsFeatureTest` | 20（移植） | 中 | 无 |
| `UnionTableLens`、`IntersectTableLens`、`MinusTableLens`、`DistinctTableLens` | intersect、minus 只有序列化；distinct 无去重取值测试（按方法名） | 移植 13.8 `LensesSetOperationsFeatureTest` | 29（移植） | 中 | 无 |
| `RotatedTableLens` | 只有序列化 | 移植 13.8 rotate 5 个 | 5（移植） | 低 | 无 |
| `SummaryFilter`（top N/Others）、`SortFilter` distinct | `SummaryFilterTest` 有 top N | 移植 13.8 `OperationsTableLensFeatureTest` 中未重复的部分 | 16（移植，去重后可能更少） | 低 | 无 |
| `UnpivotQuery`、`AssetUtil.unpivot`、`applyChangedColumnsType` | 无 | 表头列数、多个值列、null、改过类型的列 | 8 | 中 | 无 |
| `RotatedQuery` | 无 | 查询层：表头、列类型、与条件组合 | 3 | 中 | 无 |
| `DateCondition`（ToDate、Months、Days、NthQuarter、NthMonth、NthHalfYear、PartDate） | `DateConditionTest` 等只覆盖本年/上年、本月、本周、本季度 | 固定参考日期，逐个范围手算期望值，含跨年、周起始日 | 16 | 中 | 能否注入"当前时间"；时区与周起始日的规则 |
| `PreAssetQuery.isQueryMergeable` 及 `is*Mergeable` | 无（23 个测试调用的是同名的 `XUtil.isQueryMergeable`） | 条件、聚合、排序、排名、表达式、数据类型组合下是否合并 | 10 | 高（需沙箱） | 哪些组合应当合并 |
| `TextUtil.detectType`、`CSVLoader`、`ImportCSVDialogService`（预览、unpivot、行列上限） | `TextUtil`、上传服务无测试；`CSVLoaderTest` 只测数字/货币/百分比 | 日期、布尔类型识别，分隔符，首行表头，unpivot，上限提示，固定宽度文本，xlsb 的拒绝提示 | 12 | 中 | 日期与布尔的识别规则 |
| `RankingTableLens` | 只有序列化与失败 | top/bottom N 取值、并列 | 6 | 低 | 并列规则 |
| `SummaryFilter2`（worksheet 分组聚合）+ `DateRangeRef` 级别 | 只有序列化与取消 | 各日期级别分组后的聚合值（含资产里零覆盖的 am/pm、millisecond、week of month、day of year、am/pm of day） | 6 | 中 | 无 |
| `NumericRangeRef` | 只测改名与依赖 | 数值分段 | 4 | 低 | 区间边界的归属 |
| 各库方言类（`OracleSQLHelper` 等 8 个） | 引用它们的测试多是别名、引号、like 大小写、嵌套派生表（只看文件名与引用，未读方法体）；`ClickhouseHelper` 2 个、`InformixSQLHelper` 3 个、`DB2SQLHelper` 5 个测试文件引用，最少 | 固定一组查询（日期分组、top N / max rows、集合运算、统计函数、null 排序），断言 8 个库各自生成的 SQL | 待定（按"类别 × 库"展开） | 中 | 各库应当生成什么 SQL；哪些组合在哪个库不支持、应当退回内存 |
| `AggregateFormula`：`SumSQ`、`SumWT`、`Aggregate` | 未盘点 | 三个公式的值与结果类型 | 3 | 低 | 公式语义 |
| **合计** | | | **移植 70 + 新写 65（不含各库方言类一行；聚合公式一行 3 个也未计入）** | | |

按优先级：先移植 13.8（G7 主体），再写 date in（G4）和上传（G3），unpivot（G7）次之，合并判断（G5）要先等开发确认规则。各库 SQL 生成（G11）和合并判断一样要先确认规则，排在其后。

## 5. Dashboard 与机制补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 严格模式（G1） | 改 `WorksheetTest`：导出失败让用例失败、每张表单独捕获后汇总、基线文件必须都已导出、`-Dupdate.baseline`、清理 case 目录；`importCSVToEMTable` 空值安全并在失败时报错；`exportVSComponentData` 同样 | 打不开、查询异常、上传失败、少导出都会变红 | **已完成（待合并）：** G2 的 40 个已随 spec 删除、G3 的 6 个已修好、G10 的 3 个陈旧基线已删除 |
| `WSBuildServer_Spec`（G2） | 二选一：补回 `BuildServer` 资产并去掉 Linux 跳过；或删除 spec 与 302 个基线 | 客户场景回归 | **已决定删除并处理（待合并）**；日后找到资产再补回 |
| 上传（G3） | 修好基类后重跑 6 个用例，确认导出与基线一致 | 上传接线 | **已完成（待合并）：** 根因是测试配置的 mock 和 spec 里的乱码，重跑后 6 个用例都与基线一致 |
| date in（G4） | 语义放单测；dashboard 留 1～2 个代表用例，不再依赖改操作系统时间 | 条件接线 | 产品是否支持测试用的参考日期（待开发确认） |
| 执行路径（G5） | 基类记录每张表是否合并（或生成的 SQL）并纳入基线，先用于 `checkMergeable` 用例 | 下推与否的回归 | 需找到取合并结果的入口（未查） |
| 运行时取数（G6） | 基类支持 `RUNTIME_MODE`，每种查询类型选 1 个代表 worksheet | viewsheet/MV 实际取数路径 | 无 |
| 薄弱格子（G8） | 新建小 worksheet：`>=` join、date in、LIKE/starting with、Rotated/Unpivot/Concatenated 上的后置条件、ClickHouse | 这些组合的接线 | tester 衡量做哪些 |
| 各库差异（G11） | 为这几类各选 1 个代表场景：日期分组、top N / max rows、minus/intersect/full join、统计函数、null 排序；同一场景在 8 个库各建一个小 worksheet（用各库自己的表），基线按库分开；先用 G5 的"是否下推"记录确认走的是哪条路径 | 各库方言生成与取数结果 | G5 的机制；各库造数方式（待定）；ClickHouse 用例目前 `@Ignore`，先确认原因 |
| 功能面零覆盖（G12） | 新建小 worksheet：日期范围镜像与可复用条件 assembly、变量样式 `DATE_COMBOBOX` 与未覆盖的数据类型、命名分组作列引用（`NamedRangeRef`）、未设置的配置项（用 `initHome` 的 properties 参数）；表类 Cube/Data/QueryBound/Scripted/Tabular 先确认是否可创建 | 这些能力的接线 | tester 衡量做哪些；表类待开发确认 |
| 未运行的 worksheet（G9） | 核对 49 个是否作为来源被引用，再补用例或删除 | | 无 |

做法原则同 vscalc：新建小 worksheet，不改已有资产；改 zip 后用 `ws_scan.py inventory` 前后对比。

## 6. 局限与待确认

**需要开发回答：**
1. `DateCondition` 能否注入参考日期（时区、周起始日规则一并确认）？否则 date in 只能靠改系统时间测试。
2. worksheet 层的合并规则：哪些条件、聚合、表达式、数据类型组合应当下推到 SQL。
3. 上传的日期、布尔类型识别规则；`NumericRangeRef` 区间边界；排名并列规则。
4. 各库方言：日期分组、top N / max rows、集合运算、统计函数在每个库上应当生成什么 SQL；哪些组合在某个库不支持、应当退回内存计算（例如 MySQL 8.0.12 是否下推 `INTERSECT`/`EXCEPT`、`FULL OUTER JOIN`）。

**需要用户决定：**
- 严格模式后数据库不可用也会让 CI 变红（以前异常被吞掉）：`ensure-db-server` 的等待和重试是否足够。
- ws 的 CI：只能手动触发、失败不挡合入、Jenkins 无 ws 阶段，是否调整。
- G8 做哪些新场景。
- G11 每类选哪些代表场景、各库用哪些表造数。

**没验证的：**
- 评估和处理后的运行都在本机（Windows）；CI 上严格模式后的结果未确认（CI 用同一份测试配置，上传的根因应相同）。
- 严格模式的整套重跑在清理 3 个陈旧基线之前，之后只重跑了 `DataComposition_Spec`、`WSOthers_Spec` 和 `checkMergeable CM1`。
- `exportVSComponentData` 遇到既不是图表也不是表格的组件，会把上一个组件的数据再导出一遍，未改。
- 执行路径是静态信号（`mergeable=false`），实际是否下推没有运行确认。
- 49 个未运行的 worksheet 是否被其他 worksheet 引用为来源，未核对。
- 13.8 测试移植到 StyleBI 的兼容性未逐个核对，用例数按 13.8 的测试数计。
- `LIVE_MODE` 与 `RUNTIME_MODE` 的差别只确认了预览输入行数上限一项。
- 命名分组、变量、日期范围已在功能面差集里按能力清单统计，但它们在 worksheet 里的实际用法（命名分组是否被表应用、变量值如何传入）没有逐个核对。
- 功能面差集里 `InRange_Spec` 的日期范围名与 `DateCondition` 子类的对应只按名字判断；`AggregateFormula` 三个零覆盖公式在产品单测里有没有测试未盘点。
- 单测盘点中，除注明"只有序列化"的几个类外，其余按方法名判断；`SortFilter` 自身的测试未盘点。
- 库 × 处理步骤的交叉统计没有做（`ws_scan.py` 只按数据源统计表数），G11 里各库覆盖只有表数；各库方言类相关测试只看了文件名和引用，未读方法体。

## 7. 已知 bug 对照（步骤 3b-D）

范围：Redmine "Product" 项目、分类 "Worksheet Execution"、跟踪类型 Bug、2025-11-01 至 2026-11-11 创建，共 118 条（Closed 103、Rejected 11、Resolved 2、New 2）。
查询条件：`project_id=1`、`category_id=19`、`tracker_id=1`、`status_id=*`、`created_on>=2025-11-01`。该分类全部 Bug 共 2524 条，只取最近一年。
118 条里除 #81047 外只读了标题，没有读描述和日志；根因类别按我确认过的编号和标题关键词粗分，**是粗分**，分桶规则可复现。
"当前是否被覆盖"只写 datatest 一侧，产品单测一侧没有盘点（这些 bug 修复时是否带了单测未查）。

**与数据结果直接相关的 27 条**（其余 80 条见表后，Rejected 11 条不计）：

| bug id | 根因类别 | 当前是否被覆盖（datatest） |
|---|---|---|
| [#81047](https://issues.inetsoft.com/issues/81047)（New） | 表达式列：脚本函数名（`year`、`dateAdd`、`month`）与同表列名冲突，表悄悄只显示 1 行样例数据 | 否（静态）：脚本表达式里有 `year(` 4 处、`dateAdd(` 9 处、`month(` 1 处，但没找到与同表同名列的组合；`range&formula` 里同名的 `year`、`dateadd` 是 SQL 表达式列，不是同一触发条件 |
| #79997（Resolved） | 表达式列：脚本里与自己的列同名的顶层变量 | 未验证：有 8 处 `var x =`，是否与列同名未核对 |
| #77999 | 表达式列：`new Date(field['…'])` | 否：`new Date(` 有 2 处，没有 `new Date(field[` |
| #73000 | 表达式列：脚本引用另一个数据块 | 否：按 `data[`、`tables[`、`ws.` 搜，0 处（只搜了这几种写法） |
| #75662、#75698 | 表达式列：公式数据与改成分组后取不到数据（#75423 自动化 ws） | 未验证 |
| #79841 | SQL：SQL 表 WHERE 里的内置会话变量（`$(_USER_)`）与另一个提示变量同时存在时 NPE | 部分：`_USER_`、`_ROLES_`、`_GROUPS_` 在 1 个 worksheet（CI 范围内），是否与另一个提示变量同表未核对 |
| #77860 | SQL：schema 限定表的表限定列 + 行数上限预览 | 否：没有用例设置或检查预览行数上限（见 G6） |
| #75323、#75322 | SQL：子查询条件取错列、生成的 SQL 报错 | 部分：子查询在 5 个 worksheet 里出现（CI 范围内 3 个），具体写法未核对 |
| #74534 | SQL：自动生成的笛卡尔 join 导致 167 秒延迟 | 否：没有性能断言 |
| #74213 | SQL：查询没有继承 VPM | 未验证：VPM 在 vpm 模块 |
| #74100 | SQL：postgres orders 库执行查询异常 | 部分：PostgreSQL 在 CI 范围只有 5 张表（G11） |
| #78225 | 上传：JVM 区域为 th_TH 时 Excel 日期读早 543 年 | 否：spec 没有设置区域，只在一种 JVM 区域设置下运行（具体值未核实） |
| #74328 | 上传/导入：导入 vso 后部分数据缺失 | 未验证 |
| #80154（New） | 变量类型：Long 变量默认值是表达式时提示框显示 3.0E9 | 否：long 变量在资产里为 0（G12） |
| #77248 | 提示信息：join / 并集的子查询警告（如达到最大行数）不显示 | 否：断言只比表数据，spec 里没有检查警告的代码 |
| #73485、#73375、#73368、#73322、#73134、#73422 | 快照：导出、导入快照后数据缺失；快照嵌入表取不到数据；全部节点重启后数据丢失 | 否：spec 里没有快照的导出、导入、重启流程；快照嵌入表的取数只有 20 张表（CI 范围） |
| #79840、#73364、#73361、#73073 | 重命名与依赖传播：重命名列、表、数据源文件夹后依赖方取不到数据；删除被 viewsheet 依赖的列 | 否：spec 里没有重命名或删除的流程 |

**不属于 ws datatest 能验证的 80 条**（按标题关键词粗分）：

| 类别 | 条数 | 例 | 为什么不在范围 |
|---|---|---|---|
| UI / 浏览器控制台 | 24 | #77023、#76941、#75845、#75317 | 界面与对话框，不产出数据 |
| XSwapper / 内存交换 | 21 | #77877、#77676、#77622 | 内存与磁盘交换，需要压力和并发场景 |
| 并发 / 死锁 | 9 | #77874、#77873、#77311、#77681 | datatest 单线程顺序执行，需要并发单测 |
| 数据源认证 / 缓存 | 7 | #77730、#76658、#75737 | 令牌与缓存键，不在 worksheet 数据 |
| 无状态会话 | 7 | #74884、#73736、#73076 | 节点宕机、序列化、会话 |
| Worksheet MV | 3 | #78452、#78340、#78339 | 归 mv 模块 |
| 损坏数据导入的健壮性 | 3 | #77584、#77499、#77412 | 导入畸形数据的内存与异常，不是取数结果 |
| 其他 | 6 | #79843、#79842、#78053、#76236、#74108、#73121 | 资产导入、权限、图表绑定 |

**结论：** 数据相关的 27 条里，没有一类在 datatest 里有明确覆盖；覆盖最薄的是重命名与依赖传播、快照、提示信息、JVM 区域、Long 变量，以及脚本表达式的几种写法（函数同名、`new Date(field[…])`、引用其他数据块）。对应缺口见 G13。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py crosscheck ws/src/test/resources/config/wsStorage.zip ws/src/test/resources/expectData ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
./mvnw package -pl commons -DskipTests && ./mvnw test -pl ws
# 更新基线：加 -Dupdate.baseline=true（导出文件写回 ws/src/test/resources/expectData，不比对）
# 本机删除过 spec 或基线后先 ./mvnw clean -pl ws，target 里的旧拷贝会让严格模式误判
```
