package com.joshuastar.renderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.joshuastar.entity.Cow;
import com.joshuastar.entity.Mob;
import com.joshuastar.world.BlockRegistry;
import com.joshuastar.world.Chunk;
import com.joshuastar.world.World;

public class MobManager {

    private final World world;
    private final List<Mob> mobs = new ArrayList<>();
    private final Set<Long> spawnedChunks = new HashSet<>();

    private static final int MAX_COWS_PER_CHUNK = 2;
    private static final int COW_SPAWN_CHANCE_PERCENT = 15;

    private final java.util.Random random = new java.util.Random();

    public MobManager(World world) {
        this.world = world;
    }

    public void onChunkLoaded(int chunkX, int chunkZ) {

        long key = key(chunkX, chunkZ);

        if (spawnedChunks.contains(key)) {
            return;
        }

        spawnedChunks.add(key);

        if (random.nextInt(100) >= COW_SPAWN_CHANCE_PERCENT) {
            return;
        }

        int count = 1 + random.nextInt(MAX_COWS_PER_CHUNK);

        for (int i = 0; i < count; i++) {
            trySpawnCow(chunkX, chunkZ);
        }
    }

    private void trySpawnCow(int chunkX, int chunkZ) {

        int localX = random.nextInt(Chunk.SIZE_X);
        int localZ = random.nextInt(Chunk.SIZE_Z);

        int worldX = chunkX * Chunk.SIZE_X + localX;
        int worldZ = chunkZ * Chunk.SIZE_Z + localZ;

        int groundY = findGroundY(worldX, worldZ);

        if (groundY < 0) {
            return;
        }

        short surfaceBlockId = world.getBlock(worldX, groundY - 1, worldZ);

        if (surfaceBlockId != BlockRegistry.GRASS.getId()) {
            return;
        }

        Cow cow = new Cow(worldX + 0.5f, groundY, worldZ + 0.5f);
        mobs.add(cow);
    }

    private int findGroundY(int worldX, int worldZ) {
        for (int y = Chunk.SIZE_Y - 1; y > 0; y--) {
            if (world.getBlock(worldX, y, worldZ) != 0) {
                return y + 1;
            }
        }
        return -1;
    }

    public void onChunkUnloaded(int chunkX, int chunkZ) {

        spawnedChunks.remove(key(chunkX, chunkZ));

        int minX = chunkX * Chunk.SIZE_X;
        int maxX = minX + Chunk.SIZE_X;
        int minZ = chunkZ * Chunk.SIZE_Z;
        int maxZ = minZ + Chunk.SIZE_Z;

        mobs.removeIf(mob ->
                mob.getX() >= minX && mob.getX() < maxX &&
                mob.getZ() >= minZ && mob.getZ() < maxZ);
    }

    public void update(float dt) {
        for (Mob mob : mobs) {
            mob.updateWanderAI(world, dt);
            mob.tickPhysics(world);
        }
    }

    public List<Mob> getMobs() {
        return mobs;
    }

    private long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }
}