package com.joshuastar.renderer.vulkan;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class VulkanImage {

    private long image;
    private long memory;
    private long imageView;

    public long getImage() {
        return image;
    }

    public long getMemory() {
        return memory;
    }

    public long getImageView() {
        return imageView;
    }

    protected void setImage(long image) {
        this.image = image;
    }

    protected void setMemory(long memory) {
        this.memory = memory;
    }

    protected void setImageView(long imageView) {
        this.imageView = imageView;
    }
public void create(
        VkPhysicalDevice physicalDevice,
        VkDevice device,
        int width,
        int height,
        int format,
        int usage,
        int mipLevels) {

    try (MemoryStack stack = stackPush()) {

        VkImageCreateInfo imageInfo =
                VkImageCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                        .imageType(VK_IMAGE_TYPE_2D)
                        .extent(e -> e
                                .width(width)
                                .height(height)
                                .depth(1))
                        .mipLevels(mipLevels)
                        .arrayLayers(1)
                        .format(format)
                        .tiling(VK_IMAGE_TILING_OPTIMAL)
                        .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                        .usage(usage)
                        .samples(VK_SAMPLE_COUNT_1_BIT)
                        .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

        LongBuffer pImage = stack.mallocLong(1);

        if (vkCreateImage(device, imageInfo, null, pImage) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image.");
        }

        image = pImage.get(0);
        VkMemoryRequirements memRequirements =
        VkMemoryRequirements.malloc(stack);

vkGetImageMemoryRequirements(
        device,
        image,
        memRequirements);

VkPhysicalDeviceMemoryProperties memoryProperties =
        VkPhysicalDeviceMemoryProperties.malloc(stack);

vkGetPhysicalDeviceMemoryProperties(
        physicalDevice,
        memoryProperties);

int memoryTypeIndex = -1;

for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {

    if ((memRequirements.memoryTypeBits() & (1 << i)) != 0 &&
        (memoryProperties.memoryTypes(i).propertyFlags()
                & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
                == VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) {

        memoryTypeIndex = i;
        break;
    }
}

if (memoryTypeIndex == -1) {
    throw new RuntimeException("Failed to find image memory type.");
}

VkMemoryAllocateInfo allocInfo =
        VkMemoryAllocateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(memRequirements.size())
                .memoryTypeIndex(memoryTypeIndex);

LongBuffer pMemory = stack.mallocLong(1);

if (vkAllocateMemory(
        device,
        allocInfo,
        null,
        pMemory) != VK_SUCCESS) {

    throw new RuntimeException("Failed to allocate image memory.");
}

memory = pMemory.get(0);

vkBindImageMemory(
        device,
        image,
        memory,
        0);
        VkImageViewCreateInfo viewInfo =
        VkImageViewCreateInfo.calloc(stack)
                .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(image)
                .viewType(VK_IMAGE_VIEW_TYPE_2D)
                .format(format);

VkComponentMapping components = viewInfo.components();
components.r(VK_COMPONENT_SWIZZLE_IDENTITY);
components.g(VK_COMPONENT_SWIZZLE_IDENTITY);
components.b(VK_COMPONENT_SWIZZLE_IDENTITY);
components.a(VK_COMPONENT_SWIZZLE_IDENTITY);

VkImageSubresourceRange range = viewInfo.subresourceRange();
range.aspectMask(
        format == VK_FORMAT_D32_SFLOAT
                ? VK_IMAGE_ASPECT_DEPTH_BIT
                : VK_IMAGE_ASPECT_COLOR_BIT);

range.baseMipLevel(0);
range.levelCount(mipLevels);
range.baseArrayLayer(0);
range.layerCount(1);

LongBuffer pImageView = stack.mallocLong(1);

if (vkCreateImageView(
        device,
        viewInfo,
        null,
        pImageView) != VK_SUCCESS) {

    throw new RuntimeException(
            "Failed to create image view.");
}

imageView = pImageView.get(0);
    }
}
public void destroy(VkDevice device) {

    if (imageView != 0L) {
        vkDestroyImageView(device, imageView, null);
        imageView = 0L;
    }

    if (image != 0L) {
        vkDestroyImage(device, image, null);
        image = 0L;
    }

    if (memory != 0L) {
        vkFreeMemory(device, memory, null);
        memory = 0L;
    }
}
}