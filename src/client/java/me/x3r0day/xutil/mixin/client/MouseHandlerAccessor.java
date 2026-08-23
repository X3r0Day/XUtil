package me.x3r0day.xutil.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

    @Accessor("scrollWheelHandler")
    ScrollWheelHandler xutil$getScrollWheelHandler();
}
