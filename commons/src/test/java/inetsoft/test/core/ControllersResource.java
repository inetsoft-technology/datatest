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
import inetsoft.web.admin.content.plugins.PluginsService;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.RepletRegistryManager;
import inetsoft.web.admin.content.repository.ResourcePermissionService;
import inetsoft.web.admin.deploy.DeployService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.web.admin.upload.MavenClientService;
import inetsoft.web.admin.upload.UploadService;
import inetsoft.web.binding.drm.*;
import inetsoft.web.binding.handler.VSAssemblyInfoHandler;
import inetsoft.web.binding.model.*;
import inetsoft.web.binding.service.*;
import inetsoft.web.composer.vs.VSObjectTreeService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.WorksheetController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.service.LicenseService;
import inetsoft.web.viewsheet.controller.AssemblyImageService;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.controller.OpenViewsheetController;
import inetsoft.web.viewsheet.controller.ViewsheetController;
import inetsoft.web.viewsheet.controller.chart.VSChartBrushController;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsController;
import inetsoft.web.viewsheet.controller.table.BaseTableLoadDataController;
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

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

public class ControllersResource extends MockMessageResource {

   public void initControllers() {
      mockMessage(this::createControllers);
   }

   public void destroy() {
      viewsheetService = null;
      vsLifecycleService = null;
      runtimeViewsheetManager = null;
      objectModelFactoryService = null;
      placeholderService = null;
      viewsheetController = null;
      objectTreeService = null;
      securityEngine = null;
      objectService = null;
      bookmarkService = null;
      dataRefModelFactoryService = null;
      assetRepository = null;
      openViewsheetController = null;
      selectionService = null;
      imageService = null;
      worksheetService = null;
   }

   private void createControllers() {
      viewsheetService = ViewsheetEngine.getViewsheetEngine();
      worksheetService = WorksheetEngine.getWorksheetService();

      runtimeViewsheetRef = new RuntimeViewsheetRef(viewsheetService) {
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
            new VSUploadModel.VSUploadModelFactory(),
            new VSViewsheetModel.VSViewsheetModelFactory(),
            new VSAnnotationModel.VSAnnotationModelFactory(),
            new VSCalendarModel.VSCalendarModelFactory(),
            new VSChartModel.VSChartModelFactory(),
            new VSGroupContainerModel.VSGaugeModelFactory(),
            new VSTabModel.VSTabModelFactory()
      );
      objectModelFactoryService = new VSObjectModelFactoryService(modelFactories);
      placeholderService =
            new PlaceholderService(objectModelFactoryService, getMessagingTemplate(), viewsheetService);
      assetRepository = (AssetRepository) SUtil.getRepletRepository();
      objectTreeService = new VSObjectTreeService(objectModelFactoryService);
      securityEngine = SecurityEngine.getSecurity();

      objectService = new VSObjectService(placeholderService, viewsheetService, securityEngine);
      bookmarkService = new VSBookmarkService(objectService);
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

      vsLifecycleService = new VSLifecycleService(
            viewsheetService, assetRepository, placeholderService, bookmarkService,
            dataRefModelFactoryService);
      viewsheetController = new ViewsheetController(
            runtimeViewsheetRef, runtimeViewsheetManager, vsLifecycleService);
      licenseService = new LicenseService();
      openViewsheetController = new OpenViewsheetController(
            runtimeViewsheetRef, runtimeViewsheetManager, objectTreeService, viewsheetService,
            vsLifecycleService, licenseService);
      worksheetController = new WorksheetController() {
         protected WorksheetService getWorksheetEngine() {
            return worksheetService;
         }

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

      openWorksheetController = new OpenWorksheetController(runtimeViewsheetManager, assetRepository) {
         protected WorksheetService getWorksheetEngine() {
            return worksheetService;
         }
         protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
            return runtimeViewsheetRef;
         }
      };

      baseTableLoadDataController =
            new BaseTableLoadDataController(runtimeViewsheetRef, placeholderService, viewsheetService);
      maxModeAssemblyService = new MaxModeAssemblyService(placeholderService);
      selectionService = new VSSelectionService(placeholderService, viewsheetService, maxModeAssemblyService);
      imageService = new AssemblyImageService(viewsheetService);
      vsExportService = new VSExportService(viewsheetService, placeholderService);

      securityProvider = SecurityEngine.getSecurity().getSecurityProvider();
      resourcePermissionService = new ResourcePermissionService(securityProvider, SecurityEngine.getSecurity());
      repletRegistryManager = new RepletRegistryManager();
      scheduleTaskFolderService = new ScheduleTaskFolderService(ScheduleManager.getScheduleManager(), SecurityEngine.getSecurity(),
              securityProvider);
      try {
         contentRepositoryTreeService = new ContentRepositoryTreeService(securityProvider, XFactory.getRepository(),
               resourcePermissionService, repletRegistryManager, scheduleTaskFolderService);
      } catch (RemoteException e) {
         e.printStackTrace();
      }

      deployService = new DeployService(contentRepositoryTreeService,  SecurityEngine.getSecurity());
      vsassemblyInfoHandler = new VSAssemblyInfoHandler(placeholderService, dataRefModelFactoryService);
      crosstabDrillHandler = new CrosstabDrillHandler(viewsheetService, placeholderService, runtimeViewsheetRef);
      composerVSTableController = new ComposerVSTableController(runtimeViewsheetRef, placeholderService, objectTreeService,
         objectModelFactoryService, null, assetRepository, viewsheetService, crosstabDrillHandler, vsassemblyInfoHandler);
      importXLSController = new ImportXLSController(runtimeViewsheetRef, viewsheetService, placeholderService);
      importCSVDialogController = new ImportCSVDialogController(placeholderService) {
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
      vschartShowDetailsController = new VSChartShowDetailsController(runtimeViewsheetRef, placeholderService, viewsheetService);
      vschartBrushController = new VSChartBrushController(runtimeViewsheetRef, placeholderService, viewsheetService);

      pluginsService = new PluginsService(new UploadService(new MavenClientService()), securityEngine);

      fileApiService = new FileApiService(deployService, contentRepositoryTreeService);
   }

   @Override
   public String getRuntimeId() {
      return runtimeId;
   }

   public ViewsheetService getViewsheetService() {
      return viewsheetService;
   }

   public PlaceholderService getPlaceholderService() {
      return placeholderService;
   }

   public OpenViewsheetController getOpenViewsheetController() {
      return openViewsheetController;
   }

   public OpenWorksheetController getOpenWorksheetController() { return openWorksheetController; }

   public WorksheetService getWorksheetService() { return  worksheetService; }

   public VSExportService getVSExportService() { return vsExportService; }

   public ComposerVSTableController getComposerVSTableController() { return composerVSTableController; }

   public ImportXLSController getImportXLSController() { return importXLSController; }

   public ImportCSVDialogController getImportCSVDialogController() { return importCSVDialogController; }

   public VSChartShowDetailsController getVSChartShowDetailsController() { return vschartShowDetailsController; }

   public VSChartBrushController getVSChartBrushController() { return vschartBrushController; }

   public PluginsService getPluginsService() { return  pluginsService; }

   public FileApiService getFileApiService() { return fileApiService; }

   private String runtimeId;
   private RuntimeViewsheetRef runtimeViewsheetRef;
   private ViewsheetService viewsheetService;
   private WorksheetService worksheetService;
   private VSLifecycleService vsLifecycleService;
   private RuntimeViewsheetManager runtimeViewsheetManager;
   private VSObjectModelFactoryService objectModelFactoryService;
   private PlaceholderService placeholderService;
   private ViewsheetController viewsheetController;
   private WorksheetController worksheetController;
   private VSObjectTreeService objectTreeService;
   private SecurityEngine securityEngine;
   private VSObjectService objectService;
   private VSBookmarkService bookmarkService;
   private DataRefModelFactoryService dataRefModelFactoryService;
   private AssetRepository assetRepository;
   private OpenViewsheetController openViewsheetController;
   private OpenWorksheetController openWorksheetController;
   private BaseTableLoadDataController baseTableLoadDataController;
   private LicenseService licenseService;
   private VSSelectionService selectionService;
   private AssemblyImageService imageService;
   private VSExportService vsExportService;
   private SecurityProvider securityProvider;
   private ResourcePermissionService resourcePermissionService;
   private ContentRepositoryTreeService contentRepositoryTreeService;
   private RepletRegistryManager repletRegistryManager;
   private DeployService deployService;
   private ComposerVSTableController composerVSTableController;
   private CrosstabDrillHandler  crosstabDrillHandler;
   private ImportXLSController importXLSController;
   private VSAssemblyInfoHandler vsassemblyInfoHandler;
   private ImportCSVDialogController importCSVDialogController;
   private MaxModeAssemblyService maxModeAssemblyService;
   private VSChartShowDetailsController vschartShowDetailsController;
   private VSChartBrushController vschartBrushController;
   private ScheduleTaskFolderService scheduleTaskFolderService;
   private PluginsService pluginsService;

   private FileApiService fileApiService;
}
