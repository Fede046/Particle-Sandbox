package org.example.gamesand.world;

import java.awt.Color;

public enum ParticleType {
    EMPTY(new Color(0, 0, 0, 0)), // Ultimo zero = totalmente trasparente
    STONE(new Color(120, 120, 120)),    // Grigio
    SAND(new Color(194, 178, 128)),     // Giallo sabbia
    WATER(new Color(35, 137, 218));     // Blu acqua

    public final Color color;

    ParticleType(Color color) {
        this.color = color;
    }
}