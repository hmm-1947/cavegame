package com.joshuastar.renderer;

public final class TextureAtlas {

    public static final int ATLAS_SIZE = 16;
    public static final float TILE_SIZE = 1.0f / ATLAS_SIZE;

    private TextureAtlas() {
    }

    public static float getU0(int tile) {
        return (tile % ATLAS_SIZE) * TILE_SIZE;
    }

    public static float getV0(int tile) {
        return (tile / ATLAS_SIZE) * TILE_SIZE;
    }

    public static float getU1(int tile) {
        return getU0(tile) + TILE_SIZE;
    }

    public static float getV1(int tile) {
        return getV0(tile) + TILE_SIZE;
    }
}