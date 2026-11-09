# 产品仓库单元测试用例清单（计算字段与聚合公式）

> 生成日期：2026-11-09　状态：**草案，未经运行验证，未与开发确认**
> 背景与分析依据见 `01-discussion-and-findings.md` 第 13、14 节。
> 目标仓库：`stylebi-main/community/core`（分析所用分支 `test-cluster-p1-52-second-rename`）。
> 测试放置位置：`core/src/test/java/inetsoft/...`，沿用现有 JUnit 5 风格（`CalcFieldFormulaTest` 使用 `@ExtendWith(SpringExtension.class)` 与 `box.getScriptEnv()/getScope()`；`@Tag` 约定需向开发确认）。

## 0. 读法与可信度

**这份清单是设计，不是已验证的测试。** 期望值来自两个来源，已逐条标注，不要混为一谈：

| 标记 | 含义 | 期望值来源 |
|---|---|---|
| ✅ | 按定义可确定 | 数学定义或独立手算，**与当前实现无关**，是真正的"标准答案" |
| 📝 | 记录当前行为 | 读源码得出，只能防止行为意外变化，**不能证明行为正确**，需开发确认意图 |
| ❓ | 待确认 | 期望值取决于产品决策，我不替开发决定 |
| ⚠️ | 缺陷候选 | 读源码疑似问题，**未运行确认**，可能是我读错了 |

所有"读源码得出"的结论都只做了静态阅读，没有运行过任何一个用例。

## 1. 优先级与阶段

| 阶段 | 内容 | 用例数 | 理由 |
|---|---|---|---|
| 第一阶段 | A 聚合型计算字段求值、D WeightedAverage、E Correlation、I 工厂一致性 | 41 | vscalc 正在使用，缺口最大，不需要新建测试基础设施 |
| 第二阶段 | B NthMostFrequent/Mode、C DistinctCount | 26 | vscalc 正在使用，逻辑简单 |
| 第三阶段 | F 明细型计算字段、G 序列化 | 24 | 需要先确认测试入口（脚本环境、表构造方式） |
| 第四阶段 | J 通用边界（并列、负数、NaN/Infinity） | 8 | 对已有公式补边界 |

## A. 聚合型计算字段求值　`CalcFieldFormula`

位置：`report/filter/CalcFieldFormulaTest`（扩充）或新增 `CalcFieldFormulaExpressionTest`。
构造方式：沿用 `buildSingleChildFormula` 的思路，扩展出多子公式版本（多个聚合名 + 多个子公式 + 对应列位置）。
说明：vscalc 里的 `field['Sum([a])']` 写法与这里构造函数的聚合名 + 表达式之间有一层改写，这层改写需另行确认；本组用例先在构造函数层验证求值。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| A1 | 两个聚合相除 | `Sum(a)/Sum(b)`，a=[10,20,30]，b=[1,2,3] | 10.0（60/6） | ✅ |
| A2 | 两个聚合相乘 | `Average(a)*Average(b)`，同上数据 | 40.0（20×2） | ✅ |
| A3 | 聚合加常数 | `Mode(x)+10`，x=[3,3,5] | 13 | ✅ |
| A4 | 多个相关系数相加 | `Corr1+Corr2`，一组 r=1，一组 r=-1 | 0.0（浮点容差 1e-12） | ✅ |
| A5 | 条件分支，未超阈值 | `if (COUNT > 1500) {'greater than 1500'} else {'others'}`，3 个值 | "others" | ✅ |
| A6 | 条件分支，边界 | 同上，恰好 1500 个值 / 1501 个值 | "others" / "greater than 1500" | ✅ |
| A7 | 分母为 0 | `Sum(a)/Sum(b)`，b 的和为 0 | Infinity？NaN？null？ | ❓ |
| A8 | 全部为 null | 所有子公式无值，`defaultResult=false / true` | null / 0.0 | 📝 |
| A9 | 部分 null | Sum 输入 [null,3,7] | 10.0（null 被忽略） | ✅ |
| A10 | 结果为字符串 | 表达式返回字符串时 `getResult` 与 `getDoubleResult` | 字符串 / ？ | ❓ |
| A11 | 表达式语法错误 | 非法表达式 | 抛出什么异常，或返回 null | ❓ |
| A12 | 复用与分组 | 第一组累加后 `reset`，再加第二组 | 第二组结果只反映第二组数据 | ✅ |
| A13 | 克隆独立性 | 克隆后向克隆体加值 | 原对象结果不变 | ✅ |
| A14 | 日期聚合 | `Max(dateCol)` 输出 | 返回 Date 且值正确 | ✅ |

## B. `NthMostFrequentFormula` 与 `ModeFormula`（`report/filter`）

新增：`report/filter/NthMostFrequentFormulaTest`、`ModeFormulaTest`。
注意：`mv/formula` 下已有同名测试类，**包不同，不能互相替代**（见讨论文档 14.9）。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| B1 | 基本频次 | [1,2,2,3,3,3]，n=1 / 2 / 3 | 3 / 2 / 1 | ✅ |
| B2 | 并列取舍 | [1,1,2,2]，n=1 / 2 | 1 / 2（频次相同按值升序） | 📝 |
| B3 | n 大于不同值个数 | [1,2]，n=5 | null | 📝 |
| B3b | 同上且 `defaultResult=true` | 同上 | 返回 null 还是 0？与"空集返回 0"不一致 | ❓ |
| B4 | 空集 | 无输入，`defaultResult=false / true` | null / 0；`isNull()=true`；`getDoubleResult()=0` | 📝 |
| B5 | null 参与计数 | [null,null,1]，n=1 | null（null 被当作一个值，且频次最高） | ❓ 与 Sum、DistinctCount 忽略 null 不一致 |
| B6 | 字符串 | ["a","b","b"] | "b" | ✅ |
| B6b | 非数字字符串的 `getDoubleResult` | ["a","b","b"] | 抛 NumberFormatException？返回 0？ | ⚠️ 代码为 `Double.valueOf((String) obj)` |
| B7 | 混合数值类型 | `addValue(1)` 与 `addValue(1.0)` | 视为同一值还是两个值 | ❓ |
| B8 | NULL 哨兵 | `addValue(Tool.NULL_INTEGER/DOUBLE/LONG/FLOAT/SHORT)` | 按 null 处理 | 📝 |
| B9 | n 非法 | 构造 `new NthMostFrequentFormula(0)`、`(-3)` | 回退为 n=1 | 📝 |
| B10 | 无参构造 | `new NthMostFrequentFormula()` 后加值取结果 | 无参构造 n 保持默认 0，`values.get(n-1)` 越界？ | ⚠️ 需确认 `Util.createFormula(null,"NthMostFrequent")` 是否走无参构造 |
| B11 | 克隆独立 | 克隆后各自加值 | 互不影响 | ✅ |
| B12 | Mode 等价 | `ModeFormula` 与 `NthMostFrequentFormula(1)` 同输入 | 结果相同；`getName()` 为 Mode 常量 | ✅ |
| B13 | 日期值 | 多个 Date，取最频繁 | 返回对应 Date | ✅ |

## C. `DistinctCountFormula`（`report/filter`）

新增：`report/filter/DistinctCountFormulaTest`。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| C1 | 基本去重 | [1,2,2,3] | 3，类型 `Integer` | ✅ |
| C2 | null 忽略 | [1,null,1,null] | 1 | 📝 |
| C3 | NULL 哨兵忽略 | `addValue(Tool.NULL_*)` | 不计入 | 📝 |
| C4 | 空集 | 无输入，`defaultResult=false` | 0（不是 null），`isNull()=false` | 📝 ❓ 与其他公式空集返回 null 不一致 |
| C5 | 区分大小写 | ["a","A"] | 2 | ✅ |
| C6 | 混合数值类型 | `addValue(1)` 与 `addValue(1.0)` | 1 个还是 2 个 | ❓ |
| C7 | 百分比，总数为 Integer | 去重数 2，total=4，百分比类型非 NONE | 0.5 | ✅ |
| C8 | 百分比，总数为 0 | total=0 | `defaultResult=false/true` 对应 null / 0.0 | 📝 |
| C9 | 百分比，总数为 Long | total 为 Long 4 | 当前只处理 Integer，Long 会被忽略并返回原始计数 | ⚠️ |
| C10 | 克隆独立 | 克隆后向克隆体加值 | 原对象不变 | ✅ |
| C11 | double[] 空数组 | `addValue(new double[0])` | 被忽略，不抛异常 | 📝 |

## D. `WeightedAverageFormula`（`report/filter`）

新增：`report/filter/WeightedAverageFormulaTest`。输入使用 `Object[]{值, 权重}`（`SummaryFilter`、`CrossTabFilter` 的实际调用方式）。

| ID | 场景 | 输入（值，权重） | 期望 | 标记 |
|---|---|---|---|---|
| D1 | 基本 | (10,1)、(20,3) | 17.5（70/4） | ✅ |
| D2 | 等权重等于平均 | (2,1)、(4,1)、(6,1) | 4.0 | ✅ |
| D3 | 值或权重为 null | (10,null)、(null,5)、(20,2) | 20.0（前两对被跳过） | ✅ |
| D4 | 权重和为 0 | (10,0)、(20,0)，`defaultResult=false/true` | null / 0.0；`getDoubleResult()=0` | 📝 |
| D5 | 负权重不抵消 | (10,2)、(20,-1) | 0.0（total=0，n=1） | ✅ |
| D5b | 负权重抵消到 0 | (10,1)、(20,-1) | 权重和为 0 → null / 0.0 | 📝 ❓ |
| D6 | 空集 | 无输入 | null / 0.0；`isNull()=true` | 📝 |
| D7 | 数字字符串 | {"10","2"} | 按数字解析（10×2/2=10.0） | 📝 |
| D8 | 非数字字符串 | {"abc","2"} | 该对被跳过 | 📝 |
| D9 | 单值方法 | `addValue(double/int/long/float/short)` | 抛 `RuntimeException("Unsupported method called!")` | 📝 记录契约 |
| D10 | 副列 | `new WeightedAverageFormula(3).getSecondaryColumns()` | [3] | ✅ |
| D11 | double[] 路径 | `addValue(new double[]{10,2})` | 与 Object[] 路径结果一致 | ✅ |
| D12 | 复用与克隆 | `reset` 后重算；克隆独立 | 正确 | ✅ |

## E. `CorrelationFormula`（`report/filter`）

新增：`report/filter/CorrelationFormulaTest`。输入使用 `Object[]{x, y}`。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| E1 | 完全正相关 | x=[1,2,3,4]，y=[2,4,6,8] | 1.0（容差 1e-12） | ✅ |
| E2 | 完全负相关 | x=[1,2,3,4]，y=[8,6,4,2] | -1.0 | ✅ |
| E3 | 已知值 | x=[1,2,3,4,5]，y=[2,1,4,3,5] | 0.8（Σdxdy=8，Σdx²=Σdy²=10） | ✅ |
| E4 | 一方方差为 0 | x 恒为常数 | `getResult()` 为 null（`defaultResult=true` 时 0.0）；`getDoubleResult()` 为 NaN？ | ❓ ⚠️ 两个方法对 NaN 的处理不一致 |
| E5 | 只有一对数据 | n=1 | 同 E4 | ❓ |
| E6 | 含 null 的数据对 | 一对含 null，其余正常 | 结果等于去掉该对后的结果 | ✅ |
| E7 | 空集 | 无输入 | null / 0.0；`isNull()=true` | 📝 |
| E8 | 大偏移数值稳定性 | x=[1e9+1,1e9+2,1e9+3]，y=[1,2,3] | 1.0（容差 1e-6） | ⚠️ 实现用 `n·Σx² − (Σx)²` 的一次遍历公式，大偏移下可能出现灾难性抵消，**预期可能失败，需运行确认** |
| E9 | double[] 路径下标约定 | `addValue(new double[]{x,y})`（长度 2） | 当前代码校验 `length<2` 却读取 `v[1]`、`v[2]`，与 `WeightedAverageFormula` 的 `v[0]`、`v[1]` 不一致 | ⚠️ ❓ `SummaryFilter`/`CrossTabFilter` 走 Object[] 路径，需确认 double[] 是否有调用方 |
| E10 | 混合数值类型 | Integer 与 Double 混用 | 按数值计算 | ✅ |
| E11 | 副列、复用、克隆 | `getSecondaryColumns`、`reset`、克隆独立 | 正确 | ✅ |

## F. 明细型计算字段（`ExpressionRef` 求值）

**第三阶段，且有前提：** 求值发生在 `AssetQuery`（约 5000 行）里按行执行脚本。测试入口（脚本环境、表的构造）需先参考 `EndUserScriptJavaAccessTest.calcFieldStillEvaluatesNormalScript`、`EmbeddedTableScriptLockCycleTest` 中 `formulaReads...` 的构造方式，**我尚未读这些测试的构造细节**。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| F1 | 直接取列 | `field['a']`，类型 integer、double、string、date、time、timeInstant、boolean 各一例（参数化） | 值与类型不变 | ✅ |
| F2 | 四则运算 | `field['a']/field['b']`，a=10，b=4 | 2.5 | ✅ |
| F3 | 声明为 integer 的除法 | 同上，声明类型 integer | 取整？保留 2.5？ | ❓ |
| F4 | 除零 | b=0 | Infinity？null？ | ❓ |
| F5 | null 操作数 | a=null 参与 `+`、`*` | null？NaN？0？ | ❓ |
| F6 | 字符串拼接 | `field['a']+"_"+field['b']`，a="x"，b="y" | "x_y" | ✅ |
| F7 | 拼接含 null | b=null | "x_null"？"x_"？null？ | ❓ |
| F8 | `concat()` 函数 | `concat(field['s'],'a')`，含 null | 同 F7 | ❓ |
| F9 | `CALC.quarter` | 2024-02-15 → `CALC.quarter(..)*2` | 2（第 1 季度 ×2） | ✅ |
| F10 | `CALC.edate` 月末 | 2024-01-31 加 1 个月；2023-01-31 加 1 个月；2024-03-31 减 1 个月 | 2024-02-29；2023-02-28；2024-02-29 | ✅ 按 Excel EDATE 语义；❓ 需确认产品遵循该语义 |
| F11 | `dateAdd('m',5,..)` 月末 | 同样的月末日期 | 同 F10 的月末处理 | ❓ |
| F12 | 引用参数 | `field['id']+parameter.RadioButton1` | 拼接参数值 | ✅ |
| F13 | 参数缺失 | 参数未提供 | 报错？按 undefined？ | ❓ |
| F14 | 引用另一个计算字段 | `field['calc_quarter']+field['CUSTOMER_ID']` | 按依赖顺序求值 | ✅ |
| F15 | 循环引用 | A 引用 B，B 引用 A | 明确的错误，而非死循环 | ❓ |
| F16 | 声明类型与结果不一致 | 声明 double，实际返回字符串 | 转换？报错？ | ❓ |
| F17 | 表达式运行时错误 | 引用不存在的列 | 错误如何呈现，是否影响其他行 | ❓ |

## G. 序列化与基础属性（`CalculateRef`、`ExpressionRef`）

新增：`uql/viewsheet/CalculateRefTest`。依据：`CalculateRef.java` 默认 `baseOnDetail=true`，**只在 false 时写出该属性**，解析时 `val==null || !"false".equals(val)`。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| G1 | 默认值 | `new CalculateRef(true)` 序列化 | 不写出 `baseOnDetail` 属性；重新解析后为 true | ✅ |
| G2 | 聚合型往返 | `new CalculateRef(false)` | 写出 `baseOnDetail="false"`；解析后为 false | ✅ |
| G3 | 解析旧格式 | XML 无该属性 | 解析为 true（基于明细） | ✅ 这正是本项目分析中曾读反的语义 |
| G4 | 解析其他取值 | `baseOnDetail="true"`、`"TRUE"`、`""` | 均为 true | 📝 |
| G5 | `getRefType` | 两种取值 | 聚合型带 `AGG_CALC` 标志，明细型不带 | ✅ |
| G6 | `equals` 与克隆 | 相同表达式、不同 `baseOnDetail` | 不相等；克隆保持属性 | ✅ |
| G7 | 表达式特殊字符 | 表达式含 `]]>`、引号、换行、中文 | CDATA 往返后一致 | ✅ 现有 `ScriptCdataRoundTripTest` 可能已覆盖部分，需确认 |

## I. 工厂与枚举一致性（元测试，一个用例）

新增：`report/internal/UtilCreateFormulaTest`。依据：`Util.createFormula` 对普通名字创建 `inetsoft.report.filter.<名字>Formula`。

| ID | 场景 | 输入 | 期望 | 标记 |
|---|---|---|---|---|
| I1 | 枚举全覆盖 | 遍历 `AggregateFormula` 的 26 个值（除 NONE、AGGREGATE） | `Util.createFormula(null, 名字)` 返回非 null，类型在 `inetsoft.report.filter` 包下，`getName()` 与枚举一致 | ✅ 同时验证"运行时走 report/filter" |
| I2 | 带参数的名字 | `NthLargest(2)`、`NthMostFrequent(2)`、`PthPercentile(50)`、`Correlation(3)`、`WeightedAverage(3)` | 参数被正确传入 | 📝 |
| I3 | 不带参数的名字 | `NthMostFrequent`、`NthLargest`、`PthPercentile` 无括号 | 结果不应在取值时越界 | ⚠️ 与 B10 相关，该用例可能暴露无参构造的 n=0 问题 |

## J. 已有公式的通用边界补充

对 Sum、Average、Max、Min、Variance、StandardDeviation、NthLargest、NthSmallest 做参数化补充。**现状：并列几乎没有，NaN/Infinity 完全没有。**

| ID | 场景 | 期望 | 标记 |
|---|---|---|---|
| J1 | NthLargest/NthSmallest 并列 | [5,5,3]，n=2：第 2 大是 5 还是 3 | ❓ 取决于是否去重 |
| J2 | 负数 | Sum/Max/Min/Average 对含负数的输入 | ✅ |
| J3 | 输入含 NaN | Sum、Average、Max、Min | ❓ |
| J4 | 输入含 ±Infinity | 同上 | ❓ |
| J5 | int 求和溢出 | 多个 `Integer.MAX_VALUE` 求和 | 结果类型与值 | ❓ |
| J6 | 大数据量精度 | 100 万个 0.1 求平均 | 接近 0.1（容差 1e-9） | ✅ |
| J7 | Variance 数值稳定性 | 大偏移小方差数据 | 与定义值一致（容差） | ⚠️ 同 E8 的算法风险 |
| J8 | 全部为同一个值 | Variance、StandardDeviation | 0.0 | ✅ |

## 2. 汇总

共 99 个用例（A 14、B 15、C 11、D 13、E 11、F 17、G 7、I 3、J 8）。下表按每个用例的**首个标记**统计（99 个用例各计一次）；另有 5 个用例同时带两个标记（如"📝 ❓"、"⚠️ ❓"）。

| 标记 | 数量 | 意义 |
|---|---|---|
| ✅ | 49 | 按定义可确定，可以直接写 |
| 📝 | 20 | 记录当前行为，需开发确认意图 |
| ❓ | 23 | 需要开发或产品决策后才能写期望值 |
| ⚠️ | 7 | 疑似缺陷，需运行确认，可能是我读错 |

按"是否带有该标记"统计则为：✅ 49、📝 20、❓ 27、⚠️ 8。

## 3. 需要开发回答的问题（按影响排序）

1. **null 的语义不统一：** `NthMostFrequent`/`Mode` 把 null 当作一个值参与计数；`DistinctCount`、`WeightedAverage`、`Correlation`、`Sum` 等则忽略 null。这是有意为之吗？（B5、C2）
2. **空集的返回值不统一：** `DistinctCount` 空集返回 0 且 `isNull()=false`；其他公式空集返回 null（除非 `defaultResult`）。（C4）
3. **除零与 NaN 的处理：** 聚合型表达式除零（A7）、相关系数方差为 0（E4，且 `getResult` 与 `getDoubleResult` 处理不一致）。
4. **并列取舍规则：** `NthMostFrequent`/`Mode` 并列时按值升序，这是有意的契约吗？（B2）
5. **混合数值类型（Integer 与 Double）是否视为同一值？**（B7、C6）
6. **`CorrelationFormula.addValue(double[])` 的下标约定**，以及这条路径是否有调用方。（E9）
7. **日期函数的月末语义**是否遵循 Excel（F10、F11）。

## 4. 本清单未覆盖

- 交互相关（筛选、参数联动、钻取、书签）。
- MV 路径（`mv/formula`），已有独立测试。
- 图表渲染、PNG。
- Chart 内部的二次聚合与排序分组（`GraphUtil` 对 correlation、covariance、weightedAvg 找不到副列时回退为 `AverageFormula`），需另行判断是否单测。
