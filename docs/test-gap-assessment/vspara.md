# vspara 测试缺口与补充方案

> 模块：`vsothers`，profile `-Pvspara`，spec `Para_VS_Spec`、`VarExp_Binding_Spec`、`VarExp_Properties_Spec`；产品源码 `main`（2.1.0-SNAPSHOT）。最后更新 2026-11-10。
> 依据（覆盖分析、源码确认的行为、相关历史 bug）在 `vspara-evidence.md`，下文以 A1 到 A9 引用。

**状态：** 产品仓库 Java 单测已提 PR #7974；单测发现的疑点汇总在 Redmine #80963，等开发评估；浏览器验证（第 3 节）已于 2026-11-11 执行，V6 除外，结果见该节。

## 1. 现状

### 1.1 缺陷与疑点

最可能是真缺陷的是 **1、2、3、7**，其余是"待决定"（行为不一定错，要开发给出约定）。标了测试的，开发修复后要同步改该测试。


| #   | 疑点                                                                             | 可信度                                                         | 测试                                          | Redmine    |
| --- | ------------------------------------------------------------------------------ | ----------------------------------------------------------- | ------------------------------------------- | ---------- |
| 1   | 文本参数的值含 `{parameter.<自身>}` 时 `ParameterTool` 不会结束（探针：3 秒后线程仍在转），值来自客户端，可占满 CPU | 疑似缺陷，有安全影响                                                  | 无（探针会留下空转线程）                                | #80963 项 1 |
| 2   | 多值参数含 null 元素时 `ParameterTool` 抛空指针                                            | 疑似缺陷                                                        | `ParameterToolTest.Suspect`                 | 项 2        |
| 3   | 文本里 `{parameter.` 之前出现 `}`，所有参数都不被替换                                           | 疑似缺陷                                                        | `ParameterToolTest.Suspect`                 | 项 3        |
| 4   | 空数组经 `VariableTable.put` 存成 null，条件被忽略                                         | 测试人员判断：显示全部（不填 = 没传 = 不过滤，V4 浏览器验证一致）；开发未正式确认 | `ConditionVariableValueTest`                | 项 4        |
| 5   | 日历 / 范围滑块参数传空串，结果是 `""` 不是 null                                                | 不作为缺陷：界面里没选日期不发参数，空串只出现在手写 URL，与普通参数一致（V5 已验证） | `ParameterServiceTest.SelectionParameters`  | 项 5        |
| 6   | `Condition.evaluate` 原地改写哨兵值                                                   | 多半是设计                                                       | `ConditionVariableValueTest.SentinelValues` | 末尾说明       |
| 7   | `DynamicValue.fullEquals` 比较 `dval2.rvalue` 与自身，与 `fullHashCode` 不一致           | 疑似缺陷（笔误）                                                    | `DynamicValueTest.Suspect`                  | 备注项 6      |
| 8   | 内存里 `NULL` 运算符把空串也当 null，SQL 的 `IS NULL` 不会                                    | 待决定                                                         | `ConditionListHandlerSentinelTest`          | 备注项 7      |
| 9   | TOP_N / BOTTOM_N 的 N 变量缺失、非数字、空数组时保持 `$(n)` 引用不被忽略；`Long` 超过 int 范围溢出成负数       | 待确认（下游未核对）                                                  | `RankingConditionVariableTest`              | 备注项 8      |
| 10  | `Para_basequerysql`：VPM 数据源上的手写 SQL，不读受保护表也被拒绝（详情 A9）                          | 已确认：设计如此（拒绝优先），不是缺陷；在 SQL 里加 `-- vpm.tables:` 注释即可通过，见第 4 节 | 已做（2026-11-12）：注释已加进 ws，`@Ignore` 已去掉，用旧基线通过                 | 不提单        |
| 11  | 表达式默认值 `3000000000` 的 Long 变量，提示框显示 `3.0E9` 并报错                                | 缺陷                                                          | 无                                           | #80154     |
| 12  | `{parameter.p, duration, ...}` 文档列出但未实现                                        | 文档还是实现缺陷未定                                                  | `ParameterToolTest.Formats`                 | #79691     |


### 1.2 已做

- **验证机制严格化**（导出失败即失败、基线必须全部被导出），datatest PR #203、#206；7 个陈旧基线已删。
- `**VarExp_Binding_Spec` 22 个用例**加了数据比对，89 个 `.txt` 基线已提交。
- **Java 单测 9 个类**，community PR #7974：`ConditionVariableValueTest`、`ConditionVariableOperatorTest`、`RankingConditionVariableTest`、`ConditionListHandlerSentinelTest`、`ParameterServiceTest`、`ParameterToolTest`、`VariableTableJsonTypedValueTest`、`XUtilReplaceVariableValueTest`、`DynamicValueTest`。前 7 个类一起运行 162 个用例通过，后 2 个单独通过。设计清单与产品仓库已有的测试见 A3。

### 1.3 待做


| #   | 做什么                                                                                                 | 前提 / 完成标准                                                             |
| --- | --------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| 1   | 浏览器验证第 3 节 V1 到 V3，再做 V4 到 V11                                                                      | 服务启动且 jar 含 `main`；V1 用一次性实例。复现不了的回到单测核对，不要直接改 bug                    |
| 2   | 开发评估 #80963，修复后同步改 1.1 里对应的测试                                                                       | 开发回复                                                                  |
| 3   | Q1、Q4 已有答案（见第 4 节和 V9），不必再问开发；Q3 的 SQL 路径到不了（经 `VariableTable.put` 的空数组不会进入 `ConditionListHandler`） | Q1 已做（2026-11-12）：注释已加进 `ws_basequerySQL`，快照已更新，`@Ignore` 已去掉，基线未改，用例通过 |
| 4   | 在 GitHub 上生成 15 个 Chart 用例的 `ChartN.png` 基线                                                         | `VarExp_Binding_Spec` 的 Chart 用例通过                                    |
| 5   | datatest 新增 dashboard 用例：带 `.__type_`_ 的参数（G3）、缺失变量类型的 ws + vs（G4）、孤儿 `Para_source`（G7），见 A4        | 浏览器发送的 type 取值已确认：表格列绑定的参数发 JS 的 `typeof`（`string`/`number`/`boolean`，null 单元格是 `object`），写死的超链接参数发超链接里存的类型；且只在值为 null 时才发 `<名>.__type__`（`show-hyperlink.service.ts`、`hyperlink.service.ts`）。G3 已做两个，日期和布尔的行为已提 #81180（Dev feedback needed）；G4 已做（`Para_wsvartype`）；G7 不做 |
| 6   | PR #7974 的 CI 和评审                                                                                   | 合并                                                                    |
| 7   | 决定 `Hyperlink_Dynimac` 是否纳入 vspara，并解析 `docs/vs/VSParaDynimac/` 里已有的缺口记录                            | 用户决定                                                                  |


不在本轮：`VarExp_Properties_Spec` 的 10 个用例（动态值作用在属性上，`executeVS` 导出不到）；没有写单测的三项（`$(组件)` 解析、`prompt=false`、#80154 提示框格式）因为需要运行时环境，对应 V9、V11。

## 2. 缺口台账


| 缺口  | 说明                                          | 状态                                                       | 对应                                                                     |
| --- | ------------------------------------------- | -------------------------------------------------------- | ---------------------------------------------------------------------- |
| G1  | 验证机制静默通过                                    | 已处理                                                      | PR #203、#206；遗留见 A9                                                    |
| G2  | VarExp 32 个用例只比整页 PNG                       | Binding 已处理；Properties 不在本轮                              | 22 个用例                                                                 |
| G3  | 没有用例发送 `.__type__`                          | 单测已做；dashboard 用例已做两个（`Para_wscondition2_typedNullInteger`、`Para_dataview_typedNull`，2026-11-12）；日期、布尔变体不保留，见第 4 节和 #81180 | `ParameterServiceTest`；V10                                             |
| G4  | 变量组件类型面窄（无 double、float、date、datetime、char） | 转换单测已做；dashboard 用例已做（`Para_wsvartype`，2026-11-12，新建 ws `ws_vartype` 和 vs `Para_wsvartype`），见第 4 节 | `ParameterServiceTest`                                                 |
| G5  | 文本参数格式                                      | 单测已做                                                     | `ParameterToolTest`                                                    |
| G6  | 条件变量：数组、空数组、含 null、各运算符                     | 单测已做                                                     | `Condition*Test`、`RankingConditionVariableTest`；疑点 4、9                 |
| G7  | 孤儿 vs `Para_source`                         | 不做（2026-11-12）：原资产 `textPara/query` 是旧版独立查询，当前产品已无此类型，等价场景由 `Para_basequery` 等 worksheet 用例覆盖，见第 4 节 | 1.3 第 5 项                                                              |
| G8  | `prompt=false` 路径                           | 已确认（Q4）：vs 禁用参数框与 ws 变量 `prompt` 都只能压制，取并，没有优先级冲突，见第 4 节 | V9                                                                     |
| G9  | 哨兵参数 `NULL_VALUE` 等                         | 内存和 SQL 路径单测已做                                           | `ConditionVariableValueTest`、`ConditionListHandlerSentinelTest`；疑点 6、8 |
| G10 | 变量默认值的类型边界、JSON 往返                          | 单测已做，其余产品仓库已有                                            | `VariableTableJsonTypedValueTest`；疑点 11                                |
| G11 | MV × 变量条件（#79482、#79417、#80152、#80155）      | 不属于 vspara，转交 MV 专项                                      | 无                                                                      |
| G12 | 文本参数 `duration` 格式                          | 单测固定现状                                                   | 疑点 12                                                                  |


## 3. 待验证清单（浏览器，服务启动后执行）

bug 项指 #80963 里的编号。结果于 2026-11-11 在本机实例上用浏览器验证（构建含 community `afe28dda42`），V6 未做。


| #   | 对应     | 步骤                                                                                        | 修复后预期                  | 当前（单测固定的现状）                              | 结果                                                                                                                                                                                                                                                                                                                                                          |
| --- | ------ | ----------------------------------------------------------------------------------------- | ---------------------- | ---------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| V1  | 项 1    | Text 内容 `{parameter.p}`，用 `?p=%7Bparameter.p%7D` 打开                                       | 显示字面 `{parameter.p}`   | 请求不返回，一个线程占 CPU                          | 未复现（已修复）。7 秒内正常加载，显示字面 `{parameter.p}`，对照 Text 正常。已被 community `afe28dda42`（#7981）修复。                                                                                                                                                                                                                                                                       |
| V2  | 项 2    | Selection List 绑定含 NULL 的列，多选 NULL 和另一项；Hyperlink 勾选"发送选择作为参数"，目标页放 Text `{parameter.列名}` | 显示 `[a,null]` 或跳过 null | 目标页报错（空指针）                               | 未复现（已修复）。用 URL 模拟 `q=5&q=null&q.__type__=integer`，显示 `[5,null]`，不报错（`ParameterTool` 现用 `String.valueOf`）。未走真实 Selection List + Hyperlink 前端。`ParameterToolTest.Suspect.arrayWithNullElement_throwsNullPointerException` 断言旧行为，需更新。                                                                                                                          |
| V3  | 项 3    | Text 内容 `{0} {parameter.name}`，用 `?name=abc` 打开                                           | 显示 `{0} abc`           | 参数没替换                                    | 未复现（已修复）。`{0} {parameter.name}` 加 `?name=abc` 显示 `{0} abc`。已被 `afe28dda42`（#7981）修复。`ParameterToolTest.Suspect.closingBraceBeforeTheParameter_leavesTheParameterUnreplaced` 断言旧行为，需更新。                                                                                                                                                                      |
| V4  | 项 4、G6 | ws 多选变量 `v`（条件：列 属于 `$(v)`），打开参数对话框不选任何值                                                  | 与"空选"的约定一致（测试人员判断：不传就不过滤；开发未正式确认） | 条件被忽略，显示全部行                              | 符合预期（测试人员判断：不填任何数据等于没传参数，条件被忽略）。用 `Para_wsgroup` 的 `POneOf`，弹窗留空后条件被忽略（Customer1 从 11 行变 13 行，分组不生效），与代码一致（`VariableTable.put` 把空数组存成 null，条件缺值即忽略）。开发尚未正式确认；#80963 第 4 项可按此处理。                                                                                                                                                                                                                                                                           |
| V5  | 项 5    | Calendar 的 `_dateStart` 参数用链接传空值                                                          | 界面里没选日期不发参数（null），空串只出现在手写 URL | 空串                                       | 与普通参数一致，不作为缺陷。`cal_dateStart=` 显示 `[]`（空串），完全不传显示 `[null]`；普通字符串参数传空值（`?p=`）同样按"空"去匹配、不被忽略，不传才被忽略，所以日历后缀参数并不特殊（按代码，选择类参数的空值回退为原值 `""`，与普通参数相同）。已验证（2026-11-11）：做了一个绑定日期列的日历加一个"发送选择作为参数"的超链接，指向显示 `{parameter.Calendar1_dateStart}`、`{parameter.Calendar1_dateEnd}` 的目标页。没选日期点超链接，目标页两个参数都是 `null`（参数根本没发送，不是空串）；选了 2006-01-01 后目标页收到日期值（对照，说明参数名正确）。所以界面里的日历不会产生空串，`""` 只会出现在手写 URL（`cal_dateStart=`），行为同普通参数。                                                                                                                                                                                                                                                                                                   |
| V6  | 项 8    | 数据库一列同时有 NULL 和 `''`；ws 条件 `列 = $(p)`，`?p=NULL_VALUE`；比较 SQL 下推与内存过滤（Mirror 或 MV 之后）两种配置  | 行数一致，或开发说明为何不同         | SQL 只含 NULL 行，内存还含 `''` 行                | 复现了文档记录的差异。自建 H2 表 `V6T`（ID1=NULL、ID2=空串、ID3=字符串 `null`、ID4=`a`），ws 条件 `COL = $(p)`。SQL 下推（条件在绑定表上）：`p=NULL_VALUE` 只返回 ID1；内存过滤（Mirror 表，关闭 Merge SQL）：`p=NULL_VALUE` 返回 ID1 和 ID2（空串也算 null）。`EMPTY_STRING` 两边都只返回 ID2，`NULL_STRING` 和 `null` 都返回 ID3，`a` 返回 ID4。即同一个条件下推到 SQL 与在内存里执行，`NULL_VALUE` 的行集不同，与 #80963 项 7 一致。另用现成 `nullVSTest2` 验证了内存路径的哨兵接线。 |
| V7  | 项 9    | ws 的 TopN，N 为 `$(n)`，不传 `n` 且 `prompt=false` 打开                                           | 与"变量缺失"的约定一致（弹窗里 N 默认填 0，见 #81094） | N 保持未解析引用，下游未知                           | 修正：之前的结论有误。弹窗里 `top`、`Pbottom` 的输入框默认值是 0，"留空"提交的其实是 0。实测（`Para_wscondition2`）：`top=0` 受 Top N 控制的表 0 行（Top 0 为空，合理）；在弹窗点 Clear 去掉 0 后再 OK，表显示全部数据，Top N 被忽略，与普通条件"缺值即忽略"一致。所以 V7 的本意（N 缺失）行为正确，不是缺陷。另见一个待核实的点：`Pbottom=0` 时数据库表 `orders` 显示 22 行（等于没过滤），而 `top=0` 时内存表是 0 行，N=0 在两条数据路径上结果不同，是否有意不确定。                                                      |
| V8  | #80154 | ws `zz79607/expr`（#80154 附件）：Long 变量 `v`，默认值表达式 `3000000000`，在 Composer 打开                | 显示 `3000000000`，无错误    | 显示 `3.0E9` 并提示 "Please enter an integer" | 复现：`v` 显示 `3.0E9`，提示 "Please enter an integer."，#80154 未修（已报）。                                                                                                                                                                                                                                                                                              |
| V9  | G8、Q4  | ws 变量 `prompt=false` 且有默认值，vs 不设 `disableParameterSheet` 打开                               | 不弹对话框，用默认值             | 两层都只能压制、取并，没有优先级冲突（见第 4 节 Q4）            | `Para_wsvariable2` 不传参数打开：不弹窗，用默认值，数据与基线一致。但它测的不是文档设想的场景：存储里 `ws_variable2` 的变量全是 `prompt="true"`（都有默认值），vs 的 `disableFlag="true"`（即禁用参数框）才是不弹窗的原因。所以"ws `prompt=false` 且 vs 不禁用"这个组合没有被覆盖，仍需另建用例；"vs 禁用压过 ws `prompt=true`"已在浏览器里观察到。                                                                                                                      |
| V10 | G3     | `Para_wscondition` 的 vs，URL 带 `x=2024-03-15&x.__type__=date`（`x` 换成该 ws 的日期变量名）           | 行数与 `?x=2024-03-15` 一致 | 单测：得到 `java.sql.Date`                    | 通过：用 `Para_wscondition2` 的 `PstartYear`/`PendYear`（`Para_wscondition` 没有日期变量），日期与日期时间各带或不带 `.__type__=date` 共 4 种，结果一致（4 行，同基线）。                                                                                                                                                                                                                            |
| V11 | A7-C   | 跑现有 `VarExp_Binding_Spec`（`-Pvspara`），确认 `$(组件)` 取值与书签状态一致                                | 全部通过                   | 未重新运行                                    | 数据一致：22 个用例的 `compareData` 全部通过；22 个失败均为 PNG（15 个 Chart 缺 `ChartN.png` 基线，7 个 Crosstab 像素差异）。                                                                                                                                                                                                                                                               |


V1、V2、V3 最值得先验。

## 4. 遗留：`Para_basequerysql`

严格基类下失败，是产品行为变化，不是 spec 写错：该 ws 在有 VPM 的数据源上用手写 SQL（`parseResult=3`），不读受保护表也被拒绝；不传 `Ppid` 报 `Variable in VPM condition missing`，传了报 `Query rejected by VPM`。用例现已恢复（见第 4 节）。要问开发的问题和已试过的做法见 A9。

**Q1 结论（2026-11-11，读企业版 `VpmUtil` 加浏览器实测；开发尚未正式确认）：**

- 拒绝是设计如此，不是缺陷（Bug #77473，拒绝优先）。手写 SQL 解析不了，引擎不知道它读了哪些表；只要该用户适用的 VPM 带表条件（`MySQL1` 的 `VPMP1` 对 `orders.products`），就无法证明没读受保护表，默认策略 `reject` 直接拒绝。
- 两条报错是同一个检查的两步：不传 `Ppid` 时先算 VPM 条件里的变量，报 `Variable in VPM condition missing: Ppid`；传了 `Ppid` 后确认 VPM 会限制，但表未知，报 `Query rejected by VPM`。
- 修法：在 SQL 第一行加整行注释 `-- vpm.tables: orders.orders, orders.contacts, orders.order_details, orders.customers`（以 `--` 开头，`vpm.tables` 后跟冒号，表名逗号分隔，不列 `orders.products`）。浏览器实测：把 `ws_basequerySQL` 另存为复制品并加上这一行（SQL 编辑器仍显示 "SQL parsing failed"），用 `y2004=2004-01-01`、`cid=10`、不传 `Ppid`，表格正常出 4 行，无 VPM 报错。
- 报错里提示的 `/*<where>*/.../*</where>*/` 标记只在 VPM 需要往 SQL 里插入条件时才用，本用例不需要。
- 不建议用 `vpm.unparsed.sql.policy=warn`：会让 `Para_vpm1~3` 等本该被拒绝的场景也放行。
- 本节第 3 条（A9）写过"加注释后仍报表未知"，当时是直接改存储快照里的 blob，通过产品界面保存则正常。原因已查清（2026-11-12）：那次失败时测试存储目录没有重新还原，跑的还是旧内容，和改 blob 的方式无关；删掉还原目录后，直接改 blob 同样通过（见下一条）。
- 为什么 `ws_basequerySQL` 解析失败：SQL 里用了 `time(...)` 函数。语法里 `function_name` 只接受标识符和 `date`，没有放行 `time`，所以 `time(...)` 放在 select、where、order by、group by 任一处都会解析失败；Time 类型的列、`TIME '10:00:00'` 字面量、`cast(... as time)`、`hour()`、`time_format()` 都能解析。不是回归（`T_TIME` 关键字和 `function_name` 的写法从仓库初始提交就是这样，ws 在 2023-06-27 保存时 `parseResult` 就是 3）；解析失败后原 SQL 照常运行，按 SQL 解析规范属于预期结果，不提单。
- 已做（2026-11-12）：把上述注释加进 `ws_basequerySQL`，更新 `vspara-storage.zip`，去掉 `@Ignore`。基线没有重新生成，用原来的 `TableView1.txt`（13 行 × 8 列）通过。
  - 做法：没有从实例导出，而是直接改快照里该 ws 的存储块：在 `<sqlstring>` 开头插入注释行，重算内容的 MD5（文件名就是 MD5），再改 `blob-index.json` 里的摘要和长度。
  - 注意：测试用的存储是 `generate-test-resources` 阶段从 zip 还原到 `target/test-resources/server/config-vspara` 的；这个目录已存在时不会重新还原，改了 zip 后要先删掉该目录再跑，否则跑的还是旧内容。

**手写 SQL 里的参数留空：绑成 NULL，结果是空表，不适用"没传参数就忽略条件"（2026-11-12，读代码加界面实测）：**

- 现象：打开 `ws_basequerySQL` 的副本（带 `-- vpm.tables:` 注释），点 Preview，`y2004` 和 `cid` 都留空，没有报错，只有表头、没有数据；填 `y2004=2004-01-01`、`cid=10` 则有数据。
- 原因：手写 SQL 里的 `$(变量名)` 由 `VarSQL.replaceVariables` 处理，是文本替换加 JDBC 参数绑定。值为 null 时同样绑成一个 `?`，SQL 变成 `... > NULL`，数据库里任何行与 NULL 比较都不成立，所以是空表。
- 和界面条件的区别：worksheet 里用界面设置的条件，变量缺失时整条条件被忽略（`Condition.replaceVariable`，V4、V7 验证的就是这一类），所以不填就显示全部。手写 SQL 不经过条件，没有"变量缺失就去掉这段"的逻辑。
- 结论：这是手写 SQL 的固有行为（写什么执行什么），不当缺陷提。写用例时注意：手写 SQL 的用例不能用"不传参数"来期望"显示全部"，要传值。
- 开发尚未正式确认；如果产品希望手写 SQL 也"留空就不过滤"，那是设计层面的改动。

**G3：超链接传 null 加类型（2026-11-12，datatest 实测）：**

- 浏览器只在超链接参数值为字符串 `"null"` 时才发 `.__type__`，值是该参数声明的类型。所以真实场景是"null 加类型"。
- 已加用例 `Para_wscondition2_typedNullInteger`（`Para_VS_Spec`）：`top`、`Pbottom` 传 `null` 加 `integer`，两个排名条件被忽略，其余条件仍生效，表格由 5 行变成 23 行，`Paraws_condition` 由 3 行变成 4 行。符合"不传就不过滤"。
- 日期不一样：`PstartYear`、`PendYear` 传 `null` 加 `date`，值保持字符串 `"null"`（`ParameterService` 解析失败回退，A7），日期条件让表格变成空表（只有表头），没有报错。和整数的行为不一致。
- 这个日期用例没有保留：保留会把"现在是空表"固定成期望。已在实例里用同样的查询参数复现（`Para_wscondition2`，`orders` 表变成只有表头），并提了 **Bug #81180**（和 #81090 关联；后来按团队规则改成 Dev feedback needed，同时包含布尔）。修复后可重做该用例，参数是 `PstartYear=null&PstartYear.__type__=date`、`PendYear=null&PendYear.__type__=date`，其余同 `Para_wscondition2`，期望是日期条件被忽略。
- 没有点开超链接对话框，是直接用超链接产生的 URL 复现；对话框能否保存类型为 date、值为 null 的参数未验证。
- `Para_dataview_typedNull`（`Para_VS_Spec`）：`short`、`float`、`long` 传 `null` 加类型，每个单独都和"不传这个参数"得到同样的数据（逐个对比过），用例固定这个行为；`boolean` 保持正常值。
- 布尔不一样：`boolean=null&boolean.__type__=boolean` 得到 `false`（`CoreTool.getData` 的 boolean 分支，解析不了就回退 `false`），`TableView1` 变成空表，而不传是 2 行；空值加 boolean 得到 `true`。这个行为是否合理无法确定（单测里 `maybe` 当 boolean 得 false 是有记录的），按团队规则标 **Dev feedback needed**，和日期一起写进 #81180（原来的日期单已改写成这个问题），布尔用例不保留。
- 浏览器真实发送的类型不是超链接对话框里的名字：表格列绑定的参数用 JS 的 `typeof`（null 单元格是 `object`，服务端得到 null），写死的参数才是超链接里存的类型，所以日期和布尔需要"写死的、类型为 date 或 boolean、值为 null"的超链接参数才会碰到，这一点未验证。

**G7：孤儿 vs `Para_source`（2026-11-12）：不做，原资产已失效，缺口由 worksheet 用例覆盖。**

- `Para_source` 的数据源是 `textPara/query`（`subType=text`，`prefix=textPara`，类型 37），是旧版独立查询。zip 里没有这个查询，产品代码里也已经没有"文本"数据源类型。无参数运行得到 `0 x 0` 的空表，没有报错；仓库里的旧基线 `expectData/.../Para_source/TableView1.txt`（2 行）来自查询还存在的旧环境，早已对不上。
- 当前版本里"New Query"打开的是新建数据 worksheet（`composer?wsWizard=true&baseDataSource=...`），已建不出独立查询，所以无法照原样重建。
- "查询当数据源时参数接线"的等价场景是带 Database Query 的 worksheet，已由 `Para_basequery`（`ws_basequery`，日期时间和整数参数）、`Para_basequerysql`（手写 SQL）、`Para_query`、`Para_query2`、`Para_sub` 覆盖，新建一个会重复。
- 对比：vs 的数据源是 worksheet、worksheet 的数据源 `ai_v6h2` 不存在时，打开会明确报错（`Error loading data. Data source may have changed`、`No data source specified`）；`Para_source` 的独立查询缺失时却静默显示空表。判断为不提单：触发条件只有旧版导入的遗留资产，当前产品建不出这种 vs。
- 已删除陈旧基线目录 `expectData/vspara/Para_VS_Spec/Para_source/`（git 里仍可恢复）。

**G4：double、float、date、datetime 变量组件（2026-11-12）：已做，新建 ws `wsForVSPara/ws_vartype` 和 vs `ParaVS/Para_wsvartype`。**

- 原来 zip 里 worksheet 的变量组件只有 string、integer、boolean、short、long、time、timeInstant（只出现在条件里）、byte；没有 double、float、date、datetime 的变量组件。
- `ws_vartype`：内嵌表 `Data`（`Key`、`Double`、`Float`、`Date`、`DateTime` 五列，6 行 A 到 F），镜像表 `Query1` 带四个条件，由四个变量组件驱动：`vdouble`（double，下拉框，`Double >= 值`）、`vfloat`（float，多选列表，`Float` 属于列表）、`vdate`（date，多选列表，`Date` 属于列表）、`vdatetime`（timeInstant，文本输入，带默认值 `2020-05-01 12:00:00`，`DateTime <= 值`）。
- 数据和参数是专门设计的：四个条件各自只会丢掉一行（A 被 `vdouble` 丢，B 被 `vfloat` 丢，D 被 `vdate` 丢，F 被 `vdatetime` 丢），所以任何一个条件被忽略或写错，结果都会变，不会被别的条件掩盖。用例 `Para_wsvartype` 发 `vdouble=20.5`、`vfloat` 五个值、`vdate` 五个日期，不发 `vdatetime`（走默认值），期望 C、E 两行，实测一致，`vdatetime` 默认值生效（F 被丢）。
- 这两个资产最初是直接写 XML 加进 `vspara-storage.zip` 的（内嵌表数据按 `XEmbeddedTable.writePieceData` 的格式编码，vs 以 `Para_wsvariable` 为模板，去掉了选择树）。2026-11-12 按 review 意见做了验证：用 EM 的导入功能（按产品导出包的格式打成包）导入本地实例，在 Composer 里打开 `ws_vartype`（四个变量控件的类型、选项、默认值都正确显示，连线正确）和 `Para_wsvartype`，门户里用用例同样的参数打开，结果是 C、E 两行；两个资产都在 Composer 里重新保存了一次。保存后内容的语义没变（内嵌数据、条件、变量的类型/样式/选项/默认值逐项一致），只是规范化了写法（去掉冗余的 `<view>`、列选择里部分 `dataType`、加 `<id>`、元数据），zip 里现在放的是 Composer 保存出来的版本，用例重跑通过，基线不变。
- datatest 不经过提示框，提示框里的多选、默认值是在 Composer 的参数框里看的：`vdouble` 是下拉框、`vfloat` 和 `vdate` 是多选列表、`vdatetime` 是日期时间选择器且默认值已填好。

**Q4 结论（`prompt` 两层优先级）：没有优先级冲突，两层都是只能压制的开关，取并。**

- vs 禁用参数框（`ViewsheetInfo.isDisableParameterSheet`，存储里是 `disableFlag="true"`）：`VSEventUtil` 里整个"收集要弹窗的变量"的循环被跳过，所有变量都不弹。浏览器已观察到它压过 ws 变量的 `prompt="true"`（V9）。
- ws 变量 `prompt=false`：`XEngine` 收集提示变量时排除它（`isPrompt() || !promptOnly`），执行时用默认值。这一点是读代码得出的，没有在浏览器里验证。
- 参数已在 URL 里传了（`SUtil.isNeedPrompt`）、或有同名输入组件：同样不弹。
- 只有所有开关都允许，变量才会弹；任何一个说"不弹"就不弹。

