## ![](img/logo2.png)  Using Persism with Spring


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
