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

    protected Configuration getConfiguration() {
        if (this.cfg != null) {
            return this.cfg;
        }

        return createConfiguration("org.h2.Driver", "jdbc:h2:mem:test",
                "sa", "", SQLDialect.H2, 3);
    }

    protected Configuration getConfiguration(String driver, String url, String username,
                                             String password, SQLDialect dialect, int maxPoolSize) {
        if (this.cfg != null) {
            return this.cfg;
        }



        return createConfiguration(driver, url, username, password, dialect, maxPoolSize);
    }

    private Configuration createConfiguration(String driver, String url, String username,
                                            String password, SQLDialect dialect, int maxPoolSize) {
        DatabaseConfiguration cfg = new DatabaseConfiguration(driver, url, username, password, dialect, maxPoolSize);

        DatabaseModule db = new DatabaseModule();

        this.cfg = db.appJooqConfiguration(cfg, new MetricRegistry());
        return this.cfg;
    }
}
