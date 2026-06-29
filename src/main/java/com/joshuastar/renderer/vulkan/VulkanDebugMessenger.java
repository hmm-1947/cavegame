package com.joshuastar.renderer.vulkan;

public class VulkanDebugMessenger {

    public VulkanDebugMessenger() {
    }

    public void create(org.lwjgl.vulkan.VkInstance instance) {
    }

    public void destroy(org.lwjgl.vulkan.VkInstance instance) {
    }

    public static String getExtensionName() {
        return org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
    }
}