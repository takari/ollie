package io.takari.server.guice;

import java.util.List;

import javax.inject.Provider;

import org.apache.shiro.realm.Realm;

import com.google.inject.Module;


public class JaxRsServerConfig {

  private String api;
  private String docs;
  private String title;
  private String description;
  private List<Module> modules;
  private Provider<Realm> realm;

  private JaxRsServerConfig(Builder builder) {
    this.api = builder.api;
    this.docs = builder.docs;
    this.title = builder.title;
    this.description = builder.description;
    this.modules = builder.modules;
    this.realm = builder.realm;
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

  public List<Module> modules() {
    return modules;
  }
  
  public Provider<Realm> realm() {
    return realm;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String api;
    private String docs;
    private String title;
    private String description;
    private List<Module> modules;
    private Provider<Realm> realm;

    private Builder() {}

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

    public Builder modules(List<Module> modules) {
      this.modules = modules;
      return this;
    }
    
    public Builder realm(Provider<Realm> realm) {
      this.realm = realm;
      return this;
    }

    public JaxRsServerConfig build() {
      return new JaxRsServerConfig(this);
    }
  }
}
