package inetsoft.test.mv;

import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.test.core.ControllersResource;
import inetsoft.test.core.MessageTestUtils;
import inetsoft.uql.asset.AssetRepository;
import inetsoft.util.Tool;
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
    * constructor with ControllersResource to reuse services
    *
    * @param asset_id,            the viewsheet asset_id
    * @param controllersResource, the ControllersResource to reuse services
    */
   public MaterializedViewResource(String asset_id, ControllersResource controllersResource) throws Exception {
      Objects.requireNonNull(asset_id, "the asset not exist, check if asset_id is right");
      Objects.requireNonNull(controllersResource, "controllersResource cannot be null");
      this.asset_id = asset_id;

      IdentityID identityUser = new IdentityID("admin", "host-org");
      IdentityID adminRole = new IdentityID();
      adminRole.setName("Administrator");
      IdentityID[] identityRoles = {adminRole, new IdentityID("Everyone", "host-org")};
      this.principal = new SRPrincipal(identityUser, identityRoles, new String[0], "host-org", Tool.getSecureRandom().nextLong());

      // Reuse services from ControllersResource
      MVSupportService support = MVSupportService.getInstance();
      SecurityProvider securityProvider = controllersResource.getSecurityProvider();
      ResourcePermissionService resourcePermissionService = controllersResource.getResourcePermissionService();
      RepletRegistryManager repletRegistryManager = controllersResource.getRepletRegistryManager();
      ScheduleTaskFolderService scheduleTaskFolderService = controllersResource.getScheduleTaskFolderService();
      ContentRepositoryTreeService contentRepositoryTreeService = controllersResource.getContentRepositoryTreeService();
      MVService service = new MVService(contentRepositoryTreeService, support);
      AssetRepository assetRepository = controllersResource.getAssetRepository();
      this.materializedViewApiService = new MaterializedViewApiService(service, assetRepository, support);
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
      analyzeRequest.setViewsheetId(this.asset_id);
      analyzeRequest.setBypassVpm(!applyVPM);
      analyzeRequest.setExpandGroups(expandGroup);
      analyzeRequest.setFullData(true);

      return MessageTestUtils.withMockMessageContext(this.principal, null, analyzeRequest, (ctx, req) -> {
         AnalysisJob analysisJob = new AnalysisJob();
         try {
            analysisJob = this.materializedViewApiService.analyze(req, this.principal);

            //wait the analysis job complete or fail
            for (int i = 0; i < 1; i--) {
               AnalysisJob analysisJob1 = this.materializedViewApiService.getAnalysisJob(analysisJob.getId(), this.principal);
               if (analysisJob1.isComplete()) {
                  break;
               } else if (analysisJob1.isFailed()) {
                  List<AnalysisError> errors = analysisJob1.getErrors();
                  StringBuilder msg = new StringBuilder();
                  for (AnalysisError error : errors) {
                     msg.append(error.toString());
                  }
                  System.err.println("====MV Analyze Exception====" + msg);
                  try {
                     Thread.sleep(10);
                  } catch (InterruptedException ie) {
                     Thread.currentThread().interrupt();
                     throw new RuntimeException("Interrupted while waiting for analysis job", ie);
                  }
                  break;
               } else if (!analysisJob1.isComplete()) {
                  try {
                     Thread.sleep(10);
                  } catch (InterruptedException ie) {
                     Thread.currentThread().interrupt();
                     throw new RuntimeException("Interrupted while waiting for analysis job", ie);
                  }
                  if (analysisJob1.isComplete()) {
                     break;
                  }
               }
            }
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new RuntimeException("Failed to analyze MV", e);
         }
         return analysisJob;
      });
   }

   /**
    * create mv
    *
    * @param applyVPM,    apply vpm to mv
    * @param count,       times to create mv
    * @param expandGroup, create for users in group
    */
   public void createMV0(boolean applyVPM, int count, boolean... expandGroup) {
      AnalysisJob analysisJob;
      if (expandGroup == null || expandGroup.length == 0) {
         analysisJob = analysisMV(applyVPM, false);
      } else {
         analysisJob = analysisMV(applyVPM, expandGroup[0]);
      }

      MessageTestUtils.withMockMessageContext(this.principal, null, () -> {
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

            for (int i = 0; i < count; i++) {
               this.materializedViewApiService.createMaterializedView(analysisJob.getId(), createRequest, this.principal);

               //wait create mv complete or fail
               for (int j = 0; j < 1; j--) {
                  CreateMaterializedViewStatus createStatus =
                          this.materializedViewApiService.getCreationStatus(analysisJob.getId(), this.principal);
                  if (createStatus.isComplete()) {
                     break;
                  } else if (createStatus.isFailed()) {
                     String msg = createStatus.getError();
                     System.err.println("====MV Create Exception====" + msg);
                     break;
                  } else if (!createStatus.isComplete()) {
                     try {
                        Thread.sleep(10);
                     } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for MV creation", ie);
                     }
                     if (createStatus.isComplete()) {
                        break;
                     }
                  }
               }
            }
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new RuntimeException("Failed to create MV", e);
         }
      });
   }

   /**
    * create mv, for 1 time
    *
    * @param applyVPM,    apply vpm to mv
    * @param expandGroup, create for users in group
    */
   public void createMV(boolean applyVPM, boolean... expandGroup) {
      try {
         createMV0(applyVPM, 1, expandGroup);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Failed to create MV", e);
      }
   }

   /**
    * create incremental mv
    *
    * @param applyVPM,    apply vpm to mv
    * @param count,       times to create mv
    * @param expandGroup, create for users in group
    */
   public void createIncrementMV(boolean applyVPM, int count, boolean... expandGroup) {
      try {
         createMV0(applyVPM, count, expandGroup);
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException("Failed to create incremental MV", e);
      }
   }

   /**
    * remove mv
    */
   public void removeMV() {
      MessageTestUtils.withMockMessageContext(this.principal, null, () -> {
         try {
            DeleteMaterializedViewsRequest deleteRequest = new DeleteMaterializedViewsRequest();
            deleteRequest.setViews(this.views);
            this.materializedViewApiService.deleteMaterializedViews(deleteRequest, this.principal);
         } catch (RuntimeException e) {
            throw e;
         } catch (Exception e) {
            throw new RuntimeException("Failed to remove MV", e);
         }
      });
   }

   private final MaterializedViewApiService materializedViewApiService;
   private final Principal principal;
   private final String asset_id;
   List<String> views = new ArrayList<>();
}