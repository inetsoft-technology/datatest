# ws 测试缺口与补充方案

> 日期：2026-11-10　模块：`ws`（无 profile，`./mvnw test -pl ws`），spec：`ws/src/test/groovy/inetsoft/test/worksheet/cases/` 下 7 个
> datatest 基线：`Stateless-Sessions1`（`f83c650a`）；产品源码：`main`（`origin/main` `f0ef913306`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 一致）。本机产品仓库在 `D:\stylebi\community`，工作流默认路径不存在。

## 1. 结论

ws 的用例规模不小（98 个用例、873 个基线文件），团队设计表 `docs/ws/Worksheet Auto.xlsx` 里的组合也基本都做成了用例。**但真正在验证数据的不到一半。**

- 断言基类 `WorksheetTest` 吞掉异常、只比实际导出的文件，[#203](https://github.com/inetsoft-technology/datatest/pull/203) 只改了 `ViewsheetTest`，没有覆盖它。本机实际运行：98 个用例里 90 个"通过"，其中 46 个什么都没导出（`WSBuildServer_Spec` 40 个：worksheet 不在 zip 里；上传用例 6 个：基类第 214 行空指针），真正比对了数据的只有 44 个。
- CI（GitHub，ubuntu）上 `WSBuildServer_Spec`、`InRange_Spec` 整个跳过，所以**相对日期条件（date in）在 CI 上是 0 覆盖**；GitHub 工作流只能手动触发，失败也不挡合入；Jenkins 没有 ws 阶段。
- 所有用例都用设计时的 `LIVE_MODE` 取数，从没走过 viewsheet / MV 实际用的 `RUNTIME_MODE`；名为 `checkMergeable` 的用例也只比数据，不检查查询是否下推到 SQL。
- 产品单测：`RotatedQuery`、`UnpivotQuery`、上传与类型识别、worksheet 层的合并判断没有任何测试；intersect、minus、cross join、rotate、max rows 只有序列化测试。13.8 已有可移植的 lens 测试（4 个测试类、70 个）。

先做：把 `WorksheetTest` 改成和 `ViewsheetTest` 一样的严格模式（先处理会因此变红的 46 个假通过和 3 个陈旧基线），再移植 13.8 的 lens 测试，补 unpivot、date in、上传、合并判断的单测。

## 2. 覆盖情况

**规模：** 7 个 spec、98 个用例（`DataComposition` 10、`InRange` 4、`WSAsset` 2、`WSBuildServer` 40、`WSData` 24、`WSOthers` 9、`WSProperty` 9），基线 873 个 txt。`wsStorage.zip`：94 个 worksheet、4 个 viewsheet、32 个逻辑模型、13 个数据源（Oracle、MySQL、DB2、SQL Server、Informix、PostgreSQL、ClickHouse、Derby、Access、sqlite 等）。

- `WSBuildServer_Spec` 引用的 40 个 `BuildServer/...` worksheet **不在 zip 里**，导入 `assets/BuildServer.zip` 的代码已注释（`WSBuildServer_Spec.groovy` `setupSpec`），仓库里也没有这个文件。
- 49 个 worksheet 没有被任何用例运行（如 `checkMergeable/CM1_7`、`CM8_distinct`、`CM8_maxrow`、`db2formula`、`oracle q1`），是否作为其他 worksheet 的来源未核对。
- CI 实际运行的范围（去掉 Linux 上跳过的 2 个 spec 和 4 个 `@Ignore` 用例）：42 个 worksheet、470 张可见表。

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

- 运算符：inner 27、cross 14、union 10、left 8、minus 6、merge join 5、right 5、intersect 5、full 5、`>` 3、`<` 2、`<=` 2、`<>` 2；**`>=` join 只在没被运行的 worksheet 里**。
- 条件运算符：前置 equal 25、greater 24、one of 18、less 16、null 12、between 8、contains 5、starting with 2、like 1；后置共 15；排名 top 9、bottom 4。**date in 在 CI 范围内为 0**（只出现在 `InRange_Spec` 的 worksheet 里）。
- 聚合公式 24 种，Sum 59、Count 13、Max 12，其余多为 1～2 次；日期分组 16 个级别；范围列 DateRangeRef 139、NumericRangeRef 18；表达式列 SQL 113、脚本 82。
- 数据源（表数）：Oracle 122、MySQL 41、mvtest 27、DB2 21、Examples/Orders 18、SQL Server 12、Informix 9、sqlite 5、PostgreSQL 5、Access 3、securityTest 3、Derby 2；ClickHouse 用例被 `@Ignore`。
- 执行路径：只能静态看到 `mergeable=false`（用户关掉合并）7 张表；查询实际是否下推到 SQL 由运行时决定，**没有任何用例断言它**（见 G5）。
- 取数方式：98 个用例全部 `LIVE_MODE`（`WorksheetTest.groovy:135`、`:222`），`RUNTIME_MODE` 为 0（见 G6）。
- 业务设计表对照：`Worksheet Auto.xlsx` 中写了"ws->表"的行，176 行对应的表被运行并导出；17 行的 worksheet 不在 zip（如 `DataBaseQuery->leftJoin`），13 行的表名在 worksheet 里找不到（如 `range&formula->product1` 9 行），1 行是隐藏表。

**验证机制：**

- 比对遍历的是实际导出的文件，不是基线清单：`WorksheetTest.compareData`（`:310`）调用 `CompareUtil.CompareFileByFeature(null, ...)`，遍历导出目录。少导出的文件不会被发现。
- 导出失败不报错：`executeWS` 三参数版本 `catch(Exception)` 后只打印堆栈（`:100-105`，98 个用例中 97 个用这个版本）；`importCSVToEMTable`（`:176-181`）、`exportVSComponentData`（`:270-272`）同样。一张表出错时，同一 worksheet 里后面的表也不再导出（循环在 `try` 之内）。
- `exportData` 只删除同名文件，不清理 case 目录；GitHub 工作流先执行 `./mvnw clean`（`test-ws.yml:71`），本地重跑会有残留。
- CI：`test-ws.yml` 只有 `workflow_dispatch`（定时触发已注释，`:4-7`），测试步骤 `continue-on-error: true`（`:75`），运行在 `ubuntu-latest`（`:17`）；Jenkins 没有 ws 阶段。
- **本机实际运行**（2026-11-10，Windows）：98 个用例，失败 1、跳过 7、通过 90。

| 情况 | 用例 | 原因（日志） |
|---|---|---|
| 通过，但一个文件都没导出 | `WSBuildServer_Spec` 40 个 | 每个都报 `Sheet not found`，异常被吞 |
| 通过，但一个文件都没导出 | `WSProperty_Spec` 上传 6 个（xlsx、xls、txt、csv、数字/日期类型识别） | 24 次上传都在 `WorksheetTest.groovy:214` 报 `NullPointerException: Cannot get property 'message' on null object`（返回结果里没有 `validator`），异常被吞；根因未查 |
| 通过，基线有文件未导出 | `checkMergeable CM1`：`CM1_3.txt`、`CM1_5.txt`；`top sql query`：`Report/top1.txt` | 前两张表在 worksheet 里不存在；`Report/` 是报表时代的基线，只比对 `VS/` |
| 失败 1、跳过 3 | `InRange_Spec` | 改系统日期的 `runas` 未生效（无保存凭据），按真实日期运行，相对日期结果与基线不符；`@Stepwise` 使后 3 个跳过 |
| 跳过 4 | `specialChar`、`clickhouse`、`ws group case check`、`customer bugs` | `@Ignore` |

  真正比对了数据的用例 44 个（`DataComposition` 10、`WSAsset` 2、`WSData` 22、`WSOthers` 8、`WSProperty` 2）；873 个基线文件中 388 个本次没有被比对。没有"导出了但没有基线"的文件。
- 基线来源：`expectData` 共 27 次提交，均为"add exp""update txt"一类，是产品输出生成的回归基线，不是独立标准答案。

**断言层：** 每张可见表导出为整表 txt（排序后），没有 PNG。隐藏表（13 张）不导出，只通过依赖它的可见表间接验证。查询是否下推、生成的 SQL、预览行数上限都不在断言里。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | `WorksheetTest` 吞异常、只比实际导出的文件，#203 未覆盖 | worksheet 打不开、某张表查询抛异常、上传失败，用例照样通过（本机已有 46 个这样的用例） | 机制：按 #203 的方式改 `WorksheetTest` 为严格模式 | 高 |
| G2 | `WSBuildServer_Spec` 的 40 个 worksheet 不在 zip，Linux 上又被跳过 | 302 个基线文件代表的客户场景全部未验证，却显示为通过 | 机制/资产：补回资产或删除 spec 与基线（需决定） | 高 |
| G3 | 上传用例 6 个假通过；上传服务、`TextUtil.detectType` 无单测 | CSV/Excel 导入的列类型识别、分隔符、首行表头、unpivot 出错 | 机制（同 G1）+ 单测 | 高 |
| G4 | date in 条件在 CI 上 0 覆盖：`InRange_Spec` 在 Linux 跳过，只能在保存了管理员凭据的 Windows 上改系统时间运行；单测只覆盖本年/上年、本月、本周、本季度 | "年初至今""最近 N 个月/天""第 N 季度"等相对日期范围算错 | 单测（固定参考日期，期望值手算）；dashboard 不再改操作系统时间 | 高 |
| G5 | 执行路径未验证：`checkMergeable` 用例只比数据；`PreAssetQuery.isQueryMergeable` 无单测 | 本应下推的查询退化到内存（性能问题），或下推与内存两条路径结果不一致（null 排序、字符串比较、日期分组） | 单测（合并规则）+ 基类记录合并结果 | 中 |
| G6 | 只用 `LIVE_MODE`，从未走 `RUNTIME_MODE` | 只在运行时出现的错误：viewsheet/MV 读 worksheet 的结果、`runtimeSelected`、预览输入行数上限（`asset.sample.maxrows=50000`，`AssetQuery.java:3957`）以外的行为 | 机制：基类支持 `RUNTIME_MODE`，按查询类型各选代表用例 | 中 |
| G7 | lens 取值无单测：rotate、unpivot 完全没有；intersect、minus、cross join、max rows 只有序列化；full outer join 没有；非等值 join 只测取消；排名 lens 没有取值测试 | 这些操作的结果行、表头、null 处理出错，只有 datatest 回归基线可能发现 | 单测：先移植 13.8，再补缺 | 高 |
| G8 | 薄弱格子：`>=` join、date in 在 CI 范围为 0；LIKE 1、starting with 2；Rotated/Unpivot/Concatenated 几乎没有后置条件；ClickHouse 被忽略 | 这些组合下的接线出错 | dashboard：新建小 worksheet（tester 衡量） | 中 |
| G9 | 49 个 worksheet 没有用例运行 | 若是有意准备的场景，则对应能力未验证 | 核对后补用例或删除 | 低 |
| G10 | 3 个陈旧基线（`CM1_3`、`CM1_5`、`Report/top1.txt`） | 让人误以为这些对象被验证过 | 随 G1 清理 | 低 |

方向判断：期望值能独立算出 → 单测（G3、G4、G5 规则、G7）；接线 → dashboard（G8，以及 G4、G6 各少量代表用例）；验证机制本身 → 改基类（G1、G2、G6）。

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
| `TextUtil.detectType`、`CSVLoader`、`ImportCSVDialogService`（预览、unpivot、行列上限） | `TextUtil`、上传服务无测试；`CSVLoaderTest` 只测数字/货币/百分比 | 日期、布尔类型识别，分隔符，首行表头，unpivot，上限提示 | 12 | 中 | 日期与布尔的识别规则 |
| `RankingTableLens` | 只有序列化与失败 | top/bottom N 取值、并列 | 6 | 低 | 并列规则 |
| `SummaryFilter2`（worksheet 分组聚合）+ `DateRangeRef` 级别 | 只有序列化与取消 | 各日期级别分组后的聚合值 | 6 | 中 | 无 |
| `NumericRangeRef` | 只测改名与依赖 | 数值分段 | 4 | 低 | 区间边界的归属 |
| **合计** | | | **移植 70 + 新写 65** | | |

按优先级：先移植 13.8（G7 主体），再写 date in（G4）和上传（G3），unpivot（G7）次之，合并判断（G5）要先等开发确认规则。

## 5. Dashboard 与机制补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 严格模式（G1） | 改 `WorksheetTest`：导出失败让用例失败、每张表单独捕获后汇总、基线文件必须都已导出、`-Dupdate.baseline`、清理 case 目录；`importCSVToEMTable` 空值安全并在失败时报错；`exportVSComponentData` 同样 | 打不开、查询异常、上传失败、少导出都会变红 | 先处理会立即变红的：G2 的 40 个、G3 的 6 个、G10 的 3 个陈旧基线 |
| `WSBuildServer_Spec`（G2） | 二选一：补回 `BuildServer` 资产并去掉 Linux 跳过；或删除 spec 与 302 个基线 | 客户场景回归 | 资产来源需确认 |
| 上传（G3） | 修好基类后重跑 6 个用例，确认导出与基线一致 | 上传接线 | 先查 `:214` 空指针的根因 |
| date in（G4） | 语义放单测；dashboard 留 1～2 个代表用例，不再依赖改操作系统时间 | 条件接线 | 产品是否支持测试用的参考日期（待开发确认） |
| 执行路径（G5） | 基类记录每张表是否合并（或生成的 SQL）并纳入基线，先用于 `checkMergeable` 用例 | 下推与否的回归 | 需找到取合并结果的入口（未查） |
| 运行时取数（G6） | 基类支持 `RUNTIME_MODE`，每种查询类型选 1 个代表 worksheet | viewsheet/MV 实际取数路径 | 无 |
| 薄弱格子（G8） | 新建小 worksheet：`>=` join、date in、LIKE/starting with、Rotated/Unpivot/Concatenated 上的后置条件、ClickHouse | 这些组合的接线 | tester 衡量做哪些 |
| 未运行的 worksheet（G9） | 核对 49 个是否作为来源被引用，再补用例或删除 | | 无 |

做法原则同 vscalc：新建小 worksheet，不改已有资产；改 zip 后用 `ws_scan.py inventory` 前后对比。

## 6. 局限与待确认

**需要开发回答：**
1. 上传：`origin/main` 的 `ImportCSVDialogService.getPreviewTable` 每条路径都会放入 `validator`，本机运行的返回结果里却没有。是本机产品版本不同，还是 datatest 的调用方式过时？
2. `DateCondition` 能否注入参考日期（时区、周起始日规则一并确认）？否则 date in 只能靠改系统时间测试。
3. worksheet 层的合并规则：哪些条件、聚合、表达式、数据类型组合应当下推到 SQL。
4. 上传的日期、布尔类型识别规则；`NumericRangeRef` 区间边界；排名并列规则。

**需要用户决定：**
- `WSBuildServer_Spec`：补回资产还是删除。
- 是否现在就把 `WorksheetTest` 改为严格模式（会先变红 46 个用例，另有 3 个陈旧基线文件，需同时处理）。
- ws 的 CI：只能手动触发、失败不挡合入、Jenkins 无 ws 阶段，是否调整。
- G8 做哪些新场景。

**没验证的：**
- 只在本机（Windows）运行过一次；CI 上上传用例是否同样空指针未确认。
- 执行路径是静态信号（`mergeable=false`），实际是否下推没有运行确认。
- 49 个未运行的 worksheet 是否被其他 worksheet 引用为来源，未核对。
- 13.8 测试移植到 StyleBI 的兼容性未逐个核对，用例数按 13.8 的测试数计。
- `LIVE_MODE` 与 `RUNTIME_MODE` 的差别只确认了预览输入行数上限一项。
- 单测盘点中，除注明"只有序列化"的几个类外，其余按方法名判断；`SortFilter` 自身的测试未盘点。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory ws/src/test/resources/config/wsStorage.zip ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
python .claude/workflow-shared/datatest-gap/ws_scan.py crosscheck ws/src/test/resources/config/wsStorage.zip ws/src/test/resources/expectData ws/src/test/groovy/inetsoft/test/worksheet/cases/*_Spec.groovy
./mvnw package -pl commons -DskipTests && ./mvnw test -pl ws
```
