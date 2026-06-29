package com.joshuastar.world;

import java.util.Map;

public class World {

   private final Map<Long, Chunk> chunks = new java.util.concurrent.ConcurrentHashMap<>();
    private final WorldGenerator generator = new WorldGenerator();

    public World() {
        loadChunk(0, 0);
    }

    public Chunk loadChunk(int chunkX, int chunkZ) {

        long key = key(chunkX, chunkZ);

        Chunk chunk = chunks.get(key);

        if (chunk != null) {
            return chunk;
        }

chunk = new Chunk(chunkX, chunkZ);

        generator.generate(chunk);

        chunks.put(key, chunk);

 LightEngine.calculateSkyLight(this, chunk);
        LightEngine.calculateBlockLight(this, chunk);
        LightEngine.relightChunkBorders(this, chunk);

        return chunk;
    }

public Chunk getChunk(int chunkX, int chunkZ) {
        return chunks.get(key(chunkX, chunkZ));
    }

    public void unloadChunk(int chunkX, int chunkZ) {
        chunks.remove(key(chunkX, chunkZ));
    }
public short getBlock(int worldX, int worldY, int worldZ) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return 0;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

        return chunk.getBlock(localX, worldY, localZ);
    }

    public void setBlock(int worldX, int worldY, int worldZ, short id) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

chunk.setBlock(localX, worldY, localZ, id);
        LightEngine.onBlockChanged(this, worldX, worldY, worldZ);
    }

    public int getSkyLight(int worldX, int worldY, int worldZ) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return 0;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

        return chunk.getSkyLight(localX, worldY, localZ);
    }

    public void setSkyLight(int worldX, int worldY, int worldZ, int value) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

        chunk.setSkyLight(localX, worldY, localZ, value);
    }

    public int getBlockLight(int worldX, int worldY, int worldZ) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return 0;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

        return chunk.getBlockLight(localX, worldY, localZ);
    }

    public void setBlockLight(int worldX, int worldY, int worldZ, int value) {

        int chunkX = Math.floorDiv(worldX, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(worldZ, Chunk.SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return;

        int localX = worldX - chunkX * Chunk.SIZE_X;
        int localZ = worldZ - chunkZ * Chunk.SIZE_Z;

        chunk.setBlockLight(localX, worldY, localZ, value);
    }

    public int getLightOpacity(int worldX, int worldY, int worldZ) {
        return BlockRegistry.get(getBlock(worldX, worldY, worldZ)).getLightOpacity();
    }

    private long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }
}