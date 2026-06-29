package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

public class VulkanShadowFramebuffers {

    private final long[] framebuffers = new long[VulkanShadowMap.CASCADE_COUNT];

    public void create(VkDevice device, long renderPass, VulkanShadowMap shadowMap) {

        try (MemoryStack stack = stackPush()) {

            for (int i = 0; i < VulkanShadowMap.CASCADE_COUNT; i++) {

                LongBuffer attachments = stack.longs(shadowMap.getLayerView(i));

                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                        .renderPass(renderPass)
                        .pAttachments(attachments)
                        .width(VulkanShadowMap.RESOLUTION)
                        .height(VulkanShadowMap.RESOLUTION)
                        .layers(1);

                LongBuffer pFramebuffer = stack.mallocLong(1);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create shadow framebuffer!");
                }

                framebuffers[i] = pFramebuffer.get(0);
            }
        }
    }

    public void destroy(VkDevice device) {
        for (long fb : framebuffers) {
            vkDestroyFramebuffer(device, fb, null);
        }
    }

    public long getFramebuffer(int index) {
        return framebuffers[index];
    }
}