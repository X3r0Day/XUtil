package me.x3r0day.xutil.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.nio.ByteBuffer;

public final class MeshUniforms {

    public static final int SIZE = new Std140SizeCalculator()
        .putMat4f()
        .putMat4f()
        .get();

    private static final Data DATA = new Data();

    private static final DynamicUniformStorage<Data> STORAGE =
        new DynamicUniformStorage<>("xutil mesh ubo", SIZE, 16);

    private MeshUniforms() {
    }

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    public static GpuBufferSlice write(Matrix4fc projection, Matrix4fc modelView) {
        DATA.projection = new Matrix4f(projection);
        DATA.modelView = new Matrix4f(modelView);
        return STORAGE.writeUniform(DATA);
    }

    private static final class Data implements DynamicUniformStorage.DynamicUniform {

        private Matrix4f projection;
        private Matrix4f modelView;

        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                .putMat4f(projection)
                .putMat4f(modelView);
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}
