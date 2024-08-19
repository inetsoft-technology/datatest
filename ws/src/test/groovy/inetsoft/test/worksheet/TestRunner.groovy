package inetsoft.test.worksheet

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

class TestRunner {
   public static void main(String[] args) {
      if(args.length == 0) {
         args = ['inetsoft.test.worksheet.cases.TestSpec']
      }

      Launcher launcher = LauncherFactory.create()

      String sreeHome = System.getProperty("sree.home");
      if (sreeHome == null) {
         System.out.println("System property 'sree.home' is not set.");
      } else {
         System.out.println("System property 'sree.home': " + sreeHome);
      }

      System.setProperty("sree.home", sreeHome);

      for (String testClassName : args) {
         try {
            Class<?> testClass = Class.forName(testClassName);
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .build()

            launcher.execute(request, new TestExecutionListener() {
               @Override
               public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                  System.out.println("Test result for " + testClassName + ": " + testExecutionResult)
                  if(testExecutionResult.status == 'FAILED') {
                     println "----------" + testExecutionResult.throwable.toString()
                  }
               }
            });
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
}
