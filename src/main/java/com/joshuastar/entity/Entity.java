package com.joshuastar.entity;

public class Entity {

    protected float x;
    protected float y;
    protected float z;

    protected float velocityX;
    protected float velocityY;
    protected float velocityZ;

    public void tick() {
        x += velocityX;
        y += velocityY;
        z += velocityZ;
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setVelocity(float velocityX, float velocityY, float velocityZ) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getVelocityZ() {
        return velocityZ;
    }
}