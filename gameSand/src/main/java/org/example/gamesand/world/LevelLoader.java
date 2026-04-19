package org.example.gamesand.world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class LevelLoader {

    public static ParticleType[][] loadCollisionMask(String path) {
        try {
            InputStream is = LevelLoader.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Errore: Immagine maschera non trovata in " + path);
                return null;
            }
            BufferedImage image = ImageIO.read(is);
            int width = image.getWidth();
            int height = image.getHeight();
            ParticleType[][] grid = new ParticleType[width][height];

            // Riferimento colore Pietra (ignora la trasparenza per sicurezza)
            int stoneRGB = ParticleType.STONE.color.getRGB() & 0x00FFFFFF;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixelRGB = image.getRGB(x, y) & 0x00FFFFFF;

                    // Se il pixel corrisponde alla pietra, metti la pietra
                    if (pixelRGB == stoneRGB) {
                        grid[x][y] = ParticleType.STONE;
                    } else {
                        grid[x][y] = ParticleType.EMPTY;
                    }
                }
            }
            System.out.println("Mappa caricata: " + width + "x" + height);
            return grid;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage loadBackground(String path) {
        try {
            InputStream is = LevelLoader.class.getResourceAsStream(path);
            return is != null ? ImageIO.read(is) : null;
        } catch (Exception e) {
            return null;
        }
    }
}