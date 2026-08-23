package me.x3r0day.xutil.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.x3r0day.xutil.client.module.impl.render.BreakIndicator;
import me.x3r0day.xutil.client.render.MeshUniforms;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING",
        target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
        args = "ldc=hand"))
    private void xutil$renderOverlays(DeltaTracker deltaTracker, CallbackInfo ci,
            @Local(name = "projectionMatrix") Matrix4f projectionMatrix,
            @Local(name = "modelViewMatrix") Matrix4fc modelViewMatrix,
            @Local(name = "worldPartialTicks") float worldPartialTicks) {
        BreakIndicator.render3D(projectionMatrix, modelViewMatrix, worldPartialTicks);
        MeshUniforms.flipFrame();
    }
}
