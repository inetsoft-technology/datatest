package inetsoft.test.cases.Others

import inetsoft.test.modules.ViewsheetTest
import spock.lang.Specification
import spock.lang.Ignore
import inetsoft.test.core.DatatestBaseConfiguration
import inetsoft.test.core.DatatestSpringDuplicateFixConfiguration
import inetsoft.test.IntegrationTestConfiguration
import inetsoft.test.ConfigurationContextInitializer
import inetsoft.test.SreeHome
import inetsoft.test.SreeProperty
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [DatatestBaseConfiguration, IntegrationTestConfiguration, DatatestSpringDuplicateFixConfiguration], initializers = [ConfigurationContextInitializer])
@SreeHome(
   security = true,
   properties = [
      @SreeProperty(name = 'security.enabled', value = 'true'),
      @SreeProperty(name = 'security.login.orgLocation', value = 'domain')
   ]
)
class ChartOthers_Spec extends Specification{
   static ViewsheetTest vstest
   static String caseName
   def setup() {
      ViewsheetTest.initHome(this.class.getName())
   }

   /*
    check Bug#48305:chart brush
    date:var day1 = Math.random()*10.There is no fixed data, so ignore
   */
   @Ignore
   def 'Bug#48305'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#48305', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'brush1', 'drillfilter1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#48779:axis format display error in runtime
   */
   def 'Bug#48779'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#48779', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#48800:date column adhoc filter can't work binding group table
   */
   def 'Bug#48800'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#48800', caseName)
      vstest.exportAsPNG(null, ['(Home)', 'adhocfilter1'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#49260:chart axis label is wrong when uses time type column
   */
   def 'Bug#49260'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#49260', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#48305:Chart banding y aggreagte calc field,ranking sort error when multiple topN at the same time
   */
   def 'Bug#49282'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#49282', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#49932:Map chart grouping points(customerBug)
   */
   def 'Bug#49932'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#49932', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Bug#50273:stack value show more data when set fill time series with 0
   */
   def 'Bug#50273'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/Bug#50273', caseName)
      vstest.exportAsPNG(null, ['(Home)'] as String[])
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)

   }



   /*
    check export chart to match layout when chart have the horizontal scrollbar
   */
   def 'chart_H1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_H', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, true, false)

      expect:
      vstest.compareImage(null)
   }

   /*
    check export chart to expand component when chart have the horizontal scrollbar
   */
   def 'chart_H2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_H', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, false, true)

      expect:
      vstest.compareImage(null)
   }

   /*
    check export chart to match layout when chart have the Vertical scrollbar
   */
   def 'chart_V1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_V', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, true, false)

      expect:
      vstest.compareImage(null)
   }

   /*
    check export chart to expand component when chart have the Vertical scrollbar
   */
   def 'chart_V2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_V', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, false, true)

      expect:
      vstest.compareImage(null)
   }

   /*
    check export chart to match layout when chart have the Vertical & horizontal scrollbar
   */
   def 'chart_H&V1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_H&V', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, true, false)

      expect:
      vstest.compareImage(null)
   }

   /*
    check export chart to expand component when chart have the Vertical & horizontal scrollbar
   */
   def 'chart_H&V2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/chart_H&V', caseName)
      vstest.exportAsPNG(['(Home)','bk1'] as String[], null, false, true)

      expect:
      vstest.compareImage(null)
   }

   /*
    check xscale when binding date
   */
   def 'xscale'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/xscale', caseName)
      vstest.exportAsPNG(null, null)
      vstest.exportAsPDF(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check filter and format
   */
   def 'formatfilter'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/formatfilter', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }


   /*
    check date manual sort
   */
   def 'datemanualsort'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/datemanualsort', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }


  /*
    check date manual sort when exist date part & date range & formula
   */
   def 'datemanualsort2'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/datemanualsort2', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }
   /*
    check time manual sort
   */
   def 'timemanualsort'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/timemanualsort', caseName)
      vstest.exportAsPNG(null, null)

      expect:
      vstest.compareImage(null)
   }

   /*
    check Chart1 Pareto: Customer Top 10 + Others by value desc, Running Total on Y2; last point = grand total, Others = total - top 10
    check Chart2 Category x Product Top 3 + Others (color), % of Category subtotal; each stacked bar sums to 100%
   */
   def 'rankingA1'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/rankingA1', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }

   /*
    check Chart1 Product Top 5 + Others by value desc, % of grand total; percents sum to 100%, Others last
    check Chart2 Customer Top 10 by Sum(Quantity) + Others, Y = % of total Net Amount, Y2 = Sum(Quantity)
    check Chart3 Product Top 5 + Others sorted by name, % of grand total; Others still last
   */
   def 'rankingA3'() {
      caseName = specificationContext.currentIteration.name
      vstest = new ViewsheetTest('1^128^__NULL__^Chart/Others/rankingA3', caseName)
      vstest.executeVS(null, null)

      expect:
      verifyAll {
         vstest.compareData(null)
         vstest.compareImage(null)
      }
   }
}
