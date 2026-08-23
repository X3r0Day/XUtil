package me.x3r0day.xutil.client.account;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * MSA refresh token + profile, so a login survives restarts.
 */
public final class AccountStore {

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xutil-msa.json");

    private AccountStore() {
    }

    public record StoredAccount(String name, UUID uuid, String xuid, String refreshToken) {
    }

    public static Optional<StoredAccount> load() {
        if (!Files.exists(FILE)) {
            return Optional.empty();
        }
        try {
            JsonObject json = GsonHelper.parse(Files.readString(FILE, StandardCharsets.UTF_8));
            String name = GsonHelper.getAsString(json, "name", null);
            String rawUuid = GsonHelper.getAsString(json, "uuid", null);
            String xuid = GsonHelper.getAsString(json, "xuid", null);
            String refreshToken = GsonHelper.getAsString(json, "refreshToken", null);
            if (name == null || rawUuid == null || refreshToken == null) {
                return Optional.empty();
            }
            return Optional.of(new StoredAccount(name, UUID.fromString(rawUuid), xuid, refreshToken));
        } catch (RuntimeException | IOException exception) {
            return Optional.empty();
        }
    }

    public static void save(StoredAccount account) {
        JsonObject json = new JsonObject();
        json.addProperty("name", account.name());
        json.addProperty("uuid", account.uuid().toString());
        json.addProperty("xuid", account.xuid());
        json.addProperty("refreshToken", account.refreshToken());
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ignored) {
        }
    }
}
