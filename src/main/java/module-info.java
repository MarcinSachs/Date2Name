module pl.marcinsachs.exif2name {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires metadata.extractor;
    requires static lombok;
    requires org.apache.commons.io;
    requires java.prefs;

    opens pl.marcinsachs.date2name to javafx.fxml;
    exports pl.marcinsachs.date2name;
}