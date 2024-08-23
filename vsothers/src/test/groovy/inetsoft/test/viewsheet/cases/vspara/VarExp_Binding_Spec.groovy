package inetsoft.test.viewsheet.cases.vspara

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification

class VarExp_Binding_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName

   def setupSpec() {
      ViewsheetTest.initHome(this.class.getName())
   }
   /**
    * check crosstab use CB and RC to binding row and col field.
    */
   def 'Crosstab_Binding1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_Binding1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check crosstab group options with dynimac value
    */
   def 'Crosstab_BindGOptions'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindGOptions', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check crosstab group options with dynimac value
    */
   def 'Crosstab_BindAOptions'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindAOptions', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Crosstab DC dynimac binding
    */
   def 'Crosstab_BindDC1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindDC1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Crosstab DC dynimac binding
    */
   def 'Crosstab_BindDC2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindDC2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Crosstab DC dynimac binding: custom range
    */
   def 'Crosstab_BindDC3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindDC3', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check Crosstab binding use variable, trend and comparision apply
    */
   def 'Crosstab_BindTrend1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Crosstab_BindTrend1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check X, Y use Dynimac binding, group and aggregate use expression
    */
   def 'Chart_BindingXY'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingXY', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check BreakBY and Path use Expression, group and aggregate use expression
    */
   def 'Chart_BindingXYPath'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingXYPath', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check map chart G field, lat|long use dynimac binding
    */
   def 'Chart_BindingGField'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingGField', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check map chart G field use SelectionTree.drillMember to change binding
    */
   def 'Chart_BindingGField2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingGField2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * use pie,radar to check visual field use expressions.
    */
   def 'Chart_BindingVisual'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingVisual', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check High,Low,Open,Close field use Dynimac binding
    */
   def 'Chart_BindingStock'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingStock', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check T Field use Dynimac binding
    */
   def 'Chart_BindingTField'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingTField', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check start,end,milestone field on gannt chart
    */
   def 'Chart_BindingGantt'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingGantt', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check source,target field on tree,network chart
    */
   def 'Chart_BindingTree'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingTree', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /**
    * check stand DC use dynimac
    */
   def 'Chart_BindingDC1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingDC1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check custom DC use dynimac
    */
   def 'Chart_BindingDC2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingDC2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check chart special check
    */
   def 'Chart_BindingSpecial1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingSpecial1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check chart special check
    */
   def 'Chart_BindingSpecial2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindingSpecial2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check chart use variable, trend and comparision apply
    */
   def 'Chart_BindTrend1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindTrend1', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }

   /**
    * check chart use variable, trend and comparision apply, if break by use invalid col, will reset to default
    */
   def 'Chart_BindTrend2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^VarExp/Binding/Chart_BindTrend2', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'bk1'] as String[])

      expect:
      vstest.compareImage(null)
   }
}
