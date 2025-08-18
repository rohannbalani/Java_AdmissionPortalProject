module com.humber {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires jakarta.persistence;
    requires java.naming;

    exports com.humber;
    exports com.humber.model;
    exports com.humber.service;
    exports com.humber.util;

    opens com.humber to javafx.fxml;
    opens com.humber.model to org.hibernate.orm.core, javafx.base;
}
