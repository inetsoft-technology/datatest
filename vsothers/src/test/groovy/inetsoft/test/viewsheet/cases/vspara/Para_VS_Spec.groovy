package inetsoft.test.viewsheet.cases.vspara

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class Para_VS_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check worksheet condition parameter-string,double,integer
   */
   def 'Para_wscondition'() {
      def paras = new HashMap<String, String[]>()
      paras.put('string_inrange', ['Last year'] as String[])
      paras.put('string_employee', ['Annie','Eric'] as String[])
      paras.put('double_total', ['80000'] as String[])
      paras.put('integer_ordernum', ['12654'] as String[])
      paras.put('startWithPcname', ['S'] as String[])
      paras.put('likePstate', ['N%'] as String[])
      paras.put('containsPaddress', ['Rd.'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wscondition', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /*
    check worksheet post & rangking condition parameter-float
   */
   def 'Para_wscondition2'() {
      def paras = new HashMap<String, String[]>()
      paras.put('float', ['28.999'] as String[])
      paras.put('top', ['2'] as String[])
      paras.put('PstartYear', ['2020-01-01 00:00:00'] as String[])
      paras.put('PendYear', ['2021-01-01 00:00:00'] as String[])
      paras.put('Pbottom', ['2'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wscondition2', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /*
    check worksheet grouping parameter--date,boolean
   */
   def 'Para_wsgroup'() {
      def paras = new HashMap<String, String[]>()
      paras.put('lastyear', ['Last year'] as String[])
      paras.put('boolean', ['true'] as String[])
      paras.put('PstartN', ['N'] as String[])
      paras.put('PlikeA', ['A%'] as String[])
      paras.put('POneOf', ['CO','CT','MA','MD'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wsgroup', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /**
    * check group asset, which used in freehand
    */
   def 'Para_wsgroup2'() {
      def paras = new HashMap<String, String[]>()
      paras.put('P20201229', ['2020-12-29 00:00:00'] as String[])
      paras.put('P2021010306', ['2021-01-03 00:00:00', '2021-01-06 00:00:00'] as String[])
      paras.put('PCA', ['CA'] as String[])
      paras.put('PNJYV', ['NJ','NY','NV'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wsgroup2', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /*
    check worksheet variable parameter--short,long,time
   */
   def 'Para_wsvariable'() {
      def paras = new HashMap<String, String[]>()
      paras.put('long', ['45'] as String[])
      paras.put('short', ['789', '9087', '88'] as String[])
      paras.put('time', ['13:45:00', '16:56:00'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wsvariable', caseName)
      vstest.exportAsPNG(paras, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check worksheet variable-default expression parameter--byte,date
    need set prompts user is false and don't send new parameter
   */
   def 'Para_wsvariable2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wsvariable2', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
   }

   /*
    * check worksheet databasequery parameter--datetime,integer
    * check sql query with parameter
   */
   def 'Para_basequery'() {
      def paras = new HashMap<String, String[]>()
      paras.put('orderdate', ['2017-03-29 00:00:00','2017-04-14 00:00:00','2017-11-22 00:00:00', '2018-12-10 00:00:00'] as String[])
      paras.put('customer_name', ['%c%'] as String[])
      paras.put('state', ['C'] as String[])
      paras.put('region_id',['2'] as String[])
      paras.put('reseller',['0'] as String[])
      paras.put('>Pid',['3'] as String[])
      paras.put('inPstate',['NJ', 'CA', 'AK'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_basequery', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
      vstest.compareImage(null)
   }

   /**
    * check parameter on complex query. see Bug #60502, Bug #60334
    * @return
    */
   def 'Para_basequerysql'() {
      def paras = new HashMap<String, String[]>()
      paras.put('y2004', ['2004-01-01'] as String[])
      paras.put('cid', ['10'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_basequerysql', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /**
    * check defined variable parameter, then use it on SQL and JS formula of ws
    * use script to init it on vs
    * @return
    */
   def 'Para_wsFormulapara'() {
      def paras = new HashMap<String, String[]>()
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_wsFormulapara', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
   }

   /*
    check query parameter-condition & having condition
   */
   def 'Para_query'() {
      def paras = new HashMap<String, String[]>()
      paras.put('inPstate', ['NJ', 'NY', 'CA', 'CK', 'FL'] as String[])
      paras.put('>Pcid', ['3'] as String[])
      paras.put('likePcom', ['%a%'] as String[])
      paras.put('inPcom', ['Whittaker & Co, Inc', 'Direct Sales'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_query', caseName)
      vstest.executeVS(paras, null)

     expect:
      vstest.compareData(null)
      vstest.compareImage(null)
   }

   /*
    check query parameter condition with special use, like $(Pcid) <= orders.customers.customer_id
   */
   def 'Para_query2'() {
      def paras = new HashMap<String, String[]>()
      paras.put('Pcid', ['25'] as String[])
      paras.put('PstartTime', ['05:00:00'] as String[])
      paras.put('PendTime', ['07:00:00'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_query2', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
      vstest.compareImage(null)
   }

   /*
    check query parameter-subquery
   */
   def 'Para_sub'() {
      def paras = new HashMap<String, String[]>()
      paras.put('discount', ['0.3'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_sub', caseName)
      vstest.executeVS(paras, null)

     expect:
      vstest.compareData(null)
   }

   /**
    * check vs binding db table which use vpm table. use script to init parameter
    * see Issue #60912, vs have same issue
    */
   def 'Para_vpm1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_vpm1', caseName)
      vstest.executeVS(null, null)

      expect:
      vstest.compareData(null)
   }

   /**
    * check vs binding query which use vpm table.
    */
   def 'Para_vpm2'() {
      def paras = new HashMap<String, String[]>()
      paras.put('Ppid', ['10'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/para_vpm2', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /**
    * check vs binding ws table which use vpm table. see Issue #60912
    */
   def 'Para_vpm3'() {
      def paras = new HashMap<String, String[]>()
      paras.put('Ppid', ['20'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/para_vpm3', caseName)
      vstest.executeVS(paras, null)

      expect:
      vstest.compareData(null)
   }

   /*
    check data view parameter--condition
   */
   def 'Para_dataview'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('boolean', ['true'] as String[])
      paras.put('short', ['12369'] as String[])
      paras.put('date', ['2020-10-27', '2020-10-31'] as String[])
      //freehandtable
      paras.put('float', ['23.69', '1258.49'] as String[])
      paras.put('long', ['26000000000'] as String[])
      paras.put('datetime_inrange', ['Last year'] as String[])
      //crosstab
      paras.put('company_startwith', ['D'] as String[])
      paras.put('company_contains', ['Nova Tera'] as String[])
      paras.put('orderdate', ['2018-09-30 12:00:00'] as String[])
      //chart
      paras.put('employee', ['%r%'] as String[])
      paras.put('company', ['Nova Tera'] as String[])

      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_dataview', caseName)
      vstest.executeVS(paras, null)

     expect:
      vstest.compareData(null)
      vstest.compareImage(null)
   }

   /*
    check output parameter--condition
   */
   def 'Para_output'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('employee', ['Annie', 'Eric'] as String[])
      paras.put('total', ['10526'] as String[])
      paras.put('id', ['1', '2'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_output', caseName)
      vstest.exportAsPNG(paras, null)

     expect:
     vstest.compareImage(null)
   }

   /*
    check merge parameter--ws condition & element condition
   */
   def 'Para_merge'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('id1', ['14'] as String[])
      paras.put('id2', ['18'] as String[])
      paras.put('company', ['Whittaker & Co, Inc', 'Ubermeyer'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_merge', caseName)
      vstest.exportAsPNG(paras, null)

     expect:
      vstest.compareImage(null)
   }

   /*
    check highlight parameter
   */
   def 'Para_highlight'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('employee', ['Annie', 'Eric'] as String[])
      paras.put('orderdate', ['2017-01-01 00:00:00', '2020-01-01 00:00:00'] as String[])
      paras.put('total', ['106133'] as String[])
      paras.put('date_inrange', ['Last year'] as String[])
      paras.put('company', ['Computer Tech'] as String[])
      paras.put('company_value', ['700'] as String[])
      paras.put('employee_value', ['700'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_highlight', caseName)
      vstest.exportAsPNG(paras, null)

      expect:
         vstest.compareImage()
   }

   /*
    check not input value for parameter
   */
   def 'nullVSTest1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/nullVSTest1', caseName)
      vstest.executeVS(null, null)
      expect:
         vstest.compareData(null)
   }

   /*
    check NULL value for string parameter
   */
   def 'nullVSTest2'() {
      def paras = new HashMap<String, String[]>()
      paras.put('stringNull', ['NULL'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/nullVSTest2', caseName)
      vstest.executeVS(paras, null)
      expect:
         vstest.compareData(null)
   }

   /*
    text parameter.parametertnames can parse data with format correctly
   */
   def 'Para_textparameter1'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('state', ['CT', 'FL','MA','PA','CO','TX'] as String[])
      paras.put('start_date', ['2020-01-01 00:00:00'] as String[])
      paras.put('end_date', ['2022-01-01 00:00:00'] as String[])
      paras.put('num', ['100000'] as String[])
      paras.put('discount', ['75'] as String[])
      paras.put('total', ['500'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_textparameter1', caseName)
      vstest.exportAsPNG(paras, null)

      expect:
         vstest.compareImage()
   }

   /*
    text parameter.parametertnames from source and onInit parameter can get data correctly
   */
   def 'Para_textparameter2'() {
      def paras = new HashMap<String, String[]>()
      //table
      paras.put('state', ['PA', 'NJ'] as String[])
      paras.put('region', ['USA East'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_textparameter2', caseName)
      vstest.exportAsPNG(paras, null)

      expect:
         vstest.compareImage()
   }

   /*
    check uses selection list or input to pass array values that contains comma
   */
   def 'Para_comma'() {
      def paras = new HashMap<String, String[]>()
      paras.put('company', ['Whittaker & Co, Inc', 'Direct Sales', 'Daycare One', 'Rutgers Bank'] as String[])
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^ParaVS/Para_comma', caseName)
      vstest.exportAsPNG(paras, null)

     expect:
      vstest.compareImage(null)
   }
}
