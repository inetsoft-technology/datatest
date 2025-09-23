package inetsoft.test.core;

import inetsoft.analytic.composition.ViewsheetEngine;
import inetsoft.analytic.composition.ViewsheetService;
import inetsoft.enterprise.web.api.file.FileApiService;
import inetsoft.report.composition.RuntimeWorksheet;
import inetsoft.report.composition.WorksheetEngine;
import inetsoft.report.composition.WorksheetService;
import inetsoft.sree.internal.SUtil;
import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.uql.XFactory;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.util.ConfigurationContext;
import inetsoft.web.admin.content.database.model.DataModelFolderManagerService;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.DatabaseDatasourcesService;
import inetsoft.web.admin.content.repository.RepletRegistryManager;
import inetsoft.web.admin.content.repository.ResourcePermissionService;
import inetsoft.web.admin.deploy.DeployService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.web.binding.drm.*;
import inetsoft.web.binding.model.*;
import inetsoft.web.binding.service.*;
import inetsoft.web.composer.vs.VSObjectTreeService;
import inetsoft.web.composer.vs.controller.VSLayoutService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.assembly.WorksheetEventServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogService;
import inetsoft.web.portal.controller.database.DataSourceService;
import inetsoft.web.portal.controller.database.DatabaseModelBrowserService;
import inetsoft.web.portal.data.DatabaseDatasourcesController;
import inetsoft.web.service.LicenseService;
import inetsoft.web.viewsheet.controller.*;
import inetsoft.web.viewsheet.controller.chart.*;
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

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

public class ControllersResource extends MockMessageResource {

   public void initControllers() {
      mockMessage(this::createControllers);
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
      worksheetService = null;
      if (staticConfigurationContext != null) {
         staticConfigurationContext.close();
         staticConfigurationContext = null;
      }
   }

   private void createControllers() {
      viewsheetService = ViewsheetEngine.getViewsheetEngine();

      worksheetService = WorksheetEngine.getWorksheetService();

      RuntimeViewsheetRefServiceProxy runtimeViewsheetRefServiceProxy = new RuntimeViewsheetRefServiceProxy();

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

      runtimeViewsheetManager = new RuntimeViewsheetManager(viewsheetService, worksheetService);
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
         if (f instanceof DataRefModelWrapperFactory) {
            ((DataRefModelWrapperFactory) f).setDataRefModelFactoryService(dataRefModelFactoryService);
         }
      });

      ApplicationEventPublisher eventPublisher = event -> {
         if (bookmarkService != null && event instanceof ProcessBookmarkEvent pbe) {
            bookmarkService.onApplicationEvent(pbe);
         }
      };
      coreLifecycleService = new CoreLifecycleService(objectModelFactoryService, viewsheetService,
              vsLayoutService, parameterService, vsCompositionService, dataRefModelFactoryService,
              runtimeViewsheetRef, eventPublisher);
      sharedFilterService = new SharedFilterService(getMessagingTemplate(), viewsheetService);
      objectService = new VSObjectService(coreLifecycleService, viewsheetService, securityEngine, sharedFilterService);

      bookmarkService = new VSBookmarkService(objectService, viewsheetService, securityEngine, coreLifecycleService);

      vsLifecycleService = new VSLifecycleService(
              viewsheetService, assetRepository, coreLifecycleService, bookmarkService,
              dataRefModelFactoryService, vsCompositionService, parameterService, new VSLifecycleControllerServiceProxy());
      licenseService = new LicenseService();
      openViewsheetController = new OpenViewsheetController(
              runtimeViewsheetRef, runtimeViewsheetManager, vsLifecycleService, licenseService,
              new OpenViewsheetServiceProxy(), viewsheetService);

      worksheetEventService = new WorksheetEventService(viewsheetService, new WorksheetEventServiceProxy());
      openWorksheetController = new OpenWorksheetController(runtimeViewsheetManager, assetRepository, worksheetEventService) {
         protected WorksheetService getWorksheetEngine() {
            return worksheetService;
         }

         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
      };

      binaryTransferService = new BinaryTransferService();
      vsExportService = new VSExportService(viewsheetService, coreLifecycleService, parameterService);

      securityProvider = securityEngine.getSecurityProvider();
      resourcePermissionService = new ResourcePermissionService(securityProvider, securityEngine);
      repletRegistryManager = new RepletRegistryManager();
      scheduleTaskFolderService = new ScheduleTaskFolderService(ScheduleManager.getScheduleManager(), securityEngine,
              securityProvider);
      try {
         contentRepositoryTreeService = new ContentRepositoryTreeService(securityProvider, XFactory.getRepository(),
                 resourcePermissionService, repletRegistryManager, scheduleTaskFolderService);
      } catch (RemoteException e) {
         e.printStackTrace();
      }

      deployService = new DeployService(contentRepositoryTreeService, securityEngine);
      composerVSTableServiceProxy = new ComposerVSTableServiceProxy();
      composerVSTableController = new ComposerVSTableController(runtimeViewsheetRef, composerVSTableServiceProxy);
      importXLSControllerServiceProxy = new ImportXLSControllerServiceProxy();
      importXLSController = new ImportXLSController(runtimeViewsheetRef, importXLSControllerServiceProxy);

      importCSVDialogServiceProxy = new ImportCSVDialogServiceProxy();
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

      importCSVDialogService = new ImportCSVDialogService(viewsheetService, vsLayoutService, binaryTransferService);

      vsChartBrushService = new VSChartBrushService(coreLifecycleService, viewsheetService,
              new VSChartAreasServiceProxy());
      vsChartShowDetailsService = new VSChartShowDetailsService(viewsheetService, coreLifecycleService,
              new VSChartAreasServiceProxy(), new VSDialogService());
      fileApiService = new FileApiService(deployService, contentRepositoryTreeService, securityProvider);

      DatabaseDatasourcesService databaseDatasourcesService = Mockito.mock(DatabaseDatasourcesService.class);
      DatabaseModelBrowserService databaseModelBrowserService = Mockito.mock(DatabaseModelBrowserService.class);
      DataModelFolderManagerService dataModelFolderManagerService = Mockito.mock(DataModelFolderManagerService.class);
      DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
      databaseDatasourcesController = new DatabaseDatasourcesController(databaseDatasourcesService, databaseModelBrowserService,
              dataModelFolderManagerService, dataSourceService);

      openViewsheetService = new OpenViewsheetService(viewsheetService, objectTreeService);
   }

   public void initApplicationContext(ConfigurationContext context) {
      ConfigurationContext spyContext = spy(context);

      //only for .WorksheetTest.groovy
      doReturn(worksheetEventService)
              .when(spyContext)
              .getSpringBean(WorksheetEventService.class);

      doReturn(importCSVDialogService)
              .when(spyContext)
              .getSpringBean(ImportCSVDialogService.class);

      //only for checkConvert of VSCalcTest.groovy
      doReturn(openViewsheetController)
              .when(spyContext)
              .getSpringBean(OpenViewsheetController.class);

      doReturn(openViewsheetService)
              .when(spyContext)
              .getSpringBean(OpenViewsheetService.class);

      ComposerVSTableService composerVSTableService = mock(ComposerVSTableService.class);
      doReturn(composerVSTableService)
              .when(spyContext)
              .getSpringBean(ComposerVSTableService.class);

      if (staticConfigurationContext == null) {
         staticConfigurationContext = mockStatic(ConfigurationContext.class);
      }

      staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);
   }

   @Override
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
   private ImportXLSControllerServiceProxy importXLSControllerServiceProxy;
   private ImportCSVDialogServiceProxy importCSVDialogServiceProxy;
   private VSChartBrushService vsChartBrushService;
   private SharedFilterService sharedFilterService;
   private ImportCSVDialogService importCSVDialogService;
   private OpenViewsheetService openViewsheetService;
   private VSChartShowDetailsService vsChartShowDetailsService;

   MockedStatic<ConfigurationContext> staticConfigurationContext;
}
