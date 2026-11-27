# vsfreehand 测试缺口与补充方案

> 模块：`vsothers`，profile `-Pvsfreehand`，spec `Convert_Spec` / `FreeExecution_Spec` / `Freehand_Spec`（`vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vsfreehand/`），共用资产 `vsothers/src/test/resources/config/vsfreehand-storage.zip`
> datatest 基线：`Stateless-Sessions1`；产品源码：`main`（2.1.0-SNAPSHOT）
> 配套文档：[vsfreehand-g2-convert-probe.md](vsfreehand-g2-convert-probe.md)（转换前后数据对比）、[vsfreehand-java-unit-tests.md](vsfreehand-java-unit-tests.md)（产品仓库 Java 单测结果）

## 1. 结论

- vsfreehand 有 82 个用例、82 个 vs，但**几乎全靠整页 PNG 断言**：80 个用例只比 PNG，只有 2 个（`FSDeclaredVar`、`FSFormatNamedCell`）同时比单元格文本。单元格取值只要不影响渲染像素就发现不了；PNG 基线是回归基线（产品输出生成，不是独立算出的答案）。
- 转换（Convert）同样只比 `_CALC.png`。产品代码写明的意图是"转换后数据与原交叉表一致"（`ComposerVSTableService.java:479`），这一点现在由 `checkConvert` 的数据比较断言（见 probe 文档 §6），`allowedDiff` 已按列声明（G21，见 probe 文档 §6）。
- 产品仓库：转换类（`VSTableConverter`、`convertToFreehandTable`、`createGroupCond`、`isEmptyCell`、`getColType`）已有单测，开出 8 个 bug（#81255–#81262）；"快速路径 vs 脚本路径"的整条链数据正确性仍没有测试，放在 datatest 做（§5 "双路径对照"）。
- 转换分支清单 16 行：datatest 里**无资产** 4 行（空布局、预聚合列名、`sortOthersLast=false`、以另一个 vs 组件为源），**未验证** 若干（见 §2）。
- 手写公式脚本只用到 `CalcTableScope` 25 个聚合函数中的 8 个；calcfield（尤其 Aggregate 类型）在 Freehand 里只有约 1–3 个 vs；日期类型没有系统对比。

优先级：① 给 Freehand/FreeExecution 用例补 `executeVS`+`compareData`（机制层，已有先例）；② 收紧 `checkConvert` 的 `allowedDiff`；③ 开发确认语义后做 `CalcTableVSAQuery` 双路径对照。

## 2. 覆盖情况

**规模：** 82 个用例（Convert 19，其中 1 个 `@Ignore`；FreeExecution 31；Freehand 32）；zip 内 86 个 vs（2026-11-27 删 4 个孤儿后 82 个）、29 个工作表、58 个书签（删孤儿前的统计，35 个书签库含 `(Home)` 默认条目，删后少 3 个书签库，书签数未重新统计）；基线共 104 个（PNG 101：Convert 38、FreeExecution 31、Freehand 32；TXT 3：全在 FreeExecution）。

**孤儿 vs（三个 spec 取并集，82 个 `new VSCalcTest(` 对 82 个字面量 id）：4 个，已于 2026-11-27 从 zip 删除**（连同它们的书签、父文件夹条目、依赖记录；仓库里没有任何引用；删后三个 spec 重跑，失败仍只有本机 PNG 像素差异）
- `ConvertToCalc/Crosstabs/VSScriptEffect`（与 `ConvertToCalc/VSScriptEffect` 同名，后者对应用例 `@Ignore`，`Convert_Spec.groovy:282`，Bug #58711 "redesign the case"）
- `FreeExecution/BindingOptions/ComBindingTable6`（书签 `noRangeID`、`quarter` 因此也没用到）
- `FreehandTable/Others/freehand none level`
- `FreehandTable/Relation/NG4`（书签 `bk1`、`bk2` 同样没用到）

**覆盖矩阵**（静态扫描 zip，`vs_backup_scan.py inventory` + 临时统计脚本）

*来源 × 数据源（vs 个数，含 4 个孤儿，统计于删除前）*

| 来源 \ 数据源 | 逻辑模型（直连） | 工作表：SQL | 工作表：库表/模型绑定 | 同上 + 变换 | 嵌入/快照 | 嵌入 + 变换 | 合计 |
|---|---|---|---|---|---|---|---|
| FreehandTable（原生绑定） | 10 | 5 | 4 | 0 | 14 | 1 | 34 |
| FreeExecution（原生绑定） | 3 | 5 | 3 | 18 | 2 | 1 | 32 |
| ConvertToCalc（转换） | 1 | 1 | 2 | 11 | 5 | 0 | 20 |
| 合计 | 14 | 11 | 9 | 29 | 21 | 2 | 86 |

另有 11 个 vs 的 Freehand 表绑定同一 vs 里的另一个组件（`sourceInfo type=64`）。zip 内没有 cube/OLAP 源的 vs（只看了 `worksheetEntry`）。

*单元格内容特性（含该特性的 vs 个数；ConvertToCalc 列只统计 vs 里原有的 CalcTable）*

| 特性 | 全部 | ConvertToCalc | FreeExecution | FreehandTable |
|---|---|---|---|---|
| 列绑定-分组 / 汇总 / 明细 | 63 / 60 / 5 | 3 / 3 / 0 | 30 / 30 / 5 | 30 / 27 / 0 |
| 文本单元格 | 49 | 1 | 28 | 20 |
| 公式脚本-分组 / 汇总 / 明细 | 37 / 41 / 21 | 0 | 19 / 21 / 19 | 18 / 20 / 2 |
| 日期级别分组 | 44 | 3 | 23 | 18 |
| TopN（>0） / 带 Others / 反向 | 25 / 21 / 5 | 0 | 15 / 11 / 2 | 10 / 10 / 3 |
| 展开 横向 / 纵向 | 57 / 64 | 2 / 3 | 27 / 31 | 28 / 30 |
| 合并单元格 / 有跨行跨列 | 40 / 51 | 1 / 1 | 19 / 26 | 20 / 24 |
| 脚本 `data[...]` / `rowValue` / `toList` | 43 / 33 / 38 | 0 | 22 / 18 / 20 | 21 / 15 / 18 |
| 脚本用参数/`runQuery` | 2 | 0 | 2 | 0 |
| 单元格自带数据源 | 17 | 1 | 7 | 9 |
| 条件格式 / 超链接 | 23 / 2 | 0 | 16 / 0 | 7 / 2 |
| 时间序列 | 3 | 0 | 2 | 1 |
| 分组排序非升序 / 按列排序 | 29 / 2 | 0 | 18 / 2 | 11 / 0 |
| 分页（pageAfter） | 0 | 0 | 0 | 0 |

编码对照 `CellBinding.java:40-65`（type 1 文本、2 列、3 公式；btype 1 分组、2 明细、3 汇总）。只统计 `assemblyInfo` 下的一份（`state_calctable` 有重复拷贝）。未统计：NameGroup 的使用、`fmode`（全为默认 -1）。

*转换来源（`ConvertToCalc`，20 个 vs）：* 交叉表来源 17 个，表来源 5 个（含嵌入表 1 个）。读用户设置的 `VSAggregateRef`、`<calculator>`、`<dateComparison>` 得出：
- 聚合公式 14 种：Sum、Count、Max、Min、Average、DistinctCount、First、Last、Median、Concat、Covariance、WeightedAverage、NthLargest、NthMostFrequent（另有 none）。`AggregateFormula` 共 27 个，**转换来源没有**：Mode、Correlation、Variance、StandardDeviation、PopulationVariance、PopulationStandardDeviation、Product、NthSmallest、PthPercentile、SumSQ、SumWT。
- 计算器：6 个 vs 带计算器（`CroTable7`、`CroTable8`、`PdateManulCroTable1`、`PercentFormat` 带 PercentCalc；`DCPTrendTable1`、`ComTable2` 带 ChangeCalc；`CroTable8` 另有 Running Sum）。
- 日期比较：2 个 vs 有真实周期设置（`DCPTrendTable1` 的 `StandardPeriods`、`ComTable2` 的 `CustomPeriods`）；钻取 1 个（`DrillCroTable1`）。
- 其他：行头最多 3 层（`CroTable8`），列头 2 层 3 个 vs；总计 4 个、小计 3 个；TopN/BottomN 约 6 个（都带 Others）；`fillWithZero=true` 4 个；`mergeSpan=false` 3 个；无聚合的交叉表 1 个；日期级别含普通级别 0–8 与日期部分级别 513/518/521。

**转换分支清单**（来源 `VSTableConverter.java`、`ComposerVSTableService.java:460-565`、`VSLayoutTool.java:752-790`）。"有"= 静态上至少 1 个转换来源 vs 会走到，不等于数据正确（数据一致性见 probe 文档）。

| # | 分支 | 位置 | datatest 资产（静态） | 状态 | 产品 Java 单测 |
|---|---|---|---|---|---|
| 1 | 普通表 → Calc | `VSTableConverter:105-174` | 5 个表来源 vs | 有；4 个组件行集不一致（probe T1） | 已有 |
| 2 | 布局为空 / 源表为空 | `:63-67`、`ComposerVSTableService:550` | 无 | **无** | 空布局已覆盖 |
| 3 | 交叉表无聚合 → `convertAsPlainToCalc` | `:205`、`:424` | 1 个 vs | 有，一致性未单独核对 | 已覆盖 |
| 4 | 表头/汇总表头 → 文本；fake 列 → 文本 | `:284`、`:307` | 前者全部；fake 列未统计 | fake 列**未验证** | 已覆盖 |
| 5 | 多层头 cellName 复用、span 复制 | `:335-352`、`:456-502`、`:97` | 3 层行头 1 个；`mergeSpan=false` 3 个 | 有；明细区合并 span（`:374`）**未验证** | 已覆盖（span 检查大多为空检查） |
| 6 | `fixOrderInfo`：日期型保留 interval | `:518-543` | 日期级别 0–8、513/518/521 | 有（静态） | — |
| 7 | `fixTopN`：非最内层维度关 Others | `:548-600` | TopN 约 6 个带 Others，外层带 Others 的未核对 | **未验证**；`CroTable8/Crosstab1` 外层出现 `Others`（probe T4④） | 已覆盖；设计态 vs 运行时表头不一致时错位（#81258） |
| 8 | 分组列不在 `ainfo` 时 `getOriginalColumn` | `:368` | 未统计 | **未验证** | 已覆盖（#81257）；该分支只在日期比较时进入，而转换前会清日期比较，实际近似不可达 |
| 9 | 汇总格区域：明细/小计/总计 | `:393-412` | 总计 4 个、小计 3 个 | 有 | 已覆盖 |
| 10 | 百分比：`perBy`×选项四种组合 | `:431-446` | OF_GRANDTOTAL 5 个，OF_GROUP 仅 `DCPTrendTable1` | 四种组合是否各有 1 个未核对；`CroTable8` 百分比转换后值变（T2） | 已覆盖（公式名带选项 `Sum<8>/<32>/<4>/<16>`） |
| 11 | 公式列名还原：预聚合列名、双列/带 N 公式 | `VSLayoutTool:772-784` | 预聚合列名**无**（G11）；Correlation、NthSmallest、PthPercentile 无 | **无** | `getAggregateColumn` 已覆盖 |
| 12 | `clearCalculators`（百分比除外）、`clearCrosstabDrill`、`clearDateComparison` | `ComposerVSTableService:479-484`、`:524-526` | 计算器 6、钻取 1、日期比较 2 | 有；清除后值不同归入允许差异（G21） | 钻取清除已覆盖 |
| 13 | `clearDataGroup` 后 `resetDataMap` | `:504-511` | 未统计 | **未验证** | — |
| 14 | `fillBlankWithZero`、`sortOthersLast=false` | `:513-514` | `fillWithZero=true` 4 个；`sortOthersLast=false` **0 个** | 后者**无** | 已覆盖 |
| 15 | 以另一个 vs 组件为源（`vsSrc`） | `:528-529` | 0 个 | **无** | — |
| 16 | `confirmed=false` 时 `fixAggregateInfo` | `:474-477` | 测试固定 `confirmed=true` | 探测里 19 个 vs 两者一致，用例里不走 | — |

*公式脚本与特殊列（静态扫描，`cellBinding type=3` 的 `value` 去重 355 条）：* `toList` 选项：`sort=asc` 72、`sort=desc` 16、`others=leaveothers` 44、`timeseries=true` 21、`distinct=true` 1；`rounddate`：year 93、month 14、hour 17、minute 12、day 10、quarter 10、week 8、second 4、none 5；`date=`：quarter 11、hour 5、minute 4、month 3、second 3。`aggregate="true"` 的 13 处是工作表聚合标志，不是 calcfield 的 Aggregate 类型。

*书签：* 非 `(Home)` 书签 23 个，被用到 17 个，未用 6 个（4 个在孤儿 vs 上，已随孤儿删除；另 2 个是 `CM11_1_1/aaa`、`CM11_1_9/bk1`）。用例里没有一处通过事件对 Freehand 做选择/刷选，选择状态都来自书签恢复。

**验证机制：**

- 比对遍历"实际导出"，并检查基线→导出：`ViewsheetTest.groovy:471-477` 检查"基线文件是否都被导出"，`CompareUtil.groovy:471-475` 遍历导出目录逐个比；TXT 导出了但无基线会失败（`CompareUtil.groovy:249-252`）；PNG 无基线记为 `Compare Failed with exception`（`:165-167`，读代码得出）。
- 导出失败让用例失败：`onExportFailure` → `violation`（`ViewsheetTest.groovy:230-232`）；转换流程在 `VSCalcTest.groovy:50/56/63/74/81` 对转换失败、无可转换组件、未变成 CalcTable、导出失败、导出为空分别抛异常。
- `exportData` 清理：用例构造时按用例清一次（`ViewsheetTest.groovy:86-98`）；CI 整体清（`Jenkinsfile:37`、`test-vsothers.yml:71`）。
- CI 失败处理：Jenkins `catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE')`（`Jenkinsfile:78`），GitHub `continue-on-error: true`（`test-vsothers.yml:85`）；vsfreehand 失败不让构建失败，只标 UNSTABLE。
- **本机运行**（Windows，`./mvnw test -pl vsothers -Pvsfreehand`，82 个用例）：0 通过，80 个 PNG 像素差异，1 个超时，1 个跳过。
  - 导出异常 0；"基线未被导出"0；"导出了没有基线"0；TXT 比对失败 0（`FSDeclaredVar`、`FSFormatNamedCell` 的 3 个 TXT 通过）。
  - PNG 像素差异 0.50%–6.02%，本机与 CI 渲染不同，**本机无法验证 PNG 层**。
  - `Convert_Spec.CroTable2` 超时：原来 `checkConvert` 之后有 `Thread.sleep(70000)`，`@Timeout` 80 秒，本机耗时 86.7 秒；sleep 已在 2026-11-27 去掉（实测无作用，见 §6），该用例耗时约 22 秒。
  - `strictcheck` 对三个 spec 全是 "Not predicted"，不可用。
- 基线性质：PNG/TXT 都是回归基线（`docs/vs/VSTableFreehand/Freehand.xlsx` 只记录用例分布与 bug 编号，没有独立期望值）。

**断言层：**

- Freehand 表是 `TableDataVSAssembly`（`CalcTableVSAssembly.java:48`），`executeVS` 本可导出单元格 TXT，但 82 个用例里只有 2 个调用。
- Convert_Spec 的 19 个用例都是 `exportAsPNG` + `checkConvert` + `compareImage`；转换后数据比较见 probe 文档 §6。
- 整页 PNG 看不到：被裁掉的行列、隐藏列、格式化前的原值、超链接目标。

## 3. 缺口与方向

判断依据：期望值能独立算出 → Java 单测；问题出在组件、绑定、数据源、书签之间的接线 → dashboard；验证机制本身的缺陷 → 改基类/spec。

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 80/82 用例只比整页 PNG，没有单元格数据断言 | 值错但渲染差异小于容差、值在被裁掉/隐藏的行列、格式化前后值不同；PNG 基线跨平台不一致，换机器更新基线可能把错误值固化 | 机制（改 spec/基类，加 `executeVS`+`compareData`） | 高 |
| G2 | Convert 只比 `_CALC.png` | 转换后行/列、合计、聚合值与原组件不一致；PNG 是回归基线，错误被一起固化 | 机制：`VSCalcTest.checkConvert` 已加数据比较，51 个组件 32 个一致、19 个有差异（12 个设计行为，7 个待处理），见 probe 文档 | 高 |
| G3 | `CalcTableVSAQuery.getTableLens` 两条路径：快速路径先把布局转成临时 crosstab（`createCrosstabAssemblies`），用预聚合数据当底表；否则用明细表当底表在脚本里分组聚合（`:125-184`）。两条都由 `CalcTableLens.process()` 求值单元格。没有"两路径数据等价"的测试 | 同一布局两路径算出不同值（TopN、Others、空分组、合计行）；datatest 只对当前路径做 PNG 快照，路径选择条件改了不会发现 | datatest 双路径对照（§5）；产品单测只做函数级对比；2026-11-27 已补规模探测（社区 `CalcTableVSAQueryPathAgreementTest`）：4 万~10 万行、1000~4000 组下快速路径 0.6~2 s，脚本路径在同一组的行不相邻时耗时随"单元格数 × 行数"增长（10 万行 4000 组：31 s，同样的行按组排序后 1.5 s），见 #82482；结果与行顺序无关（已有测试），耗时用例 `@Disabled` 待修复；命名分组（一直走脚本路径）同日已手算核对：Others 合并/保留、升降序、`sortOthersLast`、嵌套内外层、Median/DistinctCount/总计、Top N、横向展开，均正确；组名或成员值含 `"`、`\`、`:`（组名另含 `'`）时汇总为空或丢组见 #82483，组名叫 `Others` 与自动 Others 合并重复计数见 #82484（待开发回复）；两个 `@Disabled` 用例待修复；2026-11-27 又按"列名/数据值 × 6 种布局（普通分组、Top N、Top N+Others、按值排序、手工排序、命名组）× 14 个特殊字符"扫了一遍：数据值在所有布局下都对（手工排序含 `;` 除外，#82489）；列名含 `'`、`,`、`(` 时 Top N/按值排序在脚本路径报错或空行（#82487），含 `"` 时 Top N+Others 每行都是总和（#82488）；含 `\`、`;`、`@` 见 #82472；命名组加含 `"` 的列名在本分支仍为空（#81699 的修复在别的分支） | 高 |
| G4 | 转换链 `generateLayout` → `changeTableMode` → `convertToFreehandTable` | 转换静默改数据：清计算器、清钻取/日期比较、复制 fillBlankWithZero/sortOthersLast/mergeSpan 等分支改错 | 产品单测已覆盖大部分（见 java-unit-tests）；dashboard 靠 G2 | 中（剩余分支见 §2） |
| G5 | `RuntimeCalcTableLens`（1559 行）单测只有序列化用例 | 展开、分组排序、TopN+Others、日期级别分组、空分组、合并单元格取值错误 | 单测；脚本路径的分组/TopN/Others 由 `LayoutTool.createGroupCond` 生成（`:1352-1440`），`RuntimeCalcTableLens` 只在判断 Others 单元格类型时读 TopN（`:1041`），测试对象应是 `createGroupCond`（已覆盖，见 java-unit-tests） | 中 |
| G6 | 交互只有书签恢复，没有选择/刷选后重新执行 | 选择变化后 Freehand 没刷新、缓存没失效 | dashboard（已做 2026-11-27：图表刷选，`Interaction_Spec`；选择列表/选择树的事件 datatest 没有实现，未做） | 中，刷选已完成 |
| G7 | 转换来源形态缺：11 种聚合公式（见 §2）、Percent/Change/RunningSum 之外的计算器、以另一个 vs 组件为源、`sortOthersLast=false`、空布局、外层维度带 Others 的 TopN | 这些形态的列名还原、计算器清除、TopN/Others 处理出错 | dashboard（新建 vs）；2026-11-27 已用 Java 做了其中的百分比四种选项（4/8/16/32 两条路径一致且与手算一致，旧选项 1/2 见 #82480，空交叉格见 #82479）和 TopN 下的总计（见 #82408 的补充）；同日用 Java 做了"以另一个 vs 组件为源"（表组件、交叉表组件；`CalcTableVSAQueryPathAgreementTest`）：总是走脚本路径，汇总与手算一致；交叉表源的列名被清洗（`Sum(Total)` 要写成 `Sum_Total_`，写原名得到全空）；交叉表源带总计时 `Grand Total` 行成为一个分组（按特性记录）；带列维度时列名取数据值；改源的条件后 Freehand 随之刷新（表、交叉表都对）；其余形态未做 | 中 |
| G8 | 4 个孤儿 vs（已删除）、1 个 `@Ignore`、6 个未用书签（4 个随孤儿删除）；`CroTable2` 的 70 秒 sleep（已去掉） | 孤儿/被忽略的场景没被测；CroTable2 在慢机器上偶发失败被当噪声 | 机制 + 清理 | 低 |
| G9 | 没有 pageAfter 分页、cube/OLAP 源的 Freehand 用例 | 分页符、cube 源取值错误（是否支持 Freehand over cube 未验证） | dashboard；pageAfter 更适合 vsexport | 低 |
| G18 | 转换生成的公式文本没有断言（`sum(data['…@…'])`、分组条件） | 数据碰巧相同但公式写错（换数据/分组/筛选后暴露）；转换前组件值本身错时两边一起错 | 单测（逐单元格断言，已做）+ dashboard（手算期望的小 vs） | 中 |
| G19 | 手写公式函数面窄：355 条公式，`sum` 176、`toList` 145、`mapList` 50、`inArray`/`inGroups` 19/13、`count` 9、`nthMostFrequent` 8、`nthLargest` 6、`none` 6、`countDistinct`/`max` 各 2、`rowList` 1。`CalcTableScope` 25 个聚合函数中 **17 个在 zip 里一次没用**：`average`、`min`、`product`、`concat`、`standardDeviation`、`variance`、`populationVariance`、`populationStandardDeviation`、`median`、`mode`、`correlation`、`covariance`、`weightedAverage`、`first`、`last`、`pthPercentile`、`nthSmallest` | 这些函数在 Freehand 里取值错误（空值、除零、字符串列做数值聚合、`q2[...]` 跨单元格引用）；含公式单元格一定走脚本路径（G3），函数覆盖越窄路径等价风险越大 | 产品单测补边界（函数本身已有覆盖）+ dashboard（新建 vs，按函数组，主要缺口） | 高 |
| G20 | 特殊列几乎没覆盖：calcfield 只有 3 个资产（交叉表日期型、CalcTable `First(...)` 表达式型 string、CalcTable none 聚合）；Aggregate 类型约 1 个 vs。日期：分组 44 个 vs、`rounddate` 覆盖 year…second，但没有 Date/Time/TimeInstant 系统对比，没有跨年周/闰日/空值/时区边界 | calcfield 作为分组/汇总/明细时类型或聚合时机错（先聚合还是后聚合）；日期边界错位；转换时 calcfield 丢失 | dashboard（calcfield × 返回类型 × 单元格角色，同样的交叉表做转换）+ 单测（日期边界） | 高 |
| G21 | （已做 2026-11-27）`allowedDiff` 按列声明：`cols` 设计行为、`todoCols` 待处理（`CroTable2/Crosstab2` 的 `NthLarge`、`We_Ave` 两列，`CroTable8/Crosstab1` 的 0–6 列，`CroTable8/Crosstab2` 的百分比列），其余列必须一致。形状变了的组件仍只比行数/列数；`CroTable8/Crosstab1` 本机行数不再变，但值和行序不同 | — | 向开发确认 B2 的数值变化是否也是设计 | 已做 |

## 4. Java 单测补充

产品仓库已补 Java 单测，详情（每个类的用例、bug #81255–#81262、待开发决策、弱点）见 [vsfreehand-java-unit-tests.md](vsfreehand-java-unit-tests.md)。与缺口的关系：

- G4、G5、G18、G10、G11、G14、G16 的单测部分已覆盖；转换分支的覆盖见 §2 分支清单最后一列。
- **G3（`CalcTableVSAQuery` 双路径）不在产品仓库做，放 datatest（§5）。**
- G19、G20 的函数边界/日期分组单测已部分覆盖，余下的预期值等开发回答（#81084 等）。

## 5. Dashboard 补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 数据层断言（G1） | 给 Freehand_Spec、FreeExecution_Spec 的用例补 `executeVS` + `compareData`（先例 `FreeExecution_Spec.groovy:388-399`），或在 `VSCalcTest` 加"导出 PNG 并导出数据"包装方法 | 每个 Freehand 单元格的文本值 | TXT 基线用 `-Dupdate.baseline=true` 生成，**必须人工核对**；`executeVS` 对被隐藏的组件跳过（`ViewsheetTest.groovy:186`） |
| 收紧 `allowedDiff`（G21） | 已做：按列声明（`cols`/`todoCols`），见 probe 文档 §6 | 名单内组件的其余列必须一致 | — |
| 转换来源扩展（G7） | 新建 vs（3–5 个）：以另一个 vs 组件为源、`sortOthersLast=false`、外层维度带 Others 的 TopN、Mode/Variance/StandardDeviation/Product/NthSmallest/PthPercentile/Correlation、百分比四种组合（Java 已做）、空布局 | 转换后数据与原组件一致（用 `checkConvert`） | G21 已收紧；Java 的 `CalcTableVSAQueryPathAgreementTest` 已覆盖百分比和 TopN 总计两条路径的对照 |
| 选择/刷选交互（G6） | 对 `CM11_1_1`、`CM11_1_2` 用图表刷选事件（`brushOnChart`）刷选再导出 | 刷选后 Crosstab 和它上面的 Freehand 表都刷新，取消刷选后回到原样（已做：`Interaction_Spec`，无基线，期望值从同状态的 Crosstab 读出，结论：刷新正确） | `ActionEventsUtil` 只建打开事件，选择列表/选择树的选择事件 datatest 没有实现（`RuntimeViewsheetResource` 只有图表刷选和 Show Details），需要时要先在 commons 里实现 |
| 清理孤儿与 ignore（G8） | 4 个孤儿 vs 已删除（2026-11-27）；`VSScriptEffect` 按 Bug #58711 重新设计；`CroTable2` 的 sleep 已去掉（2026-11-27） | 孤儿场景被测到；CroTable2 不再因机器速度失败 | — |
| 数据源扩展（G9） | cube/OLAP 源；pageAfter | 取值与分页 | Freehand over cube 是否支持未验证 |
| 函数面（G19） | 新建 vs（3–5 个）：统计类、取值类、双列类、脚本控制流（if/var/空值/除零）、`q2[...]` | 各函数在 Freehand 的取值；期望值在单测里手算，这里只比接线 | 依赖 G1 的 TXT 基线 |
| 特殊列（G20） | 新建 vs（3–4 个）：Aggregate/默认类型 calcfield，返回 string/number/date，分别作分组、汇总、明细；日期型 calcfield 分组；同样的交叉表做转换 | calcfield 取值；先/后聚合时机 | 依赖 `checkConvert` |
| 特殊字符列名（G10） | 新建 vs（工作表含 `YYYY, MMM D`、含引号的列名），转换后用 `checkConvert` 对比；原生绑定同理；另核对 `Sales-Region`/`Sales (USD)` + TopN+Others（#81255、#81257） | 转义后的名字在 JS 引擎里能查到真实列 | 已改用 Java 做（2026-11-27，`CalcTableVSAQueryPathAgreementTest`：19 个名字两条路径都正确，`@ ? ;` 和反斜杠出错，见 #82472）；界面不允许输入这些字符，不再新建 vs |
| 预聚合列名（G11） | 新建 vs（工作表含 `Min(ftime)` 之类的列，交叉表以它为聚合），转换后对比 | 转换不丢行 | 同上 |
| 表脚本引用组件值（G15） | 新建 vs（下拉框 + freehand + `table[0][0] = ComboBox1.selectedLabel`），书签恢复选择 | 书签状态下脚本读到组件值 | 已验证（2026-11-27）：2.1.0 已修复，Java 回归用例已加（4 种脚本写法），不再新建 vs |
| Show Details（G14） | `showDetailOnTable` helper 加 3 个用例（Others 单元格、命名分组+时间序列、无数据） | 控制器 → 服务 → lens 链不崩溃 | 服务装配同 `convertToFreehandTable` |
| 导出表尾行颜色（G16） | 带区别明显表尾行颜色的表样式 + freehand，导出 PNG/PDF 比对 | 数据行不被画成表尾行 | 转 vsexport |
| 验证 #81259/#81261 | #81261 已验证（2026-11-27）：Freehand 日期分组格的 Week of Year 和 Week 两种级别，4 种 `week.start` 下快速路径与脚本路径一致且与按日历规则手算的值一致（Java，`CalcTableVSAQueryPathAgreementTest`）；#81259 未做 | week 取整与交叉表一致 | 已做（#81261） |

### 双路径对照（G3）

**做法：** 一个 vs 里放成对的两个 Freehand 表：`<场景>_Q`（预期走快速路径）和 `<场景>_S`（在 `_Q` 上多加一个 `BIND_FORMULA` 单元格，放在明细区之外的空列，公式取常量 `1`，强制整表走脚本路径）。两张表共用同一内嵌工作表和布局。用 `executeVS` 导出两个组件的 TXT，比较时忽略公式列。每个场景三件产物：vs、手算期望（独立脚本从原始行算出，存 TXT 基线）、"两个组件逐格相等"的断言。两者都对得上手算期望才说明路径等价；只有 `_Q` 与 `_S` 不同才是 #81084 那类问题。不要用"分组改原始顺序"当开关，它会同时改变排序语义。

**底表（一个内嵌工作表，所有场景共用）：** 列 `region`、`category`（含 null 和空串）、`year`、`date`（含 null）、`sales`（含 null）、`qty`、`name`（含逗号、双引号）、`label`（字符串列，供 `Mode`/`Median`）。20–30 行；有稀疏组合、重复值、一个全 null 的分组。具体行数据未写。

**确认走了哪条路径（不进 CI）：** 本机用 `DebugCalcCrosstabCheck` 日志（命令见附录）核对每个 `_Q` 出现 `Use N crosstab(s)`、每个 `_S` 出现 `Use detail table`；日志只带组件名，组件名要全 vs 唯一。CI 测试 classpath 没有日志绑定（NOP），不要为此在 pom 加依赖。

**机制改动：** `VSCalcTest`/`ViewsheetTest` 要加"比较同一 vs 里两个组件、忽略指定列"的方法；现有 `compareData` 只拿导出文件与基线比。要先确认 `executeVS` 导出 CalcTable TXT 时列的排列和命名。

| 场景 | 布局（`_Q`） | 预期 | 风险 |
|---|---|---|---|
| P1 基准 | 行分组 `region` + `Sum(sales)`、`Count(qty)`；升序、纵向展开 | `_Q` 快速，`_S` 脚本 | **先验证"加公式格后其余单元格不变"这个前提**（未验证，不成立则整个成对设计要改） |
| P2 并列分组 + 稀疏 + TopN | 列头下两个并列分组（`category`、`year`）各带汇总，行分组 `region`，其中一个带 TopN | 触发 `splitLayoutByColumns` 拆成多个交叉表再拼回，`mergeCrosstabs` 按行下标对位 | **优先级最高。** `mergeCrosstabs` 注释写明"假定所有交叉表行数相同"（`:431`），不按分组键对齐；子交叉表行集/行序不同（TopN 排序汇总列不同、某数据列在某分组下全空）会错行 |
| P3 TopN 外层带 Others | 两层行分组，外层带 TopN+Others，内层不带 | 回退脚本（`:1517`） | 验证回退本身；对应 probe T4④ |
| P4 TopN 最内层带 Others | 两层行分组，内层带 TopN+Others | 快速 | Others 行值是否一致；`sortOthersLast` 开/关各一对 |
| P5 特殊分组值 | 行分组 `category`（含 null、空串）、`name`（含逗号、双引号）+ `Sum(sales)` | 快速 | null 与空串是否同组、特殊字符转义、分组数是否一致 |
| P6 聚合边界（#81084、#81071） | 行分组 `region`，汇总列：`label` 的 `Mode`、`Median`、`Sum`、`Average`；全空列的 `Count`、`Average`、`Max`；`fillBlankWithZero` 开/关 | 快速 | #81084 的"字符串列 `Mode` 脚本路径 `-Infinity`、交叉表 `a`"应在此复现；预期值待开发回答，**先只记录两条路径的实际输出，不写断言** |
| P7 百分比方向混用 | 同表里既有占列又有占行百分比 | 回退（`getPercentageBy` 返回 -1，`:2072`） | 验证回退；对照 probe T2 |
| P8 日期分组 | 行分组 `date`：`year`/`month`，`interval=1` 与 `>1`、含 null 日期 | `interval=1` 快速，`>1` 回退 | 边界、null 日期 |

第一轮范围建议 P1、P2、P3、P6（只记录）；P4、P5、P7、P8 之后做。其余候选：多种汇总公式（`countDistinct` 在快速路径转成 `DistinctCount`，`:1577`）、两层嵌套行分组、行列分组横向展开、排序按汇总值、`fillBlankWithZero`、计算字段作聚合（`:887-893`）、带 N 或两列公式、合计/小计。

**待决定：** ① 底表和 vs 在哪建（vs 存在备份 zip 的 blob 里，手写风险大，建议在 Composer 界面建好、导出备份后替换 zip，需要有人操作界面）；② P6 先只记录输出是否同意；③ 第一轮是否按 P1/P2/P3/P6 做。

**快速路径回退条件**（任一不满足静默回退脚本；`createCrosstabAssemblies` 外的 `catch(Exception)` 吞异常也会回退，`:136`）：
- 非明细；`quickCheck`（`:2083`）：列绑定单元格只能是汇总/分组，分组排序不能是原始顺序，**整表不能有公式单元格**，分组展开必须是 H 或 V；
- 同一列不能在嵌套分组里绑两次；行头与列头不能有同名分组；每个分组节点至多一个分组子节点；分组列必须在底表列选择里；
- TopN 带 Others 只允许最内层（`:1517`）；命名分组、日期 `interval>1` 回退（`:1527`）；行列头重复、聚合列与分组列同名回退；行列头都为空或没有聚合回退；
- `getPercentageBy==-1`（`:997`）和 `allCellUsed` 为 false（`:1958`）回退；合并后中间有被跳过的列回退（`:796`）。
- `getPercentageBy`（`:2054-2078`）：`OF_ROW_GROUP`/`OF_ROW_GRANDTOTAL` 归按行，其余百分比归按列，方向混用返回 -1；全无百分比返回 0 不回退。
- `allCellUsed`（`:1958-2031`）：只查未被合并区域吞掉的格；① 分组格既不在行头树也不在列头树；② 有 TopN 时，汇总格只在行头树而列方向有 TopN、只在列头树而行方向有 TopN、或两边都不在而任一方向有 TopN。判断依据是汇总格在哪棵树里，不是几何位置；`buildTree` 怎么挂汇总格没核对，触发布局要实测。
- `splitLayoutByColumns`（`:827-862`）：列头树根的直接子节点里有 ≥2 个分组格时，每个数据列克隆一份布局各自变成临时交叉表，由 `mergeCrosstabs`（`:433-466`）按行下标追加。代码不保证与单个交叉表一致（未验证）。

**datatest 实际走哪条路径**（静态 + 本机 DEBUG 日志，按 vs 资产 id 把日志行归到最近的用例，归属近似）：
- 静态：86 个 vs 共 182 个 CalcTable，97 个一定走脚本（90 个含公式脚本单元格，7 个含明细列单元格），85 个满足单元格级条件、可能走快速（树结构条件没查）。
- `Freehand_Spec`（87 个 vs×组件）：只脚本 58、只快速 19、**同一用例里两条都走过 10**。
- `FreeExecution_Spec`（126 个）：只脚本 110、只快速 13、两条都走 3。
- `Convert_Spec`（含转换后的 CalcTable）：只脚本 26、只快速 36、两条都走 4。
- 两个 spec 合计脚本路径约 79%，快速路径至少出现过约 21%（45/213）。
- "两条路径都走过"是 G3 最直接的证据：同一组件在同一用例内会在两条路径间切换，现有 PNG 无法区分。日志不带书签信息，哪次调用对应哪个书签未归因；213 大于静态 182，说明归属有重叠，数字只看比例。

**G1 现状（2026-11-26）：** 63 个用例已加 `executeVS` + `compareData(null)`（`Convert_Spec` 没改）。缺的 57 个用例共 209 个 TXT 基线已在本机生成，放在 `vsothers/src/test/resources/expectData/vsfreehand/`，与已提交的 21 个共 230 个；本机连续跑两遍逐字一致。
- 抽查（独立重算，数据内嵌在资产里的用例）：`Freehand_manual`、`Rank4`、`FScript3` 共 5 个文件数值全部一致；其余基线只验证了稳定性，数值未独立核对。`Rank4/Crosstab1` 中 NJ、PA、FL 的 Sum(customer_id) 并列为 16，基线取 FL、NJ，并列时选谁没有规则，换版本可能变。
- 生成基线：`update.baseline` 只在 `DcViewsheetTest` 实现，这两个 spec 不识别。先跑一遍两个 spec（数据库可达时约 5 分钟，会因缺基线和 PNG 像素差异失败），再把 `vsothers/target/test-classes/exportData/vsfreehand/<Spec>/<用例>/*.txt` 拷到 `src/test/resources/expectData/vsfreehand/` 对应位置（只拷缺的），审阅后提交。数据库必须可达：hosts 里 `mysql_server`、`db2_server` 等要指向能连的机器，否则每个用例卡十几分钟。PNG 本机有像素差异，CI 失败后再替换。
- 有图表的 vs，`executeVS` 还会导出 `ChartN.png/txt`，`compareImage(null)` 会把图表 PNG 纳入而失败，应写 `compareImage([vs 名] as String[])`；不要在本机用 `-Dupdate.baseline.png=true` 生成图表 PNG 基线。
- 待作者确认：`FSTopNTotal/Crosstab1` 的 `Total` 行是 2651，显示的三个州合计 2043，因为单元格公式 `sum(data['customer_id@reseller:$reseller'])` 不受州 TopN 限定。若意图是只汇总显示的州，改公式，基线随之更新。

### 计算字段（G20）

Java 已覆盖（`CalcTableVSAQueryPathAgreementTest` CF1–CF9，快速/脚本两路径逐格对照）；已报 #82231、#82412。聚合型计算字段在 Freehand 里怎么用不清楚，没覆盖。datatest 只补 Java 做不到的转换路径（交叉表带计算字段转 Freehand，走 `VSCalcTest.checkConvert`），建议 3 个 vs：计算字段作字符串分组、日期计算字段按月分组、`WeightedAverage` 权重为计算字段（后者等 #82231 修复）。资产要在加载 `vsfreehand-storage.zip` 的栈里造，再整包导出替换 zip。

## 6. 局限与待确认

**未验证：**
- PNG 层：本机 80 个用例像素全部不一致，不能证明 PNG 基线与产品输出一致；CI 实际结果没查。`CroTable2` 的超时只有本机数据（86.7 秒）。
- 孤儿判断是静态的（资产 id 字面量对 zip）；"没有带计算器的转换用例"是静态扫描，计算器若存放在别的节点会漏统计。
- G18–G20 的数字（355 条公式、17 个零覆盖函数、3 个含 calcfield 的资产、日期级别统计）都是静态扫描 zip，没运行；`q2[...]` 的语义只在个别公式里见到。
- 转换分支清单的"有"是静态上至少 1 个 vs 会走到；哪个组件才是被转换的组件没有逐一对上（探测里是 51 个组件）。
- 产品单测盘点只做了按类名/引用的静态统计，没有逐项审读现有 `CalcTableScopeTest` 等的断言内容。

**已查明：**
- 快速路径与脚本路径都用同一套单元格脚本，区别是底表（预聚合 crosstab vs 明细表）。回退条件说明开发意图是"只在结果一致时走快速路径"，但代码没有把"必须等价"写成声明，也没说不一致时以谁为准（静态推断）。
- TopN/Others/分组排序在脚本路径下由 `LayoutTool.createGroupCond`（`:1352-1440`）生成，快速路径对应 `CalcTableVSAQuery.createCrosstabHeaders`（`:1507-1514` 把 TopN 转成 crosstab 的排名和 Others 选项）。两处各实现一遍，是 G3 等价风险所在。
- 转换时清除计算器、钻取、日期比较：产品设计，保持；"测试里怎么表示"见 probe 文档 T5。

**仍需作者确认：**
- `Convert_Spec.CroTable2` 里 `Thread.sleep(70000)`（已解决，2026-11-27）：提交 `52fc77e7` 加入，没有说明目的（该提交还加了 `@Timeout(60)`，比 sleep 短，sleep 每次都被超时中断；`bfe85d92` 才把超时改成 80 秒）。实测：去掉后 `CroTable2` 以及后面 `CTable1`、`CTable2` 转换前后的 PNG 与带 sleep 时逐像素一致，数据检查结果不变；已连同 `@Timeout` 一起去掉，该用例耗时从约 93 秒降到约 22 秒。vsothers 的 spec 只有这一处 sleep。

**需要用户决定：**
1. 先做哪类：建议先机制层（G1、G21，不依赖开发），再双路径对照。
2. ~~4 个孤儿 vs 补用例还是删除~~ 已删除（2026-11-27）。
3. TXT 基线由谁人工核对。

**合并前待办（在 GitHub 处理）：**
1. **57 个推广用例缺 TXT 基线。** `Freehand_Spec` 32 处、`FreeExecution_Spec` 31 处 `compareData`，已提交 TXT 基线 21 个文件，63 个用例共需 230 个；缺基线的用例一跑就失败，CI 表现为 UNSTABLE。处理：按 §5 命令生成并人工审阅，或把 57 处推广从该 PR 拆出，只留 4 个试点。
2. **CI 上 PNG 层真实状态未知。** 查法：`gh run list --workflow test-vsothers.yml --branch Stateless-Sessions1 -L 5`，再 `gh run view <id> --log` 搜 `diff percent` 与 `FAILED`。如果 CI 上这 80 个也失败，G1 的前提"PNG 是现有防线"要改成"现有防线实际失效"。

## 7. Redmine bug 对照与补充缺口

**口径：** Redmine（Product 项目）标题含 "freehand" 的 Bug 共 1351 条；去掉 Rejected/Ignored 后 1174 条，按修复版本：13.x 520、12.x 82、14.x 44、stylebi-* 35、ai-* 14、未标版本 479。重点是 **StyleBI 线 2025 年起的 35 条**：读了标题，并用 `gh` 查了修复 PR（`inetsoft-technology/stylebi`）改了哪些类、有没有带单测（只看 PR 是否改了 `src/test`，`stylebi-wiz` 的回归测试没查）。2023–2024 年只读了 75 条标题；更早的和 13.x/14.x 线没有逐条读，**是否适用于 StyleBI 2.1.0 未验证**。

**结论：**
- 最近一批 bug（#75630、#75636、#75647、#75665、#75747、#75799、#75801）根因集中在"转换生成的公式"与"快速路径结果表"之间的取数：表里只剩表头或数据丢失，原因是公式引用了结果表里不存在的列名/维度名。这类错误正是 G2 能抓的，所以 G2 里已确认一致的 32 个组件值得锁成断言。
- 这批修复只有 2 个 PR 带了产品单测（#75647 的 PR 4261、#75747 的 PR 4445）；PR 4224（#75630、#75636）、4300（#75665）、4426（#75799）只写了"手工复现"。
- 出现"修复后回归"链：#62548→#79806、#62888→#80507（由 #79806 的修复引起）、#76829→#77237、#75747→#75801。datatest 里 #62548 有 `FSNone1`，#62888 没有（G15）。

| 主题 | bug | 修复带产品单测 |
|---|---|---|
| 转换 / 快速路径取数 | #75630、#75636、#75647、#75665、#75747、#75799、#75801（相关：#75588、#75746） | 2 / 5 个 PR |
| 脚本 / 作用域 | #75576、#75593、#75595、#75609、#75663、#75676、#78247、#78493 | 8 / 8 |
| 导出 | #75748、#76829、#77237、#79581 | 3 / 4（#75748 没有） |
| 锁 / 停滞 / 交换文件 / 集群 | #76905、#77909、#78115 | 3 / 3 |
| 格式 | #78092 | 1 / 1 |
| Show Details | #76564（取数失败时 NPE） | 0 / 1 |
| 性能 | #75614、#75625 | 1 / 2 |
| 编辑器 / 会话 / 批注（UI） | #72576、#72880、#75353、#75820、#75825、#78560 | 0 / 6，不在 datatest 范围 |

### 补充缺口

| # | 缺口 | 依据 | 会放过什么 | 方向 | 优先级 |
|---|---|---|---|---|---|
| G10 | 列名含特殊字符（`, ? @ { } = ; &`、引号、反斜杠）转换后公式里的列名转义错 | #75665（`escapeColName` 多写一个反斜杠，列名 `YYYY, MMM D` 转换后整列空白，PR 4300 无单测）；2023 年 #60608、#60816 同类 | 整列空白。datatest 只有 `NG_specialChar`（命名分组成员含单引号），没有任何 vs 的列名含逗号等字符（86 个 vs 里 freehand 绑定的列名只有这 1 个含特殊字符） | 单测（已做）+ Java 两条路径对照（已做 2026-11-27，发现 #82472）；dashboard vs 不做（界面不允许输入） | 高，已完成 |
| G11 | 聚合列本身是预聚合列名（`Min(ftime)`、`Concat(fecid)`）的交叉表，转换后只剩表头 | #75799（`getAggregateColumn` 总是剥一层 `formula(...)`，PR 4426 无单测） | 工作表列名已带聚合函数的交叉表转换后丢行且无提示 | 单测（已做）+ dashboard 1 个代表 vs | 高 |
| G12 | 嵌套日期分组维度转换后无数据（`Month(ndate)` 在另一个维度下）；首次修复回归后重做 | #75747、#75801；`LayoutToolTest` 已覆盖 | datatest 已有 `GroupAndTab` 和 `CroTable7`，G2 里这两个 vs 的组件转换前后一致 | 无需新增用例，G2 断言即防线 | 高 |
| G13 | TopN/Others + 横向展开分组 + 快速路径：结果表里维度名解析不到，列和聚合值丢失 | #75630（PR 4224 无单测）、#75636；2024 年 #65744、#65763、#61734 | 快速路径与脚本路径对同一布局算出不同结果；`TableArray.hasMember`、`NamedCellRange` 对 `CrossFilter` 底表的裸列名解析出错 | 单测（G3，`NamedCellRangeTest` 已覆盖）；dashboard 已有 25 个含 TopN 的 vs，G1 推广后有数据断言 | 高 |
| G14 | Show Details：取数失败 NPE；Others 单元格、命名分组+时间序列时报错；导出 CSV 为空 | #76564（`isEmptyCell` 对空 lens 解引用，PR 5154 无单测）；#58582、#62417、#65163 | 点 Show Details 崩溃。datatest 的 `RuntimeViewsheetResource` 只有 `showDetailOnChart`，没有表的封装 | 单测（已做）+ dashboard `showDetailOnTable` helper | 中 |
| G15 | 表脚本把组件值（`ComboBox.selectedLabel`、`RadioButton.selectedObject`）赋给 freehand 单元格 | #66427、#80507（13.x 线）；#79806 是 #62548 的回归 | 书签/选择状态下脚本读不到组件值，显示占位文字。86 个 vs 里 0 个脚本引用组件值 | Java 回归用例（已做 2026-11-27）：2.1.0 主干已有写穿修复（`CalcWriteTableLens`），去掉它会复现 #80507；无需 dashboard vs | 中，已完成 |
| G16 | 导出：表样式的"表尾行"颜色串到数据行 | #75748（`ExportUtil.getColType` 调用了 `setTrailerColCount`，应为 `setTrailerRowCount`，PR 4402 无单测）；#76829、#77237、#79581 已有单测 | 导出 PDF/PNG/Excel 时数据行被画成表尾行深色；viewer 不受影响 | dashboard（转 vsexport）；单测（已做） | 中 |
| G17 | 列数上限下的 freehand | #63594 一组：#63944、#64329、#64892、#65438 | 开启列数限制后合并展开单元格错位、HTML 导出无数据 | dashboard（低），用 `@SreeProperty` 设置，是否仍存在未验证 | 低 |

**没有新增缺口：** 脚本/作用域、锁/停滞/集群、格式（产品单测已覆盖）；转换后格式丢失与百分比（`Convert_Spec` 的 `PercentFormat`、`CroTable7`、`CroTable8` 已覆盖，`CroTable8/Crosstab2` 差异见 probe T2）；数据源形态 SQL 视图/物化视图（#60468、#60460、#59433，zip 有 11 个 SQL 工作表 vs，`mv` 模块已有 Freehand MV 用例）；UI/编辑器/会话/批注（composer 回归测试负责）。

**局限：** #79806、#80507、#66427 属 13.x/14.x 线；2026-11-27 核对：`origin/main` 的 `TableDataVSAScriptable` 已有对应修复（`CalcWriteTableLens`，随 #81610 进入），G15 在 2.1.0 上不受影响。

## 附：数据来源与复现

```
# 脚本能直接适用的部分（vs/书签/组件类型/绑定表）
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vsfreehand-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vsfreehand-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vsfreehand/<Spec>.groovy vsothers/src/test/resources/expectData/vsfreehand/<Spec>

# 实际运行（本机 Windows）
./mvnw package -pl commons -DskipTests
./mvnw test -pl vsothers -Pvsfreehand

# 查看 CalcTable 走快速路径还是脚本路径（测试 classpath 默认没有 SLF4J 绑定，需要临时加一个；本机 slf4j-simple 在 D:/m2-1）
# 日志行：`Use 1 crosstab(s) to support formula table "X".` / `Use detail table to ...`
./mvnw test -pl vsothers -Pvsfreehand -Dtest=Freehand_Spec \
  -Dmaven.test.additionalClasspath=D:/m2-1/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar \
  -Dorg.slf4j.simpleLogger.log.inetsoft.report.composition.execution.DebugCalcCrosstabCheck=debug
```
