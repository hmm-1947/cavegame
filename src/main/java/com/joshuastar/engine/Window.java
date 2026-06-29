package com.joshuastar.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class Window {

    private final String title;
    private final int width;
    private final int height;

    private long handle;

    public Window(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public void create() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        handle = GLFW.glfwCreateWindow(width, height, title, 0, 0);

        if (handle == 0) {
            throw new RuntimeException("Failed to create window");
        }
    }

    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }

    public long getHandle() {
        return handle;
    }
    public int getWidth() {
    return width;
    }

    public int getHeight() {
        return height;
    }
}