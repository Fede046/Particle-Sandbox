module org.example.gamesand {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.gamesand to javafx.fxml;
    exports org.example.gamesand.core;
    opens org.example.gamesand.core to javafx.fxml;
    exports org.example.gamesand.world;
    opens org.example.gamesand.world to javafx.fxml;
    exports org.example.gamesand.entities;
    opens org.example.gamesand.entities to javafx.fxml;
}