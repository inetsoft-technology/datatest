package inetsoft.test.mv;

import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.uql.asset.internal.AssetUtil;
import inetsoft.uql.XFactory;
import inetsoft.util.ConfigurationContext;
import inetsoft.util.DataSpace;
import inetsoft.util.Tool;
import inetsoft.util.ThreadContext;
import inetsoft.web.admin.content.repository.*;
import inetsoft.web.admin.content.repository.MVService;
import inetsoft.web.admin.content.repository.MVSupportService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.enterprise.web.api.mv.*;
import org.mockito.MockedStatic;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;

public class MaterializedViewResource {
   /**
    * constructor
    * @param asset_id, the viewsheet asset_id
    */
   public MaterializedViewResource(String asset_id) throws Exception {
      context = ConfigurationContext.getContext();
      context.setHome(System.getProperty("sree.home"));

      Objects.requireNonNull(asset_id, "the asset not exist, check if asset_id is right");
      this.asset_id = asset_id;

      IdentityID identityUser = new IdentityID("admin", "host-org");

      IdentityID adminRole = new IdentityID();
      adminRole.setName("Administrator");
      IdentityID[] identityRoles = {adminRole, new IdentityID("Everyone", "host-org")};

      this.principal = new SRPrincipal(identityUser, identityRoles, new String[0], "host-org", Tool.getSecureRandom().nextLong());
      DataSpace.getDataSpace();
      ThreadContext.setContextPrincipal(principal);

      MVSupportService mvSupportService = MVSupportService.getInstance();
      SecurityEngine securityEngine = SecurityEngine.getSecurity();
      SecurityProvider securityProvider = securityEngine.getSecurityProvider();
      ResourcePermissionService resourcePermissionService = new ResourcePermissionService(securityProvider, securityEngine);
      RepletRegistryManager repletRegistryManager = new RepletRegistryManager();
      ScheduleTaskFolderService scheduleTaskFolderService = new ScheduleTaskFolderService(ScheduleManager.getScheduleManager(),
              securityEngine, securityProvider);
      ContentRepositoryTreeService contentRepositoryTreeService = new ContentRepositoryTreeService(securityProvider, XFactory.getRepository(),
            resourcePermissionService, repletRegistryManager, scheduleTaskFolderService);
      MVService mvService = new MVService(contentRepositoryTreeService, mvSupportService);
      AssetRepository assetRepository = AssetUtil.getAssetRepository(false);
      materializedViewApiService = new MaterializedViewApiService(mvService, assetRepository, mvSupportService);
      materializedViewApiController = new MaterializedViewApiController(materializedViewApiService, new MaterializedViewApiServiceProxy());
   }

   /**
    * create mv, for 1 time
    * @param applyVPM, apply vpm to mv
    * @param expandGroup, create for users in group
    */
   public void createMV(boolean applyVPM, boolean... expandGroup) {
      try (MockedStatic<ConfigurationContext> staticConfigurationContext = mockStatic(ConfigurationContext.class)) {
         ConfigurationContext spyContext = spy(context);

         doReturn(materializedViewApiService)
                 .when(spyContext)
                 .getSpringBean(MaterializedViewApiService.class);

         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);

         createMV0(applyVPM, 1, expandGroup);
      }catch(Exception e){
         System.err.println("==============exceptions============");
         e.printStackTrace();
      }
   }

   /**
    * analysis mv
    * @param applyVPM, apply vpm to mv
    * @param  expandGroup, create for users in group
    * @return the analysis job
    */
   public AnalysisJob analysisMV(boolean applyVPM, boolean expandGroup) {
      AnalyzeRequest analyzeRequest = new AnalyzeRequest();
      analyzeRequest.setViewsheetId(asset_id);
      analyzeRequest.setBypassVpm(!applyVPM);
      analyzeRequest.setExpandGroups(expandGroup);
      analyzeRequest.setFullData(true);

      AnalysisJob analysisJob = new AnalysisJob();
      try {
         analysisJob = materializedViewApiController.analyze(null, analyzeRequest, principal);

         //wait the analysis job complete or fail
         for (int retry = 0; retry < 80; retry++) {
            Thread.sleep(1000);
            AnalysisJob analysisJob1 = materializedViewApiController.getAnalysisJob(analysisJob.getId(),null, principal);
            if (analysisJob1.isComplete()) {
               break;
            }
            if (analysisJob1.isFailed()) {
               List<AnalysisError> errors = analysisJob1.getErrors();
               StringBuilder msg = new StringBuilder();
               for (AnalysisError error : errors) {
                  msg.append(error.toString());
               }
               System.err.println("====MV Analyze Exception====" + msg);
               break;
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }

      return analysisJob;
   }

   /**
    * create mv
    * @param applyVPM, apply vpm to mv
    * @param count, times to create mv
    * @param expandGroup, create for users in group
    */
   public void createMV0(boolean applyVPM, int count, boolean... expandGroup) {
      AnalysisJob analysisJob;

      if(expandGroup == null || expandGroup.length == 0) {
         analysisJob = analysisMV(applyVPM, false);
      }else {
         analysisJob = analysisMV(applyVPM, expandGroup[0]);
      }

      try {
         MaterializedViewList materializedViewList = materializedViewApiController
                 .getAnalysisJobViews(analysisJob.getId(), null, this.principal);
         List<MaterializedView> mvViews = materializedViewList.getViews();

         for (MaterializedView mvView : mvViews) {
            this.views.add(mvView.getName());
         }

         CreateMaterializedViewRequest createRequest = new CreateMaterializedViewRequest();
         createRequest.setViews(this.views);
         createRequest.setNoData(false);

         for(int i = 0; i < count; i++) {
            materializedViewApiController.createMaterializedView(analysisJob.getId(), null, createRequest, this.principal);

            //wait create mv complete or fail
            for (int retry = 0; retry < 10; retry++) {
               try {
                  CreateMaterializedViewStatus createStatus = materializedViewApiController.getCreationStatus(analysisJob.getId(), null, this.principal);

                  if (createStatus.isComplete()) {
                     break;
                  } else if (createStatus.isFailed()) {
                     String msg = createStatus.getError();
                     System.err.println("====MV Create Exception==== " + msg);
                     break;
                  }
               } catch (Exception e) {
                  if (e.getMessage().contains("The materialized view creation has not been started")) {
                     Thread.sleep(2000);
                  } else {
                     e.printStackTrace();
                     break;
                  }
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }



   /**
    * create incremental mv
    * @param applyVPM, apply vpm to mv
    * @param count, times to create mv
    * @param expandGroup, create for users in group
    */
   public void createIncrementMV(boolean applyVPM, int count, boolean... expandGroup) {
      try (MockedStatic<ConfigurationContext> staticConfigurationContext = mockStatic(ConfigurationContext.class)) {
         ConfigurationContext spyContext = spy(context);

         doReturn(materializedViewApiService)
                 .when(spyContext)
                 .getSpringBean(MaterializedViewApiService.class);

         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);

         createMV0(applyVPM, count, expandGroup);
      }catch(Exception e){
         System.err.println("=========createIncrementMV =====exceptions============");
         e.printStackTrace();
      }
   }

   /**
    * remove mv
    */
   public void removeMV() {
      try {
         DeleteMaterializedViewsRequest deleteRequest = new DeleteMaterializedViewsRequest();
         deleteRequest.setViews(this.views);
         materializedViewApiController.deleteMaterializedViews(null, deleteRequest, this.principal);
      }catch(Exception e){
         e.printStackTrace();
      }
   }

   private final MaterializedViewApiService materializedViewApiService;
   private final MaterializedViewApiController materializedViewApiController;
   private  ConfigurationContext context;
   private final Principal principal;
   private final String asset_id;
   List<String> views = new ArrayList<>();
}