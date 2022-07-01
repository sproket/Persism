package net.sf.persism;

import junit.framework.TestCase;
import net.sf.persism.categories.ExternalDB;
import net.sf.persism.dao.wwi1.*;
import net.sf.persism.dao.wwi1.views.CustomerView;
import org.junit.experimental.categories.Category;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static net.sf.persism.SQL.where;

@Category(ExternalDB.class)

public final class TestWideWorldImporters extends TestCase {

    private static final Log log = Log.getLogger(TestWideWorldImporters.class);

    Connection con;
    Session session;

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

    public void testAllClasses() {


        session.query(Customer.class);


        session.query(Application.City.class);
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
            bg.setBuyingGroupName("Some test8");
            session.insert(bg);

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
