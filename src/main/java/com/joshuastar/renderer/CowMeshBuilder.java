package com.joshuastar.renderer;

import com.joshuastar.entity.Cow;

public final class CowMeshBuilder {

    private static final int TEX_W = 64;
    private static final int TEX_H = 32;

    private CowMeshBuilder() {
    }

    public static Mesh build() {

        MobMesh mesh = new MobMesh();

        mesh.addBox(
                0f, 0.55f, -0.95f,
                Cow.HEAD_SIZE, Cow.HEAD_SIZE, Cow.HEAD_SIZE,
                0, 0, TEX_W, TEX_H, 8, 8);

        mesh.addBox(
                0f, Cow.LEG_HEIGHT + Cow.BODY_HEIGHT / 2f, 0f,
                Cow.BODY_LENGTH, Cow.BODY_HEIGHT, Cow.BODY_WIDTH,
                18, 4, TEX_W, TEX_H, 10, 9);

        float legY = Cow.LEG_HEIGHT / 2f;
        float legOffsetX = Cow.BODY_WIDTH / 2f - Cow.LEG_WIDTH / 2f;
        float legOffsetZ = Cow.BODY_LENGTH / 2f - Cow.LEG_WIDTH / 2f - 0.05f;

        mesh.addBox(-legOffsetX, legY, legOffsetZ,
                Cow.LEG_WIDTH, Cow.LEG_HEIGHT, Cow.LEG_WIDTH,
                0, 12, TEX_W, TEX_H, 4, 12);

        mesh.addBox(legOffsetX, legY, legOffsetZ,
                Cow.LEG_WIDTH, Cow.LEG_HEIGHT, Cow.LEG_WIDTH,
                4, 12, TEX_W, TEX_H, 4, 12);

        mesh.addBox(-legOffsetX, legY, -legOffsetZ,
                Cow.LEG_WIDTH, Cow.LEG_HEIGHT, Cow.LEG_WIDTH,
                8, 12, TEX_W, TEX_H, 4, 12);

        mesh.addBox(legOffsetX, legY, -legOffsetZ,
                Cow.LEG_WIDTH, Cow.LEG_HEIGHT, Cow.LEG_WIDTH,
                12, 12, TEX_W, TEX_H, 4, 12);

        return mesh.getMesh();
    }
}