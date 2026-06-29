package com.joshuastar.engine;

import org.lwjgl.glfw.GLFW;

import com.joshuastar.entity.Player;
import com.joshuastar.renderer.Camera;
import com.joshuastar.renderer.Renderer;
import com.joshuastar.world.World;

public class Engine {

    private Window window;
    private Input input;

    private Renderer renderer;
    private World world;
    private Player player;
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouse = true;
    private boolean leftWasPressed = false;
    private boolean rightWasPressed = false;

    private boolean flying = false;
    private boolean gWasPressed = false;

    private float velocityX = 0f;
    private float velocityY = 0f;
    private float velocityZ = 0f;

    private static final float PLAYER_WIDTH = 0.6f;
    private boolean spaceWasPressed = false;
    private static final float PLAYER_HEIGHT = 1.8f;
    private static final float PLAYER_EYE_HEIGHT = 1.62f;

    private static final float GRAVITY = 0.08f;
    private static final float DRAG = 0.98f;
    private static final float JUMP_MOTION = 0.42f;
    private static final float GROUND_SLIPPERINESS = 0.546f;
    private static final float AIR_FRICTION = 0.91f;
    private static final float MOVE_SPEED = 0.026f;
    private static final float SPRINT_MULTIPLIER = 1.3f;
    private static final float SNEAK_MULTIPLIER = 0.3f;
    private static final float STEP_HEIGHT = 0.6f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {

        window = new Window("Minecraft", 1280, 720);
        window.create();

        input = new Input(window.getHandle());
        GLFW.glfwSetInputMode(
                window.getHandle(),
                GLFW.GLFW_CURSOR,
                GLFW.GLFW_CURSOR_DISABLED);
        world = new World();
        player = new Player();

        renderer = new Renderer();
        renderer.init(window);
        com.joshuastar.world.World w = renderer.getVulkanRenderer().getWorld();
        int spawnX = 8;
        int spawnZ = 40;
        int spawnY = findSpawnHeight(w, spawnX, spawnZ);
        renderer.getCamera().setPosition(spawnX, spawnY, spawnZ);
    }

    private int findSpawnHeight(com.joshuastar.world.World w, int x, int z) {
        for (int y = com.joshuastar.world.Chunk.SIZE_Y - 1; y > 0; y--) {
            if (w.getBlock(x, y, z) != 0) {
                return (int) (y + 1 + PLAYER_EYE_HEIGHT);
            }
        }
        return 80;
    }

    private void loop() {

        while (!window.shouldClose()) {
            double mouseX = input.getMouseX();
            double mouseY = input.getMouseY();

            if (firstMouse) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                firstMouse = false;
            }

            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            renderer.getCamera().rotate(
                    (float) deltaX * 0.15f,
                    (float) -deltaY * 0.15f);
            if (input.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                GLFW.glfwSetWindowShouldClose(window.getHandle(), true);
            }

            boolean gPressed = input.isKeyPressed(GLFW.GLFW_KEY_G);
            if (gPressed && !gWasPressed) {
                flying = !flying;
                velocityY = 0f;
            }
            gWasPressed = gPressed;

            com.joshuastar.world.World world = renderer.getVulkanRenderer().getWorld();
            Camera cam = renderer.getCamera();

            float yaw = (float) Math.toRadians(cam.getYaw());

            float forward = 0f;
            float strafe = 0f;

            if (input.isKeyPressed(GLFW.GLFW_KEY_W))
                forward += 1f;
            if (input.isKeyPressed(GLFW.GLFW_KEY_S))
                forward -= 1f;
            if (input.isKeyPressed(GLFW.GLFW_KEY_D))
                strafe += 1f;
            if (input.isKeyPressed(GLFW.GLFW_KEY_A))
                strafe -= 1f;

            float inputX = 0f;
            float inputZ = 0f;

            if (forward != 0f || strafe != 0f) {
                float len = (float) Math.sqrt(forward * forward + strafe * strafe);
                forward /= len;
                strafe /= len;

                inputX = (float) (forward * Math.sin(yaw) + strafe * Math.cos(yaw));
                inputZ = (float) (-forward * Math.cos(yaw) + strafe * Math.sin(yaw));
            }

            if (flying) {

                velocityX = inputX * 0.5f;
                velocityZ = inputZ * 0.5f;
                velocityY = 0f;

                if (input.isKeyPressed(GLFW.GLFW_KEY_SPACE))
                    velocityY = 0.5f;
                if (input.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT))
                    velocityY = -0.5f;

                moveXAndCollide(world, cam, velocityX);
                moveZAndCollide(world, cam, velocityZ);
                moveYAndCollide(world, cam, velocityY);

            } else {
                boolean onGround = isOnGround(world, cam);

                float forwardInput = forward;
                float strafeInput = strafe;

                float length = forwardInput * forwardInput + strafeInput * strafeInput;

                if (length >= 1.0E-4F) {
                    length = (float) Math.sqrt(length);

                    if (length < 1.0f)
                        length = 1.0f;

                    float accel = MOVE_SPEED / length;

                    if (input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL))
                        accel *= SPRINT_MULTIPLIER;

                    if (input.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT))
                        accel *= SNEAK_MULTIPLIER;

                    accel *= onGround ? 0.16277136F / (GROUND_SLIPPERINESS * GROUND_SLIPPERINESS * GROUND_SLIPPERINESS)
                            : 0.02F / MOVE_SPEED;

                    velocityX += inputX * accel;
                    velocityZ += inputZ * accel;
                }

                moveXAndCollide(world, cam, velocityX);
                moveZAndCollide(world, cam, velocityZ);

                boolean spacePressed = input.isKeyPressed(GLFW.GLFW_KEY_SPACE);

                if (onGround && spacePressed && !spaceWasPressed) {
                    velocityY = 0.42f;

                    if (input.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
                        velocityX -= (float) Math.sin(yaw) * 0.2f;
                        velocityZ += (float) Math.cos(yaw) * 0.2f;
                    }
                }

                spaceWasPressed = spacePressed;

                velocityY -= 0.08f;
                moveYAndCollide(world, cam, velocityY);
                velocityY *= 0.98f;

                float friction = onGround ? GROUND_SLIPPERINESS : AIR_FRICTION;

                velocityX *= friction;
                velocityZ *= friction;
            }
            boolean leftPressed = input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            boolean rightPressed = input.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

            if (leftPressed && !leftWasPressed) {
                handleBreak();
            }

            if (rightPressed && !rightWasPressed) {
                handlePlace();
            }

            leftWasPressed = leftPressed;
            rightWasPressed = rightPressed;
            renderer.render();

            window.pollEvents();
        }
    }

    private void cleanup() {
        renderer.cleanup();
        window.destroy();
    }

    private void handleBreak() {

        com.joshuastar.world.World w = renderer.getVulkanRenderer().getWorld();
        Camera cam = renderer.getCamera();

        com.joshuastar.world.RayCaster.Hit hit = com.joshuastar.world.RayCaster.cast(
                w, cam.getX(), cam.getY(), cam.getZ(), cam.getYaw(), cam.getPitch(), 8.0f);

        if (hit == null)
            return;

        w.setBlock(hit.x, hit.y, hit.z, (short) 0);

        rebuildAffectedChunks(hit.x, hit.z);
    }

    private boolean isSolid(com.joshuastar.world.World world, float x, float y, float z) {
        int bx = (int) Math.floor(x);
        int by = (int) Math.floor(y);
        int bz = (int) Math.floor(z);
        return world.getBlock(bx, by, bz) != 0;
    }

    private boolean isOnGround(com.joshuastar.world.World world, Camera cam) {
        float feetY = cam.getY() - PLAYER_EYE_HEIGHT;
        return checkCollisionAt(world, cam.getX(), feetY - 0.1f, cam.getZ())
                && velocityY <= 0f;
    }

    private boolean checkCollisionAt(com.joshuastar.world.World world, float x, float feetY, float z) {
        float half = PLAYER_WIDTH / 2f - 0.001f;
        float epsilon = 0.001f;
        float headY = feetY + PLAYER_HEIGHT;

        float[] xs = { x - half, x + half };
        float[] zs = { z - half, z + half };
        float[] ys = { feetY + epsilon, headY - epsilon };

        for (float ix : xs)
            for (float iz : zs)
                for (float iy : ys)
                    if (isSolid(world, ix, iy, iz))
                        return true;

        return false;
    }

    private void moveXAndCollide(com.joshuastar.world.World world, Camera cam, float dx) {
        if (dx == 0f)
            return;

        float feetY = cam.getY() - PLAYER_EYE_HEIGHT;
        float newX = cam.getX() + dx;

        if (!checkCollisionAt(world, newX, feetY, cam.getZ())) {
            cam.setX(newX);
            return;
        }

        if (!checkCollisionAt(world, newX, feetY + STEP_HEIGHT, cam.getZ())) {
            cam.setY(cam.getY() + STEP_HEIGHT);
            cam.setX(newX);
            return;
        }

        velocityX = 0f;
    }

    private void moveZAndCollide(com.joshuastar.world.World world, Camera cam, float dz) {
        if (dz == 0f)
            return;

        float feetY = cam.getY() - PLAYER_EYE_HEIGHT;
        float newZ = cam.getZ() + dz;

        if (!checkCollisionAt(world, cam.getX(), feetY, newZ)) {
            cam.setZ(newZ);
            return;
        }

        if (!checkCollisionAt(world, cam.getX(), feetY + STEP_HEIGHT, newZ)) {
            cam.setY(cam.getY() + STEP_HEIGHT);
            cam.setZ(newZ);
            return;
        }

        velocityZ = 0f;
    }

    private void moveYAndCollide(com.joshuastar.world.World world, Camera cam, float dy) {
        if (dy == 0f)
            return;
        float newY = cam.getY() + dy;
        float feetY = newY - PLAYER_EYE_HEIGHT;

        if (!checkCollisionAt(world, cam.getX(), feetY, cam.getZ())) {
            cam.setY(newY);
        } else {
            velocityY = 0f;
        }
    }

    private void handlePlace() {

        com.joshuastar.world.World w = renderer.getVulkanRenderer().getWorld();
        Camera cam = renderer.getCamera();

        com.joshuastar.world.RayCaster.Hit hit = com.joshuastar.world.RayCaster.cast(
                w, cam.getX(), cam.getY(), cam.getZ(), cam.getYaw(), cam.getPitch(), 8.0f);

        if (hit == null || hit.px == Integer.MIN_VALUE)
            return;

        if (intersectsPlayer(cam, hit.px, hit.py, hit.pz))
            return;

        w.setBlock(hit.px, hit.py, hit.pz, (short) 1);

        rebuildAffectedChunks(hit.px, hit.pz);
    }

    private boolean intersectsPlayer(Camera cam, int bx, int by, int bz) {

        float half = PLAYER_WIDTH / 2f;
        float feetY = cam.getY() - PLAYER_EYE_HEIGHT;
        float headY = feetY + PLAYER_HEIGHT;

        float minX = cam.getX() - half;
        float maxX = cam.getX() + half;
        float minZ = cam.getZ() - half;
        float maxZ = cam.getZ() + half;

        boolean overlapX = bx + 1 > minX && bx < maxX;
        boolean overlapY = by + 1 > feetY && by < headY;
        boolean overlapZ = bz + 1 > minZ && bz < maxZ;

        return overlapX && overlapY && overlapZ;
    }

    private void rebuildAffectedChunks(int blockX, int blockZ) {

        int chunkX = Math.floorDiv(blockX, com.joshuastar.world.Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(blockZ, com.joshuastar.world.Chunk.SIZE_Z);

        renderer.getVulkanRenderer().rebuildChunkAt(chunkX, chunkZ);
    }
}