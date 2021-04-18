## Using Records with Persism

Records are an exiting new feature of Java 16 (previewed in 14 and 15) that allow us to write simple immutable 
data classes with little ceremony. 

Let's look at some examples:

Suppose we have some query we want to get results for: 
```
SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, 
        o.Date_Paid, o.Created AS DateCreated, o.PAID      
    FROM Orders o
    JOIN Customers c ON o.Customer_ID = c.Customer_ID
```

We can write a record now to represent this result. 
```
@NotTable
public record CustomerOrder(String customerId, 
                            String companyName, 
                            String description, 
                            long orderId,
                            LocalDateTime dateCreated, 
                            Date datePaid, 
                            boolean paid) {
}
```
That's all there is. A record can be this simple. Persism treats this exactly the same a normal POJO so...

```
Connection con = DriverManager.getConnection(...);
...

// Instantiate a Persism session object with the connection
Session session = new Session(con);

// Get a list of CustomerOrder records.
List<CustomerOrder> results = session.query(CustomerOrderRec.class, sql);
```
Note that neither the order of the queried columns, nor the order of the record constructor matter. Pesism will 
match up the columns to the record properties.

**Warning:** Although records support multiple constructors Persism will not detect these other constructors 
unless you compile with```-parameters```. By default, Java does not compile in parameter names but does include 
them for records but only for the primary constructor. 



