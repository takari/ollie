package com.walmartlabs.ollie.db;

import com.codahale.metrics.MetricRegistry;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import java.lang.reflect.Method;

public abstract class AbstractDaoTest {

    private DataSource dataSource;
    private Configuration cfg;

    @Before
    public void initDataSource() {
        DatabaseConfiguration cfg = new DatabaseConfiguration("org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/postgres",
                "postgres", "q1", 3);

        DatabaseModule db = new DatabaseModule();
        this.dataSource = db.appDataSource(cfg, new MetricRegistry());

        this.cfg = db.appJooqConfiguration(this.dataSource);
    }

    @After
    public void closeDataSource() throws Exception {
        Method m = dataSource.getClass().getMethod("close");
        m.invoke(dataSource);
    }

    protected void tx(AbstractDao.Tx t) {
        try (DSLContext ctx = DSL.using(cfg)) {
            ctx.transaction(cfg -> {
                DSLContext tx = DSL.using(cfg);
                t.run(tx);
            });
        }
    }

    protected Configuration getConfiguration() {
        return cfg;
    }
}
