# Welcome

Persism is a wood simple, auto-discovery, auto-configuration, and convention over configuration ORM (Object Relational Mapping) library for Java.

## Simple

The API for Persism is small. Mostly you just need a Connection and a persism Session object, and you're good to go. 
There are some optional annotations and a Persistable implementation for where you need to track changes to properties for UPDATE statements.

## Auto-Discovery
Persism figures things out for you. Create a table, write a JavaBean, run a query. Persism uses simple mapping rules to find your table and column names and only requires an annotation where it can’t find a match.

## Convention over configuration
Persism requires no special configuration. Drop the JAR into your project and go.

Persism has annotations though they are only needed where something is outside the conventions. In most cases you probably don't even need them.

Persism can usually detect the table and column mappings for you including primary/generated keys and columns with defaults.

## Smart
Persism will do the correct thing by default. Persism understands that your class is called 
"Customer" and your table is called "CUSTOMERS". It understands that your table column is 
"CUSTOMER_ID" and your property is "customerId". Persism gets it. Heck Persism even understands 
when your class is called Category and your table is called CATEGORIES. No problem. 
Don’t even bother annotating that stuff. Persism uses annotations as a fall back – 
annotate only when something is outside the conventions.

## Tiny
Persism is under 60k and has *Zero* dependencies however it will utilize logging based on whatever is available 
at runtime - SLF4J, LOG4J or JUL.

[Javadoc](/javadoc/index.html)

[Code Coverage](/coverage/index.html)

[Release Notes](/release-notes.md)

### Other documentation

[Getting Started](/manual.md)

[Cookbook: Implementing Persistable interface](cookbook-persistable.md)

Supported Databases

![MSSQL](img/mssql.png) ![MySQL](img/mysql.png) ![JTDS](img/jtds.png) ![Derby](img/derby.png) ![Firebird](img/firebird.png) ![H2](img/h2.png) ![HQSLDB](img/hsqldb.jpg) ![Oracle](img/oracle.png) ![PostgreSQL](img/postgresql.png) ![SQLite](img/sqlite.png)
