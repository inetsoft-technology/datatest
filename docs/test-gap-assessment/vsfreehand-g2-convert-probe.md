# vsfreehand G2 探索：转换前后数据对比

> 日期：2026-11-10　对应 `vsfreehand.md` 的 G2（Convert 只比 `_CALC.png`，没有"转换前后数据一致"的检查）
> datatest 基线：`Stateless-Sessions1`（`c27b1666`）；产品源码：`main`（`f0ef913306`）
> 代码：分支 `g2-convert-data-explore`（`VSCalcTest.compareConvertData`、`ConvertDataProbe`）

这是一次**探索**，不是断言：目的是看"转换前后数据逐格对比"能不能做、差异有多少、分几类，结论决定 G2 能不能落成断言。

## 1. 结论

- 这个对比能做，且便宜：19 个 `ConvertToCalc` vs（51 个待转换组件）约 30–40 秒跑完。
- **51 个组件里 32 个转换前后一致，19 个有差异。** 其中 6 个能由产品代码解释为设计行为（转换时会清日期比较、钻取、计算器）；B1、B2 共 6 个组件经开发确认是产品设计（§6），同样属于允许差异；其余（B3、B4、C，共 7 个组件，其中 `CroTable8/Crosstab2` 的计算器部分已在 A 类）转入 TODO，由后续负责人处理（§7）。
- 所以 G2 不能直接写成"逐格相等"的断言：设计行为（A、B1、B2）的组件要声明例外；其余在 TODO 处理完之前不转成断言。
- 测试固定传 `confirmed=true`（跳过 `fixAggregateInfo`）对这批用例没有影响（§4）。

## 2. 方法

`VSCalcTest.compareConvertData(params, confirmed = true)`（`commons/src/test/groovy/inetsoft/test/modules/VSCalcTest.groovy`）：

1. 打开 vs，对每个可见的 Table / Crosstab / EmbeddedTable 取 `getVSTableLens`，导出为文本（每行一条，单元格用 ` | ` 连接，空值写 `NULL`）。所有源组件**先全部导出**，再统一转换，避免转换互相影响"转换前"的数据（有的 vs 里 Freehand 绑定的是同 vs 的另一个组件）。
2. 对每个组件调用 `convertToFreehand`（经 `RuntimeViewsheetResource.convertToFreehand`，该方法新增了 `confirmed` 参数的重载，原签名行为不变）。
3. 转换后用同名取 `getVSTableLens`（`convertToFreehandTable` 会 `renameAssembly` 保留名字，`ComposerVSTableService.java:596`）再导出。
4. 逐行逐格比较。规范化规则（都在 `sameCell`/`diffText`）：
   - 数字按 double 比（`22` 与 `22.0` 相同）；
   - 末尾 `.0` 忽略（`2022-05-01 00:00:00.0` 与 `…00:00:00`）；
   - 源里某格等于它的左邻或上邻、转换后为 `NULL`，视为合并单元格造成的重复值，忽略。
5. 行数不同的组件**无法逐行对齐**，只报告行数和首行，不比单元格。

导出文件：`vsothers/target/test-classes/exportData/vsfreehand/ConvertDataProbe/probe_<confirmed>_<vs名>/<组件名>_src.txt` 与 `_calc.txt`；汇总：`vsothers/target/g2_convert_probe.txt`。

## 3. 复现

```
./mvnw package -pl commons -DskipTests
./mvnw test -pl vsothers -Pvsfreehand -Dtest=ConvertDataProbe
cat vsothers/target/g2_convert_probe.txt
```

`ConvertDataProbe` 不以 `Spec` 结尾，不会被 CI 的 `vsfreehand/*Spec` 选中。它对每个 vs 用 `confirmed=true` 与 `false` 各跑一遍（所以汇总有 102 行，两组结果一致）。

## 4. 已排除的猜测

猜测："转换后出现文字 `Aggregate`，是因为测试固定传 `confirmed=true`，跳过了 `fixAggregateInfo`（UI 首次调用传 false）。"
验证：对全部 51 个组件分别用 `confirmed=true`/`false` 转换并比较，**结果逐个完全相同**。猜测不成立；同时说明在这批用例上，测试的转换方式与 UI 首次调用没有差异。

## 5. 结果

### 5.1 一致的组件（32 个）

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

"一致"指经 §2 的规范化后一致；规范化会掩盖数字类型（Integer/Double）和合并单元格表现形式的差异。

### 5.2 有差异的组件（19 个）

**A. 产品代码能解释的设计行为（6 个，待开发确认这些差异在测试里允许）**

产品依据：`ComposerVSTableService.java:479-484`（清钻取、清日期比较）、`:524-526`（清计算器，百分比除外）。

| 组件 | 观察到的差异 |
|---|---|
| DCPTrendTable1 / Crosstab1 | 13 行→5 行；源有 `change_Sum` 列，转换后只剩汇总值 |
| DCPTrendTable1 / Crosstab2 | 源的趋势列首行 `NULL`、其后是与上期的差（383、104.55、-598.25…），转换后变成原值（405.7、788.7、893.25…） |
| DrillCroTable1 / Crosstab1 | 18 行→55 行（源是钻取后的状态，转换后是未钻取） |
| DrillCroTable1 / Crosstab2 | 9 列→17 列，同上 |
| DrillCroTable1 / Crosstab3 | 4 行→47 行，同上 |
| CroTable8 / Crosstab2 | 表头 `Running Sum reseller: Sum(customer_id)` 变成 `Sum(customer_id)`（计算器被清）；但同一组件里 `% of total by row` 的值也变了（1%→3%），计算器被清除解释不了，见 B4 |

**B. 疑点。B1、B2 已确认为产品设计（保持，作为允许差异）；B3、B4 转入 TODO（§7）**

| # | 组件 | 观察到的差异 |
|---|---|---|
| B1 | CroTable1 / Crosstab4；CroTable3 / Crosstab1；CroTable6 / Crosstab3 | 聚合公式是 `none` 的交叉表：源里聚合列显示 `0`（有的是空），转换后每格都是文字 `Aggregate`。CroTable3 / Crosstab1 同时行数 8→17 |
| B2 | ComTable1 / TableView1 | 小时分组：源显示 `01`、`02`…，转换后是 `1970-01-01 01:00:00`（13 行里 13 行不同） |
| B2 | CroTable1 / Crosstab2 | 日期/季度分组标签：源 `2020-12-01`、`2021-03-01`…，转换后 `2020 4th`、`2021 1st`… |
| B2 | CroTable2 / Crosstab2 | 表头 `DataGroup(discount)`→`discount`；范围分组 `lessthan0.2`→`0.2`；同行的 `NthLarge` 34→23、`Max_date` 的日期分隔符 `-`→`/`。分组边界似乎变了，不只是标签 |
| B3 | CTable1 / TableView1 | 21 行→108 行（首行相同）；源 Table 里有 `conditions` 节点 |
| B3 | CTable1 / TableView3 | 15 行→16 行（首行相同） |
| B3 | ETable1 / TableView1、TableView3 | 701 行→51 行，首行内容也不同（源首行 `2022-02-22…`，转换后 `2022-01-01…`） |
| B4 | CroTable8 / Crosstab2 | `% of total by row` 1%→3%，随后的 Sum 列 4.00→3.00、5.00→1.00（行顺序或分组变了？） |
| B4 | VSScriptEffect / Crosstab1 | 4132 行里 5 行的合计值不同（如 `-14505394.33` vs `-14973647.66`）。该 vs 对应的用例当前 `@Ignore`（Bug #58711） |

**C. 暂无法归类（行数不同，无法逐行对齐）**

| 组件 | 行数 |
|---|---|
| CroTable2 / Crosstab1 | 13→22 |
| CroTable3 / Crosstab1 | 8→17（同属 B1） |
| CroTable8 / Crosstab1 | 9→12 |

## 6. 给开发的问题

1. **B1**：聚合公式为 `none` 的交叉表转换后显示文字 `Aggregate`，源里显示 `0`（或空）。哪个是预期？
   - 回答：合理，产品设计，保持。
2. **B2**：转换后日期/时间分组的标签变了（`01`→`1970-01-01 01:00:00`，`2020-12-01`→`2020 4th`），范围分组 `lessthan0.2` 变成 `0.2` 且聚合值变了。这是显示格式的差别（源里是已格式化的显示值，转换后是原值），还是转换丢了分组设置？
   - 回答：产品设计，保持。
3. **B3**：Table / 嵌入表转换成 Freehand 后行数和内容不同（21→108、701→51）。表转换是否应保持行集一致？表上的条件（`conditions`）、排序、最大行数会被保留吗？
   - 回答：转入 TODO，由后续负责人处理（§7）。
4. **B4**：`% of total by row` 在转换后数值变化（1%→3%），除了清除计算器，还有别的原因吗？
   - 回答：转入 TODO，由后续负责人处理（§7）。
5. **A**：清除日期比较、钻取、计算器后数据不同，在测试里允许吗？允许的话，测试怎么表示"已知差异"（跳过该组件，还是比较"清除后的源"）？
   - 回答：转入 TODO，由后续负责人处理（§7）。

6. **T2 / T4（新增，待开发回答）：**
   - 转换后百分比计算器（`PercentCalc`，`level`、`percentageByValue` 是表达式）应按什么规则求值？转换器只替换汇总的 `percentageOption`（`VSTableConverter.java:431-446`），没处理计算器自己的 `level`/`percentageByValue`；`CroTable8/Crosstab2` 的 `% of total by row` 因此与源不同（见 T2）。
   - 转换后小计/总计行的第一列（如 `Year(orderdate)`）为 `NULL`，源里是年份值（`CroTable2/Crosstab2`、`CroTable8/Crosstab2`、`PdateManulCroTable1/Crosstab3` 三个含 Total 行的组件全部如此），是预期吗？
   - `CroTable8/Crosstab1`：`fixTopN` 对非最内层维度会把 `Others` 关掉（`VSTableConverter.java:548-565`），但转换后的输出里外层年份仍出现 `Others` 组，且 TopN 选中的 orderno 与源不同（见 T4④）。源 XML 里三个维度 `othersValue=true`、`rankingNValue=1`、`rankingOptValue=9`（N 是否来自变量/参数未确认）。需要开发说明：这个外层 `Others` 是怎么来的？TopN 在转换后是否按同一个聚合排名？
   - `CroTable3/Crosstab1`、`CroTable2/Crosstab1`：转换后每个日期部分/Mini 都展开全部成员，源只展开部分。这是预期（Freehand 总是列出全部成员）还是转换漏了源的某种展开/过滤设置？

## 7. TODO（由后续负责人处理）

按组件列出，完成后再把 G2 转成断言：

| # | 内容 | 涉及组件 | 说明 |
|---|---|---|---|
| T1 | 表（Table / 嵌入表）转换后行集不一致：先判断是设计还是转换丢了条件/排序/最大行数 | `CTable1/TableView1`（21→108）、`CTable1/TableView3`（15→16）、`ETable1/TableView1`、`ETable1/TableView3`（701→51，内容不同） | 原 B3；`CTable1/TableView1` 源里有 `conditions` 节点，`ETable1` 没有。对照导出的 `_src.txt`/`_calc.txt` 与 zip（静态 + 读导出文本，未单步跑转换）：<br>① **`CTable1/TableView1`（21→108）：条件很可能没带到转换后的 Freehand。** 源 20 行的 `numRangeID` 只有 `greater 23`（11）、`13-23`（4）、`7-13`（3）、`3-7`（2），转换后 107 行里多出 `lessthan 3`（6 行）等源里不存在的类别，不同行从 16 个变成 97 个；源里该组件有一个 `date_range1` 上的 `timeInstant` 条件（`operation=3`）。<br>② **`CTable1/TableView3`（15→16）：** 转换后末尾多一行 `MA_MD`（源里 AZ…WA 共 14 个州，没有这一行；`TableView2` 里 `MA_MD` 是一个列，疑似命名分组），其余行相同。<br>③ **`ETable1/TableView1`、`TableView3`（701→51，即 50 行数据）：** 转换后的 50 行**全部是源行的子集**，按 `ndate` 升序排列；源 700 行是未排序的。同时 `ETable1` 源里没有 `conditions`。所以不是内容不同，而是转换后只有前 50 行且被排序（50 这个数字是否来自某个默认上限/元数据模式，`ComposerVSTableService.getMetadata` 用的是 `calc_metadata` 模式，**未确认**）。<br>④ 日期时间的文本格式本身没变（`2022-12-22 11:35:25.0` 等），差异只在行集。<br>需要开发回答：表转换成 Freehand 后，表上的条件、排序、行数上限（`maxRows`）哪些应保留？①②③ 目前看都是"没带过去"或"多出来"。 |
| T2 | `% of total by row` 转换后数值变化（**Sum 列已查清，百分比列仍未解释**） | `CroTable8/Crosstab2` | 原 B4。对照本机 `Convert_Spec/CroTable8` 的 `Crosstab2_src.txt` / `Crosstab2_calc.txt`：<br>① **Sum 列的差异是设计行为，已解释。** 表头 `Running Sum reseller: Sum(customer_id)` 被清成 `Sum(customer_id)`，源里的值是累计（1、4、5），转换后是原值（1、3、1），累计差分完全吻合；23/65/86 → 7/5/8 同理。<br>② **百分比列仍有差异：** 同一行 1%→3%，Total 行 4%/23%/29% → 8%/42%/51%。转换后三个 Total 之和为 101%（舍入），符合"占总计"；源里三个 Total 之和 56%，不是占总计。<br>③ 静态线索（未验证）：源里该列由 `PercentCalc`（`level=1`，`percentageByValue="='Rows'"`，是表达式不是常量）计算；`clearCalculators`（`ComposerVSTableService.java:752`）**保留**百分比计算器；`VSTableConverter.java:431-446` 只把汇总的 `percentageOption` 换成 `OF_ROW_GRANDTOTAL`/`OF_COL_GRANDTOTAL` 等，没有处理 `PercentCalc` 自己的 `level` 和 `percentageByValue`。（补充：stylebi-main 会话读代码发现 `getAggregateRefs` 每次都 `createAggregateRef` 产生新副本，`setPercentageOption` 改的是局部副本，转换结果的单元格上看不到，只会间接影响后续的 `findAggregateRef`；所以"只替换了 `percentageOption`"这句说得不准，这个映射是否真的影响最终取值还没确认。）也就是转换后的百分比可能按另一套规则算（待开发确认；该 vs 同时有 TopN/Others，也可能是分母是否包含 Others 的差别）。<br>④ **新观察：** 转换后 Total 行的 `Year(orderdate)` 列为 `NULL`（源里是 `97`/`98`/`99`），与百分比无关。本机 `Convert_Spec` 导出的 43 个 `_src.txt`/`_calc.txt` 对里，含 "Total" 行的只有 3 个组件（`CroTable2/Crosstab2`、`CroTable8/Crosstab2`、`PdateManulCroTable1/Crosstab3`），**3 个的 Total 行第一列全部从有值变成 `NULL`**，所以是系统性的，不是 CroTable8 特有；可能是小计行跨列单元格的取值差别，是否设计未确认。（统计口径：行内含单元格 `Total`；`CroTable2/Crosstab2`、`PdateManulCroTable1/Crosstab3` 本来就在差异名单里，这一差别可能被名单一起放过，见 G21。）**（已撤回）** 曾记录 stylebi-main 会话"行总计 `Grand Total` 标签丢失、`clearSpanCellBinding` 清掉跨度内单元格"的线索；该会话随后撤回：用户在界面用两层行维度的交叉表转 Freehand 验证，标签没有丢，那是它测试夹具的产物（夹具把多个明细行合并成一个 DETAIL 区域，跨度与真实转换不同），对应单测已删除；"`r` 区域下标当全局行号"的推测也没有任何实际后果的证据，一并撤回。datatest 现有用例里行总计标签转换后也都还在（我核对过 `CroTable2/Crosstab2`、`CroTable2/Crosstab3`、`CroTable8/Crosstab1`、`PdateManulCroTable1/Crosstab3`）。**所以 Total 行 `Year` 变 `NULL` 的原因目前没有任何线索，只有观察本身。**<br>需要开发回答：③ 中百分比计算器在转换后应按什么规则求值；④ 是否预期。 |
| T3 | 合计值 5 行不同 | `VSScriptEffect/Crosstab1` | 原 B4；对应用例当前 `@Ignore`（Bug #58711），与用例重新设计一起处理 |
| T4 | 行数不同、尚未归类 | `CroTable2/Crosstab1`（13→22）、`CroTable8/Crosstab1`（9→12）、`CroTable3/Crosstab1`（8→17，`Aggregate` 部分已确认为设计，行数差异未解释） | 原 C。对照本机 `Convert_Spec/<vs>/Crosstab1_src.txt` / `_calc.txt` 查到（口径：静态读 zip + 读导出的两份文本，未运行转换逻辑单步确认）：<br>① **不是钻取。** 三个 vs 的 `drill_filters` 全是空（CroTable2 0/6、CroTable3 0/4、CroTable8 0/4；只有 `DrillCroTable1` 非空），所以不能归入"清除钻取"那一类。<br>② `CroTable2/Crosstab1`（13→22）：源里小时 01、02 展开出 Mini 的前 3 + Others，03–05 只有一行、Mini 为 `NULL`；转换后 03–05 也展开成前 3 + Others。**总量一致**：03 时 `false/2002` 源 80.0 = 转换后 36+0+13+31；`false/2004` 源 55.0 = 0+20+35+0。所以是"源里没有展开的明细，转换后展开了"，不是数据错；为什么源里 03–05 没展开未确认。<br>③ `CroTable3/Crosstab1`（8→17）：源里 2002/2004/2005 的季度列是 `NULL`（一行），只有 2003 展开 1st–4th；转换后每年都展开 4 个季度。聚合为 `none`，值全是 `Aggregate`（B1 已确认设计），所以只剩"行集多了"。疑似转换用日期部分（`QuarterOfYear`）列出全部取值，而源交叉表只输出数据里存在的组合；未确认。<br>④ `CroTable8/Crosstab1`（9→12）：**不只是行数。** TopN 选中的 orderno 不同（1998 年：源 58、59；转换后 59、118），同一订单落在 `false` 还是 `Others` 列也不同；转换后多出一个外层 `Others` 年份组（109、73、Others），源里没有；Grand Total 行 `Others` 列 `First(...)` 由 8 变成 2。对应 §2 分支 7（`fixTopN`，外层维度带 Others）和分支 10（百分比）。这是 T4 里最像真问题的一个，但 CroTable8 还带 `=parameter.RadioButton3` 参数，是否与参数默认值有关未排除。<br>**新线索（stylebi-main 会话读代码得出，我核对了其中第一句，第二句未验证）：** `isNotInnerDimRef`（`VSTableConverter.java:565-600`，我读过）用 `VSDimensionRef.getFullName()` 去和传入 `GroupRef.getName()` 比较，匹配不到就落到末尾 `return false`，Others 保持打开，而不是"没被调到"。外层年份是日期级别维度（`Year(orderdate)` 这类），它的 `fullName` 与 `GroupRef` 的名字是否一致没有核对。产品仓库的单测会用"普通维度 vs 日期级别维度各一例"来证实或排除，结果以实际为准。<br>**结果（stylebi-main 会话已跑，`VSTableConverterTest` 10 个用例通过，我没重跑）：** `fixTopN` 对外层维度确实关掉 Others，普通维度和日期级别维度（`Year(date)`，N=3 保留）都是；`year.getFullName()` 与 `createGroupRef(null).getName()` 都是 `Year(date)`，匹配上了。所以上面"名字对不上"的线索**在最小夹具里不成立**，`CroTable8/Crosstab1` 的外层 Others 问题在 `fixTopN` 之外。仍未排除的可能（都没验证）：① `isNotInnerDimRef` 读的是 `info.getRowHeaders()/getColHeaders()`（设计态表头），而 `convertCrosstabToCalc` 的 `refs` 来自运行时表头（`getPeriodRuntimeRowHeaders`/`getRuntimeColHeaders`），真实 vs 里（日期比较、变量、周期字段）两者可能不一致，名字匹配不上就返回 false；② 转换之后 Others 在别处被改回；③ TopN 被后续逻辑重新生成。最小夹具里设计态和运行时表头相同，没覆盖到①。<br>⑤ 三个组件都不能用"行数"名单放过：②③ 值能对上，④ 值对不上。 |
| T5 | 决定"允许差异"在测试里怎么表示：跳过该组件，还是比较"清除钻取/日期比较/计算器后的源" | A 类 6 个组件 + B1/B2 的 6 个组件 | 原问题 5；`DrillCroTable1` 目前拿的是"已钻取"的源，没有验证"清除钻取后的源"与转换结果一致 |

## 8. 局限

- 源数据取自转换前的 `getVSTableLens`，用的是 `getObject` 的原值（`toString`），不是界面显示的格式化文本；日期分隔符、数字格式之类的差异可能来自"原值 vs 已格式化文本"的表现形式，而不是转换错误。**B2 里的格式类差异尤其需要开发先判断。**
- 行数不同的组件没有逐格比较，C 类和 B3 的"内容不同"只是看了首行。
- 钻取状态的源是"已钻取"的视图，所以 A 类里的钻取差异是预期的，但没有验证"清除钻取后的源"与转换结果一致。
- 只覆盖 `ConvertToCalc` 下 19 个 vs；（原写"聚合只有 Sum/Count/none"读错了节点，实际公式 14 种，见 `vsfreehand.md` §2 的更正。）
- 尚未判断哪些差异是 bug；上面 B 类都只是"需要确认"。

## 9. 已落实：`checkConvert` 现在断言转换前后数据一致

`VSCalcTest.checkConvert(params, allowedDiff = [:])`（`Convert_Spec` 每个用例都调用它，没有别的 spec 用）在转换并导出 PNG 之后，比较每个被转换组件转换前后的数据（规则同 §2），不一致就让用例失败，失败信息列出组件名和差异。

- **允许名单：** `allowedDiff` 是"组件名 → 原因"，名单里的组件允许不同，但**行数必须符合预期**：只写原因时，行数不能变；行数确实会变的组件写成 `[reason: '...', rows: '21 -> 108']`，行数必须正好这样变。这样名单里的组件如果换了另一种变化（比如丢行），仍然会让用例失败。`Convert_Spec` 里 10 个用例带了名单，共 19 个组件：12 个标"product design"（§5.2 的 A 类、B1、B2），7 个标"TODO T1/T2/T4"（`VSScriptEffect` 的 T3 对应的用例是 `@Ignore`，没有写）。其余 9 个用例不带名单，必须完全一致。
- **维护：** 处理完 §7 的 TODO 后，把对应组件从 `Convert_Spec` 的 `allowedDiff` 里删掉。组件已经和源一致但还在名单里时，运行会在 stderr 打印 `no longer differs from its source, remove it from allowedDiff`，提示可以删。
- **不需要基线：** 期望值是转换前组件自己的输出，不涉及 PNG，本机和 CI 的渲染差异不影响它。
- **产出文件：** 每个组件的 `<名>_src.txt`（转换前）和 `<名>_calc.txt`（转换后）写在该用例的导出目录里，方便排查。
- **验证：** 本机运行 `Convert_Spec`：没有任何数据检查造成的失败（失败的只有本机 PNG 差异 17 个和 `CroTable2` 超时 1 个，`VSScriptEffect` 被忽略）；反向验证：临时去掉 `DrillCroTable1` 的名单后，该用例因数据差异失败，信息列出 3 个组件的行数/列数变化，之后已还原。
- **局限：** 只对 `Convert_Spec` 现有的 19 个 vs 的形态有效；列名含特殊字符、预聚合列名这类新形态需要新建 vs（见 `vsfreehand.md` §7 的 G10、G11）。`CroTable2` 里的 `Thread.sleep(70000)` 没有动（目的未知，见 `vsfreehand.md` §6）；该用例的 `@Timeout` 从 80 秒提高到 120 秒，覆盖 sleep、转换、PNG 导出和数据检查（本机原来就要 86.7 秒）。
