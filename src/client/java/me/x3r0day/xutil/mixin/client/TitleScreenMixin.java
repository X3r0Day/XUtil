package me.x3r0day.xutil.mixin.client;

import me.x3r0day.xutil.client.addon.AddonLoader;
import me.x3r0day.xutil.client.ui.AddonWarningScreen;
import me.x3r0day.xutil.client.ui.MacroListScreen;
import me.x3r0day.xutil.client.ui.OfflineAccountScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void xutil$addAccountSwitcher(CallbackInfo ci) {
        addRenderableWidget(Button.builder(Component.literal("Offline Accounts"), button ->
                minecraft.gui.setScreen(new OfflineAccountScreen((Screen) (Object) this)))
            .bounds(width - 126, 6, 120, 20)
            .build());
        addRenderableWidget(Button.builder(Component.literal("Macros"), button ->
                minecraft.gui.setScreen(new MacroListScreen((Screen) (Object) this)))
            .bounds(width - 126, 30, 120, 20)
            .build());

        if (AddonLoader.shouldShowWarning()) {
            AddonLoader.markWarningShown();
            minecraft.gui.setScreen(new AddonWarningScreen((Screen) (Object) this));
        }
    }
}
