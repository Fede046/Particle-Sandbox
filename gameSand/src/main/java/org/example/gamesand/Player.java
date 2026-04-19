package org.example.gamesand;

import java.awt.Color;
import java.awt.Graphics2D;

public class Player {
    public float x, y;
    public int width = 12;
    public int height = 24;

    private float vx = 0;
    private float vy = 0;
    private float speed = 3.0f;
    private float gravity = 0.25f;
    private float jumpForce = -6.0f;
    private boolean grounded = false;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(World world, boolean up, boolean left, boolean right) {
        // 1. Movimento orizzontale
        if (left) vx = -speed;
        else if (right) vx = speed;
        else vx = 0;

        // 2. Salto (solo se tocca terra)
        if (up && grounded) {
            vy = jumpForce;
            grounded = false;
        }

        // 3. Applica gravità
        vy += gravity;
        // Limite di velocità di caduta (terminal velocity)
        if (vy > 8.0f) vy = 8.0f;

        // 4. Applica movimento asse X e controlla collisioni
        x += vx;
        if (checkCollision(world, x, y)) {
            x -= vx; // Se c'è un muro, annulla il movimento orizzontale
            vx = 0;
        }

        // 5. Applica movimento asse Y e controlla collisioni
        y += vy;
        grounded = false;
        if (checkCollision(world, x, y)) {
            if (vy > 0) grounded = true; // Se cadeva verso il basso e sbatte, è a terra
            y -= vy; // Annulla il movimento verticale
            vy = 0;
        }

        // 6. Non far uscire il giocatore dallo schermo (temporaneo finché non c'è la telecamera)
        if (x < 0) x = 0;
        if (x + width >= world.getWidth()) x = world.getWidth() - width;
        if (y < 0) y = 0;
        if (y + height >= world.getHeight()) y = world.getHeight() - height;
    }

    // Controlla se il rettangolo del giocatore si sovrappone a pixel solidi
    private boolean checkCollision(World world, float testX, float testY) {
        int startX = (int) testX;
        int endX = (int) (testX + width - 1);
        int startY = (int) testY;
        int endY = (int) (testY + height - 1);

        for (int cx = startX; cx <= endX; cx++) {
            for (int cy = startY; cy <= endY; cy++) {
                ParticleType p = world.getParticle(cx, cy);
                // Il giocatore sbatte contro PIETRA e SABBIA. (Ignora EMPTY e WATER)
                if (p == ParticleType.STONE || p == ParticleType.SAND) {
                    return true;
                }
            }
        }
        return false;
    }

    public void render(Graphics2D g, Camera camera) {
        // Calcola la posizione sullo schermo sottraendo l'offset della telecamera
        int screenX = (int)(x - camera.x);
        int screenY = (int)(y - camera.y);

        // Disegna il giocatore come un rettangolo rosso
        g.setColor(Color.RED);
        g.fillRect(screenX, screenY, width, height);
    }
}