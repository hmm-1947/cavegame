package com.joshuastar.renderer.vulkan;

import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class VulkanPhysicalDevice {

    private VkPhysicalDevice device;
    private QueueFamilyIndices queueFamilies;

    public void pick(VkInstance instance, long surface) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer count = stack.mallocInt(1);

            if (vkEnumeratePhysicalDevices(instance, count, null) != VK_SUCCESS) {
                throw new RuntimeException("Failed to enumerate physical devices.");
            }

            if (count.get(0) == 0) {
                throw new RuntimeException("No Vulkan capable GPU found.");
            }

            PointerBuffer devices = stack.mallocPointer(count.get(0));

            vkEnumeratePhysicalDevices(instance, count, devices);

            for (int i = 0; i < devices.capacity(); i++) {

                VkPhysicalDevice candidate =
                        new VkPhysicalDevice(devices.get(i), instance);

                QueueFamilyIndices indices =
                        findQueueFamilies(candidate, surface);

                if (!indices.isComplete()) {
                    continue;
                }

                VkPhysicalDeviceProperties properties =
                        VkPhysicalDeviceProperties.malloc(stack);

                vkGetPhysicalDeviceProperties(candidate, properties);

                System.out.println("Selected GPU: " + properties.deviceNameString());

                device = candidate;
                queueFamilies = indices;
                return;
            }

            throw new RuntimeException("No suitable Vulkan GPU found.");
        }
    }

    private QueueFamilyIndices findQueueFamilies(
            VkPhysicalDevice physicalDevice,
            long surface) {

        QueueFamilyIndices indices = new QueueFamilyIndices();

        try (MemoryStack stack = stackPush()) {

            IntBuffer count = stack.mallocInt(1);

            vkGetPhysicalDeviceQueueFamilyProperties(
                    physicalDevice,
                    count,
                    null);

            VkQueueFamilyProperties.Buffer queues =
                    VkQueueFamilyProperties.malloc(count.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(
                    physicalDevice,
                    count,
                    queues);

            for (int i = 0; i < queues.capacity(); i++) {

                if ((queues.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.setGraphicsFamily(i);
                }

                IntBuffer presentSupport = stack.mallocInt(1);

                vkGetPhysicalDeviceSurfaceSupportKHR(
                        physicalDevice,
                        i,
                        surface,
                        presentSupport);

                if (presentSupport.get(0) == 1) {
                    indices.setPresentFamily(i);
                }

                if (indices.isComplete()) {
                    break;
                }
            }
        }

        return indices;
    }
public int findMemoryType(
        int typeFilter,
        int properties) {

    try (MemoryStack stack = stackPush()) {

        org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties memoryProperties =
                org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties.malloc(stack);

        org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties(
                device,
                memoryProperties);

        for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {

            if ((typeFilter & (1 << i)) != 0 &&
                (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {

                return i;
            }
        }
    }

    throw new RuntimeException("Failed to find suitable memory type.");
}
    public VkPhysicalDevice getDevice() {
        return device;
    }

    public QueueFamilyIndices getQueueFamilies() {
        return queueFamilies;
    }
}