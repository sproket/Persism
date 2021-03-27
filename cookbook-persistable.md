## Cookbook: Implementing Persistable interface

The purpose of the Persistable (or PersistableObject abstract class) is to allow Persism to be able to detect what 
changes occur in your POJOs to only include changed columns in the SQL update statements. This can be important for 
database concurrency when you have a large numbers of users.

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

We'll fetch one from our databse and change the company name.

```
Contact contact = new Contact();
contact.setId(1);
session.fetch(contact);

contact.setCompany("Cyberdyne Systems");

session.update(contact);
```

This would produce the following update statement in the database:

```
UPDATE Contacts SET 
    FIRST_NAME = 'Fred', 
    LAST_NAME = 'Flintstone', 
    COMPANY = 'Cyberdyne Systems', 
    EMAIL = 'fred@cyberdyne.com' 
    WHERE ID = 1       
```
This is fine but since we only changed the Company column, really what we want is to have only the changed 
columns in the update statement.

Like this:

```
UPDATE Contacts SET 
    COMPANY = 'Cyberdyne Systems' 
    WHERE ID = 1       
```
When your database only updates the changed columns it will perform better with more users and reduce the chances of blocking.

### 2 ways to achieve this with Persism

#### Method 1

Inherit PersistableObject

```
public class Contact extends PersistableObject<Contact> {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..
}
```
This is the simpler way to achieve this. You may use the genric form but the non-generic form also works 
(but you'll get *"Raw use of parameterized class 'PersistableObject'"* warnings from Java)

Sometimes you may not want to or be able to use inheritance. In that case you can use the Persistable interface.

#### Method 2

Implement Persistable

**Note:** The Persistable interface was changed in version 1.1.0. The method getOriginalValue was changed to readOriginalValue 
to avoid confusion that "originalValue" was a property with a getter. If you're using an older release, replace
readOriginalValue with getOriginalValue in the examples below.

Step 1

```
public class Contact implements Persistable<Contact> {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..
}
```
Step 2

For the interface you'll need to implement the 2 methods: *saveReadState* - which is used to keep a copy of your POJO 
in its initial state and *readOriginalValue* - which returns this object to Persism to compare to at appropriate times.

```
public class Contact implements Persistable<Contact> {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..

    @Override
    public Contact readOriginalValue() {
    }
    
    @Override
    public void saveReadState() throws PersismException {
    }
}
```

To implement *readOriginalValue* you'll need a field to store the POJO instance. It can be any name you want but make sure 
to use the *@NotColumn* annotation on the field to prevent "Contact was not properly initialized. Some properties not 
initialized in the queried columns" exception.

```
public class Contact implements Persistable<Contact> {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..

    @NotColumn
    private Contact somethingToStoreOriginalState;

    @Override
    public Contact readOriginalValue() {
        return somethingToStoreOriginalState;
    }

    @Override
    public void saveReadState() throws PersismException {
    }   
}
```

To implement *saveReadState* the Persistable interface extends Cloneable which is an easy way 
to copy a POJO though you may also use other ways of doing that if you want to. 

```
public class Contact implements Persistable<Contact> {

    private int id;
    private String firstname;
    private String lastname;
    private String company;
    private String email;
    
    // Getters and Setters etc..

    @NotColumn
    private Contact somethingToStoreOriginalState;

    @Override
    public Contact readOriginalValue() {
        return somethingToStoreOriginalState;
    }

    @Override
    public void saveReadState() throws PersismException {
        try {
            somethingToStoreOriginalState = (Contact) clone();
        } catch (CloneNotSupportedException e) {
            throw new PersismException(e.getMessage(), e);
        }
    }   
}
```

That's it!

To confirm that you see the more precise SQL update statements set your logger to log "net.sf.persism" 
at "debug" level.

Thanks