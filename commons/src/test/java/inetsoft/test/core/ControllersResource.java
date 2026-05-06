package inetsoft.test.core;

import inetsoft.analytic.composition.ViewsheetEngine;
import inetsoft.analytic.composition.ViewsheetService;
import inetsoft.enterprise.web.api.file.FileApiService;
import inetsoft.report.composition.RuntimeWorksheet;
import inetsoft.report.composition.WorksheetEngine;
import inetsoft.report.composition.WorksheetService;
import inetsoft.sree.RepletRegistryManager;
import inetsoft.sree.internal.SUtil;
import inetsoft.sree.internal.cluster.Cluster;
import inetsoft.sree.internal.cluster.MockCluster;
import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.uql.XRepository;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.util.ConfigurationContext;
import inetsoft.web.admin.content.database.model.DataModelFolderManagerService;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.DatabaseDatasourcesService;
import inetsoft.web.admin.content.repository.ResourcePermissionService;
import inetsoft.web.admin.deploy.DeployService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.web.binding.drm.*;
import inetsoft.web.binding.handler.VSAssemblyInfoHandler;
import inetsoft.web.binding.model.*;
import inetsoft.web.binding.service.*;
import inetsoft.web.composer.vs.VSObjectTreeService;
import inetsoft.web.composer.vs.controller.VSLayoutService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.OpenWorksheetControllerServiceProxy;
import inetsoft.web.composer.ws.assembly.WorksheetEventServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogService;
import inetsoft.web.portal.controller.database.DataSourceService;
import inetsoft.web.portal.controller.database.DatabaseModelBrowserService;
import inetsoft.web.portal.data.DatabaseDatasourcesController;
import inetsoft.web.service.LicenseService;
import inetsoft.web.viewsheet.controller.*;
import inetsoft.web.viewsheet.controller.chart.*;
import inetsoft.web.viewsheet.handler.crosstab.CrosstabDrillHandler;
import inetsoft.web.viewsheet.model.*;
import inetsoft.web.viewsheet.model.annotation.VSAnnotationModel;
import inetsoft.web.viewsheet.model.calendar.VSCalendarModel;
import inetsoft.web.viewsheet.model.chart.VSChartModel;
import inetsoft.web.viewsheet.model.table.VSCalcTableModel;
import inetsoft.web.viewsheet.model.table.VSCrosstabModel;
import inetsoft.web.viewsheet.model.table.VSEmbeddedTableModel;
import inetsoft.web.viewsheet.model.table.VSTableModel;
import inetsoft.web.viewsheet.service.*;
import inetsoft.web.viewsheet.service.CoreLifecycleService;
import inetsoft.web.composer.ws.assembly.WorksheetEventService;
import inetsoft.web.service.BinaryTransferService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogServiceProxy;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.mockito.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

public class ControllersResource {
   
   public void initControllers() {
      MessageTestUtils.withMockMessageContext(this::createControllers);
   }
   
   public void destroy() {
      viewsheetService = null;
      vsLifecycleService = null;
      runtimeViewsheetManager = null;
      objectModelFactoryService = null;
      objectTreeService = null;
      securityEngine = null;
      objectService = null;
      bookmarkService = null;
      dataRefModelFactoryService = null;
      assetRepository = null;
      openViewsheetController = null;
      vsChartShowDetailsService = null;
      vsChartBrushService = null;
      composerVSTableService = null;
      worksheetService = null;
      if(staticConfigurationContext != null) {
         staticConfigurationContext.close();
         staticConfigurationContext = null;
      }
   }
   
   private void createControllers() {
      viewsheetService = ViewsheetEngine.getViewsheetEngine();
      Cluster cluster = new MockCluster();
      
      worksheetService = WorksheetEngine.getWorksheetService();
      
      RuntimeViewsheetRefServiceProxy runtimeViewsheetRefServiceProxy =
              Mockito.mock(RuntimeViewsheetRefServiceProxy.class);
      
      runtimeViewsheetRef = new RuntimeViewsheetRef(runtimeViewsheetRefServiceProxy) {
         @Override
         public String getRuntimeId() {
            return ControllersResource.this.runtimeId;
         }
         
         @Override
         public void setRuntimeId(String runtimeId) {
            ControllersResource.this.runtimeId = runtimeId;
         }
      };
      
      runtimeViewsheetManager = new RuntimeViewsheetManager(viewsheetService, cluster);
      List<VSObjectModelFactory<?, ?>> modelFactories = Arrays.asList(
              new VSCalcTableModel.VSCalcTableModelFactory(),
              new VSCheckBoxModel.VSCheckBoxModelFactory(),
              new VSComboBoxModel.VSComboBoxModelFactory(),
              new VSCylinderModel.VSCylinderModelFactory(),
              new VSEmbeddedTableModel.VSEmbeddedTableModelFactory(),
              new VSGaugeModel.VSGaugeModelFactory(),
              new VSImageModel.VSImageModelFactory(),
              new VSLineModel.VSLineModelFactory(),
              new VSOvalModel.VSOvalModelFactory(),
              new VSRadioButtonModel.VSRadioButtonModelFactory(),
              new VSRangeSliderModel.VSRangeSliderModelFactory(),
              new VSRectangleModel.VSRectangleModelFactory(),
              new VSSelectionContainerModel.VSSelectionContainerModelFactory(),
              new VSSelectionListModel.VSSelectionListModelFactory(),
              new VSSelectionTreeModel.VSSelectionTreeModelFactory(),
              new VSSliderModel.VSSliderModelFactory(),
              new VSSlidingScaleModel.VSThermometerModelFactory(),
              new VSSpinnerModel.VSSpinnerModelFactory(),
              new VSSubmitModel.VSSubmitModelFactory(),
              new VSTableModel.VSTableModelFactory(),
              new VSCrosstabModel.VSCrosstabModelFactory(),
              new VSTextInputModel.VSTextInputModelFactory(),
              new VSTextModel.VSTextModelFactory(),
              new VSThermometerModel.VSThermometerModelFactory(),
              new VSViewsheetModel.VSViewsheetModelFactory(),
              new VSAnnotationModel.VSAnnotationModelFactory(),
              new VSCalendarModel.VSCalendarModelFactory(),
              new VSChartModel.VSChartModelFactory(),
              new VSGroupContainerModel.VSGaugeModelFactory(),
              new VSTabModel.VSTabModelFactory()
      );
      objectModelFactoryService = new VSObjectModelFactoryService(modelFactories);
      assetRepository = (AssetRepository) SUtil.getRepletRepository();
      objectTreeService = new VSObjectTreeService(objectModelFactoryService);
      securityEngine = SecurityEngine.getSecurity();
      securityEngine.init();
      
      VSLayoutService vsLayoutService = new VSLayoutService(objectModelFactoryService);
      ParameterService parameterService = new ParameterService(viewsheetService);
      vsCompositionService = new VSCompositionService();
      
      List<DataRefModelFactory<?, ?>> dataRefModelFactories = Arrays.asList(
              new AggregateRefModel.AggregateRefModelFactory(),
              new AliasDataRefModel.AliasDataRefModelFactory(),
              new AttributeRefModel.AttributeRefModelFactory(),
              new BAggregateRefModel.VSAggregateRefModelFactory(),
              new BDimensionRefModel.VSDimensionRefModelFactory(),
              new BaseFieldModel.BaseFieldModelFactory(),
              new CalculateRefModel.CalculateRefModelFactory(),
              new ColumnRefModel.ColumnRefModelFactory(),
              new DateRangeRefModel.DateRangeRefModelFactory(),
              new FormRefModel.FormRefModelFactory(),
              new FormulaFieldModel.FormulaFieldModelFactory(),
              new ExpressionRefModel.ExpressionRefModelFactory(),
              new GroupRefModel.GroupRefModelFactory(),
              new NumericRangeRefModel.NumericRangeRefModelFactory()
      );
      dataRefModelFactoryService = new DataRefModelFactoryService(dataRefModelFactories);
      dataRefModelFactories.stream().forEach(f -> {
         if(f instanceof DataRefModelWrapperFactory) {
            ((DataRefModelWrapperFactory) f).setDataRefModelFactoryService(dataRefModelFactoryService);
         }
      });
      
      ApplicationEventPublisher eventPublisher = event -> {
         if(bookmarkService != null && event instanceof ProcessBookmarkEvent pbe) {
            bookmarkService.onApplicationEvent(pbe);
         }
      };
      inetsoft.uql.service.DataSourceRegistry dataSourceRegistry =
              ConfigurationContext.getContext().getSpringBean(inetsoft.uql.service.DataSourceRegistry.class);
      coreLifecycleService = new CoreLifecycleService(objectModelFactoryService, viewsheetService,
              vsLayoutService, parameterService, vsCompositionService, dataRefModelFactoryService,
              runtimeViewsheetRef, eventPublisher, Mockito.mock(inetsoft.report.internal.license.LicenseManager.class),
              securityEngine, cluster, dataSourceRegistry);
      sharedFilterService = new SharedFilterService(Mockito.mock(SimpMessagingTemplate.class), viewsheetService);
      objectService = new VSObjectService(coreLifecycleService, viewsheetService, securityEngine, sharedFilterService);
      
      bookmarkService = new VSBookmarkService(objectService, viewsheetService, securityEngine,
              ScheduleManager.getScheduleManager(), cluster, Mockito.mock(inetsoft.uql.util.XSessionService.class));
      
      vsLifecycleService = new VSLifecycleService(
              viewsheetService, assetRepository, coreLifecycleService, parameterService,
              Mockito.mock(VSLifecycleControllerServiceProxy.class), Mockito.mock(inetsoft.util.log.LogManager.class),
              Mockito.mock(inetsoft.uql.util.XSessionService.class), cluster, worksheetService,
              Mockito.mock(RuntimeViewsheetRefService.class));
      licenseService = new LicenseService(Mockito.mock(inetsoft.report.internal.license.LicenseManager.class));
      openViewsheetController = new OpenViewsheetController(
              runtimeViewsheetRef, runtimeViewsheetManager, vsLifecycleService, licenseService,
              Mockito.mock(OpenViewsheetServiceProxy.class), viewsheetService, securityEngine);
      
      org.springframework.beans.factory.ObjectProvider<WorksheetEventServiceProxy> worksheetEventServiceProxy =
              Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
      when(worksheetEventServiceProxy.getIfAvailable()).thenReturn(Mockito.mock(WorksheetEventServiceProxy.class));
      worksheetEventService = new WorksheetEventService(viewsheetService, worksheetEventServiceProxy);
      openWorksheetController = new OpenWorksheetController(runtimeViewsheetManager, assetRepository,
              worksheetEventService, Mockito.mock(OpenWorksheetControllerServiceProxy.class), securityEngine) {
         protected WorksheetService getWorksheetEngine() {
            return worksheetService;
         }
         
         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
      };
      
      binaryTransferService = new BinaryTransferService(Mockito.mock(inetsoft.util.FileSystemService.class));
      vsExportService = new VSExportService(viewsheetService, coreLifecycleService, parameterService,
              securityEngine, Mockito.mock(inetsoft.uql.util.XSessionService.class),
              Mockito.mock(inetsoft.util.FileSystemService.class));
      
      securityProvider = securityEngine.getSecurityProvider();
      resourcePermissionService = new ResourcePermissionService(securityProvider, securityEngine,
              Mockito.mock(inetsoft.report.LibManagerProvider.class),
              Mockito.mock(inetsoft.uql.service.DataSourceRegistry.class));
      repletRegistryManager = Mockito.mock(RepletRegistryManager.class);
      scheduleTaskFolderService = new ScheduleTaskFolderService(ScheduleManager.getScheduleManager(), securityEngine,
              securityProvider, Mockito.mock(inetsoft.util.IndexedStorage.class),
              Mockito.mock(inetsoft.uql.asset.sync.RenameTransformHandler.class));
      contentRepositoryTreeService = new ContentRepositoryTreeService(securityProvider,
              Mockito.mock(XRepository.class), resourcePermissionService,
              Mockito.mock(inetsoft.web.admin.content.repository.RepletRegistryService.class),
              scheduleTaskFolderService, Mockito.mock(inetsoft.mv.MVManager.class), securityEngine,
              ScheduleManager.getScheduleManager(), Mockito.mock(inetsoft.uql.service.DataSourceRegistry.class),
              Mockito.mock(inetsoft.sree.web.dashboard.DashboardManager.class),
              Mockito.mock(inetsoft.util.IndexedStorage.class),
              Mockito.mock(inetsoft.sree.web.dashboard.DashboardRegistryManager.class),
              Mockito.mock(inetsoft.report.LibManagerProvider.class),
              Mockito.mock(inetsoft.web.RecycleBin.class), repletRegistryManager);
      
      deployService = new DeployService(contentRepositoryTreeService, securityEngine,
              Mockito.mock(inetsoft.uql.service.DataSourceRegistry.class), Mockito.mock(inetsoft.util.IndexedStorage.class),
              Mockito.mock(inetsoft.sree.internal.DeployManagerService.class),
              Mockito.mock(inetsoft.sree.web.dashboard.DashboardRegistryManager.class),
              Mockito.mock(inetsoft.report.LibManagerProvider.class), Mockito.mock(inetsoft.util.FileSystemService.class),
              Mockito.mock(inetsoft.web.admin.content.repository.RepletRegistryService.class));
      composerVSTableServiceProxy = Mockito.mock(ComposerVSTableServiceProxy.class);
      composerVSTableController = new ComposerVSTableController(runtimeViewsheetRef, composerVSTableServiceProxy);
      composerVSTableService = new ComposerVSTableService(coreLifecycleService, objectTreeService, objectModelFactoryService,
              Mockito.mock(VSBindingService.class), assetRepository, viewsheetService, Mockito.mock(CrosstabDrillHandler.class),
              Mockito.mock(VSAssemblyInfoHandler.class));
      importXLSControllerService = new ImportXLSControllerService(viewsheetService, coreLifecycleService);
      importXLSControllerServiceProxy = Mockito.mock(ImportXLSControllerServiceProxy.class);
      importXLSController = new ImportXLSController(runtimeViewsheetRef, importXLSControllerServiceProxy,
              Mockito.mock(inetsoft.util.FileSystemService.class));
      
      importCSVDialogServiceProxy = Mockito.mock(ImportCSVDialogServiceProxy.class);
      importCSVDialogController = new ImportCSVDialogController(importCSVDialogServiceProxy, binaryTransferService) {
         public String getRuntimeId() {
            return ControllersResource.this.runtimeId;
         }
         
         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
         
         protected RuntimeWorksheet getRuntimeWorksheet(Principal principal) throws Exception {
            return worksheetService.getWorksheet(ControllersResource.this.runtimeId, principal);
         }
      };
      
      importCSVDialogService = new ImportCSVDialogService(viewsheetService, vsLayoutService, binaryTransferService,
              Mockito.mock(inetsoft.report.composition.execution.AssetDataCache.class),
              Mockito.mock(inetsoft.util.FileSystemService.class));
      
      vsChartBrushService = new VSChartBrushService(coreLifecycleService, viewsheetService,
              Mockito.mock(VSChartAreasServiceProxy.class));
      vsChartShowDetailsService = new VSChartShowDetailsService(viewsheetService, coreLifecycleService,
              Mockito.mock(VSChartAreasServiceProxy.class), Mockito.mock(VSDialogService.class));
      fileApiService = new FileApiService(deployService, contentRepositoryTreeService, securityProvider,
              Mockito.mock(inetsoft.uql.service.DataSourceRegistry.class), Mockito.mock(inetsoft.util.IndexedStorage.class),
              Mockito.mock(inetsoft.util.DataSpace.class), repletRegistryManager);
      
      DatabaseDatasourcesService databaseDatasourcesService = Mockito.mock(DatabaseDatasourcesService.class);
      DatabaseModelBrowserService databaseModelBrowserService = Mockito.mock(DatabaseModelBrowserService.class);
      DataModelFolderManagerService dataModelFolderManagerService = Mockito.mock(DataModelFolderManagerService.class);
      DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
      databaseDatasourcesController = new DatabaseDatasourcesController(databaseDatasourcesService, databaseModelBrowserService,
              dataModelFolderManagerService, dataSourceService, Mockito.mock(XRepository.class));
      
      openViewsheetService = new OpenViewsheetService(viewsheetService, objectTreeService);
   }
   
   public void initApplicationContext(ConfigurationContext context) {
      ConfigurationContext spyContext;
      // Check if context is already a mock - if so, use it directly; otherwise create a spy
      if(Mockito.mockingDetails(context).isMock()) {
         spyContext = context;
      }
      else {
         spyContext = spy(context);
      }
      
      //only for .WorksheetTest.groovy
      doReturn(worksheetEventService)
              .when(spyContext)
              .getSpringBean(WorksheetEventService.class);
      
      doReturn(importCSVDialogService)
              .when(spyContext)
              .getSpringBean(ImportCSVDialogService.class);
      
      //only for VSFormImportTest.groovy
      doReturn(importXLSControllerService)
              .when(spyContext)
              .getSpringBean(ImportXLSControllerService.class);
      
      // Check if we need to create a new static mock
      // If we already have one that's not closed, just update its behavior
      if(staticConfigurationContext != null && !staticConfigurationContext.isClosed()) {
         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);
         return;
      }
      
      // Close existing one if it exists and is not closed (cleanup)
      if(staticConfigurationContext != null && !staticConfigurationContext.isClosed()) {
         staticConfigurationContext.close();
      }
      
      // Try to create a new static mock
      try {
         staticConfigurationContext = mockStatic(ConfigurationContext.class);
         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);
      }
      catch(org.mockito.exceptions.base.MockitoException e) {
         // If there's already a static mock registered from another source,
         // we can't create a new one or configure the existing one
         if(e.getMessage() != null && e.getMessage().contains("static mocking is already registered")) {
            // In this case, we'll just skip setting up the static mock
            // The existing static mock from elsewhere will be used
            // Note: This means the static mock behavior won't be configured here
            return;
         }
         throw e; // Re-throw if it's a different exception
      }
   }
   
   public String getRuntimeId() {
      return runtimeId;
   }
   
   public ViewsheetService getViewsheetService() {
      return viewsheetService;
   }
   
   public OpenViewsheetController getOpenViewsheetController() {
      return openViewsheetController;
   }
   
   public OpenWorksheetController getOpenWorksheetController() {
      return openWorksheetController;
   }
   
   public WorksheetService getWorksheetService() {
      return worksheetService;
   }
   
   public VSExportService getVSExportService() {
      return vsExportService;
   }
   
   public ComposerVSTableController getComposerVSTableController() {
      return composerVSTableController;
   }
   
   public ImportXLSController getImportXLSController() {
      return importXLSController;
   }
   
   public ImportCSVDialogController getImportCSVDialogController() {
      return importCSVDialogController;
   }
   
   public VSChartShowDetailsService getVSChartShowDetailsService() {
      return vsChartShowDetailsService;
   }
   
   public VSChartBrushService getVSChartBrushService() {
      return vsChartBrushService;
   }
   
   public FileApiService getFileApiService() {
      return fileApiService;
   }
   
   public DatabaseDatasourcesController getDatabaseDatasourcesController() {
      return databaseDatasourcesController;
   }
   
   public CoreLifecycleService getCoreLifecycleService() {
      return coreLifecycleService;
   }
   
   public OpenViewsheetService getOpenViewsheetService() {
      return openViewsheetService;
   }
   
   public ComposerVSTableService getComposerVSTableService() {
      return composerVSTableService;
   }
   
   public ContentRepositoryTreeService getContentRepositoryTreeService() {
      return contentRepositoryTreeService;
   }
   
   public WorksheetEventService getWorksheetEventService() {
      return worksheetEventService;
   }
   
   public ImportCSVDialogService getImportCSVDialogService() {
      return importCSVDialogService;
   }
   
   private String runtimeId;
   private RuntimeViewsheetRef runtimeViewsheetRef;
   private ViewsheetService viewsheetService;
   private WorksheetService worksheetService;
   private VSLifecycleService vsLifecycleService;
   private RuntimeViewsheetManager runtimeViewsheetManager;
   private VSObjectModelFactoryService objectModelFactoryService;
   private VSObjectTreeService objectTreeService;
   private SecurityEngine securityEngine;
   private VSObjectService objectService;
   private VSBookmarkService bookmarkService;
   private DataRefModelFactoryService dataRefModelFactoryService;
   private AssetRepository assetRepository;
   private OpenViewsheetController openViewsheetController;
   private OpenWorksheetController openWorksheetController;
   private LicenseService licenseService;
   private VSExportService vsExportService;
   private SecurityProvider securityProvider;
   private ResourcePermissionService resourcePermissionService;
   private ContentRepositoryTreeService contentRepositoryTreeService;
   private RepletRegistryManager repletRegistryManager;
   private DeployService deployService;
   private ComposerVSTableController composerVSTableController;
   private ImportXLSController importXLSController;
   private ImportCSVDialogController importCSVDialogController;
   private ScheduleTaskFolderService scheduleTaskFolderService;
   private FileApiService fileApiService;
   private DatabaseDatasourcesController databaseDatasourcesController;
   private VSCompositionService vsCompositionService;
   private CoreLifecycleService coreLifecycleService;
   private WorksheetEventService worksheetEventService;
   private BinaryTransferService binaryTransferService;
   private ComposerVSTableServiceProxy composerVSTableServiceProxy;
   private ComposerVSTableService composerVSTableService;
   
   private ImportXLSControllerService importXLSControllerService;
   private ImportXLSControllerServiceProxy importXLSControllerServiceProxy;
   private ImportCSVDialogServiceProxy importCSVDialogServiceProxy;
   private VSChartBrushService vsChartBrushService;
   private SharedFilterService sharedFilterService;
   private ImportCSVDialogService importCSVDialogService;
   private OpenViewsheetService openViewsheetService;
   private VSChartShowDetailsService vsChartShowDetailsService;
   
   MockedStatic<ConfigurationContext> staticConfigurationContext;

   /**
    * Initializes a real Spring ApplicationContext using DataTestSpringConfig, which provides
    * mapdb storage and all engine beans needed after the epic-70095 singleton-to-Spring migration.
    * Safe to call multiple times — a no-op if already initialized.
    */
   public static void initSpringContext() {
      if(springContext != null && springContext.isActive()) {
         return;
      }

      // Register beans first, then set the application context on ConfigurationContext BEFORE
      // calling refresh(). This is necessary because Plugins' constructor calls
      // FileSystemService.getInstance() which goes through ConfigurationContext.getSpringBean(),
      // and that requires the application context to be set already.
      AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
      ctx.register(DataTestSpringConfig.class);
      ConfigurationContext.getContext().setApplicationContext(ctx);
      ctx.refresh();
      springContext = ctx;
   }

   private static AnnotationConfigApplicationContext springContext;
}
