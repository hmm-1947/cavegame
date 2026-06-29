package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

public class VulkanSyncObjects {

    private long imageAvailableSemaphore;
    private long renderFinishedSemaphore;
    private long inFlightFence;

    public void create(VkDevice device) {
        try (MemoryStack stack = stackPush()) {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pInFlightFence = stack.mallocLong(1);

            if (vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS ||
                vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS ||
                vkCreateFence(device, fenceInfo, null, pInFlightFence) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create sync objects!");
            }

            imageAvailableSemaphore = pImageAvailableSemaphore.get(0);
            renderFinishedSemaphore = pRenderFinishedSemaphore.get(0);
            inFlightFence = pInFlightFence.get(0);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroySemaphore(device, renderFinishedSemaphore, null);
        vkDestroySemaphore(device, imageAvailableSemaphore, null);
        vkDestroyFence(device, inFlightFence, null);
    }

    public long getImageAvailableSemaphore() { return imageAvailableSemaphore; }
    public long getRenderFinishedSemaphore() { return renderFinishedSemaphore; }
    public long getInFlightFence() { return inFlightFence; }
}