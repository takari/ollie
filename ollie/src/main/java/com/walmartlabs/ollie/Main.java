package com.walmartlabs.ollie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

  private static File workingDirectory = new File(System.getProperty("user.dir"));
  private static File userDirectory = new File(System.getProperty("user.home"));

  private final File serverDirectory;

  public Main(String serverDirectory) {
    this.serverDirectory = findServerDirectory(serverDirectory);
  }

  public void run() throws IOException {
    int port = 8080;
    File serverProperties = new File(serverDirectory, "server.properties");
    if (serverProperties.exists()) {
      System.out.format("We have server properties %s%n", serverProperties);
      Properties p = properties(new File(serverDirectory, "server.properties"));
      try {
        port = Integer.parseInt(p.getProperty("port"));
      } catch (NumberFormatException nfe) {
        // We will stick with the default
      }
    }
    System.out.format("Using port %s%n", port);
    WebServerBuilder builder = new WebServerBuilder();
    builder.port(port);
    for (File file : serverDirectory.listFiles()) {
      System.out.println(file);
      if (file.getName().equals("sites")) {
        builder.sites(file);
      }
    }
    WebServer server = builder.build();
    server.start();
  }

  private File findServerDirectory(String userSuppliedServerDirectory) {
    if (userSuppliedServerDirectory != null && new File(userSuppliedServerDirectory).exists()) {
      return new File(userSuppliedServerDirectory);
    }
    if (new File(workingDirectory, "server").exists()) {
      return new File(workingDirectory, "server");
    }
    if (new File(userDirectory, "server").exists()) {
      return new File(userDirectory, "server");
    }
    throw new RuntimeException("No valid server directory found.");
  }

  private Properties properties(File f) throws IOException {
    Properties p = new Properties();
    try (InputStream is = new FileInputStream(f)) {
      p.load(is);
    }
    return p;
  }

  public static void main(String[] args) throws IOException {
    Main main = new Main(args.length == 1 ? args[0] : null);
    main.run();
  }
}
