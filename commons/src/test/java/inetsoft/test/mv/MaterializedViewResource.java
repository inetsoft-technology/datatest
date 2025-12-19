package inetsoft.test.mv;

import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.test.core.MessageTestUtils;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.uql.asset.internal.AssetUtil;
import inetsoft.util.ConfigurationContext;
import inetsoft.util.DataSpace;
import inetsoft.util.Tool;
import inetsoft.util.ThreadContext;
import inetsoft.web.admin.content.repository.*;
import inetsoft.web.admin.content.repository.MVService;
import inetsoft.web.admin.content.repository.MVSupportService;
import inetsoft.enterprise.web.api.mv.*;

import inetsoft.test.core.ControllersResource;

import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.*;

public class MaterializedViewResource {
   /**
    * constructor
    *
    * @param asset_id, the viewsheet asset_id
    */
   public MaterializedViewResource(String asset_id, ControllersResource controllersResource) throws Exception {
      
      context = ConfigurationContext.getContext();
      context.setHome(System.getProperty("sree.home"));
      
      Objects.requireNonNull(controllersResource, "controllersResource cannot be null");
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
      ContentRepositoryTreeService contentRepositoryTreeService = controllersResource.getContentRepositoryTreeService();
      MVService mvService = new MVService(contentRepositoryTreeService, mvSupportService);
      AssetRepository assetRepository = AssetUtil.getAssetRepository(false);
      materializedViewApiService = new MaterializedViewApiService(mvService, assetRepository, mvSupportService);
      materializedViewApiController = new MaterializedViewApiController(materializedViewApiService, new MaterializedViewApiServiceProxy());
   }
   
   /**
    * create mv, for 1 time
    *
    * @param applyVPM,    apply vpm to mv
    * @param expandGroup, create for users in group
    */
   public void createMV(boolean applyVPM, boolean... expandGroup) {
      try(MockedStatic<ConfigurationContext> staticConfigurationContext = mockStatic(ConfigurationContext.class)) {
         ConfigurationContext spyContext = spy(context);
         
         doReturn(materializedViewApiService)
                 .when(spyContext)
                 .getSpringBean(MaterializedViewApiService.class);
         
         staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);
         
         createMV0(applyVPM, expandGroup);
      }
      catch(Exception e) {
         System.err.println("==============exceptions============");
         e.printStackTrace();
      }
   }
   
   /**
    * analysis mv
    *
    * @param applyVPM,    apply vpm to mv
    * @param expandGroup, create for users in group
    * @return the analysis job
    */
   public AnalysisJob analysisMV(boolean applyVPM, boolean expandGroup) {
      AnalyzeRequest analyzeRequest = new AnalyzeRequest();
      analyzeRequest.setViewsheetId(asset_id);
      analyzeRequest.setBypassVpm(!applyVPM);
      analyzeRequest.setExpandGroups(expandGroup);
      analyzeRequest.setFullData(true);
      
      return MessageTestUtils.withMockMessageContext(this.principal, null, analyzeRequest, (ctx, req) -> {
         AnalysisJob analysisJob = new AnalysisJob();
         try {
            analysisJob = materializedViewApiController.analyze(null, analyzeRequest, principal);
            
            //wait the analysis job complete or fail
            for(int retry = 0; retry < 800; retry++) {
               AnalysisJob analysisJob1 = materializedViewApiController.getAnalysisJob(analysisJob.getId(), null, principal);
               if(analysisJob1.isComplete()) {
                  break;
               }
               if(analysisJob1.isFailed()) {
                  List<AnalysisError> errors = analysisJob1.getErrors();
                  StringBuilder msg = new StringBuilder();
                  for(AnalysisError error : errors) {
                     msg.append(error.toString());
                  }
                  System.err.println("====MV Analyze Exception====" + msg);
                  break;
               }
               Thread.sleep(100);
            }
         }
         catch(RuntimeException e) {
            throw e;
         }
         catch(Exception e) {
            throw new RuntimeException("Failed to analyze MV", e);
         }
         
         return analysisJob;
      });
   }
   
   /**
    * create mv
    *
    * @param applyVPM,    apply vpm to mv
    * @param expandGroup, create for users in group
    */
   public void createMV0(boolean applyVPM, boolean... expandGroup) {
      AnalysisJob analysisJob;
      
      if(expandGroup == null || expandGroup.length == 0) {
         analysisJob = analysisMV(applyVPM, false);
      }
      else {
         analysisJob = analysisMV(applyVPM, expandGroup[0]);
      }
      
      MessageTestUtils.withMockMessageContext(this.principal, null, () -> {
         try {
            MaterializedViewList materializedViewList = materializedViewApiController
                    .getAnalysisJobViews(analysisJob.getId(), null, this.principal);
            List<MaterializedView> mvViews = materializedViewList.getViews();
            
            for(MaterializedView mvView : mvViews) {
               this.views.add(mvView.getName());
            }
            
            CreateMaterializedViewRequest createRequest = new CreateMaterializedViewRequest();
            createRequest.setViews(this.views);
            createRequest.setNoData(false);
            
            materializedViewApiController.createMaterializedView(
                    analysisJob.getId(), this.principal.getIdentityID().getOrgID(), createRequest, this.principal);
            
            //wait create mv complete or fail
            for(int retry = 0; retry < 200; retry++) {
               try {
                  CreateMaterializedViewStatus createStatus = materializedViewApiController.getCreationStatus(
                          analysisJob.getId(), null, this.principal);
                  
                  if(createStatus.isComplete()) {
                     break;
                  }
                  else if(createStatus.isFailed()) {
                     String msg = createStatus.getError();
                     System.err.println("====MV Create Exception==== " + msg);
                     break;
                  }
               }
               catch(Exception e) {
                  if(e.getMessage().contains("The materialized view creation has not been started")) {
                     Thread.sleep(100);
                  }
                  else {
                     e.printStackTrace();
                     break;
                  }
               }
            }
         }
         catch(Exception e) {
            e.printStackTrace();
         }
      });
   }
   
   /**
    * create incremental mv
    *
    * @param count, times to create incremental mv
    */
   public void createIncrementMV(int count) {
      MessageTestUtils.withMockMessageContext(this.principal, null, () -> {
         try(MockedStatic<ConfigurationContext> staticConfigurationContext = mockStatic(ConfigurationContext.class)) {
            ConfigurationContext spyContext = spy(context);
            
            doReturn(materializedViewApiService)
                    .when(spyContext)
                    .getSpringBean(MaterializedViewApiService.class);
            
            staticConfigurationContext.when(ConfigurationContext::getContext).thenReturn(spyContext);
            
            UpdateMaterializedViewRequest updateRequest = new UpdateMaterializedViewRequest();
            updateRequest.setViews(this.views);
            
            for(int i = 0; i < count; i++) {
               UpdateMaterializedViewStatus updateStatus =
                       materializedViewApiController.update(this.principal.getIdentityID().getOrgID(), updateRequest, this.principal);
               
               //wait update mv complete or fail
               for(int retry = 0; retry < 200; retry++) {
                  if(updateStatus.isComplete()) {
                     break;
                  }
                  else if(updateStatus.isFailed()) {
                     String msg = updateStatus.getError();
                     System.err.println("====MV Update Exception==== " + msg);
                     break;
                  }
                  //Thread.sleep(100);
               }
            }
         }
         catch(RuntimeException e) {
            throw e;
         }
         catch(Exception e) {
            throw new RuntimeException("Failed to update MV", e);
         }
      });
   }
   
   /**
    * remove mv
    */
   public void removeMV() {
      MessageTestUtils.withMockMessageContext(this.principal, null, () -> {
         try {
            DeleteMaterializedViewsRequest deleteRequest = new DeleteMaterializedViewsRequest();
            deleteRequest.setViews(this.views);
            materializedViewApiController.deleteMaterializedViews(null, deleteRequest, this.principal);
         }
         catch(RuntimeException e) {
            throw e;
         }
         catch(Exception e) {
            throw new RuntimeException("Failed to remove MV", e);
         }
      });
   }
   
   private final MaterializedViewApiService materializedViewApiService;
   private final MaterializedViewApiController materializedViewApiController;
   private ConfigurationContext context;
   private final SRPrincipal principal;
   private final String asset_id;
   List<String> views = new ArrayList<>();
}