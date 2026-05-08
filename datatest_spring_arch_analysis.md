# DataTest Spring 架构分析报告

## 背景

InetSoft 已将 SingletonManager 和静态工厂方法迁移为 Spring 管理的单例状态。静态访问器现在通过 `ConfigurationContext.getSpringBean()` 委托到 Spring context。

## 历史症状（Step 1 前）

运行 VPM 测试时报错：
```
inetsoft.util.MessageException: Read access denied: VPM1_Model_1
  at AbstractAssetEngine.checkAssetPermission
  at WorksheetEngine.openSheet
  at ViewsheetEngine$OpenViewsheetTask.doOpenViewsheet
```

Trace 显示：
- `security.enabled SreeEnv.getProperty()=null`
- `SecurityEngine.isSecurityEnabled()=false`
- `checkPermission(guest)=false` → 权限拒绝

## 根本原因分析

### 调用链

```
SreeEnv.getProperty("security.enabled")
  → PropertiesEngine.getInstance()
    → ConfigurationContext.getSpringBean(PropertiesEngine.class)  ✓ Spring bean
      → getUserEnhancedProperties() → init() → loadFromStorage(kvStorage)
        → kvStorage.get("security.enabled") = null  ← 数据不在 KV 里
```

- `PropertiesEngine.initEngine()` 是 `@PostConstruct`，设置 `kvStorage = keyValueStorageManager.getStorage("sreeProperties")`
- `getPropertyFromStorage("security.enabled")` 直接读 `kvStorage` → null
- 结论：`sreeProperties.db` 里真的没有 `security.enabled=true`（尽管 backup zip 有）

### 为什么 sreeProperties 为空

runner plugin 在 `generate-test-resources` 阶段写入 `sreeProperties.db`，backup zip 的 `key-value-index.json` 确认有该字段。但 Spring runtime 读不到，可能原因：
1. runner plugin 写入时使用了 org-scoped key（如 `inetsoft.org.host-org.security.enabled`）
2. runner plugin 写入路径与 Spring 读取路径不一致

## 架构层面的历史矛盾

### 双实例问题

```
┌─────────────────────────────────────────────────────┐
│  DatatestRuntimeBootstrap（Spring context）          │
│  DatatestBaseConfiguration + IntegrationTestConfig   │
│  已提供：ViewsheetService, SecurityEngine,           │
│           PropertiesEngine, KV storage...            │
└──────────────────┬──────────────────────────────────┘
                   │ ConfigurationContext 绑定
                   ▼
┌─────────────────────────────────────────────────────┐
│  ControllersResource.createControllers()（480行）    │
│  手动 new / mock 所有 controller/service             │
│  与 Spring context 中的 bean 形成两套对象             │
└─────────────────────────────────────────────────────┘
```

| 对象 | Spring context | ControllersResource | 实际用哪个 |
|---|---|---|---|
| `ViewsheetService` | ✓ | `ViewsheetEngine.getViewsheetEngine()` | 同一个（静态→Spring） |
| `SecurityEngine` | ✓ | `SecurityEngine.getSecurity()` | 同一个 |
| `LicenseManager` | ✓ mock | **另一个 mock 实例** | 不同实例！ |
| `XSessionService` | ✓ real | `mock(XSessionService.class)` | 不同实例！ |
| `CoreLifecycleService` | ✓ Spring | `new CoreLifecycleService(...)` | 不同实例！ |

`IntegrationTestConfiguration` 已经提供了 ControllersResource 手动创建的所有 bean。

## 修复方案对比

### Option 1: Fix Current（只修 security.enabled）

在 `DatatestRuntimeBootstrap.bootstrap()` 末尾添加 `alignSreeEnvAfterBootstrap()`：
- 强制重载 PropertiesEngine（`pe.init(true)`）
- 如仍为 null，从 JVM 系统属性 fallback
- pom.xml 的 `systemPropertyVariables` 添加 `security.enabled` 作为保底

| 维度 | 评估 |
|---|---|
| 工作量 | 极小（< 1小时）|
| 修复当前 VPM 错误 | ✓ |
| 未来风险 | 高：ControllersResource 会随 InetSoft 版本持续 break |
| 根本问题 | 未解决 |

### Option 2: Refactor（对齐新 Spring 架构）

`ControllersResource.initControllers()` 改为从 Spring context 取 bean：

```java
public void initControllers() {
    ConfigurationContext ctx = ConfigurationContext.getContext();
    this.openViewsheetController = ctx.getSpringBean(OpenViewsheetController.class);
    this.vsLifecycleService      = ctx.getSpringBean(VSLifecycleService.class);
    // ... 其余从 Spring context 取
}
```

| 维度 | 评估 |
|---|---|
| 工作量 | 中等（1-2天）|
| 修复当前 VPM 错误 | ✓ 顺带解决 |
| 未来风险 | 低 |
| 代码量变化 | 历史手动装配 480 行已收敛为 Spring façade；最终迁移引用后删除 façade |
| 对其他模块影响 | 需逐一迁移引用并验证，不能直接删除 |

## 历史决定

**两步走：**
- **Step 1（立即）**：`alignSreeEnvAfterBootstrap` + pom.xml fallback，让 VPM 测试先通过
- **Step 2（计划内）**：重构 ControllersResource 使用 Spring context，消除双实例问题

### 2026-05-08 核验结论（Step 7 后）

Layer2 的目标已经落地到当前阶段：datatest controller/service 获取路径已统一到 Spring context，`ControllersResource` 兼容层已删除；手动启动入口 `DatatestRuntimeBootstrap` 也已删除。

当前代码状态：

- `rg -n "ControllersResource|initControllers\(" commons ws vsothers vpm chart css vsscript vsexport mv -g "*.java" -g "*.groovy"` 对代码目录无命中。
- `rg -n "DatatestRuntimeBootstrap" commons ws vsothers vpm chart css vsscript vsexport mv -g "*.java" -g "*.groovy"` 对代码目录无命中。
- `RuntimeViewsheetResource.java` 仍保留，且当前只作为 datatest/Spock helper 被 `commons` 下的 Groovy helper 使用。
- 当前 datatest 代码没有引入产品侧 `RuntimeViewsheetExtension` 或 `@RegisterExtension`。
- `DatatestBaseConfiguration` 不能按早期清单删除。产品侧 `BaseTestConfiguration` 会把 KV 切到 in-memory `test` engine；datatest 需要读取 runner plugin 写入的 MapDB KV，因此仍要保留当前 `DatatestBaseConfiguration` 的 MapDB 恢复逻辑。
- 产品侧 `RuntimeViewsheetExtension` 是 JUnit Jupiter 的 `@RegisterExtension` 字段扩展，并不是参数注入式 `ParameterResolver`。datatest 当前是 Spock，不能按“`def test(RuntimeViewsheet vs)`”的示例直接迁移。
- `IntegrationTestConfiguration` 不是完整替代。datatest 仍需要 `DatatestSpringDuplicateFixConfiguration` 提供 `@Primary` 覆盖和缺失 bean。

因此本文后续只保留 Step8 的评估结论和后期关注项：Spock 测试继续使用 `RuntimeViewsheetResource`；未来新增或迁移到 Java/JUnit Jupiter 的测试，再使用产品侧 `RuntimeViewsheetExtension`。

---

## 架构升级计划：对齐产品 `inetsoft.test`

> 分析日期：2026-05-07
> 产品架构路径：`E:\inetsoft\sr14_0\stylebi-session\community\core\src\test\java\inetsoft\test`

### 产品架构概览

产品 `inetsoft.test` 包含 21 个文件，核心组成：

| 类别 | 文件 | 作用 |
|---|---|---|
| 自定义注解 | `@SreeHome`, `@SreeProperty`, `@DataSpaceFile`, `@PenetrationTest(s)` | 声明式测试环境配置 |
| Spring 配置 | `BaseTestConfiguration`, `IntegrationTestConfiguration`, `LibManagerTestConfiguration`, `ScheduleTestConfiguration`, `PluginsTestConfiguration`, `SwapperTestConfiguration` | 模块化 bean 定义 |
| JUnit 5 扩展 | `SreeHomeExtension`, `PenetrationTestsExtension`, `RuntimeViewsheetExtension` | 生命周期管理 |
| Spring 初始化器 | `ConfigurationContextInitializer` | 将 Spring context 绑定到 `ConfigurationContext` |
| 存储实现 | `TestKeyValueEngine`, `TestKeyValueEngineFactory` | 内存 KV 存储（AutoService 自动注册） |
| 工具类 | `TestSerializeUtils`, `XTableUtil`, `VSAssemblyFixture` | 测试断言与数据构造 |

### 现状 vs 产品架构对比

| 维度 | 当前 datatest | 产品 `inetsoft.test` |
|---|---|---|
| 启动方式 | Spock-Spring `@ContextConfiguration`；helper 通过 `DatatestSpringRuntimeInitializer.ensureInitialized()` 校验 context | `@SreeHome` + `SreeHomeExtension` 声明式 |
| Spring 绑定 | `ConfigurationContextInitializer` 绑定 Spock-Spring context | `ConfigurationContextInitializer`（标准 Spring 初始化器） |
| Controller/Service 获取 | 直接 `@Autowired` 或 `ConfigurationContext.getSpringBean()`；`ControllersResource` 已删除 | `@Autowired` / `ConfigurationContext.getSpringBean()` 直接取 Spring bean |
| Viewsheet 生命周期 | `RuntimeViewsheetResource`（Spock/Groovy helper，从 Spring context 取 bean） | `RuntimeViewsheetExtension`（JUnit 5 `@RegisterExtension` 字段扩展） |
| KV 存储 | `DatatestBaseConfiguration` 恢复 runner 写入的 MapDB KV | `TestKeyValueEngineFactory`（内存 KV，适合产品单测，不适合直接替换 datatest runner 数据） |
| 测试隔离 | 每个 Spec 手动 `setupSpec` | `@SreeHome` 注解参数控制 |

**关键前提**：datatest 已经 import `inetsoft.test.IntegrationTestConfiguration`，说明产品 test-jar 已是依赖，**沿用产品 Spring 测试设施是可行方向**。但不能把产品 `BaseTestConfiguration`、`SreeHomeExtension`、`RuntimeViewsheetExtension` 按字面整体替换进 Spock/datatest：MapDB KV、缺失 bean、JUnit/Spock 扩展模型都需要 datatest 侧适配。

---

### 改动方案（分 3 层）

#### 第 1 层：收敛启动入口，优先使用 Spock-Spring

**改哪里**：所有 Spock Spec 类和 Groovy helper 中的手动 bootstrap 调用。

**历史写法**：
```groovy
def setupSpec() {
    DatatestRuntimeBootstrap.bootstrap(System.getProperty("sree.home", "."))
}
```

**Spock 可落地写法**：
```groovy
@ContextConfiguration(
    classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration],
    initializers = [ConfigurationContextInitializer]
)
class VPM_Spec extends Specification { ... }
```

该层已完成：当前代码目录已无 `DatatestRuntimeBootstrap.bootstrap()` 调用，helper 中的 `ensureRuntimeInitialized()` 只负责确认 Spock-Spring context 已存在并绑定到 `ConfigurationContext`。后续新增 helper 时不要恢复手动 bootstrap。

`@SreeHome` / `SreeHomeExtension` 是产品侧 JUnit Jupiter 扩展，适合产品 Java/JUnit 测试。除非把对应 Spec 迁移成 JUnit Jupiter 测试，或先验证当前 Spock runner 会执行 Jupiter extension，否则不要把 `@SreeHome` 当作直接替换方案。

> **保留前提**：`DatatestBaseConfiguration` 必须继续参与 `@ContextConfiguration`，用于读取 runner plugin 已写入的 MapDB KV。不能直接改回产品 `BaseTestConfiguration`。

---

#### 第 2 层：废弃 `ControllersResource`，改用 `@Autowired` 直接注入

**改哪里**：所有仍引用 `ControllersResource` 的 Spec、helper 和 Resource。该层已完成，当前代码目录无 `ControllersResource` / `initControllers()` 命中。历史迁移范围包括：

- `commons/src/test/groovy/inetsoft/test/core/ActionEventsUtil.groovy`
- `commons/src/test/java/inetsoft/test/core/RuntimeViewsheetResource.java`
- `commons/src/test/java/inetsoft/test/core/RuntimeWorksheetResource.java`
- `commons/src/test/java/inetsoft/test/mv/MaterializedViewResource.java`
- `commons/src/test/groovy/inetsoft/test/modules/AdditionalConnectionTest.groovy`
- `commons/src/test/groovy/inetsoft/test/modules/CSSTest.groovy`
- `commons/src/test/groovy/inetsoft/test/modules/GlobalTest.groovy`
- `commons/src/test/groovy/inetsoft/test/modules/VSExportTest.groovy`
- `commons/src/test/groovy/inetsoft/test/modules/VSScriptTest.groovy`
- `commons/src/test/groovy/inetsoft/test/modules/WorksheetTest.groovy`
- `commons/src/test/groovy/inetsoft/test/mv/MVTest.groovy`

**历史写法**：
```groovy
ControllersResource controllersResource

def setup() {
    controllersResource = new ControllersResource()
    controllersResource.initControllers()
}

def "test"() {
    def service = controllersResource.viewsheetService
}
```

**Spec 中改后**：
```groovy
@Autowired ViewsheetService viewsheetService
@Autowired OpenViewsheetController openViewsheetController
@Autowired RuntimeViewsheetRef runtimeViewsheetRef
```

**helper / Java Resource 中改后**：
```java
private <T> T springBean(Class<T> type) {
   return ConfigurationContext.getContext().getSpringBean(type);
}
```

删除条件已满足；后续防回归仍可用同一命令确认：

```powershell
rg -n "ControllersResource|initControllers\(" commons ws vsothers vpm chart css vsscript vsexport mv
```

该命令除文档外应无命中。若未来重新引入相关兼容层，必须重新生成 commons test-jar，避免旧 artifact 掩盖问题：

```powershell
.\mvnw.cmd -pl commons clean test-compile jar:test-jar
```

注意：bean 来源是 `IntegrationTestConfiguration + DatatestSpringDuplicateFixConfiguration + DatatestBaseConfiguration` 的组合，不是单独的 `IntegrationTestConfiguration`。

---

#### 第 3 层：收敛 `RuntimeViewsheetResource`，JUnit 场景再用 `RuntimeViewsheetExtension`

**改哪里**：所有手动管理 viewsheet open/close 的 Spec

**当前 Spock helper 使用方式**：
```groovy
RuntimeViewsheetResource runtimeViewsheetResource

viewsheetResource = new RuntimeViewsheetResource(actionEventsUtil.createOpenViewsheetEvent(params, asset_id))
viewsheetResource.initRuntimeVS(principal)
RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet(principal)
```

当前 `RuntimeViewsheetResource` 已去掉 `ControllersResource` 构造参数，只从 Spring context 取 bean。

**JUnit Jupiter 可选写法**：如果某个测试迁移到 Java/JUnit，可使用产品侧真实 API：

```java
@RegisterExtension
RuntimeViewsheetExtension viewsheetResource =
   new RuntimeViewsheetExtension(createOpenViewsheetEvent());

@Test
void test() {
   RuntimeViewsheet rvs = viewsheetResource.getRuntimeViewsheet();
}
```

不要使用 `def "test"(RuntimeViewsheet vs)` 这种参数注入写法；当前产品 `RuntimeViewsheetExtension` 没有实现 `ParameterResolver`。

---

### 需要保留的 datatest 专属文件

| 文件 | 保留原因 |
|---|---|
| `DatatestSpringDuplicateFixConfiguration.java` | 解决 datatest 与产品配置之间的 bean 冲突（`@Primary` 覆盖），产品侧无此需求 |
| `DatatestBaseConfiguration.java` | 保留 runner plugin 写入的 MapDB KV 读取能力；产品 `BaseTestConfiguration` 的 in-memory KV 不能直接替换 |
| `MessageTestUtils.java` | Spock/Groovy 风格工具方法，与产品侧 JUnit 5 扩展风格不同 |

**已删除的历史兼容文件**：

- `ControllersResource.java`：引用迁移已完成，当前代码目录 `rg "ControllersResource|initControllers\("` 无命中。
- `DatatestRuntimeBootstrap.java`：所有 Spec/helper 不再手动 bootstrap，当前代码目录无 `DatatestRuntimeBootstrap` 命中。

**暂不删除的文件**：

- `DatatestBaseConfiguration.java`：必须保留，除非 runner plugin 与产品测试配置已经统一支持 MapDB KV。
- `RuntimeViewsheetResource.java`：Spock 体系下继续保留为 helper；当前已移除对 `ControllersResource` 的依赖。若未来把相关测试迁移到 JUnit Jupiter，再逐步替换为产品 `RuntimeViewsheetExtension`。

---

### 实施顺序

```
Step 0  [已完成] 先记录当前引用面：
        rg -n "ControllersResource|initControllers\(" commons ws vsothers vpm chart css vsscript vsexport mv

Step 1  [已完成/持续遵守] 保持所有 Spec 的 @ContextConfiguration 使用：
        DatatestBaseConfiguration + IntegrationTestConfiguration + DatatestSpringDuplicateFixConfiguration

Step 2  [已完成] 先迁移 commons helper/resource：
        ActionEventsUtil
        RuntimeViewsheetResource（去掉 ControllersResource 构造参数）
        RuntimeWorksheetResource（改为 Spring bean lookup）
        MaterializedViewResource（改为 Spring bean lookup 或直接注入所需 service）

Step 3  [已完成] 再迁移 Groovy test helper：
        AdditionalConnectionTest
        CSSTest
        GlobalTest
        VSExportTest
        VSScriptTest
        WorksheetTest
        MVTest

Step 4  [已完成] 引用清零后删除 ControllersResource.java：
        rg -n "ControllersResource|initControllers\(" commons ws vsothers vpm chart css vsscript vsexport mv

Step 5  [后续变更仍需执行] 强制干净编译 commons，避免 stale test-jar：
        .\mvnw.cmd -pl commons clean test-compile jar:test-jar

Step 6  [回归清单/后续变更仍需执行] 跑核心模块回归：
        .\mvnw.cmd test -pl vpm "-Dtest=inetsoft.test.vpm.cases.VPM_Spec"
        .\mvnw.cmd test -pl vsothers -Pvsfreehand "-Dtest=inetsoft.test.viewsheet.cases.vsfreehand.Convert_Spec"
        .\mvnw.cmd test -pl vsothers -Pvsfreehand "-Dtest=inetsoft.test.viewsheet.cases.vsfreehand.FreeExecution_Spec"
        .\mvnw.cmd test -pl vsothers -Potherassembly "-Dtest=inetsoft.test.viewsheet.cases.otherassembly.FormTable_Spec"
        .\mvnw.cmd test -pl ws "-Dtest=inetsoft.test.worksheet.cases.DataComposition_Spec"
        .\mvnw.cmd test -pl ws "-Dtest=inetsoft.test.worksheet.cases.WSProperty_Spec"
        .\mvnw.cmd test -pl vsscript "-Dtest=inetsoft.test.vsscript.cases.Bugs_Spec"
        .\mvnw.cmd test -pl vsexport "-Dtest=inetsoft.test.vsexport.cases.Component.Com_CChart_Spec"
        .\mvnw.cmd test -pl css "-Dtest=inetsoft.test.css.cases.Global_Spec"
        .\mvnw.cmd test -pl mv "-Dtest=inetsoft.test.mv.cases.incrementalmv.Append_Spec"
        .\mvnw.cmd test -pl mv "-Dtest=inetsoft.test.mv.cases.vpm.VPM_Spec"

Step 7  [已完成] 所有手动 bootstrap 引用清零后，删除 DatatestRuntimeBootstrap.java。

Step 8  [当前结论] RuntimeViewsheetResource 是否替换为 RuntimeViewsheetExtension 单独评估：
        当前结论：不直接替换。
        Spock/Groovy 测试继续保留 RuntimeViewsheetResource 作为 datatest helper；
        未来新增或迁移到 JUnit Jupiter 的 Java 测试，才使用 @RegisterExtension RuntimeViewsheetExtension。
```

### Step 8 评估结论

当前 datatest 中 `RuntimeViewsheetResource` 的引用全部来自 `commons` 下的 Groovy/Spock helper：

- `MVTest`
- `WorksheetTest`
- `VSScriptTest`
- `VSFormImportTest`
- `VSExportTest`
- `ViewsheetTest`
- `GlobalTest`
- `CSSTest`
- `AdditionalConnectionTest`

产品侧 `inetsoft.test.RuntimeViewsheetExtension` 适合 Java/JUnit Jupiter 测试，但不适合作为这些 Spock helper 的直接替换：

- 它实现的是 JUnit Jupiter `BeforeEachCallback` / `AfterEachCallback`，依赖 `@RegisterExtension` 和 `SpringExtension.getApplicationContext(context)`；当前 datatest Spec 是 Spock-Spring。
- 它固定用系统 principal 打开 viewsheet；datatest 需要按 case 注入 `admin`、`guest`、VPM 用户、additional datasource 等不同 `SRPrincipal`。
- 它只覆盖 open/get/close 生命周期；datatest helper 还集中封装了 `exportVS`、`refreshViewsheet`、`processImportXLS`、`convertToFreehand`、chart detail、brush 等操作。
- datatest 仍必须使用 `DatatestBaseConfiguration + IntegrationTestConfiguration + DatatestSpringDuplicateFixConfiguration` 的组合来读取 runner 写入的 MapDB KV 和补齐 bean，不能只按产品侧 Jupiter 扩展模型迁移。

因此 Step 8 的落地策略是：

- 保留 `RuntimeViewsheetResource.java`，定位为 datatest/Spock helper。
- 不引入 `@RegisterExtension RuntimeViewsheetExtension` 到现有 Spock Spec。
- `RuntimeViewsheetResource` 继续保留 Spring bean lookup，不在 Step8 直接改造成产品侧 extension。
- 如果未来新增 Java/JUnit Jupiter 测试，且只需要 open/get/close runtime viewsheet，再优先使用产品侧 `RuntimeViewsheetExtension`。

后期需要关注：

- 当前 `RuntimeViewsheetResource` 内部有 `private closeViewsheet(String runtimeId)`，但没有公开 `close()` / `destroy()` 生命周期入口；现有 Groovy helper 也没有调用显式关闭。若后续发现 runtime viewsheet 泄漏、测试间状态串扰，或需要与产品 extension 生命周期对齐，应单独补公开关闭 API 并加回归验证。
- `RuntimeViewsheetResource` 现在承担 open/get 之外的导出、刷新、XLS 导入、freehand 转换、chart detail/brush 等动作。未来若想进一步收敛，需要先拆分这些动作型 helper，再评估哪些 Java/JUnit 测试可以改用 `RuntimeViewsheetExtension`。

### 已实现收益与剩余关注

| 指标 | Step 7 后当前状态 | 剩余关注 |
|---|---|---|
| 手动 bootstrap 代码 | `DatatestRuntimeBootstrap` 已删除；代码目录无引用 | 新增 helper 不要恢复手动 bootstrap |
| controller 兼容层 | `ControllersResource` 已删除；代码目录无引用 | 后续通过 `rg "ControllersResource|initControllers\("` 防回归 |
| 双实例风险 | 原始手动 `new/mock` 和 façade API 维护成本已移除 | datatest 专属补充 bean 仍需显式维护 |
| Viewsheet 生命周期 | Spock/Groovy 继续使用 `RuntimeViewsheetResource` | 关注是否需要公开 `close()` / `destroy()` |
| 产品升级兼容性 | controller/service 获取跟随 Spring bean；MapDB KV 仍由 datatest 配置读取 | 不要用产品 `BaseTestConfiguration` 直接替换 `DatatestBaseConfiguration` |
| 新增测试模块的工作量 | 直接使用 Spring 注入或 helper 的 Spring lookup | Java/JUnit 新测试可优先评估 `RuntimeViewsheetExtension` |
