package com.joshuastar.renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

import java.nio.FloatBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

public class VulkanLogicalDevice {

    private VkDevice device;
    private VkQueue graphicsQueue;
    private VkQueue presentQueue;

    public void create(VkPhysicalDevice physicalDevice, QueueFamilyIndices queueFamilies) {

        try (MemoryStack stack = stackPush()) {

            Set<Integer> uniqueFamilies = new LinkedHashSet<>();
            uniqueFamilies.add(queueFamilies.getGraphicsFamily());
            uniqueFamilies.add(queueFamilies.getPresentFamily());

            VkDeviceQueueCreateInfo.Buffer queueInfos =
                    VkDeviceQueueCreateInfo.calloc(uniqueFamilies.size(), stack);

            FloatBuffer priority = stack.floats(1.0f);

            int i = 0;
            for (Integer family : uniqueFamilies) {
                queueInfos.get(i)
                        .sType$Default()
                        .queueFamilyIndex(family)
                        .pQueuePriorities(priority);
                i++;
            }

            PointerBuffer extensions = stack.mallocPointer(1);
            extensions.put(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
            extensions.flip();

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pQueueCreateInfos(queueInfos)
                    .ppEnabledExtensionNames(extensions);

            PointerBuffer pDevice = stack.mallocPointer(1);

            int result = vkCreateDevice(
                    physicalDevice,
                    createInfo,
                    null,
                    pDevice);

            if (result != VK_SUCCESS) {
                throw new RuntimeException("vkCreateDevice failed: " + result);
            }

            device = new VkDevice(
                    pDevice.get(0),
                    physicalDevice,
                    createInfo);

            PointerBuffer pQueue = stack.mallocPointer(1);

            vkGetDeviceQueue(device, queueFamilies.getGraphicsFamily(), 0, pQueue);
            graphicsQueue = new VkQueue(pQueue.get(0), device);

            vkGetDeviceQueue(device, queueFamilies.getPresentFamily(), 0, pQueue);
            presentQueue = new VkQueue(pQueue.get(0), device);
        }
    }

    public void destroy() {
        if (device != null) {
            vkDestroyDevice(device, null);
            device = null;
        }
    }

    public VkDevice getDevice() {
        return device;
    }

    public VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public VkQueue getPresentQueue() {
        return presentQueue;
    }
}