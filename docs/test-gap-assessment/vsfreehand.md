# vsfreehand 测试缺口与补充方案

> 日期：2026-11-10　模块：`vsothers`，profile `-Pvsfreehand`，spec `Convert_Spec` / `FreeExecution_Spec` / `Freehand_Spec`（`vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vsfreehand/`），共用资产 `vsothers/src/test/resources/config/vsfreehand-storage.zip`
> datatest 基线：`Stateless-Sessions1`（`ee63561b`）；产品源码：`main`（`f0ef913306`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 一致）

## 1. 结论

vsfreehand 有 82 个用例、82 个 vs，规模不小，但**几乎全靠整页 PNG 做断言**：80 个用例只比 PNG，只有 2 个用例（`FSDeclaredVar`、`FSFormatNamedCell`）同时比单元格文本。这意味着 Freehand 表的单元格取值只要不影响渲染像素，就不会被发现；PNG 基线又是回归基线（由产品输出生成，不是独立算出的答案）。

转换（Convert）这一半同样如此：转换后只比 `_CALC.png`，没有"转换前后数据一致"的检查，而这恰好是产品代码里写明的意图（`ComposerVSTableService.java:479`，"make crosstab data is same with calc"）。

产品仓库里 Freehand 的单测很多，但集中在锁、超时、百分比格式、布局回写；**转换类（`VSTableConverter`、`ComposerVSTableService.convertToFreehandTable`）没有任何单测，`CalcTableVSAQuery` 的"临时 crosstab 快速路径 vs 脚本路径"没有数据正确性测试**。

另外三类风险（G18–G20，静态扫描）：转换生成的公式文本没有断言；手写公式脚本只用到 `CalcTableScope` 25 个聚合函数中的 8 个（17 个零覆盖，`average` 也没有）；calcfield（尤其 Aggregate 类型）在 Freehand 里只有约 1–3 个资产，日期类型没有系统对比。

转换这条线的复查（§2 "转换分支清单"）：原先"转换来源聚合只有 Sum/Count/none、没有带计算器的转换"是错的（实际公式 14 种、6 个 vs 带计算器、2 个有真实日期比较）；真正没覆盖的是 16 个转换分支里的 4 个（空布局、预聚合列名、`sortOthersLast=false`、以另一个 vs 组件为源），另有 4 个未验证；另外 `allowedDiff` 只约束行数（G21），会把值错误一起放过。

先做：① 给 Freehand/FreeExecution 用例补 `executeVS`+`compareData`（机制层，已有 2 个先例）；② 给 `VSCalcTest.checkConvert` 加"转换前后数据对比"；③ 开发确认语义后补 `CalcTableVSAQuery` 双路径等价单测。

## 2. 覆盖情况

**规模：** 82 个用例（Convert 19，其中 1 个 `@Ignore`；FreeExecution 31；Freehand 32）；zip 内 86 个 vs、29 个工作表、58 个书签（35 个书签库，其中含 `(Home)` 的默认条目）；基线文件共 104 个（PNG 101：Convert 38、FreeExecution 31、Freehand 32；TXT 3：全在 FreeExecution）。
**孤儿 vs（三个 spec 取并集，用例的资产 id 全部是字面量，82 个 `new VSCalcTest(` 对 82 个 id，无 "Not mapped"）：4 个**
- `ConvertToCalc/Crosstabs/VSScriptEffect`（与 `ConvertToCalc/VSScriptEffect` 同名重复，后者对应的用例 `@Ignore`，`Convert_Spec.groovy:282`，注释 Bug #58711 "redesign the case"）
- `FreeExecution/BindingOptions/ComBindingTable6`（6 个 CalcTable + 1 个 Table；书签 `noRangeID`、`quarter` 因此也没用到）
- `FreehandTable/Others/freehand none level`
- `FreehandTable/Relation/NG4`（书签 `bk1`、`bk2` 同样没用到）

**覆盖矩阵**（维度：来源 × 单元格内容 × 数据源，已与用户确认。数字由 `vs_backup_scan.py inventory` 与一份临时统计脚本（未入库，口径见附录）得出）：

*来源 × 数据源（vs 个数，含 4 个孤儿）*

| 来源 \ 数据源 | 逻辑模型（直连） | 工作表：SQL | 工作表：库表/模型绑定 | 同上 + 变换（join/旋转/unpivot/concat） | 嵌入/快照 | 嵌入 + 变换 | 合计 |
|---|---|---|---|---|---|---|---|
| FreehandTable（原生绑定） | 10 | 5 | 4 | 0 | 14 | 1 | 34 |
| FreeExecution（原生绑定） | 3 | 5 | 3 | 18 | 2 | 1 | 32 |
| ConvertToCalc（表/交叉表转换） | 1 | 1 | 2 | 11 | 5 | 0 | 20 |
| 合计 | 14 | 11 | 9 | 29 | 21 | 2 | 86 |

另有 11 个 vs 的 Freehand 表绑定的是同一 vs 里的另一个组件（`sourceInfo type=64`：FreeExecution 6、FreehandTable 5）。zip 内没有 cube/OLAP 源的 vs（静态判断，只看了 `worksheetEntry`）。

*单元格内容特性（含该特性的 vs 个数；ConvertToCalc 列统计的是 vs 里原本就有的 CalcTable，转换产生的单元格运行时才有，这里看不到）*

| 特性 | 全部 | ConvertToCalc | FreeExecution | FreehandTable |
|---|---|---|---|---|
| 列绑定-分组 | 63 | 3 | 30 | 30 |
| 列绑定-汇总 | 60 | 3 | 30 | 27 |
| 列绑定-明细 | 5 | 0 | 5 | 0 |
| 文本单元格 | 49 | 1 | 28 | 20 |
| 公式脚本-分组 / 汇总 / 明细 | 37 / 41 / 21 | 0 / 0 / 0 | 19 / 21 / 19 | 18 / 20 / 2 |
| 日期级别分组 | 44 | 3 | 23 | 18 |
| TopN（>0） / 带 Others / 反向 | 25 / 21 / 5 | 0 / 0 / 0 | 15 / 11 / 2 | 10 / 10 / 3 |
| 展开 横向 / 纵向 | 57 / 64 | 2 / 3 | 27 / 31 | 28 / 30 |
| 合并单元格 / 有跨行跨列 | 40 / 51 | 1 / 1 | 19 / 26 | 20 / 24 |
| 脚本 `data[...]` / `rowValue` / `toList` | 43 / 33 / 38 | 0 | 22 / 18 / 20 | 21 / 15 / 18 |
| 脚本用参数/`runQuery` | 2 | 0 | 2 | 0 |
| 单元格自带数据源 | 17 | 1 | 7 | 9 |
| 条件格式（highlight） / 超链接 | 23 / 2 | 0 / 0 | 16 / 0 | 7 / 2 |
| 时间序列（timeSeries） | 3 | 0 | 2 | 1 |
| 分组排序非升序 / 按列排序 | 29 / 2 | 0 | 18 / 2 | 11 / 0 |
| 分页（pageAfter） | 0 | 0 | 0 | 0 |

编码对照产品源码：`CellBinding.java:40-65`（type 1 文本、2 列、3 公式；btype 1 分组、2 明细、3 汇总）。单元格只统计 `assemblyInfo` 下的一份（`state_calctable` 里有重复拷贝）。未统计：NameGroup 的使用（`groupSort` 里看不出）、`fmode`（全为默认 -1）。

*转换来源（`ConvertToCalc`，20 个 vs，静态扫描 + 本轮复查）：* 交叉表来源 17 个 vs，表来源 5 个（含嵌入表 1 个）。**本轮复查更正了原先三处错误统计**（原先读的是派生的 `ainfo` 节点，复查改读用户设置的 `VSAggregateRef`、`<calculator>`、`<dateComparison>`）：
- 聚合公式：原写"只有 Sum/Count/none"，**实际 14 种**：Sum、Count、Max、Min、Average、DistinctCount、First、Last、Median、Concat、Covariance、WeightedAverage、NthLargest、NthMostFrequent（另有 none）。`AggregateFormula` 共 27 个，**转换来源没有的**：Mode、Correlation、Variance、StandardDeviation、PopulationVariance、PopulationStandardDeviation、Product、NthSmallest、PthPercentile、SumSQ、SumWT（Aggregate 是特殊项，未统计）。
- 计算器：原写"未发现"，**实际 6 个 vs 带计算器**（`CroTable7`、`CroTable8`、`PdateManulCroTable1`、`PercentFormat` 带 PercentCalc；`DCPTrendTable1`、`ComTable2` 带 ChangeCalc；`CroTable8` 还有 Running Sum）。没有的计算器类型未枚举。
- 日期比较：原写"仅 1 个"，**实际 2 个 vs 有真实周期设置**：`DCPTrendTable1`（`StandardPeriods`）、`ComTable2`（`CustomPeriods`）；其余的 `<dateComparison>` 是空节点。钻取仍是 1 个（`DrillCroTable1`）。
- 其他形态（至少 1 个 vs 出现，已去掉状态副本的重复）：行头最多 3 层（`CroTable8`，1 个 vs），列头 2 层（3 个 vs）；总计 4 个 vs、小计 3 个 vs；TopN/BottomN 约 6 个 vs，其中带 Others 的 6 个；`fillWithZero=true` 4 个 vs；`mergeSpan=false` 3 个 vs；时间序列 3 个 vs；无聚合的交叉表 1 个 vs；日期级别含普通级别 0–8 与日期部分级别 513/518/521。

**转换分支清单（步骤 3b-C，来源 `VSTableConverter.java`、`ComposerVSTableService.java:460-565`、`VSLayoutTool.java:752-790`）。"有"= 静态上至少 1 个转换来源 vs 会走到，不等于已验证数据正确——数据一致性看 `vsfreehand-g2-convert-probe.md`。**

| # | 分支 | 位置 | 会走到的资产（静态） | 状态 |
|---|---|---|---|---|
| 1 | 普通表 → Calc（表头合成一个、明细区复制） | `VSTableConverter:105-174` | 5 个表来源 vs | 有；探测里 4 个组件行集不一致（T1，待查） |
| 2 | 布局为空（无绑定的表）/ 源表为空 | `:63-67`、`ComposerVSTableService:550`、`VSLayoutTool.syncCellFormat` 空源分支 | 无 | **无** |
| 3 | 交叉表无聚合 → `convertAsPlainToCalc` / `arefs` 为空 | `:205`、`:424` | 1 个 vs | 有（1 个），一致性未单独核对 |
| 4 | 表头/汇总表头 → 文本；fake 列 → 文本 | `:284`、`:307` | 前者全部；fake 列未统计 | 前者有；fake 列**未验证** |
| 5 | 多层行/列头的 cellName 复用、合并单元格 span 复制 | `:335-352`、`:456-502`、`:97` | 3 层行头 1 个 vs；`mergeSpan=false` 3 个 vs | 有；明细区合并 span（`:374`）**未验证** |
| 6 | `fixOrderInfo`：日期型保留 interval，非日期清零 | `:518-543` | 日期级别 0–8、513/518/521；范围分组 | 有（静态） |
| 7 | `fixTopN`：非最内层维度关闭 Others | `:548-600` | TopN 约 6 个 vs 带 Others，**外层维度带 Others 的未核对** | **未验证** |
| 8 | 分组列不在 `ainfo` 时 `getOriginalColumn` | `:368` | 未统计 | **未验证** |
| 9 | 汇总格所处区域：明细/小计(G_FOOTER)/总计(FOOTER) | `:393-412` | 总计 4 个 vs、小计 3 个 vs | 有 |
| 10 | 百分比：`perBy`(列/行) × 选项(OF_GROUP/OF_GRANDTOTAL) 四种组合 | `:431-446` | OF_GRANDTOTAL 5 个 vs，OF_GROUP 仅 `DCPTrendTable1`；`perBy` 两个值都有 | 四种组合**是否各有 1 个未逐一核对**；`CroTable8` 的百分比转换后数值变了（T2） |
| 11 | 公式列名还原：预聚合列名（`Min(Min(ftime))`）、双列/带 N 的公式取第一列 | `VSLayoutTool:772-779`、`:784` | 预聚合列名**无**；Covariance、WeightedAverage、NthLargest、NthMostFrequent 有；Correlation、NthSmallest、PthPercentile 无 | 预聚合列名**无**（已是 §7 的 G11，bug #75799）；双列公式部分有 |
| 12 | `clearCalculators`（百分比除外）、`clearCrosstabDrill`、`clearDateComparison` | `ComposerVSTableService:479-484`、`:524-526` | 计算器 6 个 vs、钻取 1 个、日期比较 2 个 | 有；清除后值不同归入"允许差异"，见 G21 |
| 13 | `clearDataGroup` 后 `resetDataMap` | `:504-511` | 未统计（什么情形下 `reset` 为真未确认） | **未验证** |
| 14 | `fillBlankWithZero`、`sortOthersLast`（false 一侧） | `:513-514` | `fillWithZero=true` 4 个 vs；`sortOthersLast=false` **0 个 vs** | 前者有，后者**无** |
| 15 | 以另一个 vs 组件为源（`vsSrc`） | `:528-529` | ConvertToCalc 下 0 个 vs | **无** |
| 16 | `confirmed=false` 时的 `fixAggregateInfo` | `:474-477` | 测试固定 `confirmed=true` | 仅探测里对 19 个 vs 比较过两者一致（探测 §4），用例里**不走** |

小结：16 行里**无** 4 行（2、11 的预聚合列名、14 的 false 一侧、15）、**未验证** 4 行（4 的 fake、7、8、13），其余静态上有，但数据是否正确要看探测。这张表决定 §4 的 `VSTableConverter` 单测和 §5 的转换来源 vs 补什么。

*公式脚本与特殊列（静态扫描 zip，`cellBinding type=3` 的 `value` 去重 355 条；未运行确认）：* 函数使用次数与 17 个零覆盖函数见 G19；`toList` 选项：`sort=asc` 72、`sort=desc` 16、`others=leaveothers` 44、`timeseries=true` 21、`distinct=true` 1；`rounddate`：year 93、month 14、hour 17、minute 12、day 10、quarter 10、week 8、second 4、none 5；`date=`：quarter 11、hour 5、minute 4、month 3、second 3。calcfield 与日期类型见 G20。`aggregate="true"` 的 13 处是工作表聚合标志，不是 calcfield 的 Aggregate 类型，不要混淆。

*书签：* 非 `(Home)` 书签 23 个，被用例用到 17 个，未用 6 个（4 个在孤儿 vs 上，另 2 个是 `CM11_1_1/aaa`、`CM11_1_9/bk1`）。带非 Home 书签的用例涉及 15 个 vs（未数用例个数）。用例里**没有一处**通过事件对 Freehand 做选择/刷选，选择状态都来自书签恢复。

**验证机制：**

- 比对遍历"实际导出"，并且额外检查基线→导出：`compareImage(null)`/`compareData(null)` 先在 `ViewsheetTest.groovy:471-477` 检查"基线文件是否都被导出"，再在 `CompareUtil.groovy:471-475` 遍历导出目录逐个比；TXT 导出了但无基线会失败（`CompareUtil.groovy:249-252`，"not found"）；PNG 无基线的行为静态上没读完（`CompareUtil.groovy:133-134`），**未验证**。
- 导出失败会让用例失败：`onExportFailure` → `violation`（`ViewsheetTest.groovy:230-232`，PNG/PDF/executeVS 三处调用 `:223`、`:349`、`:377`）；转换流程在 `VSCalcTest.groovy:50/56/63/74/81` 对转换失败、无可转换组件、未变成 CalcTable、导出失败、导出为空分别抛异常。
- `exportData` 清理：用例构造时按用例清一次（`ViewsheetTest.groovy:86-98`）；CI 在准备阶段整体清：`Jenkinsfile:37`（`./mvnw clean -pl vsothers`）、`.github/workflows/test-vsothers.yml:71`（`./mvnw clean`）。
- CI 对失败的处理：Jenkins `catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE')`（`Jenkinsfile:78`），GitHub `continue-on-error: true`（`test-vsothers.yml:85`）；即 vsfreehand 失败不会让构建失败，只标 UNSTABLE/不阻断。
- **实际运行**（本机 Windows，`./mvnw package -pl commons -DskipTests` 之后 `./mvnw test -pl vsothers -Pvsfreehand`，82 个用例）：**0 通过，80 个 PNG 像素差异，1 个超时，1 个跳过**。
  - 导出异常：0；"基线文件未被导出"：0；"导出了但没有基线"：0；TXT 比对失败：0（`FSDeclaredVar`、`FSFormatNamedCell` 的 3 个 TXT 通过了，Freehand 表导出的 TXT 文件名是组件名，这两个 vs 里 CalcTable 恰好叫 `Crosstab1`/`Crosstab2`）。
  - PNG 像素差异（单独列，本机与 CI 渲染不同）：80 个用例全部不一致，差异率 0.50%–6.02%（日志里 99 条 `diff percent`，无一条为 0）。因此**本机无法验证 PNG 层**，只能确认"没有导出异常、没有缺失/多余文件"。CI 结果本轮没查。
  - `Convert_Spec.CroTable2` 超时：用例里 `checkConvert` 之后有 `Thread.sleep(70000)`（`Convert_Spec.groovy:44`），加 `@Timeout` 80 秒（`:37`），本机耗时 86.7 秒。转换加导出超过约 10 秒就必然超时。该 sleep 由提交 `52fc77e7` 加入，说明是 "modify case to pass when execution"，意图未知。
  - `strictcheck` 对三个 spec 全部是 "Not predicted"（`VSCalcTest` 子类有自己的导出），所以它的预测在这里没用，以上以实际运行为准。
- 基线性质：PNG/TXT 都是回归基线（静态推断：团队文档 `docs/vs/VSTableFreehand/Freehand.xlsx` 只记录用例分布与 bug 编号，没有独立计算的期望值）；未查每个基线的生成记录。

**断言层：**

- Freehand 表是 `TableDataVSAssembly`（`CalcTableVSAssembly.java:48`），`executeVS` 本可以导出它的单元格 TXT，但 82 个用例里只有 2 个调用了 `executeVS`。其余 80 个，单元格值只能靠整页 PNG。
- Convert_Spec 的 19 个用例都是 `exportAsPNG`（转换前）+ `checkConvert`（转换并导出 `_CALC.png`）+ `compareImage`，**没有任何数据级比对**，也没有"转换前后单元格一致"的检查。
- 关键对象是否出现在基线：整页 PNG 里"可见的"都在；不可见的（被裁掉的行列、隐藏列、格式化前的原值、超链接目标）不在。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 80/82 用例只比整页 PNG，没有单元格数据断言 | 单元格值错误但渲染差异小于容差、值在被裁掉/隐藏的行列、格式化前后的值不同；另外 PNG 基线跨平台不一致，换机器更新基线可能把错误值固化 | 机制（改 spec/基类，加 `executeVS`+`compareData`） | 高 |
| G2 | Convert 只比 `_CALC.png`，无"转换前后数据一致"检查 | 转换后某些行/列、合计、聚合值与原交叉表/表不一致（例如转换漏掉小计、`fixLayoutAlias`/`fixPeridField` 改错列名），而渲染图因为是回归基线被一起"固化" | 机制（改 `VSCalcTest.checkConvert`）。**已做探索并落实为断言**：`VSCalcTest.compareConvertData` 对 51 个组件比较，32 个一致、19 个有差异，其中 12 个是设计行为（允许差异），7 个转入 TODO，见 [vsfreehand-g2-convert-probe.md](vsfreehand-g2-convert-probe.md) | 高 |
| G3 | `CalcTableVSAQuery.getTableLens` 有两条路径，区别在**单元格脚本读的底表**：快速路径先把布局转成临时 crosstab（`createCrosstabAssemblies`），用预聚合的 crosstab 数据当底表；不满足条件则用明细表当底表，在脚本里分组聚合（`CalcTableVSAQuery.java:125-184`）。两条路径都由 `CalcTableLens.process()` 求值单元格。产品单测 12 个文件都是锁/格式/回写/临时名，**没有两路径数据等价的测试**，`createCrosstabHeaders`、`quickCheck`、`createCrosstabAssemblies`、`LayoutTool.createGroupCond` 在 `src/test` 里按名字都找不到 | 同一个 Freehand 布局，快速路径与脚本路径算出不同的值（例如 TopN、Others、空分组、合计行）；datatest 只对每个 vs 的当前路径做 PNG 快照，路径选择条件改了不会发现 | 单测 | 高 |
| G4 | 转换链 `VSLayoutTool.generateLayout` → `VSTableConverter.changeTableMode` → `ComposerVSTableService.convertToFreehandTable` 在产品仓库**没有任何单测**（三个类按类名在 `src/test` 都找不到；`VSLayoutTool` 仅被 3 个无关测试引用） | 转换静默改变数据：清掉计算器（`:524-526`）、清钻取和日期比较（`:479-484`）、复制 fillBlankWithZero/sortOthersLast/mergeSpan（`:513-516`）这些分支一旦改错，只有靠 Convert_Spec 的 PNG 才可能发现 分支覆盖见 §2 "转换分支清单"：16 行里 4 行无 vs、4 行未验证 | 单测（布局部分）+ dashboard（G2） | 高 |
| G5 | `RuntimeCalcTableLens`（1559 行，展开/分组后处理）单测只有一个序列化用例（`RuntimeCalcTableLensTest`，49 行）；其余引用它的 12 个测试关注锁/停滞/超时 | 横向/纵向展开、分组排序（升/降/按列/手动）、TopN+Others、日期级别分组、空分组、合并单元格的取值错误 | 单测 | 中（需开发先确认逻辑在哪个类，见 §6） |
| G6 | 交互只有"书签恢复"，没有对 Freehand 做选择/刷选后重新执行的用例 | 选择变化后 Freehand 没刷新、缓存没失效（书签恢复是从头执行，覆盖不到增量刷新） | dashboard | 中 |
| G7 | 转换来源形态：公式 14 种（缺 Mode/Correlation/Variance/StandardDeviation/PopulationVariance/PopulationStandardDeviation/Product/NthSmallest/PthPercentile/SumSQ/SumWT）；计算器只有 Percent/Change/RunningSum 三类；以另一个 vs 组件为源、`sortOthersLast=false`、空布局、外层维度带 Others 的 TopN 都没有（见 §2 分支清单；预聚合列名见 §7 的 G11）。**原先"聚合只有 Sum/Count/none、未发现带计算器的转换"是错的，已更正** | 转换时对这些形态的列名还原、计算器清除、TopN/Others 处理出错 | dashboard（新建 vs）+ 单测（G4） | 中 |
| G8 | 4 个孤儿 vs、1 个 `@Ignore`、6 个未用书签；`CroTable2` 的 70 秒 sleep 让用例对机器速度敏感 | 孤儿/被忽略的 vs 对应的场景（ComBindingTable6 的 quarter/noRangeID 书签、NG4）实际没有被测；CroTable2 在慢机器上偶发失败，被当成噪声忽略 | 机制 + 清理 | 低 |
| G9 | 没有分页（pageAfter）、cube/OLAP 数据源的 Freehand 用例 | 分页符、cube 源下的取值错误（是否支持 Freehand over cube 未验证） | dashboard，低优先；pageAfter 可能更适合 vsexport | 低 |
| G18 | 转换产生的单元格公式文本没有断言：转换器生成 `sum(data['…@…'])`、分组条件等脚本，现在只比 PNG 和（G2 的）转换前后数据 | 数据碰巧相同但公式写错（换数据、换分组、加筛选后才暴露）；转换前的组件值本身错时，两边一起错检查不出来（期望值不独立） | 单测（`VSTableConverter` 逐单元格断言 type/btype/expansion/formula，并入 G4；预聚合列名、特殊字符列名两类转换 bug 已在 §7 的 G10/G11 记录）+ dashboard（G2 的比较之外，加手算期望值的小 vs） | 中 |
| G19 | 手写公式脚本的函数面窄：zip 内去重后 355 条公式单元格，`sum` 176、`toList` 145、`mapList` 50、`inArray`/`inGroups` 19/13、`count` 9、`nthMostFrequent` 8、`nthLargest` 6、`none` 6、`countDistinct`/`max` 各 2、`rowList` 1。产品 `CalcTableScope` 暴露 25 个聚合函数，**17 个一次都没用过**：`average`、`min`、`product`、`concat`、`standardDeviation`、`variance`、`populationVariance`、`populationStandardDeviation`、`median`、`mode`、`correlation`、`covariance`、`weightedAverage`、`first`、`last`、`pthPercentile`、`nthSmallest`。脚本写法也窄：`if`/`var`/`&&` 总共几十处，参数脚本 11 处，`runQuery` 极少 | 这些函数在 Freehand 里的取值错误（含空值、除零、字符串列做数值聚合、跨单元格引用 `q2[...]` 出错）；含公式单元格的 vs 一定走脚本路径（见 G3），脚本路径的函数覆盖越窄，G3 的路径等价风险越大 | 单测（`CalcTableScope` 未覆盖函数，期望值手算）+ dashboard（新建 vs，按函数组） | 高 |
| G20 | 特殊列几乎没有覆盖：calcfield 在 zip 里只有 3 个资产含（1 个交叉表日期型 calcfield、1 个 CalcTable 的 `First(...)` 聚合表达式型 string calcfield、1 个 CalcTable 的 none 聚合）；**Aggregate 类型 calcfield 在 Freehand 单元格里约 1 个 vs，默认类型更少**。日期：日期级别分组 44 个 vs、`rounddate` 覆盖 year…second，但没有 Date/Time/TimeInstant 三种类型的系统对比，没有跨年周/闰日/空值/时区边界，日期型 calcfield 做分组 1 例，转换的日期比较 1 例 | calcfield 作为分组/汇总/明细被 Freehand 取值时类型或聚合时机错误（先聚合还是后聚合）；日期边界分组错位；转换时 calcfield 丢失或类型变化 | dashboard（新建 vs：calcfield × 返回类型 × 单元格角色；同样的交叉表做转换）+ 单测（日期分组边界） | 高 |
| G21 | `checkConvert` 的 `allowedDiff` 只约束行数、不约束单元格：名单里 19 个组件（12 个标 product design）转换后**任何单元格值**都可以不同，用例仍然通过。开发对 B1/B2 的回答针对的是"`Aggregate` 文字"和"日期标签格式"；`CroTable2/Crosstab2` 里 `NthLarge` 34→23、范围分组边界变化是**数值**变化，不在这两个回答的范围内（静态推断，未再向开发确认） | 名单内的组件转换后值错（分组边界错位、聚合值错）被"product design"一并放过；名单越大，G2 的断言越空 | 机制：把名单从"行数"收紧为按列/按类别声明允许差异（例如只允许标签列不同、数值列必须一致）；并向开发确认 B2 里的数值变化是否也是设计 | 高 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测（G3/G4/G5，期望值可以从原始数据手算）；问题出在组件、绑定、数据源、书签之间的接线 → dashboard（G6/G7/G9）；验证机制本身的缺陷 → 改基类/spec，不是加用例（G1/G2/G8）。

## 4. Java 单测补充

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `CalcTableVSAQuery`（`getTableLens`、`createCrosstabAssemblies`） | 同一布局分别走快速路径与脚本路径，逐格比对；期望值从原始数据手算。覆盖：多种汇总公式（Sum/Count/Average/Max/DistinctCount/NthMostFrequent）、两层嵌套分组、横向展开、合计/小计、TopN+Others、百分比 | 12 | 中（要沙箱+工作表；仓库已有 `RowFetchFailureTest`、`CalcTableVSAQueryLayoutWriteBackTest` 的搭建方式可参考） | 已从源码查明哪些布局走快速路径（见 §6 的"源码查明"）；"两路径不一致时以哪个为准"代码里没有声明，需要开发确认。没有强制走某条路径的开关（在 `getTableLens`、`createCrosstabAssemblies0` 里未看到，非全仓搜索），单测要靠布局本身控制 |
| `VSLayoutTool.generateLayout` + `VSTableConverter.changeTableMode` | 交叉表→Freehand 布局：行/列头 1/2/3 层、汇总并排、总计/小计开关、mergeSpan、多聚合；简单表、嵌入表；断言每个单元格的 type/btype/expansion/formula；另补分支清单里"无/未验证"的行：`fixTopN` 外层维度 Others（`:548-600`）、`perBy`×百分比选项四种组合（`:431-446`）、双列/带 N 公式的列名（`VSLayoutTool:784`）、fake 列、空布局（预聚合列名已在 §7.2 的 `getAggregateColumn` 行） | 20 | 中（纯布局无需执行，但要构造 `TableDataVSAssembly` 与元数据） | 无 |
| `ComposerVSTableService.convertToFreehandTable` | 计算器被清除（百分比除外）、钻取被清除、日期比较被清除、fillBlankWithZero/sortOthersLast/mergeSpan 复制、`fixPeridField`、`fixLayoutAlias` | 8 | 高（服务依赖 `ViewsheetService`、集群代理，测试入口未确认） | 清除计算器后数值与原交叉表不同，这是设计如此还是需要提示用户；哪些差异是允许的 |
| `RuntimeCalcTableLens`（展开、分组、TopN、Others、合并） | 展开 横/纵/无；分组排序升/降/按列/手动；TopN n/反向/Others；日期级别分组；空/null 分组；合并单元格；fillBlankWithZero | 18 | 中 | 已查明：脚本路径的分组/TopN/Others 由 `LayoutTool.createGroupCond` 生成条件（`LayoutTool.java:1409-1440`），不在 `RuntimeCalcTableLens`；`RuntimeCalcTableLens` 只在判断 `Others` 单元格类型时读 TopN（`:1041`）。测试对象应改为 `LayoutTool.createGroupCond`（`CalcGroup`/`CalcAggregate`），目前只有 `LegacyBuddhistManualOrderTest` 引用 `CalcGroup`。用例设计表里这一行的被测类需要从 `RuntimeCalcTableLens` 调整，难度待重新评估 |
| `CalcTableScope` / `TableValueList` 的 `toList` 选项串 | `sort=asc/desc`、`rounddate=year…day`、`interval` 的组合与非法值（产品已有 `CalcTableScopeTest` 634 行、`TableValueListTest`，**未逐项审读**，补之前先对照现有用例去重） | 6 | 低 | 无 |
| `CalcTableScope` 的 25 个聚合函数（对应 G19） | 17 个零覆盖函数（`average`、`min`、`product`、`concat`、`standardDeviation`、`variance`、`populationVariance`、`populationStandardDeviation`、`median`、`mode`、`correlation`、`covariance`、`weightedAverage`、`first`、`last`、`pthPercentile`、`nthSmallest`）各至少 1 个正常值用例，加共性边界：空值、全 null、除零、单行、字符串列；期望值从原始数据手算。补之前先对照 `CalcTableScopeTest`（634 行，未逐项审读）去重 | 24 | 低 | 空值/非数值列的预期（返回 0、null 还是报错）代码里没声明，需开发确认 |
| 日期分组（`LayoutTool.createGroupCond`、`toList` 的 `rounddate`/`date`，对应 G12） | year/quarter/month/week/day/hour/minute/second 各级别；跨年周、闰日、null 日期、`interval>1`、`timeseries=true` 补零；Date/Time/TimeInstant 三种类型 | 14 | 中 | 周的起始日、时区取哪个；`interval>1` 时快速路径回退（`CalcTableVSAQuery.java:1527`）与脚本路径是否一致 |

难度：低 = 纯单元；中 = 需要脚本或沙箱环境；高 = 测试入口还没确认。用例数是设计出来的数量，不是已写好的。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 数据层断言（对应 G1） | 改 spec：给 Freehand_Spec、FreeExecution_Spec 的用例补 `executeVS(...)` + `compareData(...)`（已有 `FSDeclaredVar` 先例，`FreeExecution_Spec.groovy:388-399`）；或在 `VSCalcTest` 加一个"导出 PNG 并导出数据"的包装方法，减少 80 处重复 | 每个 Freehand 单元格的文本值 | TXT 基线用 `-Dupdate.baseline=true` 生成，**必须人工核对**，不能直接 `git add`；`executeVS` 对被隐藏的组件会跳过（`ViewsheetTest.groovy:186`） |
| 转换前后数据一致（对应 G2） | 改基类：`VSCalcTest.checkConvert0` 在转换前对每个待转换组件取 `getVSTableLens`，转换后取对应 CalcTable 的 lens，逐格比较 | 转换保持数据；期望值就是原组件自己的输出（独立来源） | 开发先列出"转换允许改变数据"的情形（清计算器、清钻取、清日期比较），这些用例单独声明例外；表头行高设为 0 的用例要跳过隐藏行 |
| 转换来源扩展（G7） | 新建 vs（3–5 个），按 §2 分支清单里"无/未验证"的行：以另一个 vs 组件为源、`sortOthersLast=false`、外层维度带 Others 的 TopN、Mode/Variance/StandardDeviation/Product/NthSmallest/PthPercentile/Correlation 聚合、百分比四种组合各 1 个、空布局（预聚合列名见 §7.2 已列的 vs） | 各形态转换后数据与原组件一致（用 G2 的比较）；期望值来自原组件，不涉及 PNG | 依赖 G2 的比较机制（已落实）；G21 收紧 `allowedDiff` 后再扩，否则新 vs 的差异会被名单吞掉 |
| 选择/刷选交互（G6） | 扩展：对 2–3 个有图表联动的 vs（如 `CM11_1_1`、`CM11_1_2`）用事件做选择/刷选再导出，而不是只恢复书签 | 选择变化后 Freehand 重新执行并刷新 | `ActionEventsUtil` 是否支持选择事件**未查**；先确认再定做法 |
| 清理孤儿与 ignore（G8） | 对 4 个孤儿 vs 逐个决定：补用例（`ComBindingTable6` 的 `quarter`/`noRangeID` 书签、`NG4`）或从 zip 删除；`VSScriptEffect` 按 Bug #58711 的注释重新设计；`CroTable2` 去掉 `Thread.sleep(70000)` 或说明目的 | 孤儿对应的场景被真正测到；CroTable2 不再因机器速度失败 | 需要作者说明 sleep 的目的 |
| 数据源扩展（G9） | 新建 vs：cube/OLAP 源；pageAfter 分页 | 取值与分页 | 产品是否支持 Freehand over cube 未验证；pageAfter 建议放 vsexport 评估 |
| 函数面扩展（对应 G19） | 新建 vs（3–5 个，按函数组）：统计类（average/median/mode/标准差/方差）、取值类（first/last/nthSmallest/concat）、双列类（correlation/covariance/weightedAverage）、脚本控制流（if/var/空值/除零）、跨单元格引用 `q2[...]` | 各函数在 Freehand 里的取值；与 G1 的 `executeVS`+`compareData` 同时用，期望值在单测里手算，这里只比接线 | 依赖 G1 的 TXT 基线机制；TXT 基线必须人工核对 |
| 特殊列（对应 G20） | 新建 vs（3–4 个）：Aggregate 类型 calcfield、默认类型 calcfield，返回 string/number/date，分别作分组、汇总、明细；日期型 calcfield 分组；同样的交叉表做转换 | calcfield 在 Freehand 与转换后的取值；先聚合/后聚合时机 | 依赖 G2 的转换前后比较机制 |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

**G1 试点结果（已做，4 个用例）：** `Freehand_Spec` 的 `CM11_1_3`（带书签 `bk1`）、`bug_topN`，`FreeExecution_Spec` 的 `FNGTable1`（命名分组）、`FSTopNTotal`（TopN + 总计），都在 `exportAsPNG` 前加了 `executeVS`，断言里加了 `compareData(null)`。

- 生成 18 个 TXT 基线（`-Dupdate.baseline=true`，没有写任何 PNG）。连续两次正常运行，导出的 18 个 TXT 逐字一致，且与基线相同；基线里没有异常、本机路径、主机名。4 个用例本机仍因 PNG 像素差异失败，与试点无关。
- TXT 里能看到整页 PNG 看不到或难以审阅的内容：TopN 的 `Others ` 行（带尾随空格，对应 `LayoutTool.java:1436` 的注释，用来区分命名分组的 Others）、命名分组标签、总计行，以及书签对结果的影响（`CM11_1_3` 的 Home 是 2×6，`bk1` 是 8×3）。
- 基线是回归基线，只对"值变了"有效。已做的人工核对：`bug_topN` 四个季度之和 108+158+139+141=546 等于总计；`FSTopNTotal/Crosstab1` 的 Grand Total 2651+10111=12762 一致。**`FSTopNTotal/Crosstab1` 的 `Total` 行：** `false` 分组显示的三个州合计 1060+494+489=2043，但 `Total` 行是 2651。看了 vs 的布局：州分组设了 TopN=3，而 `Total` 单元格的公式是 `sum(data['customer_id@reseller:$reseller'])`，只按 reseller 取数据，不受 TopN 限定的州列表约束，所以 2651 是这个单元格自己的公式决定的（包含未显示的州），不是基线把 bug 固化了。这个用例叫 "agg is none, then change to formula script rule"，用公式单元格汇总全部数据符合它的意图；如果作者认为 `Total` 应该只汇总显示出来的州，那是产品语义问题，改公式即可，基线随之更新。
- 推广到其余用例时的注意点：有图表的 vs，`executeVS` 还会导出 `ChartN.png`/`ChartN.txt`，`compareImage(null)` 会把图表 PNG 也纳入比较而失败，应改成 `compareImage([vs 名] as String[])`（同 `VarExp_Binding_Spec` 的 Crosstab 用例）；不能用 `-Dupdate.baseline.png=true` 在本机生成图表 PNG 基线（本机渲染与 CI 不同）。

**G1 推广（已做 spec，基线未提交）：** `Freehand_Spec` 与 `FreeExecution_Spec` 的其余 57 个用例用同样方式加了 `executeVS` + `compareData(null)`（`Freehand_Spec` 30 个、`FreeExecution_Spec` 27 个；含图表的 `CM11_1_1/2/4/5/6` 的 `compareImage` 改为显式文件名）。至此两个 spec 的 63 个用例都比较单元格数据（4 个试点、2 个原有的 `FSDeclaredVar`/`FSFormatNamedCell`、57 个推广）。`Convert_Spec` 没有改：`executeVS` 只会导出转换前的源表，不是 Freehand，转换后数据的比较属于 G2 的 TODO。

- 推广的 57 个用例**没有提交 TXT 基线**（只提交 spec）。在这些基线补齐之前，这 57 个用例的 `compareData` 会因基线缺失而失败。
- 已用本机运行验证过：`clean` 后用 `-Dupdate.baseline=true` 为 63 个用例生成共 230 个 TXT（含 3 个原有、18 个试点已提交的），再连续两次正常运行，两次导出的 230 个 TXT 逐字一致、与生成的基线相同；没有导出异常、没有基线缺失或多余的导出文件；TXT 里没有异常、本机路径、主机名。63 个用例的失败全部是本机 PNG 像素差异。
- 生成基线的方法：`./mvnw clean -pl vsothers`，再 `./mvnw test -pl vsothers -Pvsfreehand -Dtest=Freehand_Spec,FreeExecution_Spec -Dupdate.baseline=true`，用 `git diff`/`git status` 审阅后提交；基线是回归基线，需要人工核对。GitHub 工作流目前没有传 `update.baseline` 的入口（`.github/workflows` 里搜不到）。

## 6. 局限与待确认

- **没验证的：**
  - PNG 层：本机 80 个用例像素全部不一致（0.50%–6.02%），所以本机运行只证明"无导出异常、无缺失/多余文件、TXT 通过"，不能证明 PNG 基线与产品输出一致；CI 的实际结果没查。
  - `CroTable2` 的超时是本机耗时（86.7 秒），CI 上是否也超时未知。
  - 孤儿判断是静态的（资产 id 字面量对 zip）；"未发现计算器的转换用例"是静态扫描，计算器若存在别的节点可能漏统计。
  - 产品单测盘点只做了按类名/引用的静态统计，没有运行产品仓库的测试，也没有逐项审读 `CalcTableScopeTest` 等现有用例的断言内容。
  - G18–G20 的数字（355 条公式、17 个零覆盖函数、3 个含 calcfield 的资产、日期级别统计）都是静态扫描 zip 得出，没有运行；calcfield 若存放在未扫描的节点会漏统计；`q2[...]` 的语义只在个别公式里见到，未深入。
  - 转换分支清单的"有"是静态上至少 1 个 vs 会走到（读 zip 里用户设置的节点），没有运行确认每个分支是否真被执行；"未验证"表示本轮没找到判定办法。复查时去掉了状态副本的重复，但哪个组件才是被转换的组件没有逐一对上（探测里是 51 个组件）。
  - `RuntimeCalcTableLens` 是否真的承担展开/TopN 计算：静态推断。
- **原先要问开发的问题，现已从源码和历史查明或已回答：**
  1. **快速路径与脚本路径（已查源码）。** 两条路径都用同一套单元格脚本，区别是底表：快速路径用预聚合的 crosstab 数据，另一条用明细表。快速路径需要同时满足（`CalcTableVSAQuery.java`）：
     - 不是明细模式（`:125`）；
     - `quickCheck`（`:2083`）：列绑定单元格只能是汇总或分组，不能是明细；分组必须有排序（不是"原始顺序"）并且横向或纵向展开；**不能有公式（脚本）类型单元格**；
     - 没有嵌套的同列分组（`:824`）；多个分组会被拆成每列一个 crosstab（`:833`）；
     - 树结构有效、行列分组不重复、至少一个聚合、`perby` 有效、所有单元格都被用到（`:914-1013`，`allCellUsed` 在 `:1958`，含"汇总格跨越了带 TopN 的维度"时回退）；
     - `createCrosstabHeaders`（`:1476`）遇到以下情况回退：**TopN 带 Others 但不是最内层分组**（`:1517`）、命名分组或日期间隔大于 1（`:1527`）、列找不到；
     - 合并后中间有被跳过的列时回退（`:796`）。
     各处回退条件说明开发的意图是"只在结果一致时才走快速路径"，但代码没有把"必须等价"写成声明，也没有说不一致时以谁为准（静态推断）。
     **datatest 现状（静态，只检查 `quickCheck` 里的单元格级三条）：** 86 个 vs 里共 182 个 CalcTable，97 个一定走脚本路径（90 个含公式脚本单元格，7 个含明细列单元格），85 个满足单元格级条件、可能走快速路径（其余树结构条件没查）。实际走了哪条没有运行确认；可以打开日志 `inetsoft.report.composition.execution.DebugCalcCrosstabCheck`，`getTableLens` 会输出 `Use N crosstab(s)…`（`:146`）。
  2. **转换时清除计算器、钻取、日期比较：** 产品设计，保持；已作为允许差异记入 `vsfreehand-g2-convert-probe.md`；"测试里怎么表示"转入 TODO（该文档 §7 T5）。
  3. **TopN/Others/分组排序在脚本路径下的负责类（已查源码）：** `LayoutTool.createGroupCond`（`LayoutTool.java:1352-1440`），快速路径对应 `CalcTableVSAQuery.createCrosstabHeaders`（`:1507-1514` 把 TopN 转成 crosstab 的排名和 Others 选项）。TopN/Others 的语义因此在两处各实现一遍，这正是 G3 的等价风险所在。
- **仍需作者确认的问题：**
  1. `Convert_Spec.CroTable2` 里 `Thread.sleep(70000)` 的目的。已查到：2025-09-30 的提交 `52fc77e7`（说明 "mock context on vscalctest scope. then modify case to pass when execution."）给该用例加了 `@Timeout(60)` 和 `Thread.sleep(70000)`，2026-05-29 的提交 `bfe85d92`（"fix CroTable2 timeout issue…"）把超时改成 80 秒，sleep 保留。vs 里该用例只有一个脚本 `setRowHeight(2,0)`，没有任何耗时逻辑；仓库里 vsothers 的 spec 只有这一处 sleep。目的无法从代码和历史确认，只知道现在转换加导出超过约 10 秒用例就会超时。
- **需要用户决定的事：**
  1. 先做哪一类：建议先做机制层（G1、G2，不依赖开发），再做单测（G3/G4）。
  2. 4 个孤儿 vs 是补用例还是从 zip 里删除。
  3. 数据层断言的 TXT 基线由谁人工核对（基线是回归基线，生成后要有人确认值对不对）。

## 7. Redmine bug 对照与补充缺口

**口径：**

- Redmine（Product 项目）里标题含 "freehand" 的 Bug 共 1351 条（全部类型 1384 条）；去掉 Rejected/Ignored 后 1174 条，按修复版本分：13.x 520、12.x 82、14.x 44、stylebi-* 35、ai-* 14、未标版本 479。
- 本节重点是 **StyleBI 线（stylebi-1.2.0 / 2.1.0）2025 年起的 35 条**：逐条读了标题，并用 `gh` 查了它们的修复 PR（`inetsoft-technology/stylebi`）改了哪些类、有没有带单测。2023–2024 年只读了 75 条相关标题；更早的、以及 13.x/14.x 线的 bug 没有逐条读，**是否适用于 StyleBI 2.1.0 未验证**。主题统计只按标题关键词，口径宽松，只用来看分布。
- 产品源码以 `origin/main`（`89cf1b9012`，2026-11-10）核对。§2、§4 当初读的本地 checkout 较旧，那时还没有 `LayoutToolTest`，见下面 G4 的更正。

**结论：**

- StyleBI 线最近一批 freehand bug 的根因集中在**"转换生成的公式"与"快速路径（crosstab 优化）产出的结果表"之间的取数**：2026-07 一批（#75630、#75636、#75647、#75665、#75747、#75799、#75801）都是转换或优化后**表里只剩表头或数据丢失**，原因是生成的公式引用了结果表里不存在的列名或维度名。这类错误恰好是 G2（转换前后数据对比）能抓的，所以 G2 里已确认"一致"的 32 个组件值得尽快锁成断言。
- 这批修复里，只有 2 个 PR 带了产品单测（#75647 的 PR 4261、#75747 的 PR 4445）；PR 4224（#75630、#75636）、4300（#75665）、4426（#75799）都只写了"手工复现"，没有单测。
- 出现了"修复后回归"的链：#62548→#79806、#62888→#80507（由 #79806 的修复引起）、#76829→#77237、#75747→#75801（首次修复被回滚后重做）。datatest 里 #62548 有 `FSNone1`，#62888 一个都没有（见 G15）。

**StyleBI 线 2025 年起 35 条 bug 的分布，以及修复是否带产品单测：**

| 主题 | bug | 修复带产品单测 |
|---|---|---|
| 转换 / 快速路径取数 | #75630、#75636、#75647、#75665、#75747、#75799、#75801（相关：#75588、#75746） | 2 / 5 个 PR |
| 脚本 / 作用域 | #75576、#75593、#75595、#75609、#75663、#75676、#78247、#78493 | 8 / 8 |
| 导出 | #75748、#76829、#77237、#79581 | 3 / 4（#75748 没有） |
| 锁 / 停滞 / 交换文件 / 集群 | #76905、#77909、#78115 | 3 / 3 |
| 格式 | #78092（百分比汇总沿用了字段格式） | 1 / 1 |
| Show Details | #76564（取数失败时 NPE） | 0 / 1 |
| 性能 | #75614（宽表多分组被拆成多个 crosstab 子查询）、#75625 | 1 / 2 |
| 编辑器 / 会话 / 批注（UI） | #72576、#72880、#75353、#75820、#75825、#78560 | 0 / 6，不在 datatest 范围 |

### 7.1 补充的缺口

判断单测还是 Dashboard 的依据：**期望值能直接写出来（纯函数、给定输入有确定输出）→ Java 单测；问题出在组件、绑定、数据源、书签之间的接线 → Dashboard**。

| # | 缺口 | 依据 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|---|
| G10 | 列名含特殊字符（`, ? @ { } = ; &`、引号、反斜杠）的列，转换后公式里的列名被转义错 | #75665（`escapeColName` 多写了一个反斜杠，列名 `YYYY, MMM D` 转换后整列空白，PR 4300 无单测）；2023 年 #60608、#60816 是同一类（列名含引号） | 转换或脚本里对列名的转义出错，对应列整列空白。datatest 里只有 `NG_specialChar`（命名分组成员含单引号），**没有任何 vs 的列名含逗号等字符**（86 个 vs 里 freehand 单元格绑定的列名只有这 1 个含特殊字符） | **单测**：`escapeColName` 是纯函数，输入列名输出脚本字面量，期望值可直接写；**Dashboard**：另加 1 个代表 vs 验证接线（理由：转义后的名字最终要在 JS 引擎里查到真实列，这条链是接线） | 高 |
| G11 | 聚合列本身就是预聚合列名（`Min(ftime)`、`Concat(fecid)`）的交叉表，转换后只剩表头 | #75799（`VSLayoutTool.getAggregateColumn` 总是剥一层 `formula(...)`，PR 4426 无单测） | 工作表里列名已带聚合函数的交叉表转换后丢行，没有任何提示 | **单测**：`getAggregateColumn` 是 `public static`，期望值可直接写；**Dashboard**：1 个代表 vs（工作表含预聚合列名），用 G2 对比转换前后 | 高 |
| G12 | 嵌套的日期分组维度转换后无数据（`Month(ndate)` 在另一个维度下）；首次修复回归（#75801）后重做 | #75747、#75801；`LayoutToolTest`（5 个用例）已覆盖 | 产品单测已有；datatest 已有 `GroupAndTab`（#75747 的附件 `GroupAndTab.vso`）和 `CroTable7`（#75801 提到），G2 探索里这两个 vs 的组件转换前后一致 | **无需新增用例**：把 G2 里"一致"的组件锁成断言即可，这是这类 bug 最直接的防线 | 高（落地 G2） |
| G13 | TopN/Others + 横向展开分组 + 快速路径：结果表里维度名解析不到，列和聚合值丢失 | #75630（PR 4224 无单测）、#75636；2024 年 #65744（聚合为 none 时 TopN 与 Others 不生效）、#65763（Others 组的平均值错）、#61734 | 快速路径（临时 crosstab）与脚本路径对同一布局算出不同的结果；`TableArray.hasMember`、`NamedCellRange` 对 `CrossFilter` 底表的裸列名解析出错。`origin/main` 里 `NamedCellRange` 有测试，但**没有一个直接测"维度名解析为该维度的不同取值"**（`getCrosstabCells` 是私有方法，测试里按名字找不到） | **单测**（对应 §3 的 G3）：期望值可从原始数据手算，两条路径逐格比；**Dashboard**：TopN+Others 的 vs 已有（25 个含 TopN，21 个带 Others），G1 推广后会有数据断言，不用新增 | 高 |
| G14 | Freehand 上的 Show Details：取数失败时 NPE；Others 单元格、命名分组 + 时间序列时报错；导出 CSV 为空 | #76564（`BaseTableService.isEmptyCell` 对空 lens 解引用，PR 5154 无单测）；2023 年 #58582、#62417、2024 年 #65163 | 表构建失败或遇到特殊单元格时点 Show Details 崩溃，而不是提示。datatest 的 `RuntimeViewsheetResource` 只有 `showDetailOnChart`，**没有表的 Show Details 封装** | **单测**：`isEmptyCell` 空 lens 分支（私有方法，需走公开入口或反射，难度中）；**Dashboard**：新增 `showDetailOnTable` 的 helper 加 3 个用例（Others 单元格、命名分组 + 时间序列、无数据）（理由：控制器 → 服务 → lens 这条链是接线，期望值依赖产品行为，不能在单测里独立算出） | 中 |
| G15 | 表脚本把组件值（`ComboBox.selectedLabel`、`RadioButton.selectedObject`）赋给 freehand 单元格 | #66427、#80507（#62888 被 #79806 的修复弄回归；#80507 是 13.x 线）；`#79806` 是 #62548 的回归 | 书签/选择状态下表脚本读不到组件值，单元格显示绑定的占位文字。86 个 vs 里 **0 个** 脚本引用组件值 | **Dashboard**：新建 1 个 vs（下拉框 + freehand + 表脚本），用书签恢复选择状态；理由：期望值依赖组件与表之间的接线，单测要搭整套沙箱。13.x 线已有对应的覆盖测试，StyleBI 2.1.0 是否也有未验证 | 中 |
| G16 | 导出：freehand 表样式里"表尾行"的颜色串到数据行 | #75748（`ExportUtil.getColType` 还原时调用了 `setTrailerColCount`，应为 `setTrailerRowCount`，PR 4402 无单测）；#76829、#77237、#79581 已有产品单测 | 导出 PDF/PNG/Excel 时数据行被画成表尾行的深色。viewer 不受影响，只有导出才出现 | **Dashboard**（转 vsexport 评估）：1 个用带"区别明显的表尾行颜色"的表样式的 freehand vs，导出 PNG/PDF 比对；理由：只有渲染结果才看得出；**单测**可选：`getColType` 前后 `RuntimeCalcTableLens` 的行数/列数不变（低难度） | 中 |
| G17 | 列数上限（column limit）下的 freehand | 2023–2024 年 #63594 一组：#63944、#64329、#64892、#65438（合并展开单元格、导出 HTML 无数据） | 开启列数限制后合并展开单元格错位、HTML 导出没有数据 | **Dashboard**（低优先）：用 `@SreeProperty` 设置列数限制，1 个用例；是否仍存在**未验证** | 低 |

**没有新增缺口的几类（已有覆盖或不属于 datatest）：**

- 脚本 / 作用域（8 个 bug，修复都带产品单测）、锁 / 停滞 / 交换文件 / 集群（3 个）、格式（#78092）：产品单测已覆盖，datatest 不重复。
- 转换后格式丢失、百分比（2023–2024 年 #59719、#63270、#62953 等）：`Convert_Spec` 的 `PercentFormat`、`CroTable7`、`CroTable8` 已覆盖；`CroTable8/Crosstab2` 的 `% of total by row` 差异仍在 `vsfreehand-g2-convert-probe.md` 的 TODO（T2）。
- 数据源形态：SQL 视图/查询源（#60468、#60460）、物化视图后 "Column not found"（#59433）：zip 里有 SQL 工作表源 11 个 vs，`mv` 模块里已有 Freehand 的 MV 用例；不新增。
- UI / 编辑器 / 会话 / 批注（6 个）：不属于 datatest，由 composer 的回归测试负责。

### 7.2 补充的单测与 Dashboard（只列新增，已有的见 §4、§5）

| 被测类 / 场景 | 补什么 | 用例数 | 难度 | 待确认 |
|---|---|---|---|---|
| 单测：`LayoutTool.escapeColName` | 逐个特殊字符、引号、反斜杠、空格、中文列名的转义结果 | 10 | 低（可见性待确认） | 无 |
| 单测：`VSLayoutTool.getAggregateColumn` | 普通聚合、已被函数包裹的聚合名（`Min(Min(ftime))` 对应的单元格值）、都不匹配时 | 4 | 低（`public static`） | 无 |
| 单测：`BaseTableService.isEmptyCell` | lens 或 rlens 为空时返回 true | 2 | 中（私有静态方法，走公开入口 `createCalcTableConditions` 或反射） | 无 |
| 单测：`ExportUtil.getColType`（可选） | 调用前后 `RuntimeCalcTableLens` 的表尾行数、列数不变 | 2 | 低到中 | 无 |
| 单测：`NamedCellRange` 的裸维度名解析（并入 §4 的 `CalcTableVSAQuery` 双路径） | 对 `CrossFilter` 底表，裸维度名解析为该维度的不同取值；子合计、总计占位元组被跳过 | 6 | 中（`getCrosstabCells` 私有） | 无 |
| Dashboard：特殊字符列名 | 新建 vs（工作表含 `YYYY, MMM D`、含引号的列名），转换后用 G2 对比；原生绑定同理 | 1 个 vs | 中（需要在界面或备份里新建工作表和 vs） | 无 |
| Dashboard：预聚合列名 | 新建 vs（工作表含 `Min(ftime)` 这样的列，交叉表以它为聚合），转换后用 G2 对比 | 1 个 vs | 中（同上） | 无 |
| Dashboard：表脚本引用组件值 | 新建 vs（下拉框 + freehand + `table[0][0] = ComboBox1.selectedLabel`），书签恢复选择 | 1 个 vs | 中 | StyleBI 2.1.0 是否有对应修复 |
| Dashboard：Show Details | `showDetailOnTable` helper 加 3 个用例 | 3 | 中到高（服务装配同 `convertToFreehandTable`） | 无 |
| Dashboard：导出表尾行颜色 | 带表尾行颜色的表样式 + freehand，导出比对 | 1 个 vs | 中（转 vsexport） | 无 |

**更正（G4）：** §3 的 G4 和 §1 写的"转换链在产品仓库没有任何单测"需要收窄。`origin/main` 里现在有 `LayoutToolTest`（5 个用例），覆盖的是 `LayoutTool` 对嵌套日期维度的列名解析（#75747）。仍然没有单测的：`VSTableConverter`、`ComposerVSTableService.convertToFreehandTable`、`VSLayoutTool.getAggregateColumn`、`LayoutTool.escapeColName`、`LayoutTool.createGroupCond`、`CalcTableVSAQuery.createCrosstabHeaders`（按名字在 `origin/main` 的 `core/src/test` 里搜不到）。

**局限：**

- 只读了 StyleBI 线 35 条的详情和修复 PR；其余是标题；13.x/14.x 线（共 564 条）没有逐条读。
- 修复"是否带单测"只看 PR 里是否改了 `src/test`；有的 bug 可能在别处（例如 `stylebi-wiz` 的回归测试）有覆盖，没有查。
- #79806、#80507、#66427 属于 13.x/14.x 线，`origin/main` 里没有对应修复，G15 是否适用于 StyleBI 2.1.0 需要确认。

## 附：数据来源与复现

```
# 脚本能直接适用的部分（vs/书签/组件类型/绑定表）
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vsfreehand-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vsfreehand-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vsfreehand/<Spec>.groovy vsothers/src/test/resources/expectData/vsfreehand/<Spec>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck <同上>     # 本模块全部 "Not predicted"，不可用

# 实际运行（本机 Windows）
./mvnw package -pl commons -DskipTests
./mvnw test -pl vsothers -Pvsfreehand
```

**口径说明：**
- `crosscheck` 的孤儿列表按单个 spec 看没有意义（三个 spec 共用一个 zip），本文改用三个 spec 的并集；"calc fields / not in baseline" 两列只适用于计算字段类模块，且本模块基线是 PNG（TXT 仅 3 个），没有采用。
- 覆盖矩阵里的单元格特性、数据源分类来自一份临时统计脚本（读 zip 内 `CalcTableVSAssembly/assemblyInfo/tableLayout/cellBinding`，只统计 `assemblyInfo` 那份；数据源由 `worksheetEntry/assetEntry` 的 type 与工作表内 table 类型归类）。该脚本没有入库；要复现需重新实现同样的口径。
- 数字为 vs 个数（"有该特性的 vs"），不是单元格个数。
- §7 的 Redmine 数据：Redmine 的 issues 查询，条件为标题包含 `freehand`、状态不限、每页 100 条，按 id 倒序翻页取完（1384 条）；按 tracker、状态、年份、修复版本、类别统计，再逐条读 2025 年起的标题。修复 PR 用 `gh pr list --repo inetsoft-technology/stylebi --state all --search "<bug 号> in:title" --json number,title,mergedAt,files` 查，再用 `gh pr view <号> --json body,files` 读描述；产品单测现状用 `git grep -l -w <方法名> origin/main -- core/src/test` 核对。
- 主题分布是按标题关键词的粗统计（例如 "convert" 命中 227 条，2023 年起 49 条），词表宽松，只看相对大小，不作为精确数字引用。
