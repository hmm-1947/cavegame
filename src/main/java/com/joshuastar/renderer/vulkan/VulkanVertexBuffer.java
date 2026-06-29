package com.joshuastar.renderer.vulkan;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import com.joshuastar.renderer.Mesh;

public class VulkanVertexBuffer extends VulkanBuffer {

        private Mesh mesh;

        public void upload(
                        org.lwjgl.vulkan.VkPhysicalDevice physicalDevice,
                        org.lwjgl.vulkan.VkDevice device,
                        VulkanMemoryAllocator allocator,
                        Mesh mesh) {
                this.mesh = mesh;

                long size = (long) mesh.getVertexCount()
                                * com.joshuastar.renderer.Vertex.BYTES;

                create(
                                physicalDevice,
                                device,
                                size,
                                org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
                                                | org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

            FloatBuffer vertexData = memAllocFloat(mesh.getVertexCount() * 9);

                for (com.joshuastar.renderer.Vertex v : mesh.getVertices()) {

                        vertexData.put(v.getX());
                        vertexData.put(v.getY());
                        vertexData.put(v.getZ());

                        vertexData.put(v.getU());
                        vertexData.put(v.getV());

                        vertexData.put(v.getNx());
                        vertexData.put(v.getNy());
                        vertexData.put(v.getNz());

                        vertexData.put(v.getShade());
                }
                vertexData.flip();

                PointerBuffer mapped = BufferUtils.createPointerBuffer(1);

                vkMapMemory(
                                device,
                                getMemory(),
                                0,
                                size,
                                0,
                                mapped);

                org.lwjgl.system.MemoryUtil.memCopy(
                                org.lwjgl.system.MemoryUtil.memAddress(vertexData),
                                mapped.get(0),
                                size);

                vkUnmapMemory(device, getMemory());

                memFree(vertexData);
        }

        public Mesh getMesh() {
                return mesh;
        }
}