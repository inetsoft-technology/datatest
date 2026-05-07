/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.test.IntegrationTestConfiguration;
import inetsoft.util.ConfigurationContext;
import inetsoft.util.DataSpace;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Single entry for datatest runtime prerequisites before any code runs that calls
 * {@link ConfigurationContext#getSpringBean(Class)} (e.g. {@code ViewsheetService.getInstance()},
 * {@code DataSpace.getDataSpace()}, {@code SecurityEngine.getSecurity()}).
 * <p>
 * Performs in order:
 * <ol>
 *    <li><b>Home</b> — {@link ConfigurationContext#setHome(String)}</li>
 *    <li><b>Spring</b> — {@link AnnotationConfigApplicationContext} with the same slice as JUnit
 *        integration tests: {@link DatatestBaseConfiguration} + {@link IntegrationTestConfiguration}
 *        + {@link DatatestSpringDuplicateFixConfiguration} (datatest-only {@code @Primary} beans when
 *        core test configs both register the same types)</li>
 *    <li><b>Bind</b> — {@link ConfigurationContext#setApplicationContext} <em>before</em>
 *        {@code refresh()}, matching {@link inetsoft.test.ConfigurationContextInitializer}</li>
 * </ol>
 * Call once from each spec's {@code setupSpec} / {@code initHome} (idempotent if already started).
 * </p>
 * <p>
 * Typical Spock usage:
 * {@code DatatestRuntimeBootstrap.bootstrap(System.getProperty("sree.home", "."))} for viewsheet specs;
 * worksheet specs often use {@code ws.sree.home} instead.
 * </p>
 */
public final class DatatestRuntimeBootstrap {

   private DatatestRuntimeBootstrap() {
   }

   /**
    * Uses {@code System.getProperty("sree.home", ".")}. Convenience for viewsheet-oriented specs.
    */
   public static void bootstrap() {
      bootstrap(System.getProperty("sree.home", "."));
   }

   /**
    * Sets configuration home, then ensures Spring is bound (same as core {@code inetsoft.test} JUnit tests).
    *
    * @param home directory for {@link ConfigurationContext#setHome}; empty or null becomes {@code "."}
    */
   public static void bootstrap(String home) {
      ConfigurationContext ctx = ConfigurationContext.getContext();
      String resolved = (home == null || home.isEmpty()) ? "." : home;
      ctx.setHome(resolved);

      if(ctx.getApplicationContext() != null) {
         return;
      }

      StandardEnvironment env = new StandardEnvironment();
      Map<String, Object> props = new HashMap<>(4);
      props.put("mock.license.manager", "true");
      props.put("sree.home", resolved);
      env.getPropertySources().addFirst(new MapPropertySource("datatest", props));

      AnnotationConfigApplicationContext app = new AnnotationConfigApplicationContext();
      app.setAllowBeanDefinitionOverriding(true);
      app.setEnvironment(env);
      app.register(DatatestBaseConfiguration.class, IntegrationTestConfiguration.class,
         DatatestSpringDuplicateFixConfiguration.class);
      ctx.setApplicationContext(app);
      app.refresh();
      // Avoid Caffeine recursive compute when MVManager construction calls DataSpace.getDataSpace().
      ctx.getSpringBean(DataSpace.class);
   }
}
