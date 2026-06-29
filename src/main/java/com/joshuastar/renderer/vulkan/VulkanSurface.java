package com.joshuastar.renderer.vulkan;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

public class VulkanSurface {

    private long surface;

    public void create(VkInstance instance, long windowHandle) {

        try (MemoryStack stack = stackPush()) {

            LongBuffer pSurface = stack.mallocLong(1);

            int result = glfwCreateWindowSurface(
                    instance,
                    windowHandle,
                    null,
                    pSurface
            );

            if (result != VK_SUCCESS) {
                throw new RuntimeException("glfwCreateWindowSurface failed: " + result);
            }

            surface = pSurface.get(0);
        }
    }

    public void destroy(VkInstance instance) {
        if (surface != 0L) {
            vkDestroySurfaceKHR(instance, surface, null);
            surface = 0L;
        }
    }

    public long getSurface() {
        return surface;
    }
}