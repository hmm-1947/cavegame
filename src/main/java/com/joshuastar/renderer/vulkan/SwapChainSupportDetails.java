package com.joshuastar.renderer.vulkan;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportDetails {

    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private int[] presentModes;

    public VkSurfaceCapabilitiesKHR getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(VkSurfaceCapabilitiesKHR capabilities) {
        this.capabilities = capabilities;
    }

    public VkSurfaceFormatKHR.Buffer getFormats() {
        return formats;
    }

    public void setFormats(VkSurfaceFormatKHR.Buffer formats) {
        this.formats = formats;
    }

    public int[] getPresentModes() {
        return presentModes;
    }

    public void setPresentModes(int[] presentModes) {
        this.presentModes = presentModes;
    }
}