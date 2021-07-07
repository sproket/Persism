#Persism

## New Type Safe API

### Query

The exisitng query API only had the non type-safe version

```
<T> List<T> query(Class<T> objectClass, String sql, Object... parameters)
```

This version still exists but is deprecated?

2 new helper classes have been added - SQL and Parameters. Both provide static helper method

#### SQL Class

blah blah blah

#### Parameter Class

blah bala


#### New Query Methods
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

