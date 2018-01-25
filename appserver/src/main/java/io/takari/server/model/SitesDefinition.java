package io.takari.server.model;

import java.io.File;

public class SitesDefinition {

  private File sitesDirectory;

  public void setSitesDirectory(File sitesDirectory) {
    this.sitesDirectory = sitesDirectory;    
  }

  public File getSitesDirectory() {
    return sitesDirectory;
  }
}
