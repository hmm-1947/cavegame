package com.joshuastar.world;

public class Chunk {

    public static final int SIZE_X = 16;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 16;

    private final int chunkX;
    private final int chunkZ;

private final short[] blocks;
    private final byte[] skyLight;
    private final byte[] blockLight;

    private boolean dirty = true;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new short[SIZE_X * SIZE_Y * SIZE_Z];
        this.skyLight = new byte[SIZE_X * SIZE_Y * SIZE_Z];
        this.blockLight = new byte[SIZE_X * SIZE_Y * SIZE_Z];
    }

    private int index(int x, int y, int z) {
        return x + SIZE_X * (z + SIZE_Z * y);
    }

    public void setBlock(int x, int y, int z, short id) {

        if (x < 0 || x >= SIZE_X) return;
        if (y < 0 || y >= SIZE_Y) return;
        if (z < 0 || z >= SIZE_Z) return;

        blocks[index(x, y, z)] = id;
        dirty = true;
    }
public short getBlock(int x, int y, int z) {

        if (x < 0 || x >= SIZE_X) return 0;
        if (y < 0 || y >= SIZE_Y) return 0;
        if (z < 0 || z >= SIZE_Z) return 0;

        return blocks[index(x, y, z)];
    }

    public int getSkyLight(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X) return 0;
        if (y < 0 || y >= SIZE_Y) return 0;
        if (z < 0 || z >= SIZE_Z) return 0;
        return skyLight[index(x, y, z)] & 0xFF;
    }

    public void setSkyLight(int x, int y, int z, int value) {
        if (x < 0 || x >= SIZE_X) return;
        if (y < 0 || y >= SIZE_Y) return;
        if (z < 0 || z >= SIZE_Z) return;
        skyLight[index(x, y, z)] = (byte) value;
    }

    public int getBlockLight(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X) return 0;
        if (y < 0 || y >= SIZE_Y) return 0;
        if (z < 0 || z >= SIZE_Z) return 0;
        return blockLight[index(x, y, z)] & 0xFF;
    }

    public void setBlockLight(int x, int y, int z, int value) {
        if (x < 0 || x >= SIZE_X) return;
        if (y < 0 || y >= SIZE_Y) return;
        if (z < 0 || z >= SIZE_Z) return;
        blockLight[index(x, y, z)] = (byte) value;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public short[] getBlocks() {
        return blocks;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }
}