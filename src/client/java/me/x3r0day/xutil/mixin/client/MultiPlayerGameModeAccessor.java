package me.x3r0day.xutil.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("destroyBlockPos")
    BlockPos xutil$getDestroyBlockPos();

    @Accessor("destroyProgress")
    float xutil$getDestroyProgress();
}
