package inetsoft.test.vsexport.cases

import inetsoft.report.io.csv.CSVConfig
import inetsoft.test.viewsheet.VSExportTest
import spock.lang.IgnoreRest
import spock.lang.Specification

/**
 * check vs export as CSV
 */
class ToCSV_Spec extends Specification {
   static VSExportTest vsExportTest
   static String caseName
   CSVConfig csvConfig

   def setupSpec() {
      VSExportTest.initHome(this.class.getName())
   }

   /**
    * check vs include table,crosstab,freehand export as CSV with diff bookmark data
    */
   def 'toCSV_Normal_Spec1'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV1' , caseName)
      vsExportTest.testExportAsCSV(['(Home)', 'AKCA', 'range'] as String[], null, null)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check vs export as CSV:
    * 1. crosstab with period dimension and dynamic value
    * 2. measure cell set presenter format and use multi-header on freehand table
    */
   def 'toCSV_Normal_Spec2'() {
      caseName = specificationContext.currentIteration.name
      csvConfig = new CSVConfig(',', "'", true, false)
      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV5' , caseName)
      vsExportTest.testExportAsCSV(['(Home)', 'bk1'] as String[], null, csvConfig)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check vs export as CSV: all data type in group, tab, embedded vs
    */
   def 'toCSV_Special_Spec1'() {
      caseName = specificationContext.currentIteration.name
      csvConfig = new CSVConfig(';', '"', true, false)
      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV2' , caseName)
      vsExportTest.testExportAsCSV(null, null, csvConfig)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check vs export as CSV: table type in group as popComponent, flyover, dataView
    */
   def 'toCSV_Special_Spec2'() {
      caseName = specificationContext.currentIteration.name
      csvConfig = new CSVConfig('', "'", false, true)
      csvConfig.setExportAssemblies(['Crosstab2', 'asFlyOver'])

      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV3' , caseName)
      vsExportTest.testExportAsCSV(['bk1'] as String[], null, csvConfig)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check vs export as CSV:
    * 1. table visible option as hide or hide on print and export
    * 2. Crosstab have hide column. the hide column show on csv file
    */
   def 'toCSV_Special_Spec3'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV4' , caseName)
      vsExportTest.testExportAsCSV(null, null, null)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check vs export as CSV: tab
    * only export current table in tab
    */
   def 'toCSV_Special_Spec4'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^toCSV/toCSV6' , caseName)
      vsExportTest.testExportAsCSV(['(Home)', 'bk1'] as String[], null, null)

      expect:
      vsExportTest.compareCSV()
   }

   /**
    * check user format apply on csv
    */
   def 'toCSV_UserFormat'() {
      caseName = specificationContext.currentIteration.name
      vsExportTest = new VSExportTest('1^128^__NULL__^Format/FTables/FTable' , caseName)
      vsExportTest.testExportAsCSV(null, null, null)

      expect:
      vsExportTest.compareCSV()
   }
}
