package com.joshuastar.renderer;

import com.joshuastar.world.Block;
import com.joshuastar.world.BlockRegistry;
import com.joshuastar.world.Chunk;

public class ChunkMesh {

    private final Chunk chunk;
    private final com.joshuastar.world.World world;
    private final Mesh mesh;

    public ChunkMesh(com.joshuastar.world.World world, Chunk chunk) {
        this.world = world;
        this.chunk = chunk;
        this.mesh = new Mesh();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void rebuild() {

        mesh.clear();

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int y = 0; y < Chunk.SIZE_Y; y++) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {

                    short block = chunk.getBlock(x, y, z);

                    if (block == 0) {
                        continue;
                    }

                    addCube(x, y, z);
                }
            }
        }

        chunk.setDirty(false);
    }

  private void addCube(int x, int y, int z) {

        Block block = BlockRegistry.get(chunk.getBlock(x, y, z));

        int worldX = chunk.getChunkX() * Chunk.SIZE_X + x;
        int worldZ = chunk.getChunkZ() * Chunk.SIZE_Z + z;

        if (world.getBlock(worldX, y + 1, worldZ) == 0)
            addTopFace(worldX, y, worldZ, block);

        if (world.getBlock(worldX, y - 1, worldZ) == 0)
            addBottomFace(worldX, y, worldZ, block);

        if (world.getBlock(worldX + 1, y, worldZ) == 0)
            addRightFace(worldX, y, worldZ, block);

        if (world.getBlock(worldX - 1, y, worldZ) == 0)
            addLeftFace(worldX, y, worldZ, block);

        if (world.getBlock(worldX, y, worldZ + 1) == 0)
            addFrontFace(worldX, y, worldZ, block);

        if (world.getBlock(worldX, y, worldZ - 1) == 0)
            addBackFace(worldX, y, worldZ, block);
    }

private void addBottomFace(int x, int y, int z, Block block) {

       float[] uv = uv(block.getBottomTexture());

        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x, y - 1, z);

        float s0 = lt * ao(x - 1, y - 1, z, x, y - 1, z + 1, x - 1, y - 1, z + 1);
        float s1 = lt * ao(x + 1, y - 1, z, x, y - 1, z + 1, x + 1, y - 1, z + 1);
        float s2 = lt * ao(x + 1, y - 1, z, x, y - 1, z - 1, x + 1, y - 1, z - 1);
        float s3 = lt * ao(x - 1, y - 1, z, x, y - 1, z - 1, x - 1, y - 1, z - 1);

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x, y, z + 1, u0, v0, 0, -1, 0, s0));
        mesh.addVertex(new Vertex(x + 1, y, z + 1, u1, v0, 0, -1, 0, s1));
        mesh.addVertex(new Vertex(x + 1, y, z, u1, v1, 0, -1, 0, s2));
        mesh.addVertex(new Vertex(x, y, z, u0, v1, 0, -1, 0, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 2);
        mesh.addIndex(start + 1);

        mesh.addIndex(start + 2);
        mesh.addIndex(start);
        mesh.addIndex(start + 3);
    }

  private float[] uv(int tile) {
        return new float[] {
                TextureAtlas.getU0(tile),
                TextureAtlas.getV0(tile),
                TextureAtlas.getU1(tile),
                TextureAtlas.getV1(tile)
        };
    }

    private float ao(int s1x, int s1y, int s1z, int s2x, int s2y, int s2z, int cx, int cy, int cz) {

        boolean side1 = world.getBlock(s1x, s1y, s1z) != 0;
        boolean side2 = world.getBlock(s2x, s2y, s2z) != 0;
        boolean corner = world.getBlock(cx, cy, cz) != 0;

        if (side1 && side2) {
            return 0.0f;
        }

        int n = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);

        return (3 - n) / 4.0f + 0.25f;
    }

    private float sampleLight(int x, int y, int z) {
        int sky = world.getSkyLight(x, y, z);
        int block = world.getBlockLight(x, y, z);
        return Math.max(sky, block) / 15.0f;
    }

   private void addRightFace(int x, int y, int z, Block block) {

       float[] uv = uv(block.getSideTexture());

        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x + 1, y, z);

        float s0 = lt * ao(x + 1, y - 1, z, x + 1, y, z + 1, x + 1, y - 1, z + 1);
        float s1 = lt * ao(x + 1, y - 1, z, x + 1, y, z - 1, x + 1, y - 1, z - 1);
        float s2 = lt * ao(x + 1, y + 1, z, x + 1, y, z - 1, x + 1, y + 1, z - 1);
        float s3 = lt * ao(x + 1, y + 1, z, x + 1, y, z + 1, x + 1, y + 1, z + 1);

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x + 1, y, z + 1, u0, v1, 1, 0, 0, s0));
        mesh.addVertex(new Vertex(x + 1, y, z, u1, v1, 1, 0, 0, s1));
        mesh.addVertex(new Vertex(x + 1, y + 1, z, u1, v0, 1, 0, 0, s2));
        mesh.addVertex(new Vertex(x + 1, y + 1, z + 1, u0, v0, 1, 0, 0, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }

  private void addLeftFace(int x, int y, int z, Block block) {

        float[] uv = uv(block.getSideTexture());

        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x - 1, y, z);

        float s0 = lt * ao(x - 1, y - 1, z, x - 1, y, z - 1, x - 1, y - 1, z - 1);
        float s1 = lt * ao(x - 1, y - 1, z, x - 1, y, z + 1, x - 1, y - 1, z + 1);
        float s2 = lt * ao(x - 1, y + 1, z, x - 1, y, z + 1, x - 1, y + 1, z + 1);
        float s3 = lt * ao(x - 1, y + 1, z, x - 1, y, z - 1, x - 1, y + 1, z - 1);

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x, y, z, u0, v1, -1, 0, 0, s0));
        mesh.addVertex(new Vertex(x, y, z + 1, u1, v1, -1, 0, 0, s1));
        mesh.addVertex(new Vertex(x, y + 1, z + 1, u1, v0, -1, 0, 0, s2));
        mesh.addVertex(new Vertex(x, y + 1, z, u0, v0, -1, 0, 0, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }
private void addTopFace(int x, int y, int z, Block block) {

       float[] uv = uv(block.getTopTexture());

        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x, y + 1, z);

        float s0 = lt * ao(x - 1, y + 1, z, x, y + 1, z + 1, x - 1, y + 1, z + 1);
        float s1 = lt * ao(x + 1, y + 1, z, x, y + 1, z + 1, x + 1, y + 1, z + 1);
        float s2 = lt * ao(x + 1, y + 1, z, x, y + 1, z - 1, x + 1, y + 1, z - 1);
        float s3 = lt * ao(x - 1, y + 1, z, x, y + 1, z - 1, x - 1, y + 1, z - 1);

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x, y + 1, z + 1, u0, v0, 0, 1, 0, s0));
        mesh.addVertex(new Vertex(x + 1, y + 1, z + 1, u1, v0, 0, 1, 0, s1));
        mesh.addVertex(new Vertex(x + 1, y + 1, z, u1, v1, 0, 1, 0, s2));
        mesh.addVertex(new Vertex(x, y + 1, z, u0, v1, 0, 1, 0, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }
private void addBackFace(int x, int y, int z, Block block) {
        float[] uv = uv(block.getSideTexture());
        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x, y, z - 1);

        float s0 = lt * ao(x + 1, y, z - 1, x, y - 1, z - 1, x + 1, y - 1, z - 1);
        float s1 = lt * ao(x - 1, y, z - 1, x, y - 1, z - 1, x - 1, y - 1, z - 1);
        float s2 = lt * ao(x - 1, y, z - 1, x, y + 1, z - 1, x - 1, y + 1, z - 1);
        float s3 = lt * ao(x + 1, y, z - 1, x, y + 1, z - 1, x + 1, y + 1, z - 1);

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x + 1, y, z, u0, v1, 0, 0, -1, s0));
        mesh.addVertex(new Vertex(x, y, z, u1, v1, 0, 0, -1, s1));
        mesh.addVertex(new Vertex(x, y + 1, z, u1, v0, 0, 0, -1, s2));
        mesh.addVertex(new Vertex(x + 1, y + 1, z, u0, v0, 0, 0, -1, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }

private void addFrontFace(int x, int y, int z, Block block) {
        float[] uv = uv(block.getSideTexture());
        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        float lt = sampleLight(x, y, z + 1);

        float s0 = lt * ao(x - 1, y, z + 1, x, y - 1, z + 1, x - 1, y - 1, z + 1);
        float s1 = lt * ao(x + 1, y, z + 1, x, y - 1, z + 1, x + 1, y - 1, z + 1);
        float s2 = lt * ao(x + 1, y, z + 1, x, y + 1, z + 1, x + 1, y + 1, z + 1);
        float s3 = lt * ao(x - 1, y, z + 1, x, y + 1, z + 1, x - 1, y + 1, z + 1);

        int start = mesh.getVertexCount();
        mesh.addVertex(new Vertex(x, y, z + 1, u0, v1, 0, 0, 1, s0));
        mesh.addVertex(new Vertex(x + 1, y, z + 1, u1, v1, 0, 0, 1, s1));
        mesh.addVertex(new Vertex(x + 1, y + 1, z + 1, u1, v0, 0, 0, 1, s2));
        mesh.addVertex(new Vertex(x, y + 1, z + 1, u0, v0, 0, 0, 1, s3));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }
}