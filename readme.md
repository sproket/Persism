![](logo1.png) [Release notes](release-notes.md) 

# Welcome

Persism is a wood simple, auto discovery, auto configuration, and convention over configuration ORM (Object Relational Mapping) library for Java.

```
<dependency>
    <groupId>io.github.sproket</groupId>
    <artifactId>persism</artifactId>
    <version>1.1.0</version>
</dependency>
```
```
Connection con = DriverManager.getConnection(url, username, password);

// Instantiate a Persism session object with the connection
Session session = new Session(con);

List<Customer> list = session.query(Customer.class,"select * from Customers where name = ?", "Fred");
// or
List<Customer> list = session.query(Customer.class, "sp_FindCustomers(?)", "Fred");

Customer customer;
customer = session.fetch(Customer.class, "select * from Customers where name = ?", "Fred");
// or   customer = session.fetch(Customer.class, "sp_FindCustomer(?)", "Fred");
if (customer != null) {
    // etc...
}

// fetch an existing instance
Customer customer = new Customer();
customer.setCustomerId(123);
if (session.fetch(customer)) {
    // customer found and initialized
} 

// Supports basic types
String result = session.fetch(String.class, "select Name from Customers where ID = ?", 10);

// Fetch a count as an int - Enums are supported 
int count = session.fetch(int.class, "select count(*) from Customers where Region = ?", Region.West);

// Insert - get autoinc
Customer customer = new Customer();
customer.setCustomerName("Fred");
customer.setAddress("123 Sesame Street");

session.insert(customer); 

// Inserted and new autoinc value assigned 
assert customer.getCustomerId() > 0

// Updates
customer.setCustomerName("Barney");
sesion.update(customer); // Update Customer   
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

## Smart
Persism will do the correct thing by default. Persism understands that your class is called Customer and your table
is called CUSTOMERS. It understands that your table column is CUSTOMER_ID and your property is customerId. 

Persism understands when your class is called Category and your table is called CATEGORIES. 
No problem. No need to annotate for that. Persism uses annotations as a fall back – annotate only when 
something is outside the conventions.

## Tiny
Persism is under 70k. Yeah, fit it on a floppy if you want. Persism has Zero dependencies however it will 
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

