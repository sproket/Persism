package net.sf.persism;

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

    HSQLDB(null, "", ""),

    Other(null, "", "")
    ;

    private String schemaPattern;
    private String keywordStartDelimiter;
    private String keywordEndDelimiter;

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
