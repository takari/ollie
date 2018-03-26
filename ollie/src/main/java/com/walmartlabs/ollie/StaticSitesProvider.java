package com.walmartlabs.ollie;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.providers.ScanningAppProvider;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.PathResource;

/**
 
Scan a directory for sites directories where the directory has a FQDN. As new site directories
appear a virtual host entry is added, and the content is immediately available to be served.
 
sites
├── bar.walmartlabs.com
├── baz.walmartlabs.com
└── foo.walmartlabs.com
  
*/
public class StaticSitesProvider extends ScanningAppProvider {

  public class Filter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
      if (!dir.exists()) {
        return false;
      }
      Path path = new File(dir, name).toPath();
      // ignore hidden files
      try {
        if (path.getFileName().toString().startsWith(".") || Files.isHidden(path)) {
          return false;
        }
      } catch (IOException ignore) {
        // ignore
      }
      // Only Directories are allowed.
      if (Files.isDirectory(path)) {
        // Must have index.html
        return Files.isRegularFile(path.resolve("index.html"));
      }
      return false;
    }
  }

  public StaticSitesProvider() {
    super();
    setFilenameFilter(new Filter());
    setScanInterval(0);
  }

  /**
   * A new App has been created, create the Context for that app.
   *
   * @param app the raw app discovered.
   * @return the ContextHandler for that app
   */
  @Override
  public ContextHandler createContextHandler(App app) throws Exception {
    Path dir = new File(app.getOriginId()).toPath();
    ContextHandler context = new ContextHandler();
    context.setVirtualHosts(new String[] {dir.getFileName().toString()});
    context.setWelcomeFiles(new String[] {"index.html", "index.htm"});
    ResourceHandler staticResources = new ResourceHandler();
    staticResources.setBaseResource(new PathResource(dir));
    context.setHandler(staticResources);
    return context;
  }
}
