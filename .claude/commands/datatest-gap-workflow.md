---
description: datatest 缺口评估，逐个模块分析 - 询问选哪个模块（一次一个）；审计验证机制、做覆盖矩阵、盘点产品仓库单测，最终只产出一个文件 docs/test-gap-assessment/<module>.md，包含覆盖情况、缺口、以及两类解决方向：Java 单测补充哪些、dashboard 用例补充哪些场景。只读，不落实。
argument-hint: [模块名，如 "vscalc"；留空 = 询问选哪个模块]
---

评估一个 datatest 模块，**只产出一个文件**：`docs/test-gap-assessment/<module>.md`，结构见 `<TOOLS>/module-template.md`。不写别的文档，不落实。

路径：`<TOOLS>` = `.claude/workflow-shared/datatest-gap`；`<PRODUCT>` = 产品源码仓库（本机默认 `E:\inetsoft\sr14_0\stylebi-main\community\core`）。

**分支约定：** datatest 的主分支是 `Stateless-Sessions1`，评估与 PR 都以它为准、PR 也 merge 到它；`main` 是旧分支，**不能**当评估依据（它缺 `DcViewsheetTest`、`onExportFailure` 以及 #203 的严格 `ViewsheetTest`，机制结论会不同）。产品源码对应 `main`（版本与 datatest `pom.xml` 的 `2.1.0-SNAPSHOT` 一致）。开始前先确认：`git rev-parse --abbrev-ref HEAD` 在 datatest 是 `Stateless-Sessions1` 系（或从它切出的分支），`git -C <PRODUCT> rev-parse --abbrev-ref HEAD` 是 `main`；不是就先停下问用户。
先读 `<TOOLS>/README.md`（路由规则、证据纪律、本机的坑）。可以把独立的部分（读产品源码核实语义、盘点某个类的单测）交给子代理，但文件由你写，数字由你核对。

## 先读：哪些验证过，哪些没有

验证过：`vs_backup_scan.py`（在 vscalc 上运行，结果与手工核对一致；`strictcheck` 的预测在 vscalc、vspara `Para_VS_Spec`、chart `TrendComparison_Spec` 上与实际运行一致）；vscalc 已按本流程的产出格式做成样例 `docs/test-gap-assessment/vscalc.md`。
**没有端到端运行过：** 这套流程在其他模块上的表现；脚本在非 backup zip 资产上是否适用。汇报时说清楚哪些真的跑了。
**3b、3c（深挖、防错）在两个模块上跑过：**
- **vsfreehand：** A（功能面差集）、C（分支清单）、D（已知 bug 反推）实际做了；B（运行时探测）没有重跑，直接引用了已有的探测结果（`docs/test-gap-assessment/vsfreehand-g2-convert-probe.md`），所以 B 作为流程步骤没被独立验证；E（输出形态矩阵）没做；3c 的三条是那次复查里出过的错。
- **ws：** A、D 和 3c 的三条都做了。A 用 `ws_scan.py capability`（产品清单写死在脚本里，产品变了要重读源码）；3c 的"读用户设置节点"用 `ws_scan.py flagcheck` 交叉核对；D 只取了 Redmine 分类 "Worksheet Execution" 最近一年的 118 条，根因类别是按标题粗分的，没有逐条读描述。ws 不是典型的生成型，只有"查询下推到 SQL 还是退回内存"这一个判断点符合，所以只对它做了 C（分支清单），B、E 没做。ws 上核对时发现过两处错（一处靠交叉核对，一处靠对照产品源码的类声明）：把接口当成可实例化的类（`ScriptedTableAssembly`、`SQLExpressionRef`），以及只看表条件、漏掉命名分组和日期范围里的 date in。
- **vsscript：** 评估做过（`docs/test-gap-assessment/vsscript.md`）。`vs_backup_scan.py` 不适用（用 `VSScriptTest.printVS`，不是 `ViewsheetTest`），改用 `<TOOLS>/vsscript_scan.py`（只在 vsscript 上运行；属性清单不含 Chart 的 EGraph 脚本对象）。本机跑 vsscript 会 114/115 个用例因 PNG 渲染差异失败，不能当通过依据。D 用 2000 条 `subject ~ script` 的 Redmine 记录分类，不是全量。
- 其他模块上都没验证过。
**脚本是照 vscalc 写的，换模块会不准，而且不准时不会报错**（适用范围见 `vs_backup_scan.py` 文件头的 Applicability 和 `<TOOLS>/README.md`）。用之前先确认适用，输出里的 "Not mapped"、"Not predicted" 必须写进结论的口径。

## 步骤

一次只评估一个模块。参数没给模块名就读 `docs/test-gap-assessment/README.md` 的状态表，用 `AskUserQuestion` 让用户选，**不要自己挑**。

**1. 读现有资料，定位**
- 状态表；该模块已有的评估文件（有就在它的基础上更新，不推倒重来）。
- 团队已有的设计文档（可选）：`docs/vs/<模块相关目录>/` 下的 xlsx、md，扫一眼是否已有缺口记录（vscalc 的 `docs/vs/VSCalcfield/vscalcfield auto.xlsx` 里 `analysis`、`Miss checkpoint` 两个 sheet 就是）。有就用，没有就跳过，不为解析它写工具。
- 找到 spec、backup zip、基线目录、断言基类；文件头记录 datatest 基线（`Stateless-Sessions1`）与产品源码 `main` 的 commit。
- **向用户确认一次矩阵维度**：先用一句话说明该模块的"特性"是什么，再提议维度（vscalc 用"计算字段类型 × 使用位置 × 数据源"）。不自行默认。

**2. 审计验证机制（先于覆盖）**
读断言基类，每项给 `file:line`：比对遍历"实际导出"还是"基线清单"、导出失败是否让用例失败、`exportData` 是否清理（CI 的 `clean` 在 `Jenkinsfile` 准备阶段与 GitHub 工作流里，不要只看测试阶段的命令）、CI 对失败的处理（`continue-on-error` / `catchError`）。`ViewsheetTest` 已默认严格（#203），机制是否生效以**实际运行**为准：跑该模块的 spec，记录导出异常、未导出的基线文件、导出了但没有基线的文件（PNG 像素差异单独列，本机与 CI 渲染不同）。本地运行前先 `./mvnw package -pl commons -DskipTests`，否则下游模块用的是旧的 commons test-jar。

**3. 覆盖矩阵、断言层、单测盘点**
- 有 zip 与 Groovy spec 时运行 `vs_backup_scan.py` 的 `inventory`、`crosscheck`；不适用就说明，用等价办法并写明口径。
  - `inventory`：vs、书签、组件类型、绑定表、脚本数是通用的；计算字段那几张表只适用于计算字段类模块，其他模块的矩阵维度要另行统计，不要把 0 当结论。
  - `crosscheck`：孤儿 vs 只对**同一个 zip 的全部 spec** 才有意义（多个 spec 共用一个 zip 时，单个 spec 的孤儿列表大多是其他 spec 的 vs）；资产 id 不是字面量的用例在 "Not mapped" 里，对应 vs 可能是假孤儿。
  - `strictcheck` 只是快速预判，能运行就以运行结果为准；"Not predicted" 的用例（子类、书签不是字面量）一律靠运行。
- 断言层：哪些组件不被导出、只能靠整页 PNG；书签是否带选择状态；关键对象是否出现在基线。
- 产品仓库单测盘点：先找实现类与运行时工厂，确认运行时实际走哪个类，再按**实现类名**找测试，看完整路径（同名类跨包）。

**3b. 深挖（所有模块做 A、D；"生成型"模块再做 B、C、E）**

只扫资产、只搜类名会漏两类东西：产品"能做什么"（资产里没用到的看不见），和运行时才产生的内容（转换、导出的结果在 zip 里不存在）。下面几步补这两类。

- **A. 功能面差集（所有模块）：** 先从产品源码枚举该特性的"能力清单"——脚本函数、选项串的取值、枚举值、配置项、转换的分支——再逐项对照资产与 spec 的使用次数，**零覆盖项逐个列出**（例见 `docs/test-gap-assessment/vsfreehand.md` 的功能面差集缺口）。清单必须来自产品源码并写 `file:line`，不是凭印象；资产里统计不到的写"未统计"。
- **D. 已知 bug 反推（所有模块，有 Redmine 记录时）：** 把相关 bug 按**根因类别**归类，看哪一类没有对应的验证；每个 bug 只写 id、根因类别、当前是否被覆盖。 结果写进模板的"已知 bug 对照"一节（步骤 4）。
  - **取数：** 优先用 `<TOOLS>/redmine_fetch.py`（账号从环境变量 `REDMINE_URL`、`REDMINE_KEY` 读，翻页到 `total_count`，不经过对话）；环境变量没设就问用户，不自己去翻 MCP 配置里的凭据。没有环境变量时退而用 `mcp__redmine__getIssues` 翻页，每页结果会落到文件，用脚本汇总，不读原文，并写明"只取了前 N 条，不是全量"。`redmine_fetch.py` 尚未在真实服务器上运行过，首次运行核对 `total_count`。
  - **分类：** `<TOOLS>/bug_classify.py` 是按标题关键词的粗分，规则写给 vsscript（脚本类 bug）；换模块要先改规则。每类抽样 7 条人工核对，写明相关比例，数字当上限；不读描述就不能写"根因是……"。
  - **口径必写：** 样本条件（关键词/分类、状态、时间范围）、取到几条、总数、过滤掉几条。**不得把一次 100 条的搜索当成结论**（vsscript 第一版就是这样，被指出样本太少）。
- **生成型模块的判断：** 特性的核心是"由 A 生成 B"（表转换、导出、模板展开、迁移升级）。是的话做 B、C、E，不是就跳过。
- **B. 运行时探测（生成型）：** 跑一遍生成过程，把**输出**落下来再统计（单元格类型/公式/展开方式、行列数、关键属性），不要只统计输入资产。**运行探测是允许的**（"只读，不落实"指不改产品和不提交修复，不是不能运行）：探测代码默认放 scratchpad，附录里写复现命令；如果探测值得留在仓库（例：vsfreehand 的 `ConvertDataProbe`，非 `*Spec` 类，CI 不会选中），要先问用户，且它不属于本流程要产出的那一个文件。写进文件的是汇总结果，不是原始 dump；可参考 vsfreehand 的 G2 探测（转换前后逐组件比较，区分"允许差异"与"待确认差异"）。
- **C. 分支清单（生成型）：** 对关键生成/转换方法列出 if/switch 分支（`file:line` + 一句话含义），逐个标注"哪个资产/用例会走到"：有、无、未验证。没有用例走到的分支就是缺口，写"会放过什么错误"。
- **E. 输出形态矩阵（生成型）：** 在输入维度之外，补一张按输出形态分的矩阵（例：转换后每种单元格类型 × 展开方式 × 聚合类型），数据来自 B。
- 生成型模块的矩阵维度仍要先向用户确认，并说明输入和输出两套维度。

**3c. 防错（这三条都是 vsfreehand 复查时出过的错）**

- **读用户设置的节点，不读派生节点。** 统计资产特性时（公式、计算器、日期比较、聚合、汇总开关等），要读用户在界面上设置的节点（如 `VSAggregateRef` 的 `formulaValue`、`<calculator>`、`<dateComparison>`），不要读运行时派生出来的节点（如 `ainfo`），派生节点里的值可能和用户设置不同，会得出错误的"没有/只有"。出现"没有 X""只有 Y"这类结论，必须在另一个节点或另一个资产上交叉核对一次，再写进文件。
- **"没有"结论的口径要写明查了哪里。** 例：只搜了 zip 里的某个标签，没搜别的节点，就写"静态，只查了 X，别处可能有"，不写成"未发现"。
- **新增缺口编号前先检查文件里已有的编号。** 文件里可能已有别的章节（如 Redmine 对照的 §7）用了同样的 `G<n>`。新增前用 `grep -n '^| G[0-9]'` 列出全部已有编号，从最大号继续，不要从当前表格的末尾想当然地接。

**4. 写那一个文件**
按模板。要点：
- **缺口**：每个都写"会放过什么错误"（逃逸场景），说不出就不写。
- **Java 单测补充**：按被测类一行，写补什么、用例数、难度（低/中/高）、哪些要开发先确认。用例数是设计出来的数量。**不给人天。**
- **Dashboard 补充**：按场景一行，写新建 vs / 扩展 / 改基类、验证什么、前提。
- **已知 bug 对照**（有 Redmine 记录时，步骤 3b-D）：按模板的"已知 bug 对照"一节写，每个 bug 只写 id、根因类别、当前是否被覆盖；没有记录就删掉这一节。
- 没核对过的写"未验证"或"静态推断"，不当事实陈述。
- 不放过程记录（撤回表、讨论时间线），不放原始脚本输出，只在附录放复现命令。
- 更新 `docs/test-gap-assessment/README.md` 的状态表。

**5. 汇报，然后停下**
用通俗的话汇报：结论、最大的缺口、两类方向各补什么、需要开发回答的问题、需要用户决定的事，以及本轮哪些步骤真的运行了。问用户下一个模块。

## 停下来问（不要自己决定）

路由无法确定（单测与 dashboard 都说得通）；期望值取决于产品语义（列给开发，不替开发决定）；datatest 不在 `Stateless-Sessions1` 系、或产品源码不在 `main`；脚本不适用该模块的资产形式（问是否要写新扫描器）。
