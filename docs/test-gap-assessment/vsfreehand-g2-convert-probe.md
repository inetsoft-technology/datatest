# vsfreehand G2：转换前后数据对比

> 对应 [vsfreehand.md](vsfreehand.md) 的 G2/G21。datatest 基线 `Stateless-Sessions1`；产品源码 `main`。
> 代码：`VSCalcTest.checkConvert`（`commons/src/test/groovy/inetsoft/test/modules/VSCalcTest.groovy`）、`ConvertDataProbe`。

## 1. 结论

- 转换前后逐格对比能做，且便宜：19 个 `ConvertToCalc` vs（51 个待转换组件）约 30–40 秒跑完。
- **51 个组件里 32 个一致，19 个有差异。** 6 个由产品代码解释为设计行为（转换清日期比较、钻取、计算器）；B1、B2 共 6 个组件经开发确认是产品设计；其余 7 个（B3、B4、C）待处理（§7）。
- 所以不能写成"逐格相等"：设计行为的组件要声明例外（`allowedDiff`）；待处理的在 TODO 解决前不转成严格断言。
- 测试固定传 `confirmed=true`（跳过 `fixAggregateInfo`）对这批用例没有影响：51 个组件分别用 `true`/`false` 转换，结果逐个相同。

## 2. 方法

`VSCalcTest.compareConvertData(params, confirmed = true)`：

1. 打开 vs，对每个可见的 Table / Crosstab / EmbeddedTable 取 `getVSTableLens`，导出为文本（每行一条，单元格用 ` | ` 连接，空值写 `NULL`）。所有源组件先全部导出，再统一转换（有的 vs 里 Freehand 绑定同 vs 的另一个组件）。
2. 对每个组件调用 `convertToFreehand`（`RuntimeViewsheetResource.convertToFreehand` 新增 `confirmed` 参数重载，原签名行为不变）。
3. 转换后同名取 `getVSTableLens`（`convertToFreehandTable` 用 `renameAssembly` 保留名字，`ComposerVSTableService.java:596`）再导出。
4. 逐行逐格比较。规范化（`sameCell`/`diffText`）：数字按 double 比；末尾 `.0` 忽略；源里某格等于左邻或上邻、转换后为 `NULL`，视为合并单元格重复值，忽略。**规范化会掩盖数字类型（Integer/Double）和合并单元格表现形式的差异。**
5. 行数不同的组件无法逐行对齐，只报告行数和首行。

导出文件：`vsothers/target/test-classes/exportData/vsfreehand/ConvertDataProbe/probe_<confirmed>_<vs名>/<组件名>_src.txt` 与 `_calc.txt`；汇总 `vsothers/target/g2_convert_probe.txt`。

复现：
```
./mvnw package -pl commons -DskipTests
./mvnw test -pl vsothers -Pvsfreehand -Dtest=ConvertDataProbe
cat vsothers/target/g2_convert_probe.txt
```
`ConvertDataProbe` 不以 `Spec` 结尾，不会被 CI 选中。

## 3. 结果

### 3.1 一致的组件（32 个）

| vs（`ConvertToCalc/` 下） | 一致的组件 |
|---|---|
| Crosstabs/CroTable1 | Crosstab1、Crosstab3 |
| Crosstabs/CroTable2 | Crosstab3 |
| Crosstabs/CroTable3 | Crosstab2 |
| Crosstabs/CroTable4 | Crosstab1–4 |
| Crosstabs/CroTable5 | Crosstab1、Crosstab2 |
| Crosstabs/CroTable6 | Crosstab1、Crosstab2 |
| Crosstabs/CroTable7 | Crosstab1–3 |
| Crosstabs/DCPTrendTable1 | Crosstab3、Crosstab4 |
| Crosstabs/PdateManulCroTable1 | Crosstab1–3 |
| Crosstabs/PercentFormat | Crosstab6 |
| GroupAndTab | Crosstab1、Crosstab2 |
| Tables/CTable1 | TableView2 |
| Tables/CTable2 | TableView1–3 |
| Tables/ComTable1 | TableView2、TableView4 |
| Tables/ComTable2 | TableView1、TableView2 |
| Tables/ETable1 | TableView5 |

### 3.2 有差异的组件（19 个）

**A. 设计行为（6 个）。** 依据 `ComposerVSTableService.java:479-484`（清钻取、清日期比较）、`:524-526`（清计算器，百分比除外）。

| 组件 | 差异 |
|---|---|
| DCPTrendTable1 / Crosstab1 | 13 行→5 行；源有 `change_Sum` 列，转换后只剩汇总值 |
| DCPTrendTable1 / Crosstab2 | 源趋势列首行 `NULL`、其后是与上期的差（383、104.55、-598.25…），转换后是原值（405.7、788.7、893.25…） |
| DrillCroTable1 / Crosstab1 | 18 行→55 行（源是钻取后状态，转换后未钻取） |
| DrillCroTable1 / Crosstab2 | 9 列→17 列，同上 |
| DrillCroTable1 / Crosstab3 | 4 行→47 行，同上 |
| CroTable8 / Crosstab2 | 表头 `Running Sum reseller: Sum(customer_id)`→`Sum(customer_id)`（计算器被清）；但 `% of total by row` 也变了（T2，清计算器解释不了） |

**B. 疑点。** B1、B2 开发已确认是产品设计（保持，作为允许差异）；B3、B4 转 TODO。

| # | 组件 | 差异 |
|---|---|---|
| B1 | CroTable1 / Crosstab4；CroTable3 / Crosstab1；CroTable6 / Crosstab3 | 聚合公式 `none` 的交叉表：源聚合列显示 `0`（或空），转换后每格是文字 `Aggregate`。CroTable3 / Crosstab1 同时行数 8→17 |
| B2 | ComTable1 / TableView1 | 小时分组：源 `01`、`02`…，转换后 `1970-01-01 01:00:00`（13 行全不同） |
| B2 | CroTable1 / Crosstab2 | 日期/季度分组标签：`2020-12-01`、`2021-03-01`… → `2020 4th`、`2021 1st`… |
| B2 | CroTable2 / Crosstab2 | `DataGroup(discount)`→`discount`；范围分组 `lessthan0.2`→`0.2`；`NthLarge` 34→23；`Max_date` 分隔符 `-`→`/`。分组边界似乎变了，不只是标签 |
| B3 | CTable1 / TableView1 | 21 行→108 行（首行相同）；源 Table 有 `conditions` 节点 |
| B3 | CTable1 / TableView3 | 15 行→16 行（首行相同） |
| B3 | ETable1 / TableView1、TableView3 | 701 行→51 行，首行也不同（源 `2022-02-22…`，转换后 `2022-01-01…`） |
| B4 | CroTable8 / Crosstab2 | `% of total by row` 1%→3%，随后 Sum 列 4.00→3.00、5.00→1.00 |
| B4 | VSScriptEffect / Crosstab1 | 4132 行里 5 行合计值不同（如 `-14505394.33` vs `-14973647.66`）；对应用例 `@Ignore`（Bug #58711） |

**C. 行数不同，无法逐行对齐：** CroTable2 / Crosstab1（13→22）、CroTable3 / Crosstab1（8→17，同属 B1）、CroTable8 / Crosstab1（9→12）。

## 4. 给开发的问题与回答

| # | 问题 | 回答 |
|---|---|---|
| 1 | B1：聚合公式 `none` 的交叉表转换后显示 `Aggregate`，源里显示 `0`（或空），哪个是预期 | 合理，产品设计，保持 |
| 2 | B2：转换后日期/时间分组标签变了、范围分组边界变了，是显示格式差别（源是已格式化显示值，转换后是原值）还是丢了分组设置 | 产品设计，保持 |
| 3 | B3：表转换是否应保持行集一致？条件、排序、最大行数是否保留 | 转 TODO（T1） |
| 4 | B4：`% of total by row` 除了清计算器还有别的原因吗 | 转 TODO（T2） |
| 5 | A：清日期比较、钻取、计算器后数据不同，测试里怎么表示"已知差异" | 转 TODO（T5） |
| 6 | **待回答：** 转换后百分比计算器（`PercentCalc`，`level`、`percentageByValue` 是表达式）按什么规则求值？转换器只替换汇总的 `percentageOption`（`VSTableConverter.java:431-446`），没处理计算器自己的 `level`/`percentageByValue` | 待开发 |
| 7 | **待回答：** 转换后小计/总计行第一列（如 `Year(orderdate)`）为 `NULL`，源里是年份值（`CroTable2/Crosstab2`、`CroTable8/Crosstab2`、`PdateManulCroTable1/Crosstab3` 三个含 Total 行的组件全部如此），是预期吗 | 待开发 |
| 8 | **待回答：** `CroTable8/Crosstab1` 转换后外层年份出现 `Others` 组，TopN 选中的 orderno 与源不同。源 XML 三个维度 `othersValue=true`、`rankingNValue=1`、`rankingOptValue=9`。外层 `Others` 怎么来的？TopN 是否按同一聚合排名？ | 待开发；候选原因见 T4④（Bug #81258） |
| 9 | **待回答：** `CroTable3/Crosstab1`、`CroTable2/Crosstab1`：转换后每个日期部分/Mini 都展开全部成员，源只展开部分。是预期（Freehand 总列全部成员）还是漏了源的展开/过滤设置 | 待开发 |

## 5. 局限

- 源数据取自转换前 `getVSTableLens` 的 `getObject` 原值（`toString`），不是界面格式化文本；日期分隔符、数字格式的差异可能是"原值 vs 已格式化文本"，不是转换错误。B2 的格式类差异需开发先判断。
- 行数不同的组件没有逐格比较，C 类和 B3 的"内容不同"只看了首行。
- 钻取状态的源是"已钻取"视图，A 类的钻取差异是预期的，但没有验证"清除钻取后的源"与转换结果一致（T5）。
- 只覆盖 `ConvertToCalc` 下 19 个 vs。
- 没有判断哪些差异是 bug，B 类只是"需要确认"。

## 6. 已落实：`checkConvert` 断言转换前后数据一致

`VSCalcTest.checkConvert(params, allowedDiff = [:])`（只有 `Convert_Spec` 的每个用例调用）在转换并导出 PNG 后，比较每个被转换组件转换前后的数据（规则同 §2），不一致则用例失败，信息列出组件名和差异。

- **允许名单：** `allowedDiff` 是"组件名 → 原因"；名单内组件允许不同，但**行数必须符合预期**：只写原因时行数不能变；行数会变的写成 `[reason: '...', rows: '21 -> 108']`，行数必须正好这样变。`Convert_Spec` 里 10 个用例带名单，共 19 个组件：12 个标"product design"（A 类、B1、B2），7 个标"TODO T1/T2/T4"（`VSScriptEffect` 的 T3 用例是 `@Ignore`，没写）。其余 9 个用例不带名单，必须完全一致。
- **局限（G21）：** 名单只约束行数，不约束单元格值。
- **维护：** 处理完 TODO 后把组件从 `allowedDiff` 删掉；组件已与源一致但还在名单里时，运行会在 stderr 打印 `no longer differs from its source, remove it from allowedDiff`。
- **不需要基线：** 期望值是转换前组件自己的输出，不涉及 PNG，本机和 CI 渲染差异不影响。
- **产出文件：** 每个组件的 `<名>_src.txt`（转换前）、`<名>_calc.txt`（转换后）写在该用例的导出目录。
- **验证：** 本机运行 `Convert_Spec`，没有数据检查造成的失败（失败的只有本机 PNG 差异 17 个和 `CroTable2` 超时 1 个，`VSScriptEffect` 被忽略）；反向验证：去掉 `DrillCroTable1` 的名单后该用例因数据差异失败，列出 3 个组件的行/列数变化。
- **局限：** 只对现有 19 个 vs 的形态有效；特殊字符列名、预聚合列名需要新建 vs（G10、G11）。`CroTable2` 的 `Thread.sleep(70000)` 没动（目的未知）；该用例 `@Timeout` 从 80 秒提到 120 秒，覆盖 sleep、转换、PNG 导出和数据检查。

## 7. TODO（待处理；完成后把组件从 `allowedDiff` 删除并转成断言）

### T1 表转换后行集不一致（原 B3）

涉及：`CTable1/TableView1`（21→108）、`CTable1/TableView3`（15→16）、`ETable1/TableView1`、`ETable1/TableView3`（701→51，内容不同）。口径：静态读 zip + 读导出文本，未单步跑转换。

- **`CTable1/TableView1`：** 条件很可能没带到转换后的 Freehand。源 20 行的 `numRangeID` 只有 `greater 23`（11）、`13-23`（4）、`7-13`（3）、`3-7`（2），转换后 107 行里多出 `lessthan 3`（6 行）等源里不存在的类别，不同行从 16 个变成 97 个；源里该组件有 `date_range1` 上的 `timeInstant` 条件（`operation=3`）。
- **`CTable1/TableView3`：** 转换后末尾多一行 `MA_MD`（源里 AZ…WA 共 14 个州，没有；`TableView2` 里 `MA_MD` 是一个列，疑似命名分组），其余相同。
- **`ETable1/TableView1`、`TableView3`：** 转换后 50 行数据**全是源行的子集**，按 `ndate` 升序；源 700 行未排序，且 `ETable1` 源里没有 `conditions`。即转换后只有前 50 行且被排序。50 是否来自默认上限/元数据模式（`ComposerVSTableService.getMetadata` 用 `calc_metadata`）**未确认**。
- 日期时间文本格式本身没变，差异只在行集。
- **要开发回答：** 表转换成 Freehand 后，条件、排序、行数上限（`maxRows`）哪些应保留？

### T2 `% of total by row` 转换后数值变化（原 B4）

涉及：`CroTable8/Crosstab2`。

- **Sum 列是设计行为：** 表头 `Running Sum reseller: Sum(customer_id)` 被清成 `Sum(customer_id)`，源值是累计（1、4、5），转换后是原值（1、3、1），累计差分完全吻合；23/65/86 → 7/5/8 同理。
- **百分比列仍无解释：** 同一行 1%→3%，Total 行 4%/23%/29% → 8%/42%/51%。转换后三个 Total 之和 101%（舍入），符合"占总计"；源里三个 Total 之和 56%，不是占总计。
- **静态线索（未验证）：** 源里该列由 `PercentCalc`（`level=1`，`percentageByValue="='Rows'"`，是表达式）计算；`clearCalculators`（`ComposerVSTableService.java:752`）**保留**百分比计算器；`VSTableConverter.java:431-446` 对 `PercentCalc` 自己的 `level`/`percentageByValue` 没有处理。该 vs 同时有 TopN/Others，也可能是分母是否包含 Others 的差别。
- **`perBy` 映射的影响：** `getAggregateRefs` 每次产生新副本，`setPercentageOption` 改的是局部副本；但副本的 `getFormulaName()` 会把选项拼进公式名并写进单元格（`AggregateRef:415-424`、`VSTableConverter:448-450`），所以汇总格公式名里能看到选项。这个映射是否影响最终取值未确认。
- **Total 行第一列变 `NULL`：** 转换后 Total 行的 `Year(orderdate)` 为 `NULL`（源里是 `97`/`98`/`99`），与百分比无关。本机导出的 43 对 `_src.txt`/`_calc.txt` 里含 "Total" 行的只有 3 个组件（`CroTable2/Crosstab2`、`CroTable8/Crosstab2`、`PdateManulCroTable1/Crosstab3`），3 个全部从有值变 `NULL`，是系统性的。**原因没有线索，只有观察本身。** 其中两个本来就在差异名单里，这一差别可能被名单放过（G21）。
- 行总计 `Grand Total` 标签转换后没有丢（核对过 `CroTable2/Crosstab2`、`CroTable2/Crosstab3`、`CroTable8/Crosstab1`、`PdateManulCroTable1/Crosstab3`；用户在界面用两层行维度交叉表转 Freehand 也没丢）。
- **要开发回答：** 问题 6、7。

### T3 合计值 5 行不同

涉及：`VSScriptEffect/Crosstab1`。对应用例 `@Ignore`（Bug #58711），随用例重新设计一起处理。

### T4 行数不同、尚未归类

涉及：`CroTable2/Crosstab1`（13→22）、`CroTable8/Crosstab1`（9→12）、`CroTable3/Crosstab1`（8→17）。

- **不是钻取：** 三个 vs 的 `drill_filters` 全空（CroTable2 0/6、CroTable3 0/4、CroTable8 0/4；只有 `DrillCroTable1` 非空）。
- **② `CroTable2/Crosstab1`（13→22）：** 源里小时 01、02 展开出 Mini 的前 3 + Others，03–05 只有一行、Mini 为 `NULL`；转换后 03–05 也展开成前 3 + Others。**总量一致**：03 时 `false/2002` 源 80.0 = 转换后 36+0+13+31；`false/2004` 源 55.0 = 0+20+35+0。是"源里没展开的明细，转换后展开了"，不是数据错；为什么源里 03–05 没展开未确认。
- **③ `CroTable3/Crosstab1`（8→17）：** 源里 2002/2004/2005 的季度列是 `NULL`（一行），只有 2003 展开 1st–4th；转换后每年都展开 4 个季度。聚合为 `none`（B1 已确认设计），只剩"行集多了"。疑似转换用日期部分（`QuarterOfYear`）列出全部取值，而源交叉表只输出数据里存在的组合；未确认。
- **④ `CroTable8/Crosstab1`（9→12）：不只是行数，最像真问题。** TopN 选中的 orderno 不同（1998 年：源 58、59；转换后 59、118），同一订单落在 `false` 还是 `Others` 列也不同；转换后多出外层 `Others` 年份组（109、73、Others），源里没有；Grand Total 行 `Others` 列 `First(...)` 由 8 变 2。对应转换分支 7（`fixTopN`）和 10（百分比）。`CroTable8` 还带 `=parameter.RadioButton3` 参数，是否与参数默认值有关未排除。
  - `fixTopN` 对非最内层维度会关掉 Others（`VSTableConverter.java:548-565`），普通维度和日期级别维度（`Year(date)`，N=3）单测都成立；`year.getFullName()` 与 `createGroupRef(null).getName()` 都是 `Year(date)`，名字匹配没问题。外层 Others 出现在 `fixTopN` 之外。
  - **候选原因（单测证据，Bug #81258，Low）：** `isNotInnerDimRef` 读设计态表头（`cinfo.getRowHeaders/getColHeaders`，`VSCrosstabInfo:1704-1710`），布局用运行时表头（`getPeriodRuntimeRowHeaders`/`getRuntimeColHeaders`，`VSTableConverter:227-229`），孪生 `CalcTableVSAQuery:1521` 按布局判断；两者不同时 Others 标志落在错的维度上。日期比较在转换前已被清除（`ComposerVSTableService:483`），所以只有动态维度绑定这类设计态与运行时不同的情况才触发。**真实 vs 未复现，`CroTable8/Crosstab1` 是不是这个原因没验证。**
  - 仍未排除：转换之后 Others 在别处被改回；TopN 被后续逻辑重新生成。
- **⑤ 三个组件都不能用"行数"名单放过：** ②③ 值能对上，④ 值对不上。
- **Java 单测的弱点：** `ComposerVSTableConvertTest` 的 Hurricane 数据在交叉表里没有 Others 行，Others 行没有被验证，仍要靠真实 vs。

### T5 "允许差异"在测试里怎么表示

涉及：A 类 6 个组件 + B1/B2 的 6 个组件。选项：跳过该组件，或比较"清除钻取/日期比较/计算器后的源"。`DrillCroTable1` 目前拿的是"已钻取"的源，没有验证"清除钻取后的源"与转换结果一致。

## 8. 产品仓库 Java 单测与本对比的分工

详见 [vsfreehand-java-unit-tests.md](vsfreehand-java-unit-tests.md)。

- `ComposerVSTableConvertTest`（8 例，`slow`+`integration`）用 `EmbeddedVS1.zip` 的 Hurricane 表做最小夹具的转换前后差分，全部通过；`checkConvert` 测真实 vs。
- T2 的 `PercentCalc` 是计算器自己的设置，Java 单测没覆盖，仍待开发回答。
- 转换后关闭 viewsheet 会记 `Failed to cancel during dispose`（`ClassCastException`，Bug #81262，Low，读代码得出、未复现）：旧交叉表 scriptable 留在沙箱作用域，`CrosstabVSAScriptable:63` 强转失败，`ViewsheetSandbox:949` 在 `cancel()` 里吞掉。`Convert_Spec` 日志里若看到该警告就是它。
- 仍属 datatest：`allowedDiff`（G21）、T1、T2 的百分比计算器、T3、T5。
