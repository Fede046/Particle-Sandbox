package org.example.gamesand.core;

import org.example.gamesand.entities.Player;
import org.example.gamesand.world.ParticleType;
import org.example.gamesand.world.World;
import org.example.gamesand.world.WorldRenderer;
import org.example.gamesand.input.KeyHandler;
import org.example.gamesand.input.MouseHandler;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // ── Nuove variabili del mondo ──
    private World world;
    private WorldRenderer worldRenderer;


    // ── Variabili per l'input isolate ──
    private KeyHandler keyH;
    private MouseHandler mouseH;

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


    // ── Variabili del Giocatore e Input ──
    private Player player;



    private Camera camera;

    // ─────────────────────────────────────────────────────────
    // SETUP
    // ─────────────────────────────────────────────────────────

    public GamePanel() {
        // Disabilita la ridisegnatura automatica di AWT.
        // Se AWT chiama repaint() mentre il nostro thread disegna → glitch.
        // Gestiamo tutto noi manualmente nel loop.
        setIgnoreRepaint(true);
        setBackground(Color.BLACK);


        // Assicurati che i nomi corrispondano ai file che hai messo nella cartella resources
        world = new World("/org/example/gamesand/level_mask.png", "/org/example/gamesand/background.jpg");

        camera = new Camera(GameWindow.WIDTH, GameWindow.HEIGHT);
        // Aggiorna anche l'inizializzazione del renderer
        worldRenderer = new WorldRenderer(world, GameWindow.WIDTH, GameWindow.HEIGHT);

        // Aggiungi controlli MOUSE

        // Inizializza i gestori dell'input
        keyH = new KeyHandler();
        mouseH = new MouseHandler();

        // Aggiungili al pannello
        addKeyListener(keyH);
        addMouseListener(mouseH);
        addMouseMotionListener(mouseH);

        // Inizializza il giocatore al centro dello schermo in alto
        player = new Player(GameWindow.WIDTH / 2, 100);





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
        // Calcola l'offset della telecamera per il mouse
        int worldMouseX = mouseH.x + (int)camera.x;
        int worldMouseY = mouseH.y + (int)camera.y;

        // Se premiamo il mouse, genera particelle (il tasto destro forza il pennello a EMPTY)
        if (mouseH.isPressed) {
            ParticleType brushToUse = mouseH.isRightClick ? ParticleType.EMPTY : keyH.currentBrush;
            world.setParticle(worldMouseX, worldMouseY, brushToUse, 10);
        }

        // Passa gli input del KeyHandler al Player
        player.update(world, keyH.upPressed, keyH.leftPressed, keyH.rightPressed);

        // Aggiorna la telecamera
        camera.update(player.x, player.y, player.width, player.height, world.getWidth(), world.getHeight());

        // Aggiorna la simulazione della fisica
        world.update(camera, GameWindow.WIDTH, GameWindow.HEIGHT);
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
        // 1. Disegna lo sfondo fisso dietro a tutto
        if (world.backgroundImage != null) {
            // Lo disegniamo grande quanto lo schermo. Se vuoi che si muova con la telecamera,
            // basta calcolare un offset dividendo camera.x per un moltiplicatore (effetto parallasse!)
            g.drawImage(world.backgroundImage, 0, 0, GameWindow.WIDTH, GameWindow.HEIGHT, null);
        }

        // 2. Disegna le particelle (solo i muri, l'EMPTY ora è trasparente!)
        worldRenderer.render(g, camera);

        // Disegna il giocatore passandogli la telecamera
        player.render(g, camera);

        // Interfaccia
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + fps + "  UPS: " + ups, 10, 20);
        g.setColor(Color.YELLOW);
        g.drawString("Elemento: " + keyH.currentBrush.name(), 10, 40);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Muovi: WASD | Crea: 1, 2, 3, 0 + Click Mouse", 10, 60);
    }
}