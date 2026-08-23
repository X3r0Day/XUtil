package me.x3r0day.xutil.client.account;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.x3r0day.xutil.mixin.client.MinecraftUserAccessor;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.java.JavaAuthManager;
import net.raphimc.minecraftauth.java.model.MinecraftProfile;
import net.raphimc.minecraftauth.java.model.MinecraftToken;
import net.raphimc.minecraftauth.msa.data.MsaConstants;
import net.raphimc.minecraftauth.msa.model.MsaApplicationConfig;
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode;
import net.raphimc.minecraftauth.msa.model.MsaToken;
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService;
import net.raphimc.minecraftauth.msa.service.util.ParamMsaAuthServiceSupplier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * MSA login via MinecraftAuth (same stack Meteor uses). Device code or
 * stored refresh token → XBL/XSTS → MC services, then hot-swaps the user.
 */
public final class MicrosoftAccountManager {

    public static volatile boolean busy;
    public static volatile String userCode;
    public static volatile String verificationUri;
    public static volatile String status;
    public static volatile String error;

    private static final MsaApplicationConfig APPLICATION_CONFIG = new MsaApplicationConfig(
            MsaConstants.JAVA_TITLE_ID,
            MsaConstants.SCOPE_TITLE_AUTH);

    private MicrosoftAccountManager() {
    }

    public static synchronized void startLogin() {
        if (busy) {
            return;
        }
        busy = true;
        error = null;
        userCode = null;
        verificationUri = null;
        status = "Contacting Microsoft...";
        Thread thread = new Thread(MicrosoftAccountManager::run, "xutil-msa-login");
        thread.setDaemon(true);
        thread.start();
    }

    private static void run() {
        try {
            AccountStore.StoredAccount stored = AccountStore.load().orElse(null);

            JavaAuthManager authManager;
            if (stored != null) {
                status = "Refreshing saved session for " + stored.name() + "...";
                try {
                    authManager = JavaAuthManager.create(MinecraftAuth.createHttpClient())
                            .msaApplicationConfig(APPLICATION_CONFIG)
                            .login(stored.refreshToken());
                } catch (Exception exception) {
                    // Refresh failed — fall back to the device-code flow.
                    AccountStore.clear();
                    status = "Saved session expired, starting device login...";
                    authManager = deviceCodeLogin();
                }
            } else {
                authManager = deviceCodeLogin();
            }

            MsaToken msaToken = authManager.getMsaToken().getUpToDate();
            MinecraftToken minecraftToken = authManager.getMinecraftToken().getUpToDate();
            MinecraftProfile profile = authManager.getMinecraftProfile().getUpToDate();

            status = "Applying session...";
            if (msaToken.getRefreshToken() != null) {
                AccountStore.save(new AccountStore.StoredAccount(
                        profile.getName(), profile.getId(), null, msaToken.getRefreshToken()));
            }

            apply(profile, minecraftToken.getToken());
            error = null;
            status = "Logged in as " + profile.getName() + " - reconnect to apply";
        } catch (Exception exception) {
            error = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
            status = null;
        } finally {
            busy = false;
        }
    }

    private static JavaAuthManager deviceCodeLogin() throws Exception {
        ParamMsaAuthServiceSupplier<Consumer<MsaDeviceCode>> supplier = DeviceCodeMsaAuthService::new;
        Consumer<MsaDeviceCode> callback = MicrosoftAccountManager::onDeviceCode;
        return JavaAuthManager.create(MinecraftAuth.createHttpClient())
                .msaApplicationConfig(APPLICATION_CONFIG)
                .login(supplier, callback);
    }

    private static void onDeviceCode(MsaDeviceCode deviceCode) {
        userCode = deviceCode.getUserCode();
        verificationUri = deviceCode.getVerificationUri();
        status = "Waiting for approval at " + verificationUri;
        try {
            Util.getPlatform().openUri(deviceCode.getDirectVerificationUri());
        } catch (Exception ignored) {
        }
    }

    private static void apply(MinecraftProfile profile, String mcAccessToken) {
        Minecraft minecraft = Minecraft.getInstance();
        User user = new User(
                profile.getName(),
                profile.getId(),
                mcAccessToken,
                Optional.empty(),
                Optional.of(MsaConstants.JAVA_TITLE_ID));

        MinecraftUserAccessor accessor = (MinecraftUserAccessor) minecraft;
        accessor.xutil$setUser(user);

        // Give the skin manager a real profile future (fetch skin/cape).
        MinecraftSessionService sessionService =
                new YggdrasilAuthenticationService(minecraft.getProxy()).createMinecraftSessionService();
        CompletableFuture<ProfileResult> profileFuture = CompletableFuture.supplyAsync(
                () -> sessionService.fetchProfile(profile.getId(), false), Util.nonCriticalIoPool());
        accessor.xutil$setProfileFuture(profileFuture);
    }
}
