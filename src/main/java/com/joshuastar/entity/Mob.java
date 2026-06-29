package com.joshuastar.entity;

public class Mob extends Entity {

    protected float yaw;
    protected float wanderTimer;
    protected float idleTimer;
    protected boolean idling;

    private static final java.util.Random RANDOM = new java.util.Random();

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void updateWanderAI(com.joshuastar.world.World world, float dt) {

        if (idling) {
            idleTimer -= dt;
            if (idleTimer <= 0f) {
                idling = false;
                wanderTimer = 1.0f + RANDOM.nextFloat() * 2.0f;
                yaw = RANDOM.nextFloat() * 360f;
            }
            setVelocity(0f, velocityY, 0f);
            return;
        }

        wanderTimer -= dt;

        if (wanderTimer <= 0f) {
            idling = true;
            idleTimer = 1.0f + RANDOM.nextFloat() * 2.0f;
            setVelocity(0f, velocityY, 0f);
            return;
        }

        float speed = getMoveSpeed();
        float radians = (float) Math.toRadians(yaw);

        float dx = (float) Math.sin(radians) * speed;
        float dz = (float) -Math.cos(radians) * speed;

    if (!canMoveTo(world, x + dx, z + dz)) {
            idling = true;
            idleTimer = 0.5f;
            setVelocity(0f, velocityY, 0f);
            return;
        }

        setVelocity(dx, velocityY, dz);
    }

    protected float getMoveSpeed() {
        return 1.0f;
    }

    protected boolean canMoveTo(com.joshuastar.world.World world, float nx, float nz) {

        int groundY = findGroundY(world, nx, nz);

        if (groundY < 0) {
            return false;
        }

        return Math.abs(groundY - y) <= 1.5f;
    }

    protected int findGroundY(com.joshuastar.world.World world, float wx, float wz) {

        int bx = (int) Math.floor(wx);
        int bz = (int) Math.floor(wz);

        for (int by = com.joshuastar.world.Chunk.SIZE_Y - 1; by > 0; by--) {
            if (world.getBlock(bx, by, bz) != 0) {
                return by + 1;
            }
        }

        return -1;
    }

    public void tickPhysics(com.joshuastar.world.World world) {

        float feetY = y;
        int groundY = findGroundY(world, x + velocityX, z + velocityZ);

        if (groundY >= 0) {
            x += velocityX;
            z += velocityZ;
            y = groundY;
        }
    }
}