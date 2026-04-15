module org.example.gamesand {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.gamesand to javafx.fxml;
    exports org.example.gamesand;
}