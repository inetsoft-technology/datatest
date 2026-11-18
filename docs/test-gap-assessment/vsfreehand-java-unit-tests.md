# vsfreehand 产品仓库 Java 单测结果

> 来源：stylebi-main 单测流程产出（`stylebi-main/docs/.local/unit-test-workflow/vsfreehand/`：`report.md`、`decisions.md`、`matrix.md`、`bugs.csv`、`mutations.json`）。产品源码 `main`（`5b85601513`）；分支 `test-vsfreehand-harden`，未提交。
> 数字和判断取自上述报告，**未重跑测试，未逐个读测试代码**。矩阵期望值来自代码、注释和孪生实现，不来自 datatest 文档。
> 缺口全貌见 [vsfreehand.md](vsfreehand.md)，转换前后数据对比见 [vsfreehand-g2-convert-probe.md](vsfreehand-g2-convert-probe.md)。

## 1. 结论

- 矩阵 108 行：**通过 73**，**不一致 18**（全部已判定）、**没写 14**（原因写在矩阵里）、**待开发给期望值的探针 3**（S-14、S-16、E-12）。
- 12 个测试类、**920 个用例**（默认分组）：0 失败，20 个跳过（就是被 `@Disabled` 的"不一致"用例）；每个类单独跑、字母序和倒序都通过。`ComposerVSTableConvertTest`（8 例）带 `slow`+`integration` 标签，用 `-Dsurefire.groups=slow` 单独跑，通过。
- 变异检查 43 个，**抓到 41 个**；M-11、M-27 丢弃（没找到行为差异 / 等价变异）。
- 开出 **8 个 Redmine bug**（#81255–#81262，7 个 Low、1 个 Normal），见 §3。
- `vsfreehand.md` 里"转换类没有任何单测"**不再成立**：`VSTableConverter`、`convertToFreehandTable`、`createGroupCond`、`isEmptyCell`、`getColType` 现在都有单测。

## 2. 单测落在哪里

按矩阵"home"列统计（含不一致和没写的行）：

| 测试类 | 被测 | 通过 | 不一致 | 没写 | 探针 |
|---|---|---|---|---|---|
| `VSTableConverterTest` | `VSTableConverter.changeTableMode`（perBy×百分比、列名、多聚合、`fixTopN`、多层头） | 21 | 3 | 4 | 0 |
| `VSLayoutToolAggregateColumnTest` | `VSLayoutTool.getAggregateColumn`、`getOriginalColumn` | 4 | 1 | 1 | 0 |
| `LayoutToolTest` | `LayoutTool.createGroupCond`、`escapeColName`、`addSubTopNCond` | 11 | 3 | 1 | 0 |
| `FormulaFunctionsTest`、`FormulaFunctionsSortOrderTest` | `toList` / `mapList`（输入数组、排序、周起始日） | 4 | 6 | 2 | 0 |
| `CalcTableScopeTest` | 聚合函数边界、脚本路径 vs 交叉表路径 | 5 | 5 | 4 | 2 |
| `BaseTableServiceEmptyCellTest`（新） | `BaseTableService.isEmptyCell` | 6 | 0 | 0 | 0 |
| `ExportUtilCellFormatTest` | `ExportUtil.getColType` | 3 | 0 | 0 | 0 |
| `RuntimeCalcTableLensTest` | `IndexMap.get` | 2 | 0 | 0 | 1 |
| `NamedCellRangeTest` | 裸维度名解析 | 7 | 0 | 1 | 0 |
| `CalcTableVSAQueryTempCrosstabNameTest` | 临时 crosstab 名 | 2 | 0 | 0 | 0 |
| `ComposerVSTableConvertTest`（新） | `convertToFreehandTable` 真实链路（C-01..C-09） | 8 | 0 | 1 | 0 |

`convertToFreehandTable` 的测试入口找到了：`new ComposerVSTableService(beans)` + `RuntimeViewsheetExtension`、`IntegrationTestConfiguration`、`EmbeddedVS1.zip`（Hurricane 表，8 列）、Mockito 的 `CommandDispatcher`，写法参照 `CrosstabVSAssemblySourceRealChainTest`。所以 `vsfreehand.md` §4 里"测试入口未确认"的难度判断作废，该行已经有测试。

预言来源：规格 41、差分 30、对照 24、性质 9、钉住现状 4。

## 3. 开出的 Redmine bug

| bug | 行 | 问题 | 优先级 | 对应测试 |
|---|---|---|---|---|
| #81255 | V-08 V-09 | TopN/Others 单元格名保留了脚本标识符里不合法的字符（如 `Sales-Region`、`Qty(x)`）；运行时孪生 `getValidName` 会换成 `_` | Low | `VSTableConverterTest#dimensionNameWithHyphen…`、`#dimensionNameWithSpecialCharacter…` |
| #81256 | T-09 | `addSubTopNCond` 不转义单元格名里的双引号 | Low | `LayoutToolTest#createGroupCondEscapesADoubleQuote…` |
| #81257 | L-01 | `getOriginalColumn` 把含括号的列名（如 `Sales (USD)`）截断 | Low | `VSLayoutToolAggregateColumnTest#columnNameWithParentheses…` |
| #81258 | V-22 | 设计态与运行时表头不同时，`fixTopN` 把 Others 留在外层维度 | Low | `VSTableConverterTest#outerDimensionAtRuntime…` |
| #81259 | F-01..F-04 | `toList` / `mapList` 就地改写调用方传入的数组（`date`、`rounddate`、`distinct=false`、`mapList` 总是） | Low | `FormulaFunctionsTest` 4 个 |
| #81260 | F-05 | `mapList` 带 `sorton` + `maxrows` 丢失排名顺序（`toList` 有保护，`mapList` 没有） | Low | `FormulaFunctionsSortOrderTest` |
| #81261 | F-09 | `toList` 的 `date=week` 忽略 `week.start`，硬编码周日；交叉表的 `DateRangeRef` 遵循 `week.start` | **Normal** | `FormulaFunctionsTest#dateWeekOption…` |
| #81262 | （副发现） | 转换后关闭 viewsheet，日志出现 `Failed to cancel during dispose`（`ClassCastException`） | Low | 无（没复现，读代码得出） |

#81262 的成因（读代码，没运行）：`CrosstabVSAScriptable:63` 把 `getVSAssemblyInfo()` 强转为 `CrosstabVSAssemblyInfo`，而转换之后同名组件已是 `CalcTableVSAssemblyInfo`，旧的 scriptable 还留在作用域里；`ViewsheetSandbox:949` 在 `cancel()` 里捕获，所以每次"转换后关闭"都会警告并跳过 `cancel()` 剩余部分。

**与 `vsfreehand.md` 已有 bug 的关系：**

- #81083、#81085、#81071 此前对应的禁用用例，修复已合并（PR #8013、#8016、#8012），那五个测试类里没有残留的 `@Disabled`（#81071 的反斜杠一条是后面新写的 T-06、T-14，见下）。
- #81084（同一聚合两条路径结果不同）：经 #8015、#8054 修复后，`CalcTableScopeTest` 里的 `KNOWN_PATH_DIFFERENCES` **已删除**，只剩 4 类仍不同（见 §4），对应 7 个禁用用例。**`vsfreehand.md` 写的"12 组已钉住"已过期。**
- 列名反斜杠（#81071 第一点）：`LayoutToolTest` 里 2 个禁用用例（T-06、T-14）；UI 拒绝 `\`，只算"可达性"问题，没新开 bug。

## 4. 仍悬着的开发问题（没新开 bug）

| 问题 | 行 | 说明 |
|---|---|---|
| 字符串、日期做 `Median`、`Mode` 时哪条路径对？空数组的 `count` 是 0 还是 null？ | S-05、S-08、S-11、S-12、S-13 | **#81084 仍是开放的开发决策**，7 个禁用用例在 `CalcTableScopeTest`。测试注释认为脚本路径的字符串中位数（无值）是对的，#81084 备注认为交叉表路径的值像是对的，两者互相矛盾 |
| 全 null 分组、`fillBlankWithZero` 打开时 `max/min/median/mode` 应为 0 还是空？ | S-14（探针） | 同上，要开发回答 |
| NaN 单元格算值、缺失值还是错误？ | S-06 | 脚本 `average({NaN,1.0})` 开补零给 0，交叉表给无值。数据罕见，没写 |
| null 输入在 `product/stdev/var/median/max/mode` 之间不一致 | S-16、E-12（探针） | `IndexMap.get(-1)` 抛异常，`getReverse` 返回 -1 |

## 5. 弱点

- **C-03（TopN + Others，freehand vs crosstab）**：Hurricane 数据在交叉表里没有 Others 行，所以只比了"转换前后相等"和 TopN 上限，没真正比 Others 行。
- **V-20/V-21（span 检查）**：合成布局里大部分是空检查，只验证了 span 有被复制。
- 没复现的两处：#81258（真实 vs 里设计态与运行时表头不一致的情形）、#81262。

## 6. 没覆盖，以及原因

| 行 | 原因 |
|---|---|
| V-05 | `perBy` 选项为 8 的输入不可达（`VSAggregateRef` 的百分比 `DynamicValue` 只允许 0/1/2） |
| C-07 | `EmbeddedVS1` 没有日期列；交叉表日期维度不能设 `interval>1` |
| V-24..V-26 | 命名分组不会经 `prepareOrderInfo` 带过去（交叉表的命名分组是 `NamedRangeRef`），本身不是缺陷 |
| S-04、S-06、S-09、S-15、F-06、F-11、N-08 | 测试写错或设计如此 |
| T-12 | `createAggExpression` 是私有，`createGroupCond` 只收 `CalcGroup` |
| V-17、T-07、T-08、T-10 | 没找到相反的预言，保留为"当前行为"的钉住用例 |

## 7. 要点与证据

1. **`perBy`×百分比可观察。** `getAggregateRefs` 每次新建副本，但 `aref.getFormulaName()` 把选项拼进公式名（`AggregateRef:415-424`），写进单元格（`VSTableConverter:448-450`），汇总格公式名是 `Sum<8>`、`Sum<32>`、`Sum<4>`、`Sum<16>`（V-01..V-04、V-06 通过）。只有输入值 8 不可达（V-05）。
2. **`fixTopN` 外层 Others 的候选原因（#81258）：** `isNotInnerDimRef` 读设计态表头（`cinfo.getRowHeaders/getColHeaders`，`VSCrosstabInfo:1704-1710`），布局用运行时表头（`getPeriodRuntimeRowHeaders/getRuntimeColHeaders`，`VSTableConverter:227-229`），孪生 `CalcTableVSAQuery:1521` 按布局判断；两者不同时 Others 标志落在错的维度上。**真实 vs（动态维度、周期维度）未复现**，`CroTable8/Crosstab1` 是否由此引起没验证。
3. **`createCrosstabAssemblies`** 只在 `CalcTableVSAQueryTempCrosstabNameTest` 里测了临时名，行为没测。
4. **`CalcTableScopeTest` 已无 `KNOWN_PATH_DIFFERENCES`**，只剩 #81084 的 4 类差异（见 §4）。
5. **产品测试自身问题：** 已删除的 `CalcTableVSAQueryTempCrosstabNameTest.preFixNamingSchemeCollidesAcrossConcurrentInvocations` 拿本地辅助方法和它自己比较。
6. **语言环境：** UTC/en_US、Los_Angeles/en_GB 通过；Kolkata/de_DE、Auckland/tr_TR 有 7 个失败，全在 HEAD 已有的测试（`percentInFirstColumnMatchesSecondColumn`、`testVarianceWithPandS`、`testStandardDeviationWithPandS`）。core 的 pom 固定 en_US，CI 不受影响。

## 8. 仍留在 datatest 的事

G1、G2/G21（`checkConvert` 的 `allowedDiff`）、G6–G9、G15–G17、Dashboard 用例、自由格式纯脚本公式（U6，属于 ScriptEngine 范围）、`Sales-Region` / `Sales (USD)` + TopN + Others 的界面核对（对应 #81255、#81257）。

## 9. 与缺口表的对应及补充说明

| 被测 | 说明 |
|---|---|
| `CalcTableVSAQuery` 双路径（`getTableLens`、`createCrosstabAssemblies`） | **不在产品仓库做，放 datatest。** 快速路径在 `CalcTableVSAQuery.java:198` 直接 `new CrosstabVSAQuery(box, …)`，mock 沙箱走不通，要真实沙箱；"两路径不一致以谁为准"代码里没声明，需开发确认，先按"应一致"设计。`createCrosstabAssemblies` 只有临时名的测试 |
| `toList` / `mapList` | `toList(…,"sort=desc,maxrows=2")` 对 `[4,1,3,2]` 界面显示 `3,4`（预期 `4,3`）：`maxrows` 截断后又用 `sort2` 升序再排（`FormulaFunctions.java:178-184`、`:1510`），记在 #81071 |
| 日期分组（`createGroupCond`、`toList` 的 `rounddate`/`date`） | `toList` 各级别已覆盖；周起始日见 #81261；`interval>1` 回退（`CalcTableVSAQuery.java:1527`）与脚本路径是否一致、时区取哪个，待开发确认 |
| `CalcTableScope` 脚本 vs 交叉表 | 测试注释认为脚本路径的字符串中位数（无值）是对的，#81084 备注认为交叉表路径像是对的，互相矛盾 |

**#81071 第二点的前端观察**（界面上做的）：Freehand 表按列 g 分组，A 组 `v=10,20 w=0,0 s=x,y`，B 组 `v=空,空 w=1,2 s=z,z`。
- 全 null 的 `v`：B 组 Sum/Average/Max/Min 全显示空；Weighted Average(v, w) 在 A、B 两组都为空。
- 字符串列 `s`：Sum=0，Average=空，A、B 相同。
- 打开"空白补零"后：Sum、Average、Weighted Average、字符串列的 Sum/Average 变 0，Max/Min 仍为空。
- 不一致点：全 null 数值列的 Sum 为空而字符串列的 Sum 为 0；字符串列 Sum 为 0 而 Average 为空；开关打开后 Max/Min 不补 0。
- 新建 vs 验证这些值之前要先有开发的预期，否则基线会固化当前不一致。
