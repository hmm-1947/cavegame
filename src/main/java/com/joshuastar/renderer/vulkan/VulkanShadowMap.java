package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
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
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class VulkanShadowMap {

    public static final int CASCADE_COUNT = 3;
   public static final int RESOLUTION = 1024;

    private long image;
    private long memory;
    private long arrayView;
    private final long[] layerViews = new long[CASCADE_COUNT];
    private long sampler;

    public void create(VkPhysicalDevice physicalDevice, VkDevice device) {

        try (MemoryStack stack = stackPush()) {

            VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D)
                    .extent(e -> e
                            .width(RESOLUTION)
                            .height(RESOLUTION)
                            .depth(1))
                    .mipLevels(1)
                    .arrayLayers(CASCADE_COUNT)
                    .format(VK_FORMAT_D32_SFLOAT)
                    .tiling(VK_IMAGE_TILING_OPTIMAL)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pImage = stack.mallocLong(1);

            if (vkCreateImage(device, imageInfo, null, pImage) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shadow image.");
            }

            image = pImage.get(0);

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            vkGetImageMemoryRequirements(device, image, memRequirements);

            VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
            vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);

            int memoryTypeIndex = -1;

            for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
                if ((memRequirements.memoryTypeBits() & (1 << i)) != 0 &&
                        (memoryProperties.memoryTypes(i).propertyFlags()
                                & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) == VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) {
                    memoryTypeIndex = i;
                    break;
                }
            }

            if (memoryTypeIndex == -1) {
                throw new RuntimeException("Failed to find shadow image memory type.");
            }

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(memoryTypeIndex);

            LongBuffer pMemory = stack.mallocLong(1);

            if (vkAllocateMemory(device, allocInfo, null, pMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate shadow image memory.");
            }

            memory = pMemory.get(0);

            vkBindImageMemory(device, image, memory, 0);

            VkImageViewCreateInfo arrayViewInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(image)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D_ARRAY)
                    .format(VK_FORMAT_D32_SFLOAT);

            VkComponentMapping arrayComponents = arrayViewInfo.components();
            arrayComponents.r(VK_COMPONENT_SWIZZLE_IDENTITY);
            arrayComponents.g(VK_COMPONENT_SWIZZLE_IDENTITY);
            arrayComponents.b(VK_COMPONENT_SWIZZLE_IDENTITY);
            arrayComponents.a(VK_COMPONENT_SWIZZLE_IDENTITY);

            VkImageSubresourceRange arrayRange = arrayViewInfo.subresourceRange();
            arrayRange.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            arrayRange.baseMipLevel(0);
            arrayRange.levelCount(1);
            arrayRange.baseArrayLayer(0);
            arrayRange.layerCount(CASCADE_COUNT);

            LongBuffer pArrayView = stack.mallocLong(1);

            if (vkCreateImageView(device, arrayViewInfo, null, pArrayView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shadow array view.");
            }

            arrayView = pArrayView.get(0);

            for (int i = 0; i < CASCADE_COUNT; i++) {

                VkImageViewCreateInfo layerViewInfo = VkImageViewCreateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                        .image(image)
                        .viewType(VK_IMAGE_VIEW_TYPE_2D)
                        .format(VK_FORMAT_D32_SFLOAT);

                VkComponentMapping layerComponents = layerViewInfo.components();
                layerComponents.r(VK_COMPONENT_SWIZZLE_IDENTITY);
                layerComponents.g(VK_COMPONENT_SWIZZLE_IDENTITY);
                layerComponents.b(VK_COMPONENT_SWIZZLE_IDENTITY);
                layerComponents.a(VK_COMPONENT_SWIZZLE_IDENTITY);

                VkImageSubresourceRange layerRange = layerViewInfo.subresourceRange();
                layerRange.aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
                layerRange.baseMipLevel(0);
                layerRange.levelCount(1);
                layerRange.baseArrayLayer(i);
                layerRange.layerCount(1);

                LongBuffer pLayerView = stack.mallocLong(1);

                if (vkCreateImageView(device, layerViewInfo, null, pLayerView) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create shadow layer view.");
                }

                layerViews[i] = pLayerView.get(0);
            }

            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                    .magFilter(VK_FILTER_LINEAR)
                    .minFilter(VK_FILTER_LINEAR)
                    .addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER)
                    .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER)
                    .addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER)
                    .borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE)
                    .anisotropyEnable(false)
                    .maxAnisotropy(1.0f)
                    .unnormalizedCoordinates(false)
                    .compareEnable(true)
                    .compareOp(VK_COMPARE_OP_LESS)
                    .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
                    .minLod(0.0f)
                    .maxLod(0.0f);

            LongBuffer pSampler = stack.mallocLong(1);

            if (vkCreateSampler(device, samplerInfo, null, pSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shadow sampler.");
            }

            sampler = pSampler.get(0);
        }
    }

    public void destroy(VkDevice device) {

        vkDestroySampler(device, sampler, null);

        for (long view : layerViews) {
            vkDestroyImageView(device, view, null);
        }

        vkDestroyImageView(device, arrayView, null);
        vkDestroyImage(device, image, null);
        vkFreeMemory(device, memory, null);
    }

    public long getImageView() {
        return arrayView;
    }

    public long getLayerView(int index) {
        return layerViews[index];
    }

    public long getSampler() {
        return sampler;
    }
}