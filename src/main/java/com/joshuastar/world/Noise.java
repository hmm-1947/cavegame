package com.joshuastar.world;

public class Noise {

    private final int[] permutation = new int[512];
    private final long seed;

    public Noise(long seed) {
        this.seed = seed;

        int[] p = new int[256];

        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        java.util.Random random = new java.util.Random(seed);

        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);

            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }

        for (int i = 0; i < 512; i++) {
            permutation[i] = p[i & 255];
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {

        switch (hash & 15) {
            case 0: return  x + y;
            case 1: return -x + y;
            case 2: return  x - y;
            case 3: return -x - y;
            case 4: return  x + z;
            case 5: return -x + z;
            case 6: return  x - z;
            case 7: return -x - z;
            case 8: return  y + z;
            case 9: return -y + z;            case 10: return  y - z;
            case 11: return -y - z;
            case 12: return  y + x;
            case 13: return -y + z;
            case 14: return  y - x;
            default: return -y - z;
        }
    }

    private double perlin(double x, double y, double z) {

        int X = ((int)Math.floor(x)) & 255;
        int Y = ((int)Math.floor(y)) & 255;
        int Z = ((int)Math.floor(z)) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int A = permutation[X] + Y;
        int AA = permutation[A] + Z;
        int AB = permutation[A + 1] + Z;

        int B = permutation[X + 1] + Y;
        int BA = permutation[B] + Z;
        int BB = permutation[B + 1] + Z;

        return lerp(
            lerp(
                lerp(
                    grad(permutation[AA], x, y, z),
                    grad(permutation[BA], x - 1, y, z),
                    u
                ),
                lerp(
                    grad(permutation[AB], x, y - 1, z),
                    grad(permutation[BB], x - 1, y - 1, z),
                    u
                ),
                v
            ),
            lerp(
                lerp(
                    grad(permutation[AA + 1], x, y, z - 1),
                    grad(permutation[BA + 1], x - 1, y, z - 1),
                    u
                ),
                lerp(
                    grad(permutation[AB + 1], x, y - 1, z - 1),
                    grad(permutation[BB + 1], x - 1, y - 1, z - 1),
                    u
                ),
                v
            ),
            w
        );
    }    public double octaveNoise(
            double x,
            double y,
            double z,
            int octaves,
            double persistence,
            double scale) {

        double total = 0.0;
        double amplitude = 1.0;
        double frequency = scale;
        double max = 0.0;

        for (int i = 0; i < octaves; i++) {
            total += perlin(
                    x * frequency,
                    y * frequency,
                    z * frequency) * amplitude;

            max += amplitude;
            amplitude *= persistence;
            frequency *= 2.0;
        }

        return total / max;
    }

    private double bezier(double t, double y1, double y2, double y3) {
        double u = 1.0 - t;
        return 3.0 * u * u * t * y1
                + 3.0 * u * t * t * y2
                + t * t * t * y3;
    }

    public double continentalness(double x, double z) {
        return octaveNoise(x + 10000.0, 0.0, z + 10000.0, 4, 0.5, 0.002);
    }

    public double erosion(double x, double z) {
        return octaveNoise(x + 20000.0, 0.0, z + 20000.0, 4, 0.5, 0.004);
    }

    public double peaksValleys(double x, double z) {
        return octaveNoise(x + 30000.0, 0.0, z + 30000.0, 3, 0.5, 0.010);
    }

    public double temperature(double x, double z) {
        return octaveNoise(x + 40000.0, 0.0, z + 40000.0, 3, 0.5, 0.0015);
    }

    public double humidity(double x, double z) {
        return octaveNoise(x + 50000.0, 0.0, z + 50000.0, 3, 0.5, 0.0015);
    }

    public double caveNoise(double x, double y, double z) {
        return octaveNoise(x, y, z, 5, 0.5, 0.02);
    }

    public double caveDetailNoise(double x, double y, double z) {
        return octaveNoise(x + 5000.0, y + 5000.0, z + 5000.0, 3, 0.5, 0.06);
    }

public double density(double x, double y, double z) {

    double cNorm = (continentalness(x, z) + 1.0) * 0.5;
    double eNorm = (erosion(x, z) + 1.0) * 0.5;
    double pv = peaksValleys(x, z);

    double baseHeight = 32.0 + bezier(cNorm, 0.3, 0.7, 1.0) * 96.0;
    double flatness = 1.0 - bezier(eNorm, 0.2, 0.6, 0.9);
    double terrainHeight = baseHeight + pv * flatness * 24.0;

    double terrainDensity = terrainHeight - y;

    double spaghetti =
            Math.abs(octaveNoise(
                    x,
                    y,
                    z,
                    5,
                    0.5,
                    0.018));

    double cheese =
            octaveNoise(
                    x + 4000.0,
                    y + 4000.0,
                    z + 4000.0,
                    4,
                    0.5,
                    0.008);

    double pillars =
            octaveNoise(
                    x + 8000.0,
                    y,
                    z + 8000.0,
                    3,
                    0.5,
                    0.04);

    if (spaghetti < 0.035) {
        terrainDensity -= 40.0;
    }

    if (cheese > 0.45) {
        terrainDensity -= (cheese - 0.45) * 70.0;
    }

    if (pillars > 0.65 && y < 80) {
        terrainDensity += (pillars - 0.65) * 35.0;
    }

    return terrainDensity;
}    public int caveSeed(int chunkX, int chunkZ) {

        long n = chunkX * 341873128712L
                + chunkZ * 132897987541L
                + seed;

        n ^= (n << 13);
        n ^= (n >>> 7);
        n ^= (n << 17);

        return (int)n;
    }

    public int caveCount(int chunkX, int chunkZ) {

        java.util.Random random =
                new java.util.Random(caveSeed(chunkX, chunkZ));

        return random.nextInt(
                random.nextInt(
                        random.nextInt(15) + 1
                ) + 1
        );
    }

    public java.util.Random caveRandom(int chunkX, int chunkZ) {
        return new java.util.Random(caveSeed(chunkX, chunkZ));
    }

    public boolean shouldCarve(double x, double y, double z) {

        if (y < 4) {
            return false;
        }

        if (y > 180) {
            return false;
        }

        double spaghetti =
                Math.abs(octaveNoise(
                        x + 12000.0,
                        y,
                        z + 12000.0,
                        5,
                        0.5,
                        0.02));

        double spaghetti2 =
                Math.abs(octaveNoise(
                        x - 9000.0,
                        y,
                        z - 9000.0,
                        5,
                        0.5,
                        0.02));

        double cheese =
                octaveNoise(
                        x,
                        y,
                        z,
                        4,
                        0.5,
                        0.008);

        return spaghetti < 0.025
                || spaghetti2 < 0.025
                || cheese > 0.58;
    }
    
    public int terrainHeight(int x, int z) {
        double cNorm = (continentalness(x, z) + 1.0) * 0.5;
        double eNorm = (erosion(x, z) + 1.0) * 0.5;
        double pv = peaksValleys(x, z);

        double baseHeight = 32.0 + bezier(cNorm, 0.3, 0.7, 1.0) * 96.0;
        double flatness = 1.0 - bezier(eNorm, 0.2, 0.6, 0.9);
        double detail = pv * flatness * 24.0;

        return (int)Math.round(baseHeight + detail);
    }

    public boolean isTreeColumn(int x, int z) {

        int cellSize = 5;

        int cellX = Math.floorDiv(x, cellSize);
        int cellZ = Math.floorDiv(z, cellSize);

        long h1 = treeHash(cellX, cellZ);
        long h2 = treeHash(cellX * 928371 + 17, cellZ * 128371 + 13);

        int offsetX = (int)(Math.abs(h1) % cellSize);
        int offsetZ = (int)(Math.abs(h2) % cellSize);

        int treeX = cellX * cellSize + offsetX;
        int treeZ = cellZ * cellSize + offsetZ;

        if (x != treeX || z != treeZ) {
            return false;
        }

        return Math.abs(treeHash(cellX * 7919 + 3, cellZ * 7907 + 11)) % 100 < 35;
    }

    public int treeTrunkHeight(int x, int z) {
        return 4 + (int)(Math.abs(treeHash(x, z)) % 3);
    }

    private long treeHash(int x, int z) {
        long n = x * 668265263L + z * 374761393L + seed;
        n = (n << 13) ^ n;
        return n * (n * n * 15731L + 789221L) + 1376312589L;
    }
}