package me.x3r0day.xutil.mixin.client;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftUserAccessor {

    @Accessor("user")
    @Mutable
    @Final
    void xutil$setUser(User user);

    @Accessor("profileFuture")
    @Mutable
    @Final
    void xutil$setProfileFuture(CompletableFuture<ProfileResult> profileFuture);
}
