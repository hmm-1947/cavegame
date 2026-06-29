package com.joshuastar.renderer;

public class UIVertex {

    public static final int FLOATS = 4;
    public static final int BYTES = FLOATS * Float.BYTES;

    private final float x;
    private final float y;
    private final float u;
    private final float v;

    public UIVertex(float x, float y, float u, float v) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }
}