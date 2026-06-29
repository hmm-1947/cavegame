package com.joshuastar.renderer;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetDepthBias;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

import com.joshuastar.engine.Window;
import com.joshuastar.renderer.vulkan.UniformBufferObject;
import com.joshuastar.renderer.vulkan.VulkanBuffer;
import com.joshuastar.renderer.vulkan.VulkanCommandBuffers;
import com.joshuastar.renderer.vulkan.VulkanContext;
import com.joshuastar.renderer.vulkan.VulkanCrosshairPipeline;
import com.joshuastar.renderer.vulkan.VulkanDepthImage;
import com.joshuastar.renderer.vulkan.VulkanDescriptorPool;
import com.joshuastar.renderer.vulkan.VulkanDescriptorSetLayout;
import com.joshuastar.renderer.vulkan.VulkanDescriptorSets;
import com.joshuastar.renderer.vulkan.VulkanFramebuffers;
import com.joshuastar.renderer.vulkan.VulkanGraphicsPipeline;
import com.joshuastar.renderer.vulkan.VulkanImageViews;
import com.joshuastar.renderer.vulkan.VulkanLinePipeline;
import com.joshuastar.renderer.vulkan.VulkanMesh;
import com.joshuastar.renderer.vulkan.VulkanRenderPass;
import com.joshuastar.renderer.vulkan.VulkanShadowFramebuffers;
import com.joshuastar.renderer.vulkan.VulkanShadowMap;
import com.joshuastar.renderer.vulkan.VulkanShadowPipeline;
import com.joshuastar.renderer.vulkan.VulkanShadowRenderPass;
import com.joshuastar.renderer.vulkan.VulkanSwapChain;
import com.joshuastar.renderer.vulkan.VulkanSyncObjects;
import com.joshuastar.renderer.vulkan.VulkanTexture;
import com.joshuastar.renderer.vulkan.VulkanUniformBuffer;
import com.joshuastar.world.RayCaster;

public class VulkanRenderer {

        private VulkanContext context;

        private VulkanSwapChain swapChain;
        private VulkanImageViews imageViews;
        private VulkanLinePipeline outlinePipeline;
        private VulkanCrosshairPipeline crosshairPipeline;
        private VulkanBuffer outlineVertexBuffer;
        private VulkanBuffer crosshairVertexBuffer;
        private VulkanRenderPass renderPass;
        private VulkanTexture texture;
        private VulkanFramebuffers framebuffers;
        private VulkanCommandBuffers commandBuffers;
        private VulkanSyncObjects syncObjects;
        private VulkanGraphicsPipeline graphicsPipeline;
        private VulkanDescriptorSetLayout descriptorSetLayout;
        private VulkanDescriptorPool descriptorPool;
        private VulkanDescriptorSets descriptorSets;
        private VulkanUniformBuffer uniformBuffer;
        private UniformBufferObject ubo;
        private ChunkRenderer chunkRenderer;
        private ChunkManager chunkManager;
        private VulkanDepthImage depthImage;
        private VulkanShadowMap shadowMap;
        private VulkanShadowRenderPass shadowRenderPass;
        private VulkanShadowFramebuffers shadowFramebuffers;
        private VulkanShadowPipeline shadowPipeline;
        private final org.joml.Vector3f lightDirection = new org.joml.Vector3f(0.4f, -1.0f, 0.3f).normalize();

        private MobManager mobManager;
        private com.joshuastar.renderer.vulkan.VulkanMobPipeline mobPipeline;
        private VulkanTexture cowTexture;
        private com.joshuastar.renderer.vulkan.VulkanMesh cowMesh;

        public void init(Window window) {
                context = new VulkanContext();
                context.create(window.getHandle());

                VkDevice device = context.getDevice();

                swapChain = new VulkanSwapChain();
                swapChain.create(device, context.getSurface(), context.getQueueFamilies(), window.getWidth(),
                                window.getHeight());

                imageViews = new VulkanImageViews();
                imageViews.create(device, swapChain.getImages(), swapChain.getFormat());

                renderPass = new VulkanRenderPass();
                renderPass.create(
                                device,
                                swapChain.getFormat());
                depthImage = new VulkanDepthImage();

                depthImage.create(
                                context.getPhysicalDevice(),
                                device,
                                swapChain.getExtent().width(),
                                swapChain.getExtent().height());
                descriptorSetLayout = new VulkanDescriptorSetLayout();
                descriptorSetLayout.create(device);
                uniformBuffer = new VulkanUniformBuffer();
                uniformBuffer.create(
                                context.getPhysicalDevice(),
                                device);
                descriptorPool = new VulkanDescriptorPool();
                descriptorPool.create(device);

                commandBuffers = new VulkanCommandBuffers();
                commandBuffers.create(device, context.getQueueFamilies());

                String[] tilePaths = new String[TextureAtlas.ATLAS_SIZE * TextureAtlas.ATLAS_SIZE];
                tilePaths[0] = "/textures/top.png";
                tilePaths[2] = "/textures/side.png";
                tilePaths[3] = "/textures/side.png";
                tilePaths[4] = "/textures/side.png";
                tilePaths[5] = "/textures/side.png";
                tilePaths[6] = "/textures/sand.png";
                tilePaths[7] = "/textures/snow.png";
                tilePaths[8] = "/textures/log_top.png";
                tilePaths[9] = "/textures/log_side.png";
                tilePaths[10] = "/textures/leaves.png";

                BufferedImage img = TextureLoader.buildAtlas(tilePaths, 16, TextureAtlas.ATLAS_SIZE);
                byte[] pixels = TextureLoader.loadRGBA(img);

                int width = img.getWidth();
                int height = img.getHeight();

                texture = new VulkanTexture();
                texture.create(
                                context.getPhysicalDevice(),
                                device,
                                pixels,
                                context.getGraphicsQueue(),
                                commandBuffers.getCommandPool(),
                                width,
                                height);

                BufferedImage cowImg = TextureLoader.load("/textures/cow.png");
                byte[] cowPixels = TextureLoader.loadRGBA(cowImg);

                cowTexture = new VulkanTexture();
                cowTexture.create(
                                context.getPhysicalDevice(),
                                device,
                                cowPixels,
                                context.getGraphicsQueue(),
                                commandBuffers.getCommandPool(),
                                cowImg.getWidth(),
                                cowImg.getHeight());

                shadowMap = new VulkanShadowMap();
                shadowMap.create(context.getPhysicalDevice(), device);

                shadowRenderPass = new VulkanShadowRenderPass();
                shadowRenderPass.create(device);

                shadowFramebuffers = new VulkanShadowFramebuffers();
                shadowFramebuffers.create(device, shadowRenderPass.getRenderPass(), shadowMap);

                shadowPipeline = new VulkanShadowPipeline();
                shadowPipeline.create(device, shadowRenderPass.getRenderPass());

                descriptorSets = new VulkanDescriptorSets();
                descriptorSets.create(
                                device,
                                descriptorPool,
                                descriptorSetLayout,
                                uniformBuffer,
                                texture,
                                shadowMap);

                descriptorSets.createMobSet(
                                device,
                                descriptorPool,
                                descriptorSetLayout,
                                uniformBuffer,
                                cowTexture,
                                shadowMap);

                ubo = new UniformBufferObject();

                graphicsPipeline = new VulkanGraphicsPipeline();
                graphicsPipeline.create(
                                device,
                                renderPass.getRenderPass(),
                                descriptorSetLayout.getDescriptorSetLayout());

                mobPipeline = new com.joshuastar.renderer.vulkan.VulkanMobPipeline();
                mobPipeline.create(
                                device,
                                renderPass.getRenderPass(),
                                descriptorSetLayout.getDescriptorSetLayout());

                framebuffers = new VulkanFramebuffers();
                framebuffers.create(
                                device,
                                renderPass.getRenderPass(),
                                imageViews.getImageViews(),
                                depthImage.getImageView(),
                                swapChain.getExtent());

                commandBuffers = new VulkanCommandBuffers();
                commandBuffers.create(device, context.getQueueFamilies());

                syncObjects = new VulkanSyncObjects();
                syncObjects.create(device);
                com.joshuastar.world.World world = new com.joshuastar.world.World();

                chunkRenderer = new ChunkRenderer(world);

                mobManager = new MobManager(world);

                chunkManager = new ChunkManager(
                                context.getPhysicalDevice(),
                                context.getDevice(),
                                world,
                                chunkRenderer,
                                context.getAllocator(),
                                mobManager);

                cowMesh = new com.joshuastar.renderer.vulkan.VulkanMesh(CowMeshBuilder.build());
                cowMesh.upload(
                                context.getPhysicalDevice(),
                                device,
                                context.getAllocator());

                chunkManager.update(0, 0, 2);

                int initialWaitMs = 0;
                while (initialWaitMs < 3000) {
                        chunkManager.update(0, 0, 2);
                        if (chunkRenderer.getMeshes().size() >= 9) {
                                break;
                        }
                        try {
                                Thread.sleep(20);
                        } catch (InterruptedException ignored) {
                        }
                        initialWaitMs += 20;
                }
                outlinePipeline = new VulkanLinePipeline();
                outlinePipeline.create(device, renderPass.getRenderPass());

                crosshairPipeline = new VulkanCrosshairPipeline();
                crosshairPipeline.create(device, renderPass.getRenderPass());

                byte[] outlineData = OutlineBox.toBytes();

                outlineVertexBuffer = new VulkanBuffer();
                outlineVertexBuffer.create(
                                context.getPhysicalDevice(),
                                device,
                                outlineData.length,
                                VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

                outlineVertexBuffer.upload(device, outlineData);

                float[] crosshairVerts = {
                                -0.02f, 0.0f,
                                0.02f, 0.0f,
                                0.0f, -0.02f,
                                0.0f, 0.02f
                };

                java.nio.ByteBuffer crosshairBuf = java.nio.ByteBuffer.allocate(crosshairVerts.length * Float.BYTES)
                                .order(java.nio.ByteOrder.nativeOrder());
                for (float v : crosshairVerts)
                        crosshairBuf.putFloat(v);

                byte[] crosshairData = crosshairBuf.array();

                crosshairVertexBuffer = new VulkanBuffer();
                crosshairVertexBuffer.create(
                                context.getPhysicalDevice(),
                                device,
                                crosshairData.length,
                                VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

                crosshairVertexBuffer.upload(device, crosshairData);
                System.out.println(
                                TextureLoader.load("/textures/terrain.png"));
        }

        public void render(Camera camera) {
                VkDevice device = context.getDevice();

                try (MemoryStack stack = stackPush()) {
                        LongBuffer fence = stack.longs(syncObjects.getInFlightFence());
                        vkWaitForFences(device, fence, true, Long.MAX_VALUE);
                        vkResetFences(device, fence);

                        chunkRenderer.flushPendingDestroy(device);

                        int playerChunkX = Math.floorDiv((int) Math.floor(camera.getX()),
                                        com.joshuastar.world.Chunk.SIZE_X);
                        int playerChunkZ = Math.floorDiv((int) Math.floor(camera.getZ()),
                                        com.joshuastar.world.Chunk.SIZE_Z);
                       chunkManager.update(playerChunkX, playerChunkZ, 5);
                        mobManager.update(1.0f / 60.0f);

                        IntBuffer pImageIndex = stack.ints(0);
                        vkAcquireNextImageKHR(device, swapChain.getSwapChain(), Long.MAX_VALUE,
                                        syncObjects.getImageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
                        int imageIndex = pImageIndex.get(0);

                        VkCommandBuffer commandBuffer = commandBuffers.getCommandBuffer();
                        vkResetCommandBuffer(commandBuffer, 0);

                        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

                        if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                                throw new RuntimeException("Failed to begin recording command buffer!");
                        }

                        updateShadowCascades(camera.getX(), camera.getY(), camera.getZ(),
                                        lightDirection.x, lightDirection.y, lightDirection.z);

                        VkExtent2D shadowExtent = VkExtent2D.calloc(stack)
                                        .set(VulkanShadowMap.RESOLUTION, VulkanShadowMap.RESOLUTION);

                        for (int cascade = 0; cascade < UniformBufferObject.NUM_CASCADES; cascade++) {

                                VkRenderPassBeginInfo shadowPassInfo = VkRenderPassBeginInfo.calloc(stack)
                                                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                                                .renderPass(shadowRenderPass.getRenderPass())
                                                .framebuffer(shadowFramebuffers.getFramebuffer(cascade));

                                shadowPassInfo.renderArea().offset().set(0, 0);
                                shadowPassInfo.renderArea().extent(shadowExtent);

                                VkClearValue.Buffer shadowClear = VkClearValue.calloc(1, stack);
                                shadowClear.get(0).depthStencil().depth(1.0f).stencil(0);
                                shadowPassInfo.pClearValues(shadowClear);

                                vkCmdBeginRenderPass(commandBuffer, shadowPassInfo, VK_SUBPASS_CONTENTS_INLINE);

                                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                                shadowPipeline.getPipeline());

                                VkViewport.Buffer shadowViewport = VkViewport.calloc(1, stack);
                                shadowViewport.get(0).x(0.0f).y(0.0f)
                                                .width(VulkanShadowMap.RESOLUTION)
                                                .height(VulkanShadowMap.RESOLUTION)
                                                .minDepth(0.0f).maxDepth(1.0f);
                                vkCmdSetViewport(commandBuffer, 0, shadowViewport);

                                VkRect2D.Buffer shadowScissor = VkRect2D.calloc(1, stack);
                                shadowScissor.offset().set(0, 0);
                                shadowScissor.extent(shadowExtent);
                                vkCmdSetScissor(commandBuffer, 0, shadowScissor);

                                vkCmdSetDepthBias(commandBuffer, 1.25f, 0.0f, 1.75f);

                                java.nio.ByteBuffer lightMatrixPc = MemoryUtil.memAlloc(64);
                                ubo.lightSpaceMatrices[cascade].get(0, lightMatrixPc);
                                lightMatrixPc.rewind();

                                vkCmdPushConstants(commandBuffer, shadowPipeline.getPipelineLayout(),
                                                VK_SHADER_STAGE_VERTEX_BIT, 0, lightMatrixPc);
                               float cascadeRadius = Math.min(
        ubo.cascadeSplits.get(cascade) + 16.0f,
        96.0f);

                                for (com.joshuastar.renderer.vulkan.VulkanMesh shadowMesh : chunkRenderer
                                                .getMeshesInRadius(camera.getX(), camera.getZ(), cascadeRadius)) {

                                        if (shadowMesh == null || shadowMesh.getMesh().getIndexCount() == 0) {
                                                continue;
                                        }

                                        LongBuffer shadowVertexBuffers = stack
                                                        .longs(shadowMesh.getVertexBuffer().getBuffer());
                                        LongBuffer shadowOffsets = stack.longs(0);
                                        vkCmdBindVertexBuffers(commandBuffer, 0, shadowVertexBuffers, shadowOffsets);

                                        vkCmdBindIndexBuffer(commandBuffer, shadowMesh.getIndexBuffer().getBuffer(),
                                                        0, VK_INDEX_TYPE_UINT32);

                                        vkCmdDrawIndexed(commandBuffer, shadowMesh.getMesh().getIndexCount(), 1, 0, 0,
                                                        0);
                                }

                                for (com.joshuastar.entity.Mob mob : mobManager.getMobs()) {

                                        Matrix4f mobModel = new Matrix4f()
                                                        .translate(mob.getX(), mob.getY(), mob.getZ())
                                                        .rotateY((float) Math.toRadians(mob.getYaw()));

                                        java.nio.ByteBuffer mobShadowPc = MemoryUtil.memAlloc(64);
                                        new Matrix4f(ubo.lightSpaceMatrices[cascade]).mul(mobModel).get(0, mobShadowPc);
                                        mobShadowPc.rewind();

                                        vkCmdPushConstants(commandBuffer, shadowPipeline.getPipelineLayout(),
                                                        VK_SHADER_STAGE_VERTEX_BIT, 0, mobShadowPc);

                                        LongBuffer mobShadowVb = stack.longs(cowMesh.getVertexBuffer().getBuffer());
                                        LongBuffer mobShadowOff = stack.longs(0);
                                        vkCmdBindVertexBuffers(commandBuffer, 0, mobShadowVb, mobShadowOff);

                                        vkCmdBindIndexBuffer(commandBuffer, cowMesh.getIndexBuffer().getBuffer(),
                                                        0, VK_INDEX_TYPE_UINT32);

                                        vkCmdDrawIndexed(commandBuffer, cowMesh.getMesh().getIndexCount(), 1, 0, 0, 0);

                                        MemoryUtil.memFree(mobShadowPc);
                                }

                                MemoryUtil.memFree(lightMatrixPc);

                                vkCmdEndRenderPass(commandBuffer);
                        }

                        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                                        .renderPass(renderPass.getRenderPass())
                                        .framebuffer(framebuffers.getFramebuffers()[imageIndex]);

                        renderPassInfo.renderArea().offset().set(0, 0);
                        renderPassInfo.renderArea().extent(swapChain.getExtent());

                        VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
                        clearValues.color()
                                        .float32(0, 0.5f) // R
                                        .float32(1, 0.7f) // G
                                        .float32(2, 1.0f) // B
                                        .float32(3, 1.0f); // A
                        clearValues.get(1)
                                        .depthStencil()
                                        .depth(1.0f)
                                        .stencil(0);
                        renderPassInfo.pClearValues(clearValues);
                        ubo.model.identity();

                        float yaw = (float) Math.toRadians(camera.getYaw());
                        float pitch = (float) Math.toRadians(camera.getPitch());

                        float dirX = (float) (Math.cos(pitch) * Math.sin(yaw));
                        float dirY = (float) Math.sin(pitch);
                        float dirZ = (float) (-Math.cos(pitch) * Math.cos(yaw));

                        ubo.view.identity().lookAt(
                                        camera.getX(), camera.getY(), camera.getZ(),
                                        camera.getX() + dirX, camera.getY() + dirY, camera.getZ() + dirZ,
                                        0.0f, 1.0f, 0.0f);

                        ubo.projection.identity().perspective(
                                        (float) Math.toRadians(70.0f),
                                        (float) swapChain.getExtent().width() / swapChain.getExtent().height(),
                                        0.1f, 500.0f);
                        ubo.projection.m11(-ubo.projection.m11());

                        uniformBuffer.update(device, ubo);

                        vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                    float maxDistance = 8 * com.joshuastar.world.Chunk.SIZE_X;
float maxDistanceSq = maxDistance * maxDistance;

for (java.util.Map.Entry<Long, com.joshuastar.renderer.vulkan.VulkanMesh> entry : chunkRenderer.getMeshes().entrySet()) {

    long key = entry.getKey();

    int chunkX = (int)(key >> 32);
    int chunkZ = (int)key;

    float cx = chunkX * com.joshuastar.world.Chunk.SIZE_X + 8.0f;
    float cz = chunkZ * com.joshuastar.world.Chunk.SIZE_Z + 8.0f;

    float dx = cx - camera.getX();
    float dz = cz - camera.getZ();

    if (dx * dx + dz * dz > maxDistanceSq) {
        continue;
    }

    VulkanMesh mesh = entry.getValue();
                                if (mesh == null || mesh.getMesh().getIndexCount() == 0)
                                        continue;

                                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                                graphicsPipeline.getPipeline());

                                VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
                                viewport.get(0).x(0.0f).y(0.0f)
                                                .width(swapChain.getExtent().width())
                                                .height(swapChain.getExtent().height())
                                                .minDepth(0.0f).maxDepth(1.0f);
                                vkCmdSetViewport(commandBuffer, 0, viewport);

                                VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
                                scissor.offset().set(0, 0);
                                scissor.extent(swapChain.getExtent());
                                vkCmdSetScissor(commandBuffer, 0, scissor);

                                LongBuffer vertexBuffers = stack.longs(mesh.getVertexBuffer().getBuffer());
                                LongBuffer offsets = stack.longs(0);
                                vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);

                                LongBuffer sets = stack.longs(descriptorSets.getDescriptorSet());
                                vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                                graphicsPipeline.getPipelineLayout(), 0, sets, null);

                                vkCmdBindIndexBuffer(commandBuffer, mesh.getIndexBuffer().getBuffer(), 0,
                                                VK_INDEX_TYPE_UINT32);

                                vkCmdDrawIndexed(commandBuffer, mesh.getMesh().getIndexCount(), 1, 0, 0, 0);
                        }

                        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        mobPipeline.getPipeline());

                        for (com.joshuastar.entity.Mob mob : mobManager.getMobs()) {

                                Matrix4f mobModel = new Matrix4f()
                                                .translate(mob.getX(), mob.getY(), mob.getZ())
                                                .rotateY((float) Math.toRadians(mob.getYaw()));

                                java.nio.ByteBuffer mobPc = MemoryUtil.memAlloc(64);
                                mobModel.get(0, mobPc);
                                mobPc.rewind();

                                vkCmdPushConstants(commandBuffer, mobPipeline.getPipelineLayout(),
                                                VK_SHADER_STAGE_VERTEX_BIT, 0, mobPc);

                                LongBuffer mobVb = stack.longs(cowMesh.getVertexBuffer().getBuffer());
                                LongBuffer mobOff = stack.longs(0);
                                vkCmdBindVertexBuffers(commandBuffer, 0, mobVb, mobOff);

                                LongBuffer mobSets = stack.longs(descriptorSets.getMobDescriptorSet());
                                vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                                mobPipeline.getPipelineLayout(), 0, mobSets, null);

                                vkCmdBindIndexBuffer(commandBuffer, cowMesh.getIndexBuffer().getBuffer(), 0,
                                                VK_INDEX_TYPE_UINT32);

                                vkCmdDrawIndexed(commandBuffer, cowMesh.getMesh().getIndexCount(), 1, 0, 0, 0);

                                MemoryUtil.memFree(mobPc);
                        }

                        RayCaster.Hit hit = RayCaster.cast(
                                        getWorld(), camera.getX(), camera.getY(), camera.getZ(),
                                        camera.getYaw(), camera.getPitch(), 8.0f);

                        if (hit != null) {
                                Matrix4f model = new Matrix4f().translate(hit.x, hit.y, hit.z);
                                Matrix4f mvp = new Matrix4f(ubo.projection).mul(ubo.view).mul(model);

                                java.nio.ByteBuffer pc = MemoryUtil.memAlloc(80);
                                mvp.get(0, pc);
                                pc.putFloat(64, 0.0f);
                                pc.putFloat(68, 0.0f);
                                pc.putFloat(72, 0.0f);
                                pc.putFloat(76, 1.0f);
                                pc.rewind();

                                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                                outlinePipeline.getPipeline());
                                vkCmdPushConstants(commandBuffer, outlinePipeline.getPipelineLayout(),
                                                VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, pc);

                                LongBuffer outlineBuffers = stack.longs(outlineVertexBuffer.getBuffer());
                                float[] crosshairVerts = {
                                                -0.02f, 0.0f,
                                                0.02f, 0.0f,
                                                0.0f, -0.02f,
                                                0.0f, 0.02f
                                };
                                LongBuffer outlineOffsets = stack.longs(0);
                                vkCmdBindVertexBuffers(commandBuffer, 0, outlineBuffers, outlineOffsets);

                                vkCmdDraw(commandBuffer, 24, 1, 0, 0);

                                MemoryUtil.memFree(pc);
                        }

                        java.nio.ByteBuffer crosshairColor = MemoryUtil.memAlloc(16);
                        crosshairColor.putFloat(0, 0.0f);
                        crosshairColor.putFloat(4, 0.0f);
                        crosshairColor.putFloat(8, 0.0f);
                        crosshairColor.putFloat(12, 1.0f);
                        crosshairColor.rewind();

                        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                        crosshairPipeline.getPipeline());
                        vkCmdPushConstants(commandBuffer, crosshairPipeline.getPipelineLayout(),
                                        VK_SHADER_STAGE_FRAGMENT_BIT, 0, crosshairColor);

                        LongBuffer crosshairBuffers = stack.longs(crosshairVertexBuffer.getBuffer());
                        LongBuffer crosshairOffsets = stack.longs(0);
                        vkCmdBindVertexBuffers(commandBuffer, 0, crosshairBuffers, crosshairOffsets);

                        vkCmdDraw(commandBuffer, 4, 1, 0, 0);

                        MemoryUtil.memFree(crosshairColor);
                        vkCmdEndRenderPass(commandBuffer);

                        if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                                throw new RuntimeException("Failed to record command buffer!");
                        }

                        // Swapped to VkSubmitInfo.Buffer to ensure correct memory layout for LWJGL
                        VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack)
                                        .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                                        .waitSemaphoreCount(1)
                                        .pWaitSemaphores(stack.longs(syncObjects.getImageAvailableSemaphore()))
                                        .pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                                        .pCommandBuffers(stack.pointers(commandBuffer))
                                        .pSignalSemaphores(stack.longs(syncObjects.getRenderFinishedSemaphore()));

                        int submitResult = vkQueueSubmit(context.getGraphicsQueue(), submitInfo,
                                        syncObjects.getInFlightFence());
                        if (submitResult != VK_SUCCESS) {
                                throw new RuntimeException("Failed to submit draw command buffer! Vulkan Error Code: "
                                                + submitResult);
                        }

                        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack)
                                        .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                                        .pWaitSemaphores(stack.longs(syncObjects.getRenderFinishedSemaphore()))
                                        .swapchainCount(1)
                                        .pSwapchains(stack.longs(swapChain.getSwapChain()))
                                        .pImageIndices(pImageIndex);

                        vkQueuePresentKHR(context.getPresentQueue(), presentInfo);
                }
        }

        private void updateShadowCascades(
                        float camX, float camY, float camZ,
                        float dirX, float dirY, float dirZ) {

                float nearClip = 0.1f;
                float farClip = 500.0f;
                float lambda = 0.6f;
                float fovY = (float) Math.toRadians(70.0f);
                float aspect = (float) swapChain.getExtent().width() / swapChain.getExtent().height();

                int cascadeCount = UniformBufferObject.NUM_CASCADES;
                float[] splitDistances = new float[cascadeCount];

                for (int i = 0; i < cascadeCount; i++) {
                        float p = (i + 1) / (float) cascadeCount;
                        float logSplit = nearClip * (float) Math.pow(farClip / nearClip, p);
                        float uniformSplit = nearClip + (farClip - nearClip) * p;
                        splitDistances[i] = lambda * logSplit + (1 - lambda) * uniformSplit;
                }

                ubo.cascadeSplits.set(splitDistances[0], splitDistances[1], splitDistances[2], 0.0f);

                org.joml.Vector3f forward = new org.joml.Vector3f(dirX, dirY, dirZ).normalize();
                org.joml.Vector3f worldUp = new org.joml.Vector3f(0.0f, 1.0f, 0.0f);
                org.joml.Vector3f right = new org.joml.Vector3f();
                forward.cross(worldUp, right);

                if (right.lengthSquared() < 0.0001f) {
                        right.set(1.0f, 0.0f, 0.0f);
                } else {
                        right.normalize();
                }

                org.joml.Vector3f up = new org.joml.Vector3f();
                right.cross(forward, up);
                up.normalize();

                float tanHalfFovY = (float) Math.tan(fovY * 0.5f);
                float prevSplit = nearClip;

                for (int i = 0; i < cascadeCount; i++) {

                        float splitNear = prevSplit;
                        float splitFar = splitDistances[i];

                        float nearHeight = tanHalfFovY * splitNear;
                        float nearWidth = nearHeight * aspect;
                        float farHeight = tanHalfFovY * splitFar;
                        float farWidth = farHeight * aspect;

                        org.joml.Vector3f camPos = new org.joml.Vector3f(camX, camY, camZ);
                        org.joml.Vector3f nearCenter = new org.joml.Vector3f(forward).mul(splitNear).add(camPos);
                        org.joml.Vector3f farCenter = new org.joml.Vector3f(forward).mul(splitFar).add(camPos);

                        org.joml.Vector3f[] corners = new org.joml.Vector3f[8];
                        corners[0] = new org.joml.Vector3f(nearCenter).add(new org.joml.Vector3f(up).mul(nearHeight))
                                        .add(new org.joml.Vector3f(right).mul(nearWidth));
                        corners[1] = new org.joml.Vector3f(nearCenter).add(new org.joml.Vector3f(up).mul(nearHeight))
                                        .sub(new org.joml.Vector3f(right).mul(nearWidth));
                        corners[2] = new org.joml.Vector3f(nearCenter).sub(new org.joml.Vector3f(up).mul(nearHeight))
                                        .add(new org.joml.Vector3f(right).mul(nearWidth));
                        corners[3] = new org.joml.Vector3f(nearCenter).sub(new org.joml.Vector3f(up).mul(nearHeight))
                                        .sub(new org.joml.Vector3f(right).mul(nearWidth));
                        corners[4] = new org.joml.Vector3f(farCenter).add(new org.joml.Vector3f(up).mul(farHeight))
                                        .add(new org.joml.Vector3f(right).mul(farWidth));
                        corners[5] = new org.joml.Vector3f(farCenter).add(new org.joml.Vector3f(up).mul(farHeight))
                                        .sub(new org.joml.Vector3f(right).mul(farWidth));
                        corners[6] = new org.joml.Vector3f(farCenter).sub(new org.joml.Vector3f(up).mul(farHeight))
                                        .add(new org.joml.Vector3f(right).mul(farWidth));
                        corners[7] = new org.joml.Vector3f(farCenter).sub(new org.joml.Vector3f(up).mul(farHeight))
                                        .sub(new org.joml.Vector3f(right).mul(farWidth));

                        org.joml.Vector3f centroid = new org.joml.Vector3f();
                        for (org.joml.Vector3f corner : corners) {
                                centroid.add(corner);
                        }
                        centroid.mul(1.0f / 8.0f);

                        float radius = 0.0f;
                        for (org.joml.Vector3f corner : corners) {
                                float dist = corner.distance(centroid);
                                if (dist > radius) {
                                        radius = dist;
                                }
                        }

                        radius = Math.max(radius, 1.0f);

                        float texelSize = (radius * 2.0f) / VulkanShadowMap.RESOLUTION;
                        centroid.x = Math.round(centroid.x / texelSize) * texelSize;
                        centroid.y = Math.round(centroid.y / texelSize) * texelSize;
                        centroid.z = Math.round(centroid.z / texelSize) * texelSize;

                        org.joml.Vector3f lightUp = Math.abs(lightDirection.y) > 0.99f
                                        ? new org.joml.Vector3f(0.0f, 0.0f, 1.0f)
                                        : new org.joml.Vector3f(0.0f, 1.0f, 0.0f);

                        org.joml.Vector3f eye = new org.joml.Vector3f(centroid)
                                        .sub(new org.joml.Vector3f(lightDirection).mul(radius * 2.0f));

                        Matrix4f lightView = new Matrix4f().lookAt(
                                        eye.x, eye.y, eye.z,
                                        centroid.x, centroid.y, centroid.z,
                                        lightUp.x, lightUp.y, lightUp.z);

                        Matrix4f lightProj = new Matrix4f().ortho(
                                        -radius, radius,
                                        -radius, radius,
                                        0.01f, radius * 4.0f);

                        ubo.lightSpaceMatrices[i].set(lightProj).mul(lightView);
                        prevSplit = splitFar;
                }
        }

        public com.joshuastar.world.World getWorld() {
                return chunkRenderer.getWorld();
        }

        public void rebuildChunkAt(int chunkX, int chunkZ) {
                chunkRenderer.rebuildChunk(
                                context.getPhysicalDevice(),
                                context.getDevice(),
                                chunkX,
                                chunkZ,
                                context.getAllocator());
        }

        public void cleanup() {
                if (chunkManager != null) {
                        chunkManager.shutdown();
                }
                if (context != null) {
                        VkDevice device = context.getDevice();
                        vkDeviceWaitIdle(device);
                        if (syncObjects != null)
                                syncObjects.destroy(device);
                        if (commandBuffers != null)
                                commandBuffers.destroy(device);
                        if (framebuffers != null)
                                framebuffers.destroy(device);
                        if (graphicsPipeline != null) {
                                graphicsPipeline.destroy(device);
                        }
                        if (descriptorSetLayout != null) {
                                descriptorSetLayout.destroy(device);
                        }
                        if (depthImage != null)
                                depthImage.destroy(device);
                        if (shadowPipeline != null)
                                shadowPipeline.destroy(device);
                        if (shadowFramebuffers != null)
                                shadowFramebuffers.destroy(device);
                        if (shadowRenderPass != null)
                                shadowRenderPass.destroy(device);
                        if (shadowMap != null)
                                shadowMap.destroy(device);
                        if (mobPipeline != null)
                                mobPipeline.destroy(device);
                        if (cowMesh != null)
                                cowMesh.destroy(device);
                        if (cowTexture != null)
                                cowTexture.destroy(device);
                        if (outlinePipeline != null)
                                outlinePipeline.destroy(device);
                        if (crosshairPipeline != null)
                                crosshairPipeline.destroy(device);
                        if (outlineVertexBuffer != null)
                                outlineVertexBuffer.destroy(device);
                        if (crosshairVertexBuffer != null)
                                crosshairVertexBuffer.destroy(device);
                        if (renderPass != null)
                                renderPass.destroy(device);
                        if (imageViews != null)
                                imageViews.destroy(device);
                        if (swapChain != null)
                                swapChain.destroy(device);

                        context.destroy();
                        if (descriptorPool != null)
                                descriptorPool.destroy(device);

                        if (uniformBuffer != null)
                                uniformBuffer.destroy(device);
                }
        }

        public VulkanContext getContext() {
                return context;
        }
}