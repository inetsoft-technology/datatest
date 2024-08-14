package inetsoft.test.worksheet.cases

import inetsoft.test.ActionEventsUtil
import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Specification
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.IgnoreIf

@IgnoreIf({ os.linux })
class WSBuildServer_Spec extends Specification {
   static WorksheetTest wstest
   static String caseName

   def setupSpec() {
      WorksheetTest.initHome(this.class.getName())
      /*ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      String path = this.class.getClassLoader().getResource("assets/BuildServer.zip")
      actionEventsUtil.importAssetsFile(path)*/
   }

   def 'Actions' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Actions', null, null)

      expect:
      wstest.compareData(null)
   }

   // def 'Airport' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Airport', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }

   def 'Attribution - OWC' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Attribution - OWC', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'awsSort' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/awsSort', null, null)

      expect:
      wstest.compareData(null)
   }
   // def 'BIA Report' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/BIA Report', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }
   // def 'Call Center Monitoring' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    def paras = new HashMap<String, Object>()
   //    paras.put('counter', 2035)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Call Center Monitoring', paras, null)

   //    expect:
   //    wstest.compareData(null)
   // }
   // def 'Call Details' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Call Details', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }
   def 'Check in Summary' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Check in Summary', null, null)

      expect:
      wstest.compareData(null)
   }
   def 'chqdemo_ShipmentAnalysis' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/chqdemo_ShipmentAnalysis', null, null)

      expect:
      wstest.compareData(null)
   }
   def 'Compliance Details' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Compliance Details', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Controls Dashboard' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Controls Dashboard', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Daily EOD LongShort New' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Daily EOD LongShort New', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'dateComparison' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/dateComparison', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Drawdown - No Caching' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Drawdown - No Caching', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'ec1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/ec1', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Enrollment Trends' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Enrollment Trends', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Incident Dashboard' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Incident Dashboard', null, null)

      expect:
      wstest.compareData(null)
   }

   // def 'Incident Register' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Incident Register', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }

   def 'KRI Details' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/KRI Details', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'KRI Monthly Trend' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/KRI Monthly Trend', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Lead Details' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Lead Details', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Liquidity - OFC' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Liquidity - OFC', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Manufacturing2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Manufacturing2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Maximo Work Orders' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Maximo Work Orders', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Mermaid Item Data' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Mermaid Item Data', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Nightly test Chris' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Nightly test Chris', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'PR Employee Attrition Data' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/PR Employee Attrition Data', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'ProjectManagementData' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/ProjectManagementData', null, null)

      expect:
      wstest.compareData(null)
   }


   def 'Inc Ops with Next Work Center' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Inc Ops with Next Work Center', null, null)

      expect:
      wstest.compareData(null)
   }


   def 'Job Operations Completed' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Job Operations Completed', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Job Operations Completed - Grind' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Job Operations Completed - Grind', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Nonconformances' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Nonconformances', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Queue - C1' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Queue - C1', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Queue - Not Released to Turn' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Queue - Not Released to Turn', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Shopload - Turn' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Shopload - Turn', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Timecards - Active' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Timecards - Active', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Turn - Outside Processing Priority' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Turn - Outside Processing Priority', null, null)

      expect:
      wstest.compareData(null)
   }

   // def 'Return Analytics' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Return Analytics', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }

   def 'Risk Culture Dashboard' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Risk Culture Dashboard', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Risk Dashboard V2' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Risk Dashboard V2', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Risk Event and Controls Linkages' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Risk Event and Controls Linkages', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Risk Linked Items - Risk Assessment' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Risk Linked Items - Risk Assessment', null, null)

      expect:
      wstest.compareData(null)
   }

   // def 'Single Component Dashboard' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Single Component Dashboard', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }

   // def 'Single Critical Process Dashboard' () {
   //    caseName =  specificationContext.currentIteration.name
   //    wstest = new WorksheetTest(caseName)
   //    wstest.executeWS('1^2^__NULL__^BuildServer/Single Critical Process Dashboard', null, null)

   //    expect:
   //    wstest.compareData(null)
   // }

   def 'Single Plausible Scenario Dashboard' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Single Plausible Scenario Dashboard', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'Site Report' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/Site Report', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'SupplyChain' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/SupplyChain', null, null)

      expect:
      wstest.compareData(null)
   }

   def 'TelecomWorksheet' () {
      caseName =  specificationContext.currentIteration.name
      wstest = new WorksheetTest(caseName)
      wstest.executeWS('1^2^__NULL__^BuildServer/TelecomWorksheet', null, null)

      expect:
      wstest.compareData(null)
   }
}