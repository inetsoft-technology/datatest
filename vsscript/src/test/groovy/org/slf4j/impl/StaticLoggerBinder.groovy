package org.slf4j.impl

import inetsoft.test.vsscript.support.LogRecorder
import org.slf4j.ILoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

/**
 * The SLF4J 1.7 binding of the vsscript test JVM: routes the log calls of the product to LogRecorder, which keeps the
 * WARN / ERROR events for ScriptStateRefreshTripwire_Spec. See LogRecorder for the reason. Has no effect with slf4j 2.x.
 */
class StaticLoggerBinder implements LoggerFactoryBinder {
   /** must stay a non-final String field: slf4j 1.7 reads it reflectively */
   public static String REQUESTED_API_VERSION = '1.7.36'

   private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder()

   static StaticLoggerBinder getSingleton() { SINGLETON }

   @Override
   ILoggerFactory getLoggerFactory() { LogRecorder.FACTORY }

   @Override
   String getLoggerFactoryClassStr() { LogRecorder.FACTORY.class.name }
}
