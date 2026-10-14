module hr.tvz.possystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires java.sql;
    requires org.slf4j;
    requires jakarta.persistence;
    requires jbcrypt;
    requires java.desktop;
    requires java.net.http;
    requires java.xml.crypto;
    requires com.google.zxing;
    opens hr.tvz.possystem.controllers.admin to javafx.fxml;
    opens hr.tvz.possystem to javafx.fxml;
    exports hr.tvz.possystem;
    exports hr.tvz.possystem.constants;
    exports hr.tvz.possystem.controllers;
    exports hr.tvz.possystem.controllers.admin;
    exports hr.tvz.possystem.model;
    opens hr.tvz.possystem.controllers to javafx.fxml;
    exports hr.tvz.possystem.authentication;

    opens hr.tvz.possystem.authentication to javafx.fxml;
    exports hr.tvz.possystem.controllers.admin.products;
    opens hr.tvz.possystem.controllers.admin.products to javafx.fxml;
    exports hr.tvz.possystem.controllers.admin.refunds;
    opens hr.tvz.possystem.controllers.admin.refunds to javafx.fxml;
    exports hr.tvz.possystem.controllers.admin.users;
    opens hr.tvz.possystem.controllers.admin.users to javafx.fxml;
    exports hr.tvz.possystem.controllers.admin.sales;
    opens hr.tvz.possystem.controllers.admin.sales to javafx.fxml;
    exports hr.tvz.possystem.fisc;
}