package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.ModuleConfig;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    private final List<WindowFrame> windows = new ArrayList<>();
    // only close on the toggle key once it has been released at least once while open,
    // so the press that opened the GUI (and key-repeat) can't instantly close it
    private boolean closeArmed;
    private float anim;
    private boolean closing;
    private long lastFrameTime;

    public ClickGuiScreen() {
        super(Component.literal("XUtil"));
    }

    @Override
    protected void init() {
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
            rowHeight = Math.max(rowHeight, WindowFrame.heightFor(category));
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

        anim = closing
            ? Math.max(0f, anim - dt * 7f)
            : Math.min(1f, anim + dt * 8f);

        for (int i = 0; i < windows.size(); i++) {
            float windowAnim = closing
                ? anim
                : clamp01(anim * 1.4f - i * 0.1f);
            windows.get(i).render(graphics, font, mouseX, mouseY, windowAnim);
        }

        if (closing) return;
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).hasHoveredModule()) {
                windows.get(i).renderTooltip(graphics, font, mouseX, mouseY, width, height);
                break;
            }
        }
    }

    private static float clamp01(float value) {
        return value < 0f ? 0f : Math.min(value, 1f);
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
        if (event.key() == GLFW.GLFW_KEY_DELETE) {
            closeArmed = true;
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return true;
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).mouseClicked(event)) {
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
