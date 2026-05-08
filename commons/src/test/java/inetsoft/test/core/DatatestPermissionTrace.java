/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.sree.SreeEnv;
import inetsoft.sree.security.IdentityID;
import inetsoft.sree.security.Organization;
import inetsoft.sree.security.OrganizationManager;
import inetsoft.sree.security.Permission;
import inetsoft.sree.security.ResourceAction;
import inetsoft.sree.security.ResourceType;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.sree.security.SecurityException;
import inetsoft.sree.security.SecurityProvider;
import inetsoft.sree.security.SRPrincipal;
import inetsoft.util.ConfigurationContext;
import inetsoft.util.ThreadContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Opt-in stderr trace for permission checks (no behavior change unless force flag is set).
 * <ul>
 *    <li>{@code -Ddatatest.permission.trace=true} — dump principal, SreeEnv security keys,
 *        Permission rows, {@code checkPermission(READ)}.</li>
 *    <li>{@code -Ddatatest.force.security.enabled=true} — once per JVM, sets
 *        {@code SreeEnv.setProperty("security.enabled", "true")} before opening viewsheets
 *        (datatest-only; use to verify failures tied to {@code isSecurityEnabled()}).</li>
 * </ul>
 */
public final class DatatestPermissionTrace {

   private static final AtomicBoolean SECURITY_FORCE_APPLIED = new AtomicBoolean(false);

   private DatatestPermissionTrace() {
   }

   public static boolean isEnabled() {
      return Boolean.parseBoolean(System.getProperty("datatest.permission.trace", "false"));
   }

   public static boolean isForceSecurityEnabled() {
      return Boolean.parseBoolean(System.getProperty("datatest.force.security.enabled", "false"));
   }

   /**
    * If {@code -Ddatatest.force.security.enabled=true}, sets {@code security.enabled} in
    * {@link SreeEnv} once (first call wins). Invoked from {@link inetsoft.test.modules.VPMTest}
    * before opening a viewsheet so {@link SecurityEngine#isSecurityEnabled()} sees {@code true}.
    */
   public static void applyOptionalSecurityForce() {
      if(!isForceSecurityEnabled()) {
         return;
      }

      if(!SECURITY_FORCE_APPLIED.compareAndSet(false, true)) {
         return;
      }

      String previous = SreeEnv.getProperty("security.enabled");
      SreeEnv.setProperty("security.enabled", "true");
      System.err.println("[datatest-security-debug] FORCED SreeEnv security.enabled: was '" + previous
         + "' -> set to 'true' (-Ddatatest.force.security.enabled=true, once per JVM)");
   }

   /**
    * Logs principal, site-admin flag, stored {@link Permission} rows (READ role grants), and
    * {@link SecurityEngine#checkPermission} for REPORT/ASSET along the path derived from the asset id
    * (suffix after last {@code ^}, e.g. {@code VPM/VPM1_Model_1}).
    */
   public static void beforeOpenViewsheet(SRPrincipal user, String assetIdentifier) {
      if(!isEnabled()) {
         return;
      }

      String sheetPath = extractSheetPath(assetIdentifier);
      String orgId = Organization.getDefaultOrganizationID();
      SecurityEngine engine = SecurityEngine.getSecurity();
      SecurityProvider provider = engine.getSecurityProvider();

      errLine("======== datatest.permission.trace begin ========");
      errLine("sree.home(System.getProperty)=" + System.getProperty("sree.home"));
      errLine("sree.home(ConfigurationContext)="
         + ConfigurationContext.getContext().getHome());
      errLine("sree.home(SreeEnv.getEarlyLoadedProperty)=" + SreeEnv.getEarlyLoadedProperty("sree.home"));
      errLine("sree.home(SreeEnv)=" + SreeEnv.getProperty("sree.home"));
      errLine("sree.home(SreeEnv.getPropertyFromStorage)=" + safeGetPropertyFromStorage("sree.home"));
      errLine("(if SreeEnv still '.': defaults.properties parent; DatatestRuntimeBootstrap.alignSreeEnvAfterBootstrap should fix)");
      errLine("security.enabled SreeEnv.getEarlyLoadedProperty()="
         + SreeEnv.getEarlyLoadedProperty("security.enabled"));
      errLine("security.enabled SreeEnv.getProperty()=" + SreeEnv.getProperty("security.enabled"));
      errLine("security.enabled SreeEnv.getPropertyFromStorage()="
         + safeGetPropertyFromStorage("security.enabled"));
      errLine("datatest.force.security.enabled applied this JVM="
         + SECURITY_FORCE_APPLIED.get());
      errLine("assetIdentifier=" + assetIdentifier);
      errLine("sheetPath=" + sheetPath);
      errLine("defaultOrgId=" + orgId);
      errLine("SecurityEngine.isSecurityEnabled()=" + engine.isSecurityEnabled());
      errLine("principal.getName()=" + user.getName());
      errLine("principal.getOrgId()=" + user.getOrgId());
      errLine("principal.getClientUserID()=" + user.getClientUserID());
      errLine("principal roles=" + formatRoles(user.getRoles()));
      errLine("ThreadContext.getContextPrincipal()="
         + (ThreadContext.getContextPrincipal() == null ? "null" : ThreadContext.getContextPrincipal().getName()));
      errLine("OrganizationManager.isSiteAdmin(user)=" + OrganizationManager.getInstance().isSiteAdmin(user));

      if(provider == null) {
         errLine("SecurityProvider is null — skipping permission row / checkPermission dump");
         errLine("======== datatest.permission.trace end ========");
         return;
      }

      List<String> pathChain = buildPathChain(sheetPath);
      errLine("pathChain=" + pathChain);

      for(ResourceType resType : List.of(ResourceType.REPORT, ResourceType.ASSET)) {
         errLine("--- stored Permission rows (" + resType + ", READ role grants) ---");
         for(String path : pathChain) {
            Permission perm = provider.getPermission(resType, path, orgId);
            errLine("  path='" + path + "' perm=" + (perm == null ? "null"
               : "READ roles=" + perm.getOrgScopedRoleGrants(ResourceAction.READ, orgId)));
         }
      }

      errLine("--- SecurityEngine.checkPermission(READ) ---");
      for(ResourceType resType : List.of(ResourceType.REPORT, ResourceType.ASSET)) {
         for(String path : pathChain) {
            boolean allowed = safeCheckRead(engine, user, resType, path);
            errLine("  " + resType + " path='" + path + "' READ=" + allowed);
         }
      }

      String leaf = sheetPath == null || sheetPath.isEmpty() ? "/" : sheetPath;
      errLine("--- focus leaf (viewsheet asset path) ---");
      errLine("  REPORT leaf READ=" + safeCheckRead(engine, user, ResourceType.REPORT, leaf));
      errLine("  ASSET  leaf READ=" + safeCheckRead(engine, user, ResourceType.ASSET, leaf));

      // The actual failed permission check is on the worksheet (leaf name only, no folder prefix).
      // e.g. viewsheet "VPM/VPM1_Model_1" → worksheet leaf "VPM1_Model_1"
      String worksheetLeaf = (sheetPath != null && sheetPath.contains("/"))
         ? sheetPath.substring(sheetPath.lastIndexOf('/') + 1)
         : null;
      if(worksheetLeaf != null) {
         errLine("--- worksheet leaf path '" + worksheetLeaf + "' (name-only, no folder prefix) ---");
         for(ResourceType resType : List.of(ResourceType.REPORT, ResourceType.ASSET)) {
            Permission perm = provider.getPermission(resType, worksheetLeaf, orgId);
            errLine("  " + resType + " ws-leaf='" + worksheetLeaf + "' perm="
               + (perm == null ? "null"
               : "READ roles=" + perm.getOrgScopedRoleGrants(ResourceAction.READ, orgId)));
            errLine("  " + resType + " ws-leaf='" + worksheetLeaf + "' READ="
               + safeCheckRead(engine, user, resType, worksheetLeaf));
         }
      }

      errLine("======== datatest.permission.trace end ========");
   }

   private static String extractSheetPath(String assetIdentifier) {
      if(assetIdentifier == null) {
         return "";
      }

      int last = assetIdentifier.lastIndexOf('^');

      return last < 0 ? assetIdentifier : assetIdentifier.substring(last + 1);
   }

   private static List<String> buildPathChain(String sheetPath) {
      LinkedHashSet<String> ordered = new LinkedHashSet<>();
      ordered.add("/");

      if(sheetPath == null || sheetPath.isEmpty()) {
         return new ArrayList<>(ordered);
      }

      String normalized = sheetPath.startsWith("/") ? sheetPath.substring(1) : sheetPath;
      String[] parts = normalized.split("/");
      StringBuilder acc = new StringBuilder();

      for(String part : parts) {
         if(part == null || part.isEmpty()) {
            continue;
         }

         if(acc.length() > 0) {
            acc.append('/');
         }

         acc.append(part);
         ordered.add(acc.toString());
      }

      return new ArrayList<>(ordered);
   }

   private static String formatRoles(IdentityID[] roles) {
      if(roles == null || roles.length == 0) {
         return "[]";
      }

      return java.util.Arrays.stream(roles)
         .map(r -> r == null ? "null" : r.convertToKey())
         .collect(Collectors.joining(", ", "[", "]"));
   }

   private static boolean safeCheckRead(SecurityEngine engine, SRPrincipal user, ResourceType type,
                                        String path)
   {
      try {
         return engine.checkPermission(user, type, path, ResourceAction.READ);
      }
      catch(SecurityException | RuntimeException e) {
         errLine("  checkPermission threw: " + e.getClass().getName() + ": " + e.getMessage());
         return false;
      }
   }

   private static String safeGetPropertyFromStorage(String name) {
      try {
         return SreeEnv.getPropertyFromStorage(name);
      }
      catch(RuntimeException e) {
         return "(error: " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")";
      }
   }

   private static void errLine(String msg) {
      System.err.println("[datatest-perm-trace] " + msg);
   }
}
