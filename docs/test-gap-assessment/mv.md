# mv 测试缺口与补充方案

> 日期：2026-11-13　模块：`mv`，profile `-Ptopmv`、`-Psubmv`、`-Pincrementalmv`、`-Pwsmv`、`-Pvpm`、`-Pothers`，20 个 spec
> datatest 基线：`Stateless-Sessions1`（`ca972e26`）；产品源码：`main`（`28cf7947b9`，`2.1.0-SNAPSHOT`，与 datatest `pom.xml` 一致）
> 13.8 对照：本机 `E:\style-intelligence`，分支 `epic-test-coverage`（`6b0ba0e7f2`），见 §4 末尾

## 1. 结论

mv 的验证机制是各模块里最严的一个：每个用例导出的文件数必须等于基线文件数，逐文件比对（`MVTest.groovy:798`-`836`），导出异常和 MV 创建失败都会让用例失败，再配上"命中了哪个 MV"的断言和"与不用 MV 的结果比较"。本机 6 个 profile 全部运行了一遍：615 个用例执行，**12 个失败**（vpm 4、incrementalmv 3、others 5），全部在单独重跑时稳定复现。others 的 5 个在 `-Dmv.compare=both` 下，MV 的结果与不用 MV 的结果一致，只有基线与两者都不同，说明这 5 个是基线与当前输出不一致，不是 MV 路径与实时路径的差异；原因没有确认（见 G8），CI 上的结果也没有确认。

**已经做过和已经决定的**（本文不重复）：命中断言（datatest PR #188，约 489 处断言换成命中记录）、MV 开/关对比（`-Dmv.compare`，PR #193）、增量 MV 的源数据变化和全量重建对比（PR #194、#195）、WS MV 的一条通路（PR #200）、已修 bug 的 golden 更新（PR #196、#197）、`@Ignore` 用例和旧链接的整理（PR #198）、`others` 每个 spec 单独 JVM（PR #199）、cycle 在 `test/cluster` 的验证（enterprise PR #1604、#1701，我没有核对）。以下几项按维护者的说明是**已决定不做**的（仓库里没找到书面记录）：datatest 各 workflow 的定时运行不恢复、`mvStorage.zip` 的清理不做、`where:` 数据表改写不做、`@Retry` 不改。

本文新增的部分：

1. **反向路径几乎没有用例**（G2）：全部用例里只有 2 个断言了"没有命中"；zip 里专为此准备的 11 个 `NOT Transform` viewsheet 没有任何 spec 引用；产品侧决定 MV 能不能建、能不能命中的分支（`VSMVAnalyzer`、`SelectionUpTransformer` 的二十多个 fault）既没有 dashboard 用例，产品仓库也几乎没有单测（`SelectionUpTransformer` 2048 行只有 3 个测试，`MVTransformer` 673 行没有，G3）。
2. **`mv.*` 配置项几乎空白**（G4）：产品读的 20 多个属性里只有 `ws.mv.enabled` 被设置过。
3. **`wsmv` profile 不在 CI 里**（G6）：`test-mv.yml` 只跑 incrementalmv、topmv、submv、vpm、others 五个 profile，PR #200 加的 `WSMV_Path_Spec` 在 CI 里从不运行（静态结论，只看了 `.github/workflows/test-mv.yml`）。
4. **本机 12 个用例失败**（G8）：与已知的几处问题对得上（#79870、#79508 等），见 §2 的运行结果；基线还没跟上这些修复。
5. **组件和选项的零覆盖项**（G13）：`viewsheetInfo` 的 `associationEnabled=false`、`bypass=true`、`meta=true` 在 804 个 viewsheet 里一个都没有；整个 `CalcMV` 目录 38 个 viewsheet 没被引用（§2 的详细扫描）。

仍然成立的两点：命中断言留有一个已知的洞（78 个用例只断言"已定义"，是 PR #188 里没换的含 `ASSOCIATION_` 的断言，G1）；`getMVHitInfo` 在组件都返回纯值时退回成"已定义"。

先做：把 `wsmv` 加进 `test-mv.yml`；用现成的 `NOT Transform` 资产补反向路径用例；补 `mv.*` 配置项几个会改变结果的场景；本机失败的 12 个用例在 CI 上确认后，按各 bug 的状态逐个更新 golden（核对新输出，不直接复制）。

## 2. 覆盖情况

**规模：** 20 个 spec、622 个用例（按 `def` 统计；其中 10 个 `@Ignore`；surefire 实际执行 615 个、跳过 11 个，差异来自参数化用例的展开，`IncrementalDataChange_Spec` 12 个方法展开为 16 个）、3323 个基线文件、`mvStorage.zip`（`mvStorage-new.zip` 未跟踪，不在评估范围）里 804 个 viewsheet、414 个 worksheet、360 个书签。用例引用的 viewsheet 资产 id 共 610 个，609 个在 zip 里（另 1 个是 `${path}` 模板，由 `WSMV_Path_Spec` 在运行时创建）；**195 个 viewsheet 没有被任何 spec 按字面量引用**（`hit mv` 110、`CalcMV` 38、`NOT Transform` 11、`MV_Creator` 7 等），其中 12 个的末段名字出现在 spec 文本里（可能是间接引用），1 个有基线目录（`CalcField/NotPublic_Brush1`），其余 182 个既无引用也无基线。资产 id 不是字面量的写法在静态扫描里看不到，195 里可能含假孤儿。

**覆盖矩阵**（维度：MV 类型 × 命中的查询形态 × 数据源，已与用户确认；数字来自 `mv_inv.py` / `mv_tests.py` 对 spec 的正则统计，用例按 `def '名'()` 切分，命中断言形式按 `getMVHitInfo()` / `getMVDefInfo()` 区分）：

| profile | spec | 可执行用例 | `@Ignore` | 基线文件 | 断言命中 | 只断言已定义 | 两者都没有 | `compareWithoutMV` |
|---|---|---|---|---|---|---|---|---|
| topmv | 5 | 127 | 3 | 740 | 122 | 5 | 0 | 127 |
| submv | 2 | 85 | 1 | 729 | 83 | 2 | 0 | 83 |
| incrementalmv | 4 | 168 | 0 | `MV_Creator` 189 + `MV_Transform` 130 | 150 | 4 | 14 | 0（`IncrementalDataChange` 另有 11 处 `rebuildMV`） |
| wsmv | 1 | 1 | 0 | 含在 `MV_Creator` | 0 | 0 | 1 | 1（用探针行判断读的是 MV） |
| vpm | 1 | 14 | 0 | 39 | 14 | 0 | 0 | 10 |
| others | 7 | 217 | 6 | 1495（`hit mv` 807、`CalcField` 268、`AssociationMV` 261、`vs query and condition` 96、`tabular source` 63） | 145 | 68 | 4 | 188 |
| 合计 | 20 | 612 | 10 | 3323 | 514 | 79 | 19 | 408 |

表中"可执行用例"是按 `def` 数的、去掉 `@Ignore` 的用例数；incrementalmv 的 surefire 执行数是 172（参数化展开）；others 按 `def` 数是 217，surefire 执行 216（223 个减 7 个跳过），差 1 个来自 `MVHit_RankingAndFilters_Spec`，原因没查；topmv、submv 与 surefire 一致；"只断言已定义"合计 79 = 78 个含 `ASSOCIATION_` 断言的用例 + `CalcField_Spec.NotPublic_Text`（带 `TODO`）。

- **MV 类型：** TopMV 在 topmv、others、incrementalmv 里；SubMV 主要在 submv；关联 MV（`ASSOCIATION_` 前缀）在 `AssociationMV_Spec`（37 个用例，其中 `getMVDefInfo` 断言的占大多数，见 G1）；增量 MV 在 incrementalmv（追加、删除、转换三类）；WS MV 只有 `WSMV_Path_Spec` 一个用例；VPM 在 `VPM_Spec`。
- **命中的查询形态（按 spec 名称分，非精确）：** 聚合公式（`TopMV_AggregateFormulas` 30、`MVHit_GroupAndAggregates` 32）、条件（`TopMV_Conditions` 32、`VSCondtion` 42）、排名与过滤（`MVHit_RankingAndFilters` 46）、表结构（`TopMV_TableStructures` 27）、子查询（`TopMV_Subquery` 20）、计算字段（`CalcField` 40）、关联与选择（`AssociationMV` 37）、组件动作（`VSActions` 21）。
- **数据源：** zip 里 worksheet 绑定的数据源：JDBC 为主（`db2_1` 物理表 177 个、`mvtest`（H2）200 个、`oracle1` 51 个、`Examples/Orders` 23 个、`access` 19 个、`postgres` 5 个、`MySQL` 2 个、`sql2005` 1 个，另有模型、SQL 查询形式的绑定），Tabular 11 个表（Data gov、MongoDB REST、OData、REST JSON、Text Excel Directory）。`db2_1`、`oracle1`、`postgres` 的连接地址是 `db2_server`、`oracle_server`、`postgres_server`（CI 的 DB 服务器），`mvtest` 是 H2 文件库。**每个用例落在哪个库没有按用例统计**（未统计），所以"某个库上的 MV 行为"是否被覆盖不能下结论。Tabular 的 `TabularSource_Spec` 整个类 `@Ignore`（5 个用例）。

**验证机制：**

- 比对遍历"实际导出"并要求文件数相等：`compareData` 列出导出文件和基线文件，数量不同直接失败（`MVTest.groovy:816`-`818`），导出目录或基线目录不存在也失败（`:807`-`813`），再逐文件比内容（`:836`）。导出了但没有基线的文件，会因文件数不同而失败，所以不存在"导出了但没有基线"的静默情形。
- 导出异常让用例失败：所有书签跑完后汇总，有错就抛 `AssertionError`（`:174`-`:179`）；MV 创建失败、分析失败在 `MaterializedViewResource.java:123`-`:126`、`:180`-`:186` 抛异常，创建失败先于"完成"判断（`:235`）。
- 命中断言（PR #188）：`getMVHitInfo()` 只记录真正读过的 MV；**当所有组件都是纯值（文本等），拿不到 MV 信息时，退回 `getMVDefInfo()`，即只代表"已定义"**（`MVTest.groovy:455`-`461`，`hitUnobservable` 设置在 `:327`、`:435`）。图表数据被拷成内存快照时同样丢信息（`:432`-`:435`）。
- 与不用 MV 的结果比较（PR #193）：`-Dmv.compare=golden`（默认）只比基线，`nomv`、`both` 才比不用 MV 的结果（`MVTest.groovy:55`、`:856`-`:857`）。**CI 用默认的 golden**，所以 `compareWithoutMV()` 在 CI 里不执行（`:857` 直接返回）；基线是回归基线（来自 MV 的输出），不是独立标准答案。整套没有跑 `nomv`、`both`；只对 others 的 5 个失败用例跑过 `both`（见下面的运行结果）。
- 导出目录清理：`.github/workflows/test-mv.yml` 每个 profile 的步骤开头 `rm -rf mv/target`，运行后把报告复制到 `mv-reports/<profile>/`；本机连跑多个 profile 前也要手工删，否则存储互相污染。`Jenkinsfile` 里没有 mv 阶段。
- **CI 触发与失败处理：** `.github/workflows/test-mv.yml` 只有 `workflow_dispatch`，定时触发被注释掉（`:4`-`:6`），这是维护者的决定（所有 datatest workflow 的 `schedule` 都不恢复）；每个 profile 失败时 `exit $status`，`!cancelled()` 保证后面的 profile 照跑，整个 job 仍然失败。**workflow 里只有 `incrementalmv`、`topmv`、`submv`、`vpm`、`others` 五步（`:82`、`:97`、`:112`、`:127`、`:142`），没有 `wsmv`**，所以 `WSMV_Path_Spec` 在 CI 里不运行。`Jenkinsfile` 没有 mv。
- **实际运行结果（本机 Windows，datatest `Stateless-Sessions1` + 产品 `main`，默认 `golden`，每个 profile 单独 `-P` 运行、之前清空 `mv/target`）：**

| profile | 结果 |
|---|---|
| topmv | 130 个用例：0 失败，3 跳过（`@Ignore`） |
| submv | 86 个用例：0 失败，1 跳过 |
| wsmv | 1 个用例：0 失败 |
| vpm | 14 个用例：**4 失败**（`condition`、`group-group`、`group-subgroup`、`group-user`）：`order_time`（postgres 的 `time with time zone`）一列期望 `06:24:25`、实际 `06:01:01`，其余列相同；spec 注释已说明这一列取决于时区（`VPM_Spec.groovy:44`-`:47`）。本机时区是 China Standard Time（UTC+8），与 CI 的 `TZ=Asia/Shanghai` 一致，所以不是 JVM 时区不同。这一列的值带偏移 `+07:36:36`；据维护者此前的核对（我没有重新核对）：不用 MV 时被换算成本地时区（+08:00），有 MV 时只保留一天内的时间。期望值 `06:24:25` 比实际 `06:01:01` 多 `0:23:24`，正好是 `+08:00` 与 `+07:36:36` 的差，也就是**基线里是换算后的值，现在 MV 给出的是原始时间**，像是产品对这一列的处理变了（与 #79858、#79508 同类，**哪个修复引起的没有核对**）；基线在 07-17 因"time value"改过（`40aad662`） |
| incrementalmv | 172 个用例（`IncrementalDataChange` 的参数化展开为 16 个）：**3 失败**，行数都差 1：`Append_Spec.mvLastUpdatetime`（期望 11 行、实际 10）、`Delete_Spec.Testcase8`（5 对 4）、`Transform_Spec.Other_TestCase17`（123 对 122）。这个 profile 没有 `compareWithoutMV`（增量的 MV 本来就不等于数据库，所以不做开/关对比），无法区分是 MV 的错还是基线陈旧。据维护者说明，这个 profile 的数据依赖当前日期、每年要更新基线，三个用例都是行数差 1，**更像日期漂移，但没有核对这三个用例的条件** |
| others | 223 个用例：**5 失败**，7 跳过（`TabularSource_Spec` 整类 5 个；`MVHit_RankingAndFilters_Spec` 2 个，含 `chart-time type`）。失败：`MVHit_GroupAndAggregates.running total`（postgres 时间列 `00:00:00` 对 `23:00:00`，与 vpm 同一类）、`MVHit_RankingAndFilters.selection association4`（期望 NULL、实际 0）、`VSCondtion.assembly_query`（期望 0、实际 NULL）、`VSCondtion.C_V_5`（257 行对 255 行）、`VSCondtion.expression`（44 行对 23 行）。`-Dmv.compare=both` 下这 5 个的"MV 与不用 MV"比较都通过，golden 比较失败。`C_V_5`、`expression` 的行数（257/255、44/23，含 3 行表头）与 Redmine #79870 的验证备注（2026-11-06）一致：`expression` 41 行变 20 行、`C_V_5` 254 行变 252 行；#79870 的修复让"不等于"不再保留空值行（stylebi PR #7756、#8045），golden 是 PR #196 按 #79505 更新的；`running total` 是 #79508（Feedback，golden 按 MV 结果生成）；`selection association4`、`assembly_query` 的 NULL 与 0 之差我没有在 Redmine 找到对应记录，可能同属空值语义的修复，未核对 |

  本机的 `inetsoft-core-2.1.0-SNAPSHOT.jar` 构建于 2026-11-13 08:25，早于 `main` 当前的 `28cf7947b9`（11:14）；之后的 5 个提交是组织 id、combo box 等（没有核对它们是否影响查询结果）。jar 里已经含 11-12 的 #8082（#80155）、#8083（#80458）、#8084（#80446）三个 MV 修复，这些修复会改变 MV 输出的顺序和空值位置，datatest 的基线可能还是修复前生成的，这是**假设**，没有验证；Redmine 上已记录了其中大部分（#79870、#79508、vpm 的时间列），所以这 12 个多数不是新问题，是基线还没更新。

  topmv 的 16 个、submv 的 3 个基线文件本次没有被导出，对应的是 `@Ignore` 的用例（`composite3`、`top-aggregate-maxrow` 等）；没有"导出了但没有基线"的文件。PNG 没有参与（mv 的比对都是文本）。

**断言层：** mv 导出的是表格数据、选择列表、日历、时间滑块、范围滑块等的数据（`MVTest.groovy:701`-`:790`），导出的是运行时组件数据而不是 PNG；没看到对不导出组件的统计，未统计。命中信息记录在 `hitMVs`，与基线分开，不写进基线文件。

**功能面差集（步骤 3b-A）：** 产品能力清单来自产品源码（`file:line` 见下），对照 zip 和 spec 的使用情况：

| 能力 | 来源 | datatest 使用情况 |
|---|---|---|
| 不可合并的聚合公式（DistinctCount、Median、Mode、Product、Concat、NthLargest、NthSmallest、NthMostFrequent、PthPercentile） | `AggregateFormula.java:804`、`:1217`、`:1316`、`:2694`、`:2735`、`:2784`、`:2840`、`:2896`、`:2952` | 被 spec 引用的 viewsheet 里（读 `formulaValue`，用户设置节点）：DistinctCount 23、Median 11、Mode 4、NthLargest 3、NthMostFrequent 3、PthPercentile 2、NthSmallest 1；**Product、Concat 为 0**（不只是 `formulaValue`：在 viewsheet 所有节点的文本和属性里搜这两个词也是 0） |
| 其余聚合公式 | `AggregateFormula.java:624`、`:751`、`:1485`-`:2399` | Count 239、Sum 209、Average 34、WeightedAverage 15、First 13、Correlation 12、Max 7、Min 4、Variance 4 等；**SumSQ、SumWT 在 viewsheet 所有节点里都是 0**；`Last` 在 `formulaValue` 里是 0，只在 1 个文本组件的标量绑定（`aggregateValue`）里出现（被 spec 引用）；这些统计只覆盖 viewsheet 的聚合引用，worksheet 里的聚合公式另见 `ws_scan.py inventory` |
| 条件运算符（MV 过滤支持 ONE_OF、BETWEEN、<、>、=、NULL、STARTING_WITH、CONTAINS、LIKE） | `MVConditionListHandler.java:283`-`:322` | worksheet 前置条件里有 between 10、contains 6、equal to 34、greater than 14、less than 30、null 3、one of 27、starting with 7、date in 1；**没有 like**；viewsheet 的选择条件、MV 更新/删除条件的运算符未统计 |
| MV 配置项（`mv.*`、`ws.mv.enabled`、`date.common.options`、`time.common.options` 等） | `core/src/main/resources/inetsoft/report/defaults.properties:123`-`:130`，读取位置见产品报告的 A12 | 只有 `ws.mv.enabled`（`WSMV_Path_Spec.groovy`）被设置；`mv.outer.moveUp` 只出现在 `MVTest.clearEnv` 里复位，没有任何用例设置它；其余全部为 0：`mv.union.moveUp`（`SelectionUpTransformer.java:1906`）、`mv.ignore.nestedSelection`（`:1449`）、`mv.preferred.block`（`MVBuilder.java:964`）、`mv.dim.max.size`（`XDimDictionary.java:90`）、`mv.detail.data`（`ViewsheetSandbox.java:1467`）、`mv.cache.association`、`mv.table.metadata`、`mv.ondemand`、`mv.required`、`mv.merge.deleteCond`（`MVCreatorUtil.java:831`）、`mv.freshness`、`mv.maxAge`、`mv.max.block`、`date.common.options`、`time.common.options`（`DateMVColumn.java:64`、`:100`） |
| MV 种类 | `MVDef.java:2922`（WS MV）、`:2902`（关联 MV）、`:242`（共享）、`:2494`（`isSharedBy`） | WS MV 1 个用例；共享 MV（`shareMV`）、用户/角色/组范围的 MV 选择（`MVManager.java:677`-`:730`）只有 `vpm`、`multitenant` 部分涉及（`multitenant` 7 个 viewsheet，只有 1 个被引用） |
| 数据源类型的限制（拒绝嵌入表、cube、查询变量） | `VSMVAnalyzer.java:700`-`:736`、`:927`-`:980` | 反向用例的资产有（`NOT Transform` 的 cube、embedded 两个、subquery、group variable），但没有 spec 引用 |
| 增量方式（追加、强制追加、删除、`MV.<column>.Max/Min`、`MV.LastUpdateTime`） | `LocalMVIncremental.java:95`-`:150`、`:353` | incrementalmv 的 Append（65）、Delete（37）、IncrementalDataChange（12）覆盖；增量与旋转/反转/交叉表的组合被分析器拒绝（`VSMVAnalyzer.java:848`-`:858`），未见用例 |

**按 viewsheet 组件和选项的详细扫描（要不要做、做了什么）：**

先判断必要性。产品源码（子代理读 `VSMVAnalyzer`、`TransformationDescriptor`、`*VSAQuery`、`ViewsheetSandbox`，`file:line` 见其报告，我只核对了其中一部分）的结论是：**MV 的分析不按组件类型整体切换**，`VSMVAnalyzer.prepare()` 按"组件绑定的 worksheet 表"分组（`VSMVAnalyzer.java:640`-`:685`）；但组件类型和一批选项会改变这几件事：哪些列被当作选择列和关联列、一列是维度还是度量、能不能建只含选择列的瘦 MV（`shrinkTable`）、要不要额外建范围列和日期级别列、运行时的滑块/日历/选择元数据捷径。**所以有必要对具体 viewsheet 做扫描**，不然"组件类型覆盖"只是一个数字。动作方面：**画刷、缩放、钻取、书签恢复、刷新没有 MV 专用的判断**（没有 MV 开关）；有 MV 专用处理的是"明细数据"（`mv.detail.data`，默认关，明细请求不用 MV，`VSAQuery.java:1078`-`:1080`）、adhoc 筛选的失败回退（`MVTableMetaData.java:124`-`:187`）、导出时吞掉按需创建的确认（`ViewsheetSandbox.java:1260`-`:1266`）、按需重建。

扫描方法（脚本 `.claude/workflow-shared/datatest-gap/mv_vs_scan.py`，已提交）：复用 `vs_backup_scan.load` 读 `mvStorage.zip` 里 804 个 viewsheet 的 XML，读**用户设置的节点**（`viewsheetInfo` 的属性、`timeInfo`、选择组件的 `measureValue`、维度和聚合引用的 `formulaValue`/`percentageRValue`/`rankingOptRValue`/`dlevelRValue`、`allcalc` 的表达式），再与 spec 里字面量引用的 viewsheet 求交，并按 profile 分开；书签用 `vs_backup_scan.load_bookmarks` 解码。节点名字已对照真实 XML 核对。**只是静态存在与否，不是用例是否断言了这条规则**：被引用不等于验证过。

| 规则（触发它的节点） | zip 里的 viewsheet | 被 spec 引用 | 引用它的 profile | 判断 |
|---|---|---|---|---|
| `viewsheetInfo associationEnabled="false"`（不建关联 MV，`TableMetaDataTransformer.java:48`-`:54`） | **0**（804 个全是 true） | 0 | — | 没有用例 |
| `viewsheetInfo bypass="true"`（建 MV 时忽略 VPM，`ViewsheetSandbox.java:249`-`:250`） | **0** | 0 | — | 没有用例；`vpm` 的 14 个都是 bypass=false |
| `viewsheetInfo meta="true"`（设计态用元数据，`needMV()` 为 false，`ViewsheetSandbox.java:1477`-`:1480`） | **0** | 0 | — | 没有用例 |
| `viewsheetInfo full="true"`（关闭瘦 MV 的 `shrinkTable`，`VSMVAnalyzer.java:546`） | 103 | 98 | 只有 incrementalmv 98 | topmv、submv、others、vpm 里都是 full=false：瘦 MV 被缩列的路径有，`full=true` 只在增量 profile 里 |
| `viewsheetInfo mvOnDemand` | true 598、false 206 | 428 / 181 | 各 profile 都有 | 有 |
| 选择组件带度量（`measureValue` 非空，阻止瘦 MV，`VSMVAnalyzer.java:1204`-`:1220`） | 选择列表 10、选择树 5 | 8、5 | others 12、submv 1 | 有，几乎都在 others |
| 选择组件的数据源是别的组件（`sourceType` 非默认） | 时间滑块 5、日历 1 | 4、1 | others | 有（含义没有逐项核对） |
| adhoc 选择组件（`assemblyInfo adhoc="true"`，adhoc 筛选在失败时有专门回退，`MVTableMetaData.java:124`-`:187`） | viewsheet 本身 1 个（一个时间滑块）；**书签状态里 48 个节点、21 个 viewsheet** | viewsheet 0；书签所在的 21 个 viewsheet 全部被引用 | others（`AssociationMV/TestCase_AdhocFilter`、`CalcField/*` 等） | adhoc 筛选通过书签恢复被触发，有用例；没有核对这些用例是否断言命中或回退的消息 |
| 时间滑块：单值（日期 / 时间戳 / 时间 / 数值），复合 | 单值 78（按类型分：日期 11、时间戳 12、时间 2、数值 57，个别 viewsheet 同时有多种，所以相加大于 78），复合 14 | 单值引用 52，复合引用 12 | 数值和复合几乎都在 others；topmv、submv 各有几个日期和数值 | 有；`rangeType=16` 的日期 1 个没被引用 |
| 时间滑块对数刻度（`logScale="true"`，`RangeSelectionTransformer.java:146`-`:150`） | 1 | 1 | others | 只有 1 个 |
| 日历：`daySelection`、`type`/`mode` 组合 | daySelection=true 3、false 21；type/mode 只有 2 种组合（type=1 的 mode=1、mode=2） | 2、20 | others 为主，topmv 5、submv 1、vpm 1 | 有 |
| 图表 / 交叉表的日期级别（`dlevelRValue`，对应 `DateMVColumn` 物化的级别） | 图表：级别 5 有 179 个，其他 15 种取值各 2 至 9 个（图表合计 203 个 viewsheet 有日期级别，引用 115 个）；交叉表级别 5 有 54 个（合计 56 个，引用 42 个） | 图表级别 5 引用 109，其余级别引用 1 至 7 个 | 级别 5 在 others、submv、topmv、incrementalmv 都有；其他级别**几乎全在 others** | 有，但 topmv、submv 里只有级别 5、3、8 |
| 图表的百分比选项（`percentageRValue`，根表有聚合加百分比时丢掉物理 MV，`ChartVSAQuery.java:1856`-`:1867`） | **0**（图表 365 处引用全是 0）；交叉表 1 个 viewsheet | 交叉表 1 | others | 图表百分比没有用例；交叉表 1 个 |
| 图表 / 交叉表的排名（`rankingOptRValue` 为 9 或 10） | 图表 43、交叉表 3 | 23、3 | others 为主，topmv 1 | 有 |
| 不可合并的聚合公式 | DistinctCount 20、Median 12、Mode 6、NthLargest 4、NthMostFrequent 3、NthSmallest 2、PthPercentile 3；**Product、Concat 为 0** | 10、8、4、3、2、1、2 | 几乎全在 others，topmv 1、incrementalmv 1 | topmv 和 submv 里几乎没有不可合并公式 |
| 计算字段含 `@nomv` 标记（`MVTool.java:607`-`:614`，该表达式不可用 MV） | **0** | 0 | — | 没有用例 |
| 计算字段含 `parameter.`（同上） | 1 | 1 | submv | 只有 1 个 |
| 计算字段引用聚合（`field['Sum(...)']`，建 MV 时拒绝，`VSMVAnalyzer.java:865`-`:904`） | 23 | 21 | others 19、topmv 2 | 有 |
| 列表输入组件绑定表（组合框 / 单选 / 复选，绑定表被当作动态表，`VSMVAnalyzer.java:666`-`:669`、`:816`-`:819`） | 17 | 12 | others | 有；是否断言"不命中"没有核对 |
| 输出组件的标量绑定（文本、仪表，`MVDef.java:1163`-`:1181`） | 29 | 19 | others | 有 |
| 嵌入表组件（`EmbeddedTable`，拒绝，`VSMVAnalyzer.java:712`-`:718`） | 1 | **0** | — | 没被引用（`NOT Transform` 里另有嵌入表的 viewsheet，也没被引用） |
| Cube 绑定（拒绝，`VSMVAnalyzer.java:701`-`:708`） | 1（`NOT Transform/cube`，一个图表绑定 `___inetsoft_cube_db2_1/l1`） | **0** | — | 没被引用（`NOT Transform` 全部 11 个都没被引用） |
| 自由表（`CalcTable`，没有 MV 专用代码） | 66 | 26 | others 25、topmv 1 | 产品侧没有专用逻辑，不单独追 |
| 其他：`CalcMV/Detail_1`…`Detail_19`、`CalcMV/crosstab_*`（明细型 / 交叉表型计算字段的 MV，`VSMVAnalyzer.java:1104`-`:1118` 的 `appendDetailCalc`） | 38 | **0** | — | 整个 `CalcMV` 目录 38 个 viewsheet 没有 spec 引用，也没有基线（`CalcField_Spec` 用的是 `CalcField/*`） |

动作（静态 viewsheet 里看不到动作，从书签状态看）：书签状态里有选择（1492 个选择列表状态，分布在 342 个 viewsheet、其中 337 个被引用）、画刷 `state_brush`（32 个书签，17 个 viewsheet）、缩放 `state_zoom`（31 个，16 个）、钻取筛选 `drill_filter`（16 个，4 个）、adhoc 筛选（48 个节点，21 个 viewsheet）；`VSActions_Spec` 的用例名里有 `drill down filter1`-`3`、`brush filter`、`astimeseries-*`。画刷、缩放、钻取在产品里没有 MV 专用判断，所以这些用例主要验证"结果与不用 MV 一致"，风险较低；**没有找到被任何用例触发的 MV 专用动作**：明细数据（`DataMap.DETAIL`，`mv.detail.data` 默认关；名字含 detail 的 19 个 viewsheet 都是 `CalcMV/Detail_*`（明细型计算字段），不是明细数据动作，`MVTest` 只导出组件数据）、导出路径（导出时吞掉按需创建的确认）。这些都是静态结论，没有逐个读书签和用例。

**生成型模块补充（步骤 3b-B/C/E）：** mv 是"由 A 生成 B"的模块（由 viewsheet 生成 MV，再由查询决定命中），步骤 3b-C（分支清单）做了；3b-B（运行时探测）只用本轮对 5 个 profile 的实际运行代替，没有写探针；3b-E（输出形态矩阵）没做。

关键决策点的分支清单（产品源码，`file:line` 来自代理读取，带 `~` 的行号可能偏差 1 至 3 行；"用例"一列只做名字级的静态判断，没有逐个读用例内容）：

| 方法 | 分支（含义） | 有无用例走到 |
|---|---|---|
| `SelectionUpTransformer.java:181`-`:220` | 表有 maxRows、排名、聚合排名、交叉表、旋转、反转，各返回一个 fault，选择条件不上移 | maxRows（zip 里 7 个名字含 maxrow，被 spec 引用 6 个）、旋转（zip 里 6 个，被 spec 引用 2 个）有，反转 0，其他未验证 |
| `SelectionUpTransformer.java:1447`-`:1470` | 嵌套选择：除非 `mv.ignore.nestedSelection`，返回 fault `nestedSelection` | 名字里含 nested 的 3 个；开关未设置 |
| `SelectionUpTransformer.java:1694`-`:1727` | 父表是 join：外连接、merge join 返回 fault，左右连接单侧条件可由 `mv.outer.moveUp` 豁免 | 外连接 11 个、merge join 2 个（`submv/mergejoin`、`mergejoin2`，名字级；worksheet 里 `MergeJoinTableAssembly` 共 4 个表）；`mv.outer.moveUp` 无用例 |
| `SelectionUpTransformer.java:1729`-`:1778`、`:1880`-`:1955` | 父表是 union / intersect / minus：union 需 distinct、列数相同、`mv.union.moveUp`、选择定义一致 | 名字里含 union、intersect、minus 的共 16 个（名字级）；`mv.union.moveUp` 无用例 |
| `VSMVAnalyzer.java:690`-`:750` | 创建时拒绝 cube、嵌入表、查询变量、命名分组变量、命名分组动态条件 | **无**（`NOT Transform` 的资产未被引用） |
| `VSMVAnalyzer.java:815`-`:907` | 不适合建 MV：输入动态表、后置聚合计算、maxRows 与子选择、更新条件与旋转/反转/交叉表 | 未验证 |
| `MVTransformer.java:194`-`:252` | 多个 DistinctCount、其他不可合并公式、`NamedRangeRef` 分组：不能合并 | DistinctCount、Median、Mode 等有用例；多个 DistinctCount 并存未验证 |
| `AssetQuery.java:143`-`:228` | 转换失败时丢弃 MV 退回实时查询，`mv.required` 时抛错 | 只能靠"命中断言为空"发现，全部用例里 2 个 |
| `MVManager.java:165`-`:236` | 失败的 MV 视为不存在；按需创建（`mv.ondemand`、`MVRule`）；无 MATERIALIZATION ACCESS 返回 null | 按需创建、权限：无用例 |
| `MVManager.java:677`-`:730`、`:865`-`:908` | 先用户后组后角色；VPM 计划不相等不可用 | `vpm`、`multitenant`（后者几乎没被引用） |
| `LocalMVCreator.java:70`-`:95` | 全量 / 增量选择：关联 MV、无已有文件、无 MV 条件且非强制追加走全量 | 增量追加、强制追加有用例 |
| `MVQueryBuilder.java:153`-`:239` | 明细 / 聚合查询；MV 列缺失抛 "Column not found in mv"（触发重建） | 未验证 |
| `MVAssetQuery.java:683`-`:722` | 维度溢出（`mv.dim.max.size`）只给警告"结果不完整" | 无用例 |

产品侧决定"能不能命中"的另一半在 `TransformationDescriptor` 和 `trans` 包的其他类（`CalcFieldTransformer` 在 `MVTransformer.transform` 里被注释掉，`:491`-`:500`；`TransformationDescriptor.isWsMVNotAllowed` 恒为 false 且注释为 "currently disabled"，与 `ws.mv.enabled` 并存，`:589`-`:600`，WS MV 的语义需要开发确认）。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 78 个用例只断言"MV 已定义"（`getMVDefInfo`）：其中 32 个在 `AssociationMV_Spec`、24 个在 `CalcField_Spec`，其余 22 个分散在 `Transform`、`MVHit_RankingAndFilters`、`VSCondtion` 等；这 78 个全部含 `ASSOCIATION_` 前缀的断言，正是 PR #188 描述里"78 assertions unchanged"（含 `ASSOCIATION_` 的断言）的那批（另有 `NotPublic_Text` 带 `TODO`）。`getMVHitInfo` 在组件都是纯值时退回"已定义"（`MVTest.groovy:455`-`:461`，PR #188 描述里也写了） | 关联 MV 没有被命中、查询悄悄退回实时库，结果相同，用例照样通过；MV 被丢弃的分支（`AssetQuery.java:143`-`:228`）不会被发现 | **已知、有意保留**：PR #188 的描述和提交说明只写了"78 处没换"，没写原因。本地探针（`AssociationMV_Spec.TestCase_Selectionlist1`，临时加一行打印，已还原）显示：命中记录里只有 `MVSCRIPT1|TopMV`、`Query1|TopMV` 等 6 个基表 MV，**没有任何 `ASSOCIATION_*`**，而定义里有 12 个（6 个基表 MV + 6 个关联 MV）；也就是命中记录目前观察不到关联 MV，换成命中断言这 78 处会全部失败。这是从探针推断的原因，不是 PR 的说明。可以做的：这些用例保留关联 MV 的定义检查，另外加一条对非 `ASSOCIATION_` 名字的命中检查（探针里基表 MV 的命中是记录到的）；关联 MV 本身的命中要改机制，未确认可行 | 中 |
| G2 | 反向路径（不该走 MV、MV 不该被建）几乎没有用例：全部用例里只有 2 个断言命中为空（`VSCondtion_Spec`、`SubMV_Others_Spec`）；zip 里有 11 个 `NOT Transform` viewsheet（crosstab ws、cube、embedded 两个、subquery 两个、ranking 两个、group variable、field condition、adhocfilter_measure）没有任何 spec 引用，也没有基线 | 分析器改动后错误地对 cube、嵌入表、带查询变量、命名分组变量的表建了 MV 或命中了 MV；或本该拒绝却建出错误的 MV。查询出错时因为不命中，结果往往仍然对，只有"是否命中"这一项能暴露 | dashboard（用现成的 11 个资产：断言不命中且结果与不用 MV 一致）+ 单测（`VSMVAnalyzer.isAcceptable`） | 高 |
| G3 | 产品侧的转换层几乎没有单测：`trans` 包 11 个类中只有 `SelectionUpTransformer`（2048 行，3 个测试）；`MVTransformer`（673 行）、`WSMVTransformer`、`AbstractTransformer`（1006 行）、`CalcFieldTransformer`、`NamedGroupTransformer`、`WSMVSelectionDownTransformer`、`WSMVAggregateDownTransformer` 等没有测试；`VSMVAnalyzer`（1689 行）只有命名分组条件 11 个测试 | 选择条件上移、聚合下移、分析器接受/拒绝规则出错，只有碰巧被某个端到端用例触发才会发现，而且发现时已经是结果差异，定位很慢（`SelectionUpTransformer` 二十多个 fault 条件见 §2） | 单测（产品仓库），按 fault 条件一个一个固定；入口需要搭 Viewsheet/Worksheet 夹具，难度高，需先确认夹具办法；13.8 的 `epic-test-coverage` 分支里这些类也没有测试，不能移植（见 §4 末尾） | 高 |
| G4 | `mv.*` 配置项几乎全是空白：产品读的二十多个属性里只有 `ws.mv.enabled` 被设置；`mv.preferred.block`、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`、`mv.dim.max.size`、`mv.detail.data`、`date.common.options`、`time.common.options` 等从未改值 | 改了默认值才出现的错误：例如 #79869（`mv.preferred.block` 非默认时追加的行丢失）在 datatest 里没有对应用例；`mv.union.moveUp` 关闭时的 union 选择上移；`date.common.options` 改变物化的日期级别后，没物化的级别查询是否仍对 | dashboard（用 `@SreeProperty` 设置属性后跑代表性用例），每个属性一个场景；`mv.dim.max.size` 的溢出警告用单测（13.8 的 `MvStorageEngineFeatureTest.DictionaryPart` 有 `XDimDictionary` 溢出的用例可移植）；`mv.ondemand`、`mv.required`、`mv.metadata`、`mv.dispatcher.*` 13.8 的 `ConfigMvBehaviorS2Test` 有，可移植 | 中 |
| G5 | 增量更新：Append、Delete、Transform 共 156 个用例是 golden + 命中，**不做开/关对比，也不做与全量重建的比较**（增量的 MV 不等于数据库，所以不做开/关对比；重建对比只在 `IncrementalDataChange_Spec` 里用，PR #194、#195）；`IncrementalDataChange_Spec` 本轮 16/16 通过。仍没有用例的：#80313（第二次无新行更新后 viewer 打不开，New，等修复后补用例）；#80344（`MV.<列>.Max` 追加条件后偶发 `Index 0 out of bounds`，New）只有 `$(MV.customer_id.Max)` 一个用例部分覆盖，据维护者此前记录有过部分运行失败，本轮该 spec 没有失败 | 增量路径独有的错误：#80313、#80344 复发或未修好时，大部分增量用例比的是 MV 自己的输出，不会暴露；156 个 golden 用例里只要有一个与全量重建不同，没有测试会发现 | dashboard：给 Append/Delete 的代表性用例加 `compareWithRebuild`（适用范围：取新行的追加条件和不引用 `MV.` 的删除条件；引用 `MV.` 的删除条件在建 MV 时被忽略，全量重建会保留旧行）；#80313、#80344 等修复后补 | 中 |
| G6 | **`wsmv` profile 不在 `test-mv.yml` 里**，`WSMV_Path_Spec` 在 CI 里不运行（静态：只看了 workflow 文件）。WS MV 只有这 1 个用例（PR #200 有意只走一条通路；"是否使用 MV"的判断由产品仓库的 `AssetQueryWorksheetMVTest` 的 25 个单测覆盖）；产品侧 `WSMVTransformer`、`WSMVAnalyzer`、两个 `WSMV*DownTransformer` 没有单测；`isWsMVNotAllowed` 恒为 false 的注释与 `ws.mv.enabled` 并存 | WS MV 的创建、读取、增量在 CI 里没有任何保护；WS MV 的选择下移、聚合下移、条件被清除的行为出错只有单测能发现，而单测没有 | 机制：把 `wsmv` 加进 `test-mv.yml`（该 spec 会改动 `mvorder1.mv.db`，CI 里没关系）；单测补 `WSMVAnalyzer`、两个 `WSMV*DownTransformer`；WS MV 的语义（`isWsMVNotAllowed`）先问开发 | 高 |
| G7 | 不可合并聚合公式：Product、Concat 在 viewsheet 里 0 个；其他不可合并公式各 1 至 23 个，且只验证"结果和基线一致"，不验证退回"读明细再在内存聚合"这条路径（`AssetQuery.java:175`-`:203`） | 不可合并公式被当成可合并公式在 MV 上直接合并（例如 Product 被当作 Sum 合并），结果错但基线是 MV 输出，不会发现 | 单测（`MVTransformer.isCombinable` 对每个公式的判断，期望值可由 `AggregateFormula.isCombinable` 的规则独立得出）；dashboard 补 Product、Concat 各一个 | 中 |
| G8 | 本机 12 个用例失败且稳定复现：vpm 4 个、others 5 个、incrementalmv 3 个。多数是已知的"基线还没跟上修复"：`C_V_5`、`expression`（#79870）、`running total`（#79508）、vpm 的时间列；incrementalmv 3 个行数差 1，该 profile 的数据依赖当前日期（维护者说明）。`selection association4`、`assembly_query` 的 NULL 与 0 我没有找到记录，未核对。CI 上是否也失败未确认 | 失败常态化以后，真实的 MV 回归会被淹没；基线是 MV 的输出，修复后不更新，用例就在固定旧行为 | 机制/基线：在 CI 环境确认；按各 bug 的状态逐个更新（核对新输出对不对再更新，不直接复制）；时间列让它不依赖服务器时区；与日期相关的用例把日期条件改成相对日期 | 中 |
| G9 | 被忽略的用例：`TabularSource_Spec` 整类（5 个，据维护者说明：继续 `@Ignore`，由维护者自己重做用例）；`chart-time type`（#80458）、`top-aggregate-maxrow`（#80446）这两个对应的 bug 现在是 Resolved，修复后应去掉 `@Ignore`，并补选择列表的 golden；`composite3`（#80445）、`variable in subquery`（#80457）仍是 New；`date range and number range` 按设计不可能通过（`TopMV_Others_Spec.groovy:401`-`:407`） | Tabular 数据源的 MV 创建与命中没有回归保护（#78339 已修）；#80458、#80446 已修，但用例仍被忽略，修复没有被固定 | 用例：#80458、#80446 现在可以去掉 `@Ignore`（`MVHit_RankingAndFilters_Spec` 在 surefire 里跳过 2 个，其中 1 个是 `chart-time type`，另 1 个没查）；先在本机运行并核对新输出 | 低 |
| G10 | CI 的触发：`test-mv.yml` 只能手动触发，`Jenkinsfile` 没有 mv；这是维护者的决定（所有 datatest workflow 的 `schedule` 都不恢复），本文不建议恢复。另一个可以评估的点：5 个 profile 各启动一次 JVM，是否合并或并行（本机整套约 20 分钟，topmv 2 分 43 秒、incrementalmv 2 分 58 秒、others 7 分 31 秒） | 回归在下一次有人手动触发前一直不被发现；本轮在本机就发现 12 个失败 | 不改；只在 §6 记一个问题：手动触发的节奏是否够 | 低 |
| G11 | 195 个 viewsheet 没有被 spec 按字面量引用（`hit mv` 110、`CalcMV` 38、`NOT Transform` 11 等）；16+3 个基线属于 `@Ignore` 用例；1 个 viewsheet 有基线但没被引用 | 资产里已经准备好的场景没有被运行（可能是遗留，也可能是引用方式静态看不到）；陈旧基线让人误以为被验证过 | 先核对是真孤儿还是间接引用，再决定删除或补用例（G2 的 11 个先用上） | 低 |
| G12 | 按需创建、MV 过期、数据周期、集群：datatest 没有用例，这是有意的——cycle 的风险在真实环境，放在 `test/cluster`（CY-02/03/04，enterprise PR #1604、#1701，本轮没有运行）；`MVRule`、`MVManager` 已有单测（22 个、35 个，mock 为主）。**datatest 里没有、单测里也没有**的是 `mv.ondemand` 按需创建、`isStale`、`isExpired`、无 MATERIALIZATION ACCESS 返回 null 的分支 | 按需创建出错、MV 过期判断出错、权限缺失时的行为 | 单测（产品仓库，补上面几个分支；`mv.ondemand` 的行为 13.8 的 `ConfigMvBehaviorS2Test.mvOndemand` 已有，可移植）；cycle 与集群不属于静态 datatest | 低 |
| G13 | 按 viewsheet 组件和选项的详细扫描（§2）发现的零覆盖项：`viewsheetInfo` 的 `associationEnabled=false`、`bypass=true`、`meta=true` 三个选项 0 个 viewsheet；`full=true` 只在 incrementalmv；图表百分比 0；计算字段 `@nomv` 0；Product、Concat 聚合 0；嵌入表组件 1 个但没被引用；整个 `CalcMV` 目录 38 个 viewsheet 没被引用 | `associationEnabled=false` 时仍然建了关联 MV；`bypass` 的 MV 带了 VPM 过滤或相反（#78390、#77666 同类）；设计态 `meta` 用了 MV；`full=true` 时瘦 MV 仍被缩列，选择列表缺值；图表百分比时仍用物理 MV 导致结果错；带 `@nomv` 的计算字段被建进 MV；明细型计算字段的 MV 出错（`appendDetailCalc`） | dashboard：每个选项一个用例（改现有 viewsheet 的 `viewsheetInfo` 即可，不需要新建数据）；`CalcMV` 的 38 个 viewsheet 先核对是遗留还是可直接用，再补 spec | 中 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测；问题出在组件、绑定、数据源之间的接线 → dashboard；验证机制本身的缺陷 → 改基类，不是加用例。

## 4. Java 单测补充

产品仓库已有的 MV 单测：`core/src/test/java/inetsoft/mv` 下 87 个文件，另有 10 个在其他包（均在 `core`，其他模块没有）；唯一的 `@Disabled` 是 `RankingConditionTest` 整个类（"Ignite causing problems"，没有 bug 编号）。较强的是：聚合公式（`inetsoft.mv.formula` 15 个类里 14 个有测试，接口 `MergeableFormula` 除外）、`data` 包（`BitDimIndex`、`SubMV`、`XDimDictionary`、`MVQuery`）、`MVDef`、`MVManager`、`MVBuilder`。测试用 Mockito 的很多，没有一个显式搭嵌入数据库。同名类按完整路径区分：`inetsoft.report.filter` 下的 `DistinctCount`、`Median`、`Mode`、`NthMostFrequent` 的测试不算在 `inetsoft.mv.formula` 头上；运行时 `GroupedTableBlock.getMVFormula` 把 `DistinctCount`、`Median`、`Mode` 路由到 `inetsoft.mv.formula`，`NthMostFrequent` 路由到 `inetsoft.report.filter`（`GroupedTableBlock.java:123`-`:136`）。

没有找到直接测试的重要类（"没找到"指没有以类名命名的测试文件，且 `inetsoft/mv` 下的测试没有 `grep -w` 到类名）：

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `trans.SelectionUpTransformer`（已有 3 个） | 每个 fault 条件一个用例：maxRows、排名、聚合排名、交叉表、旋转、反转、后置条件、隐藏选择、嵌套选择、outer/merge join、union/intersect/minus（含 `mv.union.moveUp`、`mv.outer.moveUp` 开关）。期望值是"该条件下返回 false 且带哪个 fault"，可以不依赖产品输出写出 | 约 25 | 高（需要搭 Viewsheet/Worksheet 夹具，入口未确认） | 是：`mv.outer.moveUp` 在左/右连接下"单侧条件可豁免"的准确规则 |
| `VSMVAnalyzer.isAcceptable` 与 `isSuitableTable`（已有 11 个，只覆盖命名分组条件） | cube、嵌入表、查询变量、命名分组变量、命名分组动态条件、输入动态表、后置聚合计算、maxRows 与子选择、更新条件与旋转/反转/交叉表各一个 | 约 12 | 高 | 否 |
| `MVTransformer.isCombinable`、`AggregateFormula.isCombinable` | 9 个不可合并公式 + 可合并公式的逐个判断；多个 DistinctCount、单个 DistinctCount 与 `breakcol`；`NamedRangeRef` 分组 | 约 18 | 中 | 否（规则在 `AggregateFormula.java`） |
| `MVDef.canHitMV`、`isValidMV` | 分组/聚合列缺失、多个 count-distinct 与 `breakcol`、WS MV 与桌面模式恒为 true | 约 8 | 中 | 否 |
| `trans.WSMVSelectionDownTransformer`、`WSMVAggregateDownTransformer`、`WSMVTransformer` | 选择下移：旋转/反转、外连接、非简单 join、子查询值、DataRef 值、交叉表子表、子表聚合条件，各返回 false；聚合下移：父表交叉表、百分比、命名范围、命名分组 | 约 14 | 高 | 是：WS MV 的语义（`isWsMVNotAllowed` 恒为 false 的注释） |
| `MVTableMetaData`、`SharedMVUtil` | 关联 MV 的元数据、共享 MV 的判断 | `MVTableMetaData`：移植 13.8 的 `MvManageCustomerP2Test`（2 个，先要有它依赖的夹具，见下）；`SharedMVUtil`：未设计 | 中 / 高 | 否 |
| `data.XDimDictionary`、溢出路径（`mv.dim.max.size`） | 溢出时给警告且结果标"不完整"（`MVAssetQuery.java:683`-`:722`）；先移植 13.8 `MvStorageEngineFeatureTest.DictionaryPart` 里的溢出用例（`distinctValuesAtMaxSizeTurnOnOverflow`、`overflowKeepsRawValuesAndIndexIsRow`），再看是否还缺 | 移植 2 个，另约 1 个 | 中 | 否 |
| `util.MVRule`（已有 22 个）、`MVManager`（已有 35 个，mock 为主） | 补 `isStale`、`isExpired`、按需创建条件、无 MATERIALIZATION ACCESS 返回 null；`mv.ondemand`、`mv.metadata`、`mv.required`、`mv.dispatcher.*` 移植 13.8 `ConfigMvBehaviorS2Test`（6 个） | 约 8，另移植 6 个 | 中 | 否 |

13.8 里已有、StyleBI 没有测试的类，建议整体移植，不另设计（详见下面"13.8 里已有的 MV 单测"一节）：

| 被测类 | 13.8 的测试 | 个数 | 优先级 |
|---|---|---|---|
| `TransformationFault`（`trans` 包，G3 里 fault 的载体） | **13.8 里也没有测试**（该类名在 13.8 的测试里 0 次出现）；`MvStorageEngineFeatureTest` 里名字带 `FaultDetected` 的 4 个用例测的是 `mv.fs` 的 `XFileFaults`、`OverflowFault` 等，**StyleBI 没有这些类**，不能移植 | 0 | 要新写（G3） |
| `MVQueryBuilder` | `MvStorageEngineFeatureTest` 的 `aggregateTableToQuery`、`detailTableToQuery`、`missingColumnRejected`（读了方法体：`new MVQueryBuilder(table, mv, new VariableTable(), null)`） | 3 | 中 |
| `SubMVQuery` | `subMvConditionsOnDictionaryAndMeasures`、`subMvConditionSets`、`subMvConditionErrors`（方法体里 `new SubMVQuery(groups, order, subs, null)`） | 3 | 中 |
| `mv.fs`（`XFile`、`CacheBlockFile`、`StorageBlockFile`、块系统）、`mv.comm`（`XReadBuffer`、`XWriteBuffer`）、`mv.mr`（`AbstractJob`、`MVReduceTask`、`SubMVTask`） | `MvStorageEngineFeatureTest.FsPart` 里不依赖 `XFileFaults` 的用例、`MvMapreduceFeatureTest` 里不依赖 `DUtil`、`DefaultConnectionPool`、`DirectBlockFile`、`XJobDispatcher` 的用例（这四个类 StyleBI 没有） | 没有逐个数 | 低（稳定的基础设施，bug 记录主要是并发和集群） |

另有 `DateMVColumn`（493 行）、`RangeMVColumn`（414 行）、`MVIntColumn` 以外的列类型（`MVDecimalColumn`、`MVUIntColumn`、`MVRangeIntColumn`、`AbstractMeasureColumn`）、`DimValueList`、`MVRow` 没有直接测试，未列入设计（列类型的边界已被 #79649、#76971、#77154 等修复时的测试局部覆盖，未核对完整性）。

### 13.8 里已有的 MV 单测（可移植的和不可移植的）

来源：本机 `E:\style-intelligence`（`inetsoft-technology/style-intelligence` 的 13.8 代码），分支 `epic-test-coverage`，`6b0ba0e7f2`（2026-11-13），`java/src/test/java` 下 2420 个文件，按文件名、类名和 import 搜索（`E:\inetsoft\sr13_8` 不是 git 仓库、没有 MV 测试，不是这条线）。**主要读了测试方法名、类声明和 import，只读了个别用例的方法体，没有编译或运行。移植的两个障碍：**13.8 有、StyleBI `main` 没有的类（`XFileFaults`/`OverflowFault`、`mv.adm.DUtil`、`mv.comm.internal.DefaultConnectionPool`、`mv.mr.internal.XJobDispatcher`、`mv.fs.internal.DirectBlockFile`、`inetsoft.report.ReportEnv`）；13.8 的测试夹具（`DashboardHarness`、`OrdersDashboard`、`H2Sources`、`AdminRealServiceFixture`、`ReportHarness`、`SmtpSink`、`ParsedSql`）在 StyleBI 的 `core`、`enterprise` 里没有找到。 该分支的 `inetsoft/mv` 包下只有一个单测（`MVTimestampIntColumnTest`，StyleBI 已有同名测试），其余 MV 测试是特性和客户场景测试：

| 13.8 的测试 | 内容 | StyleBI 现状 | 建议 |
|---|---|---|---|
| `feature/mv/MvStorageEngineFeatureTest`（53 个，内部类 `DictionaryPart` 23、`DataPart` 4、`Built` 9、`FsPart` 14，另有 3 个） | `XDimDictionary` 的空值槽、溢出（`distinctValuesAtMaxSizeTurnOnOverflow`、`overflowKeepsRawValuesAndIndexIsRow`）、整数范围、合并、克隆、换出与读回；`MVQueryBuilder`（3 个）；`SubMVQuery` 条件（3 个）；`mv.fs` 的存储保存/重命名/删除、`XFile`、`nblock` 二进制格式、`CacheBlockFile`；另有 4 个 `XFileFaults`/`OverflowFault` 的用例 | `MVQueryBuilder`、`SubMVQuery`、`XFile`、`CacheBlockFile`、`StorageBlockFile` 都没有测试；`XDimDictionary` 有 3 个文件 23 个（溢出读、`getPatternVals`、换出写失败），与 13.8 的部分重叠 | **可移植**：`DictionaryPart`、`DataPart`、`Built`（用 `ReportEnv` 的地方要换成 `SreeEnv`）；`FsPart` 里 4 个 fault 用例**不能移植**（StyleBI 没有这些类）；其余 `FsPart` 要逐个看依赖；`SubMVQuery`、`MVQueryBuilder` 的 13.8 用例期望值要先核对是否独立于产品输出 |
| `feature/mv/MvMapreduceFeatureTest`（29 个） | 块系统的添加/追加/重命名/删除（8 个，经 `FSService.getDataNode().getBSystem()`，StyleBI 的 `XBlockSystem` 实现是 `DefaultBlockSystem`，13.8 这里返回哪个实现没核对）；任务派发、超时、取消、启动失败、重试（`AbstractJob`、`XJobPool`）；`MVReduceTask`（2）、`SubMVTask`（1）；`mv.comm` 缓冲区往返和超时（3）；数据节点管理（2）；`mv.preferred.block` 设为 1000 | `DefaultBlockSystem`、`AbstractJob`、`AbstractMapTask`、`MVReduceTask`、`SubMVTask`、`mv.comm` 没有直接测试；`XJobPool`、`XMapTaskPool`、`XJobStatus` 有 13 个测试，部分重叠 | **部分可移植**：该文件 import 了 StyleBI 没有的 `DUtil`、`DefaultConnectionPool`、`DirectBlockFile`、`XJobDispatcher`，用到它们的用例（派发、数据节点管理、连接池、直接块文件）不能直接移植；优先级低（稳定的基础设施，bug 记录里主要是并发和集群，见 G12） |
| `feature/config/ConfigMvBehaviorS2Test`（6 个） | `mv.metadata`（含后台创建时抛 `ConfirmException`）、`mv.ondemand`、`mv.dispatcher.*`、`mv.required`、`mv.enabled.all` 的行为 | datatest 和单测都没有设置这几个属性的用例 | **要先移植夹具才能移植**：它依赖 `DashboardHarness`、`OrdersDashboard`、`H2Sources`、`AdminRealServiceFixture`，StyleBI 里都没有；对应 G4 里的 `mv.ondemand`、`mv.required`、`mv.metadata`、`mv.dispatcher.*` |
| `customer/mv/MvManageCustomerP2Test`（2 个） | 关联 MV：#53263（带计算列的 mirror 表）、#62332（子视图使用关联视图）；`VSMVAnalyzer.isAssociationCompatible`、`MVTableMetaData` | `MVTableMetaData` 没有测试；关联 MV 在 datatest 里只有"已定义"断言（G1） | **要先移植夹具**（`OrdersDashboard`、`H2Sources`，StyleBI 里没有）；移植后同时是 G1 里关联 MV 命中的一个现成入口 |
| `feature/query/QueryAdvancedFeatureTest.mvUpdateVariablesInSql` | `MVConditionListHandler` 在 MV 更新里处理变量，设 `mv.preferred.block` 为 2 | `MVConditionListHandler` 有 15 个测试，没有"更新里的变量"；是否覆盖 #79869（最后一个块已满时追加的行丢失）**没有核对** | 先读方法体再定 |
| `feature/scheduler/ActionsMvMVTest`（10 个） | `MVAction` 的创建、序列化、完成邮件、取消、超时、关联 MV 排序、`SharedMVUtil.shareCreatedMV` | `MVActionTest`、`ClusterJobStoreMVActionTest` 共 10 个，`SharedMVUtil` 没有测试 | 只补 `SharedMVUtil` 部分 |
| `customer/mv/` 的 `MvCreateCustomerTest`（4）、`MvCreateCustomerP2Test`（6）、`MvReadCustomerTest`、`MvUpdateCustomerTest`、`MvScheduleCustomerTest`、`DataIsolationMvBehaviorS2Test`、`VpmHideColumnsMvFeatureTest`（各 1） | 客户场景：创建、读取、更新、调度、数据隔离、VPM 隐藏列，经 `MvLocalFactory` | StyleBI 没有等价的 core 内端到端测试；这些场景由 datatest 的 dashboard 用例覆盖 | 不移植；对照 G6、G12 |
| `feature/mv/MvGridFeatureTest`（3 个） | 集群状态检查 | 无 | 不属于 datatest 和 StyleBI core |

**13.8 里没有的**（按完整单词在 2420 个测试文件里搜索，0 个命中）：`TransformationFault`、`SelectionUpTransformer`、`WSMVAnalyzer`、`WSMVTransformer`、两个 `WSMV*DownTransformer`、`AggregateDownTransformer`、`SelectionDownTransformer`、`NamedGroupTransformer`、`CalcFieldTransformer`、`canHitMV`、`isValidMV`、`MVRule`，以及属性 `mv.outer.moveUp`、`mv.union.moveUp`、`mv.ignore.nestedSelection`、`mv.dim.max.size`、`mv.detail.data`、`date.common.options`、`mv.table.metadata`；`MVTransformer`、`VSMVAnalyzer`、`isCombinable` 只出现在 `MvManageCustomerP2Test` 和升级测试里（不是对它们的判断逻辑的测试）。**所以 G3 的主体（转换层和分析器）两条线上都是空白，不能移植，要新写。** 13.8 的 `mv.formula` 下的公式类没有单独的单测（只在 `MvStorageEngineFeatureTest.DataPart` 里有 4 个合并用例），StyleBI 反而更全（14 个类）。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 关联 MV 用例补基表 MV 的命中检查（G1） | 分两步。**第一步（用例，不改基类）**：对这 78 个用例，保留原来的 `getMVDefInfo().containsAll([...])`，从同一个列表里取出不带 `ASSOCIATION_` 前缀的名字，在 `verifyAll` 里加一条 `getMVHitInfo().containsAll([这些名字])`；用脚本按列表机械生成，期望值就是原列表里已有的名字，不取自运行输出；`CalcField_Spec.NotPublic_Text`（带 `TODO`）不动；列表里全是 `ASSOCIATION_` 名字的用例没有可加的，单独列出。改完后跑涉及的 spec，**加了之后失败的用例不删这条检查，单独列出来**（说明那个基表 MV 定义了但没被命中，要么是用例本身，要么是命中记录观察不到）。**第二步（机制，未确认可行）**：让关联 MV 自己的命中也能被记录，再把 `ASSOCIATION_` 的定义检查换成命中检查 | 第一步：基表 MV 真的被命中，而不是退回实时库；第二步：关联 MV 真的被命中 | 第一步依据本地探针：`TestCase_Selectionlist1` 的命中记录里有 6 个基表 MV、没有 `ASSOCIATION_*`（PR #188 没写不换的原因，这是推断）；只验证了这一个用例，其余 77 个要改完才知道；第二步先确认 `recordMVLookupHit` 的 `findOnly` 查找能否覆盖关联 MV，不能就只做第一步 |
| 反向路径（G2） | 用 `NOT Transform` 的 11 个现成 viewsheet，新建一个 spec；每个用例：创建 MV（预期失败或不创建）、执行、断言 `getMVHitInfo()` 为空、结果与基线一致 | 不该走 MV 的表不走 MV，结果仍正确 | 先逐个确认这 11 个 viewsheet 各自触发哪条规则（cube、embedded、subquery 等） |
| `mv.*` 配置项（G4） | 用 `@SreeProperty` 设置属性，复用现有 viewsheet：`mv.preferred.block`（Append 的一个用例）、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`（各取一个 union、外连接、嵌套选择的现有用例）、`date.common.options` | 改值后命中结果与不用 MV 一致 | 属性在 `MVTest.clearEnv` 里复位（目前只复位 `mv.outer.moveUp`），要补 |
| 增量一致性（G5） | 给 Append、Delete 的代表性用例加 `compareWithRebuild`（`MVTest.groovy:652`），适用范围：取新行的追加条件和不引用 `MV.` 的删除条件；#80313、#80344 等修复后补用例 | 增量更新后的结果与全量重建一致 | `IncrementalDataChange_Spec` 里已有写法可参考；该 spec 本轮 16/16 通过 |
| WS MV 进 CI（G6） | 把 `wsmv` profile 加进 `.github/workflows/test-mv.yml`（照 `vpm` 一步的写法） | `WSMV_Path_Spec` 在 CI 里运行 | 该 spec 会改动 `docker/src/test/resources/H2Data/mvorder1.mv.db`（不影响 CI）；本机运行已通过 |
| 不可合并公式（G7） | 为 Product、Concat 各加一个 viewsheet | 不可合并公式的结果与不用 MV 一致 | 无 |
| 重新启用被忽略的用例（G9） | #80458 的 `chart-time type`、#80446 的 `top-aggregate-maxrow` 去掉 `@Ignore`，基线换成"不用 MV 的结果"；Tabular 的 5 个等原因确认 | 已修的 bug 被固定 | 先在本机运行，确认新输出正确 |
| 失败用例的基线（G8） | 先在 CI 确认 12 个失败；按各 bug 的状态逐个更新：#79870（`C_V_5`、`expression`）、#79508（`running total`）、vpm 的时间列、增量三个用例的日期条件改相对日期 | 失败不再常态化 | 先核对新输出对不对，再更新 |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- **本轮真的运行了：** 6 个 profile 各整套运行一次；失败的 8 个（others 5、incrementalmv 3）单独重跑一次（都复现），others 的 5 个另用 `-Dmv.compare=both` 跑一次；vpm 的 4 个没有单独重跑；运行在本机 Windows，其余都是默认 `golden` 模式，没有整套跑 `nomv`、`both`；CI 上的结果没有确认。`vs_backup_scan.py` 能读 `mvStorage.zip`（`inventory` 的组件计数、`load`、`load_bookmarks` 可用），但它的计算字段几张表只对计算字段模块有意义，`crosscheck`、`strictcheck` 针对 `ViewsheetTest` 的写法，mv 的 spec 不是；用例和资产的交叉统计用自写脚本（`mv_inv.py`、`mv_tests.py`、`mv_zip.py`、`mv_files.py`，在 scratchpad，没有提交；组件和选项的扫描脚本 `mv_vs_scan.py` 已提交到工具目录），worksheet 用 `ws_scan.py inventory`；这些统计都是正则静态结果。
- 统计口径：用例按 `def '名'()` 切分；"断言命中"按 `getMVHitInfo()` 出现，"只断言已定义"按 `getMVDefInfo()` 出现且没有 `getMVHitInfo()`；没有逐个读用例内容，`IncrementalDataChange_Spec` 的 12 个用例（用 `rebuildMV` 或直接读 MV 内容断言）、`WSMV_Path_Spec` 没有 `getMVHitInfo` 但可能有其他形式的断言，我只读了后者的文件头。
- 静态推断：孤儿 viewsheet 的数量（195）、`NOT Transform` 没被引用（只搜了字面量 id）、关键词匹配的"名字级"数量；`getMVDefInfo` 在 `AssociationMV_Spec` 里是否因为关联 MV 本来就不能用命中断言（关联 MV 是单独的表）而是有意为之，没有核对。
- 产品能力清单与分支来自代理读取源码，`file:line` 大部分用 `grep -n` 核对，带 `~` 的偏差 1 至 3 行；没有检查 Ignite、`SubMVTask` 内部、`MVBuilder` 分块规则。`SelectionUpTransformer` 的 `mv.outer.moveUp`、`mv.union.moveUp`、`mv.ignore.nestedSelection` 在代码里是驼峰，默认文件里是小写，`SreeEnv` 查找是否区分大小写没有核对。
- 组件和选项扫描只读 XML 里存在的节点；`vs_backup_scan.py` 的 `inventory` 能直接读 `mvStorage.zip`，但它的计算字段几张表只适用于计算字段模块，本轮只用了它的 `load`、`load_bookmarks` 和 `inventory` 的组件计数；`crosscheck`、`strictcheck` 没有用（mv 的 spec 不是 `ViewsheetTest`）。XML 节点名（`viewsheetInfo`、`timeInfo`、`logScale`、`measureValue` 等）对照真实 XML 核对过，`rangeType` 各数字的含义没有核对。
- §7 只看了近一年（2025-11-13 起）的 165 条 Bug，不含 spark mv，更早的 1547 条没看；分类按标题，抽样 81 条里约 85% 分对，各类的数量只能当大致数字；"提到编号"只搜了 mv 的 spec 和 `community/core`、`community/server`、`enterprise` 的 `src/test/java` 文件内容，没有读测试内容，测试里没写编号的 bug 看不出来；`redmine_fetch.py` 没有时间范围过滤，是取回后再按 `created_on` 过滤的。
- 按 viewsheet 名字关键词做的统计容易误配：曾把 `unmerge-*`（不可合并公式的用例）当成 merge join 数到 33 个，已改正；表里带"名字级"的数字只表示名字里含这个词，没有核对内容；`rank|top` 这类词会匹配 `topmv` 目录，已不再使用。
- 13.8 的对照主要读了测试方法名、类声明和 import，只读了个别用例的方法体，没有编译运行；分支是 `E:\style-intelligence` 的 `epic-test-coverage`（`6b0ba0e7f2`），该分支之外（别的 `coverage-*` 分支、13.8 主线上的测试）没有查；`E:\style-intelligence` 有一个未提交的 `java/build.gradle` 改动，不是我改的，我没动。
- **需要开发回答的问题：** WS MV 的语义（`isWsMVNotAllowed` 恒为 false 并注释 "currently disabled"，与 `ws.mv.enabled` 并存）；`mv.outer.moveUp` 对左右连接单侧条件的准确规则；`mv.compare=nomv/both` 是否应作为 CI 的默认（维护者的决定是 CI 只做 golden）；`RankingConditionTest` 整类 `@Disabled("Ignite causing problems")` 现在是否仍然需要。
- **需要用户决定的事：** 是否把 `wsmv` 加进 `test-mv.yml`（G6）；关联 MV 的命中能否记录、要不要做（G1，PR #188 里没换）；#80458、#80446 现在去掉 `@Ignore` 的基线来源；12 个失败用例先在 CI 环境确认（G8）。

## 7. 已知 bug 对照（步骤 3b-D）

**样本与口径（先读这个）：**

- **取数：** Redmine 的 `Product` 项目，按标题含关键词取（`subject ~`，所有状态），用 `redmine_fetch.py` 翻页取到服务端总数：`MV` 1599 条、`materializ` 190 条、`incremental` 57 条，取回的数量和服务端 `total_count` 一致；三份合并去重后 1766 条，其中 Bug 类 1712 条（2015 年至今）。这是候选池，不是"MV bug"的结论：标题含关键词不等于是 MV 的问题。
- **范围：** 只看近一年，创建时间不早于 2025-11-13，共 **165 条 Bug**。不看 spark mv（产品已不支持，这一年里标题也没有出现 spark）。更早的 1547 条不在本节范围。
- **过滤：** 先去掉 38 条噪声，剩 **127 条**：AI 插件和 Wiz 工具类 23 条（分类为 AI Plugin、AI Wiz）、EM 对话框和界面细节 6 条、标题里的 MV 不是这个功能的 9 条（存储升级、H2 文件名、日期格式化、页分隔等）。
- **分类：** 按标题关键词分根因类别（`.claude/workflow-shared/datatest-gap/mv_bug_classify.py`，第一条命中的规则生效），**没有读全部描述**。**每类抽样 7 条（不足 7 条的取全部）读了描述核对，共 81 条：81 条都和 MV 有关，69 条分在了正确的类里（约 85%），12 条分错了**：#73865（EM 里 MV 资产的选中状态，不是选择组件）、#79400、#79402、#79387（分到日期或性能，实际是扫描起点、哈希表扩容、清理范围）、#76971（日期列写缓冲区溢出，根因是并行写）、#78059（EM 里站点管理员的开关，被 `dates` 匹配到日期类）、#78331（脚本条件被超时中断）、#78340（WS MV 没被使用）、#73220、#79265（创建或调度时的异常）、#75659（交叉表数据变了）、#77901（取数错误被吞）。**各类的数量只能当大致数字。**
- **覆盖对照：** "被提到"指 bug 编号出现在 `mv/src/test/groovy` 的 spec 里，或出现在产品仓库的测试里（`community/core`、`community/server`、`enterprise` 的 `src/test/java`）。**被提到不等于断言了这个行为**，没有读测试内容；测试里没写编号的 bug 看不出来。

| 根因类别（标题关键词归类） | bug 数 | 未关闭 | datatest 提到 | 产品单测提到 | 都没有 | 抽样核对 |
|---|---|---|---|---|---|---|
| 创建与更新失败（构建、cycle、后台任务） | 24 | 1 | 1 | 6 | 17 | 6/7 |
| VPM、权限与跨租户 | 25 | 5 | 0 | 12 | 13 | 7/7 |
| 并发、集群与文件 | 12 | 0 | 0 | 6 | 6 | 7/7 |
| 使用 MV 时的异常（NPE、越界、报错） | 9 | 1 | 0 | 3 | 6 | 4/7 |
| 增量更新与删除 | 9 | 2 | 1 | 6 | 3 | 7/7 |
| 日期、类型与时区 | 8 | 3 | 1 | 7 | 1 | 4/7 |
| 性能与内存 | 8 | 0 | 0 | 2 | 6 | 5/7 |
| 滑块、选择与书签 | 7 | 3 | 5 | 3 | 2 | 6/7 |
| 空值与 NOT 条件（MV 与实时结果不同） | 6 | 3 | 4 | 6 | 0 | 6/6 |
| MV 没被使用或读不到数据 | 6 | 1 | 1 | 3 | 2 | 5/6 |
| 公共 API（REST） | 5 | 3 | 0 | 1 | 4 | 5/5 |
| 其他结果与 MV 不一致（分组、join、子查询、聚合） | 4 | 1 | 1 | 2 | 2 | 3/4 |
| MV 代码缺陷（配置项、桩、从不调用） | 4 | 0 | 0 | 0 | 4 | 4/4 |
| 合计 | 127 | 23 | 14 | 57 | 66 | 69/81 |

**读法：**

- **datatest 能碰到的是"结果和不用 MV 不一致"那几类**：空值与 NOT 条件、滑块与选择、增量更新、日期与类型、其他结果不一致，这五类共 34 条，datatest 提到了其中 12 条，产品单测提到 24 条，两边都没提到编号的有 8 条（增量 3、滑块 2、其他结果 2、日期 1）。
- **datatest 本来就不该管的**：并发与文件、性能与内存、VPM 与跨租户、公共 API、代码缺陷、使用时的异常、创建与更新失败（后者里的集群和 cycle 放在 `test/cluster`）。66 条没被提到编号的 bug 里，有 56 条在这些类里，可以由产品单测或集群用例覆盖，不属于 datatest 的缺口；它们是否真的有测试，看不出来。
- 这一年的 bug 里，**根因最集中的是 MV 路径与实时路径在空值、NOT 条件、滑块取值上的语义不同**（上表的头几类），而 `others` 里 5 个本机失败的用例也落在这一带（见 §2），说明这类问题还在持续出现。

**未关闭的 23 条**（状态不是 Closed、Rejected、Ignored，所以包含 Resolved；状态按 2026-11-13）。"dt/pr"表示被 datatest/产品单测提到，`--` 表示没有：

| bug | 状态 | dt | pr | 类别 | 标题 |
|---|---|---|---|---|---|
| #79508 | Feedback | dt | pr | 日期、类型与时区 | postgres `time with time zone` 按小时分组，MV 与实时的分组不同 |
| #80446 | Resolved | dt | pr | 空值与 NOT | 分组表设最大行数，MV 后空值组排在最前 |
| #80458 | Resolved | dt | pr | 滑块与选择 | 时间/日期/时间戳组合范围滑块取值顺序不同 |
| #80170 | New | dt | -- | 滑块与选择 | 书签恢复的组合范围滑块少选一个值 |
| #80445 | New | dt | -- | 滑块与选择 | 同一表 join 两次，选择列表多出行 |
| #80457 | New | dt | -- | 没被使用 | 带变量的关联子查询，提示不支持且没有数据 |
| #79858 | Resolved | -- | pr | 日期、类型与时区 | 带时区时间戳改成 TimeInstant 后整列为空 |
| #79870 | Resolved | -- | pr | 空值与 NOT | 数据库表"不等于"保留了空值行 |
| #80152 | Resolved | -- | pr | 空值与 NOT | "不包含"变量条件多出行 |
| #80155 | Resolved | -- | pr | 日期、类型与时区 | Long 变量默认值超过 int 范围不生效 |
| #80096 | Resolved | -- | pr | VPM、权限与跨租户 | 修改组织 id 后组织的 MV 没有数据 |
| #80313 | New | -- | -- | 增量更新与删除 | 增量更新没有新行的第二次更新后 viewer 打不开 |
| #80344 | New | -- | -- | 增量更新与删除 | `MV.<列>.Max` 追加条件增量更新后越界（`IncrementalDataChange_Spec` 里有 `$(MV.customer_id.Max)` 用例，不是按编号找到的） |
| #80342 | New | -- | -- | 其他结果不一致 | DB2 方差和标准差单行分组为 0 |
| #73812 | Resolved | -- | -- | 使用 MV 时的异常 | 匿名用户 NP |
| #78452 | Resolved | -- | -- | 创建与更新失败 | `ws.mv.enabled=true` 时建 WS MV 报错 |
| #80856 | Resolved | -- | -- | VPM、权限与跨租户 | EM 仓库和 MV 接口的权限闸门 |
| #81297 | New | -- | -- | VPM、权限与跨租户 | 只有 MATERIALIZATION ACCESS 的用户 |
| #81309 | Resolved | -- | -- | VPM、权限与跨租户 | 克隆组织的 id 变更 |
| #81331 | In Progress | -- | -- | VPM、权限与跨租户 | id 变更被拒绝后下一个 id |
| #81059 | In Progress | -- | -- | 公共 API | 用户列表按空格拆分 |
| #81060 | In Progress | -- | -- | 公共 API | 失败的分析任务被报告为完成 |
| #81061 | In Progress | -- | -- | 公共 API | 更新不存在的 MV 带 cycle 时 NPE |

其中 datatest 能验证、**两边都没提到编号**的未关闭 bug 是：#80313、#80344、#80342（见 G5 和 T9）。#80445（`composite3`）、#80457（`variable in subquery`）有被 `@Ignore` 的用例，#80170（`rangeslider-compositevalue`）有 `@PendingFeature` 的用例，也就是说用例在，等修复后启用。公共 API 类（#81059 至 #81061）、权限类和 id 变更类不属于静态 datatest。

## 8. 具体要做（按顺序，均未开始）

| # | 要做什么 | 在哪里改 | 做完怎么验证 | 前提 / 注意 |
|---|---|---|---|---|
| T1 | 给 78 个只断言已定义的用例补基表 MV 的命中检查（G1 第一步）：保留 `getMVDefInfo().containsAll([...])`，从同一个列表取出不带 `ASSOCIATION_` 的名字，加 `getMVHitInfo().containsAll([...])`；脚本生成，期望值取自原列表；`NotPublic_Text` 不动；列表里全是 `ASSOCIATION_` 的用例单独列出 | datatest 分支 `test-mv-association-base-hit`（已建，未改文件）：`AssociationMV_Spec`、`CalcField_Spec`、`Transform_Spec`、`MVHit_*`、`VSCondtion_Spec`、`SubMV_Others_Spec`、`TopMV_*` | 跑涉及的 spec；加了之后失败的用例单独列出，不删检查；加一次反向检查：把某个基表 MV 名改错，用例应当失败 | 只验证了 `TestCase_Selectionlist1` 的命中记录里有基表 MV，其余要改完才知道 |
| T2 | 把 `wsmv` profile 加进 `test-mv.yml`（G6） | `.github/workflows/test-mv.yml`，照 `vpm` 一步的写法 | workflow 手动触发一次，`WSMV_Path_Spec` 出现在报告里 | 该 spec 改动 `docker/src/test/resources/H2Data/mvorder1.mv.db`，只影响本机 |
| T3 | 本机 12 个失败用例：先在 CI 上确认，再按各 bug 的状态逐个更新 golden（核对新输出，不直接复制）；增量三个用例的日期条件改相对日期 | `MV_Creator`、`vs query and condition`、`hit mv`、`vpm` 的基线；`Append_Spec`、`Delete_Spec`、`Transform_Spec` | 各 profile 在干净的 `mv/target` 下 0 失败；`both` 下 `nomv` 比较仍通过 | 见 G8；#79870 现在是 Resolved（Redmine，修复见 stylebi PR #7747、#7756、#8045） |
| T4 | 用 `NOT Transform` 的 11 个现成 viewsheet 建反向路径 spec（G2） | `mv/src/test/groovy/.../cases/` 下新 spec（放哪个 profile 待定） | 每个用例：不命中、结果与不用 MV 一致；反向：把对应的拒绝规则关掉时用例失败（做不到就说明） | 先逐个确认 11 个 viewsheet 各触发哪条规则 |
| T5 | `#80458`、`#80446` 对应的 `@Ignore` 用例去掉 `@Ignore`（G9） | `MVHit_RankingAndFilters_Spec.chart-time type`、`SubMV_Others_Spec.top-aggregate-maxrow` | 本机运行；基线换成"不用 MV 的结果"，`both` 下通过 | `top-aggregate-maxrow` 要补选择列表的 golden |
| T6 | `mv.*` 配置项的几个会改变结果的场景（G4）：`mv.preferred.block`（#79869）、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`、`date.common.options` | 复用现有 viewsheet，`@SreeProperty` 设置属性 | 改值后命中且结果与不用 MV 一致 | `MVTest.clearEnv` 要补复位 |
| T7 | 从 13.8 移植单测：`MvStorageEngineFeatureTest` 的 `DictionaryPart`（`XDimDictionary`）、`DataPart`、`Built`（`MVQueryBuilder`、`SubMVQuery`）；夹具移植后再移 `ConfigMvBehaviorS2Test`、`MvManageCustomerP2Test`（见 §4） | 产品仓库 `core/src/test/java/inetsoft/mv/`，新分支 `test-*` | 单独运行通过，撤掉对应产品代码后失败（逐个撤掉对应产品代码，确认测试会失败） | `ReportEnv` 要换成 `SreeEnv`；`FsPart` 的 4 个 fault 用例、依赖 `DUtil` 等四个类的 mapreduce 用例不能移植；`DashboardHarness` 等夹具 StyleBI 里没有，先问要不要移植夹具 |
| T8 | 新写单测：`SelectionUpTransformer` 的 fault、`VSMVAnalyzer.isAcceptable`、`MVTransformer.isCombinable`、`MVDef.canHitMV`、`WSMV*` 转换器（G3、G6） | 产品仓库 | 同上 | 夹具办法没确认；`mv.outer.moveUp` 规则、WS MV 语义先问开发 |
| T9 | Append、Delete 的代表性用例加 `compareWithRebuild`（G5） | `Append_Spec`、`Delete_Spec` | 增量结果与全量重建一致 | 适用范围：取新行的追加条件和不引用 `MV.` 的删除条件 |
| T10 | 问开发：WS MV 语义（`isWsMVNotAllowed`）；`mv.outer.moveUp` 对单侧条件的规则；关联 MV 的命中能否记录（G1 第二步） | — | — | 见 §6 |
| T11 | 按组件和选项扫描发现的零覆盖项补用例（G13）：`associationEnabled=false`、`bypass=true`、`meta=true`、`full=true`（非增量 profile）、图表百分比、`@nomv` 计算字段；核对 `CalcMV` 38 个 viewsheet | 复用现有 viewsheet，改 `viewsheetInfo` 的对应属性（运行时复制，不改 `mvStorage.zip`，做法同 `WSMV_Path_Spec`）；`CalcMV` 新建 spec | 每个选项：命中/不命中符合预期且结果与不用 MV 一致 | 先确认每个选项的预期行为（`associationEnabled=false` 不建关联 MV 等，见产品报告） |

## 附：数据来源与复现

```
# 每个 profile 单独运行（本机，先 ./mvnw package -pl commons -DskipTests，运行前 rm -rf mv/target）
./mvnw test -pl mv -Ptopmv
./mvnw test -pl mv -Psubmv
./mvnw test -pl mv -Pincrementalmv
./mvnw test -pl mv -Pwsmv
./mvnw test -pl mv -Pvpm
./mvnw test -pl mv -Pothers

# worksheet 统计（对 mvStorage.zip 适用；执行路径只是静态信号）
python .claude/workflow-shared/datatest-gap/ws_scan.py inventory mv/src/test/resources/config/mvStorage.zip

# 13.8 对照：直接在 E:\style-intelligence（分支 epic-test-coverage）的 java/src/test/java 下按文件名、类名、import grep；
#   StyleBI 是否有对应类：在 D:/stylebi/community/core/src/main/java 下找同名 .java

# 已知 bug 对照（近一年，需要 REDMINE_URL、REDMINE_KEY 环境变量）
#   python .claude/workflow-shared/datatest-gap/redmine_fetch.py --subject MV --out mv.json
#   python .claude/workflow-shared/datatest-gap/redmine_fetch.py --subject materializ --out mat.json
#   python .claude/workflow-shared/datatest-gap/redmine_fetch.py --subject incremental --out inc.json
#   python .claude/workflow-shared/datatest-gap/mv_bug_classify.py mv.json mat.json inc.json --since 2025-11-13 \
#       --datatest mv/src/test/groovy --product <stylebi>/community/core/src/test/java \
#       --product <stylebi>/community/server/src/test/java --product <stylebi>/enterprise/src/test/java [--sample 7]

# viewsheet 的组件和选项扫描，含书签状态（复用 vs_backup_scan.load 和 load_bookmarks）
#   python .claude/workflow-shared/datatest-gap/mv_vs_scan.py
#   python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory mv/src/test/resources/config/mvStorage.zip

# spec 和 zip 的统计：mv_inv.py、mv_tests.py、mv_zip.py、mv_files.py（自写，未提交；
# 读 mv/src/test/groovy/.../cases/*/*_Spec.groovy 与 mvStorage.zip 的 blob-index.json）
```
