# datatest-gap 共享规则与工具

被 `/datatest-gap-workflow` 使用。产出是每个模块**一个文件**：`docs/test-gap-assessment/<module>.md`，骨架见 `module-template.md`。

## 文件

| 文件 | 作用 | 验证状态 |
|---|---|---|
| `vs_backup_scan.py` | 静态扫描 StyleBI backup zip：`inventory`（组件、计算字段矩阵、书签数）、`crosscheck`（spec 对 vs 对基线）、`strictcheck`（预测严格基类会抱怨什么） | 在 vscalc 上运行，结果与手工核对一致 |
| `param_scan.py` | `params <zip>`：worksheet 变量组件（xtype×单/多值×prompt）、带变量的条件（按运算符）、VS 动态值（`=parameter.x`、`$(组件)`）；`cases <Spec.groovy…>`：每个用例的导出方法、比对方式、书签、参数。复用 `vs_backup_scan.load` | 在 vspara 上运行；`cases` 只认 `ViewsheetTest`/`VSExportTest` 写法 |
| `module-template.md` | 产出文件的骨架 | vscalc 已按它写成样例 |

## 路由：单测还是 dashboard

先问两个问题：**期望值能不依赖产品输出、独立算出来吗？** 能 → Java 单测。**问题出在组件、绑定、数据源之间的接线吗？** 是 → dashboard 用例。

| 缺口类型 | 方向 |
|---|---|
| 聚合与公式、表达式求值、类型转换、序列化往返、null/空集/并列/NaN 边界 | **Java 单测，产品仓库** |
| 某特性在各组件、各绑定位置、各数据源下是否被正确取值与渲染；导出；备份还原后资产兼容 | **dashboard 用例** |
| 一个特性两边都涉及 | 单测验证逻辑；dashboard 每类接线只放一个代表性用例，不为边界值堆 vs |
| 验证机制本身的缺陷（空转通过、导出失败不报错、基线缺失不报错） | 改断言基类，不是加用例 |
| 交互（筛选、参数联动、钻取） | 不属于静态 datatest，另行规划 |

## 一个缺口值得写进文件，需同时满足

1. 能说出**会放过什么错误**：如果代码出现某某错误，当前测试会放过。说不出就不是缺口。
2. 有证据（`file:line` 或脚本输出）。
3. 有方向和理由。

没核对过的，在文件里写"未验证"或"静态推断"，不当作事实陈述。

## 证据纪律（来自 vscalc 的真实失误）

| 失误 | 防护 |
|---|---|
| 把 XML 属性含义读反（`baseOnDetail` 默认 true，**只在 false 时才写出**，先按"有属性=明细"解读，得出"覆盖为 0"的错误结论） | 解释任何属性或开关之前，先在产品源码里找它的序列化和解析代码确认 |
| 把 zip 里 `4^16384^…` 条目当成 autosave，其实是**书签存储**（保存选择状态），导致低估了选择类组件的覆盖 | 对 zip 里不认识的条目，先解码看内容，不凭键名猜 |
| 同名类跨包（`DistinctCount`、`Mode`、`Median`、`NthMostFrequent` 在 `report/filter` 与 `mv/formula` 同名，MV 版本的测试被算到运行时版本头上） | 匹配测试看完整路径；追到运行时工厂（如 `Util.createFormula`）确认实际走哪个类 |
| 按字面量搜索漏掉间接调用（搜 `CALC.edate` 得 0 次，实际由 `CalcDateTime` 直接测） | 说"没覆盖"之前，先找到实现类，再按实现类名搜测试 |
| 手填估算数字（"约 36 个"，实际 41） | 数量由脚本或逐行计数得出，不写"约" |
| 粗脚本与精确口径不一致（统计所有 `ExpressionRef` 与只统计 `allcalc` 定义） | 文件里引用的数字必须来自脚本输出；换口径要重跑 |
| 方法名关键词当作测试质量 | 只写"没看到"；读过测试内容后才能写"没有" |
| 用静态结果下"一定缩小了数据范围"之类的结论（书签可能只存选中项） | 静态无法判断的，明说无法判断 |

## 本机环境注意事项（踩过的坑）

- 读大文件时 context-mode 会把 `Read` 换成索引搜索：用 `offset`/`limit`，或 `Bash` 的 `sed -n`，或 `ctx_execute` 只打印结论。
- 在 `Bash` 的 heredoc 里写含反斜杠的 Python 会被改写（`\n` 变成真换行）：**写文件用 `Write`/`Edit` 工具**。
- 脚本里打印中文或表情符号时，Windows 控制台默认 gbk 会报 `UnicodeEncodeError`：自写命令需 `PYTHONIOENCODING=utf-8`（`vs_backup_scan.py` 已处理）。
- 产品源码默认位置 `E:\inetsoft\sr14_0\stylebi-main\community\core`，应在 `main`；datatest 以 `Stateless-Sessions1` 为准（主分支，PR 都 merge 到它）。`main` 分支的 datatest 是旧的，缺少 `DcViewsheetTest` 等，不能用来评估。
