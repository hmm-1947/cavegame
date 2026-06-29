package com.joshuastar.renderer.vulkan;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class VulkanSwapChainSupport {

    public SwapChainSupportDetails query(VkPhysicalDevice device, long surface) {

        SwapChainSupportDetails details = new SwapChainSupportDetails();

        try (MemoryStack stack = stackPush()) {

            VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);

            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                    device,
                    surface,
                    capabilities);

            details.setCapabilities(capabilities);

            IntBuffer count = stack.mallocInt(1);

            vkGetPhysicalDeviceSurfaceFormatsKHR(
                    device,
                    surface,
                    count,
                    null);

            if (count.get(0) > 0) {

                VkSurfaceFormatKHR.Buffer formats =
                        VkSurfaceFormatKHR.malloc(count.get(0), stack);

                vkGetPhysicalDeviceSurfaceFormatsKHR(
                        device,
                        surface,
                        count,
                        formats);

                details.setFormats(formats);
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(
                    device,
                    surface,
                    count,
                    null);

            if (count.get(0) > 0) {

                IntBuffer modes = stack.mallocInt(count.get(0));

                vkGetPhysicalDeviceSurfacePresentModesKHR(
                        device,
                        surface,
                        count,
                        modes);

                int[] presentModes = new int[count.get(0)];

                for (int i = 0; i < presentModes.length; i++) {
                    presentModes[i] = modes.get(i);
                }

                details.setPresentModes(presentModes);
            }
        }

        return details;
    }
}