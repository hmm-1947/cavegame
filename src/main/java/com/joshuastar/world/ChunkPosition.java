package com.joshuastar.world;

public final class ChunkPosition {

    private final int x;
    private final int z;

    public ChunkPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ChunkPosition other)) {
            return false;
        }

        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * x + z;
    }
}