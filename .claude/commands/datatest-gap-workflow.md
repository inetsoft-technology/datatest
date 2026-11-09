---
description: datatest 缺口评估，逐个模块分析 - 询问选哪个模块（一次一个）；审计验证机制、做覆盖矩阵、盘点产品仓库单测，最终只产出一个文件 docs/test-gap-assessment/<module>.md，包含覆盖情况、缺口、以及两类解决方向：Java 单测补充哪些、dashboard 用例补充哪些场景。只读，不落实。
argument-hint: [模块名，如 "vscalc"；留空 = 询问选哪个模块]
---

评估一个 datatest 模块，**只产出一个文件**：`docs/test-gap-assessment/<module>.md`，结构见 `<TOOLS>/module-template.md`。不写别的文档，不落实。

路径：`<TOOLS>` = `.claude/workflow-shared/datatest-gap`；`<PRODUCT>` = 产品源码仓库（本机默认 `E:\inetsoft\sr14_0\stylebi-main\community\core`）。
先读 `<TOOLS>/README.md`（路由规则、证据纪律、本机的坑）。可以把独立的部分（读产品源码核实语义、盘点某个类的单测）交给子代理，但文件由你写，数字由你核对。

## 先读：哪些验证过，哪些没有

验证过：`vs_backup_scan.py`（在 vscalc 上运行，结果与手工核对一致）；vscalc 已按本流程的产出格式做成样例 `docs/test-gap-assessment/vscalc.md`。
**没有端到端运行过：** 这套流程在其他模块上的表现；脚本在非 backup zip 资产上是否适用。汇报时说清楚哪些真的跑了。

## 步骤

一次只评估一个模块。参数没给模块名就读 `docs/test-gap-assessment/README.md` 的状态表，用 `AskUserQuestion` 让用户选，**不要自己挑**。

**1. 读现有资料，定位**
- 状态表；该模块已有的评估文件（有就在它的基础上更新，不推倒重来）。
- 团队已有的设计文档（可选）：`docs/vs/<模块相关目录>/` 下的 xlsx、md，扫一眼是否已有缺口记录（vscalc 的 `docs/vs/VSCalcfield/vscalcfield auto.xlsx` 里 `analysis`、`Miss checkpoint` 两个 sheet 就是）。有就用，没有就跳过，不为解析它写工具。
- 找到 spec、backup zip、基线目录、断言基类；记录产品源码分支（`git -C <PRODUCT> rev-parse --abbrev-ref HEAD`）。
- **向用户确认一次矩阵维度**：先用一句话说明该模块的"特性"是什么，再提议维度（vscalc 用"计算字段类型 × 使用位置 × 数据源"）。不自行默认。

**2. 审计验证机制（先于覆盖）**
读断言基类，每项给 `file:line`：比对遍历"实际导出"还是"基线清单"、导出失败是否让用例失败、`exportData` 是否清理且 CI 命令是否带 `clean`、CI 对失败的处理（`continue-on-error` / `catchError`）、严格版基类使用比例。

**3. 覆盖矩阵、断言层、单测盘点**
- 有 zip 与 Groovy spec 时运行 `vs_backup_scan.py` 的 `inventory`、`crosscheck`、`strictcheck`；不适用就说明，用等价办法并写明口径。
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

路由无法确定（单测与 dashboard 都说得通）；期望值取决于产品语义（列给开发，不替开发决定）；产品源码分支与 datatest 实际使用的版本明显不同；脚本不适用该模块的资产形式（问是否要写新扫描器）。
