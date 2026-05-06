/*
 * Copyright (c) 2026, InetSoft Technology Corp, All Rights Reserved.
 */
package inetsoft.test.core;

import inetsoft.report.internal.license.LicenseManager;
import inetsoft.sree.PropertiesEngine;
import inetsoft.sree.internal.cluster.Cluster;
import inetsoft.sree.portal.PortalThemesManager;
import inetsoft.sree.security.SecurityEngine;
import inetsoft.storage.BlobCache;
import inetsoft.storage.BlobEngine;
import inetsoft.storage.BlobStorageManager;
import inetsoft.storage.KeyValueEngine;
import inetsoft.storage.KeyValueEngineFactory;
import inetsoft.storage.KeyValueStorageManager;
import inetsoft.test.BaseTestConfiguration;
import inetsoft.uql.asset.DependencyHandler;
import inetsoft.uql.util.XSessionService;
import inetsoft.util.DataCacheSweeper;
import inetsoft.util.DataSpace;
import inetsoft.util.FileSystemService;
import inetsoft.util.PasswordEncryption;
import inetsoft.util.config.InetsoftConfig;
import inetsoft.util.config.KeyValueConfig;
import inetsoft.util.config.MapDBConfig;
import inetsoft.util.log.LogManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ServiceLoader;

/**
 * Datatest storage fixtures are restored by the runner plugin into the configured
 * key-value backend. The core test base swaps that backend to an empty in-memory
 * test engine, which makes restored asset indexes invisible to repository tests.
 */
@Configuration
public class DatatestBaseConfiguration {
   private final BaseTestConfiguration delegate = new BaseTestConfiguration();

   @Bean
   public Cluster cluster() {
      return delegate.cluster();
   }

   @Bean
   public InetsoftConfig inetsoftConfig(Environment environment) {
      String home = environment.getProperty("sree.home", System.getProperty("sree.home", "."));
      Path configFile = Path.of(home, "inetsoft.yaml");
      InetsoftConfig config = InetsoftConfig.load(configFile);
      restoreMapDBKeyValueConfig(home, config);
      InetsoftConfig.BOOTSTRAP_INSTANCE = config;
      return config;
   }

   @Bean
   public KeyValueEngine keyValueEngine() {
      InetsoftConfig config = InetsoftConfig.getInstance();
      String type = config.getKeyValue().getType();

      return ServiceLoader.load(KeyValueEngineFactory.class).stream()
         .map(ServiceLoader.Provider::get)
         .filter(factory -> type.equals(factory.getType()))
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("No key-value engine factory found for type: " + type))
         .createEngine(config);
   }

   @Bean
   public KeyValueStorageManager keyValueStorageManager(KeyValueEngine engine, Cluster cluster) {
      return delegate.keyValueStorageManager(engine, cluster);
   }

   @Bean
   public BlobEngine blobEngine(InetsoftConfig config) {
      return delegate.blobEngine(config);
   }

   @Bean
   public BlobCache blobCache(BlobEngine blobEngine) {
      return delegate.blobCache(blobEngine);
   }

   @Bean
   public BlobStorageManager blobStorageManager(
      BlobEngine blobEngine, KeyValueStorageManager keyValueStorageManager,
      BlobCache blobCache, Cluster cluster)
   {
      return delegate.blobStorageManager(blobEngine, keyValueStorageManager, blobCache, cluster);
   }

   @Bean
   public LicenseManager licenseManager(Environment environment) {
      return delegate.licenseManager(environment);
   }

   @Bean
   public SecurityEngine securityEngine(Cluster cluster, LicenseManager licenseManager) {
      return delegate.securityEngine(cluster, licenseManager);
   }

   @Bean
   public LogManager logManager() {
      return delegate.logManager();
   }

   @Bean
   public PropertiesEngine propertiesEngine(
      KeyValueStorageManager keyValueStorageManager, FileSystemService fileSystemService,
      ApplicationEventPublisher eventPublisher, ObjectProvider<LogManager> logManagerProvider)
   {
      return delegate.propertiesEngine(keyValueStorageManager, fileSystemService, eventPublisher,
         logManagerProvider);
   }

   @Bean
   public FileSystemService fileSystemService(Cluster cluster, ApplicationEventPublisher eventPublisher) {
      return delegate.fileSystemService(cluster, eventPublisher);
   }

   @Bean
   public DataSpace dataSpace(BlobStorageManager blobStorageManager) {
      return delegate.dataSpace(blobStorageManager);
   }

   @Bean
   public PortalThemesManager portalThemesManager() {
      return delegate.portalThemesManager();
   }

   @Bean
   public XSessionService xSessionService() {
      return delegate.xSessionService();
   }

   @Bean
   public DataCacheSweeper dataCacheSweeper() {
      return delegate.dataCacheSweeper();
   }

   @Bean
   public PasswordEncryption passwordEncryption() {
      return delegate.passwordEncryption();
   }

   @Bean
   public DependencyHandler dependencyHandler() {
      return delegate.dependencyHandler();
   }

   private static void restoreMapDBKeyValueConfig(String home, InetsoftConfig config) {
      Path keyValueDirectory = Path.of(home, "kv");

      if(config.getKeyValue() == null || !"test".equals(config.getKeyValue().getType()) ||
         !Files.isDirectory(keyValueDirectory))
      {
         return;
      }

      MapDBConfig mapDB = new MapDBConfig();
      mapDB.setDirectory(keyValueDirectory.toString());

      KeyValueConfig keyValue = new KeyValueConfig();
      keyValue.setType("mapdb");
      keyValue.setMapdb(mapDB);
      config.setKeyValue(keyValue);
   }
}
