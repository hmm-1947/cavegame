package com.joshuastar.world;

public class WorldGenerator {

    private final Noise noise = new Noise(12345L);

    public void generate(Chunk chunk) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        int[] surface = new int[Chunk.SIZE_X * Chunk.SIZE_Z];

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {

                int wx = worldX + x;
                int wz = worldZ + z;

                int top = -1;

                for (int y = 0; y < Chunk.SIZE_Y; y++) {

                    double density = noise.density(wx, y, wz);

                    if (density > 0.0) {

                        chunk.setBlock(x, y, z, (short) 3);

                        if (y > top) {
                            top = y;
                        }

                    } else {

                        chunk.setBlock(x, y, z, (short) 0);

                    }
                }

                if (top < 1) {
                    top = 1;
                }

                surface[x + z * Chunk.SIZE_X] = top;
            }
        }

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {

                int wx = worldX + x;
                int wz = worldZ + z;

                int height = surface[x + z * Chunk.SIZE_X];

                chunk.setBlock(x, 0, z, (short) 4);

                short surfaceBlock = surfaceBlockFor(
                        noise.temperature(wx, wz),
                        noise.humidity(wx, wz));

                int dirtDepth = 3 + noise.dirtDepthVariation(wx, wz);


for (int y = height - dirtDepth; y < height; y++) {

    if (y <= 0) {
        continue;
    }

    chunk.setBlock(x, y, z, (short) 2);
}
                chunk.setBlock(x, height, z, surfaceBlock);

                if (surfaceBlock == 1
                        && x >= 2
                        && x <= 13
                        && z >= 2
                        && z <= 13
                        && height + 8 < Chunk.SIZE_Y
                        && noise.isTreeColumn(wx, wz)) {

                    placeTree(chunk, x, z, height + 1, wx, wz);
                }
            }
        }
        carveCaves(chunk);
        chunk.setDirty(true);
    }

    private void carveCaves(Chunk chunk) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        java.util.Random random = noise.caveRandom(chunk.getChunkX(), chunk.getChunkZ());

        int caves = noise.caveCount(chunk.getChunkX(), chunk.getChunkZ());

        if (random.nextInt(5) == 0) {
            caves += random.nextInt(4);
        }

        for (int i = 0; i < caves; i++) {

            double x = worldX + random.nextInt(16);
            double t = random.nextDouble();
            t *= t;
            double y = 8 + t * 112.0;
            double z = worldZ + random.nextInt(16);
            if (random.nextInt(4) == 0) {

                carveSphere(
                        chunk,
                        x,
                        y,
                        z,
                        3.5 + random.nextDouble() * 4.5);
            }

            float yaw = random.nextFloat() * ((float) Math.PI * 2.0f);
            float pitch = (random.nextFloat() - 0.5f) * 0.25f;
            float radius = 1.2f + random.nextFloat() * 3.2f;

            if (random.nextInt(8) == 0) {
                radius *= 1.8f;
            }
            int length = 80 + random.nextInt(140);

            carveTunnel(
                    chunk,
                    x,
                    y,
                    z,
                    yaw,
                    pitch,
                    radius,
                    length,
                    random);
        }
    }

    private void carveTunnel(
            Chunk chunk,
            double x,
            double y,
            double z,
            float yaw,
            float pitch,
            float radius,
            int length,
            java.util.Random random) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        float yawVelocity = 0.0f;
        float pitchVelocity = 0.0f;
        if (random.nextInt(6) == 0) {
            radius *= 1.4f;
        }
        for (int step = 0; step < length; step++) {

            double profile = Math.sin(step * Math.PI / length);

            double r = radius *
                    (0.75 + profile * 0.35);

            r *= 0.85 + random.nextDouble() * 0.3;
            radius += (random.nextFloat() - random.nextFloat()) * 0.08f;

            if (radius < 1.0f) {
                radius = 1.0f;
            }

            if (radius > 5.5f) {
                radius = 5.5f;
            }
            carveSphere(chunk, x, y, z, r);

            if (random.nextInt(120) == 0) {
                carveSphere(
                        chunk,
                        x,
                        y,
                        z,
                        r * (2.5 + random.nextDouble() * 2.5));
            }
            if (step > length / 4
                    && random.nextInt(18) == 0
                    && radius > 1.2f) {

                carveTunnel(
                        chunk,
                        x,
                        y,
                        z,
                        yaw + (random.nextFloat() - 0.5f) * 1.6f,
                        pitch * 0.5f,
                        radius * (0.6f + random.nextFloat() * 0.3f),
                        length - step,
                        new java.util.Random(random.nextLong()));

                carveTunnel(
                        chunk,
                        x,
                        y,
                        z,
                        yaw - (random.nextFloat() - 0.5f) * 1.6f,
                        pitch * 0.5f,
                        radius * (0.6f + random.nextFloat() * 0.3f),
                        length - step,
                        new java.util.Random(random.nextLong()));

                return;
            }
            double speed = 0.85 + random.nextDouble() * 0.3;

            x += Math.cos(yaw) * Math.cos(pitch) * speed;
            y += Math.sin(pitch) * speed;
            z += Math.sin(yaw) * Math.cos(pitch) * speed;

            pitchVelocity += (random.nextFloat() - random.nextFloat()) * 0.025f;
            yawVelocity += (random.nextFloat() - random.nextFloat()) * 0.045f;

            pitchVelocity *= 0.92f;
            yawVelocity *= 0.88f;
            pitch += pitchVelocity;
            yaw += yawVelocity;

            if (x < worldX - 8 || x > worldX + 24)
                break;
            if (z < worldZ - 8 || z > worldZ + 24)
                break;
            if (y < 5)
                break;

            if (y > 140 && random.nextFloat() < 0.12f) {
                break;
            }
        }
    }

    private void carveSphere(
            Chunk chunk,
            double cx,
            double cy,
            double cz,
            double radius) {

        int worldX = chunk.getChunkX() * Chunk.SIZE_X;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z;

        int minX = (int) Math.floor(cx - radius);
        int maxX = (int) Math.ceil(cx + radius);

        int minY = (int) Math.floor(cy - radius);
        int maxY = (int) Math.ceil(cy + radius);

        int minZ = (int) Math.floor(cz - radius);
        int maxZ = (int) Math.ceil(cz + radius);

        double verticalRadius = radius * 0.75;

        double radiusSq = radius * radius;
        double verticalSq = verticalRadius * verticalRadius;

        for (int wx = minX; wx <= maxX; wx++) {
            for (int wy = minY; wy <= maxY; wy++) {
                for (int wz = minZ; wz <= maxZ; wz++) {

                    double dx = wx + 0.5 - cx;
                    double dy = wy + 0.5 - cy;
                    double dz = wz + 0.5 - cz;

                    if ((dx * dx) / radiusSq
                            + (dy * dy) / verticalSq
                            + (dz * dz) / radiusSq > 1.0) {
                        continue;
                    }

                    int lx = wx - worldX;
                    int lz = wz - worldZ;

                    if (lx < 0 || lx >= Chunk.SIZE_X)
                        continue;
                    if (lz < 0 || lz >= Chunk.SIZE_Z)
                        continue;
                    if (wy < 1 || wy >= Chunk.SIZE_Y - 1)
                        continue;

                    if (chunk.getBlock(lx, wy, lz) == 4) {
                        continue;
                    }

                    chunk.setBlock(lx, wy, lz, (short) 0);
                }
            }
        }
    }

    private void placeTree(Chunk chunk, int x, int z, int baseY, int wx, int wz) {

        int trunkHeight = noise.treeTrunkHeight(wx, wz);

        for (int y = 0; y < trunkHeight; y++) {
            chunk.setBlock(x, baseY + y, z, (short) 7);
        }

        int topY = baseY + trunkHeight;

        for (int dy = -2; dy <= 0; dy++) {
            int radius = (dy == 0) ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0)
                        continue;
                    if (radius == 2 && Math.abs(dx) == 2 && Math.abs(dz) == 2)
                        continue;
                    chunk.setBlock(x + dx, topY + dy, z + dz, (short) 8);
                }
            }
        }

        chunk.setBlock(x, topY, z, (short) 8);
        chunk.setBlock(x, topY + 1, z, (short) 8);
    }

    private short surfaceBlockFor(double temp, double humidity) {
        if (temp < -0.3)
            return 6;
        if (temp > 0.3 && humidity < -0.1)
            return 5;
        return 1;
    }
}