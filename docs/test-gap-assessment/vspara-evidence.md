# vspara 分析依据

> 这是 `vspara.md` 的依据部分：第一天的覆盖分析、缺口分析、源码确认的行为、相关历史 bug。结论和待办在 `vspara.md`，这里只在需要核对"为什么"时查。
> 章节编号 A1 到 A9 被 `vspara.md` 引用。

## A1 覆盖情况（数字来自 `param_scan.py`）

**规模：** 56 个用例对 57 个 vs（zip 内共 57 个 vs、31 个 worksheet、32 个书签存储）。**孤儿 vs：`ParaVS/Para_source`（没有用例）。** 基线目录 56 个，每个用例一个。

**覆盖矩阵**（维度：参数类型 × 使用位置 × 验证方式，已与用户确认）：

*验证方式 × spec（用例数）：*

| spec | 用例 | `executeVS`（逐组件 TXT/PNG） | `exportAsPNG`（整页 PNG） | `VSExportTest`（PNG+HTML） |
|---|---|---|---|---|
| Para_VS_Spec | 24 | 17（其中 4 个同时比 PNG） | 7 | 0 |
| VarExp_Binding_Spec | 22 | 0 | 22 | 0 |
| VarExp_Properties_Spec | 10 | 0 | 8 | 2 |
| 合计 | 56 | 17 | 37 | 2 |

数字来自 `param_scan.py cases`。`Para_VS_Spec` 里 4 个用例不发送参数（`Para_wsvariable2`、`Para_wsFormulapara`、`Para_vpm1`、`nullVSTest1`）。`VarExp_*` 的 32 个用例只有 `Form_Variable` 传了参数（`testVar`），其余靠书签 `bk1` 里保存的单选/复选状态驱动动态值。

*参数类型（worksheet 变量组件，15 个，来自 `param_scan.py params`）：*

| xtype | 单值 | 多值 | prompt |
|---|---|---|---|
| string | 4 | 3 | 7 |
| byte | 2 | 0 | 2 |
| boolean | 1 | 1 | 2 |
| integer / long / time | 1 / 1 / 0 | 0 / 0 / 1 | 1 / 1 / 1 |
| short | 0 | 1 | 1 |

变量组件里**没有出现** double、float、date、datetime、char。15 个全是 `prompt="true"`。注意 spec 里还发送了 `float`、`double_total`、日期等参数，它们走的是条件里的变量（35 个带变量的条件，`condition_data` 不带 xtype），类型只能从 spec 里的字符串推断，静态看不出被解析成什么类型。

*使用位置：*

| 使用位置 | 证据 | 评价 |
|---|---|---|
| WS 条件（带变量） | 35 个条件：等于 5、属于 10、小于 1、大于 5、介于 4、开头 2、包含 2、TopN 1、BottomN 1、DateIn 2、Like 2 | 运算符齐全；小于、TopN、BottomN 只有 1 个 |
| 分组、变量、SQL 查询、VPM、子查询 | `Para_wsgroup`、`Para_wsvariable`、`Para_basequery`、`Para_vpm1/2/3`、`Para_sub` | 有 |
| WS 脚本 `parameter.x` | 2 个 worksheet（`ws_jspara`、`ws_variable2`） | 少 |
| VS `=parameter.x` / `$(组件)` 动态值 | 24 / 33 个 vs | 充分，但见断言层 |
| 输出组件、高亮、文本参数格式 | `Para_output`、`Para_highlight`、`Para_textparameter1/2` | 有，只有 PNG |
| 逗号分隔值、NULL、不传参 | `Para_comma`、`nullVSTest2`、`nullVSTest1` | 有 |

**验证机制：**

- 比对遍历"实际导出的文件"，不是"基线清单"：`CompareUtil.groovy:472`。少导出文件时没有东西可比。
- 导出失败只打印堆栈：`ViewsheetTest.groovy:168-169`、`:294-295`（`onExportFailure`，基类里只 `printStackTrace`，`:177`）。`executeVS0` 的 `try` 包住整个 `assemblies.each`，**一个组件抛异常，后面所有组件都不再导出**，用例照样通过。
- 严格基类 `DcViewsheetTest` 已修这两点（抛异常、基线必须全部被导出，`DcViewsheetTest.groovy:25-60`），vspara 用了 0 个；`VSExportTest` 没有 `try/catch`（`VSExportTest.groovy:45-85`），导出失败会让用例失败，只用在 2 个用例上。
- 基线缺失会报错：`CompareUtil.groovy:249-253`（期望或结果文件不存在都判失败）。但这只覆盖"导出了却没基线"，不覆盖"没导出"。
- `exportData` 没有清理代码；`Jenkinsfile:37` 的准备阶段执行 `./mvnw clean -pl vsothers`，GitHub 的 `test-vsothers.yml` 先执行 `./mvnw clean`（该文件约第 71 行）。
- CI 对失败的处理：Jenkins 的 vspara 阶段（`Jenkinsfile:99-107`）**没有 `catchError`**，失败会让流水线变红，与其他 4 个阶段不同（它们在 `Jenkinsfile:49,64,78,91` 用 `catchError(buildResult: 'UNSTABLE')`）；GitHub 的 vspara 步骤带 `continue-on-error: true`（`test-vsothers.yml` 约第 95 行）。
- 静态预测（`strictcheck`，未运行）：17 个 `executeVS` 用例中，**预测会导出但基线里没有的文件：0 个**；**基线里有但不会被导出的陈旧文件 7 个**：`Para_query` 的 `Chart1.png`、`Chart1.txt`（该 vs 只有 1 个 Table），`nullVSTest2` 的 `RadioButton1.txt`、`RadioButton1_selected.txt`、`SelectionList1.txt`、`SelectionList1_selected.txt`、`Text1.txt`。换严格基类后这两个用例预计会失败，需先清基线。`strictcheck` 对 `exportAsPNG` 用例不适用（它按逐组件导出预测，而整页 PNG 只产生一个文件），这类用例的输出不计入上述数字。
- 基线是回归基线还是独立标准答案：**未查**。

**断言层：**

- `executeVS0` 只导出 Chart（PNG + TXT）、Table、Crosstab、CalcTable（TXT）（`ViewsheetTest.groovy:151-164`）。Text、Gauge、RadioButton、CheckBox、ComboBox、Slider、SelectionList/Tree、Calendar、TimeSlider、Image 的动态值只能靠整页 PNG。`inventory` 汇总：这些组件在 zip 里共 RadioButton 71、Text 19、CheckBox 21、Gauge 8 等。
- VarExp 的 32 个用例（`VarExp_Binding_Spec` 22 个、`VarExp_Properties_Spec` 10 个）里，**用动态值做绑定的图表和交叉表没有数据比对**，只靠像素。（zip 全部 57 个 vs 共有 Chart 51、Crosstab 30 个组件，这是总数，不是 VarExp 单独的数字。）
- `Para_VS_Spec` 的 WS 条件、分组、SQL、VPM 类用例导出了 TXT，断言层较好；但 `Para_wscondition` 的基线文件名 `Paraws_condition.txt` 与 vs 里的表名对应（预测一致），与其他用例的 `TableView1.txt` 命名风格不一致，不影响比对。
- 关键对象是否出现在基线：参数值是否真正进入了数据，只能由"过滤后的行"间接体现。`Para_wscondition` 的基线是 4 行 × 5 列，的确体现了条件被应用。
- 书签：`bk1` 里保存了选择状态，会进入被过滤的图表和表，但 VarExp 是整页 PNG，选择状态是否被正确恢复只能从像素看。静态无法判断书签里存的是选中项还是全部状态。

## A2 缺口分析原文

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 **【机制已解决，陈旧基线已清；`Para_basequerysql` 待查】** | 验证机制：导出失败只打印、一个组件失败后面全不导出、基线里有陈旧文件 | 参数值导致某个 Table/Chart 求值抛异常，后面的组件也没导出，用例照样绿；陈旧基线让人以为这些组件被验证过 | **处理结果：** 机制由 PR #203 在 `ViewsheetTest` 基类里默认严格化（导出失败即失败、其余组件继续导出并汇总、`compareData(null)`/`compareImage(null)` 要求基线文件都被导出），无需再迁移到 `DcViewsheetTest`；#206 修了 `executeVS0` 遇到 `PairsDataSet` 的死循环。**运行确认并已清理：** `Para_VS_Spec` 在严格基类下运行，`Para_query`（`Chart1.png`、`Chart1.txt`）、`nullVSTest2`（`RadioButton1.txt`、`RadioButton1_selected.txt`、`SelectionList1.txt`、`SelectionList1_selected.txt`、`Text1.txt`）这 7 个基线文件因"基线文件未被本次运行导出"而失败，已删除。严格基类还暴露了 `Para_basequerysql` 的导出失败（`TableView1: Variable in VPM condition missing: Ppid`，原先只打印堆栈、用例照样绿），原因待查（spec 缺参数，或产品行为变化），未在此处理 | 高 |
| G2 **【Binding 已处理，Properties 不在本轮】** | VarExp 32 个用例只比整页 PNG | 动态值让图表绑到了错误的字段、交叉表聚合列错位，但整体像素在容差内或缩略后看不出来；数据层的错误全靠肉眼 | **处理结果：** `VarExp_Binding_Spec` 的 22 个用例加了 `executeVS` + `compareData`（22 个用例都含 Chart 或表格，没有空转），89 个 `.txt` 基线已提交。`executeVS0` 只导出 `ChartVSAssembly`（`.png` + `.txt`）和 `TableDataVSAssembly`（`.txt`），所以：**15 个 Chart 用例**去掉整页 PNG，改为比每个 Chart 自己的 `ChartN.png` 加 `.txt`；**7 个 Crosstab 用例**保留整页 PNG 加 `.txt`（工具无法单独导出表格 PNG，整页 PNG 是格式检查的唯一手段）。PNG 基线在 GitHub 上生成，本机像素差异（3%～13%，字体渲染）属环境问题。`VarExp_Properties_Spec` 的 10 个用例作用于 Text、Gauge、Selection 等，`executeVS` 导出不到，不在本轮范围 | 高 |
| G3 | 没有任何用例发送 `.__type__`（前端只在参数值为字符串 `"null"` 时才发，见 A7-A，所以真实场景是"null 加类型"，不是任意值加类型）；产品 `ParameterService.readParameters` 仅在有该后缀时调用 `Tool.getData(type, v, true)` | 浏览器传 `x.__type__=date` 时转换出错（格式、时区、多值、空串），datatest 与现有单测都发现不了 | 单测 + dashboard 补 1 个代表用例 | 高 |
| G4 | 变量组件的类型面窄：没有 double、float、date、datetime、char | 这些类型的默认值、提示、多值转换出错 | 单测（转换）+ dashboard（1 个 ws 覆盖缺失类型） | 中 |
| G5 | `ParameterTool.parseParameters`（文本里的 `{parameter.x}` 及 number/text/date/currency/percent 格式）在产品仓库没找到测试引用；dashboard 只有 `Para_textparameter1/2` 两个整页 PNG | 文本参数的格式化（小数位、货币符号、日期格式）出错，只能靠像素 | 单测 | 中 |
| G6 | 带变量的条件在数组值（`ONE_OF`、`BETWEEN`）、空数组、含 null 元素时的解析，单测里变量相关用例都是标量（`ConditionTest` 中名称带 variable 的 13 个，静态看是标量与"缺失即忽略"） | 多选变量传 1 个/0 个/含 null 值时条件被误忽略或误应用，返回全量或空结果 | 单测 | 中 |
| G7 | 孤儿 vs `Para_source` | 数据源参数（query 作为 source）的接线出错无人发现 | dashboard：补用例 | 低 |
| G8 | `prompt=false` 路径（`Para_wsvariable2` 的注释要求关闭提示）：15 个变量组件全是 `prompt="true"` | 关闭提示后默认值/默认表达式不生效（该开关可能设在 VS 层，静态未核对） | 单测（待开发确认入口） | 低 |
| G9 **【内存路径已补；SQL 改写产品仓库已有覆盖】** | 哨兵参数 `NULL_VALUE` / `EMPTY_STRING` / `NULL_STRING`。Redmine 上有一整簇已修 bug（#77619、#77571、#77709、#77706、#77707、#77738、#77752、#78386，约束见 #78139）。产品仓库的 SQL 改写已有 19 个 `XUtilSentinel*` / `VpmConditionSentinel*` / `SQLBoundQuery*Sentinel*` 测试类；`Condition.evaluate` 的内存路径和 `ConditionListHandler.createCondNode` 没有 | 内存过滤里哨兵值被改错，或与 SQL 路径不一致 | 单测：已写 `ConditionVariableValueTest`（内存路径）；`createCondNode` 待补 | 中 |
| G10 | 变量默认值的类型边界：Long 默认值超过 2147483647、表达式默认值、默认值含 `]]>` 或控制字符、`VariableTable` JSON 往返里 `Object[]` 的元素类型与 Time/Timestamp 精度（#79607、#80154、#80155、#77903、#77528、#77472）。变量组件里没有 double/float/date/datetime/char 的现状见 G4 | 大数默认值存成 integer、提示框显示 `3.0E9` 并报"Please enter an integer"、MV 创建后默认值不生效、多值日期参数重新加载后元素类型变化 | 单测 | 中 |
| G11 | MV × 变量条件：条件为 "not equal to" / "not contains" 一个变量，MV 创建后条件被忽略或行数变化（#79482、#79417、#80152、#80155）。vspara 不涉及 MV | 同一变量条件在 MV 前后结果不一致 | 不属于 vspara：转交 MV 专项；这里只登记 | 低 |
| G12 | 文本参数格式里 `duration`：文档（`TextAndTitles.htm`）列出 `{parameter.p, duration, 'ddd HH:mm:ss'}`，代码 `ParameterTool.formatParameterValue` 只认 text/date/number/currency/percent，其余类型原样输出（#79691，Resolved） | 用户按文档写 `duration` 得到未格式化的值 | 单测（钉住现状，期望值随 #79691 的最终处理） | 低 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测（G3、G4、G5、G6、G8、G9、G10、G12）；接线问题 → dashboard（G2、G7，以及 G3/G4 各一个代表用例）；验证机制本身 → 改基类（G1）。交互（筛选联动、参数面板输入）不属于静态 datatest，这里不排。

## A3 Java 单测设计与已写清单

产品仓库路径 `community/core/src`。盘点方式：先找实现类，再按实现类名搜测试；下面"现有测试"只写读过方法名的，没读内容的不下结论。

| 被测类 | 现有测试（读过的） | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|---|
| `web.viewsheet.service.ParameterService#readParameters` | `ParameterServiceTest` 1 个（只测丢弃上下文变量） | `.__type__` 转换（string/int/double/date/boolean/多值）、单元素数组折叠成标量、`.__type__` 自身被跳过、选择类参数（`AssetUtil.isSelectionParam`）、转换失败的行为；上下文变量 `_USER_`、`_ROLES_`、`_GROUPS_` 逐个被丢弃（#77329，伪造后绕过 VPM）；日期类型在非 UTC 时区不偏移（#78730） | 12 | 低 | 已确认（A7-A）：转换规则源码明确，可直接写；转换失败数字类型返回 `null`、其他回退字符串 |
| `report.internal.ParameterTool#parseParameters` | 未找到测试引用（`parseParameters`、`{parameter.`、`containsParameter` 在 test 目录 0 次） | `{parameter.x}` 的 number/text/date/currency/percent 格式、参数缺失、嵌套花括号、多个参数同一文本；未知格式类型（`duration`）原样输出（#79691）。动手前先在 `community/core/src/test` 搜 `ParameterTool`、`formatParameterValue`，避免与"测试覆盖率项目"已写的用例重复 | 11 | 低 | 已确认（A7-D）：缺失参数输出 `"null"`；数组含 `null` 元素疑似 NPE，需单测确认 |
| `uql.Condition`（变量解析，含 `ConditionList`） | `ConditionTest` 103 个，名称含 variable 的 13 个，静态看是标量 | 数组变量用于 ONE_OF/BETWEEN、空数组、含 null、类型不匹配、LIKE/STARTING/CONTAINS/DATE_IN/TOP_N 各用变量解析；哨兵参数 `NULL_VALUE`/`EMPTY_STRING`（G9，见 A8）：`=`、`<>`、`>`、`LIKE` 的改写、BETWEEN、IN 与混合 IN 列表、左操作数、`||` 拼接、子查询 | 20 | 低 | 内存路径已确认（A7-B）：经 `put` 的空数组被忽略；SQL 路径（`ConditionListHandler`）对空数组行为待确认（Q3） |
| `uql.schema.UserVariable` / `uql.asset.DefaultVariableAssembly` | `UserVariableCdataTest` 9 个（名称看是序列化）；`DefaultVariableAssembly` 无同名测试 | 各 xtype 的默认值、默认表达式、多值、`prompt=false` 时取默认；序列化往返；Long 默认值超过 2147483647（字面量与表达式两种，#79607、#80154）；默认值含 `]]>` 或控制字符的 XML 往返（#77903）；`VariableTable` JSON 往返里 `Object[]` 元素类型与 Time/Timestamp 精度（#77472、#77528） | 14 | 中 | 同名时 VS 输入组件覆盖 WS 变量已确认；`prompt` 两层（WS `isPrompt`、VS `disableParameterSheet`）同时设置的优先级待确认（Q4） |
| `uql.viewsheet.DynamicValue` / `DynamicValue2` | `DynamicValue2Test` 3 个 | `=parameter.x`、`$(组件)`、普通脚本表达式、数组/空值、类型转换（int/double/boolean）；多选组件 0/1/多个/含 null 元素（#79636、#72707）；公式里 `$(selection)` 跨刷新保持上一次的值（#79688）；`Integer` 传给 `String` 参数的转换（#76778） | 12 | 中 | 已确认（A7-C）：`$(组件)` 由 `ViewsheetSandbox.executeVariableValue` 解析 |
| `uql.util.XUtil#replaceVariable` | `XUtilReplaceVariableTest` 3 个 | `$(x)`/`${x}` 多种位置、转义、缺失变量、数组、编码选项；值含引号（#79742）；内置会话变量 `$(_USER_)` 用在 SQL WHERE（#79841）；1901 年以前日期与时区（#77450、#78730） | 10 | 低 | 已确认（A7-E）：`doEncode` 对每个元素单独 URL 编码，只认 `$(x)` |

合计 79 个设计用例（不是已写好的）。

**已写好的单测**（community 仓库 PR #7974，分支 `test-vspara-parameter-unit-tests`；7 个类一起运行 162 个用例全部通过）：

| 测试类 | 对应 | 说明 |
|---|---|---|
| `ConditionVariableValueTest` | `Condition` 变量解析、哨兵（G6、G9 内存路径） | 17 个 |
| `ParameterServiceTest` | `readParameters`（G3） | 在原有 1 个上增加 32 个 |
| `ParameterToolTest` | `ParameterTool`（G5、G12） | 30 个 |
| `VariableTableJsonTypedValueTest` | `VariableTable` JSON 往返（G10 的 #77472、#77528） | 17 个 |
| `XUtilReplaceVariableValueTest` | `XUtil#replaceVariable` | 约 25 个 |
| `DynamicValueTest` | `DynamicValue` | 约 35 个 |
| `ConditionListHandlerSentinelTest` | `createCondNode` 哨兵，在 Derby 上与内存结果对比（G9 的 SQL 路径） | 7 个 |
| `ConditionVariableOperatorTest` | STARTING_WITH / CONTAINS / LIKE / DATE_IN 用变量、变量缺失、类型化条件（G6 的剩余部分） | 约 40 个 |
| `RankingConditionVariableTest` | TOP_N / BOTTOM_N 的 N 取自变量 | 13 个 |

**核对后不需要再写的：** `UserVariable` 的 XML / JSON 默认值往返（`UserVariableCdataTest`、`VariableDefaultCdataRoundTripTest`）、Long 默认值超过 int 范围（`VariableAssemblyDialogServiceDefaultTypeTest`）、`DynamicValue2` 的数字解析（`DynamicValue2Test`）产品仓库已有。

**没有写单测的三项：**

| 项 | 原因 | 验证方式 |
|---|---|---|
| `$(组件)` 的解析（`ViewsheetSandbox.executeVariableValue`，A7-C） | 需要运行时 viewsheet，单测里搭不起来 | Dashboard：现有 `VarExp_*` 用例已经走这条路径，缺的是数据比对（Properties 部分不在本轮范围） |
| `prompt=false` 取默认值（G8，`uql/service/XEngine.java:1741`） | 需要集成环境；WS 与 VS 两层 prompt 的优先级待开发确认（Q4） | Dashboard：新建带 `prompt=false` 的 ws 用例，等 Q4 有答案再定期望值 |
| #80154（表达式默认值 `3000000000` 在提示框显示 `3.0E9`） | 属于提示框的数值格式，不在 `UserVariable` 里，对应的类还没定位 | 前端复现，不是 vspara 的内容 |

## A4 Dashboard 补充设计

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 严格基类 **【已处理：PR #203 已把严格校验放进 `ViewsheetTest` 基类，不再需要迁移；以下为原方案，仅供追溯】** | 迁移，不新写基类：把 `new ViewsheetTest(` 换成 `new DcViewsheetTest(`，分两批。**第一批** `VarExp_Binding_Spec`（22 处）、`VarExp_Properties_Spec`（8 处；另 2 处已是 `VSExportTest`）：每个基线目录只有 1 个 PNG，与 `exportAsPNG` 的导出对应，`DcViewsheetTest` 对 `exportAsPNG` 的失败同样抛异常（`ViewsheetTest.groovy:295` 走 `onExportFailure`），预计可直接换。**第二批** `Para_VS_Spec`（24 处）：先清 `Para_query` 的 2 个、`nullVSTest2` 的 5 个陈旧基线再换 | 导出异常、少导出文件会让用例失败 | 以上是静态检查，换完要运行确认 |
| VarExp 绑定类数据比对 **【已处理：Binding 22 个用例，见 G2；待办：在 GitHub 上生成 15 个 Chart 用例的 `ChartN.png` 基线】** | 已按 G2 的方案实施；原计划（先试 `Chart_BindingGField` 再推广）已被替代 | 动态值绑定后的图表/交叉表数据正确，不再只靠像素 | `Chart_BindingSpecial1` 曾因 `executeVS0` 的 `PairsDataSet` 死循环卡住，已由 #206 修复 |
| 带 `.__type__` 的参数 | 扩展：`Para_wscondition`、`Para_dataview` 各加一个变体用例，发送 `x.__type__` | 浏览器真实传参路径下，类型转换后条件结果正确 | 先确认浏览器发送的 type 取值（单测同一问题） |
| 缺失变量类型 | 新建 ws + vs：一个 worksheet 含 double、float、date、datetime 的变量组件，各自驱动一个条件 | 这些类型的提示/多值/默认值接线 | 无 |
| 孤儿 `Para_source` | 新建用例（vs 已在 zip） | 数据源参数接线 | 需要确认期望参数值 |
| 文本参数格式 | 不新增：留给A3 `ParameterTool` 单测；`Para_textparameter1/2` 保持各一个代表 | 格式化接线 | 无 |

每个新用例只验证"接线"，边界和语义放进A3的单测。

## A5 局限与待确认

- **运行情况：** `VarExp_Binding_Spec`、`Para_VS_Spec` 在本机运行过（严格基类下）；其余用例未运行；Java 单测的运行结果见 1.2。本机的整页 PNG 与 GitHub 基线有像素差异，属环境差异，PNG 结果以 GitHub 为准。
- `param_scan.py` 是本轮新写的，只在 vspara 上运行过；条件里变量的值类型无法从 zip 静态得到，所以 G4 只能说"变量组件里没有这些类型"，条件变量的类型未知。
- 书签 `bk1` 里保存的是什么（选中项还是全部），静态无法判断；VarExp 动态值的取值来源是否都来自书签状态，未逐个核对。
- PNG 容差：`PNGCompare` 未读，不再影响结论（PNG 基线在 GitHub 上生成，本机只看数据比对）。
- `docs/vs/VSParaDynimac/`（`vsparameter.xlsx`、`vsdynimac.xlsx`、`Hyperlink_Dynimac.zip`）里团队可能已有缺口记录，**本轮没有解析**；该目录下的 `Hyperlink_Dynimac.zip` 没有对应 spec，是否属于 vspara 范围要用户确认。
- `.__type__` 在前端的使用：只在 `show-hyperlink.service.ts` 看到有出现，未逐处读；浏览器发送哪些参数以开发为准。
- 需要开发回答的问题见下面 "A6 待开发确认的问题"。
- 需要用户决定：是否把 `Hyperlink_Dynimac` 纳入 vspara（`Hyperlink_Dynimac.zip` 没有对应 spec）。VarExp 绑定类用例已按整体追加数据比对处理。

## A6 待开发确认的问题（已对照源码核对）

2026-11-10 对照产品源码 `E:\inetsoft\sr14_0\stylebi-main\community\core\src\main\java`（以下路径都相对它）逐项核对。**已能从源码确定的，不再需要问开发，行为记在 A7，直接作为单测期望值；仍需确认的只剩下表里标"待确认"的。** 处理方式暂定：倾向提 bug / 问题单给开发，修复后补 Java 单测；也可由我们自己补，逐项决定。

| # | 问题 | 结论 | 状态 |
|---|---|---|---|
| Q1 | VPM 数据源上的手写 SQL（`parseResult=3`，不读受保护表）该怎么写才能通过？不读 `orders.products` 的 SQL 也被拒绝是否符合预期？加 `-- vpm.tables:` 后仍报"表未知"是用法不对还是产品问题？ | 见上面的 TODO | **待确认（问开发）** |
| Q2 | `.__type__` 取值与转换失败的行为 | 与产品设计无关，源码已明确，见 A7-A。前端只在参数值为字符串 `"null"` 时才发 `.__type__`（`web/projects/portal/src/app/vsobjects/show-hyperlink.service.ts:119`） | **已确认，可直接写单测** |
| Q3 | 变量为空数组时条件是"忽略"还是"恒假" | 内存过滤已确定：**缺失、null、`[null]` 和经 `VariableTable.put` 存入的空数组都被忽略（不过滤）**，只有绕过 `put` 时空数组才匹配不到任何行，见 A7-B。SQL 生成路径（`ConditionListHandler`）对空数组怎么处理未确定（可能生成 `IN ()`）。前端空选时显示全部，是因为不传值而不是传空数组，这一点以前端为准 | **内存路径已确认；SQL 路径（`ConditionListHandler`）对空数组的处理未确定，但经 `put` 的空数组到不了那里** |
| Q4 | `prompt` 在 WS 与 VS 两层的优先关系 | WS 变量与 VS 输入组件同名时，**VS 组件覆盖**（不再弹窗：`analytic/composition/event/VSEventUtil.java:1550` 起的 `refreshParameters`，`vobj instanceof InputVSAssembly` 时跳过）。`prompt=false` 有两层：WS 变量自带的 `UserVariable.isPrompt()`（`uql/service/XEngine.java:1741`，false 时用默认值、不提示）和 VS 选项 `disableParameterSheet` / `disabledVariables`（`ViewsheetInfo`，不弹给用户）。两层同时存在时的取值优先级未读清 | **同名覆盖已确认；两层同时设置时的优先级待确认** |
| Q5 | `$(组件)` 由哪个类解析 | `report/composition/execution/ViewsheetSandbox.java` 的 `executeDynamicValue` → `executeVariableValue`，规则见 A7-C | **已确认，可直接写单测** |
| Q6 | 文本参数 `{parameter.x}` 的格式串与缺失参数 | `report/internal/ParameterTool.java`，见 A7-D | **已确认，可直接写单测** |
| Q7 | `XUtil#replaceVariable` 的 `doEncode` | `uql/util/XUtil.java:5740`，见 A7-E | **已确认，可直接写单测** |

## A7 源码确认的行为（单测期望值的依据）

以下是读源码得到的，没有运行验证；写单测时按它断言，断言失败就说明理解有误或有产品问题。

**A. `.__type__` 与类型转换**（`web/viewsheet/service/ParameterService.java:61` `readParameters`，`util/CoreTool.java` `getData(type, val, strictNull)`）
- 名字以 `.__type__` 结尾的参数、上下文变量（`VariableTable.isContextVariable`）不进变量表。
- 有 `x.__type__` 时，对 `x` 的每个值调用 `Tool.getData(type[0], v, true)`；没有则保持字符串。结果长度为 1 时折叠成标量，否则保持数组（长度 0 的数组也保持数组）。选择类参数（`AssetUtil.isSelectionParam`）再走 `getSelectionParam`，失败只打日志、回退原值。
- 类型名只按"首字母（不区分大小写）+ 长度"识别（`getTypeCode`），所以 `Integer`、`integer` 都行，任何以 `i` 开头的 7 个字母的串也会被当成 integer。支持：`null`、`string`、`boolean`、`float`、`double`、`char`、`character`、`byte`、`short`、`integer`、`long`、`timeInstant`、`date`、`time`、`array`、`color`（`enum` 没有分支，原样返回字符串）。
- `strictNull=true`：值为 `"__null__"`（`FAKE_NULL`）或 `null` 返回 `null`；字符串 `"null"` 不是 null。`type` 为 null 返回原值。
- 转换失败：数字类型解析失败（`NumberFormatException`）返回 `null`；其他异常（如日期解析失败）返回原字符串；`boolean` 解析不了按 `false`；`long` 整数解析失败时返回 `Double`（不是 `Long`）；`string` 在 `strictNull=true` 下空串保持空串。
- 结果最后经 `VariableTable.put` 存入：没有非 null 元素的数组（空值数组、所有元素转换失败）存成 null。日历、范围滑块参数（名字以 `_dateStart`、`_dateEnd`、`_start`、`_end` 结尾）走 `getSelectionParam`：空串得到 null，随后回退成原始空串；带 `.__type__` 时值已不是 `String`，转换抛异常，保留类型转换的结果。
- 因此"前端传 `"null"` 加类型"的结果依类型而异：string → `"null"` 字符串；integer/double/long 等 → `null`；date → `"null"` 字符串（解析失败回退）。这是需要用单测钉住的行为，是否合理可再问开发。

**B. 条件里变量的解析**（`uql/Condition.java:369` `replaceVariable`，`:599` `evaluate`）
- 值是变量时：变量缺失或为 null → 该条件 `ignored=true`，`evaluate` 恒为 true（等于不过滤）。
- `ONE_OF` 且变量值是长度为 1 且元素为 null 的数组 → 同样忽略。
- 经 `VariableTable.put` 存入的数组，只要没有非 null 元素（`{null}`、`{null,null}`、空数组 `Object[0]`），都被存成 null，条件因此被忽略（不过滤），对所有运算符都成立。**空数组走这条路径，与"空选"显示全部一致。**
- 只有绕过 `put`（例如子类重写 `VariableTable.get`）时，空数组才不为 null、不被忽略；`ONE_OF` 对它匹配不到任何行。`{null}` 在这条路径下只对 `ONE_OF` 被忽略。
- 变量引用的写法取决于条件类型：`Condition(String type)` 默认 `ctype=true`，`getValue` 会把字符串值按类型转换，所以非字符串类型下文本 `"$(v)"` 被读成 `0`，不是变量，不会被替换；类型化条件要用 `UserVariable` 对象，或用 `new Condition(false, false)`。字符串类型下转换是恒等的，`"$(v)"` 可用。变量只在整个值恰好是 `$(name)` 时才替换，`"pre-$(v)"` 不替换。
- 变量缺失时，EQUAL_TO、LESS_THAN、GREATER_THAN、STARTING_WITH、CONTAINS、LIKE、ONE_OF、DATE_IN 都被忽略。DATE_IN 的周期文本可来自变量（如 `this year`），未知周期匹配不到任何行，不被忽略。
- TOP_N / BOTTOM_N（`RankingCondition.replaceVariable`）：N 取自变量，数字取 `intValue`（`2.9` 得 2，`3000000000L` 溢出成负数），文本按整数解析，多值取第一个。变量缺失、非数字文本、空数组时 N 保持为未解析的引用（条件不会被忽略），下游怎么处理未核对。
- 哨兵值 `NULL_VALUE`、`EMPTY_STRING`、`NULL_STRING`（`Condition.evaluate`，内存路径）：字符串值 `NULL_VALUE` 把运算符改成 `NULL`；`EMPTY_STRING` 改成 `""`；`NULL_STRING` 改成 `"null"`。`ONE_OF` 的单元素数组同理（`NULL_VALUE` 改运算符，其余替换元素）；多元素数组里的哨兵不改写，只记一条警告。`evaluate` 会修改条件本身（运算符、值、数组元素）。`ConditionListHandler.createCondNode` 的 SQL 路径规则相同。
- SQL 生成路径（`uql/jdbc/util/ConditionListHandler.java:410` 的 `ONE_OF`）逐个取值拼成 `(v1,v2,...)`；长度 1 的数组里是 `CONDITION_NULL_VALUE` 则改成 NULL 条件。空数组会拼成空括号，行为未确认。

**C. `$(组件)` 的解析**（`ViewsheetSandbox.executeDynamicValue` / `executeVariableValue`，`uql/viewsheet/internal/VSUtil.java:291`）
- 动态值整体形如 `$(name)`（以 `$(` 开头、`)` 结尾）→ 取名为 `name`（去掉结尾 `.drillMember`）的 VS 组件的值：选择树取 `autodrill` 数据；单值输入组件取 `getSelectedObject()`；复合输入组件取 `getSelectedObjects()`：0 个 → `null`，1 个 → 该元素，多个 → 数组；其他类型的组件或不存在 → `null`。
- 以 `=` 开头是脚本，执行脚本；含 `$(...)` 嵌在文本中的，走 `matchVariableFormula`，用组件值替换列名片段；已是普通列名或数据源名的不再按动态值处理。
- `$(x)` 指的是 **VS 里的输入组件**，不是 WS 变量；WS 变量/参数要用脚本 `=parameter.x`。

**D. 文本参数**（`report/internal/ParameterTool.java`）
- 格式：`{parameter.var}`、`{parameter.var, number, '$#,###M'}`、`{parameter.var, text, 'aaaaa'}`、`{parameter.var, date, 'MM/d/yy'}`、`{parameter.var, percent}`、`{parameter.var, currency}`。`containsParameter` 要求同时含 `{parameter.` 和 `}`，且 `}` 的位置大于起点 + 10。
- 参数缺失或值为 null → 输出字符串 `"null"`（不是空串，也不是保留占位符）；带格式但值为 null 也是 `"null"`。
- `number` 用 `ExtendedDecimalFormat(spec)`；`currency`/`percent` 用 `NumberFormat` 的默认语言环境实例；数字解析失败原样返回字符串；`date` 只在值是 `Date` 时格式化，否则返回 `toString()`；`text` 用 `MessageFormat(spec)`（`{0}` 为值）。格式串出错时忽略格式。
- 数组值：输出 `[v1,v2]`，每个元素各自格式化；`currency`/`percent`、带 `'...'` 格式串的对数组值不整体格式化。
- 多个参数、`spec` 里含 `{0}` 这类花括号：`parseParameter` 在遇到 `'` 在 `}` 之前时，按 `'}` 找结尾。
- **疑似问题：** 数组里含 `null` 元素时，`values[i].toString()`（`:219`）在 `try` 之外，会抛空指针。应写单测确认；如果真的抛，就是产品 bug，提给开发。

**E. `XUtil.replaceVariable(str, vars, doEncode)`**
- 只认 `$(name)`，不认 `${name}`；变量缺失或为 null 时占位符原样保留。
- 数组：每个元素 `Tool.toString`，`doEncode` 为真时每个元素单独 `encodeURL`，用 `,` 连接；空数组得不到值，占位符保留。
- 标量：`toString()`；若变量带日期格式（`vars.getFormat`）且是 `Date`，按该格式输出（对 1901 年以前的日期专门处理，bug #77450），再按需 `encodeURL`。格式出错回退 `toString()`。
- 循环终止条件：找不到 `$(` 或 `)` 位置不大于 `$(` 起点 + 2（例如 `$()`）即停止，后面的占位符不再处理。

## A8 相关历史 bug（单测期望值的依据）

2026-11-10 按标题搜 Redmine（parameter / variable，每页 100 条，约 390 条，扣掉 wiz、schedule、报表 Parameter Sheet、连接器、权限类后约 60 条），只读了 #80154、#79691、#78386 的正文，其余按标题归类，写测试前要读对应 bug 的正文和修复 PR。更早的老 bug 没有翻完。

| 簇 | bug | 状态 | 对应单测 | 备注 |
|---|---|---|---|---|
| 哨兵参数 | #77619、#77571、#77709、#77706、#77707、#77738、#77752、#77620、#77606、#77574、#77708、#78386 | 已关闭 | SQL 改写已有测试；`Condition.evaluate`（已写）、`ConditionListHandler.createCondNode`（待补）（G9） | 混合 IN 列表保持原样并记警告：产品决定见 #78139 section E。#77757、#77751、#77758 为 Rejected，其中 #77751 标题注明未复现，不作为期望值 |
| Long / 默认值 | #79607（已关）、#80154（New）、#80155（New，MV）、#77903（已关） | 部分未修 | `UserVariable` / `DefaultVariableAssembly`（G10） | #80154 未修：期望值写成"显示 `3000000000`"，当前会失败，先 `@Disabled` 并注明 bug 号，修复后启用。与 A7-A "`long` 整数解析失败返回 `Double`" 疑似同一根因，用一个单测对照 |
| VariableTable 状态 | #77528、#77472 | 已关闭 | 同上 | 往返断言：元素类型、Time/Timestamp 精度 |
| 文本参数 | #79691 | Resolved | `ParameterTool`（G12） | 文档缺陷还是实现缺陷未定，按修复结果取期望值 |
| 值替换 | #79742、#79841、#77450、#78730 | 已关闭 / Resolved | `XUtil#replaceVariable`、`readParameters` | #78730 在 UTC 以西时区触发，测试里显式设置 `TimeZone` 并在 `@AfterEach` 恢复 |
| 上下文变量 | #77329 | 已关闭 | `ParameterService#readParameters` | 逐个断言 `_USER_`、`_ROLES_` 等不进变量表 |
| 多选 / 动态值 | #79636、#72707、#79688、#76778、#79945 | 已关闭 / Resolved | `DynamicValue` / `DynamicValue2` | #79636 标题称是 #616 的回归，用例名带上 bug 号 |
| MV × 变量条件 | #79482、#79417、#80152、#80155 | 部分未修 | 不在 vspara（G11） | 转交 MV 专项 |
| 不适用 | #78233（故障转移丢 URL 参数） | 已关闭 | 集群回归 | 不在 vspara 范围 |

## A9 遗留：`Para_basequerysql` 详情

严格基类下该用例失败，原因是产品行为变化，不是 spec 写错；旧基线 `TableView1.txt` 是 VPM 出现之前导出的，用例以前只因为导出失败被吞掉才是绿的。

- worksheet `wsForVSPara/ws_basequerySQL` 在数据源 `MySQL1` 上用手写 SQL（`parseResult=3`，未解析），读 `orders.orders`、`orders.contacts`、`orders.order_details`、`orders.customers`，不读 `orders.products`。
- `MySQL1` 上有 VPM `VPMP1`，条件作用于 `orders.products`：`product_id >= $(Ppid)`。
- 不传 `Ppid`：报 `Variable in VPM condition missing: Ppid`（`VpmCondition.normalizeExpression`，经 `VpmUtil.hasBlindTableCondition`），即使这条 SQL 并不读 `products`。
- 传 `Ppid=10`：报 `Query rejected by VPM ... the sql is not parsed and the tables it reads are unknown`（默认 `vpm.unparsed.sql.policy=reject`，`VpmUtil.handleBlindVPM`）。
- 已试：在 SQL 最前面加 `-- vpm.tables: orders.orders, orders.contacts, orders.order_details, orders.customers`。`vspara-storage.zip` 可以直接改（blob 摘要是内容的 MD5，同步更新 `blob-index.json` 的 `digest` 与 `length` 即可，不用起环境），但改后产品仍报"表未知"，原因未查清；已撤回。
- 另一个做法：给 spec 加 `vpm.unparsed.sql.policy=warn`，但会同时改变 `Para_vpm1~3` 测的行为，不建议无条件使用。
- 要问开发：VPM 数据源上的原生 SQL 应怎样写才能通过；SQL 不读受保护的表也被拒绝，是否符合预期。

在此之前不要把基线改成异常。用例已加 `@Ignore`（原因同上，尚未提 bug），用例内只保留三行说明，细节在本节。得到开发回答后去掉 `@Ignore` 并更新基线。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/param_scan.py params vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/param_scan.py cases vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/*.groovy
```

`<Spec.groovy>` 在 `vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/`，三个 spec 分别用 `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec` 作为基线子目录名。
