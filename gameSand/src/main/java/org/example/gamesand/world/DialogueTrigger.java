package org.example.gamesand.world;

public class DialogueTrigger {
    public int x, y, width, height;
    public String text;
    public boolean active = true; // Se è true, può essere attivato. Se false, è già stato letto.

    public DialogueTrigger(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    // Controlla se il rettangolo del giocatore si sovrappone a questa zona
    public boolean checkCollision(float px, float py, int pWidth, int pHeight) {
        if (!active) return false;

        return px < x + width &&
                px + pWidth > x &&
                py < y + height &&
                py + pHeight > y;
    }
}