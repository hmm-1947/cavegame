package com.joshuastar.renderer;

public class MobMesh {

    private final Mesh mesh = new Mesh();

    public Mesh getMesh() {
        return mesh;
    }

    public void addBox(
            float centerX, float centerY, float centerZ,
            float width, float height, float depth,
            float u0Px, float v0Px, int textureWidth, int textureHeight,
            int faceU, int faceV) {

        float hw = width / 2f;
        float hh = height / 2f;
        float hd = depth / 2f;

        addFace(
                centerX - hw, centerY - hh, centerZ - hd,
                centerX - hw, centerY - hh, centerZ + hd,
                centerX - hw, centerY + hh, centerZ + hd,
                centerX - hw, centerY + hh, centerZ - hd,
                -1, 0, 0,
                u0Px, v0Px + faceV, faceV, faceU + faceV,
                textureWidth, textureHeight, depth, height);

        addFace(
                centerX + hw, centerY - hh, centerZ + hd,
                centerX + hw, centerY - hh, centerZ - hd,
                centerX + hw, centerY + hh, centerZ - hd,
                centerX + hw, centerY + hh, centerZ + hd,
                1, 0, 0,
                u0Px + faceV + faceU, v0Px + faceV, faceV, faceU + faceV,
                textureWidth, textureHeight, depth, height);

        addFace(
                centerX - hw, centerY + hh, centerZ - hd,
                centerX + hw, centerY + hh, centerZ - hd,
                centerX + hw, centerY - hh, centerZ - hd,
                centerX - hw, centerY - hh, centerZ - hd,
                0, 0, -1,
                u0Px + faceV, v0Px + faceV, faceU, faceU + faceV,
                textureWidth, textureHeight, width, height);

        addFace(
                centerX + hw, centerY + hh, centerZ + hd,
                centerX - hw, centerY + hh, centerZ + hd,
                centerX - hw, centerY - hh, centerZ + hd,
                centerX + hw, centerY - hh, centerZ + hd,
                0, 0, 1,
                u0Px + faceV + faceU + faceV, v0Px + faceV, faceU, faceU + faceV,
                textureWidth, textureHeight, width, height);

        addFace(
                centerX - hw, centerY + hh, centerZ - hd,
                centerX - hw, centerY + hh, centerZ + hd,
                centerX + hw, centerY + hh, centerZ + hd,
                centerX + hw, centerY + hh, centerZ - hd,
                0, 1, 0,
                u0Px + faceV, v0Px, faceU, faceV,
                textureWidth, textureHeight, width, depth);

        addFace(
                centerX - hw, centerY - hh, centerZ + hd,
                centerX - hw, centerY - hh, centerZ - hd,
                centerX + hw, centerY - hh, centerZ - hd,
                centerX + hw, centerY - hh, centerZ + hd,
                0, -1, 0,
                u0Px + faceV + faceU, v0Px, faceU, faceV,
                textureWidth, textureHeight, width, depth);
    }

    private void addFace(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float nx, float ny, float nz,
            float uPx, float vPx, float wPx, float hPx,
            int texW, int texH,
            float worldW, float worldH) {

        float u0 = uPx / texW;
        float v0 = vPx / texH;
        float u1 = (uPx + wPx) / texW;
        float v1 = (vPx + hPx) / texH;

        int start = mesh.getVertexCount();

        mesh.addVertex(new Vertex(x0, y0, z0, u0, v1, nx, ny, nz, 1.0f));
        mesh.addVertex(new Vertex(x1, y1, z1, u1, v1, nx, ny, nz, 1.0f));
        mesh.addVertex(new Vertex(x2, y2, z2, u1, v0, nx, ny, nz, 1.0f));
        mesh.addVertex(new Vertex(x3, y3, z3, u0, v0, nx, ny, nz, 1.0f));

        mesh.addIndex(start);
        mesh.addIndex(start + 1);
        mesh.addIndex(start + 2);

        mesh.addIndex(start + 2);
        mesh.addIndex(start + 3);
        mesh.addIndex(start);
    }
}