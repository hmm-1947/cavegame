package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

public class VulkanRenderPass {

    private long renderPass;

    public void create(VkDevice device, int imageFormat) {
        try (MemoryStack stack = stackPush()) {
           VkAttachmentDescription.Buffer attachments =
        VkAttachmentDescription.calloc(2, stack);

attachments.get(0)
        .format(imageFormat)
        .samples(VK_SAMPLE_COUNT_1_BIT)
        .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
        .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
        .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
        .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
        .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

attachments.get(1)
        .format(VK_FORMAT_D32_SFLOAT)
        .samples(VK_SAMPLE_COUNT_1_BIT)
        .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
        .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
        .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
        .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
        .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
        .finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack)
                    .attachment(0)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

VkAttachmentReference depthAttachmentRef =
        VkAttachmentReference.calloc(stack);

depthAttachmentRef
        .attachment(1)
        .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack)
        .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
        .colorAttachmentCount(1)
        .pColorAttachments(colorAttachmentRef)
        .pDepthStencilAttachment(depthAttachmentRef);

            // Added subpass dependency to sync layout transitions
            VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack)
                    .srcSubpass(VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                 .pAttachments(attachments)
                    .pSubpasses(subpass)
                    .pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create render pass!");
            }
            renderPass = pRenderPass.get(0);
        }
    }

    public void destroy(VkDevice device) {
        vkDestroyRenderPass(device, renderPass, null);
    }

    public long getRenderPass() { return renderPass; }
}