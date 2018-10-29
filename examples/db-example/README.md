# Ollie DB Configuration Example

## Overview

The Ollie database layer allows users to easily add database integration into
their application via Liquibase and Jooq.

The process breaks down into two steps - code generation and database
access/migration which happen during build time and runtime respectively.

This directory contains a working example of an app using Ollie to save `users`
into a database. All snippets in this document are from this example.

### Liquibase

Liquibase is a tool for initializing and managing a database's schema through a
changelog xml file.

The changelog contains a list of things called changesets. Changesets are
commands to create tables, add columns, set up foreign key constraints,
etc. Whenever you connect to the database, Liquibase performs a migration in
which it looks at the state of the database and executes any changesets that
haven't yet been executed on the database.

Liquibase does not create database instances - you have to provide Liquibase
with an existing database instance and then it can create/update whatever tables
or other schemas you specify on that instance.

### Jooq

Jooq is a code generation tool. You provide Jooq the credentials to access an
existing database and it scans the tables in that database and creates Java
classes which represent a row in the table.

Jooq also provides the means to establish connections and read/write to a
database using the classes it generated (so long as the schema structure of the
DB you are reading/writing to matches the schema structure of the DB that was
scanned for code generation).

## Code generation

### Purpose

We separate the code generation step from the run time data access step so that
you don't need to depend on your end database in order to build your
application. So rather than scanning your production database, this step creates
a temporary local database, uses liquibase to load your schemas to that DB, and
then scans those schemas with Jooq to make Jooq's model classes.

### Configuration

All of the build time code generation is controlled in your pom.xml file and
makes use of some preconfigured plugins from Ollie's parent pom.

To use this you must add the Ollie's parent pom as the parent to your pom:

```xml
<parent>
   <groupId>com.walmartlabs.ollie</groupId>
   <artifactId>ollie-parent</artifactId>
   <version>0.0.27</version>
</parent>
```

And now you must explicitly declare which plugins from the parent pom you 
would like to use in your application. They have been pre configured so, if 
you are ok with the defaults we have chosen, all you have to do is declare 
them like so:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.takari.maven.plugins</groupId>
            <artifactId>takari-lifecycle-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.jooq</groupId>
            <artifactId>jooq-codegen-maven</artifactId>
        </plugin>
    </plugins>
</build>
```

This configures LiquiBase to use your project's
`src/main/resources/liquibase.xml` as the changelog file by default and migrates
those schemas to an H2 database with this connection string:
`jdbc:h2:${basedir}/target/ollie-db-codegen;DATABASE_TO_UPPER=false`. This makes
use of the `org.h2.Driver` class so you need to add `h2database` to your list of
dependencies. The database username will be `sa` and password is blank or `""`.

Jooq then scans this database and places the generated classes in the
`target/generated-sources/persistence-jooq` folder which is be accessible in
your application in the `com.walmartlabs.persistence.jooq` package.

### Advanced Configuration

While this default configuration has its advantages, we recognize it might 
not fit the needs of your application, so we exposed certain overridable 
properties so you can customize the code generation step.

Here is an example that uses PostgreSQL for its code generation:

```xml
<properties>
    <db.schema>public</db.schema>
    <db.driver>org.postgresql.Driver</db.driver>
    <db.url>jdbc:postgresql://${db.host}:${db.port}/postgres</db.url>
    <db.username>postgres</db.username>
    <db.password>q1</db.password>
</properties>
<build>
    <plugins>
        <plugin>
            <groupId>io.takari.maven.plugins</groupId>
            <artifactId>takari-lifecycle-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>io.fabric8</groupId>
            <artifactId>docker-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.jooq</groupId>
            <artifactId>jooq-codegen-maven</artifactId>
        </plugin>
    </plugins>
</build>
```

This overrides the default schema (the postgresql driver wants the schema to be
`public` instead of `PUBLIC` in the case of H2), driver, url, username, and
password to be used in the plugins. Note that `${db.host}` and `${db .port}`
default to `localhost` and `5432` but could be overridden by explicitly
declaring `<db.port>xxxx</db.port>`.

This implementation also adds the docker-maven-plugin because, as you recall,
Liquibase does not create a database instance which means one has to already
exist. With H2 this isn't a problem but for a database like PostgreSQL you need to
have one created before the Liquibase plugin runs. Docker allows us to spin up a
Docker container with a PostgreSQL image on it for temporary use. The docker
plugin has some defaults that can be overridden as well which are:

```xml
<db.image>hub.docker.prod.walmart.com/library/postgres:10.4-alpine</db.image>
<network.mode>bridge</network.mode>
<network.name>bridge</network.name>
```

## Run Time Database Access

Your build can now generate Jooq classes.  Next you need to actually read and
write to a database at run time. Luckily Ollie has performed the heavy-lifting
here as well by providing a handful of library classes for you. These library
classes are accessible by adding Ollie as a dependency in your POM which you
have most likely already done:
 
 ```xml
 <dependency>
     <groupId>com.walmartlabs.ollie</groupId>
     <artifactId>ollie</artifactId>
     <version>${ollie.version}</version>
 </dependency>
 ```
 
 Note that database support was added in Ollie version 0.0.27.

## DatabaseModule

The `DatabaseModule` class sets up your dependency injector so that whenever you
create a DAO class it sets up your data source and attempts a Liquibase DB
migration.

It executes the migration using the changelog file
`src/main/resources/liquibase.xml` just as in the code generation step. If you
prefer to manually set up your database schema as apposed to using liquibase
migrations during run time you can set the system variable
`-Dollie.db.autoMigrate=false` to skip this migration step.

To make use of the DatabaseModule all you need to do is invoke the
`databaseSupport()` method when you create your OllieServer with the builder
class.

```
OllieServerBuilder builder = new OllieServerBuilder()
        .port(9000)
        .name("userServer") 
        .packageToScan("com.walmartlabs.ollie.example")
        .databaseSupport(true);

server = builder.build();
server.start();
```

### DatabaseConfigurationProvider

When the `DatabaseModule` attempts to wire up a datasource it needs
configuration information such as JDBC connection string. By adding
databaseSupport to your OllieServerBuilder (see example above) the
`DatabaseConfigurationProvider` automatically looks in your `.conf` file for
these configuration values.

Here is an example of the values you will need to specify in your .conf file:

```
db.driver = "org.postgresql.Driver"
db.url = "jdbc:postgresql://localhost:5432/postgres"
db.appUsername = "postgres"
db.appPassword = "q1"
db.dialect = POSTGRES
db.maxPoolSize = 10
```

### AbstractDao

The final piece of the puzzle is the AbstractDao. To interact with the DB you
need to write concrete DAO classes which extend AbstractDao. You then interact
with the database using normal Jooq conventions but the AbstractDao handles
connections, transactions, and DSLContexts for you, so you can focus on the
query logic.

Here is an example of a simple concrete DAO class which inserts into a `USERS`
table with columns `ID`, `FIRST_NAME`, and `LAST_NAME`:

```
import static com.walmartlabs.persistence.jooq.tables.Users.USERS;

@Named
public class UserDao extends AbstractDao {

    @Inject
    protected UserDao(@Named("app") Configuration cfg) {
        super(cfg);
    }

    public void insert(String firstName, String lastName) {
        UUID uuid = UUID.randomUUID();
        txResult(tx -> tx.insertInto(USERS)
                .columns(USERS.ID, USERS.FIRST_NAME, USERS.LAST_NAME)
                .values(uuid, firstName, lastName)
                .execute()
        );
    }
}
```

It is important to note that per Jooq's documentation there are certain 
features that are supported for one DB that are not supported for another. 

For example, for PostgreSQL DB's, there is a concept known as `INSERT ->
RETURNING` which fetches and returns the object that was just inserted, but that
functionality does not work if the databse you are working with is H2.  So if
you are not careful, the databases may not be interchangeable.

### AbstractDaoTest

The `AbstractDaoTest` class allows you to easily write unit tests for your DAO
classes. It provides a `getConfiguration()` method which provides the saved DB
Configuration which defaults to be an in memory H2 database. If that is
sufficient you can use it like so:

```
public class UserDaoTest extends AbstractDaoTest {
   private UserDao userDao;

   @Before
   public void setUp() throws Exception {
       userDao = new UserDao(getConfiguration());
   }

   @Test
   public void insertTest() throws Exception {
       userDao.insert("Ryan", "Savage");
       assert(true);
   }
}
```

Or, if you would like to use a custom configuration you can call the 
overloaded `getConfiguration` function like so:

```
@Before
public void setUp() throws Exception {
    userDao = new UserDao(getConfiguration(driver, url, username, password
            SQLDialect.valueOf(dialect), maxPoolSize));
}
```

Keep in mind that if you go this route you need to ensure the database you are
providing configuration for exists and is running if you want this test to pass.
