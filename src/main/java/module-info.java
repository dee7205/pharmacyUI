module org.example.gimatagui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;

    opens home to javafx.fxml;
    opens DBHandler to javafx.base; //Ito ung need gawin hehe
    exports home;
}