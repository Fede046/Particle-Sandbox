package org.example.gamesand;

import java.util.Random;

public class World {
    private final int width;
    private final int height;
    private ParticleType[][] grid;
    private Random random = new Random();

    public World(int width, int height) {
        // Definiamo dimensioni arbitrarie molto più grandi dello schermo
        this.width = 3200;
        this.height = 1200;
        this.grid = new ParticleType[this.width][this.height];

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                // Pavimento di pietra in fondo al mondo grande
                if (y > this.height - 50) {
                    grid[x][y] = ParticleType.STONE;
                } else {
                    grid[x][y] = ParticleType.EMPTY;
                }
            }
        }
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
    public void update() {
        // Bisogna aggiornare dal BASSO verso L'ALTO, altrimenti le particelle
        // cadono 600 pixel in un solo frame!
        for (int y = height - 2; y >= 0; y--) {
            // Randomizziamo se leggere da sinistra a destra o viceversa
            // per evitare che i liquidi scorrano solo da un lato
            boolean leftToRight = random.nextBoolean();

            for (int i = 0; i < width; i++) {
                int x = leftToRight ? i : width - 1 - i;
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