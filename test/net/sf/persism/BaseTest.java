package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.dao.*;

import java.sql.*;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Comments for BaseTest go here.
 *
 * @author Dan Howard
 * @since 10/8/11 6:24 PM
 */
public abstract class BaseTest extends TestCase {

    private static final Log log = Log.getLogger(BaseTest.class);

    Connection con;
    Query query;
    Command command;


    @Override
    protected void setUp() throws Exception {

        // todo review subclass setup methods. We probably don't need to do all that work between each test. Add a constructor for it instead and see how often they run then...

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (con != null) {
            MetaData.removeInstance(con);
            con.close();
        }
        super.tearDown();
    }


    public void testDates() {
        Customer customer = new Customer();
        customer.setCustomerId("123");
        customer.setAddress("123 Sesame Street");
        customer.setCity("MTL");
        customer.setCompanyName("ABC Inc");
        customer.setContactName("Fred");
        customer.setContactTitle("LORD");
        customer.setCountry("USA");

        customer.setFax("fax");
        customer.setPhone("phone");
        customer.setPostalCode("12345");
        customer.setRegion(Regions.East);
        /*

         */

        String dateOfLastOrder = "20120528192835";
        String dateRegistered = "20110612185225";
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

        customer.setDateOfLastOrder(new java.sql.Date(UtilsForTests.getCalendarFromAnsiDateString(dateOfLastOrder).getTime().getTime()));
        log.info(customer.getDateOfLastOrder());
        customer.setDateRegistered(new java.sql.Date(UtilsForTests.getCalendarFromAnsiDateString(dateRegistered).getTimeInMillis()));
        log.info(customer.getDateRegistered());

        assertEquals("date of last order s/b", dateOfLastOrder, df.format(customer.getDateOfLastOrder()));
        assertEquals("date registration s/b", dateRegistered, df.format(customer.getDateRegistered()));

        command.insert(customer);

        Customer customer2 = new Customer();
        customer2.setCustomerId(customer.getCustomerId());

        query.read(customer2);

        assertEquals("date of last order s/b", dateOfLastOrder, df.format(customer2.getDateOfLastOrder()));
        assertEquals("date registration s/b", dateRegistered, df.format(customer2.getDateRegistered()));
    }

    public void testStoredProcs() {

    }


    public void testRefreshObject() {

        try {
            log.info("testRefreshObject with : " + con.getMetaData().getURL());

            Customer customer1 = new Customer();
            customer1.setCompanyName("TEST");
            customer1.setCustomerId("MOO");
            customer1.setAddress("123 sesame street");
            customer1.setCity("city");
            customer1.setContactName("fred flintstone");
            customer1.setContactTitle("Lord");
            customer1.setCountry("US");
            customer1.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
            customer1.setFax("123-456-7890");
            customer1.setPhone("456-678-1234");
            customer1.setPostalCode("54321");
            //customer1.setRegion(Regions.East);

            command.delete(customer1); // in case it's already there.
            command.insert(customer1);

            String id = customer1.getCustomerId();

            Customer customer2 = new Customer();
            customer2.setCustomerId(id);
            query.read(customer2);

            // todo readObject should fail if ID not found??? this test is useless, read returns boolean
            assertNotNull("cust should be found ", customer2);

            long dateRegistered = customer1.getDateRegistered().getTime();

            customer1.setCountry("CA");
            customer1.setDateRegistered(null);

            assertEquals("Customer 1 country should be CA ", "CA", customer1.getCountry());
            assertEquals("Customer 1 date registered should be null", null, customer1.getDateRegistered());


            query.read(customer1);

            assertEquals("Customer 1 country should be US ", "US", customer1.getCountry());
            // we cannot test long. Need to format a date and compare as string to the seconds or minutes because SQL does not store dates with exact accuracy
            log.info(new Date(dateRegistered) + " = ? " + new Date(customer1.getDateRegistered().getTime()));
            assertEquals("Customer 1 date registered should be more or less equal since SQL can be off by 7 millis.?", "" + new Date(dateRegistered), "" + new Date(customer1.getDateRegistered().getTime()));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    public void testQueryWithSpecificColumnsWhereCaseDoesNotMatch() throws SQLException {

        log.info("testQueryWithSpecificColumnsWhereCaseDoesNotMatch with : " + con.getMetaData().getURL());

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Regions.East);

        command.delete(customer); // i case it already exists.
        command.insert(customer);

        customer.setRegion(Regions.North);
        command.update(customer);

//
        boolean failOnMissingProperties = false;

        try {
            query.readList(Customer.class, "SELECT Country from CUSTOMERS");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            assertTrue("message should contain 'Customer was not properly initialized'", e.getMessage().contains("Customer was not properly initialized"));
            failOnMissingProperties = true;
        }
        assertTrue("Should not be able to read fields if there are missing properties", failOnMissingProperties);

        // Make sure all columns are NOT the CASE of the ones in the DB.
        List<Customer> list = query.readList(Customer.class, "SELECT company_NAME, Date_Of_Last_ORDER, contact_title, pHone, rEGion, postal_CODE, FAX, DATE_Registered, ADDress, CUStomer_id, Contact_name, country, city from CUSTOMERS");

        // TODO TEST java.lang.IllegalArgumentException: argument type mismatch. Column: rEGion Type of property: class net.sf.persism.dao.Regions - Type read: class java.lang.String VALUE: BC
        // Add a value outside the enum to reproduce this error. IT IS A GOOD ERROR - we WANT TO THROW THIS so a user knows they have a value outside the ENUM

        log.info(list);
        assertEquals("list should be 1", 1, list.size());

        Customer c2 = list.get(0);
        assertEquals("region s/b north ", Regions.North, c2.getRegion());
    }

    public void testQueryResult() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Created AS DateCreated, o.PAID ");
            sb.append(" FROM ORDERS o");
            sb.append(" JOIN Customers c ON o.Customer_ID = c.Customer_ID");

            Customer c1 = new Customer();
            c1.setCustomerId("123");
            c1.setCompanyName("ABC INC");
            command.insert(c1);

            Customer c2 = new Customer();
            c2.setCustomerId("456");
            c2.setCompanyName("XYZ INC");
            command.insert(c2);

            Order order;
            order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("ORDER 1");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            order.setPaid(true);
            command.insert(order);

            assertTrue("order # > 0", order.getId() > 0);

            List<Order> orders = query.readList(Order.class, "select * from orders");
            assertEquals("should have 1 order", 1, orders.size());
            assertTrue("order id s/b > 0", orders.get(0).getId() > 0);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("123");
            order.setName("ORDER 2");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 3");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            order = DAOFactory.newOrder(con);
            order.setCustomerId("456");
            order.setName("ORDER 4");
            order.setCreated(new java.sql.Date(System.currentTimeMillis()));
            command.insert(order);

            String sql = sb.toString();
            log.info(sql);

            List<CustomerOrder> results = query.readList(CustomerOrder.class, sql);
            log.info(results);
            assertEquals("size should be 4", 4, results.size());

            // ORDER 1 s/b paid = true others paid = false
            for (CustomerOrder customerOrder : results) {
                if ("ORDER 1".equals(customerOrder.getDescription())) {
                    assertTrue("order 1 s/b paid", customerOrder.isPaid());
                } else {
                    assertFalse("order OTHER s/b NOT paid", customerOrder.isPaid());
                }
            }


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }


    public void testReadPrimitive() {

        Customer customer = new Customer();
        customer.setCompanyName("TEST");
        customer.setCustomerId("MOO");
        customer.setAddress("123 sesame street");
        customer.setCity("city");
        customer.setContactName("fred flintstone");
        customer.setContactTitle("Lord");
        customer.setCountry("US");
        customer.setDateRegistered(new java.sql.Date(System.currentTimeMillis()));
        customer.setFax("123-456-7890");
        customer.setPhone("456-678-1234");
        customer.setPostalCode("54321");
        customer.setRegion(Regions.East);

        command.delete(customer); // i case it already exists.
        command.insert(customer);

        List<String> list = query.readList(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("list should have 1", 1, list.size());
        assertEquals("String should be US", "US", list.get(0));

        String country = query.read(String.class, "SELECT Country from CUSTOMERS");
        assertEquals("String should be US", "US", country);

        country = "NOT US";

        country = query.read(String.class, "SELECT Country from CUSTOMERS");

        assertEquals("String should be US", "US", country);

        List<Date> dates = query.readList(Date.class, "select Date_Registered from Customers ");
        log.info(dates);

        Date dt = query.read(Date.class, "select Date_Registered from Customers ");
        log.info(dt);

        // Fails because there is no way to instantiate java.sql.Date - no default constructor.
        List<java.sql.Date> sdates = query.readList(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdates);

        java.sql.Date sdt = query.read(java.sql.Date.class, "select Date_Registered from Customers ");
        log.info(sdt);

        // this should fail. We can't do simple read on a primitive
        boolean failed = false;
        try {
            query.read(country);
        } catch (PersismException e) {
            failed = true;
            assertEquals("message s/b 'Cannot read a primitive type object with this method.'", "Cannot read a primitive type object with this method.", e.getMessage());
        }
        assertTrue("should have thrown the exception", failed);

    }

    protected abstract void createTables() throws SQLException;

    static Connection createMockConnection() {
        Connection con = new Connection() {
            @Override
            public Statement createStatement() throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql) throws SQLException {
                return null;
            }

            @Override
            public CallableStatement prepareCall(String sql) throws SQLException {
                return null;
            }

            @Override
            public String nativeSQL(String sql) throws SQLException {
                return null;
            }

            @Override
            public void setAutoCommit(boolean autoCommit) throws SQLException {

            }

            @Override
            public boolean getAutoCommit() throws SQLException {
                return false;
            }

            @Override
            public void commit() throws SQLException {

            }

            @Override
            public void rollback() throws SQLException {

            }

            @Override
            public void close() throws SQLException {

            }

            @Override
            public boolean isClosed() throws SQLException {
                return false;
            }

            @Override
            public DatabaseMetaData getMetaData() throws SQLException {
                return new DatabaseMetaData() {
                    @Override
                    public boolean allProceduresAreCallable() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean allTablesAreSelectable() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getURL() throws SQLException {
                        return "MISC:TEST!";
                    }

                    @Override
                    public String getUserName() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isReadOnly() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedHigh() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedLow() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedAtStart() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullsAreSortedAtEnd() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getDatabaseProductName() throws SQLException {
                        return "TestMiscellaneous";
                    }

                    @Override
                    public String getDatabaseProductVersion() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getDriverName() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getDriverVersion() throws SQLException {
                        return null;
                    }

                    @Override
                    public int getDriverMajorVersion() {
                        return 0;
                    }

                    @Override
                    public int getDriverMinorVersion() {
                        return 0;
                    }

                    @Override
                    public boolean usesLocalFiles() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean usesLocalFilePerTable() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMixedCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesUpperCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesLowerCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesMixedCaseIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getIdentifierQuoteString() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSQLKeywords() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getNumericFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getStringFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSystemFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getTimeDateFunctions() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getSearchStringEscape() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getExtraNameCharacters() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsAlterTableWithAddColumn() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsAlterTableWithDropColumn() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsColumnAliasing() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean nullPlusNonNullIsNull() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsConvert() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsConvert(int fromType, int toType) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsTableCorrelationNames() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsExpressionsInOrderBy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOrderByUnrelated() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupBy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupByUnrelated() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGroupByBeyondSelect() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsLikeEscapeClause() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleResultSets() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsNonNullableColumns() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMinimumSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCoreSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsExtendedSQLGrammar() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92IntermediateSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsANSI92FullSQL() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsFullOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsLimitedOuterJoins() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getSchemaTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getProcedureTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public String getCatalogTerm() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isCatalogAtStart() throws SQLException {
                        return false;
                    }

                    @Override
                    public String getCatalogSeparator() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsSchemasInDataManipulation() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInProcedureCalls() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInTableDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInDataManipulation() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsPositionedDelete() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsPositionedUpdate() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSelectForUpdate() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsStoredProcedures() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInComparisons() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInExists() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInIns() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsCorrelatedSubqueries() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsUnion() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsUnionAll() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
                        return false;
                    }

                    @Override
                    public int getMaxBinaryLiteralLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCharLiteralLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInGroupBy() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInIndex() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInOrderBy() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInSelect() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxColumnsInTable() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxConnections() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCursorNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxIndexLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxSchemaNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxProcedureNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxCatalogNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxRowSize() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
                        return false;
                    }

                    @Override
                    public int getMaxStatementLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxStatements() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxTableNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxTablesInSelect() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getMaxUserNameLength() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDefaultTransactionIsolation() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean supportsTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
                        return false;
                    }

                    @Override
                    public java.sql.ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getSchemas() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getCatalogs() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getTableTypes() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getTypeInfo() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsResultSetType(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownUpdatesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownDeletesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean ownInsertsAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersUpdatesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersDeletesAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean othersInsertsAreVisible(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean updatesAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean deletesAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean insertsAreDetected(int type) throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsBatchUpdates() throws SQLException {
                        return false;
                    }

                    @Override
                    public java.sql.ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
                        return null;
                    }

                    @Override
                    public Connection getConnection() throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsSavepoints() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsNamedParameters() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsMultipleOpenResults() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsGetGeneratedKeys() throws SQLException {
                        return false;
                    }

                    @Override
                    public java.sql.ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
                        return false;
                    }

                    @Override
                    public int getResultSetHoldability() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDatabaseMajorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getDatabaseMinorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getJDBCMajorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getJDBCMinorVersion() throws SQLException {
                        return 0;
                    }

                    @Override
                    public int getSQLStateType() throws SQLException {
                        return 0;
                    }

                    @Override
                    public boolean locatorsUpdateCopy() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean supportsStatementPooling() throws SQLException {
                        return false;
                    }

                    @Override
                    public RowIdLifetime getRowIdLifetime() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
                        return false;
                    }

                    @Override
                    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
                        return false;
                    }

                    @Override
                    public java.sql.ResultSet getClientInfoProperties() throws SQLException {
                        return null;
                    }

                    @Override
                    public java.sql.ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
                        return null;
                    }

                    @Override
                    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
                        return null;
                    }

                    public boolean generatedKeyAlwaysReturned() throws SQLException {
                        return false;
                    }

                    @Override
                    public <T> T unwrap(Class<T> iface) throws SQLException {
                        return null;
                    }

                    @Override
                    public boolean isWrapperFor(Class<?> iface) throws SQLException {
                        return false;
                    }
                };
            }

            @Override
            public void setReadOnly(boolean readOnly) throws SQLException {

            }

            @Override
            public boolean isReadOnly() throws SQLException {
                return false;
            }

            @Override
            public void setCatalog(String catalog) throws SQLException {

            }

            @Override
            public String getCatalog() throws SQLException {
                return null;
            }

            @Override
            public void setTransactionIsolation(int level) throws SQLException {

            }

            @Override
            public int getTransactionIsolation() throws SQLException {
                return 0;
            }

            @Override
            public SQLWarning getWarnings() throws SQLException {
                return null;
            }

            @Override
            public void clearWarnings() throws SQLException {

            }

            @Override
            public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
                return null;
            }

            @Override
            public Map<String, Class<?>> getTypeMap() throws SQLException {
                return null;
            }

            @Override
            public void setTypeMap(Map<String, Class<?>> map) throws SQLException {

            }

            @Override
            public void setHoldability(int holdability) throws SQLException {

            }

            @Override
            public int getHoldability() throws SQLException {
                return 0;
            }

            @Override
            public Savepoint setSavepoint() throws SQLException {
                return null;
            }

            @Override
            public Savepoint setSavepoint(String name) throws SQLException {
                return null;
            }

            @Override
            public void rollback(Savepoint savepoint) throws SQLException {

            }

            @Override
            public void releaseSavepoint(Savepoint savepoint) throws SQLException {

            }

            @Override
            public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
                return null;
            }

            @Override
            public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
                return null;
            }

            @Override
            public Clob createClob() throws SQLException {
                return null;
            }

            @Override
            public Blob createBlob() throws SQLException {
                return null;
            }

            @Override
            public NClob createNClob() throws SQLException {
                return null;
            }

            @Override
            public SQLXML createSQLXML() throws SQLException {
                return null;
            }

            @Override
            public boolean isValid(int timeout) throws SQLException {
                return false;
            }

            @Override
            public void setClientInfo(String name, String value) throws SQLClientInfoException {

            }

            @Override
            public void setClientInfo(Properties properties) throws SQLClientInfoException {

            }

            @Override
            public String getClientInfo(String name) throws SQLException {
                return null;
            }

            @Override
            public Properties getClientInfo() throws SQLException {
                return null;
            }

            @Override
            public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
                return null;
            }

            @Override
            public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
                return null;
            }

            public void setSchema(String schema) throws SQLException {

            }

            public String getSchema() throws SQLException {
                return null;
            }

            public void abort(Executor executor) throws SQLException {

            }

            public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

            }

            public int getNetworkTimeout() throws SQLException {
                return 0;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }
        };

        return con;
    }
}
