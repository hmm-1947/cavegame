package com.joshuastar.renderer.vulkan;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_API_VERSION_1_0;
import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

public class VulkanInstance {

    private VkInstance instance;

    public void create(VulkanDebugMessenger debugMessenger) {

        ValidationLayers.checkSupport();

        try (MemoryStack stack = stackPush()) {

            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationName(stack.UTF8("Minecraft"))
                    .applicationVersion(VK_MAKE_VERSION(1, 0, 0))
                    .pEngineName(stack.UTF8("JoshuaEngine"))
                    .engineVersion(VK_MAKE_VERSION(1, 0, 0))
                    .apiVersion(VK_API_VERSION_1_0);

            PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

            if (glfwExtensions == null) {
                throw new RuntimeException("GLFW failed to return Vulkan extensions.");
            }

            PointerBuffer extensions = stack.mallocPointer(
                    glfwExtensions.remaining() + (ValidationLayers.ENABLED ? 1 : 0));

            while (glfwExtensions.hasRemaining()) {
                extensions.put(glfwExtensions.get());
            }

            if (ValidationLayers.ENABLED) {
                extensions.put(stack.UTF8("VK_EXT_debug_utils"));
            }
            extensions.flip();

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(extensions);

            createInfo.ppEnabledLayerNames(null);

            PointerBuffer pInstance = stack.mallocPointer(1);

            int result = vkCreateInstance(
                    createInfo,
                    null,
                    pInstance);

            if (result != VK_SUCCESS) {
                throw new RuntimeException("vkCreateInstance failed: " + result);
            }

            instance = new VkInstance(
                    pInstance.get(0),
                    createInfo);
        }
    }

    public void destroy() {
        if (instance != null) {
            vkDestroyInstance(instance, null);
            instance = null;
        }
    }

    public VkInstance getInstance() {
        return instance;
    }
}