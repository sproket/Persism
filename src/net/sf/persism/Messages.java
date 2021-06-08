package net.sf.persism;

enum Messages {
    // todo that verify each occurs with code coverage (some additional tests required)

    // Errors
    PrimaryKeysDontExist("executeQuery: Primary keys don't exist in a Query. Please use params() rather than keys()"),
    ObjectNotProperlyInitialized("Object %s was not properly initialized. Some properties not initialized in the queried columns (%s)."),
    ObjectNotProperlyInitializedByQuery("Object %s was not properly initialized. Some properties not initialized by the queried columns: %s  Missing: %s"),
    IllegalArgumentReadingColumn("Illegal Argument occurred setting property: %s. Object %s. Column: %s Type of property: %s - Type read: %s VALUE: %s"),
    NumberFormatException("NumberFormatException: Column: %s Type of property: %s - Type read: %s VALUE: %s"),
    DateFormatException("%s. Column: %s Target Conversion: %s - Type read: %s VALUE: %s"),
    ReadRecordColumnNotFound("readRecord: Could not find column in the SQL query for class: %s. Missing column: %s"),
    ReadRecordCouldNotInstantiate("readRecord: Could instantiate the constructor for: %s params: %s (%s)"),
    TableHasNoPrimaryKeys("Cannot perform %s - %s has no primary keys."),
    TableHasNoPrimaryKeysForWhere("Could not determine WHERE clause for %s. No primary keys detected."),
    ClassHasNoGetterForProperty("Class %s has no getter for property %s"),
    NonAutoIncGeneratedNotSupported("Non-auto inc generated primary keys are not supported. Please assign your primary key value before performing an insert."),
    CannotReadThisType("Cannot read a %s type object with this method."),
    CouldNotFindTableNameInTheDatabase("Could not find a Table in the database named %s. Check the @Table annotation on %s"),
    CouldNotFindViewNameInTheDatabase("Could not find a View in the database named %s. Check the @View annotation on %s"),
    CouldNotDetermineTableOrViewForType("Could not determine a %s for type: %s Guesses were: %s"),
    CouldNotDetermineTableOrViewForTypeMultipleMatches("Could not determine a %s for type: %s Guesses were: %s and we found multiple matching: %s"),
    CouldNotFindConstructorForRecord("findConstructor: Could not find a constructor for class: %s properties: %s"),
    OperationNotSupportedForView("%s operation not supported for Views."),
    WhereNotSupportedForNotTableQueries("WHERE clause not supported for Queries (using @NotTable). If this is a View annotate the class as @View"),

    // WARNINGS
    UnknownConnectionType("Unknown connection type. Please contact Persism to add support for %s"),
    NoPropertyFoundForColumn("No property found for column: %s class: %s"),
    ColumnAnnotatedAsAutoIncButNAN("Column %s is annotated as auto-increment but it is not a number type (%s)"),
    DatabaseMetaDataCouldNotFindPrimaryKeys("DatabaseMetaData could not find primary keys for table %s"),
    DatabaseMetaDataCouldNotFindColumns("DatabaseMetaData could not find columns for table %s!"),
    NoPrimaryKeyFoundForTable("No primary key found for table %s. Do not use with update/delete/fetch or add a primary key"),
    NoConversionForUnknownType("Conversion: Unknown type: %s - no conversion performed."),
    TinyIntMSSQL("COLUMN: %s: MSSQL Sees tinyint as 0 - 254 - Others -127 - +127 - no conversion performed - recommend changing it to SMALLINT/Short"),
    PossibleOverflow("Possible overflow column %s - Target type is %s and Value type is %s"),
    PropertyShouldBeAnObjectType("Property %s for column %s for class %s should be an Object type to properly detect NULL for defaults (change it from the primitive type to its Boxed version)."),
    SettersFoundInReadOnlyObject("Setters found in read only object %s %s"),
    UnknownSQLType("Unknown SQL TYPE: %s"),
    ConvertorValueTypeNotYetSupported("%s not yet supported"),
    ConvertorDoNotUseClobOrBlobAsAPropertyType("Usually you should not use blob or clob as a property type on a POJO. Blob maps to byteArray, Clob maps to String"),
    ColumnTypeNotKnownForSQLType("Column type not known for SQL type %s. Reading as Object"),
    InappropriateMethodUsedForSQLTypeInstance("%s It seems you're using the %s method with %s. You might prefer to use the %s method instead for better 'Find Usages'"),
    UnknownTypeForPrimaryGeneratedKey("Unknown type for primary/generated key: %s. using getObject."),
    UnknownTypeInSetParameters("setParameters: Unknown type: %s"),
    UnSupportedTypeInSetParameters("setParameters: %s not supported yet. We're probably about to fail....."),
    ParametersDoNotUseClobOrBlob("Usually you should not use blob or clob as an SQL parameter type. Blob maps to byteArray, Clob maps to String"),


    ;

    private final String message;

    Messages(String message) {
        this.message = message;
    }

    public String message(Object... params) {
        if (params.length == 0) {
            return message;
        }
        return String.format(message, params);
    }
}
