package com.joshuastar.renderer.vulkan;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class UniformBufferObject {

    public static final int NUM_CASCADES = 3;

    public final Matrix4f model = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f[] lightSpaceMatrices = new Matrix4f[] {
            new Matrix4f(), new Matrix4f(), new Matrix4f()
    };
    public final Vector4f cascadeSplits = new Vector4f();

    public static final int SIZEOF =
            (16 * Float.BYTES * 3) + (16 * Float.BYTES * NUM_CASCADES) + (4 * Float.BYTES);
}