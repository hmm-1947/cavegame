package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class VulkanDescriptorSets {

    private long descriptorSet;
public void create(
        VkDevice device,
        VulkanDescriptorPool pool,
        VulkanDescriptorSetLayout layout,
        VulkanUniformBuffer uniformBuffer,
        VulkanTexture texture,
        VulkanShadowMap shadowMap) {

    try (MemoryStack stack = stackPush()) {

        VkDescriptorSetAllocateInfo allocInfo =
                VkDescriptorSetAllocateInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                        .descriptorPool(pool.getDescriptorPool())
                        .pSetLayouts(
                                stack.longs(layout.getDescriptorSetLayout()));

        LongBuffer pSet = stack.mallocLong(1);

        if (vkAllocateDescriptorSets(device, allocInfo, pSet) != VK_SUCCESS) {
            throw new RuntimeException("Failed to allocate descriptor set.");
        }

        descriptorSet = pSet.get(0);

        VkDescriptorBufferInfo.Buffer bufferInfo =
                VkDescriptorBufferInfo.calloc(1, stack);

        bufferInfo.get(0)
                .buffer(uniformBuffer.getBuffer())
                .offset(0)
                .range(UniformBufferObject.SIZEOF);

        VkDescriptorImageInfo.Buffer imageInfo =
                VkDescriptorImageInfo.calloc(1, stack);

        imageInfo.get(0)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                .imageView(texture.getImageView())
                .sampler(texture.getSampler());

        VkDescriptorImageInfo.Buffer shadowImageInfo =
                VkDescriptorImageInfo.calloc(1, stack);

        shadowImageInfo.get(0)
                .imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL)
                .imageView(shadowMap.getImageView())
                .sampler(shadowMap.getSampler());

        VkWriteDescriptorSet.Buffer writes =
                VkWriteDescriptorSet.calloc(3, stack);

        writes.get(0)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1)
                .pBufferInfo(bufferInfo);

        writes.get(1)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(1)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1)
                .pImageInfo(imageInfo);

        writes.get(2)
                .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(2)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1)
                .pImageInfo(shadowImageInfo);

        vkUpdateDescriptorSets(device, writes, null);
    }
}

private long mobDescriptorSet;

    public void createMobSet(
            VkDevice device,
            VulkanDescriptorPool pool,
            VulkanDescriptorSetLayout layout,
            VulkanUniformBuffer uniformBuffer,
            VulkanTexture cowTexture,
            VulkanShadowMap shadowMap) {

        try (MemoryStack stack = stackPush()) {

            VkDescriptorSetAllocateInfo allocInfo =
                    VkDescriptorSetAllocateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                            .descriptorPool(pool.getDescriptorPool())
                            .pSetLayouts(
                                    stack.longs(layout.getDescriptorSetLayout()));

            LongBuffer pSet = stack.mallocLong(1);

            if (vkAllocateDescriptorSets(device, allocInfo, pSet) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate mob descriptor set.");
            }

            mobDescriptorSet = pSet.get(0);

            VkDescriptorBufferInfo.Buffer bufferInfo =
                    VkDescriptorBufferInfo.calloc(1, stack);

            bufferInfo.get(0)
                    .buffer(uniformBuffer.getBuffer())
                    .offset(0)
                    .range(UniformBufferObject.SIZEOF);

            VkDescriptorImageInfo.Buffer imageInfo =
                    VkDescriptorImageInfo.calloc(1, stack);

            imageInfo.get(0)
                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .imageView(cowTexture.getImageView())
                    .sampler(cowTexture.getSampler());

            VkDescriptorImageInfo.Buffer shadowImageInfo =
                    VkDescriptorImageInfo.calloc(1, stack);

            shadowImageInfo.get(0)
                    .imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL)
                    .imageView(shadowMap.getImageView())
                    .sampler(shadowMap.getSampler());

            VkWriteDescriptorSet.Buffer writes =
                    VkWriteDescriptorSet.calloc(3, stack);

            writes.get(0)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(mobDescriptorSet)
                    .dstBinding(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    .descriptorCount(1)
                    .pBufferInfo(bufferInfo);

            writes.get(1)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(mobDescriptorSet)
                    .dstBinding(1)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);

            writes.get(2)
                    .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstSet(mobDescriptorSet)
                    .dstBinding(2)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(shadowImageInfo);

            vkUpdateDescriptorSets(device, writes, null);
        }
    }

    public long getMobDescriptorSet() {
        return mobDescriptorSet;
    }
    public long getDescriptorSet() {
        return descriptorSet;
    }
}