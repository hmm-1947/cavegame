package com.joshuastar.renderer.vulkan;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

public class VulkanContext {

    private final VulkanDebugMessenger debugMessenger = new VulkanDebugMessenger();
    private final VulkanInstance instance = new VulkanInstance();
    private final VulkanSurface surface = new VulkanSurface();
    private final VulkanPhysicalDevice physicalDevice = new VulkanPhysicalDevice();
    private final VulkanLogicalDevice logicalDevice = new VulkanLogicalDevice();
    private final VulkanMemoryAllocator allocator = new VulkanMemoryAllocator();

    public void create(long windowHandle) {

        instance.create(debugMessenger);

        if (ValidationLayers.ENABLED) {
            debugMessenger.create(instance.getInstance());
        }

        surface.create(
                instance.getInstance(),
                windowHandle
        );

        physicalDevice.pick(
                instance.getInstance(),
                surface.getSurface()
        );

        logicalDevice.create(
                physicalDevice.getDevice(),
                physicalDevice.getQueueFamilies()
        );

        allocator.create(
                instance.getInstance(),
                physicalDevice.getDevice(),
                logicalDevice.getDevice()
        );
    }

    public void destroy() {

        allocator.destroy();

        logicalDevice.destroy();

        surface.destroy(instance.getInstance());

        if (ValidationLayers.ENABLED) {
            debugMessenger.destroy(instance.getInstance());
        }

        instance.destroy();
    }

    public VkInstance getInstance() {
        return instance.getInstance();
    }

    public VkPhysicalDevice getPhysicalDevice() {
        return physicalDevice.getDevice();
    }

    public VkDevice getDevice() {
        return logicalDevice.getDevice();
    }

    public VkQueue getGraphicsQueue() {
        return logicalDevice.getGraphicsQueue();
    }

    public VkQueue getPresentQueue() {
        return logicalDevice.getPresentQueue();
    }

    public QueueFamilyIndices getQueueFamilies() {
        return physicalDevice.getQueueFamilies();
    }

    public long getSurface() {
        return surface.getSurface();
    }

    public VulkanMemoryAllocator getAllocator() {
        return allocator;
    }
}