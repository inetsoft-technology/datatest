# vspara 测试缺口与补充方案

> 日期：2026-11-09　模块：`vsothers`，profile `-Pvspara`，spec `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec`
> 产品源码分支：`main`（datatest 当前在 `Stateless-Sessions1`，两边实际版本是否一致未核对）

## 1. 结论

vspara 有 56 个用例，其中 54 个走宽松基类 `ViewsheetTest`，只有 2 个走会让导出失败的 `VSExportTest`。用例的"接线"面铺得不窄（WS 条件的 11 种运算符都带变量出现过，图表和交叉表的绑定、组件属性里的动态值有 32 个 vs），**问题是验证太粗、类型面太窄：**

1. **VarExp 两个 spec 的 32 个用例只比整页 PNG**（`VarExp_Binding_Spec` 22 个，`VarExp_Properties_Spec` 10 个里 8 个），图表、交叉表的数据没有导出比对。
2. **所有 56 个用例都不发送 `.__type__`**，而产品的 `ParameterService.readParameters` 在有该后缀时才做类型转换；该方法在产品仓库只有 1 个单测。浏览器真实走的转换路径两边都没测。
3. 验证机制会静默通过：导出失败只打印堆栈；`nullVSTest2`、`Para_query` 的基线里有 7 个永远不会被比对的陈旧文件。

先做：换严格基类并清掉陈旧基线；把 VarExp 的绑定类用例加上数据比对；补产品仓库的 `ParameterService`、`ParameterTool`、`Condition` 变量解析单测。

## 2. 覆盖情况

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

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | 验证机制：导出失败只打印、一个组件失败后面全不导出、基线里有陈旧文件 | 参数值导致某个 Table/Chart 求值抛异常，后面的组件也没导出，用例照样绿；陈旧基线让人以为这些组件被验证过 | 机制：vspara 换严格基类，先清 7 个陈旧基线文件 | 高 |
| G2 | VarExp 32 个用例只比整页 PNG | 动态值让图表绑到了错误的字段、交叉表聚合列错位，但整体像素在容差内或缩略后看不出来（容差是否存在未查）；数据层的错误全靠肉眼 | dashboard：对绑定类用例改用 `executeVS` 加 `compareData`（保留 PNG） | 高 |
| G3 | 没有任何用例发送 `.__type__`；产品 `ParameterService.readParameters` 仅在有该后缀时调用 `Tool.getData(type, v, true)` | 浏览器传 `x.__type__=date` 时转换出错（格式、时区、多值、空串），datatest 与现有单测都发现不了 | 单测 + dashboard 补 1 个代表用例 | 高 |
| G4 | 变量组件的类型面窄：没有 double、float、date、datetime、char | 这些类型的默认值、提示、多值转换出错 | 单测（转换）+ dashboard（1 个 ws 覆盖缺失类型） | 中 |
| G5 | `ParameterTool.parseParameters`（文本里的 `{parameter.x}` 及 number/text/date/currency/percent 格式）在产品仓库没找到测试引用；dashboard 只有 `Para_textparameter1/2` 两个整页 PNG | 文本参数的格式化（小数位、货币符号、日期格式）出错，只能靠像素 | 单测 | 中 |
| G6 | 带变量的条件在数组值（`ONE_OF`、`BETWEEN`）、空数组、含 null 元素时的解析，单测里变量相关用例都是标量（`ConditionTest` 中名称带 variable 的 13 个，静态看是标量与"缺失即忽略"） | 多选变量传 1 个/0 个/含 null 值时条件被误忽略或误应用，返回全量或空结果 | 单测 | 中 |
| G7 | 孤儿 vs `Para_source` | 数据源参数（query 作为 source）的接线出错无人发现 | dashboard：补用例 | 低 |
| G8 | `prompt=false` 路径（`Para_wsvariable2` 的注释要求关闭提示）：15 个变量组件全是 `prompt="true"` | 关闭提示后默认值/默认表达式不生效（该开关可能设在 VS 层，静态未核对） | 单测（待开发确认入口） | 低 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测（G3、G4、G5、G6、G8）；接线问题 → dashboard（G2、G7，以及 G3/G4 各一个代表用例）；验证机制本身 → 改基类（G1）。交互（筛选联动、参数面板输入）不属于静态 datatest，这里不排。

## 4. Java 单测补充

产品仓库路径 `community/core/src`。盘点方式：先找实现类，再按实现类名搜测试；下面"现有测试"只写读过方法名的，没读内容的不下结论。

| 被测类 | 现有测试（读过的） | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|---|
| `web.viewsheet.service.ParameterService#readParameters` | `ParameterServiceTest` 1 个（只测丢弃上下文变量） | `.__type__` 转换（string/int/double/date/boolean/多值）、单元素数组折叠成标量、`.__type__` 自身被跳过、选择类参数（`AssetUtil.isSelectionParam`）、转换失败的行为 | 9 | 低 | 浏览器实际发送哪些 type 取值；转换失败应报错还是回退字符串 |
| `report.internal.ParameterTool#parseParameters` | 未找到测试引用（`parseParameters`、`{parameter.`、`containsParameter` 在 test 目录 0 次） | `{parameter.x}` 的 number/text/date/currency/percent 格式、参数缺失、嵌套花括号、多个参数同一文本 | 10 | 低 | 格式串的合法写法；缺失参数应保留占位符还是替换为空 |
| `uql.Condition`（变量解析，含 `ConditionList`） | `ConditionTest` 103 个，名称含 variable 的 13 个，静态看是标量 | 数组变量用于 ONE_OF/BETWEEN、空数组、含 null、类型不匹配、LIKE/STARTING/CONTAINS/DATE_IN/TOP_N 各用变量解析 | 12 | 低 | 空数组条件是"忽略"还是"恒假"；这是产品语义，由开发定 |
| `uql.schema.UserVariable` / `uql.asset.DefaultVariableAssembly` | `UserVariableCdataTest` 9 个（名称看是序列化）；`DefaultVariableAssembly` 无同名测试 | 各 xtype 的默认值、默认表达式、多值、`prompt=false` 时取默认；序列化往返 | 8 | 中 | `prompt` 在 WS 与 VS 两层的优先关系 |
| `uql.viewsheet.DynamicValue` / `DynamicValue2` | `DynamicValue2Test` 3 个 | `=parameter.x`、`$(组件)`、普通脚本表达式、数组/空值、类型转换（int/double/boolean） | 8 | 中 | `$(组件)` 实际由哪个类解析（未查到，需开发指路） |
| `uql.util.XUtil#replaceVariable` | `XUtilReplaceVariableTest` 3 个 | `$(x)`/`${x}` 多种位置、转义、缺失变量、数组、编码选项 | 6 | 低 | `doEncode` 的预期 |

合计 53 个设计用例（不是已写好的）。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|
| 严格基类 | 改基类：vspara 三个 spec 的 `ViewsheetTest` 换为严格版（参考 `DcViewsheetTest`），VarExp 的 `exportAsPNG` 同样要在导出失败时抛异常 | 导出异常、少导出文件会让用例失败 | 先清 `Para_query` 的 2 个、`nullVSTest2` 的 5 个陈旧基线；换完要运行确认（静态预测未运行） |
| VarExp 绑定类数据比对 | 扩展：`VarExp_Binding_Spec` 22 个用例追加 `executeVS(null, ['(Home)','bk1'])` 与 `compareData`，保留 PNG；先用 `Chart_BindingGField` 一个试跑，再推广 | 动态值绑定后的图表/交叉表数据正确，不再只靠像素 | 需生成新基线；Chart 数据排序用 `sortVSDataSet`，已有 |
| 带 `.__type__` 的参数 | 扩展：`Para_wscondition`、`Para_dataview` 各加一个变体用例，发送 `x.__type__` | 浏览器真实传参路径下，类型转换后条件结果正确 | 先确认浏览器发送的 type 取值（单测同一问题） |
| 缺失变量类型 | 新建 ws + vs：一个 worksheet 含 double、float、date、datetime 的变量组件，各自驱动一个条件 | 这些类型的提示/多值/默认值接线 | 无 |
| 孤儿 `Para_source` | 新建用例（vs 已在 zip） | 数据源参数接线 | 需要确认期望参数值 |
| 文本参数格式 | 不新增：留给第 4 节 `ParameterTool` 单测；`Para_textparameter1/2` 保持各一个代表 | 格式化接线 | 无 |

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- **没有运行任何测试。** `strictcheck` 是静态预测；"换严格基类后 `Para_query`、`nullVSTest2` 会失败"需要运行确认。
- `param_scan.py` 是本轮新写的，只在 vspara 上运行过；条件里变量的值类型无法从 zip 静态得到，所以 G4 只能说"变量组件里没有这些类型"，条件变量的类型未知。
- 书签 `bk1` 里保存的是什么（选中项还是全部），静态无法判断；VarExp 动态值的取值来源是否都来自书签状态，未逐个核对。
- PNG 比对是否有容差、容差多大：`PNGCompare`（`CompareUtil.groovy:121`）未读，G2 的"在容差内看不出来"是推断。
- `docs/vs/VSParaDynimac/`（`vsparameter.xlsx`、`vsdynimac.xlsx`、`Hyperlink_Dynimac.zip`）里团队可能已有缺口记录，**本轮没有解析**；该目录下的 `Hyperlink_Dynimac.zip` 没有对应 spec，是否属于 vspara 范围要用户确认。
- `.__type__` 在前端的使用：只在 `show-hyperlink.service.ts` 看到有出现，未逐处读；浏览器发送哪些参数以开发为准。
- 产品源码在 `main` 分支，datatest 在 `Stateless-Sessions1`，产品版本是否一致未核对。
- 需要开发回答：空数组变量的条件语义；`prompt` 在 WS/VS 两层的优先关系；`$(组件)` 由哪个类解析；转换失败应报错还是回退字符串。
- 需要用户决定：是否把 `Hyperlink_Dynimac` 纳入；VarExp 绑定类用例是整体追加数据比对，还是先试一个。

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck <zip> <Spec.groovy> vsothers/src/test/resources/expectData/vspara/<Spec>
python .claude/workflow-shared/datatest-gap/param_scan.py params vsothers/src/test/resources/config/vspara-storage.zip
python .claude/workflow-shared/datatest-gap/param_scan.py cases vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/*.groovy
```

`<Spec.groovy>` 在 `vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vspara/`，三个 spec 分别用 `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec` 作为基线子目录名。
