package org.example.gamesand;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class WorldRenderer {
    private World world;
    private BufferedImage image;
    private int[] pixels;

    public WorldRenderer(World world) {
        this.world = world;
        // Crea un'immagine vuota della stessa dimensione del mondo
        image = new BufferedImage(world.getWidth(), world.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Estraiamo l'array della memoria grezza dell'immagine per scriverci velocemente
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public void render(Graphics2D g) {
        // 1. Aggiorna l'array dei pixel
        int width = world.getWidth();
        int height = world.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = x + y * width;
                pixels[index] = world.getParticle(x, y).color.getRGB();
            }
        }

        // 2. Disegna l'immagine finita
        g.drawImage(image, 0, 0, null);
    }
}