package inetsoft.test.worksheet.cases

import inetsoft.test.worksheet.WorksheetTest
import spock.lang.Specification
import spock.lang.Ignore
import spock.lang.IgnoreRest

class WSProperty_Spec extends Specification {
   //if check ignore col, you can set  ignoreTypeColumns: [0,1,2], or set null
   //Note: create a null ws, as base ws
   def fileTemp =  [ tableName: null,
                     encodingSelected: 'GBK',
                     sheetsList: null,
                     sheetSelected: null,
                     delimiter: ",",
                     delimiterTab: false,
                     detectType: true,
                     fileType: 'XLSX',
                     firstRowCB: true,
                     headerCols: 1,
                     headerNames: null,
                     ignoreTypeColumns: null,
                     newTableName: null,
                     removeQuotesCB: true,
                     unpivotCB: false ]

   def setup() {
   }

   def 'propertyMaxRows' () {
      def properties = ["join.table.maxrows":"5"]
      wstest = new WorksheetTest('propertyMaxRows')

      wstest.initHome(this.class.getName(), properties)
      wstest.executeWS('1^2^__NULL__^Common/propertyMaxRows', null, null)
      wstest.clearEnv(properties)

      expect:
      wstest.compareData(null)
   }

   def 'property_crossJoin.maxCellCount' () {
      def properties = ["crossJoin.maxCellCount":"5000000000"]
      wstest = new WorksheetTest('property_crossJoin.maxCellCount')

      wstest.initHome(this.class.getName(), properties)
      wstest.executeWS('1^2^__NULL__^Common/property_crossJoin.maxCellCount', null, null)
      wstest.clearEnv(properties)

      expect:
      wstest.compareData(null)
   }

   /**
    * upload xlsx file
    * check all col type, no header, specail name/col/sheet name
    */
   def 'upload xlsx' () {
      def file1 = fileTemp
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('upload xlsx')
      wstest.initHome(this.class.getName())

      file1.sheetSelected = 'allType'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'allType')
      file1.sheetSelected = 'date'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'date')
      file1.sheetSelected = 'noHeader'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'noHeader')
      file1.sheetSelected = 'specialColName'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'specialColName')
      file1.sheetSelected = 'A%^&$%^&()_P}"{><'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'specialSheetName')
      file1.sheetSelected = 'specialCol'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'specialCol')
      file1.sheetSelected = 'SSSSSSSSSSSSSSSSSSSSSSS_中文'
      wstest.importCSVToEMTable(wsAsset, 'excelTest1.xlsx', file1, 'longSheetName')

      expect:
      wstest.compareData(null)
   }

   /**
    * upload xls file
    * check firstRow,unpivot,headerCols properties
    */
   def 'upload xls' () {
      def file1 = fileTemp
      file1.sheetSelected = 'date'
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('upload xls')
      wstest.initHome(this.class.getName())

      file1.firstRowCB = false
      wstest.importCSVToEMTable(wsAsset,'excelTest2.xls', file1, 'firstRow')
      file1.unpivotCB = true
      wstest.importCSVToEMTable(wsAsset,'excelTest2.xls', file1, 'unpivot')
      file1.headerCols = 5
      wstest.importCSVToEMTable(wsAsset,'excelTest2.xls', file1, 'headerCols')

      expect:
      wstest.compareData(null)
   }

   /**
    * upload txt file to all col type
    * check diff delimiter, detectType and Remove Quatation Marks properties
    */
   def 'upload txt' () {
      def file1 = fileTemp
      file1.fileType = "DELIMITER"
      file1.encodingSelected = "UTF-8"
      def wsAsset  = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('upload txt')
      wstest.initHome(this.class.getName())

      file1.delimiter = ";"
      wstest.importCSVToEMTable(wsAsset,'allTypeWithSem.txt', file1, 'delimiterSem')
      file1.delimiter = " ";
      wstest.importCSVToEMTable(wsAsset,'allTypeWithBlank.txt', file1, 'delimiterSpace')
      file1.delimiter = ","
      wstest.importCSVToEMTable(wsAsset,'allTypeWithComma.txt', file1, 'delimiterComma')
      file1.delimiter = ";"
      file1.removeQuotesCB = true
      wstest.importCSVToEMTable(wsAsset,'contxtWithChar.txt', file1, 'removeQuotesCB')
      file1.delimiterTab = true
      wstest.importCSVToEMTable(wsAsset,'allTypeWithTab.txt', file1, 'delimiterTab')

      expect:
      wstest.compareData(null)
   }

   /**
    * upload csv file to all col type
    */
   def 'upload csv' () {
      def file1 = fileTemp
      file1.fileType = "DELIMITER"
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('upload csv')
      wstest.initHome(this.class.getName())

      wstest.importCSVToEMTable(wsAsset,'csvTest.csv', file1, 'csvTest')
      wstest.importCSVToEMTable(wsAsset,'CSV_CRLF.csv', file1, 'CSV_CRLF')
      file1.removeQuotesCB = true
      wstest.importCSVToEMTable(wsAsset,'csvTest2.csv', file1, 'csvTest2')
      file1.detectType = false
      wstest.importCSVToEMTable(wsAsset,'csvTest1.csv', file1, 'detectType')
      expect:
      wstest.compareData(null)
   }

   /**
    * check upload customer bugs
    */
   def 'customer bugs' () {
      def file1 = fileTemp
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('customer bugs')
      wstest.initHome(this.class.getName())

      file1.sheetSelected = "Output"
      wstest.importCSVToEMTable(wsAsset,'customData/inetsoft-20211021.xlsx', file1, 'Bug#51030')
      file1.sheetSelected = "Day wise Usage"
      file1.firstRowCB = false
      wstest.importCSVToEMTable(wsAsset,'customData/Untitled-95-1104_csv.xlsx', file1, 500, 'Bug#47216')
      file1.firstRowCB = true
      file1.sheetSelected = "Sheet0"
      wstest.importCSVToEMTable(wsAsset,'customData/Pega_data_1_anoniem.xlsx', file1, 2000, 'Bug#49756')
      file1.sheetSelected = "Sales"
      wstest.importCSVToEMTable(wsAsset,'customData/Sales Explore_Sales.xlsx', file1, 2500, 'Bug#49660')
      file1.sheetSelected = "Summary"
      wstest.importCSVToEMTable(wsAsset,'customData/Untitled-4-38_csv.xlsx', file1, 500, 'Bug')
      file1.sheetSelected = "Sheet2"
      wstest.importCSVToEMTable(wsAsset,'customData/textTest.xlsx', file1, 'Bug#56468')
      file1.sheetSelected = 'Goblins-Analysis'
      wstest.importCSVToEMTable(wsAsset,'customData/Untitled-203-2226_csv.xlsx', file1, 3000, 'Bug#54750')
      file1.sheetSelected = "Output"
      wstest.importCSVToEMTable(wsAsset,'customData/testEmptyHeader.csv', file1, 'Bug #58480')
      expect:
      wstest.compareData(null)
   }

   /**
    * upload diff type files
    * detect number type col
    */
   def 'number type detect' () {
      def file1 = fileTemp
      file1.sheetSelected="testNumber1"
      file1.ignoreTypeColumns=[1,2]
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest('number type detect')
      wstest.initHome(this.class.getName())

      wstest.importCSVToEMTable(wsAsset,'testNumber.xlsx', file1, 'xlsx')
      wstest.importCSVToEMTable(wsAsset,'testNumber.xls', file1, 'xls')
      file1.fileType = "DELIMITER"
      wstest.importCSVToEMTable(wsAsset,'testNumber.txt', file1, 'txt')
      wstest.importCSVToEMTable(wsAsset,'testNumber.csv', file1, 'csv')

      expect:
      wstest.compareData(null)
   }

   /**
    * upload diff type files
    * detect date type col
    */
   def 'date type detect' () {
      def file1 = fileTemp
      def wsAsset = '1^2^__NULL__^Common/nullws'
      wstest = new WorksheetTest( 'date type detect')
      wstest.initHome(this.class.getName())

      file1.sheetSelected = 'valid_date format'
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.xlsx', file1, 'valid date')
      file1.sheetSelected = '中文_date format'
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.xlsx', file1, '中文_date')
      file1.sheetSelected = 'valid_text format'
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.xlsx', file1, 'valid text')
      file1.sheetSelected = '中文_text format'
      file1.ignoreTypeColumns = [0]
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.xlsx', file1, '中文_text')
      file1.sheetSelected = 'invalid_date format'
      file1.ignoreTypeColumns = [0,1,4,5,6,7,8,9,11,12,14,15,16,17,19,20,22,23,24,25,26,27,28,29,30,31,32]
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.xlsx', file1, 'invalid date')

      file1.fileType = "DELIMITER"
      file1.ignoreTypeColumns = [0,1,4]
       wstest.importCSVToEMTable(wsAsset,'dateFormatTest.txt', file1, 'invalid date-txt')
      file1.ignoreTypeColumns = [0,1,4,5,6,7,8,9,11,12,14,15,16,17,19,20,22,23,24,25,26,27,28,29,30,31,32]
      wstest.importCSVToEMTable(wsAsset,'dateFormatTest.csv', file1, 'invalid date-csv')

      expect:
      wstest.compareData(null)
   }

   static WorksheetTest wstest
}


