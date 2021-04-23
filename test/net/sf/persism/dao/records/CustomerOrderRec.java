package net.sf.persism.dao.records;

import net.sf.persism.annotations.NotColumn;
import net.sf.persism.annotations.NotTable;

import java.lang.constant.DynamicConstantDesc;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

/*
Record version of CustomerOrder
testing primitives coming in as null constructor fails Caused by: java.lang.IllegalArgumentException on constructor call
 */
@NotTable
public record CustomerOrderRec(String customerId,
                               String companyName,
                               String description,
                               long orderId,
                               LocalDateTime dateCreated,
                               Date datePaid,
                               boolean paid) { // @NotColumn int extra // error java: annotation type not applicable to this kind of declaration

    public CustomerOrderRec {
        // Note this constructor is not in the objectClass.getConstructors() GOOD?
        System.out.println("CustomerOrderRec");
    }

    // NOTE: NONE OF THESE WORK BECAUSE parameters are in the class file as arg1, arg2, arg3....
    // It is possible to get the real names using javac -parameters but that's non standard so it's not supported
    // Same constructor different order? yup.
    public CustomerOrderRec(int extra, String companyName, String customerId, String description, LocalDateTime dateCreated, long orderId, Date datePaid, boolean paid) {
        this(customerId, companyName, description, orderId, dateCreated, datePaid, paid);
    }

    // constructor required to work when you have a @NotColumn. Should it be required?
    // Should we use the canonical constructor and just use a default? WTF
    public CustomerOrderRec(String companyName, String customerId, String description, LocalDateTime dateCreated, long orderId, Date datePaid, boolean paid) {
        this(customerId, companyName, description, orderId, dateCreated, datePaid, paid);
    }

    // smaller missing dateCreated and different order
    // c.Customer_ID, c.Company_Name, o.ID Order_ID, o.Name AS Description, o.Date_Paid, o.PAID
    public CustomerOrderRec(boolean paid, String description, String customerId, String companyName, long orderId, Date datePaid) {
        this(customerId, companyName, description, orderId, LocalDateTime.now(), datePaid, paid);
    }

}
