package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.account.MicrosoftAccountManager;
import me.x3r0day.xutil.client.account.OfflineAccountManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class OfflineAccountScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 170;
    private static final int COLOR_PANEL = 0xEE121218;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFAAAAAF;
    private static final int COLOR_SUCCESS = 0xFF62E67D;
    private static final int COLOR_ERROR = 0xFFFF6B6B;

    private final Screen parent;
    private EditBox usernameField;
    private String status = "The new profile is used on your next connection.";
    private boolean success;
    private boolean error;
    private Button copyButton;
    private String lastCode;

    public OfflineAccountScreen(Screen parent) {
        super(Component.literal("Account Switcher"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;

        usernameField = new EditBox(
            font,
            x + 16,
            y + 47,
            PANEL_WIDTH - 32,
            20,
            Component.literal("Offline username")
        );
        usernameField.setMaxLength(16);
        usernameField.setValue(minecraft.getUser().getName());
        addRenderableWidget(usernameField);

        addRenderableWidget(Button.builder(Component.literal("Switch Account"), button -> switchAccount())
            .bounds(x + 16, y + 78, 130, 20)
            .build());
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
            .bounds(x + PANEL_WIDTH - 146, y + 78, 130, 20)
            .build());
        addRenderableWidget(Button.builder(Component.literal("Login with Microsoft"), button -> microsoftLogin())
            .bounds(x + 16, y + 104, PANEL_WIDTH - 32, 20)
            .build());
        setInitialFocus(usernameField);
    }

    @Override
    public void tick() {
        super.tick();
        // Pull in the async login state on the render thread.
        String code = MicrosoftAccountManager.userCode;
        if (code != null && !code.equals(lastCode)) {
            if (copyButton != null) {
                removeWidget(copyButton);
            }
            int x = (width - PANEL_WIDTH) / 2;
            int y = (height - PANEL_HEIGHT) / 2;
            copyButton = addRenderableWidget(Button.builder(
                    Component.literal("Copy code: " + code), button -> copyCode())
                .bounds(x + 16, y + 130, PANEL_WIDTH - 32, 20)
                .build());
            lastCode = code;
        } else if (code == null && copyButton != null) {
            removeWidget(copyButton);
            copyButton = null;
            lastCode = null;
        }

        if (MicrosoftAccountManager.error != null) {
            status = MicrosoftAccountManager.error;
            success = false;
            error = true;
            MicrosoftAccountManager.error = null;
        } else if (MicrosoftAccountManager.status != null) {
            status = MicrosoftAccountManager.status;
            success = false;
            error = false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;

        graphics.fill(0, 0, width, height, 0x80000000);
        graphics.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + PANEL_HEIGHT + 1, GuiTheme.accent);
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, COLOR_PANEL);
        graphics.centeredText(font, "Account Switcher", width / 2, y + 9, COLOR_TEXT);
        graphics.centeredText(font, "Current: " + minecraft.getUser().getName(),
            width / 2, y + 24, COLOR_MUTED);
        graphics.text(font, "Offline Username", x + 16, y + 37, COLOR_TEXT, false);
        int statusColor = success ? COLOR_SUCCESS : error ? COLOR_ERROR : COLOR_MUTED;
        graphics.centeredText(font, status, width / 2, y + 154, statusColor);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
            switchAccount();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    private void switchAccount() {
        try {
            OfflineAccountManager.Account account = OfflineAccountManager.switchTo(usernameField.getValue().trim());
            status = "Switched to " + account.username() + " - reconnect to apply";
            success = true;
            error = false;
            if (minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.literal(
                    "[XUtil] Offline account set to " + account.username()
                        + " (" + account.uuid() + "). Reconnect to apply."));
            }
        } catch (IllegalArgumentException exception) {
            status = exception.getMessage();
            success = false;
            error = true;
        }
    }

    private void microsoftLogin() {
        if (MicrosoftAccountManager.busy) {
            return;
        }
        status = "Starting Microsoft login...";
        success = false;
        error = false;
        MicrosoftAccountManager.startLogin();
    }

    private void copyCode() {
        String code = MicrosoftAccountManager.userCode;
        if (code != null) {
            minecraft.keyboardHandler.setClipboard(code);
            status = "Code copied - open " + (MicrosoftAccountManager.verificationUri != null
                ? MicrosoftAccountManager.verificationUri : "aka.ms/devicelogin") + " and sign in";
            success = false;
            error = false;
        }
    }
}
