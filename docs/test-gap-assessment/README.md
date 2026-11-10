# datatest 缺口评估

对 datatest 的各个模块评估：现有测试覆盖了什么、缺口在哪里、**Java 单测补哪些、dashboard 用例补哪些场景**。每个模块一个文件。

## 怎么用

在 Claude Code 里运行：

```
/datatest-gap-workflow <module>
```

一次评估一个模块，由你选择。产出就是下面状态表里的那个文件；只读，不落实。
工作流：`.claude/commands/datatest-gap-workflow.md`；规则、脚本、文件骨架：`.claude/workflow-shared/datatest-gap/`。

## 模块状态

| 模块 | 文件 | 状态 |
|---|---|---|
| vscalc（`vsothers`，`-Pvscalc`） | [vscalc.md](vscalc.md) | 已评估，待评审 |
| vspara（`vsothers`，`-Pvspara`） | [vspara.md](vspara.md) | 已评估，待评审 |
| vscrosstab、vsfreehand、otherassembly（`vsothers` 其他 profile） | | 未开始 |
| chart | | 未开始 |
| vsexport | | 未开始 |
| ws、olap、tabular、vpm、vsscript、css、mv | | 未开始 |
