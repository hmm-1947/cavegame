package com.joshuastar.entity;

public class Cow extends Mob {

    public static final float BODY_WIDTH = 0.9f;
    public static final float BODY_HEIGHT = 0.9f;
    public static final float BODY_LENGTH = 1.3f;

    public static final float HEAD_SIZE = 0.5f;
    public static final float LEG_WIDTH = 0.3f;
    public static final float LEG_HEIGHT = 0.7f;

    public Cow(float x, float y, float z) {
        setPosition(x, y, z);
        this.yaw = (float) (Math.random() * 360.0);
        this.wanderTimer = 1.0f + (float) Math.random() * 2.0f;
    }

    @Override
    protected float getMoveSpeed() {
        return 0.04f;
    }

    public String getTexturePath() {
        return "/textures/cow.png";
    }
}