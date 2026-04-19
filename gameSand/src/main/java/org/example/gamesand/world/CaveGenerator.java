package org.example.gamesand.world;

import java.util.Random;

public class CaveGenerator {

    private static final Random random = new Random();

    /**
     * Genera una nuova griglia a tema caverna usando gli Automi Cellulari.
     */
    public static ParticleType[][] generate(int width, int height) {
        ParticleType[][] grid = new ParticleType[width][height];

        // 1. Inizializzazione casuale (Rumore di base)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Creiamo dei bordi solidi
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    grid[x][y] = ParticleType.STONE;
                } else {
                    // 45% di probabilità che una cella parta come PIETRA
                    grid[x][y] = random.nextFloat() < 0.45f ? ParticleType.STONE : ParticleType.EMPTY;
                }
            }
        }

        // 2. Smussamento (Cellular Automata) per 5 generazioni
        for (int i = 0; i < 5; i++) {
            grid = smoothCave(grid, width, height);
        }

        return grid;
    }

    private static ParticleType[][] smoothCave(ParticleType[][] oldGrid, int width, int height) {
        ParticleType[][] newGrid = new ParticleType[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int neighborWallCount = getSurroundingWallCount(oldGrid, x, y, width, height);

                // Regole di sopravvivenza della cella
                if (neighborWallCount > 4) {
                    newGrid[x][y] = ParticleType.STONE;
                } else if (neighborWallCount < 4) {
                    newGrid[x][y] = ParticleType.EMPTY;
                } else {
                    newGrid[x][y] = oldGrid[x][y]; // Mantiene lo stato attuale
                }
            }
        }
        return newGrid;
    }

    private static int getSurroundingWallCount(ParticleType[][] grid, int gridX, int gridY, int width, int height) {
        int wallCount = 0;
        for (int neighborX = gridX - 1; neighborX <= gridX + 1; neighborX++) {
            for (int neighborY = gridY - 1; neighborY <= gridY + 1; neighborY++) {
                // Controllo dei bordi
                if (neighborX >= 0 && neighborX < width && neighborY >= 0 && neighborY < height) {
                    if (neighborX != gridX || neighborY != gridY) {
                        if (grid[neighborX][neighborY] == ParticleType.STONE) {
                            wallCount++;
                        }
                    }
                } else {
                    // I bordi esterni fuori dallo schermo contano come muri
                    wallCount++;
                }
            }
        }
        return wallCount;
    }
}