package inetsoft.test.mv;

import inetsoft.sree.ClientInfo;
import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.uql.asset.internal.AssetUtil;
import inetsoft.uql.XFactory;
import inetsoft.util.Tool;
import inetsoft.util.ThreadContext;
import inetsoft.web.admin.content.repository.*;
import inetsoft.web.admin.content.repository.MVService;
import inetsoft.web.admin.content.repository.MVSupportService;
import inetsoft.web.admin.schedule.ScheduleTaskFolderService;
import inetsoft.enterprise.web.api.mv.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MaterializedViewResource {
   /**
    * constructor
    * @param asset_id, the viewsheet asset_id
    */
   public MaterializedViewResource(String asset_id) throws Exception {
      Objects.requireNonNull(asset_id, "the asset not exist, check if asset_id is right");
      this.asset_id = asset_id;

      IdentityID identityUser = new IdentityID("admin", "host-org");

      IdentityID adminRole = new IdentityID();
      adminRole.setName("Administrator");
      IdentityID[] identityRoles = {adminRole, new IdentityID("Everyone", "host-org")};

      this.principal = new SRPrincipal(identityUser, identityRoles, new String[0], "host-org", Tool.getSecureRandom().nextLong());
      ThreadContext.setContextPrincipal(principal);

      MVSupportService support = MVSupportService.getInstance();
      SecurityProvider securityProvider = SecurityEngine.getSecurity().getSecurityProvider();
      ResourcePermissionService resourcePermissionService = new ResourcePermissionService(securityProvider, SecurityEngine.getSecurity());
      RepletRegistryManager repletRegistryManager = new RepletRegistryManager();
      ScheduleTaskFolderService scheduleTaskFolderService = new ScheduleTaskFolderService(ScheduleManager.getScheduleManager(),
              SecurityEngine.getSecurity(), securityProvider);
      ContentRepositoryTreeService contentRepositoryTreeService = new ContentRepositoryTreeService(securityProvider, XFactory.getRepository(),
            resourcePermissionService, repletRegistryManager, scheduleTaskFolderService);
      MVService service = new MVService(contentRepositoryTreeService, support);
      AssetRepository assetRepository = AssetUtil.getAssetRepository(false);
      this.materializedViewApiService = new MaterializedViewApiService(service, assetRepository, support);
   }

   /**
    * analysis mv
    * @param applyVPM, apply vpm to mv
    * @param  expandGroup, create for users in group
    * @return the analysis job
    */
   public AnalysisJob analysisMV(boolean applyVPM, boolean expandGroup) {
      AnalyzeRequest analyzeRequest = new AnalyzeRequest();
      analyzeRequest.setViewsheetId(this.asset_id);
      analyzeRequest.setBypassVpm(!applyVPM);
      analyzeRequest.setExpandGroups(expandGroup);
      analyzeRequest.setFullData(true);

      AnalysisJob analysisJob = new AnalysisJob();
      try {
         analysisJob = this.materializedViewApiService.analyze(analyzeRequest, this.principal);

         //wait the analysis job complete or fail
         for(int i = 0; i < 1; i--) {
            AnalysisJob analysisJob1 = this.materializedViewApiService.getAnalysisJob(analysisJob.getId(), this.principal);
            if(analysisJob1.isComplete()) {
               break;
            } else if(analysisJob1.isFailed()) {
               List<AnalysisError> errors = analysisJob1.getErrors();
               StringBuilder msg = new StringBuilder();
               for (AnalysisError error : errors) {
                  msg.append(error.toString());
               }
               System.err.println("====MV Analyze Exception====" + msg);
               Thread.sleep(10);
               break;
            } else if (!analysisJob1.isComplete()) {
               Thread.sleep(10);
               if(analysisJob1.isComplete()) {
                  break;
               }
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
         MaterializedViewList materializedViewList;
         CreateMaterializedViewRequest createRequest = new CreateMaterializedViewRequest();
         materializedViewList = this.materializedViewApiService.getAnalysisJobViews(analysisJob.getId(), this.principal);
         List<MaterializedView> mvViews = materializedViewList.getViews();

         for (MaterializedView mvView : mvViews) {
            this.views.add(mvView.getName());
         }

         createRequest.setViews(this.views);
         createRequest.setNoData(false);

         for(int i = 0; i < count; i++) {
            this.materializedViewApiService.createMaterializedView(analysisJob.getId(), createRequest, this.principal);

            //wait create mv complete or fail
            for(int j = 0; j < 1; j--) {
               CreateMaterializedViewStatus createStatus =
                       this.materializedViewApiService.getCreationStatus(analysisJob.getId(), this.principal);
               if(createStatus.isComplete()){
                  break;
               } else if(createStatus.isFailed()){
                  String msg = createStatus.getError();
                  System.err.println("====MV Create Exception====" + msg);
                  break;
               } else if (!createStatus.isComplete()) {
                  Thread.sleep(10);
                  if(createStatus.isComplete()) {
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
    * create mv, for 1 time
    * @param applyVPM, apply vpm to mv
    * @param expandGroup, create for users in group
    */
   public void createMV(boolean applyVPM, boolean... expandGroup) {
      try {
         createMV0(applyVPM, 1, expandGroup);
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
      try {
         createMV0(applyVPM, count, expandGroup);
      }catch(Exception e){
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
         this.materializedViewApiService.deleteMaterializedViews(deleteRequest, this.principal);
      }catch(Exception e){
         e.printStackTrace();
      }
   }

   private final MaterializedViewApiService materializedViewApiService;
   private final Principal principal;
   private final String asset_id;
   List<String> views = new ArrayList<>();
}