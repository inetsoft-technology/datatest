/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.enterprise.web.api.file.FileApiService;
import inetsoft.analytic.composition.ViewsheetService;
import inetsoft.report.XSessionManager;
import inetsoft.report.LibManagerProvider;
import inetsoft.report.composition.WorksheetService;
import inetsoft.report.composition.execution.AssetDataCache;
import inetsoft.report.internal.license.License;
import inetsoft.report.internal.license.LicenseManager;
import inetsoft.sree.AnalyticRepository;
import inetsoft.sree.RepletRegistryManager;
import inetsoft.sree.internal.cluster.Cluster;
import inetsoft.sree.schedule.ScheduleClient;
import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.uql.XRepository;
import inetsoft.uql.asset.AssetEntry;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.uql.asset.DependencyHandler;
import inetsoft.uql.asset.sync.RenameTransformHandler;
import inetsoft.uql.jdbc.ConnectionPoolFactory;
import inetsoft.uql.jdbc.DefaultConnectionPoolFactory;
import inetsoft.uql.service.DataSourceRegistry;
import inetsoft.uql.util.XSessionService;
import inetsoft.util.DataCacheSweeper;
import inetsoft.util.DataSpace;
import inetsoft.util.FileSystemService;
import inetsoft.util.IndexedStorage;
import inetsoft.util.credential.CredentialService;
import inetsoft.web.RecycleBin;
import inetsoft.web.admin.content.database.model.DataModelFolderManagerService;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.DatabaseDatasourcesService;
import inetsoft.web.admin.content.repository.MVSupportService;
import inetsoft.web.admin.content.repository.RepletRegistryService;
import inetsoft.web.admin.content.repository.ResourcePermissionService;
import inetsoft.web.admin.deploy.DeployService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.web.binding.handler.VSAssemblyInfoHandler;
import inetsoft.web.binding.service.VSBindingService;
import inetsoft.web.composer.vs.VSObjectTreeService;
import inetsoft.web.composer.vs.controller.VSLayoutService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableServiceProxy;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.OpenWorksheetControllerServiceProxy;
import inetsoft.web.composer.ws.assembly.WorksheetEventService;
import inetsoft.web.composer.ws.assembly.WorksheetEventServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogService;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogServiceProxy;
import inetsoft.web.portal.controller.database.DataSourceService;
import inetsoft.web.portal.controller.database.DatabaseModelBrowserService;
import inetsoft.web.portal.data.DatabaseDatasourcesController;
import inetsoft.web.service.BinaryTransferService;
import inetsoft.mv.MVManager;
import inetsoft.mv.MVWorksheetStorage;
import inetsoft.storage.BlobStorageManager;
import inetsoft.sree.web.dashboard.DashboardManager;
import inetsoft.sree.web.dashboard.DashboardRegistryManager;
import inetsoft.sree.internal.DeployManagerService;
import inetsoft.uql.XDataService;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.controller.ImportXLSControllerService;
import inetsoft.web.viewsheet.controller.ImportXLSControllerServiceProxy;
import inetsoft.web.viewsheet.controller.chart.VSChartAreasServiceProxy;
import inetsoft.web.viewsheet.controller.chart.VSChartBrushService;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsService;
import inetsoft.web.viewsheet.handler.crosstab.CrosstabDrillHandler;
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef;
import inetsoft.web.viewsheet.model.VSObjectModelFactory;
import inetsoft.web.viewsheet.model.VSObjectModelFactoryService;
import inetsoft.web.viewsheet.service.CoreLifecycleService;
import inetsoft.web.viewsheet.service.RuntimeViewsheetManager;
import inetsoft.web.viewsheet.service.VSDialogService;
import inetsoft.web.viewsheet.service.VSExportService;
import inetsoft.web.viewsheet.service.CommandDispatcher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.context.event.ContextRefreshedEvent;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.rmi.RemoteException;
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Loaded together with {@link inetsoft.test.BaseTestConfiguration} and
 * {@link inetsoft.test.IntegrationTestConfiguration} via {@code @ContextConfiguration}.
 * <p>
 * Provides {@code @Primary} overrides where inetsoft-core registers overlapping beans,
 * and fills in beans required by datatest helpers that are not provided by
 * {@link inetsoft.test.IntegrationTestConfiguration}.
 * </p>
 */
@Configuration
public class DatatestSpringDuplicateFixConfiguration {

   /**
    * Registers {@link VSObjectModelFactory} beans the same way as runtime component-scan (each
    * factory is {@link Component} under {@code inetsoft.web.viewsheet.model}). Excludes
    * {@link RuntimeViewsheetRef} so it does not clash with
    * {@link inetsoft.test.IntegrationTestConfiguration#runtimeViewsheetRef}.
    */
   @Configuration
   @ComponentScan(
      basePackages = "inetsoft.web.viewsheet.model",
      useDefaultFilters = false,
      includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Component.class),
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RuntimeViewsheetRef.class)
   )
   static class DatatestVSObjectModelFactoryScan {
   }

   @Bean
   public ApplicationListener<ContextRefreshedEvent> datatestSpringRuntimeInitializer() {
      return event -> DatatestSpringRuntimeInitializer.initialize(
         inetsoft.util.ConfigurationContext.getContext(), event.getApplicationContext());
   }

   /**
    * {@link inetsoft.test.IntegrationTestConfiguration#objectModelFactoryService()} uses a hand-written
    * factory list that omits crosstab. Prefer the full set of {@link VSObjectModelFactory} beans from
    * {@link #DatatestVSObjectModelFactoryScan} (matches production wiring).
    */
   @Bean
   @Primary
   public VSObjectModelFactoryService datatestObjectModelFactoryService(
      List<VSObjectModelFactory<?, ?>> modelFactories)
   {
      return new VSObjectModelFactoryService(modelFactories);
   }

   /**
    * With {@code mock.license.manager=true}, {@link inetsoft.test.BaseTestConfiguration} supplies an
    * unstubbed mock; {@link inetsoft.util.ThreadPool} then gets null for
    * {@link LicenseManager#calculateThreadPoolSize(int, String, int)}.
    */
   @Bean
   @Primary
   public LicenseManager datatestPrimaryLicenseManager() {
      LicenseManager lm = mock(LicenseManager.class);
      License claimed = mock(License.class);
      lenient().when(claimed.standalone()).thenReturn(true);
      lenient().when(lm.calculateThreadPoolSize(anyInt(), nullable(String.class), anyInt()))
         .thenReturn(new int[] { 8, 16 });
      lenient().when(lm.getNamedUserCount()).thenReturn(100);
      lenient().when(lm.getNamedUserViewerSessionCount()).thenReturn(0);
      lenient().when(lm.getLicenseHash()).thenReturn("datatest");
      lenient().when(lm.getClaimedLicenses()).thenReturn(Set.of(claimed));
      return lm;
   }

   /**
    * Replaces {@code scheduleManager} from {@link inetsoft.test.IntegrationTestConfiguration}
    * and ensures {@link SecurityEngine#init()} is called before other beans that need
    * {@link SecurityProvider} are constructed.
    */
   @Bean(name = "scheduleManager")
   @Primary
   public ScheduleManager scheduleManager(SecurityEngine securityEngine, Cluster cluster) {
      securityEngine.init();
      return new ScheduleManager(securityEngine, cluster, mock(ScheduleClient.class),
         mock(DependencyHandler.class));
   }

   /**
    * {@link MVManager} constructs {@code MVDefMap}, which calls {@link DataSpace#getDataSpace()} and
    * {@link IndexedStorage#getIndexedStorage()} → {@link inetsoft.util.ConfigurationContext#getSpringBean}.
    * If those beans are still resolving while {@code MVManager} is being cached, Caffeine/CHM can throw
    * {@code IllegalStateException: Recursive update} (same class of issue as
    * {@link DatatestSpringRuntimeInitializer#initializeStorageAccess}).
    * Force storage beans to finish before {@code MVManager} instantiation, and use {@link Lazy} so
    * {@code new MVManager} runs on first access after refresh—not during a nested {@code getBean} stack.
    */
   @Bean(name = "mvManager")
   @Lazy
   @Primary
   @DependsOn({"dataSpace", "indexedStorage", "blobStorageManager"})
   public MVManager datatestMvManager(Cluster cluster, DataSpace dataSpace, IndexedStorage indexedStorage) {
      Objects.requireNonNull(dataSpace, "dataSpace");
      Objects.requireNonNull(indexedStorage, "indexedStorage");
      return new MVManager(cluster);
   }

   @Bean
   @Primary
   public DataCacheSweeper datatestPrimaryDataCacheSweeper() {
      return mock(DataCacheSweeper.class);
   }

   @Bean
   @Primary
   public XSessionService datatestPrimaryXSessionService() {
      return new XSessionService();
   }

   @Bean
   @Primary
   public XSessionManager xSessionManager(
      XDataService dataService,
      XSessionService sessionService,
      DataSourceRegistry dataSourceRegistry) throws RemoteException
   {
      return new XSessionManager(dataService, sessionService, dataSourceRegistry);
   }

   @Bean(name = "connectionPoolFactory")
   @Primary
   public ConnectionPoolFactory connectionPoolFactory() {
      return new DefaultConnectionPoolFactory();
   }

   @Bean
   @Primary
   public CredentialService credentialService() throws Exception {
      Constructor<CredentialService> constructor = CredentialService.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      return constructor.newInstance();
   }

   // ─── Beans used by datatest helpers but absent from IntegrationTestConfiguration ───

   /**
    * SecurityProvider is obtained lazily (after scheduleManager calls securityEngine.init()).
    */
   @Bean
   @DependsOn("scheduleManager")
   public SecurityProvider datatestSecurityProvider(SecurityEngine securityEngine) {
      return securityEngine.getSecurityProvider();
   }

   @Bean
   public WorksheetEventService worksheetEventService(ViewsheetService viewsheetService) throws Exception {
      ObjectProvider<WorksheetEventServiceProxy> proxy = mock(ObjectProvider.class);
      WorksheetEventServiceProxy serviceProxy = mock(WorksheetEventServiceProxy.class);
      WorksheetEventService[] service = new WorksheetEventService[1];

      lenient().when(serviceProxy.openWorksheet(
         anyString(), nullable(Principal.class), any(AssetEntry.class),
         anyBoolean(), anyBoolean(), any(CommandDispatcher.class)))
         .thenAnswer(invocation -> service[0].openWorksheet(
            invocation.getArgument(0),
            invocation.getArgument(1),
            invocation.getArgument(2),
            invocation.getArgument(3),
            invocation.getArgument(4),
            invocation.getArgument(5)));
      lenient().when(proxy.getIfAvailable()).thenReturn(serviceProxy);
      service[0] = new WorksheetEventService(viewsheetService, proxy);
      return service[0];
   }

   @Bean
   public OpenWorksheetController openWorksheetController(
      RuntimeViewsheetManager runtimeViewsheetManager,
      AnalyticRepository analyticRepository,
      WorksheetEventService worksheetEventService,
      WorksheetService worksheetService,
      RuntimeViewsheetRef runtimeViewsheetRef,
      SecurityEngine securityEngine)
   {
      return new OpenWorksheetController(runtimeViewsheetManager, analyticRepository.unwrap(AssetRepository.class),
         worksheetEventService, mock(OpenWorksheetControllerServiceProxy.class), securityEngine)
      {
         @Override
         protected WorksheetService getWorksheetEngine() {
            return worksheetService;
         }

         @Override
         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
      };
   }

   @Bean
   public BinaryTransferService binaryTransferService(FileSystemService fileSystemService) {
      return new BinaryTransferService(fileSystemService);
   }

   @Bean
   public VSExportService vsExportService(
      ViewsheetService viewsheetService,
      CoreLifecycleService coreLifecycleService,
      inetsoft.web.viewsheet.service.ParameterService parameterService,
      SecurityEngine securityEngine,
      XSessionService xSessionService,
      FileSystemService fileSystemService)
   {
      return new VSExportService(viewsheetService, coreLifecycleService, parameterService,
         securityEngine, xSessionService, fileSystemService);
   }

   @Bean
   public ResourcePermissionService resourcePermissionService(
      SecurityProvider datatestSecurityProvider,
      SecurityEngine securityEngine,
      LibManagerProvider libManagerProvider,
      DataSourceRegistry dataSourceRegistry)
   {
      return new ResourcePermissionService(datatestSecurityProvider, securityEngine,
         libManagerProvider, dataSourceRegistry);
   }

   @Bean
   public ScheduleTaskFolderService scheduleTaskFolderService(
      ScheduleManager scheduleManager,
      SecurityEngine securityEngine,
      SecurityProvider datatestSecurityProvider,
      IndexedStorage indexedStorage)
   {
      return new ScheduleTaskFolderService(scheduleManager, securityEngine,
         datatestSecurityProvider, indexedStorage, mock(RenameTransformHandler.class));
   }

   @Bean
   public ContentRepositoryTreeService contentRepositoryTreeService(
      SecurityProvider datatestSecurityProvider,
      ResourcePermissionService resourcePermissionService,
      ScheduleTaskFolderService scheduleTaskFolderService,
      SecurityEngine securityEngine,
      ScheduleManager scheduleManager,
      RepletRegistryManager repletRegistryManager)
   {
      return new ContentRepositoryTreeService(
         datatestSecurityProvider,
         mock(XRepository.class),
         resourcePermissionService,
         mock(RepletRegistryService.class),
         scheduleTaskFolderService,
         mock(MVManager.class),
         securityEngine,
         scheduleManager,
         mock(DataSourceRegistry.class),
         mock(DashboardManager.class),
         mock(IndexedStorage.class),
         mock(DashboardRegistryManager.class),
         mock(LibManagerProvider.class),
         mock(RecycleBin.class),
         repletRegistryManager);
   }

   /**
    * {@link inetsoft.test.mv.MaterializedViewResource} uses {@link MVSupportService#getInstance()}, which
    * resolves the bean from {@link inetsoft.util.ConfigurationContext}. Core wiring matches production:
    * {@code new MVSupportService(MVManager, SecurityEngine, ScheduleManager, Cluster)} — a Mockito mock
    * breaks {@link inetsoft.web.admin.content.repository.MVService#analyze} (controller returns null job).
    */
   @Bean
   public MVSupportService mvSupportService(
      MVManager mvManager,
      SecurityEngine securityEngine,
      ScheduleManager scheduleManager,
      Cluster cluster)
   {
      return new MVSupportService(mvManager, securityEngine, scheduleManager, cluster);
   }

   /**
    * {@link MVWorksheetStorage#getInstance()} is used when loading MV definitions (e.g. dispose / {@link inetsoft.mv.MVDefMap#get}).
    * Wired like {@code inetsoft.web.factory.EngineConfiguration#mvWorksheetStorage}; missing from the datatest slice otherwise.
    */
   @Bean
   @Lazy
   public MVWorksheetStorage mvWorksheetStorage(@Lazy BlobStorageManager blobStorageManager) {
      return new MVWorksheetStorage(blobStorageManager);
   }

   @Bean
   public DeployService deployService(
      ContentRepositoryTreeService contentRepositoryTreeService,
      SecurityEngine securityEngine,
      FileSystemService fileSystemService)
   {
      return new DeployService(
         contentRepositoryTreeService, securityEngine,
         mock(DataSourceRegistry.class), mock(IndexedStorage.class),
         mock(DeployManagerService.class), mock(DashboardRegistryManager.class),
         mock(LibManagerProvider.class), fileSystemService,
         mock(RepletRegistryService.class));
   }

   @Bean
   public ComposerVSTableController composerVSTableController(RuntimeViewsheetRef runtimeViewsheetRef) {
      return new ComposerVSTableController(runtimeViewsheetRef, mock(ComposerVSTableServiceProxy.class));
   }

   @Bean
   public ComposerVSTableService composerVSTableService(
      CoreLifecycleService coreLifecycleService,
      VSObjectTreeService objectTreeService,
      VSObjectModelFactoryService objectModelFactoryService,
      ViewsheetService viewsheetService,
      AnalyticRepository analyticRepository)
   {
      return new ComposerVSTableService(
         coreLifecycleService, objectTreeService, objectModelFactoryService,
         mock(VSBindingService.class), analyticRepository.unwrap(AssetRepository.class), viewsheetService,
         mock(CrosstabDrillHandler.class),
         mock(VSAssemblyInfoHandler.class));
   }

   @Bean
   public ImportXLSControllerService importXLSControllerService(
      ViewsheetService viewsheetService, CoreLifecycleService coreLifecycleService)
   {
      return new ImportXLSControllerService(viewsheetService, coreLifecycleService);
   }

   @Bean
   public ImportXLSController importXLSController(
      RuntimeViewsheetRef runtimeViewsheetRef,
      ImportXLSControllerService importXLSControllerService,
      FileSystemService fileSystemService)
   {
      ImportXLSControllerServiceProxy proxy =
         importXlsControllerServiceProxyDelegate(importXLSControllerService);
      return new ImportXLSController(runtimeViewsheetRef, proxy, fileSystemService);
   }

   /**
    * {@link ImportXLSControllerServiceProxy} is not a concrete bean in datatest; delegate to the
    * real {@link ImportXLSControllerService} so upload/process hit {@code ImportXLSService}.
    * <p>
    * Mockito {@code .when(proxy).sheetExists(...)} is wrapped in {@code try/catch} only because the
    * proxy interface declares {@code throws Exception} on those methods (Java requires handling).
    * </p>
    */
   private static ImportXLSControllerServiceProxy importXlsControllerServiceProxyDelegate(
      ImportXLSControllerService importXLSControllerService)
   {
      ImportXLSControllerServiceProxy proxy = mock(ImportXLSControllerServiceProxy.class);
      try {
         lenient().doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
               return importXLSControllerService.sheetExists(
                  invocation.getArgument(0, String.class),
                  invocation.getArgument(1, Principal.class));
            }
         }).when(proxy).sheetExists(anyString(), nullable(Principal.class));
         lenient().doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
               importXLSControllerService.processXLSUpload(
                  invocation.getArgument(0, String.class),
                  invocation.getArgument(1, String.class),
                  invocation.getArgument(2, String.class),
                  invocation.getArgument(3, Principal.class),
                  invocation.getArgument(4, CommandDispatcher.class));
               return null;
            }
         }).when(proxy).processXLSUpload(anyString(), anyString(), anyString(),
            nullable(Principal.class), any(CommandDispatcher.class));
      }
      catch(Exception e) {
         throw new IllegalStateException("ImportXLSControllerServiceProxy stubbing failed", e);
      }
      return proxy;
   }

   @Bean
   public ImportCSVDialogController importCSVDialogController(
      BinaryTransferService binaryTransferService,
      RuntimeViewsheetRef runtimeViewsheetRef,
      WorksheetService worksheetService)
   {
      return new ImportCSVDialogController(mock(ImportCSVDialogServiceProxy.class), binaryTransferService)
      {
         @Override
         public String getRuntimeId() {
            return runtimeViewsheetRef.getRuntimeId();
         }

         @Override
         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
      };
   }

   @Bean
   public ImportCSVDialogService importCSVDialogService(
      ViewsheetService viewsheetService,
      VSLayoutService vsLayoutService,
      BinaryTransferService binaryTransferService,
      AssetDataCache assetDataCache)
   {
      return new ImportCSVDialogService(viewsheetService, vsLayoutService, binaryTransferService,
         assetDataCache, mock(FileSystemService.class));
   }

   @Bean
   public VSChartBrushService vsChartBrushService(
      CoreLifecycleService coreLifecycleService, ViewsheetService viewsheetService)
   {
      return new VSChartBrushService(coreLifecycleService, viewsheetService,
         mock(VSChartAreasServiceProxy.class));
   }

   @Bean
   public VSChartShowDetailsService vsChartShowDetailsService(
      ViewsheetService viewsheetService, CoreLifecycleService coreLifecycleService)
   {
      return new VSChartShowDetailsService(viewsheetService, coreLifecycleService,
         mock(VSChartAreasServiceProxy.class), mock(VSDialogService.class));
   }

   @Bean
   public FileApiService fileApiService(
      DeployService deployService,
      ContentRepositoryTreeService contentRepositoryTreeService,
      SecurityProvider datatestSecurityProvider,
      RepletRegistryManager repletRegistryManager)
   {
      return new FileApiService(deployService, contentRepositoryTreeService,
         datatestSecurityProvider,
         mock(DataSourceRegistry.class), mock(IndexedStorage.class),
         mock(DataSpace.class), repletRegistryManager);
   }

   @Bean
   public DatabaseDatasourcesController databaseDatasourcesController() {
      return new DatabaseDatasourcesController(
         mock(DatabaseDatasourcesService.class),
         mock(DatabaseModelBrowserService.class),
         mock(DataModelFolderManagerService.class),
         mock(DataSourceService.class),
         mock(XRepository.class));
   }
}
