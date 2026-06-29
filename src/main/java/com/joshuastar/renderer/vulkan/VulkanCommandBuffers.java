package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;

public class VulkanCommandBuffers {

    private long commandPool;
    private VkCommandBuffer commandBuffer;

    public void create(VkDevice device, QueueFamilyIndices indices) {
        try (MemoryStack stack = stackPush()) {
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(indices.getGraphicsFamily());

            LongBuffer pCommandPool = stack.mallocLong(1);
            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool!");
            }
            commandPool = pCommandPool.get(0);

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffers = stack.mallocPointer(1);
            if (vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers!");
            }
            commandBuffer = new VkCommandBuffer(pCommandBuffers.get(0), device);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroyCommandPool(device, commandPool, null);
    }
public VkCommandBuffer beginSingleTimeCommands() {

    VkCommandBuffer cmd = commandBuffer;

    VkCommandBufferBeginInfo beginInfo =
            VkCommandBufferBeginInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

    if (vkBeginCommandBuffer(cmd, beginInfo) != VK_SUCCESS) {
        throw new RuntimeException("Failed to begin single time command buffer.");
    }

    beginInfo.free();

    return cmd;
}
public long getCommandPool() {
    return commandPool;
}
public void endSingleTimeCommands(
        VkCommandBuffer commandBuffer,
        org.lwjgl.vulkan.VkQueue graphicsQueue) {

    if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
        throw new RuntimeException("Failed to end command buffer.");
    }

    try (MemoryStack stack = stackPush()) {

        VkSubmitInfo submitInfo =
                VkSubmitInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                        .pCommandBuffers(stack.pointers(commandBuffer));

        if (vkQueueSubmit(
                graphicsQueue,
                submitInfo,
                VK_NULL_HANDLE) != VK_SUCCESS) {

            throw new RuntimeException(
                    "Failed to submit command buffer.");
        }

        vkQueueWaitIdle(graphicsQueue);
    }
}
    public VkCommandBuffer getCommandBuffer() { return commandBuffer; }
}