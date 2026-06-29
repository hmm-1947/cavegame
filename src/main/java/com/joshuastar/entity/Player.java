package com.joshuastar.entity;

public class Player extends Entity {

    private float yaw;
    private float pitch;

    public Player() {
        setPosition(8.0f, 80.0f, 8.0f);
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}