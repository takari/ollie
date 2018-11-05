package com.walmartlabs.ollie.database;

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
