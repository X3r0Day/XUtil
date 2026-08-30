package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.ModuleConfig;
import me.x3r0day.xutil.client.module.ModuleManager;import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    private static final int SEARCH_WIDTH = 110;
    private static final int BAR_HEIGHT = 20;

    private final List<WindowFrame> windows = new ArrayList<>();
    // only close on the toggle key once it has been released at least once while open,
    // so the press that opened the GUI (and key-repeat) can't instantly close it
    private boolean closeArmed;
    private float anim;
    private boolean closing;
    private long lastFrameTime;
    private EditBox searchField;
    private String query = "";

    public ClickGuiScreen() {
        super(Component.literal("XUtil"));
    }

    @Override
    protected void init() {
        searchField = new EditBox(font, 10, height - 30, SEARCH_WIDTH, BAR_HEIGHT,
            Component.literal("Search"));
        searchField.setMaxLength(32);
        searchField.setHint(Component.literal("Search..."));
        addRenderableWidget(searchField);

        windows.clear();
        int x = 10;
        int y = 10;
        int rowHeight = 0;
        for (Category category : ModuleManager.getCategories()) {
            if (x + WindowFrame.WIDTH > width - 10) {
                x = 10;
                y += rowHeight + 8;
                rowHeight = 0;
            }
            windows.add(new WindowFrame(category, x, y));
            x += WindowFrame.WIDTH + 8;
            rowHeight = Math.max(rowHeight, WindowFrame.heightFor(category, query));
        }

        Map<String, int[]> saved = ModuleConfig.loadWindowPositions();
        for (WindowFrame window : windows) {
            int[] pos = saved.get(window.getCategory().getDisplayName());
            if (pos != null) {
                window.setPosition(
                    Math.min(Math.max(0, pos[0]), width - WindowFrame.WIDTH),
                    Math.min(Math.max(0, pos[1]), height - 20));
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // delta here is a partial tick, so animate off real time instead
        long now = System.nanoTime();
        float dt = lastFrameTime == 0 ? 0f : (now - lastFrameTime) / 1_000_000_000f;
        lastFrameTime = now;

        query = searchField.getValue().trim().toLowerCase();

        anim = closing
            ? Math.max(0f, anim - dt * 7f)
            : Math.min(1f, anim + dt * 8f);

        for (int i = 0; i < windows.size(); i++) {
            float windowAnim = closing
                ? anim
                : clamp01(anim * 1.4f - i * 0.1f);
            windows.get(i).render(graphics, font, mouseX, mouseY, windowAnim, query);
        }

        if (closing) {
            super.extractRenderState(graphics, mouseX, mouseY, delta);
            return;
        }

        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).hasHoveredModule()) {
                windows.get(i).renderTooltip(graphics, font, mouseX, mouseY, width, height);
                break;
            }
        }

        int barY = height - 30;

        graphics.fill(9, barY - 1, 10 + SEARCH_WIDTH + 1, barY + BAR_HEIGHT + 1, GuiTheme.accent);
        graphics.fill(10, barY, 10 + SEARCH_WIDTH, barY + BAR_HEIGHT, 0xFF121218);

        int colorX = 10 + SEARCH_WIDTH + 8;
        boolean hoverColor = over(mouseX, mouseY, colorX, barY, 64, BAR_HEIGHT);
        graphics.fill(colorX, barY, colorX + 64, barY + BAR_HEIGHT,
            hoverColor ? 0xFF333345 : 0xE6121218);
        graphics.fill(colorX, barY, colorX + 2, barY + BAR_HEIGHT, GuiTheme.accent);
        graphics.fill(colorX + 64 - 16, barY + 4, colorX + 64 - 4, barY + BAR_HEIGHT - 4,
            GuiTheme.accent);
        graphics.centeredText(font, "Color", colorX + 24,
            barY + (BAR_HEIGHT - font.lineHeight) / 2, 0xFFFFFFFF);

        int macrosX = colorX + 72;
        boolean hoverMacros = over(mouseX, mouseY, macrosX, barY, 64, BAR_HEIGHT);
        graphics.fill(macrosX, barY, macrosX + 64, barY + BAR_HEIGHT,
            hoverMacros ? 0xFF333345 : 0xE6121218);
        graphics.fill(macrosX, barY, macrosX + 2, barY + BAR_HEIGHT, GuiTheme.accent);
        graphics.centeredText(font, "Macros", macrosX + 32,
            barY + (BAR_HEIGHT - font.lineHeight) / 2, 0xFFFFFFFF);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private static float clamp01(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
    }

    private static boolean over(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void tick() {
        if (closing && anim <= 0f) {
            Map<String, int[]> positions = new HashMap<>();
            for (WindowFrame window : windows) {
                positions.put(window.getCategory().getDisplayName(),
                    new int[]{window.getX(), window.getY()});
            }
            ModuleConfig.saveWindowPositions(positions);
            onClose();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0x2C000000);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (closing) return true;
        if (searchField.isFocused()) {
            if (event.isEscape()) {
                searchField.setFocused(false);
                return true;
            }
            return super.keyPressed(event);
        }
        if (event.key() == GLFW.GLFW_KEY_DELETE && closeArmed) {
            startClosing();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            startClosing();
            return true;
        }
        return super.keyPressed(event);
    }

    private void startClosing() {
        if (!closing) {
            closing = true;
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (searchField.isFocused()) {
            return super.keyReleased(event);
        }
        if (event.key() == GLFW.GLFW_KEY_DELETE) {
            closeArmed = true;
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return true;

        int barY = height - 30;
        int colorX = 10 + SEARCH_WIDTH + 8;
        int macrosX = colorX + 72;

        if (event.button() == 0 && over(event.x(), event.y(), 10, barY, SEARCH_WIDTH, BAR_HEIGHT)) {
            searchField.setFocused(true);
            return true;
        }
        searchField.setFocused(false);

        if (event.button() == 0 && over(event.x(), event.y(), colorX, barY, 64, BAR_HEIGHT)) {
            GuiTheme.next();
            return true;
        }
        if (event.button() == 0 && over(event.x(), event.y(), macrosX, barY, 64, BAR_HEIGHT)) {
            minecraft.gui.setScreen(new MacroListScreen(this));
            return true;
        }

        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).mouseClicked(event, query)) {
                WindowFrame window = windows.remove(i);
                windows.add(window);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (WindowFrame window : windows) {
            window.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        for (WindowFrame window : windows) {
            if (window.mouseDragged(event)) return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }
}
