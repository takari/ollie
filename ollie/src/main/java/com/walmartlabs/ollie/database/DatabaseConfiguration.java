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

import org.jooq.SQLDialect;

import java.io.Serializable;

public class DatabaseConfiguration implements Serializable {

    private final String driverClassName;
    private final String url;
    private final String appUsername;
    private final String appPassword;
    private final SQLDialect dialect;
    private final int maxPoolSize;

    public DatabaseConfiguration(String driverClassName,
                                 String url,
                                 String appUsername,
                                 String appPassword,
                                 SQLDialect dialect,
                                 int maxPoolSize) {

        this.driverClassName = driverClassName;
        this.url = url;
        this.appUsername = appUsername;
        this.appPassword = appPassword;
        this.dialect = dialect;
        this.maxPoolSize = maxPoolSize;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public String getAppUsername() {
        return appUsername;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public SQLDialect getDialect() { return dialect; }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }
}
