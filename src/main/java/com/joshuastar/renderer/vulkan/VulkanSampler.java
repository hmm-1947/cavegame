package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class VulkanSampler {

    private long sampler;

    public void create(VkDevice device) {

        try (MemoryStack stack = stackPush()) {

            VkSamplerCreateInfo samplerInfo =
                    VkSamplerCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                            .magFilter(VK_FILTER_NEAREST)
                            .minFilter(VK_FILTER_NEAREST)
                            .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                            .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                            .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                            .anisotropyEnable(false)
                            .maxAnisotropy(1.0f)
                            .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                            .unnormalizedCoordinates(false)
                            .compareEnable(false)
                            .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST);

            LongBuffer pSampler = stack.mallocLong(1);

            if (vkCreateSampler(
                    device,
                    samplerInfo,
                    null,
                    pSampler) != VK_SUCCESS) {

                throw new RuntimeException("Failed to create sampler.");
            }

            sampler = pSampler.get(0);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroySampler(device, sampler, null);
    }

    public long getSampler() {
        return sampler;
    }
}