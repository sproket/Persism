![](logo1.png) [Release notes 1.0.1](release-notes.md) 

# Welcome

Persism is a wood simple, auto discovery, auto configuration, and convention over configuration ORM (Object Relational Mapping) library for Java.

```
<dependency>
    <groupId>io.github.sproket</groupId>
    <artifactId>persism</artifactId>
    <version>1.0.1</version>
</dependency>
```
## Simple

The API for Persism is small. Mostly you just need a Connection and a persism Session object and you're good to go.
There are some optional annotations and a Persistable implementation for where you need to track changes to properties for UPDATE statements.

## Auto-Discovery
Persism figures things out for you. Create a table, write a JavaBean, run a query. Persism uses simple mapping rules to find your table and column names and only requires an annotation where it can’t find a match.

## Convention over configuration
Persism requires no special configuration. Drop the JAR into your project and go.

Persism has annotations though they are only needed where something is outside the conventions. In most cases you probably don't even need them.

Persism can usually detect the table and column mappings for you including primary/generated keys and columns with defaults.

## Smart
Persism will do the correct thing by default. Persism understands that your class is called Customer and your table is called CUSTOMERS. It understands that your table column is CUSTOMER_ID and your property is customerId. Persism gets it. Heck Persism even understands when your class is called Category and your table is called CATEGORIES. No problem. Don’t even bother annotating that stuff. Persism uses annotations as a fall back – annotate only when something is outside the conventions.

## Tiny
Persism is under 60k. Yeah, fit it on a floppy if you want. Persism has Zero dependencies however it will utilize logging based on whatever is available at runtime - SLF4J, LOG4J or JUL.

[Have a look here for the getting started guide, code coverage and Javadoc](https://sproket.github.io/Persism/)

## Compile

To run tests only basic tests: in memory databases (H2, HSSQL, Derby) + sqlite (faster)

    mvn clean test

To run basic tests + testContainers based tests (postgresql, mysql, mariadb, firebird). Need docker installed.

    mvn clean test -P include-test-containers-db

To run tests for every supported database. Needs Oracle up and running

    mvn clean test -P all-db

To generate surefire reports with every database but Oracle  (in target/site/surefire-report.html)

    mvn clean test surefire-report:report -P include-test-containers-db

Thanks!