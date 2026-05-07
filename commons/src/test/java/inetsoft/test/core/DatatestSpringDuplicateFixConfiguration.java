/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.report.internal.license.License;
import inetsoft.report.internal.license.LicenseManager;
import inetsoft.sree.internal.cluster.Cluster;
import inetsoft.sree.schedule.ScheduleClient;
import inetsoft.uql.asset.DependencyHandler;
import inetsoft.sree.schedule.ScheduleManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.uql.util.XSessionService;
import inetsoft.util.DataCacheSweeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Loaded together with {@link inetsoft.test.BaseTestConfiguration} and
 * {@link inetsoft.test.IntegrationTestConfiguration} in {@link DatatestRuntimeBootstrap}.
 * <p>
 * inetsoft-core defines overlapping beans (e.g. {@code DataCacheSweeper} from both configs;
 * {@code @ConditionalOnMissingBean} is not applied when using plain
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext} without Spring Boot).
 * Product code resolves singletons via {@link inetsoft.util.ConfigurationContext#getSpringBean(Class)},
 * which calls {@code getBean(type)} — multiple beans of the same type cause
 * {@link org.springframework.beans.factory.NoUniqueBeanDefinitionException}.
 * </p>
 * <p>
 * Marking one canonical bean {@code @Primary} fixes resolution without modifying inetsoft-core sources.
 * </p>
 */
@Configuration
public class DatatestSpringDuplicateFixConfiguration {

   /**
    * With {@code mock.license.manager=true}, {@link inetsoft.test.BaseTestConfiguration} supplies an
    * unstubbed mock; {@link inetsoft.util.ThreadPool} then gets null for
    * {@link LicenseManager#calculateThreadPoolSize(int, String, int)}.
    */
   @Bean
   @Primary
   public LicenseManager datatestPrimaryLicenseManager() {
      LicenseManager lm = mock(LicenseManager.class);
      License claimed = mock(License.class);
      lenient().when(claimed.standalone()).thenReturn(true);
      lenient().when(lm.calculateThreadPoolSize(anyInt(), nullable(String.class), anyInt()))
         .thenReturn(new int[] { 8, 16 });
      // FileAuthenticationProvider.addUser rejects when (existing FS users) >= namedUserCount +
      // getNamedUserViewerSessionCount(). Surefire sets INETSOFT_ADMIN_PASSWORD, so
      // SecurityEngine.migrateSiteAdminToChain always calls addUser; seeded security can
      // already have several users — small values (1–3) hit "exceed licensed users".
      lenient().when(lm.getNamedUserCount()).thenReturn(100);
      lenient().when(lm.getNamedUserViewerSessionCount()).thenReturn(0);
      lenient().when(lm.getLicenseHash()).thenReturn("datatest");
      lenient().when(lm.getClaimedLicenses()).thenReturn(Set.of(claimed));
      return lm;
   }

   /**
    * {@link ScheduleManager}'s constructor calls {@link SecurityEngine#getSecurityProvider()}. In this
    * bootstrap path {@link SecurityEngine#init()} may not have run yet relative to that lookup;
    * invoking {@link SecurityEngine#init()} before constructing {@link ScheduleManager} matches the
    * intended initialization order.
    */
   /**
    * Replaces {@code scheduleManager} from {@link inetsoft.test.IntegrationTestConfiguration}
    * (same bean name; {@link DatatestRuntimeBootstrap} enables definition overriding) so the faulty
    * bean is never instantiated.
    */
   @Bean(name = "scheduleManager")
   @Primary
   public ScheduleManager scheduleManager(SecurityEngine securityEngine, Cluster cluster) {
      securityEngine.init();
      return new ScheduleManager(securityEngine, cluster, mock(ScheduleClient.class),
         mock(DependencyHandler.class));
   }

   @Bean
   @Primary
   public DataCacheSweeper datatestPrimaryDataCacheSweeper() {
      return mock(DataCacheSweeper.class);
   }

   @Bean
   @Primary
   public XSessionService datatestPrimaryXSessionService() {
      return new XSessionService();
   }
}
