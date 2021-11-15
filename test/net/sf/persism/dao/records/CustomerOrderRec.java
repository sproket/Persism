package net.sf.persism.dao.records;

import net.sf.persism.ConstructorTag;
import net.sf.persism.annotations.NotTable;

import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import java.util.Date;

/*
Record version of CustomerOrder
 */
@NotTable
@ConstructorTag("MAIN?")
public record CustomerOrderRec(String customerId,
                               String companyName,
                               String description,
                               long orderId,
                               LocalDateTime dateCreated,
                               Date datePaid,
                               boolean paid) {

    // todo also see https://stackoverflow.com/questions/67168624/constructor-annotation-on-java-records


    @ConstructorTag("CANONICAL?")
    public CustomerOrderRec {
        // Note this constructor is not in the objectClass.getConstructors() GOOD?
        System.out.println("CustomerOrderRec canonical constructor...");
    }

    @ConstructorTag("C1")
    @ConstructorProperties({"customerId", "companyName", "description", "orderId"})
    public CustomerOrderRec(String customerId, String companyName, String description, long orderId) {
        this(customerId, companyName, description, orderId, null, null, false);
    }

    // NOTE: NONE OF THESE WORK BECAUSE parameters are in the class file as arg1, arg2, arg3....
    // It is possible to get the real names using javac -parameters but that's non standard so it's not supported
    // You can use @ConstructorProperties to name them though
//    @ConstructorProperties({"id", "filename"})
    @ConstructorTag("C2")
    public CustomerOrderRec(int extra, String companyName, String customerId, String description, LocalDateTime dateCreated, long orderId, Date datePaid, boolean paid) {
        this(customerId, companyName, description, orderId, dateCreated, datePaid, paid);
    }

    // constructor required to work when you have a @NotColumn. Should it be required?
    // Should we use the canonical constructor and just use a default?
    @ConstructorTag("C3")
    public CustomerOrderRec(String companyName, String customerId, String description, LocalDateTime dateCreated, long orderId, Date datePaid, boolean paid) {
        this(customerId, companyName, description, orderId, dateCreated, datePaid, paid);
    }

    // smaller missing dateCreated and different order
    // c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.PAID
    // will work with
    @ConstructorTag("C4")
    @ConstructorProperties({"paid", "description", "customerId", "companyName", "orderId", "datePaid"})
    public CustomerOrderRec(boolean paid, String description, String customerId, String companyName, long orderId, Date datePaid) {
        this(customerId, companyName, description, orderId, LocalDateTime.now(), datePaid, paid);
    }

}
