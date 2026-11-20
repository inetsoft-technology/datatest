# vsscript：没被任何测试跑到的脚本方法（JaCoCo，2026-11-18）

> 目的：用覆盖率找“还没人测”的脚本代码，作为补 Java 单测、补 datatest 的清单。放置规则见 [vsscript-upgrade-plan.md](vsscript-upgrade-plan.md) §2：**能 Java 的就 Java**。本文件只列事实和建议，不是已做的工作。

## 1. 做法和口径

- **Java 一侧**：`community/core` 全部单测（`@Tag("core")`，27593 个测试，1 个失败，178 个跳过）在 `origin/main`（`1b22c8451e` 之后）上带 JaCoCo 0.8.12 agent 运行；agent 只统计 `inetsoft.report.script.*`、`inetsoft.util.script.*`、`inetsoft.web.binding.*`。类文件用 `core/target/classes`。
- **datatest 一侧**：`vsscript` 全模块（159 个用例：115 个旧 PNG 用例 + 44 个 state 用例，本机其中 119 个失败，但脚本照样执行，统计的是“跑到”，不是“断言通过”）带同一个 agent；类文件用本机 Maven 仓库里的 `inetsoft-core-2.1.0-SNAPSHOT.jar`（早于 main 上最近几天的修复）。两边分别分析，按“类 + 方法 + 签名”合并；方法在任一边有指令被执行就算覆盖。
- **局限**：只统计这三个包；行覆盖不等于逻辑覆盖；jar 与 main 之间改过的方法可能算偏；统计不含 lambda 和合成方法；`Thermometer` / `Cylinder` / `SlidingScale` 三个已废弃组件不计；报表（report）时代的脚本类在 `report.script (other)` 里，对 vs 脚本价值有限。
- 生成：`JcReport.java`（按方法输出 CSV）+ 合并脚本，放在会话临时目录，没有进仓库。

## 2. 汇总（按方法数和行数）

| 区域 | 方法数 | Java 覆盖的方法 | datatest 覆盖的方法 | **两边都没有** | 行数 | Java 行覆盖 | datatest 行覆盖 | 两边都没有的行 |
|---|---|---|---|---|---|---|---|---|
| report.script.viewsheet | 940 | 706 | 533 | **206** | 5417 | 72.5% | 54.6% | 844 |
| report.script (other) | 865 | 681 | 432 | **157** | 6609 | 78.1% | 46.9% | 500 |
| util.script.graal (engine) | 924 | 824 | 474 | **99** | 5220 | 89.4% | 52.2% | 200 |
| util.script Calc* (functions) | 316 | 306 | 23 | **10** | 2898 | 95.3% | 5.3% | 11 |
| util.script (other) | 240 | 205 | 121 | **33** | 1801 | 87.3% | 40.3% | 87 |

读法：Java 单测覆盖了 72–95% 的行，datatest 的现有用例只覆盖 5–55%，且大多是 Java 也覆盖了的；两边合起来，`report.script.viewsheet` 里没有被任何测试跑到的是 206 个方法、844 行。

## 3. `report.script.viewsheet`（vsscript 的主体）：201 个方法、829 行没有被任何测试跑到（去掉 lambda 和废弃组件）

“小方法”列是行数 <3 或 `getClassName`、`<init>`、`getMemberKeys` 这类平凡方法的个数，不单列。

| 类 | 方法数 | 行数 | 主要未覆盖的方法（行数） | 小方法 |
|---|---|---|---|---|
| `report.script.viewsheet.CallableStatementScriptable` | 53 | 387 | `addFunctions`(90), `registerOutParameter`(19), `setFloat`(10), `setShort`(10), `setBigDecimal`(9), `setByte`(9), `setLong`(9), `setTime`(8), `setTimestamp`(8), `setObject`(7), `setObject`(7), `setObject`(7), `executeQuery`(6), `getBigDecimal`(6), `getBoolean`(6), `getByte`(6), `getBytes`(6), `getClob`(6), `getDate`(6), `getDouble`(6), `getFloat`(6), `getInt`(6), `getLong`(6), `getNClob`(6), `getNString`(6), `getObject`(6), `getRef`(6), `getRowId`(6), `getShort`(6), `getSQLXML`(6), `getString`(6), `getTime`(6), `getTimestamp`(6), `setBoolean`(6), `setDate`(6), `setDouble`(6), `setInt`(6), `setNull`(6), `setNull`(6), `setString`(6), `numberToString`(3), `getDateFromObj`(3) | 11 |
| `report.script.viewsheet.PreparedStatementScriptable` | 26 | 140 | `setBinaryStream`(8), `setBigDecimal`(7), `setByte`(7), `setFloat`(7), `setLong`(7), `setShort`(7), `setTimestamp`(7), `addBatch`(6), `clearParameters`(6), `setBoolean`(6), `setDate`(6), `setDouble`(6), `setNull`(6), `setNull`(6), `setObject`(6), `setObject`(6), `setObject`(6), `setString`(6), `setTime`(6), `execute`(4), `executeBatch`(4), `numberToString`(3), `getDate`(3) | 3 |
| `report.script.viewsheet.ViewsheetVSAScriptable` | 4 | 32 | `refresh`(23), `collectNestedViewsheets`(5), `getNestedViewsheets`(3) | 1 |
| `report.script.viewsheet.DBScriptable` | 9 | 31 | `commit`(10), `rollback`(8), `close`(5) | 6 |
| `report.script.viewsheet.VSChartBindingScriptable` | 3 | 24 | `getDimensions`(22) | 2 |
| `report.script.viewsheet.TableDataVSAScriptable` | 5 | 22 | `getHiddenColumnValue`(14), `getFormRow`(5) | 3 |
| `report.script.viewsheet.ParamScriptable` | 5 | 22 | `getMemberKeys`(9), `getMember`(8), `hasMember`(3) | 2 |
| `report.script.viewsheet.VSAScriptable` | 10 | 19 | `addProperty`(5), `clone`(4) | 8 |
| `report.script.viewsheet.SelectionVSAScriptable` | 7 | 13 | `setSingleSelection`(5) | 6 |
| `report.script.viewsheet.TableDataVSAScriptable$TableFormRowScriptable` | 2 | 13 | `getMember`(10), `getArrayElement`(3) | 0 |
| `report.script.viewsheet.VSAScriptable$TipDataCondition` | 6 | 13 | `getMember`(5) | 5 |
| `report.script.viewsheet.OutputHighlightedArray` | 8 | 10 | — | 8 |
| `report.script.viewsheet.ViewsheetScope` | 2 | 10 | `createConnection`(8) | 1 |
| `report.script.viewsheet.VSAScriptable$TipDataConditionArray` | 7 | 9 | — | 7 |
| `report.script.viewsheet.ConfirmEventScriptable` | 5 | 9 | `putMember`(3), `getMember`(3) | 3 |
| `report.script.viewsheet.ChartVSAScriptable` | 7 | 9 | — | 7 |
| `report.script.viewsheet.TableDataVSAScriptable$CalcWriteTableLens` | 2 | 8 | `setObject`(6) | 1 |
| `report.script.viewsheet.CubeVSAScriptable` | 3 | 8 | `getMember`(6) | 2 |
| `report.script.viewsheet.VSCrosstabBindingScriptable` | 3 | 5 | — | 3 |
| `report.script.viewsheet.ShapeVSAScriptable` | 2 | 4 | — | 2 |
| `report.script.viewsheet.PViewsheetScriptable` | 3 | 4 | — | 3 |
| `report.script.viewsheet.PViewsheetScriptable$AttributeScriptable` | 3 | 4 | — | 3 |
| `report.script.viewsheet.GraphCreator` | 1 | 4 | `getOriginalDataSet`(4) | 0 |
| `report.script.viewsheet.VSEGraphScriptable` | 3 | 3 | — | 3 |
| `report.script.viewsheet.RangeOutputVSAScriptable` | 2 | 3 | — | 2 |
| `report.script.viewsheet.VSPropertyDescriptor` | 1 | 3 | — | 1 |
| `report.script.viewsheet.ChartVSAScriptable$ChartArrayImpl` | 3 | 3 | — | 3 |
| `report.script.viewsheet.ChartVSAScriptable$1` | 2 | 2 | — | 2 |
| `report.script.viewsheet.VSTableLayoutInfo` | 2 | 2 | — | 2 |
| `report.script.viewsheet.CallableStatementScriptable$1` | 2 | 2 | — | 2 |
| `report.script.viewsheet.TableDataVSAScriptable$TableFilter2` | 1 | 2 | — | 1 |
| `report.script.viewsheet.ChartVSAScriptable$3` | 1 | 1 | — | 1 |
| `report.script.viewsheet.InputVSAScriptable` | 1 | 1 | — | 1 |
| `report.script.viewsheet.DataVSAScriptable` | 1 | 1 | — | 1 |
| `report.script.viewsheet.CalendarVSAScriptable` | 1 | 1 | — | 1 |
| `report.script.viewsheet.VSChartArray` | 1 | 1 | — | 1 |
| `report.script.viewsheet.OutputVSAScriptable` | 1 | 1 | — | 1 |
| `report.script.viewsheet.VSChartBindingScriptable$3` | 1 | 1 | — | 1 |
| `report.script.viewsheet.TabVSAScriptable` | 1 | 1 | — | 1 |
| `report.script.viewsheet.CrosstabVSAScriptable` | 1 | 1 | — | 1 |

## 4. `util.script.graal`（Graal 脚本引擎）：87 个方法、181 行

| 类 | 方法数 | 行数 | 主要未覆盖的方法（行数） | 小方法 |
|---|---|---|---|---|
| `util.script.graal.pool.CopyList` | 13 | 29 | `add`(3), `clear`(3), `replaceAll`(3), `removeRange`(3) | 9 |
| `util.script.graal.pool.CopyMap` | 11 | 25 | `putAll`(3), `clear`(3), `replaceAll`(3) | 8 |
| `util.script.graal.ScriptTimeoutGuard` | 5 | 22 | `liveFrames`(7), `stopWatchdogForTest`(6), `tracks`(5) | 2 |
| `util.script.graal.GraalJavaScriptEnv` | 9 | 20 | `closeQuietly`(5), `setSQL`(4) | 7 |
| `util.script.graal.BindingRootProxy` | 4 | 16 | `enumerate`(11), `assigned`(3) | 2 |
| `util.script.graal.ScriptTimeoutGuard$Watchdog` | 1 | 8 | `logFailure`(8) | 0 |
| `util.script.graal.pool.WorksheetScriptEnv` | 6 | 8 | — | 6 |
| `util.script.graal.pool.PoolMetrics` | 7 | 7 | — | 7 |
| `util.script.graal.ArrayProxy` | 2 | 6 | `getMemberKeys`(5) | 1 |
| `util.script.graal.pool.Slot` | 1 | 5 | `closeQuietly`(5) | 0 |
| `util.script.graal.JavaClassProxy` | 2 | 4 | `putMember`(3) | 1 |
| `util.script.graal.LegacyJavaShimPluginListener` | 2 | 3 | — | 2 |
| `util.script.graal.BindingRootProxy$DeclaredVarView` | 2 | 3 | — | 2 |
| `util.script.graal.ReadOnlyPrincipalProxy` | 3 | 3 | — | 3 |
| `util.script.graal.GraalJavaScriptEngine$1` | 3 | 3 | — | 3 |
| `util.script.graal.JavaPackageProxy` | 2 | 2 | — | 2 |
| `util.script.graal.pool.WsEngine` | 1 | 2 | — | 1 |
| `util.script.graal.ScriptTimeoutGuard$TokenGuard` | 1 | 2 | — | 1 |
| `util.script.graal.pool.SlotPool` | 1 | 2 | — | 1 |
| `util.script.graal.GraalJavaScriptEngine` | 1 | 1 | — | 1 |
| `util.script.graal.pool.ForeignRef$Array` | 1 | 1 | — | 1 |
| `util.script.graal.LegacyJavaShim` | 1 | 1 | — | 1 |
| `util.script.graal.OwnedVarScope$1` | 1 | 1 | — | 1 |
| `util.script.graal.HostBeanProxy` | 1 | 1 | — | 1 |
| `util.script.graal.pool.OwnedValueCodec$Home` | 1 | 1 | — | 1 |
| `util.script.graal.pool.ForeignRef$Function` | 1 | 1 | — | 1 |
| `util.script.graal.pool.ForeignRef` | 1 | 1 | — | 1 |
| `util.script.graal.pool.SlotTenant` | 1 | 1 | — | 1 |
| `util.script.graal.ScriptHostAccess` | 1 | 1 | — | 1 |
| `util.script.graal.ScriptTimeoutGuard$Guard` | 1 | 1 | — | 1 |

## 5. `report.script` 其余部分（报表时代，其中 `formula`、`TableArray` 等 vs 也用）：151 个方法、475 行

只列行数最多的 12 个类，完整数据在合并结果里。

| 类 | 方法数 | 行数 | 主要未覆盖的方法（行数） | 小方法 |
|---|---|---|---|---|
| `report.script.viewsheet.CallableStatementScriptable` | 53 | 387 | `addFunctions`(90), `registerOutParameter`(19), `setFloat`(10), `setShort`(10), `setBigDecimal`(9), `setByte`(9), `setLong`(9), `setTime`(8), `setTimestamp`(8), `setObject`(7), `setObject`(7), `setObject`(7), `executeQuery`(6), `getBigDecimal`(6), `getBoolean`(6), `getByte`(6), `getBytes`(6), `getClob`(6), `getDate`(6), `getDouble`(6), `getFloat`(6), `getInt`(6), `getLong`(6), `getNClob`(6), `getNString`(6), `getObject`(6), `getRef`(6), `getRowId`(6), `getShort`(6), `getSQLXML`(6), `getString`(6), `getTime`(6), `getTimestamp`(6), `setBoolean`(6), `setDate`(6), `setDouble`(6), `setInt`(6), `setNull`(6), `setNull`(6), `setString`(6) | 13 |
| `report.script.viewsheet.PreparedStatementScriptable` | 26 | 140 | `setBinaryStream`(8), `setBigDecimal`(7), `setByte`(7), `setFloat`(7), `setLong`(7), `setShort`(7), `setTimestamp`(7), `addBatch`(6), `clearParameters`(6), `setBoolean`(6), `setDate`(6), `setDouble`(6), `setNull`(6), `setNull`(6), `setObject`(6), `setObject`(6), `setObject`(6), `setString`(6), `setTime`(6) | 7 |
| `report.script.TableColumns` | 9 | 47 | `getMember`(12), `putMember`(11), `getMemberKeys`(10), `getArrayElement`(7) | 5 |
| `report.script.graal.ReportGraalJavaScriptEngine` | 9 | 45 | `addParameterGlobal`(23), `isDateArray`(7), `runQueryGlobal`(5) | 6 |
| `report.script.ProxyScriptable` | 9 | 41 | `putMember`(12), `init`(8), `getMember`(8) | 6 |
| `report.script.viewsheet.ViewsheetVSAScriptable` | 4 | 32 | `refresh`(23), `collectNestedViewsheets`(5) | 2 |
| `report.script.viewsheet.DBScriptable` | 9 | 31 | `commit`(10), `rollback`(8), `close`(5) | 6 |
| `report.script.formula.CalcCellScope` | 8 | 27 | `getMemberKeys`(7), `getMember`(5) | 6 |
| `report.script.viewsheet.VSChartBindingScriptable` | 3 | 24 | `getDimensions`(22) | 2 |
| `report.script.TableRowScope` | 5 | 24 | `parkShared`(20) | 4 |
| `report.script.ChartProcessor` | 3 | 23 | `addBCTarget`(18) | 2 |
| `report.script.viewsheet.TableDataVSAScriptable` | 5 | 22 | `getHiddenColumnValue`(14), `getFormRow`(5) | 3 |

## 6. 怎么处理（按 §1a 的顺序：先看 Java 能不能做）

| 组 | 内容 | 行数（约） | 放哪里 | 依据 / 备注 |
|---|---|---|---|---|
| A | 数据库脚本类：`CallableStatementScriptable`、`PreparedStatementScriptable`、`DBScriptable`、`ViewsheetScope.createConnection` | 560（占 vs 部分未覆盖的 2/3） | **Java**（内存 H2） | core 测试里有 58 个文件用 `jdbc:h2`；已有的 `ScriptJdbcAccessEnginesTest`、`ScriptJdbcLiveAccessEnginesTest` 只看引擎访问限制，没有调这些类的方法 |
| B | 小的脚本对象：`ParamScriptable`（`param`）、`ConfirmEventScriptable`（`confirmEvent`）、`PViewsheetScriptable`（`pviewsheet`，含 `AttributeScriptable`）、`VSAScriptable.TipDataCondition(Array)`、`OutputHighlightedArray`、`VSEGraphScriptable` | 约 90 | **Java**（mock 的 sandbox，现有 `*VSAScriptableTest` 的写法） | 测试里没有任何一处按名字提到 `ParamScriptable`、`ConfirmEventScriptable`、`PViewsheetScriptable`；这几个对象是 §1 的“Java 没补”清单里的 `param`、`confirmEvent` |
| C | 组件和绑定的方法：`VSChartBindingScriptable.getDimensions`(22)、`TableDataVSAScriptable.getHiddenColumnValue`(14) / `getFormRow` / `TableFormRowScriptable`、`SelectionVSAScriptable.setSingleSelection`、`VSAScriptable.addProperty` / `clone` / `removeProperty`、`ChartVSAScriptable.addTarget` / `setTrendLineExcludedMeasures` | 约 100 | **Java**（mock） | 同上 |
| D | `ViewsheetVSAScriptable.refresh`(23) / `collectNestedViewsheets` / `getNestedViewsheets`（vs 级对象 `thisViewsheet` 的刷新和嵌套 vs） | 32 | 单个对象能用 Java；**嵌套真实刷新要 datatest + 新资产** | 嵌套两层的 embedded 仪表板已记在方案 §12 的 TODO |
| E | 引擎：`CopyList`、`CopyMap`（集合包装）、`BindingRootProxy.enumerate`(11)、`GraalJavaScriptEnv` 的 `closeQuietly` / `setSQL` / `getIds` 等 | 约 130 | **Java** | `ScriptTimeoutGuard` 的 `liveFrames` / `stopWatchdogForTest` 等是测试钩子，不算 |
| F | `util.script.Calc*`（函数库）只剩 10 个平凡方法 | 11 | 不处理 | Java 覆盖 95%，是函数库里做得最好的一块 |

**优先级建议**（都是 Java）：B 和 C 先做，量小、风险低、直接是脚本作者会用到的 API（`param`、`confirmEvent`、表单行、`getDimensions`、`setSingleSelection`）；A 量最大（560 行），但要搭 H2 数据源，放在其后；E 是引擎内部的集合包装，价值较低；D 的嵌套部分等资产。

**这份清单发现问题的方式**（对 #81430 的意义）：它是“没有被跑到的代码”，不是“有 bug 的代码”；补测试时可能发现新的 bug，也可能发现某些方法是死代码（例如 Rhino 时代遗留的 `getClassName`、`TipDataCondition`）。每补一批要重新量一次，记录增量。
