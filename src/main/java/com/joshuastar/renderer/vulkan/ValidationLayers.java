package com.joshuastar.renderer.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

public final class ValidationLayers {

    public static final boolean ENABLED = false;

    public static final String[] LAYERS = {
            "VK_LAYER_KHRONOS_validation"
    };

    private ValidationLayers() {
    }

    public static PointerBuffer getLayers(MemoryStack stack) {
        return null;
    }

    public static void checkSupport() {
    }
}