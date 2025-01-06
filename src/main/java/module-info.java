module org.example.gimatagui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires com.google.protobuf;

    opens home to javafx.fxml;
    exports home;

    exports DBHandler;
    opens DBHandler to javafx.base; //Ito ung need gawin hehe
}