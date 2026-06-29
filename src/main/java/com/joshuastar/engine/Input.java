
package com.joshuastar.engine;

import org.lwjgl.glfw.GLFW;

public class Input {

    private final long window;

    public Input(long window) {
        this.window = window;
    }

    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public boolean isMouseButtonPressed(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public double getMouseX() {
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        return x[0];
    }

    public int getPressedHotbarKey() {
        for (int i = 0; i < 9; i++) {
            if (isKeyPressed(GLFW.GLFW_KEY_1 + i)) {
                return i;
            }
        }
        return -1;
    }

    public double getMouseY() {
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(window, x, y);
        return y[0];
    }
}