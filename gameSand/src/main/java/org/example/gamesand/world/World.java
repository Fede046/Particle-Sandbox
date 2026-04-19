package org.example.gamesand.world;

import org.example.gamesand.core.Camera;

import java.awt.image.BufferedImage;
import java.util.Random;

public class World {
    private final int width;
    private final int height;
    private ParticleType[][] grid;
    private Random random = new Random();

    // Nuova variabile
    public BufferedImage backgroundImage;

    public World(String maskPath, String backgroundPath) {
        // 1. Carichiamo la maschera fisica
        this.grid = LevelLoader.loadCollisionMask(maskPath);

        // 2. Se fallisce, crea una griglia di emergenza
        if (this.grid == null) {
            this.width = 800; this.height = 600;
            this.grid = new ParticleType[width][height];
        } else {
            this.width = grid.length;
            this.height = grid[0].length;
        }

        // 3. Carichiamo lo sfondo decorativo
        this.backgroundImage = LevelLoader.loadBackground(backgroundPath);
    }

    // Aggiungi questo metodo per permettere a GamePanel di rigenerare la mappa (col tasto R)
    public void generateCave() {
        this.grid = CaveGenerator.generate(width, height);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public ParticleType getParticle(int x, int y) {
        // Se esce dai bordi, consideriamolo "PIETRA" per non far uscire gli elementi
        if (x < 0 || x >= width || y < 0 || y >= height) return ParticleType.STONE;
        return grid[x][y];
    }

    // Per disegnare col mouse (genera in un raggio "brushSize")
    public void setParticle(int mouseX, int mouseY, ParticleType type, int brushSize) {
        for(int i = -brushSize; i <= brushSize; i++) {
            for(int j = -brushSize; j <= brushSize; j++) {
                int x = mouseX + i;
                int y = mouseY + j;
                // Una forma vagamente circolare
                if(i*i + j*j <= brushSize*brushSize && x >= 0 && x < width && y >= 0 && y < height) {
                    // Non sovrascrivere se stiamo disegnando elementi, tranne se il brush è EMPTY
                    if(grid[x][y] == ParticleType.EMPTY || type == ParticleType.EMPTY) {
                        grid[x][y] = type;
                    }
                }
            }
        }
    }

    // ─── LOGICA DELLA FISICA ──────────────────────────────────────
    // ─── LOGICA DELLA FISICA (OTTIMIZZATA PER TELECAMERA) ──────────────────
    public void update(Camera camera, int screenWidth, int screenHeight) {
        // 1. Definiamo un margine (es. 64 pixel) in modo che la fisica funzioni
        //    anche appena fuori dallo schermo.
        int margin = 64;

        // 2. Calcoliamo i bordi dell'Area Attiva
        int startX = Math.max(0, (int)camera.x - margin);
        int endX = Math.min(width, (int)camera.x + screenWidth + margin);

        int startY = Math.max(0, (int)camera.y - margin);
        int endY = Math.min(height - 1, (int)camera.y + screenHeight + margin);

        // 3. Aggiorniamo DAL BASSO VERSO L'ALTO, ma SOLO nell'Area Attiva
        for (int y = endY - 1; y >= startY; y--) {
            boolean leftToRight = random.nextBoolean();

            int areaWidth = endX - startX;
            for (int i = 0; i < areaWidth; i++) {
                // Calcola l'indice X reale nel mondo
                int x = leftToRight ? (startX + i) : (endX - 1 - i);

                ParticleType p = grid[x][y];

                if (p == ParticleType.SAND) {
                    updateSand(x, y);
                } else if (p == ParticleType.WATER) {
                    updateWater(x, y);
                }
            }
        }
    }

    private void updateSand(int x, int y) {
        // Controlla se può andare dritta in giù (vuoto o acqua)
        if (canSandMoveTo(x, y + 1)) {
            swap(x, y, x, y + 1);
        } else {
            // Se non può andare dritta, prova le diagonali casualmente
            boolean leftFirst = random.nextBoolean();
            int dx1 = leftFirst ? -1 : 1;
            int dx2 = leftFirst ? 1 : -1;

            if (canSandMoveTo(x + dx1, y + 1)) {
                swap(x, y, x + dx1, y + 1);
            } else if (canSandMoveTo(x + dx2, y + 1)) {
                swap(x, y, x + dx2, y + 1);
            }
        }
    }

    private void updateWater(int x, int y) {
        // Stessa caduta della sabbia
        if (isEmpty(x, y + 1)) {
            swap(x, y, x, y + 1);
            return;
        }

        boolean leftFirst = random.nextBoolean();
        int dx1 = leftFirst ? -1 : 1;
        int dx2 = leftFirst ? 1 : -1;

        if (isEmpty(x + dx1, y + 1)) {
            swap(x, y, x + dx1, y + 1);
        } else if (isEmpty(x + dx2, y + 1)) {
            swap(x, y, x + dx2, y + 1);
        } else {
            // Se non può cadere, l'acqua SCORRE ai lati
            if (isEmpty(x + dx1, y)) swap(x, y, x + dx1, y);
            else if (isEmpty(x + dx2, y)) swap(x, y, x + dx2, y);
        }
    }

    private boolean isEmpty(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return grid[x][y] == ParticleType.EMPTY;

    }
    // La sabbia può cadere nel vuoto, ma può anche sprofondare nell'acqua
    private boolean canSandMoveTo(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        ParticleType target = grid[x][y];
        return target == ParticleType.EMPTY || target == ParticleType.WATER;
    }

    private void swap(int x1, int y1, int x2, int y2) {
        ParticleType temp = grid[x1][y1];
        grid[x1][y1] = grid[x2][y2];
        grid[x2][y2] = temp;
    }
}