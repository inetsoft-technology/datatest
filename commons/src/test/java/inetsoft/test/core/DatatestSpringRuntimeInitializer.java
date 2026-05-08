/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

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
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Datatest-specific runtime initialization that must run after the Spring context
 * exists, regardless of whether the context was created by Spock/Spring or by the
 * legacy {@link DatatestRuntimeBootstrap}.
 */
public final class DatatestSpringRuntimeInitializer {
   private DatatestSpringRuntimeInitializer() {
   }

   public static void initialize(ConfigurationContext ctx) {
      initialize(ctx, ctx.getApplicationContext());
   }

   public static void initialize(ConfigurationContext ctx, ApplicationContext applicationContext) {
      synchronized(INITIALIZED_CONTEXTS) {
         if(applicationContext != null && INITIALIZED_CONTEXTS.contains(applicationContext)) {
            return;
         }

         initializeStorageAccess(ctx);
         alignSreeEnv(ctx);
         initializeSecurity(ctx);
         initializePlugins(ctx);

         if(applicationContext != null) {
            INITIALIZED_CONTEXTS.add(applicationContext);
         }
      }
   }

   private static void alignSreeEnv(ConfigurationContext ctx) {
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
               System.err.println("[datatest-spring] '" + key
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

   private static final Set<ApplicationContext> INITIALIZED_CONTEXTS =
      Collections.newSetFromMap(new WeakHashMap<>());
}
