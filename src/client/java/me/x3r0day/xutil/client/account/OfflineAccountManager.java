package me.x3r0day.xutil.client.account;

import me.x3r0day.xutil.mixin.client.MinecraftUserAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class OfflineAccountManager {

    private OfflineAccountManager() {
    }

    public static Account switchTo(String username) {
        if (!username.matches("[A-Za-z0-9_]{3,16}")) {
            throw new IllegalArgumentException("Username must be 3-16 letters, numbers, or underscores");
        }

        UUID uuid = UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)
        );
        User user = new User(username, uuid, "0", Optional.empty(), Optional.empty());

        MinecraftUserAccessor accessor = (MinecraftUserAccessor) Minecraft.getInstance();
        accessor.xutil$setUser(user);
        accessor.xutil$setProfileFuture(CompletableFuture.completedFuture(null));
        return new Account(username, uuid);
    }

    public record Account(String username, UUID uuid) {
    }
}
