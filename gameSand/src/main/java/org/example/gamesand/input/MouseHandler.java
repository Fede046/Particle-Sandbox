package org.example.gamesand.input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {
    public boolean isPressed = false;
    public boolean isRightClick = false;
    public int x = 0, y = 0;

    @Override
    public void mousePressed(MouseEvent e) {
        isPressed = true;
        x = e.getX();
        y = e.getY();
        // Controlla se è il tasto destro
        isRightClick = (e.getButton() == MouseEvent.BUTTON3);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isPressed = false;
        isRightClick = false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }
}