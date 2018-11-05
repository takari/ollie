package com.walmartlabs.ollie.config;

import com.typesafe.config.*;
import com.typesafe.config.Config;
import com.walmartlabs.ollie.database.DatabaseConfiguration;
import org.jooq.SQLDialect;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
@Singleton
public class DatabaseConfigurationProvider  {

    private final Config config;

    public DatabaseConfigurationProvider(Config config) {
        this.config = config;
    }

    private String url;
    private String driver;
    private String appUsername;
    private String appPassword;
    private String dialect;
    private int maxPoolSize;

    public DatabaseConfiguration get() {
        url = (String) getConfigValue(String.class, String.class.getComponentType(), "db.url", false);
        driver = (String) getConfigValue(String.class, String.class.getComponentType(), "db.driver", false);
        appUsername = (String) getConfigValue(String.class, String.class.getComponentType(), "db.appUsername", false);
        appPassword = (String) getConfigValue(String.class, String.class.getComponentType(), "db.appPassword", false);
        dialect = (String) getConfigValue(String.class, String.class.getComponentType(), "db.dialect", false);
        maxPoolSize = (int) getConfigValue(int.class, int.class.getComponentType(), "db.maxPoolSize", false);
        return new DatabaseConfiguration(driver, url,
                appUsername, appPassword, SQLDialect.valueOf(dialect), maxPoolSize);
    }

    private Object getConfigValue(Class<?> paramClass, Type paramType, String path, boolean nullable) {
        Optional<Object> extractedValue = ConfigExtractors.extractConfigValue(config, paramClass, path);
        if (extractedValue.isPresent()) {
            return extractedValue.get();
        }

        if (nullable && !config.hasPath(path)) {
            return null;
        }

        ConfigValue configValue = config.getValue(path);
        ConfigValueType valueType = configValue.valueType();
        if (valueType.equals(ConfigValueType.OBJECT) && Map.class.isAssignableFrom(paramClass)) {
            ConfigObject object = config.getObject(path);
            return object.unwrapped();
        } else if (valueType.equals(ConfigValueType.OBJECT)) {
            Object bean = ConfigBeanFactory.create(config.getConfig(path), paramClass);
            return bean;
        } else if (valueType.equals(ConfigValueType.LIST) && List.class.isAssignableFrom(paramClass)) {
            Type listType = ((ParameterizedType) paramType).getActualTypeArguments()[0];

            Optional<List<?>> extractedListValue =
                    ListExtractors.extractConfigListValue(config, listType, path);

            if (extractedListValue.isPresent()) {
                return extractedListValue.get();
            } else {
                List<? extends com.typesafe.config.Config> configList = config.getConfigList(path);
                return configList.stream()
                        .map(cfg -> {
                            Object created = ConfigBeanFactory.create(cfg, (Class) listType);
                            return created;
                        })
                        .collect(Collectors.toList());
            }
        }

        throw new RuntimeException("Cannot obtain config value for " + paramType + " at path: " + path);
    }
}
