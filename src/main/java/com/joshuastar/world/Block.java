package com.joshuastar.world;

public class Block {

private final short id;
    private final int topTexture;
    private final int sideTexture;
    private final int bottomTexture;
    private final int lightOpacity;
    private final int lightEmission;

    public Block(short id, int topTexture, int sideTexture, int bottomTexture) {
        this(id, topTexture, sideTexture, bottomTexture, 16, 0);
    }

    public Block(short id, int topTexture, int sideTexture, int bottomTexture, int lightOpacity, int lightEmission) {
        this.id = id;
        this.topTexture = topTexture;
        this.sideTexture = sideTexture;
        this.bottomTexture = bottomTexture;
        this.lightOpacity = lightOpacity;
        this.lightEmission = lightEmission;
    }

    public short getId() {
        return id;
    }

    public boolean isAir() {
        return id == 0;
    }

    public boolean isSolid() {
        return id != 0;
    }

    public int getTopTexture() {
        return topTexture;
    }

    public int getSideTexture() {
        return sideTexture;
    }

public int getBottomTexture() {
        return bottomTexture;
    }

    public int getLightOpacity() {
        return lightOpacity;
    }

    public int getLightEmission() {
        return lightEmission;
    }
}