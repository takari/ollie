package com.walmartlabs.ollie.database;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.codahale.metrics.MetricRegistry;
import com.walmartlabs.ollie.OllieServer;
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
     * Convenience method to use default Liquibase names and custom DB
     */
    protected Configuration getConfiguration(
            String driver,
            String url,
            String username,
            String password,
            SQLDialect dialect,
            int maxPoolSize) {

        return getConfiguration(
                OllieServer.DATABASE_CHANGELOG_RESOURCE,
                OllieServer.DATABASE_CHANGELOG_TABLE_NAME,
                OllieServer.DATABASE_CHANGELOG_LOCK_TABLE_NAME,
                driver,
                url,
                username,
                password,
                dialect,
                maxPoolSize);
    }

    /**
     * set up custom Liquibase names and custom DB
     */
    protected Configuration getConfiguration(
            String logFile,
            String logTableName,
            String lockTableName,
            String driver,
            String url,
            String username,
            String password,
            SQLDialect dialect,
            int maxPoolSize) {

        if (this.cfg != null) {
            return this.cfg;
        }

        DatabaseConfiguration cfg = new DatabaseConfiguration(driver, url, username, password, dialect, maxPoolSize);

        DatabaseModule db = new DatabaseModule(logFile, logTableName, lockTableName);

        this.cfg = db.appJooqConfiguration(cfg, new MetricRegistry());
        return this.cfg;
    }
}
