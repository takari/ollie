package com.walmartlabs.ollie.guice;

import java.util.List;

import javax.inject.Provider;

import org.apache.shiro.realm.Realm;

import com.google.inject.Module;
import com.walmartlabs.ollie.WebServerBuilder;
import com.walmartlabs.ollie.model.FilterDefinition;

public class OllieServerConfiguration {
  private String name;
  private String api;
  private String docs;
  private String title;
  private String description;
  private String packageToScan;
  private List<Module> modules;
  private Provider<Realm> realm;
  private List<Class<? extends Realm>> realms;
  private List<FilterDefinition> filterChains;

  private OllieServerConfiguration(Builder builder) {
    this.name = builder.name;
    this.api = builder.api;
    this.docs = builder.docs;
    this.title = builder.title;
    this.description = builder.description;
    this.packageToScan = builder.packageToScan;
    this.modules = builder.modules;
    this.realm = builder.realm;
    this.realms = builder.realms;
    this.filterChains = builder.filterChains;
  }

  public String name() {
    return name;
  }

  public String api() {
    return api;
  }

  public String docs() {
    return docs;
  }

  public String title() {
    return title;
  }

  public String description() {
    return description;
  }
  
  public String packageToScan() {
    return packageToScan;
  }

  public List<Module> modules() {
    return modules;
  }
  
  public Provider<Realm> realm() {
    return realm;
  }

  public List<Class<? extends Realm>> realms() {
    return realms;
  }
  
  public List<FilterDefinition> filterChains() {
    return filterChains;
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String name;
    private String api;
    private String docs;
    private String title;
    private String description;
    private String packageToScan;
    private List<Module> modules;
    private Provider<Realm> realm;
    private List<Class<? extends Realm>> realms;
    private List<FilterDefinition> filterChains;

    private Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder api(String api) {
      this.api = api;
      return this;
    }

    public Builder docs(String docs) {
      this.docs = docs;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder packageToScan(String packageToScan) {
      this.packageToScan = packageToScan;
      return this;
    }
    
    public Builder modules(List<Module> modules) {
      this.modules = modules;
      return this;
    }
        
    public Builder realm(Provider<Realm> realm) {
      this.realm = realm;
      return this;
    }

    public Builder realms(List<Class<? extends Realm>> realms) {
      this.realms = realms;
      return this;
    }

    public Builder filterChains(List<FilterDefinition> filterChains) {
      this.filterChains = filterChains;
      return this;
    }

    public OllieServerConfiguration build() {
      return new OllieServerConfiguration(this);
    }
  }
}
