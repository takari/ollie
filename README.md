# Ollie

Ollie is Java-based application framework designed to simplify the 
creation and distribution of REST-API enabled server applications.

The creators of Ollie saw that many apps had common structures. They would
have some kind of dependency injection framework, persistence, api 
documentation, a way to build and run the app, etc.. The goal with Ollie is 
to abstract as much of this common architecture as possible so that you, the 
user, can focus your attention on developing your application. By using Ollie
 you automatically get Google Guice, Rest Api, swagger, configuration files, 
 and packaging via provisio (more on these below).


## Usage

Add the takari parent as your parent POM:

```xml
<parent>
 <groupId>io.takari</groupId>
 <artifactId>takari</artifactId>
 <version>27</version>
</parent>
```

Add the ollie-targetplatform to your dependencyManagement in your POM.xml:

```xml
<dependencyManagement>
 <dependencies>
   <dependency>
     <groupId>com.walmartlabs.ollie</groupId>
     <artifactId>ollie-targetplatform</artifactId>
     <version>${ollie.version}</version>
     <type>pom</type>
     <scope>import</scope>
   </dependency>
 </dependencies>
</dependencyManagement>
```

  Note that this was written for Ollie version 0.0.20. You can find the 
  latest version here: 
  http://repo1.maven.org/maven2/com/walmartlabs/ollie/ollie/
 
Add ollie to your list of dependencies in your POM.xml:

```xml
<dependency>
 <groupId>com.walmartlabs.ollie</groupId>
 <artifactId>ollie</artifactId>
</dependency>

```


# Features

## Google Guice and Swagger

The guice framework comes built in when using Ollie. I won’t go into details
here on how to use Guice but I will show how Ollie interacts with it.

By using Ollie you can use an OllieServerBuilder to create a Guice server by
specifying the port, name, package of your project to be scanned for
the dependency injector, and the file where secret configuration values can 
be found (more on this in the configuration files section).

Here is an example using OllieServerBuilder to set up and start a server:

```java
OllieServerBuilder builder = new OllieServerBuilder()
       .port(9000)
       .name("gatekeeper")
       .packageToScan("com.walmartlabs.gatekeeper")
       .secrets(new File("/secrets/secrets.properties"));
server = builder.build();
server.start();
```

Now any class in the packageToScan can be used for Guice dependency injection
or it can be used as an API listener with Swagger documentation.

To set up an API listener with Swagger documentation you simple make a class
that implements `org.sonatype.siesta.Resource`  (you may need to add this to
your POM in order to use it) and uses the annotations in the example below
(Swagger and Guice documentation can show you other annotations that can be
used):

```java
@Named
@Singleton
@Path("/ping")
@Api(value = "/ping", tags = "ping")
public class PingListener implements Resource {

   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @ApiOperation(value = "ping")
   public Response processPing() {
       return Response.ok().build();
   }
}
```

Once you start your server you can see the Swagger documentation by
navigating to `<hostname>:<port>/docs`. So if you are running your app
locally on port 9000 you would go to `localhost:9000/docs`

## Configuration files

Ollie allows you to add a configuration file to your project in the
`src/main/resources` folder called `<projectName>.conf`. You will
automatically be able to use values in the *.conf file by using the `@Config`
annotation and you can set up configurations for multiple environments in
the same configuration file.

Let’s look at my gatekeeper.conf file as an example:

```txt
gatekeeper {
 // Shared application configuration regardless of environment
 development {
   jira.server = "https://jira-dev.walmart.com"
   jira.username = ${jira.username}
   jira.password = ${jira.password}
   crq.gate.enabled = true
 }

 production {
   jira.server = "https://jira-prod.walmart.com"
   jira.username = ${jira.username}
   jira.password = ${jira.password}
   crq.gate.enabled = false
 }
}
```

As we can see, in the gatekeeper project (note that this name must match the
name provided when starting your OllieServer) there are two environments with
the same keys but different values: development and production. Ollie will
choose which environment to use based on a parameter passed in at runtime
called `-Dollie.environment`. We will see how this is passed in later, but
when run locally this will default to development.

Configuration values can be accessed by using the `@Config` annotation:

```java
@Inject
public DefaultCrqJiraClient(@Config("jira.server") String server, @Config
("jira.username") String username,
   @Config("jira.password") String password) {
 this.server = server;
 this.username = username;
 this.password = password;
}
```

The config annotation can be used to inject configuration values into any
variable but it is recommended to use it for constructor injection as shown
above.

In the `*.conf` file, if you use the syntax `${example.name}`, Ollie will
automatically go look in the file you configured when you set up the server 
for the value. In our example it will go look in `/secrets/secrets
.properties`. I use the `/secrets/secrets.properties` location because I use 
a tool called "keywhiz" to distribute sensitive information out to the 
compute(s) my app runs on and keywhiz automatically places them in the 
`/secrets` directory. You can configure your server to look for secret data 
wherever you like. Ideally you would use a tool like keywhiz to distribute 
your secret values to your computes in a known location. Ollie will then 
access these secret values by using the `${}` syntax in the `*.conf` file. 
This way, your conf file can be pushed to github without revealing secure 
information. For completion purposes, here is what the secrets.properties 
file would look like for our example:

```properties
jira.username=myUsername
jira.password=superSecure
```


## Provisio

Ollie allows you to use a packaging method called provisio by using the
`<packaging>` tag in your POM.xml

```xml
<groupId>com.walmartlabs.gatekeeper</groupId>
<artifactId>gatekeeper</artifactId>
<version>0.0.9-SNAPSHOT</version>
<packaging>provisio</packaging>
```

Using the provisio packaging will ultimately create a
`<projectNameAndVersion>.tar.gz` file when you build your project. If you
were to unpack this tar.gz file you would find a `bin`, `etc`, and `lib`
folder. The `lib` folder holds your projects JAR as well as all of your
dependency JARs. The `etc` folder contains some configuration files used to
define your main class among other things. The `bin` folder contains a
launcher python script to run your main class by executing `./bin/launcher
start` (or optionally `./bin/launcher start -Dollie.environment=production`
to override the default environment used by the configuration).

Ultimately provisio creates a tar.gz which is an easily exportable and
runnable artifact with all the details it needs to run your application.

### Provisio setup

To allow provisio packaging to work properly you need to add an `etc` folder
and a `provisio` folder to your project’s `src/main` folder.

The `etc` folder will have two files:

1. `config.properties` which will contain your server port information:
```txt
http-server.http.port=8000
```
2. `jvm.config` which will contain:
```txt
-server
-Xmx1G
-XX:+UseG1GC
-XX:G1HeapRegionSize=32M
-XX:+UseGCOverheadLimit
-XX:+ExplicitGCInvokesConcurrent
-XX:+HeapDumpOnOutOfMemoryError
-XX:+ExitOnOutOfMemoryError
-Dollie.environment=production
```

This will set up some defaults for the launcher script to use. You will
notice the last line sets up the environment to be used by the
configuration file that we discussed before. You are able to add other 
configuration files for things like loggers. The launcher itself serves as a 
sort of readme if you open it in a text editor for more details.

The `provisio` folder will have one file:

1. `server.xml` which contains info on how to construct the tar.gz:

```xml
<runtime>
 <archive name="${project.artifactId}-${project.version}.tar.gz"/>
 <!-- Notices -->
 <fileSet to="/">
   <directory path="${basedir}">
     <include>NOTICE</include>
     <include>README.txt</include>
   </directory>
 </fileSet>
 <!-- Launcher -->
 <artifactSet to="bin">
   <artifact id="io.airlift:launcher:tar.gz:bin:${launcherVersion}">
     <unpack/>
   </artifact>
   <artifact id="io.airlift:launcher:tar.gz:properties:${launcherVersion}">
     <unpack filter="true"/>
   </artifact>
 </artifactSet>
 <fileSet to="/etc">
   <directory path="${basedir}/src/main/etc"/>
 </fileSet>
 <!-- Server -->
 <artifactSet to="/lib" ref="runtime.classpath"/>
</runtime>
```
This relies on a property <launcherVersion> which will need to be specified 
in your pom if it isn't already. At the time this was written I was using 
`<launcherVersion>0.124</launcherVersion>`

## Database
Ollie can optionally allow for easy configuration of a database layer using 
Liquibase and Jooq. Please see the [db-example readme](db-example) for 
implementation details.

# Goal

Ultimately the goal is for a build agent to build your application, place the
created tar.gz artifact in some kind of repository manager, and then kick off
your deployment job. Your deployment job will provision your compute 
information, place your tar.gz artifact on that compute(s), and then unpack 
the tar.gz and execute the launcher script with `./bin/launcher start -Dollie
.environment=production`.
