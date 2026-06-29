package com.joshuastar.renderer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.joshuastar.renderer.vulkan.VulkanMemoryAllocator;
import com.joshuastar.world.Chunk;
import com.joshuastar.world.World;

public class ChunkManager {

    private final World world;
    private final ChunkRenderer renderer;
    private final VulkanMemoryAllocator allocator;
    private final org.lwjgl.vulkan.VkDevice device;
    private final org.lwjgl.vulkan.VkPhysicalDevice physicalDevice;
    private final MobManager mobManager;

    private final Set<Long> loadedChunks = new HashSet<>();
    private final Set<Long> pendingChunks = new HashSet<>();

    private final ExecutorService worker = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1));

    private final ConcurrentLinkedQueue<BuiltChunk> finished = new ConcurrentLinkedQueue<>();

    private static final int UPLOADS_PER_FRAME = 2;

    private static class BuiltChunk {
        final int chunkX;
        final int chunkZ;
        final ChunkMesh mesh;

        BuiltChunk(int chunkX, int chunkZ, ChunkMesh mesh) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.mesh = mesh;
        }
    }

public ChunkManager(
            org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
            org.lwjgl.vulkan.VkDevice device,
            World world,
            ChunkRenderer renderer,
            VulkanMemoryAllocator allocator,
            MobManager mobManager) {

        this.world = world;
        this.device = device;
        this.physicalDevice = physicalDevice;
        this.renderer = renderer;
        this.allocator = allocator;
        this.mobManager = mobManager;
    }

    public void update(int playerChunkX, int playerChunkZ, int renderDistance) {

        Set<Long> stillNeeded = new HashSet<>();

        for (int x = playerChunkX - renderDistance; x <= playerChunkX + renderDistance; x++) {
            for (int z = playerChunkZ - renderDistance; z <= playerChunkZ + renderDistance; z++) {

                long key = key(x, z);
                stillNeeded.add(key);

                if (loadedChunks.contains(key) || pendingChunks.contains(key)) {
                    continue;
                }

                pendingChunks.add(key);

                int cx = x;
                int cz = z;

                worker.submit(() -> {
                    try {
                        Chunk chunk = world.loadChunk(cx, cz);

                        if (chunk.isDirty()) {
                            ChunkMesh mesh = renderer.buildMeshData(cx, cz);
                            finished.add(new BuiltChunk(cx, cz, mesh));
                        } else {
                            finished.add(new BuiltChunk(cx, cz, null));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        int uploadsLeft = UPLOADS_PER_FRAME;
        BuiltChunk built;

        while (uploadsLeft > 0 && (built = finished.poll()) != null) {

            long key = key(built.chunkX, built.chunkZ);
            pendingChunks.remove(key);
            loadedChunks.add(key);

if (built.mesh != null) {
                renderer.uploadMesh(
                        physicalDevice,
                        device,
                        built.chunkX,
                        built.chunkZ,
                        built.mesh,
                        allocator);
                uploadsLeft--;
            }

            mobManager.onChunkLoaded(built.chunkX, built.chunkZ);
        }
        loadedChunks.removeIf(key -> {
            if (stillNeeded.contains(key)) {
                return false;
            }

            long k = key;
            int x = (int) (k >> 32);
            int z = (int) k;

renderer.unloadChunk(device, x, z);
            world.unloadChunk(x, z);
            mobManager.onChunkUnloaded(x, z);

            return true;
        });
    }
    public void shutdown() {
        worker.shutdownNow();
    }

    private long key(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }
}