# <module> 测试缺口与补充方案

> 日期：<YYYY-MM-DD>　模块：`<maven 模块>`，profile `-P<profile>`，spec `<spec 文件>`
> 产品源码分支：<branch>（可能与 datatest 实际使用的产品版本不同）

## 1. 结论

<3 到 5 句话：覆盖得怎么样、最大的缺口、先做什么。>

## 2. 覆盖情况

**规模：** <用例数、资产数、基线文件数；孤儿资产>

**覆盖矩阵**（维度：<维度 A> × <维度 B> × <维度 C>，已与用户确认）：

<紧凑表。每个数字来自 vs_backup_scan.py 的输出，不手填、不写"约"。>

**验证机制：**

- <比对遍历"实际导出"还是"基线清单"：file:line>
- <导出失败是否让用例失败：file:line>
- <exportData 是否清理、CI 命令是否带 clean>
- <严格版基类的使用比例>
- <基线是回归基线还是独立标准答案；没查就写"未查">

**断言层：** <哪些组件或对象不导出数据，只能靠整页 PNG；关键对象是否出现在基线>

## 3. 缺口与方向

| # | 缺口 | 会放过什么错误 | 方向 | 优先级 |
|---|---|---|---|---|
| G1 | | <具体的错误行为，说不出就不是缺口> | 单测 / dashboard / 机制 | 高 |

方向判断：期望值能不依赖产品输出、独立算出来 → 单测；问题出在组件、绑定、数据源之间的接线 → dashboard；验证机制本身的缺陷 → 改基类，不是加用例。

## 4. Java 单测补充

| 被测类 | 补什么 | 用例数 | 难度 | 待开发确认 |
|---|---|---|---|---|

难度：低 = 纯单元；中 = 需要脚本或沙箱环境；高 = 测试入口还没确认。用例数是设计出来的数量，不是已写好的。

## 5. Dashboard 补充

| 场景 | 做法（新建 vs / 扩展 / 改基类） | 验证什么 | 前提 |
|---|---|---|---|

每个新用例只验证"接线"，边界和语义放进第 4 节的单测。

## 6. 局限与待确认

- <没验证的、静态推断的>
- <需要开发回答的问题>
- <需要用户决定的事>

## 附：数据来源与复现

```
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py inventory <zip>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py crosscheck <zip> <Spec.groovy> <baseline-dir>
python .claude/workflow-shared/datatest-gap/vs_backup_scan.py strictcheck <zip> <Spec.groovy> <baseline-dir>
```
