package com.kleblanc.breakthrough_backend.model;

import java.io.Serializable;

public interface iMove extends Serializable {
    public Coordinate source();
    public Coordinate target();
}
