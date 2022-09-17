## ![](img/logo2.png) Using Persism with Modules

Prior to version 2.2.0 Persism used the automatic module feature in maven. 
As of 2.2.0 Persism uses module-info.

```Java
module sproket.github.io.persism {
    requires java.sql;
    requires java.desktop;  
    requires java.logging; 

    requires static org.apache.logging.log4j;
    requires static log4j;
    requires static org.slf4j;

    exports net.sf.persism;
    exports net.sf.persism.annotations;
}
```

#### requires  java.sql

Used for jdbc access.

#### requires java.desktop

Optionally used for the @ConstructorProperties annotation for [Records](records.md) - 
This may be replaced by a Persism specific annotation to avoid the desktop 
dependency in the future.

#### requires java.logging

Optionally used as the fall back logger if the log4j or slf4j are not used.

#### requires org.apache.logging.log4j, log4j, org.slf4j

These are declared static as they are needed for compile time but not runtime. 
*Note:* The log4j (the old 1.x version) does not have a module name so the name derives
from the file name. This is usually not recommended. A future version of Persism will 
deprecate support for this older logging library.

#### exports net.sf.persism

Public classes available in Persism.

#### exports net.sf.persism.annotations

Public annotations available in Persism.

## Using Persism in a modularized application

Because Persism uses reflection, your application will need to declare the package where
your data models are defined as open.

Example:

```Java
module com.mycompany.myfxmlapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires sproket.github.io.persism;

    opens com.mycompany.myfxmlapp to javafx.fxml;
    opens com.mycompany.myfxmlapp.models to sproket.github.io.persism;

    exports com.mycompany.myfxmlapp;
}
```

