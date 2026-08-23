package me.x3r0day.xutil.client.ui;

import me.x3r0day.xutil.client.module.Category;
import me.x3r0day.xutil.client.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private final List<WindowFrame> windows = new ArrayList<>();
    // only close on the toggle key once it has been released at least once while open,
    // so the press that opened the GUI (and key-repeat) can't instantly close it
    private boolean closeArmed;

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
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        for (WindowFrame window : windows) {
            window.render(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // keep the world fully visible behind the GUI, like Meteor's ClickGUI
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_DELETE && closeArmed) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
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
