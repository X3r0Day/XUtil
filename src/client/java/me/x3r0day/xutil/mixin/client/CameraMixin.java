package me.x3r0day.xutil.mixin.client;

import me.x3r0day.xutil.client.module.impl.render.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void xutil$zoomFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        Zoom.frame(Minecraft.getInstance());
        float multiplier = (float) Zoom.getFovMultiplier();
        if (multiplier != 1f) {
            cir.setReturnValue(cir.getReturnValueF() * multiplier);
        }
    }
}
