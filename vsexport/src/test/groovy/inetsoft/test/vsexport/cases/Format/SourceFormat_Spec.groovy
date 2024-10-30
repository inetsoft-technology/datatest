package inetsoft.test.vsexport.cases

import inetsoft.test.core.ActionEventsUtil
import inetsoft.test.modules.VSExportTest
import spock.lang.IgnoreRest
import spock.lang.Specification

/**
 * check source format apply on vs, report
 */
class SourceFormat_Spec extends Specification {
   static String caseName
   static VSExportTest vsExportTest

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())

      ActionEventsUtil actionEventsUtil = new ActionEventsUtil()
      String path = this.class.getClassLoader().getResource("config/AutoDrillFormat_Cases.zip")
      println '---------------' + path
      actionEventsUtil.importAssetsFile(path)
   }

   def 'test vs LMAutoDrillFormatApply1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^AutoDrillFormatCheck/LMAutoDrillFormatApply1', caseName)

      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.compareHTML()
         vsExportTest.comparePNG()
      }
   }

   def 'test vs LMAutoDrillFormatApply2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^AutoDrillFormatCheck/LMAutoDrillFormatApply2', caseName)

      def paras = [
              pstart: ['2020-03-25 09:17:00'] as String[],
              pend: ['2024-01-18 00:00:00'] as String[],
      ]
      vsExportTest.testExportAsPNG(null, paras, true, false)
      vsExportTest.testExportAsHtml(null, paras)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.compareHTML()
         vsExportTest.comparePNG()
      }
   }

   def 'test vs QueryAutoDrillFormatApply1' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^AutoDrillFormatCheck/QueryAutoDrillFormatApply1',caseName)

      vsExportTest.testExportAsPNG(null,null, true,false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.compareHTML()
         vsExportTest.comparePNG()
      }
   }

   def 'test vs QueryAutoDrillFormatApply2' () {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^AutoDrillFormatCheck/QueryAutoDrillFormatApply2',caseName)

      vsExportTest.testExportAsPNG(null, null, true, false)
      vsExportTest.testExportAsHtml(null, null)
      vsExportTest.testExportWithPrintLayout(null, null)

      expect:
      verifyAll {
         vsExportTest.compareHTML()
         vsExportTest.comparePNG()
      }
   }
}