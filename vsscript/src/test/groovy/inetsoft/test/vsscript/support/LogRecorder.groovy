package inetsoft.test.vsscript.support

import org.slf4j.ILoggerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter
import org.slf4j.event.Level

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Records the WARN and ERROR log events of the product during a case.
 *
 * Why: the vsscript test classpath has slf4j-api 1.7.x and no binding, so every log line of the product is dropped
 * (NOPLoggerFactory), and the refresh path swallows script errors with a log line only (VSAScriptable.putMember, ...).
 * org.slf4j.impl.StaticLoggerBinder (src/test/groovy/org/slf4j/impl) binds this factory, so nothing is added to the
 * dependencies: logback-classic would need slf4j-api 2.x, which breaks inetsoft-runner-maven-plugin:configure on a clean
 * build (it loads the project classpath next to Maven's own slf4j 1.7).
 *
 * If a future classpath carries slf4j-api 2.x (a ServiceLoader provider), the binder is ignored; bound() is then false
 * and ScriptStateRefreshTripwire_Spec fails with that reason instead of passing blind.
 */
class LogRecorder {
   private static final int MAX_EVENTS = 20000
   private static final List<Map<String, String>> EVENTS = Collections.synchronizedList([])

   static final ILoggerFactory FACTORY = new ILoggerFactory() {
      @Override
      Logger getLogger(String name) { newLogger(name) }
   }

   /** true when slf4j routes the log calls of the product to this recorder */
   static boolean bound() { LoggerFactory.getILoggerFactory().is(FACTORY) }

   static void clear() { EVENTS.clear() }

   /** the events since the last clear(): [level: 'WARN' | 'ERROR', logger: full name, message: formatted text] */
   static List<Map<String, String>> events() {
      synchronized(EVENTS) { new ArrayList<>(EVENTS) }
   }

   private static Logger newLogger(String name) {
      Proxy.newProxyInstance(Logger.classLoader, [Logger] as Class[], new InvocationHandler() {
         @Override
         Object invoke(Object proxy, Method method, Object[] args) {
            String m = method.name

            switch(m) {
            case 'getName':
               return name
            case 'isWarnEnabled':
            case 'isErrorEnabled':
               return true
            case 'isTraceEnabled':
            case 'isDebugEnabled':
            case 'isInfoEnabled':
               return false
            case 'warn':
            case 'error':
               record(m == 'warn' ? 'WARN' : 'ERROR', name, args)
               return null
            case 'hashCode':
               return System.identityHashCode(proxy)
            case 'equals':
               return proxy.is(args[0])
            case 'toString':
               return "LogRecorder logger ${name}".toString()
            default:
               return null   // trace, debug, info and the other calls
            }
         }
      })
   }

   private static void record(String level, String logger, Object[] args) {
      List<Object> list = args == null ? [] : args.toList()

      // a Marker may come first
      if(list && list[0] instanceof org.slf4j.Marker) {
         list = list.drop(1)
      }

      String format = list ? String.valueOf(list[0]) : ''
      Object[] rest = list.size() > 1 ? list.drop(1) as Object[] : new Object[0]

      // the varargs form passes the arguments as one Object[]
      if(rest.length == 1 && rest[0] instanceof Object[]) {
         rest = (Object[]) rest[0]
      }

      String message = MessageFormatter.arrayFormat(format, rest).getMessage()
      // the list is only cleared by the tripwire; a whole module run must not keep every event
      if(EVENTS.size() < MAX_EVENTS) {
         EVENTS << [level: level, logger: logger, message: message]
      }
   }
}
