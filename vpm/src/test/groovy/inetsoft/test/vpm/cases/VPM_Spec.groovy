package inetsoft.test.vpm.cases

import inetsoft.sree.security.SRPrincipal
import inetsoft.test.modules.VPMTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class VPM_Spec extends Specification {

   def setup() {
      VPMTest.initHome()
   }

   /*
    *vpm definition is the first case in pict, vs source is model(not define hidden column model, sql view table in physical view)
    *not apply condition, not apply hidden column, bug #38383
    */
   def 'VPM1_Model_1'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM1_Model_1')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the first case in pict, vs source is model(define hidden column model)
    *apply condition and apply hidden column
    */
   def 'VPM1_Model_2'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM1_Model_2')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the first case in pict, vs source is worksheet(database query)
    *table 'ccccc1' apply condition and apply hidden column
    *table 'SQL Query1' apply condition, not apply hidden column
    *table 'ORDERS1' not apply condition, not apply hidden column
    */
   def 'VPM1_WS'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM1_WS')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the first case in pict, vs source is query(sql query)
    *not apply condition, not apply hidden column, bug #38383
    */
   def 'VPM1_Query'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM1_Query')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the second case in pict, vs source is model(the physical view define condition)
    *not apply vpm for user 'NJ'
    *other users, apply condition and apply hidden column:
    *admin  Conditions:ph1: ccc.STATE LIKE 'N%', Hidden Columns: NO
    *guest  Conditions:ph1: ccc.CITY < ccc.CONTACT, Hidden Columns: ph1.ccc.ADDRESS
    *NY  Conditions:ph1: ccc.CUSTOMER_ID BETWEEN 10 AND 15, Hidden Columns: ph1.ccc.ADDRESS
    */
   def 'VPM2_Model1'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM2_Model1')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the second case in pict, vs source is other model(use same physical view)
    *not apply vpm for user 'NJ'
    *other users, apply condition and apply hidden column for table 'ccc', not apply condition and not hidden column for table 'PUBLIC.CUSTOMERS'
    */
   def 'VPM2_Model1_1'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM2_Model1_1')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the second case in pict, vs source is other model(physical view not define condition, but use same table)
    *not apply vpm for user 'NJ'
    *other users, not apply condition, not apply hidden column
    */
   def 'VPM2_Model2'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM2_Model2')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the second case in pict, vs source is query(normal db table,use same table with physical view that defines condition)
    *not apply vpm for user 'NJ'
    *other users, not apply condition, not apply hidden column
    */
   def 'VPM2_Query'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM2_Query')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the third case in pict, vs source is model
    *not apply vpm for user 'NJ', 'admin'
    *other users, apply condition, apply hidden column(guest hide ['ph1.ccc.ADDRESS','ph1.ccc.ZIP'], NY hide 'ph1.ccc.ADDRESS')
    */
   def 'VPM3_Model'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM3_Model')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the third case in pict, vs source is ws(join table uses the table define hidden column)
    *not apply vpm for user 'NJ', 'admin'
    *other users, apply condition, apply hidden column(guest hide ['ph1.ccc.ADDRESS','ph1.ccc.ZIP'], NY hide 'ph1.ccc.ADDRESS')
    */
   def 'VPM3_WS'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM3_WS')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the third case in pict, vs source is query(normal db table,use same table with physical view that defines hidden column)
    *not apply vpm for user 'NJ', 'admin'
    *other users, apply condition, not apply hidden column
    */
   def 'VPM3_Query'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM3_Query')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the fourth case in pict, vs source is model(join table, no alias)
    *admin, no condition and no hidden column
    *user0, condition:"PUBLIC.CUSTOMERS.CUSTOMER_ID in (2,4,6)" hidden column:'PUBLIC.CUSTOMERS.CITY'
    *NJ, condition:"PUBLIC.CUSTOMERS.STATE = NJ" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *NY, condition:"PUBLIC.CUSTOMERS.STATE = NY" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *guest, condition:"PUBLIC.ORDERS.DISCOUNT = 0.15" hidden column:'PUBLIC.CUSTOMERS.ORDERNO'
    */
   def 'VPM4_Model_1'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM4_Model_1')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)
      vpmtest.executeVS(user0, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the fourth case in pict, vs source is model(has alias table)
    *admin, condition:"ccc.CUSTOMER_ID between 10 and 15" hidden column:no
    *user0, condition:"ccc.CUSTOMER_ID between 10 and 15" hidden column:'PUBLIC.CUSTOMERS.CITY'
    *NJ, condition:"ccc.CUSTOMER_ID between 10 and 15" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *NY, condition:"ccc.CUSTOMER_ID between 10 and 15" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *guest, condition:"ccc.CUSTOMER_ID between 10 and 15" hidden column:'PUBLIC.CUSTOMERS.ZIP'
    */
   def 'VPM4_Model_2'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM4_Model_2')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)
      vpmtest.executeVS(user0, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the fourth case in pict, vs source is query
    *admin, condition:"PUBLIC.CUSTOMERS.CUSTOMER_ID < 3" hidden column:'PUBLIC.CUSTOMERS.ADDRESS'
    *user0, condition:"PUBLIC.CUSTOMERS.CUSTOMER_ID in (2,4,6)" hidden column:'PUBLIC.CUSTOMERS.CITY'
    *NJ, not apply vpm
    *NY, condition:"PUBLIC.CUSTOMERS.STATE = NY" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *guest, condition:"PUBLIC.CUSTOMERS.COMPANY like 'F%' " hidden column:'PUBLIC.CUSTOMERS.ZIP'
    */
   def 'VPM4_Query'() {
      def parameter = new HashMap<String, String[]>()
      parameter.put('test', ['F'] as String[])
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM4_Query')
      vpmtest.executeVS(admin, parameter)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, parameter)
      vpmtest.executeVS(NY, null)
      vpmtest.executeVS(user0, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the fourth case in pict, vs source is worksheet
    *admin, no condition and no hidden column
    *user0, condition:"PUBLIC.CUSTOMERS.CUSTOMER_ID in (2,4,6)" hidden column:'PUBLIC.CUSTOMERS.CITY'
    *NJ, condition:"PUBLIC.CUSTOMERS.STATE = NJ" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    *NY, condition:"PUBLIC.CUSTOMERS.STATE = NY" hidden column:'PUBLIC.CUSTOMERS.CONTACT'
    */
   def 'VPM4_WS'() {
      def parameter = new HashMap<String, String[]>()
      parameter.put('test', ['F'] as String[])
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM4_WS')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)
      vpmtest.executeVS(user0, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the fifth case in pict, vs source is ws(join table uses db table and physical view table)
    */
   def 'VPM5_WS'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM5_WS')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the sixth case in pict, vs source is model
    */
   def 'VPM6_Model'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM6_Model')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm definition is the sixth case in pict, vs source is query
    */
   def 'VPM6_Query'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM6_Query')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm merge vpm condition (db table condition, physical view condition, trigger condition), source condition and vs condition
    */
   def 'VPM_Merge_Condition'() {
      def parameter = new HashMap<String, String[]>()
      parameter.put('test', ['N'] as String[])
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Merge_Condition')
      vpmtest.executeVS(admin, parameter)
      vpmtest.executeVS(guest, parameter)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm merge vpm hidden column (db table column, alias table column, trigger column)
    *admin: see all columns
    *guest: in 'CUSTOMERS1' hide 'ADDRESS' and 'COMPANY', in 'CUSTOMERS2' hide 'ADDRESS', in 'aaa1' hide 'DISCOUNT' and 'ORDERDATE', in 'ORDERS1' hide 'ORDERDATE', in 'PRODUCTS' hide 'CATEGORY_ID', 'PRODUCT_ID', 'cid_pid' no data
    *NJ: in 'CUSTOMERS1' and 'CUSTOMERS2' hide 'ADDRESS' and 'ZIP', in 'aaa1' and 'ORDERS1' hide 'DISCOUNT', in 'ORDERDETAILS1' hide 'CATEGORY_ID', 'cid_pid' no data
    */
   def 'VPM_Merge_HiddenColumn'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Merge_HiddenColumn')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm merge multiple vpm
    *admin: table ccc1, customer_id > 5 and reseller = false, hide 'ADDRESS' and 'ZIP'
    *       table q11, customer_id > 5, hide 'ADDRESS'
    *guest: table ccc1, customer_id > 5 and customer_id < 15 and reseller = false, hide 'ADDRESS', 'ZIP', 'COMPANY'
    *       table q11, customer_id > 5 and customer_id < 15, hide 'ADDRESS', 'COMPANY'
    */
   def 'VPM_Merge_MultipleVPM'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Merge_MultipleVPM')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm with Multi-Tenant
    *admin: table customer_id 11-20, hide 'ZIP' see Bug #40590 to get more info.
    *guest: table customer_id 11-20, hide 'ADDRESS', 'CITY'
    *NJ: table customer_id 11-32, hide 'ZIP'
    *NY: table customer_id 18-32, hide 'ADDRESS', 'CITY'
    */
   def 'VPM_MultiTenant'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_MultiTenant')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm apply to unparsable queries
    *admin: condition: unparsable_q11, unparsable_q21 CUSTOMERS.CUSTOMER_ID > 5, unparsable_q31 SUPPLIERS.SUPPLIER_ID < 5
    *       not hide column
    *guest: condition: unparsable_q11, unparsable_q21 CUSTOMERS.CUSTOMER_ID > 5, unparsable_q31 SUPPLIERS.SUPPLIER_ID < 5
    *       unparsable_q31, hide 'COMPANY', 'exp_1'
    */
   def 'VPM_Unparsable'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Unparsable')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm uses complex condition
    */
   def 'VPM_Complex_Condition'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Complex_Condition')
      vpmtest.executeVS(admin, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm special cases for hidden column in worksheet
    */
   def 'VPM_HiddenColumn_Special'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_HiddenColumn_Special')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm hidden column in query different sql clause
    */
   def 'VPM_HiddenColumn_Query'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_HiddenColumn_Query')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm hidden column in worksheet different sql clause
    */
   def 'VPM_HiddenColumn_WS'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_HiddenColumn_WS')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(guest, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *vpm condition uses expression type and there is subquery in the expression
    *NJ:can see contact_id is: 1,2,15,19,25,35
    *NY:can see contact_id is: 3,7,17,24
    */
   def 'VPM_Condition_SubqueryInExpression'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_Condition_SubqueryInExpression')
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(NY, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test inherit vpm from function
    */
   def 'VPM_InheritVPMFrom'() {
      def parameter = new HashMap<String, String[]>()
      parameter.put('cid', ['5'] as String[])
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_InheritVPMFrom')
      vpmtest.executeVS(admin, parameter)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test vpm and Design mode sample data size--Bug #61047
    */
   def 'VPM_MaxRow'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/vpm and maxrow')
      vpmtest.executeVS(admin, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test groups.includes() in lookup --Bug #61669
    */
   def 'VPM_IncludesGroups'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_IncludesGroups')
      vpmtest.executeVS(NJ, null)
      vpmtest.executeVS(admin, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test roles.includes() in lookup --Bug #61669
    */
   def 'VPM_IncludesRoles'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_IncludesRoles')
      vpmtest.executeVS(admin, null)
      vpmtest.executeVS(user0, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test use.includes() in lookup --Bug #61669
    */
   def 'VPM_IncludesUser'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/VPM_IncludesUser')
      vpmtest.executeVS(user0, null)
      vpmtest.executeVS(admin, null)

      expect:
      vpmtest.compareData(null)
   }

   /*
    *test name include special character --Bug #64152
    */
   def 'VPM_Includesspecialchar'() {
      vpmtest = new VPMTest('1^128^__NULL__^VPM/nameincludedot')
      vpmtest.executeVS(user0, null)
      vpmtest.executeVS(admin, null)

      expect:
      vpmtest.compareData(null)
   }


   SRPrincipal admin = VPMTest.createPrincipal('admin', ['Everyone', 'Administrator', 'Special'] as String[],
      [] as String[])
   SRPrincipal guest = VPMTest.createPrincipal('guest', ['Everyone'] as String[], [] as String[])
   SRPrincipal user0 = VPMTest.createPrincipal('user0', ['Everyone'] as String[], [] as String[])
   SRPrincipal NJ = VPMTest.createPrincipal('NJ', ['Everyone', 'Special'] as String[], ['NN'] as String[])
   SRPrincipal NY = VPMTest.createPrincipal('NY', ['Everyone'] as String[], ['NN'] as String[])

   static VPMTest vpmtest
}
