# mv 测试缺口与补充方案

> 日期：2026-11-13　模块：`mv`，profile `-Ptopmv`、`-Psubmv`、`-Pincrementalmv`、`-Pwsmv`、`-Pvpm`、`-Pothers`，20 个 spec
> datatest 基线：`Stateless-Sessions1`（`ca972e26`）；产品源码：`main`（`28cf7947b9`，`2.1.0-SNAPSHOT`，与 datatest `pom.xml` 一致）
> 13.8 对照：本机 `E:\style-intelligence`，分支 `epic-test-coverage`（`6b0ba0e7f2`），见 §4 末尾

## 1. 结论

mv 的验证机制是各模块里最严的一个：每个用例导出的文件数必须等于基线文件数，逐文件比对（`MVTest.groovy:798`-`836`），导出异常和 MV 创建失败都会让用例失败，再配上"命中了哪个 MV"的断言和"与不用 MV 的结果比较"。本机 6 个 profile 全部运行了一遍：615 个用例执行，**12 个失败**（vpm 4、incrementalmv 3、others 5），全部在单独重跑时稳定复现。others 的 5 个在 `-Dmv.compare=both` 下，MV 的结果与不用 MV 的结果一致，只有基线与两者都不同，说明这 5 个是基线与当前输出不一致，不是 MV 路径与实时路径的差异。**T3（2026-11-18）已查明并更新：12 个都是产品已关闭的修复让 golden 过期，没有新的产品问题，见 §6 第 7 条**；CI 上的结果仍没有确认。

**已经做过和已经决定的**（本文不重复）：命中断言（datatest PR #188，约 489 处断言换成命中记录）、MV 开/关对比（`-Dmv.compare`，PR #193）、增量 MV 的源数据变化和全量重建对比（PR #194、#195）、WS MV 的一条通路（PR #200）、已修 bug 的 golden 更新（PR #196、#197）、`@Ignore` 用例和旧链接的整理（PR #198）、`others` 每个 spec 单独 JVM（PR #199）、cycle 在 `test/cluster` 的验证（enterprise PR #1604、#1701，我没有核对）。以下几项按维护者的说明是**已决定不做**的（仓库里没找到书面记录）：datatest 各 workflow 的定时运行不恢复、`mvStorage.zip` 的清理不做、`where:` 数据表改写不做、`@Retry` 不改。

本文新增的部分：

1. **反向路径几乎没有用例**（G2）：全部用例里只有 2 个断言了"没有命中"；zip 里专为此准备的 11 个 `NOT Transform` viewsheet 没有任何 spec 引用（T4 已补 9 个，§6 第 6 条）；产品侧决定 MV 能不能建、能不能命中的分支（`VSMVAnalyzer`、`SelectionUpTransformer` 的二十多个 fault）既没有 dashboard 用例，产品仓库也几乎没有单测（`SelectionUpTransformer` 2048 行只有 3 个测试，`MVTransformer` 673 行没有，G3）。
2. **`mv.*` 配置项几乎空白**（G4）：产品读的 20 多个属性里只有 `ws.mv.enabled` 被设置过。
3. **`wsmv` profile 不在 CI 里**（G6）：`test-mv.yml` 只跑 incrementalmv、topmv、submv、vpm、others 五个 profile，PR #200 加的 `WSMV_Path_Spec` 在 CI 里从不运行（静态结论，只看了 `.github/workflows/test-mv.yml`）。
4. **本机 12 个用例失败**（G8）：与已知的几处问题对得上（#79870、#79508 等），见 §2 的运行结果；基线还没跟上这些修复。**T3 已更新这 12 个用例的 golden**（§6 第 7 条）；同时第一次整套用 `-Dmv.compare=both` 运行，发现 4 个用例用 MV 与不用 MV 的结果不同（G14，原因待查）。
5. **组件和选项的零覆盖项**（G13）：`viewsheetInfo` 的 `associationEnabled=false`、`bypass=true`、`meta=true` 在 804 个 viewsheet 里一个都没有；整个 `CalcMV` 目录 38 个 viewsheet 没被引用（§2 的详细扫描）。

仍然成立的两点：命中断言留有一个已知的洞（78 个用例只断言"已定义"，是 PR #188 里没换的含 `ASSOCIATION_` 的断言，G1）；`getMVHitInfo` 在组件都返回纯值时退回成"已定义"。

先做：手动触发一次 `test-mv.yml`，确认 wsmv 一步（PR #226 已加）和 T3 更新后的 golden 在 CI 上的结果；查 G14 的 4 个用例用 MV 与不用 MV 结果不同的原因；补 `mv.*` 配置项几个会改变结果的场景。T1、T2、T5 已在 PR #226 完成；`NOT Transform` 的反向路径用例（T4）已补 9 个（§6 第 6 条）；原来要问开发的问题大部分已从代码确认（§6）。

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
- 与不用 MV 的结果比较（PR #193）：`-Dmv.compare=golden`（默认）只比基线，`nomv`、`both` 才比不用 MV 的结果（`MVTest.groovy:55`、`:856`-`:857`）。**CI 用默认的 golden**，所以 `compareWithoutMV()` 在 CI 里不执行（`:857` 直接返回）；基线是回归基线（来自 MV 的输出），不是独立标准答案。本轮评估时整套没有跑 `nomv`、`both`；T3（2026-11-18）第一次 6 个 profile 整套用 `both` 运行，`compareWithoutMV` 让 4 个用例失败（G14），在默认的 `golden` 下这 4 个通过。
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

  本机的 `inetsoft-core-2.1.0-SNAPSHOT.jar` 构建于 2026-11-13 08:25，早于 `main` 当前的 `28cf7947b9`（11:14）；之后的 5 个提交是组织 id、combo box 等（没有核对它们是否影响查询结果）。jar 里已经含 11-12 的 #8082（#80155）、#8083（#80458）、#8084（#80446）三个 MV 修复，这些修复会改变 MV 输出的顺序和空值位置，datatest 的基线可能还是修复前生成的，这是**假设**，没有验证；Redmine 上已记录了其中大部分（#79870、#79508、vpm 的时间列），所以这 12 个多数不是新问题，是基线还没更新。（以上是 11-13 的记录；T3 在 11-18 逐个查明了原因并更新了 golden，增量三个用例不是日期漂移，`selection association4`、`assembly_query` 也有对应的修复，见 §6 第 7 条。）

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
| 数据源类型的限制（拒绝嵌入表、cube、查询变量） | `VSMVAnalyzer.java:700`-`:736`、`:927`-`:980` | 嵌入表、命名分组变量已由 `NotTransform_Spec` 覆盖（T4，断言拒绝原因）；cube 的规则在测试环境走不到（§6 第 6 条）；查询变量没有用例 |
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
| 嵌入表组件（`EmbeddedTable`，拒绝，`VSMVAnalyzer.java:712`-`:718`） | 1 | **0** | — | `NOT Transform` 的两个嵌入表 viewsheet 已由 `NotTransform_Spec` 覆盖（T4） |
| Cube 绑定（拒绝，`VSMVAnalyzer.java:701`-`:708`） | 1（`NOT Transform/cube`，一个图表绑定 `___inetsoft_cube_db2_1/l1`） | **0** | — | 没有用例：绑定在测试环境解析不出来，分析器在 `MVDef.java:1448` 抛空指针，走不到 cube 规则（T4，§6 第 6 条） |
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
| `VSMVAnalyzer.java:690`-`:750` | 创建时拒绝 cube、嵌入表、查询变量、命名分组变量、命名分组动态条件 | 嵌入表、命名分组变量：`NotTransform_Spec`（T4）；cube、查询变量、命名分组动态条件：无 |
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
| G1 | 78 个用例只断言"MV 已定义"（`getMVDefInfo`）：其中 32 个在 `AssociationMV_Spec`、24 个在 `CalcField_Spec`，其余 22 个分散在 `Transform`、`MVHit_RankingAndFilters`、`VSCondtion` 等；这 78 个全部含 `ASSOCIATION_` 前缀的断言，正是 PR #188 描述里"78 assertions unchanged"（含 `ASSOCIATION_` 的断言）的那批（另有 `NotPublic_Text` 带 `TODO`）。`getMVHitInfo` 在组件都是纯值时退回"已定义"（`MVTest.groovy:455`-`:461`，PR #188 描述里也写了） | 关联 MV 没有被命中、查询悄悄退回实时库，结果相同，用例照样通过；MV 被丢弃的分支（`AssetQuery.java:143`-`:228`）不会被发现 | **已知、有意保留**：PR #188 的描述和提交说明只写了"78 处没换"，没写原因。本地探针（`AssociationMV_Spec.TestCase_Selectionlist1`，临时加一行打印，已还原）显示：命中记录里只有 `MVSCRIPT1|TopMV`、`Query1|TopMV` 等 6 个基表 MV，**没有任何 `ASSOCIATION_*`**，而定义里有 12 个（6 个基表 MV + 6 个关联 MV）；也就是命中记录目前观察不到关联 MV，换成命中断言这 78 处会全部失败。这是从探针推断的原因，不是 PR 的说明。可以做的：这些用例保留关联 MV 的定义检查，另外加一条对非 `ASSOCIATION_` 名字的命中检查（探针里基表 MV 的命中是记录到的，已在 PR #226 做完）；关联 MV 本身的命中要改测试机制，2026-11-17 读代码确认测试侧可以记录，不用改产品（§6"读代码确认的结论"第 3 条） | 中 |
| G2 | 反向路径（不该走 MV、MV 不该被建）几乎没有用例：全部用例里只有 2 个断言命中为空（`VSCondtion_Spec`、`SubMV_Others_Spec`）；zip 里有 11 个 `NOT Transform` viewsheet（crosstab ws、cube、embedded 两个、subquery 两个、ranking 两个、group variable、field condition、adhocfilter_measure）没有任何 spec 引用，也没有基线 | 分析器改动后错误地对 cube、嵌入表、带查询变量、命名分组变量的表建了 MV 或命中了 MV；或本该拒绝却建出错误的 MV。查询出错时因为不命中，结果往往仍然对，只有"是否命中"这一项能暴露 | dashboard（用现成的 11 个资产：断言不命中且结果与不用 MV 一致；**T4 已补 9 个**，§6 第 6 条）+ 单测（`VSMVAnalyzer.isAcceptable`） | 高 |
| G3 | 产品侧的转换层几乎没有单测：`trans` 包 11 个类中只有 `SelectionUpTransformer`（2048 行，3 个测试）；`MVTransformer`（673 行）、`WSMVTransformer`、`AbstractTransformer`（1006 行）、`CalcFieldTransformer`、`NamedGroupTransformer`、`WSMVSelectionDownTransformer`、`WSMVAggregateDownTransformer` 等没有测试；`VSMVAnalyzer`（1689 行）只有命名分组条件 11 个测试 | 选择条件上移、聚合下移、分析器接受/拒绝规则出错，只有碰巧被某个端到端用例触发才会发现，而且发现时已经是结果差异，定位很慢（`SelectionUpTransformer` 二十多个 fault 条件见 §2） | 单测（产品仓库），按 fault 条件一个一个固定；入口需要搭 Viewsheet/Worksheet 夹具，难度高，需先确认夹具办法；13.8 的 `epic-test-coverage` 分支里这些类也没有测试，不能移植（见 §4 末尾） | 高 |
| G4 | `mv.*` 配置项几乎全是空白：产品读的二十多个属性里只有 `ws.mv.enabled` 被设置；`mv.preferred.block`、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`、`mv.dim.max.size`、`mv.detail.data`、`date.common.options`、`time.common.options` 等从未改值 | 改了默认值才出现的错误：例如 #79869（`mv.preferred.block` 非默认时追加的行丢失）在 datatest 里没有对应用例；`mv.union.moveUp` 关闭时的 union 选择上移；`date.common.options` 改变物化的日期级别后，没物化的级别查询是否仍对 | dashboard（用 `@SreeProperty` 设置属性后跑代表性用例），每个属性一个场景；`mv.dim.max.size` 的溢出警告用单测（13.8 的 `MvStorageEngineFeatureTest.DictionaryPart` 有 `XDimDictionary` 溢出的用例可移植）；`mv.ondemand`、`mv.required`、`mv.metadata`、`mv.dispatcher.*` 13.8 的 `ConfigMvBehaviorS2Test` 有，可移植 | 中 |
| G5 | 增量更新：Append、Delete、Transform 共 156 个用例是 golden + 命中，**不做开/关对比，也不做与全量重建的比较**（增量的 MV 不等于数据库，所以不做开/关对比；重建对比只在 `IncrementalDataChange_Spec` 里用，PR #194、#195）；`IncrementalDataChange_Spec` 本轮 16/16 通过（T9 补用例后为 41 个，按迭代计，2026-11-17 本机全部通过）。仍没有用例的：#80313（第二次无新行更新后 viewer 打不开，New，等修复后补用例）；#80344（`MV.<列>.Max` 追加条件后偶发 `Index 0 out of bounds`，New）只有 `$(MV.customer_id.Max)` 一个用例部分覆盖，据维护者此前记录有过部分运行失败，本轮该 spec 没有失败 | 增量路径独有的错误：#80313、#80344 复发或未修好时，大部分增量用例比的是 MV 自己的输出，不会暴露；数据真正变化时的增量结果只有 `IncrementalDataChange_Spec` 的用例与全量重建对照（原 16 个，T9 补完后 41 个，按迭代计）（156 个 golden 用例数据库不变，本来就不该等于全量重建，见 T9） | dashboard：在 `IncrementalDataChange_Spec` 里补数据变化的场景，对照 `Append_Spec`、`Delete_Spec` 的条件类型找缺的（原计划给 Append/Delete 现有用例加 `compareWithRebuild` 不可行，见 T9）；#80313、#80344 等修复后补 | 中 |
| G6 | **`wsmv` profile 不在 `test-mv.yml` 里**，`WSMV_Path_Spec` 在 CI 里不运行（静态：只看了 workflow 文件）。WS MV 只有这 1 个用例（PR #200 有意只走一条通路；"是否使用 MV"的判断由产品仓库的 `AssetQueryWorksheetMVTest` 的 25 个单测覆盖）；产品侧 `WSMVTransformer`、`WSMVAnalyzer`、两个 `WSMV*DownTransformer` 没有单测；`isWsMVNotAllowed` 恒为 false 的注释与 `ws.mv.enabled` 并存 | WS MV 的创建、读取、增量在 CI 里没有任何保护；WS MV 的选择下移、聚合下移、条件被清除的行为出错只有单测能发现，而单测没有 | 机制：把 `wsmv` 加进 `test-mv.yml`（该 spec 会改动 `mvorder1.mv.db`，CI 里没关系）；单测补 `WSMVAnalyzer`、两个 `WSMV*DownTransformer`；WS MV 的语义已从代码确认（§6"读代码确认的结论"第 1 条，`isWsMVNotAllowed` 的注释已过时） | 高 |
| G7 | 不可合并聚合公式：Product、Concat 在 viewsheet 里 0 个；其他不可合并公式各 1 至 23 个，且只验证"结果和基线一致"，不验证退回"读明细再在内存聚合"这条路径（`AssetQuery.java:175`-`:203`） | 不可合并公式被当成可合并公式在 MV 上直接合并（例如 Product 被当作 Sum 合并），结果错但基线是 MV 输出，不会发现 | 单测（`MVTransformer.isCombinable` 对每个公式的判断，期望值可由 `AggregateFormula.isCombinable` 的规则独立得出）；dashboard 补 Product、Concat 各一个 | 中 |
| G8 | 本机 12 个用例失败且稳定复现：vpm 4 个、others 5 个、incrementalmv 3 个。多数是已知的"基线还没跟上修复"：`C_V_5`、`expression`（#79870）、`running total`（#79508）、vpm 的时间列；incrementalmv 3 个行数差 1，该 profile 的数据依赖当前日期（维护者说明）。`selection association4`、`assembly_query` 的 NULL 与 0 我没有找到记录，未核对。CI 上是否也失败未确认 | 失败常态化以后，真实的 MV 回归会被淹没；基线是 MV 的输出，修复后不更新，用例就在固定旧行为 | 机制/基线：在 CI 环境确认；按各 bug 的状态逐个更新（核对新输出对不对再更新，不直接复制）。**T3 已做（2026-11-18）**：12 个都是已关闭修复让 golden 过期（#79508、#79870、#81084、#81182、#80953、#81043），逐个核对后更新；增量三个不是日期漂移，不用改相对日期（§6 第 7 条） | 中 |
| G9 | 被忽略的用例：`TabularSource_Spec` 整类（5 个，据维护者说明：继续 `@Ignore`，由维护者自己重做用例）；`chart-time type`（#80458）、`top-aggregate-maxrow`（#80446）这两个对应的 bug 现在是 Resolved，修复后应去掉 `@Ignore`，并补选择列表的 golden；`composite3`（#80445）、`variable in subquery`（#80457）仍是 New；`date range and number range` 按设计不可能通过（`TopMV_Others_Spec.groovy:401`-`:407`） | Tabular 数据源的 MV 创建与命中没有回归保护（#78339 已修）；#80458、#80446 已修，但用例仍被忽略，修复没有被固定 | 用例：#80458、#80446 现在可以去掉 `@Ignore`（`MVHit_RankingAndFilters_Spec` 在 surefire 里跳过 2 个，其中 1 个是 `chart-time type`，另 1 个没查）；先在本机运行并核对新输出 | 低 |
| G10 | CI 的触发：`test-mv.yml` 只能手动触发，`Jenkinsfile` 没有 mv；这是维护者的决定（所有 datatest workflow 的 `schedule` 都不恢复），本文不建议恢复。另一个可以评估的点：5 个 profile 各启动一次 JVM，是否合并或并行（本机整套约 20 分钟，topmv 2 分 43 秒、incrementalmv 2 分 58 秒、others 7 分 31 秒） | 回归在下一次有人手动触发前一直不被发现；本轮在本机就发现 12 个失败 | 不改；只在 §6 记一个问题：手动触发的节奏是否够 | 低 |
| G11 | 195 个 viewsheet 没有被 spec 按字面量引用（`hit mv` 110、`CalcMV` 38、`NOT Transform` 11 等）；16+3 个基线属于 `@Ignore` 用例；1 个 viewsheet 有基线但没被引用 | 资产里已经准备好的场景没有被运行（可能是遗留，也可能是引用方式静态看不到）；陈旧基线让人误以为被验证过 | 先核对是真孤儿还是间接引用，再决定删除或补用例（G2 的 11 个先用上） | 低 |
| G12 | 按需创建、MV 过期、数据周期、集群：datatest 没有用例，这是有意的——cycle 的风险在真实环境，放在 `test/cluster`（CY-02/03/04，enterprise PR #1604、#1701，本轮没有运行）；`MVRule`、`MVManager` 已有单测（22 个、35 个，mock 为主）。**datatest 里没有、单测里也没有**的是 `mv.ondemand` 按需创建、`isStale`、`isExpired`、无 MATERIALIZATION ACCESS 返回 null 的分支 | 按需创建出错、MV 过期判断出错、权限缺失时的行为 | 单测（产品仓库，补上面几个分支；`mv.ondemand` 的行为 13.8 的 `ConfigMvBehaviorS2Test.mvOndemand` 已有，可移植）；cycle 与集群不属于静态 datatest | 低 |
| G13 | 按 viewsheet 组件和选项的详细扫描（§2）发现的零覆盖项：`viewsheetInfo` 的 `associationEnabled=false`、`bypass=true`、`meta=true` 三个选项 0 个 viewsheet；`full=true` 只在 incrementalmv；图表百分比 0；计算字段 `@nomv` 0；Product、Concat 聚合 0；嵌入表组件 1 个但没被引用；整个 `CalcMV` 目录 38 个 viewsheet 没被引用 | `associationEnabled=false` 时仍然建了关联 MV；`bypass` 的 MV 带了 VPM 过滤或相反（#78390、#77666 同类）；设计态 `meta` 用了 MV；`full=true` 时瘦 MV 仍被缩列，选择列表缺值；图表百分比时仍用物理 MV 导致结果错；带 `@nomv` 的计算字段被建进 MV；明细型计算字段的 MV 出错（`appendDetailCalc`） | dashboard：每个选项一个用例（改现有 viewsheet 的 `viewsheetInfo` 即可，不需要新建数据）；`CalcMV` 的 38 个 viewsheet 先核对是遗留还是可直接用，再补 spec | 中 |
| G14 | 第一次整套用 `-Dmv.compare=both` 运行（T3，2026-11-18，产品 `main` `6b936637cf`，每个 profile 前清空 `mv/target`、还原 H2 文件），4 个用例的 `compareWithoutMV` 失败，`compareData`（golden）通过：`SubMV_Spec.aggbase`（不用 MV 时 `Query1` 的数值是整数，如 `109993`，用 MV 是 `109993.4167`；`Query4` 不用 MV 时出现 `-0` 且行顺序不同）、`crossjoin`（`Sum(QUANTITY)` 用 MV `251.0727`、不用 MV `251`）、`rangecolumn`（图表的数值用 MV 带小数、不用 MV 是整数）、`TopMV_AggregateFormulas_Spec.datelevel_none`（计算字段 `NTime` 的 None 级别：用 MV 是 `1970-01-01 00:02:07`，不用 MV 是 `2018-12-20 00:02:07`）。产品 `631443a906`（11-17）上 `aggbase`、`datelevel_none` 同样失败，不是 11-18 的提交引起的；golden 来自 MV 的输出，哪边对还没有核对。同类的已知差异有 `formula.avg.keep.type`（Bug #54404，`running total` 的注释），这里的方向相反（用 MV 带小数） | 默认 golden 下这些用例照样通过，用 MV 与不用 MV 的差异不会被发现；如果错的是 MV 一侧，golden 在固定错误结果 | 逐个查哪边对（用数据库独立算），是产品问题就提 bug；之后 golden 按正确的一侧更新 | 中 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测；问题出在组件、绑定、数据源之间的接线 → dashboard；验证机制本身的缺陷 → 改基类，不是加用例。

## 4. Java 单测补充

产品仓库已有的 MV 单测：`core/src/test/java/inetsoft/mv` 下 87 个文件，另有 10 个在其他包（均在 `core`，其他模块没有）；唯一的 `@Disabled` 是 `RankingConditionTest` 整个类（"Ignite causing problems"，没有 bug 编号；测试环境已不用 Ignite；本机绕过注解运行时被测试配置缺 bean 和一个死锁挡住，临时补上后通过，见 §6"读代码确认的结论"第 4 条）。较强的是：聚合公式（`inetsoft.mv.formula` 15 个类里 14 个有测试，接口 `MergeableFormula` 除外）、`data` 包（`BitDimIndex`、`SubMV`、`XDimDictionary`、`MVQuery`）、`MVDef`、`MVManager`、`MVBuilder`。测试用 Mockito 的很多，没有一个显式搭嵌入数据库。同名类按完整路径区分：`inetsoft.report.filter` 下的 `DistinctCount`、`Median`、`Mode`、`NthMostFrequent` 的测试不算在 `inetsoft.mv.formula` 头上；运行时 `GroupedTableBlock.getMVFormula` 把 `DistinctCount`、`Median`、`Mode` 路由到 `inetsoft.mv.formula`，`NthMostFrequent` 路由到 `inetsoft.report.filter`（`GroupedTableBlock.java:123`-`:136`）。

没有找到直接测试的重要类（"没找到"指没有以类名命名的测试文件，且 `inetsoft/mv` 下的测试没有 `grep -w` 到类名）：

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `trans.SelectionUpTransformer`（已有 3 个） | 每个 fault 条件一个用例：maxRows、排名、聚合排名、交叉表、旋转、反转、后置条件、隐藏选择、嵌套选择、outer/merge join、union/intersect/minus（含 `mv.union.moveUp`、`mv.outer.moveUp` 开关）。期望值是"该条件下返回 false 且带哪个 fault"，可以不依赖产品输出写出 | 约 25 | 高（需要搭 Viewsheet/Worksheet 夹具，入口未确认） | 部分：触发条件和"原条件 OR IS NULL"的改写已从代码确认（§6"读代码确认的结论"第 2 条），可按此写 fault 的期望值；上移后与不用 MV 的结果差异是否有意，等 T10 (a) |
| `VSMVAnalyzer.isAcceptable` 与 `isSuitableTable`（已有 11 个，只覆盖命名分组条件） | cube、嵌入表、查询变量、命名分组变量、命名分组动态条件、输入动态表、后置聚合计算、maxRows 与子选择、更新条件与旋转/反转/交叉表各一个 | 约 12 | 高 | 否 |
| `MVTransformer.isCombinable`、`AggregateFormula.isCombinable` | 9 个不可合并公式 + 可合并公式的逐个判断；多个 DistinctCount、单个 DistinctCount 与 `breakcol`；`NamedRangeRef` 分组 | 约 18 | 中 | 否（规则在 `AggregateFormula.java`） |
| `MVDef.canHitMV`、`isValidMV` | 分组/聚合列缺失、多个 count-distinct 与 `breakcol`、WS MV 与桌面模式恒为 true | 约 8 | 中 | 否 |
| `trans.WSMVSelectionDownTransformer`、`WSMVAggregateDownTransformer`、`WSMVTransformer` | 选择下移：旋转/反转、外连接、非简单 join、子查询值、DataRef 值、交叉表子表、子表聚合条件，各返回 false；聚合下移：父表交叉表、百分比、命名范围、命名分组 | 约 14 | 高 | 否：WS MV 的规则已从代码确认（§6"读代码确认的结论"第 1 条），`isWsMVNotAllowed` 的注释已过时 |
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
| 关联 MV 用例补基表 MV 的命中检查（G1） | 分两步。**第一步（用例，不改基类）**：对这 78 个用例，保留原来的 `getMVDefInfo().containsAll([...])`，从同一个列表里取出不带 `ASSOCIATION_` 前缀的名字，在 `verifyAll` 里加一条 `getMVHitInfo().containsAll([这些名字])`；用脚本按列表机械生成，期望值就是原列表里已有的名字，不取自运行输出；`CalcField_Spec.NotPublic_Text`（带 `TODO`）不动；列表里全是 `ASSOCIATION_` 名字的用例没有可加的，单独列出。改完后跑涉及的 spec，**加了之后失败的用例不删这条检查，单独列出来**（说明那个基表 MV 定义了但没被命中，要么是用例本身，要么是命中记录观察不到）。**第二步（改 `MVTest`，2026-11-17 已做，见 T10 (c)）**：让关联 MV 自己的命中也能被记录（读 `ViewsheetSandbox.getTableMetaData` 返回的 `MVTableMetaData` 的 `meta_rmv`，见 §6"读代码确认的结论"第 3 条），再把 `ASSOCIATION_` 的定义检查换成命中检查 | 第一步：基表 MV 真的被命中，而不是退回实时库；第二步：关联 MV 真的被命中 | 第一步依据本地探针：`TestCase_Selectionlist1` 的命中记录里有 6 个基表 MV、没有 `ASSOCIATION_*`（PR #188 没写不换的原因，这是推断）；只验证了这一个用例，其余 77 个要改完才知道；第一步已在 PR #226 做完；第二步：`recordMVLookupHit` 的 `findOnly` 查找覆盖不了关联 MV（有基表 MV 时返回基表 MV），改为读 `MVTableMetaData`；子表有运行时条件时本来就不用关联 MV，这类用例不能断言关联 MV 命中 |
| 反向路径（G2） | 用 `NOT Transform` 的 11 个现成 viewsheet，新建一个 spec；每个用例：创建 MV（预期失败或不创建）、执行、断言 `getMVHitInfo()` 为空、结果与基线一致 | 不该走 MV 的表不走 MV，结果仍正确 | **已做（T4）**：9 个进了 `NotTransform_Spec`，每条规则对应哪个 viewsheet 见 §6 第 6 条 |
| `mv.*` 配置项（G4） | 用 `@SreeProperty` 设置属性，复用现有 viewsheet：`mv.preferred.block`（Append 的一个用例）、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`（各取一个 union、外连接、嵌套选择的现有用例）、`date.common.options` | 改值后命中结果与不用 MV 一致 | 属性在 `MVTest.clearEnv` 里复位（目前只复位 `mv.outer.moveUp`），要补 |
| 增量一致性（G5） | 在 `IncrementalDataChange_Spec` 里补数据真正变化的场景，用 `rebuildMV` + `compareWithRebuild` 对照；先对照 `Append_Spec`、`Delete_Spec` 用到的条件类型列出该 spec 还没覆盖的（已对照，三批共 23 个用例已补，T9 已完成）。**不给** `Append_Spec`、`Delete_Spec` 现有用例加重建对比：这些用例数据库不变，重建对比要么必然失败、要么是空检查（原因见 T9）。#80313、#80344 等修复后补用例 | 增量更新后的结果与全量重建一致 | 该 spec 原 16/16 通过，T9 补用例后 41 个（按迭代计）全部通过 |
| WS MV 进 CI（G6） | 把 `wsmv` profile 加进 `.github/workflows/test-mv.yml`（照 `vpm` 一步的写法） | `WSMV_Path_Spec` 在 CI 里运行 | 该 spec 会改动 `docker/src/test/resources/H2Data/mvorder1.mv.db`（不影响 CI）；本机运行已通过 |
| 不可合并公式（G7） | 为 Product、Concat 各加一个 viewsheet | 不可合并公式的结果与不用 MV 一致 | 无 |
| 重新启用被忽略的用例（G9） | #80458 的 `chart-time type`、#80446 的 `top-aggregate-maxrow` 去掉 `@Ignore`，基线换成"不用 MV 的结果"；Tabular 的 5 个等原因确认 | 已修的 bug 被固定 | 先在本机运行，确认新输出正确 |
| 失败用例的基线（G8） | 按各 bug 的状态逐个更新：#79870（`C_V_5`、`expression`、`Testcase8`）、#79508（`running total`、vpm 的时间列）、#81084、#81182、#80953 与 #81043（增量两个） | 失败不再常态化 | **已做（T3）**，§6 第 7 条 |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- **本轮真的运行了：** 6 个 profile 各整套运行一次；失败的 8 个（others 5、incrementalmv 3）单独重跑一次（都复现），others 的 5 个另用 `-Dmv.compare=both` 跑一次；vpm 的 4 个没有单独重跑；运行在本机 Windows，其余都是默认 `golden` 模式，没有整套跑 `nomv`、`both`；CI 上的结果没有确认。`vs_backup_scan.py` 能读 `mvStorage.zip`（`inventory` 的组件计数、`load`、`load_bookmarks` 可用），但它的计算字段几张表只对计算字段模块有意义，`crosscheck`、`strictcheck` 针对 `ViewsheetTest` 的写法，mv 的 spec 不是；用例和资产的交叉统计用自写脚本（`mv_inv.py`、`mv_tests.py`、`mv_zip.py`、`mv_files.py`，在 scratchpad，没有提交；组件和选项的扫描脚本 `mv_vs_scan.py` 已提交到工具目录），worksheet 用 `ws_scan.py inventory`；这些统计都是正则静态结果。
- 统计口径：用例按 `def '名'()` 切分；"断言命中"按 `getMVHitInfo()` 出现，"只断言已定义"按 `getMVDefInfo()` 出现且没有 `getMVHitInfo()`；没有逐个读用例内容，`IncrementalDataChange_Spec` 的 12 个用例（用 `rebuildMV` 或直接读 MV 内容断言）、`WSMV_Path_Spec` 没有 `getMVHitInfo` 但可能有其他形式的断言，我只读了后者的文件头。
- 静态推断：孤儿 viewsheet 的数量（195）、`NOT Transform` 没被引用（只搜了字面量 id）、关键词匹配的"名字级"数量；`getMVDefInfo` 在 `AssociationMV_Spec` 里是否因为关联 MV 本来就不能用命中断言（关联 MV 是单独的表）而是有意为之，没有核对。
- 产品能力清单与分支来自代理读取源码，`file:line` 大部分用 `grep -n` 核对，带 `~` 的偏差 1 至 3 行；没有检查 Ignite、`SubMVTask` 内部、`MVBuilder` 分块规则。`SelectionUpTransformer` 的 `mv.outer.moveUp`、`mv.union.moveUp`、`mv.ignore.nestedSelection` 在代码里是驼峰，默认文件里是小写，`SreeEnv` 查找是否区分大小写没有核对。
- 组件和选项扫描只读 XML 里存在的节点；`vs_backup_scan.py` 的 `inventory` 能直接读 `mvStorage.zip`，但它的计算字段几张表只适用于计算字段模块，本轮只用了它的 `load`、`load_bookmarks` 和 `inventory` 的组件计数；`crosscheck`、`strictcheck` 没有用（mv 的 spec 不是 `ViewsheetTest`）。XML 节点名（`viewsheetInfo`、`timeInfo`、`logScale`、`measureValue` 等）对照真实 XML 核对过，`rangeType` 各数字的含义没有核对。
- §7 只看了近一年（2025-11-13 起）的 165 条 Bug，不含 spark mv，更早的 1547 条没看；分类按标题，抽样 81 条里约 88% 分对（按调整后的规则），各类的数量只能当大致数字；"提到编号"只搜了 mv 的 spec 和 `community/core`、`community/server`、`enterprise` 的 `src/test/java` 文件内容，没有读测试内容，测试里没写编号的 bug 看不出来；`redmine_fetch.py` 没有时间范围过滤，是取回后再按 `created_on` 过滤的。
- 按 viewsheet 名字关键词做的统计容易误配：曾把 `unmerge-*`（不可合并公式的用例）当成 merge join 数到 33 个，已改正；表里带"名字级"的数字只表示名字里含这个词，没有核对内容；`rank|top` 这类词会匹配 `topmv` 目录，已不再使用。
- 13.8 的对照主要读了测试方法名、类声明和 import，只读了个别用例的方法体，没有编译运行；分支是 `E:\style-intelligence` 的 `epic-test-coverage`（`6b0ba0e7f2`），该分支之外（别的 `coverage-*` 分支、13.8 主线上的测试）没有查；`E:\style-intelligence` 有一个未提交的 `java/build.gradle` 改动，不是我改的，我没动。
- **需要开发回答的问题（2026-11-17 读代码后剩下的）：** `mv.outer.moveUp=true` 时结果与不用 MV 不同（左表行在右表的匹配都不满足选择时整行被删掉，已实测，见下面"读代码确认的结论"第 2 条），是已知限制还是 bug；`mv.compare=nomv/both` 是否应作为 CI 的默认（维护者的决定是 CI 只做 golden）。`RankingConditionTest` 本机临时补上四个测试 bean 并修掉 `MVSupportService.AnalysisResult.waitFor` 的死锁后 3 个测试都通过，要不要按此重新启用（§6 第 4 条）。原来列的 WS MV 语义、关联 MV 命中能否记录已从代码确认，不用再问。
- **需要用户决定的事：** T3 更新后的 golden 在 CI 上确认（G8；用户 11-18 决定暂不触发 CI）。增量删除条件也删掉 NULL 行（#79870、#81043 的规则）用户已接受（2026-11-18）。原来列的 `wsmv` 进 CI、#80458/#80446 的基线来源已在 PR #226 处理。

### 读代码确认的结论（2026-11-17，产品源码 `main` `af5a802550`）

原来 T10 要问开发的问题，按代码能自己确定的部分如下。

1. **WS MV 是现行功能，`isWsMVNotAllowed` 的注释已过时。** `AssetQuery.java:102`-`:114`：`ws.mv.enabled` 打开时，查询先找底层表的 WS MV 挂上（`applyWSRuntimeMV`），再走 `WSMVTransformer.transform`；这条路径来自 stylebi PR #6556（Bug #78053）。代码和注释里的规则：
   - 只给直接绑查询的底层表（`BoundTableAssembly`）建 WS MV，且该表不依赖变量（`isVariableDependent`，`AssetQuery.java:453`-`:458`）。
   - 只给基于该 worksheet 的 viewsheet 查询（含预览、导出）用；worksheet 编辑器、设计模式、建 MV 时不用（`isWSMVEnabled`，`AssetQuery.java:377`-`:395`，Bug #33943）；viewsheet 关了 MV 时也不用。
   - VS MV 优先：只有 `MVTransformer.findMVTable(table) == null` 时才挂 WS MV。
   - 被连接的表有共同祖先时不做选择下移、聚合下移（`WSMVTransformer.transform`）；底层表自身的条件在 WS MV 上清空，避免重复应用，交叉表除外。
   - `TransformationDescriptor.isWsMVNotAllowed` 恒返回 `false`，注释 "currently disabled" 指的是被注释掉的旧判断（靠 `vs == null` 区分 WS MV），对现在的路径没有影响。产品仓库历史从 2024 年初始提交开始，查不到这段注释的来由。
2. **`mv.outer.moveUp` 的规则（`SelectionUpTransformer.java:1671`-`:1712`）。** 左连接且选择在子表右表、或右连接且选择在子表左表时，`true` 把选择上移到父表（与早期说明一致：if two tables use left outer join and selection in child right table, true will move selection up to parent; right outer join and selection in child left table, the same），`false` 报 `outerJoin` 并拒绝；左连接右表没有选择（右连接同理）时不受影响，直接放行；默认 `false`（`defaults.properties:127`）。上移时条件被改写成"原条件 OR 第一个条件列 IS NULL"（`addOuterCondition0`，`:1841`），用来保住外连接补出的 NULL 行。按这个写法推断，两种情况与不上移的结果不同：左表行在右表有匹配但都不满足条件时，不上移保留该行（右侧为 NULL），上移后该行被删掉；右表条件列本身为 NULL 的行，不上移被过滤，上移后留下。**第一种已实测（2026-11-17，T10 (a)）**：`SubMV_Others_Spec` 的 `left outer join-select right` 和 `right outer join-select left`，建 MV 前设 `mv.outer.moveUp=true`、`-Dmv.compare=both` 运行，MV 由两个子 MV 变成连接后整表的顶层 MV（`Query1|TopMV`、`Query2|TopMV`）并命中，结果比不用 MV 少行：左连接 `bk1`（只选 `USA West`）少了 `[1][Games][NULL][NULL]`（`Games` 匹配的是 `USA East`），8 行变 7 行；右连接 `bk1`、`bk2` 少了左侧为 NULL 的客户行，36 行变 30 行、32 行。开关为默认 `false` 时两个用例 MV 与不用 MV 一致。第二种在这两份数据里没有条件列为 NULL 的行，没有观察到。只有第一个条件项的列加了 `IS NULL`。同类开关 `mv.union.moveUp` 有过结果错误的记录（Redmine #6808），Redmine 里没有 `outer.moveUp` 的记录。属性名大小写不是问题：`PropertiesEngine.java:582` 把属性名统一转成小写，`mv.outer.moveUp` 与 `mv.outer.moveup` 读到同一个值。
3. **关联 MV 的命中在测试侧能记录，不用改产品代码。** 关联 MV 不经过组件查询，而是在 `MVTableMetaData` 里用：构造时按 `ASSOCIATION_` 加表名找 MV，存进私有字段 `meta_rmv`，用上时打 INFO "Use metadata MV for association processing"（`MVTableMetaData.java:94`），列缺失或查询失败时退回完整 MV 并打 WARN "Association MV failed, reverting back to full MV"（`:112`）。两种情况本来就不用：建元数据时表本身或子表上有运行时条件（`:76`-`:86`，`AssetUtil.containsRuntimeCondition`，12.1 起的行为）；`mv.table.metadata=false`（`ViewsheetSandbox.java:8580`）。`MVTest.recordMVLookupHit` 走 `MVManager.findRuntimeMV`，有基表 MV 时返回基表 MV（`MVManager.java:177`-`:184`），所以看不到 `ASSOCIATION_*`。做法（已在 T10 (c) 实现）：每次导出之后读 sandbox 的元数据缓存（`ViewsheetSandbox` 的私有字段 `metarep.metamap`），其中每个 `MVTableMetaData` 的 `meta_rmv` 不为空就记 `ASSOCIATION_<表>|TopMV`（Groovy 用 `.@` 读私有字段）。**不能用公开方法 `getTableMetaData(名)`**：缓存里没有时它会新建元数据，新建时找得到关联 MV 就造成假命中（试过，`CalcField_Spec.Public_Calendar` 因此多记了一个 `ASSOCIATION_Query2`）。代价是依赖两个私有字段名；可以请开发加一个公开 getter，不是前提。另外，产品对每张建了 MV 的表都建关联 MV（`VSMVAnalyzer.java:244`，不看表上有没有选择组件），所以没有选择组件的表的关联 MV 只是被定义，没有任何地方读它。
4. **`RankingConditionTest`（`@Disabled("Ignite causing problems")`）：Ignite 已不是问题，挡住它的是测试配置和一个死锁，补上后 3 个测试通过。** 它是产品仓库里唯一用 `@SreeHome(materialize = ...)` 真正物化 MV 的单测，没有同类测试可对照。
   - **注解的来历**：`@Disabled` 和上面一行注释 `// ignite error: CacheInvalidStateException: Failed to execute the cache operation (all partition owners have left the grid, partition data has been lost)` 在 StyleBI 初始提交（`0d2cdd433a`，2024-07-12，Jason Shobe）里就有，之后 Distributed Sessions（`1b6ae84fbc`，2026-05）和 #77104（2026-09）都改过这个文件，都没去掉。13.x 仓库（`E:\style-intelligence`，`mv-local/src/test/java/inetsoft/mv/RankingConditionTest.java`）里这个类没有 `@Disabled`，全部历史里也搜不到这两段文字，所以是移植到 StyleBI 时加的，没有 bug 编号。Redmine 搜 `RankingConditionTest` 只有 #36737（2019，13.2 时测试没用上 MV，已关闭），搜这条 Ignite 错误只有 #73617（会话缓存，与此无关）。
   - **现在的测试环境里没有 Ignite**：`BaseTestConfiguration` 的 `Cluster` 是 `MockCluster`，注释里的错误按构造不会再出现。
   - **实际运行（2026-11-17，T10 (b)）**：用 `-Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition` 让 `@Disabled` 失效（不改注解），`./mvnw test -pl core -Dtest=inetsoft.mv.RankingConditionTest`。依次遇到：(1) `SreeHomeExtension.beforeAll`（`:160`）`NoSuchBeanDefinitionException: MVSupportService`；(2) 补上后，`MVSupportService.AnalysisResult.waitFor()` 必然超时 10 秒——线程 dump 显示是死锁：`waitFor` 是 `synchronized`，拿着 `AnalysisResult` 的锁用 Awaitility 轮询，而 Awaitility 在另一个线程调用同样 `synchronized` 的 `isCompleted()`，永远拿不到锁。初始提交里的 `waitFor` 用 `future.get()`，没有这个问题，是 Distributed Sessions 改成 Awaitility 时引入的；产品代码里除了测试的 `SreeHomeExtension` 没有别处调用 `waitFor`；(3) 之后建 MV 时还缺 `MVWorksheetStorage`、`MVStorage`、`BlockFileStorage` 三个 bean。这几个类都是 Distributed Sessions 起改为从 Spring 取实例（`getInstance()` 调 `getSpringBean`），构造参数只要 `BlobStorageManager`，测试配置一直没跟上。
   - **临时补上之后**：在 `IntegrationTestConfiguration` 里加 `MVSupportService`、`MVWorksheetStorage`、`MVStorage`、`BlockFileStorage` 四个 `@Bean`，并去掉 `waitFor` 的 `synchronized`，3 个测试方法（`testOneGroupTopN`、`testTwoGroupsOuterTopN`、`testTwoGroupsInnerTopN`）全部通过，连跑 3 次都通过；测试断言了 `MVQuery.getMVInfos` 非空，即确实命中 MV。日志里有 3 条嵌入数据临时文件 `NoSuchFileException`（`EmbeddedData\...\0.tmp`），不影响结果。改动已全部撤掉，产品仓库没有留下修改。
   - **结论**：可以重新启用。需要的改动是上面四个测试 bean 加 `waitFor` 的死锁修复（后者是产品代码，虽然只有测试调用），交给开发决定（§6 开头的问题列表）。
5. **T10 (c) 的结果（datatest PR #228）。** `MVTest.recordAssociationMVHits` 读元数据缓存（第 3 条）。78 个用例的 `getMVDefInfo` 检查不变，观察到命中的 `ASSOCIATION_` 名字（取自原定义列表）加进 `getMVHitInfo` 检查：47 个用例全部加上，24 个加了一部分，7 个一个也没加。没加的名字每个写一行注释说明原因：28 个用例里是表上没有选择组件（关联 MV 只被定义，没人读；运行后缓存里也没有这张表的元数据），3 个（`TestCase_Census`、`chart_namegroup2`、`calcfield on crosstab`）是表上有选择、产品按设计不用关联 MV。没有发现退回完整 MV 的情况。验证：涉及的 10 个 spec 本机运行，只有 §2 已列的 6 个失败（others 5、`Other_TestCase17`），其中 `assembly_query` 失败在 `compareData`，新检查通过；反向检查：临时把 `mv.table.metadata` 设为 `false` 跑 `AssociationMV_Spec`，加了 `ASSOCIATION_` 命中检查的 29 个用例全部在这条检查上失败，没加的用例不受影响。`MVTest` 的改动作用于所有 mv 用例，6 个 profile 也都整套本机运行了一次（2026-11-17）：topmv 130 个执行 / 1 失败、submv 86 / 0、incrementalmv 172 / 3、wsmv 1 / 0、vpm 14 / 4、others 223 / 5；记录关联 MV 时没有任何报错。失败里 12 个是 §2 已列的（vpm 4、incrementalmv 3、others 5）；新出现 1 个 `TopMV_AggregateFormulas_Spec.aggregate formula2`（`compareData`，`Query1` 第 2 行两列期望 NULL、实际 0），在不含 PR #228 的 `Stateless-Sessions1` 上连跑两次也同样失败，与这次改动无关。原因（2026-11-17 查明）：两列是样本方差、样本标准差，该行分组只有一个值；产品 Bug #80342（stylebi PR #8194，`3f22680c31`，11-16 合入）把单值分组的样本方差和标准差统一为 0，与内存计算的 `VarianceFormula` 一致，MV 的 `CompositeVarianceFormula` 从 `cnt == 0` 改成 `cnt <= 1` 返回 0。用 MV 和不用 MV 结果相同（`compareWithoutMV` 通过），是有意的行为变化，golden 已按新行为更新（`admin_Query1__MV.txt`、`admin_Query1_bk2__MV.txt` 第 2 行两列 NULL 改为 0），`TopMV_AggregateFormulas_Spec` 30 个用例本机全部通过。

6. **T4 的结果（`NOT Transform` 的 11 个 viewsheet，2026-11-18，datatest PR #232）。** 逐个运行（建 MV 前后各执行一次，`-Dmv.compare=both`），并直接调用 `VSMVAnalyzer.analyze()` 读它的说明（`getInfo()`）：
   - **7 个按预期被拒绝，进了新 spec `NotTransform_Spec`：** `embedded-embedded table`、`embedded-input assembly`（嵌入表，`containsBoundEmbeddedTable`）、`group variable`（命名分组引用变量）、`subquery1`（作子查询的表自己有条件，`VSMVAnalyzer.java:190`）、`v-ranking1`、`v-ranking2`（带参数的排名条件，列不可见）、`crosstab ws`（交叉表，只在分析说明里记 fault "The block 'All Sales1' is a crosstab table"，没有警告）。每个用例断言没有定义和命中 MV、分析器给出的原因（`MVTest.getAnalysisWarnings()`，新加，读 `MaterializedViewResource` 记下的分析警告；`crosstab ws` 断言没有警告）、`compareData` 和 `compareWithoutMV`。断言原因是因为分析器抛异常时同样不建 MV（见下面的 `cube`），只断言"没有 MV"分不出来。
   - **`adhocfilter_measure`：分析器现在接受它**（图表加度量上的范围滑块），建 MV 并命中 `query|TopMV`，结果与不用 MV 一致；目录名已过时，用例固定这个现状。
   - **`cube` 不进 spec：** 图表绑定 `___inetsoft_cube_db2_1/l1` 在测试环境里解析不出来，两个聚合的 `getDataRef()` 都是 null，用不用 MV 图表都只导出 `[[NULL]]`。分析器在 `VSMVAnalyzer.analyze`（`:194`）→ `checkCombinable`（`:1134`、`:1148`）→ `MVDef.getAggregateInfo`（`:1372`）→ `createAggregateInfo`（`:1448`）抛 NullPointerException：`VSAggregateRef.createAggregateRef` 在列为空时返回 null（`VSAggregateRef.java:380`），`MVDef` 没有检查。这一步在 `isAcceptable` 的 cube 规则（`:701`）之前，所以 cube 规则在 mv 测试里走不到。绑定无效的 viewsheet 让整个分析抛异常（不是只跳过这个组件），是否算产品问题待开发判断；要测 cube 规则需要一个在测试环境里能解析的 cube 绑定。
   - **`vs assembly-field condition` 不进 spec：** 数据源 `testparameter` 不在 `mvStorage.zip` 里，表格导出 `(1 x 0)`；分析器给出 "Comparison with a field is not supported"，同一条规则已由 `VSCondtion_Spec.field`（#44378）覆盖。
   - **`subquery`：资产有错，已改成 one of，进了 spec。** 原来 `RETURNS2`（按 `ORDER_ID` 分组）的聚合后条件是 `Sum(QUANTITY) > 子查询 RETURNS1.QUANTITY`，建 MV 并命中（`ORDERS1`、`RETURNS1`、`RETURNS2`、`ASSOCIATION_RETURNS1`），但 `Query2` 用 MV 70 行、不用 MV 64 行（多出的 6 行 `Sum(QUANTITY)` 为 2 到 5）。原因：子查询返回 249 行，`>` 只取第一行（`Condition.java:667`-`:680`，`values.get(0)`）；子查询表被设成 `distinct` 后执行（`AssetConditionGroup.java:86`-`:100`），行顺序没有定义。实测不用 MV 时在 Oracle 上执行 `select distinct ORDER_ID, PRODUCT_ID, QUANTITY`，第一行 `[12005, 24, 5]`，阈值 5；用 MV 时同一子查询走 `RETURNS1` 的 MV，第一行 `[12005, 3, 1]`，阈值 1。两边的聚合都在内存里做（查询计划 "having clause will be post processed"），差别只在子查询的行顺序，即使不用 MV 结果也取决于数据库。用户判断是用例的问题：`>` 后面应是一个值，多个值应该用 one of（2026-11-18）。该 worksheet 只有这个 viewsheet 用（zip 里查过，spec 里也没有别处引用），所以直接改了 `mvStorage.zip` 里 `SUBQUERY/subquery` 的这个条件（`operation` 4 改 2，blob 和 `blob-index.json` 的 digest 一起换，其他条目不变）。改后建 MV 并命中，三个组件用 MV 与不用 MV 逐字节相同；`Query2` 23 行，`Sum(QUANTITY)` 都在子查询的取值集合（1 到 20）里，其中不超过 5 的 8 组、等于 1 的 2 组，与原来 `> 1`（69 组）、`> 5`（63 组）、共 71 个订单的结果推算一致。
   - **验证：** `NotTransform_Spec` 9 个用例在 `golden` 和 `both` 下都通过；6 个 profile 整套运行两次（`subquery` 用例加入前一次；改 zip 后在 PR 最终代码上一次，others 执行数 223 加 9），都只有 §2 已列的 12 个失败。改后的 `mvStorage.zip` 与 base 逐条目比较：只有 `SUBQUERY/subquery` 的 blob（1 个字节：`operation="4"` 改 `"2"`）和 `blob-index.json` 里它的一条不同。基线是当前输出；没走 MV 的用例两条路径相同，所以另做了一个独立核对：`crosstab ws` 全部单元格之和 17656854 等于 `group variable` 的 Others 合计（同一张表、同一个度量）。反向检查（临时 spec，未提交）：用同样的断言跑 `cube`，`warned` 失败（警告是空指针）；跑 `adhocfilter_measure`，`getMVDefInfo().isEmpty()`、`getMVHitInfo().isEmpty()` 失败；把 `group variable` 基线改一个数，`compareData` 失败。拒绝规则本身是产品代码，不改产品就关不掉，所以没有做"关掉规则"的反向检查。

7. **T3 的结果（本机 12 个失败用例，2026-11-18）。** 先在产品 `631443a906` 上逐个分析，再把产品重建到 `main` `6b936637cf` 运行后更新。12 个都是产品已关闭的修复让 golden 过期，能对比的用例用 MV 与不用 MV 的输出相同，没有发现新的产品问题。共 20 个 golden 文件内容有变化（其余导出只差换行，没有动），每个都按对应的修复核对（脚本逐格比较，不是直接复制）：
   - **vpm 4 个、`running total`：#79508**（stylebi PR #7616、#8162，Closed）：postgres 的 `time with time zone` 按库里存的时间显示，不再按 +08:00 换算。直接查 `postgres_server`：`order_time` 存的是 `06:01:01+07:36:36` 这样的值；vpm 11 个文件里每一行的 `order_time` 都等于库里存的时间（共 2877 行，其中 2805 格有变化），只有这一列变；`running total` 只有一格小时 `00` 变 `23`，与 #79508 的验证记录一致。spec 注释原说不用 MV 时会换算、所以不做 `compareWithoutMV`，这个理由已不成立，vpm 4 个用例补了不用 MV 的运行和 `compareWithoutMV`，`both` 下通过。
   - **`C_V_5`、`expression`：#79870**："不等于"按 SQL 丢掉空值行。只删行、不加行（252、20 行，与 #79870 验证备注一致），删掉的行在条件列上都是 NULL；注释和 `@Issue` 从 #79505 改为 #79870。
   - **`selection association4`：#81084**（stylebi PR #8015）：自由表的 `Count` 没有数据时为 0。只有 15 格从 NULL 变 0，其他值不变。
   - **`assembly_query`：#81182**（stylebi PR #8055）：分母为 0 的 Correlation 是 NULL。书签 `bk1` 只选 `dis = 0.15`，各组 DISCOUNT 是常数，方差为 0，只有原来的一格 0 变 NULL。
   - **`Delete_Spec.Testcase8`：#79870**（stylebi PR #7756）：建 MV 时只保留"NOT(删除条件)为真"的行，删除条件 `Year(orderdate)` 等于 2002 或 2001 时，年份为 NULL 的行也不在 MV 里。只少了这一行。
   - **`Append_Spec.mvLastUpdatetime`：#80953 与 #81043**：追加条件的公式 `CALC.edate(..., -12*(年-2003)-(月-10))` 算下来总是 2003-11-01，旧的 `edate` 把负的月份偏移都算成月底（2003-11-30），#80953（stylebi PR #7968）修好；删除条件 `orderdate <= MV.LastUpdateTime` 在更新时也删掉日期为 NULL 的 3 行（#81043，stylebi PR #8005：比较类删除条件更新时与建 MV 一样删 NULL 行）。更新后的 7 行正好是初始数据里 2003-11-01 及以后的行。
   - **`Transform_Spec.Other_TestCase17`：#81043**：删除条件 `customer_id < MV.MaxValue-30` 不匹配任何客户，更新时只删掉 `customer_id` 为 NULL 的一行（orderno 7）；追加条件在更新前后都没有加出行（`orderno` 1 到 20 各一行，没有重复），不影响结果。
   - 删除条件也删掉 NULL 行是开发在 #80570、#81043 里定的规则，用户 2026-11-18 接受；原计划"增量三个用例改相对日期"不需要（不是日期漂移）。
   - **验证：** 6 个 profile 整套用 `both` 运行，每个 profile 前清空 `mv/target`、还原 `docker/src/test/resources/H2Data` 的 `*.mv.db`：topmv 130 个执行 / 1 失败、submv 86 / 3、incrementalmv 197 / 0、wsmv 1 / 0、vpm 14 / 0、others 232 / 0；失败的 4 个只在 `compareWithoutMV` 上失败（G14），golden 都通过。另外注意：不清空 `mv/target` 连跑时，`TopMV_Subquery_Spec.Subquery_Mode3_9`、`Subquery_Mode3_10` 的书签选择没有生效而失败（`SelectState` 8 对 9），清空后通过，是上一次运行留下的存储造成的。

## 7. 已知 bug 对照（步骤 3b-D）

**样本与口径（先读这个）：**

- **取数：** Redmine 的 `Product` 项目，按标题含关键词取（`subject ~`，所有状态），用 `redmine_fetch.py` 翻页取到服务端总数：`MV` 1599 条、`materializ` 190 条、`incremental` 57 条，取回的数量和服务端 `total_count` 一致；三份合并去重后 1766 条，其中 Bug 类 1712 条（2015 年至今）。这是候选池，不是"MV bug"的结论：标题含关键词不等于是 MV 的问题。
- **范围：** 只看近一年，创建时间不早于 2025-11-13，共 **165 条 Bug**。不看 spark mv（产品已不支持，这一年里标题也没有出现 spark）。更早的 1547 条不在本节范围。
- **过滤：** 先去掉 38 条噪声，剩 **127 条**：AI 插件和 Wiz 工具类 23 条（分类为 AI Plugin、AI Wiz）、EM 对话框和界面细节 6 条、标题里的 MV 不是这个功能的 9 条（存储升级、H2 文件名、日期格式化、页分隔等）。
- **分类：** 按标题关键词分根因类别（`.claude/workflow-shared/datatest-gap/mv_bug_classify.py`，第一条命中的规则生效），**没有读全部描述**。**每类抽样 7 条（不足 7 条的取全部）读了描述核对，共 81 条：81 条都和 MV 有关，按调整后的规则有 71 条分在了正确的类里（约 88%），10 条分错了**：#73865（EM 里 MV 资产的选中状态，不是选择组件）、#79402、#79387（分到性能，实际是哈希表扩容的正确性问题、清理范围）、#76971（日期列写缓冲区溢出，根因是并行写）、#78331（脚本条件被超时中断）、#78340（WS MV 没被使用）、#73220、#79265（创建或调度时的异常）、#75659（交叉表数据变了）、#77901（取数错误被吞）。**抽样是在调整规则之前抽的**：评审后把日期规则收窄、给 VPM 规则加了 `site admin`，只影响抽样里的 #78059（改到 VPM）和 #79400（改到性能），这两条改后都分对了；调整后没有重新抽样。**各类的数量只能当大致数字。**
- **覆盖对照：** "被提到"指 bug 编号出现在 `mv/src/test/groovy` 的 spec 里，或出现在产品仓库的测试里（`community/core`、`community/server`、`enterprise` 的 `src/test/java`）。**被提到不等于断言了这个行为**，没有读测试内容；测试里没写编号的 bug 看不出来。

| 根因类别（标题关键词归类） | bug 数 | 未关闭 | datatest 提到 | 产品单测提到 | 都没有 | 抽样核对 |
|---|---|---|---|---|---|---|
| 创建与更新失败（构建、cycle、后台任务） | 24 | 1 | 1 | 6 | 17 | 6/7 |
| VPM、权限与跨租户 | 26 | 5 | 0 | 12 | 14 | 8/8 |
| 并发、集群与文件 | 12 | 0 | 0 | 6 | 6 | 7/7 |
| 使用 MV 时的异常（NPE、越界、报错） | 9 | 1 | 0 | 3 | 6 | 4/7 |
| 增量更新与删除 | 9 | 2 | 1 | 6 | 3 | 7/7 |
| 日期、类型与时区 | 6 | 3 | 1 | 6 | 0 | 4/5 |
| 性能与内存 | 9 | 0 | 0 | 3 | 6 | 6/8 |
| 滑块、选择与书签 | 7 | 3 | 5 | 3 | 2 | 6/7 |
| 空值与 NOT 条件（MV 与实时结果不同） | 6 | 3 | 4 | 6 | 0 | 6/6 |
| MV 没被使用或读不到数据 | 6 | 1 | 1 | 3 | 2 | 5/6 |
| 公共 API（REST） | 5 | 3 | 0 | 1 | 4 | 5/5 |
| 其他结果与 MV 不一致（分组、join、子查询、聚合） | 4 | 1 | 1 | 2 | 2 | 3/4 |
| MV 代码缺陷（配置项、桩、从不调用） | 4 | 0 | 0 | 0 | 4 | 4/4 |
| 合计 | 127 | 23 | 14 | 57 | 66 | 71/81 |

**读法：**

- **datatest 能碰到的是"结果和不用 MV 不一致"那几类**：空值与 NOT 条件、滑块与选择、增量更新、日期与类型、其他结果不一致，再加"MV 没被使用或读不到数据"（结果和实时不同），这六类共 38 条，datatest 提到了其中 13 条，产品单测提到 26 条，两边都没提到编号的有 9 条（增量 3、滑块 2、其他结果 2、没被使用 2）。
- **datatest 本来就不该管的**：并发与文件、性能与内存、VPM 与跨租户、公共 API、代码缺陷、使用时的异常、创建与更新失败（后者里的集群和 cycle 放在 `test/cluster`），这七类共 89 条。66 条没被提到编号的 bug 里，9 条在上一类、57 条在这一类（9 + 57 = 66），后者可以由产品单测或集群用例覆盖，不属于 datatest 的缺口；它们是否真的有测试，看不出来。
- 这一年的 bug 里，**根因最集中的是 MV 路径与实时路径在空值、NOT 条件、滑块取值上的语义不同**（上表的头几类），而 `others` 里 5 个本机失败的用例也落在这一带（见 §2），说明这类问题还在持续出现。

**未关闭的 23 条**（状态不是 Closed、Rejected、Ignored，所以包含 Resolved；状态按 2026-11-13）。"dt/pr"表示被 datatest/产品单测提到，`--` 表示没有：

| bug | 状态 | dt | pr | 类别 | 标题 |
|---|---|---|---|---|---|
| #79508 | Closed（11-17） | dt | pr | 日期、类型与时区 | postgres `time with time zone` 按小时分组，MV 与实时的分组不同 |
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

## 8. 具体要做（按顺序；T1、T2、T5 已做（PR #226，T2 的 CI 还没触发），T9 已做（PR #229），T10 的 (a)(b)(c) 都已做、剩下两个问题等开发答复，T4 已做（PR #232），T3 已做（golden 已更新，CI 未触发），其余未开始）

| # | 要做什么 | 在哪里改 | 做完怎么验证 | 前提 / 注意 |
|---|---|---|---|---|
| T1 | 给 78 个只断言已定义的用例补基表 MV 的命中检查（G1 第一步）：保留 `getMVDefInfo().containsAll([...])`，从同一个列表取出不带 `ASSOCIATION_` 的名字，加 `getMVHitInfo().containsAll([...])`；脚本生成，期望值取自原列表；`NotPublic_Text` 不动；列表里全是 `ASSOCIATION_` 的用例单独列出 | `AssociationMV_Spec`、`CalcField_Spec`、`Transform_Spec`、`MVHit_*`、`VSCondtion_Spec`、`SubMV_Others_Spec`、`TopMV_*` | 跑涉及的 spec；加了之后失败的用例单独列出，不删检查；加一次反向检查：把某个基表 MV 名改错，用例应当失败 | **已做（2026-11-16，datatest PR #226）**：78 个用例都加了命中检查；10 个 spec 本机运行，新检查只让 `CalcField_Spec` 的 `NotPublic_SelectionTree`、`NotPublic_Calendar`、`NotPublic_RangeSlider` 失败：已定义 `query1|SubMV`，命中记录只有 `query1|TopMV`。这三个 viewsheet 只有选择组件，命中靠查找记录，看不到查询时才选的子 MV（与 `NotPublic_Text` 同一原因），改为只检查 `query1|TopMV` 并加注释；这三个用例也就是反向检查：已定义但没观察到命中的 MV 会让新检查失败，而原来的 `getMVDefInfo` 检查照样通过。其余失败是 §2 已列的 8 个（others 5、`Other_TestCase17`），新检查在这些用例里都通过 |
| T2 | 把 `wsmv` profile 加进 `test-mv.yml`（G6） | `.github/workflows/test-mv.yml`，照 `vpm` 一步的写法 | workflow 手动触发一次，`WSMV_Path_Spec` 出现在报告里 | **已做（2026-11-16，datatest PR #226）**：在 incrementalmv 之后加了一步 wsmv，写法与其他 profile 相同；本机 `-Pwsmv` 1 个用例通过；CI 还没有触发。该 spec 自己建表、结束时删表，做法与 CI 里已在跑的 `IncrementalDataChange_Spec` 相同 |
| T3 | 本机 12 个失败用例：先在 CI 上确认，再按各 bug 的状态逐个更新 golden（核对新输出，不直接复制）；增量三个用例的日期条件改相对日期 | `MV_Creator`、`vs query and condition`、`hit mv`、`vpm` 的基线；`Append_Spec`、`Delete_Spec`、`Transform_Spec` | 各 profile 在干净的 `mv/target` 下 0 失败；`both` 下 `nomv` 比较仍通过 | **已做（2026-11-18）**：12 个用例的 20 个 golden 文件逐个按对应的修复核对后更新，用例加了注释和 `@Issue`；vpm 4 个加了 `compareWithoutMV`；增量三个不改相对日期（不是日期漂移）。结果和验证见 §6 第 7 条；CI 未触发 |
| T4 | 用 `NOT Transform` 的 11 个现成 viewsheet 建反向路径 spec（G2） | `mv/src/test/groovy/.../cases/others/NotTransform_Spec`（others profile） | 每个用例：不命中、结果与不用 MV 一致；反向：把对应的拒绝规则关掉时用例失败（做不到就说明） | **已做（2026-11-18，datatest PR #232，`others` profile 的 `NotTransform_Spec`，9 个用例）**：11 个 viewsheet 逐个运行并读分析器结果，见 §6 第 6 条；`subquery` 的资产有错（`>` 比较多行子查询），改成 one of；`cube`、`vs assembly-field condition` 不进 spec |
| T5 | `#80458`、`#80446` 对应的 `@Ignore` 用例去掉 `@Ignore`（G9） | `MVHit_RankingAndFilters_Spec.chart-time type`、`SubMV_Others_Spec.top-aggregate-maxrow` | 本机运行；基线换成"不用 MV 的结果"，`both` 下通过 | **已做（2026-11-16，datatest PR #226）**：两个 bug 都已 Closed（11-16 人工验证）。两个用例都去掉了 `@Ignore`，`chart-time type` 加了 `executeVSWithoutMV` 和 `compareWithoutMV`。在 `both` 模式下 MV 与不用 MV 的输出逐字节相同；`chart-time type` 换了 2 个滑块基线（最小值改为空时间那一行），与 #80458 验证记录里不用 MV 的结果一致，`Chart1_bk1` 只是去掉末尾一个空行；`top-aggregate-maxrow` 的 Query1 基线不变（本来就是不用 MV 的结果），补了 3 个选择列表基线。之后在默认 golden 模式下两个用例都通过 |
| T6 | `mv.*` 配置项的几个会改变结果的场景（G4）：`mv.preferred.block`（#79869）、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`、`date.common.options` | 复用现有 viewsheet，`@SreeProperty` 设置属性 | 改值后命中且结果与不用 MV 一致 | `MVTest.clearEnv` 要补复位；`mv.outer.moveUp` 的期望值等 T10 (a)：差异有意则用"右表匹配都满足或都不满足条件"的数据断言一致，另加一个用例固定已知差异；是 bug 则先提 bug |
| T7 | 从 13.8 移植单测：`MvStorageEngineFeatureTest` 的 `DictionaryPart`（`XDimDictionary`）、`DataPart`、`Built`（`MVQueryBuilder`、`SubMVQuery`）；夹具移植后再移 `ConfigMvBehaviorS2Test`、`MvManageCustomerP2Test`（见 §4） | 产品仓库 `core/src/test/java/inetsoft/mv/`，新分支 `test-*` | 单独运行通过，撤掉对应产品代码后失败（逐个撤掉对应产品代码，确认测试会失败） | `ReportEnv` 要换成 `SreeEnv`；`FsPart` 的 4 个 fault 用例、依赖 `DUtil` 等四个类的 mapreduce 用例不能移植；`DashboardHarness` 等夹具 StyleBI 里没有，先问要不要移植夹具 |
| T8 | 新写单测：`SelectionUpTransformer` 的 fault、`VSMVAnalyzer.isAcceptable`、`MVTransformer.isCombinable`、`MVDef.canHitMV`、`WSMV*` 转换器（G3、G6） | 产品仓库 | 同上 | 夹具办法没确认；WS MV 的规则已从代码确认（§6 第 1 条）；`mv.outer.moveUp` 的触发条件和改写方式已确认（§6 第 2 条），结果差异是否有意等 T10 (a) |
| T9 | 增量更新与全量重建的一致性（G5）。**原计划"给 `Append_Spec`、`Delete_Spec` 的代表性用例加 `compareWithRebuild`"不可行（2026-11-17 读代码确认）**，改为：先对照 `Append_Spec`、`Delete_Spec` 用到的条件类型，列出 `IncrementalDataChange_Spec` 还没覆盖的，再在该 spec 里按它的写法（建表、插入/删除行、增量更新、`rebuildMV` + `compareWithRebuild`）补用例 | `IncrementalDataChange_Spec` | 新用例增量结果与全量重建一致；反向：故意让两边不同时检查失败 | **第一批已做（2026-11-17，datatest PR #229）**：对照方法是从 `mvStorage.zip` 逐条提取 `MV_Creator/Append`（72 个 worksheet、157 条 MV 条件）和 `MV_Creator/Delete`（37 个、55 条）的条件，按位置（追加/删除 × 聚合前/聚合后）、表类型、值的种类归类。该 spec 没覆盖的有五类：聚合后条件（追加 17、删除 16 条）、组合表上的条件（Mirror、Join、Concatenated、Rotated）、常量条件（约 180 条）、字段比较和子查询值（各 1 条）、`MV.MaxValue`/`MinValue` 用在字符串、浮点、日期时间列（18 条）。第一批补了前三类，共 4 个用例：普通表上的常量追加 `customer_id >= 105` 和常量删除 `customer_id in (102, 106)`；按客户分组的表上同样的两个条件作为聚合后条件；`M_Testcase2` 的 Join 表（两张子表都改绑到用例的表）上同样的两个条件；以及下面这个行为的用例。前三个都在增量更新后和全量重建对比，该 spec 20 个用例本机全部通过。**发现的行为**：同一次更新里新取到、同时满足追加条件和删除条件的行，会被追加并保留，下一次更新才删除；全量重建则直接排除它。原因是 `LocalMVIncremental.update0`（`:67`-`:228`）先执行删除、再执行追加，追加查询不带"NOT 删除条件"，全量建 MV 时则带（`MVCreatorUtil.mergeConditionList`）。三条路径（普通表、聚合后条件、Join 表）表现相同，第一轮运行时三个用例都因此在 `compareWithRebuild` 上失败，相当于反向检查。按设计如此处理（2026-11-17 用户决定）：前三个用例的数据避开这种重叠，另用一个用例固定现状（第一次更新保留、第二次更新删除，之后与重建一致）。**第二批已做（2026-11-17，datatest PR #229）**：后两类，共 9 个用例。`MV.MaxValue`/`MV.MinValue` 一个参数化用例 7 个迭代（整数 `< MinValue`；浮点、字符串、日期时间各 `> MaxValue` 和 `< MinValue`；原型是 `maxValue_*`、`minValue_*` 资源，原用例用 `=`，数据不变时会重复追加同样的行），新值超出 MV 范围的被追加、在范围内的不追加；字段比较 `customer_id = orderno`（原型 `S_TestCase28`）；子查询 `orderno in MVCustomers1.customer_id`（原型 `S_TestCase29`，相关子查询，`MVCustomers` 表的编号是 1 到 20）。初始数据都避开追加条件，使条件只取新行。全部和重建对比，该 spec 30 个用例本机全部通过；反向检查：去掉"重建前从数据库删掉未追加的行"，9 个新用例全部在 `compareWithRebuild` 上失败，其他用例不受影响。字段比较和子查询这两类条件和常量条件一样，数据不变时每次更新都会重复取到同样的行，所以只能在数据变化的场景里和重建对比。**第三批已做（2026-11-17，datatest PR #229）**：补齐前两批只做了一部分的两类，共 10 个用例。(1) 删除条件用 `MV.MaxValue`/`MV.MinValue`：一个参数化用例 8 个迭代，沿用 `MV_Creator/Delete` 的 `maxValue_*`、`minValue_*` 资源原有的删除条件 `= MV.MaxValue`/`= MV.MinValue`（整数、浮点、字符串、日期时间），加追加条件 `> MV.MaxValue`；更新先算出 MV 的值再删，所以删掉 MV 当前的最大或最小值并追加新值。(2) 组合表：Mirror（`M_Testcase1`，按年分组、`Sum(orderno)`，条件挂在 Mirror 上，新数据只在新年份）和 Minus（`M_Testcase6`，左表绑用例的表、右表绑它的一个视图 `quantity >= 100`；资源里两张底表各有一个普通过滤条件 `orderno < X`，要去掉）。Union（`M_Testcase5`）和 Rotated（`M_Testcase9`、`M_Testcase22`）不补：它们的 MV 建在子表上（`Append_Spec` 断言命中的是子表的 SubMV），组合表自己的 MV 条件不会被用到，不存在这条增量路径。Mirror 用例没有删除条件：分组 Mirror 上对非分组列 `orderno` 的删除条件，建 MV 时和增量更新时都不生效（试过，2020 年的和仍包含被"删除"的行，增量与重建一致），因为 MV 里只有分组后的数据，没有 `orderno` 可删。该 spec 41 个用例（按迭代计）本机全部通过；反向检查：去掉重建前的数据库删除，10 个新用例全部在 `compareWithRebuild` 上失败。incrementalmv profile 整套运行 197 个执行（原 172），只有 §2 已列的 3 个失败。至此 T9 完成：Append、Delete 用到的条件类型，凡是在数据变化场景下有增量路径的，都有了和重建对比的用例。**为什么原计划不可行**：`Append_Spec`、`Delete_Spec` 的用例数据库不变，靠 MV 条件让 MV 的数据变化。产品全量建 MV 时**不用追加条件过滤**（只在多节点建 MV 时用它找拆分的日期列，`MVDispatcher.java:370`），不含 `MV.`、`@incrementalOnly` 的删除条件取反后并进表条件、建时就排除（`MVCreatorUtil.mergeConditionList`，`:828`；`isConditionValid`，`:877`）；增量更新把追加条件与表条件 AND 后取行加进已有 MV（`MVIncremental.mergeAppendCondition`）。所以数据不变时：追加取到的行本来就在 MV 里，更新后变成两份，例如 `MV_Creator/Append/S_TestCase1` 的 golden 更新前 21 行、更新后 22 行，`[17][true][TX][Austin][FISGA Corp]` 出现两次，重建只有一次，对比必然失败（这是用例设计的期望结果，不是 bug）；不含 `MV.` 的删除条件建时已排除，更新删不到行，对比必然一致但没有检查作用；含 `MV.` 的追加条件取不到新行，同样是空检查。`IncrementalDataChange_Spec` 已覆盖：`> MV.MaxValue`、`> MV.LastUpdateTime`、按日期删除、一次更新里同时追加和删除、明细表上的按月汇总（含滚动删除）、按月分组的表删除最新月再追加、无 MV 条件的强制追加、用另一列的 `MV.` 变量、viewsheet 打开时更新后刷新、VPM、增量查询中途失败 |
| T10 | 原来要问开发的三个问题，读代码后只剩一个（§6"读代码确认的结论"）。(a) 实测 `mv.outer.moveUp`；(b) 本机临时去掉 `RankingConditionTest` 的 `@Disabled` 运行一次（不提交）；(c) 关联 MV 命中检查（G1 第二步） | (a) datatest 本机；(b) 产品仓库本机；(c) `commons` 的 `MVTest.groovy` 与 78 个用例 | (a) **已做（2026-11-17）**：差异实测出现，待开发答复是已知限制还是 bug（§6 第 2 条）；(b) **已做（2026-11-17）**：测试环境已无 Ignite；挡住它的是测试配置缺 4 个 bean 和 `waitFor` 的死锁，本机临时补上后 3 个测试连跑 3 次都通过，待开发决定是否重新启用（§6 第 4 条）；(c) **已做（2026-11-17，datatest PR #228）**：结果和验证见 §6 第 5 条 | (c) 依赖产品的两个私有字段；改了 `commons` 测试代码要重建 tests jar |
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
