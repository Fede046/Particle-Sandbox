module org.example.gamesand {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.gamesand to javafx.fxml;
    exports org.example.gamesand;
}