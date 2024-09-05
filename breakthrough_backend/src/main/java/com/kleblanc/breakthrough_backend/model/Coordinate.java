package com.kleblanc.breakthrough_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Un record génère automatiquement les champs en final private, un getter pour chaque champ,
// un constructeur public, une fonction toString, une fonction equals, et une fonction hashCode.
public record Coordinate(int x, int y) {
    @JsonIgnore
    public boolean isOnBoard() {
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }
    @JsonIgnore
    public boolean isOffBoard() {
        return x < 0 || x > 7 || y < 0 || y > 7;
    }
}
