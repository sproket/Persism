package net.sf.persism;

// todo add line comment strings and multi line string comments properties for trimming in isSelect method
// oracle postgresql, mssql, SQLite
// Derby ? -- ? No mention of it exactly
// h2 -- // and /* */
// HSQLDB -- // and /* */
// Mysql # -- and /* */
// Firebird /* */ Single line?
// ACCESS ? NOT SUPPORTED? standard -- and /* */ seem to work from DBeaver...
// Informix hyphen ( -- ), braces ( { } ), and C-style ( /* . . . */ )

// todo add isXSupported methods.

enum ConnectionTypes {
    Oracle("%", "\"", "\""),

    MSSQL(null, "[", "]"),

    @Deprecated
    JTDS(null, "[", "]"),

    Derby(null, "\"", "\""),

    H2(null, "\"", "\""),

    MySQL(null, "`", "`"),

    PostgreSQL(null, "\"", "\""),

    SQLite(null, "[", "]"),

    Firebird(null, "\"", "\""),

    HSQLDB(null, "\"", "\""),

    UCanAccess(null, "[", "]"),

    Informix(null, "", ""),

    Other(null, "", "")
    ;

    private final String schemaPattern;
    // todo need more than 1 but always use the 1st. We need to know if the DB supports multiple delims to skip them when parsing....
    private final String keywordStartDelimiter;
    private final String keywordEndDelimiter;

    ConnectionTypes(String schemaPattern, String keywordStartDelimiter, String keywordEndDelimiter) {
        this.schemaPattern = schemaPattern;
        this.keywordStartDelimiter = keywordStartDelimiter;
        this.keywordEndDelimiter = keywordEndDelimiter;
    }

    public static ConnectionTypes get(String connectionUrl) {
        if (connectionUrl == null) {
            return null;
        }

        if (connectionUrl.startsWith("jdbc:h2")) {
            return H2;
        }

        if (connectionUrl.startsWith("jdbc:jtds")) {
            return JTDS;
        }

        if (connectionUrl.startsWith("jdbc:sqlserver")) {
            return MSSQL;
        }

        if (connectionUrl.startsWith("jdbc:oracle")) {
            return Oracle;
        }

        if (connectionUrl.startsWith("jdbc:sqlite")) {
            return SQLite;
        }

        if (connectionUrl.startsWith("jdbc:derby")) {
            return Derby;
        }

        if (connectionUrl.startsWith("jdbc:mysql")) {
            return MySQL;
        }

        if (connectionUrl.startsWith("jdbc:postgresql")) {
            return PostgreSQL;
        }
        
        if (connectionUrl.startsWith("jdbc:firebirdsql")) {
            return Firebird;
        }

        if (connectionUrl.startsWith("jdbc:hsqldb")) {
            return HSQLDB;
        }

        if (connectionUrl.startsWith("jdbc:ucanaccess")) {
            return UCanAccess;
        }

        if (connectionUrl.startsWith("jdbc:informix")) {
            return Informix;
        }

        return Other;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public String getKeywordStartDelimiter() {
        return keywordStartDelimiter;
    }

    public String getKeywordEndDelimiter() {
        return keywordEndDelimiter;
    }
}
