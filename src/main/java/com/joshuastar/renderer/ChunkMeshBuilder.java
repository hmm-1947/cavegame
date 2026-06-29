package com.joshuastar.renderer;

import com.joshuastar.world.Chunk;

public final class ChunkMeshBuilder {

    private ChunkMeshBuilder() {
    }
public static ChunkMesh build(com.joshuastar.world.World world, Chunk chunk) {

        ChunkMesh mesh = new ChunkMesh(world, chunk);

        mesh.rebuild();

        return mesh;
    }
}