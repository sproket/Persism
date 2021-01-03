<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>

    <?php include 'head.html' ?>
    <title>Persism</title>

</head>

<body>

<div id="right-sidebar">
    <?php include 'news.html' ?>
</div>

<div id="container">

    <div id="menu">
        <?php include 'menu.html' ?>
    </div>

<!--    <div style="color: #dc143c;">UNDER CONSTRUCTION</div>-->
    <br>

    <h1>Persistence Is Not Futile!</h1>

    <p> Persism is a wood simple, auto discovery, auto configuration, and convention over configuration ORM (Object
        Relational Mapping) library for Java.
    </p>

    <h2>Simple</h2>

    <p>
        The API for Persism is small. There are 2 primary classes: The Query for reading things from your database and the
        Command for updating things in your database. See the <a href="http://sourceforge.net/p/persism/wiki/Manual/">manual</a> for details.</p>

    <h2>Auto-Discovery</h2>

<!--    <p class="famousQuotes">Mappings? We don’t need no stinking mappings! – Tibetan Monk 15th century</p>-->

    <p>
        Persism figures things out for you. Create a table, write a JavaBean, run a query.
        Persism uses <a href="http://sourceforge.net/p/persism/wiki/MappingRules/" target="_blank">simple mapping rules</a>
        to find your table and column names and only requires an annotation where it can’t find a match.
    </p>


    <h2>Convention over configuration</h2>

<!--    <p class="famousQuotes">XML? We don’t need no stinking XML! – Tibetan Monk 16th century</p>-->

    <p>
        Persism requires no configuration. Drop the JAR into your project and go.
    </p>
    <p>
        Persism has annotations though they are only needed where something is outside the conventions. In most
        cases you probably don't even need them.
    </p>
    <p>
        Persism can usually detect the table and column mappings including primary/generated keys and columns with defaults.
    </p>

    <h2>
        Smart
    </h2>

<!--    <p class="famousQuotes">Something funny?</p>-->

    <p>
        Persism will do the correct thing by default. Persism understands that your class is called <tt>Customer</tt> and your
        table is called <tt>CUSTOMERS</tt>. It understands that your table column is <tt>CUSTOMER_ID</tt> and your
        property is <tt>customerId</tt>.
        Persism gets it. Heck Persism even understands when your class is called <tt>Category</tt> and your table is called
        <tt>CATEGORIES</tt>.
        No problem. Don’t even bother annotating that stuff. Persism uses annotations as a fall back – annotate only when
        something is outside the conventions.
    </p>

    <h2>Tiny</h2>

    <!--    <p class="famousQuotes">Something funny?</p>-->

    <p>
        Persism is under 50k. Yeah, fit it on a floppy if you want. Persism has <b>Zero</b> dependencies however it will
        utilize
        logging based on whatever is available at runtime - SLF4J, LOG4J or JUL.
    </p>


<!--
    <h2>Fast</h2>

    <p class="famousQuotes">Something funny?</p>

    <p>
        Compare with hibernate etc...
    </p>
-->
    <h2>
        Unobtrusive</h2>


<!--    <p class="famousQuotes">
        Get off my lawn &ndash; anonymous
    </p>
-->
    <p>
        Persism gets out of your way.&nbsp; The philosophy of Persism is to be the library that you almost don&rsquo;t even
        know your using.&nbsp;</p>


</div>

<div id="footer">
    <a href="http://sourceforge.net" style="border:none">
        <img src="http://sflogo.sourceforge.net/sflogo.php?group_id=13751;type=8" alt="Hosted at Sourceforge"/>
    </a>
</div>

</body>

</html>