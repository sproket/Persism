Persism is a wood simple, auto discovery, auto configuration, and convention over configuration ORM (Object Relational Mapping) library for Java.

## Simple

The API for Persism is small. There are 2 primary classes: The Query for reading things from your database and the Command for updating things in your database. See the manual for details.

## Auto-Discovery
Persism figures things out for you. Create a table, write a JavaBean, run a query. Persism uses simple mapping rules to find your table and column names and only requires an annotation where it can’t find a match.

## Convention over configuration
Persism requires no configuration. Drop the JAR into your project and go.

Persism has annotations though they are only needed where something is outside the conventions. In most cases you probably don't even need them.

Persism can usually detect the table and column mappings including primary/generated keys and columns with defaults.

## Smart
Persism will do the correct thing by default. Persism understands that your class is called Customer and your table is called CUSTOMERS. It understands that your table column is CUSTOMER_ID and your property is customerId. Persism gets it. Heck Persism even understands when your class is called Category and your table is called CATEGORIES. No problem. Don’t even bother annotating that stuff. Persism uses annotations as a fall back – annotate only when something is outside the conventions.

## Tiny
Persism is under 50k. Yeah, fit it on a floppy if you want. Persism has Zero dependencies however it will utilize logging based on whatever is available at runtime - SLF4J, LOG4J or JUL.

## Unobtrusive
Persism gets out of your way.  The philosophy of Persism is to be the library that you almost don’t even know your using.

[MANUAL](/manual.md)

[Javadoc](/javadoc/index.html)

[Code Coverage](/coverage/index.html)



