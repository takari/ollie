package com.walmartlabs.ollie.database;

import com.codahale.metrics.MetricRegistry;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public abstract class AbstractDaoTest {

    private Configuration cfg;


    protected void tx(AbstractDao.Tx t) {
        try (DSLContext ctx = DSL.using(cfg)) {
            ctx.transaction(cfg -> {
                DSLContext tx = DSL.using(cfg);
                t.run(tx);
            });
        }
    }

    /**
     * Convenience method to use default Liquibase names and h2 memory DB
     */
    protected Configuration getConfiguration() {
        if (this.cfg != null) {
            return this.cfg;
        }
        return getConfiguration("liquibase.xml", "DATABASE_CHANGE_LOG", "DATABASE_CHANGE_LOG_LOCK");
    }

    /**
     * Convenience method to use custom Liquibase names and h2 memory DB
     */
    protected Configuration getConfiguration(String logFile, String logTableName, String lockTableName) {
        return getConfiguration(logFile, logTableName, lockTableName,
                "org.h2.Driver", "jdbc:h2:mem:test",
                "sa", "", SQLDialect.H2, 3);
    }

    /**
     * Convenience method to use default Liquibase names and custom DB
     */
    protected Configuration getConfiguration(String driver, String url, String username,
                                             String password, SQLDialect dialect, int maxPoolSize) {
        return getConfiguration("liquibase.xml", "DATABASE_CHANGE_LOG", "DATABASE_CHANGE_LOG_LOCK",
                driver, url, username, password, dialect, maxPoolSize);
    }

    /**
     * set up custom Liquibase names and custom DB
     */
    protected Configuration getConfiguration(String logFile, String logTableName, String lockTableName,
                                             String driver, String url, String username,
                                             String password, SQLDialect dialect, int maxPoolSize) {
        if (this.cfg != null) {
            return this.cfg;
        }

        return createConfiguration(logFile, logTableName, lockTableName, driver, url, username, password, dialect, maxPoolSize);
    }

    private Configuration createConfiguration(String logFile, String logTableName, String lockTableName,
                                              String driver, String url, String username,
                                            String password, SQLDialect dialect, int maxPoolSize) {
        DatabaseConfiguration cfg = new DatabaseConfiguration(driver, url, username, password, dialect, maxPoolSize);

        DatabaseModule db = new DatabaseModule(logFile, logTableName, lockTableName);

        this.cfg = db.appJooqConfiguration(cfg, new MetricRegistry());
        return this.cfg;
    }
}
