/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.test.IntegrationTestConfiguration;
import inetsoft.sree.PropertiesEngine;
import inetsoft.sree.SreeEnv;
import inetsoft.sree.security.AuthenticationProvider;
import inetsoft.sree.security.EditableAuthenticationProvider;
import inetsoft.sree.security.FSUser;
import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.Organization;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.util.ConfigurationContext;
import inetsoft.util.DataSpace;
import inetsoft.util.IndexedStorage;
import inetsoft.util.Plugins;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.lang.reflect.Field;
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

   private static final Object BOOTSTRAP_LOCK = new Object();

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

      synchronized(BOOTSTRAP_LOCK) {
         var existing = ctx.getApplicationContext();
         if(existing != null) {
            boolean active = !(existing instanceof ConfigurableApplicationContext) ||
                             ((ConfigurableApplicationContext) existing).isActive();
            if(active) {
               initializeStorageAccess(ctx);
               alignSreeEnvAfterBootstrap(ctx);
               initializeSecurity(ctx);
               initializePlugins(ctx);
               return;
            }
            // Context registered but not yet refreshed (e.g. @ContextConfiguration initializer
            // ran before refresh(), or another thread set it but hasn't called refresh() yet).
            // Fall through to create and refresh our own context.
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
         initializeStorageAccess(ctx);
         alignSreeEnvAfterBootstrap(ctx);
         initializeSecurity(ctx);
         initializePlugins(ctx);
      }
   }

   /**
    * Forces {@link PropertiesEngine#init(boolean)} to reload all properties from the KV backend
    * after the Spring context is fully up. This is needed because the runner plugin may write
    * {@code security.enabled} and other keys to the mapdb store during {@code generate-test-resources},
    * but the PropertiesEngine may have been initialised lazily before the KV storage was fully
    * accessible in the Spring bootstrap chain.
    * <p>
    * As a safety net, any JVM system property whose name matches a key absent from the KV storage
    * is propagated into SreeEnv so that Surefire {@code <systemPropertyVariables>} can serve as a
    * fallback (e.g. {@code <security.enabled>true</security.enabled>} in the module pom.xml).
    * </p>
    */
   private static void alignSreeEnvAfterBootstrap(ConfigurationContext ctx) {
      PropertiesEngine propertiesEngine = ctx.getSpringBean(PropertiesEngine.class);
      ensurePropertiesStorageInitialized(propertiesEngine);
      propertiesEngine.init(true);

      for(String key : new String[] {
         "security.enabled", "security.login.orgLocation", "data.home", "adm.home"
      }) {
         if(SreeEnv.getPropertyFromStorage(key) == null) {
            String sysProp = System.getProperty(key);

            if(sysProp != null) {
               SreeEnv.setProperty(key, sysProp);
               System.err.println("[datatest-bootstrap] '" + key
                  + "' absent from KV storage; applied from System property: " + sysProp);
            }
         }
      }
   }

   private static void initializeStorageAccess(ConfigurationContext ctx) {
      // Avoid Caffeine recursive compute when static accessors are called while another bean
      // such as MVManager is still being created.
      ctx.getSpringBean(DataSpace.class);
      ctx.getSpringBean(IndexedStorage.class);
   }

   private static void ensurePropertiesStorageInitialized(PropertiesEngine propertiesEngine) {
      try {
         Field kvStorage = PropertiesEngine.class.getDeclaredField("kvStorage");
         kvStorage.setAccessible(true);

         if(kvStorage.get(propertiesEngine) == null) {
            propertiesEngine.initEngine();
         }
      }
      catch(ReflectiveOperationException e) {
         throw new IllegalStateException("Failed to verify PropertiesEngine key-value storage", e);
      }
   }

   private static void initializeSecurity(ConfigurationContext ctx) {
      SecurityEngine securityEngine = ctx.getSpringBean(SecurityEngine.class);
      securityEngine.init();

      AuthenticationProvider authenticationProvider =
         securityEngine.getSecurityProvider().getAuthenticationProvider();
      IdentityID adminId = new IdentityID("admin", Organization.getDefaultOrganizationID());

      if(authenticationProvider.getUser(adminId) == null &&
         authenticationProvider instanceof EditableAuthenticationProvider editable)
      {
         FSUser admin = new FSUser(adminId);
         admin.setActive(true);
         admin.setEmails(new String[0]);
         admin.setGroups(new String[0]);
         admin.setLocale("");
         admin.setRoles(new IdentityID[] {
            new IdentityID("Administrator", Organization.getDefaultOrganizationID())
         });
         editable.addUser(admin);
      }
   }

   private static void initializePlugins(ConfigurationContext ctx) {
      ctx.getSpringBean(Plugins.class).initBean();
   }
}
