package com.joshuastar;

import org.lwjgl.system.Configuration;

import com.joshuastar.engine.Engine;

public class Main {
    public static void main(String[] args) {
        // Increase LWJGL thread stack size to 512 KB to handle Vulkan extension queries
        Configuration.STACK_SIZE.set(512);

        // Your existing engine startup code
        Engine engine = new Engine();
        engine.run();
    }
}