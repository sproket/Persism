<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>

    <?php include 'head.html' ?>
    <title>Persism Manual</title>

</head>

<body>

<div id="right-sidebar">
    <?php include 'news.html' ?>
</div>

<div id="container">

    <div id="menu">
        <?php include 'menu.html' ?>
    </div>

    <h1>Persism Manual</h1>

    <p></p>

    <h2>Getting started</h2>

    <p>Download Persism here and add it your project.</p>

    <p></p>

    <h2>Using the Query Object</h2>

    <p></p>

    <p>Persisim uses a standard Connection object so all you need to do is Create
        the object passing in the Connection.</p>
    <pre>Query query = new Query(connection);</pre>

    <p>With the query object you can then run queries to retrieve lists of
        objects:</p>

    <p></p>
    <pre>List&lt;Customer&gt; list = query.readList(Customer.class,"select * from Customers where name = ?", "Fred");</pre>

    <p></p>

    <p>Note that generics are in place here. If you try reading a list of a
        mismatched type, you'll get a compiler error. Note also that the query follows
        the best practice of using parametrized queryes. You can specify any number of
        parameters you like and you can also use stored procedures instead of query
        strings.</p>

    <p></p>

    <p>You can also read a single object with a query string like this:</p>

    <p></p>
    <pre>Customer customer = new Customer(); query.readObject(customer, "select * from Customers where name = ?", "Fred");</pre>

    <p></p>

    <p>This method returns true if the customer was found. Note you do this by
        pre-instantiating your object first. This allows you to control memory usage of
        your objects and you can re-use the same object if you need to run multiple
        queries.</p>

    <p></p>

    <p>You can also quickly initialize an Object from the database by specifying
        the Object's primary key. This way you do not need any SQL statement.</p>

    <p></p>
<pre>Customer customer = new Customer();
customer.setCustomerId(123); query.readObject(customer);</pre>

    <p></p>

    <p>Again this method returns true to indicate the object was found and
        initialized.</p>

    <p>The query object also contains methods to read primitive Java types by
        simply using them directly.</p>
    <pre>    String result = query.read(String.class, "select Name from Customers where ID = ?", 10);</pre>

    <p></p>

    <p>Objects may also be refreshed if you you have situations where an object is
        updated in the database by a different user.</p>
<pre>Customer customer = new Customer();
customer.setCustomerId(123);
query.readObject(customer);
customer.setSomething("vlah");
query.refreshObject(customer);</pre>

    <p></p>

    <h2>Using the Command Object</h2>

    <p>The Command object is similar to the Query in that it uses a standard
        Connection for its constructor but unlike the Query, The Command object is used
        for performing updates to the database (updates, inserts and deletes).</p>
    <pre>Command command = new Command(connection);</pre>

    <p>To perform an operation simply use the appropriate method.</p>
<pre>Customer customer = new Customer();
customer.setCustomerId(123);
customer.setCustomerName("Fred");
customer.setAddress("123 Sesame Street");

command.insert(customer);

customer.setCustomerName("Barney");
command.update(customer);

command.delete(customer);</pre>

    <p>Persism will use the primary keys for the <code>update</code> and
        <code>delete</code> methods and will set the primary keys for you if they are
        generated when you do an <code>insert</code>. Persism will usually
        auto-discover the primary keys so you usually do not have to specify them in
        your POJO. Persisms will also set defaults to properites if they were not
        defined and there's a default defined for that mapped column in the
        database.</p>

    <h2>Writing Data Objects (POJOs)</h2>

    <p></p>

    <p>Persism follows the usual JavaBean convention for data objects. In most
        situations a simple POJO class is all you will need.</p>

    <p>** Example Customer **</p>

    <p>Persism uses annotation in situations where it can't discover the mapping
        for you.</p>

    <p>Persism uses the following annotations for the class:</p>

    <p>Table - used to specify the table name in the database.</p>

    <p>Query - used to specify that this class represents the result of a query. Ie
        there is no single table associated with it.</p>

    <p>Persisms uses the following annotations for properties:</p>

    <p>Column - used to specify the column name and whether or not the column is
        primary and/or generated. The 3 parameters are optional.</p>

    <p>NoColumn - used to specify that this property has no matching column. Ie
        that it's a calculated value and not read from the database.</p>

    <p>You can specify these annotations on the class field or on the getter or
        setter. Note that NoColumn is not required if your property has a getter only.
        Persism understands that a read-only property would not be in the database.</p>

    <h2>Mapping Rules for Tables</h2>

    <p>Persism uses the following rules to discover the table name in the database
        for your class. (All case insensitive)</p>

    <p>1 - Looks for a match on class name </p>

    <p>2 - Looks for a match on plural class name</p>

    <p>3 - Looks for a single table containing class name</p>

    <p>4 - Looks for a single table containg plural class name</p>

    <p>So for a class called Customer, the following would all be valid table names
        that Persism would discover.</p>

    <p>CUSTOMER</p>

    <p>CUSTOMERS</p>

    <p>TBL_CUSTOMER</p>

    <p>CUSTOMERS_TABLE</p>

    <p>Note also that Persism is smart enough to understand multi-name tables for
        example the class OrderDetail and OrderDetails would match "Order Details" in
        the database.</p>

    <h2>Mapping Rules for Columns</h2>

    <p>Persism matches the property names to the column names ignoring case and
        ignoring special characters like space and underscore.</p>

    <p>So for example a property called customerId would match:</p>

    <p>Customer_ID</p>

    <p>Customer ID</p>

    <p>CustomerID</p>

    <p>In the database.</p>

</body>
</html>