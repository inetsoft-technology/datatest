# ws 测试缺口与补充方案

> 日期：2026-11-10　模块：`ws`（无 profile，`./mvnw test -pl ws`），spec：`ws/src/test/groovy/inetsoft/test/worksheet/cases/` 下 6 个（评估时 7 个，`WSBuildServer_Spec` 已删除）。2026-11-11 更新：G1、G2、G3 的假通过、G10 已在分支 `datatest-ws-strict` 处理，PR #213 已于 2026-11-11 合入 `Stateless-Sessions1`，状态见各节
> datatest 基线：`Stateless-Sessions1`（`f83c650a`）；产品源码：`main`（`origin/main` `f0ef913306`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 一致）。

## 1. 结论

ws 的用例规模不小（评估时 98 个用例、873 个基线文件；删除 `WSBuildServer_Spec` 后 58 个用例、568 个基线 txt），团队设计表 `docs/ws/Worksheet Auto.xlsx` 里的组合也基本都做成了用例。**但评估时真正在验证数据的不到一半（98 个里 44 个）；严格模式处理后，58 个用例里除 `InRange` 和 4 个 `@Ignore` 外的 50 个都真正比对了数据。**

- 断言基类 `WorksheetTest` 吞掉异常、只比实际导出的文件，[#203](https://github.com/inetsoft-technology/datatest/pull/203) 只改了 `ViewsheetTest`，没有覆盖它。本机实际运行：98 个用例里 90 个"通过"，其中 46 个什么都没导出（`WSBuildServer_Spec` 40 个：worksheet 不在 zip 里；上传用例 6 个：基类第 214 行空指针），真正比对了数据的只有 44 个。**已处理（分支 `datatest-ws-strict`，PR #213 已合并）：** `WorksheetTest` 改为严格模式；上传用例的根因是测试 Spring 配置把 `ImportCSVDialogServiceProxy`、`FileSystemService` 换成了 Mockito mock（预览返回空结果、缓存文件为 null），已改为真实对象；`WSProperty_Spec` 里 5 处 `中文` 被写成乱码（`涓枃`），按乱码找不到 sheet，已改正；`WSBuildServer_Spec` 连同 302 个基线已删除；3 个陈旧基线已删除。
- CI（GitHub，ubuntu）上 `InRange_Spec` 整个跳过（评估时 `WSBuildServer_Spec` 也跳过，已删除），所以**表条件里的 date in（相对日期）在 CI 上是 0 覆盖**；GitHub 工作流只能手动触发，失败也不挡合入；Jenkins 没有 ws 阶段。
- 所有用例都用设计时的 `LIVE_MODE` 取数，从没走过 viewsheet / MV 实际用的 `RUNTIME_MODE`；名为 `checkMergeable` 的用例也只比数据，不检查查询是否下推到 SQL。
- 各库的 SQL 差异没有被区分：CI 用 `gcp-db-server-iac` 启动的 8 个库（MySQL、PostgreSQL、SQL Server、DB2、Oracle、Informix、Derby、ClickHouse），但 CI 范围内绑这 8 个库的 212 张表里 Oracle 占 122 张，ClickHouse 因 `@Ignore` 为 0，PostgreSQL 5、服务器上的 Derby 2（嵌入式 Derby 的 `Examples/Orders` 另有 18 张）；日期分组、top N / max rows、集合运算、统计函数在各库的 SQL 生成，既没有 dashboard 用例按库对照，产品单测也未见专门覆盖（G11）。
- 产品单测（评估时）：`RotatedQuery`、`UnpivotQuery`、上传与类型识别、worksheet 层的合并判断没有任何测试；intersect、minus、cross join、rotate、max rows 只有序列化测试。**2026-11-12 现状：** 已补 19 个测试类（4 个移植自 13.8、15 个新写），共 340 个测试，覆盖 lens、unpivot、`RotatedQuery`、排名并列、日期级别与日期条件、数值分段、各库日期与统计公式的 SQL、上传的类型识别与 `CSVLoader`、`XUtil.isQueryMergeable`；产品仓库 PR [#8046](https://github.com/inetsoft-technology/stylebi/pull/8046)。写测试发现的 8 个缺陷（#81087、#81091、#81092、#81093、#81095、#81096、#81102、#81105）都已在 `main` 修复，另报 #81178、#81193、#81220、#81229 待处理（#81193 是 #81102 修复带来的回归）。第二批（产品分支 `test-csvinfo-and-mergeable-unit-tests`，本地、未推送）又补了 `CSVInfo` 的分隔符与表头与编码猜测和 `PreAssetQuery` 的全部 `is*Mergeable`，6 个类、246 个测试，见 4b-H2。仍没有的：上传服务（环境原因，见 4b-G）、各库真正执行（只有 Derby）、unpivot 后聚合与日期类 `changeColumnType` 与 MV、`NamedRangeRef` 等零覆盖项。

已做：`WorksheetTest` 改成和 `ViewsheetTest` 一样的严格模式，并处理了因此变红的假通过和 3 个陈旧基线（PR #213 已合并）；移植 13.8 的 lens 测试，补 unpivot、date in、上传类型识别、各库 SQL 差异（生成层）的单测（PR #8046）。接下来：重新验证 `property_crossJoin.maxCellCount`（#81087 已修）；`PreAssetQuery` 合并判断的单测与基类记录"是否下推"（G5）；G8、G11、G12 的 dashboard 新场景要先定做哪些。

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
- 条件运算符：前置 equal 25、greater 24、one of 18、less 16、null 12、between 8、contains 5、starting with 2、like 1；后置共 15；排名 top 9、bottom 4。**表条件里的 date in 在 CI 范围内为 0**（只出现在 `InRange_Spec` 的 worksheet 里）。表条件之外另有 4 个 date in 条件，分布在 3 个 assembly：命名分组 `Date` 里 2 个，日期范围 `NG_SR`、`NG_FR` 里各 1 个（都在 `postprocess` worksheet，由 CI 范围内的 `DataComposition_Spec` 运行）；它们是否被表应用未核实，明细见功能面差集。`ws_scan.py` 只读表上的条件，不读命名分组和日期范围。
- 聚合公式 24 种，Sum 59、Count 13、Max 12，其余多为 1～2 次；日期分组 16 个级别；范围列 DateRangeRef 139、NumericRangeRef 18；表达式列 SQL 113、脚本 82。
- 数据源（表数）：Oracle 122、MySQL 41、mvtest 27、DB2 21、Examples/Orders 18、SQL Server 12、Informix 9、sqlite 5、PostgreSQL 5、Access 3、securityTest 3、Derby 2；ClickHouse 用例被 `@Ignore`。
- CI 的数据库：`gcp-db-server-iac` 用 Docker Compose 启动 8 个库：MySQL 8.0.12、PostgreSQL 11、SQL Server 2017、DB2 10.5、Oracle XE 11g、Informix 14.10、Derby 10.15、ClickHouse 22.1；`test-ws.yml` 测试前执行 `ensure-db-server`。ws 的服务器类数据源正好是这 8 个，Access、sqlite、mvtest、Examples/Orders、securityTest 是本地库，数据库类型以 JDBC 地址为准：`Examples/Orders` 是嵌入式 Derby（`jdbc:derby:classpath:orders_133`），`Access`、`mvtest`、`securityTest` 三个数据源都是 H2（`Access` 只是名字），sqlite 是 sqlite。CI 范围内绑这 8 个库的表共 212 张（Oracle 122、MySQL 41、DB2 21、SQL Server 12、Informix 9、PostgreSQL 5、Derby 2、ClickHouse 0）；逻辑模型查询只落在 Oracle、MySQL、DB2 三个库，其余库只有物理表或手写 SQL。来自 `ws_scan.py inventory` 的 Bound sources。
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
- **处理后**（2026-11-11，严格模式，整套重跑）：58 个用例中 50 个通过且基线文件全部已导出（`DataComposition` 10、`WSAsset` 2、`WSData` 22、`WSOthers` 8、`WSProperty` 8），`InRange` 1 失败 3 跳过（改系统日期的 `runas` 未生效，同修改前），4 个 `@Ignore` 跳过。`WSProperty_Spec` 的上传 6 个用例现在真正导出，并与基线一致。`checkMergeable CM1` 在清理陈旧基线后单独重跑通过（整套重跑在清理之前）。`property_crossJoin.maxCellCount` 单独运行会失败（修改前同样），先跑 `WSData_Spec` 则通过；2026-11-11 已查清原因：`CrossJoinTableLens` 把最先加载完的那一侧设为 `main`（变化最快的一维，`CrossJoinTableLens.java:263-273`），两侧的行数由两个线程统计，谁先完成是竞态，所以同一个 cross join 的输出行序不固定；`Query1` 是三表 cross join，输出被预览上限（`query.preview.maxrow`=5000）截断，`TestData` 与 `All Sales1` 的因子顺序互换后保留的 5000 行完全不同（基线是 143 个 id × 35 个客户，失败时是 1 个 id × 143 个订单号 × 35 个客户），而基线只记录了其中一种。已排除：输入表的原始行序（两种情况相同）、表声明顺序和运算符对（相同）、别的 worksheet 的缓存（只有这一个 worksheet 含这三张表）、依赖内存状态的 `JoinTableLens` 分支（`Query1` 用不到）。已报产品缺陷 Redmine #81087（附单元层面的复现：同样两张已加载完的小表，300 次里最快变化的一侧有 0.3%～2% 会翻转）。CI 上这个用例是否稳定取决于竞态结果，没有确认；用例本身怎么处理（改用例或放宽比对）待定。
- 基线来源：`expectData` 共 27 次提交，均为"add exp""update txt"一类，是产品输出生成的回归基线，不是独立标准答案。

**断言层：** 每张可见表导出为整表 txt（排序后），没有 PNG。隐藏表（13 张）不导出，只通过依赖它的可见表间接验证。查询是否下推、生成的 SQL、预览行数上限都不在断言里。

**功能面差集（步骤 3b-A）：**

口径：清单来自产品 `main`（`333566db8`，2026-11-11；路径前缀 `community/core/src/main/java/inetsoft/`）的常量、枚举和类声明，位置写在表里；
次数 = 该能力在 worksheet XML 里出现的次数（含隐藏表），"全部"是 94 个 worksheet，"CI"是 CI 范围的 42 个 worksheet（口径同上文）。
XML 看不到运行时传入的值（变量值、`InRange_Spec` 的日期范围名），这类能力单独说明。统计命令：`ws_scan.py capability`、`ws_scan.py flagcheck`（见附录）。

| 能力（产品清单） | 位置 | 全部 / CI | 零覆盖项 | 会放过什么错误 |
|---|---|---|---|---|
| 表 assembly 类（可实例化 15 个） | `uql/asset/*TableAssembly.java` | 用到 11 个 | `CubeTableAssembly`（:36）、`DataTableAssembly`（:35）、`QueryBoundTableAssembly`（:34）、`TabularTableAssembly`（:46）：0 / 0 | 这些表类的取数出错；是否能由用户在 worksheet 里创建、CI 有没有对应数据源，**未核实**；`ScriptedTableAssembly` 是接口（`TabularTableAssembly` 实现它），不单列 |
| 非表 assembly 类（8 个） | `uql/asset/*Assembly.java` | 用到 5 个：变量 25 / 17、命名分组 23 / 22、日期范围 2 / 2、镜像变量 1 / 1、镜像命名分组 2 / 2 | `MirrorDateRangeAssembly`、`DefaultConditionAssembly`、`MirrorConditionAssembly`：0 / 0（worksheet 里 `conditionAssemblies` 节点全是空的） | 可复用条件 assembly 与日期范围镜像的求值出错 |
| 日期分组级别（21 个，不含 none） | `uql/XConstants.java:69-158` | 用到 16 个 | `week of month`（:130）、`day of year`（:134）：0 / 0；`am/pm`（:93）、`millisecond`（:109）、`am/pm of day`（:146）只是常量，`DateRangeRef.getData` 不支持（抛 "Unsupported option"），范围列没有这些级别，不算零覆盖 | 前两个级别的分组边界、标签算错 |
| 聚合公式（27 个） | `uql/asset/AggregateFormula.java:43-149` | 用到 24 个（含 none） | `SumSQ`（:141）、`SumWT`（:145）、`Aggregate`（:149）：0 / 0 | 三个都是内部构件（MDX / 组合 SQL 用），用户选不到；不单独补测 |
| join / 并集运算 | `uql/asset/TableAssemblyOperator.java:47-107` | 14 个可选运算用到 13 个 | `>=` join（:75）：全部 1 / CI 0（在没被运行的 worksheet 里） | `>=` 连接条件的取值错 |
| 相对日期条件（`DateCondition` 子类 13 个） | `uql/asset/DateCondition.java:525-2443` | XML 里只看到 `YearCondition` 3 / 3、`QuarterCondition` 2 / 2（`postprocess` 的日期范围和命名分组，CI 运行） | 其余 11 个在 XML 里为 0 / 0；但 `InRange_Spec` 的 4 个 feature 用变量传入范围名（Year to date、Yesterday、Week before last week、This January、Last 3 months、Last 8-14 days、1st quarter this year、1st half of this year 等），按名字看几乎覆盖全部子类，**未逐个对应到子类**；该 spec 在 CI 跳过 | 相对日期范围算错（G4） |
| 条件运算（15 个）及取反、等于标志 | `uql/XCondition.java:38-129` | 表条件里 date in：全部 3 / CI 0；like 1、starting with 2；取反 25 / 22、等于标志（>=、<=）26 / 23 | 命名分组里的条件另计：date in 2 / 2（都在命名分组 `Date`）、between 15 / 15 等；日期范围 assembly 里 date in 2 / 2（`NG_SR`、`NG_FR` 各 1）；`pseudo`、`correlated`：0 / 0 | 见 G8 |
| 变量样式（6 个） | `uql/schema/UserVariable.java:50-70` | NONE、COMBOBOX、LIST、RADIO_BUTTONS、CHECKBOXES 都用到 | `DATE_COMBOBOX`（:70）：0 / 0；另有 17 个变量没写 `style`，按默认处理**未核实** | 日期变量的取值与传参 |
| 变量数据类型（13 个） | `uql/schema/XSchema.java:46-98` | CI 用到 5 个：string 18、boolean 2、timeInstant 3、date 3、time 1 | CI 零：float、decimal、char、byte、short、long（全部也为 0）、double 与 integer（只在 CI 外各 1） | 变量按类型转换并传给 SQL 时出错 |
| 列引用类型 | `uql/asset/*Ref.java` | ExpressionRef 225 / 196、DateRangeRef 148 / 140、NumericRangeRef 18 / 18 | `NamedRangeRef`：0 / 0（命名分组有 23 个，但没有表以列引用方式使用它，是否用别的方式应用**未核实**） | 命名分组作为分组列出错；`SQLExpressionRef` 是接口（三个范围引用实现它），XML 里不会以它为类名出现，不算零覆盖 |
| 配置项（读取位置） | 见右 | 已设置 2 个：`join.table.maxrows`（`ComposedTableAssembly.java:501`）、`crossJoin.maxCellCount`（`AbstractTableAssembly.java:1951`） | 未设置：`asset.sample.maxrows`（`WorksheetInfo.java:40`）、`formula.avg.keep.type`（`AggregateFormula.java:656`）、`query.cache.data`（`AssetDataCache.java:508`）、`asset.date.conditions`（`DateCondition.java:185`）、`jdbc.query.filter`（`JDBCHandler.java:186`） | 这些开关打开或改值后的行为；`query.preview.maxrow`、`query.analysis.maxrow`、`output.null.to.zero` 在 viewsheet 路径上，归 G6 |
| 上传的文件类型与限制 | `uql/util/filereader/TextFileType.java:36-52`、`web/composer/ws/dialog/ImportCSVDialogService.java` | 类型靠文件内容识别（`:187`、`:256`、`:521`），`resources/files` 里有 delimited 文本、xls、xlsx | 固定宽度文本 `FIXED`（`TextFileType.java:36`）、xlsb 的拒绝提示（`ImportCSVDialogService.java:704`）、文件大小与行列上限提示（`:709`、`:639`）：没有对应文件和用例 | 这些路径的解析和提示出错 |

- `model.fileType()` 在上传服务里没有被读取（类型由 `TextUtil.detectType` 从文件内容判断），所以 spec 里设置的 `fileType`（模板里的 `XLSX`、用例里的 `"DELIMITER"`，产品枚举是 `DELIMITED`）实际不起作用，不要当成"选了某种文件类型"。
- 日期的相对范围（G4）靠变量传入：`condition_inrange` 里 3 张表的 date in 条件 `condition_data` 为空，范围名来自 `InRange_Spec` 传的变量。
- `join` 和 `concatenation` 在 `TableAssemblyOperator` 里是基类标志位（`:47`、`:95`），不是用户可选的运算，不算零覆盖。

**生成型补充（步骤 3b-C，只做 SQL 下推判断）：**

ws 的核心不是"由 A 生成 B"，只有"某个查询下推到 SQL 还是退回内存"这一个判断点符合，G5、G11 都依赖它，所以只对它做分支清单；运行时探测（3b-B）和输出形态矩阵（3b-E）没有做。
判断入口：`PreAssetQuery.isQueryMergeable`（`report/composition/execution/PreAssetQuery.java:2992`）→ `isQueryMergeable0`（`:3035`），按顺序检查：数据源（`isSourceMergeable`，各查询类覆盖）、作为子查询的 SQL、where、group by、having、order by、select、ranking，任何一步不通过就整体退回内存。
下表每行是一个分支（位置在 `PreAssetQuery.java`，其他文件单独写出），状态是"CI 范围的资产有没有会走到它的"：有 / 部分（有，但库或写法未对上）/ 无 / 未验证（只看了代码，没有核对资产）。全部是静态判断，没有运行确认。

| 阶段 | 分支 | 结果 | CI 范围里哪些资产会走到 | 状态 |
|---|---|---|---|---|
| 数据源 | `EmbeddedQuery.java:166`、`DataQuery.java:133`、`CubeQuery.java:897` | 恒不可合并，嵌入表一律在内存算 | 嵌入表 40 张（含快照 20 张）；Data、Cube 无表 | 有 |
| 数据源 | `AssetQuery.java:890`：表 `isSQLMergeable()` 为 false（用户关闭合并），或含运行时 MV | 不可合并 | `mergeable=false` 7 张；运行时 MV 归 mv 模块 | 有 |
| 数据源 | `BoundQuery.java:285`：SQL 表走父类，其余用 `smergeable`（`XUtil.isQueryMergeable`，`:83`）；`PhysicalBoundQuery.java:130`、`LMBoundQuery.java:155` 恒为 true | 逻辑模型与物理表默认可合并 | 物理表 114、逻辑模型 63；`XUtil.isQueryMergeable` 的分支未读 | 未验证 |
| 数据源 | `MirrorQuery.java:311`：子查询可合并、库支持 `MIRROR_TABLE`、`FROM_SUBQUERY`；有最大行数而库不支持 `MAXROWS` 时不可合并 | 镜像表能否下推取决于库 | 镜像表 51 张；各库的支持情况未核对 | 未验证 |
| 数据源 | `JoinQuery.java:154`：库支持 `FROM_SUBQUERY`、`MAXROWS`、`FULL_OUTERJOIN`；`join.table.maxrows` 非 0；所有子查询可合并；同一数据源 | join 能否下推取决于库和配置 | join 65 张，full join 5 张；`join.table.maxrows` 只有 `propertyMaxRows` 设置过；full join 在哪些库未核对 | 部分 |
| 数据源 | `ConcatenatedQuery.java:319`：两表时 union / intersect / minus 分别需要库支持 `UNION_TABLE` / `INTERSECT_TABLE` / `MINUS_TABLE`；子查询可合并；同一数据源 | 并集运算能否下推取决于库 | union 10、intersect 5、minus 6；哪个库不支持哪个运算待开发确认 | 未验证 |
| 子查询 | `PreAssetQuery.java:3042`：作为子查询且 SQL 不可合并 | 不可合并 | 同上，`mergeable=false` 的表被 join、镜像引用时 | 未验证 |
| where | `:3112`、`:3143`：条件列或条件值是列，且该列不可合并（数据类型、脚本表达式列） | 不可合并 | 前置条件的表 90 张（全部 worksheet）；落在脚本表达式列上的条件未核对 | 未验证 |
| where | `:3118`：mongo 的 `ONE_OF` | 不可合并 | CI 无 mongo | 无 |
| where | `:3124`：相对日期条件（`DateCondition`，不含 `DateRange`）直接视为可合并 | 可合并 | 表上的 date in 在 CI 范围为 0 | 无 |
| where | `:3128`：条件不是 `Condition` 类型 | 不可合并 | 未核对 | 未验证 |
| where | `:3156`-`:3185`：子查询值：数据源不同（`:3166`）、子查询不可合并（`:3176`）、库不支持 `CONDITION_SUBQUERY`（`:3183`） | 不可合并 | 子查询条件在 3 个 worksheet（CI 范围），写法未核对 | 部分 |
| group by / having | `:3293`：交叉表聚合 | 不可合并 | 只在 viewsheet 路径（G6） | 无 |
| group by / having | `:3306`、`:3373`：排名带 "Group Others"、嵌套分组上的排名、按另一个数据引用排名 | 不可合并 | 排名表 13 张，具体写法未核对 | 未验证 |
| group by / having | `:3319`：distinct + 子表 + 选出的列数不等于分组加聚合数 | 不可合并 | distinct 表 21 张（全部 worksheet），该组合未核对 | 未验证 |
| group by / having | `:3414`：时间序列分组 | 不可合并 | 未核对 | 未验证 |
| group by / having | `:3485`：命名分组的分组 | 不可合并 | 命名分组在 22 个 worksheet 里定义，是否用作分组列未核实 | 未验证 |
| group by / having | `:3440`：SQL 表达式分组需库支持 `EXPRESSION_GROUPBY`；`:3453`：该库没有此日期分组函数（`UnsupportedOperationException`） | 不可合并，因库而异 | 日期分组 21 个级别用到 16 个，5 个零覆盖，库分布以 Oracle 为主（G11） | 部分 |
| group by / having | `:3351`：条件或表达式需要非分组列 | 分组与 having 改在内存做 | 未核对 | 未验证 |
| 聚合公式 | `:3498`：百分比聚合 | 不可合并 | 只在 viewsheet 路径 | 无 |
| 聚合公式 | `:3516`-`:3524`：`MODE`、`FIRST`、`LAST`、`PRODUCT`、`CONCAT`、`NTH_LARGEST`、`NTH_SMALLEST`、`NTH_MOST_FREQUENT`、`PTH_PERCENTILE` | 任何库都不下推 | First 8、Last 2、Mode 1、Product 1、Concat 1、NthLargest 1、NthSmallest 1、NthMostFrequent 1、PthPercentile 1；是否绑在库表上未核对 | 部分 |
| 聚合公式 | `:3529`：`MEDIAN` 取决于库（`XUtil.supportMedian`） | 因库而异 | Median 1，在哪个库未核对 | 部分 |
| 聚合公式 | `:3534`：Derby 上对整数列 `AVG` | 不可合并 | Derby 在 CI 范围：服务器 2 张绑定表、嵌入式 `Examples/Orders` 18 张，该组合未核对 | 未验证 |
| 聚合公式 | `:3541`：其余公式按 `helper.supportsOperation(公式名)` 逐库判断 | 因库而异 | Sum、Count、Max、Min、Average 等；Variance、StandardDeviation、Correlation、Covariance、WeightedAverage 等在哪些库下推未核对（G11） | 未验证 |
| order by | `:3621`：库的 group 输出无序（H2） | 不可合并 | H2 是本地库（`mvtest`、`securityTest`，以及名为 `Access` 的数据源） | 部分 |
| order by | `:3628`、`:3653`：库不支持 distinct + order by、聚合列 order by | 不可合并，因库而异 | 各库未核对 | 未验证 |
| order by | `:3641`：聚合含 `FIRST` / `LAST` 且有排序 | 不可合并 | First 8、Last 2，是否带排序未核对 | 未验证 |
| order by | `:3690`：隐藏列上的无括号函数关键字排序（#78489） | 不可合并 | 无对应资产 | 无 |
| select | `:1624`：没有可见列 | 不可合并 | 未核对 | 未验证 |
| select | `:1644`：列的数据类型不可合并 | 不可合并 | 未核对 | 未验证 |
| select | `:1652`：脚本表达式列 | 不可合并 | 脚本表达式列 82（CI 范围） | 有 |
| select | `:1664`-`:1675`：日期、数值、命名分组范围列（`SQLExpressionRef`）按库类型和版本 `isMergeable()` | 因库而异 | DateRangeRef 139、NumericRangeRef 18（CI 范围），库分布以 Oracle 为主（G11） | 部分 |
| select | `:1715`：SQL 表达式里引用 `parameter.` | 不可合并 | 变量与 SQL 表达式同表的情况未核对 | 未验证 |
| select | `:1723` 起：表达式里 `field['…']` 引用的列不可合并，或 `getExpressionColumn` 为 null | 不可合并 | 未核对 | 未验证 |
| ranking | `PreAssetQuery.java:3710`：有排名条件即不可合并 | 排名总在内存 | 排名表 13 张（CI 范围） | 有 |

合计 35 个分支：有 4、部分 7、无 5、未验证 19。**已验证覆盖的主要是"恒不可合并"的路径**（嵌入表、脚本表达式列、排名、用户关闭合并）；
因库而异的判断（`supportsOperation`、日期分组函数、`MEDIAN`、范围列）和各种组合条件基本没有核对，对应 G11 和 G5。

**各库 SQL 支持矩阵（3b-C 续，G11）：**

来源：产品 `community/core/src/main/resources/inetsoft/uql/sqlhelper.xml` 的 `unsupported`、`aggregate`、`sqlfunc` 节点，加上三个库在代码里的覆盖（`MySQLHelper.java:87`、`SQLServerHelper.java:104`、`DerbyHelper.java:143-158`）；由 `ws_scan.py dbmatrix` 生成。
✓ 的意思是"没有被列为不支持"，不是"实际验证过会下推"。`sqlite` 在 `sqlhelper.xml` 里没有条目，不在表中。

| 能力（`SQLHelper` 操作名） | Oracle | MySQL | SQL Server | DB2 | Informix | PostgreSQL | Derby | ClickHouse | H2 |
|---|---|---|---|---|---|---|---|---|---|
| union 下推 | ✓ | ✓（版本 > 4.1） | ✓ | ✓ | ✗ | ✓ | ✗ | ✓ | ✓ |
| intersect、minus 下推 | ✓ | ✗ | ✗ | ✓（要求各子查询列名一致，`ConcatenatedQuery.java:402`） | ✗ | ✓ | ✗ | ✓ | ✓ |
| full outer join | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✗ |
| `maxrows`（行数上限写进 SQL） | ✓ | ✓（WHERE 子查询里除外） | ✓ | ✓ | ✗ | ✓ | ✓（WHERE 子查询里除外） | ✗ | ✗ |
| FROM 子查询 | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 表达式 group by（`expression_groupby`） | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗（`expgrp` 打开时除外） | ✓ | ✓ |
| 表达式列 group by（`expression_column_groupby`） | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 聚合列、隐藏列 order by | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 多个 distinct | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ |
| `correlation`、`covariance` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ |
| stddev、var 的 SQL 函数名 | ✓ | ✓ | ✓ | – | – | – | – | – | – |
| correl、covar 的 SQL 函数名 | ✓ | – | – | ✓ | – | – | – | – | – |
| 日期函数个数（0 表示日期分组全在内存） | 22 | 25 | 22 | 30 | 9 | 24 | 9 | 0 | 0 |

- "–" 表示 `sqlhelper.xml` 里没有该函数名；`AggregateHelper.getAggregateExpression` 对这种情况返回 null，注释写明"数据库不支持该聚合函数"，但 `isAggregateMergeable`（`PreAssetQuery.java:3541`）只检查 `supportsOperation(公式名)`，两层怎么衔接没有读。
- 日期分组级别对应的函数在 `DateRangeRef.java:1052-1131`：year、quarter、month、week、day、hour、minute、second 以及 `*_part`、`dayofweek` 各用同名函数；`day of year`、`week of month` 在**任何库**都没有 SQL 函数，只在内存里算，所以它们不存在跨库差异，只需要一条路径。Informix 和 Derby 只有 9 个函数，没有 week、hour、minute、second 等级别。
- 日期分组的 `week` 在用户区域的一周起始日不是周日且设置了 `user.locale.weekGrouping` 时也不下推（`DateRangeRef.java:1040-1048`）。

**现有覆盖对照**（CI 范围，`dbmatrix` 的"场景 × 库"表；派生表算在它读取的库上，含隐藏表，数字是表数）：

- 集合运算：union 落在 Oracle 5、DB2 1、H2 4；intersect 只在 H2 3；minus 在 Oracle 2、PostgreSQL 1、H2 3。**MySQL、SQL Server 的 union 是 0，Oracle、DB2、PostgreSQL 的 intersect 是 0，不下推的库（MySQL、SQL Server、Informix、Derby）上 intersect、minus 的内存回退路径也是 0。**
- full outer join：Oracle 2、SQL Server 1（支持下推）；MySQL 1、H2 2（不支持，走内存）；DB2、PostgreSQL、Informix、Derby 为 0。
- 统计类聚合（stddev、var、correl、covar）：MySQL 1、Derby 1、H2 2；**Oracle、SQL Server 这两个有函数名映射的库是 0**，DB2 的 correl、covar 也是 0。
- 日期分组：MySQL 15、DB2 4、Oracle 3、Derby 1、H2 4、sqlite 2；**SQL Server、PostgreSQL、Informix 是 0**。日期范围列 `DateRangeRef`：SQL Server、Informix、PostgreSQL 各 1 张。
- max rows：Oracle 3、MySQL 1、Derby 6；**Informix、H2（不支持 maxrows，走内存）是 0，DB2、SQL Server、PostgreSQL 也是 0**。
- distinct：Oracle 12、MySQL 5、Derby 1、H2 1；DB2、SQL Server、PostgreSQL、Informix 是 0。
- 规模（表数，含派生表和隐藏表）：Oracle 166、MySQL 79、H2 68、Derby 30（含嵌入式 `Examples/Orders`）、DB2 29、SQL Server 17、Informix 11、PostgreSQL 8、sqlite 6、嵌入表 89；ClickHouse 为 0（`@Ignore`）。**SQL Server、Informix、PostgreSQL 的覆盖都很薄。**

**对 G11 选代表场景的含义：** 同一场景在不同库上走不同路径的，才值得跨库各跑一遍。
1. 集合运算：全部下推的库（Oracle、PostgreSQL）、只有 union 下推的库（MySQL、SQL Server）、都不下推的库（Informix、Derby）各取一个 union 和一个 minus。
2. full outer join：内存回退的库（MySQL、Derby、H2）对下推的库（Oracle、SQL Server、DB2、PostgreSQL）。
3. 统计聚合：有函数名的库（Oracle、MySQL、SQL Server）对没有的库（DB2、PostgreSQL 等）。
4. 日期分组：补上 SQL Server、PostgreSQL；再对 Informix、Derby（缺 week、hour 等级别）和没有任何日期函数的 ClickHouse、H2 各取一个。
5. max rows：支持的库对 Informix、ClickHouse、H2。

**LIVE 模式的边界**（决定先只用 `LIVE_MODE`，G6 暂缓；以下是 LIVE 下测试结果的前提）：

- 输入：每个明细表最多取 `asset.sample.maxrows` = 50000 行（`AssetQuery.java:3957`-`3975`，`defaults.properties:25`）；RUNTIME 模式没有这一项。
- 输出：最多 `query.preview.maxrow` = 5000 行（`PreAssetQuery.java:1162`-`1170`，`Util.java:2897`，`defaults.properties:154`）；RUNTIME 用 `query.runtime.maxrow`，默认 0（不限）。测试主目录没有覆盖这几个属性。
- 568 个基线里有 9 个恰好是 5000 个数据行，即被预览上限截断：`property_crossJoin.maxCellCount` 的 `TestData`、`Query1`，另外 7 个是 `@Ignore` 的 `specialChar`。截断时留下哪些行取决于输入的原始行序。
- 其他 LIVE 与 RUNTIME 的差异：聚合信息和后置条件（LIVE 只在表标了 aggregate 时使用，`PreAssetQuery.java:3209`、`3258`；`WorksheetTest.setLiveData` 用 `setAggregate` 对齐）；行数上限的来源（LIVE 用 `getMaxDisplayRows`，`:1227`-`1247`）；公共列选择（`AssetQuerySandbox.java:117`-`121`）；SQL 表达式失败（非 RUNTIME 抛异常，RUNTIME 吞掉，`AssetQuery.java:4018`-`4021`）；超时（设计时 2 分钟，运行时 20 分钟，`AssetQuery.java:843`-`866`）。

**G5 记录下推的做法（LIVE 模式下）：**

- 入口：对表的克隆调用 `AssetQuery.createAssetQuery(table, LIVE_MODE, box, false, -1L, true, true)`（`ShowPlanService.java:44` 做的是同样的调用，但固定 `RUNTIME_MODE`），再取 `isQueryMergeable(false)`（`PreAssetQuery.java:2992`）和 `getQueryPlan()`。测试配置里没有 ShowPlan，不需要 Spring。
- 计划里有生成的 SQL（`PreAssetQuery.java:3787`-`3792`）、可合并标记（`:3794`），以及 distinct、表达式、where、group by、having 各自是否下推的标记。
- 限制：源不可合并或不偏好合并时 `getQueryPlan()` 返回 null（`:3774`-`3780`），所以先记 `isQueryMergeable(false)` 的布尔值；计划模式用原始别名（`:3785`），SQL 文本和实际执行的别名不同；`getQueryPlan()` 有副作用（`setPlan(true)`、`merge(vars)`），要在克隆上调用。
- 建议先把"布尔值 + 各下推标记行"纳入基线，SQL 文本之后再加；别名和变量串是否稳定没有核对。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | `WorksheetTest` 吞异常、只比实际导出的文件，#203 未覆盖 | worksheet 打不开、某张表查询抛异常、上传失败，用例照样通过（评估时本机有 46 个这样的用例） | 机制：按 #203 的方式改 `WorksheetTest` 为严格模式。**已处理（已合并，#213）** | 高 |
| G2 | `WSBuildServer_Spec` 的 40 个 worksheet 不在 zip，Linux 上又被跳过 | 302 个基线文件代表的客户场景全部未验证，却显示为通过 | 机制/资产：补回资产或删除 spec 与基线。**已决定删除并处理（已合并，#213）** | 高 |
| G3 | 上传用例 6 个假通过；上传服务、`TextUtil.detectType` 无单测 | CSV/Excel 导入的列类型识别、分隔符、首行表头、unpivot 出错 | 机制（同 G1）+ 单测。**假通过已处理（已合并，#213）**，根因是测试配置里的 mock 和 spec 里的乱码；上传类型识别和 `CSVLoader` 的单测已补（`TextUtilTypeDetectionTest`、`CSVLoaderRulesTest`），上传服务不加单测（环境原因，见 4b-G） | 高 |
| G4 | 表上的 date in 条件在 CI 上 0 覆盖（表条件之外的 4 个 date in 条件在命名分组和日期范围里，见覆盖情况）：`InRange_Spec` 在 Linux 跳过，只能在保存了管理员凭据的 Windows 上改系统时间运行；单测只覆盖本年/上年、本月、本周、本季度（2026-11-12 已补 `DateConditionBuiltinTest`：ToDate、最近 N 个月/天、第 N 季度/半年/月、PartDate） | "年初至今""最近 N 个月/天""第 N 季度"等相对日期范围算错 | 单测（固定参考日期，期望值手算）；dashboard 不再改操作系统时间 | 高 |
| G5 | 执行路径未验证：`checkMergeable` 用例只比数据；`PreAssetQuery.isQueryMergeable` 无单测（**2026-11-12 起已补**：SQL 层 `XUtilQueryMergeableTest`，`PreAssetQuery` 的聚合、排序、条件、分组与选择列与排名、来源共 5 个类、216 个测试，见 4b-H2；基类记录"是否下推"这一半还没做） | 本应下推的查询退化到内存（性能问题），或下推与内存两条路径结果不一致（null 排序、字符串比较、日期分组） | 单测（合并规则）+ 基类记录合并结果（入口见第 5 节，分支清单见 3b-C） | 中 |
| G6 | 只用 `LIVE_MODE`，从未走 `RUNTIME_MODE` | 只在运行时出现的错误：viewsheet/MV 读 worksheet 的结果、`runtimeSelected`、预览输入行数上限（`asset.sample.maxrows=50000`，`AssetQuery.java:3957`）以外的行为 | 机制：基类支持 `RUNTIME_MODE`，按查询类型各选代表用例。**暂缓：决定先只用 `LIVE_MODE`**，LIVE 模式的边界见 3b-C 续 | 低 |
| G7 | lens 取值无单测：rotate、unpivot 完全没有；intersect、minus、cross join、max rows 只有序列化；full outer join 没有；非等值 join 只测取消；排名 lens 没有取值测试（**2026-11-12 起已补**：13.8 移植、unpivot、`RotatedQuery`、排名并列，见 4b-H；没补的是 unpivot 之后再聚合、日期类 `changeColumnType`、MV） | 这些操作的结果行、表头、null 处理出错，只有 datatest 回归基线可能发现 | 单测：先移植 13.8，再补缺 | 高 |
| G8 | 薄弱格子：`>=` join、表上的 date in 在 CI 范围为 0；LIKE 1、starting with 2；Rotated/Unpivot/Concatenated 几乎没有后置条件；ClickHouse 被忽略 | 这些组合下的接线出错 | dashboard：新建小 worksheet（tester 衡量） | 中 |
| G9 | 49 个 worksheet 没有用例运行 | 若是有意准备的场景，则对应能力未验证 | 核对后补用例或删除 | 低 |
| G10 | 3 个陈旧基线（`CM1_3`、`CM1_5`、`Report/top1.txt`） | 让人误以为这些对象被验证过 | 随 G1 清理。**已处理（已合并，#213）：** 删除 `CM1_3`、`CM1_5`、`Report/top1.txt` | 低 |
| G11 | 各库 SQL 差异未区分：绑库的表 Oracle 占 122/212，ClickHouse 为 0，PostgreSQL 5、服务器上的 Derby 2（嵌入式 Derby 的 `Examples/Orders` 另有 18 张）；产品单测里引用各库方言类（`OracleSQLHelper`、`MySQLHelper`、`SQLServerHelper`、`DB2SQLHelper`、`InformixSQLHelper`、`PostgreSQLHelper`、`DerbyHelper`、`ClickhouseHelper`）的测试，多是别名、引号、like 大小写、嵌套派生表，日期分组、top N / max rows、集合运算、统计函数的各库生成未见专门测试（生成层已补：`DateRangeRefDialectTest`、`AggregateFormulaStatSqlTest`、`SQLHelperOperationSupportTest`，只有 Derby 真正执行；dashboard 按库对照没做） | 同一个 worksheet 在某个库上生成错误或不支持的 SQL（日期部分函数、`LIMIT`/`ROWNUM`/`TOP`、minus/intersect/full join、null 排序、字符串比较与大小写），换个库正常；下推与内存两条路径的结果不一致（G5）也会因库而异 | 单测：固定一组查询，断言各库生成的 SQL；dashboard：按"场景 × 库"为每类选代表 worksheet，在 8 个库各跑一遍（需先有 G5 的"是否下推"记录）；支持矩阵和现有覆盖见 3b-C 续 | 中 |
| G12 | 功能面零覆盖：产品能力清单里 CI 范围 worksheet 零使用的项（日期分组 5/21、聚合公式 3/27、变量数据类型 8/13、列引用 `NamedRangeRef`、`>=` join、4 个表类、3 个非表 assembly、5 个配置项、上传的固定宽度文本与 xlsb），明细见"功能面差集" | 这些能力出错，没有任何用例会发现 | 能手算期望值的 → 单测（日期分组、聚合公式、相对日期、上传）；接线 → dashboard 小 worksheet；表类、命名分组的用法先向开发确认 | 中 |
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
| `SummaryFilter2`（worksheet 分组聚合）+ `DateRangeRef` 级别 | 只有序列化与取消 | 各日期级别分组后的聚合值（含资产里零覆盖的 week of month、day of year；am/pm、millisecond、am/pm of day 范围列不支持，已用测试固定） | 6 | 中 | 无 |
| `NumericRangeRef` | 只测改名与依赖 | 数值分段 | 4 | 低 | 区间边界的归属 |
| 各库方言类（`OracleSQLHelper` 等 8 个） | 引用它们的测试多是别名、引号、like 大小写、嵌套派生表（只看文件名与引用，未读方法体）；`ClickhouseHelper` 2 个、`InformixSQLHelper` 3 个、`DB2SQLHelper` 5 个测试文件引用，最少 | 固定一组查询（日期分组、top N / max rows、集合运算、统计函数、null 排序），断言 8 个库各自生成的 SQL | 待定（按"类别 × 库"展开） | 中 | 各库应当生成什么 SQL；哪些组合在哪个库不支持、应当退回内存 |
| `AggregateFormula`：`SumSQ`、`SumWT`、`Aggregate` | 内部构件，用户选不到 | 不单独补测（已由统计公式的组合 SQL 间接覆盖） | 0 | – | – |
| **合计** | | | **移植 70 + 新写 65（不含各库方言类一行；聚合公式一行不计）** | | |

**上表"难度"和"待开发确认"两列是 2026-11-10 写的，下面是它们的现状（2026-11-12）：**

| 被测类 | 当时的"待开发确认" | 现状 |
|---|---|---|
| `DateCondition` | 能否注入"当前时间"；时区与周起始日的规则 | 不能注入（子类里直接 `System.currentTimeMillis()`），已按"相对现在写期望、跨日时跳过"写了测试，并在 6 个时区验证；是否改成可注入 `Clock` 是产品的事 |
| `PreAssetQuery.isQueryMergeable` 及 `is*Mergeable` | 哪些组合应当合并 | 已做：规则全部从代码读出并手算期望，"应不应该合并"仍是产品判断；"需沙箱"实际只要 `AssetQuerySandbox` 和 `SQLBoundQuery`，不需要真实数据库（库的类型只来自数据源定义，版本由 mock 的仓库提供） |
| `TextUtil.detectType`、`CSVLoader`、`ImportCSVDialogService` | 日期与布尔的识别规则 | 已从代码读出并写了测试（`CSVLoader` 识别 `true`/`false`，`TextUtil.getType` 不识别）；上传服务因环境原因不加单测，难度实际是"高" |
| `RankingTableLens` | 并列规则 | 已读出：worksheet 排名恰好取 N 行，有意设计（4b-H） |
| `NumericRangeRef` | 区间边界的归属 | 已读出并写了测试；发现脚本版对 null 的问题（#81105，已修） |
| 各库方言类 | 各库应当生成什么 SQL；哪些组合退回内存 | 生成层已由测试固定文本，只有 Derby 真正执行；退回内存的组合（13 个方言没有日期函数）已确认，结果不变 |

**计划与现状对照（2026-11-12）**：上表是 2026-11-10 的计划，现状如下（逐类明细见 4b-H）。

| 计划项 | 现状 |
|---|---|
| lens：join、集合运算、rotate、filter（移植 13.8） | 已做：4 个类、63 个测试 |
| unpivot（`AssetUtil.unpivot`、`UnpivotQuery`） | 已做：`AssetUtilUnpivotTest`（14）、`UnpivotQueryTest`（3）；没做 unpivot 后聚合、日期类 `changeColumnType` |
| `RotatedQuery` | 已做：`RotatedQueryTest`（5） |
| `DateCondition` | 已做：`DateConditionBuiltinTest`（39）；夏令时切换日只能跳过；`YearCondition`、`QuarterCondition`、`MonthCondition`、`WeekCondition`、`WeeksCondition`、`DayCondition` 没逐个核对 |
| `PreAssetQuery.isQueryMergeable` 及 `is*Mergeable` | **已做**（产品分支 `test-csvinfo-and-mergeable-unit-tests`，本地提交、未推送）：聚合、排序、条件、分组与选择列与排名、来源（`MirrorQuery`、`JoinQuery`、`ConcatenatedQuery`、SQL 手写模式）各一个类，共 5 个类、216 个测试，见 4b-H2；没测的只有 `PhysicalBoundQuery`、`LMBoundQuery` 固定返回 true 和 mongo 的真实 helper。SQL 层 `XUtil.isQueryMergeable` 已做（`XUtilQueryMergeableTest`，16） |
| 上传（`TextUtil`、`CSVLoader`、`ImportCSVDialogService`） | 类型识别和 `CSVLoader` 已做（`TextUtilTypeDetectionTest` 43、`CSVLoaderRulesTest` 41）；上传服务不加（环境原因，4b-G）；`CSVInfo` 的分隔符、引号、表头、编码猜测已做（`CSVInfoDetectionTest`，30 个，产品仓库分支 `test-csvinfo-detection`，本地提交、未推送） |
| `RankingTableLens` | 已做：`RankingTiesTest`（6） |
| `SummaryFilter2` + `DateRangeRef` 级别 | 已做：`DateRangeRefLevelValuesTest`（5）、`DateRangeRefFullWeekLevelsTest`（3）、`DateLevelSummaryTest`（5） |
| `NumericRangeRef` | 已做：`NumericRangeRefBoundaryTest`（5） |
| 各库方言类 | 生成层已做：`DateRangeRefDialectTest`（38）、`AggregateFormulaStatSqlTest`（41）、`SQLHelperOperationSupportTest`（13）；只有 Derby 真正执行 |
| 合计 | 实际 19 个类（移植 4 + 新写 15）、340 个测试、1 个 `@Disabled`（#80538） |

按优先级：先移植 13.8（G7 主体），再写 date in（G4）和上传（G3），unpivot（G7）次之，合并判断（G5）要先等开发确认规则。各库 SQL 生成（G11）和合并判断一样要先确认规则，排在其后。

**移植进展（2026-11-11，分支 `test-ws-lens-unit-tests`，已随 PR [#8046](https://github.com/inetsoft-technology/stylebi/pull/8046) 合并，worktree 和分支已清理，见 4b-H）：** 13.8 的 4 个文件已移植为 4 个测试类、共 63 个测试，和已有的 lens、filter 测试放在同一个 JVM 里一起跑通过。

**新写进展（2026-11-11，同一分支）：** 各库方言批次 3 个类已提交：`DateRangeRefDialectTest`（38，16 个日期级别 × 各库是否下推，并在 Derby 上执行对照 java.time）、`AggregateFormulaStatSqlTest`（41，3 个 `@Disabled`）、`SQLHelperOperationSupportTest`（13，`sqlhelper.xml` 与代码覆盖的支持矩阵）。同批次新增：`DateRangeRefLevelValuesTest`（5，19 个级别取值对照 java.time）、`DateLevelSummaryTest`（5，分组求和）。过程中确认：`SummaryFilter2` 只合并相邻行，输入必须先按分组列排序（`AssetQuery` 已排）；`week of year` 是"含 1 月 1 日的周为第 1 周"，12 月末可能落入下一年第 1 周。未覆盖：`week of month`、`week of quarter`、`*_FULL_WEEK` 系列（周的定义要开发确认）。
**统计公式 SQL 缺陷已报：** #81095（协方差/相关系数只有一列为 NULL 时算错）、#81096（单行相关系数除零）。
**unpivot（同一分支）：** `AssetUtilUnpivotTest`（14，`AssetUtil.unpivot` 的列名、类型、行序、null、列数越界、重名）、`UnpivotQueryTest`（3，worksheet 里执行 unpivot 并改列类型）。要点：保留的表头列的值保持原类型，只有列名转成文本；空白表头列用 `Column [n]`；度量列类型不同且都是数字时变 Double，否则变文本（小数按 `0.00##`）；跑 worksheet 测试要 `setActive(true)` 后 `refreshColumnSelection`，否则列为空。

| 测试类 | 来源 | 个数 | 跳过 |
|---|---|---|---|
| `JoinLensesTest` | `LensesJoinsFeatureTest` | 20 | 4 |
| `SetOperationLensesTest` | `LensesSetOperationsFeatureTest`（取值类 18 个，11 个 cancel/dispose 不移植：StyleBI 已有同类测试） | 18 | 1 |
| `SortSummaryFilterTest` | `OperationsTableLensFeatureTest`（去掉 1 个死锁测试） | 15 | 1 |
| `RotateFormulaLensesTest` | `LensesTransformsFeatureTest` 的 rotate 5 个和 `FormulaTableLens` 5 个 | 10 | 0 |

移植时遇到的 13.8 与 StyleBI 的差异，分三种：
- **13.8 已修、StyleBI 没有的缺陷**（对应测试当时 `@Disabled`、写明编号；2026-11-12 起 #81091、#81092 已在 `main` 修复，测试已启用）：Bug #80298（`join.table.maxrows` 把表头行算进上限，`JoinTable.java:111`）、Bug #80297（连接列检查不阻止引擎启动，`JoinTable.java:80-93`），**已在 StyleBI 侧报为 #81092、#81091**；Bug #80538（非 distinct union 在加载中多算一行，StyleBI 侧已有 issue，状态 New，没有动）。
- **StyleBI 里新发现的缺陷（已报 #81093）**：`TextComparer.isAsciiOnly`（`report/filter/TextComparer.java`，2025-06-11 为 Bug #71460 加的快速路径）用 `getBytes(US_ASCII).length == length()` 判断 ASCII，对任何基本平面的非 ASCII 字符都返回 true（被替换成 `?`），所以 collator 被绕过，中文默认区域下按码点排序而不是拼音。已用探针验证，对应的拼音排序测试已 `@Disabled`。
- **有意的行为变化，测试已按 StyleBI 改写**：脚本一律按受限模式运行（Bug #77396），`setRestricted` 只记录标志，能否调用 Java 类由允许类的白名单决定；`FormulaTableLens` 一批一起计算的行只报告一次失败（13.8 是逐行各报一次）。

这批测试都是 lens 层的取值测试，不依赖数据库，覆盖了 G7 的主体（cross join、full outer、非等值、union、intersect、minus、distinct、rotate、top N）以及 G13 里脚本表达式列的计算（多列依赖、前向引用、出错、类白名单）。这一段写于第一批，之后 unpivot、`RotatedQuery`、排名、日期条件、上传类型检测和 `CSVLoader`、`XUtil.isQueryMergeable` 都已补测，现状见 4b-H。

### 4b. 补单测过程中遇到的未覆盖与待确认（2026-11-11，产品仓库分支 `test-ws-lens-unit-tests`，已随 PR #8046 合并）

补的测试类（写这一节时是 9 个，后来增至 15 个新写的，另有 13.8 移植 4 个，清单见 H）写完后，下面这些情况**没有测**，或测了但结论要人确认。每条写明原因，方便后续接手。

**A. 没有测到的情况**

| 类 | 没覆盖 | 原因 |
|---|---|---|
| `AggregateFormulaStatSqlTest` | 零方差（一列所有值相等）的相关系数；在真实的 MySQL / PostgreSQL / SQL Server 上执行组合 SQL；worksheet 端到端 | 只在内嵌 Derby 上跑；其他库的除零行为（MySQL 返回 NULL、PostgreSQL 报错）是经验判断，没验证；这些库要 CI 的库环境 |
| `AggregateFormulaStatSqlTest` | 各库原生函数名是否真的存在 | 只断言生成的函数名文本，没有在各库执行 |
| `DateRangeRefDialectTest` | 只有 Derby 真正执行了 SQL；其余库只断言"是否下推"和有没有表达式 | 其他库的函数语义没有执行验证；ClickHouse、H2 的"不分组"返回空表达式（看起来像遗漏）只是固定了现状；DB2 没有 second_part 同理 |
| `DateRangeRefDialectTest` | `week` 在 `week.start` 不是周日且设置了 `user.locale.weekGrouping` 时不下推 | 该分支靠系统属性，另有 `DateRangeRefWeekPushdownTest` 部分覆盖，未逐库展开 |
| `SQLHelperOperationSupportTest` | MySQL 按数据源版本（`getProductVersion()` > 4.1）判断 union；Derby 按版本（>= 10.3）判断 expression group by；Vertica、Hive、Snowflake、BigQuery、Dremio 等其他方言 | 版本分支要数据源对象；其他方言本批不在 CI 范围 |
| `DateRangeRefLevelValuesTest` | `week of month`、`week of quarter`、`month of quarter full week`、`*_FULL_WEEK` 系列（5 个）的取值 | **已补**：后来从代码读出周的规则，由 `DateRangeRefFullWeekLevelsTest` 覆盖（见 E、F）。原先写的"要开发确认"是否合理仍是产品判断 |
| `DateRangeRefLevelValuesTest` | 只测了周日为一周起始、系统时区；没测 `week.start` 其他取值、非默认时区、夏令时、`java.sql.Time` 类型 | 时区与周起始已有 `DateRangeRefPre1901Test`、`DateRangeRefWeekOfYearLocaleTest` 覆盖一部分，没有重复 |
| `DateLevelSummaryTest` | 完整链路（日期列 → `DateRangeRef` → 聚合）、`SummaryFilter2` 的条件组、cube 模式、Sum 以外的公式、输入未排序 | 只验证了"日期级别取值 + 排序后按相邻行分组求和"；完整链路由 dashboard 用例覆盖 |
| `AssetUtilUnpivotTest`、`UnpivotQueryTest` | unpivot 之后再聚合 / 做交叉表；`fixColumnSelection` 按第一行数据修正列类型；带格式或强制解析的日期类 `changeColumnType`；MV 转换 | 需要更长的 worksheet 链；日期格式要先确认"强制解析"的预期 |
| `RotatedQueryTest` | 第一列有空值 / 空白的情况；旋转后再聚合；MV；表头值很长 | 没来得及；空值在 lens 层已有 `RotatedTableLens` 的"空表头取 `Column [n]`"测试 |

**B. 看起来是产品缺陷、测试里已 `@Disabled` 的**（2026-11-12：下面 #81102、#81095、#81096 已在 `main` 修复，测试已启用，见 H）

- 已报 #81102（Redmine 里没有同类 bug，#61619 是数据量限制、#53227 是另一种缺列）：`RotatedQuery`，源表第一列有重名时，旋转后的表丢列、丢数据。`A, A, B` 得到列 `item, A, B`，第二个 A 的值全丢；`A, a`（只差大小写）得到 `item, a_1`，`A` 列不见了，`a_1` 下面是 `A` 的值。`AssetUtil.fixAlias` 的本意是给重名列加 `_1` 别名，但旋转后的列选择在校验时把它们合并掉了。两个测试：`sameNamesInTheFirstColumnAreMadeDifferent`、`namesDifferingInCaseKeepAllColumns`。
- 已报：#81095、#81096（统计公式 SQL）、#81102（旋转表重名列）。

**C. 原先认为需要开发回答的问题**（下面 E 节已从代码确认大部分，只读代码、没有向开发确认）

1. 各"周"类日期级别（week of month、week of quarter、full week 系列）的定义。
2. `AM_PM_DATE_GROUP`、`MILLISECOND_DATE_GROUP`、`AM_PM_OF_DAY_DATE_GROUP` 在 `XConstants` 里有常量，但 `DateRangeRef.getData` 抛 "Unsupported option"，是设计如此还是没做完。
3. ClickHouse、H2 的"不分组"日期级别没有 SQL 表达式，是遗漏吗。
4. 旋转后第一列重名应该报错、加别名，还是合并（#81102 里我按 `fixAlias` 的本意写成加别名，开发可能另有定论）。
5. 前面各节已列的：排名并列（ranking ties）、`NumericRangeRef` 区间边界、`DateCondition`、上传类型检测、`isQueryMergeable`。

**E. 已从代码确认的答案（2026-11-11，只读代码，没有向开发确认）**

C 里的问题，下面这些能从代码直接读出规则，所以**不必等开发**就能写期望值；"设计是否合理"仍是产品的判断，这里只说代码现在是什么。

| 问题 | 代码里的规则 | 能否写测试 |
|---|---|---|
| 各"周"级别（C1） | 周按 `minimalDaysInFirstWeek = 7` 算（`JavaScriptEngine.datePart`、`DateTimeProcessor`）：每月第一个不满整周的部分属于**上个月**的最后一周。`month/quarter/year of full week` 都取"这一周的起始日（`week.start`）所在的月"，源码注释的例子：2024-01-01 的整周归 2023。`week of month` = `Calendar.WEEK_OF_MONTH`，为 0 时退到周起始日再取。`week of quarter`：季度第一个整周为第 1 周（`DateComparisonUtil.getQuarterFirstWeek`），值 = (距该周起始日的天数 + 1) / 7 向上取整。`forceDcToDateWeekOfMonth` 只在日期对比（date comparison）里用，worksheet 里为 -1 | **能**。可用 `java.time` 独立算期望，下一批补 |
| am/pm、millisecond（C2） | 前端 `xconstants.ts` 只有常量，没有任何界面使用；Java 里只有旧的报表排序 `SortOrder`、`LayoutTool`、图表/报表常量和 viewsheet 脚本常量列表（`VSScriptableController`）用到；`DateRangeRef.getData` 不支持。结论：worksheet 里没有这些级别。剩下一问：viewsheet 脚本给范围列设这些级别时会怎样（没查） | 已用测试固定 |
| ClickHouse、H2 没有"不分组"函数（C3） | 不是这两个库特有：`sqlhelper.xml` 25 个方言里 13 个**没有任何 `sqlfunc`**（derby embedded、cloudscape、hive、denodo、h2、dremio、snowflake、google bigquery、mongo、luciddb、exasol、databricks、clickhouse），所以所有日期级别，包括 none，都不下推（`isMergeable` 为 false）。看起来是"这些库没有配置日期函数表"，不是漏了单个级别。该范围列会让整张表因此不下推的影响没有验证 | 已固定现状 |
| 旋转表重名（C4） | 根因已读出，见 #81102 的评论：`RotatedQuery.fixColumnSelection` 用互斥的 `addAttribute(column)`，同名列被跳过，`fixAlias` 看不到。大小写不同的情况另有错位，没追到 | 已 `@Disabled` |
| 排名并列（C5） | worksheet 排名在 `AssetQuery.getRankingTableLens` 里固定 `setEqualityKept(false)`（`AssetQuery.java:3292`），所以**恰好取 N 行**，不保留并列；`RankingTableLens` 用 `List.sort`（稳定排序），并列的行保持输入顺序，先出现的先取，top 和 bottom 一样。排名总是在内存里算（`PreAssetQuery.java:3710`）。风险：输入顺序本身不稳定时结果也不稳定，例如 #81087 | **能** |
| `NumericRangeRef` 边界（C5） | 看 `ValueRangeInfo.inclusiveType`，默认 `LOWER`：每段 [下界, 上界)，最小值以下 `< 第一个值`，最大值以上 `>= 最后一个值`。`UPPER`：(下界, 上界]，两端 `<=` / `>`。`NONE`：每一段两端都闭，按顺序先匹配的先得，两端的"以下 / 以上"是严格的 `<` / `>`。一个值的特例：`BETWEEN v AND v`。空值落入"Others"（SQL 的 `CASE` 里）。**怀疑的不一致**：脚本版本的"最大值以上"分支没有 `!= null` 判断，`LOWER` 下最后一个值 <= 0 时 `null >= 0` 在 JS 里为真，空值会进入最后一段，SQL 版本进入 Others。没有运行验证 | **能**（SQL 和脚本表达式两个都写），怀疑那条先验证 |
| `DateCondition` 参考日期（C5） | 不能注入。13 个子类都在方法里直接 `System.currentTimeMillis()`，`ZONE` 是静态的默认时区，周起始日读 `Tool.getFirstDayOfWeek()`。只有 `ToDateCondition` 有 `getRange(long)`，但是 private。现有的 `DateConditionTest` 也是相对"现在"取值 | 只能相对当前时间写期望（避开日界、周界、月界），或产品改成可注入 `Clock` |
| 上传类型检测（C5） | `TextUtil.detectType` 只看文件头 8 个字节：`PK 03 04` 为 xlsx，`D0 CF 11 E0 A1 B1 1A E1` 为 xls，其余一律按分隔文本。列类型 `TextUtil.getType`：日期（`数字[.-/]数字[.-/]数字`加可选时间，按前后位数判断 yyyy-M-d 还是 M-d-yyyy/yy）、时间、整数（`Integer.parseInt`）、小数（`#,###.#`、`%`、`$`、`($..)`），其余为字符串；**`TextUtil.getType` 没有布尔识别，但 `CSVLoader` 有**（更正：此前写成"没有布尔识别"不对）：`CSVLoader` 用 `AssetUtil.getType`，列类型还是默认整数时遇到 `true`/`false`（不分大小写，`AssetUtil.java:2930-2933`）就把该列识别为布尔；之后出现任何其他值（`CSVLoader.java:158-163`），该列变字符串并保留原文；一列里先有数字再有 `true` 同样变字符串。已有单测 `CSVLoaderRulesTest`。`CSVLoader` 逐行合并类型：数字类型混合变 Double，其他混合变 String，含"不适用"值的数字列变 String。`DelimitedFileReader.importHeader` 只用第一行数据判断类型 | **能** |
| `isQueryMergeable`（C5） | `PreAssetQuery.isQueryMergeable0` 的 8 个阶段已在 3b-C 列出，规则在代码里。`XUtil.isQueryMergeable` 前半段已读：非 JDBC 查询、非 UniformSQL、解析失败、lossy、数据源为空或 Hive 未启用、`jdbc.pushdown.blacklist`（`*` 或查询名）、手写 SQL 里有 `$(@...)` 变量，都不合并；后半段没读 | **能**，哪些组合"应该合并"是产品判断，但现状可固定 |

**F. 按 E 节补的测试（同一分支）**

| 类 | 测试数 | 结果 |
|---|---|---|
| `DateRangeRefFullWeekLevelsTest` | 3 | 8 个"整周"级别（week of month、week of quarter、month/quarter/year of full week 等）× 16 个日期，对照 `java.time` 按 E 节的规则算的期望，全部一致；变异检查有效 |
| `NumericRangeRefBoundaryTest` | 5（1 个 `@Disabled`） | 三种包含方式（下界闭、上界闭、两端闭）的边界，SQL（Derby 执行）和脚本（表单公式引擎）两个表达式对同一张表，4 个一致。**确认了一个不一致**：脚本版本"最大值以上"没有 null 判断，最后一个分界值 <= 0 时 null 进入最后一段，SQL 版本进 Others（见下） |
| `RankingTiesTest` | 6 | worksheet 排名恰好取 N 行、并列按输入顺序、`RankingCondition.equal` 选项在 worksheet 里被忽略（viewsheet 的 top N 会用它） |
| `TextUtilTypeDetectionTest` | 43 | 文件类型（xlsx、xls、文本）和单值类型（整数、小数、日期、时间、字符串）的规则 |
| `XUtilQueryMergeableTest` | 16 | `XUtil.isQueryMergeable` 里 UniformSQL 测试没覆盖的分支：group by、聚合列、下推黑名单（`*`、名字列表、整词比较）、内嵌 `$(@var)`、非 JDBC 查询、没有数据源 |
| `DateConditionBuiltinTest` | 39 | 相对"现在"的内置日期条件（ToDate、最近 N 个月/天、第 N 季度/半年/月、PartDate）：用 `java.time` 算期望，跨日时跳过；已在 UTC、上海、Lord_Howe、Kiritimati、Pago_Pago、伦敦下各跑通（`core/pom.xml` 写死 `-Duser.timezone=America/New_York`，`-DargLine` 覆盖不了，要改 pom 才能换时区） |
| `CSVLoaderRulesTest` | 41 | 列类型合并（整数+小数→double，其他混合→字符串，含 N/A 的数字列→字符串，`true/false`→布尔）、表头去重、分隔符、引号、BOM、行列上限 |

**F 里看起来是缺陷或值得问的：**
- **数值分段脚本表达式对 null**：`getScriptExpression()` 的最大值以上分支没有 `field != null`，LOWER 模式下最后一个分界值 <= 0 时（以及上界闭、两端闭模式下最后一个分界值为负数时，这部分按 JS 的规则推断、没有运行）`null` 进入最后一段，SQL 版本是 Others。测试 `nullIsOthersWhenTheLastValueIsNotPositive` 当时 `@Disabled`，已报 #81105，已在 `main` 修复，测试已启用并通过。
- **`1.2.3` 被识别为日期**（格式 `M.d.yy`）：上传的版本号列会被当成日期。固定了现状，没有报。
- **空字符串被识别为 double**：`getType("")`；文件加载器在调用前会跳过空值，所以现实里碰不到，固定了现状。
- **整数带千分位**（`1,234`）识别为 double，不是整数；超过 int 范围的整数（`2147483648`）也是 double。固定了现状。
- **`RankingCondition.equal` 在 worksheet 里被忽略**：`AssetQuery.getRankingTableLens` 固定 `setEqualityKept(false)`，而 viewsheet 的 top N 会传 `ranking.isEqual()`。**后来查清是有意设计**：viewsheet 的 top N 按分组（只有这条路径传 `isEqual()`），worksheet 的 top N 是平的，代码注释写明，条件编辑器的 TOP_N 也没有"包含并列"选项，见 4b-H。

**G. 上传服务（`ImportCSVDialogService`）：读了代码，决定不加单测（2026-11-12）**
- **环境不允许**：`loadData` 一开始就调 `ExcelFileSupport.getInstance().isXLSB(..)`，它用反射加载 `utils/inetsoft-xml-formats` 里的 `PoiExcelFileSupport`；该模块依赖 core，core 的测试类路径上没有它，所以连 CSV 上传也走不通。服务本身用 Mockito 能构造（5 个依赖都能顶替，`FileSystemService` 要返回 `Tool.getCacheDirectory()`，否则 `loadData` 按缓存键找不到文件，报 "Upload Timeout"）。要测只能放进 `utils/inetsoft-xml-formats/src/test`（有 core 的 tests jar 和 POI），那边的构建我没跑通（反应堆里 core 先失败）。
- **固定宽度文本（`TextFileType.FIXED`）不是上传功能**：上传的类型只由 `TextUtil.detectType` 给出（DELIMITED、XLS、XLSX），`FixedFileReader`、`FixedFileInfo` 只被 `uql/text/TextOutput`（文本数据源）用，worksheet 上传到不了。上面 §3 表里"固定宽度文本没有用例"这一条不适用于上传。
- **`process()` 把"最大单元格大小"当成列上限传给 `loadData`**（`ImportCSVDialogService.java:598-599`，`Util.getOrganizationMaxCellSize()`，默认 500，对应的参数是 `colLimit`）；`getPreviewTable` 传的是 `getOrganizationMaxColumn()`（默认 200）。看上去是传错了常量，后果有限：第一次预览多读 201-500 列再在 `populateResultMap` 里截成 200。没验证，没报。
- **`getPreviewTable` 里列上限提示可能丢**：`populateResultMap(.., csvInfo.isUnpivot())` 只在 unpivot 时检查列数，且此时表已被 `colLimit` 截成 200 列，所以改选项后的预览不会再带"列超限"提示（第一次上传预览有）。读代码得出，没运行验证。
- **xlsb 的拒绝提示**：先走 `process()` 里的 `getSheetNames`，xlsb 是 zip，会被 `detectType` 当 xlsx，`getSheetNames` 可能先失败成 "Failed to parse XLS"，到不了 `loadData` 里的 xlsb 提示。没验证。
- **`CSVInfo` 的分隔符、引号、表头、编码猜测：已补测并核对（2026-11-12，`CSVInfoDetectionTest` 30 个，产品仓库分支 `test-csvinfo-detection`，本地提交 `d3b8b53ff6`，未推送）。** 评分规则从代码读出并手算期望（前 10 行、每一行列数一致且多于 1 列得"列数 + 100"、不一致得"列数"、引号未闭合得 0；取各行最小值；严格更高才替换，所以平局取先出现的：引号处理开、再关，分隔符 tab、`;`、`,`、`|`），第一次运行 32 个中 31 个通过（唯一失败见下面的 XML），做了变异检查（平局规则、列数一致的偏移、检查行数、首行全数字判断、GB18030 到 GBK 的映射、引号必须紧跟分隔符，共 6 处单独变异都有测试变红；`+100` 改 `+99` 不改变任何实际排序，属于等价变异）。固定的现状、没有报缺陷的：
  - 每一行都带同样个数的"引号内逗号"（如 `"a,b",c`）时，不处理引号、3 列的得分（103）高于处理引号、2 列的得分（102），猜测结果是不处理引号；各行的个数不同时则处理引号。
  - 引号未闭合的行只有在它是检查范围里的最后一行时才会让引号处理被关掉（前面的行得 0 会被下一行的分数覆盖，0 不是粘住的）。
  - **首行是否表头：** 有一个"全是数字"的非空单元格就不是表头。不处理引号时用 `String.split(delim)`（正则），`|` 被当成空匹配，首行被拆成单个字符，所以 `…|c1` 这样的首行会被判为数据，同样的数据用 `,` 就是表头（探针已验证）。只在 `|` 分隔且猜测结果不处理引号时发生（例如首行之外有一个字段中间带 `"`），影响小（只是上传对话框里"首行作为表头"勾选框的默认值，用户可以手动勾上）。**已报 #81220**（Query Engine，指给 Larry Liang，状态 New），测试注释已引用该编号。
  - `getFileEncode`：UTF-8、GBK（报为 GBK，GB2312/GB18030 映射到 GBK）、UTF-16 两种字节序（报为 "Unicode"）、纯 ASCII（用系统编码）都与代码一致。
  - `CSVInfo.writeXML`/`parseXML` 在整个产品里没有任何调用，所以没有写测试；顺带发现 Tab 保存后读回来是空格（属性值里的原始 Tab 被 XML 规范化），没有用户影响。

**H. 整体状态（2026-11-12，PR [#8046](https://github.com/inetsoft-technology/stylebi/pull/8046) 已合并为一个提交 `35e99dcc92`；分支 `test-ws-lens-unit-tests` 的 24 个提交和本地 worktree 已清理）**

**验证：**
- 同一个 JVM 整体跑（`-Dsurefire.forkCount=1 -Dsurefire.reuseForks=true`）19 个类：**340 个测试，0 失败，0 错误，1 个跳过**（`SetOperationLensesTest` 的 #80538）。第一次整体跑时是 12 个跳过，都是带缺陷编号的 `@Disabled`；之后分支变基到含修复的 `main`，#81091、#81092、#81093、#81095、#81096、#81102、#81105 的修复都已在 `main` 上，对应的 11 个测试去掉 `@Disabled` 后直接通过（期望值是按代码规则独立推的，不是按修复前的输出写的）。
- 每个类单独跑：19 个类各自单独跑（`-Dtest=<类名>`），测试数和跳过数与整体一致，全部 BUILD SUCCESS，没有类之间的隐含依赖（变基到含修复的 `main` 后又单独跑了一遍，结果相同）。
- 日期相关的 162 个测试在 UTC、上海、Lord_Howe、Kiritimati、Pago_Pago、伦敦各跑通过；默认运行时区是 `core/pom.xml` 写死的 America/New_York，换时区要改 pom（`-DargLine` 覆盖不了）。
- 做过变异检查（临时改主代码看测试是否变红，已还原）：方言、统计公式、日期条件、`XUtil.isQueryMergeable`、`CSVLoader`、`AssetUtil.getType`。
- 分支里只新增测试文件，没有改主代码。

| 类 | 测试数 | 跳过 | 对象 |
|---|---|---|---|
| `JoinLensesTest` | 20 | 0 | 13.8 移植：join |
| `SetOperationLensesTest` | 18 | 1 | 13.8 移植：union、intersect、minus、distinct |
| `SortSummaryFilterTest` | 15 | 0 | 13.8 移植：排序、汇总、top N |
| `RotateFormulaLensesTest` | 10 | 0 | 13.8 移植：rotate、脚本表达式列 |
| `DateRangeRefDialectTest` | 38 | 0 | 各库日期级别下推，Derby 执行 |
| `AggregateFormulaStatSqlTest` | 41 | 0 | 统计公式组合 SQL |
| `SQLHelperOperationSupportTest` | 13 | 0 | 各库支持矩阵 |
| `DateRangeRefLevelValuesTest` | 5 | 0 | 日期级别取值 |
| `DateRangeRefFullWeekLevelsTest` | 3 | 0 | 整周级别 |
| `DateLevelSummaryTest` | 5 | 0 | 日期级别分组求和 |
| `AssetUtilUnpivotTest` | 14 | 0 | unpivot |
| `UnpivotQueryTest` | 3 | 0 | worksheet 里的 unpivot |
| `RotatedQueryTest` | 5 | 0 | 旋转表 |
| `NumericRangeRefBoundaryTest` | 5 | 0 | 数值分段边界 |
| `RankingTiesTest` | 6 | 0 | 排名并列 |
| `TextUtilTypeDetectionTest` | 43 | 0 | 文件类型与单值类型 |
| `CSVLoaderRulesTest` | 41 | 0 | 列类型合并、表头、分隔符、行列上限 |
| `XUtilQueryMergeableTest` | 16 | 0 | 下推规则（SQL 层） |
| `DateConditionBuiltinTest` | 39 | 0 | 相对"现在"的内置日期条件 |

**第二批（`CSVInfo` 和 `PreAssetQuery` 的下推规则，6 个类、246 个测试）见下面的 4b-H2，本节的表只列 PR #8046 的 19 个类。**

**已报缺陷：** #81087（cross join 行序竞态，datatest 发现）、#81091（连接列检查，13.8 #80297）、#81092（`join.table.maxrows` 算进表头，13.8 #80298）、#81093（`TextComparer.isAsciiOnly`）、#81095（协方差/相关系数 NULL 配对）、#81096（单行相关系数除零）、#81102（旋转表重名列）、#81105（数值分段脚本 null）。**这 8 个都已在 `main` 上修复**（Redmine 状态 Resolved，指给 Nelson Cao，版本 stylebi-2.1.0）。#80538（非 distinct union 多算一行）原来就有，状态 New，没有动，对应测试仍 `@Disabled`。**新报：#81178**（`MonthsCondition` 对 `Timestamp`、带时间的 `java.util.Date`，内存判断和下推 SQL、和 `Days`/`Weeks` 条件不一致；测试 `timestampEarlierOnTheStartDayIsInSqlButNotInMemory` 固定了现状，指给 Larry Liang，状态 New）。

**还没有覆盖的：**
- `PreAssetQuery` 的 `is*Mergeable` 已全部覆盖（见 4b-H2）；没测的只有固定返回 true 的 `PhysicalBoundQuery`、`LMBoundQuery`（要真实的物理表或逻辑模型），以及 mongo 的真实 helper（用重写 `getSQLHelper` 的桩代替）。
- 上传：`ImportCSVDialogService`（预览、unpivot 选项、行列上限提示、文件大小上限、xlsb 提示、Excel 读取）不加单测，原因见 G：`PoiExcelFileSupport` 在 `utils/inetsoft-xml-formats`，core 的测试类路径上没有；`CSVInfo` 的猜测已补测，见 4b-G。
- unpivot 之后再聚合或做交叉表、`fixColumnSelection` 按第一行修正类型、日期类 `changeColumnType`、MV 转换；`RotatedQuery` 的空值、旋转后再聚合、MV。
- `DateCondition`：`YearCondition`、`QuarterCondition`、`MonthCondition`、`WeekCondition`、`WeeksCondition`、`DayCondition` 没逐个核对是否已被 `DateConditionTest` 覆盖；夏令时切换日（代码里不能注入时间，只能跳过）；`DateRangeRef` 的 `week.start` 非周日、`java.sql.Time`。
- 各库：只有 Derby 真正执行了 SQL，MySQL、PostgreSQL、SQL Server 等只断言了生成的文本；MySQL/PostgreSQL 除零行为是经验判断；Vertica、Hive、Snowflake、BigQuery 等方言。
- `NamedRangeRef`、其他零覆盖的公式、4 个表类、变量数据类型等（见 G12）；dashboard 一侧的用例没有动。
- 没验证过的判断：日期级别 `none` 不下推会让整张表不下推的影响；viewsheet 脚本给范围列设 am/pm 级别会怎样。（"界面在 worksheet 排名里是否提供包含并列"已查：条件编辑器的 TOP_N 只有 N 和列，没有，见下。）

**测试里固定了现状、没有报缺陷的**（以下几条保持现状，不打算改）：
- `1.2.3` 被识别为日期（`M.d.yy`），版本号列会被当成日期。
- 空字符串被识别为 double（现实里加载器会先跳过空值）；带千分位的整数 `1,234` 和超过 int 的整数识别为 double。
- `CSVLoader`：类型只由开头几行（表头算一行）决定，之后不符的值变 null。
- `CSVLoader`：行数刚好等于 `rowLimit` 时也标记"超限"。

**逐条核对过代码、结论是有意设计或影响很小的**（测试里固定现状，注释已写明；PR #8046 里单列为"by design"）：
- **worksheet 排名忽略 `RankingCondition.equal`：有意设计。** `AssetQuery.getRankingTableLens` 有两条路径：表名以 `TABLE_VS` 开头且有分组的 viewsheet 表用 `SummaryFilter`、按分组、会传 `isEqual()`；worksheet 表走 `RankingTableLens`，固定 `setEqualityKept(false)`，代码注释写"worksheet top-n 是平的（不分组）(49245)"。条件编辑器的 TOP_N 只有 N 和列，没有"包含并列"。此前写成"可能有问题"是措辞过头。
- **`sqlhelper.xml` 25 个方言里 13 个没有任何日期函数**：`DateRangeRef.isMergeable` 由 `getExpression` 是否为空决定，空就退回内存分组，结果不变，只是不下推（性能）。ClickHouse、H2 至多是"可以加日期函数表"的改进项，不是遗漏造成的错误；`DB2` 没有 second_part 同理。
- **`AM_PM`、`MILLISECOND`、`AM_PM_OF_DAY`** 有常量，但 `DateRangeRef.getData` 抛 "Unsupported option"：前端常量文件只定义、没有对话框使用；Java 里只有 `ChartConstants`（viewsheet 脚本常量）、`SortOrder`、`LayoutTool` 引用。viewsheet 脚本给图表维度设这些级别会怎样没有验证，不在 worksheet 范围内。
- **`NthQuarter`、`NthHalfYear`、`NthMonth` 的 setter 对越界值静默忽略**：整个类里 setter 风格一致，XML 解析直接给字段赋值、不经过 setter；只有直接用 API 误传（如 `new NthQuarterCondition(5, 0)`）才会悄悄变成第 1 季度，影响很小。界面是否经过 setter 没有查。

**已报缺陷的那条**：`MonthsCondition` 的 Timestamp 差异（#81178）。探针（现在 21:01，纽约，"最近 3 个月"，SQL 范围 09-11 00:00 到 11-11 23:59:59）：Timestamp 09-11 00:00 内存 false；Timestamp 今天 23:59:59 内存 false（`Days` 为 true）；`java.util.Date` 今天中午内存 false（`Days` 为 true）；`java.sql.Date` 今天 true。`Days`、`Day`、`Week`、`Weeks` 的内存判断都按"天"比较，只有 `MonthsCondition` 不是。

**读代码得出、没运行验证：** `DaysCondition` 的 SQL 用毫秒数减 24 小时，窗口里跨夏令时切换会差 1 小时；`ImportCSVDialogService.process()` 把"最大单元格大小"当成列上限传入，改选项后的预览可能丢"列超限"提示，xlsb 提示可能被 "Failed to parse XLS" 抢先。（`CSVInfo` 选中 `quote=false` 的怀疑已验证成立并固定，见 4b-G。）

**在带修复的产品上重跑 datatest（2026-11-12，本机）：** 本机 `I:\m2` 里的 `inetsoft-core-2.1.0-SNAPSHOT` 的 `CrossJoinTableLens` 字节码和含 #81087 修复的 `main` 编译结果完全一致，所以跑的是带修复的版本。
- **`property_crossJoin.maxCellCount`**：单独在新 JVM 里跑 3 次，以及接着 `WSData_Spec` 在同一个 JVM 里跑 1 次，导出的 `Query1.txt` 校验和完全相同（MD5 `1f170b83…`），**行序已经确定，竞态消除**。输出是 5000 个不同的 id（左表 TestData 固定为变化最快的一边），既不是基线里的"143 个 id × 1 个订单 × 35 个客户"，也不是以前单独运行的"1 个 id × 143 个订单 × 35 个客户"，所以用例仍失败：**基线是按旧的竞态结果之一记录的，需要更新成新的确定输出**（改用例或放宽比对都不必了）。
- **`WSData_Spec` 另有 4 个用例失败（`rotate`、`grouping1`、`condition1`、`condition2`）**，原因是 #81102 的修复：旋转表以前把第一列重名的列丢掉，基线固定了这个错误输出（如 `rotate/Query4` 基线 7 列，只有 6 个不同的 CATEGORY_ID；现在 21 列，重名列加 `_1`、`_2` 后缀，20 个产品都在）。`rotate/Query4`、`condition1/Query7` 的表头确认有 `_n` 后缀；`condition2/Query1`（基线 3×29）、`grouping1/Query1`（基线 3×14）是旋转表、列数大幅增加，没有逐列核对。这 4 个基线也要按新输出更新，更新前应人工看一遍新输出是否符合 `fixAlias` 的规则（A、A_1、A_2）。
- 这说明严格模式后的 datatest 能发现产品修复带来的结果变化（也说明这几个基线固定了缺陷）。以上 5 个基线里**更新了 3 个（`rotate/Query4`、`condition1/Query7`、`grouping1/Query1`，核对通过，对应 3 个用例用新基线通过），其余 2 个（`condition2/Query1`、`Query3`）和 cross join 没有更新**（决定：基线更新和本文档放同一个 PR；更新前先确认新输出正确，核对结果如下）。
- **`rotate/Query4`：已独立验证，新输出正确。** 用 `PRODUCTS1` 的"产品→类别"关系逐列核对：20 个产品各出现一次；每列表头的底名就是该产品的类别；重名列的后缀 `_1`、`_2` 在同一类别内按产品号升序编号。注意列顺序不是源表顺序：先是全部不带后缀的列（6 个），再是带后缀的列。**基线已更新**，`WSData_Spec#rotate` 用新基线通过。列序规则（单元探针验证）：各名字第一次出现的列在前、按源表顺序，重名列在后、也按源表顺序；列只从源表前面的行生成（见下面 `Query3`）。
- **`condition1/Query7`、`grouping1/Query1`：已核对，基线已更新。** 来源行序和导出文件不同（导出已排序），所以没有按位置比，而是核对"新增的列正好是以前被丢弃的重名列，每列的（表头底名，数据）能对应到来源的一行"：`Query7` 来源 `MVPRODUCTS1`，19 个（类别，描述）配对完全一致，`Column [12]` 是来源里类别为空的那一行，后缀编号完整；唯一解释不了的是 `f_string` 这一列，它在旧基线里就有、位置和值没变，不是这次修复带来的。`Query1` 来源 `Embedded`，把"表头行"也算作数据行（来源把它当数据，旋转后成了一列，空小时那一行成了标签列）后，60 个来源行各出现一次，后缀编号完整。旧基线的列都不变。
- **`condition2/Query1`：被列数上限（200）截断**（来源 701 行，旋转后每行一列）；旧基线 29 列是"去掉重名列"的结果。新输出前 200 列的内容没有独立验证。
- **`condition2/Query3`：不是"列变多"这么简单，不能直接当新基线。** 它是 `PRODUCTS1`（2380 行）的旋转表，保存的列名是 1～20 的类别，按 `11` 分组、对 `2` 求和。新行为每行一列并受上限截断，而类别 `11` 第一次出现在第 1340 行，落在上限之外，分组列消失，结果从 3×2（组 3、5）变成 2×1（8）。也就是说 #81102 的修复让"保存的旋转表依赖重名列被丢弃"的 worksheet 悄悄改变了含义，**已确认是问题，已报 #81193**（回归，指给 Nelson Cao）：用单元探针复现并对比了修复前后——源表 300 行，类别 `11` 第一次出现在第 271 行，保存的列选择含 `1`～`11`；修复前旋转表 12 列（`11` 在），修复后 250 列（`11` 丢了，列选择里也没有）。上限 250 列是有意设计（#81102 里写过），修复前重名列被丢弃所以很少碰到上限，修复后每行一列，名字第一次出现得晚的列就被截掉，保存的 worksheet 里依赖它的分组、条件、聚合被悄悄丢弃。**这个基线保持不动，等开发决定**（#81193 里列了三个方向：被截断时先保留各名字第一次出现的列、加列上限提示、保留保存的选择里仍存在的名字）。
- **`property_crossJoin.maxCellCount`：只验证了一部分。** 前 5000 行的 TestData 列与 `TestData` 的行依次一致（左表最快，已验证）；其余 7 列在 5000 行里是常量，都等于 `All Sales1` 第 1 行和 `Customer1` 第 1 行。但 `Customer1` 是没有排序的绑定表（`BoundTableAssembly`，无 `sortInfo`），行序来自数据库：新输出第 1 行客户是 NJ，旧基线和 `Customer1.txt` 导出的第 1 行是 AZ。这一列依赖数据库的返回顺序，无法独立确认，把它写进基线可能固定的是数据库行序，换环境会变。

**H2. 第二批（2026-11-12，产品分支 `test-csvinfo-and-mergeable-unit-tests`，从 `origin/main` 新建，本地提交、未推送，没有开 PR）**

在同一个 JVM 里整体跑：6 个类、246 个测试，0 失败（`-Dsurefire.forkCount=1`）；每个类都做过变异检查。

| 类 | 测试数 | 对象 |
|---|---|---|
| `CSVInfoDetectionTest` | 30 | 上传的分隔符、引号、首行表头、编码猜测（4b-G） |
| `PreAssetQueryAggregateMergeableTest` | 86 | `isAggregateMergeable`：9 个永不下推的公式、百分比、无公式、`MEDIAN`（仅 Oracle 且主版本号 ≥ 10）、Derby 整数列 `AVG`、`sqlhelper.xml` 里 Sybase 不支持的方差类公式；变异 6 处全部被抓到 |
| `PreAssetQuerySortMergeableTest` | 26 | `isSortInfoMergeable`：H2 不保持分组顺序、Informix 不能按聚合排序、first/last 聚合、脚本计算列、有分组时丢弃其他列的排序；变异 5 处全部被抓到 |
| `PreAssetQueryConditionMergeableTest` | 13 | `isConditionListMergeable`：条件列和比较值的列、非 `Condition`（排名）、日期条件、mongo 的 `ONE_OF`、子查询（同源、库能力）、having；变异 6 处全部被抓到 |
| `PreAssetQuerySourceMergeableTest` | 70 | 来源判断：Mirror（`FROM_SUBQUERY`、基表是否可下推、最大行数）、Join（内连接、全外连接、merge join、数据源、最大行数）、union/intersect/minus（各库）、SQL 手写模式（解析、丢信息、无表、已有 group by）、内嵌表/旋转表/反透视表；变异 12 处全部被抓到 |
| `PreAssetQueryGroupMergeableTest` | 19 | 分组（交叉表、时间序列、命名分组、where 不能下推、排名与分组兼容的三种情况）、选择列（全隐藏、隐藏的脚本列、SQLite 的日期和布尔）、排名、整条查询、结果缓存；变异 13 处全部被抓到 |

**搭建方式（可复用）：** 公共夹具 `MergeableFixture`：用 API 构造 `UniformSQL`（`addTable`、`selection.addColumn`、`setTable`），选择列要带表名 `a.k`，否则会被当作 SQL 表达式；直接 `new SQLBoundQuery(RUNTIME_MODE, new AssetQuerySandbox(ws), table, false, false)`；按库造数据源（derby、h2、postgresql、mysql、oracle、sql server、sybase、informix、sqlite）；不执行 SQL、不开连接。脚本表达式列要 `setSQL(false)`（表达式列默认是 SQL 表达式）。库的版本由 mock 的 `XRepository.getMetaData` 返回 `DBProductVersion`，且数据源本身不能配版本（见下）。

**读代码和运行时发现的几点（没有报缺陷）：**
- **Sybase 的平方和（`SUMSQ`）**：公式名是 `SumSQFormula`，代码用小写 `sumsqformula` 去查库能力，而 `sqlhelper.xml` 里 Sybase 的禁用项写的是 `sumsq`，对不上，所以声明了不支持却仍被下推。它是内部构件（用来拼方差的组合 SQL），用户选不到，没有实际影响；测试固定了现状。
- **库的产品版本（已确认，影响很小，不报）**：`SQLHelper.getSQLHelper` 只在数据源**没有** `version` 时（MySQL、Oracle）才通过仓库查询 `DBProductVersion` 并设给 helper，Derby 总是查；数据源有版本时 helper 的版本是空字符串（单测探针验证）。数据源的 `version` 是**会持久化**的属性（`JDBCDataSource` 的 XML 里读写），但产品只给 MySQL 设过它（查询成功后缓存，保存数据源时就持久化了）。读 helper 版本的只有两处：`XUtil.supportMedian`（Oracle，版本 ≥ 10 才下推中位数）和 `DerbyHelper`（Derby 总是查，不受影响）；`MySQLHelper` 读的是数据源的版本，所以 MySQL 不受影响；`NamedRangeRef` 里按版本判断的代码已被注释掉。所以只有"Oracle 数据源的定义里手工写了 `version`"这一种情况会让中位数不下推（在内存里算，结果相同，只是慢），没有界面或代码会给 Oracle 设这个值。已写成测试固定现状，改成让 helper 读数据源版本后该测试会变红（已验证）。
- **`isSortInfoMergeable()` 会改表**：有分组时它把不在分组、聚合列上的排序从**查询持有的表**里删掉。生产里 `AssetQuerySandbox`（第 482、954 行）先克隆表再建查询，所以改的是副本，不是资产；我直接构造查询才改到资产。不算缺陷，已写成测试。
- **条件的两套判断不一致（已确认，性能问题，**已报 #81229**，指给 Nelson Cao，状态 New）**：`merge()` 对只有 AND 的 where，先用私有的 `isConditionItemMergeable` 逐个把不能下推的条件拆出去，其余仍下推，再用 `isPreConditionListMergeable`（`isConditionListMergeable`）整体复核；mongo 的 `ONE_OF` 检查只在后者。探针走完整的 `merge()`：同样的 `k=1 AND k 属于 (1,2)`，非 mongo 库两个条件都下推；**mongo 上整个 where 都留在内存里**（`k=1` 本来能下推，`UniformSQL` 的 where 为空、3 项都留给内存）；对照：脚本计算列的条件只让它自己留在内存，`k=1` 仍然下推。所有条件都在内存里照样生效，所以结果相同，只是少了一次下推。已写成测试固定现状，给 `isConditionItemMergeable` 加上 mongo 检查后该测试变红（已验证）。
- **各库能力要同时看 `sqlhelper.xml` 和 helper 类（写来源测试时第一次按 xml 推错了）**：`SQLServerHelper` 把 `CONCATENATION_TABLE` 限定成只有 union（intersect、minus 不支持），`MySQLHelper` 的 union 按数据源版本（> 4.1）、其余按 xml，`MongoHelper` 不支持 mirror 和 concatenation，`DerbyHelper` 只管表达式分组；Oracle、PostgreSQL、H2、Informix、Sybase 没有覆盖，只看 xml。之前 `SQLHelperOperationSupportTest` 的支持矩阵没有这层（只测了 xml 和 MySQL、SQL Server、Derby 的几个覆盖，见 3b-C 续），后来的来源测试里 SQL Server 的 intersect、minus 是 false 已按 helper 修正。
- **Join、Concatenated 里"数据源相同"的比较大多是多余的**：不同数据源的名字不同，组合表的 `getSource()` 已经是 null，更早就被拒绝了；只有"同名但定义不同的数据源"才走到这一步（用例里用同名、不同 URL 构造）。手写 SQL 模式里"解析成功且不丢信息"的检查与 `XUtil.isQueryMergeable` 重复（等价变异）；不重复的是"没有表或没有选择列"。
- **`isRankingMergeable` 有排名条件就返回 false**：worksheet 的排名从不下推，和 4b-E 一致，是有意设计。

**D. 写测试的几个注意点**（以后接手少走弯路）

- worksheet 级测试要有 `AssetDataCache` bean（`DistributedTableCacheStore` 要 mock 一个 `ObjectProvider`），再 `setActive(true)` 后 `refreshColumnSelection`，否则组合表的列是空的。
- `SummaryFilter2` 只合并相邻的分组行，调用前必须按分组列排序（`AssetQuery` 用 `PostProcessor.sort`）。
- 新建测试类的头注释少一个 ` */` 时，编译器报 "does not represent an annotation type"，和真正原因无关。
- 用 `-q` 加 `-Dsurefire.failIfNoSpecifiedTests=false` 跑可能一个测试都没跑也返回成功，要看 `Tests run:`。

## 5. Dashboard 与机制补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 严格模式（G1） | 改 `WorksheetTest`：导出失败让用例失败、每张表单独捕获后汇总、基线文件必须都已导出、`-Dupdate.baseline`、清理 case 目录；`importCSVToEMTable` 空值安全并在失败时报错；`exportVSComponentData` 同样 | 打不开、查询异常、上传失败、少导出都会变红 | **已完成（已合并，#213）：** G2 的 40 个已随 spec 删除、G3 的 6 个已修好、G10 的 3 个陈旧基线已删除 |
| `WSBuildServer_Spec`（G2） | 二选一：补回 `BuildServer` 资产并去掉 Linux 跳过；或删除 spec 与 302 个基线 | 客户场景回归 | **已决定删除并处理（已合并，#213）**；日后找到资产再补回 |
| 上传（G3） | 修好基类后重跑 6 个用例，确认导出与基线一致 | 上传接线 | **已完成（已合并，#213）：** 根因是测试配置的 mock 和 spec 里的乱码，重跑后 6 个用例都与基线一致 |
| date in（G4） | 语义放单测；dashboard 留 1～2 个代表用例，不再依赖改操作系统时间 | 条件接线 | 产品是否支持测试用的参考日期（待开发确认） |
| 执行路径（G5） | 基类记录每张表是否合并（或生成的 SQL）并纳入基线，先用于 `checkMergeable` 用例 | 下推与否的回归 | 入口：`AssetQuery.createAssetQuery(table, mode, box, false, -1L, true, true)` 后取 `isQueryMergeable(false)`（`PreAssetQuery.java:2992`，public）和 `getQueryPlan()`（`ShowPlanService.java:44` 做的就是这个调用）；测试配置里没有 ShowPlan，但 `WorksheetTest` 已有沙箱和表，可直接调用；计划节点里有生成的 SQL 和下推标记（`PreAssetQuery.java:3787`-`3850`），但源不可合并时返回 null、计划模式用原始别名所以 SQL 与实际执行的别名不同；`ShowPlanService` 固定用 `RUNTIME_MODE`，测试应按实际执行的 `LIVE_MODE` 调用 |
| 运行时取数（G6） | 基类支持 `RUNTIME_MODE`，每种查询类型选 1 个代表 worksheet | viewsheet/MV 实际取数路径 | 暂缓：决定先只用 `LIVE_MODE` |
| 薄弱格子（G8） | 新建小 worksheet：`>=` join、date in、LIKE/starting with、Rotated/Unpivot/Concatenated 上的后置条件、ClickHouse | 这些组合的接线 | tester 衡量做哪些 |
| 各库差异（G11） | 为这几类各选 1 个代表场景：日期分组、top N / max rows、minus/intersect/full join、统计函数、null 排序；同一场景在 8 个库各建一个小 worksheet（用各库自己的表），基线按库分开；先用 G5 的"是否下推"记录确认走的是哪条路径 | 各库方言生成与取数结果 | G5 的机制；各库造数方式（待定）；ClickHouse 用例目前 `@Ignore`，先确认原因 |
| 功能面零覆盖（G12） | 新建小 worksheet：日期范围镜像与可复用条件 assembly、变量样式 `DATE_COMBOBOX` 与未覆盖的数据类型、命名分组作列引用（`NamedRangeRef`）、未设置的配置项（用 `initHome` 的 properties 参数）；表类 Cube/Data/QueryBound/Tabular 先确认是否可创建 | 这些能力的接线 | tester 衡量做哪些；表类待开发确认 |
| 未运行的 worksheet（G9） | 核对 49 个是否作为来源被引用，再补用例或删除 | | 无 |

做法原则同 vscalc：新建小 worksheet，不改已有资产；改 zip 后用 `ws_scan.py inventory` 前后对比。

## 6. 局限与待确认

**需要开发回答：**
1. `DateCondition` 能否注入参考日期（时区、周起始日规则一并确认）？否则 date in 只能靠改系统时间测试。
2. worksheet 层的合并规则：哪些条件、聚合、表达式、数据类型组合应当下推到 SQL。
3. 上传的日期、布尔类型识别规则；`NumericRangeRef` 区间边界；排名并列规则。
4. 各库方言：支持矩阵已从 `sqlhelper.xml` 和代码覆盖读出（见 3b-C 续），MySQL 的 `INTERSECT`/`EXCEPT`、`FULL OUTER JOIN` 已确认不下推。已确认：库没有原生统计函数时 `AggregateFormula` 退回 SUM/COUNT 组合 SQL（不是退回内存），在 Derby 上跑过：方差、标准差与 Java 参考值一致；协方差、相关系数在只有一列为 NULL 时与"两列都有值的行"不一致，单行相关系数除零报错（产品缺陷，已报 #81095、#81096，已在 `main` 修复，单测已启用）。各库生成的 SQL 已由 `DateRangeRefDialectTest` 等固定文本，只有 Derby 真正执行；MySQL、PostgreSQL 的除零行为仍是经验判断。

**2026-11-12 更新（上面第 1～3 条）：** 第 1 条：`DateCondition` 不能注入时间（子类里直接 `System.currentTimeMillis()`），已按"相对现在写期望、跨日时跳过"写了单测，要不要改成可注入 `Clock` 是产品的事；第 2 条：规则从代码读出了 SQL 层部分，`PreAssetQuery` 的 `is*Mergeable` 还没读完；第 3 条：上传类型识别、数值分段边界、排名并列的规则都已从代码读出并写了测试（见 4b-E、F），"设计是否合理"仍是产品的判断。

**需要用户决定：**
- 严格模式后数据库不可用也会让 CI 变红（以前异常被吞掉）：`ensure-db-server` 的等待和重试是否足够。
- ws 的 CI：只能手动触发、失败不挡合入、Jenkins 无 ws 阶段，是否调整。
- G8 做哪些新场景。
- G11 每类选哪些代表场景、各库用哪些表造数。

**没验证的：**
- 评估和处理后的运行都在本机（Windows）；CI 上严格模式后的结果未确认（CI 用同一份测试配置，上传的根因应相同）。
- 严格模式的整套重跑在清理 3 个陈旧基线之前，之后只重跑了 `DataComposition_Spec`、`WSOthers_Spec` 和 `checkMergeable CM1`（#213 已合并；2026-11-12 在带产品修复的版本上又跑了 `WSData_Spec` 24 个用例和 `property_crossJoin.maxCellCount`：4 个失败都是旋转表的基线受 #81102 修复影响，加上 cross join，共 5 个用例（6 个基线文件）受影响，其中 3 个用例的基线已更新，其余通过；6 个 spec 的整套没有再跑，CI 上的结果未确认）。
- `exportVSComponentData` 遇到既不是图表也不是表格的组件，会把上一个组件的数据再导出一遍，未改。
- 执行路径是静态信号（`mergeable=false`），实际是否下推没有运行确认。
- 下推分支清单（3b-C）只读了 `PreAssetQuery` 和各查询类的 `isSourceMergeable`；`XUtil.isQueryMergeable`（`BoundQuery.java:83`）当时没有读，后来读了并写了 `XUtilQueryMergeableTest`。各库支持矩阵来自 `sqlhelper.xml` 和 MySQL、SQL Server、Derby 三个覆盖，其余库没有代码覆盖是按 grep `supportsOperation(String op` 判断的。
- 49 个未运行的 worksheet 是否被其他 worksheet 引用为来源，未核对。
- 13.8 测试移植到 StyleBI：**已完成并合并**（4 个类、63 个测试，PR #8046），与 13.8 的差异分三种，见第 4 节；这条不再是待确认。
- `LIVE_MODE` 与 `RUNTIME_MODE` 的差别只确认了预览输入行数上限一项。
- 命名分组、变量、日期范围已在功能面差集里按能力清单统计，但它们在 worksheet 里的实际用法（命名分组是否被表应用、变量值如何传入）没有逐个核对。
- 功能面差集里 `InRange_Spec` 的日期范围名与 `DateCondition` 子类的对应只按名字判断；`AggregateFormula` 三个零覆盖公式（`SumSQ`、`SumWT`、`Aggregate`）是内部构件，用户选不到，已由 `AggregateFormulaStatSqlTest` 的组合 SQL 间接覆盖。
- 单测盘点中，除注明"只有序列化"的几个类外，其余按方法名判断（没有再扩展）；`SortFilter` 现在有 `SortSummaryFilterTest`（13.8 移植，15 个）。
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

**分桶编号**（上表非数据类的全部编号，和 Rejected 11 条；数据相关的 27 条已在上表逐条列出。合计 27 + 80 + 11 = 118，可据此复现分桶）：

- UI / 浏览器控制台（24）：#77023、#76941、#75845、#75317、#75314、#75258、#75228、#74920、#74737、#74434、#74407、#74200、#74199、#73997、#73573、#73553、#73551、#73488、#73414、#73269、#73268、#73181、#73093、#73019
- XSwapper / 内存交换（21）：#77877、#77830、#77710、#77693、#77685、#77683、#77682、#77676、#77655、#77653、#77652、#77649、#77638、#77636、#77635、#77623、#77622、#77608、#77593、#77591、#77579
- 已拒绝（不计）（11）：#78500、#78121、#77729、#77650、#75751、#75536、#74888、#73954、#73928、#73640、#73133
- 并发 / 死锁（9）：#77874、#77873、#77681、#77311、#77301、#77273、#77223、#77215、#77158
- 数据源认证 / 缓存（7）：#77730、#77699、#77692、#77672、#76658、#75755、#75737
- 无状态会话（7）：#74884、#74739、#73736、#73735、#73631、#73571、#73076
- 其他（6）：#79843、#79842、#78053、#76236、#74108、#73121
- Worksheet MV（3）：#78452、#78340、#78339
- 损坏数据导入的健壮性（3）：#77584、#77499、#77412

**结论：** 数据相关的 27 条里，没有一类在 datatest 里有明确覆盖；覆盖最薄的是重命名与依赖传播、快照、提示信息、JVM 区域、Long 变量，以及脚本表达式的几种写法（函数同名、`new Date(field[…])`、引用其他数据块）。对应缺口见 G13。

### 7b. 补单测时发现并报告的缺陷（2026-11-11 至 11-12）

这些不在上面 118 条的范围内（分类不同，或创建时间在 2026-11-11 之后）。"测试"指产品仓库里固定或验证它的单测，"datatest"指对应的用例或基线。

| bug id | 问题 | 状态 | 测试 | datatest |
|---|---|---|---|---|
| #81087 | `CrossJoinTableLens` 的行序取决于哪边先加载完，被行数上限截断的 cross join 每次保留的行不同 | Resolved（`main`） | 修复自带 `CrossJoinTableLensRowOrderTest` | `property_crossJoin.maxCellCount`：行序已确定，基线待更新（`Customer1` 无排序、依赖数据库行序，待决定） |
| #81091 | 连接列检查不阻止 hash、merge join 引擎（13.8 #80297） | Resolved | `JoinLensesTest` | 无 |
| #81092 | `join.table.maxrows` 把表头行算进上限（13.8 #80298） | Resolved | `JoinLensesTest` | 无 |
| #81093 | `TextComparer.isAsciiOnly` 把所有基本平面非 ASCII 字符当 ASCII，中文不按拼音排序 | Resolved | `SortSummaryFilterTest` | 无 |
| #81095 | 无原生函数的库里，协方差、相关系数在只有一列为 NULL 时算错 | Resolved | `AggregateFormulaStatSqlTest` | 无 |
| #81096 | 单行相关系数除零 | Resolved | `AggregateFormulaStatSqlTest` | 无 |
| #81102 | 旋转表第一列有重名值时丢列丢数据 | Resolved | `RotatedQueryTest` | `rotate`、`grouping1`、`condition1` 的基线已更新；`condition2` 两个待定（见 #81193） |
| #81105 | 数值分段的脚本表达式把 null 放进最后一段，SQL 放进 Others | Resolved | `NumericRangeRefBoundaryTest` | 无 |
| #81178 | `MonthsCondition` 对 `Timestamp`、带时间的 `java.util.Date`，内存判断和下推 SQL 不一致 | New | `DateConditionBuiltinTest` 固定现状 | 无 |
| #81193 | #81102 修复后，旋转表只从源表前 250 行生成列，保存的 worksheet 里名字出现得晚的列悄悄消失（回归） | New | 暂无（探针验证过，未写进单测） | `condition2/Query1`、`Query3` 的基线不更新，等开发 |
| #81220 | `CSVInfo` 不处理引号时用正则切首行，`\|` 分隔的首行被拆成单个字符，首行有数字就不被当作表头 | New | `CSVInfoDetectionTest`（产品分支 `test-csvinfo-detection`，未推送） | 无 |
| #81229 | mongo 的"属于（ONE_OF）"检查只在 `isConditionListMergeable`，不在拆分用的 `isConditionItemMergeable`，带 `ONE_OF` 的 where 整个留在内存（性能，结果相同） | New | `PreAssetQueryConditionMergeableTest` 固定现状（产品分支 `test-csvinfo-and-mergeable-unit-tests`，未推送） | 无 |
| #80538 | 非 distinct union 加载中多算一行（原来就有） | New | `SetOperationLensesTest` 的最后一个 `@Disabled` | 无 |

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py capability ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py flagcheck ws/src/test/resources/config/wsStorage.zip
python .claude/workflow-shared/datatest-gap/ws_scan.py dbmatrix ws/src/test/resources/config/wsStorage.zip <产品仓库>/community/core/src/main/resources/inetsoft/uql/sqlhelper.xml ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py crosscheck ws/src/test/resources/config/wsStorage.zip ws/src/test/resources/expectData ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
./mvnw package -pl commons -DskipTests && ./mvnw test -pl ws
# 更新基线：加 -Dupdate.baseline=true（导出文件写回 ws/src/test/resources/expectData，不比对）
# 本机删除过 spec 或基线后先 ./mvnw clean -pl ws，target 里的旧拷贝会让严格模式误判
```
