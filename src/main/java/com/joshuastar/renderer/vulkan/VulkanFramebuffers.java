package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

public class VulkanFramebuffers {

    private long[] swapChainFramebuffers;

public void create(
        VkDevice device,
        long renderPass,
        long[] imageViews,
        long depthImageView,
        VkExtent2D extent){
        try (MemoryStack stack = stackPush()) {
            swapChainFramebuffers = new long[imageViews.length];

            for (int i = 0; i < imageViews.length; i++) {
LongBuffer attachments =
        stack.longs(
                imageViews[i],
                depthImageView);

                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                        .renderPass(renderPass)
                        .pAttachments(attachments)
                        .width(extent.width())
                        .height(extent.height())
                        .layers(1);

                LongBuffer pFramebuffer = stack.mallocLong(1);
                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer!");
                }
                swapChainFramebuffers[i] = pFramebuffer.get(0);
            }
        }
    }

    public void destroy(VkDevice device) {
        for (long framebuffer : swapChainFramebuffers) {
            vkDestroyFramebuffer(device, framebuffer, null);
        }
    }

    public long[] getFramebuffers() { return swapChainFramebuffers; }
}