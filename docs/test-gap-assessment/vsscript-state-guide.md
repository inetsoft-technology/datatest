# vsscript 状态断言测试：移交手册

> 给接手的人。读完这份应该能：跑起来、看懂已有代码、写出新用例、知道哪些坑已经踩过、知道下一步做什么。
> 方案与结论见 `vsscript-upgrade-plan.md`；缺口来源见 `vsscript.md`。
> 本手册里的代码都来自分支 `vsscript-state-assert-spike` 上跑通的原型，路径下文都相对 `vsscript/src/test/groovy/inetsoft/test/vsscript/state/`。

## 1. 这套东西解决什么

旧用例（`cases/`）只有"导出整页 PNG 再比像素"一条路。新的 `state/` 不导出 PNG，而是在测试进程里直接：
- 打开真实 vs、执行脚本、**读回**组件状态；
- 触发点击、**录下**服务端发出的消息；
- 读 Chart 的 `EGraph`、Crosstab 的数据；
- 导出 PDF/Excel 后解析内容。

**不需要启动任何服务，也不需要浏览器**，和旧用例一样在 Maven 里跑。

**和产品单测的分工（重要）：** 产品仓库已有单测专门测单个 `*VSAScriptable` 类的属性和全局函数（`report/script/viewsheet/` 369 个用例、`util/script/Calc*Test` 约 249 个）。这里**不要**写"逐属性 get/set"或"全局函数边界"，只写需要真实 vs 资产和完整运行时的场景。

## 2. 怎么跑

```
# 在 datatest 根目录。第一次或 commons 改动后先构建
./mvnw package -pl commons -DskipTests

# 跑全部新用例
./mvnw test -pl vsscript -Dtest='ScriptState*_Spec'

# 只跑一个 spec
./mvnw test -pl vsscript -Dtest=ScriptStateProbe_Spec -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
```
- 单个 spec 约 15 秒，三个共 30 个用例，本机全部通过（2026-11-13）。
- 看探路输出：`grep PROBE` 控制台输出（`ScriptStateProbe_Spec` 只打印不断言）。
- 测试的 vs 资产来自 `vsscript/src/test/resources/config/vsscriptStorage.zip`（130 个 vs）。资产 id 写法：`1^128^__NULL__^<zip 内路径>`，如 `1^128^__NULL__^ScriptAuto/OutPut/OutPut1`。找可用的 id：看 `cases/**/*_Spec.groovy` 里 `new VSScriptTest('…', caseName)` 的第一个参数，同时看该 spec 里的组件名。

**量覆盖率（JaCoCo，不改项目文件）：**
```
# 一次性：把 agent 和命令行工具下到任意临时目录（Maven Central，0.8.12）
./mvnw dependency:copy -Dartifact=org.jacoco:org.jacoco.agent:0.8.12:jar:runtime -DoutputDirectory=<DIR> -Dmdep.stripVersion=true
./mvnw dependency:copy -Dartifact=org.jacoco:org.jacoco.cli:0.8.12:jar:nodeps   -DoutputDirectory=<DIR> -Dmdep.stripVersion=true

# 跑测试时挂 agent（vsscript/pom.xml 的 argLine 写死，所以用环境变量，不用 -DargLine）
export JAVA_TOOL_OPTIONS="-javaagent:<DIR>/org.jacoco.agent-runtime.jar=destfile=<DIR>/x.exec,append=true,includes=inetsoft.report.script.*:inetsoft.util.script.*"
./mvnw test -pl vsscript -Dtest='ScriptState*_Spec'
unset JAVA_TOOL_OPTIONS

# 出报告（产品类文件用本机 Maven 仓库里的 jar；本机仓库在 D:/m2-1，不是 ~/.m2）
java -jar <DIR>/org.jacoco.cli-nodeps.jar report <DIR>/x.exec   --classfiles <M2>/com/inetsoft/stylebi/inetsoft-core/2.1.0-SNAPSHOT/inetsoft-core-2.1.0-SNAPSHOT.jar --csv <DIR>/x.csv
# 合并多次运行：java -jar ...cli-nodeps.jar merge a.exec b.exec --destfile all.exec
```
CSV 按 `PACKAGE`/`CLASS` 汇总 `LINE_COVERED`/`LINE_MISSED` 即可。结果与解读见方案文档 §11。

## 3. 代码结构

| 文件 | 作用 |
|---|---|
| `ScriptStateBase.groovy` | 测试基类/工具：开 vs、设脚本、读值、录命令刷新 |
| `RecordingDispatcher.groovy` | 记录所有 `sendCommand` 的 `CommandDispatcher`（commons 里的 `MessageTestUtils` 那个是 no-op，没改它） |
| `ScriptStateSpike_Spec.groovy` | 6 类方案各一个最小例子：状态断言、属性往返、差分、全局函数、点击+alert、PDF 导出 |
| `ScriptStateChartCrosstab_Spec.groovy` | Crosstab 与 Chart 的读回例子 |
| `ScriptStateProbe_Spec.groovy` | 探路：脚本异常、差分、EGraph、格式对象、属性枚举、Excel；只打印 `PROBE ...` |

**`ScriptStateBase` 的接口（`new ScriptStateBase('<资产id>')` 会打开 vs）：**

| 成员 | 用途 |
|---|---|
| `setScript(assembly, script)` | 给某组件设脚本并刷新（脚本在刷新时执行） |
| `setOnInit(script)` | 设 vs 的 `ONINIT` 脚本并刷新 |
| `eval(expr)` | 在 vs 脚本作用域里算一个表达式，返回解包后的值（数值是 `Double`） |
| `evalRaw(expr)` | 同上，但不解包 |
| `scope` | `ViewsheetScope`，可直接 `scope.execute(script, 组件名)` |
| `vs` / `rvs` / `res` / `principal` / `runtimeId` | 运行时对象，需要时直接用 |
| `refreshRecorded()` | 用录制 dispatcher 刷新，返回录到的命令 |

每个 spec 类头部必须带的注解（照抄即可，没有它 Spring 上下文起不来）：
```groovy
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(security = true, properties = [
   @SreeProperty(name = 'security.enabled', value = 'true'),
   @SreeProperty(name = 'security.login.orgLocation', value = 'domain')])
```

## 4. 写用例的配方

### 4.1 设脚本、读回状态
```groovy
def t = new ScriptStateBase('1^128^__NULL__^ScriptAuto/OutPut/OutPut1')
t.setScript('Text1', 'value = "hello"')
assert t.eval('Text1.value') == 'hello'
```

### 4.2 脚本是否报错（**先看 §5.1，刷新路径会吞异常**）
```groovy
// 直接执行才能看到异常
try { t.scope.execute('undefinedFn()', 'Text1') }
catch(Exception e) { /* ScriptException，e.cause.message 含 ReferenceError … */ }
```

### 4.3 点击 + 录消息
```groovy
def t = new ScriptStateBase(VS)
t.setScript('Text1', 'alert("clicked", "warning")')
def recorder = RecordingDispatcher.create(t.principal)
def ctx = ConfigurationContext.getContext()
// OnClickService 不是测试 Spring 上下文里的 Bean，手动构造；第二个参数 VSInputService 只用于表单提交，普通点击传 null
def service = new OnClickService(ctx.getSpringBean(CoreLifecycleService), null, ctx.getSpringBean(ViewsheetService))
MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
   service.onClick(t.runtimeId, 'Text1', null, null, null, 'http://localhost:8080/sree', t.principal, recorder)
}
def msgs = RecordingDispatcher.messages(recorder)
assert msgs[0].message == 'clicked' && msgs[0].type.toString() == 'WARNING'
```
- 只有可点击组件且 `isScriptEnabled()` 才会执行，否则**静默返回**，断言里一定要确认消息真的收到了。

### 4.4 Crosstab 数据与格式
```groovy
def t = new ScriptStateBase('1^128^__NULL__^ScriptAuto/Table/Table_Crosstab1')
t.eval('Crosstab1.data.length')          // 30.0
t.eval('Crosstab1.data[1][2]')           // 2.0
t.setScript('Crosstab1', 'cellFormat[2][2] = [StyleConstant.DECIMAL_FORMAT, "#,###.00"]')
t.eval('Crosstab1.cellFormat[2][2]')     // ExtendedDecimalFormat[#,###.00]，可 toString() 后断言
```

### 4.5 Chart：`bindingInfo` 与 `EGraph`
```groovy
def t = new ScriptStateBase('1^128^__NULL__^ScriptAuto/Chart/EGraph1')
t.setScript('Chart1',
  'dataset = [["State","Quantity"],["NJ",100],["NY",300]];\n' +
  'graph = new EGraph();\n' +
  'var elem = new PointElement("State","Quantity");\n' +
  'elem.setHint(GraphElement.HINT_ALPHA, "0.5");\n' +
  'graph.addElement(elem);\n' +
  'var scale = new LinearScale("Quantity"); scale.setMax(400);\n' +
  'graph.setScale("Quantity", scale);')
t.eval('Chart1.graph.getElementCount()')                              // 1.0
t.eval('Chart1.graph.getElement(0).getHint(GraphElement.HINT_ALPHA)') // 0.5
t.eval('Chart1.graph.getScale("Quantity").getMax()')                  // 400.0
```
- 绑定字段：**写**用 `bindingInfo.xFields = …`、`bindingInfo.setColorField(…)`；**读**用 `Chart1.bindingInfo.xFields`，但颜色字段要读 `Chart1.colorField`（`bindingInfo` 上没有读方法）。

### 4.6 差分：脚本路径 vs 静态路径
```groovy
t.setScript('Text1', 'autoSize = true')                                     // 脚本路径
t.vs.getAssembly('Text2').getVSAssemblyInfo().setAutoSizeValue(true)        // 静态路径：必须用 *Value setter
t.res.refreshViewsheet(t.principal)
assert t.eval('Text1.autoSize') == t.eval('Text2.autoSize')
```

### 4.7 导出
```groovy
// PDF：PDFBox 提取文字
file.withOutputStream { out ->
  t.res.exportVS(FileFormatInfo.EXPORT_TYPE_PDF, true, false, false, false, false,
                 ['(Home)'] as String[], false, false, null, new ExportResponse(out), t.principal) }
def text = PDDocument.load(file).withCloseable { new PDFTextStripper().getText(it) }

// Excel：没有 POI，用 JDK 读 xlsx（zip 里的 XML）
def zip = new ZipFile(file)
def sheet = zip.getInputStream(zip.getEntry('xl/worksheets/sheet1.xml')).getText('UTF-8')
def shared = zip.getInputStream(zip.getEntry('xl/sharedStrings.xml')).getText('UTF-8')
```
`FileFormatInfo` 常量：`EXPORT_TYPE_EXCEL=0`、`POWERPOINT=1`、`PDF=2`、`PNG=4`、`HTML=5`、`CSV=6`。

## 5. 已踩过的坑（别重复踩）

1. **刷新路径吞脚本异常。** 组件脚本、`ONINIT`、`ONREFRESH` 经刷新执行时，错误不抛、不发消息、测试里也没有日志（没有 SLF4J 绑定）；报错语句**之前**的效果仍然保留。要验证"脚本没报错"必须**直接** `scope.execute`。源码：`ViewsheetSandbox.java:1685` 对 `ConfirmException | ScriptException` 直接忽略。
2. **静态路径用 `*Value` setter。** `setAutoSize` 是运行时值，刷新后被重置；资产里存的静态值要用 `setAutoSizeValue`。
3. **读写入口不对称。** `bindingInfo` 只有 setter；`setPresenter` 没找到读回方式；`getPresenter(3,3)` 读取抛 `ScriptException`。先探路再写断言。
4. **数值是 `Double`。** `data.length` 读回 `30.0`，和整数比较要转。
5. **不要引用 logback 类。** 版本不匹配，`ClassNotFoundException: org.slf4j.spi.LoggingEventAware`，整个 spec 会挂。
6. **`OnClickService` 要手动构造**（见 §4.3）；表单提交需要 `VSInputService`，目前没接，是待办。
7. **脚本引擎是 Graal，不是 Rhino。** 枚举属性用 `getVSAScriptable(name).getMemberKeys()`，没有 `getIds()`。
8. **Excel 里 Text 是图片。** `xl/drawings` + `xl/media/*.png`，脚本改的 Text 值不在单元格里，要用表格类组件断言。
9. **vsscript 测试 classpath 上没有 Apache POI。** 本机 Maven 仓库在 `D:/m2-1`（不是 `~/.m2`），里面有 POI 5.4.0，加测试依赖应可行（未试）；也可以用 §4.7 的 zip 读法。
10. **bash 里别在 heredoc 里放复杂 Groovy。** 写测试文件用编辑器或 Write 工具。

## 6. 写完新用例的检查清单

1. 放在 `state/`，类名以 `_Spec` 结尾，带 §3 的注解。
2. 断言具体值，不要只断言"非空"。
3. **能失败：** 在产品里故意改坏对应 setter/映射（或把断言期望改错），确认用例变红再恢复。没做这步不要当作有效覆盖。
4. 不要重复单测方向（逐属性往返、全局函数边界）。
5. 需要像素才能验证的，放 `render/`（目录待建，见方案文档 §6），别放 `state/`。

## 7. 现状与下一步

**已完成：** 6 类方案的最小用例、Crosstab/Chart 例子、6 项探路，30 个用例全过（见方案文档 §5）。
**没做：** 批量覆盖（D1–D9 里除了原型示例以外的内容）；`render/` 目录；旧 `cases/` 的任何改动；CI 接入。
**下一步（按优先级，详见方案文档 §4、§8、§10）：**
0. 用 JaCoCo 量脚本相关类的覆盖率，得到"没覆盖"的客观清单（探路中）。资产里的脚本只有 200 个，D1 只是体检，不等于覆盖；资产不够时的补法见方案文档 §10（bug 驱动、生命周期矩阵、造资产）。
1. D1+D2：对 zip 里 130 个 vs 的所有脚本直接执行并统计 `ScriptException`；固定"刷新吞异常/点击抛异常"的行为。
2. D4：把现有 PNG 用例（Chart、Crosstab）改成状态断言版。
3. D3：事件端到端（`onSubmit` 需接 `VSInputService`）。
4. D6、D7、D5、D8、D9。

## 8. 待确认

- 刷新路径吞脚本异常是不是产品有意设计？（需开发回答）
- 直接执行脚本与刷新时执行的上下文是否一致（如 `onClick` 会设置 `setUseDValue`）？D1 会量一下。
- 产品里的 `ScriptStateLint` 能否复用？未查。
- CI 里是否收紧 `continue-on-error`；基线是否人工核对过；旧用例稳定多久后处理。
- 非标量属性（`highlighted` 内部、`dataConditions` 等）的读回方式，需逐个探。
