package com.joshuastar.entity;

public class Player extends Entity {

    private float yaw;
    private float pitch;
    private final com.joshuastar.world.Inventory inventory = new com.joshuastar.world.Inventory();

    public Player() {
        setPosition(8.0f, 80.0f, 8.0f);
    }

    public com.joshuastar.world.Inventory getInventory() {
        return inventory;
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