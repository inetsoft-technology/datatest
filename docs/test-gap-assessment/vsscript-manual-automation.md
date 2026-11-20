# VSScript 手工用例自动化

来源：`docs/vsscript/VSScript-Manual-Case.xlsx`，实际共 56 个手工用例（Component 34、OtherScript 22；表头 OverView 写的 32 和 22 与实际不符）。资产都在 `vsscriptStorage.zip` 的 `Manual/` 下，不需要新资产。
自动化落点：`vsscript/src/test/groovy/.../state/ScriptStateManual_Spec.groovy`（55 个用例）。三个数字的关系：手工用例 56 个；其中 40 个有了自动用例（含"部分"，见汇总）；这 40 个手工用例对应 55 个自动用例，因为一个手工用例常拆成几个断言型用例（如 `Viewsheet3` 的 4 个资产各一个，`ScopePara4` 有参数、无参数各一个）。用 `ScriptStateBase` 在进程内打开运行时 viewsheet，读脚本写进组件的状态，不出 PNG、不开浏览器。

## 方法

| 类 | 手工检查什么 | 自动化方式 |
|---|---|---|
| A | 打开后脚本写进组件的值（属性、文本、可见性、动作可见） | 打开后读 scope（同时核对 assembly info，见"验证"） |
| B | 先操作（选择、输入、点击、确认、改参数）再看值 | 触发和浏览器相同的事件，再读状态 |
| C | 超链接、flyover 的条件 | 读浏览器会收到的命令（单元格超链接、刷新命令里的标题） |
| D | 只能看屏幕：悬停提示的样子、弹出位置和透明度、GIF 闪烁、上传、邮件、composer 里的 UI | 保留手工 |

事件入口（`ScriptStateManual_Spec` 里的帮手）：

| 帮手 | 走的服务 |
|---|---|
| `click(t, 'Image1')` | `OnClickService.onClick` |
| `confirm(t, 'Submit1', true)` | `OnClickService.onConfirm` |
| `input(t, 'Spinner1', 60d)` | `VSInputService.singleApplySelection`（单选、复选、下拉、滑块、微调、文本输入） |
| `select(t, 'SelectionList1', [['AK']])` | `VSSelectionService.applySelection` |
| `flyover(t, 'Chart1', 'Category^VALUE:Educational')` | `VSChartFlyoverService.eventHandler`；条件串是 `维度名^VALUE:值`，多个点用 ``` `` ``` 连接；服务在 datatest 里不是 bean，手工装配 |
| `ScriptStateBase(id, [参数: [值]])` | 打开请求的参数（URL 查询串） |

`refreshRecorded()` 返回的命令里：`LoadTableDataCommand.tableCells()` 的 `hyperlinks` 是单元格超链接，`AddVSObjectCommand` 的模型 `hyperlinks` 是仪表盘超链接。

## 逐个对照

状态：**已做** = 本 spec 有用例（"部分"=只做了能自动化的部分，其余归 D）；**已覆盖** = 已有别的自动用例；**C/B** = 没做；**手工** = 保留手工。期望值取自手工用例的截图或步骤文字。

### Component-Case

| 手工用例 | 资产 | 状态 | 说明 |
|---|---|---|---|
| Viewsheet1 | Viewsheet/VS_Action | 已做 | 16 个工具栏动作被隐藏；文本 15 个值与截图一致，含资产里拼错名字的两处 true |
| Export Action Visible | Viewsheet/VS_ExportAction | 已做 | 依次选 7 种类型，已选过的保持隐藏（累积），文本始终 false |
| Viewsheet2 | Viewsheet/VS_Schedule | 手工 | 要邮件服务和真实任务 |
| Viewsheet3（4 个资产） | VS_setInputSelectedObject_* | 已做 | element、init、onclick、refresh 四种触发；含 Bug #72320 的变量表断言 |
| ChartTooltip / TipView / Flyover | Chart/TestCase-* | 已做（部分） | `toolTip` 模式串、`tipView`/`tipAlpha`、`flyoverViews`/`flyOnClick`；`tipAlpha` 是 String 属性，写 60 读回 `'60.0'` |
| dataConditions（图表） | Chart/TestCase_DataCondition | 已做 | flyover 一个点，Query1 标题 `Filter: Category = Educational`，两个点 `Educational, Games` |
| Highlight | Chart/TestCase_ChartHightlight | 已做 | 点 Image1，消息 `State_highlight was applied` |
| coordinatesetFullMap | Chart/TestCase_coordiantesetFullMap | B | 要读图形坐标 |
| drillEnabled | Chart/TestCase_DrillEnabled | 已做 | `drillEnabled` 初始 true，点 Text1 后 false |
| Padding | Chart/TestCase_Padding | 已做 | 点 Image1，padding 10 变 50 |
| SetAction | Chart/TestCase_SetAction | 已做 | 点 Image1，26 行 true 变 false，与截图一致 |
| ChartHyperlink | Chart/TestCase_ChartHyperlink | C | 图表元素超链接在图表区域模型里，要走 `VSChartAreasService` |
| FlyOver1 | Table/Table_Action1 | 已做（部分） | `tipView`、`tipAlpha`；表格 flyover（`BaseTableFlyoverService`）没做 |
| dataConditions（表格） | Table/Table_Action2 | 已做（部分） | 点 NJ，TableView1 和 Crosstab1 标题 `Filter: state = NJ`；Freehand 是 tip view，走 tip 请求，没做 |
| hyperlink（表格） | Table/Table_Action3_hyperlink | 已做 | 三种表的单元格超链接（链接、窗口、类型、参数） |
| form / actionvisible | Table/Table_Action5_form、_Action4_actionVisble | 已做 | 四个表单标志跟随 radio 双向；三种表的动作可见性 |
| Selection ActionVisible | Selection/Selection_ActionVisible、_ActionVisible2 | 已做（部分） | 列表、树、范围滑块；5 个日历没做 |
| ListTree / submitOnChange / Calendar / Container | Selection/* | 已做（部分） | radio 切换，对应属性跟随；"submitOnChange 为 false 时选值不过滤"是浏览器不发事件，归 D |
| Combox | Form/Combox_bind | 已做 | `a4-4`；选 c3 仍是 a4-4（脚本每次刷新重设）；后两个下拉的日期范围和时区 |
| Spinner | Form/Spinner | 已做（部分） | `min`/`max`/`increment`；设 60 后文本 `60: 60` |
| Upload / ImageActions | Form/UpLoad、OutPut/Image_Actions | 手工 | 上传、弹出组件位置和透明度 |
| SubmitOnClick / SubmitActions | Form/Forms_Submit、OutPut/Submit_Action | 已覆盖 | `ScriptStateSubmit_Spec` |
| refreshAfterSubmit | Form/Form_Refresh | 已做（部分） | 属性跟随 radio；刷新与否要观察刷新命令，没做 |
| GaugeActions | OutPut/Gauge_Actions | 已做（部分） | Gauge1 超链接带 `state=NJ`；flyover 后 Gauge2 被筛选、图表标题 `Filter: state = NJ` |
| TextActions | OutPut/Text_Actions | C | Text2 是 tip view，条件走 tip 请求，harness 里没找到入口 |
| GroupContainer | Others/GroupConatiner | 已做（部分） | `animate` 跟随 radio；GIF 是否闪烁归 D |

### OtherScript-Case

| 手工用例 | 资产 | 状态 | 说明 |
|---|---|---|---|
| Script_Order1 | ScriptOrder/Script_Order1 | 已覆盖 | `ScriptEngine_Spec` 的 PNG 基线 |
| Script_Order4 / OneWay1 / OneWay2 | ScriptOrder/* | 已做（部分） | 脚本联动、Export 动作随 Text1 可见性；OneWay2 的 TopN 变化没做 |
| circle1 | ScriptOrder/Script_Order_circle1 | B | 要在 composer 里改文本 |
| Scope1 | OtherScript/scope1 | 已做 | 选 AK 4 行表隐藏，加选 CA 9 行显示 |
| Scope2 / Scope5 | OtherScript/scope2、scope5 | 已做（部分） | Text1 背景绿色；31 个州的文本，选 AK 后文本和提示 `state length too small` |
| Scope3 / Scope6 | OtherScript/scope3、scope6 | B | 图表绑定；Scope6 的悬停联动归 D |
| Scope4 | OtherScript/scope4 | 已做 | 输入 3 标题 `you send value < 5`，输入 10 标题 `your send value >= 5` |
| ScopePara1 | OtherScript/scopePara1 | 已做 | 带齐 4 个提示变量打开（等于对话框点 OK）与截图一致；不带时视图表在等对话框，onLoad 没跑 |
| ScopePara2 / 3 / 4 | OtherScript/scopePara2、3、4 | 已做 | 6 种类型参数读回；radio 决定图表类型；URL 参数 |
| Confirm | OtherScript/sConfirm | 已做 | 点 Submit 弹确认；Yes 后列表禁用、radio 隐藏；No 不变 |
| Event | OtherScript/sEvent | 已做（部分） | onInit 排序；`event` 对象要经事件触发，没做 |
| pviewsheet | OtherScript/pViewsheet | B | 父子视图表要先打开链接 |
| saveworksheet / appendRow | OtherScript/saveworksheet、appendRows | 手工 | 会改工作表数据 |
| scriptAction | 新建 chart | Java 单测 | `graph.script.action.support` 建议放 core 单测 |
| parameterScript | OtherScript/Script_Order_OneWay4 | 已做 | 带参数 `state=NK,NY,NJ` 打开 |

### 汇总

| 状态 | Component | OtherScript | 合计 |
|---|---|---|---|
| 已做（含部分） | 26 | 14 | 40 |
| 已覆盖 | 2 | 1 | 3 |
| B（未做） | 1 | 4 | 5 |
| C（未做） | 2 | 0 | 2 |
| 保留手工 | 3 | 2 | 5 |
| Java 单测 | 0 | 1 | 1 |
| 合计 | 34 | 22 | 56 |

剩下的 B、C 价值不高，决定不做（2026-11-19）。

## 验证

- **对照截图：**xlsx 里有 87 张截图，挂在对应步骤的行上（解压后按 `xl/drawings/drawingN.xml` 的锚点行号对应 `xl/media/imageN.png`）。已做用例的期望都与截图核对过，没有发现不一致。
- **变异验证：**把改过的产品类用 `javac` 编译后放进 `vsscript/target/test-classes/`（排在产品 jar 之前），跑 spec，用后删除。先做对照：未改源码重编译的类与 jar `javap -c` 一致。16 处变异全部被对应用例抓到：

  | 产品改动 | 翻红的用例 |
  |---|---|
  | Spinner `min` 读 `getMax` / `increment` 改名 | `SpinnerScript_range_and_increment` |
  | GroupContainer `animate`、Crosstab `drillEnabled`、SelectionContainer `adhocEnabled`、Table `form`、Input `labels` 改名 | 各自对应的用例 |
  | Calendar `yearView`、SelectionList `dropdown` / `submitOnChange`、Chart `padding` 改名 | 各自对应的用例 |
  | `setActionVisible` 写入取反 | 4 个动作可见性用例 |
  | Chart `tipView` 改名 | 见下，第一次没翻红 |
  | `setInputSelectedObject` 去掉 `info.setSelectedObject`、去掉 Bug #72320 的变量表更新 | element、refresh / init、onclick |
  | `OnClickService.onConfirm` 里 `if(confirmEvent.confirmed())` 改 `if(false)` | `Confirm_submit_asks…` |
  | `VSChartFlyoverService` 的 `setTipConditionList` 改永远 null | 3 个 flyover 用例 |
  | `VSTableLens.addHyperlink` 去掉 `__NULL_LINK__` 过滤 | `TableAction3_hyperlinks…` |

  - **一次没抓到：**`Chart1.tipView` 改名后，脚本赋值变成普通 JS 字段，读回的还是自己写进去的值，资产里又有同样的 tip view，所以只读 scope 区分不出脚本是否生效。已补 `ChartTipView_script_reaches_the_assembly`（脚本写另一个值，读 assembly info），其余 flyover、tooltip 用例也同时核对 assembly info。
  - 未做变异：`ScopePara2`、`ScopePara4`、`ScriptOrderOneWay1`、`ScriptOrder4`、`SEvent`（读脚本引擎和参数，不是属性表映射）。
- **真实服务：**在隔离的企业版容器里（镜像 2026-11-18，`vsscriptStorage.zip` 还原存储，验证后已删除）确认了两处，都与手工截图一致：`ScopePara1` 点参数对话框 OK 后的文本；`Gauge_Actions` 悬停一个点后 Gauge 变 120、图表标题 `Filter: state = CA, 2000-01-01 00:00:00.0`。
- **新 main：**用 origin/main 14baf5fe27 的 core 重跑，`state/` 下 99 个用例全部通过；`cases/` 下旧的 PNG 比对 114/115 因像素差异失败，与之前一致（本机渲染环境，CI 里该步骤 `continue-on-error`）。

## 结论

- **这条线没有发现产品 bug。**手工用例本来就是已通过的，自动化把它们固化下来挡回归；客户端行为（D 类）没有验证。
- **两个疑点都排除了：**`ScopePara1` 的 onLoad 参数"丢失"，是 datatest 打开时没走完参数对话框（带齐 4 个变量即与截图、13.8、真实服务一致）；`Gauge_Actions` 的刷新命令里标题落后一步，只是 flyover 过程的命令顺序，真实界面显示正确。
- **API 既有行为：**`tipAlpha` 在属性表里声明为 `String`，脚本写数字读回 `'60.0'`，不是回归，没有提单。
- **用例不压缩：**每个用例打开不同的资产或验证不同的行为，同一资产上的成对用例（如 `ScopePara1`、`ScopePara4` 有无参数）是对照关系，合并会失去区分度；整个 spec 约 45 秒，几乎都是 Spring 启动。
