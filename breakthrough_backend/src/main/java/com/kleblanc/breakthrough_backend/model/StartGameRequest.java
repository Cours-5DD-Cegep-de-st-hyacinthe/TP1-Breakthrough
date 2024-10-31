package com.kleblanc.breakthrough_backend.model;

public record StartGameRequest(String whitePlayerId, String blackPlayerId, int moveTimeout) {}
