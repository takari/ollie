# Ollie DB Configuration Example
## Overview
The Ollie database layer allows users to easily add database integration into
 their application via Liquibase and Jooq. The process breaks down into two 
 steps: code generation and database access/migration which happen during 
 build time and runtime respectively. This working directory contains a 
 working example of an app using Ollie to save Users into a database. All 
 snippets in this document will come from this example.

#### Liquibase
Liquibase is a tool for initializing and managing a database's schema through
 a changelog xml file. The changelog contains a list of things called 
 changesets which are commands to create tables, add columns, set up foreign 
 key constraints, etc. Whenever you connect to the database, Liquibase will 
 perform a migration in which it will look at the state of the database and 
 execute any changesets that haven't yet taken place on the DB. Liquibase 
 does not create DB instances - you have to provide Liquibase with an 
 existing DB instance and then it can create/update whatever tables or other 
 schemas you specify on that instance.

#### Jooq
Jooq is a code generation tool. You provide Jooq the credentials to access an
 existing database and it will scan the tables in that database and create 
 java classes which represent a row in the table. Jooq also provides the 
 means to establish connections and read/write to to a database using the 
 classes it generated (so long as the schema structure of the DB you are 
 reading/writing to matches the schema structure of the DB that was scanned 
 for code generation).

## Code generation
#### Purpose
We separate the code generation step from the run time data access step so 
that you don't need to depend on your end database in order to build your 
application. So rather than scanning your production database, this step will
 create a temporary local database, use liquibase to load your schemas to 
 that DB, and then scan those schemas with Jooq to make Jooq's model classes.

#### Configuration
All of the build time code generation will happen inside your pom.xml file 
and will make use of some preconfigured plugins from Ollie's parent pom.

To use this you must add the Ollie's parent pom as the parent to your pom:
```xml
<parent>
   <groupId>com.walmartlabs.ollie</groupId>
   <artifactId>ollie-parent</artifactId>
   <version>0.0.27</version>
</parent>
```
And now you must explicitly declare which plugins from the parent pom you 
would like to use in your application. They have been pre configured so if 
you are ok with the defaults we have chosen all you have to do is declare 
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
This will configure liquibase to use your project's 
`src/main/resources/liquibase.xml` as the changelog file by default and will 
migrate those schemas to an H2 database with this connection string: 
`jdbc:h2:${basedir}/target/ollie-db-codegen;DATABASE_TO_UPPER=false`. This 
makes use of the `org.h2.Driver` class so you will need to add `h2database` to 
your list of dependencies. The database username will be sa and password is 
blank or `""`.

Jooq will then scan this database and place its generated classes in the 
`target/generated-sources/persistence-jooq` folder which will be accessible 
in your application via the `com.walmartlabs.persistence.jooq` package.

#### Advance Configuration
While this default configuration has its advantages, we recognize it might 
not fit the needs of your application so we exposed certain overridable 
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
This overrides the default schema (the postgresql driver wants the schema to 
be 'public' instead of 'PUBLIC' in the case of H2), driver, url, username, 
and password to be used in the plugins. Note that ${db.host} and ${db .port} 
default to localhost and 5432 but could be overridden by explicitly declaring
 `<db.port>xxxx</db.port>`.

This implementation also adds the docker-maven-plugin because, as you recall,
 Liquibase does not create a DB instance which means one has to already exist
  . With H2 this isn't a problem but for a DB like PostgreSQL you need to 
  have one created before the Liquibase plugin runs. Docker allows us to spin
   up a Docker container with a PostgreSQL image on it for temporary use. The
    docker plugin has some defaults that can be overridden as well which are:

```xml
<db.image>hub.docker.prod.walmart.com/library/postgres:10.4-alpine</db.image>
<network.mode>bridge</network.mode>
<network.name>bridge</network.name>
```
## Run Time Database Access
Your build can now generate Jooq classes but now you need to actually read 
and write to a database at run time. Luckily Ollie has performed the heavily 
lifting here as well by providing a handful of library classes for you. These
 library classes are accessible by adding ollie as a dependency in your POM 
 which you have most likely already done:
 ```xml
 <dependency>
     <groupId>com.walmartlabs.ollie</groupId>
     <artifactId>ollie</artifactId>
     <version>${ollie.version}</version>
 </dependency>
 ```
 Note that database support was added in Ollie version 0.0.27.

#### DatabaseModule
The DatabaseModule class will set up your dependency injector so that 
whenever you create a DAO class it will set up your, data source, and attempt
 a Liquibase DB migration. It will execute the migration using the changelog 
 file `src/main/resources/liquibase.xml` just as in the code generation step.
  If you prefer to manually set up your database schema as apposed to using 
  liquibase migrations during run time you can set the system variable: 
  `-Dollie.db.autoMigrate=false` and the migration step will be skipped.

To make use of the DatabaseModule all you need to do is invoke the 
databaseSupport() method when you create your OllieServer:
```
OllieServerBuilder builder = new OllieServerBuilder()
        .port(9000)
        .name("userServer") 
        .packageToScan("com.walmartlabs.ollie.example")
        .databaseSupport(true);

server = builder.build();
server.start();
```

#### DatabaseConfigurationProvider
When the DatabaseModule attempts to wire up a datasource it will need 
configuration information such as jdbc connection string. By adding 
databaseSupport to your OllieServerBuilder (see example above) the 
DatabaseConfigurationProvider will automatically look in your `.conf` file 
for these configuration values. Here is an example of the values you will 
need to specify in your .conf file:
```
db.driver = "org.postgresql.Driver"
db.url = "jdbc:postgresql://localhost:5432/postgres"
db.appUsername = "postgres"
db.appPassword = "q1"
db.dialect = POSTGRES
db.maxPoolSize = 10
```

#### AbstractDao
The final piece of the puzzle is the AbstractDao. To interact with the DB you
 will need to write concrete DAO classes which extend AbstractDao. You will 
 interact with the database using normal Jooq conventions but the AbstractDao
  will handle connections, transactions, and DSLContexts for you so you can 
  focus on the query logic.

Here is an example of a simple concrete DAO class which inserts into a USERS 
table with columns ID, FIRST_NAME, and LAST_NAME:
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
For example, for PostgreSQL DB's, there is a concept known as "INSERT -> 
RETURNING" which fetches and return the object that was just inserted, but 
that functionality will not work if the DB you are working with is an H2 DB. 
So if you are not careful, the DB's may not be interchangeable.

#### AbstractDaoTest
The AbstractDaoTest class allows you easily write unit tests for your DAO 
classes. It provides a getConfiguration() method which will provide the saved
 DB Configuration which will default to be an in memory H2 database. If that 
 is sufficient you can use it like so:

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
overloaded getConfiguration function like so:
```
@Before
public void setUp() throws Exception {
    userDao = new UserDao(getConfiguration(driver, url, username, password
            SQLDialect.valueOf(dialect), maxPoolSize));
}
```
Keep in mind that if you go this route you will need to ensure the DB you are
 providing configuration for exists and is running if you want this test to 
 pass.
