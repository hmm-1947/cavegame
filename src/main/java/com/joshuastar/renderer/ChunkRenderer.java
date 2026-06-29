package com.joshuastar.renderer;

import java.util.HashMap;
import java.util.Map;

import com.joshuastar.renderer.vulkan.VulkanMemoryAllocator;
import com.joshuastar.renderer.vulkan.VulkanMesh;
import com.joshuastar.world.Chunk;
import com.joshuastar.world.World;

public class ChunkRenderer {

    private final World world;

   private final Map<Long, VulkanMesh> meshes = new HashMap<>();
    private final java.util.List<VulkanMesh> pendingDestroy = new java.util.ArrayList<>();

    public ChunkRenderer(World world) {
        this.world = world;
    }
public void rebuildChunk(
            org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
            org.lwjgl.vulkan.VkDevice device,
            int chunkX,
            int chunkZ,
            VulkanMemoryAllocator allocator) {

        ChunkMesh mesh = buildMeshData(chunkX, chunkZ);

        if (mesh == null) {
            return;
        }

        uploadMesh(physicalDevice, device, chunkX, chunkZ, mesh, allocator);
    }

    public ChunkMesh buildMeshData(int chunkX, int chunkZ) {

        Chunk chunk = world.getChunk(chunkX, chunkZ);

        if (chunk == null) {
            return null;
        }

        return ChunkMeshBuilder.build(world, chunk);
    }

    public void uploadMesh(
            org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
            org.lwjgl.vulkan.VkDevice device,
            int chunkX,
            int chunkZ,
            ChunkMesh mesh,
            VulkanMemoryAllocator allocator) {

        VulkanMesh gpuMesh = new VulkanMesh(mesh.getMesh());
        gpuMesh.upload(
                physicalDevice,
                device,
                allocator);

VulkanMesh old = meshes.put(key(chunkX, chunkZ), gpuMesh);

        if (old != null) {
            pendingDestroy.add(old);
        }
    }
    public World getWorld() {
        return world;
    }

    public VulkanMesh getMesh(int chunkX, int chunkZ) {
        return meshes.get(key(chunkX, chunkZ));
    }

    public Map<Long, VulkanMesh> getMeshes() {
        return meshes;
    }

    public java.util.Collection<VulkanMesh> getMeshesInRadius(float centerX, float centerZ, float radius) {
        java.util.List<VulkanMesh> result = new java.util.ArrayList<>();
        float radiusSq = radius * radius;

        for (java.util.Map.Entry<Long, VulkanMesh> entry : meshes.entrySet()) {
            long key = entry.getKey();
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;

            float worldX = chunkX * com.joshuastar.world.Chunk.SIZE_X + com.joshuastar.world.Chunk.SIZE_X * 0.5f;
            float worldZ = chunkZ * com.joshuastar.world.Chunk.SIZE_Z + com.joshuastar.world.Chunk.SIZE_Z * 0.5f;

            float dx = worldX - centerX;
            float dz = worldZ - centerZ;

            if (dx * dx + dz * dz <= radiusSq) {
                result.add(entry.getValue());
            }
        }

        return result;
    }
public void unloadChunk(org.lwjgl.vulkan.VkDevice device, int chunkX, int chunkZ) {
        VulkanMesh mesh = meshes.remove(key(chunkX, chunkZ));
        if (mesh != null) {
            pendingDestroy.add(mesh);
        }
    }

    public void flushPendingDestroy(org.lwjgl.vulkan.VkDevice device) {
        for (VulkanMesh mesh : pendingDestroy) {
            mesh.destroy(device);
        }
        pendingDestroy.clear();
    }

    private long key(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }
}