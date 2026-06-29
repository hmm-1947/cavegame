package com.joshuastar.renderer.vulkan;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class VulkanDepthImage {

    private final VulkanImage image = new VulkanImage();

public void create(
        VkPhysicalDevice physicalDevice,
        VkDevice device,
        int width,
        int height) {

image.create(
        physicalDevice,
        device,
        width,
        height,
        VK_FORMAT_D32_SFLOAT,
        VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
        1);
}

    public void destroy(VkDevice device) {
        image.destroy(device);
    }

    public long getImageView() {
        return image.getImageView();
    }
}