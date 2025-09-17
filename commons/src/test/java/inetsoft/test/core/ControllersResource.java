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
import inetsoft.web.admin.content.plugins.PluginsService;
import inetsoft.web.admin.content.repository.ContentRepositoryTreeService;
import inetsoft.web.admin.content.repository.DatabaseDatasourcesService;
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
import inetsoft.web.composer.vs.controller.VSLayoutService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableController;
import inetsoft.web.composer.ws.OpenWorksheetController;
import inetsoft.web.composer.ws.WorksheetController;
import inetsoft.web.composer.ws.assembly.WorksheetEventServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogController;
import inetsoft.web.portal.controller.database.DataSourceService;
import inetsoft.web.portal.controller.database.DatabaseModelBrowserService;
import inetsoft.web.portal.data.DatabaseDatasourcesController;
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
import inetsoft.web.viewsheet.service.CoreLifecycleService;
import inetsoft.web.viewsheet.controller.ViewsheetControllerServiceProxy;
import inetsoft.web.viewsheet.controller.OpenViewsheetServiceProxy;
import inetsoft.web.viewsheet.controller.table.BaseTableLoadDataServiceProxy;
import inetsoft.web.composer.ws.assembly.WorksheetEventService;
import inetsoft.web.service.BinaryTransferService;
import inetsoft.web.composer.vs.objects.controller.ComposerVSTableServiceProxy;
import inetsoft.web.viewsheet.controller.ImportXLSControllerServiceProxy;
import inetsoft.web.composer.ws.dialog.ImportCSVDialogServiceProxy;
import inetsoft.web.viewsheet.controller.chart.VSChartShowDetailsServiceProxy;
import inetsoft.web.viewsheet.controller.chart.VSChartBrushServiceProxy;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import spock.lang.Shared;

import static org.mockito.Mockito.doReturn;

public class ControllersResource extends MockMessageResource {

    public void initControllers() {
        mockMessage(this::createControllers);
    }

    public void destroy() {
        viewsheetService = null;
        vsLifecycleService = null;
        runtimeViewsheetManager = null;
        objectModelFactoryService = null;
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
        coreLifecycleService = new CoreLifecycleService(objectModelFactoryService, viewsheetService, vsLayoutService, parameterService, new CoreLifecycleControllerServiceProxy(), vsCompositionService);
        sharedFilterService = new SharedFilterService(getMessagingTemplate(), viewsheetService);
        objectService = new VSObjectService(coreLifecycleService, viewsheetService, securityEngine, sharedFilterService);

        bookmarkService = new VSBookmarkService(objectService, viewsheetService, securityEngine, coreLifecycleService);
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

        vsLifecycleService = new VSLifecycleService(
                viewsheetService, assetRepository, coreLifecycleService, bookmarkService,
                dataRefModelFactoryService, vsCompositionService, parameterService, new VSLifecycleControllerServiceProxy());
        viewsheetController = new ViewsheetController(
                runtimeViewsheetRef, new ViewsheetControllerServiceProxy());
        licenseService = new LicenseService();
        openViewsheetController = new OpenViewsheetController(
                runtimeViewsheetRef, runtimeViewsheetManager, vsLifecycleService, licenseService,
                new OpenViewsheetServiceProxy(), viewsheetService);
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

        worksheetEventService = new WorksheetEventService(viewsheetService, Mockito.mock(WorksheetEventServiceProxy.class));
        openWorksheetController = new OpenWorksheetController(runtimeViewsheetManager, assetRepository, worksheetEventService) {
            protected WorksheetService getWorksheetEngine() {
                return worksheetService;
            }

            protected RuntimeViewsheetRef getRuntimeViewsheetRef() {
                return runtimeViewsheetRef;
            }
        };

        baseTableLoadDataServiceProxy = new BaseTableLoadDataServiceProxy();
        baseTableLoadDataController =
                new BaseTableLoadDataController(runtimeViewsheetRef, baseTableLoadDataServiceProxy);
        maxModeAssemblyService = new MaxModeAssemblyService(viewsheetService, coreLifecycleService);
        selectionService = new VSSelectionService(coreLifecycleService, viewsheetService, maxModeAssemblyService, sharedFilterService);
        binaryTransferService = new BinaryTransferService();
        imageService = new AssemblyImageService(viewsheetService, binaryTransferService);
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
        vsassemblyInfoHandler = new VSAssemblyInfoHandler(coreLifecycleService, dataRefModelFactoryService, parameterService);
        crosstabDrillHandler = new CrosstabDrillHandler(viewsheetService, coreLifecycleService, runtimeViewsheetRef);
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
        vsChartShowDetailsServiceProxy = new VSChartShowDetailsServiceProxy();
        vsChartBrushServiceProxy = new VSChartBrushServiceProxy();
        vschartShowDetailsController = new VSChartShowDetailsController(runtimeViewsheetRef, vsChartShowDetailsServiceProxy);
        vschartBrushController = new VSChartBrushController(runtimeViewsheetRef, vsChartBrushServiceProxy);
        fileApiService = new FileApiService(deployService, contentRepositoryTreeService, securityProvider);

        DatabaseDatasourcesService databaseDatasourcesService = Mockito.mock(DatabaseDatasourcesService.class);
        DatabaseModelBrowserService databaseModelBrowserService = Mockito.mock(DatabaseModelBrowserService.class);
        DataModelFolderManagerService dataModelFolderManagerService = Mockito.mock(DataModelFolderManagerService.class);
        DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
        databaseDatasourcesController = new DatabaseDatasourcesController(databaseDatasourcesService, databaseModelBrowserService,
                dataModelFolderManagerService, dataSourceService);
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

    public VSChartShowDetailsController getVSChartShowDetailsController() {
        return vschartShowDetailsController;
    }

    public VSChartBrushController getVSChartBrushController() {
        return vschartBrushController;
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
    private CrosstabDrillHandler crosstabDrillHandler;
    private ImportXLSController importXLSController;
    private VSAssemblyInfoHandler vsassemblyInfoHandler;
    private ImportCSVDialogController importCSVDialogController;
    private MaxModeAssemblyService maxModeAssemblyService;
    private VSChartShowDetailsController vschartShowDetailsController;
    private VSChartBrushController vschartBrushController;
    private ScheduleTaskFolderService scheduleTaskFolderService;
    private PluginsService pluginsService;

    private FileApiService fileApiService;

    private DatabaseDatasourcesController databaseDatasourcesController;
    private VSCompositionService vsCompositionService;
    private CoreLifecycleService coreLifecycleService;
    private WorksheetEventService worksheetEventService;
    private BaseTableLoadDataServiceProxy baseTableLoadDataServiceProxy;
    private BinaryTransferService binaryTransferService;
    private ComposerVSTableServiceProxy composerVSTableServiceProxy;
    private ImportXLSControllerServiceProxy importXLSControllerServiceProxy;
    private ImportCSVDialogServiceProxy importCSVDialogServiceProxy;
    private VSChartShowDetailsServiceProxy vsChartShowDetailsServiceProxy;
    private VSChartBrushServiceProxy vsChartBrushServiceProxy;

    private SharedFilterService sharedFilterService;

    private CoreLifecycleControllerService coreLifecycleControllerService;
}
