package inetsoft.test.vsscript.state

import inetsoft.test.vsscript.support.ScriptStateBase
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.analytic.composition.ViewsheetService
import inetsoft.test.core.MessageTestUtils
import inetsoft.test.vsscript.support.RecordingDispatcher
import inetsoft.util.ConfigurationContext
import inetsoft.web.viewsheet.controller.OnClickService
import inetsoft.web.viewsheet.service.CoreLifecycleService
import inetsoft.web.binding.event.VSOnClickEvent
import inetsoft.report.composition.WorksheetService
import inetsoft.sree.internal.cluster.Cluster
import inetsoft.web.viewsheet.controller.chart.VSChartAreasService
import inetsoft.web.viewsheet.controller.chart.VSChartAreasServiceProxy
import inetsoft.web.viewsheet.controller.chart.VSChartFlyoverService
import inetsoft.web.viewsheet.event.ApplySelectionListEvent
import inetsoft.web.viewsheet.event.chart.VSChartFlyoverEvent
import inetsoft.web.viewsheet.service.VSInputService
import inetsoft.web.viewsheet.service.VSSelectionService
import inetsoft.web.viewsheet.service.VSObjectService
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * The manual cases of docs/vsscript/VSScript-Manual-Case.xlsx whose check points are values that the script
 * puts into the assembly state, checked on the runtime viewsheet after it is opened. No PNG and no browser:
 * what the manual tester reads off the screen is read here from the scope. The mapping of every manual case
 * to a test (or the reason it stays manual) is in docs/test-gap-assessment/vsscript-manual-automation.md.
 */
@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ScriptStateManual_Spec extends Specification {
   static String id(String path) { "1^128^__NULL__^Manual/${path}" }

   /**
    * The assembly info the script wrote to. A property that is read back through the script scope only is not
    * enough: a property that is missing from the scriptable still reads back what the script assigned (it becomes a
    * plain JavaScript field), as the mutation check of the tip view showed.
    */
   /** OnClickService with a real VSInputService; the editor-only dependencies of VSInputService are null (as in ScriptStateSubmit_Spec) */
   static OnClickService clickService() {
      def ctx = ConfigurationContext.getContext()
      def input = new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                                     ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
      new OnClickService(ctx.getSpringBean(CoreLifecycleService), input, ctx.getSpringBean(ViewsheetService))
   }

   /** click an assembly: runs its onClick script, as the click in the browser does */
   static RecordingDispatcher.Recorder click(ScriptStateBase t, String assembly) {
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         clickService().onClick(t.runtimeId, assembly, null, null, null, ScriptStateBase.BASE_URL, t.principal, rec)
      }
      rec
   }

   static VSInputService inputService() {
      def ctx = ConfigurationContext.getContext()
      new VSInputService(ctx.getSpringBean(VSObjectService), ctx.getSpringBean(CoreLifecycleService),
                         ctx.getSpringBean(ViewsheetService), null, null, null, null, null, null, null)
   }

   /** set the value of a radio button, check box, combo box, slider, spinner or text input as the browser does, and refresh */
   static RecordingDispatcher.Recorder input(ScriptStateBase t, String assembly, Object value) {
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         inputService().singleApplySelection(t.runtimeId, assembly, value, t.principal, rec, ScriptStateBase.BASE_URL)
      }
      rec
   }

   /** select (or clear, selected=false) values of a selection list, one path per value, as a click in the list does */
   static RecordingDispatcher.Recorder select(ScriptStateBase t, String assembly, List<List<String>> values, boolean selected = true) {
      def event = new ApplySelectionListEvent()
      event.setType(ApplySelectionListEvent.Type.APPLY)
      event.setValues(values.collect { v ->
         def x = new ApplySelectionListEvent.Value()
         x.setValue(v as String[])
         x.setSelected(selected)
         x
      })
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         ConfigurationContext.getContext().getSpringBean(VSSelectionService)
            .applySelection(t.runtimeId, assembly, event, t.principal, rec, ScriptStateBase.BASE_URL)
      }
      rec
   }

   /** answer the confirm dialog of a click (Yes or No) */
   static RecordingDispatcher.Recorder confirm(ScriptStateBase t, String assembly, boolean yes) {
      def event = new VSOnClickEvent()
      event.setConfirmEvent(true)
      event.setConfirmed(yes)
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         clickService().onConfirm(t.runtimeId, assembly, null, null, true, event, ScriptStateBase.BASE_URL, t.principal, rec)
      }
      rec
   }

   /** flyover (hover, or click when flyOnClick is set) over a data point of a chart; conditions is the selection string of the point, e.g. 'State^VALUE:CA' */
   static RecordingDispatcher.Recorder flyover(ScriptStateBase t, String chart, String conditions) {
      def event = new VSChartFlyoverEvent()
      event.setChartName(chart)
      event.@conditions = conditions
      def rec = RecordingDispatcher.create(t.principal)
      MessageTestUtils.withMockMessageContext(t.principal, t.runtimeId) {
         def ctx = ConfigurationContext.getContext()
         def viewsheetService = ctx.getSpringBean(ViewsheetService)
         def areas = new VSChartAreasServiceProxy(Cluster.getInstance(), ctx.getSpringBean(WorksheetService),
                                                  new VSChartAreasService(viewsheetService))
         new VSChartFlyoverService(ctx.getSpringBean(CoreLifecycleService), viewsheetService, areas)
            .eventHandler(t.runtimeId, event, ScriptStateBase.BASE_URL, t.principal, rec)
      }
      rec
   }

   static Object model(ScriptStateBase t, String assembly) { t.vs.getAssembly(assembly).getVSAssemblyInfo() }

   // ---- Viewsheet ----

   def 'Viewsheet1_toolbar_actions_hidden_by_script'() {
      // VS_Action: onLoad hides 16 toolbar actions. Text1 prints isActionVisible for 15 names, two of them
      // misspelt in the asset ('Social Scharing', 'Full Scheen'), which stay visible
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_Action'))

      expect:
      t.eval('Text1.value') == 'false, false, false, false, false, false, true, false, false, false, false, false, true, false, false'
      ['Back', 'Previous', 'Next', 'Edit', 'Refresh', 'Email', 'Social Sharing', 'Schedule', 'Export', 'Import',
       'Zoom', 'Print', 'Full Screen', 'Close', 'Bookmark', 'Home'].every { t.eval("thisViewsheet.isActionVisible('${it}')") == false }
   }

   def 'ExportAction_hides_the_selected_export_type'() {
      // VS_ExportAction: onLoad hides the export format that is selected in the ExportType radio button
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_ExportAction'))
      def selected = t.eval('ExportType.selectedObject')

      expect:
      selected != null
      t.eval('Text1.value') == 'this action visible is: false'
      t.eval("thisViewsheet.isActionVisible('${selected}')") == false
   }

   // ---- Script scope: parameters ----

   def 'ScopePara2_parameters_of_every_type_read_back'() {
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara2'))

      expect:
      t.eval('Text1.value') == 'true, 5, a,  NJ,AK,IL,NK, 1997-01-01,  1999-12-31'
   }

   def 'ScopePara4_parameter_from_the_open_request'() {
      // the manual case opens the viewsheet with ?myPara=testaaa
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara4'), [myPara: ['testaaa'] as String[]])

      expect:
      t.eval('Text1.value') == 'testaaa'
   }

   def 'ScopePara4_without_the_parameter_is_empty_text'() {
      // negative control for the case above
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara4'))

      expect:
      t.eval('Text1.value') != 'testaaa'
   }

   // ---- Script scope: order and relations ----

   def 'ScriptOrderOneWay1_assemblies_follow_Text1'() {
      // Script_Order_OneWay1: five assemblies copy a property of Text1 in their component script
      given:
      def t = new ScriptStateBase(id('ScriptOrder/Script_Order_OneWay1'))

      expect:
      t.eval('SelectionList1.alignment') == t.eval('Text1.alignment')
      t.eval('CheckBox1.borders') == t.eval('Text1.borders')
      t.eval('FreehandTable1.data[1][0]') == t.eval('Text1.value')
   }

   def 'ScriptOrderOneWay1_change_of_Text1_reaches_the_others'() {
      // the manual case sets Text1 in the UI (value, background, alignment) and checks the five
      given:
      def t = new ScriptStateBase(id('ScriptOrder/Script_Order_OneWay1'))

      when:
      t.setScript('Text1', "value = 'test order'\nbackground = 'green'\nalignment = 4")

      then:
      t.eval('FreehandTable1.data[1][0]') == 'test order'
      t.eval('Rectangle1.background') == t.eval('Text1.background')
      t.eval('Rectangle1.background') == t.eval('java.awt.Color.green')
      t.eval('SelectionList1.alignment') == 4
   }

   def 'ScriptOrder4_onInit_and_onLoad_properties'() {
      // Script_Order4: onInit sets the chart background, onLoad the flyover views, the crosstab script the tip view
      given:
      def t = new ScriptStateBase(id('ScriptOrder/Script_Order4'))

      expect:
      t.eval('Chart1.flyoverViews') == ['Crosstab1']
      t.eval('Crosstab1.tipView') == 'Gauge1'
      t.eval('Chart1.background') == t.eval('java.awt.Color.pink')
   }

   def 'SEvent_onInit_sets_the_list_sort'() {
      given:
      def t = new ScriptStateBase(id('OtherScript/sEvent'))

      expect:
      t.eval('SelectionList1.sortType') == t.eval('StyleConstant.SORT_DESC')
   }

   // ---- Properties set by component scripts ----

   def 'ChartTooltip_tooltip_pattern'() {
      given:
      def t = new ScriptStateBase(id('Chart/TestCase-ChartTooltip'))

      expect:
      t.eval('Chart1.toolTip') == '<b>State</b>: {0} <br><b>Quantity Purchased</b>: {1,number,$##,###.00}'
      model(t, 'Chart1').getVSChartInfo().getToolTip() == '<b>State</b>: {0} <br><b>Quantity Purchased</b>: {1,number,$##,###.00}'
   }

   def 'ChartTipView_view_and_alpha'() {
      given:
      def t = new ScriptStateBase(id('Chart/TestCase-ChartTipView'))

      expect:
      t.eval('Chart1.tipView') == 'Gauge1'
      model(t, 'Chart1').alpha == '60.0'
      // tipAlpha is declared as a String property (ChartVSAScriptable, TableDataVSAScriptable), so the number that
      // the script sets reads back as the text '60.0'
      t.eval('Chart1.tipAlpha') == '60.0'
   }

   def 'ChartTipView_script_reaches_the_assembly'() {
      // the asset has the tip view in its own settings as well, so the case above cannot tell whether the script
      // worked (mutation check: a renamed tipView property still passed). A different value from the script can.
      given:
      def t = new ScriptStateBase(id('Chart/TestCase-ChartTipView'))
      assert model(t, 'Chart1').tipView == 'Gauge1'

      when:
      t.setScript('Chart1', "tipView = 'Crosstab9'; tipAlpha = 33")

      then:
      model(t, 'Chart1').tipView == 'Crosstab9'
      model(t, 'Chart1').alpha == '33.0'
   }

   def 'ChartFlyover_views_and_click'() {
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_ChartFlyover'))

      expect:
      t.eval('Chart1.flyoverViews') == ['Crosstab1', 'Gauge1']
      t.eval('Chart1.flyOnClick') == true
      model(t, 'Chart1').flyoverViews as List == ['Crosstab1', 'Gauge1']
      model(t, 'Chart1').flyOnClick == true
   }

   def 'TableAction1_tip_and_flyover_on_table_crosstab_freehand'() {
      given:
      def t = new ScriptStateBase(id('Table/Table_Action1'))

      expect:
      t.eval('TableView1.tipView') == 'Chart1'
      t.eval('TableView1.tipAlpha') == '50.0'
      t.eval('Crosstab2.tipAlpha') == '70.0'
      t.eval('FreehandTable1.tipAlpha') == '90.0'
   }

   def 'TableAction5_form_flags_follow_the_radio_button'() {
      // Table_Action5_form: form, edit, del and insert are all set from RadioButton1
      given:
      def t = new ScriptStateBase(id('Table/Table_Action5_form'))
      def selected = t.eval('RadioButton1.selectedObject')

      expect:
      ['form', 'edit', 'del', 'insert'].every { t.eval("TableView1.${it}") == selected }
   }

   def 'TableAction4_action_visibility_set_by_script'() {
      given:
      def t = new ScriptStateBase(id('Table/Table_Action4_actionVisble'))

      expect:
      ['TableView1', 'Crosstab1', 'FreehandTable1'].every { a ->
         ['Export', 'Show Details', 'Maximize', 'Hide MiniToolbar', 'Annotate Cell', 'Set Cell Size',
          'Annotate Component'].every { t.eval("${a}.isActionVisible('${it}')") == false }
      }
      t.eval("Crosstab1.isActionVisible('Drill Down')") == false
      t.eval('Crosstab1.drillEnabled') == false
   }

   def 'SelectionContainer_properties_follow_the_radio_buttons'() {
      given:
      def t = new ScriptStateBase(id('Selection/Selection_Conatiner'))

      expect:
      t.eval('CurrentSelection1.adhocEnabled') == t.eval('RadioButton1.selectedObject')
      t.eval('CurrentSelection2.showCurrentSelection') == t.eval('RadioButton2.selectedObject')
   }

   def 'SelectionActionVisible_set_by_script'() {
      given:
      def t = new ScriptStateBase(id('Selection/Selection_ActionVisible'))

      expect:
      ['Search', 'Sort', 'Reverse', 'Unselect', 'Maximize', 'Hide MiniToolbar', 'Select All'].every {
         t.eval("SelectionList1.isActionVisible('${it}')") == false
      }
      ['Clear Subtree', 'Select Subtree'].every { t.eval("SelectionTree1.isActionVisible('${it}')") == false }
      t.eval("RangeSlider1.isActionVisible('Edit')") == false
   }

   def 'SpinnerScript_range_and_increment'() {
      given:
      def t = new ScriptStateBase(id('Form/Spinner'))

      expect:
      t.eval('Spinner1.min') == 10
      t.eval('Spinner1.max') == 200
      t.eval('Spinner1.increment') == 20
   }

   def 'ComboBoxBind_values_labels_and_date_range'() {
      given:
      def t = new ScriptStateBase(id('Form/Combox_bind'))

      expect:
      t.eval('ComboBox1.values') == [4, 2, 3, 1]
      t.eval('ComboBox1.labels') == ['a4', 'b2', 'c3', 'd1']
      t.eval('ComboBox1.selectedObject') == 4
      t.eval('ComboBox2.embeddedDataOnBottom') == true
   }

   def 'GroupContainerScript_animate_follows_the_radio_button'() {
      given:
      def t = new ScriptStateBase(id('Others/GroupConatiner'))

      expect:
      t.eval('GroupContainer1.animate') == t.eval('RadioButton1.selectedObject')
      t.eval('GroupContainer1.scaleImage') == true
   }

   def 'ScopeFour_table_title_and_background_from_onInit_onLoad'() {
      // scope4: onInit defines color, onLoad defines fun(); the table script uses both
      given:
      def t = new ScriptStateBase(id('OtherScript/scope4'))

      expect: 'no value typed: fun() returns an empty text, which leaves the title null; the background is the onInit color'
      t.eval('TableView1.title') == null
      t.eval('TableView1.background') == t.eval('new java.awt.Color(200, 239, 212)')
   }

   def 'ParameterScript_state_parameter_reaches_the_text_and_the_user_is_on_the_freehand_table'() {
      // parameterScript: the manual case types NK,NY,NJ in the parameter dialog
      given:
      def t = new ScriptStateBase(id('OtherScript/Script_Order_OneWay4'), [state: ['NK,NY,NJ'] as String[]])

      expect:
      t.eval('Text1.value') == 'NK,NY,NJ'
      t.eval('FreehandTable1.data[0][0]') == 'admin'
   }

   def 'SetAction_chart_actions_after_the_image_is_clicked'() {
      // TestCase_SetAction: the check point is the text after clicking the image (its onClick script hides the chart
      // actions). The screenshot of the manual case has all 26 lines false; before the click they are all true.
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_SetAction'))
      def values = { t.eval('Text1.text').toString().readLines().collect { it.substring(it.lastIndexOf(':') + 1) } }

      expect:
      values().size() == 26
      values().every { it == 'true' }

      when:
      click(t, 'Image1')

      then:
      values().size() == 26
      values().every { it == 'false' }
   }

   def 'ScopePara1_parameters_written_by_onLoad_after_the_parameter_dialog'() {
      // scopePara1 has four prompt variables. The browser shows the "Enter Parameters" dialog and sends all four when
      // OK is clicked (empty values here); only then onLoad runs. The expected text is the screenshot of the manual
      // case, and the same text appears in the browser on a real server (checked 2026-11-19, enterprise image of
      // 2026-11-18, admin, dialog confirmed with its default values). Opening without the four variables leaves the
      // viewsheet waiting for the dialog: onLoad has not run and the text shows the onInit value of myPara.
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara1'),
         ['>customer_id': [''] as String[], varBoolean: ['true'] as String[], state: [''] as String[], vsConVar: [''] as String[]])
      def lines = t.eval('Text1.value').toString().readLines()

      expect:
      lines[0..5] == ['Radiobutton:state', 'VSCon: AK', 'myPara: onRefresh', '>customer_id: 5', 'state: IL, AK, NJ', 'varBoolean:true']
      lines.find { it.startsWith('user:') } == 'user: admin'
   }

   def 'ScopePara1_before_the_parameter_dialog_onLoad_has_not_run'() {
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara1'))

      expect:
      t.eval('Text1.value').toString().readLines().find { it.startsWith('myPara:') } == 'myPara: test'
   }

   // ---- The manual case does something first (input, selection, click, confirm); the expected value is the screenshot of the case ----

   /** flip a radio button between true and false (and back) and check that the property of the assembly follows it */
   static void followsRadio(ScriptStateBase t, String radio, String assembly, String property) {
      [true, false, true].each { v ->
         input(t, radio, v)
         assert t.eval("${assembly}.${property}") == v: "${assembly}.${property} after ${radio}=${v}"
      }
   }

   def 'ExportAction_each_selected_type_stays_hidden'() {
      // VS_ExportAction: the case selects the types one after the other and refreshes; every type that was selected
      // is still missing from the Export, Email and Schedule dialogs, and the text says false
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_ExportAction'))
      def types = t.eval('ExportType.values') as List
      def visible = { String type -> t.eval("thisViewsheet.isActionVisible('${type}')") }

      expect: 'only the initially selected type is hidden before anything is selected'
      types.size() == 7
      visible(types[0]) == false
      types.drop(1).every { visible(it) == true }

      when:
      def seen = []
      types.each { type ->
         input(t, 'ExportType', type)
         seen << type
         assert t.eval('Text1.value') == 'this action visible is: false'
         assert seen.every { visible(it) == false }: "after ${type}: ${seen.collect { it + '=' + visible(it) }}"
      }

      then:
      seen.every { visible(it) == false }
   }

   def 'ComboBox_script_keeps_its_selection'() {
      // Combox_bind: Text1 shows the selected label and value (screenshot: a4-4). The component script sets
      // selectedObjects = 4 on every refresh, so picking c3 leaves a4-4 (the arrows of the screenshot point at c3 and a4-4)
      given:
      def t = new ScriptStateBase(id('Form/Combox_bind'))

      expect:
      t.eval('Text1.value') == 'The Combox label and value: a4-4'

      when:
      input(t, 'ComboBox1', 3d)

      then:
      t.eval('Text1.value') == 'The Combox label and value: a4-4'
   }

   def 'Spinner_value_reaches_the_text'() {
      // Spinner: the text follows the spinner (screenshot after the arrow keys: 60, 'Spinner value and Label is:  60: 60')
      given:
      def t = new ScriptStateBase(id('Form/Spinner'))

      when:
      input(t, 'Spinner1', 60d)

      then:
      t.eval('Spinner1.selectedObject') == 60d
      t.eval('Text1.value') == 'Spinner value and Label is:  60: 60'
   }

   def 'ScopeFour_title_follows_the_text_input'() {
      // scope4: typing 3 and Enter gives 'you send value < 5', typing 10 gives 'your send value >= 5' (the two screenshots)
      given:
      def t = new ScriptStateBase(id('OtherScript/scope4'))

      when:
      input(t, 'TextInput1', '3')

      then:
      t.eval('TableView1.title') == 'you send value < 5'

      when:
      input(t, 'TextInput1', '10')

      then:
      t.eval('TableView1.title') == 'your send value >= 5'
      t.eval('TableView1.background') == t.eval('new java.awt.Color(200, 239, 212)')
   }

   def 'TableAction5_form_flags_follow_the_radio_both_ways'() {
      // Table_Action5_form: RadioButton1 defaults to true; false removes the edit, insert and delete menu items
      given:
      def t = new ScriptStateBase(id('Table/Table_Action5_form'))

      expect:
      t.eval('RadioButton1.selectedObject') == true
      ['form', 'edit', 'del', 'insert'].every { t.eval("TableView1.${it}") == true }

      when:
      input(t, 'RadioButton1', false)

      then:
      ['form', 'edit', 'del', 'insert'].every { t.eval("TableView1.${it}") == false }
   }

   def 'Scope1_table_visibility_follows_the_selection'() {
      // scope1: visible with 15 rows or more, hidden under 5 rows, 'hide on print and export' (visible on screen) in
      // between. The case selects AK (4 rows: hidden), then AK and CA (9 rows: shown)
      given:
      def t = new ScriptStateBase(id('OtherScript/scope1'))

      expect:
      t.eval('TableView1.data.length') == 39d
      t.eval('TableView1.visible') == true

      when:
      select(t, 'SelectionList1', [['AK']])

      then:
      t.eval('TableView1.data.length') == 4d
      t.eval('TableView1.visible') == false

      when:
      select(t, 'SelectionList1', [['CA']])

      then:
      t.eval('TableView1.data.length') == 9d
      t.eval('TableView1.visible') == true
   }

   def 'ListTree1_properties_follow_the_radio_buttons'() {
      given:
      def t = new ScriptStateBase(id('Selection/ListTree1'))

      when:
      [['dropdown', 'dropdown'], ['singleSelection', 'singleSelection'], ['wrap', 'wrapping']].each { r, prop ->
         followsRadio(t, r, 'SelectionList1', prop)
         followsRadio(t, r, 'SelectionTree1', prop)
      }
      followsRadio(t, 'expandAll', 'SelectionTree1', 'expandAll')

      then:
      noExceptionThrown()
   }

   def 'SelectionCalendar_properties_follow_the_radio_buttons'() {
      // Selection_Calendar: seven radio buttons, each switches one property of Calendar1 (the table of the manual case)
      given:
      def t = new ScriptStateBase(id('Selection/Selection_Calendar'))

      when:
      [['daySelect', 'daySelection'], ['period', 'period'], ['singleSelect', 'singleSelection'],
       ['submitOnChange', 'submitOnChange'], ['year', 'yearView'], ['dropdown', 'dropdown'],
       ['doubleCalednar', 'doubleCalendar']].each { r, prop ->
         followsRadio(t, r, 'Calendar1', prop)
      }

      then:
      noExceptionThrown()
   }

   def 'SelectionContainer_properties_follow_the_radio_buttons_both_ways'() {
      given:
      def t = new ScriptStateBase(id('Selection/Selection_Conatiner'))

      when:
      followsRadio(t, 'RadioButton1', 'CurrentSelection1', 'adhocEnabled')
      followsRadio(t, 'RadioButton2', 'CurrentSelection2', 'showCurrentSelection')

      then:
      noExceptionThrown()
   }

   def 'Highlight_click_on_the_image_applies_the_highlight'() {
      // TestCase_ChartHightlight: clicking the image shows 'State_highlight was applied' (screenshot)
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_ChartHightlight'))

      when:
      def msgs = RecordingDispatcher.messages(click(t, 'Image1')).collect { it.message }

      then:
      msgs == ['State_highlight was applied']
   }

   def 'Padding_click_on_the_image_sets_the_chart_padding'() {
      // TestCase_Padding: clicking the image applies the padding to the chart (screenshot: space on all four sides)
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_Padding'))

      expect:
      t.eval('Chart1.padding').toString() == 'java.awt.Insets[top=10,left=10,bottom=10,right=10]'

      when:
      click(t, 'Image1')

      then:
      t.eval('Chart1.padding').toString() == 'java.awt.Insets[top=50,left=50,bottom=50,right=50]'
   }

   def 'DrillEnabled_click_on_the_text_turns_the_drill_off'() {
      // TestCase_DrillEnabled: the drill-down icons are visible, after clicking the text they are gone
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_DrillEnabled'))

      expect:
      t.eval('Chart1.drillEnabled') == true

      when:
      click(t, 'Text1')

      then:
      t.eval('Chart1.drillEnabled') == false
   }

   def 'ScopeFive_text_and_alert_follow_the_state_count'() {
      // scope5: 31 states give 'state length is: 31'; selecting AK leaves one, the text says too small and an alert shows
      // (the screenshots image61, image62, image63)
      given:
      def t = new ScriptStateBase(id('OtherScript/scope5'))

      expect:
      t.eval('Text1.value') == 'state length is: 31'

      when:
      def msgs = RecordingDispatcher.messages(select(t, 'SelectionList1', [['AK']])).collect { it.message }

      then:
      t.eval('Text1.value') == 'state length too small'
      msgs == ['state length too small']
   }

   def 'ScopeParaThree_radio_buttons_drive_the_chart_style_and_the_text'() {
      // scopePara3: city (the screenshot, line chart) or state (bar chart); the text shows the first radio button
      given:
      def t = new ScriptStateBase(id('OtherScript/scopePara3'))

      when:
      input(t, 'RadioButton1', 'city')
      input(t, 'RadioButton2', 'reseller')

      then:
      t.eval('Text1.value') == 'city'
      t.eval('Chart1.separatedStyle') == t.eval('Chart.CHART_LINE')

      when:
      input(t, 'RadioButton1', 'state')

      then:
      t.eval('Text1.value') == 'state'
      t.eval('Chart1.separatedStyle') == t.eval('Chart.CHART_BAR')
   }

   def 'OneWay2_export_action_follows_the_visibility_of_Text1'() {
      // Script_Order_OneWay2: onLoad hides the Export action as long as Text1 is hidden; showing Text1 in the
      // properties dialog brings it back (the case then also edits the alpha, which changes the chart's top N)
      given:
      def t = new ScriptStateBase(id('ScriptOrder/Script_Order_OneWay2'))
      def info = model(t, 'Text1')
      def original = info.getVisibleValue()

      expect:
      t.eval('Text1.visible') == false
      t.eval("thisViewsheet.isActionVisible('Export')") == false

      when:
      info.setVisibleValue('show')
      t.res.refreshViewsheet(t.principal)

      then:
      t.eval("thisViewsheet.isActionVisible('Export')") == true

      when:
      info.setVisibleValue(original)
      t.res.refreshViewsheet(t.principal)

      then:
      t.eval("thisViewsheet.isActionVisible('Export')") == false
   }

   def 'ScopeTwo_text_background_from_its_script'() {
      // scope2: the text is green (screenshot image57)
      given:
      def t = new ScriptStateBase(id('OtherScript/scope2'))

      expect:
      t.eval('Text1.background') == t.eval('java.awt.Color.green')
   }

   def 'ComboBoxBind_date_range_and_time_zone_of_the_date_boxes'() {
      // Combox_bind: the third and fourth boxes cover 2020-11-01 to 2020-11-30 (screenshots), the last with the server time zone
      given:
      def t = new ScriptStateBase(id('Form/Combox_bind'))

      expect:
      ['ComboBox3', 'ComboBox4'].every {
         t.eval("formatDate(${it}.minDate, 'yyyy-MM-dd')") == '2020-11-01' &&
            t.eval("formatDate(${it}.maxDate, 'yyyy-MM-dd')") == '2020-11-30'
      }
      t.eval('ComboBox3.serverTimeZone') == false
      t.eval('ComboBox4.serverTimeZone') == true
   }

   def 'FormRefresh_checkbox_refreshAfterSubmit_follows_the_radio_button'() {
      // Form_Refresh (Bug #57856): the check box takes refreshAfterSubmit from the RefreshIs radio button
      given:
      def t = new ScriptStateBase(id('Form/Form_Refresh'))

      expect:
      t.eval('RefreshIs.selectedObject') == true
      t.eval('CheckBox1.refreshAfterSubmit') == true

      when:
      followsRadio(t, 'RefreshIs', 'CheckBox1', 'refreshAfterSubmit')

      then:
      noExceptionThrown()
   }

   def 'SubmitOnChange_selection_assemblies_follow_the_radio_button'() {
      // Selection_submitOnChange: with false the Apply icon shows in the mini toolbar (screenshots image13, image14)
      given:
      def t = new ScriptStateBase(id('Selection/Selection_submitOnChange'))

      expect:
      t.eval('SubmitOnChange.selectedObject') == true

      when:
      followsRadio(t, 'SubmitOnChange', 'SelectionList1', 'submitOnChange')
      followsRadio(t, 'SubmitOnChange', 'SelectionTree1', 'submitOnChange')

      then:
      noExceptionThrown()
   }

   def 'Confirm_submit_asks_and_the_answer_decides_the_state'() {
      // sConfirm: clicking Submit shows 'this is test, click Yes or No' (image65). Yes disables the selection list and
      // hides the radio button (image82), No leaves both as they were (image83)
      given:
      def yes = new ScriptStateBase(id('OtherScript/sConfirm'))
      def no = new ScriptStateBase(id('OtherScript/sConfirm'))

      expect:
      yes.eval('SelectionList1.enabled') == true
      yes.eval('RadioButton1.visible') == true

      when:
      def asked = RecordingDispatcher.messages(click(yes, 'Submit1'))

      then:
      asked.collect { it.message } == ['this is test, click Yes or No']
      asked.every { it.type.toString() == 'CONFIRM' }
      yes.eval('SelectionList1.enabled') == true

      when:
      confirm(yes, 'Submit1', true)

      then:
      yes.eval('SelectionList1.enabled') == false
      yes.eval('RadioButton1.visible') == false

      when:
      click(no, 'Submit1')
      confirm(no, 'Submit1', false)

      then:
      no.eval('SelectionList1.enabled') == true
      no.eval('RadioButton1.visible') == true
   }

   // ---- Viewsheet3: Viewsheet.setInputSelectedObject, four triggers (element, onInit, onClick, refresh) ----

   /** the five inputs of the embedded viewsheet Viewsheet3 (radio button, check box, combo box, slider, spinner) as numbers */
   static List<Double> embeddedInputs(ScriptStateBase t) {
      ['metricName_radiobutton', 'metricName_checkbox', 'metricName_combobox', 'metricName_slider', 'metricName_spinner'].collect { n ->
         def a = t.vs.getAssembly('Viewsheet3.' + n)
         def v = a.class.simpleName == 'CheckBoxVSAssembly' ? a.getSelectedObjects().toList().find() : a.getSelectedObject()
         v == null ? null : Double.valueOf(v.toString())
      }
   }

   static Double passedParameter(ScriptStateBase t) { t.eval('Viewsheet1.thisParameter.metricName') as Double }

   def 'SetInputSelectedObject_element_trigger_follows_the_slicer'() {
      // VS_setInputSelectedObject_element: the slicer is a radio button; 10 puts 10 into every input of the embedded
      // viewsheet and into the parameter of the other embedded viewsheet, 20 puts 20 (the manual case, steps 2 and 3)
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_setInputSelectedObject_element'))

      expect:
      embeddedInputs(t) == [1d, 1d, 1d, 1d, 1d]

      when:
      input(t, 'metricNameSlicer', '10')

      then:
      embeddedInputs(t) == [10d, 10d, 10d, 10d, 10d]
      passedParameter(t) == 10d
      model(t, 'Viewsheet1.Text2').getText().toString() == '10.0'
      // Bug #72320: the embedded viewsheet's parameter table gets the value too, so parameter.<input> is current at once
      t.rvs.getViewsheetSandbox().get().getSandbox('Viewsheet3').getVariableTable().get('metricName_radiobutton').toString() == '10'

      when:
      input(t, 'metricNameSlicer', '20')

      then:
      embeddedInputs(t) == [20d, 20d, 20d, 20d, 20d]
      passedParameter(t) == 20d
   }

   def 'SetInputSelectedObject_init_trigger_applies_the_slicer_value_on_open'() {
      // VS_setInputSelectedObject_init: onInit applies the slicer (a combo box that starts at 20) when the viewsheet opens
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_setInputSelectedObject_init'))

      expect:
      t.eval('metricNameSlicer.selectedObject') == 20d
      embeddedInputs(t) == [20d, 20d, 20d, 20d, 20d]
      passedParameter(t) == 20d
   }

   def 'SetInputSelectedObject_onclick_trigger_applies_on_the_click'() {
      // VS_setInputSelectedObject_onclick: moving the slider changes nothing; the Submit click applies it
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_setInputSelectedObject_onclick'))
      def before = embeddedInputs(t)

      when:
      model(t, 'metricNameSlicer').setSelectedObject(10d)

      then: 'the new slider value alone does not reach the embedded viewsheet'
      embeddedInputs(t) == before

      when:
      click(t, 'Submit1')

      then:
      embeddedInputs(t) == [10d, 10d, 10d, 10d, 10d]

      when:
      model(t, 'metricNameSlicer').setSelectedObject(20d)
      click(t, 'Submit1')

      then:
      embeddedInputs(t) == [20d, 20d, 20d, 20d, 20d]
   }

   def 'SetInputSelectedObject_refresh_trigger_applies_on_the_refresh'() {
      // VS_setInputSelectedObject_refresh: the slicer is a spinner, onLoad applies it on every refresh (input and refresh)
      given:
      def t = new ScriptStateBase(id('Viewsheet/VS_setInputSelectedObject_refresh'))

      when:
      input(t, 'metricNameSlicer', 10d)

      then:
      embeddedInputs(t) == [10d, 10d, 10d, 10d, 10d]
      passedParameter(t) == 10d

      when:
      input(t, 'metricNameSlicer', 20d)

      then:
      embeddedInputs(t) == [20d, 20d, 20d, 20d, 20d]
   }

   // ---- Hyperlinks and data conditions, read from the commands the browser would receive ----

   /** the hyperlinks of the cells of the n-th table data command of a refresh, as 'row,col' -> list of 'link | frame | type | params' */
   static Map<String, List<String>> cellLinks(RecordingDispatcher.Recorder rec, int n) {
      def load = rec.commands.findAll { it.class.simpleName.contains('LoadTableData') }[n]
      def out = [:]
      load.tableCells().eachWithIndex { row, r ->
         row.eachWithIndex { cell, k ->
            if(cell.hyperlinks) {
               out["${r},${k}".toString()] = cell.hyperlinks.collect {
                  it.link.replace('^host-org', '') + ' | ' + it.targetFrame + ' | ' + it.linkType + ' | ' +
                     it.parameterValues.collect { p -> p.name + '=' + p.value }
               }
            }
         }
      }
      out
   }

   static final String LINKED_VS = '1^128^__NULL__^Manual/LinkedAsseted/linked_vs'

   def 'TableAction3_hyperlinks_of_table_crosstab_and_freehand'() {
      // Table_Action3_hyperlink (the hyperlink case): the cells the manual case clicks. Link types: 1 = web link, 8 = viewsheet link
      given:
      def t = new ScriptStateBase(id('Table/Table_Action3_hyperlink'))
      def rec = t.refreshRecorded()
      def table = cellLinks(rec, 0)
      def crosstab = cellLinks(rec, 1)
      def freehand = cellLinks(rec, 2)

      expect: 'table: the empty cell goes to baidu, the year column opens linked_vs with the date; NY (1,1) and (1,2) have none'
      table['1,0'] == ['www.baidu.com |  | 1 | []']
      table['2,1'] == [LINKED_VS + ' | _blank | 8 | [prar1=1997-01-01 00:00:00.0]']
      table['15,1'] == [LINKED_VS + ' | _blank | 8 | [prar1=2000-01-01 00:00:00.0]']
      !table.containsKey('1,1')
      !table.containsKey('1,2')

      and: "crosstab: '1997 3rd' opens baidu with state=NJ in a new window, the '4' cell opens linked_vs"
      crosstab['1,0'] == ['www.baidu.com | _blank | 1 | [state=NJ]']
      crosstab['1,1'] == [LINKED_VS + ' |  | 8 | []']

      and: 'freehand: (1,1) goes to baidu, (1,2) opens linked_vs with state=NJ'
      freehand['1,1'] == ['www.baidu.com |  | 1 | []']
      freehand['1,2'] == [LINKED_VS + ' | _blank | 8 | [state=NJ]']
   }

   def 'GaugeActions_gauge_hyperlink_to_linked_vs'() {
      // Gauge_Actions: clicking the first gauge opens linked_vs with state=NJ (screenshot image52)
      given:
      def t = new ScriptStateBase(id('OutPut/Gauge_Actions'))
      def models = t.refreshRecorded().commands.findAll { it.class.simpleName == 'AddVSObjectCommand' }
         .collectEntries { [(it.model.absoluteName): it.model] }

      expect:
      models.Gauge1.hyperlinks.collect {
         it.link.replace('^host-org', '') + ' | ' + it.targetFrame + ' | ' + it.linkType + ' | ' + it.parameterValues.collect { p -> p.name + '=' + p.value }
      } == [LINKED_VS + ' | _blank | 8 | [state=NJ]']
      models.Gauge2.hyperlinks.size() == 0
   }

   def 'DataCondition_flyover_of_the_chart_sets_the_table_title'() {
      // TestCase_DataCondition: the point of the chart filters Query1 and the table script shows the condition in its title
      given:
      def t = new ScriptStateBase(id('Chart/TestCase_DataCondition'))
      t.paint('Chart1')

      expect:
      t.eval('Query1.title') == 'Filter: None'

      when:
      def rec = flyover(t, 'Chart1', 'Category^VALUE:Educational')

      then: 'the title is in the command the browser receives as well'
      t.eval('Query1.title') == 'Filter: Category = Educational'
      rec.commands.findAll { it.class.simpleName == 'RefreshVSObjectCommand' && it.info.absoluteName == 'Query1' }
         .every { it.info.title == 'Filter: Category = Educational' }

      when: 'two points are selected together'
      flyover(t, 'Chart1', 'Category^VALUE:Educational``Category^VALUE:Games')

      then:
      t.eval('Query1.title') == 'Filter: Category = Educational, Games'
   }

   def 'TableAction2_flyover_of_the_chart_sets_the_titles_of_table_and_crosstab'() {
      // Table_Action2: the point NJ filters the table and the crosstab; both show the condition in their titles.
      // (The freehand table is the tip view of the chart and gets its condition from the tip request, not from the flyover.)
      given:
      def t = new ScriptStateBase(id('Table/Table_Action2'))
      t.paint('Chart1')

      expect:
      t.eval('TableView1.title') == 'Table Filter: None'
      t.eval('Crosstab1.title') == 'Crosstab Filter: None'

      when:
      flyover(t, 'Chart1', 'state^VALUE:NJ')

      then:
      t.eval('TableView1.title') == 'Filter: state = NJ'
      t.eval('Crosstab1.title') == 'Filter: state = NJ'
   }

   def 'GaugeActions_flyover_filters_the_gauge_and_the_chart_title_shows_it'() {
      // Gauge_Actions: hovering a point filters Gauge2, the chart title shows Gauge2's condition (screenshot image43)
      given:
      def t = new ScriptStateBase(id('OutPut/Gauge_Actions'))
      t.paint('Chart1')

      expect:
      t.eval('Chart1.title') == 'Gauge Filter: None'

      when:
      flyover(t, 'Chart1', 'state^VALUE:NJ')

      then:
      t.vs.getAssembly('Gauge2').getTipConditionList().toString().contains('[Query2.state] [is] [equal to] [NJ]')
      t.eval('Chart1.title') == 'Filter: state = NJ'
   }
}
