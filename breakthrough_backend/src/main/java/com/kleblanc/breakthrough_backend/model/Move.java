package com.kleblanc.breakthrough_backend.model;

// Un record génère automatiquement les champs en final private, un getter pour chaque champ,
// un constructeur public, une fonction toString, une fonction equals, et une fonction hashCode.
public record Move(Coordinate source, Coordinate target) implements iMove { }
