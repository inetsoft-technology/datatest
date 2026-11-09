# vscalc 缺口评估：第一轮（round-1）

> 日期：2026-11-09　状态：**缺口与方案已给出，待评审；落实另行安排**
> 模块：`vsothers`，profile `-Pvscalc`，spec `VS_CalcField_Spec`（22 个用例，对应 22 个 viewsheet）
> 产品源码分支：`test-cluster-p1-52-second-rename`（可能与 datatest 实际使用的产品版本不同）

## 文件索引

| 文件 | 内容 |
|---|---|
| `01-discussion-and-findings.md` | 完整的讨论记录和评估：验证机制、覆盖矩阵、单测盘点、TODO、**第 16 节结论汇总** |
| `02-unit-test-case-list.md` | 产品仓库单元测试方案：99 个用例清单（草案，未运行验证） |
| `03-datatest-plan.md` | datatest 侧补法方案：推广严格基类、补孤儿 vs、补薄弱格子、选择类组件断言（精简版） |
| `evidence/inventory.md` | `vs_backup_scan.py inventory` 的输出 |
| `evidence/crosscheck-VS_CalcField_Spec.md` | `vs_backup_scan.py crosscheck` 的输出 |
| `evidence/strictcheck-VS_CalcField_Spec.md` | `vs_backup_scan.py strictcheck` 的输出：严格基类的静态预测 |

复现证据：

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory vsothers/src/test/resources/config/vscalc-storage.zip
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck vsothers/src/test/resources/config/vscalc-storage.zip vsothers/src/test/groovy/inetsoft/test/viewsheet/cases/vscalc/VS_CalcField_Spec.groovy vsothers/src/test/resources/expectData/vscalc/VS_CalcField_Spec
python .claude/workflow-shared/datatest-gap/count_cases.py docs/test-gap-assessment/vscalc/round-1/02-unit-test-case-list.md
```

## 核心结论

1. 严格验证（`DcViewsheetTest`）已有现成实现，但本模块用的是宽松版 `ViewsheetTest`：导出失败只打印堆栈、基线缺失不报错。**推广它是见效最快的改进。**
2. 选择类和展示类组件（Selection、Calendar、TimeSlider、Gauge、Text 等）**自身的内容**（列出的值、范围、读数、文本）没有数据断言，只有整页 PNG 比对；它们的过滤效果经书签进入图表和表的数据（是否真的缩小了范围无法静态判断）。
3. 单元层缺"算得对"的验证：聚合型计算字段求值只有 1 个值级用例；`report/filter` 下 8 个公式没有专门测试（vscalc 在用其中 5 个）。已有的同名测试属于 MV 路径，不能替代。
4. 孤儿 vs：`aggregate_trendcomparison` 没有用例也没有基线。
5. 读源码发现 4 个疑似问题（未运行确认）：`NthMostFrequentFormula` 无参构造 n=0、`CorrelationFormula` 大偏移数值精度、`DistinctCountFormula` 百分比只处理 `Integer`、`Correlation.addValue(double[])` 下标约定不一致。

## 本轮撤回或更正的结论

| 旧说法 | 更正 |
|---|---|
| 几乎都靠 PNG 比对 | 每个用例同时做 TXT 数据比对和 PNG 比对；chart 的 PNG 是进程内渲染 |
| "基于明细的聚合计算字段覆盖为 0" | 标反了。`baseOnDetail` 默认 true，只在 false 时才写出；无属性 = 基于明细（58 个），`false` = 聚合型（40 个） |
| `CALC.edate` 等在单元测试里 0 次出现 | 搜索方法有偏差；测试直接调用 `CalcDateTime`，日期函数覆盖较好 |
| DistinctCount、Mode、Median、NthMostFrequent 已有专门测试 | 那些测试属于 `mv/formula`，`report/filter` 版本没有 |
| 对话中提到 `testWorksheet` 的计算字段"8 个里只有 4 个出现在基线" | 口径过粗（把所有 `ExpressionRef` 都算作计算字段）；按 `allcalc` 定义统计为 4/4。`filter` 同理，4/5 而非 5/6。**以 `evidence/crosscheck-*.md` 为准** |
| `4^16384` 条目是 autosave 副本 | 是 VSBookmark（书签）存储，保存选择状态；40 个书签，spec 引用的书签都存在 |
| 选择类组件上的计算字段完全没有数据断言 | 过于绝对：书签的选择状态会进入图表和表的导出数据；没有数据断言的是这些组件自身的内容 |

## 待办与下一步

TODO 完整清单见 `01-discussion-and-findings.md` 第 15 节。立即可做：

1. 把 `02-unit-test-case-list.md` 第 3 节的 7 个语义问题发给开发确认（null 语义、空集语义、除零与 NaN 影响最大）。
2. 推广 `DcViewsheetTest` 的严格行为：先本地跑一遍统计暴露的失败数量，再分批合入。
3. 第一阶段单元测试（41 个用例）：全是 ✅ 的先落代码。

## 局限

- 单元测试覆盖按类名、文件名、方法名静态匹配，没有逐个读测试内容。
- 只覆盖 vscalc 一个 spec；其他模块未评估，不能外推。
- 用例清单期望值未运行验证；📝 项只能防意外变化，不能证明行为正确。
- 基线来源（回归基线还是独立标准答案）按要求暂缓，未查 git 历史。
