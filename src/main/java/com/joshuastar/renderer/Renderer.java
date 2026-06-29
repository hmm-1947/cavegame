package com.joshuastar.renderer;

import com.joshuastar.engine.Window;

public class Renderer {

    private final VulkanRenderer vulkanRenderer;
    private final Camera camera;

    public Renderer() {
        vulkanRenderer = new VulkanRenderer();
        camera = new Camera();
    }

    public void init(Window window) {
        vulkanRenderer.init(window);
    }

    public void render() {
        vulkanRenderer.render(camera);
    }

    public void cleanup() {
        vulkanRenderer.cleanup();
    }

    public Camera getCamera() {
        return camera;
    }

    public VulkanRenderer getVulkanRenderer() {
        return vulkanRenderer;
    }
}