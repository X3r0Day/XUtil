package me.x3r0day.xutil.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.x3r0day.xutil.client.module.impl.render.FullBright;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class LightmapMixin implements LightmapAccessor {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void xutil$fullbright(LightmapRenderState renderState, CallbackInfo ci) {
        if (!FullBright.isActive()) return;

        RenderSystem.getDevice().createCommandEncoder()
            .clearColorTexture(xutil$getTexture(), new Vector4f(1f));
        ci.cancel();
    }
}
