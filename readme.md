![](logo1.png) [Release notes 2.1.0](release-notes.md) -- [Getting Started Guide](https://sproket.github.io/Persism/manual2.html)

[Table joins are now supported!](https://sproket.github.io/Persism/join.html)

# Welcome

Persism is a wood simple, auto discovery, auto-configuration, and convention
over configuration ORM (Object Relational Mapping) library for Java 17 or later.

For Java 8 see the 1.x branch https://github.com/sproket/Persism/tree/persism1

```xml 
<dependency>
    <groupId>io.github.sproket</groupId>
    <artifactId>persism</artifactId>
    <version>2.1.0</version>
</dependency>
```
```Java 
import static net.sf.persism.Parameters.*;
import static net.sf.persism.SQL.*;

Connection con = DriverManager.getConnection(url, username, password);

// Instantiate a Persism session object with the connection
Session session = new Session(con);

List<Customer> list = session.query(Customer.class, sql("select * from Customers where CUST_NAME = ?"), params("Fred"));
// or
List<Customer> list = session.query(Customer.class, proc("sp_FindCustomers(?)"), params("Fred"));

Customer customer;
customer = session.fetch(Customer.class, sql("select * from Customers where CUST_NAME = ?"), params("Fred"));
// or   
customer = session.fetch(Customer.class, proc("sp_FindCustomer(?)"), params("Fred"));
if (customer != null) {
    // etc...
}

// You don't need the SELECT parts for Views or Tables
List<Customer> list = session.query(Customer.class, where("CUST_NAME = ?"), params("Fred"));

// You can reference the property names instead of the column names - just use :propertyName 
List<Customer> list = session.query(Customer.class, where(":name = ?"), params("Fred"));

// Order by is also supported with where() method
List<Customer> list = session.query(Customer.class, where(":name = ? ORDER BY :lastUpdated"), params("Fred"));

// Named parameters are also supported - just use @name
SQL sql = where("(:firstname = @name OR :company = @name) and :lastname = @last");
customer = session.fetch(Customer.class, sql, params(Map.of("name", "Fred", "last", "Flintstone")));

// fetch an existing instance
Customer customer = new Customer();
customer.setCustomerId(123);
if (session.fetch(customer)) {
    // customer found and initialized
} 

// Supports basic types
String result = session.fetch(String.class, sql("select Name from Customers where ID = ?"), params(10));

// Fetch a count as an int - Enums are supported 
int count = session.fetch(int.class, sql("select count(*) from Customers where Region = ?"), params(Region.West));

// Insert - get autoinc
Customer customer = new Customer();
customer.setCustomerName("Fred");
customer.setAddress("123 Sesame Street");

session.insert(customer); 

// Inserted and new autoinc value assigned 
assert customer.getCustomerId() > 0

// Update
customer.setCustomerName("Barney");
sesion.update(customer); // Update Customer   

// Delete
session.delete(customer);

// Handles transactions
session.withTransaction(() -> {
    Contact contact = getContactFromSomewhere();
    contact.setIdentity(randomUUID);
    session.insert(contact);
    
    contact.setContactName("Wilma Flintstone");
    
    session.update(contact);
    session.fetch(contact);
});
```
 

## Simple

The API for Persism is small. Mostly you just need a Connection and a Persism Session object, and you're good to go.
Your POJOs can have optional annotations for table and column names and can optionally implement a Persistable interface
for where you need to track changes to properties for UPDATE statements.

## Auto-Discovery
Persism figures things out for you. Create a table, write a JavaBean, run a query. Persism uses simple mapping rules to
find your table and column names and only requires an annotation where it can’t find a match.

## Convention over configuration
Persism requires no special configuration. Drop the JAR into your project and go.

Persism has annotations though they are only needed where something is outside the conventions. In most cases
you probably don't even need them.

Persism can usually detect the table and column mappings for you including primary/generated keys and columns
with defaults.

## Supports most common databases
Derby, Firebird, H2, HSQLDB, Informix, MSAccess, MSSQL, MySQL/MariaDB, Oracle (12+), PostgreSQL, SQLite.

## Smart
Persism will do the correct thing by default. Persism understands that your class is called Customer and your table
is called CUSTOMERS. It understands that your table column is CUSTOMER_ID and your property is customerId.

Persism understands when your class is called Category and your table is called CATEGORIES.
No problem. No need to annotate for that. Persism uses annotations as a fall back – annotate only when
something is outside the conventions.

## Tiny
Persism is under 100k. Yeah, fit it on a floppy if you want. Persism has Zero dependencies however it will
utilize logging based on whatever is available at runtime - SLF4J, LOG4J or JUL.

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

