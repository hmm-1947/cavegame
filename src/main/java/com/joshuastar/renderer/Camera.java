package com.joshuastar.renderer;

import com.joshuastar.entity.Player;

public class Camera {

    private float x;
    private float y;
    private float z;

    private float yaw;
    private float pitch;

    public void follow(Player player) {
        x = player.getX();
        y = player.getY();
        z = player.getZ();

        yaw = player.getYaw();
        pitch = player.getPitch();
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
    public void move(float dx, float dy, float dz) {
    x += dx;
    y += dy;
    z += dz;
}
public void setX(float x) { this.x = x; }
public void setY(float y) { this.y = y; }
public void setZ(float z) { this.z = z; }
public void rotate(float yawDelta, float pitchDelta) {

    yaw += yawDelta;
    pitch += pitchDelta;

    if (pitch > 89.0f)
        pitch = 89.0f;

    if (pitch < -89.0f)
        pitch = -89.0f;
}

public void setRotation(float yaw, float pitch) {
    this.yaw = yaw;
    this.pitch = pitch;
}
}