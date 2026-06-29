package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class VulkanImageViews {

    private long[] swapChainImageViews;

    public void create(VkDevice device, long[] swapChainImages, int imageFormat) {
        try (MemoryStack stack = stackPush()) {
            swapChainImageViews = new long[swapChainImages.length];

            for (int i = 0; i < swapChainImages.length; i++) {
                VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                        .image(swapChainImages[i])
                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                        .format(imageFormat);

                VkComponentMapping components = createInfo.components();
                components.r(VK_COMPONENT_SWIZZLE_IDENTITY);
                components.g(VK_COMPONENT_SWIZZLE_IDENTITY);
                components.b(VK_COMPONENT_SWIZZLE_IDENTITY);
                components.a(VK_COMPONENT_SWIZZLE_IDENTITY);

                VkImageSubresourceRange subresourceRange = createInfo.subresourceRange();
                subresourceRange.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                subresourceRange.baseMipLevel(0);
                subresourceRange.levelCount(1);
                subresourceRange.baseArrayLayer(0);
                subresourceRange.layerCount(1);

                LongBuffer pImageView = stack.mallocLong(1);
                if (vkCreateImageView(device, createInfo, null, pImageView) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create image view!");
                }
                swapChainImageViews[i] = pImageView.get(0);
            }
        }
    }

    public void destroy(VkDevice device) {
        for (long imageView : swapChainImageViews) {
            vkDestroyImageView(device, imageView, null);
        }
    }

    public long[] getImageViews() { return swapChainImageViews; }
}