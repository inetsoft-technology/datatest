# mv 测试缺口与补充方案

> 日期：2026-11-13　模块：`mv`，profile `-Ptopmv`、`-Psubmv`、`-Pincrementalmv`、`-Pwsmv`、`-Pvpm`、`-Pothers`，20 个 spec（之后新增 `TopMV_RuntimeAssets_Spec`、`NotTransform_Spec`、`MVProperties_Spec`）
> datatest 基线：`Stateless-Sessions1`（`ca972e26`）；产品源码：`main`（`28cf7947b9`，`2.1.0-SNAPSHOT`，与 datatest `pom.xml` 一致）
> 13.8 对照：本机 `E:\style-intelligence`，分支 `epic-test-coverage`（`6b0ba0e7f2`），见 §4 末尾

## 1. 结论

mv 的验证机制是各模块里最严的：导出文件数必须等于基线文件数并逐文件比对（`MVTest.groovy:798`-`836`），导出异常和 MV 创建失败都让用例失败，另有"命中了哪个 MV"的断言和"与不用 MV 的结果比较"（后者 CI 不执行，见 §2）。最近一次整套运行（2026-11-23，`-Dmv.compare=both`）只有 1 个失败：`CalcField_Spec.Aggregate_ForDate`，是 #81668 的有意行为变化，基线已按新行为更新（T15）。

**已做和已决定的**（本文不重复）：命中断言（datatest PR #188）、MV 开/关对比（`-Dmv.compare`，PR #193）、增量与全量重建对比（PR #194、#195、#229）、WS MV 的一条通路（PR #200）、golden 更新（PR #196、#197，T3）、`@Ignore` 整理与启用（PR #198、#226，T12）、`others` 每个 spec 单独 JVM（PR #199）、关联 MV 命中检查（PR #226、#228）、反向路径 `NotTransform_Spec`（PR #232）、产品单测（stylebi PR #8473）、cycle 在 `test/cluster` 验证（enterprise PR #1604、#1701，未核对）。**已决定不做**（维护者说明，仓库里没有书面记录）：datatest 各 workflow 的定时运行不恢复、`mvStorage.zip` 不清理、`where:` 数据表不改写、`@Retry` 不改、CI 只做 golden 比较。

仍然存在的缺口：`mv.*` 配置项只覆盖了 5 个（G4，T6）；`meta=true` 的单测在 stylebi PR #8746（T19，G13）；`CalcFieldTransformer` 没有单测（在 `MVTransformer.transform` 里被注释掉，G3）；`getMVHitInfo` 在组件都是纯值时退回"已定义"（G1）。

先做：手动触发一次 `test-mv.yml`，确认 wsmv 一步（T2）和 T3 更新后的 golden 在 CI 上的结果；组件都是纯值时 `getMVHitInfo` 退回"已定义"（T18）。

## 2. 覆盖情况

**规模**（2026-11-13 统计，不含之后 T4、T6、T9、T12、T14 新增和启用的用例）：20 个 spec、622 个用例（按 `def` 统计，其中 10 个 `@Ignore`；surefire 执行 615 个、跳过 11 个，差异来自参数化展开，`IncrementalDataChange_Spec` 12 个方法展开为 16 个）、3323 个基线文件、`mvStorage.zip`（`mvStorage-new.zip` 未跟踪，不在范围）里 804 个 viewsheet、414 个 worksheet、360 个书签。用例引用的 viewsheet 资产 id 610 个，609 个在 zip 里（另 1 个是 `${path}` 模板，由 `WSMV_Path_Spec` 运行时创建）。**195 个 viewsheet 没有被任何 spec 按字面量引用**（`hit mv` 110、`CalcMV` 38、`NOT Transform` 11、`MV_Creator` 7 等），其中 12 个的末段名字出现在 spec 文本里（可能是间接引用），1 个有基线目录（`CalcField/NotPublic_Brush1`），其余 182 个既无引用也无基线；非字面量的引用静态看不到，195 里可能含假孤儿。

**覆盖矩阵**（维度：MV 类型 × 命中的查询形态 × 数据源，已与用户确认；数字来自 `mv_inv.py` / `mv_tests.py` 对 spec 的正则统计，用例按 `def '名'()` 切分，命中断言按 `getMVHitInfo()` / `getMVDefInfo()` 区分；2026-11-13 统计）：

| profile | spec | 可执行用例 | `@Ignore` | 基线文件 | 断言命中 | 只断言已定义 | 两者都没有 | `compareWithoutMV` |
|---|---|---|---|---|---|---|---|---|
| topmv | 5 | 127 | 3 | 740 | 122 | 5 | 0 | 127 |
| submv | 2 | 85 | 1 | 729 | 83 | 2 | 0 | 83 |
| incrementalmv | 4 | 168 | 0 | `MV_Creator` 189 + `MV_Transform` 130 | 150 | 4 | 14 | 0（`IncrementalDataChange` 另有 11 处 `rebuildMV`） |
| wsmv | 1 | 1 | 0 | 含在 `MV_Creator` | 0 | 0 | 1 | 1（用探针行判断读的是 MV） |
| vpm | 1 | 14 | 0 | 39 | 14 | 0 | 0 | 10 |
| others | 7 | 217 | 6 | 1495（`hit mv` 807、`CalcField` 268、`AssociationMV` 261、`vs query and condition` 96、`tabular source` 63） | 145 | 68 | 4 | 188 |
| 合计 | 20 | 612 | 10 | 3323 | 514 | 79 | 19 | 408 |

"可执行用例"是按 `def` 数、去掉 `@Ignore` 的数；incrementalmv 的 surefire 执行数是 172（参数化展开）；others 按 `def` 217、surefire 执行 216，差 1 个来自 `MVHit_RankingAndFilters_Spec`，原因没查。"只断言已定义"79 = 78 个含 `ASSOCIATION_` 断言的用例（G1）+ `CalcField_Spec.NotPublic_Text`（带 `TODO`）。

- **MV 类型：** TopMV 在 topmv、others、incrementalmv；SubMV 主要在 submv；关联 MV 在 `AssociationMV_Spec`（37 个用例）；增量 MV 在 incrementalmv（追加、删除、转换）；WS MV 只有 `WSMV_Path_Spec` 一个用例；VPM 在 `VPM_Spec`。
- **查询形态（按 spec 名称，非精确）：** 聚合公式（`TopMV_AggregateFormulas` 30、`MVHit_GroupAndAggregates` 32）、条件（`TopMV_Conditions` 32、`VSCondtion` 42）、排名与过滤（`MVHit_RankingAndFilters` 46）、表结构（`TopMV_TableStructures` 27）、子查询（`TopMV_Subquery` 20）、计算字段（`CalcField` 40）、关联与选择（`AssociationMV` 37）、组件动作（`VSActions` 21）。
- **数据源：** worksheet 绑定以 JDBC 为主（`db2_1` 物理表 177、`mvtest`（H2）200、`oracle1` 51、`Examples/Orders` 23、`access` 19、`postgres` 5、`MySQL` 2、`sql2005` 1，另有模型和 SQL 查询绑定），Tabular 11 个表（Data gov、MongoDB REST、OData、REST JSON、Text Excel Directory）。`db2_1`、`oracle1`、`postgres` 连 CI 的 DB 服务器，`mvtest` 是 H2 文件库。每个用例落在哪个库未统计。`TabularSource_Spec` 整类 `@Ignore`（5 个）。

**验证机制：**

- 比对遍历"实际导出"并要求文件数相等：数量不同直接失败（`MVTest.groovy:816`-`818`），导出目录或基线目录不存在也失败（`:807`-`813`），再逐文件比内容（`:836`）；不存在"导出了但没有基线"的静默情形。
- 导出异常让用例失败：所有书签跑完后汇总抛 `AssertionError`（`:174`-`:179`）；MV 创建、分析失败在 `MaterializedViewResource.java:123`-`:126`、`:180`-`:186` 抛异常，创建失败先于"完成"判断（`:235`）。
- 命中断言（PR #188）：`getMVHitInfo()` 只记录真正读过的 MV；所有组件都是纯值时退回 `getMVDefInfo()`，只代表"已定义"（`MVTest.groovy:455`-`461`，`hitUnobservable` 在 `:327`、`:435`）；图表数据拷成内存快照时同样丢信息（`:432`-`:435`）。关联 MV 的命中从元数据缓存记录（PR #228，§6 第 3、5 条）。
- 与不用 MV 比较（PR #193）：`-Dmv.compare=golden`（默认）只比基线，`nomv`、`both` 才比不用 MV 的结果（`MVTest.groovy:55`、`:856`-`:857`）。**CI 用默认 golden，`compareWithoutMV()` 在 CI 里不执行**（`:857` 直接返回）。基线是回归基线（MV 的输出），不是独立标准答案。
- 导出目录清理：`test-mv.yml` 每个 profile 步骤开头 `rm -rf mv/target`，报告复制到 `mv-reports/<profile>/`。本机连跑多个 profile 前也必须清空 `mv/target`，否则存储互相污染（例如 `TopMV_Subquery_Spec.Subquery_Mode3_9`、`Subquery_Mode3_10` 的书签选择不生效而失败）。`Jenkinsfile` 没有 mv 阶段。H2 库（`docker/src/test/resources/H2Data`）在 `generate-test-resources` 阶段复制到 `mv/target/H2Data`，`data.home` 指向副本（`mv/pom.xml`），运行不改已跟踪的文件。
- CI 触发：`test-mv.yml` 只有 `workflow_dispatch`，定时触发被注释掉（`:4`-`:6`）；某个 profile 失败时 `exit $status`，`!cancelled()` 保证后续 profile 照跑。`wsmv` 一步已加（T2），CI 尚未触发。
- **实际运行结果**（2026-11-23，本机 Windows，产品 `main` 当日构建，`-Dmv.compare=both`，每个 profile 前清空 `mv/target`、还原 `docker/src/test/resources/H2Data` 的 `*.mv.db`；T13）：

| profile | 执行 / 失败 |
|---|---|
| topmv | 130 / 0（跳过 1） |
| submv | 86 / 0 |
| incrementalmv | 197 / 0 |
| wsmv | 1 / 0 |
| vpm | 14 / 0 |
| others | 232 / 1（跳过 5）：`CalcField_Spec.Aggregate_ForDate`，#81668（基线已更新，T15） |

  PNG 不参与（mv 的比对都是文本）。CI 上的结果未确认。

**断言层：** mv 导出表格、选择列表、日历、时间滑块、范围滑块等运行时组件数据（`MVTest.groovy:701`-`:790`），不导出 PNG；不导出组件的统计未做。命中信息记录在 `hitMVs`，不写进基线文件。

**功能面差集（步骤 3b-A）：**

| 能力 | 来源 | datatest 使用情况 |
|---|---|---|
| 不可合并的聚合公式（DistinctCount、Median、Mode、Product、Concat、NthLargest、NthSmallest、NthMostFrequent、PthPercentile） | `AggregateFormula.java:804`、`:1217`、`:1316`、`:2694`、`:2735`、`:2784`、`:2840`、`:2896`、`:2952` | 被引用的 viewsheet 里（`formulaValue`）：DistinctCount 23、Median 11、Mode 4、NthLargest 3、NthMostFrequent 3、PthPercentile 2、NthSmallest 1；**Product、Concat 在 viewsheet 所有节点里都是 0** |
| 其余聚合公式 | `AggregateFormula.java:624`、`:751`、`:1485`-`:2399` | Count 239、Sum 209、Average 34、WeightedAverage 15、First 13、Correlation 12、Max 7、Min 4、Variance 4 等；**SumSQ、SumWT 为 0**；`Last` 只在 1 个文本组件的 `aggregateValue` 里；只统计 viewsheet，worksheet 另见 `ws_scan.py inventory` |
| 条件运算符（MV 过滤支持 ONE_OF、BETWEEN、<、>、=、NULL、STARTING_WITH、CONTAINS、LIKE） | `MVConditionListHandler.java:283`-`:322` | worksheet 前置条件：between 10、contains 6、equal to 34、greater than 14、less than 30、null 3、one of 27、starting with 7、date in 1；**没有 like**；viewsheet 选择条件、MV 更新/删除条件的运算符未统计 |
| MV 配置项（`mv.*`、`ws.mv.enabled`、`date.common.options`、`time.common.options` 等） | `defaults.properties:123`-`:130` | `ws.mv.enabled`（`WSMV_Path_Spec`）；`mv.union.moveUp`（`SelectionUpTransformer.java:1924`）、`mv.outer.moveUp`、`mv.ignore.nestedSelection`（`:1449`，默认 `true`）、`mv.preferred.block`（`MVBuilder.java:964`）、`date.common.options`（`DateMVColumn.java:63`）由 T6 覆盖。其余为 0：`mv.dim.max.size`（`XDimDictionary.java:90`）、`mv.detail.data`（`ViewsheetSandbox.java:1467`）、`mv.cache.association`、`mv.table.metadata`、`mv.ondemand`、`mv.required`、`mv.merge.deleteCond`（`MVCreatorUtil.java:831`）、`mv.freshness`、`mv.maxAge`、`mv.max.block`、`time.common.options`（`DateMVColumn.java:100`） |
| MV 种类 | `MVDef.java:2922`（WS MV）、`:2902`（关联）、`:242`（共享）、`:2494`（`isSharedBy`） | WS MV 1 个用例；共享 MV、用户/角色/组范围的 MV 选择（`MVManager.java:677`-`:730`）只有 `vpm`、`multitenant` 部分涉及（`multitenant` 7 个 viewsheet 只有 1 个被引用） |
| 数据源类型的限制（拒绝嵌入表、cube、查询变量） | `VSMVAnalyzer.java:700`-`:736`、`:927`-`:980` | 嵌入表、命名分组变量：`NotTransform_Spec`（T4）；cube 规则在测试环境走不到（§6 第 6 条）；查询变量只有单测（`VSMVAnalyzerAcceptableTest`） |
| 增量方式（追加、强制追加、删除、`MV.<column>.Max/Min`、`MV.LastUpdateTime`） | `LocalMVIncremental.java:95`-`:150`、`:353` | Append（65）、Delete（37）、IncrementalDataChange 覆盖；增量与旋转/反转/交叉表的组合被分析器拒绝（`VSMVAnalyzer.java:848`-`:858`），无用例 |

**按 viewsheet 组件和选项的扫描：** MV 分析不按组件类型整体切换，按"组件绑定的 worksheet 表"分组（`VSMVAnalyzer.java:640`-`:685`）；组件类型和一批选项决定选择列/关联列、维度/度量、能否建瘦 MV（`shrinkTable`）、是否额外建范围列和日期级别列。画刷、缩放、钻取、书签恢复、刷新没有 MV 专用判断；有 MV 专用处理的是明细数据（`mv.detail.data`，默认关，`VSAQuery.java:1078`-`:1080`）、adhoc 筛选的失败回退（`MVTableMetaData.java:124`-`:187`）、导出时吞掉按需创建的确认（`ViewsheetSandbox.java:1260`-`:1266`）、按需重建。扫描脚本 `.claude/workflow-shared/datatest-gap/mv_vs_scan.py`（已提交），读用户设置的 XML 节点，与 spec 的字面量引用求交；只表示静态存在，被引用不等于断言了这条规则。

| 规则（触发节点） | zip 里的 viewsheet | 被 spec 引用 | profile | 判断 |
|---|---|---|---|---|
| `viewsheetInfo associationEnabled="false"`（不建关联 MV，`TableMetaDataTransformer.java:48`-`:54`） | 0 | — | others | 运行时改 `TestCase_Census`（T11，`MVViewsheetOptions_Spec`） |
| `viewsheetInfo bypass="true"`（建 MV、用 MV 时都不套 VPM，`ViewsheetSandbox.java:249`-`:250`、`MVManager.isVPMCompatible`） | 0 | — | 除 vpm 外全部 | 分析时由请求决定，viewsheet 里的值被覆盖（`MVSupportService.java:1478`）：`createMV(applyVPM)` 传 `bypass=!applyVPM`（`MaterializedViewResource.java:140`），除 `vpm` 外的用例都在 bypass 下建 MV；受 VPM 限制的用户在 bypass 下用 MV：`VPM_Spec` 的 bypass 用例（T11） |
| `viewsheetInfo meta="true"`（`needMV()` 为 false，`ViewsheetSandbox.java:1477`-`:1480`） | **0** | 0 | — | 只在设计态起作用（runtime 下 `needMV()` 恒为 true），datatest 以 viewer 打开，dashboard 用例走不到；由单测覆盖（`ViewsheetSandboxMVEnabledTest`，stylebi PR #8746，T19） |
| `viewsheetInfo full`（false 时只用于选择的表建瘦 MV，`VSMVAnalyzer.shrinkTable`，`:547`） | true 103 | — | 全部 | 分析时由请求决定：`createMV` 总传 `fullData=true`（`MaterializedViewResource.java:142`），viewsheet 里的值不起作用；EM 分析页默认也是 true。full=false：`MVTest.setFullData(false)`，`MVViewsheetOptions_Spec`（T11） |
| `viewsheetInfo mvOnDemand` | true 598、false 206 | 428 / 181 | 各 profile | 有 |
| 选择组件带度量（阻止瘦 MV，`VSMVAnalyzer.java:1204`-`:1220`） | 选择列表 10、选择树 5 | 8、5 | others 12、submv 1 | 有 |
| 选择组件数据源是别的组件 | 时间滑块 5、日历 1 | 4、1 | others | 有（含义未逐项核对） |
| adhoc 选择组件（`MVTableMetaData.java:124`-`:187`） | viewsheet 本身 1；书签状态里 48 个节点、21 个 viewsheet | 书签所在 21 个全部被引用 | others | 有；是否断言命中或回退消息未核对 |
| 时间滑块：单值 / 复合 | 单值 78（日期 11、时间戳 12、时间 2、数值 57，可重叠），复合 14 | 52、12 | others 为主，topmv、submv 各几个 | 有；`rangeType=16` 的日期 1 个没被引用 |
| 时间滑块对数刻度（`RangeSelectionTransformer.java:146`-`:150`） | 1 | 1 | others | 只有 1 个 |
| 日历 `daySelection`、`type`/`mode` | true 3、false 21；只有 type=1 的 mode=1、2 | 2、20 | others 为主，topmv 5、submv 1、vpm 1 | 有 |
| 图表 / 交叉表日期级别（`dlevelRValue`） | 图表 203 个 viewsheet（级别 5 有 179，其他 15 种各 2 至 9）；交叉表 56（级别 5 有 54） | 图表 115、交叉表 42 | 级别 5 各 profile 都有，其他级别几乎全在 others | topmv、submv 里只有级别 5、3、8 |
| 组件百分比（`percentageByValue`；`VSAggregateRef.java:403` 转成 `AggregateRef.setPercentage`） | 1（`hit mv/percent of`：图表、交叉表、自由表，占总计） | 1 | others | 交叉表基线有占小计、占总计，bk1 带选择；图表导出的是原始值，百分比在出图时算，导出验证不到；占分组 0。worksheet 聚合百分比另有 `SubMV_Others_Spec` 的 `child has percentage` 等；`isAggregateCombinable` 的百分比分支有单测（`AbstractTransformerTest`，stylebi PR #8677） |
| 排名（`rankingOptRValue` 9 或 10） | 图表 43、交叉表 3 | 23、3 | others 为主，topmv 1 | 有 |
| 不可合并聚合公式 | DistinctCount 20、Median 12、Mode 6、NthLargest 4、NthMostFrequent 3、NthSmallest 2、PthPercentile 3；Product、Concat 0（运行时改公式补用例，T16） | 10、8、4、3、2、1、2 | 几乎全在 others | topmv、submv 几乎没有 |
| 表达式含 `@nomv` 或 `parameter.`（`MVTool.isExpressionMVCompatible`，`:630`） | 0 | — | topmv | 分析器的拒绝有单测（`VSMVAnalyzerAcceptableTest`）；端到端：运行时加表达式（T11，`TopMV_RuntimeAssets_Spec`） |
| 计算字段含 `parameter.` | 1 | 1 | submv | 只有 1 个 |
| 计算字段引用聚合（拒绝，`VSMVAnalyzer.java:865`-`:904`） | 23 | 21 | others 19、topmv 2 | 有 |
| 列表输入组件绑定表（动态表，`VSMVAnalyzer.java:666`-`:669`、`:816`-`:819`） | 17 | 12 | others | 有；是否断言不命中未核对 |
| 输出组件标量绑定（`MVDef.java:1163`-`:1181`） | 29 | 19 | others | 有 |
| 嵌入表（拒绝，`VSMVAnalyzer.java:712`-`:718`） | 1 | 0 | — | `NOT Transform` 的两个嵌入表 viewsheet 已由 `NotTransform_Spec` 覆盖 |
| Cube 绑定（拒绝，`VSMVAnalyzer.java:701`-`:708`） | 1（`NOT Transform/cube`） | 0 | — | 测试环境走不到 cube 规则（§6 第 6 条） |
| 自由表（无 MV 专用代码） | 66 | 26 | others 25、topmv 1 | 不单独追 |
| `CalcMV/Detail_*`、`CalcMV/crosstab_*`（自由表：明细模式 19、分组汇总 19，基于 `Crosstab/CalcTest2..7`，H2 orders） | 38 | 0 | — | 测试点已被覆盖，不进 spec：明细自由表（`hit mv/freehand3`）、分组汇总与各公式（`hit mv/freehand1..4`、`element binding*`）、viewsheet 计算字段（`CalcField_Spec`）、输入组件到 worksheet 变量条件（`VSCondtion_Spec` 的 `variable_ws_*`、`input5`、`vs query and condition/*`）。2026-11-24 全部以 `both` 运行一次（临时用例，未提交）：37 个命中 MV 且与不用 MV 相同；`crosstab_11` 不用 MV 时就报 `Column not found: CalcField2`，与 MV 无关：两列公式的第二列只用到明细计算字段时，自由表的临时交叉表把它当作没用到删掉（`VSAQuery.removeUnusedCalcField`，`usedInCrosstabInfo` 看的 `getSecondaryColumn()` 这时为 null），疑为 `980291b28d`（#81627 后续）引入，已提 #82231 |

动作（从书签状态看）：选择（1492 个选择列表状态，342 个 viewsheet，其中 337 个被引用）、画刷 `state_brush`（32 个书签，17 个 viewsheet）、缩放 `state_zoom`（31，16）、钻取 `drill_filter`（16，4）、adhoc 筛选（48 个节点，21 个 viewsheet）。没有被任何用例触发的 MV 专用动作：明细数据（`DataMap.DETAIL`；名字含 detail 的 19 个是 `CalcMV/Detail_*`，不是明细动作）、导出路径。静态结论。

**生成型模块补充（步骤 3b-B/C/E）：** 3b-C（分支清单）做了；3b-B 以实际运行代替，没有写探针；3b-E 没做。

分支清单（产品源码；"用例"一列为名字级静态判断）：

| 方法 | 分支 | 有无用例走到 |
|---|---|---|
| `SelectionUpTransformer.java:181`-`:220` | maxRows、排名、聚合排名、交叉表、旋转、反转各返回 fault，选择条件不上移 | maxRows（zip 7 个，引用 6）、旋转（zip 6，引用 2）有，反转 0，其他未验证；单测见 `SelectionUpTransformerFaultTest` |
| `SelectionUpTransformer.java:1447`-`:1470` | 嵌套选择：除非 `mv.ignore.nestedSelection`，返回 fault | 默认 `true`（`defaults.properties:126`），默认不出现；单测见 `SelectionUpTransformerFaultTest` |
| `SelectionUpTransformer.java:1694`-`:1727` | 父表 join：外连接、merge join 返回 fault，`mv.outer.moveUp` 可豁免左右连接单侧条件 | 外连接 11、merge join 2（名字级）；`mv.outer.moveUp` 无 dashboard 用例（§6 第 2 条） |
| `SelectionUpTransformer.java:1729`-`:1778`、`:1880`-`:1955` | union / intersect / minus：需 distinct、列数相同、`mv.union.moveUp`、选择定义一致 | 名字级 16 个；`mv.union.moveUp` 无用例 |
| `VSMVAnalyzer.java:690`-`:750` | 拒绝 cube、嵌入表、查询变量、命名分组变量、命名分组动态条件 | 嵌入表、命名分组变量：`NotTransform_Spec`；其余只有单测 `VSMVAnalyzerAcceptableTest` |
| `VSMVAnalyzer.java:815`-`:907` | 输入动态表、后置聚合计算、maxRows 与子选择、更新条件与旋转/反转/交叉表 | 未验证 |
| `MVTransformer.java:194`-`:252` | 多个 DistinctCount、其他不可合并公式、`NamedRangeRef` 分组不能合并 | 单个公式有用例；多个 DistinctCount 并存未验证；单测 `MVTransformerCombinableTest` |
| `AssetQuery.java:143`-`:228` | 转换失败丢弃 MV 退回实时查询，`mv.required` 时抛错 | 只有命中断言能发现 |
| `MVManager.java:165`-`:236` | 失败的 MV 视为不存在；按需创建；无 MATERIALIZATION ACCESS 返回 null | datatest 无；单测 `MVManagerFindRuntimeMVTest` |
| `MVManager.java:677`-`:730`、`:865`-`:908` | 先用户后组后角色；VPM 计划不等不可用 | `vpm`、`multitenant`（后者几乎没被引用） |
| `LocalMVCreator.java:70`-`:95` | 全量 / 增量选择 | 增量追加、强制追加有用例 |
| `MVQueryBuilder.java:153`-`:239` | 明细 / 聚合查询；列缺失抛 "Column not found in mv" | datatest 未验证；单测在 `MvStorageEngineTest` |
| `MVAssetQuery.java:683`-`:722` | 维度溢出（`mv.dim.max.size`）只给警告 | datatest 无 |

`CalcFieldTransformer` 在 `MVTransformer.transform` 里被注释掉（`:491`-`:500`）；`TransformationDescriptor.isWsMVNotAllowed` 恒为 false（`:589`-`:600`），注释已过时（§6 第 1 条）。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 78 个用例只断言关联 MV "已定义"（`AssociationMV_Spec` 32、`CalcField_Spec` 24、其余 22），即 PR #188 里没换的含 `ASSOCIATION_` 的断言；`getMVHitInfo` 在组件都是纯值时退回"已定义"（`MVTest.groovy:455`-`:461`） | 关联 MV 没被命中、悄悄退回实时库，结果相同，用例照样通过 | **已做**：基表 MV 命中检查（T1，PR #226）；关联 MV 命中检查（T10 (c)，PR #228，§6 第 5 条）。仍存在：纯值组件退回"已定义"（T18） | 中 |
| G2 | 反向路径（不该走 MV）几乎没有用例：只有 2 个用例断言命中为空（`VSCondtion_Spec`、`SubMV_Others_Spec`） | 分析器错误地对 cube、嵌入表、查询变量、命名分组变量的表建或命中 MV | **已做**：`NotTransform_Spec` 9 个用例（T4，§6 第 6 条）；单测 `VSMVAnalyzerAcceptableTest`（T8）。cube 规则在测试环境仍走不到 | 高 |
| G3 | 转换层单测：`SelectionUpTransformer` 原只有 3 个测试，`MVTransformer`、`VSMVAnalyzer` 判断逻辑几乎没有；13.8 也没有，不能移植 | 选择上移、聚合下移、接受/拒绝规则出错，只有碰巧被端到端用例触发才发现 | **已做（T8，stylebi PR #8473）**：`SelectionUpTransformer` fault、`VSMVAnalyzer` 接受/拒绝、`MVTransformer.isCombinable`、`MVDef.canHitMV`/`isValidMV`、`WSMVTransformer`；`AbstractTransformer`、`NamedGroupTransformer`（stylebi PR #8677）。仍没有直接测试：`CalcFieldTransformer`（被注释掉，没有调用） | 高 |
| G4 | `mv.*` 配置项几乎空白：只有 `ws.mv.enabled` 被设置 | 改了默认值才出现的错误，例如 #79869（`mv.preferred.block` 非默认时追加的行丢失）；`mv.union.moveUp` 关闭时的 union 选择上移；`date.common.options` 改变物化级别后没物化级别的查询 | **已做（T6）**：`MVProperties_Spec`、`IncrementalDataChange_Spec` 覆盖 `mv.outer.moveUp`、`mv.ignore.nestedSelection`、`date.common.options`、`mv.union.moveUp`、`mv.preferred.block`；`mv.ondemand`、`mv.required`、`mv.metadata`、`mv.dispatcher.*`、溢出已有单测（T7） | 中 |
| G5 | 增量更新：Append、Delete、Transform 共 156 个用例是 golden + 命中，不与全量重建比较（数据库不变，本来就不该等于重建，见 T9） | 数据真正变化时增量结果错误；#80313、#80344 复发 | **已做（T9，PR #229）**：`IncrementalDataChange_Spec` 补到 41 个（按迭代计）与重建对比；T14 补 #79854、#80570/#81043。#80313、#80344（New）等修复后补 | 中 |
| G6 | WS MV 只有 `WSMV_Path_Spec` 1 个用例（PR #200 有意只走一条通路；"是否使用 MV"由产品 `AssetQueryWorksheetMVTest` 25 个单测覆盖） | WS MV 的创建、读取在 CI 里没有保护；选择下移、聚合下移出错 | `wsmv` 进 `test-mv.yml` 已做（T2，CI 未触发）；`WSMVTransformerTest` 已做（覆盖两个 `WSMV*DownTransformer`）；`WSMVAnalyzerTest` 已做（stylebi PR #8677） | 高 |
| G7 | 不可合并公式：Product、Concat 在 viewsheet 里 0 个；其余只验证结果与基线一致，不验证退回"读明细在内存聚合"的路径（`AssetQuery.java:175`-`:203`） | 不可合并公式被当成可合并在 MV 上直接合并，基线是 MV 输出，发现不了 | **已做**：单测（`MVTransformerCombinableTest`，T8）；dashboard 的 Product、Concat（T16）。整数列的 Concat 用 MV 时值带 `.0`，见 T16 | 中 |
| G8 | golden 跟不上产品修复 | 失败常态化后真实回归被淹没；基线固定旧行为 | **已做（T3）**：12 个用例的 golden 按 #79508、#79870、#81084、#81182、#80953、#81043 核对后更新（§6 第 7 条）；CI 未确认 | 中 |
| G9 | 被忽略的用例：`TabularSource_Spec` 整类 5 个（继续 `@Ignore`，维护者自己重做）；`date range and number range` 按设计不可能通过（`TopMV_Others_Spec.groovy:401`-`:407`） | Tabular 数据源的 MV 创建与命中没有回归保护（#78339 已修） | 已启用：`chart-time type`、`top-aggregate-maxrow`（T5）；`composite3`、`variable in subquery`、`rangeslider-compositevalue`（T12） | 低 |
| G10 | `test-mv.yml` 只能手动触发，`Jenkinsfile` 没有 mv（维护者决定不恢复定时）；本机整套约 20 分钟（topmv 2 分 43 秒、incrementalmv 2 分 58 秒、others 7 分 31 秒） | 回归在下一次手动触发前不被发现 | 不改 | 低 |
| G11 | 195 个 viewsheet 没被字面量引用；1 个 viewsheet 有基线没被引用；`@Ignore` 只剩 `TabularSource_Spec`（5 个）和 `date range and number range`，它们的基线不被导出 | 准备好的场景没被运行；陈旧基线让人误以为被验证过 | 先核对真孤儿还是间接引用，再删除或补用例（T17，未开始；`CalcMV` 38 个已核对，测试点已被覆盖，见 §2） | 低 |
| G12 | 按需创建、MV 过期、数据周期、集群：datatest 有意不覆盖，cycle 放在 `test/cluster`（CY-02/03/04，enterprise PR #1604、#1701）；`MVRule`、`MVManager` 已有单测（22、35 个） | MV 过期判断出错 | `mv.ondemand` 按需创建（`MVManagerFindRuntimeMVTest`，T7）；`isStale`、`isExpired` 引起的重建（stylebi PR #8677）。过期 MV 对没有 MATERIALIZATION ACCESS 的用户：照常使用、不触发重建（#82140 修复后）。只设 `mv.maxAge` 时超龄 MV 仍被使用、不重建，单测固定现状，待开发确认（#82142） | 低 |
| G13 | 零覆盖项：`associationEnabled=false`；`full=false`（datatest 分析时总传 `full=true`）；受 VPM 限制的用户在 bypass 下用 MV（datatest 除 `vpm` 外都以 bypass 建 MV，但用户不受 VPM 限制）；`@nomv`、`parameter.` 表达式端到端；`meta=true` 只在设计态起作用；Product、Concat 0（G7） | `associationEnabled=false` 仍建关联 MV；瘦 MV 缺列；bypass 的 MV 套了 VPM 或用户看不到全部行（#78390、#77666 同类）；设计态 `meta` 用了 MV；`@nomv` 表达式被建进 MV、参数变了结果不变 | **已做**：dashboard 四组（T11）；`meta` 单测（T19，stylebi PR #8746）。组件百分比、`CalcMV` 已核对，有覆盖（§2）；Product、Concat 见 T16 | 中 |
| G14 | 用 MV 与不用 MV 的差异在默认 golden 下不会被发现。`both` 下发现 4 个用例不一致，都是不用 MV 的一侧不对：`SubMV_Spec.aggbase`、`crossjoin`、`rangecolumn` 是 Average 下推 DB2 不加 CAST 被截成整数（#76921，开发决定不修；#50746），`aggbase` 的 covariance 另有浮点误差 `-0`（不提 bug）；`TopMV_AggregateFormulas_Spec.datelevel_none` 是 `CoreTool.getTimeData`（`:2265`）保留了日期（#81682，已修 `b3fa4de4aa`）。同类已知差异：`formula.avg.keep.type`（#54404） | — | **已处理**：前三个去掉 `compareWithoutMV` 并注释 #76921；`datelevel_none` 在新产品上 `both` 通过（T13）；golden 都不变 | 中 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测；问题出在组件、绑定、数据源之间的接线 → dashboard；验证机制本身的缺陷 → 改基类，不是加用例。

## 4. Java 单测补充

产品仓库已有的 MV 单测：`core/src/test/java/inetsoft/mv` 下 87 个文件，另 10 个在其他包（均在 `core`）。较强的是聚合公式（`inetsoft.mv.formula` 15 个类里 14 个有测试，接口 `MergeableFormula` 除外）、`data` 包（`BitDimIndex`、`SubMV`、`XDimDictionary`、`MVQuery`）、`MVDef`、`MVManager`、`MVBuilder`；Mockito 为主，没有显式搭嵌入数据库。同名类按完整路径区分：运行时 `GroupedTableBlock.getMVFormula` 把 `DistinctCount`、`Median`、`Mode` 路由到 `inetsoft.mv.formula`，`NthMostFrequent` 路由到 `inetsoft.report.filter`（`GroupedTableBlock.java:123`-`:136`）。

**T7、T8 已做（stylebi PR #8473，2026-11-19 合并，`612dfd4843`）**，只加测试、不改产品代码；每个新类都把覆盖的产品代码逐处改坏过，每处至少让一个用例失败；`inetsoft.mv` 下全部测试加两个 web 类一个 JVM 788 个用例 0 失败，每个类单独运行也通过。

| 测试类 | 用例 | 内容 | 对应 |
|---|---|---|---|
| `MvStorageEngineTest` | 46 | 13.8 `MvStorageEngineFeatureTest` 移植：字典 23、块数据 13、查询与存储 10（含 `MVQueryBuilder`、`SubMVQuery`、溢出）。未移植 7 个：4 个 `XFileFaults`（StyleBI 没有这些类）、3 个多节点用例（StyleBI 的 MV 文件系统是单节点） | T7 |
| `MVManagerFindRuntimeMVTest` | 18 | `MVManager.findRuntimeMV`：`mv.required`、`mv.metadata`、`mv.ondemand`（视图选项、属性、MATERIALIZATION 权限、失败后不重试、临时视图）；关联子 MV 当物理 MV 用（#62332）；取消等待中的任务 | T7 |
| `MVDispatcherSplitTest` | 8 | `MVDispatcher.processDispatch`：`mv.dispatcher.count`、`mv.dispatcher.number.maximum`、`mv.dispatcher.interval` | T7 |
| `ComposerViewsheetServiceCheckMVTest` | 5 | `checkMV`：取消等待中的 MV 后查看者（`mv.required`）和设计者（`mv.metadata`）的行为 | T7 |
| `MVSettingsServiceTest` | 5 | MV 设置页四个选项、默认 cycle、有权限的 cycle 列表；`mv.enabled.all` 是新视图按需选项的默认值 | T7 |
| `VSMVAnalyzerAssociationTest` | 7 | `isAssociationCompatible`：mirror 的计算字段不计列数（#53263），只比列数 | T7 |
| `MVTransformerCombinableTest` | 20 | `AggregateFormula.isCombinable`、`MVTransformer.isCombinable` | T8 |
| `MVDefHitTest` | 14 | `MVDef.isValidMV`、`canHitMV` | T8 |
| `VSMVAnalyzerAcceptableTest` | 23 | `isAcceptable`、`isSuitableTable` 拒绝的表和原因 | T8 |
| `SelectionUpTransformerFaultTest` | 28 | 不能上移的情况和 fault，以及能上移的情况 | T8 |
| `WSMVTransformerTest` | 21 | WS MV 的条件下移、聚合下移，哪些留在上层 | T8 |
| `RankingConditionTest`（重新启用） | 3 | 见 §6 第 4 条；不建 MV 时 3 个都失败 | T10 (b) |

T7 的 `ConfigMvBehaviorS2Test`、`MvManageCustomerP2Test` 没有移植 13.8 夹具，用 mock 改写成上表的单测；`MvManageCustomerP2Test` 端到端的分析、建 MV、打开视图没有移植。PR 描述列了被固定、看起来不像有意的行为：设计时 `mv.metadata` 抛 `MessageException`（#80878，Rejected）；`isAssociationCompatible` 只比列数；命名分组变量借用"物理查询带变量"的 fault 文字；`@nomv`、`parameter.` 的计算字段被当成输入组件的目标表拒绝（#48975），`isSuitableTable` 的后置计算分支走不到；`SelectionUpTransformer` 对交叉表子表检查两次。

**stylebi PR #8677（2026-11-23 合并，`bdd266f58f`）** 只加测试：`MVManagerFindRuntimeMVTest` 加 `isStale`、`isExpired` 引起的重建（后台重建时照用旧 MV、新鲜不重建、设计者不重建、不重复重建、超过 `mv.maxAge` 时等重建完成；现在共 26 个）；新类 `WSMVAnalyzerTest`（8）、`trans.AbstractTransformerTest`（25，条件是否动态、能否合并、子表列查找、排名）、`trans.NamedGroupTransformerTest`（10，聚合下移、改名加 mirror、命名分组明细列）。被固定、交给开发确认的行为：只设 `mv.maxAge` 时超龄 MV 仍被使用、不重建；`WSMVAnalyzer` 分析交叉表时改掉传入 worksheet 里表的聚合和条件（#82142）。过期 MV 对没有 MATERIALIZATION ACCESS 的用户返回 null 已作为 bug 修复（#82140，`6c96ee6865`），测试改为照常使用、不重建。`NamedGroupTransformer` 把日期分组改为按结果列的改写在单测里总被 `resetColumnSelection` 撤销，没写单测；18080 上端到端核对过改名分支结果与不用 MV 一致。

**stylebi PR #8746（2026-11-24 开，未合并）** 只加测试（T19）：`ViewsheetSandboxMVEnabledTest`（4，`isMVEnabled`：设计态加 meta 不用 MV、runtime 不受影响、`setMVDisabled`、明细数据要 `mv.detail.data`）；`MVTableMetaDataTest`（5，从完整 MV 读选择列表的值、关联值、含 `/` 的树选择值 #52677、按值聚合的度量）；`SharedMVUtilTest`（16，哪些 MV 定义共用一个 MV：接管已存的或被接管、WS 和 VS 的 MV 分开、重新分析后移出、共享提示、`checkMVValid`、`removeMV`）；`MVScriptableMultiBlockTest`（3，多块时维度列的 `MV.MaxValue`/`MinValue`，移植自 13.8 `QueryAdvancedFeatureTest.mvUpdateVariablesInSql`）。每个类都对改坏的产品方法验证过会失败。固定了两个看起来不是有意的行为：#82223（`SharedMVUtil.shareCreatedMV` 合并后删除的是保留的那个 MV 的集群文件）、#82219（某一块的维度列有 NULL 时 `MV.MinValue` 为 NULL）。

仍没有直接测试的类（没有以类名命名的测试文件，`inetsoft/mv` 下测试也 `grep -w` 不到）：

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `CalcFieldTransformer` | 不补：在 `MVTransformer.transform` 里被注释掉，没有调用 | — | — | 否 |
| `DateMVColumn`（493 行）、`RangeMVColumn`（414 行）、`MVDecimalColumn`、`MVUIntColumn`、`MVRangeIntColumn`、`AbstractMeasureColumn`、`DimValueList`、`MVRow` | 未列入设计（列类型边界已被 #79649、#76971、#77154 等修复时的测试局部覆盖，未核对完整性） | — | — | — |

### 13.8 里已有的 MV 单测（可移植的和不可移植的）

来源：`E:\style-intelligence` 分支 `epic-test-coverage`（`6b0ba0e7f2`），`java/src/test/java` 下 2420 个文件（`E:\inetsoft\sr13_8` 不是 git 仓库、没有 MV 测试）。主要读了方法名、类声明和 import，没有编译运行。移植障碍：13.8 有、StyleBI 没有的类（`XFileFaults`/`OverflowFault`、`mv.adm.DUtil`、`DefaultConnectionPool`、`XJobDispatcher`、`DirectBlockFile`、`inetsoft.report.ReportEnv`）；13.8 夹具（`DashboardHarness`、`OrdersDashboard`、`H2Sources`、`AdminRealServiceFixture`、`ReportHarness`、`SmtpSink`、`ParsedSql`）StyleBI 没有。

| 13.8 的测试 | 内容 | 现状 |
|---|---|---|
| `MvStorageEngineFeatureTest`（53） | `XDimDictionary`、`MVQueryBuilder`、`SubMVQuery`、`mv.fs` | 已移植 46 个（`MvStorageEngineTest`） |
| `MvMapreduceFeatureTest`（29） | 块系统、任务派发/超时/取消/重试、`MVReduceTask`、`SubMVTask`、`mv.comm` 缓冲区、数据节点 | 未移植；依赖 `DUtil` 等四个类的用例不能移植；优先级低（稳定的基础设施，bug 主要是并发和集群） |
| `ConfigMvBehaviorS2Test`（6） | `mv.metadata`、`mv.ondemand`、`mv.dispatcher.*`、`mv.required`、`mv.enabled.all` | 改写成单测（T7） |
| `MvManageCustomerP2Test`（2） | 关联 MV：#53263、#62332 | 改写成单测（T7） |
| `QueryAdvancedFeatureTest.mvUpdateVariablesInSql` | `MVConditionListHandler` 在 MV 更新里处理变量，`mv.preferred.block`=2 | 多块的 `MV.MaxValue`/`MinValue` 已移植（`MVScriptableMultiBlockTest`，stylebi PR #8746）；追加条件合并、`MV.LastUpdateTime` 已在 `MVIncrementalTest` |
| `ActionsMvMVTest`（10） | `MVAction`、`SharedMVUtil.shareCreatedMV` | `SharedMVUtil` 部分已补（`SharedMVUtilTest`，stylebi PR #8746）；`MVAction` 不移植 |
| `customer/mv/` 其他客户场景 | 创建、读取、更新、调度、数据隔离、VPM 隐藏列 | 不移植，由 datatest 覆盖 |
| `MvGridFeatureTest`（3） | 集群状态 | 不属于 datatest 和 core |

**13.8 里没有的**（按完整单词搜 0 命中）：`TransformationFault`、`SelectionUpTransformer`、`WSMVAnalyzer`、`WSMVTransformer`、两个 `WSMV*DownTransformer`、`AggregateDownTransformer`、`SelectionDownTransformer`、`NamedGroupTransformer`、`CalcFieldTransformer`、`canHitMV`、`isValidMV`、`MVRule`，以及 `mv.outer.moveUp`、`mv.union.moveUp`、`mv.ignore.nestedSelection`、`mv.dim.max.size`、`mv.detail.data`、`date.common.options`、`mv.table.metadata`；`MVTransformer`、`VSMVAnalyzer`、`isCombinable` 只出现在 `MvManageCustomerP2Test` 和升级测试里。转换层和分析器两条线上都要新写。13.8 的 `mv.formula` 公式类没有单独单测，StyleBI 更全。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 关联 MV 命中（G1） | 基表 MV 命中检查（T1）；`MVTest` 读元数据缓存记录关联 MV 命中（T10 (c)） | 基表 MV、关联 MV 真的被命中 | **已做**（PR #226、#228）；子表有运行时条件时本来就不用关联 MV，不能断言命中 |
| 反向路径（G2） | `NotTransform_Spec` | 不该走 MV 的表不走 MV，结果仍正确 | **已做（T4）** |
| `mv.*` 配置项（G4） | 用例里设属性：外连接、嵌套选择、图表用例复用现有 viewsheet；union 运行时复制资产；`mv.preferred.block` 在 `IncrementalDataChange_Spec` | 改值后命中变化，结果与不用 MV 一致或差异被固定 | **已做（T6）**；`mv.outer.moveUp`、`mv.union.moveUp` 改值后结果与不用 MV 不同的情况按已知限制固定，见 §8 |
| 增量一致性（G5） | `IncrementalDataChange_Spec` 补数据变化场景，`rebuildMV` + `compareWithRebuild`；不给 `Append_Spec`、`Delete_Spec` 现有用例加重建对比（T9） | 增量结果与全量重建一致 | **已做（T9、T14）**；#80313、#80344 等修复后补 |
| WS MV 进 CI（G6） | `wsmv` profile 加进 `test-mv.yml` | `WSMV_Path_Spec` 在 CI 里运行 | **已做（T2）**，CI 未触发 |
| 不可合并公式（G7） | 运行时复制资产，把 worksheet 镜像表和图表的聚合改成 Product、Concat | 命中符合预期；每组的 Product 等于 Concat 列出的值之积；结果与不用 MV 一致 | **已做（T16）** |
| 重新启用被忽略的用例（G9） | 去掉 `@Ignore` / `@PendingFeature`，基线取不用 MV 的结果 | 已修的 bug 被固定 | **已做（T5、T12）**；Tabular 5 个由维护者重做 |
| golden 更新（G8） | 按各 bug 核对后更新 | 失败不再常态化 | **已做（T3）** |
| 零覆盖项（G13） | 运行时改 `viewsheetInfo`（`TestCase_Census` 在仓库里改、cleanup 还原，复制会丢书签）、运行时复制资产加表达式（不改 zip）；`MVTest.setFullData`；bypass 加在 `VPM_Spec` | 见 T11 | **已做（T11）** |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- 用例和资产的交叉统计用自写脚本（`mv_inv.py`、`mv_tests.py`、`mv_zip.py`、`mv_files.py`，未提交），都是正则静态结果；用例按 `def '名'()` 切分，没有逐个读用例内容。`vs_backup_scan.py` 只用了 `load`、`load_bookmarks` 和 `inventory` 的组件计数，`crosscheck`、`strictcheck` 不适用（mv 的 spec 不是 `ViewsheetTest`）。
- 静态推断：孤儿 viewsheet 数（195）、关键词匹配的"名字级"数量（只表示名字含这个词）；`rangeType` 各数字的含义没有核对。
- 产品能力清单与分支来自代理读取源码，`file:line` 大部分用 `grep -n` 核对；没有检查 Ignite、`SubMVTask` 内部、`MVBuilder` 分块规则。属性名统一转成小写（`PropertiesEngine.java:582`），`mv.outer.moveUp` 与 `mv.outer.moveup` 读到同一个值。
- §7 只看近一年（2025-11-13 起）的 165 条 Bug，分类按标题，抽样约 88% 分对，各类数量是大致数字；"提到编号"只搜文件内容，没有读测试内容。
- 13.8 对照只查了 `epic-test-coverage` 分支，没有编译运行；`E:\style-intelligence` 有一个未提交的 `java/build.gradle` 改动，不是本次所改。
- T14 的用例除 #79854 的模拟反向检查外，都没有在修复前的产品上运行过，只在当前产品上通过。
- **需要开发回答的问题：** #82210（`mv.union.moveUp=true` 时只在一侧选值、另一侧的行全部丢失，已知限制还是 bug，T6 (5)）。其余都已有答复：`mv.outer.moveUp=true` 的结果差异是已知限制（#81658，第 2 条）；cube 绑定空指针 #81659 已修（第 6 条）；#81682 已修（G14）；`RankingConditionTest` 不改产品代码即可启用（第 4 条）；可以请开发给 `MVTableMetaData` 的关联 MV 加公开 getter（第 3 条，不是前提）。
- **需要用户决定的事：** T3 更新后的 golden 在 CI 上确认（用户 11-18 决定暂不触发 CI）；整数列的 Concat 用 MV 时值带 `.0`，是否提 bug（T16）。已决定：增量删除条件也删掉 NULL 行（#80570、#81043 的规则，用户 2026-11-18 接受）；同一次更新里同时满足追加和删除条件的行先保留、下次更新删除，按设计处理（用户 2026-11-17 决定，T9）。

### 读代码确认的结论（产品源码 `main` `af5a802550` 起）

1. **WS MV 是现行功能，`isWsMVNotAllowed` 的注释已过时。** `AssetQuery.java:102`-`:114`：`ws.mv.enabled` 打开时先挂底层表的 WS MV（`applyWSRuntimeMV`），再走 `WSMVTransformer.transform`（stylebi PR #6556，Bug #78053）。规则：
   - 只给直接绑查询、不依赖变量的底层表（`BoundTableAssembly`）建 WS MV（`AssetQuery.java:453`-`:458`）。
   - 只用于基于该 worksheet 的 viewsheet 查询（含预览、导出）；worksheet 编辑器、设计模式、建 MV 时不用（`isWSMVEnabled`，`:377`-`:395`，Bug #33943）；viewsheet 关了 MV 时也不用。
   - VS MV 优先：`MVTransformer.findMVTable(table) == null` 时才挂 WS MV。
   - 被连接的表有共同祖先时不做选择下移、聚合下移；底层表自身的条件在 WS MV 上清空，交叉表除外。
   - `isWsMVNotAllowed` 恒返回 `false`，"currently disabled" 指被注释掉的旧判断，对现在的路径无影响。
2. **`mv.outer.moveUp`（`SelectionUpTransformer.java:1671`-`:1712`）。** 左连接且选择在子表右表、或右连接且选择在子表左表时，`true` 把选择上移到父表，`false` 报 `outerJoin` 并拒绝；另一侧没有选择时不受影响；默认 `false`（`defaults.properties:127`）。上移时条件改写成"原条件 OR 第一个条件列 IS NULL"（`addOuterCondition0`，`:1841`），只是近似：外侧行在内侧的匹配都不满足条件时被删掉而不是补 NULL；内侧条件列为 NULL 的行被留下；只有第一个条件项加 `IS NULL`。第一种已实测：`SubMV_Others_Spec` 的 `left outer join-select right`（8 行变 7 行）和 `right outer join-select left`（36 行变 30、32 行），开关为 `false` 时一致；第二种在现有数据里没有观察到。开发答复是已知限制，不改行为（#81658），产品 `main` `5a2c8d4a71` 加了注释。同类开关 `mv.union.moveUp` 有过结果错误记录（#6808）。
3. **关联 MV 的命中在测试侧能记录，不用改产品代码。** 关联 MV 在 `MVTableMetaData` 里用：构造时按 `ASSOCIATION_` 加表名找 MV 存进私有字段 `meta_rmv`，用上时打 INFO "Use metadata MV for association processing"（`MVTableMetaData.java:94`），失败时退回完整 MV 并打 WARN（`:112`）。不用的情况：表或子表上有运行时条件（`:76`-`:86`）；`mv.table.metadata=false`（`ViewsheetSandbox.java:8580`）。`MVManager.findRuntimeMV` 有基表 MV 时返回基表 MV（`MVManager.java:177`-`:184`），所以查找记录看不到 `ASSOCIATION_*`。做法：导出后读 sandbox 私有字段 `metarep.metamap`，`meta_rmv` 不为空就记 `ASSOCIATION_<表>|TopMV`；不能用公开的 `getTableMetaData(名)`（缓存里没有时会新建，造成假命中）。代价是依赖两个私有字段名。产品对每张建了 MV 的表都建关联 MV（`VSMVAnalyzer.java:244`），没有选择组件的表的关联 MV 只被定义、没人读。
4. **`RankingConditionTest` 已重新启用（stylebi PR #8473），不改产品代码。** `@Disabled("Ignite causing problems")` 是移植到 StyleBI 时（初始提交 `0d2cdd433a`）加的，没有 bug 编号，13.x 里没有；测试环境的 `Cluster` 是 `MockCluster`，Ignite 不再是问题。真正的障碍：测试配置缺 `MVSupportService`、`MVWorksheetStorage`、`MVStorage`、`BlockFileStorage` 四个 bean（Distributed Sessions 起改为从 Spring 取实例）；`MVSupportService.AnalysisResult.waitFor()` 是 `synchronized` 且用 Awaitility 轮询同样 `synchronized` 的 `isCompleted()`，必然死锁（Distributed Sessions 引入）。PR 里四个 bean 声明在该测试自己的内部配置，`SreeHomeExtension` 改为自己轮询 `isCompleted()`；`waitFor()` 的死锁仍在产品里（只有测试调用），PR 描述写明了。它是产品仓库唯一用 `@SreeHome(materialize = ...)` 真正物化 MV 的单测。
5. **关联 MV 命中检查（T10 (c)，datatest PR #228）。** 78 个用例的 `getMVDefInfo` 检查不变，观察到命中的 `ASSOCIATION_` 名字加进 `getMVHitInfo` 检查：47 个用例全部加上、24 个加了一部分、7 个一个也没加；没加的名字都有注释：28 个用例是表上没有选择组件，3 个（`TestCase_Census`、`chart_namegroup2`、`calcfield on crosstab`）是产品按设计不用关联 MV。没有发现退回完整 MV 的情况。反向检查：`mv.table.metadata=false` 时加了检查的 29 个 `AssociationMV_Spec` 用例全部失败。`TopMV_AggregateFormulas_Spec.aggregate formula2` 的 golden 按 #80342（stylebi PR #8194，`3f22680c31`，单值分组的样本方差、标准差为 0）更新，MV 与不用 MV 一致。
6. **`NOT Transform` 的 11 个 viewsheet（T4，datatest PR #232）。**
   - 进 `NotTransform_Spec`、按预期被拒绝的 7 个：`embedded-embedded table`、`embedded-input assembly`（嵌入表）、`group variable`（命名分组引用变量）、`subquery1`（作子查询的表自己有条件，`VSMVAnalyzer.java:190`）、`v-ranking1`、`v-ranking2`（带参数的排名条件）、`crosstab ws`（交叉表，只记 fault，没有警告）。断言没有 MV、分析器给出的原因（`MVTest.getAnalysisWarnings()`）、`compareData` 和 `compareWithoutMV`；只断言"没有 MV"分不出分析器抛异常的情况。
   - `adhocfilter_measure`：分析器现在接受它，命中 `query|TopMV`，结果与不用 MV 一致，用例固定这个现状（目录名已过时）。
   - `subquery`：资产原来用 `>` 比较多行子查询（只取第一行，行顺序无定义，结果取决于数据库），用户判断是用例问题（2026-11-18）；已把 `mvStorage.zip` 里 `SUBQUERY/subquery` 的条件改成 one of（`operation` 4 改 2，只动这个 blob 和 `blob-index.json` 的对应条目），改后命中且用 MV 与不用 MV 逐字节相同，进 spec。
   - `cube` 不进 spec：#81659 已修（`f47db94f8b`，不再抛空指针，产品加了 `VSMVAnalyzerCubeBindingTest`）；但 base worksheet 里没有 `___inetsoft_cube_db2_1/l1`，`isAcceptable` 在 cube 规则之前因找不到表返回 false（`VSMVAnalyzer.java:692`-`:699`，只 `LOG.warn`，不记 fault），证明不了 cube 规则。要测需要一个测试环境里能解析的 cube 绑定。
   - `vs assembly-field condition` 不进 spec：数据源 `testparameter` 不在 zip 里；同一规则 "Comparison with a field is not supported" 已由 `VSCondtion_Spec.field`（#44378）覆盖。
   - 9 个用例在 `golden` 和 `both` 下都通过；独立核对：`crosstab ws` 全部单元格之和 17656854 等于 `group variable` 的 Others 合计。拒绝规则是产品代码，没有做"关掉规则"的反向检查。
7. **golden 更新（T3）。** 12 个用例都是产品已关闭的修复让 golden 过期，能对比的用例用 MV 与不用 MV 相同，没有新的产品问题；20 个 golden 文件逐格核对后更新：
   - vpm 4 个、`running total`：#79508（stylebi PR #7616、#8162）：postgres `time with time zone` 按库里存的时间显示；vpm 4 个补了 `compareWithoutMV`。
   - `C_V_5`、`expression`：#79870，"不等于"按 SQL 丢掉空值行（252、20 行）；`@Issue` 改为 #79870。
   - `selection association4`：#81084（stylebi PR #8015），自由表 `Count` 无数据时为 0。
   - `assembly_query`：#81182（stylebi PR #8055），分母为 0 的 Correlation 为 NULL。
   - `Delete_Spec.Testcase8`：#79870（stylebi PR #7756），删除条件下年份为 NULL 的行不在 MV 里。
   - `Append_Spec.mvLastUpdatetime`：#80953（stylebi PR #7968，`edate` 负月份偏移）与 #81043（stylebi PR #8005，比较类删除条件更新时删 NULL 行）。
   - `Transform_Spec.Other_TestCase17`：#81043，更新时删掉 `customer_id` 为 NULL 的一行。
   - 增量三个用例不是日期漂移，不需要改相对日期。

## 7. 已知 bug 对照（步骤 3b-D）

**口径：** Redmine `Product` 项目按标题关键词取（`MV` 1599、`materializ` 190、`incremental` 57 条，合并去重 1766 条，Bug 类 1712 条）；只看 2025-11-13 起的 **165 条 Bug**（不含 spark mv），去掉 38 条噪声（AI Plugin/Wiz 23、EM 界面 6、标题里的 MV 不是这个功能 9）剩 **127 条**。按标题关键词分类（`mv_bug_classify.py`），每类抽样至多 7 条读描述，81 条中 71 条分对（约 88%）；分错的 10 条：#73865、#79402、#79387、#76971、#78331、#78340、#73220、#79265、#75659、#77901。各类数量是大致数字。"被提到"指编号出现在 `mv/src/test/groovy` 或产品仓库 `community/core`、`community/server`、`enterprise` 的 `src/test/java` 里，不等于断言了这个行为。

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

**读法：** datatest 能碰到的是"结果与不用 MV 不一致"的六类（空值与 NOT、滑块与选择、增量、日期与类型、其他结果不一致、没被使用），共 38 条，datatest 提到 13 条、产品单测 26 条、两边都没有 9 条。其余七类（89 条）不属于 datatest 的范围，没被提到的 57 条应由产品单测或集群用例覆盖。根因最集中的是 MV 路径与实时路径在空值、NOT 条件、滑块取值上的语义不同。

**未关闭的 23 条**（统计时状态不是 Closed、Rejected、Ignored；之后已知的关闭日期写在状态里）。"dt/pr"表示被 datatest/产品单测提到：

| bug | 状态 | dt | pr | 类别 | 标题 |
|---|---|---|---|---|---|
| #79508 | Closed（11-17） | dt | pr | 日期、类型与时区 | postgres `time with time zone` 按小时分组，MV 与实时的分组不同 |
| #80446 | Closed（11-16） | dt | pr | 空值与 NOT | 分组表设最大行数，MV 后空值组排在最前 |
| #80458 | Closed（11-16） | dt | pr | 滑块与选择 | 时间/日期/时间戳组合范围滑块取值顺序不同 |
| #80170 | Closed（11-18） | dt | pr | 滑块与选择 | 书签恢复的组合范围滑块少选一个值 |
| #80445 | Closed（11-17） | dt | pr | 滑块与选择 | 同一表 join 两次，选择列表多出行 |
| #80457 | Closed（11-17） | dt | pr | 没被使用 | 带变量的关联子查询，提示不支持且没有数据 |
| #79858 | Closed（11-16） | dt | pr | 日期、类型与时区 | 带时区时间戳改成 TimeInstant 后整列为空 |
| #79870 | Closed（11-16） | dt | pr | 空值与 NOT | 数据库表"不等于"保留了空值行 |
| #80152 | Closed（11-18） | dt | pr | 空值与 NOT | "不包含"变量条件多出行 |
| #80155 | Closed（11-15） | dt | pr | 日期、类型与时区 | Long 变量默认值超过 int 范围不生效 |
| #80096 | Resolved | -- | pr | VPM、权限与跨租户 | 修改组织 id 后组织的 MV 没有数据 |
| #80313 | New | -- | -- | 增量更新与删除 | 增量更新没有新行的第二次更新后 viewer 打不开 |
| #80344 | New | -- | -- | 增量更新与删除 | `MV.<列>.Max` 追加条件增量更新后越界（`IncrementalDataChange_Spec` 有 `$(MV.customer_id.Max)` 用例部分覆盖） |
| #80342 | Closed（11-17） | -- | pr | 其他结果不一致 | DB2 方差和标准差单行分组为 0（golden 已按修复更新，§6 第 5 条） |
| #73812 | Resolved | -- | -- | 使用 MV 时的异常 | 匿名用户 NP |
| #78452 | Resolved | -- | -- | 创建与更新失败 | `ws.mv.enabled=true` 时建 WS MV 报错 |
| #80856 | Resolved | -- | -- | VPM、权限与跨租户 | EM 仓库和 MV 接口的权限闸门 |
| #81297 | New | -- | -- | VPM、权限与跨租户 | 只有 MATERIALIZATION ACCESS 的用户 |
| #81309 | Resolved | -- | -- | VPM、权限与跨租户 | 克隆组织的 id 变更 |
| #81331 | In Progress | -- | -- | VPM、权限与跨租户 | id 变更被拒绝后下一个 id |
| #81059 | In Progress | -- | -- | 公共 API | 用户列表按空格拆分 |
| #81060 | In Progress | -- | -- | 公共 API | 失败的分析任务被报告为完成 |
| #81061 | In Progress | -- | -- | 公共 API | 更新不存在的 MV 带 cycle 时 NPE |

datatest 能验证、两边都没提到编号的未关闭 bug：#80313、#80344（G5）。T14 另给已关闭、datatest 原无用例的 #79854、#79858、#80152、#80155、#80570、#81043 补了用例。公共 API、权限和 id 变更类不属于静态 datatest。新提：#81658、#81659、#81682（§6）、#82099（T14，已修，T15）、#82140、#82142（G12）、#82210（T6）、#82219、#82223（T19）、#82231（`CalcMV` 核对，与 MV 无关）。

## 8. 具体要做（T17、T18 未开始；T19 在 stylebi PR #8746，未合并；其余已做）

| # | 要做什么 | 在哪里改 | 做完怎么验证 | 状态 / 注意 |
|---|---|---|---|---|
| T1 | 78 个只断言已定义的用例补基表 MV 命中检查（G1） | `AssociationMV_Spec`、`CalcField_Spec`、`Transform_Spec`、`MVHit_*`、`VSCondtion_Spec`、`SubMV_Others_Spec`、`TopMV_*` | 跑涉及的 spec；反向检查 | **已做（datatest PR #226）**。`CalcField_Spec` 的 `NotPublic_SelectionTree`、`NotPublic_Calendar`、`NotPublic_RangeSlider` 只有选择组件，查找记录看不到查询时才选的子 MV（同 `NotPublic_Text`），只检查 `query1|TopMV` 并加注释 |
| T2 | `wsmv` profile 加进 `test-mv.yml`（G6） | `.github/workflows/test-mv.yml` | 手动触发一次，`WSMV_Path_Spec` 出现在报告里 | **已做（PR #226）**，本机通过；**CI 未触发**。该 spec 自己建表、结束时删表 |
| T3 | 过期 golden 按各 bug 核对后更新（G8） | `MV_Creator`、`vs query and condition`、`hit mv`、`vpm` 的基线；`Append_Spec`、`Delete_Spec`、`Transform_Spec` | 干净 `mv/target` 下 0 失败；`both` 下仍通过 | **已做**：12 个用例 20 个 golden 文件，加注释和 `@Issue`，见 §6 第 7 条；**CI 未触发** |
| T4 | `NOT Transform` 反向路径 spec（G2） | `others` profile 的 `NotTransform_Spec` | 不命中、结果与不用 MV 一致 | **已做（datatest PR #232，9 个用例）**，见 §6 第 6 条 |
| T5 | 启用 #80458、#80446 的 `@Ignore` 用例（G9） | `MVHit_RankingAndFilters_Spec.chart-time type`、`SubMV_Others_Spec.top-aggregate-maxrow` | `both` 下通过 | **已做（PR #226）**：MV 与不用 MV 逐字节相同；`chart-time type` 加了 `compareWithoutMV`，换了 2 个滑块基线；`top-aggregate-maxrow` 补了 3 个选择列表基线 |
| T6 | `mv.*` 配置项会改变结果的场景（G4）：`mv.preferred.block`（#79869）、`mv.union.moveUp`、`mv.outer.moveUp`、`mv.ignore.nestedSelection`、`date.common.options` | 新建 `others/MVProperties_Spec`；`mv.preferred.block` 在 `IncrementalDataChange_Spec` | 改值后命中变化；结果与不用 MV 一致，或差异按已知限制固定；`both` 和 golden 各运行一次 | **已做（2026-11-24）**：`MVProperties_Spec` 13 个迭代、`IncrementalDataChange_Spec` 加 4 个迭代，`both` 和 golden 下都通过。属性用 `MVTest.setProperties` 设、`cleanup` 里 `restoreProperties` 还原（`@SreeProperty` 在 spec 里不生效）；断言写在 MV 运行的导出、命中和 `MVDef` 上，golden 下也起作用。(1) `mv.outer.moveUp=true`：`submv/left outer join-select right` 命中 `Query1|TopMV`，bk1 6 行（不用 MV 7 行）；`right outer join-select left` 命中 `Query2|TopMV`，bk1、bk2 29、31 行（35 行）；按已知限制固定（#81658）。(2) `mv.ignore.nestedSelection=false`：`topmv/selection nested`、`selection nested1` 命中 `Query1_O|SubMV`、`Query2_O|SubMV`，不再命中默认的 TopMV，没有分析警告，结果等于基线。(3) `date.common.options`：`hit mv/chart/chart2` 的 `Query3` 不设时实列为 Year、Month，`""` 时没有，`year,quarter,month,week,day` 时 5 个（`DateMVColumn.isReal()`）；结果都等于基线。(4) `mv.preferred.block`：MVSCRIPT 5 行建 MV、追加 3 行，块数默认 1→1、2 时 3→3、3 时 2→2、5 时 1→2，结果都是 1..8、与重建一致；`MockCluster` 下撤掉 #79869 的修复多半不失败，#79869 由产品单测 `LocalFileSystemAppendRecordTest` 覆盖。(5) `mv.union.moveUp`：运行时复制 `submv/union-select in both childs`（distinct union，CATEGORIES1 id 1..7、REGIONS1 id 1..2），改绑选择、预设选中值。上移的条件：开关为 true、union 为 distinct、各子表同一列位置都有选择（`SelectionUpTransformer.java:1753`、`isMoveableToUnionTable` `:1898`）；原资产两个选择在不同列位置，永不上移。上移后各子表的条件用 OR 合并套在 union 上：选择在名称列（两侧不重叠）时与不用 MV 一致；在 id 列选 1、2（两侧都有）时 4 行（不用 MV 2 行），即 #4906/#6585 的已知行为（12.2 曾禁止上移，#6585 撤回）；只在一侧选值时，没有条件的子表被跳过（`getRuntimeConditions`，`:692`），另一侧的行全部丢失（1 行，不用 MV 3 行；只在另一侧选值时 1 行，应为 8 行），值不重叠也错，超出 #6808 定下的"值不重叠才可用"，按现状固定，已提 #82210 待开发答复；UNION ALL 或开关为 false 时两个 SubMV，结果一致 |
| T7 | 从 13.8 移植单测 | 产品仓库 `core/src/test/java/inetsoft/mv/` | 单独运行通过，改坏产品代码后失败 | **已做（stylebi PR #8473）**：`MvStorageEngineTest`（53 个移植 46 个）；`ConfigMvBehaviorS2Test`、`MvManageCustomerP2Test` 改写成 5 个单测类共 43 个，见 §4 |
| T8 | 新写转换层单测（G3、G6） | 产品仓库 | 同上 | **已做（stylebi PR #8473）**：5 个测试类共 106 个用例，用真实的 Worksheet、Viewsheet 和 `TransformationDescriptor` 搭夹具，见 §4 |
| T9 | 增量更新与全量重建一致性（G5） | `IncrementalDataChange_Spec` | 增量结果与重建一致；反向：去掉重建前的数据库删除，新用例失败 | **已做（datatest PR #229）**：补 23 个用例（41 个按迭代计），incrementalmv 整套 197 个执行。覆盖：常量追加/删除、聚合后条件、Join/Mirror/Minus 组合表、字段比较、子查询、`MV.MaxValue`/`MinValue`（整数、浮点、字符串、日期时间，追加和删除）。Union、Rotated 的 MV 建在子表上，没有这条增量路径；分组 Mirror 上对非分组列的删除条件不生效。**不给 `Append_Spec`、`Delete_Spec` 现有用例加重建对比**：它们数据库不变，全量建 MV 不用追加条件过滤（`MVDispatcher.java:370`），不含 `MV.`、`@incrementalOnly` 的删除条件建时已排除（`MVCreatorUtil.mergeConditionList`，`:828`；`isConditionValid`，`:877`），对比要么必然失败（如 `S_TestCase1` 更新后 22 行、重建 21 行）要么是空检查。**发现的行为**：同一次更新里同时满足追加和删除条件的新行先被追加保留、下次更新才删除（`LocalMVIncremental.update0`，`:67`-`:228`，先删后加、追加查询不带"NOT 删除条件"），用户决定按设计处理，用一个用例固定 |
| T10 | (a) 实测 `mv.outer.moveUp`；(b) `RankingConditionTest`；(c) 关联 MV 命中检查（G1） | (a) datatest 本机；(b) 产品仓库；(c) `commons` 的 `MVTest.groovy` 与 78 个用例 | — | **已做**：(a) 差异实测出现，已知限制（#81658，§6 第 2 条）；(b) stylebi PR #8473 重新启用（§6 第 4 条）；(c) datatest PR #228（§6 第 5 条），依赖产品两个私有字段，改 `commons` 后要重建 tests jar |
| T11 | 零覆盖项补用例（G13），dashboard 四组：`associationEnabled=false`、bypass、`full=false`、`@nomv`/`parameter.` 表达式 | `others/MVViewsheetOptions_Spec`（新）、`VPM_Spec`、`TopMV_RuntimeAssets_Spec`；`commons` 的 `MaterializedViewResource.setFullData`、`MVTest.setFullData` | `both` 和 golden；断言写在 MV 运行的导出、命中和 `MVDef` 上 | **已做（2026-11-24）**：(1) `TestCase_Census` 关掉关联：只建 `Data|TopMV`，不建 `ASSOCIATION_Data`；`bk_select` 下三个选择列表没有被排除的值（开着时 Region 1/4、State 15/50），值与 golden 相同；图表等其他组件与 golden 相同。viewsheet 在仓库里改、`cleanup` 还原（复制会丢书签）。(2) `VPM_Spec` 的 bypass 用例（`vpm/group-user`）：不用 MV 时 VPM 生效，user0 11 行、admin 700 行；bypass 建 MV 后只有 `orders1|TopMV`，user0、user1、user2、admin 都命中它，结果都等于 admin 不用 MV 的 700 行。导出读完即删，免得多出文件影响 `group-user` 的 `compareData`。(3) `TestCase_Selectionlist1` 的 full：只被选择列表用的 `MVSCRIPT1/2/3`，full=false 时 MV 只有选择列（`INTEGER`；`STRING`；`DATE1` 和它的日期级别），full=true 时全部列；两种都等于 golden 和不用 MV。(4) `TopMV_RuntimeAssets_Spec` 8 个迭代（运行时复制 `mvLastUpdatetime`）：worksheet 表的 `@nomv`、`parameter.` 表达式列让整张表不建 MV（#48975）；放在镜像表上时基表建 `MVorders1|SubMV`；viewsheet 计算字段建 `MVorders1_O|SubMV`；普通表达式建 TopMV（对照）。k=1、k=100 各运行一次，逐行核对 E；分析都没有警告。验证：`others` 253 个、`topmv` 147 个、`vpm` 15 个，`both` 和 golden 下都 0 失败（跳过的是原有的 `@Ignore`） |
| T12 | 启用 bug 已关闭的用例（G9）：`composite3`（#80445）、`variable in subquery`（#80457）、`rangeslider-compositevalue`（#80170） | `TopMV_TableStructures_Spec`、`TopMV_Conditions_Spec`、`MVHit_RankingAndFilters_Spec` | `both` 和 golden 各运行一次 | **已做（2026-11-23）**：三个用例 MV 与不用 MV 逐字节相同。`composite3` 补 21 个选择列表基线，`bk1` 0 行；`variable in subquery` 补 4 个基线，断言改为命中 `ORDERS1|TopMV` 且不命中 `ORDERS1|SubMV`；`rangeslider-compositevalue` 的 `bk_select1` 两个基线各改一行（滑块终点 `2004-08-30 05:45:05`，2004 年 `18.9294`，#79506 修复的结果） |
| T13 | 用新产品重跑（#81682、#81659 修复后） | 6 个 profile 整套；`TopMV_AggregateFormulas_Spec`、`NotTransform_Spec` 的注释 | 清空 `mv/target`、还原 H2，`both` | **已做（2026-11-23）**：core 含 `b3fa4de4aa`、`f47db94f8b`、`c4f9d75bff`；结果见 §2。`datelevel_none` 通过，golden 不变；`cube` 仍不进 spec（§6 第 6 条）。唯一失败 `CalcField_Spec.Aggregate_ForDate`：`SelectionTree2` 默认和 `bk_select` 两个基线各 30 行，度量值从 `2020-12-30 00:00:00` 变成 `2020-12-30`（#81668，聚合计算字段按声明类型转换，有意变化，与 MV 无关），基线在 T15 更新 |
| T14 | 给已关闭、datatest 没有用例的 MV bug 补用例：#79854、#79858、#80152、#80155、#80570（含 #81043） | `IncrementalDataChange_Spec`；新建 `TopMV_RuntimeAssets_Spec`（topmv） | 单独运行，`both` 和 golden；incrementalmv 整套 `both` | **已做（2026-11-23）**：不改 `mvStorage.zip`，运行时复制资产改表/列/条件，需要时在 H2（`mvorder1`）建表用完删；断言写在 MV 运行的导出上，golden 模式下也起作用。(1) #80570/#81043：删除条件含 `MV.`，更新后空值行被删掉。(2) #79854：慢视图（每行 1.5 秒）查询执行中插入第 3 行，第二次更新后为 [1, 2, 3]；有模拟反向检查。(3) #80155：Long 变量超出 int 的 `>`、`<` 三个界值（0、119、119 行）；`=` 见 T15。(4) #80152：脚本表达式列（`setSQL(false)`）上的"不包含"条件，9 行与不用 MV 相同。(5) #79858：H2 `TIMESTAMP WITH TIME ZONE` 列设为 TimeInstant，用 MV 两行都有值；复制来的 `query11` 必须清掉原有分组和排序。验证：`TopMV_RuntimeAssets_Spec` 在 `both` 和 golden 下 6/6，incrementalmv 整套 `both` 201/0 |
| T15 | #82099、#81668 修复后跟进 | `TopMV_RuntimeAssets_Spec`；`CalcField_Spec.Aggregate_ForDate` 及其 `SelectionTree2` 两个基线 | 单独运行，`both` 和 golden | **已做（2026-11-24）**：core 含 `835edbf02e`（#82099）、`c4f9d75bff`（#81668）。(1) #80155 用例加 `=` 两行：`= 4294967297` 0 行、`= 1` 6 行（对照，证明等于条件仍选得到行）。资产里 `customer_id` 存成 VARCHAR（sqlType 12），`=` 会把值当字符串绑定、H2 报转换错误，这两行把列的 sqlType 设成 H2 的实际类型 INTEGER；修复只作用于 INTEGER 列。错误发生在不用 MV 的一侧，只有 `both`/`nomv` 下的 `compareWithoutMV` 能发现，golden 下 `rowCount` 只看 MV 的导出；没有在修复前的产品上运行过。`TopMV_RuntimeAssets_Spec` 在 `both` 和 golden 下 8/8。(2) `Aggregate_ForDate` 去掉 `@PendingFeature`，`SelectionTree2` 默认和 `bk_select` 两个基线的度量值去掉 ` 00:00:00`（与导出逐行相同，其余基线不变），golden 下通过 |
| T16 | 不可合并公式 Product、Concat（G7） | `TopMV_RuntimeAssets_Spec`：运行时复制资产改聚合公式（不改 `mvStorage.zip`） | `both` 和 golden；期望值不依赖 golden：每组的 Product 等于 Concat 列出的值之积 | **已做（2026-11-25）**：两个迭代。(1) worksheet 镜像表的聚合（`topmv/merge-average`，按 DISCOUNT）：Average（原样）建 `Query1_O|TopMV` 在 MV 上合并；Product、Concat 在分析时就不建这个 MV，改为 `ORDERS1|SubMV` 读明细。(2) 图表聚合（`hit mv/chart/chart2` 的 Chart2，按月）：命中 `Query3|TopMV`，运行时读明细在内存聚合（`AssetQuery.java:175`-`:203`）。两组结果都与不用 MV 相同；Concat 过长时截断成 `...`、Product 溢出为 `∞` 的分组（merge-average 的 DISCOUNT 0）不核对。验证：`TopMV_RuntimeAssets_Spec` 整个 spec 在 `both` 和 golden 下都是 20/20；`both` 下有一次 #79858 的用例建 MV 失败（"Error executing query for MV data"，日志不输出，原因不明），连续重跑两次都通过。**发现**：交叉表（`hit mv/crosstab1`）对整数列 `QUANTITY` 做 Concat 时，用 MV 为 `20.0,9.0,…`，不用 MV 为 `20,9,…`（同样的值、同样的顺序）：MV 的度量值以 double 传给 `ConcatFormula.addValue(double)`（`buf.append(v)`），不用 MV 时是 Integer 走 `addValue(Object)`。其他公式的结果按列类型格式化，看不出来。是否提 bug、是否固定成用例，待用户决定 |
| T17 | 核对 195 个没被字面量引用的 viewsheet（G11） | `mvStorage.zip`、`expectData` | 每个 viewsheet 归类为间接引用、补用例或删除；删除后整套 `both` 仍通过 | **未开始**。先查 12 个末段名字出现在 spec 文本里的；`CalcField/NotPublic_Brush1` 有基线没被引用；`CalcMV` 38 个已核对（§2），不再处理；只删除确认无用的 |
| T18 | 组件都是纯值时 `getMVHitInfo` 退回"已定义"（G1） | `commons` 的 `MVTest.groovy`（`:455`-`:461`） | 列出走退回路径的用例；改基类后这些用例记录到真正读过的 MV，反向：不建 MV 时失败 | **未开始**。先统计走退回路径的用例，再定是否改基类 |
| T19 | 补 `ViewsheetSandbox.isMVEnabled`（`meta`，G13）、`MVTableMetaData`、`SharedMVUtil` 单测；读 13.8 `QueryAdvancedFeatureTest.mvUpdateVariablesInSql` 定是否移植 | 产品仓库 `core/src/test/java/inetsoft/report/composition/execution/`（`isMVEnabled`）、`core/src/test/java/inetsoft/mv/` | 单独运行通过，改坏产品代码后失败 | **已做（stylebi PR #8746，未合并）**：`ViewsheetSandboxMVEnabledTest`、`MVTableMetaDataTest`、`SharedMVUtilTest`、`MVScriptableMultiBlockTest` 共 28 个用例，见 §4；`mvUpdateVariablesInSql` 移植了多块 `MV.MaxValue`/`MinValue` 部分；发现 #82219、#82223 |

## 附：数据来源与复现

```
# 每个 profile 单独运行（本机，先 ./mvnw package -pl commons -DskipTests，运行前 rm -rf mv/target；
# 对比不用 MV 加 -Dmv.compare=both）
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
