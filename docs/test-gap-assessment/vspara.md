# vspara 测试缺口与补充方案

> 模块：`vsothers`，profile `-Pvspara`，spec `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec`；产品源码 `main`（2.1.0-SNAPSHOT）。
> 产品仓库 Java 单测的结果、行为规则、bug 清单见 [vspara-java-unit-tests.md](vspara-java-unit-tests.md)。

## 1. 结论

- **dashboard 用例够多（56 个用例、57 个 vs），问题在验证机制和断言层**：导出失败原先只打印堆栈，一个组件失败后面全不导出，基线里有陈旧文件；VarExp 的 32 个用例只比整页 PNG。
- **验证机制已严格化**（datatest PR #203、#206）；`VarExp_Binding_Spec` 的 22 个用例已加数据比对（89 个 `.txt` 基线）；7 个陈旧基线已删。
- **产品仓库 Java 单测已补**（PR #7974，加固 PR #8021）：14 个类 425 个用例，查出 14 个 bug（#81106–#81119）。原先的疑点 1、2、3、7 已在 `main` 修复（#7981）。
- **dashboard 侧剩：** 带 `.__type__` 的参数（G3）、缺失变量类型（G4）、孤儿 vs `Para_source`（G7）、`prompt=false`（G8）、15 个 Chart 用例的 `ChartN.png` 基线、`Para_basequerysql` 被 VPM 拒绝。
- 浏览器验证（第 6 节）服务没有启动，尚未执行。

## 2. 覆盖情况

**规模：** 56 个用例对 57 个 vs（zip 内共 57 个 vs、31 个 worksheet、32 个书签存储）；**孤儿 vs：`ParaVS/Para_source`**；基线目录 56 个。

| spec | 用例 | `executeVS`（逐组件 TXT/PNG） | `exportAsPNG`（整页 PNG） | `VSExportTest`（PNG+HTML） |
|---|---|---|---|---|
| Para_VS_Spec | 24 | 17（其中 4 个同时比 PNG） | 7 | 0 |
| VarExp_Binding_Spec | 22 | 0 | 22 | 0 |
| VarExp_Properties_Spec | 10 | 0 | 8 | 2 |

`Para_VS_Spec` 里 4 个用例不发送参数（`Para_wsvariable2`、`Para_wsFormulapara`、`Para_vpm1`、`nullVSTest1`）。`VarExp_*` 的 32 个用例只有 `Form_Variable` 传了参数，其余靠书签 `bk1` 里保存的选择状态驱动动态值。

**参数类型：** 变量组件 15 个，全是 `prompt="true"`；类型是 string、byte、boolean、integer、long、time、short，**没有 double、float、date、datetime、char**。条件里的变量（35 个条件）类型只能从 spec 字符串推断，静态看不出。

**使用位置：** WS 条件充分（等于 5、属于 10、小于 1、大于 5、介于 4、开头 2、包含 2、TopN 1、BottomN 1、DateIn 2、Like 2）；WS 脚本 `parameter.x` 只有 2 个 worksheet；VS `=parameter.x` / `$(组件)` 动态值 24 / 33 个 vs，充分但只比 PNG；文本参数格式只有 `Para_textparameter1/2` 两个整页 PNG。

**验证机制：**

- 比对遍历的是"实际导出的文件"，不是"基线清单"（`CompareUtil.groovy:472`），少导出文件时没有东西可比。
- `executeVS0` 只导出 Chart（PNG + TXT）、Table、Crosstab、CalcTable（TXT）；Text、Gauge、RadioButton、CheckBox、ComboBox、Slider、Selection、Calendar、TimeSlider、Image 的动态值只能靠整页 PNG。
- Jenkins 的 vspara 阶段**没有 `catchError`**，失败会让流水线变红（其他 4 个阶段是 UNSTABLE）；GitHub 的 vspara 步骤带 `continue-on-error: true`。
- 基线是回归基线还是独立标准答案：未查。书签 `bk1` 里存的是选中项还是全部状态：静态无法判断。

## 3. 缺口台账

| 缺口 | 说明 | 状态 |
|---|---|---|
| G1 验证机制静默通过 | 导出失败只打印、一个组件失败后面全不导出、基线里有陈旧文件 | **已处理**：PR #203 在 `ViewsheetTest` 基类默认严格（导出失败即失败、其余组件继续导出并汇总、基线文件必须都被导出）；#206 修了 `executeVS0` 遇到 `PairsDataSet` 的死循环；严格基类下 `Para_query`（`Chart1.png`、`Chart1.txt`）、`nullVSTest2`（`RadioButton1.txt`、`RadioButton1_selected.txt`、`SelectionList1.txt`、`SelectionList1_selected.txt`、`Text1.txt`）的 7 个陈旧基线因"未被导出"失败，已删除；遗留 `Para_basequerysql`，见第 5 节 |
| G2 VarExp 只比整页 PNG | 动态值绑错字段、交叉表聚合列错位在像素容差内看不出 | **Binding 已处理**：22 个用例加 `executeVS` + `compareData`。`executeVS0` 只能导出 Chart 的 `.png`/`.txt` 和表格的 `.txt`，所以 15 个 Chart 用例改为比每个 `ChartN.png` 加 `.txt`；7 个 Crosstab 用例保留整页 PNG（工具无法单独导出表格 PNG）加 `.txt`。PNG 基线在 GitHub 上生成。**待做：生成 15 个 `ChartN.png`。** Properties 的 10 个用例作用于 Text、Gauge、Selection 等，`executeVS` 导出不到，不在本轮 |
| G3 没有用例发送 `.__type__` | 浏览器传 `x.__type__=date` 时转换出错（格式、时区、多值、空串）无人发现 | 单测已做（`ParameterServiceTest`）；**dashboard 用例待做**，先确认浏览器发送的 type 取值 |
| G4 变量组件类型面窄 | 没有 double、float、date、datetime、char | 转换单测已做；**dashboard 用例待做**（一个 ws 覆盖缺失类型） |
| G5 文本参数格式 | number/text/date/currency/percent 格式出错只能靠像素 | 单测已做（`ParameterToolTest`） |
| G6 条件变量的数组、空数组、含 null | 多选变量传 0/1/含 null 值时条件被误忽略或误应用 | 单测已做；剩疑点 4、9 的产品决定 |
| G7 孤儿 vs `Para_source` | 数据源参数接线出错无人发现 | **待做**：新建用例，需先确认期望参数值 |
| G8 `prompt=false` 路径 | 关闭提示后默认值不生效 | 待开发确认两层优先级（见第 7 节）；新建带 `prompt=false` 的 ws 用例 |
| G9 哨兵参数 `NULL_VALUE` 等 | 内存过滤里哨兵被改错，或与 SQL 路径不一致 | 内存和 SQL 路径单测已做 |
| G10 变量默认值类型边界、JSON 往返 | 大数默认值存成 integer、`3.0E9`、多值日期重载后类型变 | 单测已做，其余产品已有；#80154 仍未修 |
| G11 MV × 变量条件（#79482、#79417、#80152、#80155） | 同一变量条件在 MV 前后结果不一致 | 不属于 vspara，转交 MV 专项 |
| G12 文本参数 `duration` | 文档列出，代码只认 text/date/number/currency/percent（#79691） | 单测固定现状 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测；接线问题 → dashboard；验证机制本身 → 改基类。交互（筛选联动、参数面板输入）不属于静态 datatest。

## 4. 缺陷与疑点（状态）

| # | 疑点 | 状态 |
|---|---|---|
| 1、2、3、7 | 文本参数值含 `{parameter.<自身>}` 时 `ParameterTool` 死循环；多值参数含 null 元素空指针；`}` 出现在 `{parameter.` 之前时所有参数不替换；`DynamicValue.fullEquals` 比较自身 | **已修**（#7981）；回归测试 `ParameterToolParsingTest`、`DynamicValueFullEqualsTest` |
| 4 | 空数组经 `VariableTable.put` 存成 null，条件被忽略 | 开放的产品决定（#80963 第 4 项），按现状钉住 |
| 5 | 日历 / 范围滑块参数传空串，结果是 `""` 不是 null | 开放的产品决定（#80963 第 5 项），按现状钉住 |
| 6 | `Condition.evaluate` 原地改写哨兵值 | 多半是设计（C11，不开 bug） |
| 8 | 内存 `NULL` 运算符把空串也当 null，SQL `IS NULL` 不会 | 开放的产品决定（#80963 第 7 项） |
| 9 | TOP_N / BOTTOM_N 的 N：Long 超 int 回绕成负数；N 缺失时保持 `$(n)` 引用 | 回绕是缺陷，已修（#81106）；"缺失时是否忽略排名"是开放决定（#80963 第 8 项） |
| 10 | `Para_basequerysql`：VPM 数据源上的手写 SQL 被拒绝 | 待问开发（第 5 节） |
| 11 | 表达式默认值 `3000000000` 的 Long 变量，提示框显示 `3.0E9` 并报错 | 缺陷，[#80154](https://issues.inetsoft.com/issues/80154) 未修；单测 `@Disabled("#80154")` |
| 12 | `{parameter.p, duration, ...}` 文档列出但未实现 | 文档还是实现缺陷未定（#79691） |

单测新查出的 14 个 bug 见 [vspara-java-unit-tests.md](vspara-java-unit-tests.md) §3。

## 5. 遗留：`Para_basequerysql`

严格基类下失败，原因是产品行为变化，不是 spec 写错：旧基线 `TableView1.txt` 是 VPM 出现之前导出的，用例以前只因为导出失败被吞掉才是绿的。

- worksheet `wsForVSPara/ws_basequerySQL` 在数据源 `MySQL1` 上用手写 SQL（`parseResult=3`，未解析），读 `orders.orders`、`orders.contacts`、`orders.order_details`、`orders.customers`，不读 `orders.products`。
- `MySQL1` 上有 VPM `VPMP1`，条件作用于 `orders.products`：`product_id >= $(Ppid)`。
- 不传 `Ppid`：报 `Variable in VPM condition missing: Ppid`（`VpmCondition.normalizeExpression`，经 `VpmUtil.hasBlindTableCondition`），即使这条 SQL 不读 `products`。
- 传 `Ppid=10`：报 `Query rejected by VPM ... the sql is not parsed and the tables it reads are unknown`（默认 `vpm.unparsed.sql.policy=reject`，`VpmUtil.handleBlindVPM`）。
- 已试：SQL 最前面加 `-- vpm.tables: orders.orders, ...`（`vspara-storage.zip` 可直接改：blob 摘要是内容的 MD5，同步更新 `blob-index.json` 的 `digest` 与 `length`），产品仍报"表未知"，原因未查清，已撤回。给 spec 加 `vpm.unparsed.sql.policy=warn` 会同时改变 `Para_vpm1~3` 测的行为，不建议无条件使用。
- **要问开发：** VPM 数据源上的原生 SQL 应怎样写才能通过；SQL 不读受保护的表也被拒绝，是否符合预期。
- 在此之前不把基线改成异常；用例已 `@Ignore`（尚未提 bug），得到回答后去掉 `@Ignore` 并更新基线。

## 6. 待验证清单（浏览器，服务启动后执行）

"bug 项"指 #80963 里的编号。结果列留空。V1–V3 对应的缺陷已修，用来验证修复。

| # | 对应 | 步骤 | 预期 | 结果 |
|---|---|---|---|---|
| V1 | 项 1（已修） | Text 内容 `{parameter.p}`，用 `?p=%7Bparameter.p%7D` 打开 | 显示字面 `{parameter.p}`，请求正常返回 | |
| V2 | 项 2（已修） | Selection List 绑定含 NULL 的列，多选 NULL 和另一项；Hyperlink 勾选"发送选择作为参数"，目标页放 Text `{parameter.列名}` | 显示 `[a,null]` 或跳过 null，不报错 | |
| V3 | 项 3（已修） | Text 内容 `{0} {parameter.name}`，用 `?name=abc` 打开 | 显示 `{0} abc` | |
| V4 | 项 4、G6 | ws 多选变量 `v`（条件：列 属于 `$(v)`），打开参数对话框不选任何值 | 与"空选"的约定一致（待决定）；现状：条件被忽略，显示全部行 | |
| V5 | 项 5 | Calendar 的 `_dateStart` 参数用链接传空值 | 待决定：null 还是空串；现状空串 | |
| V6 | 项 8 | 数据库一列同时有 NULL 和 `''`；ws 条件 `列 = $(p)`，`?p=NULL_VALUE`；比较 SQL 下推与内存过滤（Mirror 或 MV 之后）两种配置 | 行数一致，或开发说明为何不同；现状：SQL 只含 NULL 行，内存还含 `''` 行 | |
| V7 | 项 9 | ws 的 TopN，N 为 `$(n)`，不传 `n` 且 `prompt=false` 打开 | 与"变量缺失"的约定一致（待决定） | |
| V8 | #80154 | ws `zz79607/expr`（#80154 附件）：Long 变量 `v`，默认值表达式 `3000000000`，在 Composer 打开 | 显示 `3000000000`，无错误；现状：显示 `3.0E9` 并提示 "Please enter an integer" | |
| V9 | G8 | ws 变量 `prompt=false` 且有默认值，vs 不设 `disableParameterSheet` 打开 | 不弹对话框，用默认值；两层同时设置时的优先级待开发确认 | |
| V10 | G3 | `Para_wscondition` 的 vs，URL 带 `x=2024-03-15&x.__type__=date`（`x` 换成该 ws 的日期变量名） | 行数与 `?x=2024-03-15` 一致 | |
| V11 | `$(组件)` | 跑现有 `VarExp_Binding_Spec`（`-Pvspara`），确认 `$(组件)` 取值与书签状态一致 | 全部通过 | |

## 7. Dashboard 补充与待办

| 场景 | 做法 | 验证什么 | 前提 |
|---|---|---|---|
| VarExp 绑定类数据比对（G2） | 已实施 | 动态值绑定后的图表/交叉表数据正确 | 在 GitHub 上生成 15 个 `ChartN.png` 基线 |
| 带 `.__type__` 的参数（G3） | 扩展 `Para_wscondition`、`Para_dataview` 各加一个变体用例，发送 `x.__type__` | 浏览器真实传参路径下类型转换后条件结果正确 | 先确认浏览器发送的 type 取值 |
| 缺失变量类型（G4） | 新建 ws + vs：含 double、float、date、datetime 的变量组件，各驱动一个条件 | 这些类型的提示/多值/默认值接线 | 无 |
| 孤儿 `Para_source`（G7） | 新建用例（vs 已在 zip） | 数据源参数接线 | 确认期望参数值 |
| `prompt=false`（G8） | 新建带 `prompt=false` 的 ws 用例 | 默认值生效 | 开发回答两层优先级 |
| 文本参数格式（G5） | 不新增：留给 `ParameterTool` 单测，`Para_textparameter1/2` 各保留一个代表 | 格式化接线 | 无 |

原则：每个新用例只验证"接线"，边界和语义放进单测。

**其他待办：**

- 向开发确认：#80963 第 4、5、7、8 项（开放的产品决定）；`Para_basequerysql`；`prompt` 两层优先级；#81114（内存 LIKE 改成 SQL 通配符）对存量条件的影响。
- 产品 PR #8021：转 ready 或手动触发测试工作流确认 CI；已修 bug 对应的 `@Disabled` 去掉。
- 决定 `Hyperlink_Dynimac` 是否纳入 vspara（`Hyperlink_Dynimac.zip` 没有对应 spec），并解析 `docs/vs/VSParaDynimac/`（`vsparameter.xlsx`、`vsdynimac.xlsx`）里已有的缺口记录。

不在本轮：`VarExp_Properties_Spec` 的 10 个用例（`executeVS` 导出不到）。

## 8. 局限与待确认

- 运行情况：`VarExp_Binding_Spec`、`Para_VS_Spec` 在严格基类下运行过；其余用例没有运行。本机整页 PNG 与 GitHub 基线有像素差异（字体渲染，3%～13%），PNG 结果以 GitHub 为准。
- `param_scan.py` 只在 vspara 上运行过；条件变量的类型静态得不到，所以 G4 只能说"变量组件里没有这些类型"。
- `.__type__` 在前端只在 `show-hyperlink.service.ts` 看到出现，浏览器发送哪些参数以开发为准。
- `Hyperlink_Dynimac.zip` 是否属于 vspara 范围、`docs/vs/VSParaDynimac/` 里的缺口记录，未解析。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/param_scan.py params vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/param_scan.py cases vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/*.groovy
```

`<Spec.groovy>` 在 `vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/`，基线子目录名分别为 `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec`。
