package inetsoft.test.vsscript.cases

import groovy.json.JsonOutput
import inetsoft.test.modules.VSScriptTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Specification

class ScriptEngine_Spec extends Specification{
   def setupSpec() {
      VSScriptTest.initHome(this.class.getName())
   }

   /**
    * test execute order on diff scope: UI - >  onInit -> onReresh-> onAssembly
    */
   def 'TestCase-Script_Order1' () {
      given:
      caseName = specificationContext.currentIteration.name
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order1', caseName)
      vsScriptTest.printVS(null, null)
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order1_VS'] as String[])
   }

   /**
    * test execute order: parent-child
    */
   def 'TestCase-Script_Order2' () {
      given:
      caseName = specificationContext.currentIteration.name
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order2', caseName)
      vsScriptTest.printVS(['(Home)', 'False'] as String[], null)
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order2_VS'] as String[])
   }

   /**
    * test execute order: parent-child
    */
   def 'TestCase-Script_Order3' () {
      given:
      caseName = specificationContext.currentIteration.name
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order3', caseName)
      vsScriptTest.printVS(null, ['GroupContainer1', 'Chart1', 'Gauge2'] as String[])
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order3_VS'] as String[])
   }

   /**
    * test assembly dependency: action trigger script
    */
   def 'TestCase-Script_Order_OneWay3' () {
      given:
      caseName = specificationContext.currentIteration.name
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order_OneWay3', caseName)
      vsScriptTest.printVS(['(Home)', 'valueIsTrue'] as String[], null)
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order_OneWay3_VS'] as String[])
   }

   /**
    * test assembly dependency: parameter trigger script(manual case covered)
    */
   @Ignore
   def 'TestCase-Script_Order_OneWay4' () {
      given:
      caseName = specificationContext.currentIteration.name
      def paras = new HashMap<String, String[]>()
      paras.put('state', ['NK', 'NY', 'NJ'] as String[])
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order_OneWay4', caseName)
      vsScriptTest.printVS(paras, null, null, null, null)
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order_OneWay4_VS'] as String[])
   }

   /**
    * test assembly dependency: passOn
    */
   def 'TestCase-Script_Order_passOn1' () {
      given:
      caseName = specificationContext.currentIteration.name
      when:
      vsScriptTest = new VSScriptTest('1^128^__NULL__^ScriptAuto/ScriptEngine/Script_Order_passOn1', caseName)
      vsScriptTest.printVS(null, null)
      then:
      vsScriptTest.compareImage(['TestCase-Script_Order_passOn1_VS'] as String[])
   }

   String caseName
   static VSScriptTest vsScriptTest
}
