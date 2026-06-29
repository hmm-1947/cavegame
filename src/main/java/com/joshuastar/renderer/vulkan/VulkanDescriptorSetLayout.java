package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

public class VulkanDescriptorSetLayout {

    private long descriptorSetLayout;

    public void create(VkDevice device) {

        try (MemoryStack stack = stackPush()) {

VkDescriptorSetLayoutBinding.Buffer bindings =
        VkDescriptorSetLayoutBinding.calloc(3, stack);

bindings.get(0)
        .binding(0)
        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        .descriptorCount(1)
        .stageFlags(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);

bindings.get(1)
        .binding(1)
        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        .descriptorCount(1)
        .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

bindings.get(2)
        .binding(2)
        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        .descriptorCount(1)
        .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo layoutInfo =
                    VkDescriptorSetLayoutCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                    .pBindings(bindings);
            LongBuffer pLayout = stack.mallocLong(1);

            if (vkCreateDescriptorSetLayout(
                    device,
                    layoutInfo,
                    null,
                    pLayout) != VK_SUCCESS) {

                throw new RuntimeException(
                        "Failed to create descriptor set layout.");
            }

            descriptorSetLayout = pLayout.get(0);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
    }

    public long getDescriptorSetLayout() {
        return descriptorSetLayout;
    }
}