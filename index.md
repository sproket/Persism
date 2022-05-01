[Current Version 2.1.0](release-notes.md)

<div style="float: right">
<span style="font-weight: bold">By the numbers</span>
<ul> 
<li style="list-style-type:none;">
100k in size
</li>
<li style="list-style-type:none;">
300 unit tests
</li>
<li style="list-style-type:none;">
90% code coverage
</li>
<li style="list-style-type:none;">
11 supported dbs
</li>
<li style="list-style-type:none;">
0 dependencies
</li>
</ul> 
</div>

# ![](img/logo2.png) Welcome
<hr>
Persism is a simple, low ceremony, auto-discovery, auto-configuration, and convention over configuration ORM (Object Relational Mapping) library for Java.
<br>
<br>


<span style="font-style: italic"> "Coding by convention, kind of like Apache Wicket... I guess it has its place, yes. jOOQ also does auto-mapping of column names. But not of table names. Nice thinking" </span>
<a href="https://www.reddit.com/r/java/comments/1hxgrc/jooqs_reason_for_being_compared_to_jpa_linq_jdbc/cb1hgnw/">&nbsp; lukaseder - Author of JOOQ</a>

[**Get Started!**](/manual2.md)

## Simple

The API for Persism is small. Mostly you just need a Connection and a persism Session object, and you're good to go.
There are some optional annotations for mapping and a Persistable implementation for where you need to track changes to
properties for UPDATE statements, but that's about it.

## Auto-Discovery
Persism figures things out for you. Create a table, write a JavaBean, run a query.
Persism uses simple mapping rules to find your table and column names and only requires
an annotation where it can’t match.

## Convention over configuration
Persism requires no special configuration. Drop the JAR into your project and go.

Persism has annotations though they are only needed where something is outside the conventions.
In many cases you probably don't even need them.

Persism can usually detect the table and column mappings for you including primary/generated
keys and columns with defaults.

## Smart
Persism will do the correct thing by default. Persism understands that your class is called
"Customer" and your table is called "CUSTOMERS". It understands that your table column is
"CUSTOMER_ID" and your property is "customerId". Persism works fine even
when your class is called Category and your table is called CATEGORIES. No problem.
Persism uses annotations as a fall back –
annotate only when something is outside the conventions.

## Tiny
Persism is about 100k and has *Zero* dependencies however it will utilize logging based on whatever is available
at runtime - SLF4J, LOG4J2, LOG4J or JUL.

[SBOM Report - No vulnerable components found! :)](https://sbom.lift.sonatype.com/report/T1-a0368c8f29fdaa555824-66b418a0fe091-1648406946-e5c74ce579764856a8195d8633609be0)

[Javadoc For Version 2.x](/javadoc/persism2/index.html) - [Javadoc For Version 1.x](/javadoc/persism1/index.html)

[Code Coverage For Version 2.x](/coverage/persism2/ns-1/index.html) - [Code Coverage For Version 1.x](/coverage/persism1/index.html)

[Release Notes](/release-notes.md)

### Other documentation

[Getting Started](/manual2.md)

[Cookbook: Implementing Persistable interface](cookbook-persistable.md)

[All about Records](records.md)

[SELECT N+1 Problem?](n+1.md)

[How to use the new @Join Annotation](join.md)

Supported Databases

![MSSQL](img/mssql.png) ![MySQL](img/mysql.png) ![Derby](img/derby.png) ![Firebird](img/firebird.png) ![H2](img/h2.png) ![HQSLDB](img/hsqldb.jpg) ![Oracle](img/oracle.png) ![PostgreSQL](img/postgresql.png) ![SQLite](img/sqlite.png) ![UCanAccess](img/ucanaccess.png) ![Informix](img/informix.jpg)
