# vspara 测试缺口与补充方案

> 模块：`vsothers`，profile `-Pvspara`，spec `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec`；产品源码 `main`（2.1.0-SNAPSHOT）。最后更新 2026-11-10。
> 依据（覆盖分析、源码确认的行为、相关历史 bug）在 `vspara-evidence.md`，下文以 A1 到 A9 引用。

**状态：** 产品仓库 Java 单测已提 PR #7974；单测发现的疑点汇总在 Redmine #80963，等开发评估；浏览器验证（第 3 节）服务未启动，尚未执行。

## 1. 现状

### 1.1 缺陷与疑点

最可能是真缺陷的是 **1、2、3、7**，其余是"待决定"（行为不一定错，要开发给出约定）。标了测试的，开发修复后要同步改该测试。

| # | 疑点 | 可信度 | 测试 | Redmine |
|---|---|---|---|---|
| 1 | 文本参数的值含 `{parameter.<自身>}` 时 `ParameterTool` 不会结束（探针：3 秒后线程仍在转），值来自客户端，可占满 CPU | 疑似缺陷，有安全影响 | 无（探针会留下空转线程） | #80963 项 1 |
| 2 | 多值参数含 null 元素时 `ParameterTool` 抛空指针 | 疑似缺陷 | `ParameterToolTest.Suspect` | 项 2 |
| 3 | 文本里 `{parameter.` 之前出现 `}`，所有参数都不被替换 | 疑似缺陷 | `ParameterToolTest.Suspect` | 项 3 |
| 4 | 空数组经 `VariableTable.put` 存成 null，条件被忽略 | 待决定：空多选是否就是"显示全部" | `ConditionVariableValueTest` | 项 4 |
| 5 | 日历 / 范围滑块参数传空串，结果是 `""` 不是 null | 待决定 | `ParameterServiceTest.SelectionParameters` | 项 5 |
| 6 | `Condition.evaluate` 原地改写哨兵值 | 多半是设计 | `ConditionVariableValueTest.SentinelValues` | 末尾说明 |
| 7 | `DynamicValue.fullEquals` 比较 `dval2.rvalue` 与自身，与 `fullHashCode` 不一致 | 疑似缺陷（笔误） | `DynamicValueTest.Suspect` | 备注项 6 |
| 8 | 内存里 `NULL` 运算符把空串也当 null，SQL 的 `IS NULL` 不会 | 待决定 | `ConditionListHandlerSentinelTest` | 备注项 7 |
| 9 | TOP_N / BOTTOM_N 的 N 变量缺失、非数字、空数组时保持 `$(n)` 引用不被忽略；`Long` 超过 int 范围溢出成负数 | 待确认（下游未核对） | `RankingConditionVariableTest` | 备注项 8 |
| 10 | `Para_basequerysql`：VPM 数据源上的手写 SQL，不读受保护表也被拒绝（详情 A9） | 待确认（问开发 Q1） | 用例已 `@Ignore` | 未提 |
| 11 | 表达式默认值 `3000000000` 的 Long 变量，提示框显示 `3.0E9` 并报错 | 缺陷 | 无 | #80154 |
| 12 | `{parameter.p, duration, ...}` 文档列出但未实现 | 文档还是实现缺陷未定 | `ParameterToolTest.Formats` | #79691 |

### 1.2 已做

- **验证机制严格化**（导出失败即失败、基线必须全部被导出），datatest PR #203、#206；7 个陈旧基线已删。
- **`VarExp_Binding_Spec` 22 个用例**加了数据比对，89 个 `.txt` 基线已提交。
- **Java 单测 9 个类**，community PR #7974：`ConditionVariableValueTest`、`ConditionVariableOperatorTest`、`RankingConditionVariableTest`、`ConditionListHandlerSentinelTest`、`ParameterServiceTest`、`ParameterToolTest`、`VariableTableJsonTypedValueTest`、`XUtilReplaceVariableValueTest`、`DynamicValueTest`。前 7 个类一起运行 162 个用例通过，后 2 个单独通过。设计清单与产品仓库已有的测试见 A3。

### 1.3 待做

| # | 做什么 | 前提 / 完成标准 |
|---|---|---|
| 1 | 浏览器验证第 3 节 V1 到 V3，再做 V4 到 V11 | 服务启动且 jar 含 `main`；V1 用一次性实例。复现不了的回到单测核对，不要直接改 bug |
| 2 | 开发评估 #80963，修复后同步改 1.1 里对应的测试 | 开发回复 |
| 3 | 问开发 Q1（`Para_basequerysql`）、Q3 的 SQL 路径、Q4（`prompt` 两层优先级），见 A6 | 得到答案后去掉 `@Ignore`、更新基线 |
| 4 | 在 GitHub 上生成 15 个 Chart 用例的 `ChartN.png` 基线 | `VarExp_Binding_Spec` 的 Chart 用例通过 |
| 5 | datatest 新增 dashboard 用例：带 `.__type__` 的参数（G3）、缺失变量类型的 ws + vs（G4）、孤儿 `Para_source`（G7），见 A4 | 先确认浏览器发送的 type 取值 |
| 6 | PR #7974 的 CI 和评审 | 合并 |
| 7 | 决定 `Hyperlink_Dynimac` 是否纳入 vspara，并解析 `docs/vs/VSParaDynimac/` 里已有的缺口记录 | 用户决定 |

不在本轮：`VarExp_Properties_Spec` 的 10 个用例（动态值作用在属性上，`executeVS` 导出不到）；没有写单测的三项（`$(组件)` 解析、`prompt=false`、#80154 提示框格式）因为需要运行时环境，对应 V9、V11。

## 2. 缺口台账

| 缺口 | 说明 | 状态 | 对应 |
|---|---|---|---|
| G1 | 验证机制静默通过 | 已处理 | PR #203、#206；遗留见 A9 |
| G2 | VarExp 32 个用例只比整页 PNG | Binding 已处理；Properties 不在本轮 | 22 个用例 |
| G3 | 没有用例发送 `.__type__` | 单测已做；dashboard 用例待做 | `ParameterServiceTest`；V10 |
| G4 | 变量组件类型面窄（无 double、float、date、datetime、char） | 转换单测已做；dashboard 用例待做 | `ParameterServiceTest` |
| G5 | 文本参数格式 | 单测已做 | `ParameterToolTest` |
| G6 | 条件变量：数组、空数组、含 null、各运算符 | 单测已做 | `Condition*Test`、`RankingConditionVariableTest`；疑点 4、9 |
| G7 | 孤儿 vs `Para_source` | 待做 | 1.3 第 5 项 |
| G8 | `prompt=false` 路径 | 待开发确认（Q4） | V9 |
| G9 | 哨兵参数 `NULL_VALUE` 等 | 内存和 SQL 路径单测已做 | `ConditionVariableValueTest`、`ConditionListHandlerSentinelTest`；疑点 6、8 |
| G10 | 变量默认值的类型边界、JSON 往返 | 单测已做，其余产品仓库已有 | `VariableTableJsonTypedValueTest`；疑点 11 |
| G11 | MV × 变量条件（#79482、#79417、#80152、#80155） | 不属于 vspara，转交 MV 专项 | 无 |
| G12 | 文本参数 `duration` 格式 | 单测固定现状 | 疑点 12 |

## 3. 待验证清单（浏览器，服务启动后执行）

bug 项指 #80963 里的编号。结果列留空。

| # | 对应 | 步骤 | 修复后预期 | 当前（单测固定的现状） | 结果 |
|---|---|---|---|---|---|
| V1 | 项 1 | Text 内容 `{parameter.p}`，用 `?p=%7Bparameter.p%7D` 打开 | 显示字面 `{parameter.p}` | 请求不返回，一个线程占 CPU | |
| V2 | 项 2 | Selection List 绑定含 NULL 的列，多选 NULL 和另一项；Hyperlink 勾选"发送选择作为参数"，目标页放 Text `{parameter.列名}` | 显示 `[a,null]` 或跳过 null | 目标页报错（空指针） | |
| V3 | 项 3 | Text 内容 `{0} {parameter.name}`，用 `?name=abc` 打开 | 显示 `{0} abc` | 参数没替换 | |
| V4 | 项 4、G6 | ws 多选变量 `v`（条件：列 属于 `$(v)`），打开参数对话框不选任何值 | 与"空选"的约定一致（待决定） | 条件被忽略，显示全部行 | |
| V5 | 项 5 | Calendar 的 `_dateStart` 参数用链接传空值 | 待决定：null 还是空串 | 空串 | |
| V6 | 项 8 | 数据库一列同时有 NULL 和 `''`；ws 条件 `列 = $(p)`，`?p=NULL_VALUE`；比较 SQL 下推与内存过滤（Mirror 或 MV 之后）两种配置 | 行数一致，或开发说明为何不同 | SQL 只含 NULL 行，内存还含 `''` 行 | |
| V7 | 项 9 | ws 的 TopN，N 为 `$(n)`，不传 `n` 且 `prompt=false` 打开 | 与"变量缺失"的约定一致（待决定） | N 保持未解析引用，下游未知 | |
| V8 | #80154 | ws `zz79607/expr`（#80154 附件）：Long 变量 `v`，默认值表达式 `3000000000`，在 Composer 打开 | 显示 `3000000000`，无错误 | 显示 `3.0E9` 并提示 "Please enter an integer" | |
| V9 | G8、Q4 | ws 变量 `prompt=false` 且有默认值，vs 不设 `disableParameterSheet` 打开 | 不弹对话框，用默认值 | 两层同时设置时的优先级待开发确认 | |
| V10 | G3 | `Para_wscondition` 的 vs，URL 带 `x=2024-03-15&x.__type__=date`（`x` 换成该 ws 的日期变量名） | 行数与 `?x=2024-03-15` 一致 | 单测：得到 `java.sql.Date` | |
| V11 | A7-C | 跑现有 `VarExp_Binding_Spec`（`-Pvspara`），确认 `$(组件)` 取值与书签状态一致 | 全部通过 | 未重新运行 | |

V1、V2、V3 最值得先验。

## 4. 遗留：`Para_basequerysql`

严格基类下失败，是产品行为变化，不是 spec 写错：该 ws 在有 VPM 的数据源上用手写 SQL（`parseResult=3`），不读受保护表也被拒绝；不传 `Ppid` 报 `Variable in VPM condition missing`，传了报 `Query rejected by VPM`。用例已 `@Ignore`，旧基线不改。要问开发的问题和已试过的做法见 A9。
