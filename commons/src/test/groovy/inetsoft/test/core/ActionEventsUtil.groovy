package inetsoft.test.core

import inetsoft.enterprise.web.api.file.FileApiService
import inetsoft.sree.security.SRPrincipal
import inetsoft.util.ConfigurationContext
import inetsoft.web.composer.ws.event.OpenWorksheetEvent
import inetsoft.web.viewsheet.event.OpenViewsheetEvent

class ActionEventsUtil {
   OpenViewsheetEvent createOpenViewsheetEvent(Map<String, String[]> parameters, String vs_id) {
      createOpenViewsheetEvent(parameters, vs_id, true)
   }

   OpenViewsheetEvent createOpenViewsheetEvent(Map<String, String[]> parameters, String vs_id, Boolean isViewer) {
      OpenViewsheetEvent event = new OpenViewsheetEvent()
      event.setEntryId(vs_id)
      event.setViewer(isViewer)

      if(parameters != null) {
         event.setParameters(parameters)
      }
      return event
   }

   /**
    * rewrite openWorksheetEvent
    * @param ws_id
    * @return
    */
   OpenWorksheetEvent openWorksheetEvent(String ws_id) {
      OpenWorksheetEvent event = new OpenWorksheetEvent() {
         @Override
         String id() {
            return ws_id
         }

         @Override
         boolean openAutoSavedFile() {
            return false
         }

         @Override
         boolean gettingStartedWs() {
            return false
         }

         @Override
         boolean createQuery() {
            return false
         }
      }
      return event
   }


   /**
    * import asset file to sree.home
    */
   def importAssetsFile(String path) {
      ensureAdmin()
      FileApiService fileApiService = ConfigurationContext.getContext().getSpringBean(FileApiService)
      if(System.properties['os.name'].toString().toLowerCase().contains('windows')) {
         fileApiService.importAssets(new File(path.minus('file:/')), [], true, admin)
      }
      else {
         fileApiService.importAssets(new File(path.minus('file:')), [], true, admin)
      }
   }

   SRPrincipal admin

   private void ensureAdmin() {
      if(admin == null) {
         DatatestSpringRuntimeInitializer.ensureInitialized(System.getProperty('sree.home', '.'))
         admin = new TUtil().createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }
}
