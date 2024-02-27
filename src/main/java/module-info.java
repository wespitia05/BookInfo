module com.example.bookinfo {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.healthmarketscience.jackcess;
    requires java.sql;
    requires com.google.gson;

    opens com.example.bookinfo to javafx.fxml, com.google.gson;
    exports com.example.bookinfo;
}