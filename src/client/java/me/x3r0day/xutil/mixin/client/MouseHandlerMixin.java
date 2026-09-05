package me.x3r0day.xutil.mixin.client;

import me.x3r0day.xutil.client.module.impl.render.Zoom;
import me.x3r0day.xutil.client.repeater.ReplayEngine;
import net.minecraft.client.MouseHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin implements MouseHandlerAccessor {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void xutil$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!Zoom.isZooming()) return;

        Vector2i ticks = xutil$getScrollWheelHandler().onMouseScroll(horizontal, vertical);
        if (Zoom.onScrollTicks(ticks.y)) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void xutil$lockLook(double delta, CallbackInfo ci) {
        if (ReplayEngine.isMouseLocked()) {
            ci.cancel();
        }
    }
}
