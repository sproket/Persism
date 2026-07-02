## ![](img/logo2.png) InitializeEvent interface

InitializeEvent provides a hook method which is called by Persism whenever it reads an instance
of a POJO class from the database.

InitializeEvent will call onInitialized method once the POJO has been fully initialized by Perism (including joins).

### Example Usage

Let's say we have a relation setup between customer and invoices but invoices need to access the customer.

We could try this first:

```java
public final class Customer {
    private String customerId;
    private String customerName;
    private String region;
    private String country;

    @Join(to = Invoice.class, onProperties = "customerId", toProperties = "customerId")
    private List<Invoice> invoices = new ArrayList<>();
}

public final class Invoice {
    private int invoiceId;
    private String customerId;
    private Date invoiceDate;
    private double total;

    @Join(to = Customer.class, onProperties = "customerId", toProperties = "customerId")
    private Customer parent;
} 
```

This seems reasonable but if you run it you'll get an infinite loop!
Not a great idea.  Instead, we can do this:

```java
public final class Customer implements InitializeEvent {
    private String customerId;
    private String customerName;
    private String region;
    private String country;

    @Join(to = Invoice.class, onProperties = "customerId", toProperties = "customerId")
    private List<Invoice> invoices = new ArrayList<>();

    @Override
    public void onInitialized() {
        System.out.println("onInitialized " + this);
        // At this point everything is initialized
        // Here we can set a relationship
        for (Invoice invoice : invoices) {
            invoice.setParent(this);
        }
    }    
}

public final class Invoice {
    private int invoiceId;
    private String customerId;
    private Date invoiceDate;
    private double total;

    @NotColumn
    private Customer parent;

    public Customer getParent() {
        return parent;
    }

    public void setParent(Customer parent) {
        this.parent = parent;
    }
} 
```

