package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class VulkanBuffer {

    private long buffer;
    private long memory;
    private long size;

    public void create(
            VkPhysicalDevice physicalDevice,
            VkDevice device,
            long size,
            int usage,
            int properties) {

        this.size = size;

        try (MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo =
                    VkBufferCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                            .size(size)
                            .usage(usage)
                            .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pBuffer = stack.mallocLong(1);

            if (vkCreateBuffer(
                    device,
                    bufferInfo,
                    null,
                    pBuffer) != VK_SUCCESS) {

                throw new RuntimeException(
                        "Failed to create buffer.");
            }

            buffer = pBuffer.get(0);
                        VkMemoryRequirements memRequirements =
                    VkMemoryRequirements.malloc(stack);

            vkGetBufferMemoryRequirements(
                    device,
                    buffer,
                    memRequirements);

            VkPhysicalDeviceMemoryProperties memoryProperties =
                    VkPhysicalDeviceMemoryProperties.malloc(stack);

            vkGetPhysicalDeviceMemoryProperties(
                    physicalDevice,
                    memoryProperties);

            int memoryTypeIndex = -1;

            for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {

                if ((memRequirements.memoryTypeBits() & (1 << i)) != 0 &&
                    (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {

                    memoryTypeIndex = i;
                    break;
                }
            }

            if (memoryTypeIndex == -1) {
                throw new RuntimeException(
                        "Failed to find suitable memory type.");
            }

            VkMemoryAllocateInfo allocInfo =
                    VkMemoryAllocateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                            .allocationSize(memRequirements.size())
                            .memoryTypeIndex(memoryTypeIndex);

                                        LongBuffer pMemory = stack.mallocLong(1);

       int result = vkAllocateMemory(
        device,
        allocInfo,
        null,
        pMemory);

if (result != VK_SUCCESS) {
    throw new RuntimeException(
            "Failed to allocate buffer memory. Vulkan error = " + result);
}

            memory = pMemory.get(0);

            if (vkBindBufferMemory(
                    device,
                    buffer,
                    memory,
                    0) != VK_SUCCESS) {

                throw new RuntimeException(
                        "Failed to bind buffer memory.");
            }
        }
    }

    public void destroy(VkDevice device) {

        if (buffer != 0L) {
            vkDestroyBuffer(device, buffer, null);
            buffer = 0L;
        }

        if (memory != 0L) {
            vkFreeMemory(device, memory, null);
            memory = 0L;
        }
    }

    public long getBuffer() {
        return buffer;
    }

    public long getMemory() {
        return memory;
    }

    public long getSize() {
        return size;
    }

    protected void setBuffer(long buffer) {
        this.buffer = buffer;
    }

    protected void setMemory(long memory) {
        this.memory = memory;
    }
    public void upload(
        VkDevice device,
        byte[] data) {

    ByteBuffer src = ByteBuffer.allocateDirect(data.length);
    src.put(data);
    src.flip();

    PointerBuffer mapped = PointerBuffer.allocateDirect(1);

    if (vkMapMemory(
            device,
            memory,
            0,
            data.length,
            0,
            mapped) != VK_SUCCESS) {

        throw new RuntimeException("Failed to map buffer memory.");
    }

    memCopy(
            memAddress(src),
            mapped.get(0),
            data.length);

    vkUnmapMemory(device, memory);
}
}