package org.example.gamesand;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;

/**
 * GAMEPANEL.java — Il cuore del gioco
 *
 * Estende Canvas (AWT puro) invece di JPanel per due motivi:
 *   1. Canvas supporta BufferStrategy nativamente
 *   2. È un componente "heavyweight" — ha il suo contesto grafico OS,
 *      il che significa rendering più diretto e performante
 *
 * ─────────────────────────────────────────────────────────────
 * CONCETTO CHIAVE: BUFFERSSTRATEGY (Double Buffering)
 * ─────────────────────────────────────────────────────────────
 * Senza double buffering:
 *   → disegni direttamente sullo schermo
 *   → l'utente vede ogni frame a metà costruzione → flickering
 *
 * Con double buffering (2 buffer):
 *   → Buffer A: quello che l'utente vede (front buffer)
 *   → Buffer B: quello su cui stai disegnando (back buffer)
 *   → Quando il frame è pronto: swap atomico A↔B
 *   → L'utente vede sempre frame completi → niente flickering
 *
 * ─────────────────────────────────────────────────────────────
 * CONCETTO CHIAVE: GAME LOOP con FIXED TIMESTEP
 * ─────────────────────────────────────────────────────────────
 * Target: 60 aggiornamenti al secondo (UPS = Updates Per Second)
 * Ogni "tick" dura idealmente: 1_000_000_000 ns / 60 = ~16.67ms
 *
 * Il loop misura quanto tempo ha impiegato update+render,
 * poi dorme per il tempo rimanente fino al prossimo tick.
 *
 * Questo garantisce che la simulazione giri alla stessa velocità
 * su macchine diverse — fondamentale per la fisica deterministica.
 */
public class GamePanel extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    // TARGET: 60 aggiornamenti/render al secondo
    private static final int  TARGET_UPS       = 60;
    private static final long NANOSECONDS      = 1_000_000_000L;
    private static final long NS_PER_UPDATE    = NANOSECONDS / TARGET_UPS; // ~16.67ms

    // Il thread separato su cui gira il loop
    private Thread gameThread;
    private volatile boolean running = false;
    // volatile: garantisce visibilità della variabile tra thread diversi.
    // Senza volatile, il compilatore potrebbe cachare il valore localmente
    // e non vedere la modifica dall'esterno.

    private BufferStrategy bufferStrategy;

    // ─── Contatori per debug ─────────────────────────────────
    private int  fps, ups;           // frame e update nell'ultimo secondo
    private int  fpsCounter, upsCounter;
    private long lastSecond;         // timestamp dell'ultimo reset contatori

    // ─────────────────────────────────────────────────────────
    // SETUP
    // ─────────────────────────────────────────────────────────

    public GamePanel() {
        // Disabilita la ridisegnatura automatica di AWT.
        // Se AWT chiama repaint() mentre il nostro thread disegna → glitch.
        // Gestiamo tutto noi manualmente nel loop.
        setIgnoreRepaint(true);
        setBackground(Color.BLACK);
    }

    /**
     * Crea la BufferStrategy con 2 buffer (double buffering).
     * DEVE essere chiamata DOPO che la finestra è visibile,
     * altrimenti il Canvas non ha ancora un contesto grafico reale.
     */
    public void createBuffers() {
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
    }

    // ─────────────────────────────────────────────────────────
    // AVVIO / STOP DEL LOOP
    // ─────────────────────────────────────────────────────────

    public void startLoop() {
        if (running) return; // evita doppio avvio
        running = true;

        // Creiamo un thread daemon: si chiude automaticamente quando
        // il thread principale (EDT) termina. Senza questo, chiudere
        // la finestra non fermerebbe il game loop.
        gameThread = new Thread(this, "GameLoop-Thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    public void stopLoop() {
        running = false;
        try {
            gameThread.join(); // aspetta che il thread finisca pulitamente
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ─────────────────────────────────────────────────────────
    // GAME LOOP — run() è chiamato dal Thread
    // ─────────────────────────────────────────────────────────

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long lag = 0L; // tempo "accumulato" non ancora processato

        lastSecond = System.currentTimeMillis();

        while (running) {
            long now     = System.nanoTime();
            long elapsed = now - lastTime; // tempo trascorso dall'ultimo ciclo
            lastTime = now;
            lag += elapsed;

            // ── UPDATE ──────────────────────────────────────
            // Se il tempo accumulato supera il tick target, aggiorniamo.
            // Il while gestisce il caso in cui un frame sia stato
            // particolarmente lento: recupera i tick mancanti.
            while (lag >= NS_PER_UPDATE) {
                update();
                upsCounter++;
                lag -= NS_PER_UPDATE;
            }

            // ── RENDER ──────────────────────────────────────
            render();
            fpsCounter++;

            // ── CONTATORI DEBUG (ogni secondo) ───────────────
            long currentMs = System.currentTimeMillis();
            if (currentMs - lastSecond >= 1000) {
                fps = fpsCounter;
                ups = upsCounter;
                fpsCounter = 0;
                upsCounter  = 0;
                lastSecond  = currentMs;
            }

            // ── SLEEP ────────────────────────────────────────
            // Calcola quanto tempo rimane fino al prossimo tick
            // e cedi la CPU per quel periodo.
            long sleepNs = NS_PER_UPDATE - (System.nanoTime() - now);
            if (sleepNs > 0) {
                try {
                    // Thread.sleep() vuole millisecondi — convertiamo
                    Thread.sleep(sleepNs / 1_000_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
            // Se sleepNs <= 0 il frame era troppo lento: non dormiamo
            // e proviamo a recuperare nel prossimo ciclo.
        }
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE — logica della simulazione
    // ─────────────────────────────────────────────────────────

    /**
     * Qui andrà tutta la fisica: aggiornamento delle particelle,
     * collisioni, chimica, ecc.
     * Per ora è vuoto — lo riempiremo nei prossimi step.
     */
    private void update() {
        // TODO Step 2: World.update() → aggiorna la simulazione CA
    }

    // ─────────────────────────────────────────────────────────
    // RENDER — disegna il frame corrente
    // ─────────────────────────────────────────────────────────

    /**
     * Pattern corretto per BufferStrategy:
     *   1. Prendi il Graphics dal back buffer
     *   2. Disegna tutto
     *   3. Fai dispose() del Graphics (libera risorse native)
     *   4. show() → swap dei buffer
     *   5. Ripeti se Toolkit segnala che il buffer è perso (contentsLost)
     *
     * Il do-while gestisce casi rari ma reali in cui lo swap fallisce
     * (es. finestra minimizzata, cambio risoluzione).
     */
    private void render() {
        do {
            do {
                Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
                try {
                    drawFrame(g);
                } finally {
                    g.dispose(); // SEMPRE in finally: evita memory leak
                }
            } while (bufferStrategy.contentsRestored());

            bufferStrategy.show();

        } while (bufferStrategy.contentsLost());

        // Forza il flush sul sistema operativo (importante su Linux/X11)
        java.awt.Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Disegna il contenuto del frame.
     * Per ora: sfondo nero + testo FPS/UPS per verificare che il loop giri.
     */
    private void drawFrame(Graphics2D g) {
        // Sfondo
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // ── TODO Step 2: WorldRenderer.render(g) ──

        // Debug HUD — FPS e UPS nell'angolo in alto a sinistra
        // Questo ci dice subito se il loop sta girando correttamente
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        g.drawString("FPS: " + fps + "  UPS: " + ups, 10, 20);

        // Messaggio temporaneo al centro — sparirà quando aggiungiamo il mondo
        g.setColor(new Color(80, 80, 80));
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 16));
        String msg = "Game Loop attivo — prossimo step: World + Particelle";
        int msgW = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (getWidth() - msgW) / 2, getHeight() / 2);
    }
}