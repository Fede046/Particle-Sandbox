package org.example.gamesand.world;

import org.example.gamesand.core.Camera;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class WorldRenderer {
    private World world;
    private BufferedImage image;
    private int[] pixels;
    private int screenWidth, screenHeight;

    public WorldRenderer(World world, int screenWidth, int screenHeight) {
        this.world = world;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // CAMBIO FONDAMENTALE: TYPE_INT_ARGB al posto di TYPE_INT_RGB
        image = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public void render(Graphics2D g, Camera camera) {
        for (int y = 0; y < screenHeight; y++) {
            for (int x = 0; x < screenWidth; x++) {
                // Calcoliamo quale pixel del mondo corrisponde a questo pixel dello schermo
                int worldX = x + (int)camera.x;
                int worldY = y + (int)camera.y;

                pixels[x + y * screenWidth] = world.getParticle(worldX, worldY).color.getRGB();
            }
        }
        g.drawImage(image, 0, 0, null);
    }
}