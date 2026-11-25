package inetsoft.test.mv.cases.others

import inetsoft.mv.MVDef
import inetsoft.mv.MVManager
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.mv.MVTest
import inetsoft.uql.asset.AssetContent
import inetsoft.uql.asset.AssetEntry
import inetsoft.uql.asset.internal.AssetUtil
import inetsoft.uql.viewsheet.Viewsheet
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.springframework.test.context.ContextConfiguration
import spock.lang.Execution
import spock.lang.Shared
import spock.lang.Specification

/**
 * Viewsheet options that change the MVs of a viewsheet: "selection association" (the
 * {@code associationEnabled} attribute of {@code viewsheetInfo}) and the "full data" option of
 * the analysis. The checks are on the MV run itself (the MV definitions, the exported
 * components compared with the golden files of the original case), so they also apply in the
 * default {@code golden} mode.
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
class MVViewsheetOptions_Spec extends Specification {
   static final String CENSUS = 'AssociationMV/TestCase_Census'
   static final String[] BOOKMARKS = ['(Home)', 'bk_select']

   @Shared def admin
   def mvtest
   AssetEntry changed
   Viewsheet original

   def setupSpec() {
      MVTest.initHome(this.class.getName())
   }

   def setup() {
      if(admin == null) {
         admin = MVTest.createPrincipal('admin', ['Everyone', 'Administrator'] as String[], new String[0])
      }
   }

   // The case changes the viewsheet TestCase_Census in the repository and cleanup() restores it. A JVM killed
   // before cleanup() leaves the changed sheet behind, and the next run starts from it: restore
   // mvStorage (rm -rf mv/target) and the H2 files after an aborted run.
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

   // TestCase_Census: the selection lists (Region, State, SelectionList1) and the charts are on
   // the same table Data. With association (the default) the analysis also builds
   // ASSOCIATION_Data, and in bk_select the values not associated with the selections are
   // excluded (state 4 or 5; Region 1 of 4, State 15 of 50, the golden files of
   // AssociationMV_Spec). With associationEnabled=false no association MV is built
   // (TableMetaDataTransformer.java:52) and the sandbox does not compute associations: no value
   // is excluded. The charts and the other components are not affected and equal the golden
   // files. The viewsheet is changed in the repository (a copy would lose its bookmarks) and
   // restored in cleanup().
   def 'associationEnabled=false: no ASSOCIATION_ MV, no excluded selection values, the charts unchanged'() {
      given:
      def repository = AssetUtil.getAssetRepository(false)
      AssetEntry entry = AssetEntry.createAssetEntry('1^128^__NULL__^' + CENSUS + '^host-org')
      original = (Viewsheet) repository.getSheet(entry, admin, false, AssetContent.ALL)
      Viewsheet vs = (Viewsheet) original.clone()
      vs.getViewsheetInfo().setAssociationEnabled(false)
      changed = entry
      repository.setSheet(entry, vs, admin, true)
      mvtest = new MVTest('1^128^__NULL__^' + CENSUS)

      when:
      mvtest.executeVSWithoutMV(null, BOOKMARKS, false, true)
      mvtest.createMV(false)
      def defs = MVTest.getMVDefInfo()
      mvtest.executeVS(null, BOOKMARKS, false, true)
      Map<Boolean, List<File>> golden = goldenFiles(CENSUS).groupBy { it.text.contains('SelectState:') }

      then:
      verifyAll {
         defs.toSet() == ['Data|TopMV'] as Set
         mvtest.getMVHitInfo().contains('Data|TopMV')
         golden[true]*.name.sort() == ['admin_Region__MV.txt', 'admin_Region_bk_select__MV.txt',
                                       'admin_SelectionList1__MV.txt', 'admin_SelectionList1_bk_select__MV.txt',
                                       'admin_State__MV.txt', 'admin_State_bk_select__MV.txt']
         golden[true].findAll { excluded(exported(CENSUS, it.name)) } == []
         golden[true].findAll { values(exported(CENSUS, it.name)) != values(it.readLines('UTF-8')) }*.name == []
         golden[false].findAll { exported(CENSUS, it.name) != it.readLines('UTF-8') }*.name == []
         mvtest.compareWithoutMV()
      }
   }

   // TestCase_Selectionlist1: the selection lists are on MVSCRIPT1, MVSCRIPT2 and MVSCRIPT3,
   // tables that nothing else uses, on the columns INTEGER, STRING and DATE1. With the analysis
   // option "full data" off, such a table gets a thin MV of the selection columns only
   // (VSMVAnalyzer.shrinkTable, :547), plus the date levels of DATE1; with it on (the default,
   // and what the other cases use), all columns. The results are those of the golden files
   // either way.
   def 'full data #full: the MVs of the tables used only by selections have #label'() {
      given:
      mvtest = new MVTest('1^128^__NULL__^AssociationMV/TestCase_Selectionlist1')
      mvtest.setFullData(full)

      when:
      mvtest.executeVSWithoutMV(null, BOOKMARKS, false, true)
      mvtest.createMV(false)
      MVManager manager = MVManager.getManager()
      List<MVDef> selectionOnly = mvtest.materializedViewNames().collect { manager.get(it) }
         .findAll { MVDef mv -> mv != null && mv.getMVTable().startsWith('MVSCRIPT') }
      mvtest.executeVS(null, BOOKMARKS, false, true)

      then:
      verifyAll {
         selectionOnly.collectEntries { [it.getMVTable(), it.getColumns()*.getName().sort()] } == columns
         selectionOnly.every { it.getMetaData().isFullData() == full }
         mvtest.getMVHitInfo().containsAll(['MVSCRIPT1|TopMV', 'MVSCRIPT2|TopMV', 'MVSCRIPT3|TopMV'])
         mvtest.compareData(false)
         mvtest.compareWithoutMV()
      }

      where:
      full  | label                       | columns
      false | 'the selection column only' | [MVSCRIPT1: ['INTEGER'], MVSCRIPT2: ['STRING'],
                                               MVSCRIPT3: ['DATE1', 'Day(DATE1)', 'Month(DATE1)', 'Week(DATE1)', 'Year(DATE1)']]
      true  | 'all columns'               | [MVSCRIPT1: ['BYTE', 'DOUBLE', 'FLOAT', 'INTEGER', 'LONG', 'SHORT'],
                                               MVSCRIPT2: ['BOOLEAN', 'CHARACTER', 'STRING'],
                                               MVSCRIPT3: ['DATE1', 'Day(DATE1)', 'Day(TIMEINSTANT)', 'Month(DATE1)',
                                                           'Month(TIMEINSTANT)', 'TIME', 'TIMEINSTANT', 'Week(DATE1)',
                                                           'Week(TIMEINSTANT)', 'Year(DATE1)', 'Year(TIMEINSTANT)']]
   }

   /** The golden files of a case (the __MV ones, not the incremental ones). */
   private List<File> goldenFiles(String path) {
      File folder = new File(getClass().getResource('/expectData/' + path).toURI())
      return folder.listFiles().findAll { it.name.endsWith('__MV.txt') }.sort { it.name }
   }

   /** The lines of an exported file of the MV run. */
   private List<String> exported(String path, String name) {
      File file = new File(new File(getClass().getResource('/expectData').getPath()).getParent(),
                           'exportedData/' + path + '/' + name)
      assert file.exists(): 'not exported: ' + file
      return file.readLines('UTF-8')
   }

   /** The lines of a selection list with an excluded value (state 4 or 5). */
   private static List<String> excluded(List<String> lines) {
      return lines.findAll { it =~ /SelectState:\[[45]\]/ }
   }

   /** The values of a selection list, without states and measures. */
   private static List<String> values(List<String> lines) {
      return lines.findAll { it.contains('SelectState:') }.collect { it.substring(0, it.indexOf(']') + 1) }
   }
}
