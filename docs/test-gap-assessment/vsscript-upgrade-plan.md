# vsscript 测试升级方案（现状版）

> 更新：2026-11-20　模块：`vsscript`　Redmine：Test Case #81430
> 本页只放：目标和放置规则、方向的完成情况、验收、TODO。原始方案、验证数据、变异清单、JaCoCo 结果在 `vsscript-upgrade-notes.md`（归档，本页的 §x 指向它的同号章节）。
> 怎么跑、代码结构、写用例的配方：`vsscript-state-guide.md`。手工用例自动化：`vsscript-manual-automation.md`。旧 PNG 用例的处置：`vsscript-png-disposition.md`。

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
| D1 | 脚本健康检查（130 个 vs、213 段脚本直接执行） | **完成**：0 个真实脚本错误，3 个误报 | 探针 `ScriptStateHealthProbe`；结果 notes §5.1 |
| D2 | 异常契约（刷新吞异常 / 直接执行抛 / 点击抛） | **等开发**确认语义；#81796 用例先把现状固定住 | `ScriptStateExportTripwire_Spec`、`ScriptStateKnownBugs_Spec` |
| D3 | 事件端到端（Submit、confirm、parameter、点击、输入、选择、flyover） | **完成**；嵌套 vs 的 `onClick` 需新资产 | `ScriptStateSubmit_Spec`、`ScriptStateManual_Spec` |
| D4 | 旧 PNG 用例改状态断言 | **放弃**（收益低，2026-11-18） | — |
| D5 | 触发顺序（ONINIT / ONLOAD / ONREFRESH） | 未做：现有资产没有 ONREFRESH，需新资产 | §5 TODO |
| D6 | 差分 | **完成**，以“和 13.8 对比”的形式（找 bug 最多的方法） | 本地 `diff13/`（含机器路径，不提交） |
| D7 | 导出一致性 | **完成**：导出不报错总检；脚本值进 Excel / PDF；8 个 Table / Freehand 的整行与状态一致 | `ScriptStateExportTripwire_Spec`、`ScriptStateExportContent_Spec`；notes §5.11、§5.12 |
| D8 | 数据函数端到端（写回） | **完成**：`appendRow`、`setCellValue`、`applyChanges`、`saveWorksheet` | `ScriptStateWriteBack_Spec` |
| D9 | 渲染用例迁到 `render/` | **放弃**（2026-11-18） | — |

另做了计划外的两项：被吞错误总检（`ScriptStateRefreshTripwire_Spec`：130 个 vs 走真实刷新，记录命令、用户消息和 WARN/ERROR 日志，白名单之外就失败）和刷新复位扫描（和 13.8 比，结果是阴性，不是普遍回归）。

手工用例（`docs/vsscript/VSScript-Manual-Case.xlsx`）56 个：42 个已自动化（含部分），3 个原来已被别的用例覆盖，3 个保留手工（上传、邮件定时任务、图片弹出位置），7 个价值低不做，1 个归 Java 单测。

`state/` 现为 10 个 spec、101 个用例，本机 0 失败（2026-11-20 删去与 Java 重复的 15 个往返、差分、全局函数、PDF 文本用例和两个早期探路类）。

## 4. 成果和验收

**找到的问题：** 这个单子下共提 29 个（从 #81530 到 #81889、#81929、#81930，清单见 Redmine #81430 的评论）。大多数来自和 13.8 的差分和端口缺口扫描；D1、PNG 转换、手工用例自动化这三条线没有发现产品缺陷（价值是回归保护）。修复 PR 都带了 Java 测试，回归测试放在 Java。

**验收标准（notes §9）：**

| 标准 | 状态 |
|---|---|
| `state/` 用例在本机和 CI 结果一致 | **未验证**：CI 还没有跑到测试（“Ensure DB server” 一步 gcloud 权限报错），Linux 上的 PDF 和像素用例不知道 |
| 每个新用例“故意改坏产品后变红” | **基本满足**，例外：#81534 的用例、`Submit_without_values_changes_nothing`、Freehand 的导出页面、PDF 值一致；方法是编译改过的类放进 `vsscript/target/test-classes`（notes §5.9） |
| D1 产出异常数量和误报清单 | 完成 |
| 脚本报错、点击 warning 至少各有一个稳定用例 | 完成 |
| 旧 PNG 用例有逐个处置记录 | 完成：`vsscript-png-disposition.md`（全部保留，没有删除；比对清单的 G1 问题已修） |

**待决定：** state/ 是否收紧 CI 的 `continue-on-error`（要等 CI 先跑到）；报告步骤是否保留 `fail-on-error: false`。

## 5. TODO：需要新资产的内容（暂不做）

| 要补的资产 | 为什么需要 | 对应 |
|---|---|---|
| 嵌套两层的 embedded 仪表板（Inner 里有带 `onClick` 的 Text） | 现有资产没有嵌入 viewsheet | #79699；D3 嵌套 `onClick` |
| 带两个同时命中的高亮的输出组件 | `highlighted[name]` 多个高亮同时命中 | #80875 |
| 真实流程回归资产：`setAction`、RadioButton 切换后 `selectedObject` 刷新、脚本绑定 shape 图片、脚本设 `SelectionTree.expandAll` | 原单附件没有 | #72673、#72722、#74224、#74225、#72666 |
| 脚本库函数进测试存储（`createBulletGraph` 等） | 现在这些函数是 `undefined` | #76850、#76851 |
| 含更多 ONINIT / ONLOAD / 事件脚本的资产（现在 16 个 ONINIT、18 个 ONLOAD、0 个 ONREFRESH） | D5、D3 其余 | D5 |
| 可选，改现有资产：`Relation` 的 onInit 改 onLoad、`scope3` 的列绑定、`Selection_Calendar` 的列 | 总检白名单里的已知警告；改资产会动 PNG 基线，先评估 | #81633 |

（带分组展开的 Freehand 公式格不需要新资产：现有的 scope2 在内存里改成公式格即可，已用于差分。）

## 6. 已知限制和风险

- 直接执行脚本与刷新时执行的上下文不完全相同（如 `onClick` 会设置 `setUseDValue`）；读写入口不对称（`bindingInfo` 只有 setter，读用 `Chart1.colorField`）。
- 本机 PNG 用例 114/115 失败（渲染环境与 CI 基线不一致），以 CI 基线为准；CI 还没跑到。
- `WriteBack` 会写测试 home 的存储，行会在本地累积；CI 每次干净环境，本地删目录即可重来。
- 刷新路径吞掉脚本异常是否有意为之、`pop*` / `applyChanges` / `addAction` 的预期行为：待开发确认。
- Excel 用 `support/XlsxText` 直接读 xlsx（vsscript 测试类路径上没有 POI）；Crosstab、日期格、Chart 的导出内容未比较。
- 锁：这轮工作没有碰到锁问题；按领导的规则，碰到才记录到 `test/cluster/suites/lock`。
