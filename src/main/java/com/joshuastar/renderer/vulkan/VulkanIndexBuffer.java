package com.joshuastar.renderer.vulkan;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import com.joshuastar.renderer.Mesh;
public class VulkanIndexBuffer extends VulkanBuffer {

    private Mesh mesh;
public void upload(
        org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
        org.lwjgl.vulkan.VkDevice device,
        VulkanMemoryAllocator allocator,
        Mesh mesh) {

        this.mesh = mesh;
long size =
        (long) mesh.getIndexCount() * Integer.BYTES;


create(
        physicalDevice,
        device,
        size,
        org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
       org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
        | org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
IntBuffer indexData =
        org.lwjgl.BufferUtils.createIntBuffer(mesh.getIndexCount());

for (Integer index : mesh.getIndices()) {
    indexData.put(index);
}

indexData.flip();

PointerBuffer mapped =
        BufferUtils.createPointerBuffer(1);

vkMapMemory(
        device,
        getMemory(),
        0,
        size,
        0,
        mapped);

org.lwjgl.system.MemoryUtil.memCopy(
        org.lwjgl.system.MemoryUtil.memAddress(indexData),
        mapped.get(0),
        size);

vkUnmapMemory(device, getMemory());

    }

    public Mesh getMesh() {
        return mesh;
    }
}