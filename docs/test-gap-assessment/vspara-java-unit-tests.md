# vspara 产品仓库 Java 单测结果

> 来源：stylebi-main 单测流程产出（`unit-test-workflow/vspara/`：`report.md`、`decisions.md`、`matrix.md`、`mutations.md`）。产品 PR：community #8021，分支 `test-vspara-parameter-hardening`，只改测试代码；加固的是 PR #7974 的 9 个类。
> 数字和判断取自上述报告，未重跑。缺口全貌见 [vspara.md](vspara.md)。

## 1. 结论

- **14 个类、425 个用例，0 失败，26 个 `@Disabled("mismatch …")`**（就是已判定的"不一致"）。单独运行、`alphabetical`、`reversealphabetical`、四个环境（UTC en-US、LA en-GB、Kolkata de-DE、Auckland tr-TR）都通过。
- 基线：#7974 的 11 个类（216 个用例）在 `main` 上全绿。
- 变异检查（独立 worktree，49 个加补充）：所有 pin 行和新增行的变异都被抓住。
- 开出 **14 个 Redmine bug**（#81106–#81119，全部在 stylebi-2.1.0），其中 8 个是有独立依据的缺陷（C 类），6 个等开发给约定（B 类）。
- 疑点 1、2、3、7 已在 `main` 修复（#7981），原来的 `Suspect` 测试已删（#7996），回归测试是 `ParameterToolParsingTest`、`DynamicValueFullEqualsTest`。

## 2. 单测落在哪里

| 测试类 | 对应 |
|---|---|
| `ConditionVariableValueTest` | `Condition` 变量解析、哨兵（内存路径） |
| `ConditionVariableOperatorTest` | STARTING_WITH / CONTAINS / LIKE / DATE_IN 用变量、变量缺失、类型化条件 |
| `RankingConditionVariableTest` | TOP_N / BOTTOM_N 的 N 取自变量 |
| `ConditionListHandlerSentinelTest` | `createCondNode` 哨兵，Derby 内存库上与内存结果对比；LIKE 正则元字符 |
| `ParameterServiceTest` | `readParameters`：`.__type__` 转换、选择类参数、上下文变量 |
| `ParameterToolTest`、`ParameterToolParsingTest` | 文本参数 `{parameter.x}` 的格式与解析 |
| `VariableTableJsonTypedValueTest` | `VariableTable` JSON 往返 |
| `XUtilReplaceVariableValueTest` | `XUtil.replaceVariable` |
| `DynamicValueTest`、`DynamicValueFullEqualsTest` | `DynamicValue` |

没有写的：`$(组件)` 解析（需要运行时 viewsheet）、`prompt=false`（需要集成环境，且两层优先级待开发确认）、#80154 的提示框格式（不在 `UserVariable` 里）。产品仓库已有的不重写：`UserVariable` 的 XML/JSON 默认值往返、Long 默认值超 int、`DynamicValue2` 数字解析。

## 3. 开出的 Redmine bug

类 C = 有独立依据的缺陷；类 B = 等开发给约定。"修复"列是 `origin/main` 上能找到的提交，没找到的写"未找到"。

| bug | 类 | 问题 | 测试行 | 修复 |
|---|---|---|---|---|
| #81106 | C | TopN 的 N：Long 超 int 回绕成负数（不变量：N 不得为负） | R01、R02 | 4c52e5e17b（#8022），N 夹到 [0, Integer.MAX_VALUE] |
| #81107 | C | `Tool.getData`：byte/short/integer 超范围回绕（"200"→-56、"40000"→-25536、"3000000000"→2147483647） | S11、D06 | 未找到 |
| #81108 | C | `DynamicValue` 的 `fullHashCode` / `fullEquals` 不一致；Long 2^53+1 被判相等 | D01、D02 | 285fe2182a（#8023） |
| #81109 | C | 文本参数变量名里的 `parameter.` 被全部删除 | T01 | 09e4684183（#8024），只去开头的 |
| #81110 | C | `main` 缺 13.8 的 #79688 修复（`executeDynamicValue` 的前后缀处理） | D13、D13b | a8298dda8c（#8025） |
| #81111 | C | `readParameters` 参数值为 null 时空指针 | S03、S03b | 7202ec1f3f（#8026），null 数组当空数组 |
| #81112 | C | tr-TR 下 CONTAINS / STARTING_WITH 忽略大小写失效（`toLowerCase()` 无 Locale） | C17 | 2faf6f245f（#8027） |
| #81113 | C | 文本参数的 Long / BigDecimal 先转 double 丢精度 | T03 | 46c7bf713e（#8028） |
| #81114 | B | LIKE 通配符：内存 `%` `?`，SQL `%` `_` | C01、C02 | 439f39e174（#8029）：内存改用 SQL 通配符（`_` 单字符、`?` 字面），SQL 路径不改；依据 #48160 |
| #81115 | B | 缺失变量：内存丢条件，SQL 路径保留 `$(v)` 返回 0 行（真实 WS 能否复现未确认） | C06、C07 | 未找到 |
| #81116 | B | 取反的 EQUAL_TO 对 NULL 行：内存两值逻辑，SQL 三值逻辑 | C10 | 未找到 |
| #81117 | B | Date 标量与数组渲染不同，`Date[]` 忽略已登记的格式 | X01、X09 | 未找到 |
| #81118 | B | Long 经 double 比较 | C18 | 未找到 |
| #81119 | B | `DynamicValue` 双重检查锁竞态 | D05 | 按 Ignored 关闭 |

其他：V01 对应已有 [#80154](https://issues.inetsoft.com/issues/80154)（测试 `@Disabled("#80154")`）；C09 已在 #80963 第 7 项；C11 不开 bug（#80963 注明"可能设计如此"）。

**要注意：** #81114 的修复改变了内存 LIKE 的结果（`_` 变通配符、`?` 变字面），存量条件可能静默变化，开发 PR 没写兼容说明，需要向开发确认。#81106–#81118 里已修的，对应 `@Disabled("mismatch <行 id>")` 要去掉，并把标签里的行编号换成 bug 号。

## 4. 不一致行的判定

| 类 | 行 | 要点 |
|---|---|---|
| C 缺陷 | R01/R02/D06 | Long 3000000000 作 TopN 的 N 回绕成 -1294967296 |
| | S11 | byte/short/integer 经 double 强转回绕（`CoreTool.java:1419-1449`），推翻了"数字解析失败返回 null" |
| | D01/D02/C18 | `fullHashCode` 与 `fullEquals` 契约不一致 |
| | T01 | `ParameterTool.java:183` 删掉变量名里所有 `parameter.` |
| | D13/D13b | #79688 的修复未带到 `main`（`ViewsheetSandbox.java:7131-7164`） |
| | S03/S03b | 客户端 `"p": null` 使 `ParameterService.java:76` 空指针（客户端 JSON 可达） |
| | C06/C07 | SQL 路径保留 `$(v)`，内存路径剔除；SQL 生成里没有 `isIgnored()` 的消费者 |
| | C17、T03、D05 | tr-TR 的 `toLowerCase`；Long/BigDecimal 先转 double；`DynamicValue` 的 DCL 竞态（低优先级） |
| B 待决定 | C01/C02/C09/C10/C11/X01/X09 | LIKE 方言、内存 NULL 把空串也当 null、取反的 NULL、原地改写数组、Date 渲染 |
| D 夹具 / A 设计（已移除或改写） | S01、C05、C13、C15、J04、J05 | S01：`FormatCache` 类加载时固定时区，夹具问题，改为 S01b；C05：SQL LIKE 大小写由数据库排序规则决定，不能作 Derby 对照；C13：生产不可达；J04/J05：设计如此（上下文变量由调用方过滤） |

LIKE 方言没有预设期望（差分行，不一致归 B）；TopN 的 N 只断言不变量"N 不得为负"（夹到 0 或保持引用都算通过）。

## 5. 已确认的行为规则（单测期望值的依据）

读源码得到的规则，写测试时按它断言：

**A. `.__type__` 与类型转换**（`ParameterService.readParameters`、`CoreTool.getData(type, val, strictNull)`）
- 以 `.__type__` 结尾的参数和上下文变量（`_USER_`、`_ROLES_`、`_GROUPS_`）不进变量表。
- 有 `x.__type__` 时对每个值调用 `getData(type[0], v, true)`；结果长度为 1 折叠成标量，否则保持数组。类型名只按"首字母 + 长度"识别。前端只在值为字符串 `"null"` 时才发 `.__type__`（`show-hyperlink.service.ts:119`），所以真实场景是"null 加类型"。
- `strictNull=true`：`"__null__"` 或 null 得 null，字符串 `"null"` 不是 null。
- 转换失败：数字类型失败得 null；其他类型回退原字符串；`boolean` 解析不了得 false；`long` 整数解析失败得 `Double`。（byte/short/integer 超范围回绕，见 #81107。）
- 存入 `VariableTable.put` 后，没有非 null 元素的数组（空数组、`{null}`）存成 null。日历、范围滑块参数（`_dateStart`、`_dateEnd`、`_start`、`_end`）空串回退为空串。

**B. 条件里的变量**（`Condition.replaceVariable` / `evaluate`）
- 变量缺失或为 null → 条件 `ignored`，恒为 true（不过滤）。缺失时 EQUAL_TO、LESS_THAN、GREATER_THAN、STARTING_WITH、CONTAINS、LIKE、ONE_OF、DATE_IN 都被忽略。
- 经 `put` 存入的空数组、`{null}` 同样被存成 null，被忽略（对所有运算符），与前端"空选显示全部"一致；只有绕过 `put` 时空数组才匹配不到任何行。
- 变量只在整个值恰好是 `$(name)` 时才替换；类型化条件要用 `UserVariable` 对象或 `new Condition(false, false)`，否则 `"$(v)"` 被读成 0。
- TOP_N / BOTTOM_N：N 取自变量，数字取 `intValue`，文本按整数解析，多值取第一个；变量缺失、非数字、空数组时 N 保持未解析引用（条件不被忽略；下游：3 处告警跳过，`AssetQuery.java:3255` 抛 `ClassCastException`）。
- 哨兵 `NULL_VALUE`、`EMPTY_STRING`、`NULL_STRING`（内存与 SQL 路径规则相同）：`NULL_VALUE` 改运算符为 NULL，`EMPTY_STRING` 改为 `""`，`NULL_STRING` 改为 `"null"`；多元素数组里的哨兵不改写，只记警告；`evaluate` 会修改条件本身。

**C. `$(组件)`**（`ViewsheetSandbox.executeVariableValue`）
- 整体形如 `$(name)` → 取 VS 组件的值（选择树取 `autodrill`；复合输入组件 0 个得 null、1 个得该元素、多个得数组）；以 `=` 开头是脚本；嵌在文本里的走 `matchVariableFormula`。
- `$(x)` 是 **VS 输入组件**，不是 WS 变量；WS 变量用 `=parameter.x`。

**D. 文本参数**（`ParameterTool`）
- 格式：`{parameter.var}`、`number`、`text`、`date`、`percent`、`currency`；`containsParameter` 要求含 `{parameter.` 和 `}`，且 `}` 位置大于起点 + 10。
- 参数缺失或 null → 输出字符串 `"null"`。数字解析失败原样返回；`date` 只在值是 `Date` 时格式化；数组输出 `[v1,v2]`，逐个格式化；格式串出错时忽略格式；未知类型（如 `duration`）原样输出。

**E. `XUtil.replaceVariable`**
- 只认 `$(name)`，不认 `${name}`；变量缺失或 null 时占位符保留；数组每个元素单独编码后用 `,` 连接，空数组保留占位符；带日期格式的 `Date` 标量按格式输出（1901 年以前专门处理）。

**F. `prompt`**
- WS 变量与 VS 输入组件同名时 VS 组件覆盖（不弹窗）。`prompt=false` 有两层：WS 变量的 `UserVariable.isPrompt()`（`XEngine.java:1741`）和 VS 的 `disableParameterSheet`（`ViewsheetInfo`）；资产/VS 路径里没有竞争层，`UserVariable.isPrompt` 是旧查询的标志。

## 6. 相关历史 bug 簇

| 簇 | bug | 对应单测 |
|---|---|---|
| 哨兵参数 | #77619、#77571、#77709、#77706、#77707、#77738、#77752、#77620、#77606、#77574、#77708、#78386（已关）；约束见 #78139 section E（混合 IN 列表保持原样并告警） | SQL 改写产品已有 19 个 `XUtilSentinel*` / `VpmConditionSentinel*` / `SQLBoundQuery*Sentinel*` 测试类；内存路径和 `createCondNode` 是新增 |
| Long / 默认值 | #79607、#77903（已关）；#80154、#80155（未修） | #80154 期望值写成显示 `3000000000`，`@Disabled` 注明 bug 号 |
| `VariableTable` 状态 | #77528、#77472 | 往返：元素类型、Time/Timestamp 精度 |
| 文本参数 | #79691（`duration` 文档列出但未实现） | `ParameterToolTest` 固定现状 |
| 值替换 | #79742、#79841、#77450、#78730 | #78730 在 UTC 以西触发，测试显式设置 `TimeZone` 并恢复 |
| 上下文变量 | #77329 | 逐个断言不进变量表 |
| 多选 / 动态值 | #79636、#72707、#79688、#76778、#79945 | `DynamicValue` |
| MV × 变量条件 | #79482、#79417、#80152、#80155 | 不在 vspara（G11） |

## 7. 钉住现状与待开发回答的问题

按当前行为写成启用的测试（标 pending）：

- 疑点 4：空数组经 `put` 存成 null、条件被忽略（代码注释只写了"数组全是 null"，空数组是副作用）；开发在 #80963 里对 4、5、7、8 保留为产品决定。
- 疑点 5：日历 / 范围滑块传空串，结果是 `""` 而不是 null。
- 疑点 8：内存 `NULL` 运算符把空串也当 null，SQL `IS NULL` 不会（#78139 section E 讲的是另一种情形，不能作依据）。
- 疑点 9 的"N 缺失时是否忽略排名"（#80963 第 8 项）；N 回绕与三处下游不一致按缺陷断言。
- 疑点 12：`duration` 格式按现状。

仍悬着：SQL 路径对空数组 / 缺失变量是否忽略（C06/C07，SQL 生成不看 `isIgnored()`；提示层是否总会先补值未确认）；`prompt` 两层同时设置时的优先级。

## 8. 没验证的

- 未被抓住的变异：C01、C02（LIKE 契约未定）；T01、S11、D02、C18、C17、D06（变异与 `main` 等价，或该行在 `main` 上本已失败）。
- 每个 `@Disabled` 测试的失败原因只由写作员运行时核对，评审没有逐个复核。
- C06/C07 在真实 WS 里能否复现（多选变量不选时查询返回 0 行）没有实机确认；harness 与真实管线一致（`PreAssetQuery.java:4470-4474`）。
- 没写：S08、R05、Q01、Q02、X04(b)（Timestamp 带时间格式，需要决定）。
- 原文档 A7-A "数字解析失败返回 null" 不成立（见 S11）；分析员早先的假设 D8、D04、J02(NaN) 被运行结果推翻。
- PR #8021 在草稿状态下测试任务没有运行（只有 Semgrep 和 CLA）；转 ready 或手动触发后才能确认 CI。`core/inetsoft.yaml` 是未跟踪文件，不得入库。
