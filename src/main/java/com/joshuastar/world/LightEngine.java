package com.joshuastar.world;

import java.util.ArrayDeque;
import java.util.Deque;

public final class LightEngine {

    private LightEngine() {
    }
public static void calculateSkyLight(World world, Chunk chunk) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        Deque<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {

                int wx = worldX + x;
                int wz = worldZ + z;

                int topY = Chunk.SIZE_Y - 1;

                while (topY > 0 && world.getBlock(wx, topY, wz) == 0) {
                    topY--;
                }

                for (int y = Chunk.SIZE_Y - 1; y > topY; y--) {
                    world.setSkyLight(wx, y, wz, 15);
                    queue.add(new int[] { wx, y, wz });
                }
            }
        }

        propagate(world, queue, true);
    }

    public static void calculateBlockLight(World world, Chunk chunk) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        Deque<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    int emission = BlockRegistry.get(chunk.getBlock(x, y, z)).getLightEmission();

                    if (emission > 0) {
                        int wx = worldX + x;
                        int wz = worldZ + z;
                        world.setBlockLight(wx, y, wz, emission);
                        queue.add(new int[] { wx, y, wz });
                    }
                }
            }
        }

        propagate(world, queue, false);
    }

    public static void relightChunkBorders(World world, Chunk chunk) {

        int[] dx = { 1, -1, 0, 0 };
        int[] dz = { 0, 0, 1, -1 };

        for (int i = 0; i < 4; i++) {

            Chunk neighbor = world.getChunk(chunk.getChunkX() + dx[i], chunk.getChunkZ() + dz[i]);

            if (neighbor == null) {
                continue;
            }

            Deque<int[]> skyQueue = new ArrayDeque<>();
            Deque<int[]> blockQueue = new ArrayDeque<>();

            int nx = neighbor.getChunkX() * Chunk.SIZE_X;
            int nz = neighbor.getChunkZ() * Chunk.SIZE_Z;

            int bx = (dx[i] == 1) ? Chunk.SIZE_X - 1 : (dx[i] == -1 ? 0 : -1);
            int bz = (dz[i] == 1) ? Chunk.SIZE_Z - 1 : (dz[i] == -1 ? 0 : -1);

            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                if (bx >= 0) {
                    for (int z = 0; z < Chunk.SIZE_Z; z++) {
                        int wx = nx + bx;
                        int wz = nz + z;
                        if (world.getSkyLight(wx, y, wz) > 0) skyQueue.add(new int[] { wx, y, wz });
                        if (world.getBlockLight(wx, y, wz) > 0) blockQueue.add(new int[] { wx, y, wz });
                    }
                } else {
                    for (int x = 0; x < Chunk.SIZE_X; x++) {
                        int wx = nx + x;
                        int wz = nz + bz;
                        if (world.getSkyLight(wx, y, wz) > 0) skyQueue.add(new int[] { wx, y, wz });
                        if (world.getBlockLight(wx, y, wz) > 0) blockQueue.add(new int[] { wx, y, wz });
                    }
                }
            }

            propagate(world, skyQueue, true);
            propagate(world, blockQueue, false);
        }
    }

    public static void onBlockChanged(World world, int x, int y, int z) {

        for (boolean sky : new boolean[] { true, false }) {

            int oldLevel = sky ? world.getSkyLight(x, y, z) : world.getBlockLight(x, y, z);

            if (sky) {
                world.setSkyLight(x, y, z, 0);
            } else {
                world.setBlockLight(x, y, z, 0);
            }

            Deque<int[]> removeQueue = new ArrayDeque<>();
            removeQueue.add(new int[] { x, y, z, oldLevel });
            removeLight(world, removeQueue, sky);
        }

        Deque<int[]> blockQueue = new ArrayDeque<>();
        int emission = BlockRegistry.get(world.getBlock(x, y, z)).getLightEmission();

        if (emission > 0) {
            world.setBlockLight(x, y, z, emission);
            blockQueue.add(new int[] { x, y, z });
        }

        propagate(world, blockQueue, false);

        Deque<int[]> skyQueue = new ArrayDeque<>();

        if (isExposedToSky(world, x, y, z)) {
            world.setSkyLight(x, y, z, 15);
            skyQueue.add(new int[] { x, y, z });
        }

        propagate(world, skyQueue, true);
    }

    private static void removeLight(World world, Deque<int[]> queue, boolean sky) {

        Deque<int[]> repropagate = new ArrayDeque<>();

        while (!queue.isEmpty()) {

            int[] p = queue.poll();
            int lightLevel = p[3];

            for (int[] n : neighbors6(p[0], p[1], p[2])) {

                if (n[1] < 0 || n[1] >= Chunk.SIZE_Y) {
                    continue;
                }

                int neighborLight = sky
                        ? world.getSkyLight(n[0], n[1], n[2])
                        : world.getBlockLight(n[0], n[1], n[2]);

                if (neighborLight != 0 && neighborLight < lightLevel) {

                    if (sky) {
                        world.setSkyLight(n[0], n[1], n[2], 0);
                    } else {
                        world.setBlockLight(n[0], n[1], n[2], 0);
                    }

                    queue.add(new int[] { n[0], n[1], n[2], neighborLight });

                } else if (neighborLight >= lightLevel) {
                    repropagate.add(new int[] { n[0], n[1], n[2] });
                }
            }
        }

        propagate(world, repropagate, sky);
    }

    private static void propagate(World world, Deque<int[]> queue, boolean sky) {

        while (!queue.isEmpty()) {

            int[] pos = queue.poll();
            int x = pos[0];
            int y = pos[1];
            int z = pos[2];

            int light = sky ? world.getSkyLight(x, y, z) : world.getBlockLight(x, y, z);

            for (int[] n : neighbors6(x, y, z)) {

                if (n[1] < 0 || n[1] >= Chunk.SIZE_Y) {
                    continue;
                }

                int opacity = world.getLightOpacity(n[0], n[1], n[2]);
                int newLight = Math.max(light - opacity, 0);

                int current = sky
                        ? world.getSkyLight(n[0], n[1], n[2])
                        : world.getBlockLight(n[0], n[1], n[2]);

                if (newLight > current) {

                    if (sky) {
                        world.setSkyLight(n[0], n[1], n[2], newLight);
                    } else {
                        world.setBlockLight(n[0], n[1], n[2], newLight);
                    }

                    queue.add(n);
                }
            }
        }
    }

    private static boolean isExposedToSky(World world, int x, int y, int z) {

        if (world.getLightOpacity(x, y, z) >= 15) {
            return false;
        }

        for (int yy = y + 1; yy < Chunk.SIZE_Y; yy++) {
            if (world.getLightOpacity(x, yy, z) >= 15) {
                return false;
            }
        }

        return true;
    }

    private static int[][] neighbors6(int x, int y, int z) {
        return new int[][] {
                { x + 1, y, z }, { x - 1, y, z },
                { x, y + 1, z }, { x, y - 1, z },
                { x, y, z + 1 }, { x, y, z - 1 }
        };
    }
}