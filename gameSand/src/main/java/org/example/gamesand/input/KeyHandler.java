package org.example.gamesand.input;

import org.example.gamesand.world.ParticleType;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyHandler extends KeyAdapter {
    // Variabili pubbliche per poterle leggere facilmente da fuori
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public ParticleType currentBrush = ParticleType.SAND;
    public boolean spacePressed;

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // Movimento
        if (code == KeyEvent.VK_W) upPressed = true;
        if (code == KeyEvent.VK_A) leftPressed = true;
        if (code == KeyEvent.VK_S) downPressed = true;
        if (code == KeyEvent.VK_D) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;

        // Selezione elementi
        if (code == KeyEvent.VK_1) currentBrush = ParticleType.SAND;
        if (code == KeyEvent.VK_2) currentBrush = ParticleType.WATER;
        if (code == KeyEvent.VK_3) currentBrush = ParticleType.STONE;
        if (code == KeyEvent.VK_0) currentBrush = ParticleType.EMPTY;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Rilascia movimento
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
    }
}