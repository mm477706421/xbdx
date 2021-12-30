module com.gg.jwglxt {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires org.apache.commons.codec;
    requires java.scripting;
    requires fastjson;
    requires org.apache.commons.io;


    opens com.gg.jwglxt to javafx.fxml;
    exports com.gg.jwglxt;
}