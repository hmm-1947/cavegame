package com.joshuastar.world;

import java.util.HashSet;
import java.util.Set;

public class ChunkLoader {

    private final World world;

    private final Set<ChunkPosition> loadedChunks = new HashSet<>();

    public ChunkLoader(World world) {
        this.world = world;
    }

    public void update(int playerBlockX, int playerBlockZ, int renderDistance) {

        int playerChunkX = Math.floorDiv(playerBlockX, Chunk.SIZE_X);
        int playerChunkZ = Math.floorDiv(playerBlockZ, Chunk.SIZE_Z);

        for (int x = playerChunkX - renderDistance; x <= playerChunkX + renderDistance; x++) {
            for (int z = playerChunkZ - renderDistance; z <= playerChunkZ + renderDistance; z++) {

                ChunkPosition position = new ChunkPosition(x, z);

                if (loadedChunks.contains(position)) {
                    continue;
                }

                world.loadChunk(x, z);

                loadedChunks.add(position);
            }
        }
    }

    public Set<ChunkPosition> getLoadedChunks() {
        return loadedChunks;
    }
}