package com.joshuastar.renderer.vulkan;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VulkanCrosshairPipeline {

    private long pipelineLayout;
    private long pipeline;

    public void create(VkDevice device, long renderPass) {

        ByteBuffer vertShader = VulkanUtils.loadShader("/shaders/crosshair.vert.spv");
        ByteBuffer fragShader = VulkanUtils.loadShader("/shaders/crosshair.frag.spv");

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
                            .pPushConstantRanges(pushConstants);

            LongBuffer pLayout = stack.mallocLong(1);
            if (vkCreatePipelineLayout(device, layoutInfo, null, pLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create crosshair pipeline layout.");
            }
            pipelineLayout = pLayout.get(0);

  VkVertexInputBindingDescription.Buffer binding =
                    VkVertexInputBindingDescription.calloc(1, stack)
                            .binding(0)
                            .stride(8)
                            .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            VkVertexInputAttributeDescription.Buffer attributes =
                    VkVertexInputAttributeDescription.calloc(1, stack);

            attributes.get(0)
                    .binding(0)
                    .location(0)
                    .format(VK_FORMAT_R32G32_SFLOAT)
                    .offset(0);

            VkPipelineVertexInputStateCreateInfo vertexInput =
                    VkPipelineVertexInputStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                            .pVertexBindingDescriptions(binding)
                            .pVertexAttributeDescriptions(attributes);

            VkPipelineInputAssemblyStateCreateInfo inputAssembly =
                    VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                            .topology(VK_PRIMITIVE_TOPOLOGY_LINE_LIST)
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
                            .lineWidth(2.0f)
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
                            .blendEnable(false);

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
                throw new RuntimeException("Failed to create crosshair pipeline. VkResult = " + result);
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