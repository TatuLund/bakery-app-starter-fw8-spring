# Bakery

Vaadin 8 demo application for Spring Boot.

This project demonstrates how to build Vaadin 8 applications with Spring Boot and Spring Security, use Spring dependency injection throughout the UI and backend layers, run browserless UI integration tests with `UIUnitTest` and Spring, and apply accessible design and more modern maintenance practices in a Vaadin 8 codebase.

## Stack

- Spring Boot 2.7.18
- Vaadin 8.31.1

## Wiki

The [project Wiki](https://github.com/TatuLund/bakery-app-starter-fw8-spring/wiki) contains practical articles about the main implementation patterns in this codebase. It explains how Bakery uses Vaadin 8, Spring Boot, Spring Security, Spring Data JPA, browserless UI tests, accessibility improvements, and operational UI patterns in a maintainable legacy application.

# Running the project

`mvn spring-boot:run`

Wait for the application to start

Open http://localhost:8080/ to view the application.

Default credentials are admin@vaadin.com/admin for admin access and
barista@vaadin.com/barista for normal user access.

# Running with Docker Compose and MySQL

The repository includes a local production-like stack with two containers:

- `mysql` runs MySQL 8 with a persistent Docker volume.
- `app` builds this project into an executable war and starts it with the `production` Spring profile.

If you use Vaadin commercial components, export `VAADIN_PRO_KEY` before building so the key is available during both image build and container runtime.

Start the stack with:

`docker compose up --build`

The application will be available at http://localhost:8080/ and MySQL at localhost:3306.

The compose file maps the existing production datasource variables to the MySQL container:

- `RDS_HOSTNAME=mysql`
- `RDS_PORT=3306`
- `RDS_DB_NAME=bakery`
- `RDS_USERNAME=bakery`
- `RDS_PASSWORD=bakery`

Stop the stack with:

`docker compose down`

To also remove the persisted MySQL data volume:

`docker compose down -v`

# Running the project as an executable jar

The project is configured to automatically make the build artifact runnable using `java -jar`.
By default you can thus also run the project by executing the war file:
```
java -jar target/###artifactId###-1.0-SNAPSHOT.war
```

If you want to produce a `jar` file instead of a `war` file, change the packaging type in `pom.xml` to `<packaging>jar</packaging>`.

You also need to configure Vaadin resources to be included into the jar:

```xml
<build>
 ...
    <resources>
        <resource>
            <directory>src/main/webapp</directory>
                <filtering>false</filtering>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
                <filtering>false</filtering>
        </resource>
    </resources>
 ...
</build>
```

# Running browserless UI tests with UIUnitTest

The project also contains fast browserless UI tests built on top of TestBench `UIUnitTest`. These tests run entirely in the JVM, so they are useful for verifying Vaadin view logic, navigation, component state and Spring wiring without starting a browser.

The shared setup lives in `src/test/java/com/vaadin/starter/bakery/ui/AbstractUITest.java`. It boots a Spring-backed mock Vaadin environment, creates the UI through `SpringUIProvider`, exposes the `WebApplicationContext` for Spring navigation, and runs Spring Boot startup runners so the test data matches normal application startup.

Examples:

- `src/test/java/com/vaadin/starter/bakery/ui/SpringNavigationTest.java` verifies that navigation resolves Spring-managed views and injected dependencies.
- `src/test/java/com/vaadin/starter/bakery/ui/views/storefront/StorefrontTest.java` verifies filtering and grid contents without a browser.
- `src/test/java/com/vaadin/starter/bakery/ui/views/orderedit/*.java` contains browserless order editing tests.

Run all unit and browserless UI tests:

`mvn verify`

and make sure you have a valid license key installed.

# Running integration tests

Integration tests are implemented using TestBench. The tests take tens of minutes to run and are therefore included in a separate profile. To run the tests, execute

`mvn verify -Pit`

and make sure you have a valid license key installed.

# Running scalability tests

Scalability tests can be run as follows

1. Configure the number of concurrent users and a suitable ramp up time in the end of the `src/test/scala/*.scala` files, e.g.:
	```setUp(scn.inject( rampUsers(50) over (60 seconds)) ).protocols(httpProtocol)```

2. If you are not running on localhost, configure the baseUrl in the beginning of the `src/test/scala/*.scala` files, e.g.:

	```val baseUrl = "http://my.server.com"```

3. Make sure the server is running at the given URL. To run the local server, use
  ```mvn spring-boot:run```

4. Start a test from the command line, e.g.:
	 ```mvn -Pscalability gatling:execute -Dgatling.simulationClass=com.vaadin.starter.bakery.Barista```

5. Test results are stored into target folder, e.g.:
	```target/gatling/Barista-1487784042461/index.html```

# Developing the project

The project can be imported into the IDE of your choice as a Maven project

# License
A paid a subscription with Vaadin 8 Extended Maintenance option included is required for creating a new software project from this starter. After its creation, results can be used, developed and distributed freely, but licenses for the used commercial components are required during development. The starter or its parts cannot be redistributed as a code example or template.

For full terms, see LICENSE
