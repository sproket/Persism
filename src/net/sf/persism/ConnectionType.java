package net.sf.persism;

enum ConnectionType {
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
    private final String keywordStartDelimiter;
    private final String keywordEndDelimiter;

    ConnectionType(String schemaPattern, String keywordStartDelimiter, String keywordEndDelimiter) {
        this.schemaPattern = schemaPattern;
        this.keywordStartDelimiter = keywordStartDelimiter;
        this.keywordEndDelimiter = keywordEndDelimiter;
    }

    public static ConnectionType get(String connectionUrl) {
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

    public boolean supportsReadingFromClobType() {
        return ConnectionType.H2 == this || ConnectionType.Oracle == this || ConnectionType.HSQLDB == this || ConnectionType.Derby == this;
    }

    public boolean supportsReadingFromBlobType() {
        return ConnectionType.Oracle == this;
    }

    public boolean supportsSpacesInTableNames() {
        return Util.isNotEmpty(this.keywordStartDelimiter);
    }

    public boolean supportsNonAutoIncGenerated() {
        return ConnectionType.PostgreSQL == this || ConnectionType.MSSQL == this;
    }
}
