package com.walmartlabs.ollie.database;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.config.ConfigurationProcessor;
import com.walmartlabs.ollie.config.DatabaseConfigurationProvider;
import com.walmartlabs.ollie.config.EnvironmentSelector;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;

public class DatabaseModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(DatabaseModule.class);

    private static final int MIGRATION_MAX_RETRIES = 10;
    private static final int MIGRATION_RETRY_DELAY = 10000;

    private String DB_CHANGELOG_PATH;
    private String DB_CHANGELOG_LOG_TABLE;
    private String DB_CHANGELOG_LOCK_TABLE;

    private static final String AUTO_MIGRATE_KEY = "ollie.db.autoMigrate";

    private final Config config;

    public DatabaseModule(String changeLogPath, String logTableName, String lockTableName) {
        config = null;
        DB_CHANGELOG_PATH = changeLogPath;
        DB_CHANGELOG_LOG_TABLE = logTableName;
        DB_CHANGELOG_LOCK_TABLE = lockTableName;
    }

    public DatabaseModule(Config config, String changeLogPath, String changeLogTableName, String changeLogLockTableName) {

        this.config = config;
        this.DB_CHANGELOG_PATH = changeLogPath;
        this.DB_CHANGELOG_LOG_TABLE = changeLogTableName;
        this.DB_CHANGELOG_LOCK_TABLE = changeLogLockTableName;
    }
    public DatabaseModule(OllieServerBuilder config) {
        this(new ConfigurationProcessor(config.name(), new EnvironmentSelector().select(), null, config.secrets()).process(),
                config.changeLogFile(), config.changeLogTableName(), config.changeLogLockTableName());
    }

    @Override
    protected void configure() {
    }


    @Provides
    @Named("app")
    @Singleton
    public Configuration appJooqConfiguration(DatabaseConfiguration cfg, MetricRegistry metricRegistry) {
        DataSource ds = createDataSource(cfg, "app", cfg.getAppUsername(), cfg.getAppPassword(), metricRegistry);

        String autoMigrate = System.getProperty(AUTO_MIGRATE_KEY);
        if (autoMigrate == null || !autoMigrate.equalsIgnoreCase( "false")) {
            int retries = MIGRATION_MAX_RETRIES;
            for (int i = 0; i < retries; i++) {
                try (Connection c = ds.getConnection()) {
                    log.info("get -> performing DB migration...");
                    migrateDb(c, DB_CHANGELOG_PATH, DB_CHANGELOG_LOG_TABLE, DB_CHANGELOG_LOCK_TABLE);
                    log.info("get -> done");
                    break;
                } catch (Exception e) {
                    if (i + 1 >= retries) {
                        log.error("get -> db migration error, giving up", e);
                        throw new RuntimeException(e);
                    }

                    log.warn("get -> db migration error, retrying in {}ms: {}", MIGRATION_RETRY_DELAY, e.getMessage());
                    try {
                        Thread.sleep(MIGRATION_RETRY_DELAY);
                    } catch (InterruptedException ee) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        return createJooqConfiguration(ds, cfg.getDialect());
    }

    @Provides
    @Singleton
    public DatabaseConfiguration getDatabaseConfiguration() {
        return new DatabaseConfigurationProvider(config).get();
    }


    private static DataSource createDataSource(DatabaseConfiguration cfg,
                                               String poolName,
                                               String username,
                                               String password,
                                               MetricRegistry metricRegistry) {

        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName(poolName);
        ds.setJdbcUrl(cfg.getUrl());
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setAutoCommit(false);
        ds.setMaxLifetime(Long.MAX_VALUE);
        ds.setMinimumIdle(1);
        ds.setMaximumPoolSize(cfg.getMaxPoolSize());
        ds.setLeakDetectionThreshold(10000);
        ds.setMetricRegistry(metricRegistry);
        return ds;
    }

    private static void migrateDb(Connection conn, String logPath, String logTable, String lockTable) throws Exception {
        LogFactory.getInstance().setDefaultLoggingLevel(LogLevel.WARNING);

        Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
        db.setDatabaseChangeLogTableName(logTable);
        db.setDatabaseChangeLogLockTableName(lockTable);

        Liquibase lb = new Liquibase(logPath, new ClassLoaderResourceAccessor(), db);
        lb.update((String) null);
    }

    private static Configuration createJooqConfiguration(DataSource ds, SQLDialect dialect) {
        Settings settings = new Settings();
        settings.setRenderSchema(false);
        settings.setRenderCatalog(false);
        settings.setRenderNameStyle(RenderNameStyle.AS_IS);
        return new DefaultConfiguration()
                .set(settings)
                .set(ds)
                .set(dialect);
    }
}
