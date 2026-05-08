package inetsoft.test.core;

import inetsoft.enterprise.web.api.file.FileApiService;
import inetsoft.analytic.composition.ViewsheetService;
import inetsoft.report.composition.WorksheetService;
import inetsoft.sree.AnalyticRepository;
import inetsoft.sree.RepletRegistryManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.uql.XRepository;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.util.ConfigurationContext;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.ResourcePermissionService;
import inetsoft.web.admin.deploy.DeployService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableService;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.assembly.WorksheetEventService;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogService;
import inetsoft.web.portal.data.DatabaseDatasourcesController;
import inetsoft.web.service.BinaryTransferService;
import inetsoft.web.viewsheet.controller.ImportXLSController;
import inetsoft.web.viewsheet.controller.ImportXLSControllerService;
import inetsoft.web.viewsheet.controller.OpenViewsheetController;
import inetsoft.web.viewsheet.service.RuntimeViewsheetManager;
import inetsoft.web.viewsheet.model.RuntimeViewsheetRef;
import inetsoft.web.viewsheet.controller.chart.VSChartBrushService;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsService;
import inetsoft.web.viewsheet.model.VSObjectModelFactoryService;
import inetsoft.web.viewsheet.service.CoreLifecycleService;
import inetsoft.web.binding.service.DataRefModelFactoryService;
import inetsoft.web.service.LicenseService;
import inetsoft.web.viewsheet.controller.OpenViewsheetService;
import inetsoft.web.viewsheet.service.SharedFilterService;
import inetsoft.web.viewsheet.service.VSBookmarkService;
import inetsoft.web.viewsheet.service.VSExportService;
import inetsoft.web.viewsheet.service.VSLifecycleService;
import inetsoft.web.viewsheet.service.VSObjectService;
import inetsoft.web.composer.vs.VSObjectTreeService;
import inetsoft.web.viewsheet.service.VSCompositionService;

public class ControllersResource {

   /**
    * Populates all controller/service fields from the Spring application context.
    * Must be called after the Spring context is fully initialized (i.e. after
    * {@link DatatestRuntimeBootstrap#bootstrap()} or after {@code @ContextConfiguration}
    * has set up the context).
    */
   public void initControllers() {
      MessageTestUtils.withMockMessageContext(this::loadFromSpring);
   }

   private void loadFromSpring() {
      ConfigurationContext ctx = ConfigurationContext.getContext();
      viewsheetService = ctx.getSpringBean(ViewsheetService.class);
      worksheetService = ctx.getSpringBean(WorksheetService.class);
      runtimeViewsheetRef = ctx.getSpringBean(RuntimeViewsheetRef.class);
      runtimeViewsheetManager = ctx.getSpringBean(RuntimeViewsheetManager.class);
      objectModelFactoryService = ctx.getSpringBean(VSObjectModelFactoryService.class);
      assetRepository = ctx.getSpringBean(AnalyticRepository.class).unwrap(AssetRepository.class);
      objectTreeService = ctx.getSpringBean(VSObjectTreeService.class);
      securityEngine = ctx.getSpringBean(SecurityEngine.class);
      vsCompositionService = ctx.getSpringBean(VSCompositionService.class);
      dataRefModelFactoryService = ctx.getSpringBean(DataRefModelFactoryService.class);
      coreLifecycleService = ctx.getSpringBean(CoreLifecycleService.class);
      sharedFilterService = ctx.getSpringBean(SharedFilterService.class);
      objectService = ctx.getSpringBean(VSObjectService.class);
      bookmarkService = ctx.getSpringBean(VSBookmarkService.class);
      vsLifecycleService = ctx.getSpringBean(VSLifecycleService.class);
      licenseService = ctx.getSpringBean(LicenseService.class);
      openViewsheetController = ctx.getSpringBean(OpenViewsheetController.class);
      openWorksheetController = ctx.getSpringBean(OpenWorksheetController.class);
      worksheetEventService = ctx.getSpringBean(WorksheetEventService.class);
      binaryTransferService = ctx.getSpringBean(BinaryTransferService.class);
      vsExportService = ctx.getSpringBean(VSExportService.class);
      resourcePermissionService = ctx.getSpringBean(ResourcePermissionService.class);
      repletRegistryManager = ctx.getSpringBean(RepletRegistryManager.class);
      scheduleTaskFolderService = ctx.getSpringBean(ScheduleTaskFolderService.class);
      contentRepositoryTreeService = ctx.getSpringBean(ContentRepositoryTreeService.class);
      deployService = ctx.getSpringBean(DeployService.class);
      composerVSTableController = ctx.getSpringBean(ComposerVSTableController.class);
      composerVSTableService = ctx.getSpringBean(ComposerVSTableService.class);
      importXLSControllerService = ctx.getSpringBean(ImportXLSControllerService.class);
      importXLSController = ctx.getSpringBean(ImportXLSController.class);
      importCSVDialogController = ctx.getSpringBean(ImportCSVDialogController.class);
      importCSVDialogService = ctx.getSpringBean(ImportCSVDialogService.class);
      vsChartBrushService = ctx.getSpringBean(VSChartBrushService.class);
      vsChartShowDetailsService = ctx.getSpringBean(VSChartShowDetailsService.class);
      fileApiService = ctx.getSpringBean(FileApiService.class);
      databaseDatasourcesController = ctx.getSpringBean(DatabaseDatasourcesController.class);
      openViewsheetService = ctx.getSpringBean(OpenViewsheetService.class);
   }

   /** No-op: all beans are Spring singletons; the context owns their lifecycle. */
   public void destroy() {
   }

   /** No-op: Spring context is already correctly configured. */
   public void initApplicationContext(ConfigurationContext context) {
   }

   public String getRuntimeId() {
      return runtimeViewsheetRef != null ? runtimeViewsheetRef.getRuntimeId() : null;
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
   private ComposerVSTableService composerVSTableService;
   private ImportXLSControllerService importXLSControllerService;
   private ImportCSVDialogService importCSVDialogService;
   private VSChartBrushService vsChartBrushService;
   private SharedFilterService sharedFilterService;
   private OpenViewsheetService openViewsheetService;
   private VSChartShowDetailsService vsChartShowDetailsService;
}
