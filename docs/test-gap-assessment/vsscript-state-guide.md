# vsscript 状态断言测试：移交手册

> 给接手的人。读完这份应该能：跑起来、看懂已有代码、写出新用例、知道哪些坑已经踩过、知道下一步做什么。
> 方案与结论见 `vsscript-upgrade-plan.md`；缺口来源见 `vsscript.md`。
> 本手册里的代码都来自分支 `vsscript-state-assert-spike` 上跑通的原型，文件在 `vsscript/src/test/groovy/inetsoft/test/vsscript/` 下的三个目录里：`state/`（用例）、`support/`（共用工具）、`probe/`（探路类），下文文件名前写了目录。

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
./mvnw test -pl vsscript -Dtest=ScriptStateSpike_Spec -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false

# 探路类（名字不带 _Spec，默认不会跑，需要显式指定）
./mvnw test -pl vsscript -Dtest=ScriptStateProbe -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
```
- 单个 spec 约 15 秒。`state/` 下 6 个断言型 spec（`*_Spec`）共 **44 个用例，没有 `@PendingFeature`**（2026-11-18 清理了与 Java 重复的 16 个，见方案 §1a）。**注意：** #81530 的 5 个回归用例需要含 `f0aad94be6` 的 `inetsoft-core`；本机 Maven 仓库里的 `inetsoft-core` jar 早于它时这 5 个会红，重新 `install` core 后 0 失败（2026-11-17，已用该提交覆盖验证）。
- **探路类不带 `_Spec` 后缀，不属于测试套件。** `vsscript/pom.xml` 的 surefire 配置是 `<include>**/*_Spec</include>`，所以 `ScriptStateProbe`、`ScriptStateSubmitProbe` 不会被默认运行（它们只打印、不断言，放进套件只会虚增"绿"用例）。要看探路输出：显式 `-Dtest=ScriptStateProbe`，再 `grep PROBE`。
- **CI：** 这些 spec **没有单独接入 CI**。默认 `./mvnw test -pl vsscript` 会连同旧的 `cases/` 一起跑 `*_Spec`；评估文档记录 GitHub 工作流里 vsscript 步骤是 `continue-on-error: true`，所以即使失败也不会让流水线变红。是否单独收紧见方案 §7。
- **合并前自测：** `./mvnw package -pl commons -DskipTests`，再 `./mvnw test -pl vsscript -Dtest='ScriptState*_Spec'`。
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

# 出报告（产品类文件用本机 Maven 仓库里的 jar；本机仓库是 `~/.m2/settings.xml` 里的 `localRepository`，不一定是 ~/.m2）
java -jar <DIR>/org.jacoco.cli-nodeps.jar report <DIR>/x.exec   --classfiles <M2>/com/inetsoft/stylebi/inetsoft-core/2.1.0-SNAPSHOT/inetsoft-core-2.1.0-SNAPSHOT.jar --csv <DIR>/x.csv
# 合并多次运行：java -jar ...cli-nodeps.jar merge a.exec b.exec --destfile all.exec
```
CSV 按 `PACKAGE`/`CLASS` 汇总 `LINE_COVERED`/`LINE_MISSED` 即可。结果与解读见方案文档 §11。

## 3. 代码结构

三个目录，各管一类，不要混放：
- `state/`：**只放 `*_Spec`**，也就是用例。surefire 只运行 `**/*_Spec`（`vsscript/pom.xml`），所以只有这里的文件会跑进套件和 CI。
- `support/`：共用工具（`ScriptStateBase`、`RecordingDispatcher`），**不是用例**，spec 和探路类都用它们。新的共用方法加在这里；将来别的模块也要用时再考虑提到 `commons`（`commons` 改了要先 `./mvnw package -pl commons -DskipTests`）。
- `probe/`：探路类（`*Probe`），不带 `_Spec`，所以默认不跑，要 `-Dtest=<类名>` 显式指定；只打印、不断言，结论固化进 `state/` 的用例后可以删。

| 文件 | 作用 |
|---|---|
| `support/ScriptStateBase.groovy` | 测试基类/工具：开 vs、设脚本、读值、录命令刷新 |
| `support/RecordingDispatcher.groovy` | 记录所有 `sendCommand` 的 `CommandDispatcher`（commons 里的 `MessageTestUtils` 那个是 no-op，没改它） |
| `support/LogRecorder.groovy` | 记录产品的 WARN / ERROR 日志事件（`LogRecorder.events()`、`clear()`、`bound()`）。vsscript 的测试 classpath 上只有 slf4j-api 1.7 而没有绑定，产品的日志全被丢弃；`src/test/groovy/org/slf4j/impl/StaticLoggerBinder.groovy` 是 slf4j 1.7 的绑定，把日志路由到它（**这个文件在三个目录之外**，因为包名必须是 `org.slf4j.impl`）。没有加任何依赖 |
| `state/ScriptStateRefreshTripwire_Spec.groovy` | **被吞错误总检**：对测试存储里全部 vs（130 个）走真实打开 + 真实刷新，同时记录命令（`MessageCommand`）、线程的用户消息、WARN/ERROR 日志；出现白名单（`EXPECTED` / `OPTIONAL`）之外的事件，或白名单里的条目不再出现，都失败（白名单只能缩小）。已知 7 个 vs 有条目（脚本自己的 `alert`、`pviewsheet` 误报、两个资产配置警告、`Relation` 的 #81633），1 个可选（`ShapseApply` 缺形状图片）。用 `-Dtripwire.print=true` 打印当前全部事件，方便更新白名单 |
| `state/ScriptStateSpike_Spec.groovy` | 6 类方案各一个最小例子：状态断言、属性往返、差分、全局函数、点击+alert、PDF 导出 |
| `state/ScriptStateChartCrosstab_Spec.groovy` | Crosstab 与 Chart 的读回例子 |
| `probe/ScriptStateProbe.groovy` | **探路（不是测试套件）**：脚本异常、差分、EGraph、格式对象、属性枚举、Excel；只打印 `PROBE ...`，需 `-Dtest=ScriptStateProbe` 显式运行 |
| `state/ScriptStateSubmit_Spec.groovy` | Submit 组件（8 个用例，含 onClick 只执行一次）：`refreshAfterSubmit` 往返、提交应用输入值、对照用例、Submit 脚本能读到刚提交的值、`alert` 录到 2 次（已固定条数） |
| `probe/ScriptStateSubmitProbe.groovy` | **探路（不是测试套件）**：Submit 资产结构、`VSInputService` 依赖、读入口，只打印 |
| `state/ScriptStateKnownBugs_Spec.groovy` | 已修复的 Chart/Crosstab 脚本 bug 在真实资产上的回归：#80708/#80709（Chart 与 Crosstab 两半，#81530）、#80711，共 10 个用例。#81531、#81534、#81540、#81550 的用例（三件套：对照、\[bug present\]、`@PendingFeature`）已在 2026-11-18 删除：修复已合并并带了 Java 测试（`VSChartBindingScriptableTest`、`ChartConstantsScopeTest`、`AxisScriptableTest`、`RangeOutputVSAssemblyInfoTest`），core 含修复后这些用例会变红。**仍未修的 bug 如果只有真实 vs 才能显示，仍可用三件套的写法** |
| `state/ScriptStateChartScriptFixes_Spec.groovy` | 已在 stylebi 修好的 Chart 脚本 bug 的端到端回归（5 个用例）：#81032 的箱线图边框色（取真实 `VGraph` 绘制并数像素）、`y2Axis` 不泄漏到主度量 |
| `probe/ScriptStateHealthProbe.groovy` | **探路**：D1，对 130 个 vs 的所有脚本直接执行并统计 `ScriptException`（`-Dprobe.offset/limit` 可分段）。共用工具 `ScriptStateBase.viewsheetIds()` / `paint()` 在基类里 |
| `probe/ScriptStateBugHuntProbe.groovy` | **探路**（已精简为 3 个方法）：BH3 编辑器清单里的常量与类是否都有定义（需 `-Dprobe.lists=<json>`）、BH5 找有两个以上度量的图表、BH10 全局轴对象（`xAxis`/`yAxis`/`y2Axis`）写后读回矩阵。之前探到的结论都已固化进 spec |

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

### 4.8 表单提交（Submit）
```groovy
// VSInputService 只需要 3 个真实 Bean，其余 7 个编辑器依赖传 null
def ctx = ConfigurationContext.getContext()
def input = new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                               ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
def click = new OnClickService(ctx.getSpringBean(CoreLifecycleService), input, ctx.getSpringBean(ViewsheetService))

// InputValue / VSSubmitEvent 是接口，用 Map 强转
def items = [[assemblyName: { 'Spinner1' }, value: { 7 }] as InputValue] as InputValue[]
def ev = [values: { items }] as VSSubmitEvent
MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
   click.onClick(t.runtimeId, 'Submit1', null, null, ev, 'http://localhost:8080/sree', t.principal, recorder) }
assert t.eval('Spinner1.selectedObject') == 7d
```
完整用例见 `ScriptStateSubmit_Spec.groovy`。读回入口：TextInput 用 `value`，Spinner/Slider 用 `selectedObject`。

### 4.9 从 Redmine 取 bug 做回归（bug 驱动）
- 仓库里的 `.claude/workflow-shared/datatest-gap/redmine_fetch.py` 需要环境变量 `REDMINE_URL`、`REDMINE_KEY`，且只存标题等精简字段，**没有描述**。要拿描述，用 Redmine 的 `getIssues`（MCP 工具），参数 `subject: "~script"`（**必须带 `~`**）、`status_id: ["*"]`、`sort: "updated_on:desc"`、`limit`、`nometa: 1`，结果很大，要落盘后用脚本筛，别直接打印。
- 筛选条件：Bug、Resolved/Closed、描述里有复现脚本、有预期结果。
- 写法：脚本用 `t.scope.execute(script, '组件名')` 直接执行（刷新路径会吞异常）；bug 仍复现的用例标 `@PendingFeature(reason='Redmine #xxxx ...')`，并配一个正常路径的对照用例。**用例变成"意外通过"时 Spock 会报失败，那是该 bug 已修复的信号，去掉标记即可。**
- 先用探路 spec 打印实际结果，再写断言；读不到状态时用 Java 对象读（如 `t.vs.getAssembly('Chart1').getVSChartInfo().getXFields()[0].getDateLevelValue()`）。

## 5. 已踩过的坑（别重复踩）

1. **刷新路径吞脚本异常。** 组件脚本、`ONINIT`、`ONLOAD` 经刷新执行时，错误不抛、不发消息，只写服务端日志（`ViewsheetSandbox.updateAssembly` 忽略 `ScriptException`；`VSAScriptable.putMember` 捕获所有异常，只写 ERROR 日志，见 #81633）；报错语句**之前**的效果仍然保留。测试里的日志要用 `LogRecorder` 才看得到（见 §3），`ScriptStateRefreshTripwire_Spec` 就是这样盯整个存储的。要验证"脚本没报错"必须**直接** `scope.execute`。源码：`ViewsheetSandbox.java:1685` 对 `ConfirmException | ScriptException` 直接忽略。
2. **静态路径用 `*Value` setter。** `setAutoSize` 是运行时值，刷新后被重置；资产里存的静态值要用 `setAutoSizeValue`。
3. **读写入口不对称。** `bindingInfo` 只有 setter；`setPresenter` 没找到读回方式；`getPresenter(3,3)` 读取抛 `ScriptException`。先探路再写断言。
4. **数值是 `Double`。** `data.length` 读回 `30.0`，和整数比较要转。
5. **不要引用 logback 类，也不要给 vsscript 加 logback-classic / slf4j-api 2.x 这类测试依赖。** classpath 上是 slf4j-api 1.7.36，logback 1.3 以上需要 slf4j 2.x：直接引用会 `ClassNotFoundException: org.slf4j.spi.LoggingEventAware`；把 slf4j-api 2.0.x 加成依赖后，**干净构建时** `inetsoft-runner-maven-plugin:configure` 会失败（它把项目 classpath 加载到 Maven 自带的 slf4j 1.7 旁边，报 `Failed to get storage context: ... org/slf4j/LoggerFactory`）；`target/test-resources` 已存在时不会触发，所以在本机很容易漏掉。要看日志用 `LogRecorder`。若将来 classpath 上出现 slf4j 2.x，`StaticLoggerBinder` 会被忽略，`LogRecorder.bound()` 为 false，总检会直接失败而不是“盲过”。
6. **`OnClickService` 要手动构造**（见 §4.3）；表单提交要再带 `VSInputService`（见 §4.8）。
7. **脚本引擎是 Graal，不是 Rhino。** 枚举属性用 `getVSAScriptable(name).getMemberKeys()`，没有 `getIds()`。
8. **Excel 里 Text 是图片。** `xl/drawings` + `xl/media/*.png`，脚本改的 Text 值不在单元格里，要用表格类组件断言。
9. **vsscript 测试 classpath 上没有 Apache POI。** 本机 Maven 仓库（`~/.m2/settings.xml` 里的 `localRepository`，不一定是 `~/.m2`）里有 POI 5.4.0，加测试依赖应可行（未试）；也可以用 §4.7 的 zip 读法。
10. **一次提交里 Submit 的组件脚本的 `alert` 会被录到两次**（输入应用和刷新各执行一次，onClick 脚本只执行一次，见方案 §5.7），断言消息时用"包含"，别断言条数。
11. **bash 里别在 heredoc 里放复杂 Groovy。** 写测试文件用编辑器或 Write 工具。

## 6. 写完新用例的检查清单

1. 放在 `state/`，类名以 `_Spec` 结尾，带 §3 的注解。
2. 断言具体值，不要只断言"非空"。
3. **能失败：** 在产品里故意改坏对应 setter/映射，确认用例变红再恢复。没做这步不要当作有效覆盖。已有 6 个变异检查的结果见方案 §5.9。
   - **更安全的做法（2026-11-17 已用，不改本机 Maven 仓库）：** 把改过的 `.class` 编译（`javac -proc:none`，类路径取 `dependency:build-classpath` 的结果再加 `tern-annotations` jar）后放进 `vsscript/target/test-classes/`，它在类路径上排在 `inetsoft-core` jar 之前；用后删除并确认无残留。改动前先把未改源码重编译并与 jar 逐类 `javap -c` 比对，不一致就停。补丁要打在**用例实际走到的路径**上（`formatNumber` 曾因此漏报）。脚本未入库。
   - 旧工具（会替换本机 Maven 仓库里的 jar，被打断要手动恢复）：`.claude/workflow-shared/datatest-gap/vsscript_mutation_check.py`。它编译一个改坏的产品类，**临时替换本机 Maven 仓库里的 `inetsoft-core` jar**（先备份，每项后恢复，最后校验 sha256），跑指定 spec 并列出变红的用例。
   - 前置：产品源码要和 jar 一致；先生成 classpath：`./mvnw -o dependency:build-classpath -pl vsscript -Dmdep.outputFile=<文件>`；设置环境变量 `CP_FILE`（必填）、`M2_REPO`、`PRODUCT_CORE`、`JAVAC`（见脚本头部注释）。
   - **运行期间不要同时做别的构建；被中途杀掉时，把 `<MUT_WORK>_orig.jar` 拷回原 jar。**
   - 新增变异：在脚本的 `MUTATIONS` 里加一行（源文件、改动函数、spec、预期变红的用例）；改动函数必须只匹配到一处，否则脚本直接报错。
   - **注意：** 仓库里这份是把我实际跑通的脚本改成环境变量配置后的版本，**改造后没有整体重跑过**，只做了语法检查；第一次用请先看对照项是否全绿。
4. 不要重复单测方向（逐属性往返、全局函数边界）。
5. 需要像素才能验证的，放 `render/`（目录待建，见方案文档 §6），别放 `state/`。

## 7. 现状与下一步

**已完成：** 6 类方案的最小用例、Crosstab/Chart 例子、探路、Submit（`onSubmit`、onClick），被吞错误总检（`ScriptStateRefreshTripwire_Spec`，1 个用例遍历 130 个 vs），`_Spec` 断言型用例 59 个 + 总检 1 个（5 个 pending，已知 bug 仍存在，见方案 §5.8、§5.10）；探路类 4 个（不属于套件）；D1 已跑（无真实脚本错误）；24 个有效变异全部翻红（方案 §5.9）；已提 4 个单（方案 §5.10）。
**没做：** 批量覆盖（D1–D9 里除了原型示例以外的内容）；`render/` 目录；旧 `cases/` 的任何改动；CI 接入。
**下一步（按优先级，详见方案文档 §4、§8、§10）：**
0. JaCoCo 基线（方案 §11）、Submit、D1 已完成；D2（固定异常契约）未做。
1. 补漏扫描：取只修在 13.8 / wiz 线的脚本类 bug，在 stylebi 上复测（已提 4 个单，方法见方案 §5.10）。
2. 总检之外的补充：编辑器列出的名字与运行时的核对（已在 Java 层做过，见 #81635）、属性赋值的健壮性（见 #81633）；这两类**先看 Java 单测能不能做**，不能的才放这里。
3. D3 其余（`confirm` 取消分支、嵌套 vs 的 `onClick`、`parameter`）、D7。
4. D2 等开发给出语义；D4、D9 已决定不做；D5、D6、D8 先判断 Java 单测能不能覆盖。

**放哪里的规则（2026-11-18，领导定）：** 能用 Java 单测覆盖的就用 Java 单测；没有的先补；只有 Java 单测机制够不到的（需要真实打开的 vs 与资产、刷新/提交流程、真实绘图、对所有资产的检查）才放 datatest。

## 8. 待确认

- 刷新路径吞脚本异常是不是产品有意设计？（需开发回答）
- 直接执行脚本与刷新时执行的上下文是否一致（如 `onClick` 会设置 `setUseDValue`）？D1 已量：误报来自缺参数、内嵌 vs 缺父视图表（方案 §5.1）。
- 产品里的 `ScriptStateLint` 能否复用？未查。
- 提交流程里输入组件的组件脚本在"输入应用"和刷新各执行一次（Submit 自己的脚本关掉刷新后仍是 2 次），是否有意？（方案 §5.7）
- CI 里是否收紧 `continue-on-error`；基线是否人工核对过；旧用例稳定多久后处理。
- 非标量属性（`highlighted` 内部、`dataConditions` 等）的读回方式，需逐个探。
