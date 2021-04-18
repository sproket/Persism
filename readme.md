
This version of Persism is for testing experimental features.

If you run ```mvn verify``` you can generate the javadoc - the main change 
is in the Session to support better overloads.

###Changes are:
* Insert returns```Result<T>``` containing int and changed Object to support records
* New wrapper classes for SQL and Parameters
* new fetch and query methods

**Note** these are breaking changes.

Feedback appreciated.

Thanks!


