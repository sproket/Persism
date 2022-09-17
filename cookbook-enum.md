## ![](img/logo2.png)  Cookbook: Using Java Enums

Recently while  

Let's take an example:
```
public class Contact {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..
}
```

mysql and posgress support natively - you can match Java enum to those enums 

etc...
``` 
        commands.add("CREATE TABLE Customers ( " +
                " Customer_ID varchar(10) PRIMARY KEY NOT NULL, " +
                " Company_Name VARCHAR(30) NULL, " +
                " Contact_Name VARCHAR(30) NULL, " +
                " Contact_Title VARCHAR(10) NULL, " +
                " Address VARCHAR(40) NULL, " +
                " City VARCHAR(30) NULL, " +
                " Region ENUM('North', 'South', 'East', 'West'), " +

```

and in Java 
```java
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setContactName("Fred");
        customer.setRegion(Regions.East);
        customer.setStatus('1');
        customer.setAddress("123 Sesame Street");
```