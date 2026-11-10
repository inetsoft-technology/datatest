# vscalc 测试缺口与补充方案

> 日期：2026-11-09　模块：`vsothers`，profile `-Pvscalc`，spec `VS_CalcField_Spec`
> datatest 基线：`Stateless-Sessions1`（主分支）；产品源码：`main`（`f12b07ca59`，2.1.0-SNAPSHOT，与 datatest `pom.xml` 版本一致）。第 4 节的产品单测盘点已在 `main` 上复核。

## 1. 结论

vscalc 的 dashboard 覆盖面不窄：22 个用例覆盖了 11 种图表类型、明细型和聚合型两类计算字段、各种聚合函数，团队设计表里列的缺口基本都已落实。**问题不在"有没有用例"，而在"用例能不能发现错误"：**

1. **验证机制会静默通过**：导出失败只打印堆栈、基线文件缺失不报错，而且 CI 失败也不会让流水线变红。
2. **基线只是产品自己的输出**，只能发现回归，不能证明"算得对"。产品仓库里聚合型计算字段的求值只有 1 个值级单测，vscalc 在用的 5 个聚合函数没有专门单测。

先做两件事：直接修改 `ViewsheetTest`，把 Stateless-Sessions1 上 `DcViewsheetTest` 的严格逻辑合进基类（`main` 上没有 `DcViewsheetTest`），一次覆盖 chart、vsothers 共 31 个 spec，严格即默认，不加开关；给孤儿 vs `aggregate_trendcomparison` 补用例。再补产品仓库单测（9 组，共 99 个用例设计），最后补 dashboard 的薄弱场景。

## 2. 覆盖情况

**规模：** 22 个用例对应 22 个 vs，zip 里共 23 个 vs，**`aggregate_trendcomparison` 没有用例也没有基线**。40 个书签（保存选择状态），spec 引用的书签都存在。

**覆盖矩阵**（维度：计算字段类型 × 使用位置 × 数据源，已与用户确认）：

| 计算字段类型 | date | string | double | integer | time | timeInstant | boolean | 合计 |
|---|---|---|---|---|---|---|---|---|
| 明细型（逐行计算） | 13 | 18 | 9 | 11 | 2 | 4 | 1 | 58 |
| 聚合型（对聚合结果再算） | 6 | 3 | 25 | 5 | 1 | **0** | **0** | 40 |

| 使用位置 | 覆盖的 vs 数（共 23） | 评价 |
|---|---|---|
| Chart 的 x / y / 维度 / 度量 | 14 / 16 / 18 / 19 | 充分 |
| Chart 的 color / size / text / **shape** | 7 / 4 / 4 / **2** | shape 薄 |
| Crosstab 的维度 / 度量 / **条件** | 12 / 12 / **3** | 条件薄 |
| CalcTable 的单元格 / 度量 / 维度 / **条件** | 9 / 9 / 8 / **1** | 条件很薄 |
| Table 的列 / **条件** | 6 / **2** | 条件薄 |
| Calendar / SelectionList / SelectionTree / TimeSlider | 6 / 6 / 5 / 3 | |
| Gauge / Text | 3 / 3 | 类型和表达式单一（仅 integer，表达式均为"其他"） |

数据源：绑定的表有 Query1（6 个 vs）、OrdersAndReturns（4）、Order Model（4）等，其余基本一对一；底层是 DB2、MySQL 还是 embedded 未细分。
图表类型已有 BAR、BAR_STACK、LINE、PIE、FUNNEL、GANTT、MAP、TREE、NETWORK、SUNBURST、TREEMAP；没有 AREA、POINT、RADAR、STOCK/CANDLE、BOXPLOT、MEKKO、WATERFALL、PARETO、STEP 等。

**验证机制：**

- 比对遍历的是"实际导出的文件"，不是"基线清单"：`CompareUtil.groovy:472`。导出少了文件时没有东西可比，用例照样通过。
- 导出失败只打印堆栈：`ViewsheetTest.groovy:177`（`onExportFailure`）。
- Stateless-Sessions1 上已有严格版基类 `DcViewsheetTest`（`commons/.../modules/DcViewsheetTest.groovy`；`main` 上没有）：导出失败抛异常（`:22`），不给文件名时要求基线文件必须全部被导出（`prepareCompare`），并支持 `-Dupdate.baseline=true`。但使用它的 spec 只有 2 个（`DateComparison_Spec`、`Crosstab_DateComparsion_Spec`）。vscalc 用的是宽松版。严格逻辑已经写好，不用开发新框架，只需要合进基类（第 3 节 G1 方案）。
- `exportData` 没有清理代码，但 CI 都会先清：Jenkins 准备阶段执行 `./mvnw clean -pl vsothers`（`Jenkinsfile:37`），GitHub 工作流先执行 `./mvnw clean`（`test-vsothers.yml:71`）。上一次导出的残留文件不是问题（本地手工重跑除外）。
- CI 对失败的处理：GitHub 工作流 5 个测试步骤全是 `continue-on-error: true`；Jenkins 的 vscalc 阶段是 `catchError(buildResult: 'UNSTABLE')`，失败只让构建变 UNSTABLE。
- 静态预测：22 个用例的基线文件与预期导出文件**完全一致**，所以换严格基类预计不会因文件集合而失败（未运行）。
- 基线来源（回归基线还是独立标准答案）：**未查**。

**断言层：** 只有 Chart（TXT + PNG）和 Table、Crosstab、CalcTable（TXT）会导出数据。Selection、Calendar、TimeSlider、Gauge、Text 自身的内容只能靠整页 PNG（22 个用例中 19 个调用了 `exportAsPNG`）。但书签保存了选择状态，它们的**过滤效果**会进入被过滤的图表和表的数据：绑定计算字段的选择类组件 × 书签共 42 对，其中 23 对带有已应用的选择状态，分布在 11 个 vs。
另有 6 个 vs 的个别计算字段名没有出现在基线 txt 里：`parent-child tree` 0/1、`calc_data group` 3/6、`filter` 4/5、`testDB2` 5/6、`testModel` 6/7、`testQuery` 7/8（子串匹配，可能是只做了条件或换了表头，需核对）。

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 导出失败、基线缺失不报错；一个组件失败后面的组件都不导出；CI 失败不挡合入 | 某个组件导出抛异常或少导出文件，用例仍然通过 | 机制：直接改 `ViewsheetTest`，把 `DcViewsheetTest` 的严格逻辑合进去（见下） | 高 |
| G2 | 孤儿 vs `aggregate_trendcomparison` | 趋势/比较类聚合的计算字段结果出错，无人发现 | dashboard：补用例 | 高 |
| G3 | 聚合型计算字段求值只有 1 个值级单测，且表达式只是聚合名本身 | `Sum/Sum`、`Average*Average`、`Mode+10`、`if…else` 这类表达式算错 | 单测 | 高 |
| G4 | Correlation、WeightedAverage、DistinctCount、Mode、NthMostFrequent 在 `report/filter` 无专门单测（已有的同名测试属于 MV 路径，不能替代） | 这些聚合函数实现改错，只有 vs 回归基线可能发现 | 单测 | 高 |
| G5 | 明细型计算字段的值级语义没有系统测试 | 四则运算、除零、null、拼接、日期、参数替换出错 | 单测 + dashboard 代表用例 | 中 |
| G6 | 基线只是产品自己的输出 | 已有的错误结果被固化为基线，永远不报错 | 单测的手算期望值；新基线抽样独立核对 | 中 |
| G7 | 选择类、展示类组件自身内容只有 PNG | 选择列表里计算字段的取值列表出错，但在 PNG 容差内 | dashboard：扩展基类 | 中 |
| G8 | 薄弱格子：聚合型 timeInstant/boolean、shape/text/size、condition 位置、Gauge/Text 类型、新图表类型 | 计算字段在这些位置取值或渲染出错 | dashboard：新建小 vs | 中 |
| G9 | 上述 6 个 vs 的个别计算字段没有数据断言 | 这些字段的结果出错，数据比对发现不了 | dashboard：核对后补 | 低 |

### G1 方案：直接改 `ViewsheetTest`

**为什么不只迁移 vscalc：** 宽松逻辑在基类里，所有继承它的 spec 都有同样的问题（vspara 的 G1 也是这个问题）。逐个 spec 迁移，两套基类会长期并存，新 spec 照抄 `new ViewsheetTest(` 还是宽松的。直接改基类，一次覆盖所有 spec。

**影响面**（chart、vsothers 两个模块，都通过 `commons` 的 test-jar 引用基类）：

| 使用方式 | spec 数 | 调用处 |
|---|---|---|
| 直接 `new ViewsheetTest(` | 24（chart 10，vsothers 14） | 410（210 + 200） |
| 子类 `VSCalcTest`（Convert、FreeExecution、Freehand） | 3 | 82 |
| 子类 `VSActionsTest`（Actions）、`VSFormImportTest`（FormTable） | 2 | 7 + 3 |
| `DcViewsheetTest`（两个 DateComparison spec） | 2 | 2 |
| **合计** | **31** | |

这 31 个 spec 中共有 836 处 `compareData`/`compareImage` 调用，其中 834 处没有传文件名，所以"基线文件必须都已导出"的检查几乎对所有用例生效。传了文件名的路径不用改：导出文件缺失时，`CompareUtil.FileCompare` 已经会报 "not found"。

**改什么**（`commons/.../modules/ViewsheetTest.groovy`）：

1. `onExportFailure`（`:177`）默认改为抛异常，不再只打印堆栈。
2. `executeVS0` 的 `try` 包住了整个 `assemblies.each`（`:140-170`），第一个组件抛异常后，后面的组件都不会导出。改成每个组件单独 catch，把失败收集起来，循环结束后一次抛出并列出所有失败的组件；否则即使改成严格模式，每次也只能看到第一个错误。另外 `sandboxOpt.get()`（`:133`）在 `isPresent` 判断之外，没有 sandbox 时会抛 `NoSuchElementException`，应改成明确报错。
3. `compareData`/`compareImage` 不传文件名时，要求基线文件都已导出（逻辑上移自 `DcViewsheetTest.prepareCompare`）。
4. 支持 `-Dupdate.baseline=true` 写回基线（逻辑上移自 `DcViewsheetTest.updateBaseline`；PNG 默认只补缺失的文件，不覆盖已有的）。
5. 用例开始时清空本 case 的 exportData 目录。现在只删除同名文件（`:353`），而第 3 条检查的是导出目录里有没有这个文件：本地重跑时，上次残留的文件会让"少导出"的情况漏检。CI 先执行 `mvn clean`，不受影响。清理放在构造函数里，前提是没有 spec 在同一个 case 里创建两个实例（**未核对**）。
6. `VSFormImportTest:54` 自己也吞异常（`ex.printStackTrace()`），同样要改；`VSCalcTest` 已经会抛异常，`VSActionsTest` 没有 catch。
7. `DcViewsheetTest` 不改。基类新加的方法改名避开了它的同名 private 方法（`checkBaselineExported`、`exportFolder()` 等），它照常编译运行；严格逻辑在它和基类里各执行一次，结果一致。

**上线：** 严格即默认，不加开关。基类提交后，各模块自己跑、自己处理变红的用例（产品 bug 报给开发，陈旧基线删除或用 `update.baseline` 重新生成并抽样核对）。

**本机运行结果**（2026-11-10，PNG 像素差异因本机与 CI 渲染不同，不计入）：

- vscalc：22 个用例，数据比对全部通过，没有导出异常，没有未导出的基线文件；22 个失败全是 PNG 像素差异（文字抗锯齿/字体）。
- vspara：查出 3 个用例。`Para_basequerysql` 导出异常 `Variable in VPM condition missing: Ppid`（`MySQL1` 的 VPM `VPMP1` 要求变量 `Ppid`，spec 未传），以前被吞掉、导出目录为空而假通过；`Para_query`、`nullVSTest2` 是静态预测过的 7 个陈旧基线文件。其余失败为 PNG 像素差异和 `VSExportTest` 的 HTML 比对（与本改动无关）。

**风险与未知：**

- 其他模块会失败多少，静态上算不出来，要各模块自己跑。`vs_backup_scan.py strictcheck` 只适配 vscalc 的写法：没有 `executeVS` 也按 `(Home)` 推算组件文件；`exportAsPNG` 只认第一个参数是书签列表的写法，认不出 `exportAsPNG(null, [...])`；不认识 `exportAsPDF` 生成的 `-0.png` 页图；不分析 `VSCalcTest` 等子类。拿去跑 chart 等 spec 会大量误判，所以不采用。已实际运行的：vscalc 0 个问题，vspara 3 个用例（见上）。
- 改的是 `commons` 的测试代码，chart、vsothers 都依赖它的 test-jar，改完要先重新构建 `commons`，再跑下游模块。
- 光改基类挡不住合入：GitHub 工作流仍是 `continue-on-error`，Jenkins 仍是 UNSTABLE（第 6 节，由你决定）。

## 4. Java 单测补充（产品仓库 `core`）

运行时走 `report/filter` 版本公式，不走 `mv/formula`（`Util.createFormula`，`mv.formula` 只在物化视图改写时启用）。用例数是设计出来的数量，不是已写好的。

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|
| `CalcFieldFormula` | 多聚合运算、条件分支、除零、null、字符串结果 | 14 | 中（需脚本环境） | 除零返回什么；字符串结果 |
| `WeightedAverageFormula` | 基本、null、权重和为 0、负权重 | 13 | 低（纯单元） | 权重和为 0 返回什么 |
| `CorrelationFormula` | 已知值、方差为 0、一对数据、大偏移精度 | 11 | 低 | NaN 处理 |
| `NthMostFrequentFormula` / `ModeFormula` | 频次、并列、n 越界、null | 15 | 低 | null 语义；并列规则 |
| `DistinctCountFormula` | 去重、null、空集、百分比 | 11 | 低 | 空集返回 0 |
| 明细型计算字段求值（`ExpressionRef`） | 算术、拼接、null、日期函数、参数、类型不一致 | 17 | 高（测试入口未确认） | 多项 |
| `CalculateRef` 序列化 | `baseOnDetail` 默认值与往返 | 7 | 低 | 无 |
| 公式工厂一致性 | 26 个枚举值都能创建 `report/filter` 实例 | 3 | 低 | 无 |
| 已有公式的通用边界 | 并列、负数、NaN、整数溢出 | 8 | 低 | NaN、溢出 |
| **合计** | | **99** | | |

按优先级：前五行加工厂一致性共 67 个，是 G3、G4 的主体，大多是纯单元，先做；明细型求值要先确认测试入口。

## 5. Dashboard 补充

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| 验证机制（G1） | 不改 spec，改 `ViewsheetTest`（第 3 节 G1 方案）。就 vscalc 而言，`strictcheck` 静态预测 22 个用例的基线与导出文件集合完全一致（基线多出 0 个、导出缺基线 0 个），所以预计不会因文件集合失败 | 导出失败、基线缺失不再静默通过 | 各模块自行运行并处理变红的用例 |
| 孤儿 vs（G2） | 给 `aggregate_trendcomparison` 加 feature 和基线：书签 `(Home)`、`bk1`，预计导出 7 个文件 | 趋势/比较聚合的计算字段 | vs 已在 zip 里；基线用 `-Dupdate.baseline=true`（仅严格基类可用），生成后抽样独立核对 |
| 聚合型 timeInstant、boolean（G8） | 新建小 vs | 目前 0 个 | 在 StyleBI 里制作并导出 backup 并入 zip |
| Chart 的 shape / text / size 视觉绑定（G8） | 新建小 vs | 计算字段用于视觉绑定 | 同上 |
| Table / Crosstab / CalcTable 的 condition 位置（G8） | 新建小 vs | 计算字段用于条件 | 同上 |
| Gauge / Text 非 integer 类型（G8） | 新建小 vs | 计算字段用于 Gauge、Text | 同上 |
| 新图表类型（G8） | 新建小 vs（AREA、POINT、RADAR、WATERFALL 等择要） | 计算字段在这些图表上取值与渲染 | 同上；先确认产品允许这些图表绑定计算字段 |
| 选择类组件自身内容（G7） | 扩展 `executeVS0` 导出这些组件的数据 | 选择列表的取值列表等 | `commons` 的 `ExportUtil` 已有 `exportSelectionList`，可能可复用；能否从沙箱取到数据未验证 |
| 个别计算字段未入基线（G9） | 核对那 6 个 vs，确认是只做条件还是漏断言 | | 无 |

做法原则：**新建小 vs，不改已有 vs**（改已有 vs 会让已有基线整体变动，混进噪声）；每个新用例只验证"接线"，边界和语义放在第 4 节的单测里，不为边界值堆 vs。zip 是整个 storage 的二进制备份，改完后用 `vs_backup_scan.py inventory` 前后对比，确认只有预期的 vs 变了。

## 6. 局限与待确认

**需要开发回答（决定单测期望值怎么写）：**
1. null 语义不统一：`NthMostFrequent`/`Mode` 把 null 当作一个值计数，`DistinctCount`、`Sum` 等忽略 null。是否有意？
2. 空集返回值不统一：`DistinctCount` 返回 0 且 `isNull()=false`，其他公式返回 null。
3. 除零与 NaN：聚合型表达式除零；`Correlation` 方差为 0 时 `getResult` 返回 null 而 `getDoubleResult` 返回 NaN。
4. `Mode` 并列按值升序，是否为有意的契约。
5. Integer 与 Double 混合时是否视为同一值。
6. `CorrelationFormula.addValue(double[])` 的下标约定，以及这条路径是否有调用方。
7. 日期函数的月末语义是否遵循 Excel。

**读源码发现的疑似缺陷（均未运行确认，可能是读错）：**
`NthMostFrequentFormula` 无参构造时 n 为 0，取结果时疑似越界；`CorrelationFormula` 用一次遍历求和公式，大偏移数值下疑似精度丢失；`DistinctCountFormula` 百分比只处理 `Integer` 类型的总数；`Correlation.addValue(double[])` 下标与 `WeightedAverage` 不一致。

**需要你决定：**
- 严格模式要不要真正挡住合入：目前 GitHub 工作流 `continue-on-error`、Jenkins 为 UNSTABLE，改基类只是把"静默通过"变成"测试失败"。
- PNG 基线在哪个平台生成，要和 CI 一致。
- G8 里要做哪几个新场景。

**没验证的：**
- 严格模式的实际效果只是静态预测，而且只对 vscalc 做了预测；运行时异常，以及其他 30 个 spec 会失败多少，都要跑过才知道。
- 单测盘点按类名、文件名、方法名静态匹配，没有逐个读测试内容。
- `donut chart` 这个 vs 的类型是 PIE（3），不是 DONUT（0x13），是否有意未核实。
- 选择状态是否真的缩小了数据范围，静态上无法判断（书签可能只保存选中项）。
- 只覆盖 vscalc 一个 spec，其他模块未评估。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vscalc-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
```
