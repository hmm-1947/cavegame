package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class VulkanUIDescriptorSet {

    private long descriptorSet;

    public void create(
            VkDevice device,
            VulkanDescriptorPool descriptorPool,
            VulkanUIDescriptorSetLayout layout,
            VulkanTexture texture) {

        try (MemoryStack stack = stackPush()) {

            LongBuffer pLayouts = stack.longs(layout.getDescriptorSetLayout());

            VkDescriptorSetAllocateInfo allocInfo =
                    VkDescriptorSetAllocateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                            .descriptorPool(descriptorPool.getDescriptorPool())
                            .pSetLayouts(pLayouts);

            LongBuffer pSet = stack.mallocLong(1);

            if (vkAllocateDescriptorSets(device, allocInfo, pSet) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate UI descriptor set.");
            }

            descriptorSet = pSet.get(0);

            VkDescriptorImageInfo.Buffer imageInfo =
                    VkDescriptorImageInfo.calloc(1, stack)
                            .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                            .imageView(texture.getImageView())
                            .sampler(texture.getSampler());

            VkWriteDescriptorSet.Buffer write =
                    VkWriteDescriptorSet.calloc(1, stack);

            write.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(descriptorSet)
                    .dstBinding(0)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);

            vkUpdateDescriptorSets(device, write, null);
        }
    }

    public long getDescriptorSet() {
        return descriptorSet;
    }
}