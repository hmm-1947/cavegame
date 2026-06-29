package com.joshuastar.renderer.vulkan;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;
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
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

public class VulkanGraphicsPipeline {

        private long pipelineLayout;
        private long graphicsPipeline;
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;

        public void create(
                        VkDevice device,
                        long renderPass,
                        long descriptorSetLayout) {

                ByteBuffer vertShader = VulkanUtils.loadShader("/shaders/triangle.vert.spv");

                ByteBuffer fragShader = VulkanUtils.loadShader("/shaders/triangle.frag.spv");

                try (MemoryStack stack = stackPush()) {

                        long vertModule = createShaderModule(device, vertShader);

                        long fragModule = createShaderModule(device, fragShader);

                        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2,
                                        stack);

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
                        VkPipelineLayoutCreateInfo layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                                        .pSetLayouts(stack.longs(descriptorSetLayout));

                        LongBuffer pPipelineLayout = stack.mallocLong(1);

                        if (vkCreatePipelineLayout(
                                        device,
                                        layoutInfo,
                                        null,
                                        pPipelineLayout) != VK_SUCCESS) {

                                throw new RuntimeException(
                                                "Failed to create pipeline layout.");
                        }

                        pipelineLayout = pPipelineLayout.get(0);
                        VkVertexInputBindingDescription.Buffer binding = VkVertexInputBindingDescription.calloc(1,
                                        stack);

                        binding.get(0)
                                        .binding(0)
                                        .stride(com.joshuastar.renderer.Vertex.BYTES)
                                        .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

                        VkVertexInputAttributeDescription.Buffer attributes = VkVertexInputAttributeDescription
                                        .calloc(4, stack);

                        attributes.get(0)
                                        .binding(0)
                                        .location(0)
                                        .format(VK_FORMAT_R32G32B32_SFLOAT)
                                        .offset(0);

                        attributes.get(1)
                                        .binding(0)
                                        .location(1)
                                        .format(VK_FORMAT_R32G32_SFLOAT)
                                        .offset(12);

                        attributes.get(2)
                                        .binding(0)
                                        .location(2)
                                        .format(VK_FORMAT_R32G32B32_SFLOAT)
                                        .offset(20);

                        attributes.get(3)
                                        .binding(0)
                                        .location(3)
                                        .format(VK_FORMAT_R32_SFLOAT)
                                        .offset(32);

                        VkPipelineVertexInputStateCreateInfo vertexInput = VkPipelineVertexInputStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                                        .pVertexBindingDescriptions(binding)
                                        .pVertexAttributeDescriptions(attributes);
                        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                                        .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                                        .primitiveRestartEnable(false);

                        VkViewport.Buffer viewport = VkViewport.calloc(1, stack)
                                        .x(0.0f)
                                        .y(0.0f)
                                        .width((float) WIDTH)
                                        .height((float) HEIGHT)
                                        .minDepth(0.0f)
                                        .maxDepth(1.0f);

                        VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);

                        scissor.offset().set(0, 0);
                        scissor.extent()
                                        .width(WIDTH)
                                        .height(HEIGHT);

                        VkPipelineRasterizationStateCreateInfo rasterizer;
                        rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                                        .depthClampEnable(false)
                                        .rasterizerDiscardEnable(false)
                                        .polygonMode(VK_POLYGON_MODE_FILL)
                                        .lineWidth(1.0f)
                                        .cullMode(VK_CULL_MODE_BACK_BIT)
                                        .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                                        .depthBiasEnable(false);

                        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                                        .sampleShadingEnable(false)
                                        .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

                        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState
                                        .calloc(1, stack)
                                        .colorWriteMask(
                                                        VK_COLOR_COMPONENT_R_BIT |
                                                                        VK_COLOR_COMPONENT_G_BIT |
                                                                        VK_COLOR_COMPONENT_B_BIT |
                                                                        VK_COLOR_COMPONENT_A_BIT)
                                        .blendEnable(false);

                        VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                                        .logicOpEnable(false)
                                        .pAttachments(colorBlendAttachment);

                        IntBuffer dynamicStates = stack.ints(
                                        VK_DYNAMIC_STATE_VIEWPORT,
                                        VK_DYNAMIC_STATE_SCISSOR);
                        VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                                        .pDynamicStates(dynamicStates);

                        VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                                        .depthTestEnable(true)
                                        .depthWriteEnable(true)
                                        .depthCompareOp(VK_COMPARE_OP_LESS)
                                        .depthBoundsTestEnable(false)
                                        .stencilTestEnable(false);
                        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo
                                        .calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                                        .viewportCount(1)
                                        .scissorCount(1);
                        VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1,
                                        stack);

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
                                        .basePipelineHandle(VK_NULL_HANDLE)
                                        .basePipelineIndex(-1)
                                        .pDynamicState(dynamicState)
                                        .layout(pipelineLayout)
                                        .renderPass(renderPass)
                                        .subpass(0);

                        LongBuffer pPipeline = stack.mallocLong(1);

                        if (vkCreateGraphicsPipelines(
                                        device,
                                        VK_NULL_HANDLE,
                                        pipelineInfo,
                                        null,
                                        pPipeline) != VK_SUCCESS) {

                                throw new RuntimeException(
                                                "Failed to create graphics pipeline.");
                        }

                        graphicsPipeline = pPipeline.get(0);
                        vkDestroyShaderModule(device, vertModule, null);
                        vkDestroyShaderModule(device, fragModule, null);

                        return;
                }
        }

        private long createShaderModule(
                        VkDevice device,
                        ByteBuffer code) {

                try (MemoryStack stack = stackPush()) {

                        VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                                        .pCode(code);

                        LongBuffer pShaderModule = stack.mallocLong(1);

                        if (vkCreateShaderModule(
                                        device,
                                        createInfo,
                                        null,
                                        pShaderModule) != VK_SUCCESS) {

                                throw new RuntimeException(
                                                "Failed to create shader module.");
                        }

                        return pShaderModule.get(0);
                }
        }

        public void destroy(VkDevice device) {

                if (graphicsPipeline != 0L) {
                        vkDestroyPipeline(device, graphicsPipeline, null);
                        graphicsPipeline = 0L;
                }

                if (pipelineLayout != 0L) {
                        vkDestroyPipelineLayout(device, pipelineLayout, null);
                        pipelineLayout = 0L;
                }
        }

        public long getPipeline() {
                return graphicsPipeline;
        }

        public long getPipelineLayout() {
                return pipelineLayout;
        }
}