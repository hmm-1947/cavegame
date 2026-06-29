package com.joshuastar.renderer.vulkan;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class VulkanUIPipeline {

    private long pipelineLayout;
    private long pipeline;

    public void create(VkDevice device, long renderPass, long descriptorSetLayout) {

        ByteBuffer vertShader = VulkanUtils.loadShader("/shaders/ui.vert.spv");
        ByteBuffer fragShader = VulkanUtils.loadShader("/shaders/ui.frag.spv");

        try (MemoryStack stack = stackPush()) {

            long vertModule = createShaderModule(device, vertShader);
            long fragModule = createShaderModule(device, fragShader);

            VkPipelineShaderStageCreateInfo.Buffer shaderStages =
                    VkPipelineShaderStageCreateInfo.calloc(2, stack);

            shaderStages.get(0)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_VERTEX_BIT)
                    .module(vertModule)
                    .pName(stack.UTF8("main"));

            shaderStages.get(1)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                    .module(fragModule)
                    .pName(stack.UTF8("main"));

            VkPushConstantRange.Buffer pushConstants =
                    VkPushConstantRange.calloc(1, stack)
                            .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT)
                            .offset(0)
                            .size(16);

            VkPipelineLayoutCreateInfo layoutInfo =
                    VkPipelineLayoutCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                            .pSetLayouts(stack.longs(descriptorSetLayout))
                            .pPushConstantRanges(pushConstants);

            LongBuffer pLayout = stack.mallocLong(1);
            if (vkCreatePipelineLayout(device, layoutInfo, null, pLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create UI pipeline layout.");
            }
            pipelineLayout = pLayout.get(0);

            VkVertexInputBindingDescription.Buffer binding =
                    VkVertexInputBindingDescription.calloc(1, stack)
                            .binding(0)
                            .stride(com.joshuastar.renderer.UIVertex.BYTES)
                            .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            VkVertexInputAttributeDescription.Buffer attributes =
                    VkVertexInputAttributeDescription.calloc(2, stack);

            attributes.get(0)
                    .binding(0)
                    .location(0)
                    .format(VK_FORMAT_R32G32_SFLOAT)
                    .offset(0);

            attributes.get(1)
                    .binding(0)
                    .location(1)
                    .format(VK_FORMAT_R32G32_SFLOAT)
                    .offset(8);

            VkPipelineVertexInputStateCreateInfo vertexInput =
                    VkPipelineVertexInputStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                            .pVertexBindingDescriptions(binding)
                            .pVertexAttributeDescriptions(attributes);

            VkPipelineInputAssemblyStateCreateInfo inputAssembly =
                    VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                            .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                            .primitiveRestartEnable(false);

            VkPipelineViewportStateCreateInfo viewportState =
                    VkPipelineViewportStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                            .viewportCount(1)
                            .scissorCount(1);

            VkPipelineRasterizationStateCreateInfo rasterizer =
                    VkPipelineRasterizationStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                            .depthClampEnable(false)
                            .rasterizerDiscardEnable(false)
                            .polygonMode(VK_POLYGON_MODE_FILL)
                            .lineWidth(1.0f)
                            .cullMode(VK_CULL_MODE_NONE)
                            .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                            .depthBiasEnable(false);

            VkPipelineMultisampleStateCreateInfo multisampling =
                    VkPipelineMultisampleStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                            .sampleShadingEnable(false)
                            .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment =
                    VkPipelineColorBlendAttachmentState.calloc(1, stack)
                            .colorWriteMask(
                                    VK_COLOR_COMPONENT_R_BIT |
                                    VK_COLOR_COMPONENT_G_BIT |
                                    VK_COLOR_COMPONENT_B_BIT |
                                    VK_COLOR_COMPONENT_A_BIT)
                            .blendEnable(true)
                            .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
                            .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                            .colorBlendOp(VK_BLEND_OP_ADD)
                            .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
                            .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
                            .alphaBlendOp(VK_BLEND_OP_ADD);

            VkPipelineColorBlendStateCreateInfo colorBlending =
                    VkPipelineColorBlendStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                            .logicOpEnable(false)
                            .pAttachments(colorBlendAttachment);

            VkPipelineDepthStencilStateCreateInfo depthStencil =
                    VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                            .depthTestEnable(false)
                            .depthWriteEnable(false)
                            .depthCompareOp(VK_COMPARE_OP_LESS)
                            .depthBoundsTestEnable(false)
                            .stencilTestEnable(false);

            int[] dynamicStatesArr = { VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR };
            VkPipelineDynamicStateCreateInfo dynamicState =
                    VkPipelineDynamicStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                            .pDynamicStates(stack.ints(dynamicStatesArr));

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo =
                    VkGraphicsPipelineCreateInfo.calloc(1, stack);

            pipelineInfo.get(0)
                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(shaderStages)
                    .pVertexInputState(vertexInput)
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizer)
                    .pMultisampleState(multisampling)
                    .pColorBlendState(colorBlending)
                    .pDepthStencilState(depthStencil)
                    .pDynamicState(dynamicState)
                    .layout(pipelineLayout)
                    .renderPass(renderPass)
                    .subpass(0)
                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1);

            LongBuffer pPipeline = stack.mallocLong(1);

            int result = vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, pPipeline);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create UI pipeline. VkResult = " + result);
            }

            pipeline = pPipeline.get(0);

            vkDestroyShaderModule(device, vertModule, null);
            vkDestroyShaderModule(device, fragModule, null);
        }
    }

    private long createShaderModule(VkDevice device, ByteBuffer code) {
        try (MemoryStack stack = stackPush()) {
            VkShaderModuleCreateInfo createInfo =
                    VkShaderModuleCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                            .pCode(code);

            LongBuffer pModule = stack.mallocLong(1);
            if (vkCreateShaderModule(device, createInfo, null, pModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create shader module.");
            }
            return pModule.get(0);
        }
    }

    public void destroy(VkDevice device) {
        if (pipeline != 0L) vkDestroyPipeline(device, pipeline, null);
        if (pipelineLayout != 0L) vkDestroyPipelineLayout(device, pipelineLayout, null);
    }

    public long getPipeline() { return pipeline; }
    public long getPipelineLayout() { return pipelineLayout; }
}