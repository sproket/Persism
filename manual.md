## Getting started

Download Persism [here](https://github.com/sproket/Persism/releases) and add it your project.

If you are using Maven:
```
<dependency>
    <groupId>io.github.sproket</groupId>
    <artifactId>persism</artifactId>
    <version>1.0.1</version>
</dependency>
```

Persism uses a standard Connection object so all you need to do is create the Session object 
passing in the Connection.

Here's a common method to do this:
```
Properties props = new Properties();
props.load(getClass().getResourceAsStream("/mydb.properties"));

String driver = props.getProperty("database.driver");
String url = props.getProperty("database.url");
String username = props.getProperty("database.username");
String password = props.getProperty("database.password");

Class.forName(driver);

Connection con = DriverManager.getConnection(url, username, password);

// Instantiate a Persism session object with the connection
Session session = new Session(con);
```

## Querying Data

With the session object you can then run queries to retrieve lists of objects:
```
List<Customer> list = session.query(Customer.class,"select * from Customers where name = ?", "Fred");
// or
List<Customer> list = session.query(Customer.class, "sp_FindCustomers(?)", "Fred");
```

**Note:** Generics are in place here. If you try querying for a list of a mismatched type, 
you'll get a compiler error. Note also that the query follows the best practice of using 
parameterized queries, and you can also use stored procedures instead of query strings.

You can also read a single object with a query string like this:

```
Customer customer;
customer = session.fetch(Customer.class, "select * from Customers where name = ?", "Fred");
// or   customer = session.fetch(Customer.class, "sp_FindCustomer(?)", "Fred");
if (customer != null) {
    // etc...
}
```
This method returns null if the customer was not found.

You can also quickly initialize an Object from the database by specifying the Object's primary key. This way you do not need any SQL statement.

```
Customer customer = new Customer();
customer.setCustomerId(123);
if (session.fetch(customer)) {
    // customer found
} 
```
This method returns true to indicate the object was found and initialized. Note you do this 
by pre-instantiating your object first. This allows you to control memory usage of your objects, 
so you can re-use the same object if you need to run multiple queries.

The query can also return primitive Java types by simply using them directly.
```
String result = session.fetch(String.class, "select Name from Customers where ID = ?", 10);

int count = session.fetch(int.class, "select count(*) from Customers where Region = ?", Region.West);
// Note Enums are supported 

List<String> names = session.query(String.class, "select Name from Customers Order By Name");
```

**Note:** Use the query method for lists, and the fetch method for single results.

## Updating Data

With the session object you can perform inserts, updates and deletes with data objects.

To perform an operation simply use the appropriate method.
### Insert
```
Customer customer = new Customer();
customer.setCustomerId(123);
customer.setCustomerName("Fred");
customer.setAddress("123 Sesame Street");

session.insert(customer); // insert new Customer

// or with autoinc
Customer customer = new Customer();
customer.setCustomerName("Fred");
customer.setAddress("123 Sesame Street");

session.insert(customer); // insert new Customer

// Inserted and new autoinc value assigned 
assert customer.getCustomerId() > 0
```
**Note:**  Persism detects if a String is longer than the specified width in the database and will trim it 
before inserting into the DB - avoiding a DB Truncation error. Persism will log a warning when that occurs. 

### Update
```
customer.setCustomerName("Barney");
sesion.update(customer); // Update Customer   
```

**Note:** If your POJO extends *PersistableObject* or implements *Persistable* then only the changed columns will 
be used in the update statement.

**Note:**  Persism detects if a String is longer than the specified width in the database and will trim it
before updating the DB - avoiding a DB Truncation error. Persism will log a warning when that occurs.

### Delete
```
session.delete(customer); // Delete Customer
```

Persism will use the primary keys for the update and delete methods and will set the primary key 
for you if it's an autoincrement when you do an insert. 

### AutoCloseable

Session implements AutoCloseable so if you're using connection pooling you 
can use this form:

```
try (Session session = new Session(dataSource.getConnection())) {
  customer.setCustomerName("Barney");
  sesion.update(customer); 
  ... etc ...   
}
```

## Defaults and Primary Keys

Persism will usually discover primary keys, so you usually do not have to specify them 
in your POJO with Annotations. Persism will also set defaults to properties if they were 
not set and there's a default defined for that mapped column in the database.

**Note:** Wherever you have defaults you should not use primitve types since there's no way to 
detect NULL. Best practice is to use Object types for these cases.

## Writing Data Objects (POJOs)

Persism follows the usual JavaBean convention for data objects exactly as you would define Entity type objects
with JPA - though you won't need all those annotations which seem to be required with that framework. ;)

Examples come from [Northwind and Pubs Databases](https://docs.microsoft.com/en-us/dotnet/framework/data/adonet/sql/linq/downloading-sample-databases)

Let's take the Categories table from Northwind:

![](img/nwcategories.png)

Here's the class for this:
```
public class Category {

    private int categoryId;
    private String categoryName;
    private String description;
    private byte[] picture;

    // Getters and Setters etc..
}
```

**Note:** You don't need any annotation in this case. Persism will match columns to property names ignoring 
case or spaces or underscores in the column names - and it will also match up the table name handling the 
singular/plural naming as well.  

This would work fine if the table was called "Category" or "CATEGORY" or "CATEGORIES". You'd only need an 
annotation if the table had an unusual name.    

**Note:** The binary Picture (Blob) column maps into byte array. This works fine for both reading 
and writing back to the database.

Here's another example from Northwind:

![](img/nworderdetails.png)

Here's the class for this:

```
public class OrderDetail {

    private int orderId;
    private int productId;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal discount;

    // Getters and Setters etc..  
}
```

**Note:** You don't need to annotate for the table name even though it has a space in it.

You could also specify your types with doubles or floats like this:

```
public class OrderDetail {

    private int orderId;
    private int productId;
    private double unitPrice;
    private int quantity;
    private double discount;

    // Getters and Setters etc..  
}
```

In this situation, Persism will log a warning if it needs to 'downcast' because the database may return a larger type than 
you specified in your class.

```
WARN - Possible overflow column UnitPrice - Property is Double and column value is BigDecimal
```

## Annotations

Persism uses annotation in situations where it can't discover the mapping for you.

Let's look at the PUBS database for this:

![](img/pubsauthors.png)

Hmm, some funny names here. Here's a class for that:

```
@Table("authors") // not really required in this case
public class Author {

    @Column(name = "aU_iD") // Usually case won't matter for the annotation
    private String authorId;

    @Column(name = "au_LNAME")
    private String lastName;

    @Column(name = "au_FNAME")
    private String firstName;

    private String phone;
    private String address;
    private String city;
    private String state;

    @Column(name = "zIp")
    private String postalCode;

    private boolean contract;
    
    // Getters and Setters etc..
}
```

Persism uses the following annotations for Table which need to be specified on the Class:

- *Table* - used to specify the table name in the database.
- *NotTable* - used to specify that this class represents the result of a query - that there is no 
single table associated with it.

Persism uses the following annotations for Columns.

- *Column* - used to specify the column name and whether the column is primary, autoincrement or has a default. 
The 3 parameters are optional.
- *NotColumn* - used to specify that this property has no matching column. Ie that it's a calculated value and not read from the database.

**Note:** These annotations can be specified on the field or on the getter or setter. 

**Note:** *NotColumn* is not required if your property has a getter only. Persism understands that a 
read-only property would not be in the database.


## Support for hierarchical objects
A somewhat common but misguided way to model data is to have POJO type objects inside other POJOs.

example:
```
public class Customer {
    private String customerId;
    private String companyName;
    private String contactName;
    private String contactTitle;
    // etc...
    
    private List<Order> orders;
    
    // Getters and Setters etc...
}

public class Order {
    private int orderId;
    private String customerId;
    private Date orderDate;
    private Date requiredDate;
    private Date shippedDate;

    private List<OrderDetail> details;
    
    // Getters and Setters etc...
}
```
This might seem like a logical approach, but it is often very expensive for the database because it may
need to query for all the orders and all the line items and product details etc every time you just want
a list of Customers.

Persism can support this, but it's left up to you on when you want to read the data. A better approach 
is to write a class representing the columns returned from a joining query and use the *@NotTable* annotation. 
You'll usually see much better performance.

This is how you can do it though if you need to:
```
public class Customer {
    private String customerId;
    private String companyName;
    private String contactName;
    private String contactTitle;
    // etc...
    
    @NotColumn
    private List<Order> orders;
    
    // Getters and Setters etc...
}

public class Order {
    private int orderId;
    private String customerId;
    private Date orderDate;
    private Date requiredDate;
    private Date shippedDate;

    @NotColumn
    private List<OrderDetail> details;
    
    // Getters and Setters etc...
}
```
We annotate these as *@NotColumn* so they'll be ignored by the SQL query. Then you can define these yourself as required:
```
Customer customer;
List<Order> orders;
customer = session.fetch(Customer.class, "select * from Customers where name = ?", "Fred");
orders = session.query(Order.class, "select * from orders where customerId = ", customer.getCustomerId());
customer.setOrders(orders);
// etc...
```

## Java types to SQL types

| Java Type(s) | SQL Type(s)       | Notes |
| :-------------    | :----------:                  | :----------: |
|  boolean          | BIT, INT, SHORT, BYTE, NUMBER, CHAR(1)| Oracle doesn't have a bit so it reads number types as BigDecimal or Char(1) - 1 or '1' for true |
|  short, int, long     | INT, BIGINT, LONG, AUTOINCREMENT  | Any whole number maps fine but you may see downcast warnings |
|  float, double, BigDecimal    | NUMBER, REAL, FLOAT, DOUBLE  | Any floating point type maps fine  but you may see downcast warnings |
|  byte[]    | BLOB  | Binary large objects will be read as a byte array. **Do not** use Blob as a Java type in your POJO. Max size is 2147483647|
|  String  | CHAR, VARCHAR, NVARCHAR, TEXT, CLOB  | Large or small char types map to String.  **Do not** use Clob a Java type in your POJO. Max size is 2147483647|
|  enum  | VARCHAR and/or ENUM db type (PostgreSQL, MySQL and H2)  | Database Enum types can map to String or Enum in Java. Enum in Java can be stored as Enum (if supported) or Varchar  |
|  UUID  | VARCHAR, BINARY(16) or Native (PostgreSQL or MSSQL only)  | UUID types are read as String types and then converted |
|  sql.Timestamp, util.Date, LocalDateTime  | DATETIME / TIMESTAMP | DateTime types are generally read as sql.Timestamp and converted as appropriate |
|  sql.Date, LocalDate  | DATE | Usually used for DATE only types |
|  sql.Time, LocalTime  | TIME | Usually used for TIME only types (For Oracle you can use TIMESTAMP) |

**Note:** For Date related types it's recommended to use the new java.time types (LocalDate, LocalTime, LocalDateTime)
since they are immutable. 

**Note:** Although Java primitive types like int, float, double, boolean are fully supported you should use 
the Object types if you need to support *NULL*. It's especially important if you have defaults in
your database since primitive types can never detect not being set.


## Unsupported SQL Types

The following is a list of SQL types defined in ```java.sql.Types``` which are currently not supported.

```
OTHER       = 1111 (supported but no type checking is done)
JAVA_OBJECT = 2000
DISTINCT    = 2001
STRUCT      = 2002
ARRAY       = 2003
REF         = 2006
DATALINK    = 70
ROWID       = -8
SQLXML      = 2009

- Microsoft specific DateTimeOffset ( -155 )
```

## Warning and Error Messages 

### Warnings

**Column is annotated as autoIncrement but it is not Long or Integer type - Ignoring.**

> Occurs if you happen to annotate a String or other type as an autoincrement value.

**Unknown connection type. Please contact Persism to add support.**

> Occurs if you are using an unknown JDBC connection - Persism should work fine as long as it's JDBC compliant. 
Ping me - I'll add it and add some unit tests.  

**Property not found for column 'X' on class 'Z'.**

> Occurs if you have a column in your database table where you have no associated property.

**No primary key found for table. Do not use with update/delete/fetch or add a primary key.**

> Occurs in cases where Persism detects a table with no primary key. 
> This kind of table could only be used by Persism for querying. 

**TRUNCATION with Column: 'column name' for table: 'table name'.**

> Occurs if you have a String value too wide for the associated column in the database.

**Column type not known for SQL type** 

> Occurs when querying data where the SQL type read is not defined in ```java.sql.Types```. 
It will be treated by Persim as Object type.   

**Conversion: Unknown Persism type 'class name' - no conversion performed.**

> Occurs in cases where Persism doesn't know about a type defined in ```java.sql.Types```. 
> See [unsupported types](#unsupported-sql-types)


**Property X for column Y should be an Object type to properly detect NULL for defaults (change it from the primitive type to its Boxed version).**

> This occurs if you have a default on a column in your database but you use a primitive (int, float, double, etc) for 
> the property on your POJO class. You should use the boxed version (Integer, Float, Double, etc) in order to detect NULL.
> Otherwise the default would never be set. 


### Errors

Below is the list of specific Exceptions Persism may throw.

**Could not determine a table for type: 'POJO class name' Guesses were: 'list of guesses'** 

> This occurs when Persism cannot determine the table name in the database from the POJO class name. 
You can resolve this by adding an annotation to specify the table name.  


**Cannot perform UPDATE/FETCH/DELETE - 'table name' has no primary keys.**

> This occurs when Persism is attempting an operation on the database that requires a primary be defined for the table.

**Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert.**

> This occurs if you INSERT and have a String type (CHAR or UUID etc) as a primary and you are attempting to assign it 
from a default in the database. Currently, retrieving this value back from the database in not supported by JDBC. 
It is possible to do this in database specific ways but not possible with some databases. To resolve this make sure to 
assign your primary keys values from Java in these cases. **Note:** This only works with PostgreSQL. 


**Object 'POJO class name' was not properly initialized. Some properties not initialized in the queried columns ('list of missing columns')**
 
> Persism throws this exception because your POJO would not be properly initialized if you miss some columns in your query.
This could cause NullPointerExceptions in your code. Either include all columns (or use ```SELECT *```) or annotate 
the property in you class with *@NotColumn*.

**Parse Exceptions** 

> This can occur in specific cases where the JDBC returns a Date type as a String. The format used to convert to 
a date type is ```yyyy-MM-dd hh:mm:ss``` for DateTime types and ```yyyy-MM-dd``` for Date types.


**Note:** Persism may log errors when cleaning up ResultSet or Statement objects or when rolling back an SQL transaction.


## Logging

Here's an example logback configuration for logging with Persism:

```
<?xml version="1.0" encoding="UTF-8"?>

<!-- For assistance related to logback-translator or configuration  -->
<!-- files in general, please contact the logback user mailing list -->
<!-- at http://www.qos.ch/mailman/listinfo/logback-user             -->
<!--                                                                -->
<!-- For professional support please see                            -->
<!--    http://www.qos.ch/shop/products/professionalSupport         -->
<!--                                                                -->
<configuration>
    <appender name="A1" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-5p %d{MMM dd HH:mm:ss} %c - %m%n</pattern>
        </encoder>
    </appender>
    <appender name="R" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!--See http://logback.qos.ch/manual/appenders.html#RollingFileAppender-->
        <!--and http://logback.qos.ch/manual/appenders.html#TimeBasedRollingPolicy-->
        <!--for further documentation-->
        <File>${user.home}/logs/persism.log</File>
        <encoder>
            <pattern>%-5p %d{MMM dd HH:mm:ss} %c - %m%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${user.home}/logs/persism.%d.log</fileNamePattern>
            <maxHistory>4</maxHistory>
        </rollingPolicy>
    </appender>
    <logger name="org" level="ERROR"/>
    <logger name="net" level="ERROR"/>
    <logger name="net.sf.persism" level="WARN"/>

    <!-- These can be used for JDBC level logging -->
    <!-- ERROR, WARN, INFO, DEBUG, OFF -->
    <logger name="jdbc.sqlonly" level="OFF"/>
    <logger name="jdbc.audit" level="OFF"/>
    <logger name="jdbc.sqltiming" level="OFF"/>
    <logger name="jdbc.connection" level="OFF"/>
    <logger name="jdbc.resultset" level="OFF"/>
    
    <root level="INFO">
        <appender-ref ref="A1"/>
        <appender-ref ref="R"/>
    </root>
</configuration>
```

## Known Issues

- No support for newer Timezone related date types yet
- No support for XML type yet  
- Boolean type columns using "Is" style names will require annotations 
- Generated primary keys only work with Autoincrement types. UUID/String types with generated 
  defaults do not return into the inserted object as primary keys.
- Singular/plural table name guessing does not work with words like Tax - Taxes, Fax - Faxes

## Special Thanks

Thanks to the various JDBC and database developers for helping to make this possible.

------------------------------------------

