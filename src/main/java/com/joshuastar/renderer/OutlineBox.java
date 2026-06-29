package com.joshuastar.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OutlineBox {

    public static final float[] VERTICES = {
        0,0,0, 1,0,0,
        1,0,0, 1,0,1,
        1,0,1, 0,0,1,
        0,0,1, 0,0,0,

        0,1,0, 1,1,0,
        1,1,0, 1,1,1,
        1,1,1, 0,1,1,
        0,1,1, 0,1,0,

        0,0,0, 0,1,0,
        1,0,0, 1,1,0,
        1,0,1, 1,1,1,
        0,0,1, 0,1,1
    };

    public static byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(VERTICES.length * Float.BYTES).order(ByteOrder.nativeOrder());
        for (float v : VERTICES) buffer.putFloat(v);
        return buffer.array();
    }

    private OutlineBox() {}
}