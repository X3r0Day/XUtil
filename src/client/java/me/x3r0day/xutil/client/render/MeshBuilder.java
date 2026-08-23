package me.x3r0day.xutil.client.render;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class MeshBuilder {

    private final IndexType indexType = IndexType.INT;
    private final int primitiveVerticesSize;

    private ByteBuffer vertices;
    private long verticesPointerStart;
    private long verticesPointer;

    private ByteBuffer indices;
    private long indicesPointer;
    private int indicesCount;

    private int vertexI;
    private boolean building;

    private double cameraX;
    private double cameraY;
    private double cameraZ;

    public MeshBuilder(VertexFormat format) {
        this.primitiveVerticesSize = format.getVertexSize();
    }

    public void begin() {
        if (building) throw new IllegalStateException("Mesh.begin() called while already building.");

        verticesPointer = verticesPointerStart;
        vertexI = 0;
        indicesCount = 0;
        building = true;

        Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        cameraX = camera.x;
        cameraY = camera.y;
        cameraZ = camera.z;
    }

    public MeshBuilder vec3(double x, double y, double z) {
        long p = verticesPointer;
        MemoryUtil.memPutFloat(p, (float) (x - cameraX));
        MemoryUtil.memPutFloat(p + 4, (float) (y - cameraY));
        MemoryUtil.memPutFloat(p + 8, (float) (z - cameraZ));
        verticesPointer += 12;
        return this;
    }

    public MeshBuilder color(int r, int g, int b, int a) {
        long p = verticesPointer;
        MemoryUtil.memPutByte(p, (byte) r);
        MemoryUtil.memPutByte(p + 1, (byte) g);
        MemoryUtil.memPutByte(p + 2, (byte) b);
        MemoryUtil.memPutByte(p + 3, (byte) a);
        verticesPointer += 4;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = indicesPointer + indicesCount * (long) indexType.bytes;
        MemoryUtil.memPutInt(p, i1);
        MemoryUtil.memPutInt(p + indexType.bytes, i2);
        MemoryUtil.memPutInt(p + indexType.bytes * 2L, i3);
        MemoryUtil.memPutInt(p + indexType.bytes * 3L, i3);
        MemoryUtil.memPutInt(p + indexType.bytes * 4L, i4);
        MemoryUtil.memPutInt(p + indexType.bytes * 5L, i1);
        indicesCount += 6;
    }

    public void ensureQuadCapacity() {
        ensureCapacity(4, 6);
    }

    private void ensureCapacity(int vertexCount, int indexCount) {
        if (vertices == null || indices == null) {
            allocateBuffers(1024, 2048);
            return;
        }

        if ((vertexI + vertexCount) * primitiveVerticesSize >= vertices.capacity()) {
            int offset = getVerticesOffset();
            int newSize = Math.max(vertices.capacity() * 2,
                vertices.capacity() + vertexCount * primitiveVerticesSize);
            ByteBuffer newVertices = BufferUtils.createByteBuffer(newSize);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(vertices), MemoryUtil.memAddress0(newVertices), offset);
            vertices = newVertices;
            verticesPointerStart = MemoryUtil.memAddress0(vertices);
            verticesPointer = verticesPointerStart + offset;
        }

        if ((indicesCount + indexCount) * indexType.bytes >= indices.capacity()) {
            int newSize = Math.max(indices.capacity() * 2,
                indices.capacity() + indexCount * indexType.bytes);
            ByteBuffer newIndices = BufferUtils.createByteBuffer(newSize);
            MemoryUtil.memCopy(MemoryUtil.memAddress0(indices), MemoryUtil.memAddress0(newIndices),
                indicesCount * (long) indexType.bytes);
            indices = newIndices;
            indicesPointer = MemoryUtil.memAddress0(indices);
        }
    }

    private void allocateBuffers(int vertexCount, int indexCount) {
        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * vertexCount);
        verticesPointer = verticesPointerStart = MemoryUtil.memAddress0(vertices);

        indices = BufferUtils.createByteBuffer(indexCount * indexType.bytes);
        indicesPointer = MemoryUtil.memAddress0(indices);
    }

    public void end() {
        if (!building) throw new IllegalStateException("Mesh.end() called while not building.");
        building = false;
    }

    public boolean isBuilding() {
        return building;
    }

    public GpuBufferSlice uploadVertexBuffer(CommandEncoder encoder) {
        vertices.limit(getVerticesOffset());
        return encoder.transientMemory().uploadGpu(vertices, primitiveVerticesSize, GpuBuffer.USAGE_VERTEX);
    }

    public GpuBufferSlice uploadIndexBuffer(CommandEncoder encoder) {
        indices.limit(indicesCount * indexType.bytes);
        return encoder.transientMemory().uploadGpu(indices, indexType.bytes, GpuBuffer.USAGE_INDEX);
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    private int getVerticesOffset() {
        return (int) (verticesPointer - verticesPointerStart);
    }
}
