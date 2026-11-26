package inetsoft.test.mv.cases.others

import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVTest
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

/**
 * Viewsheets of mvStorage.zip that no other spec used, where the MV gave other results than
 * the run without MV until the bugs below were fixed (docs/test-gap-assessment/mv.md, T17).
 * They have no golden files: the checks are on the MV run itself, so they also apply in the
 * default golden mode, and with -Dmv.compare=both|nomv on the comparison with the run without
 * MV. Where a component still differs for a reason that is not fixed, only the fixed
 * components are compared ({@link #sameAsWithoutMV}).
 */
@Execution(ExecutionMode.SAME_THREAD)
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class MVFixedBugs_Spec extends Specification {
   @Shared def admin
   def mvtest

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }

   def cleanup() {
      mvtest?.removeMV()
   }

   private void run(String path) {
      mvtest = new MVTest('1^128^__NULL__^' + path)
      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)
   }

   // worksheet submv/top_rotate: Query1 = ORDERS1 grouped by Year(ORDER_DATE), PAID, DISCOUNT with
   // Sum(ORDER_ID), Query2 = Query1 rotated. The rotated column names are the Sum values, and the
   // viewsheet table Query1 selects 43 of them (12101, 12024, ...). The Sum of the integer
   // ORDER_ID came back as a Double from the SubMV, so the names became 12101.0, ... and the
   // table lost every data column (4 x 1). The table Query2 of the same viewsheet still differs:
   // NULL sorts first with MV and last in DB2 (82322), so it is not compared.
   @Issue(['https://issues.inetsoft.com/issues/82319', 'https://issues.inetsoft.com/issues/82322'])
   def 'submv/top_rotate: the rotated table on a Sum of an integer column keeps its 43 columns'() {
      given:
      String path = 'submv/top_rotate'
      run(path)
      List<String> table = exported(path, 'admin_Query1__MV.txt')

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['Query1|SubMV', 'Query3|SubMV', 'Query5|SubMV'])
         table[0] == 'The table size(row x col) is: (4 x 44)'
         table[1].startsWith('[ORDER_ID][12101][12024][12031]')
         sameAsWithoutMV(path, 'admin_Query1__MV.txt')
         sameAsWithoutMV(path, 'admin_Query3__MV.txt')
      }
   }

   // hit mv/table1: RangeSlider1 (range type month) on ORDERS1.range, a worksheet date range
   // column of ORDER_DATE at the year level. The labels take the format of the column, yyyy;
   // with MV the format was lost and the labels were 2002 Jan / 2005 Jan.
   @Issue('https://issues.inetsoft.com/issues/82320')
   def 'hit mv/table1: the range slider on a year date range column has year labels'() {
      given:
      String path = 'hit mv/table1'
      run(path)
      List<String> slider = exported(path, 'admin_RangeSlider1__MV.txt')

      expect:
      verifyAll {
         mvtest.getMVHitInfo().containsAll(['ORDERS1|TopMV', 'Query1|TopMV'])
         slider[0] == 'The min value is: [2002]'
         slider[1] == 'The max value is: [2005]'
         mvtest.compareWithoutMV()
      }
   }

   // AssociationMV/TestCase_Special1: SelectionList2 is bound to Query1.CUSTOMER_ID and
   // SelectionList1 to ORDERS1.PAID, columns these worksheet tables do not have. Both lists have
   // no data; with MV SelectionList2 used to show 12788, the sum of ORDERS.CUSTOMER_ID.
   @Issue('https://issues.inetsoft.com/issues/82321')
   def 'AssociationMV/TestCase_Special1: a selection list bound to a missing column has no data'() {
      given:
      String path = 'AssociationMV/TestCase_Special1'
      run(path)

      expect:
      verifyAll {
         exported(path, 'admin_SelectionList1__MV.txt') == ['[[NULL]]']
         exported(path, 'admin_SelectionList2__MV.txt') == ['[[NULL]]']
         mvtest.compareWithoutMV()
      }
   }

   // topmv/rotate1, rotate2: Query2 is the rotated table of Query1, an embedded table grouped by
   // Year(ORDER_DATE), DISCOUNT, PAID. A rotated column holds values of different types; its MV
   // stored them as text (numbers without their type, dates with ".0", string order). No MV is
   // built on a rotated table now, only on the table before the rotation. rotate1 is then the
   // same as without MV in every component. In rotate2 the table is the same; its selection
   // list still differs in the order and the state of the values (82323 is back in Feedback for
   // it), so it is not compared.
   @Issue('https://issues.inetsoft.com/issues/82323')
   def '#path: no MV on the rotated table, the table the same as without MV'() {
      given:
      run(path)

      expect:
      verifyAll {
         MVTest.getMVDefInfo() == ['Query1|SubMV']
         mvtest.getMVHitInfo() == ['Query1|SubMV']
         !exported(path, 'admin_Query1__MV.txt').any { it.contains(' 00:00:00.0]') }
         sameAsWithoutMV(path, 'admin_Query1__MV.txt')
         !whole || allSameAsWithoutMV()
      }

      where:
      path            | whole
      'topmv/rotate1' | true
      'topmv/rotate2' | false
   }

   /** {@link MVTest#compareWithoutMV} as a condition (it returns nothing and fails on a difference). */
   private boolean allSameAsWithoutMV() {
      mvtest.compareWithoutMV()
      return true
   }

   /** The lines of an exported file of the MV run. */
   private List<String> exported(String path, String name) {
      File file = new File(folder(path), name)
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   /**
    * Whether one exported file of the MV run equals the same file of the run without MV. True in
    * the default golden mode, which has no run without MV.
    */
   private boolean sameAsWithoutMV(String path, String name) {
      if(MVTest.COMPARE_MODE == 'golden') {
         return true
      }

      File noMV = new File(folder(path), name.replace('__MV.txt', '__NoMV.txt'))
      assert noMV.exists(): 'not exported without MV: ' + noMV
      assert exported(path, name) == noMV.readLines('UTF-8'): name + ' differs from the result without MV'
      return true
   }

   private File folder(String path) {
      return new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                      'exportedData/' + path)
   }
}
