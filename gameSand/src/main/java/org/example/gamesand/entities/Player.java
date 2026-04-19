package org.example.gamesand.entities; // Stesso package di Entity

import org.example.gamesand.world.World;
import java.awt.Color;

public class Player extends Entity {

    public Player(float startX, float startY) {
        // Chiama il costruttore della classe "padre" passando le sue dimensioni fisse (12x24)
        super(startX, startY, 12, 24);

        // Personalizza il colore del giocatore
        this.color = Color.RED;
    }

    public void update(World world, boolean up, boolean left, boolean right) {
        // 1. Gestione Input Specifica del Giocatore
        if (left) vx = -speed;
        else if (right) vx = speed;
        else vx = 0;

        // Salto
        if (up && grounded) {
            vy = jumpForce;
            grounded = false;
        }

        // 2. Chiama la fisica universale ereditata!
        applyPhysics(world);
    }
}