# vsfreehand 测试缺口与补充方案

> 日期：2026-11-10　模块：`vsothers`，profile `-Pvsfreehand`，spec `Convert_Spec` / `FreeExecution_Spec` / `Freehand_Spec`（`vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vsfreehand/`），共用资产 `vsothers/src/test/resources/config/vsfreehand-storage.zip`
> datatest 基线：`Stateless-Sessions1`（`ee63561b`）；产品源码：`main`（`f0ef913306`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 一致）

## 1. 结论

vsfreehand 有 82 个用例、82 个 vs，规模不小，但**几乎全靠整页 PNG 做断言**：80 个用例只比 PNG，只有 2 个用例（`FSDeclaredVar`、`FSFormatNamedCell`）同时比单元格文本。这意味着 Freehand 表的单元格取值只要不影响渲染像素，就不会被发现；PNG 基线又是回归基线（由产品输出生成，不是独立算出的答案）。

转换（Convert）这一半同样如此：转换后只比 `_CALC.png`，没有"转换前后数据一致"的检查，而这恰好是产品代码里写明的意图（`ComposerVSTableService.java:479`，"make crosstab data is same with calc"）。

产品仓库里 Freehand 的单测很多，但集中在锁、超时、百分比格式、布局回写；**转换类（`VSTableConverter`、`ComposerVSTableService.convertToFreehandTable`）没有任何单测，`CalcTableVSAQuery` 的"临时 crosstab 快速路径 vs 脚本路径"没有数据正确性测试**。

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

*转换来源（`ConvertToCalc`，20 个 vs，静态扫描）：* 交叉表来源 17 个 vs，表来源 5 个（含嵌入表 1 个）；交叉表聚合公式只出现 `Sum`（17）、`Count`（3）、`none`（3），**没有 Average/Max/Min/DistinctCount 等**；日期比较仅 1 个（`DCPTrendTable1`），钻取仅 1 个（`DrillCroTable1`），`mergeSpan=false` 3 个；聚合节点下没有计算器子节点，未发现带计算器的转换用例（静态，计算器若存于别处则可能漏统计，未验证）。

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
| G2 | Convert 只比 `_CALC.png`，无"转换前后数据一致"检查 | 转换后某些行/列、合计、聚合值与原交叉表/表不一致（例如转换漏掉小计、`fixLayoutAlias`/`fixPeridField` 改错列名），而渲染图因为是回归基线被一起"固化" | 机制（改 `VSCalcTest.checkConvert`）。**已做探索**：`VSCalcTest.compareConvertData` 对 51 个组件比较，32 个一致、19 个有差异，其中 12 个是设计行为（允许差异），7 个转入 TODO，见 [vsfreehand-g2-convert-probe.md](vsfreehand-g2-convert-probe.md) | 高 |
| G3 | `CalcTableVSAQuery.getTableLens` 有两条路径，区别在**单元格脚本读的底表**：快速路径先把布局转成临时 crosstab（`createCrosstabAssemblies`），用预聚合的 crosstab 数据当底表；不满足条件则用明细表当底表，在脚本里分组聚合（`CalcTableVSAQuery.java:125-184`）。两条路径都由 `CalcTableLens.process()` 求值单元格。产品单测 12 个文件都是锁/格式/回写/临时名，**没有两路径数据等价的测试**，`createCrosstabHeaders`、`quickCheck`、`createCrosstabAssemblies`、`LayoutTool.createGroupCond` 在 `src/test` 里按名字都找不到 | 同一个 Freehand 布局，快速路径与脚本路径算出不同的值（例如 TopN、Others、空分组、合计行）；datatest 只对每个 vs 的当前路径做 PNG 快照，路径选择条件改了不会发现 | 单测 | 高 |
| G4 | 转换链 `VSLayoutTool.generateLayout` → `VSTableConverter.changeTableMode` → `ComposerVSTableService.convertToFreehandTable` 在产品仓库**没有任何单测**（三个类按类名在 `src/test` 都找不到；`VSLayoutTool` 仅被 3 个无关测试引用） | 转换静默改变数据：清掉计算器（`:524-526`）、清钻取和日期比较（`:479-484`）、复制 fillBlankWithZero/sortOthersLast/mergeSpan（`:513-516`）这些分支一旦改错，只有靠 Convert_Spec 的 PNG 才可能发现 | 单测（布局部分）+ dashboard（G2） | 高 |
| G5 | `RuntimeCalcTableLens`（1559 行，展开/分组后处理）单测只有一个序列化用例（`RuntimeCalcTableLensTest`，49 行）；其余引用它的 12 个测试关注锁/停滞/超时 | 横向/纵向展开、分组排序（升/降/按列/手动）、TopN+Others、日期级别分组、空分组、合并单元格的取值错误 | 单测 | 中（需开发先确认逻辑在哪个类，见 §6） |
| G6 | 交互只有"书签恢复"，没有对 Freehand 做选择/刷选后重新执行的用例 | 选择变化后 Freehand 没刷新、缓存没失效（书签恢复是从头执行，覆盖不到增量刷新） | dashboard | 中 |
| G7 | 转换来源窄：聚合只有 Sum/Count/none，日期比较/钻取各 1 个 vs，未发现带计算器的转换用例；嵌入表 1 个 | 转换时 Average/Max/DistinctCount/百分比等聚合、计算器被清除、钻取状态清理在更多形态下的错误 | dashboard（新建 vs）| 中 |
| G8 | 4 个孤儿 vs、1 个 `@Ignore`、6 个未用书签；`CroTable2` 的 70 秒 sleep 让用例对机器速度敏感 | 孤儿/被忽略的 vs 对应的场景（ComBindingTable6 的 quarter/noRangeID 书签、NG4）实际没有被测；CroTable2 在慢机器上偶发失败，被当成噪声忽略 | 机制 + 清理 | 低 |
| G9 | 没有分页（pageAfter）、cube/OLAP 数据源的 Freehand 用例 | 分页符、cube 源下的取值错误（是否支持 Freehand over cube 未验证） | dashboard，低优先；pageAfter 可能更适合 vsexport | 低 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测（G3/G4/G5，期望值可以从原始数据手算）；问题出在组件、绑定、数据源、书签之间的接线 → dashboard（G6/G7/G9）；验证机制本身的缺陷 → 改基类/spec，不是加用例（G1/G2/G8）。

## 4. Java 单测补充

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `CalcTableVSAQuery`（`getTableLens`、`createCrosstabAssemblies`） | 同一布局分别走快速路径与脚本路径，逐格比对；期望值从原始数据手算。覆盖：多种汇总公式（Sum/Count/Average/Max/DistinctCount/NthMostFrequent）、两层嵌套分组、横向展开、合计/小计、TopN+Others、百分比 | 12 | 中（要沙箱+工作表；仓库已有 `RowFetchFailureTest`、`CalcTableVSAQueryLayoutWriteBackTest` 的搭建方式可参考） | 已从源码查明哪些布局走快速路径（见 §6 的"源码查明"）；"两路径不一致时以哪个为准"代码里没有声明，需要开发确认。没有强制走某条路径的开关（在 `getTableLens`、`createCrosstabAssemblies0` 里未看到，非全仓搜索），单测要靠布局本身控制 |
| `VSLayoutTool.generateLayout` + `VSTableConverter.changeTableMode` | 交叉表→Freehand 布局：行/列头 1/2/3 层、汇总并排、总计/小计开关、mergeSpan、多聚合；简单表、嵌入表；断言每个单元格的 type/btype/expansion/formula | 14 | 中（纯布局无需执行，但要构造 `TableDataVSAssembly` 与元数据） | 无 |
| `ComposerVSTableService.convertToFreehandTable` | 计算器被清除（百分比除外）、钻取被清除、日期比较被清除、fillBlankWithZero/sortOthersLast/mergeSpan 复制、`fixPeridField`、`fixLayoutAlias` | 8 | 高（服务依赖 `ViewsheetService`、集群代理，测试入口未确认） | 清除计算器后数值与原交叉表不同，这是设计如此还是需要提示用户；哪些差异是允许的 |
| `RuntimeCalcTableLens`（展开、分组、TopN、Others、合并） | 展开 横/纵/无；分组排序升/降/按列/手动；TopN n/反向/Others；日期级别分组；空/null 分组；合并单元格；fillBlankWithZero | 18 | 中 | 已查明：脚本路径的分组/TopN/Others 由 `LayoutTool.createGroupCond` 生成条件（`LayoutTool.java:1409-1440`），不在 `RuntimeCalcTableLens`；`RuntimeCalcTableLens` 只在判断 `Others` 单元格类型时读 TopN（`:1041`）。测试对象应改为 `LayoutTool.createGroupCond`（`CalcGroup`/`CalcAggregate`），目前只有 `LegacyBuddhistManualOrderTest` 引用 `CalcGroup`。用例设计表里这一行的被测类需要从 `RuntimeCalcTableLens` 调整，难度待重新评估 |
| `CalcTableScope` / `TableValueList` 的 `toList` 选项串 | `sort=asc/desc`、`rounddate=year…day`、`interval` 的组合与非法值（产品已有 `CalcTableScopeTest` 634 行、`TableValueListTest`，**未逐项审读**，补之前先对照现有用例去重） | 6 | 低 | 无 |

难度：低 = 纯单元；中 = 需要脚本或沙箱环境；高 = 测试入口还没确认。用例数是设计出来的数量，不是已写好的。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 数据层断言（对应 G1） | 改 spec：给 Freehand_Spec、FreeExecution_Spec 的用例补 `executeVS(...)` + `compareData(...)`（已有 `FSDeclaredVar` 先例，`FreeExecution_Spec.groovy:388-399`）；或在 `VSCalcTest` 加一个"导出 PNG 并导出数据"的包装方法，减少 80 处重复 | 每个 Freehand 单元格的文本值 | TXT 基线用 `-Dupdate.baseline=true` 生成，**必须人工核对**，不能直接 `git add`；`executeVS` 对被隐藏的组件会跳过（`ViewsheetTest.groovy:186`） |
| 转换前后数据一致（对应 G2） | 改基类：`VSCalcTest.checkConvert0` 在转换前对每个待转换组件取 `getVSTableLens`，转换后取对应 CalcTable 的 lens，逐格比较 | 转换保持数据；期望值就是原组件自己的输出（独立来源） | 开发先列出"转换允许改变数据"的情形（清计算器、清钻取、清日期比较），这些用例单独声明例外；表头行高设为 0 的用例要跳过隐藏行 |
| 转换来源扩展（G7） | 新建 vs（3–5 个）：交叉表带 Average/Max/Min/DistinctCount/百分比聚合；带计算器的交叉表；嵌入表；以另一个 vs 组件为源 | 各形态转换后数据与原组件一致 | 依赖上一行的比较机制先落地，否则又是 PNG |
| 选择/刷选交互（G6） | 扩展：对 2–3 个有图表联动的 vs（如 `CM11_1_1`、`CM11_1_2`）用事件做选择/刷选再导出，而不是只恢复书签 | 选择变化后 Freehand 重新执行并刷新 | `ActionEventsUtil` 是否支持选择事件**未查**；先确认再定做法 |
| 清理孤儿与 ignore（G8） | 对 4 个孤儿 vs 逐个决定：补用例（`ComBindingTable6` 的 `quarter`/`noRangeID` 书签、`NG4`）或从 zip 删除；`VSScriptEffect` 按 Bug #58711 的注释重新设计；`CroTable2` 去掉 `Thread.sleep(70000)` 或说明目的 | 孤儿对应的场景被真正测到；CroTable2 不再因机器速度失败 | 需要作者说明 sleep 的目的 |
| 数据源扩展（G9） | 新建 vs：cube/OLAP 源；pageAfter 分页 | 取值与分页 | 产品是否支持 Freehand over cube 未验证；pageAfter 建议放 vsexport 评估 |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- **没验证的：**
  - PNG 层：本机 80 个用例像素全部不一致（0.50%–6.02%），所以本机运行只证明"无导出异常、无缺失/多余文件、TXT 通过"，不能证明 PNG 基线与产品输出一致；CI 的实际结果没查。
  - `CroTable2` 的超时是本机耗时（86.7 秒），CI 上是否也超时未知。
  - 孤儿判断是静态的（资产 id 字面量对 zip）；"未发现计算器的转换用例"是静态扫描，计算器若存在别的节点可能漏统计。
  - 产品单测盘点只做了按类名/引用的静态统计，没有运行产品仓库的测试，也没有逐项审读 `CalcTableScopeTest` 等现有用例的断言内容。
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
