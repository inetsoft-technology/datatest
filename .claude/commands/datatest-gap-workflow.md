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

**4. 写那一个文件**
按模板。要点：
- **缺口**：每个都写"会放过什么错误"（逃逸场景），说不出就不写。
- **Java 单测补充**：按被测类一行，写补什么、用例数、难度（低/中/高）、哪些要开发先确认。用例数是设计出来的数量。**不给人天。**
- **Dashboard 补充**：按场景一行，写新建 vs / 扩展 / 改基类、验证什么、前提。
- 没核对过的写"未验证"或"静态推断"，不当事实陈述。
- 不放过程记录（撤回表、讨论时间线），不放原始脚本输出，只在附录放复现命令。
- 更新 `docs/test-gap-assessment/README.md` 的状态表。

**5. 汇报，然后停下**
用通俗的话汇报：结论、最大的缺口、两类方向各补什么、需要开发回答的问题、需要用户决定的事，以及本轮哪些步骤真的运行了。问用户下一个模块。

## 停下来问（不要自己决定）

路由无法确定（单测与 dashboard 都说得通）；期望值取决于产品语义（列给开发，不替开发决定）；datatest 不在 `Stateless-Sessions1` 系、或产品源码不在 `main`；脚本不适用该模块的资产形式（问是否要写新扫描器）。
