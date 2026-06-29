package com.joshuastar.world;

public class RayCaster {

    public static class Hit {
        public final int x, y, z;
        public final int px, py, pz;

        public Hit(int x, int y, int z, int px, int py, int pz) {
            this.x = x; this.y = y; this.z = z;
            this.px = px; this.py = py; this.pz = pz;
        }
    }

    public static Hit cast(World world, float startX, float startY, float startZ, float yaw, float pitch, float maxDistance) {

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float dx = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float dy = (float) Math.sin(pitchRad);
        float dz = (float) (-Math.cos(pitchRad) * Math.cos(yawRad));

        float step = 0.05f;
        int prevX = Integer.MIN_VALUE, prevY = Integer.MIN_VALUE, prevZ = Integer.MIN_VALUE;

        for (float t = 0; t < maxDistance; t += step) {

            float px = startX + dx * t;
            float py = startY + dy * t;
            float pz = startZ + dz * t;

            int bx = (int) Math.floor(px);
            int by = (int) Math.floor(py);
            int bz = (int) Math.floor(pz);

            if (world.getBlock(bx, by, bz) != 0) {
                return new Hit(bx, by, bz, prevX, prevY, prevZ);
            }

            prevX = bx;
            prevY = by;
            prevZ = bz;
        }

        return null;
    }
}