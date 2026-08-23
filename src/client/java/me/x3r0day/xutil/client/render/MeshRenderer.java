package me.x3r0day.xutil.client.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4fc;

import java.util.Optional;
import java.util.OptionalDouble;

public final class MeshRenderer {

    private MeshRenderer() {
    }

    public static void render(Matrix4fc projection, Matrix4fc modelView, MeshBuilder mesh) {
        if (mesh.isBuilding()) {
            mesh.end();
        }

        int indexCount = mesh.getIndicesCount();
        if (indexCount <= 0) return;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuBufferSlice vertexBuffer = mesh.uploadVertexBuffer(encoder);
        GpuBufferSlice indexBuffer = mesh.uploadIndexBuffer(encoder);
        int firstIndex = Math.toIntExact(indexBuffer.offset() / mesh.getIndexType().bytes);
        GpuBufferSlice meshData = MeshUniforms.write(projection, modelView);

        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        try (RenderPass pass = XutilRenderPipelines.OVERLAY.wantsDepthTexture()
                ? encoder.createRenderPass(() -> "xutil_overlay", target.getColorTextureView(),
                    Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())
                : encoder.createRenderPass(() -> "xutil_overlay", target.getColorTextureView(),
                    Optional.empty())) {
            pass.setPipeline(XutilRenderPipelines.OVERLAY);
            pass.setUniform("MeshData", meshData);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indexBuffer.buffer(), mesh.getIndexType());
            pass.drawIndexed(indexCount, 1, firstIndex, 0, 0);
        }
    }
}
