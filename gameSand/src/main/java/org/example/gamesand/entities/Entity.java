package org.example.gamesand.entities; // Aggiorna il package in base a dove metti il file

import org.example.gamesand.core.Camera;
import org.example.gamesand.world.ParticleType;
import org.example.gamesand.world.World;
import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Entity {
    public float x, y;
    public int width, height;

    // Modificate in "protected" così le classi figlie possono leggerle/modificarle
    protected float vx = 0;
    protected float vy = 0;
    protected float speed = 3.0f;
    protected float gravity = 0.25f;
    protected float jumpForce = -6.0f;
    protected boolean grounded = false;
    protected Color color = Color.WHITE; // Colore base di default

    public Entity(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // ── LOGICA DI FISICA UNIVERSALE ──
    public void applyPhysics(World world) {
        // 1. Applica gravità (terminal velocity a 8.0f)
        vy += gravity;
        if (vy > 8.0f) vy = 8.0f;

        // 2. Movimento X e collisioni
        x += vx;
        if (checkCollision(world, x, y)) {
            x -= vx;
            vx = 0;
        }

        // 3. Movimento Y e collisioni
        y += vy;
        grounded = false;
        if (checkCollision(world, x, y)) {
            if (vy > 0) grounded = true;
            y -= vy;
            vy = 0;
        }

        // 4. Limiti del mondo (impedisce cadute nel vuoto)
        if (x < 0) x = 0;
        if (x + width >= world.getWidth()) x = world.getWidth() - width;
        if (y < 0) y = 0;
        if (y + height >= world.getHeight()) y = world.getHeight() - height;
    }

    // ── COLLISIONI UNIVERSALI ──
    protected boolean checkCollision(World world, float testX, float testY) {
        int startX = (int) testX;
        int endX = (int) (testX + width - 1);
        int startY = (int) testY;
        int endY = (int) (testY + height - 1);

        for (int cx = startX; cx <= endX; cx++) {
            for (int cy = startY; cy <= endY; cy++) {
                ParticleType p = world.getParticle(cx, cy);
                // Tutti sbattono contro PIETRA e SABBIA
                if (p == ParticleType.STONE || p == ParticleType.SAND) {
                    return true;
                }
            }
        }
        return false;
    }

    // ── RENDERING UNIVERSALE ──
    public void render(Graphics2D g, Camera camera) {
        int screenX = (int)(x - camera.x);
        int screenY = (int)(y - camera.y);

        g.setColor(color);
        g.fillRect(screenX, screenY, width, height);
    }
}