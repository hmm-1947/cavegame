package com.joshuastar.renderer.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

public class VulkanTexture {
    private int mipLevels;
    private final VulkanImage image = new VulkanImage();
    private final VulkanBuffer stagingBuffer = new VulkanBuffer();
    private long imageView;
    private long sampler;

    public void create(
            VkPhysicalDevice physicalDevice,
            VkDevice device,
            byte[] pixels,
            VkQueue graphicsQueue,
            long commandPool,
            int width,
            int height) {

        // 1. Create staging buffer (CPU → GPU transfer source)
        stagingBuffer.create(
                physicalDevice,
                device,
                pixels.length,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
                        | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        stagingBuffer.upload(device, pixels);

        // 2. Create GPU image (texture)
        mipLevels = (int) Math.floor(Math.log(Math.max(width, height)) / Math.log(2)) + 1;

        image.create(
                physicalDevice,
                device,
                width,
                height,
                VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT
                        | VK_IMAGE_USAGE_TRANSFER_SRC_BIT
                        | VK_IMAGE_USAGE_SAMPLED_BIT,
                mipLevels);

        // 3. Transition: UNDEFINED → TRANSFER_DST
        transitionImageLayout(
                device,
                graphicsQueue,
                commandPool,
                image.getImage(),
                VK_IMAGE_LAYOUT_UNDEFINED,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

        // 4. Copy buffer → image
        copyBufferToImage(
                device,
                graphicsQueue,
                commandPool,
                stagingBuffer.getBuffer(),
                image.getImage(),
                width,
                height);

        // 5. Transition: TRANSFER_DST → SHADER_READ_ONLY
        // 5. Generate mipmaps (also transitions to SHADER_READ_ONLY)
        generateMipmaps(
                device,
                graphicsQueue,
                commandPool,
                image.getImage(),
                width,
                height,
                mipLevels);

        createImageView(device, VK_FORMAT_R8G8B8A8_UNORM);
        createSampler(device);
    }

    private void generateMipmaps(
            VkDevice device,
            VkQueue graphicsQueue,
            long commandPool,
            long image,
            int width,
            int height,
            int mipLevels) {

        VkCommandBuffer commandBuffer = beginSingleTimeCommands(device, commandPool);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .image(image)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);

            barrier.subresourceRange(it -> it
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseArrayLayer(0)
                    .layerCount(1)
                    .levelCount(1));

            int mipWidth = width;
            int mipHeight = height;

            for (int i = 1; i < mipLevels; i++) {

                barrier.subresourceRange().baseMipLevel(i - 1);
                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

                vkCmdPipelineBarrier(
                        commandBuffer,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        0, null, null, barrier);

                org.lwjgl.vulkan.VkImageBlit.Buffer blit = org.lwjgl.vulkan.VkImageBlit.calloc(1, stack);

                blit.srcOffsets(0).set(0, 0, 0);
                blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
                final int srcMip = i - 1;
                blit.srcSubresource(it -> it
                        .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                        .mipLevel(srcMip)
                        .baseArrayLayer(0)
                        .layerCount(1));
                int dstWidth = Math.max(1, mipWidth / 2);
                int dstHeight = Math.max(1, mipHeight / 2);

                blit.dstOffsets(0).set(0, 0, 0);
                blit.dstOffsets(1).set(dstWidth, dstHeight, 1);
                final int dstMip = i;
                blit.dstSubresource(it -> it
                        .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                        .mipLevel(dstMip)
                        .baseArrayLayer(0)
                        .layerCount(1));

                vkCmdBlitImage(
                        commandBuffer,
                        image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                        image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        blit,
                        VK_FILTER_LINEAR);

                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                vkCmdPipelineBarrier(
                        commandBuffer,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                        0, null, null, barrier);

                mipWidth = dstWidth;
                mipHeight = dstHeight;
            }

            barrier.subresourceRange().baseMipLevel(mipLevels - 1);
            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

            vkCmdPipelineBarrier(
                    commandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                    0, null, null, barrier);
        }

        endSingleTimeCommands(device, graphicsQueue, commandBuffer, commandPool);
    }

    public long getSampler() {
        return sampler;
    }

    public void destroy(VkDevice device) {

        stagingBuffer.destroy(device);
        image.destroy(device);

        if (sampler != 0) {
            vkDestroySampler(device, sampler, null);
        }

        if (imageView != 0) {
            vkDestroyImageView(device, imageView, null);
        }
    }

    private void createImageView(
            VkDevice device,
            int format) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(image.getImage())
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(format);

            viewInfo.components(it -> it
                    .r(VK_COMPONENT_SWIZZLE_IDENTITY)
                    .g(VK_COMPONENT_SWIZZLE_IDENTITY)
                    .b(VK_COMPONENT_SWIZZLE_IDENTITY)
                    .a(VK_COMPONENT_SWIZZLE_IDENTITY));
            viewInfo.subresourceRange(it -> it
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(mipLevels)
                    .baseArrayLayer(0)
                    .layerCount(1));

            LongBuffer pView = stack.mallocLong(1);

            if (vkCreateImageView(device, viewInfo, null, pView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image view");
            }

            imageView = pView.get(0);
        }
    }

    private void createSampler(VkDevice device) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack)
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
                    .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
                    .minLod(0.0f)
                    .maxLod((float) mipLevels);

            LongBuffer pSampler = stack.mallocLong(1);

            if (vkCreateSampler(device, samplerInfo, null, pSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create sampler");
            }

            sampler = pSampler.get(0);
        }
    }

    private void transitionImageLayout(
            VkDevice device,
            VkQueue graphicsQueue,
            long commandPool,
            long image,
            int oldLayout,
            int newLayout) {

        VkCommandBuffer commandBuffer = beginSingleTimeCommands(device, commandPool);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .oldLayout(oldLayout)
                    .newLayout(newLayout)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .image(image)
                    .subresourceRange(it -> it
                            .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                            .baseMipLevel(0)
                            .levelCount(1)
                            .baseArrayLayer(0)
                            .layerCount(1));

            int srcStage;
            int dstStage;

            if (newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

                srcStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                dstStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

            } else if (newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                srcStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                dstStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

            } else {
                throw new RuntimeException("Unsupported layout transition");
            }

            vkCmdPipelineBarrier(
                    commandBuffer,
                    srcStage,
                    dstStage,
                    0,
                    null,
                    null,
                    barrier);
        }

        endSingleTimeCommands(device, graphicsQueue, commandBuffer, commandPool);
    }

    private VkCommandBuffer beginSingleTimeCommands(
            VkDevice device,
            long commandPool) {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);

            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);

            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);

            return commandBuffer;
        }
    }

    private void endSingleTimeCommands(
            VkDevice device,
            VkQueue graphicsQueue,
            VkCommandBuffer commandBuffer,
            long commandPool) {

        vkEndCommandBuffer(commandBuffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                    .pCommandBuffers(stack.pointers(commandBuffer));

            vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
            vkQueueWaitIdle(graphicsQueue);
        }

        vkFreeCommandBuffers(device, commandPool, commandBuffer);
    }

    private void copyBufferToImage(
            VkDevice device,
            VkQueue graphicsQueue,
            long commandPool,
            long buffer,
            long image,
            int width,
            int height) {

        VkCommandBuffer commandBuffer = beginSingleTimeCommands(device, commandPool);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack)
                    .bufferOffset(0)
                    .bufferRowLength(0)
                    .bufferImageHeight(0);

            region.imageSubresource(it -> it
                    .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(1));

            region.imageOffset(it -> it.set(0, 0, 0));
            region.imageExtent(it -> it
                    .width(width)
                    .height(height)
                    .depth(1));

            vkCmdCopyBufferToImage(
                    commandBuffer,
                    buffer,
                    image,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    region);
        }

        endSingleTimeCommands(device, graphicsQueue, commandBuffer, commandPool);
    }

    public long getImageView() {
        return image.getImageView();
    }
}