package com.joshuastar.world;

public final class BlockRegistry {

public static final Block AIR = new Block((short) 0, 0, 0, 0, 1, 0);

public static final Block GRASS = new Block((short) 1, 0, 2, 3);
    public static final Block DIRT = new Block((short) 2, 3, 3, 3);
    public static final Block STONE = new Block((short) 3, 4, 4, 4);
    public static final Block BEDROCK = new Block((short) 4, 5, 5, 5);
    public static final Block SAND = new Block((short) 5, 6, 6, 6);
    public static final Block SNOW = new Block((short) 6, 7, 7, 7);
    public static final Block LOG = new Block((short) 7, 8, 9, 8);
public static final Block LEAVES = new Block((short) 8, 10, 10, 10, 1, 0);

    private static final Block[] BLOCKS = {
            AIR,
            GRASS,
            DIRT,
            STONE,
            BEDROCK,
            SAND,
            SNOW,
            LOG,
            LEAVES
    };
    public static Block get(short id) {
        if (id < 0 || id >= BLOCKS.length) return AIR;
        return BLOCKS[id];
    }
}