#Persism 2.0 - New Type Safe API

## Query and Fetch 

The existing query API only had the non type-safe version

```
<T> List<T> query(Class<T> objectClass, String sql, Object... parameters)
```

This version still exists but is deprecated?

2 new helper classes have been added - SQL and Parameters. Both provide static helper method


### New Query Methods
```
<T> List<T> query(Class<T> objectClass)
```

Query to return all results. Useful for small tables.


```
<T> List<T> query(Class<T> objectClass, Parameters parameters)
```

Query to return any results matching the primary key values provided.

``` 
<T> List<T> query(Class<T> objectClass, SQL sql)
```

Query for any arbitrary SQL statement.

```  
<T> List<T> query(Class<T> objectClass, SQL sql, Parameters parameters)
```

Query for a list of objects of the specified class using the specified SQL query and parameters.

## SQL Class

blah blah blah

## Parameter Class

Parameters can be specified as a list or as a set of named parameters.

### Named parameters

