package me.x3r0day.xutil.mixin.client;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Lightmap.class)
public interface LightmapAccessor {

    @Accessor("texture")
    GpuTexture xutil$getTexture();
}
