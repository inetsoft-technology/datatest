# vsscript 测试升级方案（现状版）

> 更新：2026-11-20　模块：`vsscript`　Redmine：Test Case #81430
> 本页只放：目标和放置规则、方向的完成情况、验收、TODO。原始方案、验证数据、变异清单、JaCoCo 结果是本文件的上一版：`git show 5487b4c0:docs/test-gap-assessment/vsscript-upgrade-plan.md`（本页的“旧版 §x”指它）。
> 怎么跑、代码结构、写用例的配方：`vsscript-state-guide.md`。手工用例自动化：`vsscript-manual-automation.md`。

## 1. 为什么、做什么

vsscript 原来只有“整页 PNG 像素比对”一条验证路径，但脚本的效果是**状态**不是像素；脚本在刷新路径上的错误还会被吞掉（`ViewsheetSandbox.java:1685`），PNG 基线可能在脚本已坏时照样通过。方案是加一条**状态断言**路径：不启动服务、不导出 PNG，直接打开真实的 viewsheet，读脚本效果、触发点击、记录发给前端的消息、解析导出文件（`state/`）。

目标（领导，2026-11-18）：升级 vsscript 的测试，让自动化覆盖更全、发现更多问题；手工用例尽量自动化。范围：只做探针和现有框架的升级，需要新 Dashboard（资产）的写进 §5 TODO。

## 2. 一个发现或一条回归测试放哪里（按顺序判断）

1. Java 单测已经覆盖 → 不在 datatest 加用例。
2. 还没有，但 Java 单测能写 → 补 Java 单测（bug 未修的用 `@Disabled("Bug #xxxxx ...")` 写期望行为，修好后去掉）。
3. 只有 Java 单测够不到的才放 datatest：真实打开的 vs 与资产、刷新/提交/导出流程、真实绘图、对全部 130 个资产的检查。

探针只用来**发现**，结论落到哪一层由上面的顺序决定。

## 3. 方向和完成情况

| # | 方向 | 状态 | 落点 |
|---|---|---|---|
| D1 | 脚本健康检查（130 个 vs、213 段脚本直接执行） | **完成**：0 个真实脚本错误，3 个误报 | 探针 `ScriptStateHealthProbe`；结果旧版 §5.1 |
| D2 | 异常契约（刷新吞异常 / 直接执行抛 / 点击抛） | **导出路径已定**：#81796 修好（PR #8467），导出时失败的组件脚本被跳过、文档带警告文字，`ScriptStateExportTripwire_Spec` 已改成固定这个行为；**刷新路径仍等开发**确认（NPE / ClassCastException 按 #81633 的设计只写日志） | `ScriptStateExportTripwire_Spec`、`ScriptStateRefreshTripwire_Spec` |
| D3 | 事件端到端（Submit、confirm、parameter、点击、输入、选择、flyover） | **完成**；嵌套 vs 的 `onClick` 需新资产 | `ScriptStateSubmit_Spec`、`ScriptStateManual_Spec` |
| D4 | 旧 PNG 用例改状态断言 | **放弃**（收益低，2026-11-18） | — |
| D5 | 触发顺序（ONINIT / ONLOAD） | **完成**（2026-11-23）：不需要新资产，脚本在真实资产上内存里设置。onInit 只跑一次、onLoad 每次刷新都跑；onInit 写的带“设计值/运行值”的属性第二次刷新就回到设计值（文档里的已知陷阱）。注意：产品里没有独立的 ONREFRESH 脚本，Composer 里的 “onRefresh” 就是 onLoad，旧版把它们数成两个，“0 个 ONREFRESH”不是资产缺口 | `ScriptStateTriggerOrder_Spec` |
| D6 | 差分 | **完成**，以“和 13.8 对比”的形式（找 bug 最多的方法） | 本地 `diff13/`（含机器路径，不提交） |
| D7 | 导出一致性 | **完成**：导出不报错总检；脚本值进 Excel / PDF；8 个 Table / Freehand 的整行与状态一致 | `ScriptStateExportTripwire_Spec`、`ScriptStateExportContent_Spec`；本表 |
| D8 | 数据函数端到端（写回） | **完成**：`appendRow`、`setCellValue`、`applyChanges`、`saveWorksheet` | `ScriptStateWriteBack_Spec` |
| D9 | 渲染用例迁到 `render/` | **放弃**（2026-11-18） | — |

另做了计划外的两项：被吞错误总检（`ScriptStateRefreshTripwire_Spec`：130 个 vs 走真实刷新，记录命令、用户消息和 WARN/ERROR 日志，白名单之外就失败）和刷新复位扫描（和 13.8 比，结果是阴性，不是普遍回归）。

手工用例（`docs/vsscript/VSScript-Manual-Case.xlsx`）56 个：42 个已自动化（含部分），3 个原来已被别的用例覆盖，3 个保留手工（上传、邮件定时任务、图片弹出位置），7 个价值低不做，1 个归 Java 单测。

`state/` 现为 14 个 spec、110 个用例（2026-11-23 新增 4 个 spec 共 9 个用例，见下），本机 0 失败（2026-11-20 删去与 Java 重复的 15 个往返、差分、全局函数、PDF 文本用例和两个早期探路类）。2026-11-23 新增：`ScriptStateExportMatrix_Spec`（失败脚本放在测试存储里每种组件类型上，导出 Excel / PDF；白名单只能缩小：SelectionTree 刷新 #82107、Tab / 内嵌 Viewsheet 无警告文字；SelectionList #82105 已修复，已移出）、`ScriptStateResetSweep_Spec`（alpha / tip 复位；白名单已清空（Image / GroupContainer 的 `imageAlpha`，#82098 已修复））、`ScriptStateScopeReads_Spec`（#81786 越界读 undefined、#81929 未设置参数读 null，及其记录在案的副作用 `'x' in parameter` 恒为 true）、`ScriptStateTriggerOrder_Spec`（D5）。

## 4. 成果和验收

**找到的问题：** 这个单子下共提 29 个（从 #81530 到 #81889、#81929、#81930，清单见 Redmine #81430 的评论）。大多数来自和 13.8 的差分和端口缺口扫描；D1、PNG 转换、手工用例自动化这三条线没有发现产品缺陷（价值是回归保护）。修复 PR 都带了 Java 测试，回归测试放在 Java。

**验收标准（旧版 §9）：**

| 标准 | 状态 |
|---|---|
| `state/` 用例在本机和 CI 结果一致 | **未验证**：CI 还没有跑到测试（“Ensure DB server” 一步 gcloud 权限报错），Linux 上的 PDF 和像素用例不知道 |
| 每个新用例“故意改坏产品后变红” | **基本满足**，例外：#81534 的用例、`Submit_without_values_changes_nothing`、Freehand 的导出页面、PDF 值一致；方法是编译改过的类放进 `vsscript/target/test-classes`（旧版 §5.9） |
| D1 产出异常数量和误报清单 | 完成 |
| 脚本报错、点击 warning 至少各有一个稳定用例 | 完成 |
| 旧 PNG 用例有处置记录 | 完成：见 §4a（全部保留，没有删除；比对清单的 G1 问题已修） |

**待决定：** state/ 是否收紧 CI 的 `continue-on-error`（要等 CI 先跑到）；报告步骤是否保留 `fail-on-error: false`。

## 4a. 旧 PNG 用例的处置（`cases/`，D4、D9 放弃，2026-11-20）

**处置：全部保留，没有删除、没有改动用例本身**；它们继续做渲染回归（像素），脚本效果的状态断言见 §3。本机 114/115 失败（渲染环境），以 CI 基线为准，CI 还没跑到。每个 spec 的用例数（共 115）：

| spec | 用例 | spec | 用例 | spec | 用例 |
|---|---|---|---|---|---|
| Bugs_Spec | 5 | Form_Spec | 5 | Selection_Spec | 17 |
| ChartBindingInfo_Spec | 13 | Others_Spec | 6 | Table_Spec | 13 |
| ChartProperties_Spec | 16 | OutPut_Spec | 3 | Viewsheet_Spec | 3 |
| Egraph_Spec | 28 | ScriptEngine_Spec | 6 | | |

全部 130 个 vs（包括这些用例用到的）还各被刷新总检和导出总检覆盖。

**比对清单的修正（G1）：** `ChartProperties_Spec` 的 LengendsProperty 用例把 `LengendsProperty1` 写了两遍，`LengendsProperty2` 导出了、基线也在，却从不被比，已改成比 `LengendsProperty2`；`Viewsheet_Spec` 三个用例用无参 `compareImage()`（比“实际导出了什么”，某张丢失不会被发现），已改成点名。本机无法核对基线，第一次 CI 跑到时 `LengendsProperty2` 如果变红，先怀疑基线。

**没有任何用例引用的基线（8 张，2026-11-20 已删除，评审建议；可由 git 恢复）**，是用例改名或删除后留下的：
`cases/Chart/ChartBindingInfo_Spec/TestCase-BandingField_Chartmap.png`、`cases/Chart/ChartProperties_Spec/TestCase-Chart_barCornerRadius_barCornerRadius1/2/3.png`、`…/TestCase-FillBlank_fillTimeGap.png`（现用的是 `TestCase-Chart_FillBlank_fillTimeGap`）、`cases/Form_Spec/TestCase_RCB_binding_script1.png`、`cases/Others_Spec/TestCase-Tab_bottomTabs.png`、`cases/Viewsheet_Spec/TestCase-VS_embedded_VS.png`。基线现为 238 张，每张都被比对清单点名。

## 5. TODO：需要新资产的内容（暂不做）

| 要补的资产 | 为什么需要 | 对应 |
|---|---|---|
| 嵌套两层的 embedded 仪表板（Inner 里有带 `onClick` 的 Text） | 现有资产没有嵌入 viewsheet | #79699；D3 嵌套 `onClick` |
| 带两个同时命中的高亮的输出组件 | `highlighted[name]` 多个高亮同时命中 | #80875 |
| 真实流程回归资产：`setAction`、RadioButton 切换后 `selectedObject` 刷新、脚本绑定 shape 图片、脚本设 `SelectionTree.expandAll` | 原单附件没有 | #72673、#72722、#74224、#74225、#72666 |
| 脚本库函数进测试存储（`createBulletGraph` 等） | 现在这些函数是 `undefined` | #76850、#76851 |
| 含更多事件脚本的资产（现在 16 个 ONINIT、18 个 ONLOAD；ONREFRESH 就是 onLoad，不是另一种脚本） | D3 其余 | 仅事件脚本需要；D5 已不需要 |
| 可选，改现有资产：`Relation` 的 onInit 改 onLoad、`scope3` 的列绑定、`Selection_Calendar` 的列 | 总检白名单里的已知警告；改资产会动 PNG 基线，先评估 | #81633 |

（带分组展开的 Freehand 公式格不需要新资产：现有的 scope2 在内存里改成公式格即可，已用于差分。）

## 6. 已知限制和风险

- 直接执行脚本与刷新时执行的上下文不完全相同（如 `onClick` 会设置 `setUseDValue`）；读写入口不对称（`bindingInfo` 只有 setter，读用 `Chart1.colorField`）。
- 本机 PNG 用例 114/115 失败（渲染环境与 CI 基线不一致），以 CI 基线为准；CI 还没跑到。
- `WriteBack` 会写测试 home 的存储，行会在本地累积；CI 每次干净环境，本地删目录即可重来。
- 刷新路径吞掉脚本异常是否有意为之、`pop*` / `applyChanges` / `addAction` 的预期行为：待开发确认。
- Excel 用 `support/XlsxText` 直接读 xlsx（vsscript 测试类路径上没有 POI）；Crosstab、日期格、Chart 的导出内容未比较。
- 锁：这轮工作没有碰到锁问题；按领导的规则，碰到才记录到 `test/cluster/suites/lock`。
