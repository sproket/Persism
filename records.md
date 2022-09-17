## ![](img/logo2.png)  Using Records with Persism

Records are an exiting new feature of Java 16 (previewed in 14 and 15) that allow us to write simple immutable 
data classes with little ceremony. 

### Reading records:

Suppose we have some query we want to get results for: 
```sql 
SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, 
        o.Date_Paid, o.Created AS DateCreated, o.PAID      
    FROM Orders o
    JOIN Customers c ON o.Customer_ID = c.Customer_ID
```

We can write a record now to represent this result. 
```java 
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
That's all there is to it. A record can be this simple. Persism treats this exactly the same a normal POJO except that
records are immutable. 

You can also supply other constructors for queries. Let's assume you have another query and want to use this same record
but maybe without the Date_Paid, DateCreated and PAID columns. OK.

```java 
@NotTable
public record CustomerOrder(String customerId, 
                            String companyName, 
                            String description, 
                            long orderId,
                            LocalDateTime dateCreated, 
                            Date datePaid, 
                            boolean paid) {
                            
    // Add another constructor
    @ConstructorProperties({"customerId", "companyName", "description", "orderId"})
    public CustomerOrderRec(String customerId, String companyName, String description, long orderId) {
        this(customerId, companyName, description, orderId, null, null, false);
    }
                           
}
```
Now this same record can be used with a different query:

```sql 
SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description 
    FROM Orders o
    JOIN Customers c ON o.Customer_ID = c.Customer_ID
```
Note in this case we need to supply the property name parameters because by default Java does not compile parameter names 
into the compiled class files. Use the ```@ConstructorProperties``` for this. This a standard annotation in the 
```java.beans``` package.  The other way to do this is to compile your project with "-parameters" but this is a 
non-standard compiler switch - it may not work on every jvm / platform.

Note that neither the order of the queried columns, nor the order of the record constructor matter. Persism will 
match up the columns to the record properties.

**Note:** You can't use the simple fetch method with a record since we expect a mutable object. If you try it 
you'll get ```"Cannot read a Record type object with this method."``` Exception.


## Using Records for Tables

### Insert records:

You can insert a record the same way you would with a normal POJO. The only thing that's different is that Persism
can't modify the original afterward if you had auto increment or other columns with defaults. The insert method now
returns a Result object containing the row change count (as before) and the updated record object.

```java 
Result<Invoice> result = session.insert(invoice);

assertTrue("rows s/b > 0", result.rows() > 0);
assertTrue("Invoice ID > 0", result.dataObject().invoiceId() > 0);
assertNotNull("Created s/b not null", result.dataObject().dateCreated());
```

### Update records:

Since you can't modify a record, if you want to make changes you need to instantiate a new record and call
the session update method as usual.
```java 
Invoice oldInvoice...

Invoice invoice = new Invoice(oldInvoice.id(), oldInvoice.custId(), etc....)

session.update(invoice);
```

You could also add a copy constructor to your record for this purpose.



### Delete records:

Delete works the same as with normal POJOs.

### Notes

The @Column and @NotColumn annotation are supported. Not sure why you'd need @NotColumn though.

Adding calculated fields is easy with Records!

```java 
public record OrderItem(int orderId, String description, int qty, double price) {

    // calculated field
    public double total() {
        return price * qty;
    }
}

```
