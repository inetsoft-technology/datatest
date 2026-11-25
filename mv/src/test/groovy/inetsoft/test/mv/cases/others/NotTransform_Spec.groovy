package inetsoft.test.mv.cases.others

import inetsoft.test.mv.MVTest
import inetsoft.uql.ConditionList
import inetsoft.uql.asset.AbstractTableAssembly
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.Worksheet
import inetsoft.uql.asset.internal.AssetUtil
import spock.lang.Shared
import spock.lang.Specification
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

/**
 * The viewsheets of the NOT Transform folder: tables the MV analyzer must not create an MV for.
 * Each case checks that no MV is defined or hit, that the analyzer gives the expected reason (an
 * exception in the analyzer also leaves no MV, so "no MV" alone does not show the rule worked),
 * and that the result is the one without MV. Two rules of other folders are here as well (T17):
 * a crosstab table with MV append conditions and a physical query with a variable.
 * <p>
 * Not covered here, see docs/test-gap-assessment/mv.md (T4):
 * <ul>
 * <li>{@code cube}: the cube binding does not resolve in this environment (the chart is empty
 * with and without MV): the base worksheet has no table {@code ___inetsoft_cube_db2_1/l1}, so the
 * analyzer rejects it before the cube rule ("could not find table assembly", logged only, no
 * reason in the analysis result). Since #81659 it no longer throws, and no MV is created.</li>
 * <li>{@code vs assembly-field condition}: its data source {@code testparameter} is not in
 * mvStorage.zip, the table has no columns; the field comparison rule is covered by
 * {@code VSCondtion_Spec.field}.</li>
 * </ul>
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class NotTransform_Spec extends Specification {
   @Shared def admin
   def mvtest
   AssetEntry changed
   Worksheet original

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }

   def cleanup() {
      Throwable failure = null

      // every step runs even when an earlier one fails, so nothing is left for the next case
      [{ mvtest?.removeMV() },
       { if(changed != null) AssetUtil.getAssetRepository(false).setSheet(changed, original, admin, true) }].each { Closure step ->
         try {
            step()
         }
         catch(Throwable e) {
            if(failure == null) {
               failure = e
            }
            else {
               failure.addSuppressed(e)
            }
         }
      }

      if(failure != null) {
         throw failure
      }
   }

   private void run(String name) {
      runPath('NOT Transform/' + name)
   }

   private void runPath(String path) {
      mvtest = new MVTest('1^128^__NULL__^' + path)
      mvtest.executeVSWithoutMV(null, null, false, true)
      mvtest.createMV(false)
      mvtest.executeVS(null, null, false, true)
   }

   /** The lines of an exported file of the MV run. */
   private List<String> exported(String path, String name) {
      File file = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                           'exportedData/' + path + '/' + name)
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   private boolean warned(String text) {
      return mvtest.getAnalysisWarnings().any { it.contains(text) }
   }

   // An embedded table bound to an embedded table component (VSMVAnalyzer.isAcceptable,
   // containsBoundEmbeddedTable).
   def 'embedded-embedded table'() {
      given:
      run('embedded-embedded table')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('Query1: Embedded table assembly and Input assembly bound to embedded table do not support MV.')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // An embedded table an input component (Slider1) is bound to, shown in a table component.
   def 'embedded-input assembly'() {
      given:
      run('embedded-input assembly')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('Query2: Embedded table assembly and Input assembly bound to embedded table do not support MV.')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // A named group whose condition uses a variable (VSMVAnalyzer.containsNamedGroupVariable).
   // The variable has no value, so every employee falls in Others; the total equals the sum of
   // all cells of 'crosstab ws' (same table and measure).
   def 'group variable'() {
      given:
      run('group variable')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('All Sales1: Table assembly with named group referencing a variable does not support MV.')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // The table used as a subquery has a condition of its own (VSMVAnalyzer.analyze).
   def 'subquery1'() {
      given:
      run('subquery1')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('can not support condition defined on the subquery')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // A ranking condition with a parameter ($(top)) on a column that is not visible.
   def 'v-ranking1'() {
      given:
      run('v-ranking1')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('To support parameterized ranking condition "[ORDER_ID] [is] [top] [$(top)]"')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // The same with a bottom N ranking.
   def 'v-ranking2'() {
      given:
      run('v-ranking2')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('To support parameterized ranking condition "[ORDER_ID] [is] [bottom] [$(top)]"')
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // A crosstab table. The analyzer gives no warning, it only records the fault "The block
   // 'All Sales1' is a crosstab table. Moving selections up from a crosstab table is not
   // supported." in the analysis descriptor, which the MV API does not return.
   def 'crosstab ws'() {
      given:
      run('crosstab ws')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         mvtest.getAnalysisWarnings().isEmpty()
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // A pre-condition 'ORDER_ID one of RETURNS1.ORDER_ID' on ORDERS1 and a post-condition
   // 'Sum(QUANTITY) one of RETURNS1.QUANTITY' on RETURNS2, both with a subquery on RETURNS1. The
   // analyzer accepts subquery tables (VSMVAnalyzer.addSubQueryTable), so all four tables get an
   // MV and are hit, and the result is the one without MV. The post-condition used to be
   // 'greater than', which takes the first of the 249 subquery values (Condition.evaluate); their
   // order is not defined (5 first without MV, 1 with MV), so the results differed. A subquery
   // with many values belongs to 'one of', so the worksheet SUBQUERY/subquery was changed.
   def 'subquery'() {
      given:
      run('subquery')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().containsAll(['ORDERS1|TopMV', 'RETURNS1|TopMV', 'RETURNS2|TopMV',
                                            'ASSOCIATION_RETURNS1|TopMV'])
         mvtest.getMVHitInfo().containsAll(['ORDERS1|TopMV', 'RETURNS1|TopMV', 'RETURNS2|TopMV'])
         mvtest.getAnalysisWarnings().isEmpty()
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // Despite the folder name, the analyzer accepts this viewsheet now (a chart and a range
   // slider on the measure): the MV is created and hit, and the result is the one without MV.
   // No bug is known for it; see docs/test-gap-assessment/mv.md §6 item 6. If the analyzer is
   // meant to reject it again, this case fails on the MV checks.
   def 'adhocfilter_measure'() {
      given:
      run('adhocfilter_measure')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().containsAll(['query|TopMV'])
         mvtest.getMVHitInfo().containsAll(['query|TopMV'])
         mvtest.getAnalysisWarnings().isEmpty()
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }
   }

   // The table component is bound to the worksheet table MVCustomers1 itself, a crosstab
   // (groupInfo crosstab="true": state by reseller, Sum(customer_id)) with MV append conditions
   // (M_TestCase21: state = AK, delete state one of AK, CA, FL; M_Testcase26: reseller = false,
   // delete the same). VSMVAnalyzer.isSuitableTable (:853-:858) records the fault "A transformed
   // (rotated/unpivot/crosstab) table has MV update condition." and creates no MV; the MV API does
   // not return the fault, so there is no warning. Unlike Append_Spec.M_Testcase8, where the
   // crosstab is a mirror and the MV falls back to its base table (MVorders1|SubMV), there is no
   // table below, so the viewsheet runs without MV. No golden files: the checks are on the MV
   // run (no MV, rows exported) and, with -Dmv.compare=both|nomv, the result without MV.
   def '#path: a crosstab table with MV append conditions gets no MV'() {
      given:
      runPath(path)

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         mvtest.getAnalysisWarnings().isEmpty()
         exported(path, 'admin_Query1__MV.txt').size() > 2
         mvtest.compareWithoutMV()
      }

      where:
      path << ['MV_Creator/Append/M_TestCase21', 'MV_Creator/Append/M_Testcase26']
   }

   // The control of the case above: without the MV conditions (append and delete) the same
   // crosstab table gets an MV, so the append conditions are what the analyzer rejects. The
   // delete conditions are removed as well: a full build drops the rows they match (S_TestCase23
   // to 26), and the result would differ from the one without MV. The worksheet is changed in
   // the repository and restored in cleanup().
   def 'M_Testcase26 without MV conditions: the crosstab table gets an MV'() {
      given:
      def repository = AssetUtil.getAssetRepository(false)
      AssetEntry entry = AssetEntry.createAssetEntry('1^2^__NULL__^MV_Creator/Append/M_Testcase26^host-org')
      original = (Worksheet) repository.getSheet(entry, admin, false, AssetContent.ALL)
      Worksheet ws = (Worksheet) original.clone()
      AbstractTableAssembly table = (AbstractTableAssembly) ws.getAssembly('MVCustomers1')
      table.setMVUpdatePreConditionList(new ConditionList())
      table.setMVUpdatePostConditionList(new ConditionList())
      table.setMVDeletePreConditionList(new ConditionList())
      table.setMVDeletePostConditionList(new ConditionList())
      changed = entry
      repository.setSheet(entry, ws, admin, true)

      when:
      runPath('MV_Creator/Append/M_Testcase26')

      then:
      verifyAll {
         MVTest.getMVDefInfo() == ['MVCustomers1|TopMV']
         mvtest.getMVHitInfo().contains('MVCustomers1|TopMV')
         mvtest.compareWithoutMV()
      }
   }

   // A worksheet table on a physical query (query4) whose SQL uses a variable: the analyzer
   // rejects it (VSMVAnalyzer.isAcceptable, containsQueryVariable, :723).
   def 'query_variable: a physical query with a variable gets no MV'() {
      given:
      runPath('vs query and condition/query_variable')

      expect:
      verifyAll {
         MVTest.getMVDefInfo().isEmpty()
         mvtest.getMVHitInfo().isEmpty()
         warned('query4_variable: Table assembly referencing physical query with a variable does not support MV.')
         mvtest.compareWithoutMV()
      }
   }
}
