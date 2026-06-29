package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDevice;

public class VulkanDescriptorPool {

    private long descriptorPool;

    public void create(VkDevice device) {

        try (MemoryStack stack = stackPush()) {

VkDescriptorPoolSize.Buffer poolSizes =
        VkDescriptorPoolSize.calloc(2, stack);

poolSizes.get(0)
        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        .descriptorCount(2);

poolSizes.get(1)
        .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        .descriptorCount(4);

            VkDescriptorPoolCreateInfo poolInfo =
                    VkDescriptorPoolCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSizes)
                            .maxSets(2);

            LongBuffer pPool = stack.mallocLong(1);

            if (vkCreateDescriptorPool(
                    device,
                    poolInfo,
                    null,
                    pPool) != VK_SUCCESS) {

                throw new RuntimeException(
                        "Failed to create descriptor pool.");
            }

            descriptorPool = pPool.get(0);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroyDescriptorPool(device, descriptorPool, null);
    }

    public long getDescriptorPool() {
        return descriptorPool;
    }
}