package com.joshuastar.renderer.vulkan;

import com.joshuastar.renderer.Mesh;

public class VulkanMesh {

    private final Mesh mesh;

    private final VulkanVertexBuffer vertexBuffer;
    private final VulkanIndexBuffer indexBuffer;

    public VulkanMesh(Mesh mesh) {
        this.mesh = mesh;
        this.vertexBuffer = new VulkanVertexBuffer();
        this.indexBuffer = new VulkanIndexBuffer();
    }
public void upload(
        org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
        org.lwjgl.vulkan.VkDevice device,
        VulkanMemoryAllocator allocator) {

vertexBuffer.upload(
        physicalDevice,
        device,
        allocator,
        mesh);

indexBuffer.upload(
        physicalDevice,
        device,
        allocator,
        mesh);
    }

public void destroy(org.lwjgl.vulkan.VkDevice device) {

    vertexBuffer.destroy(device);
    indexBuffer.destroy(device);
}

    public Mesh getMesh() {
        return mesh;
    }

    public VulkanVertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanIndexBuffer getIndexBuffer() {
        return indexBuffer;
    }
}