package org.example.gamesand.core;

/**
 * MAIN.java — Entry Point
 *
 * Responsabilità: avviare il gioco sul thread corretto.
 *
 * PERCHÉ SwingUtilities.invokeLater?
 * Swing non è thread-safe. Tutte le operazioni sulla UI (creare finestre,
 * aggiungerci componenti) devono avvenire sull'Event Dispatch Thread (EDT).
 * invokeLater() schedula il nostro codice sull'EDT in modo sicuro.
 *
 * Il game loop vero e proprio girerà su un thread SEPARATO (lo vedremo in
 * GamePanel), ma la creazione della finestra parte sempre dall'EDT.
 */
//Sing -> libreri a graficoa standard integrata in Java serve per creare le interfacce grafiche
public class Main {

    public static void main(String[] args) {
        // Schedula la creazione della finestra sull'EDT di Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.start(); // avvia il game loop
        });
    }
}