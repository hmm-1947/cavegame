package com.joshuastar.renderer.vulkan;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class VulkanUniformBuffer extends VulkanBuffer {

    private FloatBuffer data;
    private PointerBuffer mapped;

    public void create(
            VkPhysicalDevice physicalDevice,
            VkDevice device) {

        super.create(
                physicalDevice,
                device,
                UniformBufferObject.SIZEOF,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
                        | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        int floatCount = UniformBufferObject.SIZEOF / Float.BYTES;
        data = memAllocFloat(floatCount);
        mapped = BufferUtils.createPointerBuffer(1);

        vkMapMemory(
                device,
                getMemory(),
                0,
                UniformBufferObject.SIZEOF,
                0,
                mapped);
    }

public void update(
        VkDevice device,
        UniformBufferObject ubo) {

    ubo.model.get(0, data);
    ubo.view.get(16, data);
    ubo.projection.get(32, data);

    int offset = 48;
    for (int i = 0; i < UniformBufferObject.NUM_CASCADES; i++) {
        ubo.lightSpaceMatrices[i].get(offset, data);
        offset += 16;
    }

data.put(offset, ubo.cascadeSplits.x());
    data.put(offset + 1, ubo.cascadeSplits.y());
    data.put(offset + 2, ubo.cascadeSplits.z());
    data.put(offset + 3, ubo.cascadeSplits.w());

    org.lwjgl.system.MemoryUtil.memCopy(
            org.lwjgl.system.MemoryUtil.memAddress(data),
            mapped.get(0),
            UniformBufferObject.SIZEOF);
}

public void destroy(VkDevice device) {
        if (data != null) {
            memFree(data);
        }
        vkUnmapMemory(device, getMemory());
        super.destroy(device);
    }
}