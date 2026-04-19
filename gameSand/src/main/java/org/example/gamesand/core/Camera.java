package org.example.gamesand.core;

public class Camera {
    public float x, y;
    private int screenWidth, screenHeight;

    public Camera(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void update(float targetX, float targetY, int playerWidth, int playerHeight, int worldWidth, int worldHeight) {
        // Centra la telecamera sul giocatore
        x = targetX + (playerWidth / 2f) - (screenWidth / 2f);
        y = targetY + (playerHeight / 2f) - (screenHeight / 2f);

        // Limita la telecamera ai bordi del mondo per non vedere il "vuoto" esterno
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > worldWidth - screenWidth) x = worldWidth - screenWidth;
        if (y > worldHeight - screenHeight) y = worldHeight - screenHeight;
    }
}