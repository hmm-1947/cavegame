package com.joshuastar.renderer.vulkan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

public final class VulkanUtils {

    private VulkanUtils() {
    }

    public static ByteBuffer loadShader(String resource) {

        try (InputStream input = VulkanUtils.class.getResourceAsStream(resource)) {

            if (input == null) {
                throw new RuntimeException("Shader not found: " + resource);
            }

            byte[] bytes = input.readAllBytes();
System.out.println("Loaded shader: " + resource + " (" + bytes.length + " bytes)");
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            return buffer;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}