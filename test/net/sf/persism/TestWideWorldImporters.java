package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.wwi1.*;
import net.sf.persism.dao.wwi1.views.CustomerView;
import net.sf.persism.dao.wwi1.views.CustomerViewFail;
import net.sf.persism.logging.LogMode;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static net.sf.persism.Parameters.params;
import static net.sf.persism.SQL.where;

/*
    To test I added some duplicate tables
    Copied Sales.Customers to Application.Customers
    Copied Application.Cites to Sales.Cities
 */

@Category(ExternalDB.class)
public final class TestWideWorldImporters extends TestCase {

    private static final Log log = Log.getLogger(TestWideWorldImporters.class, LogMode.JUL);

    Connection con;
    Session session;

    // todo add a test around Multiple Views - same name and use the Schema.ViewName annotation + have a fail one with duplicates

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // this test is in case we use TestWideWorldImportersContainer
        //noinspection ConstantConditions
        if (getClass().equals(TestWideWorldImporters.class)) {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/wwi.properties"));
            String driver = props.getProperty("database.driver");
            String url = props.getProperty("database.url");
            String username = props.getProperty("database.username");
            String password = props.getProperty("database.password");
            Class.forName(driver);

            con = DriverManager.getConnection(url, username, password);

            session = new Session(con);
        }
    }

    public void testLogger() {
        log.debug("debug %s","x");
        log.debug("debug");
        log.info("info");
        log.info("info", new Throwable());
        log.warn("warn");
        log.warn("warn", new Throwable());
        log.warnNoDuplicates("warn no dup");
        log.error("error");
        log.error("error", new Throwable());
        log.isDebugEnabled();
    }

    public void testAllClassesQuery() {

        boolean fail = false;
        try {
            session.query(Application.City.class);
        } catch (PersismException e) {
            assertEquals("s/b same", Message.MoreThanOneTableOrViewInDifferentSchemas.message("TABLE", "Cities"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        fail = false;
        try {
            session.query(CustomerViewFail.class);
        } catch (PersismException e) {
            assertEquals("s/b same", Message.MoreThanOneTableOrViewInDifferentSchemas.message("VIEW", "Customers"), e.getMessage());
            fail = true;
        }
        assertTrue(fail);

        session.query(BuyingGroup.class);
        session.query(City.class);
        session.query(Color.class);
        session.query(Country.class);
        session.query(Customer.class);
        session.query(CustomerCategory.class);
        session.query(CustomerTransaction.class);
        session.query(DeliveryMethod.class);
        session.query(Invoice.class);
        session.query(InvoiceLine.class);
        session.query(PackageType.class);
        session.query(PaymentMethod.class);
        session.query(Person.class);
        session.query(PurchaseOrder.class);
        session.query(PurchaseOrderLine.class);
        session.query(SpecialDeal.class);
        session.query(State.class);
        session.query(StockGroup.class);
        session.query(StockItem.class);
        session.query(StockItemHolding.class);
        session.query(StockItemStockGroup.class);
        session.query(StockItemTransaction.class);
        session.query(Supplier.class);
        session.query(SupplierCategory.class);
        session.query(SupplierTransaction.class);
        session.query(SystemParameter.class);
        session.query(TransactionType.class);
        session.query(VehicleTemperature.class);

        // views
        session.query(CustomerView.class);
        session.query(net.sf.persism.dao.wwi1.views.Supplier.class);
        session.query(net.sf.persism.dao.wwi1.views.VehicleTemperature.class);


    }

    public void testReadOnlyTemporal() {
        var cities = session.query(City.class, where(":cityName = ?"), params("Aberdeen"));
        log.info("count: " + cities.size());
        assertTrue(cities.size() > 0);
        log.info(cities);
        var city = cities.get(0);
        var pop = city.getLatestRecordedPopulation();
        city.setLatestRecordedPopulation(0L);
        session.update(city);
        city.setLatestRecordedPopulation(pop);
        session.update(city);

        city = session.fetch(City.class, where(":cityName = ?"), params("my city"));
        if (city != null) {
            session.delete(city);
        }
        city = new City();
        city.setLatestRecordedPopulation(100L);
        city.setCityName("my city");
        city.setStateProvinceId(1);
        city.setLastEditedBy(1);
        session.insert(city);
        assertTrue(city.getCityId() > 0);
    }

    public void testInsertWithSequence() {
        // insert specifying ID
        City city = new City();
        city.setCityId(38189);
        if (session.fetch(city)) {
            assertNotNull(city.getValidFrom());
            assertNotNull(city.getValidTo());
            session.delete(city);
        }
        city.setCityName("SOMEWHERE");
        city.setStateProvinceId(1);
        city.setLastEditedBy(1);
        session.insert(city);
        assertNotNull(city.getValidFrom());
        assertNotNull(city.getValidTo());

        // insert not specifying id
        City city2 = session.fetch(City.class, where(":cityName = ?"), params("SOMEWHERE2"));
        if (city2 != null) {
            session.delete(city2);
        }
        city2 = new City();
        city2.setCityName("SOMEWHERE2");
        city2.setStateProvinceId(1);
        city2.setLastEditedBy(1);
        session.insert(city2);
        assertTrue(city2.getCityId() > 0);

        city2.setStateProvinceId(2);
        session.update(city2);
    }

    public void testUpdateWithTemporal() {

        // clowns https://github.com/Microsoft/mssql-jdbc/issues/656

        var person = session.fetch(Person.class, where(":personId = 1"));
        var buyingGroups = session.query(BuyingGroup.class, where(":buyingGroupId = 1"));
        System.out.println(buyingGroups);
        assertTrue(buyingGroups.size() > 0);
        var bg = buyingGroups.get(0);
        //System.out.println(bg.getValidFrom() + " " + bg.getValidTo());
        // Tailspin Toys
        bg.setBuyingGroupName("Tailspin Toys");
        session.update(bg);

        bg.setBuyingGroupName("Tailspin Toys!!");
        session.update(bg);

        bg = new BuyingGroup();
        try {
            bg.setLastEditedBy(person.getPersonId());
            bg.setBuyingGroupName("Some test102");
            session.insert(bg);
            log.error("BuyingGroup: " + bg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            session.delete(bg);
        }

    }

    @Override
    protected void tearDown() throws Exception {
        con.close();
        super.tearDown();
    }
}
