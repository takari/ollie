package com.walmartlabs.ollie.db;

import com.walmartlabs.ollie.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

@Named
@Singleton
public class DatabaseConfigurationProvider implements Provider<DatabaseConfiguration> {

    @Inject
    @Config("db.url")
    private String url;

    @Inject
    @Config("db.driver")
    private String driver;

    @Inject
    @Config("db.appUsername")
    private String appUsername;

    @Inject
    @Config("db.appPassword")
    private String appPassword;

    @Inject
    @Config("db.maxPoolSize")
    private int maxPoolSize;

    @Override
    public DatabaseConfiguration get() {
        return new DatabaseConfiguration(driver, url,
                appUsername, appPassword, maxPoolSize);
    }
}
