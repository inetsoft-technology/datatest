package inetsoft.test.core;

import inetsoft.report.internal.license.*;
import inetsoft.sree.PropertiesEngine;
import inetsoft.sree.internal.cluster.Cluster;
import inetsoft.sree.internal.cluster.MockCluster;
import inetsoft.sree.portal.PortalThemesManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.storage.*;
import inetsoft.storage.fs.FilesystemBlobEngineFactory;
import inetsoft.test.IntegrationTestConfiguration;
import inetsoft.uql.asset.DependencyHandler;
import inetsoft.uql.jdbc.ConnectionPoolFactory;
import inetsoft.uql.jdbc.DefaultConnectionPoolFactory;
import inetsoft.util.*;
import inetsoft.util.config.*;
import inetsoft.util.log.LogManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Spring configuration for datatest integration tests.
 *
 * Uses the real mapdb storage backend (reads inetsoft.yaml as-is, no type override)
 * so test assets stored in the kv/ and blob/ directories are accessible.
 * Engine beans (ViewsheetService, WorksheetService, AnalyticRepository, etc.)
 * are provided by the imported IntegrationTestConfiguration.
 */
@Configuration
@Import(IntegrationTestConfiguration.class)
public class DataTestSpringConfig {

   @Bean
   public Cluster cluster() {
      return new MockCluster();
   }

   /**
    * Loads InetsoftConfig from inetsoft.yaml without modifying the keyValue.type.
    * This preserves the "mapdb" type so real test data in kv/ is accessible.
    * Uses ConfigurationContext.getHome() because different test modules use different
    * home system properties (sree.home vs ws.sree.home), but both set it on ConfigurationContext
    * before calling initSpringContext().
    */
   @Bean
   public InetsoftConfig inetsoftConfig() {
      String home = ConfigurationContext.getContext().getHome();
      Path configFile = Paths.get(home, "inetsoft.yaml");
      return InetsoftConfig.BOOTSTRAP_INSTANCE = InetsoftConfig.load(configFile);
   }

   @Bean
   public KeyValueEngine keyValueEngine(InetsoftConfig config) {
      String type = config.getKeyValue().getType();

      for(KeyValueEngineFactory factory : ServiceLoader.load(KeyValueEngineFactory.class)) {
         if(factory.getType().equals(type)) {
            return factory.createEngine(config);
         }
      }

      throw new RuntimeException("No KeyValueEngineFactory found for type: " + type);
   }

   @Bean
   public KeyValueStorageManager keyValueStorageManager(KeyValueEngine engine, Cluster cluster) {
      return new KeyValueStorageManager(engine, cluster);
   }

   @Bean
   public BlobEngine blobEngine(InetsoftConfig config) {
      return new FilesystemBlobEngineFactory().createEngine(config);
   }

   @Bean
   public BlobStorageManager blobStorageManager(BlobEngine blobEngine,
                                                KeyValueStorageManager keyValueStorageManager,
                                                Cluster cluster)
   {
      return new BlobStorageManager(blobEngine, keyValueStorageManager, null, cluster);
   }

   @Bean
   public DataSpace dataSpace(BlobStorageManager blobStorageManager) {
      return new DataSpace(blobStorageManager);
   }

   @Bean
   public LicenseManager licenseManager() {
      LicenseManager licenseManager = spy(new LicenseManager());
      License license = mock(License.class);
      doReturn(Set.of(license)).when(licenseManager).getClaimedLicenses();
      doReturn("test-license").when(licenseManager).getLicenseHash();
      return licenseManager;
   }

   @Bean
   public ElasticLicenseService elasticLicenseService() {
      return new NoopElasticLicenseService();
   }

   @Bean
   public SecurityEngine securityEngine(Cluster cluster, LicenseManager licenseManager) {
      return spy(new SecurityEngine(licenseManager, cluster));
   }

   @Bean
   public LogManager logManager() {
      return mock(LogManager.class);
   }

   @Bean
   public FileSystemService fileSystemService(Cluster cluster, ApplicationEventPublisher eventPublisher) {
      return new FileSystemService(cluster, eventPublisher);
   }

   @Bean
   public PropertiesEngine propertiesEngine(KeyValueStorageManager keyValueStorageManager,
                                            FileSystemService fileSystemService,
                                            ApplicationEventPublisher eventPublisher,
                                            ObjectProvider<LogManager> logManagerProvider)
   {
      return new PropertiesEngine(keyValueStorageManager, fileSystemService, eventPublisher,
                                  logManagerProvider);
   }

   @Bean
   public PortalThemesManager portalThemesManager() {
      return mock(PortalThemesManager.class);
   }

   @Bean
   public PasswordEncryption passwordEncryption() {
      return new LocalPasswordEncryptionFactory().createPasswordEncryption(new SecretsConfig());
   }

   @Bean
   public DependencyHandler dependencyHandler() {
      return mock(DependencyHandler.class);
   }

   @Bean
   public ConnectionPoolFactory connectionPoolFactory() {
      return new DefaultConnectionPoolFactory();
   }
}
