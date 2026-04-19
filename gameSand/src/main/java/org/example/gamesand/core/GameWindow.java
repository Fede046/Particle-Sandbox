package org.example.gamesand.core;

import javax.swing.JFrame;
import java.awt.Dimension;

/**
 * GAMEWINDOW.java — La Finestra
 *
 * Responsabilità: creare e configurare il JFrame che contiene tutto.
 *
 * SEPARAZIONE DELLE RESPONSABILITÀ:
 * GameWindow  → gestisce la finestra OS (titolo, dimensioni, chiusura)
 * GamePanel   → gestisce il rendering e il game loop
 *
 * Questa separazione è importante: se in futuro vuoi aggiungere un menu,
 * un pannello di debug, o passare a fullscreen, tocchi solo GameWindow
 * senza rompere nulla della simulazione.
 */
public class GameWindow {

    // Dimensioni della finestra in pixel.
    // Ogni pixel = una particella nella simulazione.
    // 800x600 = 480.000 particelle — gestibilissimo per iniziare.
    public static final int WIDTH  = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Particle Sandbox";

    //Classe standard di Swing per creare una finestra dotata di bordi, icon e pulsanti
    private final JFrame frame;
    private final GamePanel panel; // il Canvas dove gira tutto

    public GameWindow() {
        panel = new GamePanel();

        frame = new JFrame(TITLE);
        //Dice al programma di chiudersi  completamente quando chiudo la finestra
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Diciamo al layout manager quanto deve essere grande il Canvas.
        // Se usassimo frame.setSize() includeremmo i bordi della finestra
        // nel conteggio — preferredSize garantisce le dimensioni INTERNE esatte.
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        frame.add(panel);
        frame.pack();                    // ridimensiona il frame attorno al panel
        frame.setResizable(false);       // finestra fissa per ora
        frame.setLocationRelativeTo(null); // centra sullo schermo
        frame.setVisible(true);

        // Dopo setVisible il Canvas ha una dimensione reale e possiamo
        // creare la BufferStrategy (il double buffering).
        panel.createBuffers();
    }

    /**
     * Delega l'avvio del game loop al GamePanel.
     * GameWindow non sa COME gira il loop, sa solo CHE esiste.
     */
    public void start() {
        panel.startLoop();
    }
}