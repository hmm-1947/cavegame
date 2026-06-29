package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_CONCURRENT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class VulkanSwapChain {

    private long swapChain;
    private long[] swapChainImages;
    private int swapChainImageFormat;
    private VkExtent2D swapChainExtent;

    public void create(VkDevice device, long surface, QueueFamilyIndices indices, int width, int height) {
        // Allocate permanently off-heap so it survives into the render loop
        swapChainExtent = VkExtent2D.malloc().set(width, height);
        swapChainImageFormat = VK_FORMAT_B8G8R8A8_UNORM;

        try (MemoryStack stack = stackPush()) {
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface)
                    .minImageCount(2)
                    .imageFormat(swapChainImageFormat)
                    .imageColorSpace(0) 
                    .imageExtent(swapChainExtent)
                    .imageArrayLayers(1)
                    .imageUsage(16) // VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
                    .preTransform(1) // VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR
                    .compositeAlpha(1) // VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR
                    .presentMode(VK_PRESENT_MODE_FIFO_KHR)
                    .clipped(true)
                    .oldSwapchain(0);

            if (indices.getGraphicsFamily() != indices.getPresentFamily()) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.getGraphicsFamily(), indices.getPresentFamily()));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            LongBuffer pSwapChain = stack.longs(0);
            if (vkCreateSwapchainKHR(device, createInfo, null, pSwapChain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create swap chain!");
            }
            swapChain = pSwapChain.get(0);

            IntBuffer pImageCount = stack.ints(0);
            vkGetSwapchainImagesKHR(device, swapChain, pImageCount, null);
            LongBuffer pSwapchainImages = stack.mallocLong(pImageCount.get(0));
            vkGetSwapchainImagesKHR(device, swapChain, pImageCount, pSwapchainImages);

            swapChainImages = new long[pImageCount.get(0)];
            for (int i = 0; i < swapChainImages.length; i++) {
                swapChainImages[i] = pSwapchainImages.get(i);
            }
        }
    }

    public void destroy(VkDevice device) {
        vkDestroySwapchainKHR(device, swapChain, null);
        if (swapChainExtent != null) {
            swapChainExtent.free(); // Free the permanent memory
        }
    }

    public long getSwapChain() { return swapChain; }
    public long[] getImages() { return swapChainImages; }
    public int getFormat() { return swapChainImageFormat; }
    public VkExtent2D getExtent() { return swapChainExtent; }
}