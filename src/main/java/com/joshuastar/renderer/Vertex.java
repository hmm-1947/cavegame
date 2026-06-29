package com.joshuastar.renderer;

public class Vertex {
public static final int FLOATS = 9;
    public static final int BYTES = FLOATS * Float.BYTES;

    private final float x;
    private final float y;
    private final float z;

    private final float u;
    private final float v;

    private final float nx;
    private final float ny;
    private final float nz;

    private final float shade;

    public Vertex(
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            float shade) {

        this.x = x;
        this.y = y;
        this.z = z;

        this.u = u;
        this.v = v;

        this.nx = nx;
        this.ny = ny;
        this.nz = nz;

        this.shade = shade;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }

    public float getNx() {
        return nx;
    }

    public float getNy() {
        return ny;
    }

public float getNz() {
        return nz;
    }

    public float getShade() {
        return shade;
    }
}